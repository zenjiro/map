package ksj;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 国土数値情報の標高・傾斜度3次メッシュデータ（JPGIS準拠データ）を読み込んで、CVS形式で保存するプログラムです。
 * @author zenjiro
 * @since 5.00
 * 2006/10/28
 */
public class ConvertHeightToCSV {

	/**
	 * メインメソッドです。
	 * @param args コマンドライン引数
	 * @throws SAXException XML読み込み例外
	 * @throws ParserConfigurationException 解析設定例外
	 * @throws IOException 入出力例外
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		for (final File inFile : new File("b").listFiles()) {
			if (inFile.getName().matches("G04-b-81_[0-9]{4}\\.xml")) { // G04-a-81_[0-9]{4}\\.xml 
				System.out.println(inFile);
				final File outFile = new File(inFile.getName().replace(".xml", ".csv"));
				final SAXParserFactory factory = SAXParserFactory.newInstance();
				final SAXParser parser = factory.newSAXParser();
				parser.parse(inFile, new DefaultHandler() {
					/**
					 * インデント
					 */
					private int indent = 0;

					/**
					 * デバッグ表示をするかどうか
					 */
					private final boolean IS_DEBUG = false;

					/**
					 * 読み込み中のデータ
					 */
					private StringBuilder buffer = new StringBuilder();

					/**
					 * 属性名
					 */
					private String attributeName;

					/**
					 * データ
					 */
					private String any;

					/**
					 * 3次メッシュコード、8桁整数
					 */
					private String meshCode;

					/**
					 * 平均標高、0.1m単位、浮動小数点数、99999=データなし
					 */
					private String height;

					/**
					 * 最大傾斜角度、0.1m単位、浮動小数点数、999=データなし、99.9=データなし
					 */
					private String slope;

					/**
					 * 最大傾斜方向、整数、1=北、2=北東、…、8=北西、99=データなし、0=データなし
					 */
					private String direction;

					/**
					 * 最低標高コード、整数、0=陸上、5=海面下、9=データなし
					 */
					private String sea;

					/**
					 * CSVファイルへの出力ストリーム
					 */
					private final PrintWriter out = new PrintWriter(new OutputStreamWriter(
							new FileOutputStream(outFile), "UTF-8"));

					/**
					 * 細分メッシュのセルIDと細分メッシュコードの対応表
					 */
					private final Map<String, String> cellMeshMap = new HashMap<String, String>();

					/**
					 * セルID
					 */
					private String cellID;

					@Override
					public void startDocument() throws SAXException {
						this.out
								.println("# 3次メッシュコード（8桁整数）,平均標高（0.1m単位、浮動小数点数、99999=データなし）,最大傾斜角度（0.1度単位、浮動小数点数、999=データなし、99.9=データなし）,最大傾斜方向（整数、1=北、2=北東、…、8=北西、99=データなし、0=データなし）,最低標高コード（整数、0=陸上、5=海面下、9=データなし）");
						this.out.println("# or");
						this.out
								.println("# 1/4細分メッシュコード（10桁整数）,標高値（0.1m単位、整数、8888=データなし、9999=データなし）,最大傾斜角度（0.1度単位、浮動小数点数、999=データなし、99.9=データなし）,最大傾斜方向（整数、1=北、2=北東、…、8=北西、99=データなし、0=データなし）,測定コード（整数、0=その他の地域、1=陸水、2=海水、3=等高線のないもの、4=埋立地、5=海面下の地域）");
						if (this.IS_DEBUG) {
							System.out.println("start of document {");
							this.indent++;
						}
					}

					@Override
					public void startElement(final String uri, final String localName, final String qName,
							final Attributes attributes) throws SAXException {
						this.buffer.delete(0, this.buffer.length());
						if (qName.equals("jps:JP_Cell")) {
							this.cellID = attributes.getValue("id");
						} else if (qName.equals("jps:Record")) {
							this.cellID = attributes.getValue("id").replaceFirst("_[^_]+$", "");
						}
						if (this.IS_DEBUG) {
							final Map<String, String> map = new HashMap<String, String>();
							for (int i = 0; i < attributes.getLength(); i++) {
								map.put(attributes.getQName(i), attributes.getValue(i));
							}
							System.out.printf("%" + this.indent * 4
									+ "sstart element: surl = %s, localName = %s, qName = %s, attributes = %s {\n", "",
									uri, localName, qName, map);
							this.indent++;
						}
					}

					@Override
					public void characters(final char[] ch, final int start, final int length) throws SAXException {
						final String string = new String(ch, start, length).trim();
						this.buffer.append(string);
						if (string.length() > 0 && this.IS_DEBUG) {
							System.out.printf("%" + this.indent * 4 + "sgot data: string = %s\n", "", string);
						}
					}

					@Override
					public void endElement(final String uri, final String localName, final String qName)
							throws SAXException {
						if (this.IS_DEBUG) {
							this.indent--;
							System.out.printf("%" + this.indent * 4 + "s}\n", "");
						}
						if (qName.equals("jps:Record.attributes")) {
							if (this.attributeName.equals("平均標高") || this.attributeName.equals("標高値")) {
								this.height = this.any;
							} else if (this.attributeName.equals("最大傾斜_角度")) {
								this.slope = this.any;
							} else if (this.attributeName.equals("最大傾斜_方向")) {
								this.direction = this.any;
							} else if (this.attributeName.equals("最低標高コード") || this.attributeName.equals("測定コード")) {
								this.sea = this.any;
							}
						} else if (qName.equals("AttributeName")) {
							this.attributeName = this.buffer.toString();
						} else if (qName.equals("Any")) {
							this.any = this.buffer.toString();
						} else if (qName.equals("JP_MeshCode.meshCode")) {
							this.meshCode = this.buffer.toString();
							if (this.cellID != null) {
								this.cellMeshMap.put(this.cellID, this.meshCode);
							}
						} else if (qName.equals("jps:Record")) {
							if (this.cellID != null) {
								this.meshCode = this.cellMeshMap.get(this.cellID);
							}
							this.out.printf("%s,%s,%s,%s,%s\n", this.meshCode, this.height, this.slope, this.direction,
									this.sea);
						}
					}

					@Override
					public void endDocument() throws SAXException {
						this.out.close();
						if (this.IS_DEBUG) {
							this.indent--;
							System.out.println("}");
						}
					}
				});
			}
		}
	}

}
