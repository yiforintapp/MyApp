package com.leo.appmaster.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;

/**
 * Created by chenfs on 16-3-30.
 */
public class CommonSettingItem extends RelativeLayout {
    private TextView mTvTitle;
    private TextView mTvSummary;
    private RelativeLayout mRlIconParent;
    private RippleView mRvMain;

    private ImageView mIvArrow;
    private CheckBox mCb;

    public static final int TYPE_ARROW = 1;
    public static final int TYPE_CHECKBOX = 2;

    public static final int ID_RIPPLE_VIEW_MAIN = R.id.rv_main;

    private int mCurrentType = 1;

    public CommonSettingItem(Context context) {
        super(context);
    }

    public CommonSettingItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommonSettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(mContext).inflate(R.layout.item_setting_arrow, this, true);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvSummary = (TextView) findViewById(R.id.tv_summary);
        mIvArrow = (ImageView) findViewById(R.id.iv_arrow);
        mRlIconParent = (RelativeLayout) findViewById(R.id.rl_content_tip);
        mRvMain = (RippleView) findViewById(R.id.rv_main);
    }

    public int getMainRippleViewId() {
        return mRvMain.getId();
    }


    public void setRippleViewOnClickLinstener(OnClickListener l) {
        mRvMain.setOnClickListener(l);
    }


    /*  <ImageView
    android:id="@+id/iv_arrow"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_centerInParent="true"
    android:src="@drawable/icn_settings_arrow" />*/


   /* <CheckBox
    android:gravity="center"
    android:id="@+id/cb_setting_advanced_protect"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_centerInParent="true"
    android:button="@drawable/selector_checkbox"
    android:clickable="false"
    android:focusable="false" />*/

    public boolean isChecked() {
        if (mCb != null && mCurrentType == TYPE_CHECKBOX) {
            return mCb.isChecked();
        } else {
            return false;
        }
    }

    public void setChecked(boolean checked) {
        if (mCb != null && mCurrentType == TYPE_CHECKBOX) {
            mCb.setChecked(checked);
        }
    }

    public void setType(int type) {
        mCurrentType = type;
        switch (mCurrentType) {
            case TYPE_ARROW:
                mRlIconParent.removeAllViews();
                if (mIvArrow == null) {
                    mIvArrow = new ImageView(mContext);
                }
                mRlIconParent.addView(mIvArrow);
                RelativeLayout.LayoutParams lp = (LayoutParams) mIvArrow.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                mIvArrow.setLayoutParams(lp);
                mIvArrow.setImageResource(R.drawable.icn_settings_arrow);
                break;
            case TYPE_CHECKBOX:
                mRlIconParent.removeAllViews();
                if (mCb == null) {
                    mCb = new CheckBox(mContext);
                }
                mRlIconParent.addView(mCb);
                RelativeLayout.LayoutParams lp2 = (LayoutParams) mCb.getLayoutParams();
                lp2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp2.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp2.addRule(RelativeLayout.CENTER_IN_PARENT);
                mCb.setLayoutParams(lp2);
                mCb.setButtonDrawable(R.drawable.selector_checkbox);
                mCb.setGravity(Gravity.CENTER);
                mCb.setClickable(false);
                mCb.setFocusable(false);
                break;
            default:
                break;
        }
    }

    public void setSummaryVisable(boolean flag) {
        mTvSummary.setVisibility(flag == true ? View.VISIBLE : View.GONE);
    }

    public void setSummary(String s) {
        mTvSummary.setText(s);
    }

    public void setSummary(int id) {
        mTvSummary.setText(id);
    }

    public void setTitle(String s) {
        mTvTitle.setText(s);
    }

    public void setTitle(int id) {
        mTvTitle.setText(id);
    }
}
