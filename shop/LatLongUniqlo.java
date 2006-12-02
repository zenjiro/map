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
 * ユニクロのホームページを解析して、店舗の緯度経度を取得するクラスです。
 * @author zenjiro
 * @since 4.12
 * 2006/08/18
 */
public class LatLongUniqlo implements LatLongParser {

	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		return "http://www.mapion.co.jp/c/f?uc=21&grp=uniqlo&bool=benefit1*station2*infoN*edit10*free1*free4*edit9*free2*edit4&oi=admi3code&ob=0&pg=3&edit10=&free1=&free4=&edit9=&free2=&edit4=&benefit1="
				+ URLEncoder.encode(cityLabel, this.ENCODING);
	}

	public Location getLocation(final String url) throws IOException {
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.ENCODING));
			final Pattern pattern = Pattern.compile("nl=([0-9]+)/([0-9]+)/([0-9.]+)&el=([0-9]+)/([0-9]+)/([0-9.]+)");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					final int latInt = Integer.parseInt(matcher.group(1));
					final int latMin = Integer.parseInt(matcher.group(2));
					final double latSec = Double.parseDouble(matcher.group(3));
					final int longInt = Integer.parseInt(matcher.group(4));
					final int longMin = Integer.parseInt(matcher.group(5));
					final double longSec = Double.parseDouble(matcher.group(6));
					final Point2D point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(longInt + longMin / 60.0 + longSec / 3600,
							latInt + latMin / 60.0 + latSec / 3600));
					scanner.close();
					return new Location(new Point2D.Double(point.getX(), -point.getY()), "ユニクロ");
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
			final Pattern pattern = Pattern
					.compile("<a href=\"(/c/f\\?uc=4&pg=1&grp=uniqlo&ino=[^<>]+)\" target=\"_blank\">");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					ret.add("http://www.mapion.co.jp" + matcher.group(1));
				}
			}
			scanner.close();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return ret;
	}

	public String getPrefix() {
		return "latlong_uniqlo_";
	}

	/**
	 * エンコーディング
	 */
	private final String ENCODING = "EUC-JP";

	public Collection<Location> getLocations(final String url) throws IOException {
		return null;
	}

}
