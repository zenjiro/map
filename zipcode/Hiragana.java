package zipcode;

import java.util.Scanner;

/**
 * カタカナをひらがなに置換するプログラムです。
 * 元のファイルに半角カタカナが存在する場合、
 * nkf hoge | java zipcode.Hiragana
 * で、全てひらがなになります。
 * @author zenjiro
 * 2006/03/10
 */
public class Hiragana {
	/**
	 * メインメソッドです。
	 * @param args
	 */
	public static void main(final String[] args) {
		final Scanner scanner = new Scanner(System.in, "ISO-2022-JP");
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			char before = 'ァ';
			char after = 'ぁ';
			for (int i = 0; i < 84; i++) {
				line = line.replace(before, after);
				before++;
				after++;
			}
			System.out.println(line);
		}
		scanner.close();
	}
}
