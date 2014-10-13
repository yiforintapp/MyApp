package com.leo.appmaster.ui;

public interface LeoGridBaseAdapter {
	public void reorderItems(int oldPosition, int newPosition);

	public void setHideItem(int hidePosition);

	public void removeItem(int position);
}
