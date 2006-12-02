package map;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Stack;

/**
 * 弧を表すクラスです。
 * @author zenjiro
 */
class ArcData {
	/**
	 * PathIterator の currentSegment の戻り値の配列の大きさ
	 */
	private static final int COORDS_NUMBER = 6;

	/**
	 * JR を表す定数です。
	 */
	public static final int RAILWAY_JR = 1;

	/**
	 * JR 新幹線を表す定数です。
	 */
	public static final int RAILWAY_JR_SHINKANSEN = 2;

	/**
	 * JR 以外の鉄道を表す定数です。
	 */
	public static final int RAILWAY_OTHER = 0;

	/**
	 * 主要地方道を表す定数です。
	 */
	public static final int ROAD_CHIHODO = 3;

	/**
	 * 高速道路を表す定数です。
	 */
	public static final int ROAD_HIGHWAY = 5;

	/**
	 * 県道を表す定数です。
	 */
	public static final int ROAD_KENDO = 2;

	/**
	 * 国道を表す定数です。
	 */
	public static final int ROAD_KOKUDO = 4;

	/**
	 * 名前のある一般の道路を表す定数です。
	 */
	public static final int ROAD_MAJOR = 1;

	/**
	 * 通常の道路を表す定数です。
	 */
	public static final int ROAD_NORMAL = 0;

	/**
	 * 多重連結のポリゴンを表現するために外周と内周の間をつなぐアークを表す定数です。
	 */
	public static final int TAG_DETACHED = COORDS_NUMBER;

	/**
	 * 図郭にかかるポリゴンを閉じるための図郭線の一部であるアークを表す定数です。
	 */
	public static final int TAG_EDGE_OF_MAP = 5;

	/**
	 * 座標系の境界線にかかるポリゴンを閉じるための座標系境界線の一部であるアークを表す定数です。
	 */
	public static final int TAG_EDGE_OF_WORLD = 15;

	/**
	 * 実際に描画される通常のアークを表す定数です。
	 */
	public static final int TAG_NORMAL = 0;

	/**
	 * 高架を表す定数です。
	 */
	public static final int TAG_OVERPASS = 1;

	/**
	 * 地下、トンネル部を表す定数です。
	 */
	public static final int TAG_TUNNEL = 2;

	/**
	 * 未確定行政界、河口部の海岸先等、ポリゴンを形成するために引かれる不確実な界線を表す定数です。
	 */
	public static final int TAG_UNCERTAIN = 4;

	/**
	 * 橋の下の水涯線を表す定数です。
	 */
	public static final int TAG_UNDER_BRIDGE = 3;

	/**
	 * 大字、町丁目界を表す定数です。
	 */
	public static final int TYPE_GYOUSEI_CHOME = 4;

	/**
	 * 都市、東京都の区界を表す定数です。
	 */
	public static final int TYPE_GYOUSEI_CITY = 2;

	/**
	 * 都道府県界を表す定数です。
	 */
	public static final int TYPE_GYOUSEI_PREFECTURE = 1;

	/**
	 * 町村、指定都市の区界を表す定数です。
	 */
	public static final int TYPE_GYOUSEI_VILLAGE = 3;

	/**
	 * 内水面界を表す定数です。
	 */
	public static final int TYPE_MIZU_INSIDE = 13;

	/**
	 * 海岸線を表す定数です。
	 */
	public static final int TYPE_MIZU_SEASHORE = 14;

	/**
	 * 鉄道を表す定数です。
	 */
	public static final int TYPE_RAILWAY = 5;

	/**
	 * 道路線を表す定数です。
	 */
	public static final int TYPE_ROAD = COORDS_NUMBER;

	/**
	 * 不明な図式分類コードを表す定数です。
	 */
	public static final int TYPE_UNKNOWN = 0;

	/**
	 * 墓地界を表す定数です。
	 */
	public static final int TYPE_ZYOTI_GRAVEYARD = 11;

	/**
	 * その他の場地界を表す定数です。
	 */
	public static final int TYPE_ZYOTI_OTHER = 12;

	/**
	 * 都市公園界を表す定数です。
	 */
	public static final int TYPE_ZYOTI_PARK = 8;

	/**
	 * 鉄道敷界を表す定数です。
	 */
	public static final int TYPE_ZYOTI_RAILROAD = 7;

	/**
	 * 学校敷地界を表す定数です。
	 */
	public static final int TYPE_ZYOTI_SCHOOL = 9;

	/**
	 * 神社、寺院境内界を表す定数です。
	 */
	public static final int TYPE_ZYOTI_TEMPLE = 10;

	/**
	 * 個別番号
	 */
	private String arcName; // 個別番号

	/**
	 * 属性
	 */
	private String attribute; // 属性

	/**
	 * 属性を表示する x 座標
	 */
	private double attributeX; // 属性を表示する x 座標

	/**
	 * 属性を表示する y 座標
	 */
	private double attributeY; // 属性を表示する y 座標

	/**
	 * 図式分類コード
	 */
	private int classification; // 図式分類コード

	/**
	 * パスの始点
	 */
	private final Point2D firstPoint;

	/**
	 * パスの終点
	 */
	private final Point2D lastPoint;
	
	/**
	 * 構成するパス
	 */
	private GeneralPath path; // 構成するパス
	
	/**
	 * 鉄道の種類
	 */
	private int railwayType; // 鉄道の種類

	/**
	 * 逆向きのパス
	 */
	private GeneralPath reversalPath;

	/**
	 * 道路の種類
	 */
	private int roadType; // 道路の種類

	/**
	 * 線種タグ
	 */
	private int tag; // 線種タグ

	/**
	 * 弧を初期化します。
	 * @param string 弧の個別番号
	 * @param generalPath 弧を構成するパス
	 * @param type 弧の図式分類コード
	 * @param lineTag 弧の線種タグ
	 */
	ArcData(final String string, final GeneralPath generalPath, final int type, final int lineTag) {
		this.arcName = string;
		this.path = generalPath;
		this.classification = type;
		this.tag = lineTag;
		Point2D first = null;
		double lastX = Double.NaN;
		double lastY = Double.NaN;
		for (final PathIterator iter = this.path.getPathIterator(new AffineTransform()); !iter
		.isDone(); iter.next()) {
			final double[] coords = new double[COORDS_NUMBER];
			iter.currentSegment(coords);
			final double x = coords[0];
			final double y = coords[1];
			if (first == null) {
				first = new Point2D.Double(x, y);
			}
			lastX = x;
			lastY = y;
		}
		this.firstPoint = first;
		this.lastPoint = new Point2D.Double(lastX, lastY);
	}

	/**
	 * 個別番号を取得します。
	 * @return 個別番号
	 */
	String getArcName() {
		return this.arcName;
	}

	/**
	 * 属性を取得します。
	 * @return 属性
	 */
	String getAttribute() {
		return this.attribute;
	}

	/**
	 * 属性を描画する x 座標を取得します。
	 * @return x 座標
	 */
	public double getAttributeX() {
		return this.attributeX;
	}

	/**
	 * 属性を描画する y 座標を取得します。
	 * @return x 座標
	 */
	public double getAttributeY() {
		return this.attributeY;
	}

	/**
	 * 図式分類コードを取得します。
	 * @return 図式分類コード
	 */
	int getClassification() {
		return this.classification;
	}

	/**
	 * 弧を構成するパスを取得します。
	 * @return パス
	 */
	GeneralPath getPath() {
		return this.path;
	}

	/**
	 * 鉄道の種類を取得します。
	 * @return 鉄道の種類
	 */
	int getRailwayType() {
		return this.railwayType;
	}
	
	/**
	 * 弧を構成するパスを逆向きにしたパスを取得します。
	 * @return パス
	 */
	GeneralPath getReversalPath() {
		if (this.reversalPath == null) {
			final GeneralPath ret = new GeneralPath();
			final Stack<Point2D> stack = new Stack<Point2D>();
			for (final PathIterator iter = this.path.getPathIterator(new AffineTransform()); !iter
					.isDone(); iter.next()) {
				final double[] coords = new double[COORDS_NUMBER];
				iter.currentSegment(coords);
				final double x = coords[0];
				final double y = coords[1];
				stack.push(new Point2D.Double(x, y));
			}
			final Point2D localFirstPoint = stack.pop();
			ret.moveTo((float) localFirstPoint.getX(), (float) localFirstPoint.getY());
			while (!stack.isEmpty()) {
				final Point2D point = stack.pop();
				if ((this.tag == ArcData.TAG_DETACHED) || (this.tag == ArcData.TAG_EDGE_OF_MAP)
						|| (this.tag == ArcData.TAG_EDGE_OF_WORLD)) {
					ret.lineTo((float) point.getX(), (float) point.getY());
				} else {
					ret.lineTo((float) point.getX(), (float) point.getY());
				}
			}
			this.reversalPath = ret;
			return ret;
		} else {
			return this.reversalPath;
		}
	}

	/**
	 * 道路の種類を取得します。
	 * @return 道路の種類
	 */
	int getRoadType() {
		return this.roadType;
	}

	/**
	 * 線種タグを取得します。
	 * @return 線種タグ
	 */
	int getTag() {
		return this.tag;
	}

	/**
	 * 属性を設定します。
	 * @param string 属性
	 */
	void setAttribute(final String string) {
		this.attribute = string;
	}

	/**
	 * 属性を描画する座標を設定します。
	 * @param x x 座標
	 * @param y y 座標
	 */
	public void setAttributeLocation(final double x, final double y) {
		this.attributeX = x;
		this.attributeY = y;
	}

	/**
	 * 鉄道の種類を設定します。
	 * @param type 鉄道の種類
	 */
	void setRailwayType(final int type) {
		this.railwayType = type;
	}

	/**
	 * 道路の種類を設定します。
	 * @param type 道路の種類
	 */
	void setRoadType(final int type) {
		this.roadType = type;
	}

	@Override
	public String toString() {
		return "[Arc " + this.arcName + "(" + this.attribute + ")]";
	}
	
	/**
	 * @return パスを直線にしたもの
	 */
	Line2D getLine() {
		return new Line2D.Double(this.firstPoint, this.lastPoint);
	}
	
	/**
	 * @return パスを逆向きの直線にしたもの
	 */
	Line2D getReversalLine() {
		return new Line2D.Double(this.lastPoint, this.firstPoint);
	}
}
