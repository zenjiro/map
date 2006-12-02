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
 * am/pmの住所を取得するクラスです。
 * @author zenjiro
 * @since 3.16
 * 2005/12/04
 */
public class AmPm implements AddressParser {
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
					.compile(" +<TD class=\"font2\"><A href=\"[^<>]+\">([^a-zA-Z0-9]+)</TD>");
			final Pattern addressPattern = Pattern.compile(" +<TD class=\"font2\">([^a-zA-Z0-9]+)</TD>");
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
		return "am/pm";
	}

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return "ampm_";
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		return "http://www.mapion.co.jp/c/f?uc=21&oi=admi3code&ob=0&grp=ampm&bool=station2&station2="
				+ URLEncoder.encode(prefectureLabel + cityLabel, AmPm.ENCODING) + "&x=0&y=0";
	}
}
