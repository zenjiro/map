/*
 * $Id: MeshCodeManager.java,v 1.2 2002/03/17 10:52:42 nie Exp $
 */
package jp.jasminesoft.gcat.scalc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/** 
 * メッシュコードと平面直角座標系の系番号を管理するクラス。
 *
 * MeshCodeManager は、Hashtable クラス中に MeshInfo を
 * 管理しています。Java2 の HashMap は使わないようにしてください。
 * これは、本クラスを Java Applet 上から利用する場合を想定して
 * いるためです。
 *
 * @version $Revision: 1.2 $ $Date: 2002/03/17 10:52:42 $
 * @author  Yoshinori Nie
 */
public class MeshCodeManager {
	/** 自分自身へのポインタ */
	private static MeshCodeManager _instance;

	/** パラメータファイル名 */
	private String paramfile = "mesh.info";

	/** パラメータファイルのエンコーディング */
	private String enc = "SJIS";

	/** メッシュコードオブジェクトを管理するマップ */
	private static Hashtable<Integer, MeshInfo> meshMap;

	/**
	 * デフォルトコンストラクタ
	 */
	private MeshCodeManager() {
		meshMap = MeshParameter.map;
	}

	/** 
	 * インスタンスを得る。
	 *
	 * @return MeshCodeManager
	 */
	public static synchronized MeshCodeManager getInstance() {
		if (_instance == null) {
			_instance = new MeshCodeManager();
		}
		return _instance;
	}

	/**
	 * 読み込むパラメータファイルの指定
	 *
	 * @param paramfile パラメータファイル
	 */
	public void setParameterFilename(final String paramfile) {
		this.paramfile = paramfile;
	}

	/**
	 * 読み込むパラメータファイルを得る
	 *
	 * @return パラメータファイル
	 */
	public String getParameterFilename() {
		return this.paramfile;
	}

	/**
	 * 読み込むパラメータファイルの文字エンコードの設定
	 *
	 * @param enc エンコーディング
	 */
	public void setParameterFileEncoding(final String enc) {
		this.enc = enc;
	}

	/**
	 * 読み込むパラメータファイルの文字エンコードを得る
	 *
	 * @return エンコーディング
	 */
	public String getParameterFileEncoding() {
		return this.enc;
	}

	/**
	 * パラメータファイルからのデータの読み込み
	 */
	public void loadParameter() {
		final File f = new File(this.paramfile);
		if (!f.exists()) {
			System.err.println(this.paramfile + " is not found.");
			return;
		}
		meshMap = new Hashtable<Integer, MeshInfo>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(this.paramfile),
					this.enc));
			String line = null;
			while ((line = in.readLine()) != null) {
				this.addLine(line);
			}
		} catch (final UnsupportedEncodingException e) {
			System.err.println(this.enc + " is not supported encoding.");
		} catch (final IOException e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				in.close();
			} catch (final Exception ex) {
			}
		}
	}

	/**
	 * 読み込んだデータ行の登録。
	 * データは「メッシュ番号」「名称」「系」の順で記述されている。
	 *
	 * @param line 読み込んだデータ行
	 */
	private void addLine(final String line) {
		final StringTokenizer st = new StringTokenizer(line, ",");
		int meshCode = 0;
		String name = null;
		int kei = -1;
		while (st.hasMoreTokens()) {
			try {
				meshCode = Integer.parseInt(st.nextToken());
				name = st.nextToken();
				kei = Integer.parseInt(st.nextToken());
				meshMap.put(new Integer(meshCode), new MeshInfo(meshCode, name, kei));
			} catch (final Exception e) {
				System.err.println("Error: " + line);
			} finally {
				meshCode = 0;
				name = null;
				kei = -1;
			}
			break; // 残りのデータ読み込み処理はスキップさせる。
		}
	}

	/**
	 * メッシュコードからメッシュオブジェクトを入手する。
	 *
	 * @param meshCode メッシュコード
	 * @return メッシュオブジェクト
	 */
	public MeshInfo getMeshInfo(final int meshCode) {
		return meshMap.get(new Integer(meshCode));
	}

	/**
	 * キーの一覧を返す。
	 *
	 * @return キー一覧 (Enumeration)
	 */
	public Enumeration<Integer> keys() {
		return meshMap.keys();
	}

	/**
	 * メッシュテーブルが利用可能かどうかを調べる。
	 *
	 * @return メッシュテーブルに値が読み込まれていれば<code>true</code>を
	 * 返す。そうでなければ<code>false</code>を返す。
	 */
	public boolean isReady() {
		if (meshMap != null && meshMap.size() > 0)
			return true;
		return false;
	}

	/**
	 * 引数の緯度経度から、適当と思われる系番号を返す。
	 * 適当な系番号が見つからなかった場合、0 を返す。
	 *
	 * @param lat 緯度 (ddd.mmss 形式)
	 * @param lng 経度 (ddd.mmss 形式)
	 * @return 系番号
	 */
	public int getProperKei(final double lat, final double lng) {
		final int b = (int) ((int) lat * 1.5);
		final int l = (int) lng - 100;
		//System.out.println("b:"+b+",l:"+l);
		final String key_str = Integer.toString(b) + Integer.toString(l);
		final int key = new Integer(key_str).intValue();
		final MeshInfo minfo = this.getMeshInfo(key);
		//System.out.println(key+":"+minfo);
		if (minfo != null)
			return minfo.getKei();
		return 0;
	}
}
