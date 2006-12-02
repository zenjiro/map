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
 * ブックオフの住所を取得するクラスです。
 * @author zenjiro
 * @since 4.07
 * 2006/07/22
 */
public class BookOff implements AddressParser {
	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * 市区町村コード
	 */
	private String cityID = null;

	/**
	 * @since 4.07
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			final Pattern shopPattern = Pattern
					.compile(" +<td width=\"150\" valign=\"top\" class=\"s3\"><a href=\"[^<>]+\" title=\"[^<>]+\">([^<>]+)</a><img [^<>]+></td>");
			final Pattern addressPattern = Pattern
					.compile(" +<td width=\"180\" class=\"s3\"> ([^a-zA-Z0-9]+[^<>]+)<br>");
			final Pattern cityIDPattern = Pattern.compile("<a name=\"[0-9]+\">");
			String shopName = null;
			boolean isThisCity = false;
			if (this.cityID == null) {
				System.out.println("BookOff: 市区町村コードがnullなので、全ての市区町村の住所を抽出します。" + url);
				isThisCity = true;
			}
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (isThisCity) {
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
				if (this.cityID != null) {
					if (line.contains("<a name=\"" + this.cityID + "\">")) {
						isThisCity = true;
					} else if (line.contains("<a name=\"")) {
						final Matcher cityIDMatcher = cityIDPattern.matcher(line);
						if (cityIDMatcher.find()) {
							isThisCity = false;
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
	 * @since 4.07
	 */
	public String getEncoding() {
		return ENCODING;
	}

	/**
	 * @since 4.07
	 */
	public String getLabel(final String shopName) {
		return "ブックオフ";
	}

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return "bookoff_";
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		this.cityID = cityID;
		return "http://www2.info-mapping.com/bookoff/kensaku/searchshop.asp?fkncode=" + cityID.substring(0, 2);
	}
}
