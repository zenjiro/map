package web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ネットワーク関係のユーティリティクラスです。
 * @author zenjiro
 * Created on 2005/05/07 17:48:17
 */
public class WebUtilities {

    /**
     * 市区町村名と圧縮ファイルの対応表をファイルから読み込みます。
     * @param file 保存されたファイル
     * @return 市区町村名と圧縮ファイルの対応表。内容は Map<都道府県名, Map<市区町村名, 圧縮ファイル名>>
     * @throws IOException 入出力例外
     */
    public static Map<String, Map<String, String>> loadFileList(final URL file)
            throws IOException {
        final Map<String, Map<String, String>> ret = new ConcurrentHashMap<String, Map<String, String>>();
        final Scanner scanner = new Scanner(new InputStreamReader(
                file.openStream(), "SJIS"));
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] data = line.split(",");
            if (data.length == 3) {
                final String prefecture = data[0];
                final String city = data[1];
                final String filename = data[2];
                if (!prefecture.startsWith("#")) {
                    if (!ret.containsKey(prefecture)) {
                        ret
                                .put(prefecture,
                                        new ConcurrentHashMap<String, String>());
                    }
                    ret.get(prefecture).put(city, filename);
                }
            }
        }
        scanner.close();
        return ret;
    }

    /**
     * ストリームを使ってファイルをコピーします。
     * @param in 入力ストリーム
     * @param out 出力ストリーム
     * @throws IOException 入出力例外 
     */
    public static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte buf[] = new byte[1024];
        int size;
        while ((size = in.read(buf)) != -1) {
            out.write(buf, 0, size);
        }
        out.close();
    }
    
}
