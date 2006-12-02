package map;

import java.awt.geom.Point2D;

import jp.jasminesoft.gcat.scalc.XY2LatLong;

/**
 * 座標変換を行うユーティリティクラスです。
 * @author zenjiro
 * Created on 2005/10/15
 */
public class JasmineUtil {

	/**
	 * 平面直角座標を緯度経度に変換します。
	 * @param point 点
	 * @param kei 座標系
	 * @return 変換後の点（経度、緯度）
	 */
	public static Point2D toLatLong(final Point2D point, final int kei) {
		return toLatLong(point.getX(), point.getY(), kei);
	}

	/**
	 * 平面直角座標を緯度経度に変換します。
	 * @param x x座標
	 * @param y y座標
	 * @param kei 座標系
	 * @return 変換後の点（経度、緯度）
	 */
	public static Point2D toLatLong(final double x, final double y, final int kei) {
		final XY2LatLong toLatLong = new XY2LatLong(kei);
		toLatLong.setX(x);
		toLatLong.setY(y);
		final double latitude = toLatLong.getLatitude();
		final double longtitude = toLatLong.getLongitude();
		return new Point2D.Double(longtitude, latitude);
	}

}
