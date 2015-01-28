
package com.leo.appmaster.imagehide;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class PhotoItem implements Serializable {
    private static final long serialVersionUID = 8682674788506891598L;
    // private int photoID;
    private boolean select;
    private String path;
    private long size;

    public PhotoItem(String path) {
        select = false;
        this.path = path;
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

    public long getSize() {
        File file = new File(path);
        if (file.isFile()) {
            return file.length();
        }
        return 0;

    }
}
