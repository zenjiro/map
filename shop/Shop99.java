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
 * ショップ99の住所を取得するクラスです。
 * @author zenjiro
 * @since 4.07
 * 2006/07/22
 */
public class Shop99 implements AddressParser {
	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * @since 4.07
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			final Pattern shopPattern = Pattern
					.compile("<td width=\"108\" class=\"vs2\"><a href=[^<>]+>([^<>a-zA-Z0-9]+)</a></td>");
			final Pattern addressPattern = Pattern.compile("<td class=\"vs2\">([^<>a-zA-Z]+)　?</td>");
			String shopName = null;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher shopMatcher = shopPattern.matcher(line);
				final Matcher addressMatcher = addressPattern.matcher(line);
				if (shopMatcher.matches()) {
					shopName = shopMatcher.group(1);
				} else if (addressMatcher.matches()) {
					final String address = addressMatcher.group(1).replaceFirst("　", "");
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
		return "ショップ99";
	}

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return "shop99_";
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		return "http://www.shop99.co.jp/cgi-bin/usr/tempo/search.cgi?keyword="
				+ URLEncoder.encode(cityLabel, Shop99.ENCODING) + "&addr1=" +
				URLEncoder.encode(prefectureLabel, Shop99.ENCODING);
	}
}
