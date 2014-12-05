
package com.leo.appmaster.backup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppItemInfo;

public class AppRestoreItemView extends FrameLayout implements OnClickListener {

    private ImageView mIcon;
    private View mInstall;
    private View mDelete;
    private TextView mTitle;
    private TextView mVersion;
    private TextView mAppSize;

    public AppRestoreItemView(Context context) {
        this(context, null);
    }

    public AppRestoreItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppRestoreItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIcon = (ImageView) findViewById(R.id.app_icon);
        mInstall =  findViewById(R.id.button_install);
        mInstall.setOnClickListener(this);
        mDelete =  findViewById(R.id.button_delete);
        mDelete.setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.app_title);
        mVersion = (TextView) findViewById(R.id.app_version);
        mAppSize = (TextView) findViewById(R.id.app_size);
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        AppBackupRestoreActivity activity = (AppBackupRestoreActivity)context;
        AppBackupRestoreManager backupManager = activity.getBackupManager();
        Object tag = getTag();
        if(tag instanceof AppItemInfo) {
            AppItemInfo app = (AppItemInfo) tag;
            if(v == mInstall) {
                backupManager.restoreApp(context, app);
            } else if (v == mDelete) {
                activity.tryDeleteApp(app);
            }
        }
    }
    
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }
    
    public void setVersion(CharSequence version) {
        mVersion.setText(version);
    }
    
    public void setSize(CharSequence size) {
        mAppSize.setText(size);
    }
    
    public void setIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
    }

}
