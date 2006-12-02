/*
 * $Id: Rate.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */
package jp.jasminesoft.gcat.scalc;

/** 
 * Rateは、求点における縮率を求めるクラスです。
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
class Rate extends Pala2 {
	/**
	 * 求点のY座標
	 */
	double setY;

	/**
	 * 求点における縮率 
	 */
	double m;

	/**
	 * コンストラクタ
	 * @param b b
	 * @param l l
	 * @param setY 設定Y
	 */
	Rate(final double b, final double l, final double setY) {
		//スーパークラスPala2に求点の経緯度を渡します。
		super(b, l);
		//求点のY座標
		this.setY = setY;
		final double olive = Math.pow(setY, 2) / (2 * this.mrc * this.prc * Math.pow(this.m0, 2));
		final double mac = Math.pow(setY, 4)
				/ (24 * Math.pow(this.mrc, 2) * Math.pow(this.prc, 2) * Math.pow(this.m0, 4));
		//縮率
		this.m = this.m0 * (1.0 + olive + mac);
	}

	/**
	 * 求点における縮率を返すメソッド
	 * @return 求点における縮率
	 */
	double getM() {
		return this.m;
	}

	/**
	 * 縮率が正しいか判定するメソッド
	 * @return 縮率が正しいかどうか
	 */
	boolean hanteiM() {
		if (0.9999 <= this.m && this.m < 1.0001)
			return true;
		else
			return false;
	}
}
