package com.zlf.appmaster.home;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;

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
                Toast.makeText(mActivity,"asd",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
