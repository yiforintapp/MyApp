
package com.leo.appmaster.quickgestures;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
/**
 * QuickGestureRadioSeekBarDialog
 * @author run
 *
 */
public class QuickGestureRadioSeekBarDialog extends LEOBaseDialog {
    private Context mContext;
    private TextView seekbar_text, sure_button, seekbar_text_progress, title;
    private SeekBar mSeekBar;
    private AppMasterPreference sp_notice_flow;
    private int progressInt;
    private ListView mRadioListView;

    private OnDiaogClickListener mListener;

    public interface OnDiaogClickListener {
        public void onClick(int progress);
    }

    public QuickGestureRadioSeekBarDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        sp_notice_flow = AppMasterPreference.getInstance(mContext);
        initUI();
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_quick_gesture_radio_seekbar_setting, null);
        Resources resources = AppMasterApplication.getInstance().getResources();
        title = (TextView) dlgView.findViewById(R.id.dialog_tilte);
        seekbar_text = (TextView) dlgView.findViewById(R.id.seekbar_text);
        seekbar_text_progress = (TextView) dlgView.findViewById(R.id.seekbar_text_progress);
        sure_button = (TextView) dlgView.findViewById(R.id.sure_button);
        // progressInt = sp_notice_flow.getFlowSettingBar();
        seekbar_text.setText(resources.getString(R.string.flow_settting_dialog_remain));
        // seekbar_text_progress.setText(progressInt + "%");
        mSeekBar = (SeekBar) dlgView.findViewById(R.id.qucik_seekBar);
        // mSeekBar.setProgress(progressInt);
        mRadioListView = (ListView) dlgView.findViewById(R.id.radioLV);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Resources resources = AppMasterApplication.getInstance().getResources();
                if (progress == 0) {
                    seekbar_text_progress.setText(1 + "%");
                } else {
                    seekbar_text_progress.setText(progress + "%");
                }
                QuickGestureWindowManager.updateView(mContext, 1, seekBar.getProgress());
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
}
