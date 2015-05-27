
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.QuickGestureFloatWindowEvent;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.model.QuickGestureSettingBean;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.ui.QuickGestureRadioSeekBarDialog.OnDiaogClickListener;
import com.leo.appmaster.quickgestures.view.QuickGesturesAreaView;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;

/**
 * QuickGestureActivity
 * 
 * @author run
 */
public class QuickGestureActivity extends BaseActivity implements OnItemClickListener,
        OnCheckedChangeListener, OnTouchListener {
    private ListView mQuickGestureLV;
    private CommonTitleBar mTitleBar;
    private QuickGestureAdapter mAdapter;
    private List<QuickGestureSettingBean> mQuickGestureSettingOption;
    private AppMasterPreference mPre;
    private QuickGestureRadioSeekBarDialog mAlarmDialog;
    private QuickGestureSlideTimeDialog mSlideTimeDialog;
    private boolean mAlarmDialogFlag = false;
    private boolean mLeftBottom, mRightBottm, mRightCenter, mLeftCenter;
    private List<QuickGsturebAppInfo> mFreeApps;
    private FreeDisturbSlideTimeAdapter mSlideTimeAdapter;
    private TextView mLeftTopView, mLeftBottomView, mRightTopView, mRightBottomView;
    private RelativeLayout mTipRL;
    private ImageView mHandImage, mArrowImage;
    private boolean mFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_gesture);
        mQuickGestureSettingOption = new ArrayList<QuickGestureSettingBean>();
        mPre = AppMasterPreference.getInstance(this);
        initUi();
        fillSettingData();
        mAdapter = new QuickGestureAdapter(this, mQuickGestureSettingOption);
        mQuickGestureLV.setAdapter(mAdapter);
        LeoEventBus.getDefaultBus().register(this);
    }

    private void initUi() {
        mQuickGestureLV = (ListView) findViewById(R.id.quick_gesture_lv);
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_quick_gesture_title_bar);
        mTitleBar.openBackView();
        mQuickGestureLV.setOnItemClickListener(this);
        mTipRL = (RelativeLayout) findViewById(R.id.quick_tipRL);
        mLeftTopView = (TextView) findViewById(R.id.gesture_left_tips_top_tv);
        mLeftBottomView = (TextView) findViewById(R.id.gesture_left_tips_bottom);
        mRightTopView = (TextView) findViewById(R.id.gesture_right_tips_top_tv);
        mRightBottomView = (TextView) findViewById(R.id.gesture_right_tips_bottom);
        mHandImage = (ImageView) findViewById(R.id.gesture_handIV);
        mArrowImage = (ImageView) findViewById(R.id.gesture_arrowIV);
        if (!AppMasterPreference.getInstance(this)
                .getFristSlidingTip()) {
            gestureTranslationAnim(mHandImage, mArrowImage);
            mTipRL.setVisibility(View.VISIBLE);
            mTipRL.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {

                }
            });
            quickTipAnim(mTipRL);
            mLeftTopView.setOnTouchListener(this);
            mLeftBottomView.setOnTouchListener(this);
            mRightTopView.setOnTouchListener(this);
            mRightBottomView.setOnTouchListener(this);

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
         if (mAlarmDialogFlag) {
         FloatWindowHelper.mEditQuickAreaFlag = true;
         updateFloatWindowBackGroudColor();
         }
    }

    @Override
    protected void onPause() {
        super.onPause();
         if (FloatWindowHelper.mEditQuickAreaFlag == true) {
         FloatWindowHelper.mEditQuickAreaFlag = false;
         updateFloatWindowBackGroudColor();
         }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
    }

    public void onEventMainThread(QuickGestureFloatWindowEvent event) {
        String flag = event.editModel;
        if (FloatWindowHelper.QUICK_GESTURE_SETTING_DIALOG_RADIO_SLIDE_TIME_SETTING_FINISH_NOTIFICATION
                .equals(flag)) {
            mQuickGestureSettingOption.clear();
            fillSettingData();
            mAdapter.notifyDataSetChanged();
        } else if (FloatWindowHelper.QUICK_GESTURE_ADD_FREE_DISTURB_NOTIFICATION.equals(flag)) {
            showSlideShowTimeSettingDialog();
        }

    }

    // 构建列表数据
    private void fillSettingData() {
        QuickGestureSettingBean gestureSettingOpenGesture = new QuickGestureSettingBean();
        gestureSettingOpenGesture.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_open_quick_gesture));
        gestureSettingOpenGesture.setCheck(mPre.getSwitchOpenQuickGesture());
        gestureSettingOpenGesture.setBackageDraw(R.drawable.bg);
        mQuickGestureSettingOption.add(gestureSettingOpenGesture);
        QuickGestureSettingBean gestureSettingSlidingAreaLocation = new QuickGestureSettingBean();
        gestureSettingSlidingAreaLocation.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_sliding_area_location_title));
        gestureSettingSlidingAreaLocation.setBackageDraw(R.drawable.bg_upround);
        mQuickGestureSettingOption.add(gestureSettingSlidingAreaLocation);
        QuickGestureSettingBean gestureSettingAbleSlidingTime = new QuickGestureSettingBean();
        gestureSettingAbleSlidingTime.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_able_sliding_time));
        gestureSettingAbleSlidingTime.setBackageDraw(R.drawable.bg_downround);
        mQuickGestureSettingOption.add(gestureSettingAbleSlidingTime);
        QuickGestureSettingBean gestureSettingNoReadMessage = new QuickGestureSettingBean();
        gestureSettingNoReadMessage.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_no_read_message_tip));
        gestureSettingNoReadMessage.setCheck(mPre.getSwitchOpenNoReadMessageTip());
        gestureSettingNoReadMessage.setBackageDraw(R.drawable.bg_upround);
        mQuickGestureSettingOption.add(gestureSettingNoReadMessage);
        QuickGestureSettingBean gestureSettingRecentlyContact = new QuickGestureSettingBean();
        gestureSettingRecentlyContact.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_recently_contact));
        gestureSettingRecentlyContact.setCheck(mPre.getSwitchOpenRecentlyContact());
        gestureSettingRecentlyContact.setBackageDraw(R.drawable.bg_zeroround);
        mQuickGestureSettingOption.add(gestureSettingRecentlyContact);
        QuickGestureSettingBean gestureSettingContactMessagTip = new QuickGestureSettingBean();
        gestureSettingContactMessagTip.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_privacy_contact_message_tip));
        gestureSettingContactMessagTip.setBackageDraw(R.drawable.bg_downround);
        gestureSettingContactMessagTip.setCheck(mPre.getSwitchOpenPrivacyContactMessageTip());
        mQuickGestureSettingOption.add(gestureSettingContactMessagTip);

    }

    private class QuickGestureAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;

        public QuickGestureAdapter(Context context, List<QuickGestureSettingBean> beans) {
            layoutInflater = LayoutInflater.from(context);
            mQuickGestureSettingOption = beans;
        }

        @Override
        public int getCount() {
            return mQuickGestureSettingOption.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mQuickGestureSettingOption.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        class ViewHolder {
            @SuppressWarnings("unused")
            ImageView imageView, arrowImageVIew;
            CheckBox switchView;
            TextView title, content, segmentationTv, lineTextView;
            RelativeLayout itemRL;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup arg2) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_quick_gesture_item, null);
                vh.itemRL = (RelativeLayout) convertView.findViewById(R.id.quick_gesture_content);
                vh.imageView = (ImageView) convertView.findViewById(R.id.quick_gesture_option_IV);
                vh.switchView = (CheckBox) convertView.findViewById(R.id.quick_gesture_check);
                vh.title = (TextView) convertView.findViewById(R.id.quick_gesture_item_nameTV);
                vh.content = (TextView) convertView.findViewById(R.id.quick_gesture_item_cotentTV);
                vh.segmentationTv = (TextView) convertView.findViewById(R.id.segmentation_tv);
                vh.lineTextView = (TextView) convertView.findViewById(R.id.quick_line_tv);
                vh.arrowImageVIew = (ImageView) convertView.findViewById(R.id.quick_gesture_arrow);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.switchView.setTag(position);
            QuickGestureSettingBean bean = mQuickGestureSettingOption.get(position);
            vh.title.setText(bean.getName());
            // 设置背景
            vh.itemRL.setBackgroundResource(bean.getBackageDraw());
            // 设置复选框
            if (position == 0 || position == 3
                    || position == 4
                    || position == 5) {
                vh.switchView.setVisibility(View.VISIBLE);
                if (position == 0) {
                    vh.switchView.setChecked(bean.isCheck());
                } else if (position == 3) {
                    vh.switchView.setChecked(bean.isCheck());
                } else if (position == 4) {
                    vh.switchView.setChecked(bean.isCheck());
                } else if (position == 5) {
                    vh.switchView.setChecked(bean.isCheck());
                }
            } else {
                vh.switchView.setVisibility(View.GONE);
            }
            // 显示下划线
            if (position == 1 || position == 3 || position == 4) {
                vh.lineTextView.setVisibility(View.VISIBLE);
            } else {
                vh.lineTextView.setVisibility(View.GONE);
            }
            // 显示间隔条
            if (position == 0 || position == 2) {
                vh.segmentationTv.setVisibility(View.VISIBLE);
            } else {
                vh.segmentationTv.setVisibility(View.GONE);
            }
            // 显示箭头
            if (position == 1 || position == 2) {
                vh.arrowImageVIew.setVisibility(View.VISIBLE);
            } else {
                vh.arrowImageVIew.setVisibility(View.GONE);
            }
            // 显示划出时机
            if (position == 2) {
                vh.content.setVisibility(View.VISIBLE);
                if (mPre.getSlideTimeJustHome()) {
                    vh.content
                            .setText(R.string.pg_appmanager_quick_gesture_slide_time_just_home_text);
                }
                if (mPre.getSlideTimeAllAppAndHome()) {
                    vh.content
                            .setText(R.string.pg_appmanager_quick_gesture_slide_time_home_and_all_app_text);
                }
            } else {
                vh.content.setVisibility(View.GONE);
            }
            vh.switchView.setOnCheckedChangeListener(QuickGestureActivity.this);
            return convertView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (mPre.getSwitchOpenQuickGesture()) {
            if (arg2 == 1) {
                FloatWindowHelper.mEditQuickAreaFlag = true;
                showSettingDialog(true);
            } else if (arg2 == 2) {
                showSlideShowTimeSettingDialog();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        int position = (Integer) arg0.getTag();
        if (position == 0) {
            if (!arg1) {
                QuickGestureManager.getInstance(this).stopFloatWindow();
                // 移除悬浮窗
                FloatWindowHelper.removeAllFloatWindow(QuickGestureActivity.this);
            } else {
                if (mPre.getSwitchOpenQuickGesture()) {
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                        @Override
                        public void run() {
//                            FloatWindowHelper.createFloatWindow(QuickGestureActivity.this,
//                                    AppMasterPreference
//                                            .getInstance(getApplicationContext())
//                                            .getQuickGestureDialogSeekBarValue());
                            QuickGestureManager.getInstance(QuickGestureActivity.this)
                                    .startFloatWindow();
                        }
                    });
                }
            }
            mPre.setSwitchOpenQuickGesture(arg1);
            mQuickGestureSettingOption.get(position).setCheck(arg1);
        } else if (position == 3) {
            mPre.setSwitchOpenNoReadMessageTip(arg1);
            mQuickGestureSettingOption.get(position).setCheck(arg1);
            if (arg1) {
                // 查看短信数据库未读短信数量
                AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
                    @Override
                    public void run() {
                        QuickGestureManager.getInstance(QuickGestureActivity.this).mMessages = PrivacyContactUtils
                                .getSysMessage(QuickGestureActivity.this,
                                        QuickGestureActivity.this.getContentResolver(),
                                        "read=0 AND type=1", null, false);
                        if (QuickGestureManager.getInstance(QuickGestureActivity.this).mMessages != null
                                && QuickGestureManager.getInstance(QuickGestureActivity.this).mMessages
                                        .size() > 0) {
                            LockManager.getInstatnce().isShowSysNoReadMessage = true;
                            FloatWindowHelper.removeShowReadTipWindow(QuickGestureActivity.this);
                        }
                    }
                });
            }
        } else if (position == 4) {
            mPre.setSwitchOpenRecentlyContact(arg1);
            mQuickGestureSettingOption.get(position).setCheck(arg1);
            if (arg1) {
                AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                    @Override
                    public void run() {
                        String selection = Calls.TYPE + "=? and " + Calls.NEW + "=?";
                        String[] selectionArgs = new String[] {
                                String.valueOf(Calls.MISSED_TYPE), String.valueOf(1)
                        };
                        QuickGestureManager.getInstance(QuickGestureActivity.this).mCallLogs = PrivacyContactUtils
                                .getSysCallLog(QuickGestureActivity.this,
                                        QuickGestureActivity.this.getContentResolver(), selection,
                                        selectionArgs);
                        if (QuickGestureManager.getInstance(QuickGestureActivity.this).mCallLogs != null
                                && QuickGestureManager.getInstance(QuickGestureActivity.this).mCallLogs
                                        .size() > 0) {
                            LockManager.getInstatnce().isShowSysNoReadMessage = true;
                            FloatWindowHelper.removeShowReadTipWindow(QuickGestureActivity.this);
                        }
                    }
                });
            }
        } else if (position == 5) {
            mPre.setSwitchOpenPrivacyContactMessageTip(arg1);
            mQuickGestureSettingOption.get(position).setCheck(arg1);
        }
    }

    // 构建滑动区域设置列表数据
    private List<DialogRadioBean> initDialogRadioTextData() {
        List<DialogRadioBean> datas = new ArrayList<DialogRadioBean>();
        DialogRadioBean bean1 = new DialogRadioBean();
        bean1.name = this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_dialog_radio_left_bottom_text);
        bean1.isCheck = mPre.getDialogRadioLeftBottom();
        datas.add(bean1);

        DialogRadioBean bean2 = new DialogRadioBean();
        bean2.name = this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_dialog_radio_right_bottom_text);
        bean2.isCheck = mPre.getDialogRadioRightBottom();
        datas.add(bean2);

        DialogRadioBean bean3 = new DialogRadioBean();
        bean3.name = this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_dialog_radio_left_center_text);
        bean3.isCheck = mPre.getDialogRadioLeftCenter();
        datas.add(bean3);

        DialogRadioBean bean4 = new DialogRadioBean();
        bean4.name = this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_dialog_radio_right_center_text);
        bean4.isCheck = mPre.getDialogRadioRightCenter();
        datas.add(bean4);
        return datas;
    }

    class DialogRadioBean {
        String name;
        boolean isCheck;
    }

    // 滑动区域设置
    private void showSettingDialog(boolean flag) {
        if (mAlarmDialog == null) {
            mAlarmDialog = new QuickGestureRadioSeekBarDialog(this);
        }
        mAlarmDialog.setShowRadioListView(flag);
        List<DialogRadioBean> data = initDialogRadioTextData();
        RadioListViewAdapter adapter = new RadioListViewAdapter(this, data);
        mAlarmDialog.setRadioListViewAdapter(adapter);
        mAlarmDialog
                .setTitle(R.string.pg_appmanager_quick_gesture_option_sliding_area_location_title);
        mAlarmDialog.setSeekBarTextVisibility(false);
        mAlarmDialog.setSeekbarTextProgressVisibility(false);
        mAlarmDialog.setSeekBarProgressValue(mPre.getQuickGestureDialogSeekBarValue());
        mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {

            @Override
            public void onClick(int progress) {
                FloatWindowHelper.mEditQuickAreaFlag = false;
                mAlarmDialogFlag = false;
                // 保存设置的值
                if (mLeftBottom || mRightBottm || mLeftCenter || mRightCenter) {
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                        @Override
                        public void run() {
                            mPre.setDialogRadioLeftBottom(mLeftBottom);
                            mPre.setDialogRadioRightBottom(mRightBottm);
                            mPre.setDialogRadioLeftCenter(mLeftCenter);
                            mPre.setDialogRadioRightCenter(mRightCenter);
                            mPre.setQuickGestureDialogSeekBarValue(mAlarmDialog
                                    .getSeekBarProgressValue());
                        }
                    });
                    updateFloatWindowBackGroudColor();
                    if (mAlarmDialog != null) {
                        mAlarmDialog.dismiss();
                    }
                } else {
                    Toast.makeText(
                            QuickGestureActivity.this,
                            QuickGestureActivity.this
                                    .getResources()
                                    .getString(
                                            R.string.pg_appmanager_quick_gesture_option_dialog_radio_toast_text),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        mAlarmDialog.setLeftButtomClick(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                FloatWindowHelper.mEditQuickAreaFlag = false;
                mAlarmDialogFlag = false;
                updateFloatWindowBackGroudColor();
                if (mAlarmDialog != null) {
                    mAlarmDialog.dismiss();
                }
            }
        });
        mAlarmDialog.setCancelable(false);
        mAlarmDialog.show();
        mAlarmDialogFlag = true;
        updateFloatWindowBackGroudColor();
    }

    // 更新背景
    private void updateFloatWindowBackGroudColor() {
        FloatWindowHelper
                .updateFloatWindowBackgroudColor(FloatWindowHelper.mEditQuickAreaFlag);
        FloatWindowHelper.createFloatWindow(QuickGestureActivity.this, AppMasterPreference
                .getInstance(getApplicationContext()).getQuickGestureDialogSeekBarValue());
    }

    class RadioListViewAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private List<DialogRadioBean> mData;

        public RadioListViewAdapter(Context context, List<DialogRadioBean> data) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mData.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }

        @Override
        public View getView(int arg0, View convertView, ViewGroup arg2) {
            ViewHolder vh = null;
            if (vh == null) {
                vh = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.activity_dialog_radio_listview_item,
                        null);
                vh.textView = (TextView) convertView.findViewById(R.id.dialog_radio_itme_tv);
                vh.checkBox = (CheckBox) convertView.findViewById(R.id.dialog_radio_itme_normalRB);
                vh.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        int flag = (Integer) arg0.getTag();
                        if (flag == 0) {
                            mLeftBottom = arg1;
                            mPre.setDialogRadioLeftBottom(arg1);
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(new QuickGestureFloatWindowEvent(
                                            FloatWindowHelper.QUICK_GESTURE_SETTING_DIALOG_RADIO_FINISH_NOTIFICATION));
                        } else if (flag == 1) {
                            mRightBottm = arg1;
                            mPre.setDialogRadioRightBottom(arg1);
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(new QuickGestureFloatWindowEvent(
                                            FloatWindowHelper.QUICK_GESTURE_SETTING_DIALOG_RADIO_FINISH_NOTIFICATION));
                        } else if (flag == 2) {
                            mLeftCenter = arg1;
                            mPre.setDialogRadioLeftCenter(arg1);
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(new QuickGestureFloatWindowEvent(
                                            FloatWindowHelper.QUICK_GESTURE_SETTING_DIALOG_RADIO_FINISH_NOTIFICATION));
                        } else if (flag == 3) {
                            mRightCenter = arg1;
                            mPre.setDialogRadioRightCenter(arg1);
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(new QuickGestureFloatWindowEvent(
                                            FloatWindowHelper.QUICK_GESTURE_SETTING_DIALOG_RADIO_FINISH_NOTIFICATION));
                        }
                    }
                });
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            DialogRadioBean bean = mData.get(arg0);
            vh.textView.setText(bean.name);
            vh.checkBox.setTag(arg0);
            vh.checkBox.setChecked(bean.isCheck);
            return convertView;
        }
    }

    // 滑动时机对话框
    private void showSlideShowTimeSettingDialog() {
        if (mSlideTimeDialog == null) {
            mSlideTimeDialog = new QuickGestureSlideTimeDialog(this);
        }
        mSlideTimeDialog.setFreeDisturbVisibility(true);
        mSlideTimeDialog
                .setFreeDisturbText(R.string.pg_appmanager_quick_gesture_slide_time_no_disturb_text);
        mSlideTimeDialog.setTitle(R.string.pg_appmanager_quick_gesture_option_able_sliding_time);
        mFreeApps = getFreeDisturbApps();
        mSlideTimeAdapter = new FreeDisturbSlideTimeAdapter(this,
                mFreeApps);
        if (mFreeApps != null && mFreeApps.size() > 1) {
            mSlideTimeDialog.setFreeDisturbAppAddBtVisVisibility(false);
            mSlideTimeDialog.setFreeDisturbAppHorizontalVisVisibility(true);
        } else {
            mSlideTimeDialog.setFreeDisturbAppAddBtVisVisibility(true);
            mSlideTimeDialog.setFreeDisturbAppHorizontalVisVisibility(false);
        }
        mSlideTimeDialog.setFreeDisturbAdapter(mSlideTimeAdapter);

        mSlideTimeDialog.setRightBtnListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                AppMasterPreference.getInstance(QuickGestureActivity.this).setSlideTimeJustHome(
                        mSlideTimeDialog.getJustHometCheckStatus());
                AppMasterPreference.getInstance(QuickGestureActivity.this)
                        .setSlideTimeAllAppAndHome(mSlideTimeDialog.getAppHomeCheckStatus());
                mSlideTimeDialog.dismiss();
                LeoEventBus
                        .getDefaultBus()
                        .post(new QuickGestureFloatWindowEvent(
                                FloatWindowHelper.QUICK_GESTURE_SETTING_DIALOG_RADIO_SLIDE_TIME_SETTING_FINISH_NOTIFICATION));
            }
        });
        mSlideTimeDialog.setFreeDisturbAppAddBtClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showAllAppDialog();
            }
        });
        mSlideTimeDialog.setOnItemListenerFreeDisturb(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (arg2 == 0) {
                    showAllAppDialog();
                }
            }
        });
        mSlideTimeDialog.setOnLongClickListenerFreeDisturb(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                getEditFreeDisturbAppInfo(true);
                mSlideTimeAdapter.notifyDataSetChanged();
                return false;
            }
        });
        mSlideTimeDialog.setLeftButtonClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mSlideTimeDialog.dismiss();
            }
        });
        mSlideTimeDialog.show();
    }

    private void getEditFreeDisturbAppInfo(boolean flag) {
        if (mFreeApps != null && mFreeApps.size() > 0) {
            for (QuickGsturebAppInfo freeDisturbAppInfo : mFreeApps) {
                String pageName = freeDisturbAppInfo.packageName;
                if (!"add_free_app".equals(pageName)) {
                    freeDisturbAppInfo.isEditFreeDisturb = flag;
                }
            }
        }
    }

    // 免打扰应用列表
    private void showAllAppDialog() {
        final QuickGestureFreeDisturbAppDialog freeDisturbApp = new QuickGestureFreeDisturbAppDialog(
                this, 1);
        freeDisturbApp.setTitle(R.string.pg_appmanager_quick_gesture_select_free_disturb_app_text);
        freeDisturbApp.setRightBt(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (freeDisturbApp != null) {
                    // 添加确认免打扰应用
                    final List<BaseInfo> addFreeAppNames = freeDisturbApp.getAddFreePackageName();
                    final List<BaseInfo> removeFreeAppNames = freeDisturbApp
                            .getRemoveFreePackageName();
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                        @Override
                        public void run() {
                            if (addFreeAppNames != null && addFreeAppNames.size() > 0) {
                                for (BaseInfo object : addFreeAppNames) {
                                    QuickGsturebAppInfo string = (QuickGsturebAppInfo) object;
                                    AppMasterPreference.getInstance(QuickGestureActivity.this)
                                            .setFreeDisturbAppPackageNameAdd(string.packageName);
                                }
                            }
                            if (removeFreeAppNames != null && removeFreeAppNames.size() > 0) {
                                for (Object object : removeFreeAppNames) {
                                    QuickGsturebAppInfo string = (QuickGsturebAppInfo) object;
                                    AppMasterPreference.getInstance(QuickGestureActivity.this)
                                            .setFreeDisturbAppPackageNameRemove(string.packageName);
                                }
                            }
                        }
                    });
                    freeDisturbApp.dismiss();
                    LeoEventBus.getDefaultBus().post(
                            new QuickGestureFloatWindowEvent(
                                    FloatWindowHelper.QUICK_GESTURE_ADD_FREE_DISTURB_NOTIFICATION));
                }
            }
        });
        freeDisturbApp.setLeftBt(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                freeDisturbApp.dismiss();
            }
        });
        freeDisturbApp.show();
    }

    // 获取免干扰应用
    private List<QuickGsturebAppInfo> getFreeDisturbApps() {
        List<QuickGsturebAppInfo> freeDisturbApp = new ArrayList<QuickGsturebAppInfo>();
        // 添加Item
        QuickGsturebAppInfo addImageInfo = new QuickGsturebAppInfo();
        addImageInfo.icon = this.getResources().getDrawable(R.drawable.switch_add_block);
        addImageInfo.packageName = "add_free_app";
        freeDisturbApp.add(addImageInfo);
        List<String> packageNames = null;
        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(this)
                .getAllPkgInfo();
        packageNames = QuickGestureManager.getInstance(this).getFreeDisturbAppName();
        for (AppItemInfo appDetailInfo : list) {
            QuickGsturebAppInfo appInfo = new QuickGsturebAppInfo();
            appInfo.icon = appDetailInfo.icon;
            appInfo.packageName = appDetailInfo.packageName;
            appInfo.label = appDetailInfo.label;
            if (packageNames != null) {
                if (packageNames.contains(appDetailInfo.packageName)) {
                    appInfo.isFreeDisturb = true;
                    freeDisturbApp.add(appInfo);
                }
            }
        }
        return freeDisturbApp;
    }

    private class FreeDisturbSlideTimeAdapter extends BaseAdapter {
        List<QuickGsturebAppInfo> mFreeDisturbApps = null;
        LayoutInflater mInflater;

        public FreeDisturbSlideTimeAdapter(Context context, List<QuickGsturebAppInfo> mApps) {
            mFreeDisturbApps = mApps;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mFreeDisturbApps.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mFreeDisturbApps.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        class ViewHolder {
            ImageView imageView, deleteImageView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup arg2) {
            ViewHolder vh = null;
            if (vh == null) {
                vh = new ViewHolder();
                convertView = mInflater.inflate(R.layout.free_disturb_horizontal_item, null);
                vh.imageView = (ImageView) convertView.findViewById(R.id.free_app_icon_iv);
                vh.deleteImageView = (ImageView) convertView
                        .findViewById(R.id.delete_free_app_icon_iv);
                vh.deleteImageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        QuickGsturebAppInfo infoTag = (QuickGsturebAppInfo) arg0.getTag();
                        if (mFreeApps != null && mFreeApps.size() > 0) {
                            mFreeApps.remove(infoTag);
                            AppMasterPreference.getInstance(QuickGestureActivity.this)
                                    .setFreeDisturbAppPackageNameRemove(infoTag.packageName);
                            mSlideTimeAdapter.notifyDataSetChanged();
                        }
                        if (mFreeApps == null || mFreeApps.size() <= 1) {
                            if (mSlideTimeDialog != null) {
                                mSlideTimeDialog.setFreeDisturbAppAddBtVisVisibility(true);
                                mSlideTimeDialog.setFreeDisturbAppHorizontalVisVisibility(false);
                            }
                        }
                    }
                });
            }
            QuickGsturebAppInfo info = mFreeDisturbApps.get(position);
            vh.imageView.setImageDrawable(info.icon);
            if (info.isEditFreeDisturb) {
                vh.deleteImageView.setVisibility(View.VISIBLE);
                vh.deleteImageView.setTag(info);
            } else {
                vh.deleteImageView.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int width = view.getWidth();
        int height = view.getHeight();
        float downX = 0;
        float downY = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = Math.abs(event.getX() - downX);
                float moveY = Math.abs(event.getY() - downY);

                if (moveX > width / 50 || moveY > width / 50) {
                    if (!mFlag) {
                        mTipRL.clearAnimation();
                        mTipRL.setVisibility(View.GONE);
                        String toastText = QuickGestureActivity.this.getResources().getString(
                                R.string.quick_gesture_first_open_sliding_toast);
                        Toast.makeText(this, toastText, Toast.LENGTH_SHORT)
                                .show();
                        Intent intent;
                        intent = new Intent(AppMasterApplication.getInstance(),
                                QuickGesturePopupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("show_orientation", 2);
                        try {
                            AppMasterApplication.getInstance().startActivity(intent);
                            FloatWindowHelper.mGestureShowing = true;
                            AppMasterPreference.getInstance(this).setFristSlidingTip(true);
                        } catch (Exception e) {
                        }
                        mFlag = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return true;
    }

    private void quickTipAnim(View view) {
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        alpha.setDuration(1000);
        ScaleAnimation scale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
        scale.setDuration(1000);
        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(alpha);
        animation.addAnimation(scale);
        view.setAnimation(animation);
        animation.start();
    }

    private void gestureTranslationAnim(final View view1, final View view2) {
        view1.clearAnimation();
        view2.clearAnimation();
        AnimatorSet animatorSet = new AnimatorSet();
        // 箭头动画
        ObjectAnimator alphaArrow = ObjectAnimator.ofFloat(view2, "alpha", 0, 0, 1);
        alphaArrow.setDuration(2000);
        alphaArrow.setRepeatCount(-1);
        PropertyValuesHolder arrowHolderX = PropertyValuesHolder
                .ofFloat("translationX", 0, 0, -200);
        PropertyValuesHolder arrowHolderY = PropertyValuesHolder
                .ofFloat("translationY", 0, 0, -200);
        ObjectAnimator translateArrow = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
                view2, arrowHolderX, arrowHolderY);
        translateArrow.setDuration(2000);
        translateArrow.setRepeatCount(-1);
        // 手势动画
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view1, "alpha", 0, 1, 1);
        alpha.setDuration(2000);
        alpha.setRepeatCount(-1);
        PropertyValuesHolder valuesHolderX = PropertyValuesHolder
                .ofFloat("translationX", 0, 270, 0);
        PropertyValuesHolder valuesHolderY = PropertyValuesHolder
                .ofFloat("translationY", 0, 300, 0);
        ObjectAnimator translate = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(view1,
                valuesHolderX, valuesHolderY);
        translate.setRepeatCount(-1);
        translate.setInterpolator(new AccelerateDecelerateInterpolator());
        translate.setDuration(2000);
        animatorSet.playTogether(translate, alpha, alphaArrow, translateArrow);
        animatorSet.start();
    }
}
