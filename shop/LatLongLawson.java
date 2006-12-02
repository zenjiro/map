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
 * ローソンのホームページを解析して、店舗の緯度経度を取得するクラスです。
 * @author zenjiro
 * @since 4.12
 * 2006/08/17
 */
public class LatLongLawson implements LatLongParser {

	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		return "http://map.lawson.co.jp/c/f/?uc=106&admi2=" + cityID.substring(0, 2) + "&admi3=" + cityID + "&vp=100";
	}

	public Location getLocation(final String url) throws IOException {
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.ENCODING));
			final Pattern pattern = Pattern
					.compile("<img src=\"http://vmap.mapion.co.jp/m/k\\?grp=lawson01&uc=1&el=([0-9]+)/([0-9]+)/([0-9.]+)&nl=([0-9]+)/([0-9]+)/([0-9.]+)&size=[0-9,]+&scl=[0-9]+\" alt=\"\" width=\"[0-9]+\" height=\"[0-9]+\" border=\"[0-9]+\">");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					final int longInt = Integer.parseInt(matcher.group(1));
					final int longMin = Integer.parseInt(matcher.group(2));
					final double longSec = Double.parseDouble(matcher.group(3));
					final int latInt = Integer.parseInt(matcher.group(4));
					final int latMin = Integer.parseInt(matcher.group(5));
					final double latSec = Double.parseDouble(matcher.group(6));
					final Point2D point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(longInt + longMin / 60.0 + longSec / 3600,
							latInt + latMin / 60.0 + latSec / 3600));
					scanner.close();
					return new Location(new Point2D.Double(point.getX(), -point.getY()), "ローソン");
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
			final Pattern pattern = Pattern.compile("<a href=\"javascript:GoSubmit\\(([0-9]),([0-9]+)\\)\">");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					final String uc = matcher.group(1);
					final String shopID = matcher.group(2);
					final String url2 = "http://map.lawson.co.jp/c/f/?tempo_id="
							+ shopID
							+ "&uc="
							+ uc
							+ "&find_tempo_name=&find_tel=&page=1&vp=10&mx=30&case%5B%5D=&case%5B%5D=&case%5B%5D=&case%5B%5D=&case%5B%5D=";
					ret.add(url2);
				}
			}
			scanner.close();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return ret;
	}

	public String getPrefix() {
		return "latlong_lawson_";
	}

	/**
	 * エンコーディング
	 */
	private final String ENCODING = "SJIS";

	public Collection<Location> getLocations(final String url) throws IOException {
		return null;
	}

}
