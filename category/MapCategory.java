package category;

/**
 * 1つの地図の種類が持つべきメソッドを集めたインターフェイスです。
 * @author zenjiro
 * @since 6.1.5
 */
public interface MapCategory {
	/**
	 * @param zoom 縮尺
	 * @return 地図を描画するかどうか
	 * @since 6.1.5
	 */
	public boolean isShow(final double zoom);
}
