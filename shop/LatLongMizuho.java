package shop;

import java.io.UnsupportedEncodingException;

/**
 * みずほ銀行の座標を取得するクラスです。
 * @author zenjiro
 * @since 4.13
 */
public class LatLongMizuho extends LatLongMapion {

	@Override
	protected String getEncoding() {
		return "EUC-JP";
	}

	@Override
	protected String getLabel() {
		return "みずほ";
	}

	@Override
	public String getPrefix() {
		return "latlong_mizuho_";
	}

	@Override
	public String getURL(final String cityID, final String cityLabel, final String prefectureLabel)
			throws UnsupportedEncodingException {
		return "http://vip.mapion.co.jp/c/f?uc=21&admi3code=" + cityID
		+ "&bool=(%2Badmi3code)&grp=mizuho&oi=key6&ob=0&mx=230";
	}

	@Override
	protected String getURLPattern() {
		return "<a href=\"(/c/f\\?uc=4&pg=1&ino=[A-Z0-9]+&grp=mizuho)\">[^<>]+[^出][^張][^所]</a>";
	}

}
