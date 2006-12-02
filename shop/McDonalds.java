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
 * マクドナルドの住所を取得するクラスです。
 * @author zenjiro
 * 2005/12/03
 */
public class McDonalds implements AddressParser {
	/**
	 * 最初のURL
	 */
	private static final String URL1 = "http://www.mcdonalds.co.jp/cgi-bin/shop/search3/store_list.cgi?keyword=";

	/**
	 * 次のURL
	 */
	private static final String URL2 = "&Image1.x=0&Image1.y=0";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "mcdonalds_";

	/**
	 * @since 4.09
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			final Pattern pattern = Pattern
					.compile("<a href=\"/cgi-bin/shop/search3/store_data.cgi\\?strcode=[0-9]+\" target=\"_blank\">([^<>]+)</a>");
			final Pattern pattern2 = Pattern.compile("<td width=\"[0-9]+%\">([^<>]+)</td>");
			String caption = null;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					caption = matcher.group(1);
					continue;
				}
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					if (caption != null) {
						final String address = matcher2.group(1);
						ret.put(address.replace(" ", ""), caption.replace(" ", ""));
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
	 * @since 4.09
	 */
	public String getEncoding() {
		return ENCODING;
	}

	/**
	 * @since 4.09
	 */
	public String getLabel(final String shopName) {
		return "マック";
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
		return URL1 + URLEncoder.encode(prefectureLabel + cityLabel, ENCODING) + URL2;
	}
}
