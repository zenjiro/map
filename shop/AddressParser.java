package shop;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 店舗の住所を解析するクラスが実装するべきメソッドを定義したインターフェイスです。
 * @since 4.07
 * @author zenjiro
 * 2006/07/21
 */
public interface AddressParser {
	/**
	 * @return キャッシュファイル名の接頭辞
	 */
	public String getPrefix();
	
	/**
	 * @param shopName 店舗名
	 * @return 描画する文字列
	 */
	public String getLabel(final String shopName);
	
	/**
	 * @return ホームページのエンコーディング
	 */
	public String getEncoding();
	
	/**
	 * @param cityID 市区町村コード
	 * @param cityLabel 市区町村名
	 * @param prefectureLabel 都道府県名
	 * @return 取得するべきURL
	 * @throws UnsupportedEncodingException 未対応エンコーディング例外
	 */
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel) throws UnsupportedEncodingException;
	
	/**
	 * @param url 取得するべきURL
	 * @return 住所をキー、店舗名を値とする対応表
	 * @throws IOException 入出力例外
	 */
	public Map<String, String> getAddresses(final String url) throws IOException;
}
