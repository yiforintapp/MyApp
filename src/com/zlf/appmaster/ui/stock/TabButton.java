package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;

/**
 * Created by think on 2014/11/19.
 */
public class TabButton extends RelativeLayout {
//    private static final String TAG = "TabButton";
    TextView mTitle;
    public TabButton(Context context) {
        super(context);
        initView(context,null);

    }

    public TabButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs);
    }

    private void initView(Context context, AttributeSet attrs){
        LayoutInflater inflater = LayoutInflater.from(context);
        if (isInEditMode()) {
            return;
        }

        try{
            View view = inflater.inflate(R.layout.layout_tab_button, this, true);

            mTitle = (TextView)findViewById(R.id.title);
            if(null != attrs){
//                QLog.i(TAG,"attrs");
                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabButton);
//                QLog.i(TAG,"TypedArray");
                String attrStr = a.getString(R.styleable.TabButton_titleText);
                if(!TextUtils.isEmpty(attrStr)){
                    mTitle.setText(attrStr);
//                    QLog.i(TAG,"setText:"+attrStr);
                }
                a.recycle();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void setTitleText(String text){
        if (mTitle != null) {
            mTitle.setText(text);
        }
    }
}

