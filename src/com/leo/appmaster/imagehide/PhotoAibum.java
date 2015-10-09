package com.leo.appmaster.imagehide;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PhotoAibum implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	private String count; 
    private String dirPath;
    private Date mLastModifyDate;
	private List<PhotoItem> bitList = new ArrayList<PhotoItem>();
	
	public PhotoAibum() {
	    
	}
	
	
	public PhotoAibum(String name, String count) {
		super();
		this.name = name;
		this.count = count;
	}


	public List<PhotoItem> getBitList() {
		return bitList;
	}

	public void setBitList(List<PhotoItem> bitList) {
		this.bitList = bitList;
	}


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String path) {
        this.dirPath = path;
        setModifiDate();
    }
	
    public void setModifiDate() {
        File file = new File(dirPath);
        mLastModifyDate = new Date(file.lastModified());
    }
    
    public Date getLastmodified() {
        return mLastModifyDate;
    }
    
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
}
