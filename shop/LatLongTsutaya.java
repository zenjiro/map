package shop;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.Const;
import map.UTMUtil;
import map.WGSUtil;

/**
 * ユニクロのホームページを解析して、店舗の緯度経度を取得するクラスです。
 * @author zenjiro
 * @since 4.12
 * 2006/08/18
 */
public class LatLongTsutaya implements LatLongParser {

	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
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
				+ URLEncoder.encode(cityLabel.replaceFirst(".+支庁", ""), this.ENCODING) + "&FROM=1&TO=20";
	}

	public Location getLocation(final String url) throws IOException {
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.ENCODING));
			final Pattern patternY = Pattern.compile("y = \"([0-9.]+)\";");
			final Pattern patternX = Pattern.compile("x = \"([0-9.]+)\";");
			double y = Double.NaN;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcherY = patternY.matcher(line);
				final Matcher matcherX = patternX.matcher(line);
				if (matcherY.find()) {
					y = Double.parseDouble(matcherY.group(1));
				} else if (matcherX.find()) {
					final double x = Double.parseDouble(matcherX.group(1));
					final Point2D point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(x, y));
					scanner.close();
					return new Location(new Point2D.Double(point.getX(), -point.getY()), "ツタヤ");
				}
			}
			scanner.close();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return null;
	}

	public Collection<String> getURLs(final String url) throws IOException {
		final Collection<String> ret = new ArrayList<String>();
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.ENCODING));
			final Pattern pattern = Pattern.compile("<A HREF=\"(tenpo.zhtml\\?FCID=[0-9]+)\">");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					ret.add("http://www.tsutaya.co.jp/shop/" + matcher.group(1));
				}
			}
			scanner.close();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return ret;
	}

	public String getPrefix() {
		return "latlong_tsutaya_";
	}

	/**
	 * エンコーディング
	 */
	private final String ENCODING = "SJIS";

	public Collection<Location> getLocations(final String url) throws IOException {
		return null;
	}

}
