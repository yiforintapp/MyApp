
package com.leo.appmaster.backup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;

public class AppBackupItemView extends FrameLayout {

    public static final int STATE_UNSELECTED = 0;
    public static final int STATE_SELECTED = 1;
    public static final int STATE_BACKUPED = 2;

    private ImageView mAppIcon;
    private TextView mAppTitle;
    private TextView mAppVersion;
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
        mAppVersion = (TextView) findViewById(R.id.app_version);
        mChecked = (ImageView) findViewById(R.id.app_check);
    }

    public void setTitle(CharSequence title) {
        mAppTitle.setText(title);
    }
    
    public void setVersion(CharSequence version) {
        mAppVersion.setText(version);
    }

    public void setIcon(Drawable icon) {
        mAppIcon.setImageDrawable(icon);
    }

    public void setState(int state) {
        switch (state) {
            case STATE_SELECTED:
                mChecked.setImageResource(R.drawable.checkbox_checked);
                break;
            case STATE_BACKUPED:
                mChecked.setImageResource(R.drawable.checkbox_checked_unclickable);
                break;
            default:
                mChecked.setImageResource(R.drawable.checkbox_normal);
                break;
        }
    }

}
