import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 *
 * @author ponkotuy, b-wind
 * Date: 15/03/09.
 */
public class Main {
    private static Logger log = LoggerFactory.getLogger("com.ponkotuy.update");

    public static void main(String[] args) throws Exception {
        try {
            List<String> urls = getProperties("update.properties");
            for(String uStr : urls) {
                URL url = new URL(uStr);
                Path dst = Paths.get(url.getPath()).getFileName();
                if(Files.exists(dst)) {
                    log.info(dst.getFileName() + "の更新をチェックします。");
                }else{
                    log.info(dst.getFileName() + "は存在しません。ダウンロードします。");
                }
                URLConnection conn = Connection.withRedirect(url, getLastModified(dst));
                if(conn == null) {
                    log.info(dst.getFileName() + "に変更はありません");
                } else {
                    Connection.download(conn, dst);
                    log.info(dst.getFileName() + "のダウンロードが完了しました");
                }
            }
        } catch(MalformedURLException e) {
            log.error("おやっ、URLの書式に異常です！ぽんこつさんが悪いです！");
            System.exit(1);
        } catch(IOException e) {
            log.error("おやっ、IOExceptionです！",e);
            System.exit(1);
        } catch(GeneralSecurityException e) {
            log.error("おやっ、SecurityException です！",e);
            System.exit(1);
        }
    }

    private static List<String> getProperties(String fName) throws IOException {
        FileSystem fs = FileSystems.getDefault();
        Path path = fs.getPath(fName);
        Properties p = new Properties();
        try(InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
            p.load(is);
        }
        List<String> result = new ArrayList<>(p.size());
        for(Object prop : p.keySet()) {
            String key = (String) prop;
            result.add(p.getProperty(key));
        }
        return result;
    }

    private static long getLastModified(Path dst) {
        FileTime fTime;
        try {
            fTime = Files.getLastModifiedTime(dst, NOFOLLOW_LINKS);
            return fTime.toMillis();
        } catch(IOException | NullPointerException e) {
            return 0L;
        }
    }
}
