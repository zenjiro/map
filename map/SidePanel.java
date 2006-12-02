package map;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * サイドパネルです。
 * @author zenjiro
 * Created on 2005/05/13 8:08:20
 */
public class SidePanel extends JPanel {
	/**
	 * コンストラクタです。
	 * @param maps 地図
	 * @param panel 地図を表示するパネル
	 * @param loadMap 地図を読み込むためのオブジェクト
	 * @throws IOException 入出力例外 
	 */
	public SidePanel(final Map<String, MapData> maps, final MapPanel panel, final LoadMap loadMap) throws IOException {
		this.setLayout(new BorderLayout());
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("読み込み", new LoadMapPanel(maps, panel, loadMap));
		tabbedPane.addTab("検索", new SearchPanel(maps, panel));
		tabbedPane.addTab("住所", new IsjPanel(panel.getPrefectures(), panel));
		this.add(tabbedPane);
	}
}
