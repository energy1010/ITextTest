package com.younger;

import java.io.File;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
	
	private static final Logger logger = LoggerFactory.getLogger(App.class); 
	
	
	public static void main(String[] args) {

//		args = new String[] { 
//				"input",
////				"-bt", "bookmark.temp", 
//				"output", 
////				"-c", "config",
////				"newbook",
//				"bookmark",
////		"-g"
//				} ; 
		System.out.println("args: "+Arrays.toString(args));
		logger.debug("args: "+Arrays.toString(args));
		  Options opts = new Options();  
		  opts.addOption("h", false, "the command help");  
//	        opts.addOption("i", "--input" , true, "input file");  
//	        opts.addOption("o", "--output" , true, "output file");  
//	        opts.addOption("b", "--bookmark" , true, "bookmark file");  
	        opts.addOption("c",  "config" ,true, "generate pdf with config file");  
	        opts.addOption("g", "gui", false, "start with gui");  
	        
	        opts.addOption("ct", "configTemplate", false, "generate config template file");
	        opts.addOption("bt", "bookmarkTemplate", true, "generate bookmark template file");
	        opts.addOption("fb", "bookmarkFormat", false, "format and overwrite bookmark file");
	        opts.addOption("pt", "printBookmarkTree", false, "print bookmark tree generated from bookmark file");
	        opts.addOption("ow", "overwrite", false, "overwrite output as input");  
//	        Option user = OptionBuilder.withArgName("type").hasArg().withDescription("target the search type").create("t");  
	        // 此处定义参数类似于 java 命令中的 -D<name>=<value>  
	        
	        OptionBuilder.withArgName("property=value");
			@SuppressWarnings("static-access")
			Option property = OptionBuilder  
	                .hasArgs(2)  
	                .withValueSeparator()  
	                .withDescription("use value for given property(property=value)")  
	                .create("D");  
	        property.setRequired(false);  
	        opts.addOption(property);
	        
	     
	        
	        /*支持三种CLI选项解析：
	        BasicParser：直接返回参数数组值
	        PosixParser：解析参数及值(-s10)
	        GnuParser：解析参数及值(--size=10
	        */
	        CommandLineParser parser = new BasicParser();  
//	        CommandLineParser parser = new GnuParser ();  
//	        CommandLineParser parser = new PosixParser();  
		
	        CommandLine cl;  
	        try {  
	            cl = parser.parse(opts, args);  
	            String[] otherArgs = cl.getArgs();
	            String bookmarkTemplate = cl.getOptionValue("bt");  
	            boolean generateConfigTemplate =  cl.hasOption("ct")? true: false;
	            String config = cl.getOptionValue("c");  
                boolean startWithGUI = cl.hasOption("g")? true: false;
                boolean formatBookmark= cl.hasOption("fb")? true: false;
                boolean printBookmarkTree =  cl.hasOption("pt")? true: false;
                boolean overwriteInput =  cl.hasOption("ow")? true: false;
	            
//	            if (cl.getOptions().length > 0) {  
	                if (cl.hasOption('h')) {  
	                    HelpFormatter hf = new HelpFormatter();  
	                    hf.printHelp("May Options", opts);  
	                } else { 
	                	
	                    if(bookmarkTemplate!=null){
	                    	//generate bookmark template
	    	            	logger.info("generateBookmarkTemplate: "+bookmarkTemplate);
	                    	BookmarkHelper.generateBookmarkTemplate(bookmarkTemplate);
	                    	return;
	                    }
	                	
	                    
		                  if(generateConfigTemplate){
		                	  logger.info("generate default config file: config.properties");
		                	  String configFile = "config.properties";
		                	  BookmarkHelper.generateConfigTemplateFile(configFile);
		                	  return;
		                  }
	                    
	                	if(otherArgs.length!=3){
	                		   // print usage  
	    	    	        HelpFormatter formatter = new HelpFormatter();  
	    	    	        formatter.printHelp( "<input> <output> <bookmark>", opts );
	    	    	        return;
	                	}
	                	String input = otherArgs[0].trim();
	                	String output = otherArgs[1].trim();
	                	String bookmark = otherArgs[2].trim();
	                	
	                  if(printBookmarkTree){
	                	  logger.info("print Bookmark tree generate from bookmark file: "+ bookmark);
	                    	BookmarkHelper.printTreeModel(bookmark, 0, true);
	                    	return;
	                  }
	               
	                    
	                    if(formatBookmark){
	                    	//TODO: formate bookmark file
	                    	logger.info("formart Bookmark file: "+ bookmark);
	                    	BookmarkHelper.formatBookmark(bookmark);
	                    	return;
	                    }
	                    
//	                    System.out.println(cmd.getOptionProperties("D").getProperty("key1") );  
//	                    System.out.println(cmd.getOptionProperties("D").getProperty("key2") );
	                    if(!startWithGUI){
		                    BookmarkHelper bookmarkHelper =	new BookmarkHelper();
//		        			System.out.println("run with cmd mode");
		        			logger.info("run with cmd mode");
		        			System.out.println(String.format("oldFile:%s newFile:%s outlinesFile:%s configFile:%s", input==null? "null": input, output==null? "null":output, bookmark==null?"null":bookmark, config==null? "null":config));
		        		
		        			bookmarkHelper.setOutlines(input, output, bookmark, config);
	                    }else{
//	                    	System.out.println("run with gui mode");
	                    	logger.info("run with gui mode");
	                    	SwingUtilities.invokeLater(new Runnable() {
	            				public void run() {
	            					System.out.println("run with gui");
	            					new CreateOutlineFrame();
	            
	            				}
	            			});
	                    }
	                    
	                    if(overwriteInput){
	                    	System.out.println("overwriteInput");
	                    	File inputF = new File( input);
	                    	File outputF = new File(output);
	                    	if( outputF.isFile() &&  outputF.exists()){
	                    		if(inputF.isFile() &&  inputF.exists()) inputF.delete();
	                    		outputF.renameTo(inputF);
	                    	}
	                    }
	                    
	                }  
//	            } else {  
//	            	String input = otherArgs[0].trim();
//                	String output = otherArgs[1].trim();
//                	String bookmark = otherArgs[2].trim();
////	                System.err.println("ERROR_NOARGS");  
////	                logger.error("no args error");
////	                // print usage  
////	    	        HelpFormatter formatter = new HelpFormatter();  
////	    	        formatter.printHelp( "<input> <output> <bookmark>", opts );  
//	            }  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	            logger.error("run error", e);
	            // print usage  
		        HelpFormatter formatter = new HelpFormatter();  
		        formatter.printHelp( "<input> <output> <bookmark>", opts );  
	        }  
	    }  

}
