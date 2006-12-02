package shop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 三菱東京UFJ銀行の座標を取得するクラスです。
 * @author zenjiro
 * @since 4.13
 */
public class LatLongMUFJ extends LatLongMapion {

	@Override
	protected String getEncoding() {
		return "SJIS";
	}

	@Override
	protected String getLabel() {
		return "MUFJ";
	}

	@Override
	public String getPrefix() {
		return "latlong_mufj_";
	}

	@Override
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		return "http://www.mapion.co.jp/c/f?uc=21&pg=3&bool=admi2code*benefit1&grp=bk_mufg&ob=u&oi=key6%252Cedit5&admi2="
				+ cityID.substring(0, 2)
				+ "&benefit1="
				+ URLEncoder.encode(cityLabel, "EUC-JP")
				+ "&=%B8%A1%A1%A1%BA%F7";
	}

	@Override
	protected String getURLPattern() {
		return "<a href=\"(/c/f\\?uc=4&ino=[A-Z0-9]+&pg=1&grp=bk_mufg)\">[^A][^T][^M][^<>]+</a>";
	}

}
