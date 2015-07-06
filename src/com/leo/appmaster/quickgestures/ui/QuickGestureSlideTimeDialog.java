
package com.leo.appmaster.quickgestures.ui;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

/**
 * QuickGestureSlideTimeDialog
 * 
 * @author run
 */
public class QuickGestureSlideTimeDialog extends LEOBaseDialog {
    private Context mContext;
    private TextView sure_button, mLeftBt, title, mFreeDisturbTv;
    private ListView mRadioListView;
    private LinearLayout mFreeDisturbAppHs, mFreeDisturbAppAddBt, mFilterAppLt;
    private CheckBox mJustHomeCb, mAppHomeCb;
    private LeoHorizontalListView mHorizontalLV;
    private boolean mIsFilterAppEditFlag;
    private boolean mCancelEdit;
    private UpdateFilterAppClickListener mUpdateFilterApp;

    public interface OnDiaogClickListener {
        public void onClick(int progress);
    }

    public interface UpdateFilterAppClickListener {
        public void updateFilterAppClickListener();
    }

    public QuickGestureSlideTimeDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    @Override
    public void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
        // 免干扰应用显示区域显示判断
        setFilterAppBtVisibility(AppMasterPreference.getInstance(mContext)
                .getSlideTimeAllAppAndHome());
    }

    @SuppressLint("CutPasteId")
    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_quick_gesture_slide_time_setting, null);
        title = (TextView) dlgView.findViewById(R.id.dialog_tilte);
        sure_button = (TextView) dlgView.findViewById(R.id.quick_slide_time_setting_dlg_right_btn);
        mLeftBt = (TextView) dlgView.findViewById(R.id.quick_slide_time_setting_dlg_left_btn);
        mRadioListView = (ListView) dlgView.findViewById(R.id.radioLV);
        mFreeDisturbTv = (TextView) dlgView.findViewById(R.id.free_disturbTV);
        mJustHomeCb = (CheckBox) dlgView.findViewById(R.id.dialog_radio_slide_time_just_home_cb);
        mAppHomeCb = (CheckBox) dlgView
                .findViewById(R.id.dialog_radio_slide_time_all_app_and_home_cb);
        mFreeDisturbAppAddBt = (LinearLayout) dlgView.findViewById(R.id.no_free_app_bt);
        mHorizontalLV = (LeoHorizontalListView) dlgView
                .findViewById(R.id.quick_gesture_horizontalLV);
        mFilterAppLt = (LinearLayout) dlgView.findViewById(R.id.fiter_app_LT);
        mJustHomeCb.setSelected(AppMasterPreference.getInstance(mContext).getSlideTimeJustHome());
        mAppHomeCb.setSelected(AppMasterPreference.getInstance(mContext)
                .getSlideTimeAllAppAndHome());
        mJustHomeCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mJustHomeCb.setSelected(true);
                mAppHomeCb.setSelected(false);
                setFilterAppBtVisibility(false);
            }
        });
        mAppHomeCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mJustHomeCb.setSelected(false);
                mAppHomeCb.setSelected(true);
                setFilterAppBtVisibility(true);
            }
        });
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);

    }

    public void setFilterAppBtVisibility(boolean flag) {
        if (flag) {
            mFilterAppLt.setVisibility(View.VISIBLE);
        } else {
            mFilterAppLt.setVisibility(View.GONE);
        }
    }

    public boolean getJustHometCheckStatus() {
        return mJustHomeCb.isSelected();
    }

    public boolean getAppHomeCheckStatus() {
        return mAppHomeCb.isSelected();
    }

    public void setRightBtnListener(android.view.View.OnClickListener rListener) {
        sure_button.setOnClickListener(rListener);
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
    }

    public void setFreeDisturbAdapter(BaseAdapter adapter) {
        mHorizontalLV.setAdapter(adapter);
    }

    public void setOnItemListenerFreeDisturb(OnItemClickListener listener) {
        mHorizontalLV.setOnItemClickListener(listener);
    }

    public void setOnLongClickListenerFreeDisturb(OnLongClickListener listener) {
        mHorizontalLV.setOnLongClickListener(listener);
    }

    public void setFreeDisturbAppHorizontalVisVisibility(boolean flag) {
        if (flag == true) {
            mHorizontalLV.setVisibility(View.VISIBLE);
        } else if (flag == false) {
            mHorizontalLV.setVisibility(View.GONE);
        }
    }

    public void setFreeDisturbAppAddBtVisVisibility(boolean flag) {
        if (flag == true) {
            mFreeDisturbAppAddBt.setVisibility(View.VISIBLE);
        } else if (flag == false) {
            mFreeDisturbAppAddBt.setVisibility(View.GONE);
        }
    }

    public void setFreeDisturbAppAddBtClickListener(
            android.view.View.OnClickListener onClickListener) {
        mFreeDisturbAppAddBt
                .setOnClickListener((android.view.View.OnClickListener) onClickListener);
    }

    public void setLeftButtonClickListener(android.view.View.OnClickListener listener) {
        mLeftBt.setOnClickListener(listener);
    }

    public void setIsEdit(boolean flag) {
        mIsFilterAppEditFlag = flag;
    }

    public void setUpdateFilterAppClickListener(UpdateFilterAppClickListener click) {
        mUpdateFilterApp = click;
    }

    // TODO
    public boolean cancelEdit() {
        return mCancelEdit;
    }

    @Override
    public void onBackPressed() {
        if (!mIsFilterAppEditFlag) {
            super.onBackPressed();
        } else {
            mIsFilterAppEditFlag = false;
            mUpdateFilterApp.updateFilterAppClickListener();
        }
    }

    
    @Override
    protected void onStop() {
        super.onStop();
        mJustHomeCb.setSelected(AppMasterPreference.getInstance(mContext).getSlideTimeJustHome());
        mAppHomeCb.setSelected(AppMasterPreference.getInstance(mContext)
                .getSlideTimeAllAppAndHome());
    }
}
