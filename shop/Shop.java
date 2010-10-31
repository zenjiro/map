package shop;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import map.MapPanel;

/**
 * 店舗のホームページを解析して座標を取得するクラスです。
 * @since 4.07
 * @author zenjiro
 * 2006/07/21
 */
public class Shop {
	/**
	 * キャッシュを保存するディレクトリ
	 */
	public static final String CACHE_DIR = ".map" + File.separator + "shops";

	/**
	 * キャッシュファイル名の接尾語
	 */
	public static final String SUFFIX = ".csv";

	/**
	 * @param cityID 市区町村コード
	 * @param cityLabel 市区町村名
	 * @param prefectureLabel 都道府県名
	 * @param isj 街区レベル位置参照情報
	 * @param panel 地図を描画するパネル
	 * @return コンビニの座標と表記の対応表
	 * @throws InterruptedException 割り込み例外
	 */
	public Map<Point2D, String> getShops(final String cityID, final String cityLabel, final String prefectureLabel,
			final Map<String, Point2D> isj, final MapPanel panel) throws InterruptedException {
		final Map<Point2D, String> ret = new ConcurrentHashMap<Point2D, String>();
		final Map<String, Point2D> tempIsj = new ConcurrentHashMap<String, Point2D>();
		for (final Map.Entry<String, Point2D> entry4 : isj.entrySet()) {
			tempIsj.put(entry4.getKey().replaceAll(",", ""), entry4.getValue());
		}

		final Set<AddressParser> parsers = new LinkedHashSet<AddressParser>(Arrays.asList(new AddressParser[] {
				new SMBC(), new Mos(), new Yoshinoya(), new Matsuya(), new MisterDonut(), new KFC(), new Daiso(),
				new Doutor(), new Shop99() }));
		final Set<AddressParser> doneSet = new HashSet<AddressParser>(parsers);
		panel.addMessage("店舗の住所をダウンロードしています。");
		try {
			for (final AddressParser parser : parsers) {
				new Thread(new ShopThread(parser, cityID, cityLabel, prefectureLabel, tempIsj, ret, doneSet)).start();
				Thread.sleep(10);
			}
			while (doneSet.size() > 1) {
				Thread.sleep(100);
			}
			Thread.sleep(10);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		panel.removeMessage();
		panel.addMessage("店舗の座標をダウンロードしています。");
		final ExecutorService service = Executors.newCachedThreadPool();
		final Collection<Future<Map<Point2D, String>>> futures = new ArrayList<Future<Map<Point2D, String>>>();
		for (final LatLongParser parser : new LatLongParser[] { new LatLongMUFJ(), new LatLongMizuho(),
				new LatLongResona(), new LatLongLawson(), new LatLongFamima(), new LatLongAmPm(), new LatLongSunks(),
				new LatLongUniqlo(), new LatLongTsutaya(), new LatLongYellowHat(), new LatLongTeng(),
				new LatLongNissan(), new LatLongTimes() }) {
			futures.add(service.submit(new LatLongCallable(parser, cityID, cityLabel, prefectureLabel)));
			Thread.sleep(10);
		}
		service.shutdown();
		for (final Future<Map<Point2D, String>> future : futures) {
			try {
				ret.putAll(future.get());
			} catch (final ExecutionException e) {
				System.err.printf("%s: 店舗の座標を取得できませんでした。cityID = %s, cityLabel = %s, prefectureLabel = %s\n", this
						.getClass().getName(), cityID, cityLabel, prefectureLabel);
				e.printStackTrace();
			}
		}
		panel.removeMessage();
		return ret;
	}
}
