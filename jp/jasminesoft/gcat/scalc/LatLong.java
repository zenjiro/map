/*
 * $Id: LatLong.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */

package jp.jasminesoft.gcat.scalc;

/** 
 * LatLong は、XY座標を経緯度に変換するクラスです。
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

class LatLong extends Pala2 {
    /**
     * 求点のY座標 / 縮率(0.9999)
     */
    private double ym;

    /**
     * 求点座標の垂足緯度
     */
    private double phi;

    /**
     * 座標系原点の経度（度数法）
     */
    private double gentenL1;

    /**
     * コンストラクタ。スーパークラスのPala2を初期化します。
     * @param y 求点のY座標
     * @param phi 求点座標の垂足緯度
     * @param gentenL 座標系原点の経度
     */
    LatLong(final double y, final double phi, final double gentenL) {
	//Pala2 にphi（求点座標の垂足緯度）を渡します
	super(phi); 
	this.ym = y / this.m0;
	this.phi = phi;
	this.gentenL1 = gentenL;
    }

    /**
     * X座標を緯度に変換するメソッド
     *
     * @return 緯度 
     */
    protected double getB(){
	final double B1 = this.phi * this.rho 
              - (Math.pow(this.ym,2) * this.t / (2.0 * this.mrc * this.prc)) * this.rho
              + (Math.pow(this.ym,4) * this.t * (5.0 + 3.0 * Math.pow(this.t,2) + Math.pow(this.eta,2)
              - 9.0 * Math.pow(this.eta,2) * Math.pow(this.t,2) - 4.0 * Math.pow(this.eta,4))
              / ( 24.0 * this.mrc * Math.pow(this.prc,3))) * this.rho;

	final double B2 = (Math.pow(this.ym,6) * this.t * (61.0 + 90.0 * Math.pow(this.t,2)
              + 45.0 * Math.pow(this.t,4) + 46.0 * Math.pow(this.eta,2) 
              - 252.0 * Math.pow(this.t,2) * Math.pow(this.eta,2) 
              - 90.0 * Math.pow(this.t,4) * Math.pow(this.eta,2)) 
              / (720.0 * this.mrc * Math.pow(this.prc,5))) * this.rho;

	return (B1 - B2);
    }

    /**
     * Y座標を経度に変換するメソッド
     *
     * @return 経度
     */ 
    protected double getL(){
	final double L1 = this.ym / ( this.prc * Math.cos(this.phi)) * this.rho
              - (Math.pow(this.ym,3) * (1.0 + 2.0 * Math.pow(this.t,2) + Math.pow(this.eta,2))
              / (6.0 * Math.pow(this.prc,3) * Math.cos(this.phi))) * this.rho;

	final double L2 = (Math.pow(this.ym,5) * (5.0 + 28.0 * Math.pow(this.t,2) + 24.0 * Math.pow(this.t,4)
              + 6.0 * Math.pow(this.eta,2) + 8.0 * Math.pow(this.t,2) * Math.pow(this.eta,2))
              / (120.0 * Math.pow(this.prc,5) * Math.cos(this.phi))) * this.rho;

	//System.out.println("LatLong::getL(), gentenL="+gentenL+",L1="+L1+",L2="+L2);
	return (this.gentenL1 + L1 + L2);
    }
}

