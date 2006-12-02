package map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地図を丁目単位で塗り分けるクラスです。
 * 作成日: 2004/01/09
 * @author zenjiro
 */
class PaintTyome {
	/**
	 * 地図が変化したかどうか
	 */
	private boolean isChanged;

	/** 地図を丁目単位で塗り分けます。
	 * このメソッドを呼び出した直後に isChanged() を呼び出すと、
	 * このメソッドによって地図の状態が変化したかどうかが取得できます。
	 * @param maps 地図
	 * @throws IOException 入出力例外
	 */
	void paintTyome(final Map<String, MapData> maps) throws IOException {
		this.isChanged = false;
		final Map<String, Collection<PolygonData>> attributePolygonMap = new ConcurrentHashMap<String, Collection<PolygonData>>(); // String -> Collection<Polygon> の Map
		// 属性をキー、ポリゴンの集合を値とする Map を初期化する
		for (final MapData mapData : maps.values()) {
			if (mapData.hasTyome()) {
				for (final PolygonData polygon : mapData.getTyome().values()) {
					final String attribute = polygon.getAttribute();
					if (attribute != null) {
						if (!attributePolygonMap.containsKey(attribute)) {
							attributePolygonMap.put(attribute, new ArrayList<PolygonData>());
						}
						attributePolygonMap.get(attribute).add(polygon);
					}
				}
			}
		}
		// 保存されている色を読み込む since 4.16
		for (final MapData mapData : maps.values()) {
			if (mapData.hasTyome()) {
				for (final PolygonData polygon : mapData.getTyome().values()) {
					final int color = this.loadColor(polygon);
					if (color != Const.Paint.NOT_FOUND) {
						polygon.setTyomeColorIndex(color);
						final String attribute = polygon.getAttribute();
						if (attribute != null) {
							if (attributePolygonMap.containsKey(attribute)) {
								for (final PolygonData polygon2 : attributePolygonMap.get(attribute)) {
									if (polygon != polygon2) {
										polygon2.setTyomeColorIndex(color);
									}
								}
							}
						}
					}
				}
			}
		}
		//System.out.println("attribute <-> polygon = " + attributePolygonMap);
		// Polygon をキー、Collection<Polygon> を値とする Map を作る
		final Map<PolygonData, Collection<PolygonData>> adjacentGraph = new ConcurrentHashMap<PolygonData, Collection<PolygonData>>();
		for (final MapData mapData : maps.values()) {
			if (mapData.hasTyome()) {
				final Map<String, Collection<PolygonData>> adjacentPolygons = mapData.getAdjacentGraph();
				for (final String polygonName : adjacentPolygons.keySet()) {
					adjacentGraph.put(mapData.getTyome().get(polygonName), adjacentPolygons.get(polygonName));
				}
			}
		}
		//System.out.println("隣接グラフ：" + adjacentGraph);
		// 塗り分ける
		for (final MapData mapData : maps.values()) {
			if (mapData.hasTyome()) {
				for (final PolygonData polygon : mapData.getTyome().values()) {
					if (polygon.getPolygonName() != null) {
						this.fixColorRecursively(polygon, adjacentGraph, attributePolygonMap);
					}
				}
			}
		}
	}

	/**
	 * 直前の塗り分けで、地図の状態が変化したかどうかを取得します。
	 * @return 地図の状態が変化したかどうか
	 */
	boolean isChanged() {
		return this.isChanged;
	}

	/**
	 * 指定したポリゴンの色を再帰的に決定します。
	 * red, green, blue, yellow, magenda, cyan の順に色を決めます。
	 * @param polygon ポリゴン
	 * @param adjacentGraph 隣接グラフ
	 * @param attributePolygonMap 属性とポリゴンの関連づけ
	 * @throws FileNotFoundException ファイル未検出例外
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 */
	private void fixColorRecursively(final PolygonData polygon,
			final Map<PolygonData, Collection<PolygonData>> adjacentGraph,
			final Map<String, Collection<PolygonData>> attributePolygonMap) throws FileNotFoundException,
			UnsupportedEncodingException {
		if (polygon.getTyomeColorIndex() != 0) {
			return;
		}
		final boolean[] isUsed = new boolean[8];
		this.addUsedColors(polygon, adjacentGraph, isUsed);
		final String attribute = polygon.getAttribute();
		if (attribute != null) {
			if (attributePolygonMap.containsKey(attribute)) {
				for (final PolygonData polygon2 : attributePolygonMap.get(attribute)) {
					if (polygon != polygon2) {
						this.addUsedColors(polygon2, adjacentGraph, isUsed);
					}
				}
			}
		}
		boolean isPainted = false;
		for (int i = 1; i < 8; ++i) {
			if (!isUsed[i]) {
				polygon.setTyomeColorIndex(i);
				if (this.loadColor(polygon) != i && this.loadColor(polygon) != Const.Paint.NOT_FOUND) {
					System.out.printf("%s: %sの色を%dから%dに変えました。\n", this.getClass().getName(), polygon, this
							.loadColor(polygon), i);
				}
				this.saveColor(polygon);
				isPainted = true;
				break;
			}
			if (!isPainted) {
				polygon.setTyomeColorIndex(8);
			}
		}
		this.isChanged = true;
		if (attribute != null) {
			if (attributePolygonMap.containsKey(attribute)) {
				for (final PolygonData polygon2 : attributePolygonMap.get(attribute)) {
					if (polygon != polygon2) {
						polygon2.setTyomeColorIndex(polygon.getTyomeColorIndex());
						if (this.loadColor(polygon2) != polygon.getTyomeColorIndex() && this.loadColor(polygon2) != Const.Paint.NOT_FOUND) {
							System.out.printf("%s: つられて%sの色を%dから%dに変えました。\n", this.getClass().getName(), polygon2, this
									.loadColor(polygon2), polygon.getTyomeColorIndex());
						}
						this.saveColor(polygon2);
					}
				}
			}
		}
		if (adjacentGraph.containsKey(polygon)) {
			for (final PolygonData polygon2 : adjacentGraph.get(polygon)) {
				this.fixColorRecursively(polygon2, adjacentGraph, attributePolygonMap);
			}
		}
	}

	/**
	 * あるポリゴンに隣接しているポリゴンの色を調べます。
	 * @param polygon ポリゴン
	 * @param adjacentGraph 隣接グラフ
	 * @param isUsed その色が使われているかどうか
	 */
	private void addUsedColors(final PolygonData polygon,
			final Map<PolygonData, Collection<PolygonData>> adjacentGraph, final boolean[] isUsed) {
		if (adjacentGraph.containsKey(polygon)) {
			for (final PolygonData polygon2 : adjacentGraph.get(polygon)) {
				if (polygon2.getTyomeColorIndex() < isUsed.length) {
					isUsed[polygon2.getTyomeColorIndex()] = true;
				}
			}
		}
	}

	/**
	 * ポリゴンの色を保存します。
	 * @param polygon ポリゴン
	 * @throws FileNotFoundException ファイル未検出例外
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 */
	private void saveColor(final PolygonData polygon) throws FileNotFoundException, UnsupportedEncodingException {
		final String id = polygon.getPolygonName();
		final String attribute = polygon.getAttribute();
		final int color = polygon.getTyomeColorIndex();
		new File(Const.Paint.CACHE_DIR).mkdirs();
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(
				Const.Paint.CACHE_DIR + File.separator + id + Const.Paint.CACHE_SUFFIX), true), Const.Paint.ENCODING));
		out.println(attribute + "," + color);
		out.close();
	}

	/**
	 * ポリゴンの色を読み込みます。
	 * @param polygon ポリゴン
	 * @return 色
	 */
	private int loadColor(final PolygonData polygon) {
		final String id = polygon.getPolygonName();
		final String attribute = polygon.getAttribute();
		try {
			final Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(new File(
					Const.Paint.CACHE_DIR + File.separator + id + Const.Paint.CACHE_SUFFIX)), Const.Paint.ENCODING));
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final Scanner scanner2 = new Scanner(line);
				scanner2.useDelimiter(",");
				if (scanner2.hasNext()) {
					final String attribute2 = scanner2.next();
					if (attribute.equals(attribute2)) {
						if (scanner2.hasNextInt()) {
							final int color = scanner2.nextInt();
							return color;
						}
					}
				}
			}
			scanner.close();
			return Const.Paint.NOT_FOUND;
		} catch (final IOException e) {
			return Const.Paint.NOT_FOUND;
		}
	}
}
