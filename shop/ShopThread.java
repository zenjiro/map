package shop;

import isj.ISJUtil;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * 1つの店舗のホームページを解析して住所を抽出し、座標を求めるクラスです。
 * @author zenjiro
 * @since 4.11
 * 2006/08/05
 */
public class ShopThread implements Runnable {

	/**
	 * 住所解析エンジン
	 */
	private final AddressParser parser;

	/**
	 * 市区町村コード
	 */
	private final String cityID;

	/**
	 * 市区町村名
	 */
	private final String cityLabel;

	/**
	 * 都道府県名
	 */
	private final String prefectureLabel;

	/**
	 * 住所と座標の対応表
	 */
	final Map<String, Point2D> tempIsj;

	/**
	 * 座標と表示する店舗名の対応表（戻り値）
	 */
	final Map<Point2D, String> ret;

	/**
	 * 処理が完了した住所解析エンジンの一覧
	 */
	final Set<AddressParser> doneSet;

	/**
	 * 住所解析エンジンを与えてクラスを初期化します。
	 * @param parser 住所解析エンジン
	 * @param cityID 市区町村コード
	 * @param cityLabel 市区町村名
	 * @param prefectureLabel 都道府県名
	 * @param tempIsj 住所と座標の対応表
	 * @param ret 座標と表示する店舗名の対応表（戻り値)
	 * @param doneSet 処理が完了した住所解析エンジンの一覧
	 */
	public ShopThread(final AddressParser parser, final String cityID, final String cityLabel,
			final String prefectureLabel, final Map<String, Point2D> tempIsj, final Map<Point2D, String> ret,
			final Set<AddressParser> doneSet) {
		this.parser = parser;
		this.cityID = cityID;
		this.cityLabel = cityLabel;
		this.prefectureLabel = prefectureLabel;
		this.tempIsj = tempIsj;
		this.ret = ret;
		this.doneSet = doneSet;
	}

	public void run() {
		try {
			final String url = this.parser.getURL(this.cityID, this.cityLabel, this.prefectureLabel);
			if (!new File(Shop.CACHE_DIR).exists()) {
				new File(Shop.CACHE_DIR).mkdirs();
			}
			final String cacheFile = Shop.CACHE_DIR + File.separator + this.parser.getPrefix() + this.cityID
					+ Shop.SUFFIX;
			if (!new File(cacheFile).exists()) {
				final PrintWriter out = new PrintWriter(new File(cacheFile), "SJIS");
				ISJUtil.parseAddresses(this.parser.getAddresses(url), out, this.tempIsj);
				out.close();
			}
			final Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(new File(cacheFile)), "SJIS"));
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final String[] items = line.split(",");
				if (items.length == 4) {
					final double x = Double.parseDouble(items[2]);
					final double y = Double.parseDouble(items[3]);
					final Point2D.Double point = new Point2D.Double(x, y);
					if (!this.ret.containsKey(point)) {
						this.ret.put(point, this.parser.getLabel(items[1]));
					}
				} else {
					System.out.printf("ShopThread: %sのデータ形式が不正です：%s\n", cacheFile, line);
				}
			}
			scanner.close();
			this.doneSet.remove(this.parser);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
