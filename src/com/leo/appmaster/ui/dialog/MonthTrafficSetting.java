
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
import com.leo.appmaster.utils.DipPixelUtil;

public class MonthTrafficSetting extends LEOBaseDialog {
    private Context mContext;
    private TextView seekbar_text, sure_button,seekbar_text_progress;
    private SeekBar mSeekBar;
    private AppMasterPreference sp_notice_flow;
    private int progressInt;
    private int progressTextWidth,progressTextHeight;
    
    private OnDiaogClickListener mListener;

    public interface OnDiaogClickListener {
        public void onClick(int progress);
    }

    public MonthTrafficSetting(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        sp_notice_flow = AppMasterPreference.getInstance(mContext);
        initUI();
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_flow_setting, null);
        Resources resources = AppMasterApplication.getInstance().getResources();
        seekbar_text = (TextView) dlgView.findViewById(R.id.seekbar_text);
        seekbar_text.setText(resources.getString(R.string.flow_settting_dialog_remain));
        
        seekbar_text_progress = (TextView) dlgView.findViewById(R.id.seekbar_text_progress);
        progressInt = sp_notice_flow.getFlowSettingBar();
        seekbar_text_progress.setText(progressInt+"%");
        //得到seekbar_text_progress 大小，初始化位置
        ViewTreeObserver vto =  seekbar_text_progress.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                seekbar_text_progress.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                progressTextWidth = seekbar_text_progress.getWidth();
                progressTextHeight = seekbar_text_progress.getHeight();
                
                /**init the position of seekbar_text_progress **/
                resetSeekbarTextMargin(mSeekBar.getThumb().getBounds().centerX());
            }
        });
        
        sure_button = (TextView) dlgView.findViewById(R.id.sure_button);
        
        mSeekBar = (SeekBar) dlgView.findViewById(R.id.mSeekBar);
        mSeekBar.setProgress(progressInt);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    seekbar_text_progress.setText(1+"%");
                }else {
                    seekbar_text_progress.setText(progress+"%");
                }
                resetSeekbarTextMargin(seekBar.getThumb().getBounds().centerX());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progressInt = seekBar.getProgress();
            }
        });
        
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {
                    mListener.onClick(progressInt);
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "datapage", "freechange");
                }
                dialog.dismiss();
            }
        };
        
        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }
    
    public void setOnClickListener(OnDiaogClickListener listener) {
        mListener = listener;
    }
    
    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        sure_button.setTag(rListener);
        sure_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) sure_button
                        .getTag();
                lListener.onClick(MonthTrafficSetting.this, 1);
            }
        });
    }
    
    /**
     * reset the posotion of seekbar_text_progress
     * @param seekBarCenterX
     */
    private void resetSeekbarTextMargin(int seekBarCenterX){
        int leftMargin = seekBarCenterX-progressTextWidth/2+DipPixelUtil.dip2px(mContext, 6.5f);
        if(leftMargin<0){
            leftMargin = 0;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(progressTextWidth, progressTextHeight);
        params.leftMargin = leftMargin;
        seekbar_text_progress.setLayoutParams(params);
    }
}
