import com.dropbox.core.*;
import org.dom4j.DocumentException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

public class TheTest {

    @Test
    public void test() throws IOException, DbxException, DocumentException {
        HDOut out = new HDOut();
        out.auth("sw", "");
        Integer unseen = out.unseen();
        if (unseen != null) {
            HDOut.Episode ep = out.episode(unseen);
            System.out.println(ep);
        }
//        DbxRequestConfig config = new DbxRequestConfig("JavaTutorial/1.0", Locale.getDefault().toString());
//        DbxClient client = new DbxClient(config, "");
//        URL url = new URL("http://ya.ru/");
//        InputStream stream = url.openStream();
//        try {
//            DbxEntry.File uploadedFile = client.uploadFile("/ya.txt", DbxWriteMode.force(), -1, stream);
//            System.out.println("Uploaded: " + uploadedFile.toString());
//        } finally {
//            stream.close();
//        }
    }

}
