package com.younger;
import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;

public class FontUtil {

	/**
	 * 使用iTextAsian.jar中的字体
	 */
	public static BaseFont getFont() {
		BaseFont font = null;
		try {
//			font = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			font = BaseFont.createFont("STSongStd-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return font;
	}
	
	/**
	 * 使用Windows系统字体(TrueType) 
	 */
	public static BaseFont getFont1() {
		BaseFont font = null;
		try {//Hei.ttf
//			BaseFont.createFont("C:/WINDOWS/Fonts/SIMYOU.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
			font = BaseFont.createFont("/Library/Fonts/DroidSansFallback.ttf",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED); 
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return font;
	}
	
	public static BaseFont getFont2(){
		BaseFont font = null;
		try {
			font = BaseFont.createFont("/Library/Fonts/SIMYOU.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return font;
	}
	

}
