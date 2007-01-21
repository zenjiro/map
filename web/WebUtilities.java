package web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.Const;

/**
 * ネットワーク関係のユーティリティクラスです。
 * @author zenjiro
 * Created on 2005/05/07 17:48:17
 */
public class WebUtilities {

    /**
     * 数値地図2500（空間データ基盤）のダウンロードページを解析して、市区町村名と圧縮ファイルの対応表を取得します。
     * @return 市区町村名と圧縮ファイルの対応表。内容は Map<都道府県名, Map<市区町村名, 圧縮ファイル名>>
     * @throws IOException 入出力例外 
     */
    public static Map<String, Map<String, String>> getFileList()
            throws IOException {
        final Map<String, Map<String, String>> ret = new ConcurrentHashMap<String, Map<String, String>>();
        final String baseURL = Const.SDF2500.BASE_URL;
        final String topPage = "search.html";
        final String encoding = "SJIS";
        final Scanner scanner = new Scanner(new InputStreamReader(new URL(
                baseURL + topPage).openStream(), encoding));
        final Map<String, String> prefectures = new ConcurrentHashMap<String, String>();
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            //System.out.println("DEBUG: line = " + line);
            final Pattern pattern = Pattern
                    .compile(".*<A href=\"([a-z]+\\.html)\">(.+)</A>.*");
            final Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                if (matcher.groupCount() == 2) {
                    prefectures.put(matcher.group(2), matcher.group(1));
                }
            }
        }
        scanner.close();
        //System.out.println("DEBUG: prefectures = " + prefectures);
        for (final Map.Entry<String, String> entry : prefectures.entrySet()) {
            final Map<String, String> cities = new ConcurrentHashMap<String, String>();
            boolean isMajorCity = false;
            String majorCityName = null;
            boolean isOsaka = false;
            String osakaCityName = null;
            final Scanner scanner2 = new Scanner(new InputStreamReader(new URL(
                    baseURL + entry.getValue()).openStream(), encoding));
            while (scanner2.hasNextLine()) {
                final String line = scanner2.nextLine();
                final Pattern majorCityPattern = Pattern
                        .compile("<tr><td +rowspan=\"[0-9]+\">(.+) </td>");
                final Matcher majorCityMatcher = majorCityPattern.matcher(line);
                if (majorCityMatcher.matches()) {
                    if (majorCityMatcher.groupCount() == 1) {
                        isMajorCity = true;
                        majorCityName = majorCityMatcher.group(1);
                        //System.out.println("DEBUG: majorCityName = " + majorCityName);
                    }
                }
                final Pattern osakaPattern = Pattern
                        .compile("<TITLE>大阪府市町村一覧表</TITLE>");
                final Matcher osakaMatcher = osakaPattern.matcher(line);
                if (osakaMatcher.matches()) {
                    isOsaka = true;
                    majorCityName = "大阪市";
                }
                if (isMajorCity) {
                    final Pattern pattern = Pattern
                            .compile(".*<td>(.+)</td><td><input type=\"button\" onClick=jumpStartpage\\(\"[0-9]+\"\\) value=\"サンプル\"></td><td align=\"right\"><a href=\"data25k/.+/[0-9]+\\.lzh\">[0-9]+byte</a></td><td align=\"right\"><a href=\"(data2500/[a-z]+/[0-9]+\\.lzh)\">[0-9]+byte</a></td>");
                    final Matcher matcher = pattern.matcher(line);
                    //System.out.println("DEBUG: line = " + line);
                    if (matcher.matches()) {
                        if (matcher.groupCount() == 2) {
                            final String label = majorCityName
                                    + matcher.group(1);
                            final String file = matcher.group(2);
                            cities.put(label, file);
                        }
                    } else {
                        final Pattern pattern2 = Pattern
                                .compile("<tr><td colspan=\"2\">(.+)</td><td><input type=\"button\" onClick=jumpStartpage\\(\"[0-9]+\"\\) value=\"サンプル\"></td><td align=\"right\"><a href=\"data25k/.+/[0-9]+\\.lzh\">[0-9]+byte</a></td><td align=\"right\"><a href=\"(data2500/[a-z]+/[0-9]+\\.lzh)\">[0-9]+byte</a></td>");
                        final Matcher matcher2 = pattern2.matcher(line);
                        if (matcher2.matches()) {
                            if (matcher2.groupCount() == 2) {
                                final String label = matcher2.group(1);
                                final String file = matcher2.group(2);
                                cities.put(label, file);
                            }
                        }
                    }
                } else if (isOsaka) {
                    final Pattern pattern = Pattern
                            .compile(".*<TD VALIGN=\"TOP\" WIDTH=\"77\" STYLE=\"font-size:1em\">(.+)</TD>");
                    final Matcher matcher = pattern.matcher(line);
                    //System.out.println("DEBUG: line = " + line);
                    if (matcher.matches()) {
                        if (matcher.groupCount() == 1) {
                            osakaCityName = majorCityName + matcher.group(1);
                        }
                    } else {
                        final Pattern pattern2 = Pattern
                                .compile(".*<TD VALIGN=\"TOP\" COLSPAN=\"2\" WIDTH=\"[0-9]+\" STYLE=\"font-size:1em\">(.+)</TD>");
                        final Matcher matcher2 = pattern2.matcher(line);
                        if (matcher2.matches()) {
                            if (matcher2.groupCount() == 1) {
                                osakaCityName = matcher2.group(1);
                            }
                        } else {
                            final Pattern pattern3 = Pattern
                                    .compile(".*<TD ALIGN=\"RIGHT\" VALIGN=\"TOP\" WIDTH=\"[0-9]+\" STYLE=\"font-size:1em\"><A HREF=\"(data2500/[a-z]+/[0-9]+.lzh)\">[0-9]+byte</A></TD>");
                            final Matcher matcher3 = pattern3.matcher(line);
                            if (matcher3.matches()) {
                                if (matcher3.groupCount() == 1) {
                                    final String file = matcher3.group(1);
                                    cities.put(osakaCityName, file);
                                    //System.out.println("DEBUG: osakaCityName = " + osakaCityName + ", file = " + file);
                                }
                            }
                        }
                    }
                } else {
                    final Pattern pattern = Pattern
                            .compile("<tr><td>(.+)</td><td><input type=\"button\" onClick=jumpStartpage\\(\"[0-9]+\"\\) value=\"サンプル\"></td><td align=\"right\"><a href=\"data25k/[a-z]+/[0-9]+\\.lzh\">[0-9]+byte</a></td><td align=\"right\"><a href=\"(data2500/[a-z]+/[0-9]+\\.lzh)\">[0-9]+byte</a></td>");
                    final Matcher matcher = pattern.matcher(line);
                    //System.out.println("DEBUG: line = " + line);
                    if (matcher.matches()) {
                        if (matcher.groupCount() == 2) {
                            final String label = matcher.group(1);
                            final String file = matcher.group(2);
                            cities.put(label, file);
                            //System.out.println("DEBUG: label = " + label + ", file = " + file);
                        }
                    }
                }
            }
            scanner2.close();
            ret.put(entry.getKey(), cities);
        }
        return ret;
    }

    /**
     * 数値地図2500（空間データ基盤）のダウンロードページを解析して取得した市区町村名と圧縮ファイルの対応表をファイルに保存します。
     * @param file 保存するファイル
     * @throws IOException 入出力例外
     */
    public static void saveFileList(final File file) throws IOException {
        final Map<String, Map<String, String>> prefectures = WebUtilities
                .getFileList();
        final PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), "SJIS"));
        out.println("#都道府県名,地区町村名,ファイル名");
        for (final Map.Entry<String, Map<String, String>> entry : prefectures
                .entrySet()) {
            final String prefecture = entry.getKey();
            for (final Map.Entry<String, String> entry2 : entry.getValue()
                    .entrySet()) {
                final String city = entry2.getKey();
                final String filename = entry2.getValue();
                out.println(prefecture + "," + city + "," + filename);
            }
        }
        out.close();
    }

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
