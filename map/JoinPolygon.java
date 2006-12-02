package map;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 複数の図郭にまたがったポリゴンを見かけ上結合し、
 * 丁目の属性を結合されたポリゴンの中央に配置するクラスです。
 * @author zenjiro
 * 作成日: 2004/01/09
 */
class JoinPolygon {
	/**
	 * 変更されたかどうか
	 */
	private boolean isChanged;

	/** 複数の図郭にまたがったポリゴンを見かけ上結合します。
	 * このメソッドを呼び出した直後に isChanged() を呼び出すと、
	 * このメソッドによって地図の状態が変化したかどうかが取得できます。
	 * @param maps 地図
	 * @param visibleRectangle 現在表示されている領域（仮想座標）
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	void joinPolygon(final Map<String, MapData> maps, final Rectangle2D visibleRectangle) throws IOException {
		this.isChanged = false;
		final Map<PolygonData, Set<PolygonData>> joiningPolygons = new ConcurrentHashMap<PolygonData, Set<PolygonData>>();
		for (final MapData mapData : maps.values()) {
			if (mapData.hasTyome()) {
				if (mapData.getBounds().intersects(visibleRectangle)) {
					final Map<String, String> edgePolygons = mapData.getEdgePolygons();
					if (edgePolygons != null) {
						for (final MapData mapData2 : maps.values()) {
							if (mapData2.hasTyome()) {
								if (mapData2.getBounds().intersects(visibleRectangle)) {
									final Map<String, String> edgePolygons2 = mapData2
											.getEdgePolygons();
									if (edgePolygons2 != null) {
										if (mapData.hashCode() < mapData2.hashCode()) {
											//System.out.println("1 = " + mapData.getMapName() + ", 2 = " + mapData2.getMapName());
											//System.out.println("edgePolygons = " + edgePolygons);
											//System.out.println("edgePolygons2 = " + edgePolygons2);
											for (final Map.Entry<String, String> entry3 : edgePolygons
													.entrySet()) {
												final String key = entry3.getKey();
												final String polygonName = entry3.getValue();
												if (mapData.getTyome().containsKey(polygonName)) {
													final PolygonData polygon = mapData.getTyome().get(
															polygonName);
													if (polygon.getArea().getBounds2D().intersects(
															visibleRectangle)) {
														if (edgePolygons2.containsKey(key)) {
															final String polygonName2 = edgePolygons2
																	.get(key);
															final Map<String, PolygonData> polygons2 = mapData2
																	.getTyome();
															if (polygons2.containsKey(polygonName2)) {
																final PolygonData polygon2 = polygons2
																		.get(polygonName2);
																if ((polygon.getAttribute() != null)
																		&& ((polygon2)
																				.getAttribute() != null)) {
																	if (polygon
																			.getAttribute()
																			.equals(
																					(polygon2)
																							.getAttribute())) {
																		//System.out.println(polygonName + " is joining to " + polygonName2);
																		if (!joiningPolygons
																				.containsKey(polygon)) {
																			joiningPolygons
																					.put(
																							polygon,
																							new HashSet<PolygonData>());
																		}
																		joiningPolygons
																				.get(polygon).add(
																						polygon2);
																	} else {
																		System.out
																				.println("WARNING: "
																						+ polygon
																						+ "と"
																						+ polygons2
																								.get(polygonName2)
																						+ "は属性が一致しません。");
																	}
																}
															}
														}
													}
												}
												if (mapData.hasZyouti()) {
													if (mapData.getZyouti()
															.containsKey(polygonName)) {
														final PolygonData polygon = mapData.getZyouti()
																.get(polygonName);
														if (polygon.getArea().getBounds2D()
																.intersects(visibleRectangle)) {
															if (edgePolygons2.containsKey(key)) {
																final String polygonName2 = edgePolygons2
																		.get(key);
																if (mapData2.hasZyouti()) {
																	final Map<String, PolygonData> polygons2 = mapData2
																			.getZyouti();
																	if (polygons2
																			.containsKey(polygonName2)) {
																		if ((polygon.getAttribute() != null)
																				&& polygons2
																						.get(
																								polygonName2)
																						.getAttribute() != null) {
																			if (polygon
																					.getAttribute()
																					.equals(
																							polygons2
																									.get(
																											polygonName2)
																									.getAttribute())) {
																				//System.out.println(polygonName + " is joining to " + polygonName2);
																				if (!joiningPolygons
																						.containsKey(polygon)) {
																					joiningPolygons
																							.put(
																									polygon,
																									new HashSet<PolygonData>());
																				}
																				joiningPolygons
																						.get(
																								polygon)
																						.add(
																								polygons2
																										.get(polygonName2));
																			} else {
																				//System.out.println(polygon + "と" + polygon2 + "は属性が一致しません。");
																			}
																		}
																	}
																}
															}
														}
													}
												}
												if (mapData.hasMizu()) {
													if (mapData.getMizu().containsKey(polygonName)) {
														final PolygonData polygon = mapData.getMizu()
																.get(polygonName);
														if (polygon.getArea().getBounds2D()
																.intersects(visibleRectangle)) {
															if (edgePolygons2.containsKey(key)) {
																final String polygonName2 = edgePolygons2
																		.get(key);
																if (mapData2.hasMizu()) {
																	final Map<String, PolygonData> polygons2 = mapData2
																			.getMizu();
																	if (polygons2
																			.containsKey(polygonName2)) {
																		if ((polygon.getAttribute() != null)
																				&& ((polygons2
																						.get(polygonName2))
																						.getAttribute() != null)) {
																			if (polygon
																					.getAttribute()
																					.equals(
																							(polygons2
																									.get(polygonName2))
																									.getAttribute())) {
																				//System.out.println(polygonName + " is joining to " + polygonName2);
																				if (!joiningPolygons
																						.containsKey(polygon)) {
																					joiningPolygons
																							.put(
																									polygon,
																									new HashSet<PolygonData>());
																				}
																				joiningPolygons
																						.get(
																								polygon)
																						.add(
																								polygons2
																										.get(polygonName2));
																			} else {
																				//System.out.println(polygon + "と" + polygon2 + "は属性が一致しません。");
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		final ArrayList<Set<PolygonData>> finalPolygons = new ArrayList<Set<PolygonData>>();
		for (final Entry<PolygonData, Set<PolygonData>> entry : joiningPolygons.entrySet()) {
			final PolygonData key = entry.getKey();
			final Set<PolygonData> values = entry.getValue();
			boolean isKeyFound = false;
			for (final Set<PolygonData> set : finalPolygons) {
				if (set.contains(key)) {
					set.addAll(values);
					isKeyFound = true;
				}
			}
			for (final PolygonData value : values) {
				for (final Set<PolygonData> set : finalPolygons) {
					if (set.contains(value)) {
						set.add(key);
						set.addAll(values);
						isKeyFound = true;
					}
				}
			}
			if (!isKeyFound) {
				final Set<PolygonData> set = new LinkedHashSet<PolygonData>();
				set.add(key);
				set.addAll(values);
				finalPolygons.add(set);
			}
		}
		for (final Set<PolygonData> polygons : finalPolygons) {
			final Area area = new Area();
			for (final PolygonData polygon : polygons) {
				if (polygon != null) {
					area.add(polygon.getArea());
				}
			}
			for (final PolygonData polygon : polygons) {
				if (polygon != null) {
					final double x = area.getBounds().getCenterX();
					final double y = area.getBounds().getCenterY();
					if ((polygon.getX() != x) || (polygon.getY() != y)) {
						polygon.setX(x);
						polygon.setY(y);
						this.isChanged = true;
					}
					polygon.setArea(area);
				}
			}
		}
	}

	/**
	 * 直前の結合で、地図の状態が変化したかどうかを取得します。
	 * @return 地図の状態が変化したかどうか
	 */
	boolean isChanged() {
		return this.isChanged;
	}
}
