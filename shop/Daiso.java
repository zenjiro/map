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
 * ダイソーの住所を取得するクラスです。
 * @author zenjiro
 * @since 4.07
 * 2006/07/22
 */
public class Daiso implements AddressParser {
	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "EUC-JP";

	/**
	 * @since 4.07
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			final Pattern shopPattern = Pattern
					.compile(" +<td>([^<>a-zA-Z0-9]+店)</td> ");
			final Pattern addressPattern = Pattern.compile(" +<td>([^<>a-zA-Z0-9]+[^店])</td> ");
			String shopName = null;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher shopMatcher = shopPattern.matcher(line);
				final Matcher addressMatcher = addressPattern.matcher(line);
				if (shopMatcher.matches()) {
					shopName = shopMatcher.group(1);
				} else if (addressMatcher.matches()) {
					final String address = addressMatcher.group(1);
					if (shopName != null) {
						ret.put(address, shopName);
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
		return "ダイソー";
	}

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return "daiso_";
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		return "http://www.daiso-sangyo.co.jp/shop/list/text.php?word="
				+ URLEncoder.encode(prefectureLabel + cityLabel, Daiso.ENCODING) + "&Submit1=%B8%A1%BA%F7";
	}
}
