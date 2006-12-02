package map;

import java.awt.print.Printable;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

/**
 * 印刷を行うユーティリティクラスです。
 * @author zenjiro
 * Created on 2005/10/04
 */
public class PrintUtil {
	/**
	 * ダイアログを表示して印刷します。
	 * @param printable 印刷するオブジェクト
	 * @throws PrintException 印刷例外
	 */
	public static void print(final Printable printable) throws PrintException {
		final DocFlavor docFlavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		final HashPrintRequestAttributeSet hashPrintRequestAttributeSet = new HashPrintRequestAttributeSet();
		final PrintService printService = ServiceUI.printDialog(null, 0, 0, PrintServiceLookup
				.lookupPrintServices(docFlavor, hashPrintRequestAttributeSet), PrintServiceLookup
				.lookupDefaultPrintService(), docFlavor, hashPrintRequestAttributeSet);
		final DocPrintJob docPrintJob = printService.createPrintJob();
		final PrintJobListener printJoblistener = new PrintJobAdapter() {
			@Override
			public void printDataTransferCompleted(PrintJobEvent e) {
			}
		};
		docPrintJob.addPrintJobListener(printJoblistener);
		final DocAttributeSet docAttributeSet = new HashDocAttributeSet();
		final Doc doc = new SimpleDoc(printable, docFlavor, docAttributeSet);
		docPrintJob.print(doc, hashPrintRequestAttributeSet);
	}
}
