/*
 * $Id: Radians.java,v 1.4 2002/03/19 00:42:31 nie Exp $
 */
package jp.jasminesoft.gcat.scalc;

/** 
 * 与えられた度数法の経緯度を、弧度法で返すクラスです。
 * angle 値を求める際に、わずかな誤差が発生します。
 *
 * 本パッケージは、琉球大学工学部情報工学科 宮城研究室の成果物
 * を、ジャスミンソフトが整理・統合したものです。再利用を快諾
 * して頂いた宮城 隼夫教授以下、宮城研究室のスタッフにこの場を
 * 借りて感謝致します。
 * 
 * @version $Revision: 1.4 $ $Date: 2002/03/19 00:42:31 $
 * @author  Miho Nagata
 * @author  Yoshinori Nie
 */
public class Radians {
	/**
	 * dddmmss.sをddd.mmsssに修正した経緯度 
	 */
	private double degree;

	/**
	 * 単位を度に変換した経緯度
	 */
	private double angle;

	/**
	 * ラジアンに変換した経緯度
	 */
	private double radian;

	/**
	 * Math.PI / 180
	 */
	private static final double k = Math.PI / 180.0;

	/**
	 * コンストラクタ
	 * @param setDeg 度
	 */
	public Radians(final double setDeg) {
		this.degree = setDeg / 10000.0;
		/*
		 * 度の部分を得る
		 */
		final int deg = (int) this.degree;
		//System.out.println("deg:"+deg);
		/*
		 * 分の部分を得る
		 */
		//min = (int)((degree - deg)*100.0);
		//System.out.println("min:"+min);
		final double n_min0 = setDeg - (deg * 10000.0);
		final int n_min = (int) (n_min0 / 100.0);
		//System.out.println("min:"+n_min);
		/* 
		 * 秒の部分を得る 
		 */
		//sec = ((degree - deg)*100 - min)*100;
		//System.out.println("sec:"+sec);
		final double n_sec = (setDeg - (deg * 10000.0) - (n_min * 100.0));
		//System.out.println("nie sec:"+n_sec);
		/*
		 * 単位を度へ 
		 * ここでわずかですが、誤差が発生します。
		 */
		//angle = (deg + min/60.0 + sec/3600.0);
		//System.out.println("miho angle:"+angle);
		this.angle = (deg + n_min / 60.0 + n_sec / 3600.0);
		//System.out.println("nie angle:"+angle);
		/**
		 * ラジアンに変換
		 */
		this.radian = this.angle * k;
		//System.out.println("radian:"+radian);
	}

	/**
	 * 経緯度をラジアンに変換した値を返すメソッド
	 *
	 * @return ラジアン値
	 */
	public double getRadian() {
		return this.radian;
	}

	/**
	 * 経緯度（単位は度）を返すメソッド
	 * @return 度
	 */
	public double getAngle() {
		return this.angle;
	}
}
