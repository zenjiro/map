package map;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ksj.ShapeIO;
import map.Const.Zoom;
import web.WebUtilities;

/**
 * 国土数値情報の行政界・海岸線（面）から作成された都道府県に関するユーティリティクラスです。
 * @author zenjiro
 * 2005/11/11
 */
public class Prefectures {
	/**
	 * 必要に応じて市区町村のデータを読み込んだり、開放したりします。
	 * @param prefectures 都道府県の一覧
	 * @param panel 地図を表示するパネル
	 * @param maps 地図
	 * @param loadMap 地図を読み込むためのオブジェクト
	 * @return 地図が変化したかどうか
	 * @throws IOException 入出力例外
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 * @throws ExecutionException 実行例外
	 * @throws InterruptedException 割り込み例外
	 */
	public static boolean loadCities(final Collection<Prefecture> prefectures, final MapPanel panel,
			final Map<String, MapData> maps, final LoadMap loadMap) throws UnsupportedEncodingException, IOException,
			InterruptedException, ExecutionException {
		boolean ret = false;
		if (panel.getZoom() >= Zoom.LOAD_CITIES) {
			Progress.getInstance().setLoadMapPaintTyomeProgress(0);
			final Rectangle2D visibleRectangle = panel.getVisibleRectangle(false);
			for (final Prefecture prefecture : prefectures) {
				if (prefecture.getBounds().intersects(visibleRectangle)) {
					final Shape shape = prefecture.hasFine() ? prefecture.getFineShape() : prefecture.getShape();
					if (shape.intersects(visibleRectangle)) {
						if (prefecture.hasCities()) {
						} else {
							if (!new File(Const.Ksj.CACHE_DIR).exists()) {
								new File(Const.Ksj.CACHE_DIR).mkdirs();
							}
							final File textFile = new File(Const.Ksj.CACHE_DIR + File.separator + Const.Ksj.TXT_PREFIX
									+ prefecture.getId() + Const.Ksj.TXT_SUFFIX);
							final URL url = new URL(Const.Ksj.BASE_URL + Const.Ksj.ZIP_PREFIX + prefecture.getId()
									+ Const.Ksj.ZIP_SUFFIX);
							if (textFile.exists()) {
							} else {
								final File cacheDir = new File(Const.Ksj.CACHE_DIR);
								if (!cacheDir.exists()) {
									cacheDir.mkdir();
								}
								final File file = new File(Const.Ksj.CACHE_DIR + File.separator + Const.Ksj.ZIP_PREFIX
										+ prefecture.getId() + Const.Ksj.ZIP_SUFFIX);
								file.createNewFile();
								panel.addMessage(url + "をダウンロードしています。");
								WebUtilities.copy(url.openStream(), new FileOutputStream(file));
								final ZipFile zipFile = new ZipFile(file);
								for (final Enumeration<? extends ZipEntry> enumeration = zipFile.entries(); enumeration
										.hasMoreElements();) {
									final ZipEntry entry = enumeration.nextElement();
									if (entry.getName().endsWith(".txt")) {
										WebUtilities.copy(zipFile.getInputStream(entry), new FileOutputStream(
												Const.Ksj.CACHE_DIR + File.separator + new File(entry.getName())));
									}
								}
								panel.removeMessage();
							}
							prefecture.loadCities();
							ret = true;
						}
					} else {
						prefecture.freeCities();
						prefecture.freeFine();
					}
				} else {
					prefecture.freeCities();
					prefecture.freeFine();
				}
			}
		} else {
			for (final Prefecture prefecture : prefectures) {
				prefecture.freeCities();
				prefecture.freeFine();
			}
		}
		if (panel.getZoom() >= Zoom.LOAD_FINE_CITIES) {
			Progress.getInstance().setLoadMapPaintTyomeProgress(4);
			final Rectangle2D visibleRectangle = panel.getVisibleRectangle(false);
			for (final Prefecture prefecture : prefectures) {
				if (prefecture.getBounds().intersects(visibleRectangle)) {
					final Shape shape = prefecture.hasFine() ? prefecture.getFineShape() : prefecture.getShape();
					if (shape.intersects(visibleRectangle)) {
						if (!prefecture.hasFine()) {
							prefecture.loadFine();
						}
					}
				}
			}
		} else {
			for (final Prefecture prefecture : prefectures) {
				if (prefecture.hasFine()) {
					prefecture.freeFine();
				}
			}
		}
		if (panel.getZoom() >= Zoom.LOAD_FINE_CITIES) {
			Progress.getInstance().setLoadMapPaintTyomeProgress(8);
			final Rectangle2D visibleRectangle = panel.getVisibleRectangle(false);
			for (final Prefecture prefecture : prefectures) {
				if (prefecture.getBounds().intersects(visibleRectangle)) {
					final Shape shape = prefecture.hasFine() ? prefecture.getFineShape() : prefecture.getShape();
					if (shape.intersects(visibleRectangle)) {
						if (prefecture.hasCities()) {
							for (final City city : prefecture.getCities()) {
								final Shape shape2 = city.hasFineShape() ? city.getFineShape() : city.getShape();
								if (shape2.getBounds2D().intersects(visibleRectangle)) {
									if (shape2.intersects(visibleRectangle)) {
										if (city.loadKsjFineRoad()) {
											ret = true;
										}
									} else {
										city.freeKsjFineRoad();
									}
								} else {
									city.freeKsjFineRoad();
								}
							}
						}
					} else {
						if (prefecture.hasCities()) {
							for (final City city : prefecture.getCities()) {
								city.freeKsjFineRoad();
							}
						}
					}
				} else {
					if (prefecture.hasCities()) {
						for (final City city : prefecture.getCities()) {
							city.freeKsjFineRoad();
						}
					}
				}
			}
		} else {
			for (final Prefecture prefecture : prefectures) {
				if (prefecture.hasCities()) {
					for (final City city : prefecture.getCities()) {
						city.freeKsjFineRoad();
					}
				}
			}
		}
		if (panel.getZoom() >= Zoom.LOAD_GYOUSEI) {
			Progress.getInstance().setLoadMapPaintTyomeProgress(12);
			final Rectangle2D visibleRectangle = panel.getVisibleRectangle(false);
			final Collection<URL> urls = new ArrayList<URL>();
			for (final Prefecture prefecture : prefectures) {
				if (prefecture.hasCities()) {
					for (final City city : prefecture.getCities()) {
						if (city.getURL() != null) {
							final Shape shape = city.hasFineShape() ? city.getFineShape() : city.getShape();
							if (shape.getBounds2D().intersects(visibleRectangle)) {
								if (shape.intersects(visibleRectangle)) {
									if (!city.has2500()) {
										urls.add(city.getURL());
										city.setHas2500(true);
									}
								}
							}
						}
					}
				}
			}
			if (!urls.isEmpty()) {
				loadMap.loadMaps(urls, maps);
				ret = true;
			}
		} else {
			synchronized (maps) {
				maps.clear();
			}
			for (final Prefecture prefecture : prefectures) {
				if (prefecture.hasCities()) {
					for (final City city : prefecture.getCities()) {
						city.setHas2500(false);
					}
				}
			}
		}
		// since 3.08
		if (panel.getZoom() >= Zoom.LOAD_ALL) {
			Progress.getInstance().setLoadMapPaintTyomeProgress(16);
			final Rectangle2D visibleRectangle = panel.getVisibleRectangle(false);
			for (final Prefecture prefecture : prefectures) {
				if (prefecture.hasCities()) {
					for (final City city : prefecture.getCities()) {
						if (city.getURL() != null) {
							final Shape shape = city.hasFineShape() ? city.getFineShape() : city.getShape();
							if (shape.getBounds2D().intersects(visibleRectangle)) {
								if (shape.intersects(visibleRectangle)) {
									if (!city.hasIsj()) {
										city.loadIsj();
										// since 3.16
										city.loadShops();
									}
								}
							}
						}
					}
				}
			}
		}
		Progress.getInstance().setLoadMapPaintTyomeProgress(20);
		return ret;
	}

	/**
	 * 都道府県を読み込みます。
	 * @param preferences 色の設定
	 * @param panel 地図を描画するパネル
	 * @return 都道府県の一覧
	 */
	public static Collection<Prefecture> loadPrefectures(final MapPreferences preferences, final MapPanel panel) {
		final Collection<Prefecture> ret = new ArrayList<Prefecture>();
		for (final Map.Entry<Shape, String> entry : ShapeIO.readShapes(Const.Prefecture.PREFECTURES).entrySet()) {
			final String[] values = entry.getValue().split("_");
			if (values.length == 2) {
				final String idString = values[0];
				final int id = Integer.parseInt(idString);
				final String label = values[1];
				ret.add(new Prefecture(entry.getKey(), label, idString, preferences.getTyomeFillColor(id == 30
						|| id == 13 ? (id + 2) % 6 + 1 : id % 6 + 1), panel));
			} else {
				System.out.println("WARNING: 都道府県名の表記がおかしいです。" + entry.getValue());
			}
		}
		return ret;
	}
}
