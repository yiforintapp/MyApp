
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
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LeoSeekBar;
import com.leo.appmaster.ui.RippleView1;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LanguageUtils;

public class MonthTrafficSetting extends LEOBaseDialog {
    private Context mContext;
    private TextView seekbar_text, sure_button, seekbar_text_progress;
    private LeoSeekBar mSeekBar;
    private AppMasterPreference sp_notice_flow;
    private int progressInt;
    private int progressTextWidth, progressTextHeight;
    private OnDiaogClickListener mListener;
    private RippleView1 mRvBlue;


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
        mRvBlue = (RippleView1) dlgView.findViewById(R.id.rv_dialog_blue_button);
        seekbar_text_progress = (TextView) dlgView.findViewById(R.id.seekbar_text_progress);
        progressInt = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getOverDataInvokePercent();
        seekbar_text_progress.setText(progressInt + "%");

        // 得到seekbar_text_progress 大小，初始化位置
        ViewTreeObserver vto = seekbar_text_progress.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                seekbar_text_progress.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                progressTextWidth = seekbar_text_progress.getWidth();
                progressTextHeight = seekbar_text_progress.getHeight();

                /** init the position of seekbar_text_progress **/
                resetSeekbarTextMargin(mSeekBar.getSeekBarThumb().getBounds().centerX());
            }
        });

        sure_button = (TextView) dlgView.findViewById(R.id.sure_button);

        mSeekBar = (LeoSeekBar) dlgView.findViewById(R.id.mSeekBar);
        // 设置seekbar显示位置
        setSeeBarProgress(progressInt);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    if (!LanguageUtils.isRightToLeftLanguage(null)) {
                        seekbar_text_progress.setText(1 + "%");
                    } else {
                        seekbar_text_progress.setText(100 + "%");
                    }
                } else {
                    if (!LanguageUtils.isRightToLeftLanguage(null)) {
                        // 系统语言为从左到右显示的语言
                        seekbar_text_progress.setText(progress + "%");
                    } else {
                        // 系统语言为从右到左显示的语言
                        if (progress == 100) {
                            seekbar_text_progress.setText(AppBusinessManager.mRtToLtSeeBarMin + "%");
                        } else {
                            seekbar_text_progress.setText((AppBusinessManager.mRtToLtSeeBarMax - progress) + "%");
                        }

                    }
                }
                resetSeekbarTextMargin(((LeoSeekBar) seekBar).getSeekBarThumb().getBounds()
                        .centerX());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!LanguageUtils.isRightToLeftLanguage(null)) {
                    progressInt = seekBar.getProgress();
                } else {
                    // 系统语言为从右到左显示的语言
                    progressInt = AppBusinessManager.mRtToLtSeeBarMax - (seekBar.getProgress());
                }
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

    /*
     * 显示方向处理
     */
    private void setSeeBarProgress(int value) {
        if (!LanguageUtils.isRightToLeftLanguage(null)) {
            mSeekBar.setProgress(value);
        } else {
            mSeekBar.setProgress(AppBusinessManager.mRtToLtSeeBarMax - value);
        }
    }

    public void setOnClickListener(OnDiaogClickListener listener) {
        mListener = listener;
    }

    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        mRvBlue.setTag(rListener);
        mRvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
                        .getTag();
                try {
                    lListener.onClick(MonthTrafficSetting.this, 1);
                } catch (Exception e) {
                }
            }
        });
//        mRvBlue.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//
//            @Override
//            public void onRippleComplete(RippleView arg0) {
//                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
//                        .getTag();
//                try {
//                    lListener.onClick(MonthTrafficSetting.this, 1);
//                } catch (Exception e) {
//                }
//            }
//        });
    }

    /**
     * reset the posotion of seekbar_text_progress
     *
     * @param seekBarCenterX
     */
    private void resetSeekbarTextMargin(int seekBarCenterX) {
        int leftMargin = seekBarCenterX - progressTextWidth / 2 + DipPixelUtil.dip2px(mContext, 6f);
        if (leftMargin < 0) {
            leftMargin = 0;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(progressTextWidth,
                progressTextHeight);
        params.leftMargin = leftMargin;
        seekbar_text_progress.setLayoutParams(params);
    }
}
