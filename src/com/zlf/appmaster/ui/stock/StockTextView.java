package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zlf.appmaster.R;

/**
 * 股票涨跌显示Text
 * Date:20140602
 * @author Deping Huang
 *
 */
public class StockTextView extends TextView {
	// 涨跌的颜色
	int mColorRise;
	int mColorSlumped;
	int mColorDefault;

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

	private void init(Context context){
		mColorDefault = this.getCurrentTextColor();
		mColorRise = context.getResources().getColor(R.color.stock_rise);
		mColorSlumped = context.getResources().getColor(R.color.stock_slumped);
	}
	
	public void setRiseInfo(int riseInfo){
		if(riseInfo > 0){
			this.setTextColor(mColorRise);
		}
		else if(riseInfo < 0){
			this.setTextColor(mColorSlumped);
		}
		else
			this.setTextColor(mColorDefault);
	}

}
