package ksj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Shapeオブジェクトをストリームに書き込んだり、ストリームから復元したりするユーティリティクラスです。
 * @author zenjiro
 * 2005/11/10
 */
public class ShapeIO {
	/**
	 * Shapeオブジェクトの一覧をストリームに書き込みます。
	 * 文字列中に「,」が含まれていると、正しく読み込めなくなるので注意してください。
	 * @param shapes Shapeオブジェクトと文字列の入ったマップ
	 * @param outputStream 出力ストリーム
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 */
	public static void writeShape(final Map<Shape, String> shapes, final OutputStream outputStream)
			throws UnsupportedEncodingException {
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream, "SJIS"));
		out.println("# label, type1, x1, y1, type2, x2, y2, ...");
		for (final Map.Entry<Shape, String> entry : shapes.entrySet()) {
			out.print(entry.getValue());
			final PathIterator iterator = entry.getKey().getPathIterator(new AffineTransform());
			while (!iterator.isDone()) {
				final float[] coords = new float[6];
				final int type = iterator.currentSegment(coords);
				out.print("," + type + "," + coords[0] + "," + coords[1]);
				iterator.next();
			}
			out.println();
		}
		out.close();
	}

	/**
	 * ストリームからShapeオブジェクトの一覧を読み込みます。
	 * @param in 入力ストリーム
	 * @return Shapeオブジェクトの一覧
	 */
	public static Map<Shape, String> readShapes(final InputStream in) {
		final Map<Shape, String> ret = new ConcurrentHashMap<Shape, String>();
		final Scanner scanner = new Scanner(in, "SJIS");
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			if (!line.startsWith("#")) {
				final Scanner scanner2 = new Scanner(line);
				scanner2.useDelimiter(",");
				if (scanner2.hasNext()) {
					final String string = scanner2.next();
					final GeneralPath path = new GeneralPath();
					while (scanner2.hasNextInt()) {
						if (scanner2.hasNextInt()) {
							final int type = scanner2.nextInt();
							if (scanner2.hasNextFloat()) {
								final float x = scanner2.nextFloat();
								if (scanner2.hasNextFloat()) {
									final float y = scanner2.nextFloat();
									switch (type) {
									case PathIterator.SEG_MOVETO:
										path.moveTo(x, y);
										break;
									case PathIterator.SEG_LINETO:
									case PathIterator.SEG_CUBICTO:
									case PathIterator.SEG_QUADTO:
										path.lineTo(x, y);
										break;
									case PathIterator.SEG_CLOSE:
										path.closePath();
										break;
									}
								}
							}
						}
					}
					ret.put(path, string);
				}
			}
		}
		scanner.close();
		return ret;
	}
	
	/**
	 * テスト用のメソッドです。
	 * @param args コマンドライン引数
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	public static void main(final String[] args) throws FileNotFoundException {
		if (args.length != 1) {
			System.out.println("使い方：java Main filename");
			System.exit(1);
		}
		final Map<Shape, String> shapes = ShapeIO.readShapes(new FileInputStream(new File(args[0])));
		final JFrame frame = new JFrame("テスト");
		frame.setSize(640, 480);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLocationByPlatform(true);
		final ShapePanel panel = new ShapePanel();
		final Random random = new Random();
		final Font font = new Font("MS Gothic", Font.PLAIN, 2000);
		for (final Map.Entry<Shape, String> entry : shapes.entrySet()) {
			final Shape shape = entry.getKey();
			panel.add(shape);
			panel.setFillColor(shape, new Color(1 - random.nextFloat() / 5,
					1 - random.nextFloat() / 5, 1 - random.nextFloat() / 5));
			panel.setLabel(shape, entry.getValue());
			panel.setFont(shape, font);
		}
		frame.add(panel);
		frame.setVisible(true);
	}
}
