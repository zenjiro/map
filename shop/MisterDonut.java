package shop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ミスタードーナッツの住所を取得するクラスです。
 * @author zenjiro
 * @since 3.16
 * 2005/12/04
 */
public class MisterDonut implements AddressParser {
	/**
	 * 最初のURL
	 */
	private static final String URL = "http://vip.mapion.co.jp/c/f?grp=misterdonut&uc=21&bool=admi2code&admi3=";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "EUC-JP";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "misterdonut_";

	/**
	 * @since 4.09
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			String caption = null;
			final Pattern pattern = Pattern.compile("<A HREF=\"[^<>]+\">([^<>]+)</A>");
			final Pattern pattern2 = Pattern.compile("<font [^<>]+>([^<>]+)</font>");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					caption = matcher.group(1);
				}
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					if (caption != null) {
						final String address = matcher2.group(1);
						if (!address.startsWith("：")) {
							ret.put(address, caption);
						}
					}
				}
			}
			scanner.close();
		} catch (final FileNotFoundException e) {
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
		return "ミスド";
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
		return URL + cityID;
	}
}
