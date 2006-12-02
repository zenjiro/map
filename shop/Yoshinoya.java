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
 * 吉野家の住所を取得するクラスです。
 * @author zenjiro
 * 2005/12/03
 */
public class Yoshinoya implements AddressParser {
	/**
	 * 最初のURL
	 * この後に都道府県コードが来ます。
	 */
	private static final String URL1 = "http://vip.mapion.co.jp/c/f?vp=10&p=1&grp=yoshinoya&uc=21&ob=0&mx=20&bool=admi2code&pg=&sfn=yoshinoya_search-partinfo-list_00&oi=admi3code&admi2code=";

	/**
	 * 次のURL
	 * この後に市区町村コードが来ます。
	 */
	private static final String URL2 = "&admi3=";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "EUC-JP";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "yoshinoya_";

	/**
	 * @since 4.09
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			String caption = null;
			final Pattern pattern = Pattern.compile("<a href=[^<>]+>([^<>]+)</a>");
			final Pattern pattern2 = Pattern.compile("<td [^<>]+>([^<>]+)</td>");
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
						if (this.cityLabel == null) {
							System.out.println("Matsuya: 市区町村名がnullなので、住所のチェックをしません。" + address);
							ret.put(address, caption);
						} else {
							if (address.contains(this.cityLabel)) {
								ret.put(address, caption);
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
	 * @since 4.09
	 */
	public String getEncoding() {
		return ENCODING;
	}

	/**
	 * @since 4.09
	 */
	public String getLabel(final String shopName) {
		return "吉牛";
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
		this.cityLabel = cityLabel;
		return URL1 + cityID.substring(0, 2) + URL2 + cityID;
	}

	/**
	 * 市区町村名
	 */
	private String cityLabel = null;

}
