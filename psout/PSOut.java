package psout;

import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.print.DocFlavor;
import javax.print.StreamPrintServiceFactory;

/**
 * PSファイルを出力するためのクラスの、PrinterJobを用いた実装です。
 * @author zenjiro
 * Created on 2005/02/10 22:31:44
 */
public class PSOut {
    /**
     * PSファイルを出力します。
     * @param file 出力するファイル
     * @param painter 印刷のためのオブジェクト 
     * @throws PrinterException 
     * @throws IOException 
     */
    public static void print(final File file, final Printable painter) throws PrinterException,
            IOException {
        final StreamPrintServiceFactory[] factories = PrinterJob
                .lookupStreamPrintServices(DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
        if (factories.length > 0) {
            final FileOutputStream out = new FileOutputStream(file);
            final PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(factories[0].getPrintService(out));
            job.setPrintable(painter);
            job.print();
            out.close();
        } else {
            System.err.println("ERROR: No suitable factories.");
        }
    }
}
