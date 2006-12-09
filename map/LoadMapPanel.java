package map;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import map.Const.Sdf2500;

import web.WebUtilities;

/**
 * 読み込む地図を選択するパネルです。 
 * @author zenjiro
 * Created on 2005/05/13 8:27:58
 */
public class LoadMapPanel extends JPanel {
	/**
	 * 地区町村名とURLの対応表 
	 */
	final Map<String, URL> urls;

	/**
	 * 読み込む地図を選択するパネルを初期化します。
	 * @param maps 地図
	 * @param panel 地図を表示するパネル
	 * @param loadMap 地図を読み込むためのオブジェクト
	 * @throws IOException 入出力例外
	 */
	public LoadMapPanel(final Map<String, MapData> maps, final MapPanel panel, final LoadMap loadMap) throws IOException {
		this.urls = new ConcurrentHashMap<String, URL>();
		final JPanel keywordPanel = new JPanel();
		keywordPanel.setLayout(new BorderLayout(Const.GAP, Const.GAP));
		final JTextField keywordField = new JTextField();
		final JButton searchButton = new JButton("検索");
		keywordPanel.add(keywordField, BorderLayout.CENTER);
		keywordPanel.add(searchButton, BorderLayout.EAST);
		final JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout(Const.GAP, Const.GAP));
		final JList resultList = new JList();
		resultPanel.add(new JScrollPane(resultList));
		final JButton loadButton = new JButton("読み込み");
		this.setLayout(new GridBagLayout());
		this.add(keywordPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(Const.GAP,
						Const.GAP, 0, Const.GAP), 0, 0));
		this.add(resultPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(Const.GAP,
						Const.GAP, Const.GAP, Const.GAP), 0, 0));
		this.add(loadButton, new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.BOTH, new Insets(0, Const.GAP, Const.GAP, Const.GAP), 0, 0));
		final Map<String, Map<String, String>> files = WebUtilities.loadFileList(Sdf2500.FILE_LIST);
		keywordField.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				searchButton.doClick(200);
			}
		});
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					final String keyword = keywordField.getText();
					// test from here 4.12
					final Pattern latLongPattern = Pattern
							.compile("(tokyo: *)?([0-9]+\\.?[0-9]*), *([0-9]+\\.?[0-9]*)");
					final Pattern latLongDMSPattern = Pattern.compile(
					"(tokyo: *)?([0-9]+)/([0-9]+)/([0-9]+\\.?[0-9]*), *([0-9]+)/([0-9]+)/([0-9]+\\.?[0-9]*)"		
					);
					final Matcher latLongMatcher = latLongPattern.matcher(keyword);
					final Matcher latLongDMSMatcher = latLongDMSPattern.matcher(keyword);
					if (latLongMatcher.matches() || latLongDMSMatcher.matches()) {
						final double latitude;
						final double longitude;
						if (latLongMatcher.matches()) {
							latitude = Double.parseDouble(latLongMatcher.group(2));
							longitude = Double.parseDouble(latLongMatcher.group(3));
						} else {
							final int latInteger = Integer
									.parseInt(latLongDMSMatcher.group(2));
							final int latMinutes = Integer
									.parseInt(latLongDMSMatcher.group(3));
							final double latSeconds = Double
									.parseDouble(latLongDMSMatcher.group(4));
							final int longInteger = Integer
									.parseInt(latLongDMSMatcher.group(5));
							final int longMinutes = Integer
									.parseInt(latLongDMSMatcher.group(6));
							final double longSeconds = Double
									.parseDouble(latLongDMSMatcher.group(7));
							latitude = latInteger + latMinutes / 60.0
									+ latSeconds / 3600;
							longitude = longInteger + longMinutes
									/ 60.0 + longSeconds / 3600;
						}
						final Point2D point;
						if (keyword.startsWith("tokyo:")) {
							point = UTMUtil.toUTM(WGSUtil.tokyoToWGS(longitude, latitude));
						} else {
							point = UTMUtil.toUTM(longitude, latitude);
						}
						panel.moveTo(point.getX(), -point.getY());
						if (panel.getZoom() < Const.Zoom.LOAD_ALL) {
							panel.zoomDetail();
						}
						panel.getActionListener().actionPerformed(
								new ActionEvent(this, this.hashCode(), "move"));
						panel.repaint();
					}
					// test to here 4.12
					final Pattern pattern = Pattern.compile(keyword);
					final String baseURL = Const.Sdf2500.BASE_URL;
					LoadMapPanel.this.urls.clear();
					for (final Map.Entry<String, Map<String, String>> entry : files.entrySet()) {
						final String prefecture = entry.getKey();
						for (final Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
							final String city = entry2.getKey();
							final String filename = entry2.getValue();
							if (pattern.matcher(prefecture + city).find()) {
								LoadMapPanel.this.urls.put(prefecture + city, new URL(baseURL
										+ filename));
							}
						}
					}
					resultList.setListData(LoadMapPanel.this.urls.keySet().toArray());
					if (LoadMapPanel.this.urls.size() == 1) {
						resultList.setSelectedIndex(0);
						loadButton.doClick(200);
					}
				} catch (final IOException exception) {
					exception.printStackTrace();
				}
			}
		});
		resultList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					loadButton.doClick(200);
				}
			}
		});
		resultList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(final KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					loadButton.doClick(200);
				}
			}
		});
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					final Collection<URL> selectedURLs = new ArrayList<URL>();
					for (final Object o : resultList.getSelectedValues()) {
						selectedURLs.add(LoadMapPanel.this.urls.get(o));
					}
					maps.clear();
					final Collection<String> loadedMaps;
						loadedMaps = loadMap.loadMaps(selectedURLs, maps);
					panel.moveTo(panel.getBounds(loadedMaps));
					if (panel.getZoom() < Const.Zoom.LOAD_FINE_ROAD) {
						panel.zoomWide();
					}
					panel.getActionListener().actionPerformed(
							new ActionEvent(this, this.hashCode(), "load"));
					panel.repaint();
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}

		});
	}
}
