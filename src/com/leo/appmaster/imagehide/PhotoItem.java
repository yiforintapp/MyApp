package com.leo.appmaster.imagehide;

import java.io.Serializable;

public class PhotoItem implements Serializable {
    private static final long serialVersionUID = 8682674788506891598L;
    private int photoID;
    private boolean select;
    private String path;

    public PhotoItem(String path) {
        select = false;
        this.path = path;
    }

    public PhotoItem(String path,int id) {
        select = false;
        this.path = path;
        this.photoID = id;
    }

    public PhotoItem(boolean flag) {
        select = flag;
    }

    public void setPhotoId(int id) {
        this.photoID = id;
    }

    public int getPhotoId() {
        return photoID;
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
