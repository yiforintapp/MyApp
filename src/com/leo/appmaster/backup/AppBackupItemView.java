
package com.leo.appmaster.backup;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

public class AppBackupItemView extends FrameLayout {

    public static final int STATE_UNSELECTED = 0;
    public static final int STATE_SELECTED = 1;
    public static final int STATE_BACKUPED = 2;

    private ImageView mAppIcon;
    private TextView mAppTitle;
    private TextView mAppVersion;
    private TextView mAppSize;
    private TextView tv_app_check;
    private ImageView mChecked;

    public AppBackupItemView(Context context) {
        this(context, null);
    }

    public AppBackupItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppBackupItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppIcon = (ImageView) findViewById(R.id.app_icon);
        mAppTitle = (TextView) findViewById(R.id.app_title);
        mAppSize = (TextView) findViewById(R.id.app_size);
        mChecked = (ImageView) findViewById(R.id.app_check);
        tv_app_check = (TextView) findViewById(R.id.tv_app_check);
    }

    public void setTitle(CharSequence title) {
        mAppTitle.setText(title);
    }

    public void setVersion(CharSequence version) {
        mAppVersion.setText(version);
    }

    public void setSize(CharSequence size) {
        mAppSize.setText(size);
    }

    public void setIcon(Drawable icon) {
        mAppIcon.setImageDrawable(icon);
    }

    public void setState(int state) {
        switch (state) {
            case STATE_SELECTED:
                setEnabled(true);
                tv_app_check.setVisibility(View.GONE);
                mChecked.setImageResource(R.drawable.app_select);
                mChecked.setVisibility(View.VISIBLE);
                break;
            case STATE_BACKUPED:
                setEnabled(false);
                Resources resources = AppMasterApplication.getInstance().getResources();
                tv_app_check.setVisibility(View.VISIBLE);
                tv_app_check.setText(resources.getString(R.string.back_backed));
                mChecked.setVisibility(View.GONE);
                break;
            default:
                setEnabled(true);
                tv_app_check.setVisibility(View.GONE);
                mChecked.setImageResource(R.drawable.app_unselect);
                mChecked.setVisibility(View.VISIBLE);
                break;
        }
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }

}
