package category;

import map.Const;

/**
 * 詳細な数値地図2500（空間データ基盤）の設定です。
 * @author zenjiro
 * @since 6.1.5
 */
public class DetailSDF2500MapCategory implements MapCategory {
	public boolean isShow(final double zoom) {
		return zoom >= Const.Zoom.LOAD_ALL;
	}
}
