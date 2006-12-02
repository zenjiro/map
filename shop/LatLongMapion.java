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
 * マピオンを使っている店舗の座標を取得するための抽象クラスです。
 * @author zenjiro
 * @since 4.13
 */
public abstract class LatLongMapion implements LatLongParser {

	/**
	 * @return 店舗一覧ページ、店舗別ページのエンコーディング
	 */
	protected abstract String getEncoding();

	/**
	 * @return 地図中に描画する文字列
	 */
	protected abstract String getLabel();

	public Location getLocation(final String url) throws IOException {
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.getEncoding()));
			final Pattern latitudePattern = Pattern.compile("nl=([0-9]+)/([0-9]+)/([0-9.]+)");
			final Pattern longitudePattern = Pattern.compile("el=([0-9]+)/([0-9]+)/([0-9.]+)");
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher latitudeMatcher = latitudePattern.matcher(line);
				final Matcher longitudeMatcher = longitudePattern.matcher(line);
				if (latitudeMatcher.find() && longitudeMatcher.find()) {
					final int latitudeDegree = Integer.parseInt(latitudeMatcher.group(1));
					final int latitudeMinutes = Integer.parseInt(latitudeMatcher.group(2));
					final double latitudeSeconds = Double.parseDouble(latitudeMatcher.group(3));
					final int longitudeDegree = Integer.parseInt(longitudeMatcher.group(1));
					final int longitudeMinutes = Integer.parseInt(longitudeMatcher.group(2));
					final double longitudeSeconds = Double.parseDouble(longitudeMatcher.group(3));
					final double latitude = latitudeDegree + latitudeMinutes / 60.0 + latitudeSeconds / 3600;
					final double longitude = longitudeDegree + longitudeMinutes / 60.0 + longitudeSeconds / 3600;
					scanner.close();
					final Point2D point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(longitude, latitude));
					return new Location(new Point2D.Double(point.getX(), -point.getY()), this.getLabel());
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

	public abstract String getPrefix();

	public abstract String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException;

	/**
	 * @return 店舗一覧ページから店舗別ページのURLを抽出するためのパターン。これのパターンにマッチするgroup(1)は「/」から始まっているべきです。
	 */
	protected abstract String getURLPattern();

	public Collection<String> getURLs(final String url) throws IOException {
		final Collection<String> ret = new ArrayList<String>();
		final URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(Const.TIMEOUT);
		connection.setReadTimeout(Const.TIMEOUT);
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), this.getEncoding()));
			final Pattern pattern = Pattern.compile(this.getURLPattern());
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					ret.add("http://www.mapion.co.jp" + matcher.group(1));
				}
			}
			scanner.close();
		} catch (SocketTimeoutException e) {
			System.out.println(this.getClass().getName() + ": タイムアウトしました。" + url);
		}
		return ret;
	}

}
