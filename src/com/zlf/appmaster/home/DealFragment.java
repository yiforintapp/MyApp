package com.zlf.appmaster.home;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.utils.Utils;

/**
 * Created by Administrator on 2016/7/19.
 */
public class DealFragment extends BaseFragment implements View.OnClickListener {


    private TextView mTvJump;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_deal;
    }

    @Override
    protected void onInitUI() {
        mTvJump = (TextView) findViewById(R.id.tv_changjiang);
        setListener();
    }

    private void setListener() {
        mTvJump.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_changjiang:
                jumpToCJLH();
                break;
        }
    }

    private void jumpToCJLH() {
        Utils.startAPP(Constants.CJLH_PACKAGENAME,mActivity);
    }
}
