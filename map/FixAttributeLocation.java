package map;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import map.Const.Fonts;
import map.KsjRailway.Railway;
import map.KsjRailway.Station;
import search.CellSearch;
import search.Search;

/**
 * 属性を描画する座標を計算するクラスです。
 * @author zenjiro
 * 作成日: 2004/01/16
 */
class FixAttributeLocation {
	
	/** 属性を描画する座標を決定します。
	 * @param maps 地図
	 * @param prefectures 都道府県の一覧
	 * @param panel パネル
	 * @throws IOException 
	 */
	public void fixAttributeLocation(final Map<String, MapData> maps, final Collection<Prefecture> prefectures,
			final MapPanel panel) throws IOException {
		final MapPreferences preferences = panel.getMapPreferences();
		final Font tatemonoFont = preferences.getTatemonoPreferences().getFont();
		final Font zyoutiFont = preferences.getZyoutiPreferences().getFont();
		final Font mizuFont = preferences.getMizuPreferences().getFont();
		final Font tyomeFont1 = new Font(Fonts.GOTHIC, Font.PLAIN, (int) (18 * panel.getFontZoom()));
		final Font tyomeFont2 = new Font(Fonts.GOTHIC, Font.PLAIN, (int) (16 * panel.getFontZoom()));
		final Font tyomeFont3 = new Font(Fonts.GOTHIC, Font.PLAIN, (int) (14 * panel.getFontZoom()));
		final Font ekiFont = preferences.getEkiPreferences().getFont();
		final Font roadFont = preferences.getNormalRoadPreferences().getFont();
		final Font tetudouFont = preferences.getRailwayPreferences().getFont();
		final Search search = new CellSearch(1000, 100);
		final Rectangle2D visibleRectangle = panel.getVisibleRectangle(true); // 現在画面に表示されている範囲（仮想座標）
		final Collection<Point> usedPoints = new HashSet<Point>(); // 既に配置されたポリゴンの中心座標（仮想座標）
		final double tatemonoPointSize = 4; // 建物に表示する点の直径
		final double ekiPointSize = 8; // 駅に表示する点の直径
		final double zoom = panel.getZoom();
		if (zoom >= Const.Zoom.LOAD_ALL) {
			// 駅の属性の表示位置を計算する
			for (final MapData mapData : maps.values()) {
				if (mapData.hasEki()) {
					if (panel.isVisible(mapData.getBounds())) {
						for (final PointData point : mapData.getEki().values()) {
							final Rectangle2D pointRectangle = new Rectangle2D.Double(point.getX()
									- (ekiPointSize / 2 / zoom), point.getY() - (ekiPointSize / 2 / zoom), ekiPointSize
									/ zoom, ekiPointSize / zoom);
							search.insert(pointRectangle, null);
						}
					}
				}
			}
			for (final MapData mapData : maps.values()) {
				if (mapData.hasEki()) {
					if (panel.isVisible(mapData.getBounds())) {
						final FontMetrics metrics = panel.getFontMetrics(ekiFont);
						final double attributeHeight = metrics.getHeight() / zoom;
						for (final PointData point : mapData.getEki().values()) {
							this.fixPointAttributeLocation(point, usedPoints, visibleRectangle, zoom, attributeHeight,
									metrics, ekiPointSize, search);
						}
					}
				}
			}
		}
		// 国土数値情報の駅の表示位置を計算する
		if (zoom < Const.Zoom.LOAD_ALL && zoom >= Const.Zoom.LOAD_FINE_CITIES) {
			final FontMetrics metrics = panel.getFontMetrics(panel.getMapPreferences()
					.getKsjRailwayStationPreferences().getFont());
			final double attributeHeight = metrics.getHeight() / zoom;
			for (final Prefecture prefecture : panel.getPrefectures()) {
				for (final Station station : prefecture.getKsjRailwayStations()) {
					if (panel.isVisible(station.getShape())) {
						final PointData point = new PointData(null, -1, station.getShape().getBounds2D().getCenterX(),
								station.getShape().getBounds2D().getCenterY());
						point.setAttribute(station.getStation());
						this.fixPointAttributeLocation(point, usedPoints, visibleRectangle, zoom, attributeHeight,
								metrics, ekiPointSize, search);
						station.setCaptionLocation(new Point2D.Double(point.getAttributeX(), point.getAttributeY()));
					}
				}
			}
		}
		// 国土数値情報の鉄道の表示位置を計算する
		if (zoom < Const.Zoom.LOAD_ALL && zoom >= Const.Zoom.LOAD_FINE_CITIES) {
			final FontMetrics metrics = panel.getFontMetrics(panel.getMapPreferences().getKsjRailwayPreferences()
					.getFont());
			final double captionHeight = metrics.getHeight() / zoom;
			final Collection<String> fixedCaptions = new HashSet<String>();
			for (final Prefecture prefecture : panel.getPrefectures()) {
				for (final Railway curve : prefecture.getKsjRailwayCurves()) {
					curve.setCaptionLocation(null);
					if (!fixedCaptions.contains(curve.getCaption())) {
						final double captionWidth = metrics.stringWidth(curve.getCaption()) / zoom;
						final PathIterator iterator = curve.getShape().getPathIterator(new AffineTransform());
						final double[] coords = new double[6];
						while (!iterator.isDone()) {
							final int segment = iterator.currentSegment(coords);
							if (segment == PathIterator.SEG_LINETO) {
								final double x = coords[0];
								final double y = coords[1];
								final Rectangle2D captionRectangle = new Rectangle2D.Double(x - captionWidth / 2, y
										- captionHeight, captionWidth, captionHeight);
								if (visibleRectangle.contains(captionRectangle)
										&& search.search(captionRectangle).isEmpty()) {
									curve.setCaptionLocation(new Point2D.Double(x - captionWidth / 2, y));
									fixedCaptions.add(curve.getCaption());
									search.insert(captionRectangle, null);
									break;
								}
							}
							iterator.next();
						}
					}
				}
			}
		}
		// 店舗の表示位置を計算する
		if (zoom >= Const.Zoom.LOAD_ALL) {
			final FontMetrics metrics = panel.getFontMetrics(panel.getMapPreferences().getTatemonoPreferences()
					.getFont());
			final double attributeHeight = metrics.getHeight() / zoom;
			if (prefectures != null) {
				for (final Prefecture prefecture : prefectures) {
					if (prefecture.hasCities()) {
						for (final City city : prefecture.getCities()) {
							if (panel.isVisible(city.hasFineShape() ? city.getFineShape() : city.getShape())) {
								if (city.hasShops()) {
									for (final PointData point : city.getShops()) {
										this.fixPointAttributeLocation(point, usedPoints, visibleRectangle, zoom,
												attributeHeight, metrics, tatemonoPointSize, search);
									}
								}
							}
						}
					}
				}
			}
		}
		if (zoom >= Const.Zoom.LOAD_FINE_ROAD) {
			// 公共建物の属性の表示位置を計算する
			for (final MapData mapData : maps.values()) {
				if (mapData.hasTatemono()) {
					if (panel.isVisible(mapData.getBounds())) {
						final FontMetrics metrics = panel.getFontMetrics(tatemonoFont);
						final double attributeHeight = metrics.getHeight() / zoom;
						for (final PolygonData polygon : mapData.getTatemono().values()) {
							this.fixTatemonoAttributeLocation(polygon, usedPoints, visibleRectangle, zoom,
									attributeHeight, metrics, tatemonoPointSize, search);
						}
					}
				}
			}
			// 場地の属性の表示位置を計算する
			for (final MapData mapData : maps.values()) {
				if (mapData.hasZyouti()) {
					fixAttributeLocation(panel, zyoutiFont, search, visibleRectangle, usedPoints, tatemonoPointSize,
							zoom, mapData, mapData.getZyouti());
				}
			}
			// 内水面の属性の表示位置を計算する
			for (final MapData mapData : maps.values()) {
				if (mapData.hasMizu()) {
					fixAttributeLocation(panel, mizuFont, search, visibleRectangle, usedPoints, tatemonoPointSize,
							zoom, mapData, mapData.getMizu());
				}
			}
			// 道路の属性の表示位置を計算する
			{
				final FontMetrics metrics = panel.getFontMetrics(roadFont);
				final double attributeHeight = metrics.getHeight() / zoom;
				final Collection<String> fixedAttributes = new HashSet<String>();
				for (final MapData mapData : maps.values()) {
					if (mapData.hasRoadArc()) {
						final Map<String, ArcData> arcs = mapData.getRoadArc();
						fixAttributeLocation(arcs, panel, search, visibleRectangle, zoom, metrics, attributeHeight,
								fixedAttributes, mapData);
					} else if (mapData.hasLargeRoadArc()) {
						final Map<String, ArcData> arcs = mapData.getLargeRoadArc();
						fixAttributeLocation(arcs, panel, search, visibleRectangle, zoom, metrics, attributeHeight,
								fixedAttributes, mapData);
					}
				}
			}
			// 鉄道の属性の表示位置を計算する
			{
				final FontMetrics metrics = panel.getFontMetrics(tetudouFont);
				final double attributeHeight = metrics.getHeight() / zoom;
				final Collection<String> fixedAttributes = new HashSet<String>();
				for (final MapData mapData : maps.values()) {
					if (mapData.hasOthers()) {
						if (panel.isVisible(mapData.getBounds())) {
							for (final ArcData arc : mapData.getOthers().values()) {
								final String attribute = arc.getAttribute();
								if (attribute != null) {
									arc.setAttributeLocation(0, 0);
									if (!fixedAttributes.contains(attribute)) {
										final double attributeWidth = metrics.stringWidth(arc.getAttribute()) / zoom;
										final PathIterator iter3 = arc.getPath().getPathIterator(new AffineTransform());
										final double[] coords = new double[6];
										while (!iter3.isDone()) {
											final int currentSegment = iter3.currentSegment(coords);
											if (currentSegment == PathIterator.SEG_LINETO) {
												final double currentX = coords[0];
												final double currentY = coords[1];
												final Rectangle2D attributeRectangle = new Rectangle2D.Double(currentX
														- (attributeWidth / 2), currentY - attributeHeight,
														attributeWidth, attributeHeight);
												if (visibleRectangle.contains(attributeRectangle)
														&& search.search(attributeRectangle).isEmpty()) {
													arc.setAttributeLocation(currentX - (attributeWidth / 2), currentY);
													fixedAttributes.add(attribute);
													search.insert(attributeRectangle, null);
													break;
												}
											}
											iter3.next();
										}
									}
								}
							}
						}
					}
				}
			}
			// 丁目の属性の表示位置を計算する
			for (final MapData mapData : maps.values()) {
				if (mapData.hasTyome()) {
					if (panel.isVisible(mapData.getBounds())) {
						for (final PolygonData polygon : mapData.getTyome().values()) {
							polygon.setAttributeLocation(0, 0);
							this.fixTyomeAttributeLocation(polygon, tyomeFont1, visibleRectangle, usedPoints, panel,
									true, search);
							this.fixTyomeAttributeLocation(polygon, tyomeFont2, visibleRectangle, usedPoints, panel,
									true, search);
							this.fixTyomeAttributeLocation(polygon, tyomeFont3, visibleRectangle, usedPoints, panel,
									false, search);
						}
					}
				}
			}
			// 町丁目の読みの表示位置を計算する
			if (zoom >= Const.Zoom.LOAD_ALL) {
				for (final MapData mapData : maps.values()) {
					if (mapData.hasTyome()) {
						if (panel.isVisible(mapData.getBounds())) {
							for (final PolygonData polygon : mapData.getTyome().values()) {
								final Font yomiFont = preferences.getTyomePreferences().getFont();
								final FontMetrics yomiMetrics = panel.getFontMetrics(yomiFont);
								final Font tyomeFont = polygon.getTyomeFont();
								if (tyomeFont != null) {
									final FontMetrics tyomeMetrics = panel.getFontMetrics(tyomeFont);
									if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_TYOME) {
										if (polygon.getAttribute() != null) {
											if (panel.getVisibleRectangle(true).contains(polygon.getAttributeX(),
													polygon.getAttributeY())) {
												if (polygon.hasYomi()) {
													final double kanjiWidth = tyomeMetrics.stringWidth(polygon
															.getAttribute().replaceFirst("[0-9０-９]+$", ""))
															/ zoom;
													final double kanjiHeight = tyomeMetrics.getHeight() / zoom;
													final double yomiWidth = yomiMetrics.stringWidth(polygon.getYomi())
															/ zoom;
													final double yomiHeight = yomiMetrics.getHeight() / zoom;
													final double yomiX = polygon.getAttributeX()
															+ (kanjiWidth - yomiWidth) / 2;
													final double yomiY = polygon.getAttributeY() - kanjiHeight;
													final Rectangle2D rectangle = new Rectangle2D.Double(yomiX, yomiY
															- yomiHeight, yomiWidth, yomiHeight);
													if (panel.getVisibleRectangle(true).contains(rectangle)) {
														if (search.search(rectangle).isEmpty()) {
															polygon.setYomiLocation(yomiX, yomiY);
															search.insert(rectangle, null);
														} else {
															polygon.setYomiLocation(0, 0);
														}
													} else {
														polygon.setYomiLocation(0, 0);
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
		// 街区レベル位置参照情報の表示位置を計算する
		if (zoom >= Const.Zoom.LOAD_ALL) {
			final FontMetrics metrics = panel.getFontMetrics(panel.getMapPreferences().getIsjPreferences().getFont());
			final double attributeHeight = metrics.getHeight() / zoom;
			if (prefectures != null) {
				for (final Prefecture prefecture : prefectures) {
					if (prefecture.hasCities()) {
						for (final City city : prefecture.getCities()) {
							if (panel.isVisible(city.hasFineShape() ? city.getFineShape() : city.getShape())) {
								city.clearIsjLabels();
								if (city.hasIsj()) {
									for (final Map.Entry<String, Point2D> entry : city.getIsj().entrySet()) {
										final String key = entry.getKey();
										final String[] strings = key.split(",");
										if (strings.length == 4) {
											final Point2D point = entry.getValue();
											if (panel.getVisibleRectangle(true).contains(point)) {
												final double attributeWidth = metrics.stringWidth(strings[3]) / zoom;
												final Rectangle2D rectangle = new Rectangle2D.Double(point.getX()
														- attributeWidth / 2, point.getY() - attributeHeight / 2,
														attributeWidth, attributeHeight);
												if (panel.getVisibleRectangle(true).contains(rectangle)) {
													if (search.search(rectangle).isEmpty()) {
														city.getIsjLabels()
																.put(
																		new Point2D.Double(point.getX()
																				- attributeWidth / 2, point.getY()
																				+ attributeHeight / 2), strings[3]);
														search.insert(rectangle, null);
													}
												}
											}
										} else {
											System.out.println("WARNING: 街区レベル位置参照情報の形式が不正です。" + key);
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

	/**
	 * 道路の属性位置を決定します。
	 * @param arcs 弧の一覧
	 * @param panel パネル
	 * @param search 検索エンジン
	 * @param visibleRectangle 表示されている範囲
	 * @param zoom 表示倍率
	 * @param metrics フォントメトリクス
	 * @param attributeHeight 属性の高さ
	 * @param fixedAttributes 決定済みの属性の一覧
	 * @param mapData 地図
	 */
	private void fixAttributeLocation(final Map<String, ArcData> arcs, final MapPanel panel, final Search search,
			final Rectangle2D visibleRectangle, final double zoom, final FontMetrics metrics,
			final double attributeHeight, final Collection<String> fixedAttributes, final MapData mapData) {
		if (panel.isVisible(mapData.getBounds())) {
			for (final ArcData arc : arcs.values()) {
				final String attribute = arc.getAttribute();
				if (attribute != null) {
					arc.setAttributeLocation(0, 0);
					if (!fixedAttributes.contains(attribute)) {
						final double attributeWidth = metrics.stringWidth(arc.getAttribute()) / zoom;
						final Point2D currentPoint = arc.getPath().getCurrentPoint();
						final Rectangle2D attributeRectangle = new Rectangle2D.Double(currentPoint.getX()
								- (attributeWidth / 2), currentPoint.getY() - attributeHeight, attributeWidth,
								attributeHeight);
						if (visibleRectangle.contains(attributeRectangle)
								&& search.search(attributeRectangle).isEmpty()) {
							arc.setAttributeLocation(currentPoint.getX() - (attributeWidth / 2), currentPoint.getY());
							fixedAttributes.add(attribute);
							search.insert(attributeRectangle, null);
						}
					}
				}
			}
		}
	}

	/**
	 * 場地、内水面の属性位置を決定します。
	 * @param panel パネル
	 * @param font フォント
	 * @param search 検索エンジン
	 * @param visibleRectangle 表示されている範囲
	 * @param usedPoints 決定済みの点
	 * @param tatemonoPointSize これ意味あるの？
	 * @param zoom 表示倍率
	 * @param mapData 地図
	 * @param polygons ポリゴンの一覧
	 */
	private void fixAttributeLocation(final MapPanel panel, final Font font, final Search search,
			final Rectangle2D visibleRectangle, final Collection<Point> usedPoints, final double tatemonoPointSize,
			final double zoom, final MapData mapData, final Map<String, PolygonData> polygons) {
		if (panel.isVisible(mapData.getBounds())) {
			final FontMetrics metrics = panel.getFontMetrics(font);
			final double attributeHeight = metrics.getHeight() / zoom;
			for (final PolygonData polygon : polygons.values()) {
				this.fixPolygonAttributeLocation(polygon, usedPoints, visibleRectangle, zoom, attributeHeight, metrics,
						tatemonoPointSize, search);
			}
		}
	}

	/**
	 * 町丁目の属性位置を決定します。
	 * @param polygon ポリゴン
	 * @param font フォント
	 * @param visibleRectangle 表示されている矩形
	 * @param usedPoints 既に属性が配置された点
	 * @param panel パネル
	 * @param contains 完全に含まれているかどうか
	 * @param search 既に属性が配置されている領域を検索するためのオブジェクト
	 */
	private void fixTyomeAttributeLocation(final PolygonData polygon, final Font font,
			final Rectangle2D visibleRectangle, final Collection<Point> usedPoints, final MapPanel panel,
			final boolean contains, final Search search) {
		if (polygon.getAttribute() != null) {
			final double zoom = panel.getZoom();
			final FontMetrics metrics = panel.getFontMetrics(font);
			final double attributeHeight = metrics.getHeight() / zoom;
			final double size = 4;
			final double attributeWidth = metrics.stringWidth(polygon.getAttribute()) / zoom;
			if (contains) {
				if (polygon.getArea().getBounds().getWidth() < attributeWidth) {
					return;
				}
			}
			final Rectangle2D pointRectangle = new Rectangle2D.Double(-polygon.getX() - (size / zoom), -polygon.getY()
					- (size / zoom), (size * 2) / zoom, (size * 2) / zoom);
			Rectangle2D attributeRectangle;
			// ポリゴンの中心
			attributeRectangle = new Rectangle2D.Double(polygon.getX() - (attributeWidth / 2), polygon.getY()
					- (attributeHeight / 2), attributeWidth, attributeHeight);
			final Point point = new Point((int) polygon.getX(), (int) polygon.getY());
			if (this.isTyomePutable(polygon, point, usedPoints, attributeRectangle, visibleRectangle, contains, search)) {
				polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				polygon.setTyomeFont(font);
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(point);
			}
			final int div = 4; // 分割数
			double dx; // 動かす幅
			double dy; // 動かす高さ
			dx = (polygon.getArea().getBounds().getWidth() - attributeWidth) / 2 / div;
			dy = (polygon.getArea().getBounds().getHeight() - attributeHeight) / 2 / div;
			for (int i = 1; i <= div; i++) {
				// ポリゴンの中心から上
				attributeRectangle = new Rectangle2D.Double(polygon.getX() - (attributeWidth / 2), polygon.getY()
						- (attributeHeight / 2) - (dy * i), attributeWidth, attributeHeight);
				if (this.isTyomePutable(polygon, point, usedPoints, attributeRectangle, visibleRectangle, contains,
						search)) {
					polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
					polygon.setTyomeFont(font);
					search.insert(pointRectangle, null);
					search.insert(attributeRectangle, null);
					usedPoints.add(point);
				}
				// ポリゴンの中心から下
				attributeRectangle = new Rectangle2D.Double(polygon.getX() - (attributeWidth / 2), polygon.getY()
						- (attributeHeight / 2) + (dy * i), attributeWidth, attributeHeight);
				if (this.isTyomePutable(polygon, point, usedPoints, attributeRectangle, visibleRectangle, contains,
						search)) {
					polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
					polygon.setTyomeFont(font);
					search.insert(pointRectangle, null);
					search.insert(attributeRectangle, null);
					usedPoints.add(point);
				}
				// ポリゴンの中心から右
				attributeRectangle = new Rectangle2D.Double(polygon.getX() - (attributeWidth / 2) + (dx * i), polygon
						.getY()
						- (attributeHeight / 2), attributeWidth, attributeHeight);
				if (this.isTyomePutable(polygon, point, usedPoints, attributeRectangle, visibleRectangle, contains,
						search)) {
					polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
					polygon.setTyomeFont(font);
					search.insert(pointRectangle, null);
					search.insert(attributeRectangle, null);
					usedPoints.add(point);
				}
				// ポリゴンの中心から左
				attributeRectangle = new Rectangle2D.Double(polygon.getX() - (attributeWidth / 2) - (dx * i), polygon
						.getY()
						- (attributeHeight / 2), attributeWidth, attributeHeight);
				if (this.isTyomePutable(polygon, point, usedPoints, attributeRectangle, visibleRectangle, contains,
						search)) {
					polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
					polygon.setTyomeFont(font);
					search.insert(pointRectangle, null);
					search.insert(attributeRectangle, null);
					usedPoints.add(point);
				}
			}
		}
	}

	/**
	 * 点の属性の表示位置を決定します。
	 * @param point 点
	 * @param usedPoints 既に属性が配置されたポリゴンの中心座標
	 * @param visibleRectangle 現在表示されている領域
	 * @param zoom 倍率
	 * @param attributeHeight 属性の高さ
	 * @param metrics フォントメトリクス
	 * @param size 点の直径
	 * @param search 属性によって使用済みの領域を検索するためのオブジェクト
	 */
	private void fixPointAttributeLocation(final PointData point, final Collection<Point> usedPoints,
			final Rectangle2D visibleRectangle, final double zoom, final double attributeHeight,
			final FontMetrics metrics, final double size, final Search search) {
		if (point.getAttribute() != null) {
			point.setAttributeLocation(0, 0);
			final double attributeWidth = metrics.stringWidth(point.getAttribute()) / zoom;
			Rectangle2D attributeRectangle;
			final Rectangle2D pointRectangle = new Rectangle2D.Double(point.getX() - (size / zoom), point.getY()
					- (size / zoom), (size * 2) / zoom, (size * 2) / zoom);
			// 点の右
			attributeRectangle = new Rectangle2D.Double(point.getX() + (size / zoom), point.getY()
					- (attributeHeight / 2), attributeWidth, attributeHeight);
			final Point location = new Point((int) point.getX(), (int) point.getY());
			if (this.isPointPutable(attributeRectangle, location, usedPoints, visibleRectangle, search)) {
				point.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(location);
			}
			// 点の左
			attributeRectangle = new Rectangle2D.Double(point.getX() - attributeWidth - (size / zoom), point.getY()
					- (attributeHeight / 2), attributeWidth, attributeHeight);
			if (this.isPointPutable(attributeRectangle, location, usedPoints, visibleRectangle, search)) {
				point.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(location);
			}
			// 点の右上
			attributeRectangle = new Rectangle2D.Double(point.getX() + (size / zoom), point.getY() - attributeHeight,
					attributeWidth, attributeHeight);
			if (this.isPointPutable(attributeRectangle, location, usedPoints, visibleRectangle, search)) {
				point.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(location);
			}
			// 点の右下
			attributeRectangle = new Rectangle2D.Double(point.getX() + (size / zoom), point.getY(), attributeWidth,
					attributeHeight);
			if (this.isPointPutable(attributeRectangle, location, usedPoints, visibleRectangle, search)) {
				point.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(location);
			}
			// 点の左上
			attributeRectangle = new Rectangle2D.Double(point.getX() - attributeWidth - (size / zoom), point.getY()
					- attributeHeight, attributeWidth, attributeHeight);
			if (this.isPointPutable(attributeRectangle, location, usedPoints, visibleRectangle, search)) {
				point.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(location);
			}
			// 点の左下
			attributeRectangle = new Rectangle2D.Double(point.getX() - attributeWidth - (size / zoom), point.getY(),
					attributeWidth, attributeHeight);
			if (this.isPointPutable(attributeRectangle, location, usedPoints, visibleRectangle, search)) {
				point.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(location);
			}
		}
	}

	/**
	 * 建物の属性の表示位置を決定します。
	 * @param polygon ポリゴン
	 * @param usedPoints 既に属性が配置されたポリゴンの中心座標
	 * @param visibleRectangle 現在表示されている領域
	 * @param zoom 倍率
	 * @param attributeHeight 属性の高さ
	 * @param metrics フォントメトリクス
	 * @param size 点の直径
	 * @param search 属性によって使用済みの領域を検索するためのオブジェクト
	 */
	private void fixTatemonoAttributeLocation(final PolygonData polygon, final Collection<Point> usedPoints,
			final Rectangle2D visibleRectangle, final double zoom, final double attributeHeight,
			final FontMetrics metrics, final double size, final Search search) {
		if ((polygon.getAttribute() != null) && (polygon.getTatemonoCode() != PolygonData.TATEMONO_STATION)) {
			polygon.setAttributeLocation(0, 0);
			final double attributeWidth = metrics.stringWidth(polygon.getAttribute()) / zoom;
			final Rectangle2D pointRectangle = new Rectangle2D.Double(polygon.getX() - (size / zoom), polygon.getY()
					- (size / zoom), (size * 2) / zoom, (size * 2) / zoom);
			Rectangle2D attributeRectangle;
			// 点の右
			attributeRectangle = new Rectangle2D.Double(polygon.getX() + (size / zoom), polygon.getY()
					- (attributeHeight / 2), attributeWidth, attributeHeight);
			final Point point = new Point((int) polygon.getX(), (int) polygon.getY());
			if (this.isPutable(point, usedPoints, attributeRectangle, visibleRectangle, search)) {
				polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(point);
			}
			// 点の左
			attributeRectangle = new Rectangle2D.Double(polygon.getX() - attributeWidth - (size / zoom), polygon.getY()
					- (attributeHeight / 2), attributeWidth, attributeHeight);
			if (this.isPutable(point, usedPoints, attributeRectangle, visibleRectangle, search)) {
				polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(point);
			}
			// 点の右上
			attributeRectangle = new Rectangle2D.Double(polygon.getX() + (size / 2 / zoom), polygon.getY()
					- attributeHeight, attributeWidth, attributeHeight);
			if (this.isPutable(point, usedPoints, attributeRectangle, visibleRectangle, search)) {
				polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(point);
			}
			// 点の右下
			attributeRectangle = new Rectangle2D.Double(polygon.getX() + (size / 2 / zoom), polygon.getY(),
					attributeWidth, attributeHeight);
			if (this.isPutable(point, usedPoints, attributeRectangle, visibleRectangle, search)) {
				polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(point);
			}
			// 点の左上
			attributeRectangle = new Rectangle2D.Double(polygon.getX() - attributeWidth - (size / 2 / zoom), polygon
					.getY()
					- attributeHeight, attributeWidth, attributeHeight);
			if (this.isPutable(point, usedPoints, attributeRectangle, visibleRectangle, search)) {
				polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(point);
			}
			// 点の左下
			attributeRectangle = new Rectangle2D.Double(polygon.getX() - attributeWidth - (size / 2 / zoom), polygon
					.getY(), attributeWidth, attributeHeight);
			if (this.isPutable(point, usedPoints, attributeRectangle, visibleRectangle, search)) {
				polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(point);
			}
		}
	}

	/**
	 * ポリゴンの属性位置を決定します。
	 * @param polygon ポリゴン
	 * @param usedPoints 既に属性が配置されたポリゴンの中心座標
	 * @param visibleRectangle 現在表示されている矩形
	 * @param zoom 倍率
	 * @param attributeHeight 属性の高さ
	 * @param metrics フォントメトリクス
	 * @param size 点の大きさ
	 * @param search 属性が決定されている領域を検索するためのオブジェクト
	 */
	private void fixPolygonAttributeLocation(final PolygonData polygon, final Collection<Point> usedPoints,
			final Rectangle2D visibleRectangle, final double zoom, final double attributeHeight,
			final FontMetrics metrics, final double size, final Search search) {
		if (polygon.getAttribute() != null) {
			polygon.setAttributeLocation(0, 0);
			final double attributeWidth = metrics.stringWidth(polygon.getAttribute()) / zoom;
			final Rectangle2D pointRectangle = new Rectangle2D.Double(polygon.getX() - (size / zoom), polygon.getY()
					- (size / zoom), (size * 2) / zoom, (size * 2) / zoom);
			Rectangle2D attributeRectangle;
			// ポリゴンの中心
			attributeRectangle = new Rectangle2D.Double(polygon.getX() - (attributeWidth / 2), polygon.getY()
					- (attributeHeight / 2), attributeWidth, attributeHeight);
			final Point point = new Point((int) polygon.getX(), (int) polygon.getY());
			if (this.isPutable(point, usedPoints, attributeRectangle, visibleRectangle, search)) {
				polygon.setAttributeLocation(attributeRectangle.getX(), attributeRectangle.getMaxY());
				search.insert(pointRectangle, null);
				search.insert(attributeRectangle, null);
				usedPoints.add(point);
			}
		}
	}

	/**
	 * @param point 点
	 * @param usedPoints 既に属性が配置されたポリゴンの中心座標
	 * @param attributeRectangle 属性の矩形
	 * @param visibleRectangle 現在表示されている矩形
	 * @param search 既に属性が配置された領域を検索するためのオブジェクト
	 * @return 属性を配置することができるかどうか
	 */
	private boolean isPutable(final Point point, final Collection<Point> usedPoints,
			final Rectangle2D attributeRectangle, final Rectangle2D visibleRectangle, final Search search) {
		return !usedPoints.contains(point) && visibleRectangle.contains(point)
				&& visibleRectangle.contains(attributeRectangle) && search.search(point).isEmpty()
				&& search.search(attributeRectangle).isEmpty();
	}

	/**
	 * @param attributeRectangle 属性の矩形
	 * @param point 点
	 * @param usedPoints 既に属性が配置されたポリゴンの中心座標
	 * @param visibleRectangle 現在表示されている矩形
	 * @param search 既に属性が配置された領域を検索するためのオブジェクト
	 * @return 点を置くことができるかどうか
	 */
	private boolean isPointPutable(final Rectangle2D attributeRectangle, final Point point,
			final Collection<Point> usedPoints, final Rectangle2D visibleRectangle, final Search search) {
		return !usedPoints.contains(point) && visibleRectangle.contains(attributeRectangle)
				&& search.search(attributeRectangle).isEmpty();
	}

	/**
	 * @param polygon ポリゴン
	 * @param point 点
	 * @param usedPoints 既に属性が配置されたポリゴンの中心座標
	 * @param attributeRectangle 属性の矩形
	 * @param visibleRectangle 現在表示されている矩形
	 * @param contains ポリゴンが属性を完全に含むかどうか
	 * @param search 属性が配置された領域を検索するためのオブジェクト
	 * @return 町丁目の属性を配置することができるかどうか
	 */
	private boolean isTyomePutable(final PolygonData polygon, final Point point, final Collection<Point> usedPoints,
			final Rectangle2D attributeRectangle, final Rectangle2D visibleRectangle, final boolean contains,
			final Search search) {
		if (contains) {
			return !usedPoints.contains(point) && visibleRectangle.contains(attributeRectangle)
					&& polygon.getArea().contains(attributeRectangle) && search.search(attributeRectangle).isEmpty();
		} else {
			return !usedPoints.contains(point) && visibleRectangle.contains(attributeRectangle)
					&& polygon.getArea().intersects(attributeRectangle) && search.search(attributeRectangle).isEmpty();
		}
	}
}
