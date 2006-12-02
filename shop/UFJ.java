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
 * 三菱東京UFJ銀行の住所を取得するプログラムです。
 * @author zenjiro
 * 2005/12/04
 * @since 3.16
 */
public class UFJ implements AddressParser {
	/**
	 * 都道府県名
	 */
	private String prefectureLabel = null;

	/**
	 * @since 4.10
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), this.getEncoding()));
			String caption = null;
			final Pattern pattern = Pattern
					.compile("<a href=\"/c/f\\?uc=[0-9]+&ino=[A-Z0-9]+&pg=[0-9]+&grp=bk_mufg\">([^<>]+)</a>");
			final Pattern pattern2 = Pattern
					.compile("<td bgcolor=\"#[a-z]+\" width=\"[0-9]+\"><font class=\"[a-z]+\">([^<>]+)</font></td>");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					caption = matcher.group(1);
				}
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					if (caption != null) {
						if (!caption.startsWith("ATM")) {
							if (this.prefectureLabel == null) {
								System.out.println("UFJ: 都道府県名がnullです。" + url);
							} else {
								ret.put(this.prefectureLabel + matcher2.group(1), caption);
							}
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
	 * @since 4.10
	 */
	public String getEncoding() {
		return "SJIS";
	}

	/**
	 * @since 4.10
	 */
	public String getLabel(final String shopName) {
		return "MUFJ";
	}

	/**
	 * @since 4.10
	 */
	public String getPrefix() {
		return "ufj_";
	}

	/**
	 * @since 4.10
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		this.prefectureLabel = prefectureLabel;
		return "http://www.mapion.co.jp/c/f?uc=21&pg=3&bool=admi2code*benefit1&grp=bk_mufg&ob=u&oi=key6%252Cedit5&admi2="
				+ cityID.substring(0, 2)
				+ "&benefit1="
				+ URLEncoder.encode(cityLabel, "EUC-JP")
				+ "&=%B8%A1%A1%A1%BA%F7";
	}
}
