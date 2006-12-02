/*
 * $Id: Phi.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */

package jp.jasminesoft.gcat.scalc;

/** 
 * Phiは、Newton-Raphson式を用いて
 * 求点座標の垂足緯度の近似値を求めるクラスです。
 *
 * 本パッケージは、琉球大学工学部情報工学科 宮城研究室の成果物
 * を、ジャスミンソフトが整理・統合したものです。再利用を快諾
 * して頂いた宮城 隼夫教授以下、宮城研究室のスタッフにこの場を
 * 借りて感謝致します。
 * 
 * @version $Revision: 1.2 $ $Date: 2002/03/17 10:52:42 $
 * @author  Miho Nagata
 * @author  Yoshinori Nie
 */

class Phi extends Pala {
    /**
     * 赤道から緯度までの子午線弧長を求めるクラス
     */ 
    private final ArcLength arc = new ArcLength();

    /**
     * 求点座標の垂足緯度
     */ 
    private final double phi[] = new double[5];

    /**
     *　原点までの子午線弧長にX/m0を加えた値。 
     */
    private double sxm;

    /**
     * コンストラクタ
     * @param gentenB 原点B
     * @param setX 設定X
     */
    Phi(final double gentenB, final double setX) {
	//原点緯度までの子午線弧長
	final double arc_l = this.arc.getArcLength(gentenB); 

	this.sxm = arc_l + setX / this.m0;
    }

    /**
     * phi[i]による子午線曲率半径を求めるメソッド
     * @param p 不明
     * @return phi[i]による子午線曲率半径
     */
    protected double getM(final double p) {
	final double q = 1.0 - Math.pow(this.e * Math.sin(p),2);
	final double M = this.ra * (1-Math.pow(this.e,2)) / Math.sqrt(Math.pow(q,3));
	return M;
    }

    /**
     * phiを返すメソッド
     * @return phi
     */
    protected double getPhi() {
	//phiを求める
	for (int i = 0; i < 5; i++) {
	    if(i == 0) {
		this.phi[i] = this.gentenB;
	    } else if (i > 0) {
		this.phi[i] = this.phi[i-1] - 
		    (this.arc.getArcLength(this.phi[i-1]) - this.sxm) / this.getM(this.phi[i-1]); 
	    }
	}
	return this.phi[4];
    }
}
