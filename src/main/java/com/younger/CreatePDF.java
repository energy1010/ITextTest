package com.younger;
import java.io.FileOutputStream;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfWriter;


/**
 * 创建基本的pdf
 *
 * @author Administrator
 *
 */
public class CreatePDF {
	
	
	public CreatePDF() throws Exception {

		Rectangle pagesize = new Rectangle(PageSize.A4);
		pagesize.setBackgroundColor(new BaseColor(204,232,207));
		Document document = new Document(pagesize);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("outline.pdf"));
		document.open();
		// Code 1
		document.add(new Chunk("Chapter 1").setLocalDestination("1"));
		document.newPage();

		document.add(new Chunk("Chapter 2").setLocalDestination("2"));
		document.add(new Paragraph(new Chunk("Sub 2.1").setLocalDestination("2.1")));
		document.add(new Paragraph(new Chunk("Sub 2.2").setLocalDestination("2.2")));
		document.newPage();

		document.add(new Chunk("Chapter 3").setLocalDestination("3"));

		// Code 2
		PdfContentByte cb = writer.getDirectContent();
		PdfOutline root = cb.getRootOutline();

		// Code 3
		PdfOutline oline1 = new PdfOutline(root, PdfAction.gotoLocalPage("1", false), "Chapter 1");

		PdfOutline oline2 = new PdfOutline(root, PdfAction.gotoLocalPage("2", false), "Chapter 2");
		oline2.setOpen(false);
		PdfOutline oline2_1 = new PdfOutline(oline2, PdfAction.gotoLocalPage("2.1", false), "Sub 2.1");
		PdfOutline oline2_2 = new PdfOutline(oline2, PdfAction.gotoLocalPage("2.2", false), "Sub 2.2");

		PdfOutline oline3 = new PdfOutline(root, PdfAction.gotoLocalPage("3", false), "Chapter 3");

		/* chapter08/StandardType1Fonts.java */
		Font[] fonts = new Font[14];
		fonts[0] = new Font(FontFamily.COURIER, Font.DEFAULTSIZE, Font.NORMAL);
		fonts[1] = new Font(FontFamily.COURIER, Font.DEFAULTSIZE, Font.ITALIC);
		fonts[2] = new Font(FontFamily.COURIER, Font.DEFAULTSIZE, Font.BOLD);
		fonts[3] = new Font(FontFamily.COURIER, Font.DEFAULTSIZE, Font.BOLD | Font.ITALIC);

		fonts[4] = new Font(FontFamily.HELVETICA, Font.DEFAULTSIZE, Font.NORMAL);
		fonts[5] = new Font(FontFamily.HELVETICA, Font.DEFAULTSIZE, Font.ITALIC);
		fonts[6] = new Font(FontFamily.HELVETICA, Font.DEFAULTSIZE, Font.BOLD);
		fonts[7] = new Font(FontFamily.HELVETICA, Font.DEFAULTSIZE, Font.BOLD | Font.ITALIC);

		fonts[8] = new Font(FontFamily.TIMES_ROMAN, Font.DEFAULTSIZE, Font.NORMAL);
		fonts[9] = new Font(FontFamily.TIMES_ROMAN, Font.DEFAULTSIZE, Font.ITALIC);
		fonts[10] = new Font(FontFamily.TIMES_ROMAN, Font.DEFAULTSIZE, Font.BOLD);
		fonts[11] = new Font(FontFamily.TIMES_ROMAN, Font.DEFAULTSIZE, Font.BOLD | Font.ITALIC);
		// fonts[11] = new Font(FontFamily.TIMES_ROMAN, Font.DEFAULTSIZE,
		// Font.BOLDITALIC);
		fonts[12] = new Font(FontFamily.ZAPFDINGBATS, Font.DEFAULTSIZE, Font.NORMAL);
		fonts[13] = new Font(FontFamily.ZAPFDINGBATS, Font.DEFAULTSIZE, Font.ITALIC);
		// fonts[10] = new Font(FontFamily.ZAPFDINGBATS, Font.DEFAULTSIZE,
		// Font.BOLD);
		// fonts[14] = new Font(FontFamily.SYMBOL, Font.DEFAULTSIZE);
		// fonts[13] = new Font(FontFamily.ZAPFDINGBATS, Font.DEFAULTSIZE,
		// FontFamily.UNDEFINED, new Color(0xFF, 0x00, 0x00));
		for (int i = 0; i < 14; i++) {
			document.add(new Paragraph("quick brown fox jumps over the lazy dog  " + fonts[i].getFamilyname(), fonts[i]));
		}
		
		PdfGraphics2D graphics2D = new PdfGraphics2D(cb, PageSize.A4.getWidth(), PageSize.A4.getHeight());
		
		graphics2D.drawString("Hello World", 36, 54);
		graphics2D.dispose();

//		FontSelector
//		FontFactory.;
		
//		BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
//		Font font = new Font(bf, 12);
////		System.err.println(bf.getClass().getName());
//		document.add(new Paragraph("\u5341\u950a\u57cb\u4f0f", font));
		
//		PdfEncodings.loadCmap("GBK2K-H", PdfEncodings.CRLF_CID_NEWLINE);
//		byte text[] = "你好".getBytes(); 
//		String cid = PdfEncodings.convertCmap("GBK2K-H", text);
//		BaseFont bf = BaseFont.createFont("STSong-Light",
//		BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED); 
//		Paragraph p = new Paragraph(cid, new Font(bf, 14));
		
		document.close();
	}

	public static void main(String[] args) {
		try {
			CreatePDF outline = new CreatePDF();
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}
