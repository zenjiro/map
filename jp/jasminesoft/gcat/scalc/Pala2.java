/*
 * $Id: Pala2.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */
package jp.jasminesoft.gcat.scalc;

/** 
 * Pala2は計算で必要な変数が書かれたクラスです。
 * コンストラクタによって初期化されます。
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
class Pala2 extends Pala {
	/**
	 * それぞれの変数を求めるのに必要な、緯度、経度、座標系を受け取ります。
	 * @param b 緯度
	 * @param l 経度
	 * @param kei 座標系
	 */
	Pala2(final double b, final double l, final int kei) {
		this.b = b;
		this.l = l;
		this.coord = new Coordinate(kei);
		this.gentenB = this.coord.radianGB();
		this.gentenL = this.coord.radianGL();
		this.lam = l - this.gentenL;
		this.eta = this.e1 * Math.cos(b);
		this.t = Math.tan(b);
		this.q = 1.0 - Math.pow(this.e * Math.sin(b), 2);
		this.prc = this.ra / Math.sqrt(this.q);
		this.mrc = this.ra * (1 - Math.pow(this.e, 2)) / Math.sqrt(Math.pow(this.q, 3));
	}

	/**
	 * コンストラクタ
	 * @param b 緯度
	 * @param l 経度
	 */
	Pala2(final double b, final double l) {
		this(b, l, 0);
	}

	/**
	 * コンストラクタ
	 * @param b 緯度
	 */
	Pala2(final double b) {
		this(b, 0.0, 0);
	}
}
