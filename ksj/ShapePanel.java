package ksj;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Shapeオブジェクトを表示するパネルです。
 * 印刷もできます。
 * @author zenjiro
 * Created on 2005/08/22
 */
public class ShapePanel extends JPanel implements Printable {
	/**
	 * 境界色
	 */
	private Map<Shape, Color> borderColors;

	/**
	 * 塗りつぶし色
	 */
	private Map<Shape, Color> fillColors;

	/**
	 * フォント
	 */
	private Map<Shape, Font> fonts;

	/**
	 * ダブルバファリング用のイメージ
	 */
	Image image;

	/**
	 * アンチエイリアス表示するかどうか
	 */
	final boolean isAntialias;

	/**
	 * 再描画する必要があるかどうか
	 */
	boolean isChanged;

	/**
	 * ラベル
	 */
	private Map<Shape, String> labels;

	/**
	 * 直前にマウスがいたx座標
	 */
	int lastMouseX;

	/**
	 * 直前にマウスがいたy座標
	 */
	int lastMouseY;

	/**
	 * オフセット
	 */
	double offsetX;

	/**
	 * オフセット
	 */
	double offsetY;

	/**
	 * 選択範囲
	 */
	Rectangle2D selection;

	/**
	 * 描画するオブジェクト
	 */
	private Collection<Shape> shapes;

	/**
	 * 表示倍率
	 */
	double zoom;

	/**
	 * コンストラクタです。
	 */
	public ShapePanel() {
		this(false);
	}

	/**
	 * コンストラクタです。
	 * @param isAntialias アンチエイリアス表示するかどうか
	 */
	public ShapePanel(final boolean isAntialias) {
		this.isAntialias = isAntialias;
		this.shapes = new ArrayList<Shape>();
		this.labels = new HashMap<Shape, String>();
		this.fonts = new HashMap<Shape, Font>();
		this.borderColors = new HashMap<Shape, Color>();
		this.fillColors = new HashMap<Shape, Color>();
		this.selection = new Rectangle2D.Double();
		this.setBackground(Color.WHITE);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					ShapePanel.this.lastMouseX = e.getX();
					ShapePanel.this.lastMouseY = e.getY();
				} else {
					ShapePanel.this.lastMouseX = e.getX();
					ShapePanel.this.lastMouseY = e.getY();
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					final Point2D p1 = ShapePanel.this.toVirtualLocation(new Point2D.Double(
							ShapePanel.this.lastMouseX, ShapePanel.this.lastMouseY));
					final Point2D p2 = ShapePanel.this.toVirtualLocation(new Point2D.Double(e.getX(), e.getY()));
					ShapePanel.this.selection.setFrameFromDiagonal(p1, p2);
					ShapePanel.this.isChanged = true;
					ShapePanel.this.repaint();
				}
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
					final Point2D p1 = ShapePanel.this.toVirtualLocation(new Point2D.Double(
							ShapePanel.this.lastMouseX, ShapePanel.this.lastMouseY));
					final Point2D p2 = ShapePanel.this.toVirtualLocation(new Point2D.Double(e.getX(), e.getY()));
					ShapePanel.this.selection.setFrameFromDiagonal(p1, p2);
					ShapePanel.this.isChanged = true;
					ShapePanel.this.repaint();
				} else {
					ShapePanel.this.offsetX -= e.getX() - ShapePanel.this.lastMouseX;
					ShapePanel.this.offsetY -= e.getY() - ShapePanel.this.lastMouseY;
					ShapePanel.this.lastMouseX = e.getX();
					ShapePanel.this.lastMouseY = e.getY();
					ShapePanel.this.isChanged = true;
					ShapePanel.this.repaint();
				}
			}
		});
		this.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(final MouseWheelEvent e) {
				final double newZoom = ShapePanel.this.zoom
						* (1 + (double) e.getWheelRotation() / 10);
				ShapePanel.this.offsetX = ((ShapePanel.this.offsetX + e.getX())
						/ ShapePanel.this.zoom * newZoom - e.getX());
				ShapePanel.this.offsetY = ((ShapePanel.this.offsetY + e.getY())
						/ ShapePanel.this.zoom * newZoom - e.getY());
				ShapePanel.this.zoom = newZoom;
				ShapePanel.this.isChanged = true;
				ShapePanel.this.repaint();
			}
		});
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				ShapePanel.this.image = null;
				ShapePanel.this.repaint();
			}
		});
	}

	/**
	 * 全てのオブジェクトを削除します。
	 */
	public void clear() {
		this.shapes.clear();
		this.borderColors.clear();
		this.fillColors.clear();
		this.fonts.clear();
		this.labels.clear();
	}
	
	/**
	 * 表示倍率を再計算して再描画します。
	 */
	public void redraw() {
		this.image = null;
		this.repaint();
	}
	
	/**
	 * @param shape 描画するオブジェクト
	 */
	public void add(final Shape shape) {
		this.shapes.add(shape);
	}

	/**
	 * オブジェクトを描画します。
	 * @param g グラフィクスコンテキスト
	 */
	private void draw(final Graphics2D g) {
		g.translate(-this.offsetX, -this.offsetY);
		g.scale(this.zoom, this.zoom);
		for (final Shape shape : this.shapes) {
			if (this.fillColors.containsKey(shape)) {
				if (this.fillColors.get(shape) != null) {
					g.setColor(this.fillColors.get(shape));
					g.fill(shape);
				}
			} else {
				g.setColor(Color.WHITE);
				g.fill(shape);
			}
			if (this.borderColors.containsKey(shape)) {
				if (this.borderColors.get(shape) != null) {
					g.setColor(this.borderColors.get(shape));
					g.draw(shape);
				}
			} else {
				g.setColor(Color.BLACK);
				g.draw(shape);
			}
		}
		g.setColor(Color.BLACK);
		for (final Map.Entry<Shape, String> entry : this.labels.entrySet()) {
			if (this.fonts.containsKey(entry.getKey())) {
				g.setFont(this.fonts.get(entry.getKey()));
			}
			final FontMetrics metrics = this.getFontMetrics(g.getFont());
			final Rectangle2D rectangle = metrics.getStringBounds(entry.getValue(), g);
			g
					.drawString(
							entry.getValue(),
							(float) (entry.getKey().getBounds().getCenterX() - rectangle.getWidth() / 2),
							(float) (entry.getKey().getBounds().getCenterY()
									+ rectangle.getHeight() / 2 - metrics.getDescent()));
		}
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke((float) (2 / this.zoom)));
		g.draw(this.selection);
	}

	/**
	 * @return 選択されている長方形
	 */
	public Rectangle2D getSelection() {
		return this.selection;
	}

	@Override
	public void paintComponent(final Graphics graphics) {
		if (this.image == null) {
			int minX = Integer.MAX_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxY = Integer.MIN_VALUE;
			for (final Shape shape : this.shapes) {
				minX = Math.min(minX, shape.getBounds().x);
				minY = Math.min(minY, shape.getBounds().y);
				maxX = Math.max(maxX, shape.getBounds().x + shape.getBounds().width);
				maxY = Math.max(maxY, shape.getBounds().y + shape.getBounds().height);
			}
			final double zoomX = (double) (this.getWidth() - 1) / (maxX - minX);
			final double zoomY = (double) (this.getHeight() - 1) / (maxY - minY);
			this.zoom = zoomX < zoomY ? zoomX : zoomY;
			this.offsetX = (minX + maxX) / 2 * this.zoom - this.getWidth() / 2;
			this.offsetY = (minY + maxY) / 2 * this.zoom - this.getHeight() / 2;
			this.image = this.createImage(this.getWidth(), this.getHeight());
			this.isChanged = true;
		}
		if (this.isChanged) {
			final Graphics2D g = (Graphics2D) this.image.getGraphics();
			g.setColor(this.getBackground());
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			if (this.isAntialias) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
			}
			this.draw(g);
			this.isChanged = false;
		}
		graphics.drawImage(this.image, 0, 0, this);
	}

	public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex)
			throws PrinterException {
		if (pageIndex == 0) {
			final Graphics2D g = (Graphics2D) graphics;
			final double newZoom = pageFormat.getImageableWidth() / this.getWidth();
			g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			g.scale(newZoom, newZoom);
			this.draw(g);
			return Printable.PAGE_EXISTS;
		} else {
			return Printable.NO_SUCH_PAGE;
		}
	}

	/**
	 * @param shape 描画するオブジェクト
	 * @param color 境界色
	 */
	public void setBorderColor(final Shape shape, final Color color) {
		this.borderColors.put(shape, color);
	}

	/**
	 * @param shape 描画するオブジェクト
	 * @param color 塗りつぶし色
	 */
	public void setFillColor(final Shape shape, final Color color) {
		this.fillColors.put(shape, color);
	}

	/**
	 * @param shape 描画するオブジェクト
	 * @param font フォント
	 */
	public void setFont(final Shape shape, final Font font) {
		this.fonts.put(shape, font);
	}

	/**
	 * @param shape 描画するオブジェクト
	 * @param label ラベル
	 */
	public void setLabel(final Shape shape, final String label) {
		this.labels.put(shape, label);
	}

	/**
	 * 仮想座標を取得します。
	 * @param location 実座標
	 * @return 仮想座標
	 */
	Point2D toVirtualLocation(final Point2D location) {
		return new Point2D.Double((this.offsetX + location.getX()) / this.zoom,
				(this.offsetY + location.getY()) / this.zoom);
	}
}
