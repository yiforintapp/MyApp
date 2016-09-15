
package com.zlf.appmaster.home;

import android.os.Bundle;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.WinTopItem;
import com.zlf.appmaster.ui.HorizontalListView;

import java.util.ArrayList;
import java.util.List;


public class WinTestActivity extends BaseActivity {

    private HorizontalListView mHlistview;
    private WinTopAdapter mWinAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_top5);
        init();
        setListener();
        loadData();
    }

    private void init() {
        mHlistview = (HorizontalListView) findViewById(R.id.h_listview);
        mWinAdapter = new WinTopAdapter(this);
        mHlistview.setAdapter(mWinAdapter);
    }

    private void setListener() {

    }

    private void loadData() {
        List<WinTopItem> list = new ArrayList<WinTopItem>();
        for (int i = 0; i < 10; i++) {
            WinTopItem item = new WinTopItem();
            String name = "winName" + i;
            long price = (long) 54321.21;
            item.setWinName(name);
            item.setWinPrice(price);
            list.add(item);
        }
        if (list.size() > 0) {
            mWinAdapter.setList(list);
            mWinAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
