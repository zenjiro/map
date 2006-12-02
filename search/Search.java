package search;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * 2次元のポリゴンを矩形検索、点検索するためのインターフェイスです。
 * @author zenjiro
 * Created on 2004/12/13
 */
public interface Search {
    /**
     * オブジェクトを追加します。
     * @param key ポリゴン
     * @param value データ
     */
    public void insert(Shape key, String value);
    
    /**
     * 指定した点を含むポリゴンに対応するデータの一覧を取得します。
     * @param p 点
     * @return データの一覧
     */
    public Collection<String> search(Point2D p);
    
    /**
     * 指定した矩形と重なるポリゴンに対応するデータの一覧を取得します。
     * @param r 矩形
     * @return データの一覧
     */
    public Collection<String> search(Rectangle2D r);
    
    /**
     * 直前の検索における、ノードへのアクセス回数を取得します。
     * @return ノードへのアクセス回数
     */
    public int getNodeAccess();
    
    /**
     * 直前の検索における、ポリゴンへのアクセス回数を取得します。
     * @return ポリゴンへのアクセス回数
     */
    public int getShapeAccess();
    
    /**
     * ノードの数を取得します。
     * @return ノードの数
     */
    public int getNodeNumber();
    
    /**
     * ディスクの使用量を取得します。
     * @return ディスク使用量
     */
    public int getDiskUsage();
}
