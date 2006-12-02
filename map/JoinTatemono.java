package map;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性を基準にしてポリゴンを結合するクラスです。
 * @author zenjiro
 * 作成日: 2004/01/10
 */
class JoinTatemono {
    /**
     * 変更されたかどうか
     */
    private boolean isChanged;

    /** 属性を基準にしてポリゴンを見かけ上結合します。
     * このメソッドを呼び出した直後に isChanged() を呼び出すと、
     * このメソッドによって地図の状態が変化したかどうかが取得できます。
     * @param maps 地図
     * @throws IOException 
     */
    void joinTatemono(final Map<String, MapData> maps) throws IOException {
        this.isChanged = false;
        final Map<String, Collection<PolygonData>> attributePolygonMap = new ConcurrentHashMap<String, Collection<PolygonData>>(); // String -> Collection<Polygon> の Map
        // 属性をキー、ポリゴンの集合を値とする Map を初期化する
        synchronized (maps) {
			for (final MapData mapData : maps.values()) {
				if (mapData.hasTatemono()) {
					for (final PolygonData polygon : mapData.getTatemono().values()) {
						if (polygon.getAttribute() != null) {
							if (polygon.getAttribute().length() > 0) {
								if (!attributePolygonMap.containsKey(polygon.getAttribute())) {
									attributePolygonMap.put(polygon.getAttribute(),
										new ArrayList<PolygonData>());
								}
								attributePolygonMap.get(polygon.getAttribute()).add(polygon);
							}
						}
					}
				}
			}
		}
        for (final Collection<PolygonData> polygons : attributePolygonMap.values()) {
            if (polygons.size() > 1) {
                Rectangle2D rect = null;
                for (final PolygonData polygon : polygons) {
                    if (rect == null) {
                        rect = polygon.getArea().getBounds();
                    } else {
                        rect.add(polygon.getArea().getBounds());
                    }
                }
                for (final PolygonData polygon : polygons) {
                    polygon.setX(rect.getCenterX());
                    polygon.setY(rect.getCenterY());
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
