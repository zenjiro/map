package map;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * プログレスバーを管理するクラスです。
 * @author zenjiro
 */
public class Progress {

	/**
	 * インスタンス
	 */
	private static Progress instance;

	/**
	 * プログレスバー
	 */
	private JProgressBar progressBar;

	/**
	 * プログレスバーの値
	 */
	private int value;

	/**
	 * 非公開のコンストラクタ
	 */
	private Progress() {
		this.progressBar = new JProgressBar();
		this.progressBar.setMaximum(300);
		this.progressBar.setPreferredSize(new Dimension(Const.GUI.PROGRESS_BAR_WIDTH, Const.GUI.PROGRESS_BAR_HEIGHT));
		this.progressBar.setVisible(false);
		this.status = Status.COMPLETE;
		this.startTime = Long.MAX_VALUE;
		new Timer(20, new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				if (Progress.this.value < Progress.this.progressBar.getValue()) {
					Progress.this.progressBar.setValue(Progress.this.value);
				} else if (Progress.this.value > Progress.this.progressBar.getValue()) {
					Progress.this.progressBar.setValue(Math.min(Progress.this.progressBar.getValue()
							+ 10, Progress.this.value));
					if (Progress.this.progressBar.getValue() == Progress.this.progressBar.getMaximum()) {
						Progress.this.progressBar.setVisible(false);
					}
				}
				if (Progress.this.status != Status.COMPLETE && System.currentTimeMillis() - Progress.this.startTime > 1000) {
					Progress.this.progressBar.setVisible(true);
				}
			}

		}).start();
	}

	/**
	 * @return インスタンス
	 */
	public static Progress getInstance() {
		if (Progress.instance == null) {
			Progress.instance = new Progress();
		}
		return Progress.instance;
	}

	/**
	 * @return プログレスバー
	 */
	public JProgressBar getProgressBar() {
		return this.progressBar;
	}

	/**
	 * プログレスバーを初期化します。
	 */
	public void initialize() {
		this.startTime = System.currentTimeMillis();
		this.progressBar.setValue(0);
		this.value = 0;
	}
	
	/**
	 * プログレスバーが動き始めた時刻
	 */
	private long startTime;

	/**
	 * プログレスバーを完了状態にします。
	 */
	public void complete() {
		this.status = Status.COMPLETE;
		this.value = this.progressBar.getMaximum();
		this.startTime = Long.MAX_VALUE;
	}

	/**
	 * 地図読み込みの進捗状況を設定します。
	 * @param value 進捗割合[%]
	 */
	public void setLoadMapPaintTyomeProgress(final int value) {
		if (this.status == Status.LOADING_MAP_PAINTING_TYOME) {
			this.value = value;
		}
	}

	/**
	 * ビットマップキャッシュ生成の進捗状況を設定します。
	 * @param value 進捗割合[%]
	 */
	public void setCreateBitmapProgress(final int value) {
		if (this.status == Status.CREATING_BITMAP) {
			this.value = 100 + value;
		}
	}

	/**
	 * 再描画の進捗状況を設定します。
	 * @param value 進捗割合[%]
	 */
	public void setRepaintProgress(final int value) {
		if (this.status == Status.REPAINTING) {
			this.value = 200 + value;
		}
	}

	/**
	 * 状態を表す列挙型です。
	 * @author zenjiro
	 */
	public enum Status {
		/**
		 * 地図を読み込んで町丁目を塗り分けている状態
		 */
		LOADING_MAP_PAINTING_TYOME,
		/**
		 * ビットマップキャッシュを生成している状態
		 */
		CREATING_BITMAP,
		/**
		 * 再描画中の状態
		 */
		REPAINTING,
		/**
		 * 完了した状態
		 */
		COMPLETE
	}

	/**
	 * 状態
	 */
	private Status status;

	/**
	 * @param status 状態
	 */
	public void setStatus(final Status status) {
		this.status = status;
	}

}
