package shop;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * 店舗のホームページを解析して緯度経度を抜き出すクラスが実装するべきメソッドを定義したインターフェイスです。
 * @author zenjiro
 * @since 4.12
 * 2006/08/17
 */
public interface LatLongParser {
	/**
	 * @return キャッシュファイル名の接頭辞
	 */
	public String getPrefix();

	/**
	 * @param cityID 市区町村コード
	 * @param cityLabel 市区町村名
	 * @param prefectureLabel 都道府県名
	 * @return 取得するべきURL
	 * @throws UnsupportedEncodingException 未対応エンコーディング例外
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException;

	/**
	 * @param url 店舗一覧ページのURL
	 * @return 店舗の座標の一覧
	 * @throws IOException 入出力例外
	 */
	public Collection<Location> getLocations(final String url) throws IOException;

	/**
	 * @param url 店舗別ページの一覧が記載されているページのURL
	 * @return 店舗別ページの一覧
	 * @throws IOException 入出力例外
	 */
	public Collection<String> getURLs(final String url) throws IOException;

	/**
	 * @param url 店舗別ページのURL
	 * @return 店舗の座標と表示する文字列
	 * @throws IOException 入出力例外
	 */
	public Location getLocation(final String url) throws IOException;

	/**
	 * 店舗の座標と表示する文字列をカプセル化するクラスです。
	 * @author zenjiro
	 * @since 4.12
	 * 2006/08/17
	 */
	public static class Location {
		/**
		 * 店舗の座標
		 */
		private Point2D location;

		/**
		 * 表示する文字列
		 */
		private String caption;

		/**
		 * @return 店舗の座標
		 */
		public Point2D getLocation() {
			return this.location;
		}

		/**
		 * @return 表示する文字列
		 */
		public String getCaption() {
			return this.caption;
		}

		@Override
		public String toString() {
			return this.caption + "@" + this.location;
		}

		/**
		 * @param location 店舗の座標
		 * @param caption 表示する文字列
		 */
		public Location(final Point2D location, final String caption) {
			this.location = location;
			this.caption = caption;
		}
	}

}
