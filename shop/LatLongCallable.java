package shop;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import shop.LatLongParser.Location;

/**
 * 店舗の座標を並行して取得するためのクラスです。
 * @author zenjiro
 * @since 4.13
 */
public class LatLongCallable implements Callable<Map<Point2D, String>> {

	/**
	 * 市区町村コード
	 */
	private final String cityID;

	/**
	 * 市区町村名
	 */
	private final String cityLabel;

	/**
	 * 都道府県名
	 */
	private final String prefectureLabel;

	/**
	 * 店舗の座標を取得するためのオブジェクト
	 */
	final LatLongParser parser;

	/**
	 * コンストラクタです。
	 * @param parser 店舗の座標を取得するためのオブジェクト
	 * @param cityID 市区町村コード
	 * @param cityLabel 市区町村名
	 * @param prefectureLabel 都道府県名
	 */
	public LatLongCallable(final LatLongParser parser, final String cityID, final String cityLabel,
			final String prefectureLabel) {
		this.parser = parser;
		this.cityID = cityID;
		this.cityLabel = cityLabel;
		this.prefectureLabel = prefectureLabel;
	}

	public Map<Point2D, String> call() throws IOException {
		final Map<Point2D, String> ret = new ConcurrentHashMap<Point2D, String>();
		new File(Shop.CACHE_DIR).mkdirs();
		final String cacheFile = Shop.CACHE_DIR + File.separator + this.parser.getPrefix() + this.cityID + Shop.SUFFIX;
		if (!new File(cacheFile).exists()) {
			final PrintWriter out = new PrintWriter(new File(cacheFile), "SJIS");
			final String url = this.parser.getURL(this.cityID, this.cityLabel, this.prefectureLabel);
			final Collection<String> urls = this.parser.getURLs(url);
			if (urls == null) {
				final Collection<Location> locations = this.parser.getLocations(url);
				if (locations != null) {
					for (final Location location : locations) {
						out.println(location.getCaption() + "," + location.getLocation().getX() + ","
								+ location.getLocation().getY());
					}
				}
			} else {
				for (final String url2 : urls) {
					final Location location = this.parser.getLocation(url2);
					if (location != null) {
						out.println(location.getCaption() + "," + location.getLocation().getX() + ","
								+ location.getLocation().getY());
					}
				}
			}
			out.close();
		}
		final Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(new File(cacheFile)), "SJIS"));
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			final String[] items = line.split(",");
			if (items.length == 3) {
				final String caption = items[0];
				final double x = Double.parseDouble(items[1]);
				final double y = Double.parseDouble(items[2]);
				ret.put(new Point2D.Double(x, y), caption);
			} else {
				System.out.printf(this.getClass().getName() + ": %sのデータ形式が不正です：%s\n", cacheFile, line);
			}
		}
		scanner.close();
		return ret;
	}

}
