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
 * CoCo壱番屋の住所を取得するクラスです。
 * @author zenjiro
 * @since 3.16
 * 2005/12/04
 */
public class Cocoichi implements AddressParser {
	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * @since 4.08
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			final Pattern pattern1 = Pattern.compile("<p class=\"s-listtext1\">(<img [^<>]+>)?([^<>]+)</p>");
			final Pattern pattern2 = Pattern.compile("<p class=\"s-listtext2\">([^<>〜]+)</p>");
			String caption = null;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher1 = pattern1.matcher(line);
				if (matcher1.find()) {
					caption = matcher1.group(2);
				}
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					final String address = matcher2.group(1);
					if (caption != null) {
						if (this.cachedCityLabel == null) {
							System.out.println("Cocoichi: 市区町村名がnullなので、全ての市区町村の住所を抽出します。" + url);
							ret.put(address, caption);
						} else {
							if (address.contains(this.cachedCityLabel)) {
								ret.put(address, caption);
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
	 * @since 4.08
	 */
	public String getEncoding() {
		return ENCODING;
	}

	/**
	 * @since 4.08
	 */
	public String getLabel(final String shopName) {
		return "ココイチ";
	}

	/**
	 * @since 4.08
	 */
	public String getPrefix() {
		return "cocoichi_";
	}

	/**
	 * 市区町村名
	 * @since 4.08
	 */
	private String cachedCityLabel = null;

	/**
	 * @since 4.08
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		this.cachedCityLabel = cityLabel;
		return "http://www.ichibanya.co.jp/shop/search/search.html?fkeyword=" + URLEncoder.encode(cityLabel, ENCODING)
				+ "&x=0&y=0&fpref=";
	}
}
