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

public class StockChartView extends LinearLayout {

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
    private final int o[];
    private View p[];
    private OnKLineRadioChangeListener q;
    private int r[];
    private DataHandler s;
    private boolean t;
    private StockTextView u[];
    private TextView v[];
    private StockTextView w[];
    private TextView x[];
    private final int y[];
    private final int z[];
    private final int A[];
    private final int B[];

    public StockChartView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        d = 0;
        o = (new int[]{
                R.id.min_line, R.id.daily_line, R.id.weekly_line, R.id.monthly_line
        });
        p = new View[4];
        q = new OnKLineRadioChangeListener();
        t = false;
        u = new StockTextView[5];
        v = new TextView[5];
        w = new StockTextView[5];
        x = new TextView[5];
        y = (new int[]{
                R.id.stock_buy_1_price, R.id.stock_buy_2_price, R.id.stock_buy_3_price, R.id.stock_buy_4_price, R.id.stock_buy_5_price
        });
        z = (new int[]{
                R.id.stock_buy_1_count, R.id.stock_buy_2_count, R.id.stock_buy_3_count, R.id.stock_buy_4_count, R.id.stock_buy_5_count
        });
        A = (new int[]{
                R.id.stock_sell_1_price, R.id.stock_sell_2_price, R.id.stock_sell_3_price, R.id.stock_sell_4_price, R.id.stock_sell_5_price
        });
        B = (new int[]{
                R.id.stock_sell_1_count, R.id.stock_sell_2_count, R.id.stock_sell_3_count, R.id.stock_sell_4_count, R.id.stock_sell_5_count
        });
        a(context);
    }

    public StockChartView(Context context) {
        super(context);
        d = 0;
        o = (new int[]{
                R.id.min_line, R.id.daily_line, R.id.weekly_line, R.id.monthly_line
        });
        p = new View[4];
        q = new OnKLineRadioChangeListener();
        t = false;
        u = new StockTextView[5];
        v = new TextView[5];
        w = new StockTextView[5];
        x = new TextView[5];
        y = (new int[]{
                R.id.stock_buy_1_price, R.id.stock_buy_2_price, R.id.stock_buy_3_price, R.id.stock_buy_4_price, R.id.stock_buy_5_price
        });
        z = (new int[]{
                R.id.stock_buy_1_count, R.id.stock_buy_2_count, R.id.stock_buy_3_count, R.id.stock_buy_4_count, R.id.stock_buy_5_count
        });
        A = (new int[]{
                R.id.stock_sell_1_price, R.id.stock_sell_2_price, R.id.stock_sell_3_price, R.id.stock_sell_4_price, R.id.stock_sell_5_price
        });
        B = (new int[]{
                R.id.stock_sell_1_count, R.id.stock_sell_2_count, R.id.stock_sell_3_count, R.id.stock_sell_4_count, R.id.stock_sell_5_count
        });
        a(context);
    }

    public void initData(String s1, String s2, boolean flag, StockChartTool stockcharttool) {
        e = flag;
        f = stockcharttool;
        d = 0;
        b(d);
        getMinuteLine();
    }

    private void a(Context context) {
        m = new ArrayList();
        s = new DataHandler();
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        if (isInEditMode())
            return;
        View view = layoutinflater.inflate(R.layout.view_stockchart, this, true);
        for (int i1 = 0; i1 < 4; i1++) {
            p[i1] = findViewById(o[i1]);
            p[i1].setOnClickListener(q);
        }

        p[0].setSelected(true);
        p[0].setEnabled(false);
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
        r = new int[4];
        for (int j1 = 0; j1 < 5; j1++) {
            u[j1] = (StockTextView) findViewById(y[j1]);
            v[j1] = (TextView) findViewById(z[j1]);
            w[j1] = (StockTextView) findViewById(A[j1]);
            x[j1] = (TextView) findViewById(B[j1]);
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
            u[i1].setText("--");
            v[i1].setText("--");
            w[i1].setText("--");
            x[i1].setText("--");
        }

    }

    private void a(int i1) {
        switch (i1) {
            case 0: // '\0'
                h.setVisibility(8);
                i.setVisibility(8);
                break;

            case 1: // '\001'
                j.setVisibility(8);
                break;

            case 2: // '\002'
                k.setVisibility(8);
                break;

            case 3: // '\003'
                l.setVisibility(8);
                break;
        }
    }

    private void b(int i1) {
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
                if (u[i1] == null) {
                    QLog.e("StockChartView", "mBuyInfoPriceTV[i] == null");
                    break;
                }
                if (abuyorsellinfo[i1] == null) {
                    QLog.e("StockChartView", "buyInfo[i] == null");
                    break;
                }
                u[i1].setRiseInfo(abuyorsellinfo[i1].getRiseInfo());
                u[i1].setText(abuyorsellinfo[i1].getPriceFormat());
                v[i1].setText(abuyorsellinfo[i1].getCountFormat());
                w[i1].setRiseInfo(abuyorsellinfo1[i1].getRiseInfo());
                w[i1].setText(abuyorsellinfo1[i1].getPriceFormat());
                x[i1].setText(abuyorsellinfo1[i1].getCountFormat());
                i1++;
            } while (true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void getKLine() {
        if (d != 0) {
            if (r[1] == 2) {
                g.setVisibility(8);
                return;
            }
            if (r[1] == 1) {
                g.setVisibility(0);
                return;
            }
            if (r[1] == 0)
                g.setVisibility(0);
        }
        getBaseKLineData();
    }

    private void getBaseKLineData() {
        r[1] = 1;
        if (t) {
            QLog.e("StockChartView", "上一次请求还未处理完");
            return;
        }
        if (f == null) {
            return;
        } else {
            t = true;
            f.getDailyKLines(new Seven());
            return;
        }
    }

    private void addTag( ArrayList arraylist) {
        for (StockKLine stockKLine: (ArrayList<StockKLine>)arraylist) {
            stockKLine.setClose(stockKLine.getClose() * 100.00f);
            stockKLine.setOpen(stockKLine.getOpen() * 100.00f);
            stockKLine.setHigh(stockKLine.getHigh() * 100.00f);
            stockKLine.setLow(stockKLine.getLow() * 100.00f);
            stockKLine.setPreClose(stockKLine.getPreClose() * 100.00f);
        }
    }

    public void changeData() {
//        addTag(m);
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

    static OnClickChartListener a(StockChartView stockchartview) {
        return stockchartview.n;
    }

    static View[] b(StockChartView stockchartview) {
        return stockchartview.p;
    }

    static int c(StockChartView stockchartview) {
        return stockchartview.d;
    }

    static void a(StockChartView stockchartview, int i1) {
        stockchartview.a(i1);
    }

    static int b(StockChartView stockchartview, int i1) {
        return stockchartview.d = i1;
    }

    static void d(StockChartView stockchartview) {
        stockchartview.getMinuteLine();
    }

    static void e(StockChartView stockchartview) {
        stockchartview.getKLine();
    }

    static void c(StockChartView stockchartview, int i1) {
        stockchartview.b(i1);
    }

    static CircularProgressView f(StockChartView stockchartview) {
        return stockchartview.g;
    }

    static MinuteLine g(StockChartView stockchartview) {
        return stockchartview.i;
    }

    static void a(StockChartView stockchartview, StockTradeInfo stocktradeinfo) {
        stockchartview.setHandicapFromStockInfo(stocktradeinfo);
    }

    static int[] h(StockChartView stockchartview) {
        return stockchartview.r;
    }

    static DataHandler i(StockChartView stockchartview) {
        return stockchartview.s;
    }

    static ArrayList a(StockChartView stockchartview, ArrayList arraylist) {
        return stockchartview.m = arraylist;
    }

    static boolean a(StockChartView stockchartview, boolean flag) {
        return stockchartview.t = flag;
    }

    private class OnKLineRadioChangeListener
            implements android.view.View.OnClickListener {

        StockChartView a;

        public void onClick(View view) {
            for (int i1 = 0; i1 < StockChartView.b(a).length; i1++) {
                StockChartView.b(a)[i1].setSelected(false);
                StockChartView.b(a)[i1].setEnabled(true);
            }

            view.setSelected(true);
            view.setEnabled(false);
            StockChartView.a(a, StockChartView.c(a));
            int j1 = view.getId();
            if (j1 == R.id.min_line) {
                StockChartView.b(a, 0);
                StockChartView.d(a);
            } else {
                if (j1 == R.id.daily_line)
                    StockChartView.b(a, 1);
                else if (j1 == R.id.weekly_line)
                    StockChartView.b(a, 2);
                else if (j1 == R.id.monthly_line)
                    StockChartView.b(a, 3);
                StockChartView.e(a);
            }
            StockChartView.c(a, StockChartView.c(a));
        }

        OnKLineRadioChangeListener() {
            super();
            a = StockChartView.this;

        }

//        OnKLineRadioChangeListener(1 1) {
//            this();
//        }
    }


    private class DataHandler extends Handler {

        WeakReference a;

        public void handleMessage(Message message) {
            super.handleMessage(message);
            StockChartView stockchartview = (StockChartView) a.get();
            if (message.what == 1)
                StockChartView.f(stockchartview).setVisibility(8);
            else if (message.what == 0) {
                if (stockchartview == null)
                    return;
                if (StockChartView.c(stockchartview) != 0)
                    StockChartView.f(stockchartview).setVisibility(8);
                stockchartview.changeData();
            }
            StockChartView.a(stockchartview, false);
        }

        public DataHandler() {
            a = new WeakReference(StockChartView.this);
        }
    }


    private class One implements android.view.View.OnClickListener

    {

        final StockChartView a;

        public void onClick(View view) {
            if (StockChartView.a(a) != null)
                StockChartView.a(a).onClick(1);
        }

        One()

        {
            super();
            a = StockChartView.this;

        }
    }


    private class Two implements android.view.View.OnClickListener {

        final StockChartView a;

        public void onClick(View view) {
            if (StockChartView.a(a) != null)
                StockChartView.a(a).onClick(2);
        }

        Two() {
            super();
            a = StockChartView.this;

        }
    }


    private class Three implements android.view.View.OnClickListener {

        final StockChartView a;

        public void onClick(View view) {
            if (StockChartView.a(a) != null)
                StockChartView.a(a).onClick(3);
        }

        Three() {
            super();
            a = StockChartView.this;
        }
    }


    private class Four implements android.view.View.OnClickListener {

        final StockChartView a;

        public void onClick(View view) {
            if (StockChartView.a(a) != null)
                StockChartView.a(a).onClick(0);
        }

        Four() {
            super();
            a = StockChartView.this;
        }
    }


    private class Five implements OnGetMinuteDataListener {

        final StockChartView a;

        public void onError() {
            if (StockChartView.c(a) == 0)
                StockChartView.f(a).setVisibility(8);
        }

        public void onDataFinish(ArrayList arraylist) {
            StockChartView.g(a).setMinuteData(arraylist);
            if (StockChartView.c(a) == 0)
                StockChartView.f(a).setVisibility(8);
        }

        Five()

        {
            super();
            a = StockChartView.this;

        }
    }


    private class Six implements OnGetHandicapListener {

        final StockChartView a;

        public void onDataFinish(JSONObject jsonobject) {
            StockChartView.a(a, StockTradeInfo.resloveHandicapData(jsonobject));
        }

        public void onError() {
        }

        Six() {
            super();
            a = StockChartView.this;
        }
    }


    private class Seven implements OnGetDailyKLinesListener {

        final StockChartView a;

        public void onError() {
            if (StockChartView.h(a)[1] == 1)
                StockChartView.h(a)[1] = 0;
            if (StockChartView.c(a) != 0)
                StockChartView.i(a).sendEmptyMessage(1);
        }

        public void onDataFinish(ArrayList arraylist) {
            if (arraylist == null || arraylist.size() == 0) {
                return;
            } else {
                StockChartView.a(a, arraylist);
                StockChartView.i(a).sendEmptyMessage(0);
                StockChartView.h(a)[1] = 2;
                return;
            }
        }

        Seven() {
            super();
            a = StockChartView.this;
        }
    }

}