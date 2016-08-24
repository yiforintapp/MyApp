package com.zlf.appmaster.chartview.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockKLine;
import com.zlf.appmaster.chartview.view.ChartTextLayout;
import com.zlf.appmaster.chartview.view.LongClickImageView;
import com.zlf.appmaster.chartview.view.OnTouchChartListener;

import java.util.ArrayList;
import java.util.Timer;

public class KLineChart extends FrameLayout
{

    public static final int ADJUST_TYPE_NO = 0;
    public static final int ADJUST_TYPE_FORWARD = 1;
    public static final int ADJUST_TYPE_BACKWARD = 2;
    public static final int EXTRA_TYPE_VOLUME = 0;
    public static final int EXTRA_TYPE_KDJ = 1;
    public static final int EXTRA_TYPE_MACD = 2;
    private CandleChart a;
    private a b;
    private a c;
    private a d;
    private a e;
    private View f;
    private Context g;
    private boolean h;
    private boolean i;
    private ScaleGestureDetector j;
    private int k;
    private float l;
    private ChartTextLayout m;
    private int n;
    private OnTouchChartListener o;
    private Timer p;
    private final int q = 300;
    private Point r;
    private final int s = 10;
    private m t;
    private View u;
    private View v;
    private TextView w;
    private TextView x;
    private TextView y;
    private TextView z;
    private TextView A;
    private TextView B;
    private TextView C;
    private ArrayList D;
    private ArrayList E;
    private ArrayList F;
    private int G;
    private int H;
    private int I;
    private int J;
    private View K;
    private ImageView L;
    private LongClickImageView M;
    private LongClickImageView N;
    private static int O = 0;
    private static int P = 1;

    public KLineChart(Context context, AttributeSet attributeset, int i1)
    {
        super(context, attributeset, i1);
        h = false;
        i = false;
        n = 60;
        E = null;
        F = null;
        G = 1;
        H = 2;
        J = 0;
        a(context, attributeset);
        a(context);
    }

    public KLineChart(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        h = false;
        i = false;
        n = 60;
        E = null;
        F = null;
        G = 1;
        H = 2;
        J = 0;
        a(context, attributeset);
        a(context);
    }

    public KLineChart(Context context)
    {
        super(context);
        h = false;
        i = false;
        n = 60;
        E = null;
        F = null;
        G = 1;
        H = 2;
        J = 0;
        a(context, ((AttributeSet) (null)));
        a(context);
    }

    private void a(Context context, AttributeSet attributeset)
    {
        if (attributeset == null)
        {
            return;
        } else
        {
            TypedArray typedarray = context.obtainStyledAttributes(attributeset, R.styleable.KLine);
            I = typedarray.getInt(R.styleable.KLine_KLine_mode, 0);
            return;
        }
    }

    private void a(Context context)
    {
        g = context;
        t = new m(this, this);
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        if (isInEditMode())
            return;
        View view = layoutinflater.inflate(R.layout.layout_stock_kline_chart, this, true);
        f = view.findViewById(R.id.layout_repair);
        a = (CandleChart)view.findViewById(R.id.candle_chart);
        c = (a)view.findViewById(R.id.volume_chart);
        c.setShowLeftValue(true);
        m = (ChartTextLayout)view.findViewById(R.id.chart_text_layout);
        u = view.findViewById(R.id.layout_select_extra);
        d = (a)view.findViewById(R.id.kdj_chart);
        e = (a)view.findViewById(R.id.macd_chart);
        w = (TextView)view.findViewById(R.id.tv_select_volume);
        w.setOnClickListener(new o(this));
        x = (TextView)view.findViewById(R.id.tv_select_kdj);
        x.setOnClickListener(new o(this));
        y = (TextView)view.findViewById(R.id.tv_select_macd);
        y.setOnClickListener(new o(this));
        b = e;
        b.setShowLeftValue(false);
        a.setShowLeftValue(false);
        z = (TextView)view.findViewById(R.id.tv_repair_forward);
        A = (TextView)view.findViewById(R.id.tv_repair_backward);
        B = (TextView)view.findViewById(R.id.tv_repair_no);
        z.setOnClickListener(new q(this));
        A.setOnClickListener(new q(this));
        B.setOnClickListener(new q(this));
        v = view.findViewById(R.id.progress_loading);
        e();
        if (I == 2)
        {
            c.setOnClickListener(new p(this));
            d.setOnClickListener(new p(this));
            e.setOnClickListener(new p(this));
            K = view.findViewById(R.id.kline_handle_view);
            K.setVisibility(0);
            L = (ImageView)view.findViewById(R.id.kline_move_handle);
            M = (LongClickImageView)view.findViewById(R.id.kline_move_right);
            N = (LongClickImageView)view.findViewById(R.id.kline_move_left);
            L.setOnClickListener(new l(this, null));
            M.setOnClickListener(new d(this));
            M.setLongClickRepeatListener(new e(this), 20L);
            N.setOnClickListener(new f(this));
            N.setLongClickRepeatListener(new g(this), 20L);
            a.setOnTouchListener(new j(this));
            C = (TextView)findViewById(R.id.extra_view);
            C.setVisibility(0);
            C.setOnClickListener(new h(this));
        }
        if (I == 1)
            a.setOnTouchListener(new k(this));
    }

    private void a(int i1)
    {
        if (i1 == O)
        {
            if (D.size() < n)
            {
                k = 0;
            } else
            {
                int j1 = 1;
                if (k + j1 > D.size() - n)
                    k = D.size() - n;
                else
                    k += j1;
            }
        } else
        if (i1 == P)
        {
            if (D.size() < n)
            {
                k = 0;
            } else
            {
                byte byte0 = -1;
                if (k + byte0 > 0)
                    k += byte0;
                else
                    k = 0;
            }
            a(true, true);
        }
        a(true, true);
    }

    public void setCurExtraText()
    {
        switch (H)
        {
            case 0: // '\0'
                C.setText("成交量");
                break;

            case 1: // '\001'
                C.setText("KDJ");
                break;

            case 2: // '\002'
                C.setText("MACD");
                break;
        }
    }

    public void setShowRepairView(boolean flag)
    {
        if (flag)
            f.setVisibility(0);
        else
            f.setVisibility(8);
    }

    private void c()
    {
        z.setSelected(false);
        A.setSelected(false);
        B.setSelected(false);
        v.setVisibility(8);
        switch (G)
        {
            case 1: // '\001'
                z.setSelected(true);
                break;

            case 0: // '\0'
                B.setSelected(true);
                break;

            case 2: // '\002'
                A.setSelected(true);
                break;
        }
        a(false);
    }

    private void d()
    {
        w.setSelected(false);
        x.setSelected(false);
        y.setSelected(false);
        b.setVisibility(8);
        switch (H)
        {
            case 0: // '\0'
                b = c;
                w.setSelected(true);
                break;

            case 1: // '\001'
                b = d;
                x.setSelected(true);
                break;

            case 2: // '\002'
                b = e;
                y.setSelected(true);
                break;
        }
        b.setVisibility(0);
        g();
    }

    public void setInBigPicMode()
    {
        a.j = 1;
        n = 104;
        j = new ScaleGestureDetector(g, new n(this, null));
        u.setVisibility(0);
        y.setSelected(true);
        b.setShowLeftValue(true);
        a.setShowLeftValue(true);
    }

    private void e()
    {
        a.j = I;
        if (a.j == 1)
        {
            n = 104;
            j = new ScaleGestureDetector(g, new n(this, null));
            u.setVisibility(0);
            y.setSelected(true);
            b.setShowLeftValue(true);
            a.setShowLeftValue(true);
        } else
        if (a.j == 2)
        {
            j = new ScaleGestureDetector(g, new n(this, null));
            y.setSelected(true);
        }
    }

    public void setKLineType(int i1)
    {
        a.setKLineType(i1);
    }

    public void setForwardData(ArrayList arraylist)
    {
        E = arraylist;
        t.sendEmptyMessage(1);
    }

    public void setBackwardData(ArrayList arraylist)
    {
        F = arraylist;
        t.sendEmptyMessage(1);
    }

    public void setKLineData(ArrayList arraylist)
    {
        D = arraylist;
        if (D != null && D.size() > 0)
        {
            ArrayList arraylist1 = new ArrayList();
            k = 0;
            if (arraylist.size() > n)
            {
                k = arraylist.size() - n;
                for (int i1 = 0; i1 < n; i1++)
                    arraylist1.add(arraylist.get(i1 + k));

            } else
            {
                arraylist1 = D;
            }
            a.a(arraylist1, n);
            g();
        }
    }

    private void f()
    {
        if (a.j == 1)
        {
            int i1 = (int)b.getMaxTextAreaLeft();
            if (i1 > a.a)
                a.a = i1;
            b.a(a.a, 0, 0, 0);
            m.setChartTextMode(a.j);
            m.setPicMargins(a.a, 0, 0, 0);
        } else
        if (a.j == 2)
        {
            a.a = 0;
            b.a(0, 0, 0, 0);
            m.setChartTextMode(a.j);
            m.setPicMargins(0, 0, 0, 0);
        } else
        {
            a.a = 0;
        }
    }

    private void g()
    {
        if (D == null)
        {
            return;
        } else
        {
            b.a(k, n);
            b.setSourceData(D);
            f();
            return;
        }
    }

    private void a(boolean flag)
    {
        if (k < 0)
            k = 0;
        ArrayList arraylist = D;
        if (G == 1 && E != null)
            arraylist = E;
        else
        if (G == 2 && F != null)
            arraylist = F;
        if (arraylist == null)
            return;
        int i1 = arraylist.size() - k;
        if (i1 > n)
            i1 = n;
        ArrayList arraylist1 = new ArrayList();
        for (int j1 = 0; j1 < i1; j1++)
            arraylist1.add(arraylist.get(k + j1));

        a.a(arraylist1, n);
        b.a(k, n);
        if (flag)
            b.a();
        else
            b.setSourceData(arraylist);
        f();
        invalidate();
    }

    private void a(boolean flag, boolean flag1)
    {
        if (k < 0)
            k = 0;
        ArrayList arraylist = D;
        if (G == 1 && E != null)
            arraylist = E;
        else
        if (G == 2 && F != null)
            arraylist = F;
        if (arraylist == null)
            return;
        int i1 = arraylist.size() - k;
        if (i1 > n)
            i1 = n;
        ArrayList arraylist1 = new ArrayList();
        for (int j1 = 0; j1 < i1; j1++)
            arraylist1.add(arraylist.get(k + j1));

        a.a(arraylist1, n);
        b.a(k, n);
        if (flag)
            b.a();
        else
            b.setSourceData(arraylist);
        if (I == 2 && flag1 && o != null)
            o.onMove(i1 - 1);
        f();
        invalidate();
    }

    private void a(float f1)
    {
        n *= f1;
        if (n > 300)
            n = 300;
        else
        if (n < 30)
            n = 30;
        a(true);
    }

    private void b(float f1)
    {
        int ai[] = new int[2];
        a.getLocationInWindow(ai);
        f1 -= ai[0];
        int i1 = a.a(f1);
        PointF pointf = a.a(i1);
        m.setCrossPoint(pointf.x, pointf.y);
        m.setVolumeY(b.getTop(), b.a(i1));
        m.setStockInfo(a.b(i1), b.getUnitValue());
        m.postInvalidate();
        if (o != null)
            o.onTouchDown(i1);
        a.setCurStockKLineInfo(i1);
    }

    private boolean a(MotionEvent motionevent)
    {
        switch (motionevent.getAction())
        {
            case 0: // '\0'
                r = new Point((int)motionevent.getX(), (int)motionevent.getY());
                p = new Timer();
                p.schedule(new i(this), 300L);
                return true;

            case 2: // '\002'
                Point point = new Point((int)motionevent.getX(), (int)motionevent.getY());
                int i1 = Math.abs(point.x - r.x);
                int j1 = Math.abs(point.y - r.y);
                int k1 = (int)Math.sqrt(i1 * i1 + j1 * j1);
                boolean flag = k1 >= 10;
                if (flag)
                {
                    p.cancel();
                    return false;
                } else
                {
                    return true;
                }
        }
        p.cancel();
        return false;
    }

    private void h()
    {
        if (r != null)
            b(r.x);
        m.setVisibility(0);
        h = true;
        r = null;
    }

    public void setOnTouchChartListener(OnTouchChartListener ontouchchartlistener)
    {
        o = ontouchchartlistener;
    }

    public StockKLine getItem(int i1)
    {
        return a.b(i1);
    }

    public int getAdjustType()
    {
        return G;
    }

    public StockKLine getLastItem()
    {
        return a.getLastItem();
    }

    public void setAdjustType(int i1)
    {
        G = i1;
        c();
    }

    public int getExtraType()
    {
        return H;
    }

    public void setExtraType(int i1)
    {
        H = i1;
        d();
    }

    static void a(KLineChart klinechart)
    {
        klinechart.h();
    }

    static void b(KLineChart klinechart)
    {
        klinechart.c();
    }

    static int a()
    {
        return O;
    }

    static void a(KLineChart klinechart, int i1)
    {
        klinechart.a(i1);
    }

    static int b()
    {
        return P;
    }

    static OnTouchChartListener c(KLineChart klinechart)
    {
        return klinechart.o;
    }

    static int b(KLineChart klinechart, int i1)
    {
        return klinechart.G = i1;
    }

    static ArrayList d(KLineChart klinechart)
    {
        return klinechart.E;
    }

    static View e(KLineChart klinechart)
    {
        return klinechart.v;
    }

    static ArrayList f(KLineChart klinechart)
    {
        return klinechart.F;
    }

    static int g(KLineChart klinechart)
    {
        return klinechart.H;
    }

    static int c(KLineChart klinechart, int i1)
    {
        return klinechart.H = i1;
    }

    static void h(KLineChart klinechart)
    {
        klinechart.d();
    }

    static boolean a(KLineChart klinechart, boolean flag)
    {
        return klinechart.i = flag;
    }

    static void a(KLineChart klinechart, float f1)
    {
        klinechart.a(f1);
    }

    static ArrayList i(KLineChart klinechart)
    {
        return klinechart.D;
    }

    static boolean j(KLineChart klinechart)
    {
        return klinechart.i;
    }

    static boolean k(KLineChart klinechart)
    {
        return klinechart.h;
    }

    static ChartTextLayout l(KLineChart klinechart)
    {
        return klinechart.m;
    }

    static void b(KLineChart klinechart, float f1)
    {
        klinechart.b(f1);
    }

    static boolean b(KLineChart klinechart, boolean flag)
    {
        return klinechart.h = flag;
    }

    static CandleChart m(KLineChart klinechart)
    {
        return klinechart.a;
    }

    static float c(KLineChart klinechart, float f1)
    {
        return klinechart.l = f1;
    }

    static int n(KLineChart klinechart)
    {
        return klinechart.n;
    }

    static int d(KLineChart klinechart, int i1)
    {
        return klinechart.k = i1;
    }

    static float o(KLineChart klinechart)
    {
        return klinechart.l;
    }

    static int p(KLineChart klinechart)
    {
        return klinechart.k;
    }

    static void c(KLineChart klinechart, boolean flag)
    {
        klinechart.a(flag);
    }

    static boolean a(KLineChart klinechart, MotionEvent motionevent)
    {
        return klinechart.a(motionevent);
    }

    static ScaleGestureDetector q(KLineChart klinechart)
    {
        return klinechart.j;
    }

    static m r(KLineChart klinechart)
    {
        return klinechart.t;
    }

    static ImageView s(KLineChart klinechart)
    {
        return klinechart.L;
    }

    static LongClickImageView t(KLineChart klinechart)
    {
        return klinechart.M;
    }

    static LongClickImageView u(KLineChart klinechart)
    {
        return klinechart.N;
    }

}