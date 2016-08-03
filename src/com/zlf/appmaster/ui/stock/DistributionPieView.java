package com.zlf.appmaster.ui.stock;

/**
 * 分布饼图饼图
 * author:Deping Huang
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.zlf.appmaster.R;
import com.zlf.appmaster.utils.DipPixelUtil;

import java.util.ArrayList;
import java.util.List;

public class DistributionPieView extends View {
	private final static String TAG = DistributionPieView.class.getSimpleName();

    private boolean mbInitMeasure = true;

	private int mScrHeight;
	private int mScrWidth;

	private Paint mBGPaint;
	private Paint mAttrPaint;
	private Paint mPaintText;

	// 百分比提示区域矩形
	private int PERCENT_PROMPT_START_SPAN;		// 起始点与饼图边缘的距离

    // 提示线
    private int PROMPT_LINE_START_POINT_WIDTH;    // 点宽
    private int PROMPT_LINE_WIDTH;          // 线宽
    private int PROMPT_LINE_TXT_SPAN;       // 与文字的间隙
	// 字号
	private int FONT_PERCENT;			// 比例
	private int FONT_NAME;				// 名称

	private FanShapedCalc mFanShapedCalc = new FanShapedCalc();
	// 饼图底色
	private final int mBGColor = 0xffccd7e4;
	// 饼图提示的背景
	private final int mBGPromptColor = 0xffffffff;

	protected boolean mbShowPrompt = true;
	protected List<PercentPrompt> mPercentPrompt = new ArrayList<PercentPrompt>();

	private String mPromptTag = "";	// 提示标记



	public DistributionPieView(Context context){
		super(context);
		initView(context);
	}

	public DistributionPieView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public DistributionPieView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	 @SuppressLint("NewApi")
	 private void initView(Context context) {
		
		if(VERSION.SDK_INT >=14){
			this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		
		PERCENT_PROMPT_START_SPAN = DipPixelUtil.dip2px(context, 15);
        PROMPT_LINE_TXT_SPAN = DipPixelUtil.dip2px(context, 2);
        PROMPT_LINE_START_POINT_WIDTH = DipPixelUtil.dip2px(context, 2);
        PROMPT_LINE_WIDTH = DipPixelUtil.dip2px(context, 70);
		
		FONT_PERCENT = DipPixelUtil.dip2px(context, 12);
		FONT_NAME = DipPixelUtil.dip2px(context, 15.3f);

		//// 屏幕信息
		//DisplayMetrics dm = getResources().getDisplayMetrics();
		//mScrHeight = dm.heightPixels;
		//mScrWidth = dm.widthPixels;	
		
		// 绘制前获取XML中定义的宽高信息
		this.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                
                @Override
                public boolean onPreDraw() {
                        // TODO Auto-generated method stub
                    //mScrWidth = getMeasuredWidth();
                    //mScrHeight = (int)((float)mScrWidth*0.74f); // view高度根据手机的宽度来计算

                	mScrHeight = getMeasuredHeight();
                    mScrWidth = (int)((float)mScrHeight * 1.35f); // 画的宽度根据高度来计算

            		//QLog.i(TAG, "mScrHeight:" + mScrHeight + "    mScrWidth:" + mScrWidth);
                    return true;
                }
        });


		// 设置边缘特殊效果
		BlurMaskFilter PaintBGBlur = new BlurMaskFilter(1,
				BlurMaskFilter.Blur.INNER);

		mAttrPaint = new Paint();
		mAttrPaint.setStyle(Paint.Style.FILL);
		mAttrPaint.setStrokeWidth(4);
		mAttrPaint.setMaskFilter(PaintBGBlur);
		mAttrPaint.setAntiAlias(true); // 抗锯
		
		mBGPaint = new Paint();
		mBGPaint.setColor(mBGColor);
		mBGPaint.setStyle(Paint.Style.FILL);
		mBGPaint.setStrokeWidth(4);
		mBGPaint.setMaskFilter(PaintBGBlur);
		mBGPaint.setAntiAlias(true);
		

		mPaintText = new Paint();
		mPaintText.setAntiAlias(true);
		mPaintText.setTextAlign(Paint.Align.LEFT);
		// PaintText.setTypeface(Typeface.DEFAULT_BOLD);

	}
	
	public void onDraw(Canvas canvas){
//        QLog.i(TAG, "getLayoutParams().height:"+getLayoutParams().height);
//        if (mbInitMeasure){
//            QLog.i(TAG, "mScrHeight:"+mScrHeight);
//            mbInitMeasure = false;
//            getLayoutParams().height = mScrHeight;
//            requestLayout();
//        }


		//画布背景
		canvas.drawColor(Color.WHITE);
		
		float cirX = getMeasuredWidth() / 2;
		float cirY = getMeasuredHeight() / 2 ;
		float radius = mScrWidth / 4 ;
        float radiusInner = radius * 0.8f;
		
		float arcLeft = cirX - radius;
		float arcTop  = cirY - radius;
		float arcRight = cirX + radius;
		float arcBottom = cirY + radius;
		RectF arcRF0 = new RectF(arcLeft ,arcTop,arcRight,arcBottom);
		
		mBGPaint.setColor(mBGColor);
		canvas.drawCircle(cirX, cirY, radius, mBGPaint);
	 
		float CurrPer = 0f; //偏移角度
		float Percentage =  0f; //当前所占比例
								
									
		int dataLen = mPercentPrompt.size();
		for(int i = 0; i < dataLen; i++)
		{
			//将百分比转换为饼图显示角度
			Percentage = 360 * mPercentPrompt.get(i).getPercent();
			Percentage = (float)(Math.round(Percentage *100))/100;
			 
			// --------------------------- 在饼图中所占比例(扇形区域)--------------------------------------//
			mAttrPaint.setColor(mPercentPrompt.get(i).getBGColor());
			canvas.drawArc(arcRF0, CurrPer, Percentage, true, mAttrPaint); 
			
			// --------------------------- 文字提示区 --------------------------------------//

            if (mbShowPrompt){
                // 计算线的起始点
                mFanShapedCalc.CalcArcEndPointXY(cirX, cirY, radius + PERCENT_PROMPT_START_SPAN, CurrPer +  Percentage/2);
                float startX = mFanShapedCalc.getPosX();
                float startY = mFanShapedCalc.getPosY();
                canvas.drawCircle(startX,startY, PROMPT_LINE_START_POINT_WIDTH, mAttrPaint);

                mPaintText.setColor(mPercentPrompt.get(i).getBGColor());
                mPaintText.setTextSize(FONT_PERCENT);
                FontMetricsInt fontMetrics = mPaintText.getFontMetricsInt();
                float baseline = startY - fontMetrics.bottom - PROMPT_LINE_TXT_SPAN;

                // 计算是在左边还是右边
                if (startX+1 > cirX){
                    mPaintText.setTextAlign(Paint.Align.LEFT);
                    canvas.drawLine(startX, startY, startX + PROMPT_LINE_WIDTH, startY, mAttrPaint);
                    canvas.drawText(mPercentPrompt.get(i).getPrompt(), startX + PROMPT_LINE_TXT_SPAN, baseline, mPaintText);
                }
                else {
                    mPaintText.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawLine(startX, startY, startX - PROMPT_LINE_WIDTH, startY, mAttrPaint);
                    canvas.drawText(mPercentPrompt.get(i).getPrompt(), startX - PROMPT_LINE_TXT_SPAN, baseline, mPaintText);
                }
            }

			//下次的起始角度
			CurrPer += Percentage;
		}
		
		mBGPaint.setColor(mBGPromptColor);
		canvas.drawCircle(cirX, cirY, radiusInner, mBGPaint);

		if (dataLen != 0){
			mPaintText.setColor(Color.BLACK);
		}
		else {
			mPaintText.setColor(mBGColor);
		}

		mPaintText.setTextSize(FONT_NAME);
        mPaintText.setTextAlign(Paint.Align.CENTER);
		FontMetricsInt fontMetrics = mPaintText.getFontMetricsInt();
		float baseline = cirY;
		canvas.drawText(mPromptTag+"分布", cirX, baseline, mPaintText);
        baseline = cirY - fontMetrics.top;
		if (dataLen != 0){
			mPaintText.setColor(getResources().getColor(R.color.main_red));
			canvas.drawText("详情>", cirX, baseline, mPaintText);
		}

	}

	public String getPromptTag() {
		return mPromptTag;
	}

	public void setPromptTag(String promptTag) {
		this.mPromptTag = promptTag;
	}


	protected class PercentPrompt{
		private String mPrompt;
		private float mPercent;
		private int mBGColor;
		
		public PercentPrompt(int bgColor, float percent, String prompt){
			mBGColor = bgColor;
			mPercent = percent;
			mPrompt = prompt;
		}
		
		public int getBGColor(){
			return mBGColor;
		}
		public String getPrompt(){
			return mPrompt;
		}
		public float getPercent(){
			return mPercent;
		}
	}
	

	/**
	 * 	  
	 *  计算扇形的边缘中点
	 */
	private class FanShapedCalc {
		

		//Position位置
		private float posX = 0.0f;
		private float posY = 0.0f;			
		
		//依圆心坐标，半径，扇形角度，计算出扇形终射线与圆弧交叉点的xy坐标
		public void CalcArcEndPointXY(float cirX, float cirY, float radius, float cirAngle){

			//将角度转换为弧度		
	        float arcAngle = (float) (Math.PI * cirAngle / 180.0);
	        if (cirAngle < 90)
	        {
	            posX = cirX + (float)(Math.cos(arcAngle)) * radius;
	            posY = cirY + (float)(Math.sin(arcAngle)) * radius;
	        }
	        else if (cirAngle == 90)
	        {
	            posX = cirX;
	            posY = cirY + radius;
	        }
	        else if (cirAngle > 90 && cirAngle < 180)
	        {
	        	arcAngle = (float) (Math.PI * (180 - cirAngle) / 180.0);
	            posX = cirX - (float)(Math.cos(arcAngle)) * radius;
	            posY = cirY + (float)(Math.sin(arcAngle)) * radius;
	        }
	        else if (cirAngle == 180)
	        {
	            posX = cirX - radius;
	            posY = cirY;
	        }
	        else if (cirAngle > 180 && cirAngle < 270)
	        {
	        	arcAngle = (float) (Math.PI * (cirAngle - 180) / 180.0);
	            posX = cirX - (float)(Math.cos(arcAngle)) * radius;
	            posY = cirY - (float)(Math.sin(arcAngle)) * radius;
	        }
	        else if (cirAngle == 270)
	        {
	            posX = cirX;
	            posY = cirY - radius;
	        }
	        else
	        {
	        	arcAngle = (float) (Math.PI * (360 - cirAngle) / 180.0);
	            posX = cirX + (float)(Math.cos(arcAngle)) * radius;
	            posY = cirY - (float)(Math.sin(arcAngle)) * radius;
	        }
					
		}


		public float getPosX() {
			return posX;
		}


		public float getPosY() {
			return posY;
		}
		
	}


	
	

}