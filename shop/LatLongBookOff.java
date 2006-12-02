package shop;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
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
 * ブックオフの住所を取得するクラスです。
 * @author zenjiro
 * @since 4.07
 * 2006/07/22
 */
public class LatLongBookOff implements LatLongParser {
	/**
	 * エンコーディング
	 */
	private static final String ENCODING = "SJIS";

	/**
	 * 市区町村コード
	 */
	private String cityID = null;

	/**
	 * @since 4.07
	 */
	public String getPrefix() {
		return "latlong_bookoff_";
	}

	/**
	 * @since 4.07
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		this.cityID = cityID;
		return "http://www2.info-mapping.com/bookoff/kensaku/searchshop.asp?fkncode=" + cityID.substring(0, 2);
	}

	public Location getLocation(final String url) throws IOException {
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
		final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), LatLongBookOff.ENCODING));
		final Pattern pattern = Pattern
				.compile("<a href=\"/bookoff/kensaku/map\\.asp\\?GPOS=([0-9.]+),([0-9.]+)&GSCL=[0-9]+&IID=[0-9]+\">");
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			final Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				final double longitude = Double.parseDouble(matcher.group(1));
				final double latitude = Double.parseDouble(matcher.group(2));
				final Point2D point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(longitude, latitude));
				scanner.close();
				return new Location(new Point2D.Double(point.getX(), -point.getY()), "ブックオフ");
			}
		}
		scanner.close();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return null;
	}

	public Collection<Location> getLocations(final String url) throws IOException {
		return null;
	}

	public Collection<String> getURLs(final String url) throws IOException {
		final Collection<String> ret = new ArrayList<String>();
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(),
					LatLongBookOff.ENCODING));
			final Pattern pattern = Pattern.compile("a href=\"(map\\.asp\\?ID=[0-9]+)\"");
			final Pattern cityIDPattern = Pattern.compile("<a name=\"[0-9]+\">");
			boolean isThisCity = false;
			if (this.cityID == null) {
				System.out.println(this.getClass().getName() + ": 市区町村コードがnullなので、全ての市区町村の住所を抽出します。" + url);
				isThisCity = true;
			}
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (isThisCity) {
					final Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						ret.add("http://www2.info-mapping.com/bookoff/kensaku/" + matcher.group(1));
					}
				}
				if (this.cityID != null) {
					if (line.contains("<a name=\"" + this.cityID + "\">")) {
						isThisCity = true;
					} else if (line.contains("<a name=\"")) {
						final Matcher cityIDMatcher = cityIDPattern.matcher(line);
						if (cityIDMatcher.find()) {
							isThisCity = false;
						}
					}
				}
			}
			scanner.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return ret;
	}
}
