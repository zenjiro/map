package ksj;

import java.awt.Shape;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 国土数値情報の道路データをCSVファイルに変換したものと、国土数値情報の市区町村界を
 * CSVファイルに変換したものを読み込んで、市区町村ごとに道路データを別のファイルに出力するプログラムです。
 * @author zenjiro
 * @since 5.04
 * 2007/01/06
 */
public class SplitRoadWithCities {

	/**
	 * モードを表す列挙型です。
	 * @author zenjiro
	 */
	public enum Mode {
		/**
		 * 道路データ
		 */
		ROAD,
		/**
		 * 鉄道データの曲線
		 */
		RAILWAY,
		/**
		 * 鉄道データの駅
		 */
		STATION;
	}

	/**
	 * メインメソッドです。
	 * @param args コマンドライン引数
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(final String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		for (final Mode mode : new Mode[] { Mode.STATION, Mode.RAILWAY }) {
			for (int i = 1; i < 48; i++) {
				final String prefectureID = new Formatter().format("%02d", i).toString();
				final Map<Shape, String> cities = ShapeIO.readShapes(new FileInputStream("data/cities_" + prefectureID
						+ ".csv"));
				final Map<Shape, String> roads = ShapeIO.readShapes(new FileInputStream(
						(mode == Mode.ROAD ? "data/ksj_road_" : mode == Mode.STATION ? "data/ksj_railway_stations_"
								: "data/ksj_railway_curves_")
								+ prefectureID + ".csv"));
				final Map<String, Map<Shape, String>> result = new HashMap<String, Map<Shape, String>>();
				for (final Map.Entry<Shape, String> city : cities.entrySet()) {
					for (final Map.Entry<Shape, String> road : roads.entrySet()) {
						if (city.getKey().intersects(road.getKey().getBounds2D())) {
							if (!result.containsKey(city.getValue())) {
								result.put(city.getValue(), new HashMap<Shape, String>());
							}
							result.get(city.getValue()).put(road.getKey(), road.getValue());
						}
					}
				}
				for (final Map.Entry<String, Map<Shape, String>> entry : result.entrySet()) {
					final String[] items = entry.getKey().split("_");
					if (items.length == 4) {
						final String cityID = items[2];
						final String outFile = (mode == Mode.ROAD ? "ksj_road_city_"
								: mode == Mode.STATION ? "ksj_railway_stations_city_" : "ksj_railway_curves_city_")
								+ cityID + ".csv";
						ShapeIO.writeShape(entry.getValue(), new FileOutputStream(outFile));
						System.out.println("wrote " + outFile);
					} else {
						System.out.println("bad city caption: " + entry.getKey());
					}
				}
			}
		}
	}
}
