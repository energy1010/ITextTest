package com.younger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

public class BookmarkHelper {

	private static Logger log = LoggerFactory.getLogger(BookmarkHelper.class);

	private static final String INDEX = "Index";
	private static final String TITLE = "Title";
	private static final String ACTION = "Action";
	private static final String PAGE = "Page";
	private static final String OPEN = "Open";
	private static final String COLOR = "Color";
	private static final String STYLE = "Style";

	private static int mline = -1;

	public static DefaultTreeModel getContentTree(String outlinesFile, int pageOffset, boolean withSecIndex)
			throws FileNotFoundException {
		if (!outlinesFile.endsWith(".txt")) {
			outlinesFile += ".txt";
		}
		File dstFile = new File(outlinesFile);
		if (!dstFile.exists()) {
			System.err.println(String.format("dst file %s exists, delete it!", dstFile.getAbsolutePath()));
			log.error(String.format("dst file %s exists, delete it!", dstFile.getAbsolutePath()));
			throw new FileNotFoundException(outlinesFile + " not found!");
		}
		// 读取书签文件， 得到书签对象
		OutlineObject outlineObject = readOutlinesFile(outlinesFile);
		int offset = outlineObject.getM_pageoffset();

		List<ContentItem> m_data = outlineObject.getM_data();
		int size = m_data.size();
		if (size == 0) {
			log.error("outline size is 0");
			return null;
		}
		if (pageOffset > 0) {
			offset = pageOffset;
		}
		DefaultTreeModel treeModel = null;
		treeModel = createOutlines(m_data, offset, withSecIndex, null, null, 0, treeModel);
		return treeModel;
	}

	/**
	 * List item:String[] = {INDEX, PAGE, TITLE}
	 * </p>
	 * %为分割符， #单行注释， \@多行注释 $offset设置页码偏移量</br>
	 * 
	 * @param fileName
	 * @param pageOffset
	 */
	private static OutlineObject readOutlinesFile(String fileName) {
		System.out.println("read outline from file " + fileName);
		List<ContentItem> lines = new ArrayList<ContentItem>();
		int pageOffset = 0;

		int lineNo = 0;
		BufferedReader reader = null;
		try {
			// Reader 类是 Java 的 I/O 中读字符的父类，而 InputStream 类是读字节的父类，
			// InputStreamReader 类就是关联字节到字符的桥梁，它负责在 I/O 过程中处理读取字节到字符的转换，
			// 而具体字节到字符的解码实现它由 StreamDecoder 去实现，
			// 在 StreamDecoder 解码过程中必须由用户指定 Charset 编码格式。值得注意的是如果你没有指定 Charset，
			// 将使用本地环境中的默认字符集，例如在中文环境中将使用 GBK 编码。
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String line = null;
			boolean hasMultipleLines = false;
			while ((line = reader.readLine()) != null) {
				lineNo++;
				line = line.trim();
				// System.out.println("read line:"+line);
				if (line.startsWith("@")) {
					// 多行注释
					if (!hasMultipleLines) {
						hasMultipleLines = true;
					} else {
						hasMultipleLines = false;
					}
					continue;
				}
				if (line.equals("") || line.startsWith("#") || hasMultipleLines) {
					continue;
				}
				if (line.startsWith("$offset")) {
					try {
						pageOffset = Integer.parseInt(line.replace("$offset=", "").trim());
						System.out.println("get pageOffset=" + pageOffset + " from " + fileName);
						log.debug("pageOffset=" + pageOffset);
					} catch (Exception e) {
						System.err.println("invalida page offset " + line + "  " + line.replace("$offset=", "").trim());
					}
					continue;
				}
				ContentItem contentItem = getContentItem(line);
				if (contentItem == null) {
					throw new RuntimeException("at line " + lineNo + "--- invalid line:" + line);
				}
				lines.add(contentItem);
			}
		} catch (Exception e) {
			System.err.println("readOutlinesFile error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new OutlineObject(lines, pageOffset);
	}

	private static ContentItem getContentItem(String line) {
		// 去掉中间及两端的所有空格
		line = line.replaceAll("\\s+", "");
		Pattern pattern = Pattern.compile("([\\d*\\.{0,1}]*)%([+-]?\\d+)%(.+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			int groupCount = matcher.groupCount();
			if (groupCount != 3) {
				System.err.println("current line is error: " + line);
				log.error("current line is error: " + line);
				System.exit(1);
			}
			String index = matcher.group(1);
			String page = matcher.group(2);
			String title = matcher.group(3);
			ContentItem contentIterm = new ContentItem(index, title, page);
			return contentIterm;
		}
		log.error(String.format("invalid line:%s\n", line));
		return null;
	}

	public static void printTreeModel(String outlinesFile, int pageOffset, boolean withSecIndex) {
		DefaultTreeModel treeModel;
		try {
			treeModel = getContentTree(outlinesFile, 0, true);
			// 打印目录树
			if (treeModel == null) {
				System.err.println("null TreeModel");
				return;
			}
			printTreeModel(treeModel);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void generateConfigTemplateFile(String configFile) {
		// TODO:generateConfigTemplateFile
		StringBuffer sb = new StringBuffer();
		sb.append("#pdf配置");sb.append("\n");sb.append("\n");sb.append("\n");
		sb.append("\n");
		sb.append("#编码设置");
		sb.append("\n");
		sb.append("encoding=utf-8");sb.append("\n");
		sb.append("\n");
		
		sb.append("#水印设置");
		sb.append("\n");
		sb.append("addwatermark=false");sb.append("\n");
		sb.append("#addwatermark=true");sb.append("\n");
		sb.append("#watermarkangle=25");sb.append("\n");
		sb.append("#watermark=energy1010");sb.append("\n");
		sb.append("\n");
		
		sb.append("#目录设置");sb.append("\n");
		sb.append("#其他目录颜色");sb.append("\n");
		sb.append("#outline_color=0 0.50196 0.50196");sb.append("\n");
		sb.append("#一级目录颜色");sb.append("\n");
		sb.append("outline_color_sec=255 0 0");sb.append("\n");
		sb.append("#二级目录颜色");sb.append("\n");
		sb.append("outline_color_subsec=0 0 255");sb.append("\n");
		sb.append("#默认目录颜色");sb.append("\n");
		sb.append("outline_color=0 0.50196 0.50196");sb.append("\n");
		sb.append("outline_style=bold");sb.append("\n");
		sb.append("#outline_color=red");sb.append("\n");
		sb.append("\n");
		
		sb.append("#pdf加密");sb.append("\n");
		sb.append("encrypt_pdf=false");sb.append("\n");
		sb.append("encrypt_user_pwd=12345");sb.append("\n");
		sb.append("encrypt_owner_pwd=12345");sb.append("\n");
		sb.append("\n");
		BufferedWriter fw = null;
		try {
			 fw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(configFile), StandardCharsets.UTF_8) );
			fw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}finally {
			if(fw!=null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void printTreeModel(TreeModel treeModel) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		System.out.println("max level: " + root.getDepth());
		// 前序遍历
		Enumeration<DefaultMutableTreeNode> enu = root.preorderEnumeration();
		while (enu.hasMoreElements()) {
			DefaultMutableTreeNode node = enu.nextElement();
			boolean isRoot = node.isRoot();
			boolean isLeaf = node.isLeaf();
			int depth = node.getLevel();
			if (isRoot) {
				System.out.println("-" + node.getUserObject());
			} else {
				System.out.printf(" ");
				for (int i = 0; i < depth - 1; i++) {
					System.out.printf("|");
				}
				System.out.printf("+" + node.getUserObject());
				if (!isLeaf) {
					System.out.printf("/");
				}
				System.out.println();
			}
		}
	}

	/**
	 * 给pdf文件添加水印
	 * 
	 * @param InPdfFile
	 *            要加水印的原pdf文件路径
	 * @param outPdfFile
	 *            加了水印后要输出的路径
	 * @param markImagePath
	 *            水印图片路径
	 * @param pageSize
	 *            原pdf文件的总页数（该方法是我当初将数据导入excel中然后再转换成pdf所以我这里的值是用excel的行数计算出来的，
	 *            如果不是我这种可以 直接用reader.getNumberOfPages()获取pdf的总页数）
	 * @throws Exception
	 */
	public static void addWatermark(PdfStamper stamp, Rectangle pageRectangle, int pageSize, String markText,
			String rotation) {
		// Image img;
		try {
			// img = Image.getInstance(markImagePath);
			float width = pageRectangle.getWidth();
			float height = pageRectangle.getHeight();
			// img.setAbsolutePosition(width/2f,height/2f);
			// img.setRotation(45);
			// // ifen0292.jpgmg.setTransparency(transparency)
			// img.setGrayFill(20);//透明度，灰色填充
			// FontFactory.register("/Library/Fonts/Hei.ttf");
			PdfGState gs = new PdfGState();
			PdfContentByte content;
			// BaseFont font = new Font(FontFamily.HELVETICA, Font.DEFAULTSIZE,
			// Font.NORMAL).getCalculatedBaseFont(false);//FontUtil.getFont2();
			BaseFont helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			for (int i = 1; i <= pageSize; i++) {
				content = stamp.getOverContent(i);// 在内容上方加水印
				// content = stamp.getUnderContent(i);
				// PdfGraphics2D graphics2D = new PdfGraphics2D(content,
				// PageSize.A4.getWidth(), PageSize.A4.getHeight());
				// graphics2D.drawString("Hello World", 36, 54);
				// graphics2D.dispose();
				// 插入水印
				gs.setFillOpacity(0.3f);
				gs.setStrokeOpacity(0.6f);
				content.saveState();
				content.setGState(gs);

				// 开始写入文本
				if (i % 2 == 0) {
					content.setColorFill(BaseColor.RED);
				} else {
					content.setColorFill(BaseColor.BLUE);
				}
				content.beginText();
				content.setFontAndSize(helv, 40);
				// content.setTextMatrix(0, 0);
				// 水印文字成45度角倾斜
				content.showTextAligned(Element.ALIGN_CENTER, markText, width / 2, height / 2,
						Integer.valueOf(rotation));
				// under.addImage(img);

				content.endText();
				// content.setRGBColorStroke(0xFF, 0x00, 0x00);
				// content.setLineWidth(5f);
				// content.ellipse(250, 450, 350, 550);
				// content.stroke();
				content.restoreState();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建层次目录 TODO:用树结构表示，同时可视化显示
	 * 
	 * @param data
	 * @return
	 */
	public static DefaultTreeModel createOutlines(List<ContentItem> data, int pageOffset, boolean withSecIndex,
			String parentTitle, DefaultMutableTreeNode parentNode, int depth, DefaultTreeModel m_treeModel) {

		// System.out.println(String.format("createOutlines %s" , parentTitle)
		// );
		List<ContentItem> list = new ArrayList<ContentItem>();// sec level
		ContentItem contentItem = null;
		int lastLine = mline + 1;// current start from 0
		do {
			mline++;// start from 0 in data
			// 根目錄為Null
			String title = null;
			String secIndex = data.get(mline).index;
			// if (withSecIndex && (!secIndex.equals("0"))) {
			// //根目錄沒有标号
			// title = secIndex + " " + data.get(mline).title;
			// } else {
			// //目录
			title = data.get(mline).title;
			// }
			String page = data.get(mline).page;
			contentItem = new ContentItem();
			contentItem.setTitle(title);
			contentItem.setIndex(secIndex);
			if (page != null && !page.isEmpty()) {
				page = Integer.parseInt(page) + pageOffset + "";
				contentItem.setPage(Integer.parseInt(page) + pageOffset + "");
			} else {
				contentItem.setPage("0");
			}
			// for(int i=0;i<depth;i++){
			// System.out.print("---");
			// }
			// System.out.println(String.format("index:%s, page:%s, title:%s",
			// secIndex, page, title));
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(secIndex + "\t" + title + "\t" + page);
			if ((m_treeModel == null || m_treeModel.getRoot() == null) && parentNode == null) {
				// log.info("set root "+title);
				m_treeModel = new DefaultTreeModel(newNode);
			} else {
				if (isSubling(secIndex, "0")) {
					((DefaultMutableTreeNode) m_treeModel.getRoot()).add(newNode);
				} else {
					// log.info(String.format( "add child node %s %s ", newNode
					// , parentNode));
					parentNode.add(newNode);
				}
			}

			if (hasChildren(mline, data)) {
				m_treeModel = createOutlines(data, pageOffset, withSecIndex, title, newNode, depth + 1, m_treeModel);// ,
																														// mline);
			}
			list.add(contentItem);
		} while (hasSubling(lastLine, data, mline));
		return m_treeModel;
	}

	/**
	 * treeModel转为目录结构
	 * 
	 * @param node
	 * @param withSecIndex
	 * @param config
	 * @return
	 */
	public List<HashMap<String, Object>> createOutlines(DefaultMutableTreeNode node, boolean withSecIndex,
			PDFConfig config) {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> map = new HashMap<String, Object>();
		boolean isRoot = node.isRoot();
		boolean isLeaf = node.isLeaf();
		int childCount = node.getChildCount();
		int depth = node.getLevel();// start from 0
		String[] data = node.getUserObject().toString().split("\t");
		if (data.length != 3) {
			System.err.println("invalid tree model");
			return null;
		}
		String secIndex = data[0].trim();
		String title = data[1].trim();
		String page = data[2].trim();
		String outlineTitle = null;
		if (secIndex.isEmpty() || secIndex.equals("0") || secIndex.startsWith("-")) {
			// outlineTitle="目录";
			outlineTitle = title;
		} else {
			outlineTitle = secIndex + "\t" + title;
		}
		map.put(TITLE, outlineTitle);
		map.put(ACTION, "GoTo");
		map.put(PAGE, page);
		map.put(OPEN, true);
		// 设置目录颜色
		if (depth == 1 && !secIndex.startsWith("-") && !secIndex.equals("0")) {
			// 一级目录
			map.put(COLOR, config.getValue(PDFConfig.outline_color_sec, "0 0.50196 0.50196"));// "0
																								// 0.50196
																								// 0.50196"
		} else if (depth == 2) {
			map.put(COLOR, config.getValue(PDFConfig.outline_color_subsec, "black"));// "0
																						// 0.50196
																						// 0.50196"
		} else if (depth > 2) {
			map.put(COLOR, config.getValue(PDFConfig.outline_color, "black"));// "0
																				// 0.50196
																				// 0.50196"
		} else {
			if (config.getValue(PDFConfig.outline_color_subsec) == null
					&& config.getValue(PDFConfig.outline_color_sec) == null) {
				map.put(COLOR, config.getValue(PDFConfig.outline_color, "0 0.50196 0.50196"));// "0
																								// 0.50196
																								// 0.50196"
			}
		}
		map.put(STYLE, config.getValue(PDFConfig.outline_style, "bold"));
		if (isRoot) {
			// add map
			list.add(map);
			for (int i = 0; i < childCount; i++) {
				DefaultMutableTreeNode chi = (DefaultMutableTreeNode) node.getChildAt(i);
				int dep1 = chi.getLevel();
				if (dep1 != depth + 1) {
					continue;
				}
				List<HashMap<String, Object>> map2 = createOutlines(chi, withSecIndex, config);
				list.addAll(map2);
				// System.out.println("all size: "+ list.size());
			}

		} else {
			if (!isLeaf) {
				List<HashMap<String, Object>> map1 = new ArrayList<HashMap<String, Object>>();
				for (int i = 0; i < childCount; i++) {
					DefaultMutableTreeNode chi = (DefaultMutableTreeNode) node.getChildAt(i);
					int dep1 = chi.getLevel();
					if (dep1 != depth + 1) {
						continue;
					}
					List<HashMap<String, Object>> map2 = createOutlines(chi, withSecIndex, config);
					map1.addAll(map2);
				}
				map.put("Kids", map1);
			} // if

			list.add(map);
		}
		return list;
	}

	public void setOutlines(String oldFile, String newFile, String outlinesFile, String configFile)
			throws DocumentException, IOException {
		PdfReader reader = null;
		PdfStamper stamp = null;

		try {

			DefaultTreeModel treeModel = getContentTree(outlinesFile, 0, true);
			if (treeModel == null) {
				System.err.println("null TreeModel");
				return;
			}
			// 打印目录树
			printTreeModel(treeModel);
			// create a reader for a certain document
			if( !new File(oldFile).exists() || !new File(oldFile).isFile() ){
				throw new FileNotFoundException(String.format("input file:%s not found!", oldFile) );
			}
			reader = new PdfReader(oldFile);
			System.out.println("page count " + reader.getNumberOfPages());

			if (configFile == null || configFile.isEmpty())
				configFile = PDFConfig.DEFAULT_CONF_FILE;
			PDFConfig pdfconfig = new PDFConfig(configFile);
			pdfconfig.print();

			// we create a stamper that will copy the document to a new file
			stamp = new PdfStamper(reader, new FileOutputStream(newFile));

			if (pdfconfig.getValue(PDFConfig.encrypt_pdf).equalsIgnoreCase("true")) {
				String userpwd = pdfconfig.getValue(PDFConfig.encrypt_user_pwd, "12345");
				String ownerpwd = pdfconfig.getValue(PDFConfig.encrypt_owner_pwd, "12345");
				stamp.setEncryption(userpwd.getBytes(), ownerpwd.getBytes(),
						PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING, false); //
				// PdfWriter.STANDARD_ENCRYPTION_40);
			}

			List<HashMap<String, Object>> out = createOutlines((DefaultMutableTreeNode) treeModel.getRoot(), true,
					pdfconfig);
			if (out != null) {
				stamp.setOutlines(out);
			} else {
				System.exit(-1);
			}
			int pageNum = reader.getNumberOfPages();
			Rectangle pageRectangle = reader.getPageSize(pageNum);

			if (pdfconfig.getValue(PDFConfig.addwatermark).equalsIgnoreCase("true")) {
				// 添加水印
				String markText = pdfconfig.getValue(PDFConfig.watermark, "energy1010");
				String markRotation = pdfconfig.getValue(PDFConfig.watermarkangle, "45");
				addWatermark(stamp, pageRectangle, pageNum, markText, markRotation);
			}

			System.out.println(
					String.format("write pdf from file [%s] to [%s] %s", oldFile, newFile, true ? "succ!" : "fail!"));

		} catch (Exception e) {
			System.err.println("setOutlines error: " + e.getMessage());
		} finally {
			if (stamp != null)
				stamp.close();
			if (reader != null)
				reader.close();
		}
	}

	/**
	 * List item:String[] = {INDEX, PAGE, TITLE}
	 * </p>
	 * %为分割符， #单行注释， \@多行注释</br>
	 * 
	 * @param file
	 * @param pageOffset
	 */
	// private List<Map<String, String>> readOutlinesFile1(String file, int
	// pageOffset) {
	// log.debug("read outline from file " + file);
	// System.out.println("read outline from file " + file);
	// List<Map<String, String>> lines = new ArrayList<Map<String, String>>();
	// Map<String, String> map = null;
	// BufferedReader reader = null;
	// try {
	// reader = new BufferedReader(new FileReader(file));
	// String line = null;
	// boolean hasMultipleLines = false;
	// while ((line = reader.readLine()) != null) {
	// // line = line.replaceAll("^\\s*|\\s", "");
	// line = line.trim();
	// if (line.startsWith("@")) {
	// if (!hasMultipleLines) {
	// hasMultipleLines = true;
	// } else {
	// hasMultipleLines = false;
	// }
	// }
	// if (line.equals("") || line.startsWith("#") || hasMultipleLines) {
	// continue;
	// }
	// if(line.startsWith("$offset")){
	// try {
	// m_pageOffset = Integer.parseInt(line.replace("$offset=", "").trim());
	// pageOffset = m_pageOffset;
	// System.out.println("pageOffset="+pageOffset);
	// log.debug("pageOffset="+pageOffset);
	// if(pageOffset!=m_pageOffset){
	// System.err.println("pageoffset is not equal!");
	// }
	// } catch (Exception e) {
	// System.err.println("invalida page offset "+ line + "
	// "+line.replace("$offset=", "").trim() );
	// }
	// continue;
	// }
	// // 去掉中间及两端的所有空格
	// line = line.replaceAll("\\s+", "");
	// Pattern pattern = Pattern.compile("([\\d*\\.{0,1}]*)%([+-]?\\d+)%(.+)",
	// Pattern.CASE_INSENSITIVE);
	// Matcher matcher = pattern.matcher(line);
	// while (matcher.find()) {
	// int groupCount = matcher.groupCount();
	// if (groupCount != 3) {
	// System.err.println("current line is error: " + line);
	// System.exit(1);
	// }
	// map = new HashMap<String, String>();
	// // for (int i = 0; i < groupCount+1; i++) {
	// // System.out.println(matcher.group(i));
	// // }
	// map.put("Index", matcher.group(1));
	// int page = Integer.valueOf(matcher.group(2)) + pageOffset;
	// map.put("Page", page + "");
	// map.put("Title", matcher.group(3));
	// map.put("Open", "true");
	// // map.put("Color", "red");
	// System.out.println(String.format("index:%s, page:%s, title:%s",
	// matcher.group(1), page + "", matcher.group(3)));
	// lines.add(map);
	// }
	// }
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// reader.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// return lines;
	// }

	// /**
	// * @deprecated
	// * List item:String[] = {INDEX, PAGE, TITLE}</p> %为分割符， #单行注释， \@多行注释</br>
	// *
	// * @param file
	// * @param pageOffset
	// */
	// private List<Map<String, String>> readOutlinesFile1(String file) {
	//// log.debug("read outline from file " + file);
	//// System.out.println("read outline from file " + file);
	// List<Map<String, String>> lines = new ArrayList<Map<String, String>>();
	// Map<String, String> map = null;
	// BufferedReader reader = null;
	// int pageOffset = -1;
	// try {
	// reader = new BufferedReader(new FileReader(file));
	// String line = null;
	// boolean hasMultipleLines = false;
	// while ((line = reader.readLine()) != null) {
	// // line = line.replaceAll("^\\s*|\\s", "");
	// line = line.trim();
	// if (line.startsWith("@")) {
	// if (!hasMultipleLines) {
	// hasMultipleLines = true;
	// } else {
	// hasMultipleLines = false;
	// }
	// }
	// if (line.equals("") || line.startsWith("#") || hasMultipleLines) {
	// continue;
	// }
	// if(line.startsWith("$offset")){
	// try {
	// pageOffset = Integer.parseInt(line.replace("$offset=", "").trim());
	// System.out.println("pageOffset="+pageOffset);
	//// log.debug("pageOffset="+pageOffset);
	// } catch (Exception e) {
	// System.err.println("invalida page offset "+ line + "
	// "+line.replace("$offset=", "").trim() );
	// }
	// continue;
	// }
	// // 去掉中间及两端的所有空格
	// line = line.replaceAll("\\s+", "");
	// Pattern pattern = Pattern.compile("([\\d*\\.{0,1}]*)%([+-]?\\d+)%(.+)",
	// Pattern.CASE_INSENSITIVE);
	// Matcher matcher = pattern.matcher(line);
	// while (matcher.find()) {
	// int groupCount = matcher.groupCount();
	// if (groupCount != 3) {
	// System.err.println("current line is error: " + line);
	// System.exit(1);
	// }
	// map = new HashMap<String, String>();
	// // for (int i = 0; i < groupCount+1; i++) {
	// // System.out.println(matcher.group(i));
	// // }
	// map.put("Index", matcher.group(1));
	// int page = Integer.valueOf(matcher.group(2)) + pageOffset;
	// map.put("Page", page + "");
	// map.put("Title", matcher.group(3));
	// map.put("Open", "true");
	// // map.put("Color", "red");
	// System.out.println(String.format("index:%s, page:%s, title:%s",
	// matcher.group(1), page + "", matcher.group(3)));
	// lines.add(map);
	// }
	// }
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// reader.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// return lines;
	// }

	private static boolean hasSubling(int lastLine, List<ContentItem> m_data, int mline) {
		if (mline >= m_data.size() - 1)
			return false;
		String lastIndexStr = m_data.get(lastLine).index;
		String nextIndexStr = m_data.get(mline + 1).index;
		// System.out.println("hasSubling print "+ lastLine+ " "+ (m_line+1));
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < nextIndexStr.length(); i++) {
			if (nextIndexStr.charAt(i) == '.') {
				stringBuffer.append("@");
			} else {
				stringBuffer.append(nextIndexStr.charAt(i));
			}
		}
		nextIndexStr = stringBuffer.toString();
		stringBuffer = new StringBuffer();
		for (int i = 0; i < lastIndexStr.length(); i++) {
			if (lastIndexStr.charAt(i) == '.') {
				stringBuffer.append("@");
			} else {
				stringBuffer.append(lastIndexStr.charAt(i));
			}
		}
		lastIndexStr = stringBuffer.toString();

		String[] currentIndexArray = nextIndexStr.replaceAll("@", " ").split(" ");
		String[] lastIndexArray = lastIndexStr.replaceAll("@", " ").split(" ");
		if (currentIndexArray.length != lastIndexArray.length)
			return false;// 根据 . 分割后的长度判断是否为兄弟节点
		return true;
	}

	private static boolean hasChildren(int line, List<ContentItem> m_data) {
		if (line >= m_data.size() - 1)
			return false;
		String currIndexStr = (String) m_data.get(line).index;
		String nextIndexStr = (String) m_data.get(line + 1).index;
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < currIndexStr.length(); i++) {
			if (currIndexStr.charAt(i) == '.') {
				stringBuffer.append("@");
			} else {
				stringBuffer.append(currIndexStr.charAt(i));
			}
		}
		currIndexStr = stringBuffer.toString();
		stringBuffer = new StringBuffer();
		for (int i = 0; i < nextIndexStr.length(); i++) {
			if (nextIndexStr.charAt(i) == '.') {
				stringBuffer.append("@");
			} else {
				stringBuffer.append(nextIndexStr.charAt(i));
			}
		}
		nextIndexStr = stringBuffer.toString();
		String[] currentIndexArray = currIndexStr.replaceAll("@", " ").split(" ");
		String[] nextIndexArray = nextIndexStr.replaceAll("@", " ").split(" ");
		// && currentIndexArray[currentIndexArray.length-2]==
		// nextIndexArray[currentIndexArray.length-2]
		if (currentIndexArray.length >= nextIndexArray.length) {
			return false;// 根据 . 分割后的长度判断是否为同级
		}
		return true;
	}

	private static boolean isSubling(String index1, String index2) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < index1.length(); i++) {
			if (index1.charAt(i) == '.') {
				stringBuffer.append("@");
			} else {
				stringBuffer.append(index1.charAt(i));
			}
		}
		index1 = stringBuffer.toString();
		stringBuffer = new StringBuffer();
		for (int i = 0; i < index2.length(); i++) {
			if (index2.charAt(i) == '.') {
				stringBuffer.append("@");
			} else {
				stringBuffer.append(index2.charAt(i));
			}
		}
		index2 = stringBuffer.toString();

		String[] currentIndexArray = index1.replaceAll("@", " ").split(" ");
		String[] lastIndexArray = index2.replaceAll("@", " ").split(" ");
		if (currentIndexArray.length != lastIndexArray.length)
			return false;// 根据 . 分割后的长度判断是否为兄弟节点
		return true;
	}

	public static String formatBookmark(String bookmarkFileName) {
		String result = null;
		try {
			DefaultTreeModel treeModel = getContentTree(bookmarkFileName, 0, true);
			if (treeModel == null) {
				System.err.println("null TreeModel");
				return null;
			}
			File TempFile = new File(bookmarkFileName + ".tmp");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bookmarkFileName), StandardCharsets.UTF_8 ) );
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
			System.out.println("max level: " + root.getDepth());
			// 前序遍历
			Enumeration<DefaultMutableTreeNode> enu = root.preorderEnumeration();
			while (enu.hasMoreElements()) {
				DefaultMutableTreeNode node = enu.nextElement();
				boolean isRoot = node.isRoot();
				boolean isLeaf = node.isLeaf();
				int depth = node.getLevel();
				if (isRoot) {
					System.out.println("-" + node.getUserObject());
				} else {
					bw.write(" ");
					System.out.printf(" ");
					for (int i = 0; i < depth - 1; i++) {
						System.out.printf("|");
						bw.write("|");
					}
					System.out.printf("+" + node.getUserObject());
					bw.write("+" + node.getUserObject());
					if (!isLeaf) {
						System.out.printf("/");
						bw.write("/");
					}
					System.out.println();
					bw.write("\n");
				}
			}
			bw.close();
			if(TempFile.isFile() && TempFile.exists() ){
				new File(bookmarkFileName ).delete();
				log.debug("save bookmarkFileName: "+ bookmarkFileName);
				TempFile.renameTo(new File(bookmarkFileName ));
			}
			
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}

		return result;
	}

	public static void generateBookmarkTemplate(String templateFile) {

		if(!templateFile.endsWith(".txt")){
			templateFile = templateFile.concat(".txt");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("#title: 标题\n");
		sb.append("#\t%为分割符， #单行注释， @多行注释");
		sb.append("\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("#书签格式:\n");
		sb.append("#\t序号\t起始页码\t书签名\n\n\n");
		sb.append("#偏移量\n$offset=11\n");
		sb.append("\n");
		sb.append("@ start 多行注释\n");
		sb.append("#实际页码 page 5\n");	
		sb.append("\n0\t%\t-6\t%\t前言\n");
		sb.append("\n0\t%\t-6\t%\t目录\n");
		sb.append("#实际页码 page 5\n");	
		sb.append("\n1\t%\t1\t%\t第一章\n");
		sb.append("\t1.1\t%\t2\t%\t第一章第一节\n");
		sb.append("\t\t1.1.1\t%\t3\t%\t第一章第一节第一小节\n");
		sb.append("\t\t#1.1.2\t%\t3\t%\t第一章第一节第二小节\n\n");
		sb.append("\t1.2\t%\t4\t%\t第一章第二节\n");

		sb.append("\n2\t%\t5\t%\t第二章 \n");
		sb.append("\t2.1\t%\t2\t%\t第二章第一节\n");
		sb.append("\t2.2\t%\t4\t%\t第一章第二节\n");
		
		sb.append("\n3\t%\t8\t%\t第三章 \n");
		sb.append("\t3.1\t%\t2\t%\t第三章第一节\n");
		sb.append("\t3.2\t%\t4\t%\t第三章第二节\n");
		
		sb.append("\n4\t%\t8\t%\t第四章 \n");
		sb.append("\t4.1\t%\t2\t%\t第四章第一节\n");
		sb.append("\t4.2\t%\t4\t%\t第四章第二节\n");
		
		sb.append("\n5\t%\t8\t%\t第五章 \n");
		sb.append("\t5.1\t%\t2\t%\t第五章第一节\n");
		sb.append("\t5.2\t%\t4\t%\t第五章第二节\n");
		
		sb.append("\n0\t%\t10\t%\t结语 \n");
		sb.append("\n0\t%\t10\t%\t附录 \n");
		
		sb.append("@ end 多行注釋結束\n");
		BufferedWriter fw;
//		InputStreamReader类是从字节到字符的转化桥梁，OutputstreamWriter类是从字符到字节的转化桥梁。所以如果需要添加上编码时只需要在生成IInputStreamReader或者OutputstreamWriter对象的时候加上编码格式即可。
//
//		对于文件，只需要从里到外包装FileInputStream,InputStreamReader，BufferedReader即可。即先是读到字节，然后字节转化为字符，然后把字符送到缓冲区。
		try {
			fw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(templateFile), StandardCharsets.UTF_8) );
			//TODO: 保存文件编码不是utf-8
//			String content =new String(sb.toString().getBytes(), StandardCharsets.UTF_8); //Charset.forName("utf-8") );
//			 new String(str.getBytes("ISO8859-1"),"UTF-8");
			String content =sb.toString();
			fw.write(content);
			fw.flush();
			fw.close();
			log.info("generateBookmarkTemplate :"+templateFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
