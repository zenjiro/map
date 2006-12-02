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
 * ファミリーマートの住所を取得するクラスです。
 * @author zenjiro
 * 2005/12/03
 */
public class Famima implements AddressParser {
	/**
	 * 最初のURL
	 */
	private static final String URL1 = "http://famimap.cis-mapple.ne.jp/scripts/servlet/SearchAddress?site_id=50&ng_url=index.html&ok_url=index.html&status=50&zip=&c_ken=";

	/**
	 * 次のURL
	 */
	private static final String URL2 = "&c_shi=&mode=shop_oaza&md=fm&code=";

	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * キャッシュファイル名の接頭語
	 */
	private static final String PREFIX = "famima_";

	/**
	 * @since 4.07
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream(), ENCODING));
			String caption = null;
			final Pattern pattern = Pattern.compile("<a href=\"javascript:formon.+\">([^<>]+)</a>");
			final Pattern pattern2 = Pattern.compile("<td bgcolor=\"white\">&nbsp;([^<>]+)</td>");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					caption = matcher.group(1);
				}
				final Matcher matcher2 = pattern2.matcher(line);
				if (matcher2.find()) {
					if (caption != null) {
						ret.put(matcher2.group(1), caption);
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
		return "ファミマ";
	}

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return PREFIX;
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		return URL1 + cityID.substring(0, 2) + URL2 + cityID;
	}
}
