package com.zlf.appmaster.chartview.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockKLine;
import com.zlf.appmaster.chartview.chart.KLineChart;
import com.zlf.appmaster.chartview.chart.MinuteLine;
import com.zlf.appmaster.chartview.tool.OnGetDailyKLinesListener;
import com.zlf.appmaster.chartview.tool.OnGetHandicapListener;
import com.zlf.appmaster.chartview.tool.OnGetMinuteDataListener;
import com.zlf.appmaster.chartview.tool.StockChartTool;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.appmaster.utils.QLog;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StockChartDetailView extends LinearLayout {

    private final int a = 0;
    private final int b = 1;
    private final int c = 2;
    public static final int KLINE_TYPE_MIN = 0;
    public static final int KLINE_TYPE_DAILY = 1;
    public static final int KLINE_TYPE_WEEKLY = 2;
    public static final int KLINE_TYPE_MONTHLY = 3;
    private int d;
    private boolean e;
    private StockChartTool f;
    private CircularProgressView g;
    private View h;
    private MinuteLine i;
    private KLineChart j;
    private KLineChart k;
    private KLineChart l;
    private ArrayList m;
    private OnClickChartListener n;
    private int o[];
    private DataHandler p;
    private boolean q;
    private StockTextView r[];
    private TextView s[];
    private StockTextView t[];
    private TextView u[];
    private final int v[];
    private final int w[];
    private final int x[];
    private final int y[];

    public StockChartDetailView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        d = 0;
        q = false;
        r = new StockTextView[5];
        s = new TextView[5];
        t = new StockTextView[5];
        u = new TextView[5];
        v = (new int[]{
                R.id.stock_buy_1_price, R.id.stock_buy_2_price, R.id.stock_buy_3_price, R.id.stock_buy_4_price, R.id.stock_buy_5_price
        });
        w = (new int[]{
                R.id.stock_buy_1_count, R.id.stock_buy_2_count, R.id.stock_buy_3_count, R.id.stock_buy_4_count, R.id.stock_buy_5_count
        });
        x = (new int[]{
                R.id.stock_sell_1_price, R.id.stock_sell_2_price, R.id.stock_sell_3_price, R.id.stock_sell_4_price, R.id.stock_sell_5_price
        });
        y = (new int[]{
                R.id.stock_sell_1_count, R.id.stock_sell_2_count, R.id.stock_sell_3_count, R.id.stock_sell_4_count, R.id.stock_sell_5_count
        });
        a(context);
    }

    public StockChartDetailView(Context context) {
        super(context);
        d = 0;
        q = false;
        r = new StockTextView[5];
        s = new TextView[5];
        t = new StockTextView[5];
        u = new TextView[5];
        v = (new int[]{
                R.id.stock_buy_1_price, R.id.stock_buy_2_price, R.id.stock_buy_3_price, R.id.stock_buy_4_price, R.id.stock_buy_5_price
        });
        w = (new int[]{
                R.id.stock_buy_1_count, R.id.stock_buy_2_count, R.id.stock_buy_3_count, R.id.stock_buy_4_count, R.id.stock_buy_5_count
        });
        x = (new int[]{
                R.id.stock_sell_1_price, R.id.stock_sell_2_price, R.id.stock_sell_3_price, R.id.stock_sell_4_price, R.id.stock_sell_5_price
        });
        y = (new int[]{
                R.id.stock_sell_1_count, R.id.stock_sell_2_count, R.id.stock_sell_3_count, R.id.stock_sell_4_count, R.id.stock_sell_5_count
        });
        a(context);
    }

    public void initData(String s1, String s2, boolean flag, StockChartTool stockcharttool) {
        e = flag;
        f = stockcharttool;
        d = 0;
        a(d);
        getMinuteLine();
    }

    private void a(Context context) {
        m = new ArrayList();
        p = new DataHandler();
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        if (isInEditMode())
            return;
        View view = layoutinflater.inflate(R.layout.view_stock_detail_chart, this, true);
        j = (KLineChart) view.findViewById(R.id.kline_daily);
        j.setKLineType(1);
        j.setOnClickListener(new One());
        k = (KLineChart) view.findViewById(R.id.kline_weekly);
        k.setKLineType(2);
        k.setOnClickListener(new Two());
        l = (KLineChart) view.findViewById(R.id.kline_monthly);
        l.setKLineType(3);
        l.setOnClickListener(new Three());
        i = (MinuteLine) findViewById(R.id.mline);
        i.setOnClickListener(new Four());
        h = findViewById(R.id.handicap_layout);
        g = (CircularProgressView) findViewById(R.id.content_loading);
        o = new int[4];
        for (int i1 = 0; i1 < 5; i1++) {
            r[i1] = (StockTextView) findViewById(v[i1]);
            s[i1] = (TextView) findViewById(w[i1]);
            t[i1] = (StockTextView) findViewById(x[i1]);
            u[i1] = (TextView) findViewById(y[i1]);
        }

    }

    public void setOnClickChartListener(OnClickChartListener onclickchartlistener) {
        n = onclickchartlistener;
    }

    public void refreshData() {
        getMinuteLine();
        getKLine();
    }

    public void clearMinuteData(float f1) {
        i.clearData(f1);
        for (int i1 = 0; i1 < 5; i1++) {
            r[i1].setText("--");
            s[i1].setText("--");
            t[i1].setText("--");
            u[i1].setText("--");
        }

    }

    private void a(int i1) {
        switch (i1) {
            default:
                break;

            case 0: // '\0'
                if (e)
                    h.setVisibility(0);
                else
                    h.setVisibility(8);
                i.setVisibility(0);
                break;

            case 1: // '\001'
                j.setVisibility(0);
                break;

            case 2: // '\002'
                k.setVisibility(0);
                break;

            case 3: // '\003'
                l.setVisibility(0);
                break;
        }
    }

    private void getMinuteLine() {
        g.setVisibility(0);
        if (f == null)
            return;
        f.getMinuteData(new Five());
        if (e)
            f.getHandicapData(new Six());
    }

    private void setHandicapFromStockInfo(StockTradeInfo stocktradeinfo) {
        StockTradeInfo.BuyOrSellInfo abuyorsellinfo[] = stocktradeinfo.getBuyInfo();
        StockTradeInfo.BuyOrSellInfo abuyorsellinfo1[] = stocktradeinfo.getSellInfo();
        if (abuyorsellinfo == null || abuyorsellinfo1 == null)
            return;
        try {
            int i1 = 0;
            do {
                if (i1 >= 5)
                    break;
                if (r[i1] == null) {
                    QLog.e("StockChartDetailView", "mBuyInfoPriceTV[i] == null");
                    break;
                }
                if (abuyorsellinfo[i1] == null) {
                    QLog.e("StockChartDetailView", "buyInfo[i] == null");
                    break;
                }
                r[i1].setRiseInfo(abuyorsellinfo[i1].getRiseInfo());
                r[i1].setText(abuyorsellinfo[i1].getPriceFormat());
                s[i1].setText(abuyorsellinfo[i1].getCountFormat());
                t[i1].setRiseInfo(abuyorsellinfo1[i1].getRiseInfo());
                t[i1].setText(abuyorsellinfo1[i1].getPriceFormat());
                u[i1].setText(abuyorsellinfo1[i1].getCountFormat());
                i1++;
            } while (true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void getKLine() {
        if (d != 0) {
            if (o[1] == 2) {
                g.setVisibility(8);
                return;
            }
            if (o[1] == 1) {
                g.setVisibility(0);
                return;
            }
            if (o[1] == 0)
                g.setVisibility(0);
        }
        getBaseKLineData();
    }

    private void getBaseKLineData() {
        o[1] = 1;
        if (q) {
            QLog.e("StockChartDetailView", "上一次请求还未处理完");
            return;
        }
        if (f == null) {
            return;
        } else {
            q = true;
            f.getDailyKLines(new Seven());
            return;
        }
    }

    public void changeData() {
        j.setKLineData(m);
        k.setKLineData(StockKLine.changeDaily2Week(m));
        l.setKLineData(StockKLine.changeDaily2Month(m));
        StockKLine.calcAdjust(m);
        ArrayList arraylist = StockKLine.getForwardList(m);
        j.setForwardData(arraylist);
        k.setForwardData(StockKLine.changeDaily2Week(arraylist));
        l.setForwardData(StockKLine.changeDaily2Month(arraylist));
        j.setAdjustType(1);
        k.setAdjustType(1);
        l.setAdjustType(1);
    }

    static OnClickChartListener a(StockChartDetailView stockchartdetailview) {
        return stockchartdetailview.n;
    }

    static int b(StockChartDetailView stockchartdetailview) {
        return stockchartdetailview.d;
    }

    static CircularProgressView c(StockChartDetailView stockchartdetailview) {
        return stockchartdetailview.g;
    }

    static MinuteLine d(StockChartDetailView stockchartdetailview) {
        return stockchartdetailview.i;
    }

    static void a(StockChartDetailView stockchartdetailview, StockTradeInfo stocktradeinfo) {
        stockchartdetailview.setHandicapFromStockInfo(stocktradeinfo);
    }

    static int[] e(StockChartDetailView stockchartdetailview) {
        return stockchartdetailview.o;
    }

    static DataHandler f(StockChartDetailView stockchartdetailview) {
        return stockchartdetailview.p;
    }

    static ArrayList a(StockChartDetailView stockchartdetailview, ArrayList arraylist) {
        return stockchartdetailview.m = arraylist;
    }

    static boolean a(StockChartDetailView stockchartdetailview, boolean flag) {
        return stockchartdetailview.q = flag;
    }

    private class DataHandler extends Handler {

        WeakReference a;

        public void handleMessage(Message message) {
            super.handleMessage(message);
            StockChartDetailView stockchartdetailview = (StockChartDetailView) a.get();
            if (message.what == 1)
                StockChartDetailView.c(stockchartdetailview).setVisibility(8);
            else if (message.what == 0) {
                if (stockchartdetailview == null)
                    return;
                if (StockChartDetailView.b(stockchartdetailview) != 0)
                    StockChartDetailView.c(stockchartdetailview).setVisibility(8);
                stockchartdetailview.changeData();
            }
            StockChartDetailView.a(stockchartdetailview, false);
        }

        public DataHandler() {
            a = new WeakReference(StockChartDetailView.this);
        }
    }


    private class One
            implements android.view.View.OnClickListener

    {

        final StockChartDetailView a;

        public void onClick(View view) {
            if (StockChartDetailView.a(a) != null)
                StockChartDetailView.a(a).onClick(1);
        }

        One()

        {
            super();
            a = StockChartDetailView.this;

        }
    }


    private class Two
            implements android.view.View.OnClickListener {

        final StockChartDetailView a;

        public void onClick(View view) {
            if (StockChartDetailView.a(a) != null)
                StockChartDetailView.a(a).onClick(2);
        }

        Two() {
            super();
            a = StockChartDetailView.this;
        }
    }


    private class Three
            implements android.view.View.OnClickListener {

        final StockChartDetailView a;

        public void onClick(View view) {
            if (StockChartDetailView.a(a) != null)
                StockChartDetailView.a(a).onClick(3);
        }

        Three() {
            super();
            a = StockChartDetailView.this;
        }
    }


    private class Four
            implements android.view.View.OnClickListener {

        final StockChartDetailView a;

        public void onClick(View view) {
            if (StockChartDetailView.a(a) != null)
                StockChartDetailView.a(a).onClick(0);
        }

        Four() {
            super();
            a = StockChartDetailView.this;
        }
    }


    private class Five
            implements OnGetMinuteDataListener {

        final StockChartDetailView a;

        public void onError() {
            if (StockChartDetailView.b(a) == 0)
                StockChartDetailView.c(a).setVisibility(8);
        }

        public void onDataFinish(ArrayList arraylist) {
            StockChartDetailView.d(a).setMinuteData(arraylist);
            if (StockChartDetailView.b(a) == 0)
                StockChartDetailView.c(a).setVisibility(8);
        }

        Five() {
            super();
            a = StockChartDetailView.this;
        }
    }


    private class Six
            implements OnGetHandicapListener {

        final StockChartDetailView a;

        public void onDataFinish(JSONObject jsonobject) {
            StockChartDetailView.a(a, StockTradeInfo.resloveHandicapData(jsonobject));
        }

        public void onError() {
        }

        Six() {
            super();
            a = StockChartDetailView.this;
        }
    }


    private class Seven
            implements OnGetDailyKLinesListener {

        final StockChartDetailView a;

        public void onError() {
            if (StockChartDetailView.e(a)[1] == 1)
                StockChartDetailView.e(a)[1] = 0;
            if (StockChartDetailView.b(a) != 0)
                StockChartDetailView.f(a).sendEmptyMessage(1);
        }

        public void onDataFinish(ArrayList arraylist) {
            if (arraylist == null || arraylist.size() == 0) {
                return;
            } else {
                StockChartDetailView.a(a, arraylist);
                StockChartDetailView.f(a).sendEmptyMessage(0);
                StockChartDetailView.e(a)[1] = 2;
                return;
            }
        }

        Seven() {
            super();
            a = StockChartDetailView.this;
        }
    }

}