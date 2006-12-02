package isj;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import map.MapPanel;
import map.UTMUtil;
import web.WebUtilities;

/**
 * 街区レベル位置参照情報を取得するユーティリティクラスです。
 * @author zenjiro
 * 2005/12/03
 */
public class ISJUtil {
	/**
	 * 古い展開済みファイルの接尾語
	 */
	public static final String OLD_CSV_SUFFIX = "_2003.csv";

	/**
	 * 古い圧縮ファイルの接尾語
	 */
	public static final String OLD_ZIP_SUFFIX = "-02.0a.zip";

	/**
	 * 街区レベル位置参照情報の配布URL
	 */
	public static final String BASE_URL = "http://nlftp.mlit.go.jp/isj/dls/data/";

	/**
	 * キャッシュディレクトリの相対パス
	 */
	public static final String CACHE_DIR = ".map" + File.separator + "isj";

	/**
	 * 圧縮ファイルの接頭語
	 */
	public static final String ZIP_PREFIX = "03.0a/";

	/**
	 * 圧縮ファイルの接尾語
	 */
	public static final String ZIP_SUFFIX = "-03.0a.zip";

	/**
	 * 展開済みファイルの接尾語
	 */
	public static final String CSV_SUFFIX = "_2004.csv";

	/**
	 * 古い圧縮ファイルの接頭語
	 */
	public static final String OLD_ZIP_PREFIX = "02.0a/";

	/**
	 * キャッシュファイルの接頭語
	 */
	public static final String CACHE_PREFIX = "isj_";

	/**
	 * キャッシュファイルの接尾語
	 */
	public static final String CACHE_SUFFIX = ".csv";

	/**
	 * 街区レベル位置参照情報をダウンロードし、読み込みます。
	 * @param id 市区町村コード
	 * @param panel 地図を描画するパネル
	 * @return 街区レベル位置参照情報
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public static Map<String, Point2D> loadIsj(final String id, final MapPanel panel) throws IOException,
			FileNotFoundException {
		final Map<String, Point2D> ret = new ConcurrentHashMap<String, Point2D>();
		File csvFile = new File(ISJUtil.CACHE_DIR + File.separator + id + ISJUtil.CSV_SUFFIX);
		final File oldCsvFile = new File(ISJUtil.CACHE_DIR + File.separator + id + ISJUtil.OLD_CSV_SUFFIX);
		final URL url = new URL(ISJUtil.BASE_URL + ISJUtil.ZIP_PREFIX + id + ISJUtil.ZIP_SUFFIX);
		final URL oldUrl = new URL(ISJUtil.BASE_URL + ISJUtil.OLD_ZIP_PREFIX + id + ISJUtil.OLD_ZIP_SUFFIX);
		final String cacheFile = ISJUtil.CACHE_DIR + File.separator + ISJUtil.CACHE_PREFIX + id + ISJUtil.CACHE_SUFFIX;
		if (!new File(cacheFile).exists()) {
			if (csvFile.exists()) {
				// 平成16年の展開済みファイルがあるとき
			} else {
				if (oldCsvFile.exists()) {
					// 平成15年の展開済みファイルがあるとき
					csvFile = oldCsvFile;
				} else {
					final File cacheDir = new File(ISJUtil.CACHE_DIR);
					if (!cacheDir.exists()) {
						cacheDir.mkdir();
					}
					try {
						url.openStream();
						// 平成16年の圧縮ファイルをダウンロードできるとき
						final File file = new File(ISJUtil.CACHE_DIR + File.separator + id + ISJUtil.ZIP_SUFFIX);
						file.createNewFile();
						panel.addMessage(url + "をダウンロードしています。");
						get(url, file);
						panel.removeMessage();
						csvFile = new File(ISJUtil.CACHE_DIR + File.separator + id + ISJUtil.CSV_SUFFIX);
					} catch (final FileNotFoundException e) {
						try {
							System.out.println("WARNING: failed to get " + url);
							oldUrl.openStream();
							// 平成15年の圧縮ファイルをダウンロードできるとき
							final File file = new File(ISJUtil.CACHE_DIR + File.separator + id + ISJUtil.OLD_ZIP_SUFFIX);
							file.createNewFile();
							panel.addMessage(oldUrl + "をダウンロードしています。");
							get(oldUrl, file);
							panel.removeMessage();
							csvFile = new File(ISJUtil.CACHE_DIR + File.separator + id + ISJUtil.OLD_CSV_SUFFIX);
						} catch (final FileNotFoundException e1) {
							System.out.println("WARNING: failed to get " + oldUrl);
							e1.printStackTrace();
						}
					}
				}
			}
			{
				panel.addMessage(csvFile + "の座標系を変換しています。");
				final PrintWriter out = new PrintWriter(new File(cacheFile), "SJIS");
				final Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(csvFile), "SJIS"));
				boolean isFirst = true;
				while (scanner.hasNextLine()) {
					final String line = scanner.nextLine();
					if (isFirst) {
						isFirst = false;
					} else {
						final Scanner scanner2 = new Scanner(line);
						scanner2.useDelimiter(",");
						final StringBuilder string = new StringBuilder();
						string.append(scanner2.next().replaceAll("\"", "") + ",");
						string.append(scanner2.next().replaceAll("\"", "") + ",");
						string.append(scanner2.next().replaceAll("\"", "").replaceFirst("二十丁目$", "20-").replaceFirst(
								"十九丁目$", "19-").replaceFirst("十八丁目$", "18-").replaceFirst("十七丁目$", "17-").replaceFirst(
								"十六丁目$", "16-").replaceFirst("十五丁目$", "15-").replaceFirst("十四丁目$", "14-").replaceFirst(
								"十三丁目$", "13-").replaceFirst("十二丁目$", "12-").replaceFirst("十一丁目$", "11-").replaceFirst(
								"十丁目$", "10-").replaceFirst("九丁目$", "9").replaceFirst("八丁目$", "8-").replaceFirst(
								"七丁目$", "7-").replaceFirst("六丁目$", "6-").replaceFirst("五丁目$", "5-").replaceFirst(
								"四丁目$", "4-").replaceFirst("三丁目$", "3-").replaceFirst("二丁目$", "2-").replaceFirst(
								"一丁目$", "1-")
								+ ",");
						string.append(scanner2.next().replaceAll("\"", ""));
						scanner2.next();
						scanner2.next();
						scanner2.next();
						final String latitude = scanner2.next();
						final String longitude = scanner2.next();
						scanner2.next();
						final int rep = scanner2.nextInt();
						if (rep == 1) {
							if (longitude.length() == 10 && latitude.length() == 9) {
								final Point2D point = UTMUtil.toUTM(Double.parseDouble(longitude), -Double
										.parseDouble(latitude));
								out.println(string.toString() + "," + point.getX() + "," + point.getY());
								//ret.put(string.toString(), point);
							} else {
								System.out.println("WARNING: invalid longitude or latitude: " + line);
							}
						}
					}
				}
				scanner.close();
				out.close();
				panel.removeMessage();
			}
		}
		{
			final Scanner scanner = new Scanner(new File(cacheFile), "SJIS");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final String[] items = line.split(",");
				if (items.length == 6) {
					ret.put(items[0] + "," + items[1] + "," + items[2] + "," + items[3], new Point2D.Double(Double
							.parseDouble(items[4]), Double.parseDouble(items[5])));
				} else {
					System.out.println("WARNING: invalid isj line: " + line);
				}
			}
			scanner.close();
		}
		return ret;
	}

	/**
	 * ZIPファイルを取得して展開します。
	 * @param url URL
	 * @param file 出力ファイル
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ZipException
	 */
	private static void get(final URL url, final File file) throws IOException, FileNotFoundException, ZipException {
		WebUtilities.copy(url.openStream(), new FileOutputStream(file));
		final ZipFile zipFile = new ZipFile(file);
		for (final Enumeration<? extends ZipEntry> enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
			final ZipEntry entry = enumeration.nextElement();
			if (entry.getName().endsWith(".csv")) {
				WebUtilities.copy(zipFile.getInputStream(entry), new FileOutputStream(ISJUtil.CACHE_DIR
						+ File.separator + new File(entry.getName())));
			}
		}
	}

	/**
	 * アドレスマッチングを行い、住所、店舗名、x座標、y座標をカンマ区切りで出力ストリームに書き出します。
	 * @param addresses 住所と店舗名の対応表
	 * @param out 出力ストリーム
	 * @param isj 街区レベル位置参照情報
	 */
	public static void parseAddresses(final Map<String, String> addresses, final PrintWriter out,
			final Map<String, Point2D> isj) {
		final Pattern pattern = Pattern.compile("([^-]+-[0-9]+)-");
		final Pattern pattern3 = Pattern.compile("(.+)字[^0-9字]+([0-9]+)番[0-9]+$");
		final Pattern pattern4 = Pattern.compile("(.+)字[^0-9字]+([0-9]+)$");
		for (final Map.Entry<String, String> entry3 : addresses.entrySet()) {
			String address = entry3.getKey().replace("−", "-").replace("ー", "-").replace("‐", "-").replace("—", "-")
					.replace("０", "0").replace("１", "1").replace("２", "2").replace("３", "3").replace("４", "4").replace(
							"５", "5").replace("６", "6").replace("７", "7").replace("８", "8").replace("９", "9").replace(
							"一丁目", "1丁目").replace("二丁目", "2丁目").replace("三丁目", "3丁目").replace("四丁目", "4丁目").replace(
							"五丁目", "5丁目").replace("六丁目", "6丁目").replace("七丁目", "7丁目").replace("八丁目", "8丁目").replace(
							"九丁目", "9丁目");
			final String caption = entry3.getValue().replaceAll(",", "");
			final Matcher matcher = pattern.matcher(address);
			if (matcher.find()) {
				address = matcher.group(1);
			}
			if (isj.containsKey(address)) {
			} else {
				address = address.replaceFirst("-.+", "");
				if (isj.containsKey(address)) {
				} else {
					address = ISJUtil.replaceTyomeToHyphen(address, "([^0-9]+[0-9]+)丁目([0-9]+)");
					if (isj.containsKey(address)) {
					} else {
						final String address2 = address.replace("ケ丘", "ヶ丘");
						if (isj.containsKey(address2)) {
							address = address2;
						} else {
							if (address.matches(".+[0-9]+番[0-9]+号(　.+)?$")) {
								address = address.replaceAll("番[0-9]+号(　.+)?", "");
							}
							if (isj.containsKey(address)) {
							} else {
								final Matcher matcher3 = pattern3.matcher(address);
								if (matcher3.matches()) {
									address = matcher3.group(1) + matcher3.group(2);
								}
								if (isj.containsKey(address)) {
								} else {
									final Matcher matcher4 = pattern4.matcher(address);
									if (matcher4.matches()) {
										address = matcher4.group(1) + matcher4.group(2);
									}
									if (isj.containsKey(address)) {
									} else {
										final String address3 = address.replaceAll("番地?の?[0-9]+$", "");
										if (isj.containsKey(address3)) {
											address = address3;
										} else {
											final String address4 = address.replaceAll("番地$", "");
											if (isj.containsKey(address4)) {
												address = address4;
											} else {
												continue;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			final Point2D point = isj.get(address);
			address = address.replaceAll(",", "");
			out.println(address + "," + caption + "," + point.getX() + "," + point.getY());
		}
	}

	/**
	 * 「(aaa)bbb(ccc)ddd」を「aaa-ccc」に置換します。
	 * @param string 置換前の文字列
	 * @param regex 正規表現。丸括弧が2つあるべきです。
	 * @return 置換後の文字列
	 */
	private static String replaceTyomeToHyphen(final String string, final String regex) {
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(string);
		if (matcher.find()) {
			return matcher.group(1) + "-" + matcher.group(2);
		} else {
			return string;
		}
	}
}
