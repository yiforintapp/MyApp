
package com.leo.appmaster.battery;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;

public class EmptyFragment extends BaseFragment {

    @Override
    protected int layoutResourceId() {
        return R.layout.activity_batter_show_empty_view;
    }

    @Override
    protected void onInitUI() {
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
