package com.zlf.appmaster.model;

import com.zlf.appmaster.model.stock.StockIndex;

/**
 * Created by Administrator on 2016/10/18.
 */
public class SelectStockInfo extends BaseInfo {
    public int mType;
    public StockIndex mStockIndex;

    public static int CURRENT_SELECT = 0;
    public static int OTHERS = 1;
    public static int TITLE = 2;

    //构造函数
    public SelectStockInfo(int type, StockIndex stockIndex) {
        this.mType = type;
        this.mStockIndex = stockIndex;
    }
}
