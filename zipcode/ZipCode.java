package zipcode;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 郵便番号データを読み込むクラスです。
 * @author zenjiro
 * 2006/03/09
 */
public class ZipCode {
	/**
	 * 漢字表記と読みの対応表
	 */
	final Map<String, String> data;

	/**
	 * 郵便番号データを読み込むクラスを初期化します。
	 * @param cityID 市区町村コード
	 */
	public ZipCode(final String cityID) {
		this.data = new ConcurrentHashMap<String, String>();
		final Scanner scanner = new Scanner(this.getClass().getResourceAsStream(cityID.substring(0, 2) + ".csv"),
				"SJIS");
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			final String[] items = line.split("[,\"]+");
			if (items.length >= 9) {
				if (items[0].equals(cityID)) {
					final String kanji = this.normalize(items[8]);
					final String yomi = items[5];
					this.data.put(kanji, yomi);
				}
			}
		}
		scanner.close();
	}

	/**
	 * 読みを取得します。
	 * @param kanji 漢字
	 * @return 読み
	 */
	public String getYomi(final String kanji) {
		return this.data.get(this.normalize(kanji));
	}
	
	/**
	 * 表記揺れを吸収します。
	 * @param kanji 漢字
	 * @return 表記揺れを吸収した漢字
	 */
	private String normalize(final String kanji) {
		return kanji.replace("ヶ", "が").replace("ケ", "が").replace("ノ", "の").replace("之",
		"の").replace("乃", "の").replace("冶", "治").replace("け", "が").replace("祗", "祇");
	}
}
