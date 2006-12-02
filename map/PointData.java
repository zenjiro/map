package map;
/**
 * 点データを表すクラスです。
 * @author zenjiro
 * 作成日: 2004/01/22
 */
class PointData {
    /**
     * 三角点の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_DATUMS = 7301; // 三角点

    /**
     * 駅の図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_STATION = 2420; // 駅

    /**
     * 不明な図式分類コードを表す定数です。
     */
    public static int CLASSIFICATION_UNKNOWN = 0; // 不明
    /**
     * 属性
     */
    private String attribute; // 属性
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
     * 個別番号
     */
    private String pointName; // 個別番号
    /**
     * x座標
     */
    private double x; // x 座標
    /**
     * y座標
     */
    private double y; // y 座標

    /**
     * 点を初期化します。
     * @param name 個別番号
     * @param code 図式分類コード
     * @param x x 座標
     * @param y y 座標
     */
    PointData(final String name, final int code, final double x, final double y) {
        this.pointName = name;
        this.classificationCode = code;
        this.x = x;
        this.y = y;
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
    int getClassificationCode() {
        return this.classificationCode;
    }

    /**
     * 個別番号を取得します。
     * @return 個別番号
     */
    String getPointName() {
        return this.pointName;
    }

    /**
     * x 座標を取得します。
     * @return x 座標
     */
    double getX() {
        return this.x;
    }

    /**
     * y 座標を取得します。
     * @return y 座標
     */
    double getY() {
        return this.y;
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

    @Override
	public String toString() {
        return "[Point " + this.pointName + "(" + this.attribute + ")]";
    }
}
