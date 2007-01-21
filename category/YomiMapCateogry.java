package category;

import map.Const;

/**
 * 住所の読みの設定です。
 * @author zenjiro
 * @since 6.1.5
 */
public class YomiMapCateogry implements MapCategory {
	public boolean isShow(final double zoom) {
		return zoom >= Const.Zoom.LOAD_GYOUSEI;
	}
}
