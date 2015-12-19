
package com.leo.appmaster.callFilter;

import android.view.View;
import android.widget.ListView;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;

public class CallFilterFragment extends BaseFragment {

    private ListView mBlackListView;
    private View mNothingToShowView;

    @Override
    protected int layoutResourceId() {
        return R.layout.black_list_fragment;
    }

    @Override
    protected void onInitUI() {
        mBlackListView = (ListView) findViewById(R.id.list_black_list);
        mBlackListView.setVisibility(View.GONE);
        mNothingToShowView = findViewById(R.id.content_show_nothing);
        mNothingToShowView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
