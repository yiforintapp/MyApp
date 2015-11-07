package com.leo.appmaster.phoneSecurity;

/**
 * Created by runlee on 15-10-17.
 */
public class InstructModel{
    int image;
    int title;
    int content;
    int selectImage;
    boolean isSelect;

    public InstructModel(int image,int content, int title,boolean isSelect) {
        this.content = content;
        this.title = title;
        this.image=image;
        this.isSelect=isSelect;
    }
    public InstructModel(int image,int selectImage,int content) {
        this.content = content;
        this.image=image;
        this.selectImage=selectImage;
    }
}