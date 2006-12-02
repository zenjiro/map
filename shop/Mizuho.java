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
 * みずほ銀行の住所を取得するクラスです。
 * @author zenjiro
 * 2006/03/12
 */
public class Mizuho implements AddressParser {
	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "mizuho_";

	/**
	 * @since 4.10
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), this.getEncoding()));
			String caption = null;
			final Pattern pattern = Pattern.compile("<font [^<>]+\"><a href=\"[^\"<>]+\">([^<>]+)</a></font>");
			final Pattern pattern2 = Pattern.compile("<font [^<>]+>([^<>]+)</font>");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					if (matcher.group(1).endsWith("支店")) {
						caption = matcher.group(1);
					}
				}
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					if (caption != null) {
						final String address = matcher2.group(1);
						if (this.prefectureLabel == null) {
							System.out.println("Mizuho: 都道府県名がnullなので、チェックをしません。");
							ret.put(address, caption);
							caption = null;
						} else {
							if (address.startsWith(this.prefectureLabel)) {
								ret.put(address, caption);
								caption = null;
							}
						}
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
	 * @since 4.10
	 */
	public String getEncoding() {
		return "EUC-JP";
	}

	/**
	 * @since 4.10
	 */
	public String getLabel(final String shopName) {
		return "みずほ";
	}

	/**
	 * @since 4.10
	 */
	public String getPrefix() {
		return PREFIX;
	}

	/**
	 * @since 4.10
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		this.prefectureLabel = prefectureLabel;
		return "http://vip.mapion.co.jp/c/f?uc=21&admi3code=" + cityID
				+ "&bool=(%2Badmi3code)&grp=mizuho&oi=key6&ob=0&mx=230";
	}

	/**
	 * 都道府県名
	 */
	private String prefectureLabel = null;
}
