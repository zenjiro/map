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
	 * メインメソッドです。
	 * @param args コマンドライン引数
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(final String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		for (int i = 1; i < 48; i++) {
			final String prefectureID = new Formatter().format("%02d", i).toString();
			final Map<Shape, String> cities = ShapeIO
					.readShapes(new FileInputStream("cities_" + prefectureID + ".csv"));
			final Map<Shape, String> roads = ShapeIO.readShapes(new FileInputStream("data/ksj_road_" + prefectureID
					+ ".csv"));
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
					ShapeIO.writeShape(entry.getValue(), new FileOutputStream("ksj_road_city_" + cityID + ".csv"));
					System.out.println("wrote " + "ksj_road_city_" + cityID + ".csv");
				} else {
					System.out.println("bad city caption: " + entry.getKey());
				}
			}
		}
	}
}
