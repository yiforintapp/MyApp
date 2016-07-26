package com.leo.appmaster.home;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;

/**
 * Created by Administrator on 2016/7/18.
 */
public class FindTabFragment extends BaseFragment {
    private TextView mFindTv;

    @Override
    protected void onInitUI() {
        mFindTv = (TextView) findViewById(R.id.find_tab_tv);
        mFindTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, "测试点击", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_find;
    }
}


