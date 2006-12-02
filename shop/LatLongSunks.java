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
 * サークルKサンクスのホームページを解析して、店舗の緯度経度を取得するクラスです。
 * @author zenjiro
 * @since 4.12
 * 2006/08/18
 */
public class LatLongSunks implements LatLongParser {

	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		return "http://sp.chizumaru.com/dbh/200080/list.aspx?account=200080&accmd=0&c1=1%2C2&adr=" + cityID;
	}

	public Location getLocation(final String url) throws IOException {
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.ENCODING));
			final Pattern xPattern = Pattern.compile("<input type='hidden' name='x' value='([0-9.]+)'>");
			final Pattern yPattern = Pattern.compile("<input type='hidden' name='y' value='([0-9.]+)'>");
			String caption = null;
			double x = Double.NaN;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher xMatcher = xPattern.matcher(line);
				final Matcher yMatcher = yPattern.matcher(line);
				if (line.contains("icon_circlek.gif")) {
					caption = "サークルK";
				} else if (line.contains("icon_sunkus.gif")) {
					caption = "サンクス";
				} else if (xMatcher.matches()) {
					x = Double.parseDouble(xMatcher.group(1));
				} else if (yMatcher.matches()) {
					final double y = Double.parseDouble(yMatcher.group(1));
					final Point2D point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(x / 3600, y / 3600));
					final Location location = new Location(new Point2D.Double(point.getX(), -point.getY()), caption);
					scanner.close();
					return location;
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
					.compile("<a href=\"javascript:PageLink\\('([0-9]+)','_self'\\);\" target=\"_self\" class=\"text12\">");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					ret.add("http://sp.chizumaru.com/dbh/200080/detailmap.aspx?account=200080&bid=" + matcher.group(1));
				}
			}
			scanner.close();
		} catch (final SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return ret;
	}

	public String getPrefix() {
		return "latlong_sunks_";
	}

	/**
	 * エンコーディング
	 */
	private final String ENCODING = "SJIS";

	public Collection<Location> getLocations(final String url) throws IOException {
		return null;
	}

}
