package ksj;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import map.UTMUtil;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 国土数値情報の鉄道データ（JPGIS準拠データ）を読み込んで、都道府県単位のCSVファイルに変換するプログラムです。
 * @author zenjiro
 */
public class ConvertRailway {

	/**
	 * 状態を表す列挙型です。
	 * @author zenjiro
	 */
	enum Mode {
		/**
		 * 曲線
		 */
		CURVE,
		/**
		 * 文字列
		 */
		LABEL,
		/**
		 * 点
		 */
		POINT,
		/**
		 * 駅の曲線
		 */
		STATION_CURVE,
		/**
		 * 駅の文字列
		 */
		STATION_LABEL,
		/**
		 * 未知
		 */
		UNKNOWN
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		System.out.println("started on " + new Date());
		final Map<Shape, String> prefectures = new HashMap<Shape, String>();
		for (final File file : new File(".").listFiles()) {
			if (file.getName().matches("prefecture_[0-9][0-9]\\.csv")) {
				prefectures.putAll(ShapeIO.readShapes(new FileInputStream(file)));
			}
		}
		System.out.println("load prefecture done on " + new Date());
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();
		parser.parse(new File("N02-05.xml"), new DefaultHandler() {
			private StringBuilder buffer;

			private String curveID;

			private final Map<String, List<Point2D>> curves = new HashMap<String, List<Point2D>>();

			private int indent = 0;

			private final boolean IS_DEBUG = false;

			private String ksjInt;

			private String ksjLin;

			private String ksjOpc;

			private String ksjRac;

			private String ksjStn;

			private final Map<String, String> labels = new HashMap<String, String>();

			private final Map<String, List<String>> lines = new HashMap<String, List<String>>();

			private Mode mode = Mode.UNKNOWN;

			private String pointID;

			private final Map<String, Point2D> points = new HashMap<String, Point2D>();

			private final Map<String, String> pointsPrefectures = new HashMap<String, String>();

			private final Map<String, List<String>> prefecturesCurves = new HashMap<String, List<String>>();

			private final Map<String, List<String>> prefecturesLines = new HashMap<String, List<String>>();

			private final Map<String, List<String>> prefecturesStations = new HashMap<String, List<String>>();

			private final Map<String, List<String>> stationCureves = new HashMap<String, List<String>>();

			private final Map<String, String> stationLabels = new HashMap<String, String>();

			@Override
			public void characters(final char[] ch, final int start, final int length) throws SAXException {
				final String string = new String(ch, start, length).trim();
				this.buffer.append(string);
				if (this.IS_DEBUG && string.length() > 0) {
					System.out.printf("%" + this.indent * 4 + "sgot data: string = %s\n", "", string);
				}
			}

			@Override
			public void endDocument() throws SAXException {
				this.indent--;
				if (this.IS_DEBUG) {
					System.out.println("}");
				}
				System.out.println("load xml done on " + new Date());
				for (final Map.Entry<String, List<String>> entry : this.prefecturesLines.entrySet()) {
					final Map<Shape, String> lines = new HashMap<Shape, String>();
					for (final String curveID : entry.getValue()) {
						if (this.lines.get(curveID).size() == 2) {
							final Point2D point1 = this.points.get(this.lines.get(curveID).get(0));
							final Point2D point2 = this.points.get(this.lines.get(curveID).get(1));
							lines.put(new Line2D.Double(point1, point2), this.labels.get(curveID));
						}
					}
					if (entry.getKey() != null) {
						try {
							final File file = new File("ksj_railway_lines_" + entry.getKey().replaceAll("_.+", "")
									+ ".csv");
							ShapeIO.writeShape(lines, new FileOutputStream(file));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("write lines done on " + new Date());
				for (final Map.Entry<String, List<String>> entry : this.prefecturesCurves.entrySet()) {
					final Map<Shape, String> curves = new HashMap<Shape, String>();
					for (final String curveID : entry.getValue()) {
						GeneralPath path = null;
						for (final Point2D point : this.curves.get(curveID)) {
							if (path == null) {
								path = new GeneralPath();
								path.moveTo((float) point.getX(), (float) point.getY());
							} else {
								path.lineTo((float) point.getX(), (float) point.getY());
							}
						}
						curves.put(path, this.labels.get(curveID));
					}
					if (entry.getKey() != null) {
						try {
							final File file = new File("ksj_railway_curves_" + entry.getKey().replaceAll("_.+", "")
									+ ".csv");
							ShapeIO.writeShape(curves, new FileOutputStream(file));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("write curves done on " + new Date());
				for (final Map.Entry<String, List<String>> entry : this.prefecturesStations.entrySet()) {
					final Map<Shape, String> stations = new HashMap<Shape, String>();
					for (final String curveID : entry.getValue()) {
						if (this.stationCureves.get(curveID).size() == 2) {
							final Point2D point1 = this.points.get(this.stationCureves.get(curveID).get(0));
							final Point2D point2 = this.points.get(this.stationCureves.get(curveID).get(1));
							stations.put(new Line2D.Double(point1, point2), this.stationLabels.get(curveID));
						}
					}
					if (entry.getKey() != null) {
						try {
							final File file = new File("ksj_railway_stations_" + entry.getKey().replaceAll("_.+", "")
									+ ".csv");
							ShapeIO.writeShape(stations, new FileOutputStream(file));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("all done on " + new Date());
			}

			@Override
			public void endElement(final String uri, final String localName, final String qName) throws SAXException {
				this.indent--;
				if (this.IS_DEBUG) {
					System.out.printf("%" + this.indent * 4 + "s}\n", "");
				}
				switch (this.mode) {
				case POINT:
					if (qName.equals("DirectPosition.coordinate")) {
						final String[] coordinates = this.buffer.toString().split(" ");
						if (coordinates.length == 2) {
							final Point2D point = UTMUtil.toUTM(Double.parseDouble(coordinates[1]), Double
									.parseDouble(coordinates[0]));
							final Point2D.Double utmPoint = new Point2D.Double(point.getX(), -point.getY());
							this.points.put(this.pointID, utmPoint);
							for (final Map.Entry<Shape, String> entry : prefectures.entrySet()) {
								if (entry.getKey().getBounds2D().contains(utmPoint)) {
									if (entry.getKey().contains(utmPoint)) {
										this.pointsPrefectures.put(this.pointID, entry.getValue());
									}
								}
							}
						}
					}
					break;
				case CURVE:
					if (qName.equals("DirectPosition.coordinate")) {
						final String[] coordinates = this.buffer.toString().split(" ");
						if (coordinates.length == 2) {
							final Point2D point = UTMUtil.toUTM(Double.parseDouble(coordinates[1]), Double
									.parseDouble(coordinates[0]));
							this.curves.get(this.curveID).add(new Point2D.Double(point.getX(), -point.getY()));
						}
					}
					break;
				case STATION_CURVE:
					break;
				case LABEL:
				case STATION_LABEL:
					if (qName.equals("ksj:EB02")) {
						this.labels.put(this.curveID, this.ksjRac + "_" + this.ksjInt + "_" + this.ksjLin + "_"
								+ this.ksjOpc);
					} else if (qName.equals("ksj:EB03")) {
						this.stationLabels.put(this.curveID, this.ksjRac + "_" + this.ksjInt + "_" + this.ksjLin + "_"
								+ this.ksjOpc + "_" + this.ksjStn);
					} else if (qName.equals("ksj:RAC")) {
						this.ksjRac = this.buffer.toString();
					} else if (qName.equals("ksj:INT")) {
						this.ksjInt = this.buffer.toString();
					} else if (qName.equals("ksj:LIN")) {
						this.ksjLin = this.buffer.toString();
					} else if (qName.equals("ksj:OPC")) {
						this.ksjOpc = this.buffer.toString();
					} else if (qName.equals("ksj:STN")) {
						this.ksjStn = this.buffer.toString();
					}
					break;
				}
			}

			@Override
			public void startDocument() throws SAXException {
				if (this.IS_DEBUG) {
					System.out.println("start of document {");
				}
				this.indent++;
			}

			@Override
			public void startElement(final String uri, final String localName, final String qName,
					final Attributes attributes) throws SAXException {
				if (this.IS_DEBUG) {
					final Map<String, String> map = new HashMap<String, String>();
					for (int i = 0; i < attributes.getLength(); i++) {
						map.put(attributes.getQName(i), attributes.getValue(i));
					}
					System.out.printf("%" + this.indent * 4
							+ "sstart element: surl = %s, localName = %s, qName = %s, attributes = %s {\n", "", uri,
							localName, qName, map);
				}
				if (qName.equals("jps:GM_Point")) {
					this.pointID = attributes.getValue("id");
					this.mode = Mode.POINT;
				} else if (qName.equals("jps:GM_Curve")) {
					this.curveID = attributes.getValue("id");
					if (this.curveID.endsWith("-st_lin")) {
						this.stationCureves.put(this.curveID, new ArrayList<String>());
						this.mode = Mode.STATION_CURVE;
					} else {
						this.lines.put(this.curveID, new ArrayList<String>());
						this.curves.put(this.curveID, new ArrayList<Point2D>());
						this.mode = Mode.CURVE;
					}
				} else if (qName.equals("GM_PointRef.point")) {
					final String pointID = attributes.getValue("idref");
					final String prefecture = this.pointsPrefectures.get(pointID);
					if (this.mode == Mode.STATION_CURVE) {
						this.stationCureves.get(this.curveID).add(pointID);
						if (!this.prefecturesStations.containsKey(prefecture)) {
							this.prefecturesStations.put(prefecture, new ArrayList<String>());
						}
						this.prefecturesStations.get(prefecture).add(this.curveID);
					} else {
						this.lines.get(this.curveID).add(pointID);
						this.curves.get(this.curveID).add(this.points.get(pointID));
						if (!this.prefecturesLines.containsKey(prefecture)) {
							this.prefecturesLines.put(prefecture, new ArrayList<String>());
						}
						this.prefecturesLines.get(prefecture).add(this.curveID);
						if (!this.prefecturesCurves.containsKey(prefecture)) {
							this.prefecturesCurves.put(prefecture, new ArrayList<String>());
						}
						this.prefecturesCurves.get(prefecture).add(this.curveID);
					}
				} else if (qName.equals("ksj:EB02")) {
					this.mode = Mode.LABEL;
					this.ksjRac = null;
					this.ksjInt = null;
					this.ksjLin = null;
					this.ksjOpc = null;
				} else if (qName.equals("ksj:EB03")) {
					this.mode = Mode.STATION_LABEL;
					this.ksjRac = null;
					this.ksjInt = null;
					this.ksjLin = null;
					this.ksjOpc = null;
					this.ksjStn = null;
				} else if (qName.equals("ksj:LOC")) {
					this.curveID = attributes.getValue("idref");
				}
				this.buffer = new StringBuilder();
				this.indent++;
			}
		});
	}

}
