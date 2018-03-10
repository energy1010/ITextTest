package com.younger;

/**
 * title, page, index 书签项
 * @author Administrator
 *
 */
public class ContentItem{
	String title;
	String page; 
	String index;
	
	public ContentItem() {
		
	}
	
	public ContentItem(String pindex, String ptitle, String ppage) {
		title = ptitle;
		index = pindex;
		page = ppage;
	}
	
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return String.format("[ index:%s, page:%s, title:%s ]", index, page, title);
	}
}