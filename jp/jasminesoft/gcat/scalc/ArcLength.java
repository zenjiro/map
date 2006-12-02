/*
 * $Id: ArcLength.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */

package jp.jasminesoft.gcat.scalc;

/** 
 * ArcLengthは、緯度までの子午線弧長を求めるクラスです。
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

class ArcLength extends Pala {
    /* K1 から k6を求める */
    /**
     * K1
     */
    private final double  k1
       = 1.0
       + 3.0/4.0 * Math.pow(this.e,2)
       + 45.0/64.0 * Math.pow(this.e,4)
       + 175.0/256.0 *  Math.pow(this.e,6)
       + 11025.0/16384.0 * Math.pow(this.e,8)
       + 43659.0/65536.0 * Math.pow(this.e,10)
       + 693693.0/1048576.0 * Math.pow(this.e,12)
       + 19324305.0/29360128.0 * Math.pow(this.e,14)
       + 4927697775.0/7516192768.0 * Math.pow(this.e,16);

    /**
     * K2
     */
    private final double k2
       = 3.0/4.0 * Math.pow(this.e,2)
       + 15.0/16.0 * Math.pow(this.e,4)
       + 525.0/512.0 *  Math.pow(this.e,6)
       + 2205.0/2048.0 * Math.pow(this.e,8)
       + 72765.0/65536.0 * Math.pow(this.e,10)
       + 297297.0/262144.0 * Math.pow(this.e,12)
       + 135270135.0/117440512.0 * Math.pow(this.e,14)
       + 547521975.0/469762048.0 * Math.pow(this.e,16);

    /**
     * K3
     */
    private final double k3
       = 15.0/64.0 * Math.pow(this.e,4)
       + 105.0/256.0 * Math.pow(this.e,6)
       + 2205.0/4096.0 * Math.pow(this.e,8)
       + 10395.0/16384.0 * Math.pow(this.e,10)
       + 1486485.0/2097152.0 * Math.pow(this.e,12)
       + 45090045.0/5870256.0 * Math.pow(this.e,14)
       + 766530765.0/939524096.0 * Math.pow(this.e,16);

    /**
     * K4
     */
    private final double k4
       = 35.0/512.0 * Math.pow(this.e,6)
       + 315.0/2048.0 * Math.pow(this.e,8)
       + 31185.0/131072.0 * Math.pow(this.e,10)
       + 165165.0/524288.0 * Math.pow(this.e,12)
       + 45090045.0/117440512.0 * Math.pow(this.e,14)
       + 209053845.0/469762048.0 * Math.pow(this.e,16);

    /**
     * K5
     */
    private final double k5
       = 315.0/16384.0 * Math.pow(this.e,8)
       + 3465.0/65536.0 * Math.pow(this.e,10)
       + 99099.0/1048576.0 * Math.pow(this.e,12)
       + 4099095.0/29360128.0 * Math.pow(this.e,14)
       + 348423075.0/1879048192.0 * Math.pow(this.e,16);

    /**
     * K6
     */
    private final double k6
       = 693.0/131072.0 * Math.pow(this.e,10)
       + 9009.0/524288.0 * Math.pow(this.e,12)
       + 4099095.0/117440512.0 * Math.pow(this.e,14)
       + 26801775.0/469762048.0 * Math.pow(this.e,16);

    /**
     * K7
     */
    private final double k7
       = 3003.0/2097152.0 * Math.pow(this.e,12)
       + 315315.0/58720256.0 * Math.pow(this.e,14)
       + 11486475.0/939524096.0 * Math.pow(this.e,16);

    /**
     * K8
     */
    private final double k8
       = 45045.0/117440512.0 * Math.pow(this.e,14)
       + 765765.0/469762048.0 * Math.pow(this.e,16);

    /**
     * K9
     */
    private final double k9
       = 765765.0/7516192768.0 * Math.pow(this.e,16);

    /**
     * 赤道から、受け取った緯度(b)までの子午線の弧長を求めるメソッド
     *
     * @param b 緯度
     * @return 赤道から、受け取った緯度(b)までの子午線の弧長
     */
    protected double getArcLength(final double b) {
	final double arc = this.ra * (1.0 - Math.pow(this.e,2))
              *((this.k1 * b - this.k2/2.0 * Math.sin(2.0*b) + this.k3/4.0 * Math.sin(4.0*b)
              - this.k4/6.0 * Math.sin(6.0*b) + this.k5/8.0 * Math.sin(8.0*b)
              - this.k6/10.0 * Math.sin(10.0*b) + this.k7/12.0 * Math.sin(12.0*b)
              - this.k8/14.0 * Math.sin(14.0*b) + this.k9/16.0 * Math.sin(16.0*b)));
	return arc;
    }

    /**
     * 子午線弧長の差を返すメソッド
     *
     * @param b
     * @param gentenB
     * @return 子午線弧長の差
     */
    protected double getArcGap(final double b, final double gentenB){
	final double gap = this.getArcLength(b) - this.getArcLength(gentenB);
	return gap;
    }
}
