package hd;

import com.dropbox.core.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sync {

    private final Logger log = Logger.getLogger(getClass().getName());
    private final String login;
    private final String password;
    private final String token;

    public Sync(String login, String password, String token) {
        this.login = login;
        this.password = password;
        this.token = token;
    }

    @Scheduled(fixedDelay = 15 * 60 * 1000)
    private void sync() {
        log.info("starting sync");
        try (HDOut hd = new HDOut()) {
            hd.auth(login, password);
            log.info("checking for unseen episodes");
            Integer unseen = hd.unseen();
            if (unseen != null) {
                log.info("fetching episode info");
                HDOut.Episode ep = hd.episode(unseen);
                log.info("uploading " + ep);
                upload(new URL(ep.url), "/" + ep.filename());
                log.info("marking episode as seen");
                hd.markAsSeen(unseen);
            }
        } catch (Throwable e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        log.info("sync done");
    }

    private DbxEntry.File upload(URL url, String name) throws IOException, DbxException {
        DbxRequestConfig config = new DbxRequestConfig("DbOut/1.0", Locale.getDefault().toString());
        DbxClient client = new DbxClient(config, token);
        try (InputStream stream = url.openStream()) {
            return client.uploadFile(name, DbxWriteMode.force(), -1, stream);
        }
    }

}
