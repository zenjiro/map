package ksj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import map.MapPanel;
import map.UTMUtil;

/**
 * 国土数値情報の行政界・海岸線データを読み込むユーティリティクラスです。
 * 座標系は緯度、経度の0.1秒単位で、整数です。
 * @author zenjiro
 * Created on 2005/10/30
 */
public class LoadKsj {
	/**
	 * 線分を表すクラスです。
	 */
	private static class Link {
		/**
		 * 始点
		 */
		Point p1;

		/**
		 * 終点
		 */
		Point p2;

		/**
		 * 中間点の一覧
		 */
		List<Point> points;

		/**
		 * 線分を初期化します。
		 * @param p1 始点
		 * @param p2 終点
		 */
		public Link(final Point p1, final Point p2) {
			this.p1 = p1;
			this.p2 = p2;
			this.points = new LinkedList<Point>();
		}
	}

	/**
	 * データの状態
	 */
	private enum Status {
		/**
		 * エリアデータ
		 */
		AREA,
		/**
		 * エリア台帳データ
		 */
		AREA_LEDGER,
		/**
		 * ヘッダデータ
		 */
		HEADER,
		/**
		 * リンクデータ
		 */
		LINK,
		/**
		 * ノードデータ
		 */
		NODE,
		/**
		 * 無視するデータ
		 */
		SKIP
	}

	/**
	 * 地図データを読み込んでUTM座標に変換します。
	 * @param directory ディレクトリ
	 * @param regex ファイル名の正規表現
	 * @param cacheFile 座標変換済みのデータを記録するキャッシュファイル
	 * @param isFast 急ぐかどうか
	 * @param panel 地図を描画するパネル
	 * @return 地図データ
	 * @throws FileNotFoundException ファイル未検出例外
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 */
	public static Map<Shape, String> loadShapesUTM(final File directory, final String regex,
		final String cacheFile, final boolean isFast, final MapPanel panel) throws UnsupportedEncodingException,
		FileNotFoundException {
		if (new File(cacheFile).exists()) {
			return ShapeIO.readShapes(new FileInputStream(new File(cacheFile)));
		} else {
			panel.addMessage(regex + "の座標変換をして" + cacheFile + "に保存しています。");
			final Map<Shape, String> ret = new ConcurrentHashMap<Shape, String>();
			final Map<Shape, String> shapes = loadShapes(directory, regex, isFast);
			for (final Map.Entry<Shape, String> entry : shapes.entrySet()) {
				final GeneralPath path = new GeneralPath();
				final PathIterator iterator = entry.getKey().getPathIterator(new AffineTransform());
				while (!iterator.isDone()) {
					final float[] coords = new float[6];
					final int type = iterator.currentSegment(coords);
					switch (type) {
					case PathIterator.SEG_MOVETO: {
						final Point2D point = UTMUtil.toUTM(coords[0] / 36000.0,
							coords[1] / 36000.0);
						path.moveTo((float) point.getX(), (float) point.getY());
						break;
					}
					case PathIterator.SEG_LINETO: {
						final Point2D point = UTMUtil.toUTM(coords[0] / 36000.0,
							coords[1] / 36000.0);
						path.lineTo((float) point.getX(), (float) point.getY());
						break;
					}
					case PathIterator.SEG_CLOSE:
						path.closePath();
						break;
					}
					iterator.next();
				}
				ret.put(path, entry.getValue());
			}
			ShapeIO.writeShape(ret, new FileOutputStream(new File(cacheFile)));
			panel.removeMessage();
			return ret;
		}
	}

	/**
	 * 地図データを読み込みます。
	 * @param file 読み込むファイル
	 * @param isFast 急ぐかどうか
	 * @return 地図データ
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	public static Map<Shape, String> loadShapes(final File file, final boolean isFast)
		throws UnsupportedEncodingException, FileNotFoundException {
		final Map<Shape, String> ret = new ConcurrentHashMap<Shape, String>();
		final Scanner scanner = new Scanner(
			new InputStreamReader(new FileInputStream(file), "SJIS"));
		Status status = Status.SKIP;
		final Map<Integer, Map<Integer, Point>> nodes = new ConcurrentHashMap<Integer, Map<Integer, Point>>();
		final Map<Integer, Map<Integer, Link>> links = new ConcurrentHashMap<Integer, Map<Integer, Link>>();
		final Map<Integer, GeneralPath> areas = new ConcurrentHashMap<Integer, GeneralPath>();
		final Map<Integer, String> areaIDCityCodeTable = new ConcurrentHashMap<Integer, String>();
		final Map<String, String> attributes = new ConcurrentHashMap<String, String>();
		int areaID = -1;
		int linkMesh = -1;
		int linkID = -1;
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			// System.out.println("DEBUG: line = " + line);
			switch (line.charAt(0)) {
			case 'H':
				status = Status.HEADER;
				break;
			case 'N': {
				status = Status.NODE;
				final int mesh = Integer.parseInt(line.substring(3, 9).replace(" ", ""));
				final int id = Integer.parseInt(line.substring(9, 15).replace(" ", ""));
				final int x = Integer.parseInt(line.substring(15, 23).replace(" ", ""));
				final int y = -Integer.parseInt(line.substring(23, 31).replace(" ", ""));
				if (!nodes.containsKey(mesh)) {
					nodes.put(mesh, new ConcurrentHashMap<Integer, Point>());
				}
				nodes.get(mesh).put(id, new Point(x, y));
				break;
			}
			case 'L': {
				status = Status.LINK;
				final int mesh1 = Integer.parseInt(line.substring(3, 9).replace(" ", ""));
				final int id1 = Integer.parseInt(line.substring(9, 15).replace(" ", ""));
				final int mesh2 = Integer.parseInt(line.substring(15, 21).replace(" ", ""));
				final int id2 = Integer.parseInt(line.substring(21, 27).replace(" ", ""));
				final int id = Integer.parseInt(line.substring(27, 33).replace(" ", ""));
				linkMesh = mesh1;
				linkID = id;
				if (!links.containsKey(mesh1)) {
					links.put(mesh1, new ConcurrentHashMap<Integer, Link>());
				}
				links.get(mesh1).put(id,
					new Link(nodes.get(mesh1).get(id1), nodes.get(mesh2).get(id2)));
				links.get(mesh1).put(-id,
					new Link(nodes.get(mesh2).get(id2), nodes.get(mesh1).get(id1)));
				break;
			}
			case 'A': {
				status = Status.AREA;
				areaID = parseInt(line, 25, 33);
				final String cityCode = line.substring(40, 45);
				areaIDCityCodeTable.put(areaID, cityCode);
				break;
			}
			case 'D': {
				status = Status.AREA_LEDGER;
				final String cityCode = line.substring(8, 13);
				final String line2 = line.substring(16).replace("　", " ");
				final Scanner scanner2 = new Scanner(line2);
				scanner2.useDelimiter("[ 　]+");
				final String prefecture = scanner2.next();
				String city = scanner2.next();
				while (scanner2.hasNext()) {
					city += scanner2.next();
				}
				attributes.put(cityCode, cityCode.substring(0, 2) + "_" + prefecture + "_"
					+ cityCode + "_" + city);
				break;
			}
			default:
				switch (status) {
				case LINK:
					if (!isFast) {
						final Scanner scanner2 = new Scanner(line);
						while (scanner2.hasNextInt()) {
							final int x = scanner2.nextInt();
							if (scanner2.hasNextInt()) {
								final int y = -scanner2.nextInt();
								final Point point = new Point(x, y);
								links.get(linkMesh).get(linkID).points.add(point);
								links.get(linkMesh).get(-linkID).points.add(0, point);
							} else {
								System.out.println("WARNING: 奇数個の要素があります。" + line);
							}
						}
					}
					break;
				case AREA:
					if (line.length() > 11) {
						final int mesh1 = Integer.parseInt(line.substring(0, 6).replace(" ", ""));
						final int id1 = Integer.parseInt(line.substring(6, 12).replace(" ", ""));
						final Link link1 = links.get(mesh1).get(id1);
						addLink(link1, areas, areaID, isFast);
						if (line.length() > 25) {
							final int mesh2 = Integer.parseInt(line.substring(14, 20).replace(" ",
								""));
							final int id2 = Integer.parseInt(line.substring(20, 26)
								.replace(" ", ""));
							final Link link2 = links.get(mesh2).get(id2);
							addLink(link2, areas, areaID, isFast);
							if (line.length() > 39) {
								final int mesh3 = Integer.parseInt(line.substring(28, 34).replace(
									" ", ""));
								final int id3 = Integer.parseInt(line.substring(34, 40).replace(
									" ", ""));
								final Link link3 = links.get(mesh3).get(id3);
								addLink(link3, areas, areaID, isFast);
								if (line.length() > 53) {
									final int mesh4 = Integer.parseInt(line.substring(42, 48)
										.replace(" ", ""));
									final int id4 = Integer.parseInt(line.substring(48, 54)
										.replace(" ", ""));
									final Link link4 = links.get(mesh4).get(id4);
									addLink(link4, areas, areaID, isFast);
									if (line.length() > 67) {
										final int mesh5 = Integer.parseInt(line.substring(56, 62)
											.replace(" ", ""));
										final int id5 = Integer.parseInt(line.substring(62, 68)
											.replace(" ", ""));
										final Link link5 = links.get(mesh5).get(id5);
										addLink(link5, areas, areaID, isFast);
									}
								}
							}
						}
					}
					break;
				}
			}
		}
		scanner.close();
		for (final Map.Entry<Integer, GeneralPath> entry : areas.entrySet()) {
			final int id = entry.getKey();
			final GeneralPath path = entry.getValue();
			if (areaIDCityCodeTable.containsKey(id)) {
				final String cityCode = areaIDCityCodeTable.get(id);
				if (attributes.containsKey(cityCode)) {
					ret.put(path, attributes.get(cityCode));
				} else {
					System.out.println("WARNING: attributesにcityCodeがありません。cityCode = " + cityCode
						+ ".");
				}
			} else {
				System.out
					.println("WARNING: areaIDCityCodeTableにareaIDがありません。areaID = " + id + ".");
			}
		}
		return ret;
	}

	/**
	 * 地図データを読み込みます。
	 * @param directory ディレクトリ
	 * @param regex ファイル名の正規表現
	 * @param isFast 急ぐかどうか
	 * @return 地図データ
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	public static Map<Shape, String> loadShapes(final File directory, final String regex,
		final boolean isFast) throws UnsupportedEncodingException, FileNotFoundException {
		final Map<Shape, String> ret = new ConcurrentHashMap<Shape, String>();
		for (final File file : directory.listFiles(new FileFilter() {
			public boolean accept(final File pathname) {
				return pathname.getName().matches(regex);
			}
		})) {
			ret.putAll(LoadKsj.loadShapes(file, isFast));
		}
		return ret;
	}

	/**
	 * エリアにリンクを追加します。
	 * @param link リンク
	 * @param areas 領域の一覧
	 * @param areaID 領域のID
	 * @param isFast 急ぐかどうか
	 */
	private static void addLink(final Link link, final Map<Integer, GeneralPath> areas, final int areaID,
		final boolean isFast) {
		if (isFast) {
			if (!areas.containsKey(areaID)) {
				areas.put(areaID, new GeneralPath());
				areas.get(areaID).moveTo(link.p1.x, link.p1.y);
			}
			areas.get(areaID).lineTo(link.p2.x, link.p2.y);
		} else {
			for (final Point point : link.points) {
				if (areas.containsKey(areaID)) {
					areas.get(areaID).lineTo(point.x, point.y);
				} else {
					areas.put(areaID, new GeneralPath());
					areas.get(areaID).moveTo(link.p1.x, link.p1.y);
				}
			}
		}
	}

	/**
	 * テストを行うメソッドです。
	 * @param args コマンドライン引数
	 * @throws FileNotFoundException ファイル未検出例外
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 */
	public static void main(final String[] args) throws UnsupportedEncodingException,
		FileNotFoundException {
//		final JFrame frame = new JFrame("テスト");
//		final ShapePanel panel = new ShapePanel();
//		final Random random = new Random();
//		final Font font = new Font("MS Gothic", Font.PLAIN, 1000);
		final File file = new File("../../../../ksj");
//		final String pattern = "N03-11A-2K_(24|25|26|27|28|29|30)\\.txt";
		final String pattern = "N03-11A-2K_[0-9][0-9]\\.txt";
		final Map<Shape, String> tyome = LoadKsj.loadShapes(file, args.length > 0 ? args[0] : pattern, false);
//		final Map<Shape, String> allCityShapes = new ConcurrentHashMap<Shape, String>();
		final Map<String, Map<Shape, String>> cities = new ConcurrentHashMap<String, Map<Shape, String>>();
//		final Map<String, Area> prefectures = new ConcurrentHashMap<String, Area>();
		for (final Map.Entry<Shape, String> entry : tyome.entrySet()) {
			final Shape shape = entry.getKey();
			final GeneralPath path = new GeneralPath();
			final PathIterator iterator = shape.getPathIterator(new AffineTransform());
			while (!iterator.isDone()) {
				final float[] coords = new float[6];
				final int type = iterator.currentSegment(coords);
				switch (type) {
				case PathIterator.SEG_MOVETO: {
					final Point2D point = UTMUtil.toUTM(coords[0] / 36000.0, coords[1] / 36000.0);
					path.moveTo((float) point.getX(), (float) point.getY());
					break;
				}
				case PathIterator.SEG_LINETO: {
					final Point2D point = UTMUtil.toUTM(coords[0] / 36000.0, coords[1] / 36000.0);
					path.lineTo((float) point.getX(), (float) point.getY());
					break;
				}
				case PathIterator.SEG_CLOSE:
					path.closePath();
					break;
				}
				iterator.next();
			}
			final String prefecture = entry.getValue().split("_")[0] + "_"
				+ entry.getValue().split("_")[1];
//			if (prefectures.containsKey(prefecture)) {
//				prefectures.get(prefecture).add(new Area(path));
//			} else {
//				prefectures.put(prefecture, new Area(path));
//			}
			if (!cities.containsKey(prefecture)) {
				cities.put(prefecture, new ConcurrentHashMap<Shape, String>());
			}
			cities.get(prefecture).put(path, entry.getValue());
//			panel.add(path);
//			panel.setFillColor(path, new Color(1 - random.nextFloat() / 5,
//				1 - random.nextFloat() / 5, 1 - random.nextFloat() / 5));
//			panel.setLabel(path, entry.getValue());
//			panel.setFont(path, font);
//			allCityShapes.put(path, entry.getValue());
		}
//		final Map<Shape, String> prefectureShapes = new ConcurrentHashMap<Shape, String>();
//		for (final Map.Entry<String, Area> entry : prefectures.entrySet()) {
//			final Shape shape = entry.getValue();
//			//			panel.add(shape);
//			panel.setFillColor(shape, new Color(1 - random.nextFloat() / 5,
//				1 - random.nextFloat() / 5, 1 - random.nextFloat() / 5));
//			panel.setLabel(shape, entry.getKey());
//			panel.setFont(shape, font);
//			prefectureShapes.put(shape, entry.getKey());
//		}
//		frame.add(panel);
//		frame.setLocationByPlatform(true);
//		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		frame.setSize(800, 600);
//		frame.setVisible(true);
		// test
//		ShapeIO.writeShape(prefectureShapes, new FileOutputStream(new File("prefectures.csv")));
//		for (final Map.Entry<Shape, String> entry : prefectureShapes.entrySet()) {
//			final Map<Shape, String> map = new ConcurrentHashMap<Shape, String>();
//			map.put(entry.getKey(), entry.getValue());
//			final String id = entry.getValue().substring(0, 2);
//			ShapeIO.writeShape(map, new FileOutputStream(new File("prefecture_" + id + ".csv")));
//		}
		for (final Map.Entry<String, Map<Shape, String>> entry : cities.entrySet()) {
			final String id = entry.getKey().substring(0, 2);
			ShapeIO.writeShape(entry.getValue(), new FileOutputStream(new File("cities_" + id
				+ ".csv")));
		}
	}

	/**
	 * 文字列から整数を切り出します。
	 * @param line 文字列
	 * @param start 開始位置
	 * @param end 終了位置
	 * @return 整数
	 */
	private static int parseInt(final String line, final int start, final int end) {
		int ret;
		ret = Integer.parseInt(line.substring(start, end).replace(" ", ""));
		return ret;
	}
}
