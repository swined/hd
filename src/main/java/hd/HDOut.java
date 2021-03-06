package hd;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class HDOut implements Closeable {

    private static final String BASE = "http://hdout.tv/";
    private final CloseableHttpClient client = HttpClientBuilder
            .create()
            .setDefaultRequestConfig(RequestConfig
                .custom()
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000)
                .build()
            )
            .build();

    public static class Episode {

        public final String title;
        public final String name;
        public final int season;
        public final int episode;
        public final String url;


        public Episode(String title, String name, int season, int episode, String url) {
            this.title = title;
            this.name = name;
            this.season = season;
            this.episode = episode;
            this.url = url;
        }

        @Override
        public String toString() {
            return String.format("[%s S%02dE%02d %s : %s]", title, season, episode, name, url);
        }

        public String filename() {
            return String.format("%s S%02dE%02d %s.mp4", fix(title), season, episode, fix(name));
        }

        private static String fix(String str) {
            return str.replaceAll("[:\\/]", ".");
        }

    }

    public void auth(String login, String password) throws IOException {
        get(RequestBuilder.post()
                .setUri(BASE)
                .addParameter("login", login)
                .addParameter("password", password)
                .addParameter("iapp", "1")
                .build());
    }

    @SuppressWarnings("unchecked")
    public Integer unseen() throws IOException, DocumentException {
        for (Node node : (List<Node>) new SAXReader().read(new StringReader(get(RequestBuilder.get().setUri(BASE + "List/my/XML/").build()))).selectNodes("/document/fp[@id='my']/serieslist/item[@allseen='0']/episodes/eitem")) {
            Number vposp = node.numberValueOf("vposp");
            if (vposp == null || vposp.intValue() < 90)
                return node.numberValueOf("id_episodes").intValue();
        }
        return null;
    }

    public void markAsSeen(int id) throws IOException {
        get(RequestBuilder.get().setUri(BASE + "?usecase=MarkEpisode&id=" + id).build());
    }

    public Episode episode(int id) throws IOException, DocumentException {
        Document xml = new SAXReader().read(new StringReader(get(RequestBuilder.get().setUri(BASE + "EpisodeLink/" + id + "/XML/").build())));
        if (xml.selectSingleNode("/flashdocument/error[@type='nomoney']") != null)
            throw new IllegalStateException("no money");
        return new Episode(
                xml.valueOf("/flashdocument/item/seriesitem/etitle"),
                xml.valueOf("/flashdocument/item/etitle"),
                xml.numberValueOf("/flashdocument/item/snum").intValue(),
                xml.numberValueOf("/flashdocument/item/enum").intValue(),
                xml.valueOf("/flashdocument/item/videourl")
        );
    }

    private String get(HttpUriRequest request) throws IOException {

        try (CloseableHttpResponse response = client.execute(request)) {
            String html = IOUtils.toString(response.getEntity().getContent());
            if (html.contains("<form id=\"loginform\""))
                throw new IllegalStateException("not logged in");
            return html;
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

}
