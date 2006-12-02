package map;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import java.awt.geom.Point2D;

/**
 * UTM座標に関するユーティリティクラスです。
 * @author zenjiro
 * Created on 2005/10/22
 */
public class UTMUtil {
	/**
	 * 赤道半径
	 */
	private static final double a = 6378137;

	/**
	 * 偏平率
	 */
	private static final double f = 1.0 / 298.257222101;

	/**
	 * 地球楕円体の離心率を2乗したもの
	 */
	private static final double e2 = 2 * f - f * f;

	/**
	 * 地球楕円体の離心率を4乗したもの
	 */
	private static final double e4 = e2 * e2;

	/**
	 * 地球楕円体の離心率を6乗したもの
	 */
	private static final double e6 = e4 * e2;

	/**
	 * 地球楕円体の離心率を8乗したもの
	 */
	private static final double e8 = e6 * e2;

	/**
	 * 地球楕円体の離心率を10乗したもの
	 */
	private static final double e10 = e8 * e2;

	/**
	 * 中央子午線
	 */
	private static final double lambda0 = (139 + 44 / 60.0 + 28.8759 / 3600) * PI / 180;

	/**
	 * A'
	 */
	private static final double a_ = 1 + 3 / 4 * e2 + 45 / 64 * e4 + 175 / 256 * e6 + 11025 / 16384
			* e8 + 43659 / 65536 * e10;

	/**
	 * B'
	 */
	private static final double b_ = 3 / 4 * e2 + 15 / 16 * e4 + 525 / 512 * e6 + 2205 / 2048 * e8
			+ 72765 / 65536 * e10;

	/**
	 * C'
	 */
	private static final double c_ = 15 / 64 * e4 + 105 / 256 * e6 + 2205 / 4096 * e8 + 10395
			/ 16384 * e10;

	/**
	 * D'
	 */
	private static final double d_ = 35 / 512 * e6 + 315 / 2048 * e8 + 31185 / 131072 * e10;

	/**
	 * E'
	 */
	private static final double e_ = 315 / 16384 * e8 + 3465 / 65536 * e10;

	/**
	 * F'
	 */
	private static final double f_ = 693 / 131072 * e10;

	/**
	 * 第2離心率の2乗
	 */
	private static final double e_2 = e2 / (1 - e2);

	/**
	 * 縮尺係数
	 */
	private static final double m0 = .9996;

	/**
	 * 緯度経度をUTM座標に変換します。
	 * @param longitude 経度（度単位）
	 * @param latitude 緯度（度単位）
	 * @return 緯度経度をUTM座標に変換した点
	 */
	public static Point2D toUTM(final double longitude, final double latitude) {
		final double lambda = longitude * PI / 180;
		final double phi = latitude * PI / 180;
		final double largeB = a
				* (1 - e2)
				* (a_ * phi - b_ / 2 * sin(2 * phi) + c_ / 4 * sin(4 * phi) - d_ / 6 * sin(6 * phi)
						+ e_ / 8 * sin(8 * phi) - f_ / 10 * sin(10 * phi));
		final double sinPhi = sin(phi);
		final double largeN = a / sqrt(1 - e2 * sinPhi * sinPhi);
		final double t = tan(phi);
		final double t2 = t * t;
		final double t4 = t * t * t * t;
		final double t6 = t * t * t * t * t * t;
		final double cosPhi = cos(phi);
		final double eta2 = e_2 * cosPhi * cosPhi;
		final double eta4 = eta2 * eta2;
		final double l = lambda - lambda0;
		final double l2 = l * l;
		final double l3 = l2 * l;
		final double l4 = l3 * l;
		final double l5 = l4 * l;
		final double l6 = l5 * l;
		final double l7 = l6 * l;
		final double l8 = l7 * l;
		final double x = largeN * l * cosPhi + largeN * l3 / 6 * pow(cos(phi * (1 - t2 + eta2)), 3)
				+ largeN * l5 / 120
				* pow(cos(phi * (5 - 18 * t2 + t4 + 14 * eta2 - 58 * t2 * eta2)), 5) + largeN * l7
				/ 5040 * pow(cos(phi * (61 - 479 * t2 + 179 * t4 * t6)), 7);
		final double y = largeB + largeN * l2 / 2 * sinPhi * cosPhi + largeN * l4 / 24 * sinPhi
				* pow(cos(phi * (5 - t2 + 9 * eta2 + 4 * eta4)), 3) + largeN * l6 / 720 * sinPhi
				* pow(cos(phi * (61 - 58 * t2 + t4 + 270 * eta2 - 330 * t2 * eta2)), 5) + largeN
				* l8 / 40320 * sinPhi * pow(cos(phi * (1385 - 3111 * t2 + 543 * t4 - t6)), 7);
		final double largeX = m0 * x + 500000;
		final double largeY = m0 * y;
		return new Point2D.Double(largeX, largeY);
	}
	
	/**
	 * 経度緯度をUTM座標に変換します。
	 * @param point （経度、緯度）を表す点
	 * @return 経度緯度をUTM座標に変換した点
	 */
	public static Point2D toUTM(final Point2D point) {
		return toUTM(point.getX(), point.getY());
	}
}
