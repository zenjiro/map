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
 * ドトール、エクセルシオールカフェの住所を取得するクラスです。
 * @author zenjiro
 * @since 4.07
 * 2006/07/22
 */
public class Doutor implements AddressParser {
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
					.compile(" +<td bgcolor=\"[^<>]+\" valign=\"middle\" class=small>[^<>]+<a href=\"[^<>]+\"><font color=\"[^<>]+\">([^a-zA-Z0-9]+)</font></a> ");
			final Pattern addressPattern = Pattern.compile(" +<td bgcolor=\"[^<>]+\" class=small> &nbsp;([^a-zA-Z0-9]+[^<>]+)</td>");
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
		if (shopName.startsWith("ドトール")) {
			return "ドトール";
		} else {
			return "エクシオ";
		}
	}

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return "doutor_";
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		return "http://www.mapion.co.jp/c/f?uc=21&grp=doutor&bool=benefit1&oi=admi2code&ob=0&benefit1="
				+ URLEncoder.encode(prefectureLabel + cityLabel, "SJIS") + "&x=37&y=13";
	}
}
