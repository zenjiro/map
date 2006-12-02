/*
 * $Id: MeshInfo.java,v 1.2 2002/03/17 14:07:31 nie Exp $
 */

package jp.jasminesoft.gcat.scalc;

/** 
 * メッシュコードを管理するクラス。
 *
 * @version $Revision: 1.2 $ $Date: 2002/03/17 14:07:31 $
 * @author  Yoshinori Nie
 */

public class MeshInfo {
    /** メッシュコード */
    private int code;

    /** メッシュ名称 */
    private String name;

    /** 系 */
    private int kei;

    /** 系における本メッシュの最小 X 座標値 (単位:メートル) */
    private double x1;

    /** 系における本メッシュの最小 Y 座標値 (単位:メートル) */
    private double y1;

    /** 系における本メッシュの最大 X 座標値 (単位:メートル) */
    private double x2;

    /** 系における本メッシュの最大 Y 座標値 (単位:メートル) */
    private double y2;

    /**
     * コンストラクタ
     *
     * @param code メッシュコード
     * @param name 名称
     * @param kei 系
     */
    public MeshInfo(final int code, final String name, final int kei) {
	this.code = code;
	this.name = name;
	this.kei = kei;
    }

    /**
     * コンストラクタ
     *
     * @param code メッシュコード
     * @param name 名称
     * @param kei 系
     * @param x1 このメッシュの最小 X 値 (メートル)
     * @param y1 このメッシュの最小 Y 値 (メートル)
     * @param x2 このメッシュの最大 X 値 (メートル)
     * @param y2 このメッシュの最大 Y 値 (メートル)
     */
    public MeshInfo(final int code, final String name, final int kei, final double x1, final double y1,
		    final double x2, final double y2) 
    {
	this.code = code;
	this.name = name;
	this.kei = kei;
	this.x1 = x1;
	this.y1 = y1;
	this.x2 = x2;
	this.y2 = y2;
    }
    /**
     * メッシュコードを返す。
     *
     * @return メッシュコード
     */
    public final int getMeshCode() { return this.code; }

    /**
     * 名称を返す。
     *
     * @return 名称
     */
    public final String getName() { return this.name; }

    /**
     * 系を返す。
     *
     * @return 系
     */
    public final int getKei() { return this.kei; }

    /**
     * このメッシュの最小 X 値(単位：メートル)を返す。
     *
     * @return このメッシュの最小 X 値(単位：メートル)
     */
    public final double getX1() { return this.x1; }

    /**
     * このメッシュの最小 Y 値(単位：メートル)を返す。
     *
     * @return このメッシュの最小 Y 値(単位：メートル)
     */
    public final double getY1() { return this.y1; }

    /**
     * このメッシュの最大 X 値(単位：メートル)を返す。
     *
     * @return このメッシュの最大 X 値(単位：メートル)
     */
    public final double getX2() { return this.x2; }

    /**
     * このメッシュの最大 Y 値(単位：メートル)を返す。
     *
     * @return このメッシュの最大 Y 値(単位：メートル)
     */
    public final double getY2() { return this.y2; }

    /**
     * ハッシュコードを返す。
     *
     * @return ハッシュコード
     */
    @Override
	public int hashCode() { return this.code; }

    /**
     * メッシュコードが等しいかどうかを調べる。
     *
     * @param o 比較対象オブジェクト
     * @return 引数のオブジェクトのメッシュコード値が等しければ
     * <code>true</code>を返す。そうでなければ<code>false</code>を
     * 返す。
     * @exception ClassCastException
     */
    @Override
	public boolean equals(final Object o) {
	return (this.code == ((MeshInfo)o).getMeshCode());
    }

    /**
     * デバッグ用表示メソッド
     *
     * @return デバッグ用文字列
     */
    @Override
	public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("<MeshInfo code=\"");
	sb.append(this.code);
	sb.append("\" name=\"");
	sb.append(this.name);
	sb.append("\" kei=\"");
	sb.append(this.kei);
	sb.append("\" x1=\"" + this.x1);
	sb.append("\" y1=\"" + this.y1);
	sb.append("\" x2=\"" + this.x2);
	sb.append("\" y2=\"" + this.y2);
	sb.append("\">");
	return sb.toString();
    }
}
