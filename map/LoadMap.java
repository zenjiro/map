package map;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import map.Const.Zoom;

/**
 * 地図を読み込むクラスです。
 * @author zenjiro
 * 作成日: 2004/01/09
 */
public class LoadMap {
	/**
	 * 変更されたかどうか
	 */
	private boolean isChanged;

	/**
	 * 道路が変更されたかどうか
	 */
	private boolean isRoadChanged;

	/**
	 * @return 道路が変更されたかどうか
	 */
	public boolean isRoadChanged() {
		return this.isRoadChanged;
	}
	
	/** 
	 * 地図を読み込む必要があれば読み込み、開放する必要があれば開放します。
	 * このメソッドを呼び出した直後に isChanged() を呼び出すと、
	 * このメソッドによって地図の状態が変化したかどうかが取得できます。
	 * @param maps 地図
	 * @param panel パネル
	 * @param visibleRectangle 表示されている領域（仮想座標）
	 * @throws IOException 
	 */
	public void loadMap(final Map<String, MapData> maps, final MapPanel panel, final Rectangle2D visibleRectangle) throws IOException {
		final double zoom = panel.getZoom();
		this.isChanged = false;
		this.isRoadChanged = false;
		for (final MapData mapData : maps.values()) {
			final Rectangle2D preLoadRectangle = new Rectangle2D.Double(visibleRectangle.getX()
					- (visibleRectangle.getWidth() * Const.PRE_LOAD_COEFFICIENT), visibleRectangle.getY()
					- (visibleRectangle.getHeight() * Const.PRE_LOAD_COEFFICIENT), visibleRectangle.getWidth()
					+ (visibleRectangle.getWidth() * Const.PRE_LOAD_COEFFICIENT * 2), visibleRectangle.getHeight()
					+ (visibleRectangle.getHeight() * Const.PRE_LOAD_COEFFICIENT * 2));
			final Rectangle2D keepRectangle = new Rectangle2D.Double(visibleRectangle.getX()
					- (visibleRectangle.getWidth() * Const.KEEP_COFFICIENT), visibleRectangle.getY()
					- (visibleRectangle.getHeight() * Const.KEEP_COFFICIENT), visibleRectangle.getWidth()
					+ (visibleRectangle.getWidth() * Const.KEEP_COFFICIENT * 2), visibleRectangle.getHeight()
					+ (visibleRectangle.getHeight() * Const.KEEP_COFFICIENT * 2));
			// データを開放する
			if (zoom < Zoom.LOAD_GYOUSEI) {
				if (mapData.hasGyousei()) {
					mapData.freeSi_tyo();
					mapData.freeTyome();
					mapData.freeGyousei();
					this.isChanged = true;
				}
			}
			if (zoom < Zoom.LOAD_ALL) {
				if (mapData.hasRoadArc()) {
					mapData.freeRoadArc();
					mapData.freeOthers();
					mapData.freeEki();
					this.isChanged = true;
				}
				if (mapData.hasMizuArc()) {
					mapData.freeMizu();
					mapData.freeMizuArc();
					this.isChanged = true;
				}
				if (mapData.hasTatemonoArc()) {
					mapData.freeTatemono();
					mapData.freeTatemonoArc();
					this.isChanged = true;
				}
				if (mapData.hasZyouti()) {
					mapData.freeZyouti();
					this.isChanged = true;
				}
			}
			if (mapData.getBounds().intersects(preLoadRectangle)) {
				// データを読み込む
				if (zoom >= Zoom.LOAD_GYOUSEI) {
					if (!mapData.hasGyousei()) {
						mapData.loadGyousei();
						//mapData.loadSi_tyo();
						mapData.loadTyome();
						this.isChanged = true;
					}
				}
				if (zoom >= Zoom.LOAD_ALL) {
					if (!mapData.hasEki()) {
						mapData.loadEki();
						this.isChanged = true;
					}
					if (!mapData.hasOthers()) {
						mapData.loadOthers();
						this.isChanged = true;
					}
					if (!mapData.hasRoadArc()) {
						mapData.loadRoadArc();
						this.isChanged = true;
						this.isRoadChanged = true;
					}
					if (!mapData.hasMizuArc()) {
						mapData.loadMizuArc();
						mapData.loadMizu();
						this.isChanged = true;
					}
					if (!mapData.hasTatemonoArc()) {
						mapData.loadTatemonoArc();
						mapData.loadTatemono();
						this.isChanged = true;
					}
					if (!mapData.hasZyouti()) {
						mapData.loadZyouti();
						this.isChanged = true;
					}
				}
			} else if (!mapData.getBounds().intersects(keepRectangle)) {
				// データを開放する
				if (mapData.hasGyousei()) {
					mapData.freeSi_tyo();
					mapData.freeTyome();
					mapData.freeGyousei();
				}
				if (mapData.hasMizuArc()) {
					mapData.freeMizu();
					mapData.freeMizuArc();
				}
				if (mapData.hasOthers()) {
					mapData.freeZyouti();
				}
				if (mapData.hasTatemonoArc()) {
					mapData.freeTatemono();
					mapData.freeTatemonoArc();
				}
				if (mapData.hasRoadArc()) {
					mapData.freeRoadArc();
				}
				if (mapData.hasEki()) {
					mapData.freeEki();
				}
			}
		}
	}

	/**
	 * 直前の読み込みで、地図の状態が変化したかどうかを取得します。
	 * @return 地図の状態が変化したかどうか
	 */
	boolean isChanged() {
		return this.isChanged;
	}
}
