
package com.leo.appmaster.backup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.ThirdAppManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;

public class AppDeletetemView extends FrameLayout implements OnClickListener {

    private ImageView mAppIcon_delete;
    private TextView mAppTitle_delete;
    private TextView mAppVersion_delete;
    private TextView mAppSize_delete;
    private ImageView mButton_delete;

    public AppDeletetemView(Context context) {
        this(context, null);
    }

    public AppDeletetemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppDeletetemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppIcon_delete = (ImageView) findViewById(R.id.iv_icon);
        mAppTitle_delete = (TextView) findViewById(R.id.tv_title);
        // mAppVersion_delete = (TextView) findViewById(R.id.tv_version);
        mAppSize_delete = (TextView) findViewById(R.id.tv_size);
        mButton_delete = (ImageView) findViewById(R.id.bg_delete);
        mButton_delete.setOnClickListener(this);
    }

    public void setTitle(CharSequence title) {
        mAppTitle_delete.setText(title);
    }

    public void setVersion(CharSequence version) {
        mAppVersion_delete.setText(version);
    }

    public void setSize(CharSequence size) {
        mAppSize_delete.setText(size);
    }

    public void setIcon(Drawable icon) {
        mAppIcon_delete.setImageDrawable(icon);
    }

    @Override
    public void onClick(View v) {
        Object tag = getTag();
        if (tag instanceof AppItemInfo) {
            AppItemInfo app = (AppItemInfo) tag;
            if (v == mButton_delete) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "home", "newuninstall");

                ((ThirdAppManager) MgrContext.getManager(MgrContext.MGR_THIRD_APP)).
                        uninstallApp(app.packageName);

            }
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
