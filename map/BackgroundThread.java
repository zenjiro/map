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
	private final Map<String, MapData> maps;

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
	boolean isChanged;

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
			Progress.getInstance().setLoadMapPaintTyomeProgress(0);
			if (this.prefectures != null) {
				if (Prefectures.loadCities(this.prefectures, this.panel, this.maps, this.loadMap) && this.panel.isRouteMode()) {
					this.panel.initializeGraph();
				}
			}
			synchronized (this.maps) {
				Progress.getInstance().setLoadMapPaintTyomeProgress(20);
				this.loadMap.loadMap(this.maps, this.panel, visibleRectangle);
				if (this.loadMap.isRoadChanged() && this.panel.isRouteMode()) {
					this.panel.initializeGraph();
				}
				Progress.getInstance().setLoadMapPaintTyomeProgress(40);
				this.panel.loadYomi();
				Progress.getInstance().setLoadMapPaintTyomeProgress(50);
				new PaintTyome().paintTyome(this.maps);
				Progress.getInstance().setLoadMapPaintTyomeProgress(60);
				new JoinPolygon().joinPolygon(this.maps, visibleRectangle);
				Progress.getInstance().setLoadMapPaintTyomeProgress(80);
			}
			new JoinTatemono().joinTatemono(this.maps);
			this.panel.removeMessage();
			Progress.getInstance().setLoadMapPaintTyomeProgress(100);
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
				Progress.getInstance().initialize();
				Progress.getInstance().setStatus(Progress.Status.LOADING_MAP_PAINTING_TYOME);
				this.isChanged = false;
				final Rectangle2D visibleRectangle = this.panel.getVisibleRectangle(false);
				final double zoom = this.panel.getZoom();
				final double offsetX = this.panel.getOffsetX();
				final double offsetY = this.panel.getOffsetY();
				final double saturationDifference = this.panel.getSaturationDifference();
				this.loadMapPaintTyomeJoinTyome(visibleRectangle);
				Progress.getInstance().setStatus(Progress.Status.CREATING_BITMAP);
				Progress.getInstance().setCreateBitmapProgress(0);
				this.panel.createBitmapCache(zoom, offsetX, offsetY, saturationDifference);
				Progress.getInstance().setStatus(Progress.Status.REPAINTING);
				Progress.getInstance().setRepaintProgress(0);
				this.panel.setChanged();
				this.panel.forceRepaint();
				Progress.getInstance().complete();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
