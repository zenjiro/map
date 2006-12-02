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
 * 松屋の住所を取得するクラスです。
 * @author zenjiro
 * 2005/12/03
 */
public class Matsuya implements AddressParser {
	/**
	 * 最初のURL
	 * この後に市区町村コードが来ます。
	 */
	private static final String URL = "http://sp.chizumaru.com/dbh/matsuyaf/list.aspx?account=matsuyaf&accmd=0&arg=&c1=&c2=&c3=&c4=&c5=&c6=&c7=&c8=&c9=&c10=&c11=&c12=&c13=&c14=&c15=&c16=&c17=&c18=&c19=&c20=&c21=&c22=&c23=&c24=&c25=&c26=&c27=&c28=&c29=&c30=&adr=";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "matsuya_";

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
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 市区町村名
	 */
	private String cityLabel = null;

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
		return "松屋";
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
		return URL + cityID;
	}
}
