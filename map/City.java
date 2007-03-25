package map;

import isj.ISJUtil;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import ksj.ShapeIO;
import map.KsjRailway.Railway;
import map.KsjRailway.Station;
import shop.Shop;

/**
 * 国土数値情報の行政界・海岸線（面）から作成された1つの市区町村を管理するクラスです。
 * @author zenjiro
 * 2005/11/11
 */
public class City {
	/**
	 * 数値地図2500（空間データ基盤）を読み込んだかどうか
	 */
	private boolean has2500;

	/**
	 * 外接長方形
	 */
	private final Rectangle2D bounds;

	/**
	 * 高精度の領域
	 */
	private Shape fineShape;

	/**
	 * 市区町村コード
	 */
	private final String id;

	/**
	 * 市区町村名
	 */
	private final String label;

	/**
	 * 都道府県名
	 */
	private final String prefecture;

	/**
	 * 領域
	 */
	private final Shape shape;

	/**
	 * 数値地図2500（空間データ基盤）のURL
	 */
	private final URL url;

	/**
	 * 街区レベル位置参照情報
	 */
	private Map<String, Point2D> isj;

	/**
	 * 街区レベル位置参照情報のラベル位置
	 */
	private Map<Point2D, String> isjLabels;

	/**
	 * コンビニの一覧
	 * @since 3.16
	 */
	private Collection<PointData> shops;

	/**
	 * 地図を描画するパネル。ステータスバーのメッセージを伝えるために持ちます。
	 */
	private final MapPanel panel;

	/**
	 * 高精度の国土数値情報の道路データ
	 * @since 5.04
	 */
	private Collection<Railway> ksjFineRoad;

	/**
	 * 市区町村を初期化します。
	 * @param shape 領域
	 * @param label 市区町村名
	 * @param id 市区町村コード
	 * @param url 数値地図2500（空間データ基盤）のURL
	 * @param prefecture 都道府県名
	 * @param panel 地図を描画するパネル。ステータスバーのメッセージを伝えるために持ちます。
	 */
	public City(final Shape shape, final String label, final String id, final URL url, final String prefecture,
			final MapPanel panel) {
		this.shape = shape;
		this.panel = panel;
		this.bounds = shape.getBounds2D();
		this.label = label;
		this.id = id;
		this.url = url;
		this.prefecture = prefecture;
		this.isjLabels = new ConcurrentHashMap<Point2D, String>();
		this.ksjFineRoad = new ConcurrentLinkedQueue<Railway>();
		// since 6.1.2
		this.ksjRailwayCurves = new ConcurrentLinkedQueue<Railway>();
		this.ksjRailwayStations = new ConcurrentLinkedQueue<Station>();
	}

	/**
	 * 街区レベル位置参照情報のラベル位置を空にします。
	 */
	public void clearIsjLabels() {
		this.isjLabels.clear();
	}

	/**
	 * 高精度の領域を開放します。
	 */
	public void freeFineShape() {
		this.fineShape = null;
	}

	/**
	 * 街区レベル位置参照情報を開放します。
	 */
	public void freeIsj() {
		this.isj = null;
	}

	/**
	 * @return 外接長方形
	 */
	public Rectangle2D getBounds() {
		return this.bounds;
	}

	/**
	 * @return 高精度の領域
	 */
	public Shape getFineShape() {
		return this.fineShape;
	}

	/**
	 * @return 市区町村コード
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return 街区レベル位置参照情報
	 */
	public Map<String, Point2D> getIsj() {
		return this.isj;
	}

	/**
	 * @return 街区レベル位置参照情報のラベル位置
	 */
	public Map<Point2D, String> getIsjLabels() {
		return this.isjLabels;
	}

	/**
	 * @return 市区町村名
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
	 * @return 数値地図2500（空間データ基盤）のURL
	 */
	public URL getURL() {
		return this.url;
	}

	/**
	 * @return 数値地図2500（空間データ基盤）を読み込んだかどうか
	 */
	public boolean has2500() {
		return this.has2500;
	}

	/**
	 * @return 高精度の領域を持っているかどうか
	 */
	public boolean hasFineShape() {
		return this.fineShape != null;
	}

	/**
	 * @return 街区レベル位置参照情報を持っているかどうか
	 */
	public boolean hasIsj() {
		return this.isj != null;
	}

	/**
	 * 街区レベル位置参照情報をダウンロードし、読み込みます。
	 * @throws IOException 
	 */
	public void loadIsj() throws IOException {
		this.isj = ISJUtil.loadIsj(this.id, this.panel);
	}

	/**
	 * @param shape 高精度の領域
	 */
	public void setFineShape(final Shape shape) {
		this.fineShape = shape;
	}

	/**
	 * @param has2500 数値地図2500（空間データ基盤）を読み込んだかどうか 
	 */
	public void setHas2500(final boolean has2500) {
		this.has2500 = has2500;
	}

	/**
	 * @since 3.16
	 * @return 店舗の一覧
	 */
	public Collection<PointData> getShops() {
		return this.shops;
	}

	/**
	 * @since 3.16
	 * @return 店舗の一覧を持っているかどうか
	 */
	public boolean hasShops() {
		return this.shops != null;
	}

	/**
	 * 店舗の一覧を読み込みます。
	 * @throws InterruptedException 
	 * @since 3.16
	 */
	public void loadShops() throws InterruptedException {
		this.shops = new ArrayList<PointData>();
		final Map<String, Point2D> tempIsj = new ConcurrentHashMap<String, Point2D>();
		for (final Map.Entry<String, Point2D> entry4 : this.isj.entrySet()) {
			tempIsj.put(entry4.getKey().replaceAll(",", ""), entry4.getValue());
		}
		// since 4.07
		final Map<Point2D, String> points = new Shop().getShops(this.id, this.label, this.prefecture, this.isj,
				this.panel);
		for (final Map.Entry<Point2D, String> entry : points.entrySet()) {
			final Point2D point = entry.getKey();
			final String attribute = entry.getValue();
			final PointData pointData = new PointData(attribute, PointData.CLASSIFICATION_UNKNOWN, point.getX(), point
					.getY());
			pointData.setAttribute(attribute);
			this.shops.add(pointData);
		}

	}

	/**
	 * @return 都道府県名
	 */
	public String getPrefecture() {
		return this.prefecture;
	}

	/**
	 * 高精度の道路データを開放します。
	 * @since 5.04
	 */
	public void freeKsjFineRoad() {
		if (!this.ksjFineRoad.isEmpty()) {
			this.ksjFineRoad.clear();
		}
	}

	/**
	 * @return 高精度の国土数値情報の道路データ
	 * @since 5.04
	 */
	public Collection<Railway> getKsjFineRoad() {
		return this.ksjFineRoad;
	}

	/**
	 * @return 高精度の国土数値情報の道路データを持っているかどうか
	 * @since 5.04
	 */
	public boolean hasKsjFineRoad() {
		return !this.ksjFineRoad.isEmpty();
	}

	/**
	 * @return 国土数値情報の駅を持っているかどうか
	 * @since 6.1.2
	 */
	public boolean hasKsjRailwayStations() {
		return !this.ksjRailwayStations.isEmpty();
	}

	/**
	 * @return 国土数値情報の鉄道データを持っているかどうか
	 * @since 6.1.2
	 */
	public boolean hasKsjRailwayCurves() {
		return !this.ksjRailwayCurves.isEmpty();
	}

	/**
	 * 高精度な国土数値情報の道路データを読み込みます。
	 * @return 読み込んだかどうか
	 * @since 5.04
	 */
	public boolean loadKsjFineRoad() {
		if (this.ksjFineRoad.isEmpty()) {
			final InputStream in = City.class.getResourceAsStream(Const.DIR + Const.KSJ.ROAD_FINE_PREFIX + this.id
					+ Const.KSJ.ROAD_SUFFIX);
			if (in != null) {
				for (final Map.Entry<Shape, String> entry : ShapeIO.readShapes(in).entrySet()) {
					this.ksjFineRoad.add(new Railway(entry.getKey(), entry.getValue()));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 国土数値情報の鉄道データの曲線
	 * @since 6.2.2
	 */
	final private Collection<Railway> ksjRailwayCurves;

	/**
	 * 国土数値情報の鉄道データの駅
	 * @since 6.2.2
	 */
	final private Collection<Station> ksjRailwayStations;

	/**
	 * 鉄道データの曲線を読み込みます。
	 * @return 読み込んだかどうか
	 * @since 6.2.2
	 */
	public boolean loadKsjRailwayCurves() {
		if (this.ksjRailwayCurves.isEmpty()) {
			final InputStream in = Prefecture.class.getResourceAsStream(Const.DIR + Const.KSJ.RAILWAY_CURVES_PREFIX
					+ this.id + Const.KSJ.RAILWAY_SUFFIX);
			if (in != null) {
				for (final Map.Entry<Shape, String> entry : ShapeIO.readShapes(in).entrySet()) {
					this.ksjRailwayCurves.add(new Railway(entry.getKey(), entry.getValue()));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 鉄道データの駅を読み込みます。
	 * @return 読み込んだかどうか
	 * @since 6.2.2
	 */
	public boolean loadKsjRailwayStations() {
		if (this.ksjRailwayStations.isEmpty()) {
			final InputStream in = Prefecture.class.getResourceAsStream(Const.DIR + Const.KSJ.RAILWAY_STATIONS_PREFIX
					+ this.id + Const.KSJ.RAILWAY_SUFFIX);
			if (in != null) {
				for (final Map.Entry<Shape, String> entry : ShapeIO.readShapes(in).entrySet()) {
					this.ksjRailwayStations.add(new Station(entry.getKey(), entry.getValue()));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 鉄道データの曲線を開放します。
	 * @since 6.2.2
	 */
	public void freeKsjRailwayCurves() {
		freeKsjRailway(this.ksjRailwayCurves);
	}

	/**
	 * 鉄道データの駅を開放します。
	 * @since 6.2.2
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
	 * @return 鉄道データの駅
	 */
	public Collection<Station> getKsjRailwayStations() {
		return this.ksjRailwayStations;
	}
}
