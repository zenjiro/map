package shop;

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
 * 三井住友銀行のホームページから住所を抽出するクラスです。
 * @since 4.10
 * @author zenjiro
 * 2006/07/30
 */
public class SMBC implements AddressParser {

	/**
	 * @since 4.10
	 */
	public Map<String, String> getAddresses(final String url) throws IOException {
		final Map<String, String> ret = new ConcurrentHashMap<String, String>();
		final Scanner urlScanner = new Scanner(new InputStreamReader(new URL(url).openStream(), this.getEncoding()));
		final Pattern urlPattern = Pattern.compile("<A HREF=\"([^<>]+)\"><B>[^<>]+</B></FONT></A></TD>");
		String url2 = null;
		while (urlScanner.hasNextLine()) {
			final String line = urlScanner.nextLine();
			final Matcher urlMatcher = urlPattern.matcher(line);
			if (urlMatcher.matches()) {
				url2 = urlMatcher.group(1);
				break;
			}
		}
		urlScanner.close();
		if (url2 == null) {
			System.out.println("SMBC: no URLs found: " + url);
		}
		final Scanner scanner = new Scanner(new InputStreamReader(new URL(url2).openStream(), this.getEncoding()));
		final Pattern shopPattern = Pattern.compile("<td bgcolor=\"#E8F5DA\" width=\"285\"><span class=\"t12lh\"><a href=\"[^<>]+\">([^<>]+)</a></span><br>");
		final Pattern addressPattern = Pattern.compile("<span class=\"t10\">([^<>]+)</span></td>");
		String shopName = null;
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			final Matcher shopMatcher = shopPattern.matcher(line);
			final Matcher addressMatcher = addressPattern.matcher(line);
			if (shopMatcher.matches()) {
				shopName = shopMatcher.group(1);
			} else if (addressMatcher.matches()) {
				final String address = addressMatcher.group(1);
				if (shopName != null) {
					if (!shopName.contains("ローンプラザ")) {
						ret.put(address, shopName);
					}
				}
			}
		}
		scanner.close();
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
		return "三井住友";
	}

	/**
	 * @since 4.10
	 */
	public String getPrefix() {
		return "smbc_";
	}

	/**
	 * @since 4.10
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		return "http://map.e-map.co.jp/standard/11034020/ssmtop.htm?tod_jusyo=" + cityID.substring(0, 2)
				+ "&fw_jusyo=" + URLEncoder.encode(cityLabel, this.getEncoding()) + "&cpopt1=2"; // test
	}

}
