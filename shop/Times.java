package shop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * タイムズの住所を取得するクラスです。
 * @author zenjiro
 * @since 4.04
 * 2006/07/17
 */
public class Times implements AddressParser {
	/**
	 * 最初のURL
	 */
	private static final String URL = "http://map.times-info.net/map/spot_addr.php?kind=1&key=";

	/**
	 * 最後のURL
	 */
	private static final String URL2 = "&lk=0&pg=1";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "EUC-JP";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "times_";

	/**
	 * @since 4.09
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			final Pattern pattern1 = Pattern.compile("<p>([^0-9a-zA-Z]+[^<>]+)</p>");
			final Pattern pattern2 = Pattern.compile("<p><a href=[^<>]+>([^<>]+)</a></p>");
			String shopName = null;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				//System.out.println("Times: line = " + line);
				final Matcher matcher1 = pattern1.matcher(line);
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher1.find()) {
					final String address = matcher1.group(1);
					if (!address.startsWith("検索結果") && !address.startsWith("住所") && !address.equals("駐車場名称")
							&& !address.equals("満空情報") && !address.equals("時間貸台数") && !address.equals("料金情報")
							&& !address.equals("住所")) {
						if (shopName != null) {
							ret.put(matcher1.group(1), shopName);
							shopName = null;
						}
					}
				} else if (matcher2.find()) {
					shopName = matcher2.group(1);
				}
			}
			scanner.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * @since 4.09
	 */
	public String getEncoding() {
		return ENCODING;
	}

	/**
	 * @since 4.09
	 */
	public String getLabel(final String shopName) {
		return "タイムズ";
	}

	/**
	 * @since 4.09
	 */
	public String getPrefix() {
		return PREFIX;
	}

	/**
	 * @since 4.09
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		return URL + URLEncoder.encode(prefectureLabel + cityLabel, Times.ENCODING) + URL2;
	}
}
