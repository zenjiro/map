package category;

import map.Const;

/**
 * 国土数値情報の鉄道、道路の設定です。
 * @author zenjiro
 * @since 6.1.5
 */
public class RailwayKSJMapCategory implements MapCategory {

	public boolean isShow(double zoom) {
		return zoom >= Const.Zoom.LOAD_KSJ_RAILWAY && zoom < Const.Zoom.LOAD_ALL;
	}

}
