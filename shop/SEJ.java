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
 * セブンイレブンのホームページから住所を抽出するクラスです。
 * @since 4.10
 * @author zenjiro
 * 2006/07/30
 */
public class SEJ implements AddressParser {

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
			System.out.println("SEJ: no URLs found: " + url);
		}
		final Scanner scanner = new Scanner(new InputStreamReader(new URL(url2).openStream(), "SJIS"));
		final Pattern shopPattern = Pattern.compile("\t<TD><A HREF=\"[^<>]+\"><B>([^<>]+)</B></A></TD>");
		final Pattern addressPattern = Pattern.compile("\t<TD><FONT COLOR=\"#000000\">([^<>]+)</FONT></TD>");
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
					ret.put(address, shopName);
				}
			}
		}
		scanner.close();
		return ret;
	}

	public String getEncoding() {
		return "EUC-JP";
	}

	public String getLabel(final String shopName) {
		return "セブン";
	}

	public String getPrefix() {
		return "sej_";
	}

	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException {
		return "http://gis.e-map.co.jp/standard/13125010/sjwctl.htm?P_TODID=" + cityID.substring(0, 2) + "&FW="
				+ URLEncoder.encode(cityLabel, this.getEncoding())
				+ "&SEARCH.x=0&SEARCH.y=0&X_shop_id=40000&X_cancel_url=http%3A%2F%2Fwww.sej.co.jp%2F&RG=2.5&NU=30";
	}

}
