package com.zlf.appmaster.hometab;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.home.SwapListAdapter;
import com.zlf.appmaster.model.SelectStockInfo;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.utils.PrefConst;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/17.
 */
public class SelectStockActivity extends Activity {

    public static final String SELECT_TAG = "select_tag";
    public static final String ALL_TAG = "all_tag";

    private ListView mSwapListView;
    private SwapListAdapter mSwapAdapter;

    private ArrayList<StockIndex> mSelectItems;
    private ArrayList<StockIndex> mAllItems;
    private ArrayList<SelectStockInfo> mList;

    private CommonToolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_select);
        init();
        initData();
        setAdapter();
    }

    private void init() {
        mSwapListView = (ListView) findViewById(R.id.select_swap_list_view);
        mToolBar = (CommonToolbar) findViewById(R.id.select_toolbar);
        mToolBar.setToolbarTitle(getResources().getString(R.string.select_stock_back));
    }

    private void initData() {
        mSelectItems = (ArrayList<StockIndex>) getIntent().getSerializableExtra(SELECT_TAG);
        mAllItems =  (ArrayList<StockIndex>) getIntent().getSerializableExtra(ALL_TAG);
        getData();
    }

    private void getData() {
        SelectStockInfo info;
        info = new SelectStockInfo(SelectStockInfo.TITLE, null);
        mList = new ArrayList<SelectStockInfo>();
        mList.add(info);
        for (StockIndex stockIndex: mSelectItems) {
            info = new SelectStockInfo(SelectStockInfo.CURRENT_SELECT, stockIndex);
            mList.add(info);
        }
        info = new SelectStockInfo(SelectStockInfo.TITLE, null);
        mList.add(info);
        for (StockIndex stockIndex: mAllItems) {
            info = new SelectStockInfo(SelectStockInfo.OTHERS, stockIndex);
            mList.add(info);
        }
    }

    private void setAdapter() {
        mSwapAdapter = new SwapListAdapter(this, mList);
        mSwapListView.setAdapter(mSwapAdapter);
    }

    public void deleteList(int position, int type) {
        if (position < mList.size()) {
            SelectStockInfo temp = mList.get(position);
            mList.remove(position);
            if (SelectStockInfo.CURRENT_SELECT == type) {
                temp.mType = SelectStockInfo.OTHERS;
                mList.add(temp);
            } else {
                temp.mType = SelectStockInfo.CURRENT_SELECT;
                mList.add(1, temp);
            }
            saveSelectStock();
            if (mSwapAdapter != null) {
                mSwapAdapter.notifyDataSetChanged();
            }
        }
    }

    public void sortList(int position) {
        if (mList != null && position < mList.size() && position >= 2) {
            SelectStockInfo temp = mList.get(position);
            SelectStockInfo beforeTemp = mList.get(position - 1);
            mList.remove(position);
            mList.add(position, beforeTemp);
            mList.remove(position - 1);
            mList.add(position - 1, temp);
            saveSelectStock();
            if (mSwapAdapter != null) {
                mSwapAdapter.notifyDataSetChanged();
            }
        }
    }

    private void saveSelectStock() {
        StringBuffer selectBuffer = new StringBuffer();
        for (SelectStockInfo s: mList) {
            if (SelectStockInfo.CURRENT_SELECT == s.mType) {
                selectBuffer.append(s.mStockIndex.getCode()).append("_");
            }
        }
        String selectString = selectBuffer.toString();
        if (selectString.length() > 0) {
            selectString = selectString.substring(0, selectString.length() - 1);
        }
        LeoSettings.setString(PrefConst.SELECT_STOCK, selectString);
    }
}
