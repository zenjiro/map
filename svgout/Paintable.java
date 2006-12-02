package svgout;

import java.awt.Dimension;
import java.awt.Graphics;

/**
 * paint(java.awt.Graphics g)メソッドを定義するインターフェイスです。
 * @author zenjiro
 * @since 4.13
 */
public interface Paintable {
	/**
	 * グラフィクスを描画します。
	 * @param g 描画対象
	 */
	public void paint(final Graphics g);
	
	/**
	 * @return 描画領域の大きさ
	 */
	public Dimension getSize();
}
