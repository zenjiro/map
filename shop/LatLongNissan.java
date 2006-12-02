package shop;

import java.io.UnsupportedEncodingException;

/**
 * 日産レンタカー座標を取得するクラスです。
 * @author zenjiro
 * @since 4.13
 */
public class LatLongNissan extends LatLongMapion {

	@Override
	protected String getEncoding() {
		return "EUC-JP";
	}

	@Override
	protected String getLabel() {
		return "日産レンタカー";
	}

	@Override
	public String getPrefix() {
		return "latlong_nissan_";
	}

	@Override
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		return "http://www.mapion.co.jp/c/f?&uc=21&grp=nissanfs&bool=admi3code&oi=admi3code&ob=0&admi2code="
				+ cityID.indexOf(0, 2) + "&admi3code=" + cityID;
	}

	@Override
	protected String getURLPattern() {
		return "<a href=\"(/c/f\\?uc=4&pg=1&ino=[A-Z0-9]+&grp=nissanfs)\">";
	}

}
