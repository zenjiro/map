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
 * りそな銀行の住所を取得するクラスです。
 * @author zenjiro
 * 2006/03/12
 */
public class Resona implements AddressParser {
	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "resona_";

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
			final Pattern pattern2 = Pattern.compile("<b>([^<>]+)</b><br>");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (line.startsWith("りそな銀行")) {
					caption = line;
				}
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					if (caption != null) {
						final String address = matcher2.group(1);
						if (this.prefectureLabel == null) {
							System.out.println("Resona: 都道府県名がnullなので、チェックをしません。");
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
		return "りそな";
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
		return "http://www.mapion.co.jp/c/f?vp=20&p=1&grp=resona&uc=21&ob=0&mx=500&bool=%28%2Bkey1%29*admi2code*sales1*sales2*sales3*sales4*sales5*sales6*sales7*sales8*edit9*edit10&pg=&sfn=resona_search-partinfo-list_00&oi=edit4&sales1=1&key1=100&key1=200&admi2code="
				+ cityID.substring(0, 2) + "&admi3=" + cityID + "&%B8%A1%BA%F7=%B9%CA%A4%EA%B9%FE%A4%DF";
	}
}
