package com.younger;

import java.util.List;

/**
 * 书签对象
 * contain outline item and page offset
 * @see com.younger.ContentItem
 * @author Administrator
 *
 */
public class OutlineObject {
	
	private List<ContentItem> m_data ;
	
	private int m_pageoffset;
	
	
	
	public List<ContentItem> getM_data() {
		return m_data;
	}



	public void setM_data(List<ContentItem> m_data) {
		this.m_data = m_data;
	}



	public int getM_pageoffset() {
		return m_pageoffset;
	}



	public void setM_pageoffset(int m_pageoffset) {
		this.m_pageoffset = m_pageoffset;
	}



	public OutlineObject(List<ContentItem> items, int pageOffset ){
		m_data = items;
		m_pageoffset = pageOffset;
	}

}
