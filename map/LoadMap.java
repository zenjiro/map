package map;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jp.gr.java_conf.dangan.util.lha.LhaHeader;
import jp.gr.java_conf.dangan.util.lha.LhaInputStream;
import map.Const.Zoom;
import web.WebUtilities;

/**
 * 地図を読み込むクラスです。
 * @author zenjiro
 * 作成日: 2004/01/09
 */
public class LoadMap {
	/**
	 * 変更されたかどうか
	 */
	private boolean isChanged;

	/**
	 * 地図を描画するパネル
	 */
	private MapPanel panel;

	/** 
	 * 地図を読み込む必要があれば読み込み、開放する必要があれば開放します。
	 * このメソッドを呼び出した直後に isChanged() を呼び出すと、
	 * このメソッドによって地図の状態が変化したかどうかが取得できます。
	 * @param maps 地図
	 * @param panel パネル
	 * @param visibleRectangle 表示されている領域（仮想座標）
	 * @throws IOException 
	 */
	public void loadMap(final Map<String, MapData> maps, final MapPanel panel, final Rectangle2D visibleRectangle) throws IOException {
		this.panel = panel;
		final double zoom = panel.getZoom();
		this.isChanged = false;
		for (final MapData mapData : maps.values()) {
			final Rectangle2D preLoadRectangle = new Rectangle2D.Double(visibleRectangle.getX()
					- (visibleRectangle.getWidth() * Const.PRE_LOAD_COEFFICIENT), visibleRectangle.getY()
					- (visibleRectangle.getHeight() * Const.PRE_LOAD_COEFFICIENT), visibleRectangle.getWidth()
					+ (visibleRectangle.getWidth() * Const.PRE_LOAD_COEFFICIENT * 2), visibleRectangle.getHeight()
					+ (visibleRectangle.getHeight() * Const.PRE_LOAD_COEFFICIENT * 2));
			final Rectangle2D keepRectangle = new Rectangle2D.Double(visibleRectangle.getX()
					- (visibleRectangle.getWidth() * Const.KEEP_COFFICIENT), visibleRectangle.getY()
					- (visibleRectangle.getHeight() * Const.KEEP_COFFICIENT), visibleRectangle.getWidth()
					+ (visibleRectangle.getWidth() * Const.KEEP_COFFICIENT * 2), visibleRectangle.getHeight()
					+ (visibleRectangle.getHeight() * Const.KEEP_COFFICIENT * 2));
			// データを開放する
			if (zoom < Zoom.LOAD_GYOUSEI) {
				if (mapData.hasGyousei()) {
					mapData.freeSi_tyo();
					mapData.freeTyome();
					mapData.freeGyousei();
					this.isChanged = true;
				}
			}
			if (zoom < Zoom.LOAD_ALL) {
				if (mapData.hasRoadArc()) {
					mapData.freeRoadArc();
					mapData.freeOthers();
					mapData.freeEki();
					this.isChanged = true;
				}
				if (mapData.hasMizuArc()) {
					mapData.freeMizu();
					mapData.freeMizuArc();
					this.isChanged = true;
				}
				if (mapData.hasTatemonoArc()) {
					mapData.freeTatemono();
					mapData.freeTatemonoArc();
					this.isChanged = true;
				}
				if (mapData.hasZyouti()) {
					mapData.freeZyouti();
					this.isChanged = true;
				}
			}
			if (mapData.getBounds().intersects(preLoadRectangle)) {
				// データを読み込む
				if (zoom >= Zoom.LOAD_GYOUSEI) {
					if (!mapData.hasGyousei()) {
						mapData.loadGyousei();
						//mapData.loadSi_tyo();
						mapData.loadTyome();
						this.isChanged = true;
					}
				}
				if (zoom >= Zoom.LOAD_ALL) {
					if (!mapData.hasEki()) {
						mapData.loadEki();
						this.isChanged = true;
					}
					if (!mapData.hasOthers()) {
						mapData.loadOthers();
						this.isChanged = true;
					}
					if (!mapData.hasRoadArc()) {
						mapData.loadRoadArc();
						this.isChanged = true;
					}
					if (!mapData.hasMizuArc()) {
						mapData.loadMizuArc();
						mapData.loadMizu();
						this.isChanged = true;
					}
					if (!mapData.hasTatemonoArc()) {
						mapData.loadTatemonoArc();
						mapData.loadTatemono();
						this.isChanged = true;
					}
					if (!mapData.hasZyouti()) {
						mapData.loadZyouti();
						this.isChanged = true;
					}
				}
				if (zoom >= Zoom.LOAD_2500) {
					// since 5.01 何もしない。
				}
			} else if (!mapData.getBounds().intersects(keepRectangle)) {
				// データを開放する
				if (mapData.hasGyousei()) {
					mapData.freeSi_tyo();
					mapData.freeTyome();
					mapData.freeGyousei();
				}
				if (mapData.hasMizuArc()) {
					mapData.freeMizu();
					mapData.freeMizuArc();
				}
				if (mapData.hasOthers()) {
					mapData.freeZyouti();
				}
				if (mapData.hasTatemonoArc()) {
					mapData.freeTatemono();
					mapData.freeTatemonoArc();
				}
				if (mapData.hasRoadArc()) {
					mapData.freeRoadArc();
				}
				if (mapData.hasEki()) {
					mapData.freeEki();
				}
			}
		}
	}

	/**
	 * 直前の読み込みで、地図の状態が変化したかどうかを取得します。
	 * @return 地図の状態が変化したかどうか
	 */
	boolean isChanged() {
		return this.isChanged;
	}

	/**
	 * URLの一覧を指定して地図を読み込みます。
	 * @param selectedURLs URLの一覧
	 * @param maps 地図
	 * @return 読み込んだ地図の図葉名
	 * @throws IOException 入出力例外
	 * @throws FileNotFoundException ファイル未検出例外
	 */
	public Collection<String> loadMaps(final Collection<URL> selectedURLs, final Map<String, MapData> maps)
			throws IOException, FileNotFoundException {
		final Collection<File> cachedFiles = new HashSet<File>();
		final String cacheDir = Const.Sdf2500.CACHE_DIR;
		final Collection<String> loadedMaps = new ArrayList<String>();
		final Set<String> baseDirs = new HashSet<String>();
		for (final URL url : selectedURLs) {
			final String[] separatedPath = url.getPath().split("/");
			if (separatedPath.length > 1) {
				final String prefecture = separatedPath[separatedPath.length - 2];
				final String filename = separatedPath[separatedPath.length - 1];
				new File(cacheDir + File.separator + prefecture).mkdirs();
				final File outFile = new File(cacheDir + File.separator + prefecture + File.separator + filename);
				if (outFile.exists() && url.openConnection().getContentLength() == outFile.length()) {
				} else {
					if (this.panel != null) {
						this.panel.addMessage(url + "をダウンロードしています。");
						WebUtilities.copy(url.openStream(), new FileOutputStream(outFile));
						this.panel.removeMessage();
					}
				}
				baseDirs.add(cacheDir + File.separator + prefecture);
				cachedFiles.add(outFile);
			} else {
				System.out.println("WARNING: wrong URL " + url);
			}
		}
		final Map<String, Set<String>> extractedFiles = new ConcurrentHashMap<String, Set<String>>();
		final Map<String, String> mapCityTable = new ConcurrentHashMap<String, String>();
		if (new File(Const.Sdf2500.EXTRACTED_LOG_FILE).isFile()) {
			final Scanner scanner = new Scanner(new File(Const.Sdf2500.EXTRACTED_LOG_FILE));
			while (scanner.hasNextLine()) {
				final String[] mapNames = scanner.nextLine().split("\t");
				if (mapNames.length > 1) {
					for (int i = 1; i < mapNames.length; i++) {
						final String filename = mapNames[0];
						if (!extractedFiles.containsKey(filename)) {
							extractedFiles.put(filename, new HashSet<String>());
						}
						extractedFiles.get(filename).add(mapNames[i]);
						mapCityTable.put(mapNames[i], filename);
					}
				}
			}
			scanner.close();
		}
		for (final File file : cachedFiles) {
			if (!extractedFiles.containsKey(file.getPath())) {
				this.panel.addMessage(file + "を展開しています。");
				final String[] separatedPath = file.getPath().split("\\" + File.separator);
				if (separatedPath.length > 1) {
					final String prefecture = separatedPath[separatedPath.length - 2];
					final LhaInputStream in = new LhaInputStream(new FileInputStream(file));
					LhaHeader entry;
					final Collection<String> mapStrings = new ArrayList<String>();
					while ((entry = in.getNextEntry()) != null) {
						final String entryPath = entry.getPath();
						final String path = cacheDir + File.separator + prefecture + File.separator + entryPath;
						final File outFile = new File(path);
						if (path.endsWith(File.separator)) {
							outFile.mkdir();
							if (entryPath.indexOf(File.separator) == entryPath.length() - 1) {
								loadedMaps.add(entryPath.substring(0, entryPath.length() - 1).toLowerCase());
								mapStrings.add(entryPath.substring(0, entryPath.length() - 1).toLowerCase());
							}
						} else {
							if (!outFile.exists() || entry.getOriginalSize() != outFile.length()) {
								WebUtilities.copy(in, new FileOutputStream(outFile));
							}
						}
					}
					final PrintWriter out = new PrintWriter(new FileWriter(new File(Const.Sdf2500.EXTRACTED_LOG_FILE),
							true));
					out.print(file.getPath());
					for (final String map : mapStrings) {
						out.print("\t" + map);
					}
					out.println();
					out.close();
				} else {
					System.out.println("WARNING: wrong path " + file);
				}
				this.panel.removeMessage();
			} else {
				loadedMaps.addAll(extractedFiles.get(file.getPath()));
			}
		}
		for (final String baseDir : baseDirs) {
			final Collection<String> loadFiles = new HashSet<String>();
			for (final String file : new File(baseDir).list()) {
				loadFiles.add(file.toLowerCase());
			}
			for (final String mapName : loadedMaps) {
				if (loadFiles.contains(mapName)) {
					final MapData map = new MapData(baseDir, mapName);
					synchronized (maps) {
						maps.put(mapName, map);
					}
				}
			}
		}
		return loadedMaps;
	}
}
