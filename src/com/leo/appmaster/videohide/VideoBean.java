
package com.leo.appmaster.videohide;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;

public class VideoBean implements Serializable {
    private String name;
     private String count;
    private String dirPath;
    private Date mLastModifyDate;
    private List<VideoItemBean> bitList = new ArrayList<VideoItemBean>();
    private String path;

    
    public VideoBean(String name, String count, String dirPath,
			Date mLastModifyDate, List<VideoItemBean> bitList, String path) {
		super();
		this.name = name;
		this.count = count;
		this.dirPath = dirPath;
		this.mLastModifyDate = mLastModifyDate;
		this.bitList = bitList;
		this.path = path;
	}
	public VideoBean() {
        super();
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
    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
    public Date getmLastModifyDate() {
        return mLastModifyDate;
    }
    public void setmLastModifyDate() {
        File file = new File(dirPath);
        mLastModifyDate = new Date(file.lastModified());
    }
    public List<VideoItemBean> getBitList() {
        return bitList;
    }
    public void setBitList(List<VideoItemBean> bitList) {
        this.bitList = bitList;
    }
    public String getCount() {
        return count;
    }
    public void setCount(String count) {
        this.count = count;
    }
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setmLastModifyDate(Date mLastModifyDate) {
		this.mLastModifyDate = mLastModifyDate;
	}
 
    

}
