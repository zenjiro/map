package ksj;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.Const;
import map.UTMUtil;

/**
 * 国土数値情報の道路をCSV形式に変換するプログラムです。
 * @author zenjiro
 */
public class ConvertRoadToCSV {
	/**
	 * 線分データを表すクラスです。
	 * @author zenjiro
	 */
	static class Link {
		/**
		 * 点列
		 */
		final List<Point2D> points;

		/**
		 * 属性
		 */
		final String attribute;

		/**
		 * 線分データを初期化します。
		 * @param points 点列
		 * @param attribute 属性
		 */
		public Link(final List<Point2D> points, final String attribute) {
			this.points = points;
			this.attribute = attribute;
		}
	}

	/**
	 * メインメソッドです。
	 * @param args コマンドライン引数
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	public static void main(final String[] args)
			throws UnsupportedEncodingException, FileNotFoundException {
		final Map<String, String> attributes = new HashMap<String, String>();
		{
			final File file = new File("N01_07L_dl.txt");
			final Scanner scanner = new Scanner(new InputStreamReader(
					new FileInputStream(file), "SJIS"));
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (line.startsWith("DL")) {
					final String attributeNumber = line.substring(3, 14).trim();
					final String[] items = line.substring(18).split(" ");
					final String attribute = (items[0] + (items.length > 1 ? items[1]
							: "")).trim().replace(" ", "").replace("０", "0")
							.replace("１", "1").replace("２", "2").replace("３",
									"3").replace("４", "4").replace("５", "5")
							.replace("６", "6").replace("７", "7").replace("８",
									"8").replace("９", "9").replaceFirst("号線$",
									"号");
					attributes.put(attributeNumber, attribute);
				}
			}
			scanner.close();
		}

		final Pattern pattern = Pattern.compile("N01-07L-2K-([0-9][0-9])\\.txt");
		for (final File file : new File("").listFiles()) {
			final Matcher matcher = pattern.matcher(file.getName());
			if (matcher.matches()) {
				System.out.println(file);
				final Map<String, Link> links = new HashMap<String, Link>();
				{
					final Scanner scanner = new Scanner(new InputStreamReader(
							new FileInputStream(file), "SJIS"));
					boolean isLink = false;
					String lastLink = null;
					while (scanner.hasNextLine()) {
						final String line = scanner.nextLine();
						if (line.startsWith("L")) {
							final String meshCode = line.substring(3, 9).trim();
							final String linkNumber = line.substring(27, 33)
									.trim();
							final String attributeNumber = line.substring(35,
									45).trim();
							if (attributes.containsKey(attributeNumber)) {
								final String attribute = attributes
										.get(attributeNumber);
								links.put(meshCode + "_" + linkNumber,
										new Link(new ArrayList<Point2D>(),
												attribute));
								lastLink = meshCode + "_" + linkNumber;
							}
							isLink = true;
						} else {
							if (isLink) {
								if (lastLink != null) {
									final String[] items = line.trim().split(
											"[ \t]+");
									for (int i = 1; i < items.length; i += 2) {
										final int x = Integer
												.parseInt(items[i - 1]);
										final int y = Integer
												.parseInt(items[i]);
										Point2D point = UTMUtil
												.toUTM(new Point2D.Double(
														x / 36000.0,
														y / 36000.0));
										links.get(lastLink).points
												.add(new Point2D.Double(point
														.getX(), -point.getY()));
									}
								}
							}
						}
					}
					scanner.close();
				}

				final Map<Shape, String> shapes = new HashMap<Shape, String>();
				final Map<Shape, String> simpleShapes = new HashMap<Shape, String>();
				{
					for (final Map.Entry<String, Link> entry : links.entrySet()) {
						final Link link = entry.getValue();
						GeneralPath path = null;
						Point2D start = null;
						Point2D end = null;
						for (final Point2D point : link.points) {
							if (path == null) {
								path = new GeneralPath();
								path.moveTo((float) point.getX(), (float) point
										.getY());
								start = point;
							} else {
								path.lineTo((float) point.getX(), (float) point
										.getY());
								end = point;
							}
						}
						shapes.put(path, link.attribute);
						simpleShapes.put(new Line2D.Double(start, end), link.attribute);
					}
				}
				ShapeIO.writeShape(shapes, new FileOutputStream(new File(Const.Ksj.ROAD_FINE_PREFIX + matcher.group(1) + Const.Ksj.ROAD_SUFFIX)));
				ShapeIO.writeShape(simpleShapes, new FileOutputStream(new File(Const.Ksj.ROAD_SIMPLE_PREFIX + matcher.group(1) + Const.Ksj.ROAD_SUFFIX)));
			}
		}
	}
}
