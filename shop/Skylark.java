package shop;

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
 * すかいらーくの住所を取得するクラスです。
 * @author zenjiro
 * @since 3.16
 * 2005/12/04
 */
public class Skylark implements AddressParser {
	/**
	 * 最初のURL
	 */
	private static final String URL1 = "http://www2.info-mapping.com/skylark/map/list2.asp?chihou=";

	/**
	 * 次のURL
	 */
	private static final String URL2 = "&ctyname=";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "skylark_";

	/**
	 * @since 4.09
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
		final Pattern pattern1 = Pattern.compile("<font class=\"shopname\"><b>([^<>]+)</b></font>");
		final Pattern pattern2 = Pattern.compile("住所：([^<>]+)");
		String caption = null;
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			final Matcher matcher1 = pattern1.matcher(line);
			if (matcher1.find()) {
				caption = matcher1.group(1);
			}
			final Matcher matcher2 = pattern2.matcher(line);
			if (matcher2.find()) {
				if (caption != null) {
					ret.put(matcher2.group(1), caption);
					break;
				}
			}
		}
		scanner.close();
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
		for (final String attribute : new String[] { "すかいらーく", "バーミヤン", "ガスト", "夢庵", "ジョナサン", "藍屋" }) {
			if (shopName.startsWith(attribute)) {
				return attribute;
			}
		}
		return "不明なすかいらーく";
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
		return URL1 + cityID.substring(0, 2) + URL2 + URLEncoder.encode(cityLabel, Skylark.ENCODING);
	}
}
