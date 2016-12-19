package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.utils.LeoLog;

/**
 * 股票涨跌显示Text
 * Date:20140602
 *
 * @author Deping Huang
 */
public class StockTextView extends TextView {
    private Context mContext;
    private static final int RED = 1;
    private static final int GREEN = 2;
    private double mNowPrice = 0;
    private int mRiseInfo;
    private String mText;
    private int type;
    // 涨跌的颜色
    int mColorRise;
    int mColorSlumped;
    int mColorDefault;

    public android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    restore();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public StockTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public StockTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public StockTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mColorDefault = this.getCurrentTextColor();
        mColorRise = context.getResources().getColor(R.color.stock_rise);
        mColorSlumped = context.getResources().getColor(R.color.stock_slumped);
    }

    public void restore() {
        int bgColor;
        setText(mText);
        setTextColor(mContext.getResources().getColor(R.color.white));
        if (type == RED) {
            bgColor = mContext.getResources().getColor(R.color.stock_rise);
        } else {
            bgColor = mContext.getResources().getColor(R.color.stock_slumped);
        }
        setBackgroundColor(bgColor);
    }

    public void setFlashText(String text, int type, double nowP) {

        LeoLog.e("TimeService", "text: " + text +  "----------" + "now : " + nowP + "----- save : " + mNowPrice);

        if (mRiseInfo == 0) {
            setTextColor(mContext.getResources().getColor(R.color.white));
            setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.stock_equal_text_bg));
            return;
        }

        if (mNowPrice != nowP) {
            mText = text;
            this.type = type;
            int textColor;
            int textBackGround = mContext.getResources().getColor(R.color.white);
            if (type == RED) {
                textColor = mContext.getResources().getColor(R.color.stock_rise);
            } else {
                textColor = mContext.getResources().getColor(R.color.stock_slumped);
            }
            setText(text);
            setTextColor(textColor);
            setBackgroundColor(textBackGround);

            mHandler.sendEmptyMessageDelayed(1, 200);
        }
    }

    public void setRiseInfo(int riseInfo) {
        if (riseInfo > 0) {
            this.setTextColor(mColorRise);
        } else if (riseInfo < 0) {
            this.setTextColor(mColorSlumped);
        } else {
            this.setTextColor(mColorDefault);
        }
    }

    public void saveNowPrice(double nowP) {
        mNowPrice = nowP;
    }

    public void saveRiseInfo(int riseInfo) {
        mRiseInfo = riseInfo;
    }
}
