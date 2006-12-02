/*
 * $Id: XY2LatLong.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */
package jp.jasminesoft.gcat.scalc;

/** 
 * 平面直角座標値から緯度経度値を求めるクラスです。
 *
 * @version $Revision: 1.2 $ $Date: 2002/03/17 10:52:42 $
 * @author  Yoshinori Nie
 */
public class XY2LatLong {
	/** 平面直角座標系の X 座標値 */
	private double x;

	/** 平面直角座標系の Y 座標値 */
	private double y;

	/** 平面直角座標系の系番号 */
	private final int kei;

	/** 原点の緯度（ラジアン）*/
	private double gentenB;

	/** 原点の経度（度）*/
	private double gentenL;

	/** 求める緯度 */
	private Dms lat;

	/** 求める経度 */
	private Dms lng;

	/**
	 * コンストラクタ
	 *
	 * @param kei 平面直角座標系の系番号
	 */
	public XY2LatLong(final int kei) {
		final Coordinate coord = new Coordinate(kei);
		this.gentenB = coord.radianGB();
		this.gentenL = coord.angleGL();
		this.kei = kei;
	}

	/**
	 * 平面直角座標系の系番号を返す。
	 *
	 * @return 系番号
	 */
	public int getKei() {
		return this.kei;
	}

	/**
	 * 平面直角座標系の X 値をセットする。
	 *
	 * @param x 平面直角座標系の X 値
	 */
	public void setX(final double x) {
		this.reset();
		this.x = x;
	}

	/**
	 * 平面直角座標系の Y 値をセットする。
	 *
	 * @param y 平面直角座標系の Y 値
	 */
	public void setY(final double y) {
		this.reset();
		this.y = y;
	}

	/**
	 * 現在、セットされている平面直角座標系の X 値を得る。
	 *
	 * @return 平面直角座標系の X 値
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * 現在、セットされている平面直角座標系の Y 値を得る。
	 *
	 * @return 平面直角座標系の Y 値
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * 緯度値を得る。
	 *
	 * @return 緯度
	 */
	public double getLatitude() {
		if (this.lat == null)
			this.calc();
		return this.lat.getValue();
	}

	/**
	 * 緯度値を得る。
	 *
	 * @return 緯度(度分秒表記)
	 */
	public String getDMSLatitude() {
		if (this.lat == null)
			this.calc();
		return this.lat.getDMSValue();
	}

	/**
	 * 経度値を得る。
	 *
	 * @return 経度
	 */
	public double getLongitude() {
		if (this.lng == null)
			this.calc();
		return this.lng.getValue();
	}

	/**
	 * 経度値を得る。
	 *
	 * @return 経度(度分秒表記)
	 */
	public String getDMSLongitude() {
		if (this.lat == null)
			this.calc();
		return this.lng.getDMSValue();
	}

	/**
	 * 算出結果のリセット
	 */
	public void reset() {
		this.lng = null;
		this.lat = null;
	}

	/**
	 * 座標変換 (平面直角座標 -> 緯度経度)
	 */
	private void calc() {
		// Phiオブジェクト作成
		final Phi p = new Phi(this.gentenB, this.x);
		final double phi = p.getPhi();
		// LatLong オブジェクト作成
		final LatLong llcnv = new LatLong(this.y, phi, this.gentenL);
		this.lat = new Dms(llcnv.getB());
		this.lng = new Dms(llcnv.getL());
	}
}
