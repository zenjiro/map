/*
 * This source code is written in UTF-8.
 * To compile, type 'javac -encoding UTF-8 *.java'.
 * To run, type 'java Main'.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.swing.UIManager;
import javax.swing.WindowConstants;

import map.BackgroundThread;
import map.Const;
import map.LoadMap;
import map.MainFrame;
import map.MapData;
import map.MapPanel;
import search.CellSearch;
import search.Search;

/*
 * Map
 * 国土地理院の数値地図2500（空間データ基盤）を表示するプログラムです。
 * Copyright (C) 2003-2010 zenjiro
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 * 地図を表示するプログラムです。
 * @author  zenjiro
 */
public class Main {
	/**
	 * プログラム本体です。
	 * @param args コマンドライン引数
	 * @throws Exception 例外
	 */
	public static void main(final String[] args) throws Exception {
		final Map<String, MapData> maps = new ConcurrentHashMap<String, MapData>();
		final Search search = new CellSearch(2000, 1500);
		final MapPanel panel = new MapPanel(maps);
		panel.setFocusable(true);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final LoadMap loadMap = new LoadMap();
		final MainFrame frame = new MainFrame(maps, panel, loadMap);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(Const.GUI.FRAME_WIDTH, Const.GUI.FRAME_HEIGHT);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		panel.addMessage("地図を読み込んでいます。");
		panel.init();
		final List<String> list = new ArrayList<String>();
		final String baseDir = args.length > 0 ? args[0] : "data";
		if (new File(baseDir).isDirectory()) {
			final String[] files = new File(baseDir).list();
			final Pattern pattern = Pattern.compile("[0-9][0-9][a-zA-Z][a-zA-Z][0-9][0-9][0-9]");
			for (int i = 0; i < files.length; i++) {
				if (pattern.matcher(files[i]).matches()) {
					list.add(files[i].toLowerCase());
				}
			}
		}
		for (final String mapName : list) {
			final MapData map = new MapData(baseDir, mapName);
			maps.put(mapName, map);
			search.insert(map.getBounds(), mapName);
		}
		panel.setSearch(search);
		panel.calcMinMaxXY();
		panel.zoomAutomaticaly();
		panel.moveToCenter();
		final Timer timer = new Timer();
		timer.schedule(new BackgroundThread(maps, panel, loadMap), 0, 200);
		panel.removeMessage();
	}
}
