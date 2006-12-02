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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.Const;
import map.UTMUtil;
import map.WGSUtil;

/**
 * タイムズのホームページを解析して、店舗の緯度経度を取得するクラスです。
 * @author zenjiro
 * @since 4.13
 * 2006/09/24
 */
public class LatLongTimes implements LatLongParser {

	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		return "http://map.times-info.net/map/spot_addr.php?kind=1&key="
				+ URLEncoder.encode(prefectureLabel + cityLabel, LatLongTimes.this.ENCODING) + "&lk=0&pg=1";
	}

	public Location getLocation(final String url) throws IOException {
		return null;
	}

	public Collection<String> getURLs(final String url) throws IOException {
		return null;
	}

	public String getPrefix() {
		return "latlong_times_";
	}

	/**
	 * エンコーディング
	 */
	private final String ENCODING = "EUC-JP";

	public Collection<Location> getLocations(final String url) throws IOException {
		final Collection<Location> ret = new ArrayList<Location>();
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.ENCODING));
			final Pattern pattern = Pattern.compile("javascript:moveMap\\('([0-9.]+)','([0-9.]+)','','[A-Z0-9]+'\\)");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					final double latitude = Double.parseDouble(matcher.group(1));
					final double longitude = Double.parseDouble(matcher.group(2));
					final Point2D point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(latitude, longitude));
					ret.add(new Location(new Point2D.Double(point.getX(), -point.getY()), "タイムズ"));
				}
			}
			scanner.close();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return ret;
	}

}
