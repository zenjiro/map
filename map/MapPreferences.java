package map;
import java.awt.Color;
import java.awt.Font;

/**
 * 地図の設定を扱うクラスのインターフェイスです。
 * @author zenjiro
 * 作成日: 2004/02/05
 */
public interface MapPreferences {
    /**
     * パネルの背景色を取得します。
     * @return 背景色
     */
    Color getBackGroundColor();

    /**
     * 主要地方道の設定を取得します。
     * 属性の描画色とフォントは無視されます。
     * @return 主要地方道の設定
     */
    Preferences getChihodoPreferences();

    /**
     * 市区町村界の設定を取得します。
     * 塗りつぶし色は無視されます。
     * @return 市区町村界の設定
     */
    Preferences getSi_tyoPreferences();

    /**
     * 高速道路の設定を取得します。
     * 属性の描画色とフォントは無視されます。
     * @return 高速道路の設定
     */
    Preferences getHighwayPreferences();

    /**
     * JR 在来線の設定を取得します。
     * 属性の描画色とフォントは無視されます。
     * @return JR 在来線の設定
     */
    Preferences getJRPreferences();

    /**
     * JR 新幹線の設定を取得します。
     * 属性の描画色とフォントは無視されます。
     * @return JR 新幹線の設定
     */
    Preferences getJRShinkansenPreferences();

    /**
     * 県道の設定を取得します。
     * 属性の描画色とフォントは無視されます。
     * @return 県道の設定
     */
    Preferences getKendoPreferences();

    /**
     * 国道の設定を取得します。
     * 属性の描画色とフォントは無視されます。
     * @return 国道の設定
     */
    Preferences getKokudoPreferences();

    /**
     * 名前のある道路の設定を取得します。
     * 属性の描画色とフォントは無視されます。
     * @return 名前のある道路の設定
     */
    Preferences getMajorRoadPreferences();

    /**
     * 情報が無いときに表示する長方形の色を取得します。
     * @return 情報が無いときに表示する長方形の色
     */
    Color getMapBoundsColor();

    /**
     * 通常の道路の設定を取得します。
     * @return 通常の道路の設定
     */
    Preferences getNormalRoadPreferences();

    /**
     * 公園の設定を取得します。
     * @return 公園の設定
     */
    Preferences getParkPreferences();

    /**
     * JR 以外の鉄道の設定を取得します。
     * @return JR 以外の鉄道の設定
     */
    Preferences getRailwayPreferences();

    /**
     * 建物の設定を取得します。
     * @return 建物の設定
     */
    Preferences getTatemonoPreferences();

    /**
     * 丁目界の設定を取得します。
     * 塗りつぶし色は無視されます。
     * フォントは読みに使われます。
     * @return 丁目界の設定
     */
    Preferences getTyomePreferences();

    /**
     * 丁目を塗り分ける色を取得します。
     * @param index 何色目か
     * @return 色
     */
    Color getTyomeFillColor(int index);

    /**
     * 海や川、内水面の設定を取得します。
     * @return 水の設定
     */
    Preferences getMizuPreferences();

    /**
     * 駅の設定を取得します。
     * 線の幅は点の直径を意味します。
     * @return 駅の設定
     */
    Preferences getEkiPreferences();

    /**
     * 公園以外の場地の設定を取得します。
     * @return 場地の設定
     */
    Preferences getZyoutiPreferences();

    /**
     * @return 市区町村界の設定
     */
    Preferences getCityPreferences();
    
    /**
     * @return 都道府県界の設定
     */
    Preferences getPrefecturePreferences();

    /**
     * @return 街区レベル位置参照情報の設定
     */
    Preferences getIsjPreferences();
    
    /**
     * @return 国土数値情報の鉄道データのうち、JRの設定
     */
    Preferences getKsjRailwayJRPreferences();
    
    /**
     * @return 国土数値情報の鉄道データのうち、JR以外の設定
     */
    Preferences getKsjRailwayPreferences();
    
    /**
     * @return 国土数値情報の鉄道データのうち、駅の設定
     */
    Preferences getKsjRailwayStationPreferences();
    
    /**
     * 弧とポリゴンの設定を扱うクラスです。
     * @author zenjiro
     */
    class Preferences {
        /**
         * 属性の描画色
         */
        private Color attributeColor;

        /**
         * 境界色
         */
        private Color borderColor;

        /**
         * 塗りつぶし色
         */
        private Color fillColor;

        /**
         * フォント
         */
        private Font font;

        /**
         * 線の幅
         */
        private double width;

        /**
         * 指定した塗りつぶし色、境界色、線の幅、属性の描画色、フォントを持った設定を初期化します。
         * @param fillColor 塗りつぶし色
         * @param borderColor 境界色
         * @param width 線の幅
         * @param attributeColor 属性の描画色
         * @param font フォント
         */
        public Preferences(final Color fillColor, final Color borderColor, final double width,
            final Color attributeColor, final Font font) {
            setFillColor(fillColor);
            setBorderColor(borderColor);
            setWidth(width);
            setAttributeColor(attributeColor);
            setFont(font);
        }

        /**
         * 属性の描画色を取得します。
         * @return 属性の描画色
         */
        public Color getAttributeColor() {
            return this.attributeColor;
        }

        /**
         * 境界色を取得します。
         * @return 境界色
         */
        public Color getBorderColor() {
            return this.borderColor;
        }

        /**
         * 塗りつぶし色を取得します。
         * @return 塗りつぶし色
         */
        public Color getFillColor() {
            return this.fillColor;
        }

        /**
         * フォントを取得します。
         * @return フォント
         */
        public Font getFont() {
            return this.font;
        }

        /**
         * 線の幅を取得します。
         * @return 線の幅
         */
        public float getWidth() {
            return (float) this.width;
        }

        /**
         * 属性の描画色を設定します。
         * @param color 属性の描画色
         */
        public void setAttributeColor(final Color color) {
            this.attributeColor = color;
        }

        /**
         * 境界色を設定します。
         * @param color 境界色
         */
        public void setBorderColor(final Color color) {
            this.borderColor = color;
        }

        /**
         * 塗りつぶし色を設定します。
         * @param color 塗りつぶし色
         */
        public void setFillColor(final Color color) {
            this.fillColor = color;
        }

        /**
         * フォントを設定します。
         * @param font フォント
         */
        public void setFont(final Font font) {
            this.font = font;
        }

        /**
         * 線の幅を設定します。
         * @param d 線の幅
         */
        public void setWidth(final double d) {
            this.width = d;
        }
    }
	/**
	 * 文字の大きさを変更します。
	 * @param zoom 文字の大きさ
	 */
	public void setFontZoom(final double zoom);
	
}
