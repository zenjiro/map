package map;

import java.awt.Color;
import java.awt.Font;

import category.CityKSJMapCategory;
import category.DetailSDF2500MapCategory;
import category.ISJMapCategory;
import category.KSJMapCategory;
import category.MapCategory;
import category.RailwayKSJMapCategory;
import category.SDF2500MapCategory;
import category.YomiMapCateogry;

import map.Const.Fonts;

/**
 * デフォルトの地図の設定を扱うクラスです。
 * @author zenjiro
 * 作成日：2004年1月7日
 */
public class DefaultMapPreferences implements MapPreferences {
	/**
	 * 市区町村界の設定
	 */
	private Preferences cityPreferences = new Preferences(null, Color.BLACK, 0.1, Color.BLACK, new Font(Fonts.GOTHIC,
			Font.PLAIN, 20));

	/**
	 * 都道府県界の設定
	 */
	private Preferences prefecturePreferences = new Preferences(null, Color.BLACK, 0.5, Color.BLACK, new Font(
			Fonts.GOTHIC, Font.PLAIN, 30));

	/**
	 * 高速道路の設定
	 */
	private final Preferences highwayPreferences = new Preferences(new Color(160, 220, 160), new Color(160, 220, 160)
			.darker(), 19, null, null);

	/**
	 * 国道の設定
	 */
	private final Preferences kokudoPreferences = new Preferences(new Color(250, 180, 180), new Color(250, 180, 180)
			.darker(), 15, null, null);

	/**
	 * 県道の設定
	 */
	private final Preferences kendoPreferences = new Preferences(new Color(255, 255, 180), new Color(255, 255, 180)
			.darker(), 9, null, null);

	/**
	 * 主要地方道の設定
	 */
	private final Preferences chihodoPreferences = new Preferences(new Color(255, 255, 180), new Color(255, 255, 180)
			.darker(), 15, null, null);

	/**
	 * 名前のある道路の設定
	 */
	private final Preferences majorRoadPreferences = new Preferences(Color.WHITE, Color.GRAY, 15, null, null);

	/**
	 * 生活道路の設定
	 */
	private Preferences normalRoadPreferences = new Preferences(Color.WHITE, Color.LIGHT_GRAY, 7, // GRAY
			Color.BLUE, new Font(Fonts.GOTHIC, Font.PLAIN, 15));

	/**
	 * JRの設定
	 */
	private final Preferences jrPreferences = new Preferences(Color.WHITE, Color.GRAY, 6, null, null);

	/**
	 * JR新幹線の設定
	 */
	private final Preferences jrShinkansenPreferences = new Preferences(Color.WHITE, Color.GRAY, 6, null, null);

	/**
	 * 私鉄の設定
	 */
	private Preferences railwayPreferences = new Preferences(Color.WHITE, Color.GRAY, 4, Color.BLUE, new Font(
			Fonts.GOTHIC, Font.PLAIN, 16));

	/**
	 * 内水面の設定
	 */
	private Preferences mizuPreferences = new Preferences(new Color(200, 210, 250), new Color(200, 210, 250).darker(),
			1, Color.BLACK, new Font(Fonts.MINCHO, Font.PLAIN, 14));

	/**
	 * 城地の設定
	 */
	private Preferences zyoutiPreferences = new Preferences(new Color(220, 220, 220),
			new Color(220, 220, 220).darker(), 1, Color.BLACK, new Font(Fonts.GOTHIC, Font.PLAIN, 15));

	/**
	 * 公園の設定
	 */
	private Preferences parkPreferences = new Preferences(new Color(200, 250, 160), new Color(200, 250, 160).darker(),
			1, Color.BLACK, new Font(Fonts.GOTHIC, Font.PLAIN, 15));

	/**
	 * 公共建物の設定
	 */
	private Preferences tatemonoPreferences = new Preferences(new Color(180, 180, 180), new Color(180, 180, 180)
			.darker(), 1, Color.BLACK, new Font(Fonts.GOTHIC, Font.PLAIN, 12));

	/**
	 * 駅の設定
	 */
	private Preferences ekiPreferences = new Preferences(Color.RED, Color.BLACK, 8, Color.BLACK, new Font(Fonts.GOTHIC,
			Font.PLAIN, 20));

	/**
	 * 丁目の設定
	 */
	private Preferences tyomePreferences = new Preferences(null, Color.BLACK, 1, new Color(0, 0, // old BLACK
			160), new Font(Fonts.GOTHIC, Font.PLAIN, 9));

	/**
	 * 市区町村の設定
	 */
	private Preferences si_tyoPreferences = new Preferences(new Color(250, 250, 230), Color.BLACK, 4, new Color(0, 0,
			160), new Font(Fonts.GOTHIC, Font.PLAIN, 40));

	/**
	 * 街区レベル位置参照情報の設定
	 */
	private Preferences isjPreferences = new Preferences(null, null, 0, new Color(180, 180, 180).darker(), new Font(
			Fonts.GOTHIC, Font.PLAIN, 10));

	/**
	 * 国土数値情報の鉄道データのうち、JRの設定
	 */
	private Preferences ksjRailwayJRPreferences = new Preferences(Color.WHITE, Color.GRAY, 1.5f, null, null);

	/**
	 * 国土数値情報の鉄道データのうち、JR以外の設定
	 */
	private Preferences ksjRailwayPreferences = new Preferences(Color.WHITE, Color.GRAY, 1, Color.BLUE, new Font(
			Fonts.GOTHIC, Font.PLAIN, 13));

	/**
	 * 国土数値情報の鉄道データのうち、駅の設定
	 */
	private Preferences ksjRailwayStationPreferences = new Preferences(Color.RED, Color.GRAY, 3, Color.BLACK, new Font(
			Fonts.GOTHIC, Font.PLAIN, 15));

	/**
	 * 国土数値情報の道路データのうち、高速道路の設定
	 */
	private Preferences ksjRoadHighawyPreferences = new Preferences(this.highwayPreferences.getFillColor(),
			this.highwayPreferences.getBorderColor(), 2f, null, null);

	/**
	 * 国土数値情報の道路データのうち、国道の設定
	 */
	private Preferences ksjRoadKokudoPreferences = new Preferences(this.kokudoPreferences.getFillColor(),
			this.kokudoPreferences.getBorderColor(), 1.5f, null, null);

	/**
	 * 国土数値情報の道路データのうち、主要道路の設定
	 */
	private Preferences ksjRoadMajorPreferences = new Preferences(this.chihodoPreferences.getFillColor(),
			this.chihodoPreferences.getBorderColor(), 1.5f, Color.BLUE.darker(), new Font(Fonts.GOTHIC, Font.PLAIN, 15));

	/**
	 * ルートの設定
	 */
	private Preferences routePreferences = new Preferences(new Color(140, 140, 220), new Color(100, 100, 180), 2,
			Color.BLUE, new Font(Fonts.GOTHIC, Font.BOLD, 15));

	public Color getBackGroundColor() {
		return Color.WHITE;
	}

	public Preferences getChihodoPreferences() {
		return this.chihodoPreferences;
	}

	public Preferences getCityPreferences() {
		return this.cityPreferences;
	}

	public Preferences getEkiPreferences() {
		return this.ekiPreferences;
	}

	public Preferences getHighwayPreferences() {
		return this.highwayPreferences;
	}

	public Preferences getIsjPreferences() {
		return this.isjPreferences;
	}

	public Preferences getJRPreferences() {
		return this.jrPreferences;
	}

	public Preferences getJRShinkansenPreferences() {
		return this.jrShinkansenPreferences;
	}

	public Preferences getKendoPreferences() {
		return this.kendoPreferences;
	}

	public Preferences getKokudoPreferences() {
		return this.kokudoPreferences;
	}

	public Preferences getKsjRailwayJRPreferences() {
		return this.ksjRailwayJRPreferences;
	}

	public Preferences getKsjRailwayPreferences() {
		return this.ksjRailwayPreferences;
	}

	public Preferences getKsjRailwayStationPreferences() {
		return this.ksjRailwayStationPreferences;
	}

	/**
	 * 国土数値情報の道路データのうち、高速道路の設定を取得します。
	 */
	public Preferences getKsjRoadHighwayPreferences() {
		return this.ksjRoadHighawyPreferences;
	}

	/**
	 * 国土数値情報の道路データのうち、国道の設定を取得します。
	 */
	public Preferences getKsjRoadKokudoPreferences() {
		return this.ksjRoadKokudoPreferences;
	}

	/**
	 * 国土数値情報の道路データのうち、主要な道路の設定を取得します。
	 */
	public Preferences getKsjRoadMajorPreferences() {
		return this.ksjRoadMajorPreferences;
	}

	public Preferences getMajorRoadPreferences() {
		return this.majorRoadPreferences;
	}

	public Color getMapBoundsColor() {
		return Color.BLACK;
	}

	public Preferences getMizuPreferences() {
		return this.mizuPreferences;
	}

	public Preferences getNormalRoadPreferences() {
		return this.normalRoadPreferences;
	}

	public Preferences getParkPreferences() {
		return this.parkPreferences;
	}

	public Preferences getPrefecturePreferences() {
		return this.prefecturePreferences;
	}

	public Preferences getRailwayPreferences() {
		return this.railwayPreferences;
	}

	public Preferences getSi_tyoPreferences() {
		return this.si_tyoPreferences;
	}

	public Preferences getTatemonoPreferences() {
		return this.tatemonoPreferences;
	}

	/**
	 * 丁目を塗り分ける色を取得します。
	 * @param index 何色目か
	 * @return 色
	 */
	public Color getTyomeFillColor(final int index) {
		if (index == 0) {
			return Color.WHITE; // 白
		} else if (index == 1) {
			return new Color(250, 250, 230); // 黄色
		} else if (index == 2) {
			return new Color(230, 250, 250); // 水色
		} else if (index == 3) {
			return new Color(240, 250, 230); // 緑色
		} else if (index == 4) {
			return new Color(230, 230, 250); // 青色
		} else if (index == 5) {
			return new Color(250, 230, 255); // 赤紫色
		} else if (index == 6) {
			return new Color(250, 230, 230); // サーモンピンク色
		} else if (index == 7) {
			return new Color(255, (255 + 230) / 2, 230); // 橙色
		} else {
			return new Color(220, 240, 220); // 暗い緑色
		}
	}

	public Preferences getTyomePreferences() {
		return this.tyomePreferences;
	}

	public Preferences getZyoutiPreferences() {
		return this.zyoutiPreferences;
	}

	/**
	 * 文字の大きさを変更します。
	 * @param zoom 文字の大きさ
	 */
	public void setFontZoom(final double zoom) {
		this.normalRoadPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (15 * zoom)));
		this.railwayPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (16 * zoom)));
		this.mizuPreferences.setFont(new Font(Fonts.MINCHO, Font.PLAIN, (int) (14 * zoom)));
		this.zyoutiPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (15 * zoom)));
		this.parkPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (15 * zoom)));
		this.tatemonoPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (12 * zoom)));
		this.ekiPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (20 * zoom)));
		this.si_tyoPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (40 * zoom)));
		this.cityPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (20 * zoom)));
		this.prefecturePreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (30 * zoom)));
		this.isjPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (10 * zoom)));
		this.tyomePreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (9 * zoom)));
		this.ksjRailwayPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (13 * zoom)));
		this.ksjRailwayStationPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (15 * zoom)));
		this.ksjRoadMajorPreferences.setFont(new Font(Fonts.GOTHIC, Font.PLAIN, (int) (15 * zoom)));
		this.routePreferences.setFont(new Font(Fonts.GOTHIC, Font.BOLD, (int) (15 * zoom)));
	}

	public Preferences getRoutePreferences() {
		return this.routePreferences;
	}

	public float getRoutePointSize() {
		return 10;
	}

	/**
	 * 街区レベル位置参照情報の設定
	 */
	private final MapCategory isjMapCategory = new ISJMapCategory();
	
	/**
	 * 国土数値情報の設定
	 */
	private final MapCategory ksjMapCategory = new KSJMapCategory();
	
	/**
	 * 数値地図2500（空間データ基盤）の設定
	 */
	private final MapCategory sdf2500MapCategory = new SDF2500MapCategory();
	
	/**
	 * 読みの設定
	 */
	private final MapCategory yomiMapCategory = new YomiMapCateogry();
	
	/**
	 * 詳細な数値地図2500（空間データ基盤）の設定
	 */
	private final MapCategory detailSDF2500MapCategory = new DetailSDF2500MapCategory();
	
	/**
	 * 国土数値情報の市区町村の設定
	 */
	private final MapCategory cityKSJMapCategory = new CityKSJMapCategory();
	
	/**
	 * 国土数値情報の鉄道の設定
	 */
	private final MapCategory railwayKSJMapCategory = new RailwayKSJMapCategory();
	
	public MapCategory getISJMapCategory() {
		return this.isjMapCategory;
	}

	public MapCategory getKSJMapCategory() {
		return this.ksjMapCategory;
	}

	public MapCategory getSDF2500MapCateogry() {
		return this.sdf2500MapCategory;
	}

	public MapCategory getYomiMapCategory() {
		return this.yomiMapCategory;
	}

	public MapCategory getCityKSJMapCategory() {
		return this.cityKSJMapCategory;
	}

	public MapCategory getDetailSDF2500MapCategory() {
		return this.detailSDF2500MapCategory;
	}

	public MapCategory getRailwayKSJMapCateogry() {
		return this.railwayKSJMapCategory;
	}

}
