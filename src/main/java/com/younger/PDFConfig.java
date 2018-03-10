package com.younger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * pdf 配置
 * @author Administrator
 *
 */
public class PDFConfig {
	
	/**默认配置文件 */
	public static final String DEFAULT_CONF_FILE="config.properties";
	
	public static final String encrypt_pdf="encrypt_pdf";
	public static final String ENCODING = "encoding";
	
	
	public static final String encrypt_user_pwd="encrypt_user_pwd";
	public static final String encrypt_owner_pwd="encrypt_owner_pwd";
	public static final String addwatermark="addwatermark";
	
	public static final String watermarkangle="watermarkangle";
	public static final String watermark="watermark";
	
	public static final String outline_color="outline_color";
	public static final String outline_color_sec="outline_color_sec";
	public static final String outline_color_subsec="outline_color_subsec";
	public static final String outline_color_default="outline_color_default";
	
	public static final String outline_style="outline_style";
//	public static final String 
	
	private Properties prop = new Properties();//属性集合对象 
	
//	private Map<String, String> values = null;
	
	public PDFConfig(String configFile) throws IOException{
//		Properties prop = new Properties();//属性集合对象 
		if(new File(configFile).exists() && new File(configFile).isFile()  ){
			System.out.println("load properties from file: "+configFile );
			FileInputStream fis = new FileInputStream(configFile);//属性文件流     
			prop.load(fis);//将属性文件流装载到Properties对象中    
		}else{
			System.out.println("load default properties");
			//set default properties
			prop.put(encrypt_pdf, "false");
			prop.put(ENCODING, "utf-8");
			prop.put(addwatermark, "false");
			prop.put(outline_color, prop.getProperty(outline_color,"black"));
			prop.put(outline_color_sec, prop.getProperty(outline_color_sec,"black"));
			prop.put(outline_color_subsec, prop.getProperty(outline_color_subsec,"black"));
			
			
//			prop.put(watermark, "energy1010");
			
		}
	}
	
	public String getValue(String key){
		return getValue(key, null);
	}

	public String getValue(String key, String defaultVal){
		String val = defaultVal;
		if(prop.containsKey(key)){
			val = prop.getProperty(key);
		}
		return val;
	}
	
	public void print(){
		for(Entry<Object, Object> e:prop.entrySet()){
			System.out.println(e.getKey()+"\t"+e.getValue());
		}
	}

	
	
}
