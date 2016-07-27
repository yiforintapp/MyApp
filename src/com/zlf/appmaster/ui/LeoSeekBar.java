package com.zlf.appmaster.ui;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class LeoSeekBar extends SeekBar{

    public LeoSeekBar(Context context){
        super(context);
    }
    
    public LeoSeekBar(Context context , AttributeSet attrs){
        super(context, attrs);
    }
    
    public LeoSeekBar(Context context,AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }
    
    private Drawable mThumb;
    
    @Override
    public void setThumb(Drawable thumb) {
        // TODO Auto-generated method stub
        super.setThumb(thumb);
        mThumb = thumb;
    }
    
    public Drawable getSeekBarThumb(){
        return mThumb;
    }
}
