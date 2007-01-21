package category;

import map.Const;

/**
 * 国土数値情報の市区町村の設定です。
 * @author zenjiro
 * @since 6.1.5
 */
public class CityKSJMapCategory implements MapCategory {
	public boolean isShow(final double zoom) {
		return zoom >= Const.Zoom.LOAD_KSJ_CITIES;
	}
}
