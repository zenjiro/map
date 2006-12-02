package shop;

import java.io.UnsupportedEncodingException;

/**
 * りそな銀行の座標を取得するクラスです。
 * @author zenjiro
 * @since 4.13
 */
public class LatLongResona extends LatLongMapion {

	@Override
	protected String getEncoding() {
		return "SJIS";
	}

	@Override
	protected String getLabel() {
		return "りそな";
	}

	@Override
	public String getPrefix() {
		return "latlong_resona_";
	}

	@Override
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		return "http://www.mapion.co.jp/c/f?vp=20&p=1&grp=resona&uc=21&ob=0&mx=500&bool=admi2code&pg=&sfn=resona_search-partinfo-list_00&oi=edit4&admi2code="
				+ cityID.substring(0, 2) + "&admi3=" + cityID;
	}

	@Override
	protected String getURLPattern() {
		return "<a href=\"(/c/f\\?uc=4&grp=resona&ino=[A-Z0-9]+)\">";
	}
}
