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
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import map.Const.Zoom;

/**
 * 読み込む地図を選択するパネルです。 
 * @author zenjiro
 * Created on 2005/05/13 8:27:58
 */
public class IsjPanel extends JPanel {
	/**
	 * 読み込む地図を選択するパネルを初期化します。
	 * @param prefectures 都道府県の一覧
	 * @param panel 地図を表示するパネル
	 */
	public IsjPanel(final Collection<Prefecture> prefectures, final MapPanel panel) {
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
		final JButton goButton = new JButton("移動");
		this.setLayout(new GridBagLayout());
		this.add(keywordPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(Const.GAP,
						Const.GAP, 0, Const.GAP), 0, 0));
		this.add(resultPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(Const.GAP,
						Const.GAP, Const.GAP, Const.GAP), 0, 0));
		this.add(goButton, new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.BOTH, new Insets(0, Const.GAP, Const.GAP, Const.GAP), 0, 0));
		final Map<String, Point2D> result = new TreeMap<String, Point2D>();
		keywordField.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				searchButton.doClick(200);
			}
		});
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					result.clear();
					final String keyword = keywordField.getText();
					final Pattern pattern = Pattern.compile(keyword);
					for (final Prefecture prefecture : prefectures) {
						if (prefecture.hasCities()) {
							for (final City city : prefecture.getCities()) {
								if (city.hasIsj()) {
									for (final Map.Entry<String, Point2D> entry : city.getIsj()
											.entrySet()) {
										final String[] strings = entry.getKey().split(",");
										if (strings.length == 4) {
											final String caption = strings[2] + strings[3];
											if (pattern.matcher(caption).find()) {
												result.put(caption, entry.getValue());
											}
										} else {
											System.out.println("WARNING: 街区レベル位置参照情報の形式が不正です。" + entry.getKey());
										}
									}
								}
							}
						}
					}
					resultList.setListData(result.keySet().toArray(new String[] {}));
					if (result.size() == 1) {
						resultList.setSelectedIndex(0);
						goButton.doClick(200);
					}
				} catch (final UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (final IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		resultList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					goButton.doClick(200);
				}
			}
		});
		resultList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(final KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					goButton.doClick(200);
				}
			}
		});
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				double minX = Double.POSITIVE_INFINITY;
				double minY = Double.POSITIVE_INFINITY;
				double maxX = Double.NEGATIVE_INFINITY;
				double maxY = Double.NEGATIVE_INFINITY;
				for (final Object o : resultList.getSelectedValues()) {
					final Point2D point = result.get(o);
					minX = Math.min(minX, point.getX());
					minY = Math.min(minY, point.getY());
					maxX = Math.max(maxX, point.getX());
					maxY = Math.max(maxY, point.getY());
				}
				panel.moveTo((minX + maxX) / 2, (minY + maxY) / 2);
				if (Math.min(maxX - minX, maxY - minY) * Zoom.LOAD_ALL < Math.min(panel.getWidth(),
						panel.getHeight())) {
					panel.zoomDetail();
				} else {
					panel.zoomWide();
				}
				panel.repaint();
			}
		});
	}
}
