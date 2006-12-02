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
 * ケンタッキーフライドチキンの住所を取得するクラスです。
 * 検索結果が1件のときは直接詳細ページが表示されるので、注意が必要です。（cityID=27103、28101、28102）
 * @author zenjiro
 * @since 3.16
 * 2005/12/04
 */
public class KFC implements AddressParser {
	/**
	 * 最初のURL
	 */
	private static final String URL = "http://search.kfc.co.jp/store_search.cgi?gid=KFC&city=";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "EUC-JP";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "kfc_";

	/**
	 * @since 4.08
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			final Pattern shopPattern = Pattern.compile("\t+<TD class=\"m\">([^<>]+店)</TD>");
			final Pattern addressPattern = Pattern.compile("\t+<TD class=\"m\">(" + this.prefectureLabel
					+ "[^<>]+)</TD>");
			final Pattern singleShopPattern = Pattern.compile("<SPAN class=\"shop\">([^<>]+店)</SPAN>");
			final Pattern singleAddressPattern = Pattern.compile("住所.+<br>([^<>]+) *</TD>");
			String shopName = null;
			boolean isSingle = false;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (!isSingle) {
					if (line.equals("<TITLE>店舗詳細</TITLE>")) {
						isSingle = true;
					}
				}
				if (isSingle) {
					final Matcher shopMatcher = singleShopPattern.matcher(line);
					final Matcher addressMatcher = singleAddressPattern.matcher(line);
					if (shopMatcher.find()) {
						shopName = shopMatcher.group(1);
					} else if (addressMatcher.find()) {
						final String address = addressMatcher.group(1).trim();
						if (shopName != null) {
							ret.put(address, shopName);
							break;
						}
					}
				} else {
					final Matcher shopMatcher = shopPattern.matcher(line);
					final Matcher addressMatcher = addressPattern.matcher(line);
					if (shopMatcher.matches()) {
						shopName = shopMatcher.group(1);
					} else if (addressMatcher.matches()) {
						final String address = addressMatcher.group(1).trim();
						if (shopName != null) {
							ret.put(address, shopName);
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
	 * 都道府県名
	 */
	private String prefectureLabel = null;

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
		return "KFC";
	}

	/**
	 * @since 4.08
	 */
	public String getPrefix() {
		return PREFIX;
	}

	/**
	 * @since 4.08
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		this.prefectureLabel = prefectureLabel;
		return URL + cityID;
	}
}
