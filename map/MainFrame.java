package map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.print.PrintException;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import route.Route;

/**
 * 地図を表示するフレームです。
 * @author zenjiro
 * Created on 2003/11/01, 16:00
 */
public class MainFrame extends JFrame {
	/**
	 * 終了の実装です。
	 * @author zenjiro
	 * Created on 2005/02/28 17:49:02
	 */
	class ExitListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			System.exit(0);
		}
	}

	/**
	 * ラスタ画像に出力する実装です。
	 * @author zenjiro
	 * Created on 2005/02/28 15:39:42
	 */
	class ImageExportListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			try {
				final JFileChooser chooser = new JFileChooser(new File("."));
				chooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public boolean accept(final File file) {
						return file.getName().toLowerCase().endsWith(".png")
								|| file.getName().toLowerCase().endsWith(".jpg")
								|| file.getName().toLowerCase().endsWith(".bmp");
					}

					@Override
					public String getDescription() {
						return "ラスタ画像ファイル（*.png、*.jpg、*.bmp）";
					}
				});
				chooser.showDialog(MainFrame.this, "出力");
				final File selectedFile = chooser.getSelectedFile();
				if (selectedFile != null) {
					final String fileName = selectedFile.getName();
					if (fileName != null) {
						if (fileName.toLowerCase().endsWith(".bmp")) {
							MainFrame.this.panel.printRaster(selectedFile, "bmp");
						} else if (fileName.toLowerCase().endsWith(".jpg")) {
							MainFrame.this.panel.printRaster(selectedFile, "jpg");
						} else if (fileName.toLowerCase().endsWith(".png")) {
							MainFrame.this.panel.printRaster(selectedFile, "png");
						}
					}
				}
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 印刷の実装です。
	 */
	class PrintListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			try {
				MainFrame.this.panel.print();
			} catch (final PrintException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * PSファイルを出力する実装です。
	 * @author zenjiro
	 * Created on 2005/03/20 16:42:34
	 */
	class PSExportListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			try {
				final JFileChooser chooser = new JFileChooser(new File("."));
				chooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public boolean accept(final File file) {
						return file.getName().toLowerCase().endsWith(".svg")
								|| file.getName().toLowerCase().endsWith(".ps");
					}

					@Override
					public String getDescription() {
						return "ベクトル画像ファイル（*.svg、*.ps）";
					}
				});
				chooser.showDialog(MainFrame.this, "出力");
				final File selectedFile = chooser.getSelectedFile();
				if (selectedFile != null) {
					final String fileName = selectedFile.getName();
					if (fileName != null) {
						if (fileName.toLowerCase().endsWith(".svg")) {
							MainFrame.this.panel.printSVG(selectedFile);
						} else if (fileName.toLowerCase().endsWith(".ps")) {
							MainFrame.this.panel.printPS(selectedFile);
						} else {
							MainFrame.this.panel.printSVG(new File(fileName + ".svg"));
						}
					}
				}
			} catch (final PrinterException e1) {
				e1.printStackTrace();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 地図を表示するパネル
	 */
	final MapPanel panel;

	/**
	 * ステータスバー
	 */
	private final JLabel statusBar;

	/**
	 * ステータスバーを納めるパネル
	 * @since 5.02
	 */
	private final JPanel statusPanel;
	
	/**
	 * 新しくフレームを初期化します。
	 * @param maps 地図
	 * @param panel 地図を表示するパネル
	 * @param loadMap 地図を読み込むためのオブジェクト
	 * @throws IOException 入出力例外
	 */
	public MainFrame(final Map<String, MapData> maps, final MapPanel panel, final LoadMap loadMap) throws IOException {
		this.panel = panel;
		this.setTitle("Map");
		this.setLayout(new BorderLayout());
		final JSplitPane splitPane = new JSplitPane();
		this.add(splitPane, BorderLayout.CENTER);
		final JPanel sidePanel = new SidePanel(maps, panel, loadMap);
		sidePanel.setPreferredSize(new Dimension(Const.GUI.SIDE_PANEL_WIDTH, 0));
		sidePanel.setMinimumSize(new Dimension(0, 0));
		splitPane.setLeftComponent(sidePanel);
		splitPane.setRightComponent(panel);
		splitPane.setOneTouchExpandable(true);
		final JMenuBar menuBar = new JMenuBar();
		this.add(menuBar, BorderLayout.NORTH);
		final JMenu fileMenu = new JMenu("ファイル(F)");
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);
		final JMenuItem imageExportItem = new JMenuItem("ラスタ画像として出力（PNG、JPEG、BMPファイル）(I)...");
		imageExportItem.setMnemonic('I');
		imageExportItem.addActionListener(new ImageExportListener());
		fileMenu.add(imageExportItem);
		final JMenuItem psExportItem = new JMenuItem("ベクトル画像として出力（SVG、PSファイル）(E)...");
		psExportItem.setMnemonic('E');
		psExportItem.addActionListener(new PSExportListener());
		fileMenu.add(psExportItem);
		fileMenu.addSeparator();
		final JMenuItem printItem = new JMenuItem("印刷(P)...");
		printItem.setMnemonic('P');
		printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		printItem.addActionListener(new PrintListener());
		fileMenu.add(printItem);
		fileMenu.addSeparator();
		final JMenuItem exitItem = new JMenuItem("終了(X)");
		exitItem.setMnemonic('X');
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
		exitItem.addActionListener(new ExitListener());
		fileMenu.add(exitItem);
		final JMenu viewMenu = new JMenu("表示(V)");
		viewMenu.setMnemonic('V');
		menuBar.add(viewMenu);
		final JMenuItem zoomInItem = new JMenuItem("拡大(I)");
		zoomInItem.setMnemonic('I');
		zoomInItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.SHIFT_DOWN_MASK
				| InputEvent.CTRL_DOWN_MASK));
		zoomInItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.zoomIn();
				panel.forceRepaint();
			}
		});
		viewMenu.add(zoomInItem);
		final JMenuItem zoomOutItem = new JMenuItem("縮小(O)");
		zoomOutItem.setMnemonic('O');
		zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		zoomOutItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.zoomOut();
				panel.forceRepaint();
			}
		});
		viewMenu.add(zoomOutItem);
		viewMenu.addSeparator();
		final JMenuItem zoomWholeItem = new JMenuItem("全域表示(H)");
		zoomWholeItem.setMnemonic('H');
		zoomWholeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
		zoomWholeItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.zoomWhole();
				panel.forceRepaint();
			}
		});
		viewMenu.add(zoomWholeItem);
		final JMenuItem zoomWideItem = new JMenuItem("広域表示（1/75000）(W)");
		zoomWideItem.setMnemonic('W');
		zoomWideItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));
		zoomWideItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.zoomWide();
				panel.forceRepaint();
			}
		});
		viewMenu.add(zoomWideItem);
		final JMenuItem zoomMiddleItem = new JMenuItem("中域表示（1/21000）(M)");
		zoomMiddleItem.setMnemonic('M');
		zoomMiddleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));
		zoomMiddleItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.zoomMiddle();
				panel.forceRepaint();
			}
		});
		viewMenu.add(zoomMiddleItem);
		final JMenuItem zoomDetailItem = new JMenuItem("詳細表示（1/10000）(D)");
		zoomDetailItem.setMnemonic('D');
		zoomDetailItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK));
		zoomDetailItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.zoomDetail();
				panel.forceRepaint();
			}
		});
		viewMenu.add(zoomDetailItem);
		viewMenu.addSeparator();
		final JMenuItem moveNorthItem = new JMenuItem("北へ移動(R)");
		moveNorthItem.setMnemonic('R');
		moveNorthItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK));
		moveNorthItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.scroll(0, -MainFrame.this.getHeight() / 4);
				panel.forceRepaint();
			}
		});
		viewMenu.add(moveNorthItem);
		final JMenuItem moveSouthItem = new JMenuItem("南へ移動(U)");
		moveSouthItem.setMnemonic('U');
		moveSouthItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK));
		moveSouthItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.scroll(0, MainFrame.this.getHeight() / 4);
				panel.forceRepaint();
			}
		});
		viewMenu.add(moveSouthItem);
		final JMenuItem moveWestItem = new JMenuItem("西へ移動(T)");
		moveWestItem.setMnemonic('T');
		moveWestItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK));
		moveWestItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.scroll(-MainFrame.this.getWidth() / 4, 0);
				panel.forceRepaint();
			}
		});
		viewMenu.add(moveWestItem);
		final JMenuItem moveEastItem = new JMenuItem("東へ移動(E)");
		moveEastItem.setMnemonic('E');
		moveEastItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK));
		moveEastItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.scroll(MainFrame.this.getWidth() / 4, 0);
				panel.forceRepaint();
			}
		});
		viewMenu.add(moveEastItem);
		viewMenu.addSeparator();
		final JMenuItem fontSizeIncrementItem = new JMenuItem("文字を大きく(L)");
		fontSizeIncrementItem.setMnemonic('L');
		fontSizeIncrementItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.increaseFontSize();
				panel.forceRepaint();
			}
		});
		viewMenu.add(fontSizeIncrementItem);
		final JMenuItem fontSizeDecrementItem = new JMenuItem("文字を小さく(S)");
		fontSizeDecrementItem.setMnemonic('S');
		fontSizeDecrementItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.decreaseFontSize();
				panel.forceRepaint();
			}
		});
		viewMenu.add(fontSizeDecrementItem);
		final JMenuItem fontSizeResetItem = new JMenuItem("文字を標準の大きさに(N)");
		fontSizeResetItem.setMnemonic('N');
		fontSizeResetItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.resetFontSize();
				panel.forceRepaint();
			}
		});
		viewMenu.add(fontSizeResetItem);
		viewMenu.addSeparator();
		final JMenuItem darkerItem = new JMenuItem("色を鮮やかに(V)");
		darkerItem.setMnemonic('V');
		darkerItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.increaseSaturation();
				panel.forceRepaint();
			}
		});
		viewMenu.add(darkerItem);
		final JMenuItem brighterItem = new JMenuItem("色を淡く(G)");
		brighterItem.setMnemonic('G');
		brighterItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.decreaseSaturation();
				panel.forceRepaint();
			}
		});
		viewMenu.add(brighterItem);
		final JMenuItem brightnessResetItem = new JMenuItem("標準の色合い(F)");
		brightnessResetItem.setMnemonic('F');
		brightnessResetItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.resetSaturation();
				panel.forceRepaint();
			}
		});
		viewMenu.add(brightnessResetItem);
		viewMenu.addSeparator();
		final JCheckBoxMenuItem centerMarkItem = new JCheckBoxMenuItem("中心点(C)");
		centerMarkItem.setMnemonic('C');
		centerMarkItem.addActionListener(new ActionListener(){
			public void actionPerformed(final ActionEvent e) {
				panel.toggleCenterMark();
				centerMarkItem.setSelected(panel.isCenterMark());
				panel.setChanged();
				panel.forceRepaint();
			}});
		viewMenu.add(centerMarkItem);
		final JMenu toolMenu = new JMenu("ツール(T)");
		toolMenu.setMnemonic('T');
		menuBar.add(toolMenu);
		final JCheckBoxMenuItem routeModeMenuItem = new JCheckBoxMenuItem("ルート探索モード(R)");
		routeModeMenuItem.setMnemonic('R');
		routeModeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				panel.toggleRouteMode();
				routeModeMenuItem.setSelected(panel.isRouteMode());
				panel.setChanged();
				panel.forceRepaint();
			}
		});
		toolMenu.add(routeModeMenuItem);
		toolMenu.addSeparator();
		final ButtonGroup routeButtonGroup = new ButtonGroup();
		final JRadioButtonMenuItem highwayRouteMenuItem = new JRadioButtonMenuItem("高速道路モード(H)");
		highwayRouteMenuItem.setMnemonic('H');
		highwayRouteMenuItem.setSelected(true);
		highwayRouteMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Route.getInstance().setSpeed(Route.HIGHWAY_SPEED);
				if (panel.isRouteMode()) {
					Route.getInstance().calcRoute();
					panel.setChanged();
					panel.forceRepaint();
				}
			}
		});
		routeButtonGroup.add(highwayRouteMenuItem);
		toolMenu.add(highwayRouteMenuItem);
		final JRadioButtonMenuItem normalRouteMenuItem = new JRadioButtonMenuItem("一般道モード(N)");
		normalRouteMenuItem.setMnemonic('N');
		normalRouteMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Route.getInstance().setSpeed(Route.NORMAL_SPEED);
				if (panel.isRouteMode()) {
					Route.getInstance().calcRoute();
					panel.setChanged();
					panel.forceRepaint();
				}
			}
		});
		routeButtonGroup.add(normalRouteMenuItem);
		toolMenu.add(normalRouteMenuItem);
		final JRadioButtonMenuItem bikeRouteMenuItem = new JRadioButtonMenuItem("自転車モード(B)");
		bikeRouteMenuItem.setMnemonic('B');
		bikeRouteMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Route.getInstance().setSpeed(Route.BIKE_SPEED);
				if (panel.isRouteMode()) {
					Route.getInstance().calcRoute();
					panel.setChanged();
					panel.forceRepaint();
				}
			}
		});
		routeButtonGroup.add(bikeRouteMenuItem);
		toolMenu.add(bikeRouteMenuItem);
		final JRadioButtonMenuItem walkRouteMenuItem = new JRadioButtonMenuItem("徒歩モード(W)");
		walkRouteMenuItem.setMnemonic('W');
		walkRouteMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Route.getInstance().setSpeed(Route.WALK_SPEED);
				if (panel.isRouteMode()) {
					Route.getInstance().calcRoute();
					panel.setChanged();
					panel.forceRepaint();
				}
			}
		});
		routeButtonGroup.add(walkRouteMenuItem);
		toolMenu.add(walkRouteMenuItem);
		toolMenu.addSeparator();
		final JMenuItem clearItem = new JMenuItem("地点とルートを消去(C)");
		clearItem.setMnemonic('C');
		clearItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Route.getInstance().clearRoute();
				panel.setChanged();
				panel.forceRepaint();
			}
		});
		toolMenu.add(clearItem);
		this.statusBar = new JLabel(panel.getMessage());
		this.statusPanel = new JPanel();
		this.statusPanel.setLayout(new BorderLayout());
		this.statusPanel.add(this.statusBar, BorderLayout.CENTER);
		this.statusPanel.add(Progress.getInstance().getProgressBar(), BorderLayout.EAST);
		this.add(this.statusPanel, BorderLayout.SOUTH);
		panel.setStatusBar(this.statusBar);
	}
}
