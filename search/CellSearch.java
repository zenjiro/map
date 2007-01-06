package search;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * cell methodによる検索を行うクラスです。
 * @author zenjiro
 * Created on 2005/02/08 21:42:46
 */
public class CellSearch implements Search {
    /**
     * オブジェクトとラベルをカプセル化するクラスです。
     * @author zenjiro
     * Created on 2005/02/08 22:01:09
     */
    private class Entry {
        /**
         * オブジェクト
         */
        final Shape shape;

        /**
         * ラベル
         */
        final String label;

        /**
         * オブジェクトとラベルをカプセル化するクラスを初期化します。
         * @param shape オブジェクト
         * @param label ラベル
         */
        Entry(final Shape shape, final String label) {
            this.shape = shape;
            this.label = label;
        }

        @Override
		public String toString() {
            return this.label;
        }
    }

    /**
     * セルの幅
     */
    private final double width;

    /**
     * セルの高さ
     */
    private final double height;

    /**
     * データ構造
     */
    private final Map<Point, ArrayList<Entry>> data;

    /**
     * ノードへのアクセス回数
     */
    private int nodeAccess;

    /**
     * データへのアクセス回数
     */
    private int shapeAccess;

    /**
     * cell methodによる検索を行うクラスを初期化します。
     * @param width
     * @param height
     */
    public CellSearch(final double width, final double height) {
        this.width = width;
        this.height = height;
        this.data = new ConcurrentHashMap<Point, ArrayList<Entry>>();
    }

    public void insert(final Shape shape, final String label) {
        final Rectangle2D bounds = shape.getBounds2D();
        final int x1 = (int) Math.round(bounds.getMinX() / this.width);
        final int x2 = (int) Math.round(bounds.getMaxX() / this.width);
        final int y1 = (int) Math.round(bounds.getMinY() / this.height);
        final int y2 = (int) Math.round(bounds.getMaxY() / this.height);
        final Entry entry = new Entry(shape, label);
        for (int y = y1; y <= y2; ++y) {
            for (int x = x1; x <= x2; ++x) {
                final Rectangle2D cell = new Rectangle2D.Double((x - 0.5) * this.width, (y - 0.5)
                        * this.height, this.width, this.height);
                if (shape.intersects(cell)) {
                    final Point key = new Point(x, y);
                    if (this.data.containsKey(key)) {
                        final ArrayList<Entry> list = this.data.get(key);
                        list.add(entry);
                    } else {
                        final ArrayList<Entry> list = new ArrayList<Entry>();
                        list.add(entry);
                        this.data.put(key, list);
                    }
                }
            }
        }
    }

    public Set<String> search(final Point2D query) {
        final Set<String> ret = new HashSet<String>();
        this.nodeAccess = 1;
        this.shapeAccess = 0;
        final int x = (int) Math.round(query.getX() / this.width);
        final int y = (int) Math.round(query.getY() / this.height);
        final Point key = new Point(x, y);
        if (this.data.containsKey(key)) {
            final ArrayList<Entry> list = this.data.get(key);
            for (final Entry entry : list) {
                ++this.nodeAccess;
                if (entry.shape.getBounds2D().contains(query)) {
                    ++this.shapeAccess;
                    if (entry.shape.contains(query)) {
                        ret.add(entry.label);
                    }
                }
            }
        }
        return ret;
    }

    public Set<String> search(final Rectangle2D query) {
        final Set<String> ret = new HashSet<String>();
        this.nodeAccess = 1;
        this.shapeAccess = 1;
        final int x1 = (int) Math.round(query.getMinX() / this.width);
        final int x2 = (int) Math.round(query.getMaxX() / this.width);
        final int y1 = (int) Math.round(query.getMinY() / this.height);
        final int y2 = (int) Math.round(query.getMaxY() / this.height);
        for (int y = y1; y <= y2; ++y) {
            for (int x = x1; x <= x2; ++x) {
                final Point key = new Point(x, y);
                if (this.data.containsKey(key)) {
                    final ArrayList<Entry> list = this.data.get(key);
                    for (final Entry entry : list) {
                        ++this.nodeAccess;
                        if (!ret.contains(entry.label)) {
                            if (entry.shape.getBounds2D().intersects(query)) {
                                ++this.shapeAccess;
                                if (entry.shape.intersects(query)) {
                                    ret.add(entry.label);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public int getNodeAccess() {
        return this.nodeAccess;
    }

    public int getShapeAccess() {
        return this.shapeAccess;
    }

    public int getNodeNumber() {
        throw new UnsupportedOperationException("ノード数は未実装です。");
    }

    public int getDiskUsage() {
        throw new UnsupportedOperationException("ディスク使用量は未実装です。");
    }
}
