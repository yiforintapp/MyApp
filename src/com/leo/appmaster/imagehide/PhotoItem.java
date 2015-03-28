package com.leo.appmaster.imagehide;

import java.io.Serializable;

public class PhotoItem implements Serializable {
	private static final long serialVersionUID = 8682674788506891598L;
//	private int  photoID;
	private boolean select;
	private String path;
	public PhotoItem(String path) {
		select = false;
		this.path=path;
	}
	
	public PhotoItem(boolean flag) {
		select = flag;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isSelect() {
		return select;
	}
	public void setSelect(boolean select) {
		this.select = select;
	}
}
