package svgout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.Document;

import com.sun.org.apache.xerces.internal.impl.xs.dom.DocumentImpl;

/**
 * SVG出力するためのユーティリティクラスです。
 * @author zenjiro
 * @since 4.13
 */
public class SVGOut {
	/**
	 * SVGファイルを出力します。
	 * @param file ファイル
	 * @param painter 描画を行うオブジェクト
	 * @throws UnsupportedEncodingException サポート外エンコーディング例外
	 * @throws SVGGraphics2DIOException SVGファイルに関する入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	public static void print(final File file, final Paintable painter) throws UnsupportedEncodingException,
			SVGGraphics2DIOException, FileNotFoundException {
		final Document domFactory = new DocumentImpl();
		final SVGGraphics2D svgGenerator = new SVGGraphics2D(domFactory);
		painter.paint(svgGenerator);
		svgGenerator.setSVGCanvasSize(painter.getSize());
		final String useCssStr = System.getProperty("useCss", "true");
		final boolean useCss = useCssStr.equalsIgnoreCase("true");
		final Writer outWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		svgGenerator.stream(outWriter, useCss);
	}
}
