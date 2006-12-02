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
 * ツタヤの住所を取得するクラスです。
 * @author zenjiro
 * @since 4.07
 * 2006/07/22
 */
public class Tsutaya implements AddressParser {
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
					.compile("<TD ALIGN=\"LEFT\" VALIGN=\"TOP\" WIDTH=\"50%\"><FONT CLASS=f09pt SIZE=2><B><A HREF=\"[^<>]+\">([^<>]+)</A>");
			String shopName = null;
			boolean wasLastLineZipCode = false;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher shopMatcher = shopPattern.matcher(line);
				if (shopMatcher.matches()) {
					shopName = shopMatcher.group(1);
				} else if (wasLastLineZipCode) {
					final String address = line.trim().replaceAll(" ", "");
					if (shopName != null) {
						ret.put(address, shopName);
					}
					wasLastLineZipCode = false;
				} else if (line.contains("〒")) {
					wasLastLineZipCode = true;
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
		return "ツタヤ";
	}

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return "tsutaya_";
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		final Map<String, String> prefectures = new ConcurrentHashMap<String, String>();
		prefectures.put("北海道", "1");
		prefectures.put("青森県", "2");
		prefectures.put("秋田県", "3");
		prefectures.put("岩手県", "4");
		prefectures.put("宮城県", "5");
		prefectures.put("山形県", "6");
		prefectures.put("福島県", "7");
		prefectures.put("東京都", "8");
		prefectures.put("神奈川県", "9");
		prefectures.put("千葉県", "10");
		prefectures.put("埼玉県", "11");
		prefectures.put("栃木県", "12");
		prefectures.put("群馬県", "13");
		prefectures.put("茨城県", "14");
		prefectures.put("山梨県", "15");
		prefectures.put("新潟県", "16");
		prefectures.put("富山県", "17");
		prefectures.put("石川県", "18");
		prefectures.put("福井県", "19");
		prefectures.put("長野県", "20");
		prefectures.put("岐阜県", "21");
		prefectures.put("静岡県", "22");
		prefectures.put("愛知県", "23");
		prefectures.put("三重県", "24");
		prefectures.put("滋賀県", "25");
		prefectures.put("京都府", "26");
		prefectures.put("大阪府", "27");
		prefectures.put("兵庫県", "28");
		prefectures.put("奈良県", "29");
		prefectures.put("和歌山県", "30");
		prefectures.put("鳥取県", "31");
		prefectures.put("島根県", "32");
		prefectures.put("岡山県", "33");
		prefectures.put("広島県", "34");
		prefectures.put("山口県", "35");
		prefectures.put("香川県", "36");
		prefectures.put("徳島県", "37");
		prefectures.put("愛媛県", "38");
		prefectures.put("高知県", "39");
		prefectures.put("大分県", "40");
		prefectures.put("福岡県", "41");
		prefectures.put("佐賀県", "42");
		prefectures.put("長崎県", "43");
		prefectures.put("宮崎県", "44");
		prefectures.put("熊本県", "45");
		prefectures.put("鹿児島県", "46");
		prefectures.put("沖縄県", "47");
		return "http://www.tsutaya.co.jp/shop/dis-l.zhtml?PREF=" + prefectures.get(prefectureLabel) + "&TXT_CITY="
				+ URLEncoder.encode(cityLabel.replaceFirst(".+支庁", ""), Tsutaya.ENCODING) + "&FROM=1&TO=20";
	}
}
