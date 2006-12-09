package map;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.Const.Sdf2500;
import map.KsjRailway.Railway;
import map.KsjRailway.Station;

import ksj.LoadKsj;
import ksj.ShapeIO;

/**
 * 国土数値情報の行政界・海岸線（面）から作成された1つの都道府県を管理するクラスです。
 * @author zenjiro
 * 2005/11/10
 */
public class Prefecture {
	/**
	 * 外接長方形
	 */
	private final Rectangle2D bounds;

	/**
	 * 都道府県内の市区町村の一覧
	 */
	private Collection<City> cities;

	/**
	 * 色
	 */
	private final Color color;

	/**
	 * 高精度の領域
	 */
	private Shape fineShape;

	/**
	 * 都道府県コード
	 */
	private final String id;

	/**
	 * 都道府県名
	 */
	private final String label;

	/**
	 * 領域
	 */
	private final Shape shape;

	/**
	 * 地図を描画するパネル
	 */
	private final MapPanel panel;

	/**
	 * 簡略化した国土数値情報の道路データ
	 */
	private Collection<Railway> ksjSimpleRoad;

	/**
	 * 国土数値情報の鉄道データの曲線
	 */
	final private Collection<Railway> ksjRailwayCurves;

	/**
	 * 国土数値情報の鉄道データの駅
	 */
	final private Collection<Station> ksjRailwayStations;

	/**
	 * 新しい都道府県を初期化します。
	 * @param shape 領域
	 * @param label 都道府県名
	 * @param id 都道府県コード
	 * @param color 色
	 * @param panel 地図を描画するパネル
	 */
	public Prefecture(final Shape shape, final String label, final String id, final Color color, final MapPanel panel) {
		this.shape = shape;
		this.panel = panel;
		this.bounds = shape.getBounds2D();
		this.label = label;
		this.id = id;
		this.color = color;
		this.cities = new ArrayList<City>();
		this.ksjSimpleRoad = new ArrayList<Railway>();
		this.ksjRailwayCurves = new ArrayList<Railway>();
		this.ksjRailwayStations = new ArrayList<Station>();
	}

	/**
	 * 市区町村の一覧を開放します。
	 */
	public void freeCities() {
		if (!this.cities.isEmpty()) {
			this.cities.clear();
		}
	}

	/**
	 * 高精度の情報を開放します。
	 */
	public void freeFine() {
		for (final City city : this.cities) {
			city.freeFineShape();
		}
		this.fineShape = null;
		// since 4.17
		this.freeKsjRailwayCurves();
		this.freeKsjRailwayStations();
	}

	/**
	 * 高精度の情報を読み込みます。
	 * @throws IOException 
	 */
	public void loadFine() throws IOException {
		this.loadFineShape();
		this.loadFineCities();
		// since 4.17
		this.loadKsjRailwayStations();
		this.loadKsjRailwayCurves();
	}

	/**
	 * @return 高精度の情報があるかどうか
	 */
	public boolean hasFine() {
		return this.fineShape != null;
	}

	/**
	 * @return 外接長方形
	 */
	public Rectangle2D getBounds() {
		return this.bounds;
	}

	/**
	 * @return 市区町村の一覧
	 * @throws IOException 入出力例外
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 */
	public Collection<City> getCities() throws UnsupportedEncodingException, IOException {
		if (this.cities.isEmpty()) {
			this.loadCities();
		}
		return this.cities;
	}

	/**
	 * @return 色
	 */
	public Color getColor() {
		return this.color;
	}

	/**
	 * @return 高精度の領域
	 */
	public Shape getFineShape() {
		return this.fineShape;
	}

	/**
	 * @return 都道府県コード
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return 都道府県名
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * @return 領域
	 */
	public Shape getShape() {
		return this.shape;
	}

	/**
	 * @return 市区町村の一覧を持っているかどうか
	 */
	public boolean hasCities() {
		return !this.cities.isEmpty();
	}

	/**
	 * 市区町村の一覧を読み込みます。
	 * @throws IOException 入出力例外
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 */
	public void loadCities() throws UnsupportedEncodingException, IOException {
		final Map<Shape, String> shapes = LoadKsj.loadShapesUTM(new File(Const.Ksj.CACHE_DIR), Const.Ksj.TXT_PREFIX
				+ this.id + Const.Ksj.TXT_SUFFIX, Const.Ksj.CACHE_DIR + File.separator + Const.Ksj.CACHE_PREFIX
				+ this.id + Const.Ksj.CACHE_SUFFIX, true, this.panel);
		final Map<String, URL> urls = new ConcurrentHashMap<String, URL>();
		final java.util.Scanner scanner = new java.util.Scanner(new InputStreamReader(Sdf2500.FILE_LIST.openStream(),
				"SJIS"));
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			if (!line.startsWith("#")) {
				final String[] strings = line.split(",");
				if (strings.length == 3) {
					final String filename = strings[2];
					final Pattern pattern = Pattern.compile(".+/([0-9][0-9][0-9][0-9][0-9])\\.lzh");
					final Matcher matcher = pattern.matcher(filename);
					if (matcher.matches()) {
						final String cityID = matcher.group(1);
						final URL url = new URL(Const.Sdf2500.BASE_URL + filename);
						urls.put(cityID, url);
					} else {
						System.out.println("WARNING: ファイル名の形式が不正です。" + filename);
					}
				} else {
					System.out.println("WARNING: ファイル一覧の形式が不正です。" + line);
				}
			}
		}
		for (final Map.Entry<Shape, String> entry : shapes.entrySet()) {
			final String[] values = entry.getValue().split("_");
			if (values.length == 4) {
				this.cities.add(new City(entry.getKey(), values[3], values[2], urls.get(values[2]), this.label,
						this.panel));
			} else {
				System.out.println("WARNING: 市区町村名の表記がおかしいです。" + entry.getValue());
			}
		}
	}

	/**
	 * 高精度の市区町村の一覧を読み込みます。
	 * @throws IOException 
	 */
	private void loadFineCities() throws IOException {
		final Map<String, Collection<City>> citiesMap = new ConcurrentHashMap<String, Collection<City>>();
		for (final City city : this.cities) {
			if (!citiesMap.containsKey(city.getId())) {
				citiesMap.put(city.getId(), new ArrayList<City>());
			}
			citiesMap.get(city.getId()).add(city);
		}
		final Map<Shape, String> shapes = LoadKsj.loadShapesUTM(new File(Const.Ksj.CACHE_DIR), Const.Ksj.TXT_PREFIX
				+ this.id + Const.Ksj.TXT_SUFFIX, Const.Ksj.CACHE_DIR + File.separator + Const.Ksj.CACHE_PREFIX_FINE
				+ this.id + Const.Ksj.CACHE_SUFFIX_FINE, false, this.panel);
		for (final Map.Entry<Shape, String> entry : shapes.entrySet()) {
			final String[] values = entry.getValue().split("_");
			if (values.length == 4) {
				final String id2 = values[2];
				if (citiesMap.containsKey(id2)) {
					final Collection<City> cities2 = citiesMap.get(id2);
					for (final City city : cities2) {
						if (!city.hasFineShape()) {
							city.setFineShape(entry.getKey());
							break;
						}
					}
				} else {
					System.out.println("WARNING: 高精度の市区町村にはあって荒い市区町村にはありません。" + entry.getValue());
				}
			} else {
				System.out.println("WARNING: 市区町村名の形式が不正です。" + entry.getValue());
			}
		}
	}

	/**
	 * 高精度の領域を読み込みます。
	 */
	private void loadFineShape() {
		final Map<Shape, String> map = ShapeIO.readShapes(Prefecture.class.getResourceAsStream(Const.DIR
				+ Const.Prefecture.PREFECTURE_PREFIX + this.id + Const.Prefecture.PREFECTURE_SUFFIX));
		if (map.isEmpty()) {
			System.out.println("WARNING: 高精度の都道府県の情報が読み込めませんでした。" + Const.Prefecture.PREFECTURE_PREFIX + this.id
					+ Const.Prefecture.PREFECTURE_SUFFIX);
		} else {
			for (final Shape shape2 : map.keySet()) {
				this.fineShape = shape2;
			}
		}
	}

	/**
	 * 簡略化した道路データの読み込みます。
	 * @since 5.01
	 */
	public void loadKsjSimpleRoad() {
		if (this.ksjSimpleRoad.isEmpty()) {
			for (final Map.Entry<Shape, String> entry : ShapeIO.readShapes(
					Prefecture.class.getResourceAsStream(Const.DIR + Const.Ksj.ROAD_SIMPLE_PREFIX + this.id
							+ Const.Ksj.ROAD_SUFFIX)).entrySet()) {
				this.ksjSimpleRoad.add(new Railway(entry.getKey(), entry.getValue()));
			}
		}
	}

	/**
	 * 鉄道データの曲線を読み込みます。
	 * @since 4.17
	 */
	private void loadKsjRailwayCurves() {
		if (this.ksjRailwayCurves.isEmpty()) {
			for (final Map.Entry<Shape, String> entry : ShapeIO.readShapes(
					Prefecture.class.getResourceAsStream(Const.DIR + Const.Ksj.RAILWAY_CURVES_PREFIX + this.id
							+ Const.Ksj.RAILWAY_SUFFIX)).entrySet()) {
				this.ksjRailwayCurves.add(new Railway(entry.getKey(), entry.getValue()));
			}
			for (final Map.Entry<Shape, String> entry : ShapeIO.readShapes(
					Prefecture.class.getResourceAsStream(Const.DIR + Const.Ksj.ROAD_PREFIX + this.id
							+ Const.Ksj.ROAD_SUFFIX)).entrySet()) {
				this.ksjRailwayCurves.add(new Railway(entry.getKey(), entry.getValue()));
			}
		}
	}

	/**
	 * 鉄道データの駅を読み込みます。
	 * @since 4.17
	 */
	private void loadKsjRailwayStations() {
		if (this.ksjRailwayStations.isEmpty()) {
			for (final Map.Entry<Shape, String> entry : ShapeIO.readShapes(
					Prefecture.class.getResourceAsStream(Const.DIR + Const.Ksj.RAILWAY_STATIONS_PREFIX + this.id
							+ Const.Ksj.RAILWAY_SUFFIX)).entrySet()) {
				this.ksjRailwayStations.add(new Station(entry.getKey(), entry.getValue()));
			}
		}
	}

	/**
	 * 簡略化した道路データを開放します。
	 * @since 5.01
	 */
	public void freeKsjSimpleRoad() {
		freeKsjRailway(this.ksjSimpleRoad);
	}

	/**
	 * 鉄道データの曲線を開放します。
	 * @since 4.17
	 */
	public void freeKsjRailwayCurves() {
		freeKsjRailway(this.ksjRailwayCurves);
	}

	/**
	 * 鉄道データの駅を開放します。
	 * @since 4.17
	 */
	public void freeKsjRailwayStations() {
		freeKsjRailway(this.ksjRailwayStations);
	}

	/**
	 * 鉄道データを開放します。
	 * @param railways 鉄道データ
	 * @since 4.17
	 */
	private void freeKsjRailway(final Collection<? extends Railway> railways) {
		if (!railways.isEmpty()) {
			railways.clear();
		}
	}

	/**
	 * @return 鉄道データの曲線
	 */
	public Collection<Railway> getKsjRailwayCurves() {
		return this.ksjRailwayCurves;
	}

	/**
	 * @return 鉄道データの直線
	 */
	public Collection<Railway> getKsjRailwayLines() {
		return this.ksjSimpleRoad;
	}

	/**
	 * @return 鉄道データの駅
	 */
	public Collection<Station> getKsjRailwayStations() {
		return this.ksjRailwayStations;
	}

}
