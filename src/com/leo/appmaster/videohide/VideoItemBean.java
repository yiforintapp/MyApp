package com.leo.appmaster.videohide;

import java.io.Serializable;

public class VideoItemBean implements Serializable {
	private boolean select;
	private String path;
	private String name;

	public VideoItemBean(boolean select, String path, String name) {
		super();
		this.select = select;
		this.path = path;
		this.name = name;

	}

	public VideoItemBean(String path) {
		super();
		this.path = path;
	}

	public VideoItemBean(boolean select) {
		super();
		this.select = select;
	}

	public VideoItemBean() {
		super();
	}

	public boolean isSelect() {
		return select;
	}

	public void setSelect(boolean select) {
		this.select = select;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
