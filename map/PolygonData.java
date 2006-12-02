package map;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Area;

/**
 * ポリゴンを表すクラスです。
 * @author zenjiro
 * 作成日: 2003/12/11
 */
class PolygonData {
    /**
     * 公共建物の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_BUILDING = 3500; // 公共建物

    /**
     * 墓地の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_GRAVEYARD = 6215; // 墓地

    /**
     * 湖池等の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_LAKE = 6105; // 湖池等

    /**
     * その他の場地の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_OTHER = 6200; // その他の場地

    /**
     * 都市公園の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_PARK = 6242; // 都市公園

    /**
     * 鉄道敷の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_RAILROAD = 6241; // 鉄道敷

    /**
     * 河川の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_RIVER = 6201; // 河川

    /**
     * 学校敷地の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_SCHOOL = 6243; // 学校敷地

    /**
     * 市区町村の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_SI_TYO = 1105; // 市区町村

    /**
     * 神社、寺院境内の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_TEMPLE = 6244; // 神社・寺院境内

    /**
     * 大字、町丁目の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_TYOME = 1106; // 大字・町丁目

    /**
     * 不明な図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_UNKNOWN = 0; // 不明な図式分類コード

    /**
     * 美術館の公共建物コードを表す定数です。
     */
    public static int TATEMONO_ART_MUSEUM = 3529; // 美術館

    /**
     * 消防署の公共建物コードを表す定数です。
     */
    public static int TATEMONO_FIRE_STATION = 3516; // 消防署

    /**
     * 体育施設の公共建物コードを表す定数です。
     */
    public static int TATEMONO_GYM = 3530; // 体育施設

    /**
     * 病院
     */
    public static int TATEMONO_HOSPITAL = 3532; // 病院

    /**
     * 幼稚園、保育園の公共建物コードを表す定数です。
     */
    public static int TATEMONO_KINDERGARTEN = 3525; // 幼稚園・保育園

    /**
     * 図書館の公共建物コードを表す定数です。
     */
    public static int TATEMONO_LIBRARY = 3528; // 図書館

    /**
     * その他の官公署の公共建物コードを表す定数です。
     */
    public static int TATEMONO_OTHER = 3503; // その他の官公署

    /**
     * 警察官駐在所、派出所の公共建物コードを表す定数です。
     */
    public static int TATEMONO_POLICE_BOX = 3515; // 警察官駐在所・派出所

    /**
     * 警察署の公共建物コードを表す定数です。
     */
    public static int TATEMONO_POLICE_STATION = 3514; // 警察署

    /**
     * 郵便局の公共建物コードを表す定数です。
     */
    public static int TATEMONO_POST_OFFICE = 3509; // 郵便局

    /**
     * 公会堂、公民館の公共建物コードを表す定数です。
     */
    public static int TATEMONO_PUBLIC_HALL = 3526; // 公会堂・公民館

    /**
     * 保健所の公共建物コードを表す定数です。
     */
    public static int TATEMONO_PUBLIC_HEALTH_CENTER = 3531; // 保健所

    /**
     * 市、特別区、町、村、指定としの区の役場、支所及び出張所の公共建物コードを表す定数です。
     */
    public static int TATEMONO_PUBLIC_OFFICE = 3519; // 市・特別区・町・村・指定都市の区の役場、支所および出張所

    /**
     * 揚排水ポンプ場の公共建物コードを表す定数です。
     */
    public static int TATEMONO_PUMP = 3556; // 揚排水ポンプ場

    /**
     * 学校の公共建物コードを表す定数です。
     */
    public static int TATEMONO_SCHOOL = 3524; // 学校

    /**
     * 博物館の公共建物コードを表す定数です。
     */
    public static int TATEMONO_SIENCE_MUSEUM = 3527; // 博物館

    /**
     * 駅舎の公共建物コードを表す定数です。
     */
    public static int TATEMONO_STATION = 3543; // 駅舎

    /**
     * 不明な公共建物コードを表す定数です。
     */
    public static int TATEMONO_UNKNOWN = 0; // 不明な公共建物コード
    /**
     * 表す領域（図郭にまたがるポリゴンを結合したもの）
     */
    private Area area;
    /**
     * 属性
     */
    private String attribute;
    /**
     * 属性を描画するx座標
     */
    private double attributeX; // 属性を描画する x 座標
    /**
     * 属性を描画するy座標
     */
    private double attributeY; // 属性を描画する y 座標
    /**
     * 図式分類コード
     */
    private int classificationCode; // 図式分類コード
    /**
     * 塗りつぶし色
     */
    private Color fillColor; // 塗りつぶし色
    /**
     * 丁目を塗り分けるときに何色目を使うか
     */
    private int tyomeColorIndex; // 丁目を塗り分けるときに何色目を使うか
    /**
     * 個別番号
     */
    private String polygonName; // 個別番号
    /**
     * 公共建物コード
     */
    private int tatemonoCode; // 公共建物コード
    /**
     * 丁目のフォント
     */
    private Font tyomeFont; // 丁目のフォント
    /**
     * 代表点のx座標
     */
    private double x; // 代表点の x 座標
    /**
     * 代表点のy座標
     */
    private double y; // 代表点の y 座標

    /**
     * 読み
     */
    private String yomi;
    
    /**
     * 読みを描画する座標の左端のx座標
     */
    private double yomiX;
    
    /**
     * 読みを描画する座標の下端のy座標
     */
    private double yomiY;

    /**
     * @return 読みがあるかどうか
     */
    public boolean hasYomi() {
    	return this.yomi != null;
    }
    
    /**
     * @return 読み
     */
    public String getYomi() {
    	return this.yomi;
    }
    
    /**
     * 読みを設定します。
     * @param yomi 読み
     */
    public void setYomi(final String yomi) {
    	this.yomi = yomi;
    }

    /**
     * 読みを描画する位置を設定します。
     * @param x x座標
     * @param y y座標
     */
    public void setYomiLocation(final double x, final double y) {
    	this.yomiX = x;
    	this.yomiY = y;
    }

    /**
     * @return 読みを描画する左端のx座標
     */
    public double getYomiX() {
    	return this.yomiX;
    }
    
    /**
     * @return 読みを描画する下端のy座標
     */
    public double getYomiY() {
    	return this.yomiY;
    }
    
    /**
     * ポリゴンを初期化します。
     * @param polygonName ポリゴンの個別番号
     * @param area ポリゴンが表す領域
     * @param type ポリゴンの種類
     * @param x 代表点の x 座標
     * @param y 代表点の y 座標
     */
    PolygonData(final String polygonName, final Area area, final int type, final double x, final double y) {
        this.polygonName = polygonName;
        this.area = area;
        this.classificationCode = type;
        this.x = x;
        this.y = y;
    }

    /**
     * 丁目を塗り分けるときに何色目を使うかを設定します。
         * @param index 何色目を使うか
         */
    public void setTyomeColorIndex(final int index) {
        this.tyomeColorIndex = index;
    }

    /**
     * 丁目を塗り分けるときに何色目を使うかを取得します。
         * @return 何色目を使うか
         */
    public int getTyomeColorIndex() {
        return this.tyomeColorIndex;
    }

    /**
     * このポリゴンが表す領域を取得します。
     * @return 領域
     */
    Area getArea() {
        return this.area;
    }

    /** 属性を取得します。
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

    /** 図式分類コードを取得します。
     * @return 図式分類コード
     */
    int getClassificationCode() {
        return this.classificationCode;
    }

    /** 塗りつぶし色を取得します。
     * @return 塗りつぶし色
     */
    public Color getFillColor() {
        return this.fillColor;
    }

    /** 個別番号を取得します。
     * @return 個別番号
     */
    String getPolygonName() {
        return this.polygonName;
    }

    /** 公共建物コードを取得します。
     * @return 公共建物コード
     */
    public int getTatemonoCode() {
        return this.tatemonoCode;
    }

    /** 丁目のフォントを取得します。
     * @return 丁目のフォント
     */
    Font getTyomeFont() {
        return this.tyomeFont;
    }

    /** 代表点の x 座標を取得します。
     * @return 代表点の x 座標
     */
    double getX() {
        return this.x;
    }

    /** 代表点の y 座標を取得します。
     * @return 代表点の y 座標
     */
    double getY() {
        return this.y;
    }

    /**
     * このポリゴンが表す領域を設定します。
     * 複数の図葉ファイルに含まれるポリゴンのばあい、それらを結合した領域を設定します。
     * @param area 領域
     */
    void setArea(final Area area) {
        this.area = area;
    }

    /** 属性を設定します。
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

    /** 塗りつぶし色を設定します。
     * @param color 塗りつぶし色
     */
    public void setFillColor(final Color color) {
        this.fillColor = color;
    }

    /** 公共建物コードを設定します。
     * @param i 公共建物コード
     */
    public void setTatemonoCode(final int i) {
        this.tatemonoCode = i;
    }

    /** 丁目のフォントを設定します。
     * @param font 丁目のフォント
     */
    void setTyomeFont(final Font font) {
        this.tyomeFont = font;
    }

    /** 代表点の x 座標を設定します。
     * @param d 代表点の x 座標
     */
    public void setX(final double d) {
        this.x = d;
    }

    /** 代表点の y 座標を設定します。
     * @param d 代表点の y 座標
     */
    public void setY(final double d) {
        this.y = d;
    }

    @Override
	public String toString() {
        return "[Polygon " + this.polygonName + "(" + this.attribute + ")]";
    }
}
