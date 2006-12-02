/*
 * $Id: Dms.java,v 1.3 2002/03/18 08:49:00 nie Exp $
 */
package jp.jasminesoft.gcat.scalc;

/** 
 * Dmsは、度数法の単位を度から、度分秒に変換するクラスです。
 *
 * 本パッケージは、琉球大学工学部情報工学科 宮城研究室の成果物
 * を、ジャスミンソフトが整理・統合したものです。再利用を快諾
 * して頂いた宮城 隼夫教授以下、宮城研究室のスタッフにこの場を
 * 借りて感謝致します。
 * 
 * @version $Revision: 1.3 $ $Date: 2002/03/18 08:49:00 $
 * @author  Miho Nagata
 * @author  Yoshinori Nie
 */
class Dms {
	/**
	 * 度
	 */
	private int deg;

	/**
	 * 分
	 */
	private int min;

	/**
	 * 秒
	 */
	private double sec;

	/**
	 * コンストラクタ
	 * @param degree 度
	 */
	Dms(final double degree) {
		//整数部（度）を取り出す
		this.deg = (int) degree;
		//度を除いた部分を分へ
		this.min = (int) ((degree - this.deg) * 60.0);
		if (this.min < 0)
			this.min = 0;
		//残りの部分を秒に変換
		this.sec = (degree - this.deg - this.min / 60.0) * 3600.0;
		if (this.sec < 0)
			this.sec = 0;
		//System.out.println("deg="+deg+",min="+min+",sec="+sec);
	}

	/**
	 * 度を返すメソッド
	 * @return 度
	 */
	protected int getDeg() {
		return this.deg;
	}

	/**
	 * 分を返すメソッド
	 * @return 分
	 */
	protected int getMin() {
		return this.min;
	}

	/**
	 * 秒を返すメソッド
	 * @return 秒
	 */
	protected double getSec() {
		return this.sec;
	}

	/**
	 * 10 進法での値を返す。
	 * @return 10 進法での値
	 */
	protected double getValue() {
		return (this.deg + this.min / 60.0 + this.sec / 3600.0);
	}

	/**
	 * 度分秒表記での値を返す。
	 * @return 度分秒表記での値
	 */
	protected String getDMSValue() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.deg);
		sb.append("\u00b0");
		final String min_str = "0" + Integer.toString(this.min);
		sb.append(min_str.substring(min_str.length() - 2, min_str.length()));
		sb.append("\u0027");
		String sec_str = Double.toString(this.sec);
		final int e_pos = sec_str.indexOf("E");
		if (e_pos > 0) {
			final String kt = sec_str.substring(e_pos + 1 + 1); // "E-xx"
			final StringBuffer sec_sb = new StringBuffer();
			try {
				int kt_s = Integer.parseInt(kt);
				for (; kt_s > 0; --kt_s)
					sec_sb.append('0');
				for (int i = 0; i < sec_str.length(); i++) {
					final char ch = sec_str.charAt(i);
					if (ch == 'E')
						break;
					if (ch != '.')
						sec_sb.append(ch);
				}
				sec_str = sec_sb.toString();
			} catch (final NumberFormatException e) {
			}
		}
		for (int i = 0; i < Math.min(6, sec_str.length()); i++) {
			final char ch = sec_str.charAt(i);
			if (i == 2)
				sb.append('.');
			if (ch != '.')
				sb.append(ch);
		}
		//System.out.println("DMS::getDMSValue() " + deg + "/" + min + "/" + sec);
		return sb.toString();
	}
}
