package com.younger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.SimpleBookmark;

/**
 * 分割pdf文件
 * @author Administrator
 *
 */
public class SplitBook {

	public static void split(String pdfPath,  int fileCounts, String filePrefix ) throws FileNotFoundException {
	
		
		if(! (new File(pdfPath).isFile()&&new File(pdfPath).exists() ) ){
			throw new FileNotFoundException(pdfPath + " not found");
		}
			
		filePrefix = filePrefix ==null || filePrefix.isEmpty() ? pdfPath: filePrefix;
		
		PdfReader reader = null;
		try {
//			PdfCopy
			reader = new PdfReader(pdfPath);
			int pageCount = reader.getNumberOfPages();
			int eachCount = pageCount / fileCounts;
			// List list = SimpleBookmark.getBookmark(reader);
			// SimpleBookmark.exportToXML(list, new
			// FileOutputStream("bookmark.xml"),"Utf-8",true);
			
			String PdfName = filePrefix;
			if(PdfName.contains(".")){
				PdfName =  filePrefix.substring(0, filePrefix.lastIndexOf("."));
			}
			
			PdfStamper pdfStamper = null;
			reader.close();
			for (int i = 0; i < fileCounts; i++) {
				reader = new PdfReader(pdfPath);
				pdfStamper = new PdfStamper(reader, new FileOutputStream(String.format("%s_%d.pdf", PdfName, i)));
				if (i == fileCounts - 1) {
					reader.selectPages(String.format("%d-%d", (fileCounts - 1) * eachCount + 1, pageCount));
				} else {
					reader.selectPages(String.format("%d-%d", i * eachCount + 1, eachCount * (i + 1)));
				}
//				pdfStamper.setEncryption("123".getBytes(), "12345".getBytes(),
//				 PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING|PdfWriter.ALLOW_MODIFY_CONTENTS|PdfWriter.ALLOW_MODIFY_ANNOTATIONS, PdfWriter.STANDARD_ENCRYPTION_128);
				pdfStamper.close();
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

	}

	public static void writeBookmark2XML(String pdfPath, String xmlPath) {
		PdfReader reader;
		try {
			reader = new PdfReader(pdfPath);
			List list = SimpleBookmark.getBookmark(reader);
			SimpleBookmark.exportToXML(list, new FileOutputStream(xmlPath), "Utf-8", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printHelp(){
		System.out.println("splitBook [pdffile] [numsplit] [filePrefix]");
		
	}
	
	public static void main(String[] args) {
		try {
			SplitBook.split("c.pdf",  5, "python");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
