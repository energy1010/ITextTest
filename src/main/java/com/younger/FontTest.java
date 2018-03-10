package com.younger;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

public class FontTest {

	public static void main(String[] args) {

		System.out.println("Registering fonts with the FontFactory");

		FontFactory.register("C:\\WINDOWS\\Fonts\\simhei.ttf");
		FontFactory.register("C:\\WINDOWS\\Fonts\\simkai.ttf");
		FontFactory.register("C:\\WINDOWS\\Fonts\\simsun.ttc");

		// step 1: creation of a document-object
		Document document = new Document();

		try {
			// step 2: creation of the writer
			// RtfWriter2.getInstance(document, new
			// FileOutputStream("c:\\registerfont.rtf"));
			PdfWriter.getInstance(document, new FileOutputStream("fontTest.rtf"));

			// step 3: we open the document
			document.open();

			// step 4: we add content to the document
			Font font0 = FontFactory.getFont(BaseFont.HELVETICA, BaseFont.WINANSI, 12);
			String text0 = "中文This is the quite popular built in font '" + BaseFont.HELVETICA + "'.";
			document.add(new Paragraph(text0, font0));
			Font font1 = FontFactory.getFont("黑体", BaseFont.WINANSI, 12);
			String text1 = "中文This is the quite popular True Type font 'ComicSansMS'.";
			document.add(new Paragraph(text1, font1));
			Font font2 = FontFactory.getFont("楷体_gb2312", BaseFont.WINANSI, 12);
			String text2 = "中文This is the quite popular True Type font 'ComicSansMS-Bold'.";
			document.add(new Paragraph(text2, font2));
			Font font3 = FontFactory.getFont("simhei", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
			String text3 = "中文\u5951\u7d04\u8005\u4f4f\u6240\u30e9\u30a4\u30f3\uff11";
			document.add(new Paragraph(text3, font3));
			BufferedWriter out = new BufferedWriter(new FileWriter("c:\\registered.txt"));
			out.write("These fonts were registered at the FontFactory:\r\n");
			for (Iterator<String> i = FontFactory.getRegisteredFonts().iterator(); i.hasNext();) {
				out.write((String) i.next());
				out.write("\r\n");
			}
			out.write("\r\n\r\nThese are the families these fonts belong to:\r\n");
			for (Iterator<String> i = FontFactory.getRegisteredFamilies().iterator(); i.hasNext();) {
				out.write((String) i.next());
				out.write("\r\n");
			}
			out.flush();
			out.close();
		} catch (DocumentException de) {
			System.err.println(de.getMessage());
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}

		// step 5: we close the document
		document.close();

	}

}
