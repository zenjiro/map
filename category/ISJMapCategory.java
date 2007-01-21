package category;

import map.Const;

/**
 * 街区レベル位置参照情報の設定です。
 * @author zenjiro
 * @since 6.1.5
 */
public class ISJMapCategory implements MapCategory {
	public boolean isShow(final double zoom) {
		return zoom >= Const.Zoom.LOAD_ALL;
	}
}
