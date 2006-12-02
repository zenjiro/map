package map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TimerTask;

/**
 * 地図の読み込み、ポリゴンの結合、塗り分け、属性配置位置の計算、再描画など、
 * バックグラウンドで行う作業を管理するクラスです。
 * @author zenjiro
 * 作成日: 2004/01/19
 */
public class BackgroundThread extends TimerTask {
	/**
	 * 地図
	 */
	private final Map<String, MapData> maps; // 地図

	/**
	 * 都道府県の一覧
	 */
	private final Collection<Prefecture> prefectures;

	/**
	 * 地図を表示するパネル
	 */
	private final MapPanel panel;

	/**
	 * 地図の状態が変化したかどうか
	 */
	boolean isChanged; // 地図の状態が変化したかどうか

	/**
	 * 地図を読み込むためのオブジェクト
	 */
	private final LoadMap loadMap;

	/**
	 * 地図の数
	 */
	private int mapSize;

	/**
	 * バックグラウンドで行う処理を初期化します。
	 * @param maps 地図
	 * @param panel パネル
	 * @param loadMap 地図を読み込むためのオブジェクト
	 */
	public BackgroundThread(final Map<String, MapData> maps, final MapPanel panel, final LoadMap loadMap) {
		this.maps = maps;
		this.panel = panel;
		this.prefectures = this.panel.getPrefectures();
		this.isChanged = true;
		this.loadMap = loadMap;
		this.mapSize = maps.size();
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				BackgroundThread.this.isChanged = true;
			}
		});
		panel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				BackgroundThread.this.isChanged = true;
			}
		});
		panel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(final MouseWheelEvent e) {
				BackgroundThread.this.isChanged = true;
			}
		});
		panel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				BackgroundThread.this.isChanged = true;
			}
		});
		panel.setActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				BackgroundThread.this.isChanged = true;
			}
		});
	}

	/**
	 * 地図の読み込み、ポリゴンの結合、塗り分けを行います。
	 * @param visibleRectangle 表示されている領域（仮想座標）
	 */
	void loadMapPaintTyomeJoinTyome(final Rectangle2D visibleRectangle) {
		try {
			this.panel.addMessage("地図を読み込んでいます。");
			if (this.prefectures != null) {
				Prefectures.loadCities(this.prefectures, this.panel, this.maps, this.loadMap);
			}
			synchronized (this.maps) {
				this.loadMap.loadMap(this.maps, this.panel, visibleRectangle);
				this.panel.loadYomi();
				new PaintTyome().paintTyome(this.maps);
				new JoinPolygon().joinPolygon(this.maps, visibleRectangle);
			}
			new JoinTatemono().joinTatemono(this.maps);
			this.panel.removeMessage();
		} catch (final Exception exception) {
			System.err.println("EXCEPTION: Failed to load map.");
			exception.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			if (this.maps.size() != this.mapSize) {
				this.isChanged = true;
				this.mapSize = this.maps.size();
			}
			if (this.isChanged) {
				this.isChanged = false;
				final Rectangle2D visibleRectangle = this.panel.getVisibleRectangle(false);
				final double zoom = this.panel.getZoom();
				final double offsetX = this.panel.getOffsetX();
				final double offsetY = this.panel.getOffsetY();
				this.loadMapPaintTyomeJoinTyome(visibleRectangle);
				this.panel.createBitmapCache(zoom, offsetX, offsetY);
				this.panel.setChanged();
				this.panel.forceRepaint();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
