package category;

/**
 * 国土数値情報の設定です。
 * @author zenjiro
 * @since 6.1.5
 */
public class KSJMapCategory implements MapCategory {
	public boolean isShow(final double zoom) {
		return true;
	}
}
