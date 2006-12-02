/*
 * $Id: Pala.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */

package jp.jasminesoft.gcat.scalc;

/** 
 * Palaは、計算で必要な変数をまとめたクラスです。
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

class Pala {
    /**
     * 求点の緯度
     */
    protected double b;

    /**
     * 求点の経度
     */
    protected double l;

    /**
     * 座標系原点の経緯度を返すクラスです。
     */
    protected Coordinate coord;

    /**
     * 座標系原点の緯度
     */
    protected double gentenB;

    /**
     * 座標系原点の経度
     */
    protected double gentenL;

    /**
     * 求点経度と原点経度の差 (lambda)
     */
    protected double lam;  //経度の差

    /**
     * 緯度のtangent
     */
    protected double t;

    /**
     * 第二離心率 X 緯度のcosine
     */
    protected double eta;

    /**
     * 卯酉線曲率半径、子午線曲率半径を求める為の変数
     */
    protected double q;

    /**
     * 卯酉線曲率半径 (parallel radius of curvature)
     */
    protected double prc; 

    /**
     * 子午線曲率半径 (meridian radius of curvature)
     */
    protected double mrc; 

    /**
     * ベッセル楕円体の長半径 (semi-major axis)
     */
    protected double ra = 6377397.155;

    /**
     * ベッセル楕円体の短半径 (semi-minor axis)
     */
    protected double rb = 6356078.9363;

    /**
     * 離心率を求める為の変数
     */
    protected double dr = Math.sqrt((Math.pow(this.ra,2) - Math.pow(this.rb,2)));

    /**
     * 第一離心率 (eccentricity)
     */
    protected double e = this.dr / this.ra;

    /**
     * 第二離心率
     */
    protected double e1 = this.dr / this.rb;

    /**
     * 弧度法を度数法に変換する変数
     */
    protected double rho = 180.0 / Math.PI;

    /**
     * 縮率 (rate)（19座標系）
     */
    protected double m0 = 0.9999;
}
