package com.leo.appmaster.ui;

import com.leo.appmaster.model.BaseInfo;

public interface DragGridBaseAdapter {
	/**
	 * ������������
	 * @param oldPosition
	 * @param newPosition
	 */
	public void reorderItems(int oldPosition, int newPosition);
	
	
	/**
	 * ����ĳ��item����
	 * @param hidePosition
	 */
	public void setHideItem(int hidePosition);
	
	/**
	 * ɾ��ĳ��item
	 * @param mLastSelectApp
	 */
	public void removeItem(int position);
	

}
