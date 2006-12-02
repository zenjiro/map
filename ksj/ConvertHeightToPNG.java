package ksj;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;

import map.Const;
import map.DefaultMapPreferences;
import map.UTMUtil;
import map.WGSUtil;

/**
 * 国土数値情報の標高・傾斜度3次メッシュデータ、1/4細分メッシュデータを読み込んでPNGファイルに保存するプログラムです。
 * カレントディレクトリに
 * "height_%d_%d_%f_%d_%d.png", 幅（実座標）, 高さ（実座標）, 表示倍率, 左（実座標）, 上（実座標） 
 * という名前のPNGファイルが生成されます。
 * 全てのメッシュを一旦メモリに格納するので、実行時に-Xmx800mなどを付けないとjava.lang.OutOfMemoryErrorで落ちます。
 * 3次メッシュデータでは、全国の処理に1時間20分かかりました。
 * @author zenjiro
 * @since 5.00
 * 2006/10/28
 */
public class ConvertHeightToPNG {

	/**
	 * メインメソッドです。
	 * @param args
	 * @throws IOException 
	 * @throws NoninvertibleTransformException 
	 */
	public static void main(String[] args) throws IOException, NoninvertibleTransformException {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		final Map<Shape, Color> shapes = new LinkedHashMap<Shape, Color>();
		for (final File file : new File(".").listFiles()) {
			if (file.getName().matches("G04-b-81_[0-9]{4}\\.csv")) {
//							if (file.getName().matches("G04-b-81_(51|52|53)(34|35)\\.csv")) {
				System.out.println(file + ", using "
						+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024 + "[MB]");
				final Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				while (scanner.hasNextLine()) {
					final String line = scanner.nextLine();
					if (!line.startsWith("#")) {
						final String[] items = line.split(",");
						if (items.length == 5) {
							final String mesh = items[0];
							final String heightString = items[1];
							@SuppressWarnings("unused")
							final String slopeString = items[2];
							@SuppressWarnings("unused")
							final String direction = items[3];
							@SuppressWarnings("unused")
							final String sea = items[4];
							if (!heightString.equals("99999") && !heightString.equals("9999")
									&& !heightString.equals("8888")) {
								final float height = (float) (1 / (1 + Math
										.exp(-Double.parseDouble(heightString) * .002 - 1)));
								final float height2 = (float) (1 / (1 + Math
										.exp(Double.parseDouble(heightString) * .06 + 1)));
								final Rectangle2D rectangle = mesh.length() == 8 ? meshToRectangle(mesh)
										: detailMeshToRectangle(mesh);
								final Point2D p1 = UTMUtil.toUTM(WGSUtil.tokyoToWGS(rectangle.getMinX(), rectangle
										.getMinY()));
								final Point2D p2 = UTMUtil.toUTM(WGSUtil.tokyoToWGS(rectangle.getMaxX(), rectangle
										.getMinY()));
								final Point2D p3 = UTMUtil.toUTM(WGSUtil.tokyoToWGS(rectangle.getMinX(), rectangle
										.getMaxY()));
								final Point2D p4 = UTMUtil.toUTM(WGSUtil.tokyoToWGS(rectangle.getMaxX(), rectangle
										.getMaxY()));
								final GeneralPath path = new GeneralPath();
								final float overlap = 0; // 200
								path.moveTo((float) p1.getX(), (float) -p1.getY());
								path.lineTo((float) p2.getX() + overlap, (float) -p2.getY());
								path.lineTo((float) p4.getX() + overlap, (float) -p4.getY() - overlap);
								path.lineTo((float) p3.getX(), (float) -p3.getY() - overlap);
								path.closePath();
								final Rectangle2D bounds = path.getBounds2D();
								minX = Math.min(minX, bounds.getMinX());
								minY = Math.min(minY, bounds.getMinY());
								maxX = Math.max(maxX, bounds.getMaxX());
								maxY = Math.max(maxY, bounds.getMaxY());
								final Color color = Color.getHSBColor(.4f - height2, Math.min(.4f - height2, 1 - height
										* height), height + height2 * .8f);
								final double x = path.getBounds2D().getCenterX();
								final double y = path.getBounds2D().getCenterY();
								final AffineTransform transform = new AffineTransform();
								transform.translate(x, y);
								transform.scale(1.2, 1.2);
								transform.translate(-x, -y);
								shapes.put(transform.createTransformedShape(path).getBounds2D(), color);
							}
						} else {
							System.out.println("要素数が不正です。" + line);
						}
					}
				}
				scanner.close();
			}
		}
		for (final double zoom : new double[] { Const.Ksj.Height.zoom4 }) {
			final double offsetX = minX * zoom;
			final double offsetY = minY * zoom;
			final double width = (maxX - minX) * zoom;
			final double height = (maxY - minY) * zoom;
			final BufferedImage image = new BufferedImage(Const.Ksj.Height.WIDTH, Const.Ksj.Height.HEIGHT,
					BufferedImage.TYPE_INT_BGR);
			final Graphics2D g = (Graphics2D) image.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (int y = (int) (Math.floor(offsetY / Const.Ksj.Height.HEIGHT)) * Const.Ksj.Height.HEIGHT; y - offsetY < height; y += Const.Ksj.Height.HEIGHT) {
				for (int x = (int) (Math.floor(offsetX / Const.Ksj.Height.WIDTH)) * Const.Ksj.Height.WIDTH; x - offsetX < width; x += Const.Ksj.Height.WIDTH) {
					g.setColor(new DefaultMapPreferences().getMizuPreferences().getFillColor());
					g.fillRect(0, 0, image.getWidth(), image.getHeight());
					final double virtualX = x / zoom;
					final double virtualY = y / zoom;
					final double virtualWidth = Const.Ksj.Height.WIDTH / zoom;
					final double virtualHeight = Const.Ksj.Height.HEIGHT / zoom;
					final AffineTransform transform = new AffineTransform();
					transform.translate(-x, -y);
					transform.scale(zoom, zoom);
					g.transform(transform);
					boolean hadDrawn = false;
					for (Map.Entry<Shape, Color> entry : shapes.entrySet()) {
						if (entry.getKey().intersects(virtualX, virtualY, virtualWidth, virtualHeight)) {
							g.setColor(entry.getValue());
							g.fill(entry.getKey());
							hadDrawn = true;
						}
					}
					g.transform(transform.createInverse());
					final File file = new File(new Formatter().format("%s%d_%d_%f_%d_%d.png", Const.Ksj.Height.PREFIX,
							Const.Ksj.Height.WIDTH, Const.Ksj.Height.HEIGHT, zoom, x, y).toString());
					if (hadDrawn) {
						ImageIO.write(image, "PNG", file);
						System.out.println("wrote " + file);
					} else {
						System.out.println("skipped " + file);
					}
				}
			}
		}
	}

	/**
	 * 3次メッシュコードから、対応する長方形の経度、緯度を求めます。
	 * @param mesh 3次メッシュコード
	 * @return 長方形（単位は経度、緯度）
	 */
	public static Rectangle2D meshToRectangle(final String mesh) {
		if (mesh.matches("[0-9]{4}[0-7]{2}[0-9]{2}")) {
			final float latitude = Integer.parseInt(mesh.substring(0, 2)) / 1.5f
					+ Integer.parseInt(mesh.substring(4, 5)) * 2f / 3 / 8 + Integer.parseInt(mesh.substring(6, 7)) * 2f
					/ 3 / 8 / 10;
			final float longitude = Integer.parseInt(mesh.substring(2, 4)) + 100
					+ Integer.parseInt(mesh.substring(5, 6)) / 8f + Integer.parseInt(mesh.substring(7, 8)) / 8f / 10;
			return new Rectangle2D.Float(longitude, latitude, 1f / 8 / 10, 2f / 3 / 8 / 10);
		} else {
			throw new IllegalArgumentException("3次メッシュコードの形式が不正です。");
		}
	}

	/**
	 * 1/4細分メッシュコードから、対応する長方形の経度、緯度を求めます。
	 * @param mesh 1/4細分メッシュコード
	 * @return 長方形（単位は経度、緯度）
	 */
	public static Rectangle2D detailMeshToRectangle(final String mesh) {
		if (mesh.matches("[0-9]{4}[0-7]{2}[0-9]{2}(00|01|02|03|04|05|06|07|08|09|10|11|12|13|14|15)")) {
			final float latitude = Integer.parseInt(mesh.substring(0, 2)) / 1.5f
					+ Integer.parseInt(mesh.substring(4, 5)) * 2f / 3 / 8 + Integer.parseInt(mesh.substring(6, 7)) * 2f
					/ 3 / 8 / 10;
			final float longitude = Integer.parseInt(mesh.substring(2, 4)) + 100
					+ Integer.parseInt(mesh.substring(5, 6)) / 8f + Integer.parseInt(mesh.substring(7, 8)) / 8f / 10;
			final float width = 1f / 8 / 10 / 4;
			final float height = 2f / 3 / 8 / 10 / 4;
			final int index = Integer.parseInt(mesh.substring(8, 10));
			return new Rectangle2D.Float(longitude + (index % 4) * width, latitude + height * 3 - (index / 4) * height,
					width, height);
		} else {
			throw new IllegalArgumentException("1/4細分メッシュコードの形式が不正です。");
		}
	}
}
