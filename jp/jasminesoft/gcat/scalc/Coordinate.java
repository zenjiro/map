/*
 * $Id: Coordinate.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */

package jp.jasminesoft.gcat.scalc;

/** 
 * Coordinateは、座標系番号を受け取り、原点の緯度経度を返すクラスです。
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

class Coordinate {
    /**
     * 座標系原点の緯度
     */
    private double gentenB;

    /**
     * 座標系原点の経度
     */    
    private double gentenL;

    /**
     * コンストラクタ
     * @param i 不明
     */
    Coordinate(final int i){
	switch (i) {
	case 1:
	    this.gentenB = 330000.0;
	    this.gentenL = 1293000.0;
	    break;
	case 2:
	    this.gentenB = 330000.0;
	    this.gentenL = 1310000.0;
	    break;
	case 3:
	    this.gentenB = 360000.0;
	    this.gentenL = 1321000.0;
	    break;
	case 4:
	    this.gentenB = 330000.0;
	    this.gentenL = 1333000.0;
	    break;
	case 5:
	    this.gentenB = 360000.0;
	    this.gentenL = 1342000.0;
	    break;
	case 6:
	    this.gentenB = 360000.0;
	    this.gentenL = 1360000.0;
	    break;
	case 7:
	    this.gentenB = 360000.0;
	    this.gentenL = 1371000.0;
	    break;
	case 8:
	    this.gentenB = 360000.0;
	    this.gentenL = 1383000.0;
	    break;
	case 9:
	    this.gentenB = 360000.0;
	    this.gentenL = 1395000.0;
	    break;
	case 10:
	    this.gentenB = 400000.0;
	    this.gentenL = 1405000.0;
	    break;
	case 11:
	    this.gentenB = 440000.0;
	    this.gentenL = 1401500.0;
	    break;
	case 12:
	    this.gentenB = 440000.0;
	    this.gentenL = 1421500.0;
	    break;
	case 13:
	    this.gentenB = 440000.0;
	    this.gentenL = 1441500.0;
	    break;
	case 14:
	    this.gentenB = 260000.0;
	    this.gentenL = 1420000.0;
	    break;
	case 15:
	    this.gentenB = 260000.0;
	    this.gentenL = 1273000.0;
	    break;
	case 16:
	    this.gentenB = 260000.0;
	    this.gentenL = 1240000.0;
	    break;
	case 17:
	    this.gentenB = 260000.0;
	    this.gentenL = 1310000.0;
	    break;
	case 18:
	    this.gentenB = 200000.0;
	    this.gentenL = 1360000.0;
	    break;
	case 19:
	    this.gentenB = 260000.0;
	    this.gentenL = 1540000.0;
	    break;
	} 
    }

    /**
     * 座標系原点の緯度を返すメソッド
     * @return 座標系原点の緯度
     */
    protected double getGB(){
	return this.gentenB;
    }

    /**
     * 座標系原点の経度を返すメソッド
     * @return 座標系原点の経度
     */
    protected double getGL(){
	return this.gentenL;
    }

    /**
     * 座標系原点の緯度（ラジアン）を返すメソッド
     * @return 座標系原点の緯度（ラジアン）
     */
    protected double radianGB(){
	final Radians rb0 = new Radians(this.gentenB);
	return rb0.getRadian();
    }

    /**
     * 座標系原点の経度（ラジアン）を返すメソッド
     * @return 座標系原点の経度（ラジアン）
     */
    protected double radianGL(){
	final Radians rl0 = new Radians(this.gentenL);
	return rl0.getRadian();
    }

    /**
     * 座標系原点の緯度（度）を返すメソッド
     * @return 座標系原点の緯度（度）
     */
    protected double angleGB(){
	final Radians ab0 = new Radians(this.gentenB);
	return ab0.getAngle();
    }
 
    /**
     * 座標系原点の経度（度）を返すメソッド
     * @return 座標系原点の経度（度）
     */
    protected double angleGL(){
	final Radians al0 = new Radians(this.gentenL);
	return al0.getAngle();
    }
}
