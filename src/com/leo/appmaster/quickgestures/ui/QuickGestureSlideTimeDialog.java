
package com.leo.appmaster.quickgestures.ui;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyDeletEditEvent;
import com.leo.appmaster.quickgestures.QuickGestureWindowManager;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

/**
 * QuickGestureSlideTimeDialog
 * 
 * @author run
 */
public class QuickGestureSlideTimeDialog extends LEOBaseDialog {
    private Context mContext;
    private TextView sure_button, title, mFreeDisturbTv;
    private ListView mRadioListView;
    private OnDiaogClickListener mListener;
    private LinearLayout mFreeDisturbAppHs;
    private CheckBox mJustHomeCb, mAppHomeCb;
    private ImageView mAddFreeDisturbAppIv;

    public interface OnDiaogClickListener {
        public void onClick(int progress);
    }

    public QuickGestureSlideTimeDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    @SuppressLint("CutPasteId")
    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_quick_gesture_slide_time_setting, null);
        Resources resources = AppMasterApplication.getInstance().getResources();
        title = (TextView) dlgView.findViewById(R.id.dialog_tilte);
        sure_button = (TextView) dlgView.findViewById(R.id.sure_button);
        mRadioListView = (ListView) dlgView.findViewById(R.id.radioLV);
        mFreeDisturbAppHs = (LinearLayout) dlgView.findViewById(R.id.slide_time_app_hs);
        mFreeDisturbTv = (TextView) dlgView.findViewById(R.id.free_disturbTV);
        mJustHomeCb = (CheckBox) dlgView.findViewById(R.id.dialog_radio_slide_time_just_home_cb);
        mAppHomeCb = (CheckBox) dlgView
                .findViewById(R.id.dialog_radio_slide_time_all_app_and_home_cb);
        mAddFreeDisturbAppIv = (ImageView) dlgView.findViewById(R.id.add_free_disturb_appIV);
        mJustHomeCb.setSelected(AppMasterPreference.getInstance(mContext).getSlideTimeJustHome());
        mAppHomeCb.setSelected(AppMasterPreference.getInstance(mContext)
                .getSlideTimeAllAppAndHome());
        mJustHomeCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mJustHomeCb.setSelected(true);
                mAppHomeCb.setSelected(false);
                AppMasterPreference.getInstance(mContext).setSlideTimeJustHome(true);
                AppMasterPreference.getInstance(mContext).setSlideTimeAllAppAndHome(false);
                dismiss();
                LeoEventBus
                        .getDefaultBus()
                        .post(new PrivacyDeletEditEvent(
                                QuickGestureWindowManager.QUICK_GESTURE_SETTING_DIALOG_RADIO_SLIDE_TIME_SETTING_FINISH_NOTIFICATION));
            }
        });
        mAppHomeCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mJustHomeCb.setSelected(false);
                mAppHomeCb.setSelected(true);
                AppMasterPreference.getInstance(mContext).setSlideTimeJustHome(false);
                AppMasterPreference.getInstance(mContext).setSlideTimeAllAppAndHome(true);
                dismiss();
                LeoEventBus
                        .getDefaultBus()
                        .post(new PrivacyDeletEditEvent(
                                QuickGestureWindowManager.QUICK_GESTURE_SETTING_DIALOG_RADIO_SLIDE_TIME_SETTING_FINISH_NOTIFICATION));
            }
        });
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {

                dialog.dismiss();
            }
        };

        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setAddFreeDisturbOnClickListener(android.view.View.OnClickListener listener) {
        mAddFreeDisturbAppIv.setOnClickListener(listener);
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
                lListener.onClick(QuickGestureSlideTimeDialog.this, 1);
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

    public void setFreeDisturbVisibility(boolean flag) {
        if (flag == true) {
            mFreeDisturbTv.setVisibility(View.VISIBLE);
        } else if (flag == false) {
            mFreeDisturbTv.setVisibility(View.GONE);
        }
    }

    public void setFreeDisturbText(int textId) {
        if (textId > 0) {
            mFreeDisturbTv.setText(textId);
        }
    }

    public void setFreeDisturbApp(List<Integer> apps) {
        if (apps != null && apps.size() > 0) {
            if (mFreeDisturbAppHs != null) {
                for (Integer integer : apps) {
                    ImageView view = new ImageView(mContext);
                    view.setImageResource(integer);
                    mFreeDisturbAppHs.addView(view);
                }
            }
        }
        int count = mFreeDisturbAppHs.getChildCount();
        // for (int i = 0; i < count; i++) {
        // View view = mFreeDisturbAppHs.getChildAt(i);
        // view.setOnLongClickListener(new OnLongClickListener() {
        //
        // @Override
        // public boolean onLongClick(View arg0) {
        // Toast.makeText(mContext, "进入编辑模式", Toast.LENGTH_SHORT).show();
        // return false;
        // }
        // });
        // }
    }

    // public void setFreeDisturbAppOnItemListener(){
    // mFreeDisturbAppHs
    // }
}
