package com.younger;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/******************
 * 19 * bookmarks(list) item: 20 * 21 * HashMap = 22 * (key, value) 23 * "Title"
 * "some title" 24 * "Action" "GoTo" 25 * "Page" "some page" 26 * 27 * others
 * may be: 28 * "Style" "some style" 29 * "Color" "some color" 30 * "Open"
 * "True | False" 31 * 32
 ******************/

public class CreateOutlineFrame extends JFrame implements ActionListener {
	
	
	private BookmarkHelper bookmarkHelper = new BookmarkHelper();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1070908780175754617L;

	private JButton btn_edit_file;
	private JButton btn_save_path;
	private JButton btn_outline_file;
	private JButton btn_run;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JTree jtree;
	private JFileChooser chooser;
	private String oldFile;
	private String newFile;
	private String outlineFile;
	
	DefaultTreeModel treeModel= null;

	public CreateOutlineFrame() {
		init();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

//	private static void printHelp() {
//		System.out.println("PdfBookmarkHelper <oldFile> <newFile> <scriptFile> [pageOffset] [pdfconfig]");
//	}

	public void actionPerformed(ActionEvent event) {
		chooser.setCurrentDirectory(new File(".") );
		if (event.getSource() == btn_save_path) {
//			保存文件
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("pdf files", "pdf"));
			int returnValue = chooser.showSaveDialog(CreateOutlineFrame.this);
			if (returnValue == 0) {
				this.newFile = chooser.getSelectedFile().getAbsolutePath();
				this.btn_outline_file.setEnabled(true);
			}
		} else if (event.getSource() == btn_edit_file) {
			//选择要添加书签的文件
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("pdf files", "pdf"));
			int returnValue = chooser.showOpenDialog(CreateOutlineFrame.this);
			if (returnValue == 0) {
				this.oldFile = chooser.getSelectedFile().getAbsolutePath();
				this.btn_save_path.setEnabled(true);
			}
		} else if (event.getSource() == btn_outline_file) {
			//选择目录文件
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("text files", "txt"));
			int returnValue = chooser.showOpenDialog(CreateOutlineFrame.this);
			if (returnValue == 0) {
				this.outlineFile = chooser.getSelectedFile().getAbsolutePath();
				this.btn_run.setEnabled(true);
			}
			 try {
				 treeModel= bookmarkHelper.getContentTree(outlineFile, 0, true);
				if(treeModel!=null){
					jtree.setModel(treeModel);
					expandTree(jtree);
					jtree.invalidate();
				}
			 } catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else if (event.getSource() == btn_run) {
			SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

				@Override
				protected Boolean doInBackground() throws Exception {
//					String resultString = JOptionPane.showInputDialog("please set the pageOffset (default is 0)", 0);
//					int pageOffset = 0;
//					if (resultString != null && !resultString.isEmpty()) {
//						pageOffset = Integer.valueOf(resultString);
//					}
//					String oldFile, String newFile, String outlinesFile, int pageOffset ,boolean withSecIndex
//				
//					return treeModel==null? false: true;
					  PdfReader reader = null;
					  PdfStamper stamp =  null;
						
					
					try{
					  if(treeModel==null){
						  System.err.println("null TreeModel");
						  return false;
					  }
						bookmarkHelper.printTreeModel(treeModel);
						// create a reader for a certain document
						 reader = new PdfReader(oldFile);
						System.out.println("page count " + reader.getNumberOfPages());
						
						PDFConfig pdfconfig = new PDFConfig("config.properties");
						pdfconfig.print();
					
						// we create a stamper that will copy the document to a new file
						 stamp = new PdfStamper(reader, new FileOutputStream(newFile));
						
							if(pdfconfig.getValue(PDFConfig.encrypt_pdf).equalsIgnoreCase("true")){
								String userpwd = pdfconfig.getValue(PDFConfig.encrypt_user_pwd, "12345");
								String ownerpwd = pdfconfig.getValue(PDFConfig.encrypt_owner_pwd, "12345");
										 stamp.setEncryption(	userpwd.getBytes(), ownerpwd.getBytes(),
							 PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING, 
							 false); //
//							 PdfWriter.STANDARD_ENCRYPTION_40);
							}
							
						 List<HashMap<String, Object>> out = bookmarkHelper.createOutlines( (DefaultMutableTreeNode)treeModel.getRoot(), true, pdfconfig) ;
						if(out!=null){
						 stamp.setOutlines(out);
						}else{
							System.exit(-1);
						}
						int pageNum = reader.getNumberOfPages();
						Rectangle pageRectangle = reader.getPageSize(pageNum);
						
						if(pdfconfig.getValue(PDFConfig.addwatermark).equalsIgnoreCase("true")){
						// 添加水印
							String markText = pdfconfig.getValue(PDFConfig.watermark, "energy1010");
							String markRotation = pdfconfig.getValue(PDFConfig.watermarkangle, "45");
							bookmarkHelper.addWatermark(stamp, pageRectangle, pageNum, markText, markRotation);
						}
					
						System.out.println(String.format("write pdf from file [%s] to [%s] %s", oldFile, newFile, true ? "succ!" : "fail!"));
						
						} catch (Exception e) {
							System.err.println(e.getMessage());
						}
						finally{
							if(stamp!=null)
							stamp.close();
							if(reader!=null)
							reader.close();
						}
					return null;
				}

				protected void done() {
					btn_save_path.setEnabled(false);
					btn_outline_file.setEnabled(false);
					btn_run.setEnabled(false);
					String message;
					try {
						message = String.format("write pdf from file \n[%s] \nto [%s] %s", oldFile, newFile, get() ? "succ!" : "fail!");
						JOptionPane.showMessageDialog(CreateOutlineFrame.this, message, "write pdf result", JOptionPane.INFORMATION_MESSAGE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			};
			worker.execute();
		}
	}
	

    public static void expandTree(JTree tree) { 
        TreeNode root = (TreeNode) tree.getModel().getRoot(); 
        expandAll(tree, new TreePath(root), true); 
    } 

   
    private static void expandAll(JTree tree, TreePath parent, boolean expand) { 
        // Traverse children 
        TreeNode node = (TreeNode) parent.getLastPathComponent(); 
        if (node.getChildCount() >= 0) { 
            for (Enumeration e = node.children(); e.hasMoreElements(); ) { 
                TreeNode n = (TreeNode) e.nextElement(); 
                TreePath path = parent.pathByAddingChild(n); 
                expandAll(tree, path, expand); 
            } 
        } 
        // Expansion or collapse must be done bottom-up 
        if (expand) { 
            tree.expandPath(parent); 
        } else { 
            tree.collapsePath(parent); 
        } 
    } 

	private void init() {
		
		setTitle("IText App");
		getContentPane().setLayout(new BorderLayout());
		leftPanel = new JPanel(new BorderLayout());
		jtree = new JTree();
		jtree.setModel(null);
		leftPanel.add(jtree);
		
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBorder(new EtchedBorder());
		btn_edit_file = new JButton("选择要添加书签的文件");
		btn_edit_file.setPreferredSize(new Dimension(180, btn_edit_file.getPreferredSize().height));
		btn_edit_file.setMaximumSize(new Dimension(180, btn_edit_file.getPreferredSize().height));
		btn_edit_file.addActionListener(this);
		rightPanel.add(btn_edit_file);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));

		btn_save_path = new JButton("选择新文件的保存路径");
		btn_save_path.setPreferredSize(new Dimension(180, btn_save_path.getPreferredSize().height));
		btn_save_path.setMaximumSize(new Dimension(180, btn_save_path.getPreferredSize().height));
		btn_save_path.addActionListener(this);
		btn_save_path.setEnabled(false);
		rightPanel.add(btn_save_path);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));

		btn_outline_file = new JButton("选择定义好的书签文件");
		btn_outline_file.setPreferredSize(new Dimension(180, btn_outline_file.getPreferredSize().height));
		btn_outline_file.setMaximumSize(new Dimension(180, btn_outline_file.getPreferredSize().height));
		btn_outline_file.addActionListener(this);
		btn_outline_file.setEnabled(false);
		rightPanel.add(btn_outline_file);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));

		btn_run = new JButton("执行");
		btn_run.setPreferredSize(new Dimension(180, btn_run.getPreferredSize().height));
		btn_run.setMaximumSize(new Dimension(180, btn_run.getPreferredSize().height));
		btn_run.addActionListener(this);
		btn_run.setEnabled(false);
		rightPanel.add(btn_run);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));

		chooser = new JFileChooser(".");

		this.setSize(180, 160);
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((scrSize.width - getSize().width) / 2, (scrSize.height - getSize().height) / 2);

		this.getContentPane().add(leftPanel, BorderLayout.WEST);
		this.getContentPane().add(rightPanel, BorderLayout.EAST);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

//	public static void main(String[] args) throws DocumentException, IOException {
		
//		  String oldFile = 
//					args[0].trim();//
////					"毛泽东语录.pdf";//
//			"python.pdf";
//				  "chuangye.pdf";
//	  String newFile = 
////						  "chuangye-bookmark.pdf";
//					args[1].trim();//
////					"毛泽东语录带书签.pdf";//
//			"python_test.pdf";
//			String outlinesFile =
//					args[2].trim(); //
////					"content_毛泽东语录.txt" ;
//			"content_创业基础与创新实践.txt";
////			String pageOffset = null;
////			if(args.length>=4){
////				pageOffset = args[3].trim();
////			}
//			
//			String configFile = null;
//			
//			if(args.length>=4){
//				configFile = args[3].trim();
//			}
//			
//			args = new String[] { oldFile, newFile, outlinesFile} ; //, pageOffset};
//			if (args.length > 0) {
//				if (args.length <3  ) {
//					System.err.println("invalid args:"+args);
//					printHelp();
//					System.exit(1);
//				}
				
//				  String oldFile = 	args[0].trim();//
//					String newFile = args[1].trim();//
//					String outlinesFile =args[2].trim(); //
//					String pageOffset = null;
//					if(args.length>=4){
//						pageOffset = args[3].trim();
//					}
		
			
			
//		} else {
//			
//		}


}
