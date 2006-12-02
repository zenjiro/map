package shop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ローソンの住所を取得するクラスです。
 * @author zenjiro
 * @since 3.16
 * 2005/12/04
 */
public class Lawson implements AddressParser {
	/**
	 * 最初のURL
	 */
	private static final String URL1 = "http://map.lawson.co.jp/c/f/?uc=106&admi2=";

	/**
	 * 次のURL
	 */
	private static final String URL2 = "&admi3=";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "lawson_";

	/**
	 * @since 4.07
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			String caption = null;
			final Pattern pattern = Pattern.compile("<a href=\"[^<>]+\">([^<>]+)</a>");
			final Pattern pattern2 = Pattern.compile("<td class=\".+\">([^<>]+)</td>");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					caption = matcher.group(1);
				}
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					if (caption != null) {
						ret.put(matcher2.group(1), caption);
					}
				}
			}
			scanner.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * @since 4.07
	 */
	public String getEncoding() {
		return ENCODING;
	}

	/**
	 * @since 4.07
	 */
	public String getLabel(final String shopName) {
		return "ローソン";
	}

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return PREFIX;
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) {
		return URL1 + cityID.substring(0, 2) + URL2 + cityID;
	}
}
