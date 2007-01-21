package map;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

/**
 * 定数を集めたクラスです。
 * @author zenjiro
 * Created on 2005/05/03 9:07:02
 */
public class Const {
	/**
	 * 国土数値情報関係の定数を集めたクラスです。
	 * @author zenjiro
	 * 2005/11/14
	 */
	public static class Ksj {
		/**
		 * 国土数値情報の配布URL
		 */
		public static final String BASE_URL = "http://nlftp.mlit.go.jp/ksj/dls/data/";

		/**
		 * キャッシュディレクトリの相対パス
		 */
		public static final String CACHE_DIR = ".map" + File.separator + "ksj";

		/**
		 * 国土数値情報の行政界・海岸線の展開済みファイルの接頭語
		 */
		public static final String TXT_PREFIX = "N03-11A-2K_";

		/**
		 * 国土数値情報の行政界・海岸線の展開済みファイルの接尾語
		 */
		public static final String TXT_SUFFIX = ".txt";

		/**
		 * 国土数値情報の行政界・海岸線の圧縮ファイルの接頭語
		 */
		public static final String ZIP_PREFIX = "N03-11A-";

		/**
		 * 国土数値情報の行政界・海岸線の圧縮ファイルの接尾語
		 */
		public static final String ZIP_SUFFIX = "-01.0a.zip";

		/**
		 * 座標変換済みのキャッシュファイルの接頭語
		 */
		public static final String CACHE_PREFIX = "ksj_";

		/**
		 * 座標変換済みのキャッシュファイルの接尾語
		 */
		public static final String CACHE_SUFFIX = ".csv";

		/**
		 * 座標変換済みの高精度のキャッシュファイルの接尾語
		 */
		public static final String CACHE_PREFIX_FINE = "ksj_fine_";

		/**
		 * 座標変換済みの高精度のキャッシュファイルの接尾語
		 */
		public static final String CACHE_SUFFIX_FINE = ".csv";

		/**
		 * 鉄道データの曲線の接頭語
		 * @since 4.17
		 */
		public static final String RAILWAY_CURVES_PREFIX = "ksj_railway_curves_city_";

		/**
		 * 鉄道データの駅の接頭語
		 * @since 4.17
		 */
		public static final String RAILWAY_STATIONS_PREFIX = "ksj_railway_stations_city_";

		/**
		 * 鉄道データの接尾語
		 * @since 4.17
		 */
		public static final String RAILWAY_SUFFIX = ".csv";

		/**
		 * 道路データの接頭語
		 * @since 5.01
		 */
		public static final String ROAD_FINE_PREFIX = "ksj_road_city_";

		/**
		 * 簡略化した道路データの接頭語
		 * @since 5.01
		 */
		public static final String ROAD_SIMPLE_PREFIX = "ksj_road_simple_";

		/**
		 * 道路データの接尾語
		 * @since 5.01
		 */
		public static final String ROAD_SUFFIX = ".csv";

		/**
		 * 国土数値情報の標高・傾斜度3次メッシュに関する定数を集めたクラスです。
		 * @author zenjiro
		 * @since 5.00
		 */
		public static class Height {
			/**
			 * 幅
			 */
			public static final int WIDTH = 200;

			/**
			 * 高さ
			 */
			public static final int HEIGHT = 200;

			/**
			 * 接頭語
			 */
			public static final String PREFIX = "height_";

			/**
			 * 倍率1
			 */
			public static final double zoom1 = .0005;

			/**
			 * 倍率2
			 */
			public static final double zoom2 = .001;

			/**
			 * 倍率3
			 */
			public static final double zoom3 = .002;

			/**
			 * 倍率4
			 */
			public static final double zoom4 = .004;
		}

	}

	/**
	 * 数値地図2500関係の定数を集めたクラスです。
	 * @author zenjiro
	 * 2005/11/14
	 */
	public static class Sdf2500 {
		/**
		 * 数値地図2500（空間データ基盤）の配布URL 
		 */
		public static final String BASE_URL = "http://sdf.gsi.go.jp/";

		/**
		 * キャッシュディレクトリの相対パス 
		 */
		public static final String CACHE_DIR = ".map" + File.separator + "cache";

		/**
		 * 展開済みファイルの一覧を記録したファイル
		 */
		public static final String EXTRACTED_LOG_FILE = CACHE_DIR + File.separator + "extractedfiles.txt";

		/**
		 * ファイルの一覧が記載されているファイル
		 */
		public static final URL FILE_LIST = Const.class.getResource("files.csv");
	}

	/**
	 * 表示倍率関係の定数を集めたクラスです。
	 * @author zenjiro
	 * 2005/11/26
	 */
	public static class Zoom {
		/**
		 * 表示倍率の変化率
		 */
		public static final double RATE = 1.1;

		/**
		 * ディスプレイの解像度（dpi）
		 */
		public static final int RESOLUTION = Toolkit.getDefaultToolkit().getScreenResolution();

		/**
		 * 国土数値情報の市区町村データを読み込む倍率
		 */
		public static final double LOAD_CITIES = Const.Zoom.RESOLUTION / 2.5 * 100 / 900000;

		/**
		 * 高精度の国土数値情報の市区町村データを読み込む倍率
		 */
		public static final double LOAD_FINE_CITIES = Const.Zoom.RESOLUTION / 2.5 * 100 / 150000;

		/**
		 * 高精度の国土数値情報の道路データを読み込む表示倍率
		 */
		public static final double LOAD_FINE_ROAD = Const.Zoom.RESOLUTION / 2.5 * 100 / 75000;

		/**
		 * 丁目のポリゴンデータを読み込む表示倍率
		 */
		public static final double LOAD_GYOUSEI = Const.Zoom.RESOLUTION / 2.5 * 100 / 21000;

		/**
		 * 全てのデータを読み込む表示倍率 
		 */
		public static final double LOAD_ALL = Const.Zoom.RESOLUTION / 2.5 * 100 / 10000;

		/**
		 * 表示倍率の最大値
		 */
		public static final double MAX_VALUE = Const.Zoom.RESOLUTION / 2.5 * 100 / 2500;
	}

	/**
	 * フォント関係の定数を集めたクラスです。
	 * @author zenjiro
	 * 2006/06/25
	 */
	public static class Fonts {
		/**
		 * MS UI Gothicがあるかどうか
		 */
		public static final boolean HAS_MS_FONTS = Arrays.asList(
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains(
				"MS UI Gothic");

		/**
		 * ゴシック体のフォント
		 */
		public static final String GOTHIC = HAS_MS_FONTS ? "MS UI Gothic" : "SansSerif";

		/**
		 * 明朝体のフォント
		 */
		public static final String MINCHO = HAS_MS_FONTS ? "ＭＳ Ｐ明朝" : "Serif";
	}

	/**
	 * コンポーネント間の隙間[px]
	 */
	public static final int GAP = 4;

	/**
	 * 一度読み込んだ地図を捨てずに置いておく範囲
	 */
	public static final double KEEP_COFFICIENT = 1;

	/**
	 * 地図を先読みする範囲
	 */
	public static final double PRE_LOAD_COEFFICIENT = 0;

	/**
	 * 都道府県に関する定数を集めたクラスです。
	 * @author zenjiro
	 * @since 4.17
	 */
	public static class Prefecture {

		/**
		 * 全ての都道府県の情報が記載されているファイル
		 */
		public static final InputStream PREFECTURES = Const.class.getResourceAsStream(DIR + "prefectures.csv");

		/**
		 * 高精度の都道府県の情報が記載されているファイルの拡張子
		 */
		public static final String PREFECTURE_SUFFIX = ".csv";

		/**
		 * 高精度の都道府県の情報が記載されているファイルの接頭語
		 */
		public static final String PREFECTURE_PREFIX = "prefecture_";

	}

	/**
	 * URL関連のタイムアウト[ms]
	 */
	public static final int TIMEOUT = 5000;

	/**
	 * ビットマップキャッシュに関する定数を集めたクラスです。
	 * @author zenjiro
	 * @since 4.14
	 */
	public static class BitmapCache {
		/**
		 * ビットマップキャッシュを生成する表示倍率
		 */
		public static final double ZOOM = 0;

		/**
		 * ビットマップキャッシュの幅
		 */
		public static final int WIDTH = 200;

		/**
		 * ビットマップキャッシュの高さ
		 */
		public static final int HEIGHT = Const.BitmapCache.WIDTH;

		/**
		 * ビットマップキャッシュを保存するディレクトリ
		 */
		public static final String CACHE_DIR = ".map" + File.separator + "bitmap";

		/**
		 * ビットマップキャッシュの接頭辞
		 */
		public static final String PREFIX = "bitmap_6.1.3_";
	}

	/**
	 * 塗り分けに関する定数を集めたクラスです。
	 * @author zenjiro
	 * @since 4.16
	 */
	public static class Paint {
		/**
		 * 塗り分け情報を保存するディレクトリ
		 */
		public static final String CACHE_DIR = ".map" + File.separator + "paint";

		/**
		 * 塗り分け情報を記録したファイルの拡張子
		 */
		public static final String CACHE_SUFFIX = ".csv";

		/**
		 * 色が見つからなかったことを表す定数
		 */
		public static final int NOT_FOUND = -1;

		/**
		 * ファイルのエンコーディング
		 */
		public static final String ENCODING = "SJIS";
	}

	/**
	 * リソース中の位置
	 */
	public static final String DIR = "/data/";

	/**
	 * GUIに関する定数を集めたクラスです。
	 * @author zenjiro
	 * @since 5.02
	 */
	public static class GUI {
		/**
		 * サイドパネルの幅
		 */
		public static final int SIDE_PANEL_WIDTH = 200;

		/**
		 * フレームの幅
		 */
		public static final int FRAME_WIDTH = 640;

		/**
		 * フレームの高さ
		 */
		public static final int FRAME_HEIGHT = 480;

		/**
		 * プログレスバーの高さ
		 */
		public static final int PROGRESS_BAR_HEIGHT = 10;

		/**
		 * プログレスバーの幅
		 */
		public static final int PROGRESS_BAR_WIDTH = 150;
	}
	
	/**
	 * 地図の種類を列挙するクラスです。
	 * @author zenjiro
	 * @since 6.2.0
	 */
	public static class MapCategory {
	}

}
