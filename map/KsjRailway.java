package map;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * 国土数値情報の鉄道データを扱うクラスです。
 * @author zenjiro
 * @since 4.17
 */
public class KsjRailway {

	/**
	 * 鉄道区分コードの列挙型です。
	 * @author zenjiro
	 * @since 4.17
	 */
	public enum Classification {
		/**
		 * JR
		 */
		JR,
		/**
		 * 私鉄
		 */
		PRIVATE,
		/**
		 * その他の鉄道
		 */
		OTHER,
		/**
		 * 不明
		 */
		UNKNOWN
	}

	/**
	 * 事業者種別コードの列挙型です。
	 * @author zenjiro
	 * @since 4.17
	 */
	public enum Business {
		/**
		 * 新幹線
		 */
		SHINKANSEN,
		/**
		 * JR在来線
		 */
		JR,
		/**
		 * 公営鉄道
		 */
		PUBLIC,
		/**
		 * 民営鉄道
		 */
		PRIVATE,
		/**
		 * 第3セクタ
		 */
		SEMI_PUBLIC,
		/**
		 * 高速道路
		 */
		ROAD_HIGHWAY,
		/**
		 * 国道
		 */
		ROAD_KOKUDO,
		/**
		 * 主要な道路
		 */
		ROAD_MAJOR,
		/**
		 * 不明
		 */
		UNKNOWN
	}

	/**
	 * 直線、曲線、駅が持つ共通の機能をまとめた抽象クラスです。
	 * @author zenjiro
	 * @since 4.17
	 */
	public static class Railway {

		/**
		 * 直線、曲線を初期化します。
		 * 表記は「鉄道区分コード_事業者種別コード_路線名_会社名」の形式である必要があります。
		 * @param shape 形
		 * @param caption 表記
		 */
		public Railway(final Shape shape, final String caption) {
			this.shape = shape;
			final String[] captions = caption.split("_");
			if (captions.length == 4) {
				final int classification = Integer.parseInt(captions[0]);
				switch (classification) {
				case 11:
					this.classification = Classification.JR;
					break;
				case 12:
					this.classification = Classification.PRIVATE;
					break;
				case 13:
				case 14:
				case 15:
				case 16:
				case 17:
				case 21:
				case 22:
				case 23:
				case 24:
				case 25:
					this.classification = Classification.OTHER;
					break;
				default:
					this.classification = Classification.UNKNOWN;
				}
				final int business = Integer.parseInt(captions[1]);
				switch (business) {
				case 1:
					this.business = Business.SHINKANSEN;
					break;
				case 2:
					this.business = Business.JR;
					break;
				case 3:
					this.business = Business.PUBLIC;
					break;
				case 4:
					this.business = Business.PRIVATE;
					break;
				case 5:
					this.business = Business.SEMI_PUBLIC;
					break;
				default:
					this.business = Business.UNKNOWN;
				}
				String company = captions[3].replaceFirst(".+旅客鉄道$", "JR").replaceFirst("^阪急電鉄$", "阪急").replaceFirst(
						"^阪神電気鉄道$", "阪神").replaceFirst("^神戸電鉄$", "神鉄").replaceFirst("^近畿日本鉄道$", "近鉄").replaceFirst(
						"電気鉄道$", "電鉄");
				String line = captions[2].replaceFirst("^本線$", "").replaceFirst("ケーブル線$", "ケーブル").replaceFirst("^智頭線$",
						"").replaceFirst("^神戸本線$", "神戸線").replaceFirst("^宝塚本線$", "宝塚線")
						.replaceFirst("モノレール線$", "モノレール").replaceFirst("^京都本線$", "京都線");
				if (line.endsWith("ケーブル") || line.endsWith("モノレール") || line.startsWith("JR")) {
					company = "";
				} else if (company.endsWith("新交通")) {
					line = "";
				}
				this.caption = company + line;
			} else if (captions.length == 1) {
				// since 5.01 道路のとき
				this.caption = captions[0];
				this.classification = Classification.UNKNOWN;
				if (this.caption.contains("高速")) {
					this.business = Business.ROAD_HIGHWAY;
				} else if (this.caption.startsWith("国道")) {
					this.business = Business.ROAD_KOKUDO;
				} else {
					this.business= Business.ROAD_MAJOR;
				}
			} else {
				throw new IllegalArgumentException(
						"Invalid caption: caption must be RAC_INT_LIN_OPC format. caption = " + caption);
			}
		}

		/**
		 * 形
		 */
		private Shape shape;

		/**
		 * 事業者種別コード
		 */
		Business business;

		/**
		 * 鉄道区分コード
		 */
		Classification classification;

		/**
		 * 表記
		 */
		String caption;
		
		/**
		 * 文字列の描画位置
		 */
		private Point2D captionLocation;

		/**
		 * @return 形
		 */
		public Shape getShape() {
			return this.shape;
		}

		/**
		 * @return 事業者種別コード
		 */
		public Business getBusiness() {
			return this.business;
		}

		/**
		 * @return 鉄道区分コード
		 */
		public Classification getClassification() {
			return this.classification;
		}

		/**
		 * @return 表記
		 */
		public String getCaption() {
			return this.caption;
		}

		public String toString() {
			return "[" + this.classification + ", " + this.business + ", " + this.caption + "]";
		}

		/**
		 * @return 文字列の描画位置
		 */
		public Point2D getCaptionLocation() {
			return this.captionLocation;
		}

		/**
		 * @param captionLocation 文字列の描画位置
		 */
		public void setCaptionLocation(Point2D captionLocation) {
			this.captionLocation = captionLocation;
		}
	}

	/**
	 * 駅を表すクラスです。
	 * @author zenjiro
	 * @since 4.17
	 */
	public static class Station extends Railway {

		/**
		 * 駅名
		 */
		private final String station;

		/**
		 * @return 駅名
		 */
		public String getStation() {
			return this.station;
		}

		/**
		 * 駅を初期化します。
		 * 表記は「鉄道区分コード_事業者種別コード_路線名_会社名_駅名」の形式である必要があります。
		 * @param shape 形
		 * @param caption 表記
		 */
		public Station(final Shape shape, final String caption) {
			super(shape, caption.replaceFirst("_[^_]+$", ""));
			this.station = caption.replaceFirst("^.+_", "");
		}

		public String toString() {
			return "[" + super.classification + ", " + super.business + ", " + super.caption + ", " + this.station + "]";
		}
	}

}
