package hd;

import com.dropbox.core.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sync {

    private final Logger log = Logger.getLogger(getClass().getName());
    private final String login;
    private final String password;
    private final DbxClient client;

    public Sync(String login, String password, String token) {
        this.login = login;
        this.password = password;
        this.client = new DbxClient(new DbxRequestConfig("DbOut/1.0", Locale.getDefault().toString()), token);
    }

    @Scheduled(fixedDelay = 15 * 60 * 1000)
    private void sync() {
        try (HDOut hd = new HDOut()) {
            hd.auth(login, password);
            Integer unseen = hd.unseen();
            if (unseen != null) {
                HDOut.Episode ep = hd.episode(unseen);
                log.info(ep.filename());
                if (client.getMetadata("/" + ep.filename()) == null)
                    upload(new URL(ep.url), "/" + ep.filename());
                else
                    log.info("already exists");
                hd.markAsSeen(unseen);
                log.info("done");
            }
        } catch (Throwable e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private DbxEntry.File upload(URL url, String name) throws IOException, DbxException {
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        try (InputStream stream = conn.getInputStream()) {
            return client.uploadFile(name, DbxWriteMode.force(), -1, stream);
        }
    }

}
