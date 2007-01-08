package map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Formatter;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.swing.JLabel;
import javax.swing.JPanel;

import map.KsjRailway.Business;
import map.KsjRailway.Railway;
import map.KsjRailway.Station;

import org.apache.batik.svggen.SVGGraphics2DIOException;

import psout.PSOut;
import route.Route;
import route.Route.Category;
import search.Search;
import svgout.Paintable;
import svgout.SVGOut;
import zipcode.ZipCode;

/**
 * 地図をパネルに描画するクラスです。
 * @author zenjiro
 */
public class MapPanel extends JPanel implements Printable {
	/**
	 * アンチエイリアス表示するかどうか
	 */
	boolean isAntialias;

	/**
	 * 文字の大きさ
	 */
	private double fontZoom;

	/**
	 * 裏描画用のイメージ
	 */
	private Image image;

	/**
	 * 地図が変化したかどうか
	 */
	boolean isChanged;

	/**
	 * 直前のマウスカーソル座標
	 */
	double lastMouseX; // 直前のマウスカーソル座標

	/**
	 * 直前のマウスカーソル座標
	 */
	double lastMouseY; // 直前のマウスカーソル座標

	/**
	 * 背景スレッドに再計算を要求するためのアクションリスナ
	 */
	private ActionListener listener;

	/**
	 * 地図の設定
	 */
	MapPreferences mapPreferences; // 地図の設定

	/**
	 * 地図
	 */
	private final Map<String, MapData> maps;

	/**
	 * x座標の最大値
	 */
	private double maxX;

	/**
	 * y座標の最大値
	 */
	private double maxY;

	/**
	 * x座標の最小値
	 */
	private double minX;

	/**
	 * y座標の最小値
	 */
	private double minY;

	/**
	 * 表示倍率の最小値
	 */
	double minZoom;

	/**
	 * オフセット（実座標）
	 */
	double offsetX; // オフセット(実座標)

	/**
	 * オフセット（実座標）
	 */
	double offsetY; // オフセット(実座標)

	/**
	 * 都道府県の一覧
	 */
	private Collection<Prefecture> prefectures;

	/**
	 * 彩度の差分
	 */
	private float saturationDifference;

	/**
	 * 地図を検索するためのデータ構造
	 */
	private Search search;

	/**
	 * パネルの幅と高さ
	 */
	private Dimension size;

	/**
	 * 表示倍率
	 */
	double zoom; // 表示倍率

	/**
	 * ステータスバーに表示するメッセージ
	 */
	private final Stack<String> messages;

	/**
	 * ステータスバー
	 */
	private JLabel statusBar;

	/**
	 * 画面中央の都道府県市区町村名
	 * @since 4.06
	 */
	private String centerPrefectureCity;

	/**
	 * 画面中央の町丁目名
	 * @since 4.06
	 */
	private String centerTyome;

	/**
	 * 中心点を表示するかどうか
	 * @since 6.0.0
	 */
	private boolean isCenterMark;

	/**
	 * ルート探索モードかどうか
	 * @since 6.0.0
	 */
	private boolean isRouteMode;

	/**
	 * 地図を表示するパネルを初期化します。
	 */
	public void init() {
		this.mapPreferences = new DefaultMapPreferences();
		this.setBackground(this.mapPreferences.getBackGroundColor());
		this.offsetX = 0;
		this.offsetY = 0;
		this.zoom = 1;
		this.isChanged = true;
		this.fontZoom = 1;
		this.saturationDifference = 0;
		this.lastMouseX = this.offsetX;
		this.lastMouseY = this.offsetY;
		this.isAntialias = true;
		this.prefectures = Prefectures.loadPrefectures(this.mapPreferences, this);
		this.centerPrefectureCity = "";
		this.centerTyome = "";
		this.isCenterMark = false;
		this.isRouteMode = false;
		this.addMouseListener(new MouseAdapter() {
			@Override
			/**
			 * ダブルクリックで移動、拡大、縮小をします。
			 * @since 4.05
			 */
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() > 1) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						// since 6.1.0
						if (MapPanel.this.isRouteMode()) {
							Route.getInstance().removeNearestPoint(
									toVirtualLocation(new Point2D.Double(e.getX(), e.getY())),
									32 / MapPanel.this.getZoom());
							Route.getInstance().calcRoute();
						}
						final Point2D point = MapPanel.this.toVirtualLocation(new Point2D.Double(e.getX(), e.getY()));
						MapPanel.this.moveTo(point.getX(), point.getY());
						if (MapPanel.this.getZoom() < Const.Zoom.LOAD_CITIES) {
							MapPanel.this.zoomCities();
						} else if (MapPanel.this.getZoom() < Const.Zoom.LOAD_FINE_CITIES) {
							MapPanel.this.zoomFineCities();
						} else if (MapPanel.this.getZoom() < Const.Zoom.LOAD_FINE_ROAD) {
							MapPanel.this.zoomWide();
						} else if (MapPanel.this.getZoom() < Const.Zoom.LOAD_GYOUSEI) {
							MapPanel.this.zoomMiddle();
						} else if (MapPanel.this.getZoom() < Const.Zoom.LOAD_ALL) {
							MapPanel.this.zoomDetail();
						}
					} else {
						if (MapPanel.this.getZoom() > Const.Zoom.LOAD_ALL) {
							MapPanel.this.zoomDetail();
						} else if (MapPanel.this.getZoom() > Const.Zoom.LOAD_GYOUSEI) {
							MapPanel.this.zoomMiddle();
						} else if (MapPanel.this.getZoom() > Const.Zoom.LOAD_FINE_ROAD) {
							MapPanel.this.zoomWide();
						} else if (MapPanel.this.getZoom() > Const.Zoom.LOAD_FINE_CITIES) {
							MapPanel.this.zoomFineCities();
						} else if (MapPanel.this.getZoom() > Const.Zoom.LOAD_CITIES) {
							MapPanel.this.zoomCities();
						} else {
							MapPanel.this.zoomWhole();
						}
					}
					MapPanel.this.forceRepaint();
				} else {
					// since 6.0.0
					if (MapPanel.this.isRouteMode()) {
						if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0
								|| e.getButton() == MouseEvent.BUTTON3) {
							Route.getInstance().removeNearestPoint(
									toVirtualLocation(new Point2D.Double(e.getX(), e.getY())),
									32 / MapPanel.this.getZoom());
							Route.getInstance().calcRoute();
						} else if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0
								|| e.getButton() == MouseEvent.BUTTON2) {
							Route.getInstance().insertPoint(toVirtualLocation(new Point2D.Double(e.getX(), e.getY())));
							Route.getInstance().calcRoute();
						} else {
							Route.getInstance().addPoint(toVirtualLocation(new Point2D.Double(e.getX(), e.getY())));
							Route.getInstance().calcRoute();
						}
						MapPanel.this.forceRepaint();
					}
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				MapPanel.this.lastMouseX = e.getX();
				MapPanel.this.lastMouseY = e.getY();
				MapPanel.this.isAntialias = false;
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				MapPanel.this.isAntialias = true;
			}

		});
		this.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(final MouseEvent e) {
				MapPanel.this.offsetX -= (e.getX() - MapPanel.this.lastMouseX);
				MapPanel.this.offsetY -= (e.getY() - MapPanel.this.lastMouseY);
				MapPanel.this.lastMouseX = e.getX();
				MapPanel.this.lastMouseY = e.getY();
				MapPanel.this.isChanged = true;
				MapPanel.this.forceRepaint();
			}

			public void mouseMoved(final MouseEvent e) {
				if (e.getModifiersEx() == (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK)) {
					final Point2D point = MapPanel.this.toVirtualLocation(new Point2D.Double(e.getX(), e.getY()));
					System.out.println("DEBUG: x = " + point.getX() + ", y = " + point.getY());
				}
			}
		});
		this.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(final MouseWheelEvent e) {
				final int wheelRotation = e.getWheelRotation();
				final int centerX = e.getX();
				final int centerY = e.getY();
				MapPanel.this.doWheelRotation(wheelRotation, centerX, centerY);
				MapPanel.this.isChanged = true;
				MapPanel.this.forceRepaint();
			}
		});
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				MapPanel.this.isChanged = true;
			}
		});
	}

	/**
	 * 地図を表示するパネルを初期化します。
	 * @param maps 地図
	 */
	public MapPanel(final Map<String, MapData> maps) {
		this.maps = maps;
		this.messages = new Stack<String>();
	}

	/**
	 * ステータスバーに表示するメッセージをスタックに積みます。
	 * @param message メッセージ
	 */
	public void addMessage(final String message) {
		this.messages.push(message);
		this.statusBar.setText(this.getMessage());
	}

	/**
	 * 座標の最大値と最小値を計算します。
	 */
	public void calcMinMaxXY() {
		this.minX = Double.POSITIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		if (this.maps != null) {
			if (this.maps.isEmpty()) {
				if (this.prefectures != null) {
					for (final Prefecture prefecture : this.prefectures) {
						final Rectangle2D boudns = prefecture.getBounds();
						this.minX = Math.min(this.minX, boudns.getMinX());
						this.minY = Math.min(this.minY, boudns.getMinY());
						this.maxX = Math.max(this.maxX, boudns.getMaxX());
						this.maxY = Math.max(this.maxY, boudns.getMaxY());
					}
				} else {
					System.out.println(this.getClass().getName() + ": 遅延起動中と思われます。calcMinMaxXYをまた呼んでね。");
				}
			} else {
				for (final MapData mapData : this.maps.values()) {
					final Rectangle bounds = mapData.getBounds().getBounds();
					if (bounds.getMinX() < this.minX) {
						this.minX = bounds.getMinX();
					}
					if (bounds.getMinY() < this.minY) {
						this.minY = bounds.getMinY();
					}
					if (this.maxX < bounds.getMaxX()) {
						this.maxX = bounds.getMaxX();
					}
					if (this.maxY < bounds.getMaxY()) {
						this.maxY = bounds.getMaxY();
					}
				}
			}
		}
	}

	/**
	 * ビットマップキャッシュを生成してファイルに保存します。
	 * @param zoom 表示倍率
	 * @param offsetX オフセット（実座標）
	 * @param offsetY オフセット（実座標）
	 * @param saturationDifference 彩度の増分
	 * @throws IOException 入出力例外
	 */
	public void createBitmapCache(final double zoom, final double offsetX, final double offsetY,
			final double saturationDifference) throws IOException {
		if (zoom >= Const.BitmapCache.ZOOM) {
			this.addMessage("地図を描画しています。");
			final BufferedImage image = new BufferedImage(Const.BitmapCache.WIDTH, Const.BitmapCache.HEIGHT,
					BufferedImage.TYPE_INT_BGR);
			final Graphics2D g = (Graphics2D) image.getGraphics();
			final boolean isTransform = true;
			final int width = MapPanel.this.size == null ? MapPanel.this.getWidth() : MapPanel.this.size.width;
			final int height = MapPanel.this.size == null ? MapPanel.this.getHeight() : MapPanel.this.size.height;
			new File(Const.BitmapCache.CACHE_DIR).mkdirs();
			int maxCount = 0;
			for (int y = (int) (Math.floor(offsetY / Const.BitmapCache.HEIGHT)) * Const.BitmapCache.HEIGHT; y - offsetY < height; y += Const.BitmapCache.HEIGHT) {
				for (int x = (int) (Math.floor(offsetX / Const.BitmapCache.WIDTH)) * Const.BitmapCache.WIDTH; x
						- offsetX < width; x += Const.BitmapCache.WIDTH) {
					maxCount++;
				}
			}
			int count = 0;
			for (int y = (int) (Math.floor(offsetY / Const.BitmapCache.HEIGHT)) * Const.BitmapCache.HEIGHT; y - offsetY < height; y += Const.BitmapCache.HEIGHT) {
				for (int x = (int) (Math.floor(offsetX / Const.BitmapCache.WIDTH)) * Const.BitmapCache.WIDTH; x
						- offsetX < width; x += Const.BitmapCache.WIDTH) {
					final File file = new File(new Formatter().format("%s%s%d_%d_%f_%f_%d_%d.png",
							Const.BitmapCache.CACHE_DIR + File.separator, Const.BitmapCache.PREFIX,
							Const.BitmapCache.WIDTH, Const.BitmapCache.HEIGHT, saturationDifference, zoom, x, y)
							.toString());
					if (!file.exists()) {
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g.setColor(MapPanel.this.mapPreferences.getMizuPreferences().getFillColor());
						g.fillRect(0, 0, width, height);
						final double virtualX = x / zoom;
						final double virtualY = y / zoom;
						final double virtualWidth = width / zoom;
						final double virtualHeight = height / zoom;
						final Point2D center = MapPanel.this
								.toVirtualLocation(new Point2D.Double(width / 2, height / 2));
						MapPanel.this.centerPrefectureCity = "";
						MapPanel.this.centerTyome = "";
						try {
							this.drawHeight(g, x, y, Const.BitmapCache.WIDTH, Const.BitmapCache.HEIGHT, zoom);
							final AffineTransform transform = new AffineTransform();
							transform.translate(-x, -y);
							transform.scale(zoom, zoom);
							if (isTransform) {
								g.transform(transform);
							}
							this.drawKsj(g, isTransform, transform, virtualX, virtualY, virtualWidth, virtualHeight,
									center, zoom);
							this.drawSdf(g, isTransform, virtualX, virtualY, virtualWidth, virtualHeight, center,
									transform, zoom);
							this.drawKsjRailway(g, isTransform, transform, virtualX, virtualY, virtualWidth,
									virtualHeight, zoom);
							this.drawCities(g, isTransform, transform, virtualX, virtualY, virtualWidth, virtualHeight,
									center, zoom, true);
							if (isTransform) {
								g.transform(transform.createInverse());
							}
						} catch (final Exception e2) {
							e2.printStackTrace();
						}
						ImageIO.write(image, "PNG", file);
					}
					count++;
					Progress.getInstance().setCreateBitmapProgress((int) ((double) count / maxCount * 100));
				}
			}
			g.dispose();
			this.removeMessage();
		}
	}

	/**
	 * 文字を小さくします。
	 */
	public void decreaseFontSize() {
		this.fontZoom *= 0.9;
		this.mapPreferences.setFontZoom(this.fontZoom);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "decrement font size"));
		}
		this.isChanged = true;
	}

	/**
	 * 彩度を減らします。
	 */
	public void decreaseSaturation() {
		this.saturationDifference -= 0.05;
		this.isChanged = true;
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom fine cities"));
		}
	}

	/**
	 * マウスホイールの回転をシミュレートします。
	 * @param wheelRotation マウスホイールの回転方向、1で手前、-1で向こう
	 * @param centerX マウスポインタのx座標（実座標）
	 * @param centerY マウスポインタのy座標（実座標）
	 */
	void doWheelRotation(final int wheelRotation, final int centerX, final int centerY) {
		final double newZoom = Math.min(Const.Zoom.MAX_VALUE, Math.max(this.minZoom, wheelRotation > 0 ? this.zoom
				* Const.Zoom.RATE : this.zoom / Const.Zoom.RATE));
		this.zoom(newZoom, centerX, centerY);
	}

	/**
	 * 図形の輪郭を描画します。
	 * @param g 描画対象
	 * @param shape 描画するオブジェクト
	 * @param isTransform 描画対象全体を座標変換するかどうか
	 * @param transform 座標変換
	 */
	private void draw(final Graphics2D g, final Shape shape, final boolean isTransform, final AffineTransform transform) {
		if (isTransform) {
			g.draw(shape);
		} else {
			g.draw(transform.createTransformedShape(shape));
		}
	}

	/**
	 * @param g
	 * @param isTransform
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NoninvertibleTransformException
	 */
	private void drawBackground(final Graphics2D g, final boolean isTransform) throws UnsupportedEncodingException,
			IOException, FileNotFoundException, NoninvertibleTransformException {
		final int width = (this.size == null) ? this.getWidth() : this.size.width;
		final int height = (this.size == null) ? this.getHeight() : this.size.height;
		if (this.mapPreferences != null && this.zoom > 0) {
			g.setColor(this.mapPreferences.getMizuPreferences().getFillColor());
			g.fillRect(0, 0, width, height);
			final AffineTransform transform = new AffineTransform();
			transform.translate(-this.offsetX, -this.offsetY);
			transform.scale(this.zoom, this.zoom);
			final double zoom = this.zoom;
			final double x = this.offsetX / zoom;
			final double y = this.offsetY / zoom;
			final double w = width / zoom;
			final double h = height / zoom;
			this.drawHeight(g, this.offsetX, this.offsetY, width, height, zoom);
			if (isTransform) {
				g.transform(transform);
			}
			final Point2D center = this.toVirtualLocation(new Point2D.Double(width / 2, height / 2));
			this.drawKsj(g, isTransform, transform, x, y, w, h, center, zoom);
			this.drawSdf(g, isTransform, x, y, w, h, center, transform, zoom);
			this.drawKsjRailway(g, isTransform, transform, x, y, w, h, zoom);
			this.drawCities(g, isTransform, transform, x, y, w, h, center, zoom, true);
			if (isTransform) {
				g.transform(transform.createInverse());
			}
		}
	}

	/**
	 * 市区町村を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param x 描画領域の左端のx座標（仮想座標）
	 * @param y 描画領域の上端のy座標（仮想座標）
	 * @param w 描画領域の幅（仮想座標）
	 * @param h 描画領域の高さ（仮想座標）
	 * @param center 中心の座標（仮想座標）
	 * @param zoom 表示倍率
	 * @param isDark 暗くする部分のみ描画するかどうか
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void drawCities(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final double x, final double y, final double w, final double h, final Point2D center, final double zoom,
			final boolean isDark) throws UnsupportedEncodingException, IOException {
		if (this.zoom >= Const.Zoom.LOAD_CITIES) {
			if (this.prefectures != null) {
				for (final Prefecture prefecture : this.prefectures) {
					if (prefecture.hasCities()) {
						this.setFixedStroke(g, this.mapPreferences.getCityPreferences().getWidth(), isTransform, zoom);
						for (final City city : prefecture.getCities()) {
							final Shape shape = city.hasFineShape() ? city.getFineShape() : city.getShape();
							if (shape.intersects(x, y, w, h)) {
								if (isDark) {
									if (city.getURL() == null) {
										g.setColor(Color.BLACK);
										final Composite composite = g.getComposite();
										g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f));
										this.fill(g, shape, isTransform, transform);
										g.setComposite(composite);
									}
								} else {
									g.setColor(Color.BLACK);
									this.draw(g, shape, isTransform, transform);
									if (city.hasFineShape() && shape.contains(center)) {
										this.centerPrefectureCity = prefecture.getLabel() + city.getLabel();
									}
								}
							}
						}
						if (!isDark) {
							this.setFixedStroke(g, this.mapPreferences.getPrefecturePreferences().getWidth(),
									isTransform, zoom);
							this.draw(g, prefecture.hasFine() ? prefecture.getFineShape() : prefecture.getShape(),
									isTransform, transform);
						}
					}
				}
			}
		}
	}

	/**
	 * 駅の属性を描画します。
	 * @param g 描画対象
	 * @param mapData 地図
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawEkiLabel(final Graphics2D g, final MapData mapData, final Rectangle2D visibleRectangle,
			final double zoom, final double offsetX, final double offsetY) throws FileNotFoundException, IOException {
		if (mapData.hasEki()) {
			final double pointSize = this.mapPreferences.getEkiPreferences().getWidth();
			final Font ekiFont = this.mapPreferences.getEkiPreferences().getFont();
			g.setFont(ekiFont);
			final FontMetrics metrics = this.getFontMetrics(ekiFont);
			final int descent = metrics.getDescent();
			for (final PointData point : mapData.getEki().values()) {
				if (point.getAttribute() != null) {
					final Ellipse2D ellipse = new Ellipse2D.Double((point.getX() * zoom) - offsetX - (pointSize / 2),
							(point.getY() * zoom) - offsetY - (pointSize / 2), pointSize, pointSize);
					g.setColor(this.mapPreferences.getEkiPreferences().getFillColor());
					g.fill(ellipse);
					g.setColor(this.mapPreferences.getEkiPreferences().getBorderColor());
					g.draw(ellipse);
					if (point.getAttributeX() != 0 && point.getAttributeY() != 0) {
						g.drawString(point.getAttribute(), (float) ((point.getAttributeX() * zoom) - offsetX),
								(float) ((point.getAttributeY() * zoom) - offsetY - descent));
					}
				}
			}
		}
	}

	/**
	 * 行政界を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @param zoom 表示倍率
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawGyousei(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData, final double zoom) throws FileNotFoundException, IOException {
		if (mapData.hasGyousei() && mapData.hasTyome()) {
			for (final ArcData arc : mapData.getGyousei().values()) {
				if (arc.getTag() == ArcData.TAG_NORMAL) {
					if ((arc.getClassification() == ArcData.TYPE_GYOUSEI_PREFECTURE)
							|| (arc.getClassification() == ArcData.TYPE_GYOUSEI_CITY)
							|| (arc.getClassification() == ArcData.TYPE_GYOUSEI_VILLAGE)) {
						g.setColor(this.mapPreferences.getSi_tyoPreferences().getBorderColor());
						this.setVariableStroke(g, this.mapPreferences.getSi_tyoPreferences().getWidth(), isTransform,
								zoom);
					} else {
						g.setColor(this.mapPreferences.getTyomePreferences().getBorderColor());
						this.setVariableStroke(g, this.mapPreferences.getTyomePreferences().getWidth(), isTransform,
								zoom);
					}
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
		}
	}

	/**
	 * 街区レベル位置参照情報を描画します。
	 * @param g 描画対象
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void drawIsj(final Graphics2D g, final Rectangle2D visibleRectangle, final double zoom,
			final double offsetX, final double offsetY) throws UnsupportedEncodingException, IOException {
		final Font font = this.mapPreferences.getIsjPreferences().getFont();
		final int descent = this.getFontMetrics(font).getDescent();
		g.setFont(font);
		g.setColor(this.mapPreferences.getIsjPreferences().getAttributeColor());
		if (this.prefectures != null) {
			for (final Prefecture prefecture : this.prefectures) {
				if (prefecture.hasCities()) {
					for (final City city : prefecture.getCities()) {
						if ((city.hasFineShape() ? city.getFineShape() : city.getShape()).intersects(visibleRectangle)) {
							for (final Map.Entry<Point2D, String> entry : city.getIsjLabels().entrySet()) {
								g.drawString(entry.getValue(), (float) (entry.getKey().getX() * zoom - offsetX),
										(float) (entry.getKey().getY() * zoom - offsetY - descent));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 国土数値情報を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param x 描画領域の左端のx座標（仮想座標）
	 * @param y 描画領域の上端のy座標（仮想座標）
	 * @param w 描画領域の幅（仮想座標）
	 * @param h 描画領域の高さ（仮想座標）
	 * @param center 中心の座標（仮想座標）
	 * @param zoom 表示倍率
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void drawKsj(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final double x, final double y, final double w, final double h, final Point2D center, final double zoom)
			throws UnsupportedEncodingException, IOException {
		// 都道府県を描画する
		this.drawPrefectures(g, isTransform, transform, x, y, w, h, zoom);
		// 市区町村を描画する
		this.drawCities(g, isTransform, transform, x, y, w, h, center, zoom, false);
	}

	/**
	 * 文字列を描画します。
	 * @param g 描画対象
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void drawLabels(final Graphics2D g, final Rectangle2D visibleRectangle, final double zoom,
			final double offsetX, final double offsetY) throws FileNotFoundException, IOException,
			UnsupportedEncodingException {
		// 最短経路探索の探索結果を描画する since 5.03
		if (this.isRouteMode) {
			for (final boolean is1st : new boolean[] { true, false }) {
				for (final Shape shape : Route.getInstance().getRoute()) {
					if (is1st) {
						g.setStroke(new BasicStroke(this.getMapPreferences().getRoutePreferences().getWidth() + 2));
						g.setColor(this.getMapPreferences().getRoutePreferences().getBorderColor());
					} else {
						g.setStroke(new BasicStroke(this.getMapPreferences().getRoutePreferences().getWidth()));
						g.setColor(this.getMapPreferences().getRoutePreferences().getFillColor());
					}
					final AffineTransform transform = new AffineTransform();
					transform.translate(-this.offsetX, -this.offsetY);
					transform.scale(this.zoom, this.zoom);
					this.draw(g, shape, false, transform);
				}
			}
		}
		// 最短経路探索の経由地を描画する since 6.0.0
		if (this.isRouteMode) {
			for (final Point2D point : Route.getInstance().getPoints()) {
				final Point2D point2 = toRealLocation(point);
				final float radius = 4;
				final GeneralPath path = new GeneralPath();
				path.moveTo((float) point2.getX() - radius, (float) point2.getY() - radius);
				path.lineTo((float) point2.getX() + radius, (float) point2.getY() + radius);
				path.moveTo((float) point2.getX() + radius, (float) point2.getY() - radius);
				path.lineTo((float) point2.getX() - radius, (float) point2.getY() + radius);
				g.setStroke(new BasicStroke(3));
				g.setColor(Color.BLACK);
				g.draw(path);
				g.setStroke(new BasicStroke(2));
				g.setColor(Color.YELLOW);
				g.draw(path);
			}
		}
		if (this.maps != null & zoom >= Const.Zoom.LOAD_GYOUSEI) {
			g.setStroke(new BasicStroke(1f));
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(visibleRectangle)) {
					// 建物のラベルを描画する
					this.drawTatemonoLabel(g, mapData, visibleRectangle, zoom, offsetX, offsetY);
					// 場地のラベルを描画する
					this.drawZyoutiLabel(g, mapData, visibleRectangle, zoom, offsetX, offsetY);
					// 内水面のラベルを描画する
					this.drawMizuLabel(g, mapData, visibleRectangle, zoom, offsetX, offsetY);
					// 丁目のラベルを描画する 
					this.drawTyomeLabel(g, mapData, visibleRectangle, zoom, offsetX, offsetY);
					// 町丁目の読みを描画する
					this.drawTyomeYomi(g, mapData, visibleRectangle, zoom, offsetX, offsetY);
					// 駅のラベルを描画する
					this.drawEkiLabel(g, mapData, visibleRectangle, zoom, offsetX, offsetY);
					// 道路のラベルを描画する
					this.drawRoadLabel(g, mapData, visibleRectangle, zoom, offsetX, offsetY);
					// 鉄道のラベルを描画する
					this.drawRailwayLabel(g, mapData, visibleRectangle, zoom, offsetX, offsetY);
				}
			}
			// 銀行、コンビニ、ファストフード店を描画する
			if (zoom >= Const.Zoom.LOAD_ALL) {
				this.drawShops(g, visibleRectangle, zoom, offsetX, offsetY);
			}
			// 街区レベル位置参照情報を描画する
			if (zoom >= Const.Zoom.LOAD_ALL) {
				this.drawIsj(g, visibleRectangle, zoom, offsetX, offsetY);
			}
		}
		// 国土数値情報の駅名を描画する
		if (zoom < Const.Zoom.LOAD_ALL && zoom >= Const.Zoom.LOAD_FINE_CITIES) {
			drawKsjRailwayStationLabels(g, visibleRectangle, zoom, offsetX, offsetY);
		}
		// 国土数値情報の鉄道の文字列を描画する
		if (zoom < Const.Zoom.LOAD_ALL && zoom >= Const.Zoom.LOAD_FINE_CITIES) {
			drawKsjRailwayCurveLabels(g, visibleRectangle, zoom, offsetX, offsetY);
		}
		// ルートの文字列を描画する
		if (Route.getInstance().getCaption() != null && Route.getInstance().getCaptionLocation() != null) {
			drawRouteLabel(g, visibleRectangle, zoom, offsetX, offsetY);
		}
		// 中心点を描画する
		if (this.isCenterMark) {
			final int size = 32;
			final int diameter = 18;
			final RoundRectangle2D.Double centerMark = new RoundRectangle2D.Double(this.getWidth() / 2 - size / 2, this
					.getHeight()
					/ 2 - size / 2, size, size, diameter, diameter);
			g.setStroke(new BasicStroke(3));
			g.setColor(Color.WHITE);
			g.draw(centerMark);
			g.setStroke(new BasicStroke(1.5f));
			g.setColor(Color.RED);
			g.draw(centerMark);
		}
	}

	/**
	 * 国土数値情報の鉄道データの文字列を描画します。
	 * @param g 描画対象
	 * @param visibleRectangle 表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX 平行移動するx座標（実座標）
	 * @param offsetY 平行移動するy座標（実座標）
	 * @throws IOException 入出力例外
	 */
	private void drawKsjRailwayCurveLabels(final Graphics2D g, final Rectangle2D visibleRectangle, final double zoom,
			final double offsetX, final double offsetY) throws IOException {
		if (this.prefectures != null) {
			g.setFont(this.mapPreferences.getKsjRailwayPreferences().getFont());
			final int descent = this.getFontMetrics(g.getFont()).getDescent();
			g.setColor(this.mapPreferences.getKsjRailwayPreferences().getAttributeColor());
			for (final Prefecture prefecture : this.prefectures) {
				final Collection<Railway> railways = prefecture.getKsjRailwayCurves();
				drawKsjRailwayCaptions(railways, g, visibleRectangle, zoom, offsetX, offsetY, descent);
				if (prefecture.hasCities()) {
					for (final City city : prefecture.getCities()) {
						if (city.hasKsjFineRoad()) {
							drawKsjRailwayCaptions(city.getKsjFineRoad(), g, visibleRectangle, zoom, offsetX, offsetY,
									descent);
						}
					}
				}
			}
		}
	}

	/**
	 * ルートの文字列を描画します。
	 * @param g 描画対象
	 * @param visibleRectangle 表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX 平行移動するx座標（実座標）
	 * @param offsetY 平行移動するy座標（実座標）
	 */
	private void drawRouteLabel(final Graphics2D g, final Rectangle2D visibleRectangle, final double zoom,
			final double offsetX, final double offsetY) {
		g.setFont(this.mapPreferences.getRoutePreferences().getFont());
		final int descent = this.getFontMetrics(g.getFont()).getDescent();
		g.setColor(this.mapPreferences.getRoutePreferences().getAttributeColor());
		g.drawString(Route.getInstance().getCaption(),
				(float) (Route.getInstance().getCaptionLocation().getX() * zoom - offsetX), (float) (Route
						.getInstance().getCaptionLocation().getY()
						* zoom - offsetY - descent));
	}

	/**
	 * 国土数値情報の鉄道データの文字列を描画します。
	 * @param g 描画対象
	 * @param visibleRectangle 表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX 平行移動するx座標（実座標）
	 * @param offsetY 平行移動するy座標（実座標）
	 */
	private void drawKsjRailwayStationLabels(final Graphics2D g, final Rectangle2D visibleRectangle, final double zoom,
			final double offsetX, final double offsetY) {
		if (this.prefectures != null) {
			g.setFont(this.mapPreferences.getKsjRailwayStationPreferences().getFont());
			final int descent = this.getFontMetrics(g.getFont()).getDescent();
			g.setColor(this.mapPreferences.getKsjRailwayStationPreferences().getAttributeColor());
			for (final Prefecture prefecture : this.prefectures) {
				final Collection<? extends Railway> railways = prefecture.getKsjRailwayStations();
				drawKsjRailwayCaptions(railways, g, visibleRectangle, zoom, offsetX, offsetY, descent);
			}
		}
	}

	/**
	 * 国土数値情報の鉄道データの文字列を描画します。
	 * @param railways 鉄道データ
	 * @param g 描画対象
	 * @param visibleRectangle 表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX 平行移動するx座標（実座標）
	 * @param offsetY 平行移動するy座標（実座標）
	 * @param descent 文字列のディセント（実座標）
	 */
	private void drawKsjRailwayCaptions(final Collection<? extends Railway> railways, final Graphics2D g,
			final Rectangle2D visibleRectangle, final double zoom, final double offsetX, final double offsetY,
			final int descent) {
		for (final Railway railway : railways) {
			if (railway.getCaptionLocation() != null) {
				if (visibleRectangle.contains(railway.getShape().getBounds2D())) {
					g.drawString(railway instanceof Station ? ((Station) railway).getStation() : railway.getCaption(),
							(float) (railway.getCaptionLocation().getX() * zoom - offsetX), (float) (railway
									.getCaptionLocation().getY()
									* zoom - offsetY - descent));
				}
			}
		}
	}

	/**
	 * 小縮尺での鉄道を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @param zoom 表示倍率
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawLargeRailway(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData, final double zoom) throws FileNotFoundException, IOException {
		if (mapData.hasOthers()) {
			g.setColor(this.mapPreferences.getRailwayPreferences().getBorderColor());
			this.setFixedStroke(g, 1, isTransform, zoom);
			for (final ArcData arc : mapData.getOthers().values()) {
				if (arc.getClassification() == ArcData.TYPE_RAILWAY) {
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
		}
	}

	/**
	 * 小縮尺での道路を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @param zoom 表示倍率
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawLargeRoad(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData, final double zoom) throws FileNotFoundException, IOException {
		if (mapData.hasLargeRoadArc()) {
			if (mapData.hasTyome()) {
				this.setFixedStroke(g, 3, isTransform, zoom);
				for (final ArcData arc : mapData.getLargeRoadArc().values()) {
					if (arc.getRoadType() == ArcData.ROAD_HIGHWAY) {
						g.setColor(this.mapPreferences.getHighwayPreferences().getBorderColor());
					} else if (arc.getRoadType() == ArcData.ROAD_KOKUDO) {
						g.setColor(this.mapPreferences.getKokudoPreferences().getBorderColor());
					} else if (arc.getRoadType() == ArcData.ROAD_CHIHODO) {
						g.setColor(this.mapPreferences.getChihodoPreferences().getBorderColor());
					}
					this.draw(g, arc.getPath(), isTransform, transform);
				}
				this.setFixedStroke(g, 2, isTransform, zoom);
				for (final ArcData arc : mapData.getLargeRoadArc().values()) {
					if (arc.getRoadType() == ArcData.ROAD_HIGHWAY) {
						g.setColor(this.mapPreferences.getHighwayPreferences().getFillColor());
					} else if (arc.getRoadType() == ArcData.ROAD_KOKUDO) {
						g.setColor(this.mapPreferences.getKokudoPreferences().getFillColor());
					} else if (arc.getRoadType() == ArcData.ROAD_CHIHODO) {
						g.setColor(this.mapPreferences.getChihodoPreferences().getFillColor());
					}
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
			this.setFixedStroke(g, 1, isTransform, zoom);
			for (final ArcData arc : mapData.getLargeRoadArc().values()) {
				if (arc.getRoadType() == ArcData.ROAD_HIGHWAY) {
					g.setColor(this.mapPreferences.getHighwayPreferences().getBorderColor());
				} else if (arc.getRoadType() == ArcData.ROAD_KOKUDO) {
					g.setColor(this.mapPreferences.getKokudoPreferences().getBorderColor());
				} else if (arc.getRoadType() == ArcData.ROAD_CHIHODO) {
					g.setColor(this.mapPreferences.getChihodoPreferences().getBorderColor());
				}
				this.draw(g, arc.getPath(), isTransform, transform);
			}
		}
	}

	/**
	 * 地図を描画します。
	 * SVGGraphics2D#transform(AffineTransform)の誤差が大きくて使いものにならないので、AffineTransform#createTransformedShape(Shape)を使うようにしました。
	 * @param g グラフィクスコンテキスト
	 * @param isTransform 描画対象全体を座標変換するかどうか。
	 * @throws IOException 
	 */
	public void drawMap(final Graphics2D g, final boolean isTransform) throws IOException {
		this.addMessage("地図を描画しています。");
		new FixAttributeLocation().fixAttributeLocation(this.maps, this.prefectures, this);
		this.centerPrefectureCity = "";
		this.centerTyome = "";
		try {
			this.drawBackground(g, isTransform);
			this.drawLabels(g, this.getVisibleRectangle(true), this.getZoom(), this.getOffsetX(), this.getOffsetY());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		this.removeMessage();
	}

	/**
	 * 内水面の輪郭を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawMizu(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData) throws FileNotFoundException, IOException {
		if (mapData.hasMizuArc()) {
			for (final ArcData arc : mapData.getMizuArc().values()) {
				if (arc.getTag() == ArcData.TAG_NORMAL) {
					if (arc.getClassification() == ArcData.TYPE_MIZU_INSIDE) {
						g.setColor(this.mapPreferences.getMizuPreferences().getBorderColor());
						this.draw(g, arc.getPath(), isTransform, transform);
					}
				}
			}
		}
	}

	/**
	 * 内水面の属性を描画します。
	 * @param g 描画対象
	 * @param mapData 地図
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawMizuLabel(final Graphics2D g, final MapData mapData, final Rectangle2D visibleRectangle,
			final double zoom, final double offsetX, final double offsetY) throws FileNotFoundException, IOException {
		if (mapData.hasMizu()) {
			g.setColor(this.mapPreferences.getMizuPreferences().getAttributeColor());
			final Font mizuFont = this.mapPreferences.getMizuPreferences().getFont();
			g.setFont(mizuFont);
			final FontMetrics metrics = this.getFontMetrics(mizuFont);
			final int descent = metrics.getDescent();
			for (final PolygonData polygon : mapData.getMizu().values()) {
				if (polygon.getAttribute() != null) {
					if (visibleRectangle.contains(polygon.getAttributeX(), polygon.getAttributeY())) {
						g.drawString(polygon.getAttribute(), (float) ((polygon.getAttributeX() * zoom) - offsetX),
								(float) ((polygon.getAttributeY() * zoom) - offsetY - descent));
					}
				}
			}
		}
	}

	/**
	 * 国土数値情報の標高・傾斜度3次メッシュから生成されたPNGファイルを描画します。
	 * @param g 描画対象
	 * @param offsetX 描画領域の左端のx座標（実座標）
	 * @param offsetY 描画領域の上端のy座標（実座標）
	 * @param width 描画領域の幅（実座標）
	 * @param height 描画領域の高さ（実座標）
	 * @param zoom 表示倍率
	 * @throws IOException 
	 * @since 5.00
	 */
	private void drawHeight(final Graphics2D g, final double offsetX, final double offsetY, final double width,
			final double height, final double zoom) throws IOException {
		final double z2;
		if (zoom < Const.Ksj.Height.zoom2) {
			z2 = Const.Ksj.Height.zoom1;
		} else if (zoom < Const.Ksj.Height.zoom3) {
			z2 = Const.Ksj.Height.zoom2;
		} else if (zoom < Const.Ksj.Height.zoom4) {
			z2 = Const.Ksj.Height.zoom3;
		} else {
			z2 = Const.Ksj.Height.zoom4;
		}
		final double x2 = offsetX / zoom * z2;
		final double y2 = offsetY / zoom * z2;
		final double w2 = width / zoom * z2;
		final double h2 = height / zoom * z2;
		for (int y = (int) (Math.floor(y2 / Const.Ksj.Height.HEIGHT)) * Const.Ksj.Height.HEIGHT; y - y2 < h2; y += Const.Ksj.Height.HEIGHT) {
			for (int x = (int) (Math.floor(x2 / Const.Ksj.Height.WIDTH)) * Const.Ksj.Height.WIDTH; x - x2 < w2; x += Const.Ksj.Height.WIDTH) {
				final InputStream in = this.getClass().getResourceAsStream(
						new Formatter().format("%s%s%d_%d_%f_%d_%d.png", Const.DIR, Const.Ksj.Height.PREFIX,
								Const.Ksj.Height.WIDTH, Const.Ksj.Height.HEIGHT, z2, x, y).toString());
				if (in != null) {
					final Image image = ImageIO.read(in);
					g.drawImage(image, (int) (x * zoom / z2 - offsetX), (int) (y * zoom / z2 - offsetY),
							(int) (Const.Ksj.Height.WIDTH * zoom / z2 + 1),
							(int) (Const.Ksj.Height.HEIGHT * zoom / z2 + 1), this);
					in.close();
				}
			}
		}
	}

	/**
	 * 都道府県を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param x 描画領域の左端のx座標（仮想座標）
	 * @param y 描画領域の上端のy座標（仮想座標）
	 * @param w 描画領域の幅（仮想座標）
	 * @param h 描画領域の高さ（仮想座標）
	 * @param zoom 表示倍率
	 */
	private void drawPrefectures(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final double x, final double y, final double w, final double h, final double zoom) {
		if (this.prefectures != null) {
			for (final Prefecture prefecture : this.prefectures) {
				this.setFixedStroke(g, this.mapPreferences.getPrefecturePreferences().getWidth(), isTransform, zoom);
				if (prefecture.getBounds().intersects(x, y, w, h)) {
					final Shape shape = prefecture.hasFine() ? prefecture.getFineShape() : prefecture.getShape();
					if (shape.intersects(x, y, w, h)) {
						final Color color = prefecture.getColor();
						if (this.saturationDifference == 0) {
							g.setColor(color);
						} else {
							final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(),
									new float[] { 0, 0, 0 });
							hsb[1] = Math.min(1, Math.max(0, hsb[1] + this.saturationDifference));
							g.setColor(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
						}
						// test
						//this.fill(g, shape, isTransform, transform);
						g.setColor(Color.BLACK);
						this.draw(g, shape, isTransform, transform);
					}
				}
			}
		}
	}

	/**
	 * 国土数値情報の鉄道を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param x 描画領域の左端のx座標（仮想座標）
	 * @param y 描画領域の上端のy座標（仮想座標）
	 * @param w 描画領域の幅（仮想座標）
	 * @param h 描画領域の高さ（仮想座標）
	 * @param zoom 表示倍率
	 * @throws IOException 入出力例外
	 */
	private void drawKsjRailway(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final double x, final double y, final double w, final double h, final double zoom) throws IOException {
		if (this.prefectures != null) {
			if (this.zoom < Const.Zoom.LOAD_ALL && Const.Zoom.LOAD_FINE_CITIES <= this.zoom) {
				for (final Prefecture prefecture : this.prefectures) {
					if (prefecture.hasCities()) {
						for (final boolean isBorder : new boolean[] { true, false }) {
							for (final City city : prefecture.getCities()) {
								if (city.hasKsjFineRoad()) {
									for (final Business business : new Business[] { Business.ROAD_MAJOR,
											Business.ROAD_KOKUDO }) {
										drawKsjFineRoad(g, city.getKsjFineRoad(), isBorder, business, isTransform,
												transform, zoom);
									}
								}
							}
						}
					}
					drawKsjRailway(g, prefecture.getKsjRailwayCurves(), prefecture, isTransform, transform, x, y, w, h,
							zoom);
				}
				this.setFixedStroke(g, this.mapPreferences.getKsjRailwayStationPreferences().getWidth() + 2,
						isTransform, zoom);
				g.setColor(this.mapPreferences.getKsjRailwayStationPreferences().getBorderColor());
				drawKsjStation(g, isTransform, transform, x, y, w, h);
				this.setFixedStroke(g, this.mapPreferences.getKsjRailwayStationPreferences().getWidth(), isTransform,
						zoom);
				g.setColor(this.mapPreferences.getKsjRailwayStationPreferences().getFillColor());
				drawKsjStation(g, isTransform, transform, x, y, w, h);
				for (final Prefecture prefecture : this.prefectures) {
					if (prefecture.hasCities()) {
						for (final boolean isBorder : new boolean[] { true, false }) {
							for (final City city : prefecture.getCities()) {
								if (city.hasKsjFineRoad()) {
									drawKsjFineRoad(g, city.getKsjFineRoad(), isBorder, Business.ROAD_HIGHWAY,
											isTransform, transform, zoom);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @param g 描画対象
	 * @param railways 道路の一覧
	 * @param isBorder 外側を描画するかどうか
	 * @param business 道路の種類
	 * @param isTransform 座標変換するかどうか
	 * @param transform 座標変換
	 * @param zoom 表示倍率
	 */
	private void drawKsjFineRoad(final Graphics2D g, final Collection<Railway> railways, final boolean isBorder,
			final Business business, final boolean isTransform, final AffineTransform transform, final double zoom) {
		switch (business) {
		case ROAD_MAJOR:
			if (isBorder) {
				this.setFixedStroke(g, this.getMapPreferences().getKsjRoadMajorPreferences().getWidth() + 2,
						isTransform, zoom);
				g.setColor(this.getMapPreferences().getKsjRoadMajorPreferences().getBorderColor());
			} else {
				this.setFixedStroke(g, this.getMapPreferences().getKsjRoadMajorPreferences().getWidth(), isTransform,
						zoom);
				g.setColor(this.getMapPreferences().getKsjRoadMajorPreferences().getFillColor());
			}
			break;
		case ROAD_KOKUDO:
			if (isBorder) {
				this.setFixedStroke(g, this.getMapPreferences().getKsjRoadKokudoPreferences().getWidth() + 2,
						isTransform, zoom);
				g.setColor(this.getMapPreferences().getKsjRoadKokudoPreferences().getBorderColor());
			} else {
				this.setFixedStroke(g, this.getMapPreferences().getKsjRoadKokudoPreferences().getWidth(), isTransform,
						zoom);
				g.setColor(this.getMapPreferences().getKsjRoadKokudoPreferences().getFillColor());
			}
			break;
		case ROAD_HIGHWAY:
			if (isBorder) {
				this.setFixedStroke(g, this.getMapPreferences().getKsjRoadHighwayPreferences().getWidth() + 2,
						isTransform, zoom);
				g.setColor(this.getMapPreferences().getKsjRoadHighwayPreferences().getBorderColor());
			} else {
				this.setFixedStroke(g, this.getMapPreferences().getKsjRoadHighwayPreferences().getWidth(), isTransform,
						zoom);
				g.setColor(this.getMapPreferences().getKsjRoadHighwayPreferences().getFillColor());
			}
			break;
		}
		for (final Railway railway : railways) {
			if (railway.getBusiness() == business) {
				this.draw(g, railway.getShape(), isTransform, transform);
			}
		}
	}

	/**
	 * 国土数値情報の鉄道データの駅を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param x 描画領域の左端のx座標（仮想座標）
	 * @param y 描画領域の上端のy座標（仮想座標）
	 * @param w 描画領域の幅（仮想座標）
	 * @param h 描画領域の高さ（仮想座標）
	 */
	private void drawKsjStation(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final double x, final double y, final double w, final double h) {
		for (final Prefecture prefecture : this.prefectures) {
			if (prefecture.getBounds().intersects(x, y, w, h)) {
				if (prefecture.hasFine()) {
					for (final Railway railway : prefecture.getKsjRailwayStations()) {
						this.draw(g, railway.getShape(), isTransform, transform);
					}
				}
			}
		}
	}

	/**
	 * 国土数値情報の鉄道を描画します。
	 * @param g 描画対象
	 * @param railways 鉄道
	 * @param fineRoad 高精度の道路データ
	 * @param prefecture 都道府県
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param x 描画領域の左端のx座標（仮想座標）
	 * @param y 描画領域の上端のy座標（仮想座標）
	 * @param w 描画領域の幅（仮想座標）
	 * @param h 描画領域の高さ（仮想座標）
	 * @param zoom 表示倍率
	 */
	private void drawKsjRailway(final Graphics2D g, final Collection<? extends Railway> railways,
			final Prefecture prefecture, final boolean isTransform, final AffineTransform transform, final double x,
			final double y, final double w, final double h, final double zoom) {
		if (prefecture.getBounds().intersects(x, y, w, h)) {
			if (prefecture.hasFine()) {
				drawKsjRailway(g, KsjRailway.Business.UNKNOWN, railways, isTransform, transform, zoom);
				drawKsjRailway(g, KsjRailway.Business.JR, railways, isTransform, transform, zoom);
				drawKsjRailway(g, KsjRailway.Business.SHINKANSEN, railways, isTransform, transform, zoom);
			}
		}
	}

	/**
	 * 国土数値情報の鉄道データを描画します。
	 * @param g 描画対象
	 * @param railways 鉄道データ
	 * @param business 事業者種別
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param zoom 表示倍率
	 */
	private void drawKsjRailway(final Graphics2D g, final Business business,
			final Collection<? extends Railway> railways, final boolean isTransform, final AffineTransform transform,
			final double zoom) {
		if (business == KsjRailway.Business.JR || business == KsjRailway.Business.SHINKANSEN) {
			this.setFixedStroke(g, this.mapPreferences.getKsjRailwayJRPreferences().getWidth() + 2, isTransform, zoom);
			g.setColor(this.mapPreferences.getKsjRailwayJRPreferences().getBorderColor());
		} else if (business == KsjRailway.Business.ROAD_HIGHWAY) {
			this
					.setFixedStroke(g, this.mapPreferences.getKsjRoadHighwayPreferences().getWidth() + 2, isTransform,
							zoom);
			g.setColor(this.mapPreferences.getKsjRoadHighwayPreferences().getBorderColor());
		} else if (business == KsjRailway.Business.ROAD_KOKUDO) {
			this.setFixedStroke(g, this.mapPreferences.getKsjRoadKokudoPreferences().getWidth() + 2, isTransform, zoom);
			g.setColor(this.mapPreferences.getKsjRoadKokudoPreferences().getBorderColor());
		} else if (business == KsjRailway.Business.ROAD_MAJOR) {
			this.setFixedStroke(g, this.mapPreferences.getKsjRoadMajorPreferences().getWidth() + 2, isTransform, zoom);
			g.setColor(this.mapPreferences.getKsjRoadMajorPreferences().getBorderColor());
		} else {
			this.setFixedStroke(g, this.mapPreferences.getKsjRailwayPreferences().getWidth(), isTransform, zoom);
			g.setColor(this.mapPreferences.getKsjRailwayPreferences().getBorderColor());
		}
		for (final Railway railway : railways) {
			drawKsjRailway(g, railway, business, isTransform, transform);
		}
		if (business == KsjRailway.Business.JR) {
			setFixedJRStroke(g, this.mapPreferences.getKsjRailwayJRPreferences().getWidth(), 5, isTransform);
			g.setColor(this.mapPreferences.getKsjRailwayJRPreferences().getFillColor());
		} else if (business == KsjRailway.Business.SHINKANSEN) {
			setFixedJRStroke(g, this.mapPreferences.getKsjRailwayJRPreferences().getWidth(), 15, isTransform);
			g.setColor(this.mapPreferences.getKsjRailwayJRPreferences().getFillColor());
		} else if (business == KsjRailway.Business.ROAD_HIGHWAY) {
			this.setFixedStroke(g, this.mapPreferences.getKsjRoadHighwayPreferences().getWidth(), isTransform, zoom);
			g.setColor(this.mapPreferences.getKsjRoadHighwayPreferences().getFillColor());
		} else if (business == KsjRailway.Business.ROAD_KOKUDO) {
			this.setFixedStroke(g, this.mapPreferences.getKsjRoadKokudoPreferences().getWidth(), isTransform, zoom);
			g.setColor(this.mapPreferences.getKsjRoadKokudoPreferences().getFillColor());
		} else if (business == KsjRailway.Business.ROAD_MAJOR) {
			this.setFixedStroke(g, this.mapPreferences.getKsjRoadMajorPreferences().getWidth(), isTransform, zoom);
			g.setColor(this.mapPreferences.getKsjRoadMajorPreferences().getFillColor());
		} else {
			return;
		}
		for (final Railway railway : railways) {
			drawKsjRailway(g, railway, business, isTransform, transform);
		}
	}

	/**
	 * 国土数値情報の鉄道データを描画します。
	 * @param g 描画対象
	 * @param railway 鉄道データ
	 * @param business 事業者種別
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 */
	private void drawKsjRailway(final Graphics2D g, final Railway railway, final Business business,
			final boolean isTransform, final AffineTransform transform) {
		if (business == KsjRailway.Business.JR || business == KsjRailway.Business.SHINKANSEN
				|| business == KsjRailway.Business.ROAD_HIGHWAY || business == KsjRailway.Business.ROAD_KOKUDO
				|| business == KsjRailway.Business.ROAD_MAJOR) {
			if (railway.getBusiness() == business) {
				this.draw(g, railway.getShape(), isTransform, transform);
			}
		} else {
			if (railway.getBusiness() != KsjRailway.Business.JR
					&& railway.getBusiness() != KsjRailway.Business.SHINKANSEN
					&& railway.getBusiness() != KsjRailway.Business.ROAD_HIGHWAY
					&& railway.getBusiness() != KsjRailway.Business.ROAD_KOKUDO
					&& railway.getBusiness() != KsjRailway.Business.ROAD_MAJOR) {
				this.draw(g, railway.getShape(), isTransform, transform);
			}
		}
	}

	/**
	 * 表示倍率によらず一定の太さで表示されるJRの塗りつぶし部のストロークを設定します。
	 * @param g 描画対象
	 * @param width 塗りつぶし部の幅
	 * @param dash 破線の長さ
	 * @param isTransform 描画対象を座標変換するかどうか
	 */
	private void setFixedJRStroke(final Graphics2D g, final float width, final int dash, final boolean isTransform) {
		if (isTransform) {
			g.setStroke(new BasicStroke((float) (width / this.zoom), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
					(float) (10 / this.zoom), new float[] { (float) (width * dash / this.zoom) }, 0));
		} else {
			g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { width
					* dash }, 0));
		}
	}

	/**
	 * 鉄道を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @param zoom 表示倍率
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawRailway(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData, final double zoom) throws FileNotFoundException, IOException {
		if (mapData.hasOthers() && mapData.hasTyome()) {
			this.setVariableStroke(g, this.mapPreferences.getJRPreferences().getWidth() + 4, isTransform, zoom);
			for (final ArcData arc : mapData.getOthers().values()) {
				if (arc.getClassification() == ArcData.TYPE_RAILWAY) {
					if (arc.getRailwayType() == ArcData.RAILWAY_JR) {
						g.setColor(this.mapPreferences.getJRPreferences().getBorderColor());
						this.draw(g, arc.getPath(), isTransform, transform);
					} else if (arc.getRailwayType() == ArcData.RAILWAY_JR_SHINKANSEN) {
						g.setColor(this.mapPreferences.getJRShinkansenPreferences().getBorderColor());
						this.draw(g, arc.getPath(), isTransform, transform);
					}
				}
			}
			for (final ArcData arc : mapData.getOthers().values()) {
				if (arc.getClassification() == ArcData.TYPE_RAILWAY) {
					if (arc.getRailwayType() == ArcData.RAILWAY_JR) {
						if (isTransform) {
							g.setStroke(new BasicStroke(this.mapPreferences.getJRPreferences().getWidth(),
									BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { this.mapPreferences
											.getJRPreferences().getWidth() * 5 }, 0));
						} else {
							g
									.setStroke(new BasicStroke((float) (this.mapPreferences.getJRPreferences()
											.getWidth() * this.zoom), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
											(float) (10 * this.zoom), new float[] { (float) (this.mapPreferences
													.getJRPreferences().getWidth() * 5 * this.zoom) }, 0));
						}
						g.setColor(this.mapPreferences.getJRPreferences().getFillColor());
						this.draw(g, arc.getPath(), isTransform, transform);
					} else if (arc.getRailwayType() == ArcData.RAILWAY_JR_SHINKANSEN) {
						if (isTransform) {
							g.setStroke(new BasicStroke(this.mapPreferences.getJRShinkansenPreferences().getWidth(),
									BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { this.mapPreferences
											.getJRPreferences().getWidth() * 15 }, 0));
						} else {
							g.setStroke(new BasicStroke((float) (this.mapPreferences.getJRShinkansenPreferences()
									.getWidth() * this.zoom), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
									(float) (10 * this.zoom), new float[] { (float) (this.mapPreferences
											.getJRPreferences().getWidth() * 15 * this.zoom) }, 0));
						}
						g.setColor(this.mapPreferences.getJRShinkansenPreferences().getFillColor());
						this.draw(g, arc.getPath(), isTransform, transform);
					} else {
						this.setVariableStroke(g, this.mapPreferences.getRailwayPreferences().getWidth(), isTransform,
								zoom);
						g.setColor(this.mapPreferences.getRailwayPreferences().getBorderColor());
						this.draw(g, arc.getPath(), isTransform, transform);
					}
				}
			}
		}
	}

	/**
	 * 鉄道の属性を描画します。
	 * @param g 描画対象
	 * @param mapData 地図
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawRailwayLabel(final Graphics2D g, final MapData mapData, final Rectangle2D visibleRectangle,
			final double zoom, final double offsetX, final double offsetY) throws FileNotFoundException, IOException {
		if (mapData.hasOthers()) {
			final Font railFont = this.mapPreferences.getRailwayPreferences().getFont();
			g.setFont(railFont);
			final FontMetrics metrics = this.getFontMetrics(railFont);
			final int descent = metrics.getDescent();
			g.setColor(this.mapPreferences.getRailwayPreferences().getAttributeColor());
			for (final ArcData arc : mapData.getOthers().values()) {
				if (arc.getAttribute() != null) {
					if (arc.getAttributeX() != 0 && arc.getAttributeY() != 0) {
						g.drawString(arc.getAttribute(), (float) ((arc.getAttributeX() * zoom) - offsetX),
								(float) ((arc.getAttributeY() * zoom) - offsetY - descent));
					}
				}
			}
		}
	}

	/**
	 * 道路の輪郭部分を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @param zoom 表示倍率
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawRoad(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData, final double zoom) throws FileNotFoundException, IOException {
		if (mapData.hasRoadArc() && mapData.hasTyome()) {
			for (final ArcData arc : mapData.getRoadArc().values()) {
				if (arc.getRoadType() == ArcData.ROAD_HIGHWAY) {
					this.setVariableFatStroke(g, this.mapPreferences.getHighwayPreferences().getWidth(), isTransform,
							zoom);
					g.setColor(this.mapPreferences.getHighwayPreferences().getBorderColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				} else if (arc.getRoadType() == ArcData.ROAD_KOKUDO) {
					this.setVariableFatStroke(g, this.mapPreferences.getKokudoPreferences().getWidth(), isTransform,
							zoom);
					g.setColor(this.mapPreferences.getKokudoPreferences().getBorderColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				} else if (arc.getRoadType() == ArcData.ROAD_KENDO) {
					this.setVariableFatStroke(g, this.mapPreferences.getKendoPreferences().getWidth(), isTransform,
							zoom);
					g.setColor(this.mapPreferences.getKendoPreferences().getBorderColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				} else if (arc.getRoadType() == ArcData.ROAD_CHIHODO) {
					this.setVariableFatStroke(g, this.mapPreferences.getChihodoPreferences().getWidth(), isTransform,
							zoom);
					g.setColor(this.mapPreferences.getChihodoPreferences().getBorderColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				} else if (arc.getRoadType() == ArcData.ROAD_MAJOR) {
					this.setVariableFatStroke(g, this.mapPreferences.getMajorRoadPreferences().getWidth(), isTransform,
							zoom);
					g.setColor(this.mapPreferences.getMajorRoadPreferences().getBorderColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				} else {
					this.setVariableFatStroke(g, this.mapPreferences.getNormalRoadPreferences().getWidth(),
							isTransform, zoom);
					g.setColor(this.mapPreferences.getNormalRoadPreferences().getBorderColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
		}
	}

	/**
	 * 道路の属性を描画します。
	 * @param g 描画対象
	 * @param mapData 地図
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawRoadLabel(final Graphics2D g, final MapData mapData, final Rectangle2D visibleRectangle,
			final double zoom, final double offsetX, final double offsetY) throws FileNotFoundException, IOException {
		final Font roadFont = this.mapPreferences.getNormalRoadPreferences().getFont();
		if (mapData.hasRoadArc()) {
			g.setFont(roadFont);
			final FontMetrics metrics = this.getFontMetrics(roadFont);
			final int descent = metrics.getDescent();
			g.setColor(this.mapPreferences.getNormalRoadPreferences().getAttributeColor());
			for (final ArcData arc : mapData.getRoadArc().values()) {
				if (arc.getAttribute() != null) {
					g.drawString(arc.getAttribute(), (float) ((arc.getAttributeX() * zoom) - offsetX), (float) ((arc
							.getAttributeY() * zoom)
							- offsetY - descent));
				}
			}
		}
		// 高速道路、国道のラベルを描画する
		if (!mapData.hasRoadArc() && mapData.hasLargeRoadArc()) {
			g.setFont(roadFont);
			final FontMetrics metrics = this.getFontMetrics(roadFont);
			final int descent = metrics.getDescent();
			g.setColor(this.mapPreferences.getNormalRoadPreferences().getAttributeColor());
			for (final ArcData arc : mapData.getLargeRoadArc().values()) {
				if (arc.getAttribute() != null) {
					g.drawString(arc.getAttribute(), (float) ((arc.getAttributeX() * zoom) - offsetX), (float) ((arc
							.getAttributeY() * zoom)
							- offsetY - descent));
				}
			}
		}
	}

	/**
	 * 数値地図2500を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param x 描画領域の左端のx座標（仮想座標）
	 * @param y 描画領域の上端のy座標（仮想座標）
	 * @param w 描画領域の幅（仮想座標）
	 * @param h 描画領域の高さ（仮想座標）
	 * @param center 中心の座標（仮想座標）
	 * @param transform 座標変換
	 * @param zoom 表示倍率
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawSdf(final Graphics2D g, final boolean isTransform, final double x, final double y, final double w,
			final double h, final Point2D center, final AffineTransform transform, final double zoom)
			throws FileNotFoundException, IOException {
		if (this.maps != null & this.zoom >= Const.Zoom.LOAD_GYOUSEI) {
			g.setStroke(new BasicStroke(1f));
			// 海を描画する
			this.drawSeas(g, isTransform, transform, x, y, w, h);
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					// 丁目を描画する
					this.fillTyome(g, isTransform, transform, center, mapData);
				}
			}
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					// 場地を描画する
					this.fillZyouti(g, isTransform, transform, mapData);
					// 内水面を描画する
					this.fillMizu(g, isTransform, transform, mapData);
				}
			}
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					// 建物を描画する
					this.fillTatemono(g, isTransform, transform, mapData);
				}
			}
			this.setVariableStroke(g, this.mapPreferences.getZyoutiPreferences().getWidth(), isTransform, zoom);
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					// 場地界を描画する
					this.drawZyouti(g, isTransform, transform, mapData);
					// 内水面界を描画する
					this.drawMizu(g, isTransform, transform, mapData);
					// 建物界を描画する
					this.drawTatemono(g, isTransform, transform, mapData);
				}
			}
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					// 道路の輪郭を描画する
					this.drawRoad(g, isTransform, transform, mapData, zoom);
				}
			}
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					// 道路の塗りつぶし部を描画する
					this.fillRoad(g, isTransform, transform, mapData, zoom);
				}
			}
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					// 行政界を描画する
					this.drawGyousei(g, isTransform, transform, mapData, zoom);
				}
			}
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					// 鉄道を描画する
					this.drawRailway(g, isTransform, transform, mapData, zoom);
				}
			}
			// 丁目の情報がないときは、鉄道、駅、高速道路、国道、市区町村界を描画する
			for (final MapData mapData : this.maps.values()) {
				if (mapData.getBounds().intersects(x, y, w, h)) {
					if (!mapData.hasRoadArc()) {
						this.drawLargeRoad(g, isTransform, transform, mapData, zoom);
					}
					if (!mapData.hasTyome()) {
						this.drawLargeRailway(g, isTransform, transform, mapData, zoom);
					}
					// for debug
					//						g2.setColor(this.mapPreferences.getMapBoundsColor());
					//						g2.setStroke(new BasicStroke(1f));
					//						g2.draw(mapData.getBounds());
					//						g2.setFont(new Font("default", Font.PLAIN, 300));
					//						g2.drawString(mapData.getMapName(), (float) mapData.getBounds().getBounds().getX(), (float) mapData.getBounds().getBounds().getMaxY());
				}
			}
		}
	}

	/**
	 * 海を塗りつぶします。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param x 描画領域の左端のx座標（仮想座標）
	 * @param y 描画領域の上端のy座標（仮想座標）
	 * @param w 描画領域の幅（仮想座標）
	 * @param h 描画領域の高さ（仮想座標）
	 */
	private void drawSeas(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final double x, final double y, final double w, final double h) {
		for (final MapData mapData : this.maps.values()) {
			if (mapData.getBounds().intersects(x, y, w, h)) {
				if (mapData.hasTyome()) {
					g.setColor(this.mapPreferences.getMizuPreferences().getFillColor());
					this.fill(g, mapData.getBounds(), isTransform, transform);
					this.draw(g, mapData.getBounds(), isTransform, transform);
				}
			}
		}
	}

	/**
	 * 店舗を描画します。
	 * @param g 描画対象
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void drawShops(final Graphics2D g, final Rectangle2D visibleRectangle, final double zoom,
			final double offsetX, final double offsetY) throws UnsupportedEncodingException, IOException {
		final Font font = this.mapPreferences.getTatemonoPreferences().getFont();
		final int descent = this.getFontMetrics(font).getDescent();
		final double pointSize = 4;
		g.setFont(font);
		g.setColor(this.mapPreferences.getTatemonoPreferences().getAttributeColor());
		if (this.prefectures != null) {
			for (final Prefecture prefecture : this.prefectures) {
				if (prefecture.hasCities()) {
					for (final City city : prefecture.getCities()) {
						if ((city.hasFineShape() ? city.getFineShape() : city.getShape()).intersects(visibleRectangle)) {
							if (city.hasShops()) {
								for (final PointData point : city.getShops()) {
									if (point.getAttribute() != null) {
										if (visibleRectangle.contains(point.getAttributeX(), point.getAttributeY())) {
											g.fill(new Ellipse2D.Double((point.getX() * zoom) - offsetX
													- (pointSize / 2), (point.getY() * zoom) - offsetY
													- (pointSize / 2), pointSize, pointSize));
											g.drawString(point.getAttribute(),
													(float) ((point.getAttributeX() * zoom) - offsetX), (float) ((point
															.getAttributeY() * zoom)
															- offsetY - descent));
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 建物の輪郭を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawTatemono(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData) throws FileNotFoundException, IOException {
		if (mapData.hasTatemonoArc()) {
			for (final ArcData arc : mapData.getTatemonoArc().values()) {
				if (arc.getTag() == ArcData.TAG_NORMAL) {
					g.setColor(this.mapPreferences.getTatemonoPreferences().getBorderColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
		}
	}

	/**
	 * 建物の属性を描画します。
	 * @param g 描画対象
	 * @param mapData 地図
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawTatemonoLabel(final Graphics2D g, final MapData mapData, final Rectangle2D visibleRectangle,
			final double zoom, final double offsetX, final double offsetY) throws FileNotFoundException, IOException {
		if (mapData.hasTatemono()) {
			g.setColor(this.mapPreferences.getTatemonoPreferences().getAttributeColor());
			final Font tatemonoFont = this.mapPreferences.getTatemonoPreferences().getFont();
			g.setFont(tatemonoFont);
			final FontMetrics metrics = this.getFontMetrics(tatemonoFont);
			final int descent = metrics.getDescent();
			final double pointSize = 4;
			for (final PolygonData polygon : mapData.getTatemono().values()) {
				if (polygon.getAttribute() != null) {
					if (visibleRectangle.contains(polygon.getAttributeX(), polygon.getAttributeY())) {
						g.fill(new Ellipse2D.Double((polygon.getX() * zoom) - offsetX - (pointSize / 2), (polygon
								.getY() * zoom)
								- offsetY - (pointSize / 2), pointSize, pointSize));
						g.drawString(polygon.getAttribute(), (float) ((polygon.getAttributeX() * zoom) - offsetX),
								(float) ((polygon.getAttributeY() * zoom) - offsetY - descent));
					}
				}
			}
		}
	}

	/**
	 * 町丁目の属性を描画します。
	 * @param g 描画対象
	 * @param mapData 地図
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawTyomeLabel(final Graphics2D g, final MapData mapData, final Rectangle2D visibleRectangle,
			final double zoom, final double offsetX, final double offsetY) throws FileNotFoundException, IOException {
		if (mapData.hasTyome()) {
			g.setColor(this.mapPreferences.getTyomePreferences().getAttributeColor());
			for (final PolygonData polygon : mapData.getTyome().values()) {
				final Font tyomeFont = polygon.getTyomeFont();
				if (tyomeFont != null) {
					g.setFont(tyomeFont);
					final FontMetrics metrics = this.getFontMetrics(tyomeFont);
					final int descent = metrics.getDescent();
					if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_TYOME) {
						if (polygon.getAttribute() != null) {
							if (visibleRectangle.contains(polygon.getAttributeX(), polygon.getAttributeY())) {
								g.drawString(polygon.getAttribute(),
										(float) ((polygon.getAttributeX() * zoom) - offsetX), (float) ((polygon
												.getAttributeY() * zoom)
												- offsetY - descent));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 町丁目の読みを描画します。
	 * @param g 描画対象
	 * @param mapData 地図
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawTyomeYomi(final Graphics2D g, final MapData mapData, final Rectangle2D visibleRectangle,
			final double zoom, final double offsetX, final double offsetY) throws FileNotFoundException, IOException {
		if (zoom >= Const.Zoom.LOAD_ALL) {
			if (mapData.hasTyome()) {
				g.setColor(this.mapPreferences.getTyomePreferences().getAttributeColor());
				final Font yomiFont = this.mapPreferences.getTyomePreferences().getFont();
				g.setFont(yomiFont);
				final int descent = g.getFontMetrics().getDescent();
				for (final PolygonData polygon : mapData.getTyome().values()) {
					if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_TYOME) {
						if (polygon.getAttribute() != null) {
							if (visibleRectangle.contains(polygon.getAttributeX(), polygon.getAttributeY())) {
								if (polygon.hasYomi()) {
									g.drawString(polygon.getYomi(), (float) (polygon.getYomiX() * zoom - offsetX),
											(float) (polygon.getYomiY() * zoom - offsetY) - descent);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 場地界を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void drawZyouti(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData) throws FileNotFoundException, IOException {
		if (mapData.hasZyouti()) {
			for (final ArcData arc : mapData.getOthers().values()) {
				if (arc.getClassification() != ArcData.TYPE_RAILWAY) {
					if (arc.getTag() == ArcData.TAG_NORMAL) {
						g.setColor(this.mapPreferences.getZyoutiPreferences().getBorderColor());
						this.draw(g, arc.getPath(), isTransform, transform);
					}
				}
			}
		}
	}

	/**
	 * 場地の属性を描画します。
	 * @param g 描画対象
	 * @param mapData 地図
	 * @param visibleRectangle 実際に表示されている範囲（仮想座標）
	 * @param zoom 表示倍率
	 * @param offsetX x軸方向のオフセット（実座標）
	 * @param offsetY y軸方向のオフセット（実座標）
	 * @throws IOException
	 */
	private void drawZyoutiLabel(final Graphics2D g, final MapData mapData, final Rectangle2D visibleRectangle,
			final double zoom, final double offsetX, final double offsetY) throws IOException {
		if (mapData.hasZyouti()) {
			g.setColor(this.mapPreferences.getZyoutiPreferences().getAttributeColor());
			final Font zyoutiFont = this.mapPreferences.getZyoutiPreferences().getFont();
			g.setFont(zyoutiFont);
			final FontMetrics metrics = this.getFontMetrics(zyoutiFont);
			final int descent = metrics.getDescent();
			for (final PolygonData polygon : mapData.getZyouti().values()) {
				if (polygon.getAttribute() != null) {
					if (visibleRectangle.contains(polygon.getAttributeX(), polygon.getAttributeY())) {
						g.drawString(polygon.getAttribute(), (float) ((polygon.getAttributeX() * zoom) - offsetX),
								(float) ((polygon.getAttributeY() * zoom) - offsetY - descent));
					}
				}
			}
		}
	}

	/**
	 * 図形を塗りつぶします。
	 * @param g 描画対象
	 * @param shape 描画するオブジェクト
	 * @param isTransform 描画対象全体を座標変換するかどうか
	 * @param transform 座標変換
	 */
	private void fill(final Graphics2D g, final Shape shape, final boolean isTransform, final AffineTransform transform) {
		if (isTransform) {
			g.fill(shape);
		} else {
			g.fill(transform.createTransformedShape(shape));
		}
	}

	/**
	 * 内水面を塗りつぶします。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void fillMizu(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData) throws FileNotFoundException, IOException {
		if (mapData.hasMizu()) {
			for (final PolygonData polygon : mapData.getMizu().values()) {
				if (polygon.getArea() != null) {
					g.setColor(this.mapPreferences.getMizuPreferences().getFillColor());
					this.fill(g, polygon.getArea(), isTransform, transform);
					this.draw(g, polygon.getArea(), isTransform, transform);
				}
			}
		}
	}

	/**
	 * 道路の塗りつぶし部分を描画します。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @param zoom 表示倍率
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void fillRoad(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData, final double zoom) throws FileNotFoundException, IOException {
		if (mapData.hasRoadArc() && mapData.hasTyome()) {
			// 一般の道路を描画する
			for (final ArcData arc : mapData.getRoadArc().values()) {
				if (arc.getRoadType() == ArcData.ROAD_NORMAL) {
					g.setColor(this.mapPreferences.getNormalRoadPreferences().getFillColor());
					this.setVariableStroke(g, this.mapPreferences.getNormalRoadPreferences().getWidth(), isTransform,
							zoom);
					this.draw(g, arc.getPath(), isTransform, transform);
				} else if (arc.getRoadType() == ArcData.ROAD_MAJOR) {
					g.setColor(this.mapPreferences.getMajorRoadPreferences().getFillColor());
					this.setVariableStroke(g, this.mapPreferences.getMajorRoadPreferences().getWidth(), isTransform,
							zoom);
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
			// 主要地方道、県道を描画する
			for (final ArcData arc : mapData.getRoadArc().values()) {
				if (arc.getRoadType() == ArcData.ROAD_KENDO) {
					this.setVariableStroke(g, this.mapPreferences.getKendoPreferences().getWidth(), isTransform, zoom);
					g.setColor(this.mapPreferences.getKendoPreferences().getFillColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				} else if (arc.getRoadType() == ArcData.ROAD_CHIHODO) {
					this
							.setVariableStroke(g, this.mapPreferences.getChihodoPreferences().getWidth(), isTransform,
									zoom);
					g.setColor(this.mapPreferences.getChihodoPreferences().getFillColor());
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
			// 国道を描画する
			this.setVariableStroke(g, this.mapPreferences.getKokudoPreferences().getWidth(), isTransform, zoom);
			g.setColor(this.mapPreferences.getKokudoPreferences().getFillColor());
			for (final ArcData arc : mapData.getRoadArc().values()) {
				if (arc.getRoadType() == ArcData.ROAD_KOKUDO) {
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
			// 高速道路を描画する
			this.setVariableStroke(g, this.mapPreferences.getHighwayPreferences().getWidth(), isTransform, zoom);
			g.setColor(this.mapPreferences.getHighwayPreferences().getFillColor());
			for (final ArcData arc : mapData.getRoadArc().values()) {
				if (arc.getRoadType() == ArcData.ROAD_HIGHWAY) {
					this.draw(g, arc.getPath(), isTransform, transform);
				}
			}
		}
	}

	/**
	 * 建物を塗りつぶします。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void fillTatemono(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData) throws FileNotFoundException, IOException {
		if (mapData.hasTatemono()) {
			for (final PolygonData polygon : mapData.getTatemono().values()) {
				if (polygon.getArea() != null) {
					g.setColor(this.mapPreferences.getTatemonoPreferences().getFillColor());
					this.fill(g, polygon.getArea(), isTransform, transform);
				}
			}
		}
	}

	/**
	 * 町丁目を塗りつぶします
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @param center 中心の座標（仮想座標）
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void fillTyome(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final Point2D center, final MapData mapData) throws FileNotFoundException, IOException {
		if (mapData.hasTyome()) {
			for (final PolygonData polygon : mapData.getTyome().values()) {
				if (polygon.getArea() != null) {
					if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_TYOME) {
						final Color color = this.mapPreferences.getTyomeFillColor(polygon.getTyomeColorIndex());
						if (this.saturationDifference == 0) {
							g.setColor(color);
						} else {
							final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(),
									new float[] { 0, 0, 0 });
							hsb[1] = Math.min(1, Math.max(0, hsb[1] + this.saturationDifference));
							g.setColor(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
						}
						this.fill(g, polygon.getArea(), isTransform, transform);
						// since 4.06
						if (polygon.getArea().contains(center)) {
							this.centerTyome = polygon.getAttribute() + "付近";
						}
					}
				}
			}
		}
	}

	/**
	 * 場地を塗りつぶします。
	 * @param g 描画対象
	 * @param isTransform 描画対象を座標変換するかどうか
	 * @param transform 座標変換
	 * @param mapData 地図
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void fillZyouti(final Graphics2D g, final boolean isTransform, final AffineTransform transform,
			final MapData mapData) throws FileNotFoundException, IOException {
		if (mapData.hasZyouti()) {
			for (final PolygonData polygon : mapData.getZyouti().values()) {
				if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_RAILROAD) {
					g.setColor(this.mapPreferences.getZyoutiPreferences().getFillColor());
					this.fill(g, polygon.getArea(), isTransform, transform);
				} else if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_PARK) {
					g.setColor(this.mapPreferences.getParkPreferences().getFillColor());
					this.fill(g, polygon.getArea(), isTransform, transform);
				} else if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_SCHOOL) {
					g.setColor(this.mapPreferences.getZyoutiPreferences().getFillColor());
					this.fill(g, polygon.getArea(), isTransform, transform);
				} else if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_TEMPLE) {
					g.setColor(this.mapPreferences.getZyoutiPreferences().getFillColor());
					this.fill(g, polygon.getArea(), isTransform, transform);
				} else if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_GRAVEYARD) {
					g.setColor(this.mapPreferences.getZyoutiPreferences().getFillColor());
					this.fill(g, polygon.getArea(), isTransform, transform);
				} else if (polygon.getClassificationCode() == PolygonData.CLASSIFICATION_OTHER) {
					g.setColor(this.mapPreferences.getZyoutiPreferences().getFillColor());
					this.fill(g, polygon.getArea(), isTransform, transform);
				} else {
					System.out.println(this.getClass().getName() + ": unknown classification code "
							+ polygon.getClassificationCode());
				}
			}
		}
	}

	/**
	 * 強制的に再描画します。
	 */
	public synchronized void forceRepaint() {
		final Graphics g = MapPanel.this.getGraphics();
		MapPanel.this.paintComponent(g);
		g.dispose();
	}

	/**
	 * @return 背景スレッドに再計算を要求するためのアクションリスナ
	 */
	public ActionListener getActionListener() {
		return this.listener;
	}

	/**
	 * 指定された地図全てが収まる長方形を求めます。
	 * @param mapNames 地図の一覧
	 * @return 長方形
	 * @since 3.03
	 */
	public Rectangle2D getBounds(final Collection<String> mapNames) {
		Rectangle2D ret = null;
		for (final String mapName : mapNames) {
			if (this.maps.containsKey(mapName)) {
				final Rectangle bounds = this.maps.get(mapName).getBounds().getBounds();
				if (ret == null) {
					ret = bounds;
				} else {
					ret = ret.createUnion(bounds);
				}
			}
		}
		return ret;
	}

	/**
	 * @return 文字の大きさ
	 */
	public double getFontZoom() {
		return this.fontZoom;
	}

	/**
	 * @return 地図の設定
	 */
	public MapPreferences getMapPreferences() {
		return this.mapPreferences;
	}

	/**
	 * オブジェクトが存在する最も大きい x 座標を取得します。
	 * @return オブジェクトが存在する最も大きい x 座標
	 */
	double getMaxX() {
		return this.maxX;
	}

	/**
	 * オブジェクトが存在する最も大きい y 座標を取得します。
	 * @return オブジェクトが存在する最も大きい y 座標
	 */
	double getMaxY() {
		return this.maxY;
	}

	/**
	 * @return メッセージ
	 */
	public String getMessage() {
		if (this.messages.size() > 0) {
			return this.messages.peek();
		} else {
			return this.centerPrefectureCity + this.centerTyome + " ";
		}
	}

	/**
	 * オブジェクトが存在する最も小さい x 座標を取得します。
	 * @return オブジェクトが存在する最も小さい x 座標
	 */
	double getMinX() {
		return this.minX;
	}

	/** オブジェクトが存在する最も小さい y 座標を取得します。
	 * @return オブジェクトが存在する最も小さい x 座標
	 */
	double getMinY() {
		return this.minY;
	}

	/**
	 * @return 表示倍率の最小値
	 */
	public double getMinZoom() {
		return this.minZoom;
	}

	/**
	 * オブジェクトが存在する範囲を取得します。
	 * @return オブジェクトが存在する範囲（仮想座標）
	 */
	Rectangle2D getObjectArea() {
		return new Rectangle2D.Double(this.minX, this.minY, this.maxX - this.minX, this.maxY - this.minY);
	}

	/**
	 * @return x方向のオフセット
	 */
	public double getOffsetX() {
		return this.offsetX;
	}

	/**
	 * @return y方向のオフセット
	 */
	public double getOffsetY() {
		return this.offsetY;
	}

	/**
	 * @return 都道府県の一覧
	 */
	public Collection<Prefecture> getPrefectures() {
		return this.prefectures;
	}

	/**
	 * @return 地図を検索するためのデータ構造
	 */
	public Search getSearch() {
		return this.search;
	}

	/**
	 * 表示されている範囲を取得します。
	 * @param isTight 正確な表示範囲を取得するかどうか
	 * @return 表示されている範囲（仮想座標）
	 */
	public Rectangle2D getVisibleRectangle(final boolean isTight) {
		if (this.zoom < Const.BitmapCache.ZOOM || isTight) {
			final int width = (this.size == null) ? this.getWidth() : this.size.width;
			final int height = (this.size == null) ? this.getHeight() : this.size.height;
			return new Rectangle2D.Double(this.offsetX / this.zoom, this.offsetY / this.zoom, width / this.zoom, height
					/ this.zoom);
		} else {
			final int width = (this.size == null) ? this.getWidth() : this.size.width;
			final int height = (this.size == null) ? this.getHeight() : this.size.height;
			final int x1 = (int) (Math.floor(MapPanel.this.offsetX / Const.BitmapCache.WIDTH))
					* Const.BitmapCache.WIDTH;
			final int y1 = (int) (Math.floor(MapPanel.this.offsetY / Const.BitmapCache.HEIGHT))
					* Const.BitmapCache.HEIGHT;
			final int x2 = (int) (Math.floor((MapPanel.this.offsetX + width) / Const.BitmapCache.WIDTH))
					* Const.BitmapCache.WIDTH;
			final int y2 = (int) (Math.floor((MapPanel.this.offsetY + height) / Const.BitmapCache.HEIGHT))
					* Const.BitmapCache.HEIGHT;
			return new Rectangle2D.Double(x1 / this.zoom, y1 / this.zoom, (x2 - x1 + Const.BitmapCache.WIDTH)
					/ this.zoom, (y2 - y1 + Const.BitmapCache.HEIGHT) / this.zoom);
		}
	}

	/**
	 * 倍率を取得します。
	 * @return 倍率
	 */
	public double getZoom() {
		return this.zoom;
	}

	/**
	 * 文字を大きくします。
	 */
	public void increaseFontSize() {
		this.fontZoom /= 0.9;
		this.mapPreferences.setFontZoom(this.fontZoom);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "increment font size"));
		}
		this.isChanged = true;
	}

	/**
	 * 彩度を増やします。
	 */
	public void increaseSaturation() {
		this.saturationDifference += 0.05;
		this.isChanged = true;
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom fine cities"));
		}
	}

	/**
	 * 指定したオブジェクトが表示エリア内にあるかどうかを取得します。
	 * @param shape オブジェクト
	 * @return 指定したオブジェクトが表示エリア内にあるかどうか
	 */
	boolean isVisible(final Shape shape) {
		return shape.intersects(this.getVisibleRectangle(false));
	}

	/**
	 * 町丁目の読みを調べます。
	 * @throws IOException
	 */
	public void loadYomi() throws IOException {
		if (this.getZoom() >= Const.Zoom.LOAD_ALL) {
			final Map<String, ZipCode> zipCodes = new ConcurrentHashMap<String, ZipCode>();
			for (final Prefecture prefecture : this.prefectures) {
				if (prefecture.hasFine()) {
					if (this.isVisible(prefecture.getFineShape())) {
						for (final City city : prefecture.getCities()) {
							if (city.hasFineShape()) {
								if (this.isVisible(city.getFineShape())) {
									if (!zipCodes.containsKey(city.getId())) {
										zipCodes.put(city.getId(), new ZipCode(city.getId()));
									}
								}
							}
						}
					}
				}
			}
			for (final MapData map : this.maps.values()) {
				if (map.hasTyome()) {
					for (final ZipCode zipCode : zipCodes.values()) {
						for (final PolygonData polygon : map.getTyome().values()) {
							if (!polygon.hasYomi()) {
								final String attribute = polygon.getAttribute().replaceFirst("[0-9０-９]+$", "");
								final String yomi = zipCode.getYomi(attribute);
								if (yomi != null) {
									polygon.setYomi(yomi);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 指定した点が画面の中央になるように、地図をスクロールさせます。
	 * @param x x座標（仮想座標）
	 * @param y y座標（仮想座標）
	 */
	public void moveTo(final double x, final double y) {
		final int width = (this.size == null) ? this.getWidth() : this.size.width;
		final int height = (this.size == null) ? this.getHeight() : this.size.height;
		this.offsetX = x * this.zoom - width / 2;
		this.offsetY = y * this.zoom - height / 2;
		this.isChanged = true;
	}

	/**
	 * 指定された長方形が表示されるように表示倍率を変更し、表示位置を移動します。
	 * @param rectangle 長方形
	 * @since 3.03
	 */
	public void moveTo(final Rectangle2D rectangle) {
		final int width = (this.size == null) ? this.getWidth() : this.size.width;
		final int height = (this.size == null) ? this.getHeight() : this.size.height;
		final double zoomX = width / rectangle.getWidth();
		final double zoomY = height / rectangle.getHeight();
		if (zoomY < zoomX) {
			this.zoom = zoomY;
		} else {
			this.zoom = zoomX;
		}
		this.moveTo(rectangle.getCenterX(), rectangle.getCenterY());
	}

	/**
	 * 明石市に移動します。
	 */
	public void moveToAkashi() {
		this.zoom = 0.36;
		this.offsetX = 23000;
		this.offsetY = -1382500;
	}

	/**
	 * 地図の中央が画面の中央になるように、地図をスクロールさせます。
	 */
	public void moveToCenter() {
		final int width = (this.size == null) ? this.getWidth() : this.size.width;
		final int height = (this.size == null) ? this.getHeight() : this.size.height;
		this.offsetX = ((this.minX + this.maxX) / 2 * this.zoom) - width / 2;
		this.offsetY = ((this.minY + this.maxY) / 2 * this.zoom) - height / 2;
		this.isChanged = true;
	}

	@Override
	public void paintComponent(final Graphics g) {
		if (this.prefectures == null) {
			super.paintComponent(g);
		}
		try {
			if (this.zoom < Const.BitmapCache.ZOOM) {
				if (this.isChanged) {
					final Image tempImage = this.createImage(this.getWidth(), this.getHeight());
					final Graphics2D g2 = (Graphics2D) tempImage.getGraphics();
					if (this.isAntialias) {
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						if (Const.Fonts.HAS_MS_FONTS) {
							g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
									RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
						}
					}
					this.drawMap(g2, true);
					g.drawImage(tempImage, 0, 0, this);
					this.image = tempImage;
					this.isChanged = false;
				} else {
					g.drawImage(this.image, 0, 0, this);
				}
			} else {
				Progress.getInstance().setRepaintProgress(0);
				final int width = MapPanel.this.size == null ? MapPanel.this.getWidth() : MapPanel.this.size.width;
				final int height = MapPanel.this.size == null ? MapPanel.this.getHeight() : MapPanel.this.size.height;
				final Image image = this.createImage(width, height);
				final Graphics2D g2 = (Graphics2D) image.getGraphics();
				new File(Const.BitmapCache.CACHE_DIR).mkdirs();
				final Area clip = new Area();
				final Rectangle2D visibleRectangle = this.getVisibleRectangle(true);
				final double zoom = this.getZoom();
				final double offsetX = this.getOffsetX();
				final double offsetY = this.getOffsetY();
				final double saturationDifference = this.getSaturationDifference();
				Progress.getInstance().setRepaintProgress(20);
				for (int y = (int) (Math.floor(offsetY / Const.BitmapCache.HEIGHT)) * Const.BitmapCache.HEIGHT; y
						- offsetY < height; y += Const.BitmapCache.HEIGHT) {
					for (int x = (int) (Math.floor(offsetX / Const.BitmapCache.WIDTH)) * Const.BitmapCache.WIDTH; x
							- offsetX < width; x += Const.BitmapCache.WIDTH) {
						final File file = new File(new Formatter().format("%s%s%d_%d_%f_%f_%d_%d.png",
								Const.BitmapCache.CACHE_DIR + File.separator, Const.BitmapCache.PREFIX,
								Const.BitmapCache.WIDTH, Const.BitmapCache.HEIGHT, saturationDifference, zoom, x, y)
								.toString());
						if (file.exists()) {
							final Image image2 = ImageIO.read(file);
							g2.drawImage(image2, x - (int) offsetX, y - (int) offsetY, this);
						} else {
							clip.add(new Area(new Rectangle2D.Double(x - (int) offsetX, y - (int) offsetY,
									Const.BitmapCache.WIDTH, Const.BitmapCache.HEIGHT)));
						}
					}
				}
				Progress.getInstance().setRepaintProgress(40);
				if (!clip.isEmpty()) {
					final Shape originalClip = g2.getClip();
					g2.clip(clip);
					this.drawBackground(g2, true);
					g2.setClip(originalClip);
				}
				Progress.getInstance().setRepaintProgress(60);
				if (this.prefectures != null) {
					new FixAttributeLocation().fixAttributeLocation(this.maps, this.prefectures, this);
				}
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (Const.Fonts.HAS_MS_FONTS) {
					g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
				}
				Progress.getInstance().setRepaintProgress(80);
				this.drawLabels(g2, visibleRectangle, zoom, offsetX, offsetY);
				g2.dispose();
				g.drawImage(image, 0, 0, this);
				Progress.getInstance().setRepaintProgress(100);
			}
		} catch (final IndexOutOfBoundsException e) {
			// 読み込もうとしたビットマップキャッシュが保存中だったとき
			this.isChanged = true;
		} catch (final IIOException e) {
			// 読み込もうとしたビットマップキャッシュが保存中だったとき
			this.isChanged = true;
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 現在表示されている地図をダイアログを表示して印刷します。
	 * @throws PrintException 印刷例外
	 */
	public void print() throws PrintException {
		PrintUtil.print(this);
	}

	public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
		try {
			if (pageIndex == 0) {
				final Graphics2D g = (Graphics2D) graphics;
				g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
				final double newZoom = Math.min(pageFormat.getImageableWidth() / this.getWidth(), pageFormat
						.getImageableHeight()
						/ this.getHeight());
				g.scale(newZoom, newZoom);
				g.setClip(0, 0, this.getWidth(), this.getHeight());
				this.drawMap(g, true);
				return Printable.PAGE_EXISTS;
			} else {
				return Printable.NO_SUCH_PAGE;
			}
		} catch (final IOException e) {
			e.printStackTrace();
			return Printable.NO_SUCH_PAGE;
		}
	}

	/**
	 * 現在表示されている地図をPSファイルに出力します。
	 * @param file ファイル
	 * @throws IOException ファイル入出力例外
	 * @throws PrinterException 印刷例外
	 */
	public void printPS(final File file) throws PrinterException, IOException {
		PSOut.print(file, this);
	}

	/**
	 * 現在表示されている地図をラスタファイルに出力します。
	 * @param file ファイル
	 * @param format ファイル形式（png、jpg、bmp）
	 * @throws IOException 入出力例外 
	 */
	public void printRaster(final File file, final String format) throws IOException {
		final BufferedImage tempImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = (Graphics2D) tempImage.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (Const.Fonts.HAS_MS_FONTS) {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		this.drawMap(g2, true);
		ImageIO.write(tempImage, format, file);
	}

	/**
	 * SVGファイルを出力します。
	 * @param file ファイル
	 * @throws FileNotFoundException ファイル未検出例外
	 * @throws SVGGraphics2DIOException SVG関連入出力例外
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 */
	public void printSVG(final File file) throws UnsupportedEncodingException, SVGGraphics2DIOException,
			FileNotFoundException {
		SVGOut.print(file, new Paintable() {
			public Dimension getSize() {
				return MapPanel.this.getSize();
			}

			public void paint(final Graphics g) {
				try {
					g.clipRect(0, 0, MapPanel.this.getWidth(), MapPanel.this.getHeight());
					MapPanel.this.drawMap((Graphics2D) g, false);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * ステータスバーに表示されるメッセージをスタックから取り出して捨てます。
	 */
	public void removeMessage() {
		if (this.messages.size() > 0) {
			this.messages.pop();
		}
		this.statusBar.setText(this.getMessage());
	}

	/**
	 * 文字の大きさを標準に戻します。
	 */
	public void resetFontSize() {
		this.fontZoom = 1;
		this.mapPreferences.setFontZoom(this.fontZoom);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "reset font size"));
		}
		this.isChanged = true;
	}

	/**
	 * 彩度をリセットします。
	 */
	public void resetSaturation() {
		this.saturationDifference = 0;
		this.isChanged = true;
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom fine cities"));
		}
	}

	/**
	 * スクロールします。
	 * @param dx x軸方向の移動距離（実座標）
	 * @param dy y軸方向の移動距離（実座標）
	 */
	public void scroll(final double dx, final double dy) {
		this.offsetX += dx;
		this.offsetY += dy;
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "move"));
		}
		this.isChanged = true;
	}

	/**
	 * @param listener 背景スレッドに再計算を要求するためのアクションリスナ
	 */
	public void setActionListener(final ActionListener listener) {
		this.listener = listener;
	}

	/**
	 * 地図が変化したことを伝えます。
	 */
	public void setChanged() {
		this.isChanged = true;
	}

	/**
	 * 表示倍率にかかわらず、一定の太さに設定します。
	 * @param g 描画対象
	 * @param strokeWidth 線の幅
	 * @param isTransform 描画対象全体を座標変換するかどうか
	 * @param zoom 表示倍率
	 */
	private void setFixedStroke(final Graphics2D g, final float strokeWidth, final boolean isTransform,
			final double zoom) {
		if (isTransform) {
			g.setStroke(new BasicStroke((float) (strokeWidth / zoom)));
		} else {
			g.setStroke(new BasicStroke(strokeWidth));
		}
	}

	/**
	 * @param minZoom 表示倍率の最小値
	 */
	public void setMinZoom(final double minZoom) {
		this.minZoom = minZoom;
	}

	/**
	 * @param search 地図を検索するためのデータ構造
	 */
	public void setSearch(final Search search) {
		this.search = search;
	}

	/**
	 * @param statusBar ステータスバー
	 */
	public void setStatusBar(final JLabel statusBar) {
		this.statusBar = statusBar;
	}

	/**
	 * SWT版ではパネルの大きさが取得できないので、強制的に設定します。
	 * @param size パネルの大きさ
	 */
	public void setSWTSize(final Dimension size) {
		this.size = size;
	}

	/**
	 * 表示倍率に応じて線の幅が変わるように、太めの線を設定します。
	 * @param g 描画対象
	 * @param strokeWidth 線の幅
	 * @param isTransform 描画対象全体を座標変換するかどうか
	 * @param zoom 表示倍率
	 */
	private void setVariableFatStroke(final Graphics2D g, final float strokeWidth, final boolean isTransform,
			final double zoom) {
		if (isTransform) {
			g.setStroke(new BasicStroke(strokeWidth + (float) (2 / zoom)));
		} else {
			g.setStroke(new BasicStroke((float) (strokeWidth * zoom) + 2));
		}
	}

	/**
	 * 表示倍率に応じて線の幅が変わるように設定します。
	 * @param g 描画対象
	 * @param strokeWidth 線の幅
	 * @param isTransform 描画対象全体を座標変換するかどうか
	 * @param zoom 表示倍率
	 */
	private void setVariableStroke(final Graphics2D g, final float strokeWidth, final boolean isTransform,
			final double zoom) {
		if (isTransform) {
			g.setStroke(new BasicStroke(strokeWidth));
		} else {
			g.setStroke(new BasicStroke((float) (strokeWidth * zoom)));
		}
	}

	/**
	 * 実座標を取得します。
	 * @param location 仮想座標
	 * @return 実座標
	 */
	Point2D toRealLocation(final Point2D location) {
		return new Point2D.Double((location.getX() * this.zoom) - this.offsetX, (location.getY() * this.zoom)
				- this.offsetY);
	}

	/**
	 * 仮想座標を取得します。
	 * @param location 実座標
	 * @return 仮想座標
	 */
	Point2D toVirtualLocation(final Point2D location) {
		return new Point2D.Double((this.offsetX + location.getX()) / this.zoom, (this.offsetY + location.getY())
				/ this.zoom);
	}

	/**
	 * 倍率を変更します。
	 * @param newZoom 倍率
	 * @param x 中心のx座標（実座標）
	 * @param y 中心のy座標（実座標）
	 */
	private void zoom(final double newZoom, final int x, final int y) {
		final double newX = ((this.offsetX + x) / this.zoom * newZoom) - x;
		final double newY = ((this.offsetY + y) / this.zoom * newZoom) - y;
		this.offsetX = newX;
		this.offsetY = newY;
		this.zoom = newZoom;
		this.isChanged = true;
	}

	/**
	 * 自動倍率設定します。
	 */
	public void zoomAutomaticaly() {
		final int width = (this.size == null) ? this.getWidth() : this.size.width;
		final int height = (this.size == null) ? this.getHeight() : this.size.height;
		final double zoomX = width / (this.maxX - this.minX);
		final double zoomY = height / (this.maxY - this.minY);
		if (zoomY < zoomX) {
			this.zoom = zoomY;
		} else {
			this.zoom = zoomX;
		}
		this.minZoom = this.zoom;
		this.isChanged = true;
	}

	/**
	 * 国土数値情報の荒い市区町村界を読み込む縮尺にします。
	 */
	public void zoomCities() {
		this.zoom(Const.Zoom.LOAD_CITIES, this.getWidth() / 2, this.getHeight() / 2);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom cities"));
		}
	}

	/**
	 * 詳細表示します。
	 */
	public void zoomDetail() {
		this.zoom(Const.Zoom.LOAD_ALL, this.getWidth() / 2, this.getHeight() / 2);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom detail"));
		}
	}

	/**
	 * 国土数値情報の細かい市区町村界を読み込む縮尺にします。
	 */
	public void zoomFineCities() {
		this.zoom(Const.Zoom.LOAD_FINE_CITIES, this.getWidth() / 2, this.getHeight() / 2);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom fine cities"));
		}
	}

	/**
	 * 拡大します。
	 */
	public void zoomIn() {
		this.doWheelRotation(1, this.getWidth() / 2, this.getHeight() / 2);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom in"));
		}
	}

	/**
	 * 中域表示します。
	 */
	public void zoomMiddle() {
		this.zoom(Const.Zoom.LOAD_GYOUSEI, this.getWidth() / 2, this.getHeight() / 2);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom middle"));
		}
	}

	/**
	 * 縮小します。
	 */
	public void zoomOut() {
		this.doWheelRotation(-1, this.getWidth() / 2, this.getHeight() / 2);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom out"));
		}
	}

	/**
	 * 全域表示します。
	 */
	public void zoomWhole() {
		this.zoomAutomaticaly();
		this.moveToCenter();
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom whole"));
		}
	}

	/**
	 * 広域表示します。
	 */
	public void zoomWide() {
		this.zoom(Const.Zoom.LOAD_FINE_ROAD, this.getWidth() / 2, this.getHeight() / 2);
		if (this.listener != null) {
			this.listener.actionPerformed(new ActionEvent(this, this.hashCode(), "zoom wide"));
		}
	}

	/**
	 * @return 彩度の増分
	 */
	public float getSaturationDifference() {
		return this.saturationDifference;
	}

	/**
	 * @return 中心点を表示するかどうか
	 * @since 6.0.0
	 */
	public boolean isCenterMark() {
		return this.isCenterMark;
	}

	/**
	 * 中心点を表示するかどうかを切り替えます。
	 * @since 6.0.0
	 */
	public void toggleCenterMark() {
		this.isCenterMark = !this.isCenterMark;
	}

	/**
	 * @return ルート探索モードかどうか
	 * @since 6.0.0
	 */
	public boolean isRouteMode() {
		return this.isRouteMode;
	}

	/**
	 * ルート探索モードかどうかを切り替えます。
	 * @since 6.0.0
	 */
	public void toggleRouteMode() {
		this.isRouteMode = !this.isRouteMode;
		if (this.isRouteMode) {
			this.initializeGraph();
		} else {
			Route.getInstance().clear();
			Route.getInstance().clearRoute();
		}
	}

	/**
	 * 最短経路探索用のグラフを初期化します。
	 * @since 6.0.0
	 */
	public void initializeGraph() {
		if (this.prefectures != null) {
			try {
				if (Const.Zoom.LOAD_FINE_CITIES <= this.zoom && this.zoom < Const.Zoom.LOAD_ALL) {
					Route.getInstance().clear();
					for (final Prefecture prefecture : this.prefectures) {
						if (prefecture.hasCities()) {
							for (final City city : prefecture.getCities()) {
								if (city.hasKsjFineRoad()) {
									for (final Railway railway : city.getKsjFineRoad()) {
										switch (railway.getBusiness()) {
										case ROAD_HIGHWAY:
											Route.getInstance().add(railway.getShape(), Category.ROAD_HIGHWAY);
											break;
										case ROAD_KOKUDO:
											Route.getInstance().add(railway.getShape(), Category.ROAD_KOKUDO);
											break;
										case ROAD_MAJOR:
											Route.getInstance().add(railway.getShape(), Category.ROAD_MAJOR);
											break;
										default:
											Route.getInstance().add(railway.getShape(), Category.UNKNOWN);
										}
									}
								}
							}
						}
					}
				}
			} catch (final IOException exception) {
				exception.printStackTrace();
			}
		}
		if (this.maps != null) {
			try {
				if (this.zoom >= Const.Zoom.LOAD_ALL) {
					Route.getInstance().clear();
					for (final MapData mapData : this.maps.values()) {
						if (mapData.hasRoadArc() && mapData.hasTyome()) {
							for (final ArcData arc : mapData.getRoadArc().values()) {
								switch (arc.getRoadType()) {
								case ArcData.ROAD_HIGHWAY:
									Route.getInstance().add(arc.getPath(), Category.ROAD_HIGHWAY);
									break;
								case ArcData.ROAD_KOKUDO:
									Route.getInstance().add(arc.getPath(), Category.ROAD_KOKUDO);
									break;
								case ArcData.ROAD_KENDO:
									Route.getInstance().add(arc.getPath(), Category.ROAD_KENDO);
									break;
								case ArcData.ROAD_CHIHODO:
									Route.getInstance().add(arc.getPath(), Category.ROAD_CHIHODO);
									break;
								case ArcData.ROAD_MAJOR:
									Route.getInstance().add(arc.getPath(), Category.ROAD_MAJOR);
									break;
								case ArcData.ROAD_NORMAL:
									Route.getInstance().add(arc.getPath(), Category.ROAD_OTHER);
									break;
								default:
									Route.getInstance().add(arc.getPath(), Category.UNKNOWN);
								}
							}
						}
					}
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

}
