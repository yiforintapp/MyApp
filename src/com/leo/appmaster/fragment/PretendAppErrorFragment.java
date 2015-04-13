
package com.leo.appmaster.fragment;

import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.GestureTextView;

public class PretendAppErrorFragment extends PretendFragment {

    private GestureTextView mGtv;
    private TextView mTitle;

    private String mTips = "";

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_pretend_app_error;
    }

    @Override
    protected void onInitUI() {
        mTitle = (TextView) findViewById(R.id.tv_pretend_app_name);
        if (mTitle != null) {
            mTitle.setText(mTips);
        }
        mGtv = (GestureTextView) findViewById(R.id.tv_make_sure);
        mGtv.setPretendFragment(this);
    }

    public void setErrorTip(String name) {
        mTips = name;
        if (mTitle != null) {
            mTitle.setText(mTips);
        }
    }
}
