
package com.leo.appmaster.quickgestures.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

/**
 * QuickGestureRadioSeekBarDialog
 * 
 * @author run
 */
public class QuickGestureRadioSeekBarDialog extends LEOBaseDialog {
    private Context mContext;
    private TextView seekbar_text, sure_button, seekbar_text_progress, title;
    private SeekBar mSeekBar;
    private int progressInt;
    private ListView mRadioListView;
    private ImageView mLeftBottom, mRightBottom, mLeftCenter, mRightCenter;
    private LinearLayout mLeftBottomLt, mRightBottomLt, mLeftCenterLt, mRightCenterLt;

    private OnDiaogClickListener mListener;

    public interface OnDiaogClickListener {
        public void onClick(int progress);
    }

    public QuickGestureRadioSeekBarDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (AppMasterPreference.getInstance(mContext).getDialogRadioLeftBottom()) {
            mLeftBottom.setBackgroundDrawable(mContext.getResources()
                    .getDrawable(R.drawable.select));
        } else {
            mLeftBottom.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.unselect));
        }
        if (AppMasterPreference.getInstance(mContext).getDialogRadioRightBottom()) {
            mRightBottom.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.select));
        } else {
            mRightBottom.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.unselect));
        }

        if (AppMasterPreference.getInstance(mContext).getDialogRadioLeftCenter()) {
            mLeftCenter.setBackgroundDrawable(mContext.getResources()
                    .getDrawable(R.drawable.select));
        } else {
            mLeftCenter.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.unselect));
        }
        if (AppMasterPreference.getInstance(mContext).getDialogRadioRightCenter()) {
            mRightCenter.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.select));
        } else {
            mRightCenter.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.unselect));
        }
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_quick_gesture_radio_seekbar_setting, null);
        Resources resources = AppMasterApplication.getInstance().getResources();
        title = (TextView) dlgView.findViewById(R.id.dialog_tilte);
        seekbar_text = (TextView) dlgView.findViewById(R.id.seekbar_text);
        seekbar_text_progress = (TextView) dlgView.findViewById(R.id.seekbar_text_progress);
        sure_button = (TextView) dlgView.findViewById(R.id.quick_seekbar_setting_dlg_right_btn);
        seekbar_text.setText(resources.getString(R.string.flow_settting_dialog_remain));
        mSeekBar = (SeekBar) dlgView.findViewById(R.id.qucik_seekBar);
        mLeftBottom = (ImageView) dlgView.findViewById(R.id.dialog_radio_left_bottomRB);
        mRadioListView = (ListView) dlgView.findViewById(R.id.radioLV);
        mLeftBottomLt = (LinearLayout) dlgView.findViewById(R.id.left_bottom_lt);
        mRightBottomLt = (LinearLayout) dlgView.findViewById(R.id.right_bottom_lt);
        mLeftCenterLt = (LinearLayout) dlgView.findViewById(R.id.left_center_lt);
        mRightBottom = (ImageView) dlgView.findViewById(R.id.dialog_radio_right_bottomRB);
        mRightCenterLt = (LinearLayout) dlgView.findViewById(R.id.right_center_lt);
        mLeftCenter = (ImageView) dlgView.findViewById(R.id.dialog_radio_left_centerRB);
        mRightCenter = (ImageView) dlgView.findViewById(R.id.dialog_radio_right_center_normalRB);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    seekbar_text_progress.setText(1 + "%");
                } else {
                    seekbar_text_progress.setText(progress + "%");
                }
                FloatWindowHelper.updateView(mContext, seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progressInt = seekBar.getProgress();
                QuickGestureManager.getInstance(mContext).setSlidAreaSize(progressInt);
            }
        });

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {
                    mListener.onClick(progressInt);
                }
            }
        };

        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!QuickGestureActivity.isSureBt) {
            LeoEventBus.getDefaultBus().post(
                    new PrivacyEditFloatEvent(
                            QuickGestureManager.getInstance(mContext).QUICK_GESTURE_SETTING_EVENT));
            QuickGestureManager.getInstance(mContext).mSlidAreaSize = AppMasterPreference
                    .getInstance(
                            mContext).getQuickGestureDialogSeekBarValue();
            FloatWindowHelper.updateView(mContext,
                    QuickGestureManager.getInstance(mContext).mSlidAreaSize);
            LeoEventBus.getDefaultBus().post(
                    new PrivacyEditFloatEvent(
                            QuickGestureManager.getInstance(mContext).QUICK_GESTURE_SETTING_EVENT));
        } else {
            QuickGestureActivity.isSureBt = false;
        }
        FloatWindowHelper.mEditQuickAreaFlag = false;
        // QuickGestureActivity.mAlarmDialogFlag = false;
        FloatWindowHelper.updateFloatWindowBackgroudColor(mContext, false);
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
                lListener.onClick(QuickGestureRadioSeekBarDialog.this, 1);
            }
        });
    }

    public void setShowRadioListView(boolean flag) {
        if (flag == true) {
            mRadioListView.setVisibility(View.VISIBLE);
        } else if (flag == false) {
            mRadioListView.setVisibility(View.GONE);
        }
    }

    public void setRadioListViewAdapter(BaseAdapter adapter) {
        mRadioListView.setAdapter(adapter);
    }

    public void setTitle(int id) {
        title.setText(id);
    }

    public void setSeekBarText(int id) {
        seekbar_text.setText(id);
    }

    public void setSeekBarTextVisibility(boolean flag) {
        if (flag == true) {
            seekbar_text.setVisibility(View.VISIBLE);
        } else if (flag == false) {
            seekbar_text.setVisibility(View.GONE);
        }

    }

    public void setSeekbarTextProgressVisibility(boolean flag) {
        if (flag == true) {
            seekbar_text_progress.setVisibility(View.VISIBLE);
        } else if (flag == false) {
            seekbar_text_progress.setVisibility(View.GONE);
        }
    }

    public int getSeekBarProgressValue() {
        return progressInt;
    }

    public void setSeekBarProgressValue(int value) {
        mSeekBar.setProgress(value);
    }

    public void setRadioOnItemClickListener(OnItemClickListener listener) {
        mRadioListView.setOnItemClickListener(listener);
    }

    public void setLeftBottomBackgroud(Drawable drawable) {
        mLeftBottom.setBackgroundDrawable(drawable);
    }

    public void setRightBottomBackgroud(Drawable drawable) {
        mRightBottom.setBackgroundDrawable(drawable);
    }

    public void setLeftCenterBackgroud(Drawable drawable) {
        mLeftCenter.setBackgroundDrawable(drawable);
    }

    public void setRightCenterBackgroud(Drawable drawable) {
        mRightCenter.setBackgroundDrawable(drawable);
    }

    public void setLeftBottomOnClickListener(android.view.View.OnClickListener listener) {
        mLeftBottomLt.setOnClickListener(listener);
    }

    public void setRightBottomOnClickListener(android.view.View.OnClickListener listener) {
        mRightBottomLt.setOnClickListener(listener);
    }

    public void setLeftCenterOnClickListener(android.view.View.OnClickListener listener) {
        mLeftCenterLt.setOnClickListener(listener);
    }

    public void setRightCenterOnClickListener(android.view.View.OnClickListener listener) {
        mRightCenterLt.setOnClickListener(listener);
    }
}
