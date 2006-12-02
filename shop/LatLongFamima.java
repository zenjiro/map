package shop;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.Const;
import map.UTMUtil;
import map.WGSUtil;

/**
 * ファミリーマートのホームページを解析して、店舗の緯度経度を取得するクラスです。
 * @author zenjiro
 * @since 4.12
 * 2006/08/17
 */
public class LatLongFamima implements LatLongParser {

	public String getURL(final String cityID, final String cityLabel,
			final String prefectureLabel) throws UnsupportedEncodingException {
		return "http://famimap.cis-mapple.ne.jp/scripts/servlet/SearchAddress?site_id=50&ng_url=index.html&ok_url=index.html&status=50&zip=&c_ken="
				+ cityID.substring(0, 2)
				+ "&c_shi=&mode=shop_oaza&md=fm&code="
				+ cityID;
	}

	public Location getLocation(final String url) throws IOException {
		return null;
	}

	public Collection<String> getURLs(final String url) throws IOException {
		return null;
	}

	public String getPrefix() {
		return "latlong_famima_";
	}

	/**
	 * エンコーディング
	 */
	private final String ENCODING = "SJIS";

	public Collection<Location> getLocations(final String url) throws IOException {
		final Collection<Location> ret = new ArrayList<Location>();
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
		final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.ENCODING));
		final Pattern pattern = Pattern
				.compile("<a href=\"javascript:formon\\('([0-9.]+)','([0-9.]+)','','ooazaform'\\)\">");
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			final Matcher latLongMatcher = pattern.matcher(line);
			if (latLongMatcher.find()) {
				final double latitude = Double.parseDouble(latLongMatcher.group(1));
				final double longitude = Double.parseDouble(latLongMatcher.group(2));
				final Point2D point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(longitude, latitude));
				ret.add(new Location(new Point2D.Double(point.getX(), -point.getY()), "ファミマ"));
			}
		}
		scanner.close();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return ret;
	}

}
