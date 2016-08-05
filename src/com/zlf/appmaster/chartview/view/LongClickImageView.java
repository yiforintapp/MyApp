// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 

package com.zlf.appmaster.chartview.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class LongClickImageView extends ImageView
        implements View.OnLongClickListener {

    private LongClickRepeatListener a;
    private long b;
    private MyHandler c;

    public LongClickImageView(Context context) {
        super(context);
        a();
    }

    public LongClickImageView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        a();
    }

    public LongClickImageView(Context context, AttributeSet attributeset, int i) {
        super(context, attributeset, i);
        a();
    }

    private void a() {
        c = new MyHandler();
        setOnLongClickListener(new One());
    }

    public boolean onLongClick(View view) {
        return false;
    }

    public void setLongClickRepeatListener(LongClickRepeatListener longclickrepeatlistener, long l) {
        a = longclickrepeatlistener;
        b = l;
    }

    public void setLongClickRepeatListener(LongClickRepeatListener longclickrepeatlistener) {
        setLongClickRepeatListener(longclickrepeatlistener, 100L);
    }

    static MyHandler a(LongClickImageView longclickimageview) {
        return longclickimageview.c;
    }

    static long b(LongClickImageView longclickimageview) {
        return longclickimageview.b;
    }

    static LongClickRepeatListener c(LongClickImageView longclickimageview) {
        return longclickimageview.a;
    }

    private class MyHandler extends Handler {

        private WeakReference a;

        public void handleMessage(Message message) {
            super.handleMessage(message);
            LongClickImageView longclickimageview = (LongClickImageView) a.get();
            if (longclickimageview != null && LongClickImageView.c(longclickimageview) != null)
                LongClickImageView.c(longclickimageview).repeatAction();
        }

        MyHandler() {
            a = new WeakReference(LongClickImageView.this);
        }
    }

    public interface LongClickRepeatListener {

        public abstract void repeatAction();
    }


    private class One implements OnLongClickListener

    {

        final LongClickImageView a;

        public boolean onLongClick(View view) {
            (new Thread(new LongClickThread())).start();
            return true;
        }

        One()

        {
            super();
            a = LongClickImageView.this;
        }

        private class LongClickThread implements Runnable {

            private int b;
            LongClickImageView a;

            public void run() {
                for (; a.isPressed(); SystemClock.sleep(LongClickImageView.b(a) / 5L)) {
                    b++;
                    if (b % 5 == 0)
                        LongClickImageView.a(a).sendEmptyMessage(1);
                }

            }

            public LongClickThread() {
                super();
                a = LongClickImageView.this;
            }

        }

    }

}
