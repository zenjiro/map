package map;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 地図データを扱うクラスです。
 * @author zenjiro
 * 作成日: 2003/12/11
 */
public class MapData implements Comparable<MapData> {
	/**
	 * 隣接グラフ
	 */
	private Map<String, Collection<PolygonData>> adjacentGraph; // String -> Colleciton<Polygon> の Map

	/**
	 * 弧とポリゴンの対応表
	 */
	private Map<ArcData, Collection<String>> arcPolygonMap; // Arc -> Collection<String> の Map

	/**
	 * 属性と代表点の一覧
	 */
	private Map<String, Collection<Point2D>> attributes = null;

	/**
	 * ベースディレクトリ
	 */
	private String baseDir;

	/**
	 * 図郭の端にあるポリゴンの一覧
	 */
	private Map<String, String> edgePolygons; // String -> String の Map

	/**
	 * 駅の点データ
	 */
	private Map<String, PointData> eki; // String -> Point の Map

	/**
	 * 行政界の弧データ
	 */
	private Map<String, ArcData> gyousei; // String  -> Arc の Map

	/**
	 * 高速道路と国道の弧データ
	 */
	private Map<String, ArcData> largeRoadArc;

	/**
	 * 図葉名
	 */
	private String mapName;

	/**
	 * 内水面のポリゴンデータ
	 */
	private Map<String, PolygonData> mizu; // String -> Polygon の Map

	/**
	 * 内水面の弧データ
	 */
	private Map<String, ArcData> mizuArc; // String -> Arc の Map

	/**
	 * その他の弧データ
	 */
	private Map<String, ArcData> others; // String -> Arc の Map

	/**
	 * この地図が表す領域（仮想座標）
	 */
	private Shape rectangle; // この地図が表す領域（仮想座標）

	/**
	 * 道路の弧データ
	 */
	private Map<String, ArcData> roadArc; // String -> Arc の Map

	/**
	 * 市町界のポリゴンデータ
	 */
	private Map<String, PolygonData> si_tyo; // String -> Polygon の Map

	/**
	 * 建物のポリゴンデータ
	 */
	private Map<String, PolygonData> tatemono; // String -> Tatemono の Map

	/**
	 * 建物界の弧データ
	 */
	private Map<String, ArcData> tatemonoArc; // String -> Arc の Map

	/**
	 * 町丁目のポリゴンデータ
	 */
	private Map<String, PolygonData> tyome; // String -> Polygon の Map

	/**
	 * 城地のポリゴンデータ
	 */
	private Map<String, PolygonData> zyouti; // String -> Polygon の Map

	/** 地図を初期化します。
	 * @param baseDir 地図データのあるディレクトリ
	 * @param mapName 地図の名前
	 * @throws NumberFormatException 数値形式例外
	 * @throws IOException 入出力例外
	 */
	public MapData(final String baseDir, final String mapName) throws NumberFormatException,
		IOException {
		this.setBaseDir(baseDir);
		this.setMapName(mapName);
		this.loadRectangle();
		this.edgePolygons = new ConcurrentHashMap<String, String>();
		this.arcPolygonMap = new ConcurrentHashMap<ArcData, Collection<String>>();
	}

	/** 丁目の隣接グラフを計算します。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	private void calcAdjacencyGraph() throws FileNotFoundException, IOException {
		this.adjacentGraph = new ConcurrentHashMap<String, Collection<PolygonData>>();
		if (this.tyome == null) {
			this.loadTyome();
		}
		for (final Collection<String> polygonNames : this.arcPolygonMap.values()) {
			final Collection<PolygonData> adjacencyPolygons = new ArrayList<PolygonData>();
			for (final String polygonName : polygonNames) {
				final PolygonData polygon = this.tyome.get(polygonName);
				adjacencyPolygons.add(polygon);
			}
			if (adjacencyPolygons.size() == 2) {
				final Iterator<PolygonData> iter2 = adjacencyPolygons.iterator();
				final PolygonData polygon = iter2.next();
				final PolygonData polygon2 = iter2.next();
				if (polygon != null) {
					if (!this.adjacentGraph.containsKey(polygon.getPolygonName())) {
						this.adjacentGraph.put(polygon.getPolygonName(),
							new ArrayList<PolygonData>());
					}
					if (!this.adjacentGraph.containsKey(polygon2.getPolygonName())) {
						this.adjacentGraph.put(polygon2.getPolygonName(),
							new ArrayList<PolygonData>());
					}
					this.adjacentGraph.get(polygon.getPolygonName()).add(polygon2);
					this.adjacentGraph.get(polygon2.getPolygonName()).add(polygon);
				}
			}
		}
	}

	/** このオブジェクトと他のオブジェクトの大小関係を比較します。
	 * @param o 比較対象のオブジェクト
	 * @return 大小関係
	 */
	public int compareTo(final MapData o) {
		return this.mapName.compareTo(o.getMapName());
	}

	/**
	 * 駅の点データを開放します。
	 */
	void freeEki() {
		this.eki = null;
	}

	/**
	 * 行政界の弧データを開放します。
	 */
	void freeGyousei() {
		this.gyousei = null;
	}

	/**
	 * 高速道路を国道の弧データを開放します。
	 */
	void freeLargeRoadArc() {
		this.largeRoadArc = null;
	}

	/**
	 * 水のポリゴンデータを開放します。
	 */
	void freeMizu() {
		this.mizu = null;
	}

	/**
	 * 内水面界の弧データを開放します。
	 */
	void freeMizuArc() {
		this.mizuArc = null;
	}

	/**
	 * 鉄道、場地界の弧データを開放します。
	 */
	void freeOthers() {
		this.others = null;
	}

	/**
	 * 道路の弧データを開放します。
	 */
	void freeRoadArc() {
		this.roadArc = null;
	}

	/**
	 * 市町界のポリゴンデータを開放します。
	 */
	void freeSi_tyo() {
		this.si_tyo = null;
	}

	/**
	 * 建物のポリゴンデータを開放します。
	 */
	void freeTatemono() {
		this.tatemono = null;
	}

	/**
	 * 建物の弧データを開放します。
	 */
	void freeTatemonoArc() {
		this.tatemonoArc = null;
	}

	/**
	 * 大字、町丁目のポリゴンデータを開放します。
	 */
	void freeTyome() {
		this.tyome = null;
	}

	/**
	 * 場地界のポリゴンデータを開放します。
	 */
	void freeZyouti() {
		this.zyouti = null;
	}

	/** 丁目の隣接グラフを取得します。
	 * @return 丁目の隣接グラフ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, Collection<PolygonData>> getAdjacentGraph() throws FileNotFoundException,
		IOException {
		if (this.adjacentGraph == null) {
			this.calcAdjacencyGraph();
		}
		return this.adjacentGraph;
	}

	/**
	 * 弧とポリゴンのMapを取得します。
	 * @return 弧とポリゴンの Map
	 */
	Map<ArcData, Collection<String>> getArcPolygonMap() {
		return this.arcPolygonMap;
	}

	/**
	 * @return 属性の一覧
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 * @throws FileNotFoundException ファイル未検出例外
	 * @throws IOException 入出力例外
	 */
	public Map<String, Collection<Point2D>> getAttributes() throws UnsupportedEncodingException,
		FileNotFoundException, IOException {
		if (this.attributes == null) {
			this.attributes = new ConcurrentHashMap<String, Collection<Point2D>>();
			final Map<String, ArcData> tempArc = new ConcurrentHashMap<String, ArcData>();
			if (this.gyousei == null) {
				final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
					+ File.separator + "gyousei" + File.separator + "gyousei.arc";
				if (new File(fileName).canRead()) {
					this.loadArc(tempArc, new BufferedReader(new InputStreamReader(new FileInputStream(
						fileName))));
				}
			} else {
				tempArc.putAll(this.gyousei);
			}
			if (this.mizuArc == null) {
				final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
					+ File.separator + "mizu" + File.separator + "mizu.arc";
				if (new File(fileName).canRead()) {
					this.loadArc(tempArc, new BufferedReader(new InputStreamReader(new FileInputStream(
						fileName))));
				}
			} else {
				tempArc.putAll(this.mizuArc);
			}
			if (this.others == null) {
				final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
					+ File.separator + "others" + File.separator + "others.arc";
				if (new File(fileName).canRead()) {
					this.loadArc(tempArc, new BufferedReader(new InputStreamReader(new FileInputStream(
						fileName))));
				}
			} else {
				tempArc.putAll(this.others);
			}
			if (this.tatemonoArc == null) {
				final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
					+ File.separator + "tatemono" + File.separator + "tatemono.arc";
				if (new File(fileName).canRead()) {
					this.loadArc(tempArc, new BufferedReader(new InputStreamReader(new FileInputStream(
						fileName))));
				}
			} else {
				tempArc.putAll(this.tatemonoArc);
			}
			final Map<String, PolygonData> tempPolygon = new ConcurrentHashMap<String, PolygonData>();
			if (this.tyome == null) {
				this.loadPolygon(tempPolygon, tempArc, new BufferedReader(new InputStreamReader(
					new FileInputStream(this.baseDir + File.separator + this.mapName.toUpperCase()
						+ File.separator + "gyousei" + File.separator + "tyome.pgn"))));
				this.loadPolygonAttribute(tempPolygon, new BufferedReader(new InputStreamReader(
					new FileInputStream(this.baseDir + File.separator + this.mapName.toUpperCase()
						+ File.separator + "gyousei" + File.separator + "tyome.atr"), "SJIS")),
					PolygonData.CLASSIFICATION_TYOME);
			} else {
				tempPolygon.putAll(this.tyome);
			}
			if (this.mizu == null) {
				final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
					+ File.separator + "mizu" + File.separator + "mizu.pgn";
				if (new File(fileName).canRead()) {
					this.loadPolygon(tempPolygon, tempArc, new BufferedReader(new InputStreamReader(
						new FileInputStream(fileName))));
					this.loadPolygonAttribute(tempPolygon, new BufferedReader(new InputStreamReader(
						new FileInputStream(this.baseDir + File.separator
							+ this.mapName.toUpperCase() + File.separator + "mizu" + File.separator
							+ "mizu.atr"), "SJIS")), PolygonData.CLASSIFICATION_RIVER);
				}
			} else {
				tempPolygon.putAll(this.mizu);
			}
			if (this.zyouti == null) {
				final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
					+ File.separator + "others" + File.separator + "zyouti.pgn";
				if (new File(fileName).canRead()) {
					this.loadPolygon(tempPolygon, tempArc, new BufferedReader(new InputStreamReader(
						new FileInputStream(fileName))));
					this.loadPolygonAttribute(tempPolygon, new BufferedReader(new InputStreamReader(
						new FileInputStream(this.baseDir + File.separator
							+ this.mapName.toUpperCase() + File.separator + "others"
							+ File.separator + "zyouti.atr"), "SJIS")),
						PolygonData.CLASSIFICATION_PARK);
				}
			} else {
				tempPolygon.putAll(this.zyouti);
			}
			if (this.tatemono == null) {
				final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
					+ File.separator + "tatemono" + File.separator + "tatemono.pgn";
				if (new File(fileName).canRead()) {
					this.loadPolygon(tempPolygon, tempArc, new BufferedReader(new InputStreamReader(
						new FileInputStream(fileName))));
					this.loadPolygonAttribute(tempPolygon, new BufferedReader(new InputStreamReader(
						new FileInputStream(this.baseDir + File.separator
							+ this.mapName.toUpperCase() + File.separator + "tatemono"
							+ File.separator + "tatemono.atr"), "SJIS")),
						PolygonData.CLASSIFICATION_BUILDING);
				}
			} else {
				tempPolygon.putAll(this.zyouti);
			}
			for (final PolygonData polygon : tempPolygon.values()) {
				final String attribute = polygon.getAttribute();
				if (polygon.getAttribute() != null) {
					final Point2D point = new Point2D.Double(polygon.getX(), polygon.getY());
					if (!this.attributes.containsKey(attribute)) {
						this.attributes.put(attribute, new ArrayList<Point2D>());
					}
					this.attributes.get(attribute).add(point);
				}
			}
			if (this.eki != null) {
				for (final PointData pointData : this.eki.values()) {
					final String attribute = pointData.getAttribute();
					final Point2D point = new Point2D.Double(pointData.getX(), pointData.getY());
					if (!this.attributes.containsKey(attribute)) {
						this.attributes.put(attribute, new ArrayList<Point2D>());
					}
					this.attributes.get(attribute).add(point);
				}
			}
		}
		return this.attributes;
	}

	/**
	 * 地図データのあるディレクトリを取得します。
	 * @return ディレクトリ
	 */
	String getBaseDir() {
		return this.baseDir;
	}

	/**
	 * 地図が表す領域を取得します。
	 * @return 領域
	 */
	public Shape getBounds() {
		return this.rectangle;
	}

	/**
	 * 図郭にまたがるポリゴンの一覧を取得します。
	 * Mapのキーは図郭上の線分の文字列表現、
	 * 値はポリゴンの名前です。
	 * @return 図郭にまたがるポリゴンの一覧
	 */
	Map<String, String> getEdgePolygons() {
		return this.edgePolygons;
	}

	/**
	 * 駅の点データを取得します。
	 * @return 駅の点データ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, PointData> getEki() throws FileNotFoundException, IOException {
		if (this.eki == null) {
			this.loadEki();
		}
		return this.eki;
	}

	/**
	 * 行政界の弧データを取得します。
	 * @return 行政界の弧データ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, ArcData> getGyousei() throws FileNotFoundException, IOException {
		if (this.gyousei == null) {
			this.loadGyousei();
		}
		return this.gyousei;
	}

	/**
	 * 高速道路と国道の弧データを取得します。
	 * @return 道路の弧データ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, ArcData> getLargeRoadArc() throws FileNotFoundException, IOException {
		if (this.largeRoadArc == null) {
			System.out.println("WARNING: largeRoadArcがnullなのにgetLargeRoadArc()が呼び出されました：" + this);
			this.loadLargeRoadArc();
		}
		return this.largeRoadArc;
	}

	/**
	 * 地図のファイル名を取得します。
	 * @return 地図のファイル名
	 */
	String getMapName() {
		return this.mapName;
	}

	/** 内水面のポリゴンデータを取得します。
	 * @return 内水面のポリゴンデータ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, PolygonData> getMizu() throws FileNotFoundException, IOException {
		if ((this.mizu == null) && (this.mizuArc != null)) {
			this.loadMizu();
		}
		return this.mizu;
	}

	/**
	 * 内水面界の弧データを取得します。
	 * @return 内水面界の弧データ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, ArcData> getMizuArc() throws FileNotFoundException, IOException {
		if (this.mizuArc == null) {
			this.loadMizuArc();
		}
		return this.mizuArc;
	}

	/**
	 * 鉄道、場地界の弧データを取得します。
	 * @return 鉄道、場地界の弧データ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, ArcData> getOthers() throws FileNotFoundException, IOException {
		if (this.others == null) {
			System.out.println("WARNING: othersがnullなのにgetOthers()が呼び出されました：" + this);
			this.loadOthers();
		}
		return this.others;
	}

	/**
	 * 道路の弧データを取得します。
	 * @return 道路の弧データ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, ArcData> getRoadArc() throws FileNotFoundException, IOException {
		if (this.roadArc == null) {
			System.out.println("WARNING: roadArcがnullなのにgetRoadArc()が呼び出されました：" + this);
			this.loadRoadArc();
		}
		return this.roadArc;
	}

	/**
	 * 市町界のポリゴンデータを取得します。
	 * @return 市町界のポリゴンデータ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, PolygonData> getSi_tyo() throws FileNotFoundException, IOException {
		if (this.gyousei == null) {
			this.loadGyousei();
		}
		if (this.si_tyo == null) {
			this.loadSi_tyo();
		}
		return this.si_tyo;
	}

	/**
	 * 建物のポリゴンデータを取得します。
	 * @return 建物のポリゴンデータ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, PolygonData> getTatemono() throws FileNotFoundException, IOException {
		if (this.tatemonoArc == null) {
			this.loadTatemonoArc();
		}
		if (this.tatemono == null) {
			this.loadTatemono();
		}
		return this.tatemono;
	}

	/**
	 * 建物界の弧データを取得します。
	 * @return 建物界の弧データ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, ArcData> getTatemonoArc() throws FileNotFoundException, IOException {
		if (this.tatemonoArc == null) {
			this.loadTatemonoArc();
		}
		return this.tatemonoArc;
	}

	/**
	 * 大字、町丁目界のポリゴンデータを取得します。
	 * @return 大字、町丁目界のポリゴンデータ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, PolygonData> getTyome() throws FileNotFoundException, IOException {
		if (this.gyousei == null) {
			this.loadGyousei();
		}
		if (this.tyome == null) {
			this.loadTyome();
		}
		return this.tyome;
	}

	/**
	 * 場地のポリゴンデータを取得します。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 * @return 場地のポリゴンデータ
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	Map<String, PolygonData> getZyouti() throws FileNotFoundException, IOException {
		if (this.others == null) {
			this.loadOthers();
		}
		if (this.zyouti == null) {
			this.loadZyouti();
		}
		return this.zyouti;
	}

	/**
	 * 駅の点データを持っているかどうかを取得します。
	 * @return 駅の点データを持っているかどうか
	 */
	boolean hasEki() {
		return this.eki != null;
	}

	/**
	 * 行政界の弧データを持っているかどうかを取得します。
	 * @return 行政界の弧データを持っているか
	 */
	boolean hasGyousei() {
		return this.gyousei != null;
	}

	/**
	 * @return 高速道路、国道の弧データを持っているかどうか
	 */
	boolean hasLargeRoadArc() {
		return this.largeRoadArc != null;
	}

	/**
	 * 内水面のポリゴンデータを持っているかどうかを取得します。
	 * @return 内水面のポリゴンデータを持っているかどうか
	 */
	boolean hasMizu() {
		return this.mizu != null;
	}

	/**
	 * 内水面界の弧データを持っているかどうかを取得します。
	 * @return 内水面界の弧データを持っているかどうか
	 */
	boolean hasMizuArc() {
		return this.mizuArc != null;
	}

	/**
	 * 鉄道、場地界の弧データを持っているかどうかを取得します。
	 * @return 鉄道、場地界の弧データを持っているかどうか
	 */
	boolean hasOthers() {
		return this.others != null;
	}

	/**
	 * 道の弧データを持っているかどうかを取得します。
	 * @return 道の弧データを持っているかどうか
	 */
	boolean hasRoadArc() {
		return this.roadArc != null;
	}

	/**
	 * 市町界のポリゴンデータを持っているかどうかを取得します。
	 * @return 市町界のポリゴンデータを持っているかどうか
	 */
	boolean hasSi_tyo() {
		return this.si_tyo != null;
	}

	/**
	 * 建物のポリゴンデータを持っているかどうかを取得します。
	 * @return 建物のポリゴンデータを持っているかどうか
	 */
	boolean hasTatemono() {
		return this.tatemono != null;
	}

	/**
	 * 建物界の弧データを持っているかどうかを取得します。
	 * @return 建物界の弧データを持っているかどうか
	 */
	boolean hasTatemonoArc() {
		return this.tatemonoArc != null;
	}

	/**
	 * 大字、町丁目のポリゴンデータを持っているかどうかを取得します。
	 * @return 大字、町丁目のポリゴンデータを持っているかどうか
	 */
	boolean hasTyome() {
		return this.tyome != null;
	}

	/**
	 * 場地界のポリゴンデータを持っているかどうかを取得します。
	 * @return 場地界のポリゴンデータを持っているかどうか
	 */
	boolean hasZyouti() {
		return this.zyouti != null;
	}

	/**
	 * 弧の情報をファイルから読み込みます。
	 * @param arcs 弧
	 * @param in 入力ストリーム
	 * @throws IOException 例外
	 */
	private void loadArc(final Map<String, ArcData> arcs, final BufferedReader in) throws IOException {
		String line;
		String arcName = null;
		int type = 0;
		int tag = 0;
		GeneralPath path = null;
		int kei = -1;
		double x0 = Double.NaN;
		double y0 = Double.NaN;
		while ((line = in.readLine()) != null) {
			final StringTokenizer tokenizer = new StringTokenizer(line, ",");
			if (tokenizer.countTokens() == 16) {
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				kei = Integer.parseInt(tokenizer.nextToken());
				tokenizer.nextToken();
				x0 = Double.parseDouble(tokenizer.nextToken());
				y0 = Double.parseDouble(tokenizer.nextToken());
			} else if (tokenizer.countTokens() == 4) {
				// 新しい弧が始まるとき
				if (path != null) {
					arcs.put(arcName, new ArcData(arcName, path, type, tag));
				}
				final String code = tokenizer.nextToken(); // 図式分類コード
				if (code.equals("L1101")) {
					type = ArcData.TYPE_GYOUSEI_PREFECTURE;
				} else if (code.equals("L1103")) {
					type = ArcData.TYPE_GYOUSEI_CITY;
				} else if (code.equals("L1104")) {
					type = ArcData.TYPE_GYOUSEI_VILLAGE;
				} else if (code.equals("L1106")) {
					type = ArcData.TYPE_GYOUSEI_CHOME;
				} else if (code.equals("L2300")) {
					type = ArcData.TYPE_RAILWAY;
				} else if (code.equals("L2110")) {
					type = ArcData.TYPE_ROAD;
				} else if (code.equals("L6241")) {
					type = ArcData.TYPE_ZYOTI_RAILROAD;
				} else if (code.equals("L6242")) {
					type = ArcData.TYPE_ZYOTI_PARK;
				} else if (code.equals("L6243")) {
					type = ArcData.TYPE_ZYOTI_SCHOOL;
				} else if (code.equals("L6244")) {
					type = ArcData.TYPE_ZYOTI_TEMPLE;
				} else if (code.equals("L6215")) {
					type = ArcData.TYPE_ZYOTI_GRAVEYARD;
				} else if (code.equals("L6200")) {
					type = ArcData.TYPE_ZYOTI_OTHER;
				} else if (code.equals("L5101")) {
					type = ArcData.TYPE_MIZU_INSIDE;
				} else if (code.equals("L5106")) {
					type = ArcData.TYPE_MIZU_SEASHORE;
				} else {
					type = ArcData.TYPE_UNKNOWN;
				}
				tag = Integer.parseInt(tokenizer.nextToken()); // 線種タグ
				arcName = tokenizer.nextToken(); // 個別番号
				path = null;
			} else if (tokenizer.countTokens() == 2) {
				// 弧を構成する座標のとき
				//				float y = -Float.parseFloat(tokenizer.nextToken()); // y 座標
				//				float x = Float.parseFloat(tokenizer.nextToken()); // x 座標
				//				if (path != null) {
				//					path.lineTo(x + (float) getBounds().getX(), y + (float) getBounds().getMaxY());
				//				} else {
				//					path = new GeneralPath();
				//					path.moveTo(x + (float) getBounds().getX(), y + (float) getBounds().getMaxY());
				//				}
				//				// test
				final double x = Double.parseDouble(tokenizer.nextToken());
				final double y = Double.parseDouble(tokenizer.nextToken());
				final Point2D p = UTMUtil.toUTM(JasmineUtil.toLatLong(x + x0, y + y0, kei));
				if (path != null) {
					path.lineTo((float) p.getX(), (float) -p.getY());
				} else {
					path = new GeneralPath();
					path.moveTo((float) p.getX(), (float) -p.getY());
				}
			}
		}
		if (path != null) {
			arcs.put(arcName, new ArcData(arcName, path, type, tag));
		}
	}

	/**
	 * 弧の情報を属性情報をファイルから読み込みます。
	 * @param arcs 弧
	 * @param in 入力ストリーム
	 * @throws IOException 入出力例外
	 */
	private void loadArcAttribute(final Map<String, ArcData> arcs, final BufferedReader in) throws IOException {
		String line;
		while ((line = in.readLine()) != null) {
			final StringTokenizer tokenizer = new StringTokenizer(line, ",");
			if (tokenizer.countTokens() > 0) {
				final String firstToken = tokenizer.nextToken(); // 「FH」or 図式分類コード
				if (!firstToken.equals("FH")) {
					// 属性レコードのとき
					if (tokenizer.countTokens() > 2) {
						final String name = tokenizer.nextToken(); // 個別番号
						tokenizer.nextToken(); // 属性フィールドの個数
						String attribute = tokenizer.nextToken(); // 属性
						if (arcs.containsKey(name)) {
							final ArcData arc = arcs.get(name);
							attribute = attribute.replaceFirst("電気鉄道", "電鉄");
							arc.setAttribute(attribute);
							if (attribute.indexOf("新幹線") > 0) {
								arc.setRailwayType(ArcData.RAILWAY_JR_SHINKANSEN);
							} else if (attribute.startsWith("ＪＲ")) {
								arc.setRailwayType(ArcData.RAILWAY_JR);
							} else {
								arc.setRailwayType(ArcData.RAILWAY_OTHER);
							}
							if ((attribute.indexOf("高速") > 0) || attribute.endsWith("自動車道")) {
								arc.setRoadType(ArcData.ROAD_HIGHWAY);
							} else if (attribute.startsWith("国道")) {
								arc.setRoadType(ArcData.ROAD_KOKUDO);
							} else if (attribute.startsWith("県道") || attribute.startsWith("府道")
								|| attribute.startsWith("道道") || attribute.startsWith("都道")) {
								arc.setRoadType(ArcData.ROAD_KENDO);
							} else if (attribute.startsWith("主要地方道")) {
								arc.setRoadType(ArcData.ROAD_CHIHODO);
								arc.setAttribute(attribute.replaceFirst("主要地方道", ""));
							} else {
								arc.setRoadType(ArcData.ROAD_MAJOR);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 駅の点データを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadEki() throws FileNotFoundException, IOException {
		final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "others" + File.separator + "eki.pnt";
		final String attributeFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "others" + File.separator + "eki.atr";
		this.eki = new ConcurrentHashMap<String, PointData>();
		if (new File(fileName).canRead()) {
			this.loadPoint(this.eki, new BufferedReader(new InputStreamReader(new FileInputStream(
				fileName))));
			if (new File(attributeFileName).canRead()) {
				this.loadPointAttribute(this.eki, new BufferedReader(new InputStreamReader(
					new FileInputStream(attributeFileName), "SJIS")),
					PointData.CLASSIFICATION_STATION);
			}
		}
	}

	/**
	 * 行政界の弧ファイルを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadGyousei() throws FileNotFoundException, IOException {
		final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "gyousei" + File.separator + "gyousei.arc";
		this.gyousei = new ConcurrentHashMap<String, ArcData>();
		if (new File(fileName).canRead()) {
			this.loadArc(this.gyousei, new BufferedReader(new InputStreamReader(new FileInputStream(
				fileName))));
		}
	}

	/**
	 * 高速道路、国道の弧データを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadLargeRoadArc() throws FileNotFoundException, IOException {
		final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "road" + File.separator + "roadntwk.arc";
		final String attributeFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "road" + File.separator + "road.atr";
		this.largeRoadArc = new ConcurrentHashMap<String, ArcData>();
		final Map<String, ArcData> tempArc = new ConcurrentHashMap<String, ArcData>();
		if (new File(fileName).canRead()) {
			this.loadArc(tempArc, new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName))));
			if (new File(attributeFileName).canRead()) {
				this.loadArcAttribute(tempArc, new BufferedReader(new InputStreamReader(
					new FileInputStream(attributeFileName), "SJIS")));
			}
		}
		for (final Map.Entry<String, ArcData> entry : tempArc.entrySet()) {
			final ArcData arc = entry.getValue();
			if (arc.getRoadType() == ArcData.ROAD_HIGHWAY
				|| arc.getRoadType() == ArcData.ROAD_KOKUDO
				|| arc.getRoadType() == ArcData.ROAD_CHIHODO) {
				this.largeRoadArc.put(entry.getKey(), arc);
			}
		}
	}

	/**
	 * 内水面のポリゴンファイルを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadMizu() throws FileNotFoundException, IOException {
		final String polygonFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "mizu" + File.separator + "mizu.pgn";
		if (new File(polygonFileName).canRead()) {
			this.mizu = new ConcurrentHashMap<String, PolygonData>();
			this.loadPolygon(this.mizu, this.mizuArc, new BufferedReader(new InputStreamReader(
				new FileInputStream(polygonFileName))));
			final String attributeFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
				+ File.separator + "mizu" + File.separator + "mizu.atr";
			if (new File(attributeFileName).canRead()) {
				this.loadPolygonAttribute(this.mizu, new BufferedReader(new InputStreamReader(
					new FileInputStream(attributeFileName), "SJIS")),
					PolygonData.CLASSIFICATION_RIVER);
			}
		}
	}

	/**
	 * 水界の弧ファイルを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadMizuArc() throws FileNotFoundException, IOException {
		final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "mizu" + File.separator + "mizu.arc";
		if (new File(fileName).canRead()) {
			this.mizuArc = new ConcurrentHashMap<String, ArcData>();
			this.loadArc(this.mizuArc, new BufferedReader(new InputStreamReader(new FileInputStream(
				fileName))));
		}
	}

	/**
	 * 鉄道、場地界の弧データを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadOthers() throws FileNotFoundException, IOException {
		this.others = new ConcurrentHashMap<String, ArcData>();
		final String attributeFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "others" + File.separator + "tetudou.atr";
		this.loadArc(this.others, new BufferedReader(new InputStreamReader(new FileInputStream(
			this.baseDir + File.separator + this.mapName.toUpperCase() + File.separator + "others"
				+ File.separator + "others.arc"))));
		if (new File(attributeFileName).canRead()) {
			this.loadArcAttribute(this.others, new BufferedReader(new InputStreamReader(
				new FileInputStream(attributeFileName), "SJIS")));
		}
	}

	/** 点の情報をファイルから読み込みます。
	 * @param points 点
	 * @param in 入力ストリーム
	 * @throws IOException 例外
	 */
	private void loadPoint(final Map<String, PointData> points, final BufferedReader in) throws IOException {
		String line;
		int kei = -1;
		double x0 = Double.NaN;
		double y0 = Double.NaN;
		while ((line = in.readLine()) != null) {
			final StringTokenizer tokenizer = new StringTokenizer(line, ",");
			if (tokenizer.countTokens() == 16) {
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				kei = Integer.parseInt(tokenizer.nextToken());
				tokenizer.nextToken();
				x0 = Double.parseDouble(tokenizer.nextToken());
				y0 = Double.parseDouble(tokenizer.nextToken());
			} else if (tokenizer.countTokens() == 4) {
				final String classificationCode = tokenizer.nextToken(); // 図式分類コード
				int code;
				if (classificationCode.equals("P2420")) {
					code = PointData.CLASSIFICATION_STATION;
				} else if (classificationCode.equalsIgnoreCase("P7301")) {
					code = PointData.CLASSIFICATION_DATUMS;
				} else {
					code = PointData.CLASSIFICATION_UNKNOWN;
				}
				final String name = tokenizer.nextToken(); // 個別番号
				//				double y = -Double.parseDouble(tokenizer.nextToken()); // y 座標
				//				double x = Double.parseDouble(tokenizer.nextToken()); // x 座標
				//				PointData point = new PointData(name, code, x + getBounds().getX(), y
				//						+ getBounds().getMaxY());
				// test
				final double x = Double.parseDouble(tokenizer.nextToken());
				final double y = Double.parseDouble(tokenizer.nextToken());
				final Point2D p = UTMUtil.toUTM(JasmineUtil.toLatLong(x + x0, y + y0, kei));
				final PointData point = new PointData(name, code, p.getX(), -p.getY());
				points.put(name, point);
			}
		}
	}

	/** 点データの属性情報をファイルから読み込みます。
	 * @param points 点
	 * @param in 入力ストリーム
	 * @param classificationCode 図式分類コードの例
	 * @throws IOException 入出力例外
	 */
	private void loadPointAttribute(final Map<String, PointData> points, final BufferedReader in,
		final int classificationCode) throws IOException {
		String line;
		while ((line = in.readLine()) != null) {
			final StringTokenizer tokenizer = new StringTokenizer(line, ",");
			if (tokenizer.countTokens() > 0) {
				final String firstToken = tokenizer.nextToken(); // 「FH」or 図式分類コード
				if (!firstToken.equals("FH")) {
					// 属性レコードのとき
					if (classificationCode == PointData.CLASSIFICATION_STATION) {
						// 駅
						if (tokenizer.countTokens() == 3) {
							final String name = tokenizer.nextToken(); // 個別番号
							tokenizer.nextToken(); // 属性フィールドの個数
							String attribute = tokenizer.nextToken(); // 属性
							if (!attribute.endsWith("駅")) {
								attribute = attribute + "駅";
							}
							if (points.containsKey(name)) {
								points.get(name).setAttribute(attribute);
							}
						}
					} else if (classificationCode == PointData.CLASSIFICATION_DATUMS) {
						// 三角点
						if (tokenizer.countTokens() == 13) {
							final String name = tokenizer.nextToken(); // 個別番号
							tokenizer.nextToken(); // 属性フィールドの個数
							tokenizer.nextToken(); // 3 次メッシュコード
							tokenizer.nextToken(); // 点コード
							final String attribute = tokenizer.nextToken(); // 属性
							if (points.containsKey(name)) {
								points.get(name).setAttribute(attribute);
							}
						}
					}
				}
			}
		}
	}

	/** ポリゴンの情報をファイルから読み込みます。
	 * @param polygons ポリゴン
	 * @param arcs ポリゴンを構成する弧
	 * @param in 入力ストリーム
	 * @throws IOException 例外
	 */
	private void loadPolygon(final Map<String, PolygonData> polygons, final Map<String, ArcData> arcs,
		final BufferedReader in) throws IOException {
		String line;
		String polygonName = null;
		GeneralPath path = null;
		int type = 0;
		double x = 0;
		double y = 0;
		int kei = -1;
		double x0 = Double.NaN;
		double y0 = Double.NaN;
		while ((line = in.readLine()) != null) {
			final StringTokenizer tokenizer = new StringTokenizer(line, ",");
			if (tokenizer.countTokens() == 16) {
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				kei = Integer.parseInt(tokenizer.nextToken());
				tokenizer.nextToken();
				x0 = Double.parseDouble(tokenizer.nextToken());
				y0 = Double.parseDouble(tokenizer.nextToken());
			} else if (tokenizer.countTokens() == 5) {
				// 新しいポリゴンが始まるとき
				if ((path != null) && (polygonName != null)) {
					polygons.put(polygonName, new PolygonData(polygonName, new Area(path), type, x,
						-y));
				}
				type = this.parsePolygonType(tokenizer.nextToken()); // 図式分類コード
				polygonName = tokenizer.nextToken(); // 個別番号
				tokenizer.nextToken(); // 当該ポリゴンを構成するアーク指定レコードの数
				//				y = -Double.parseDouble(tokenizer.nextToken()); // 代表点の y 座標
				//				x = Double.parseDouble(tokenizer.nextToken()); // 代表点の x 座標
				// test
				final double tempX = Double.parseDouble(tokenizer.nextToken());
				final double tempY = Double.parseDouble(tokenizer.nextToken());
				final Point2D p = UTMUtil.toUTM(JasmineUtil.toLatLong(tempX + x0, tempY + y0, kei));
				x = p.getX();
				y = p.getY();
				path = new GeneralPath();
			} else if (tokenizer.countTokens() == 1) {
				// ポリゴンを構成する弧のとき
				final int arcIndex = Integer.parseInt(tokenizer.nextToken());
				// アーク指定レコード
				final String arcName = new Integer(Math.abs(arcIndex)).toString();
				final ArcData arc = arcs.get(arcName);
				if (arcIndex < 0) {
					path.append(arc.getReversalPath(), true);
				} else {
					path.append(arc.getPath(), true);
				}
				if (!this.arcPolygonMap.containsKey(arc)) {
					this.arcPolygonMap.put(arc, new ArrayList<String>());
				}
				this.arcPolygonMap.get(arc).add(polygonName);
				if (arc.getTag() == ArcData.TAG_EDGE_OF_MAP) {
					final PathIterator iter = arc.getPath().getPathIterator(new AffineTransform());
					final double[] coords = new double[6];
					iter.currentSegment(coords);
					final int x1 = (int) coords[0];
					final int y1 = (int) coords[1];
					if (!iter.isDone()) {
						iter.next();
						if (iter.currentSegment(coords) == PathIterator.SEG_LINETO) {
							final int x2 = (int) coords[0];
							final int y2 = (int) coords[1];
							if ((x1 < x2) || (y1 < y2)) {
								this.edgePolygons.put(x1 + "_" + y1 + "_" + x2 + "_" + y2,
									polygonName);
								// test
								this.edgePolygons.put(x2 + "_" + y2 + "_" + x1 + "_" + y1,
									polygonName);
							} else {
								this.edgePolygons.put(x2 + "_" + y2 + "_" + x1 + "_" + y1,
									polygonName);
								// test
								this.edgePolygons.put(x1 + "_" + y1 + "_" + x2 + "_" + y2,
									polygonName);
							}
							if (!iter.isDone()) {
								iter.next();
								if (iter.currentSegment(coords) == PathIterator.SEG_LINETO) {
									final int x3 = (int) coords[0];
									final int y3 = (int) coords[1];
									if ((x2 < x3) || (y2 < y3)) {
										this.edgePolygons.put(x2 + "_" + y2 + "_" + x3 + "_" + y3,
											polygonName);
										// test
										this.edgePolygons.put(x3 + "_" + y3 + "_" + x2 + "_" + y2,
											polygonName);
									} else {
										this.edgePolygons.put(x3 + "_" + y3 + "_" + x2 + "_" + y2,
											polygonName);
										// test
										this.edgePolygons.put(x2 + "_" + y2 + "_" + x3 + "_" + y3,
											polygonName);
									}
									if (!iter.isDone()) {
										iter.next();
										if (iter.currentSegment(coords) == PathIterator.SEG_LINETO) {
											final int x4 = (int) coords[0];
											final int y4 = (int) coords[1];
											if ((x3 < x4) || (y3 < y4)) {
												this.edgePolygons.put(x3 + "_" + y3 + "_" + x4
													+ "_" + y4, polygonName);
												// test
												this.edgePolygons.put(x4 + "_" + y4 + "_" + x3
													+ "_" + y3, polygonName);
											} else {
												this.edgePolygons.put(x4 + "_" + y4 + "_" + x3
													+ "_" + y3, polygonName);
												// test
												this.edgePolygons.put(x3 + "_" + y3 + "_" + x4
													+ "_" + y4, polygonName);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			if ((path != null) && (polygonName != null)) {
				polygons
					.put(polygonName, new PolygonData(polygonName, new Area(path), type, x, -y));
			}
		}
	}

	/**
	 * ポリゴンの属性情報をファイルから読み込みます。
	 * @param polygons ポリゴン
	 * @param in 入力ストリーム
	 * @param classificationCode 図式分類コードの例
	 * @throws IOException 入出力例外
	 * @throws NumberFormatException 数値形式例外
	 */
	private void loadPolygonAttribute(final Map<String, PolygonData> polygons, final BufferedReader in,
		final int classificationCode) throws NumberFormatException, IOException {
		String line;
		while ((line = in.readLine()) != null) {
			final StringTokenizer tokenizer = new StringTokenizer(line, ",");
			if (tokenizer.countTokens() > 0) {
				final String firstToken = tokenizer.nextToken(); // 「FH」or 図式分類コード
				if (!firstToken.equals("FH")) {
					// 属性レコードのとき
					final String polygonName = tokenizer.nextToken(); // 個別番号
					if (tokenizer.hasMoreTokens()) {
						tokenizer.nextToken(); // 属性フィールドの数
					}
					final StringBuffer stringBuffer = new StringBuffer();
					if (tokenizer.hasMoreTokens()) {
						if ((classificationCode == PolygonData.CLASSIFICATION_SI_TYO)
							|| (classificationCode == PolygonData.CLASSIFICATION_TYOME)) {
							tokenizer.nextToken(); // 属性フィールド 1
						} else if (classificationCode == PolygonData.CLASSIFICATION_BUILDING) {
							// 属性フィールド 1
							polygons.get(polygonName).setTatemonoCode(
								Integer.parseInt(tokenizer.nextToken()));
						} else {
							stringBuffer.append(tokenizer.nextToken());
							// 属性フィールド 1
						}
					}
					if (tokenizer.hasMoreTokens()) {
						stringBuffer.append(tokenizer.nextToken());
						// 属性フィールド 2
					}
					String attribute = stringBuffer.toString();
					if (classificationCode == PolygonData.CLASSIFICATION_TYOME) {
						attribute = Pattern.compile("二十丁目$").matcher(attribute).replaceFirst("20");
						attribute = Pattern.compile("十九丁目$").matcher(attribute).replaceFirst("19");
						attribute = Pattern.compile("十八丁目$").matcher(attribute).replaceFirst("18");
						attribute = Pattern.compile("十七丁目$").matcher(attribute).replaceFirst("17");
						attribute = Pattern.compile("十六丁目$").matcher(attribute).replaceFirst("16");
						attribute = Pattern.compile("十五丁目$").matcher(attribute).replaceFirst("15");
						attribute = Pattern.compile("十四丁目$").matcher(attribute).replaceFirst("14");
						attribute = Pattern.compile("十三丁目$").matcher(attribute).replaceFirst("13");
						attribute = Pattern.compile("十二丁目$").matcher(attribute).replaceFirst("12");
						attribute = Pattern.compile("十一丁目$").matcher(attribute).replaceFirst("11");
						attribute = Pattern.compile("十丁目$").matcher(attribute).replaceFirst("10");
						attribute = Pattern.compile("九丁目$").matcher(attribute).replaceFirst("９");
						attribute = Pattern.compile("八丁目$").matcher(attribute).replaceFirst("８");
						attribute = Pattern.compile("七丁目$").matcher(attribute).replaceFirst("７");
						attribute = Pattern.compile("六丁目$").matcher(attribute).replaceFirst("６");
						attribute = Pattern.compile("五丁目$").matcher(attribute).replaceFirst("５");
						attribute = Pattern.compile("四丁目$").matcher(attribute).replaceFirst("４");
						attribute = Pattern.compile("三丁目$").matcher(attribute).replaceFirst("３");
						attribute = Pattern.compile("二丁目$").matcher(attribute).replaceFirst("２");
						attribute = Pattern.compile("一丁目$").matcher(attribute).replaceFirst("１");
						attribute = Pattern.compile("３０丁目$").matcher(attribute).replaceFirst("30");
						attribute = Pattern.compile("２９丁目$").matcher(attribute).replaceFirst("29");
						attribute = Pattern.compile("２８丁目$").matcher(attribute).replaceFirst("28");
						attribute = Pattern.compile("２７丁目$").matcher(attribute).replaceFirst("27");
						attribute = Pattern.compile("２６丁目$").matcher(attribute).replaceFirst("26");
						attribute = Pattern.compile("２５丁目$").matcher(attribute).replaceFirst("25");
						attribute = Pattern.compile("２４丁目$").matcher(attribute).replaceFirst("24");
						attribute = Pattern.compile("２３丁目$").matcher(attribute).replaceFirst("23");
						attribute = Pattern.compile("２２丁目$").matcher(attribute).replaceFirst("22");
						attribute = Pattern.compile("２１丁目$").matcher(attribute).replaceFirst("21");
						attribute = Pattern.compile("２０丁目$").matcher(attribute).replaceFirst("20");
						attribute = Pattern.compile("１９丁目$").matcher(attribute).replaceFirst("19");
						attribute = Pattern.compile("１８丁目$").matcher(attribute).replaceFirst("18");
						attribute = Pattern.compile("１７丁目$").matcher(attribute).replaceFirst("17");
						attribute = Pattern.compile("１６丁目$").matcher(attribute).replaceFirst("16");
						attribute = Pattern.compile("１５丁目$").matcher(attribute).replaceFirst("15");
						attribute = Pattern.compile("１４丁目$").matcher(attribute).replaceFirst("14");
						attribute = Pattern.compile("１３丁目$").matcher(attribute).replaceFirst("13");
						attribute = Pattern.compile("１２丁目$").matcher(attribute).replaceFirst("12");
						attribute = Pattern.compile("１１丁目$").matcher(attribute).replaceFirst("11");
						attribute = Pattern.compile("１０丁目$").matcher(attribute).replaceFirst("10");
						attribute = Pattern.compile("丁目$").matcher(attribute).replaceFirst("");
					} else if (classificationCode == PolygonData.CLASSIFICATION_BUILDING) {
						attribute = Pattern.compile("郵便局$").matcher(attribute).replaceFirst("局");
						attribute = Pattern.compile("警察署$").matcher(attribute).replaceFirst("署");
						attribute = Pattern.compile("消防署$").matcher(attribute).replaceFirst("署");
						attribute = Pattern.compile("コミュニティーセンター$").matcher(attribute)
							.replaceFirst("コミセン");
						attribute = Pattern.compile("幼稚園$").matcher(attribute).replaceFirst("幼");
						attribute = Pattern.compile("小学校$").matcher(attribute).replaceFirst("小");
						attribute = Pattern.compile("中学校$").matcher(attribute).replaceFirst("中");
						attribute = Pattern.compile("高校$").matcher(attribute).replaceFirst("高");
						attribute = Pattern.compile("高等学校$").matcher(attribute).replaceFirst("高");
						attribute = Pattern.compile("工業高等専門学校$").matcher(attribute).replaceFirst(
							"工専");
						attribute = Pattern.compile("短期大学$").matcher(attribute).replaceFirst("短大");
						attribute = Pattern.compile("大学校$").matcher(attribute).replaceFirst("大");
						attribute = Pattern.compile("大学$").matcher(attribute).replaceFirst("大");
						attribute = Pattern.compile("センター$").matcher(attribute).replaceFirst("センタ");
						attribute = Pattern.compile("センタ−$").matcher(attribute).replaceFirst("センタ");
						attribute = Pattern.compile("公共職業安定所$").matcher(attribute).replaceFirst(
							"職安");
					} else if ((classificationCode == PolygonData.CLASSIFICATION_RAILROAD)
						|| (classificationCode == PolygonData.CLASSIFICATION_PARK)
						|| (classificationCode == PolygonData.CLASSIFICATION_SCHOOL)
						|| (classificationCode == PolygonData.CLASSIFICATION_TEMPLE)
						|| (classificationCode == PolygonData.CLASSIFICATION_GRAVEYARD)
						|| (classificationCode == PolygonData.CLASSIFICATION_OTHER)) {
						attribute = Pattern.compile("小学校$").matcher(attribute).replaceFirst("小");
						attribute = Pattern.compile("中学校").matcher(attribute).replaceAll("中");
						attribute = Pattern.compile("高校").matcher(attribute).replaceAll("高");
						attribute = Pattern.compile("高等学校").matcher(attribute).replaceAll("高");
						attribute = Pattern.compile("工業高等専門学校$").matcher(attribute).replaceFirst(
							"工専");
						attribute = Pattern.compile("短期大学$").matcher(attribute).replaceFirst("短大");
						attribute = Pattern.compile("大学校$").matcher(attribute).replaceFirst("大");
						attribute = Pattern.compile("大学$").matcher(attribute).replaceFirst("大");
					}
					polygons.get(polygonName).setAttribute(attribute);
				}
			}
		}
	}

	/** 地図がある領域をファイルから読み込みます。
	 * @throws NumberFormatException 数値形式例外
	 * @throws IOException 入出力例外
	 */
	private void loadRectangle() throws NumberFormatException, IOException {
		final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
			this.baseDir + File.separator + this.mapName.toUpperCase() + File.separator
				+ this.mapName.toLowerCase() + ".txt")));
		String line;
		while ((line = in.readLine()) != null) {
			final StringTokenizer tokenizer = new StringTokenizer(line, ",");
			if (tokenizer.countTokens() == 10) {
				final int kei = Integer.parseInt(tokenizer.nextToken());
				//				double y2 = -Double.parseDouble(tokenizer.nextToken());
				//				double x1 = Double.parseDouble(tokenizer.nextToken());
				//				double y1 = -Double.parseDouble(tokenizer.nextToken());
				//				double x2 = Double.parseDouble(tokenizer.nextToken());
				//				setRectangle(new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1));
				// test
				final double x1 = Double.parseDouble(tokenizer.nextToken());
				final double y1 = Double.parseDouble(tokenizer.nextToken());
				final double x2 = Double.parseDouble(tokenizer.nextToken());
				final double y2 = Double.parseDouble(tokenizer.nextToken());
				final double x3 = Double.parseDouble(tokenizer.nextToken());
				final double y3 = Double.parseDouble(tokenizer.nextToken());
				final double x4 = Double.parseDouble(tokenizer.nextToken());
				final double y4 = Double.parseDouble(tokenizer.nextToken());
				final Point2D p1 = UTMUtil.toUTM(JasmineUtil.toLatLong(x1, y1, kei));
				final Point2D p2 = UTMUtil.toUTM(JasmineUtil.toLatLong(x2, y2, kei));
				final Point2D p3 = UTMUtil.toUTM(JasmineUtil.toLatLong(x3, y3, kei));
				final Point2D p4 = UTMUtil.toUTM(JasmineUtil.toLatLong(x4, y4, kei));
				//				final Rectangle2D rectangle = new Rectangle2D.Double();
				//				rectangle.setFrameFromDiagonal(p1.getY(), -p1.getX(), p2.getY(), -p2.getX());
				//				setRectangle(rectangle);
				final GeneralPath path = new GeneralPath();
				path.moveTo((float) p1.getX(), (float) -p1.getY());
				path.lineTo((float) p3.getX(), (float) -p3.getY());
				path.lineTo((float) p2.getX(), (float) -p2.getY());
				path.lineTo((float) p4.getX(), (float) -p4.getY());
				path.closePath();
				this.setRectangle(path);
				break;
			}
		}
		in.close();
	}

	/**
	 * 道の弧データを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadRoadArc() throws FileNotFoundException, IOException {
		final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "road" + File.separator + "roadntwk.arc";
		final String attributeFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "road" + File.separator + "road.atr";
		this.roadArc = new ConcurrentHashMap<String, ArcData>();
		if (new File(fileName).canRead()) {
			this.loadArc(this.roadArc, new BufferedReader(new InputStreamReader(new FileInputStream(
				fileName))));
			if (new File(attributeFileName).canRead()) {
				this.loadArcAttribute(this.roadArc, new BufferedReader(new InputStreamReader(
					new FileInputStream(attributeFileName), "SJIS")));
			}
		}
	}

	/** 
	 * 市町界のポリゴンファイルを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadSi_tyo() throws FileNotFoundException, IOException {
		final String polygonFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "gyousei" + File.separator + "si_tyo.pgn";
		this.si_tyo = new ConcurrentHashMap<String, PolygonData>();
		if (new File(polygonFileName).canRead()) {
			this.loadPolygon(this.si_tyo, this.gyousei, new BufferedReader(new InputStreamReader(
				new FileInputStream(polygonFileName))));
			/*
			 String attributeFileName = baseDir + File.separator + mapName.toUpperCase() + File.separator + "gyousei" + File.separator + "si_tyo.atr";
			 if (new File(attributeFileName).canRead()) {
			 loadPolygonAttribute(
			 si_tyo,
			 new BufferedReader(new InputStreamReader(new FileInputStream(attributeFileName), "SJIS")),
			 Polygon.CLASSIFICATION_SI_TYO);
			 }
			 */
		}
	}

	/**
	 * 建物のポリゴンファイルを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadTatemono() throws FileNotFoundException, IOException {
		final String polygonFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "tatemono" + File.separator + "tatemono.pgn";
		final String attributeFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "tatemono" + File.separator + "tatemono.atr";
		this.tatemono = new ConcurrentHashMap<String, PolygonData>();
		if (new File(polygonFileName).canRead()) {
			this.loadPolygon(this.tatemono, this.tatemonoArc, new BufferedReader(new InputStreamReader(
				new FileInputStream(polygonFileName))));
			if (new File(attributeFileName).canRead()) {
				this.loadPolygonAttribute(this.tatemono, new BufferedReader(new InputStreamReader(
					new FileInputStream(attributeFileName), "SJIS")),
					PolygonData.CLASSIFICATION_BUILDING);
			}
		}
	}

	/** 建物の弧ファイルを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadTatemonoArc() throws FileNotFoundException, IOException {
		final String fileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "tatemono" + File.separator + "tatemono.arc";
		this.tatemonoArc = new ConcurrentHashMap<String, ArcData>();
		if (new File(fileName).canRead()) {
			this.loadArc(this.tatemonoArc, new BufferedReader(new InputStreamReader(new FileInputStream(
				fileName))));
		}
	}

	/** 丁目界のポリゴンファイルを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadTyome() throws FileNotFoundException, IOException {
		this.tyome = new ConcurrentHashMap<String, PolygonData>();
		this.loadPolygon(this.tyome, this.gyousei, new BufferedReader(new InputStreamReader(
			new FileInputStream(this.baseDir + File.separator + this.mapName.toUpperCase()
				+ File.separator + "gyousei" + File.separator + "tyome.pgn"))));
		this.loadPolygonAttribute(this.tyome, new BufferedReader(new InputStreamReader(
			new FileInputStream(this.baseDir + File.separator + this.mapName.toUpperCase()
				+ File.separator + "gyousei" + File.separator + "tyome.atr"), "SJIS")),
			PolygonData.CLASSIFICATION_TYOME);
	}

	/**
	 * 場地のポリゴンデータを読み込みます。
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	void loadZyouti() throws FileNotFoundException, IOException {
		final String polygonFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "others" + File.separator + "zyouti.pgn";
		final String attributeFileName = this.baseDir + File.separator + this.mapName.toUpperCase()
			+ File.separator + "others" + File.separator + "zyouti.atr";
		this.zyouti = new ConcurrentHashMap<String, PolygonData>();
		if (new File(polygonFileName).canRead()) {
			this.loadPolygon(this.zyouti, this.others, new BufferedReader(new InputStreamReader(
				new FileInputStream(polygonFileName))));
			if (new File(attributeFileName).canRead()) {
				this.loadPolygonAttribute(this.zyouti, new BufferedReader(new InputStreamReader(
					new FileInputStream(attributeFileName), "SJIS")),
					PolygonData.CLASSIFICATION_PARK);
			}
		}
	}

	/**
	 * ポリゴンの図式分類コードを定数に変換します。
	 * @param code 図式分類コード
	 * @return 定数
	 */
	private int parsePolygonType(final String code) {
		if (code.equals("A1105")) {
			return PolygonData.CLASSIFICATION_SI_TYO;
		} else if (code.equals("A1106")) {
			return PolygonData.CLASSIFICATION_TYOME;
		} else if (code.equals("A6241")) {
			return PolygonData.CLASSIFICATION_RAILROAD;
		} else if (code.equals("A6242")) {
			return PolygonData.CLASSIFICATION_PARK;
		} else if (code.equals("A6243")) {
			return PolygonData.CLASSIFICATION_SCHOOL;
		} else if (code.equals("A6244")) {
			return PolygonData.CLASSIFICATION_TEMPLE;
		} else if (code.equals("A6215")) {
			return PolygonData.CLASSIFICATION_GRAVEYARD;
		} else if (code.equals("A6200")) {
			return PolygonData.CLASSIFICATION_OTHER;
		} else if (code.equals("A5101")) {
			return PolygonData.CLASSIFICATION_RIVER;
		} else if (code.equals("A5105")) {
			return PolygonData.CLASSIFICATION_LAKE;
		} else if (code.equals("A3500")) {
			return PolygonData.CLASSIFICATION_BUILDING;
		} else {
			return PolygonData.CLASSIFICATION_UNKNOWN;
		}
	}

	/**
	 * @param string
	 */
	private void setBaseDir(final String string) {
		this.baseDir = string;
	}

	/**
	 * @param string
	 */
	private void setMapName(final String string) {
		this.mapName = string;
	}

	/**
	 * この地図が表す領域を設定します。
	 * @param rectangle この地図が表す領域
	 */
	private void setRectangle(final Shape rectangle) {
		this.rectangle = rectangle;
	}

	@Override
	public String toString() {
		return "[MapData name=" + this.mapName + "]";
	}
}
