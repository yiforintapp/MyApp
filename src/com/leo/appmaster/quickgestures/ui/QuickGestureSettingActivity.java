
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity.NameComparator;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.ui.QuickGestureRadioSeekBarDialog.OnDiaogClickListener;
import com.leo.appmaster.quickgestures.ui.QuickGestureSlideTimeDialog.UpdateFilterAppClickListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;

/**
 * QuickGestureActivity
 * 
 * @author run
 */
public class QuickGestureSettingActivity extends BaseActivity implements  OnClickListener,
        UpdateFilterAppClickListener {
    private CommonTitleBar mTitleBar;
    private AppMasterPreference mPre;
    private QuickGestureRadioSeekBarDialog mAlarmDialog;
    private QuickGestureSlideTimeDialog mSlideTimeDialog;
    public static boolean mAlarmDialogFlag = false;
    private List<QuickGsturebAppInfo> mFreeApps;
    private FreeDisturbSlideTimeAdapter mSlideTimeAdapter;
    private TextView mSlidingTimeTv, mSlidAreaTv;
    private RelativeLayout   mSlidingArea, mSlidingTime, mNoReadMessageOpen,
            mRecentlyContactOPen, mPrivacyContactOpen, mActivityRootView,mStrengthenModeView;
    private ImageView  mNoReadMessageOpenCK,
            mRecentlyContactOpenCK, mPrivacyContactOpenCK,mStrengthModeOpenCk;
    private boolean  mNoReadMessageFlag, mRecentlyContactFlag,
            mPrivacyContactFlag,mStrengthenModeFlag;
    private String slidingArea = "";
    public static final String FROME_STATUSBAR = "from_statusbar";
    public static boolean isSureBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_quick_gesture);
        mPre = AppMasterPreference.getInstance(this);
        initUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPre.getSwitchOpenQuickGesture()) {
            initChexkBox();
            setOnClickListener();
        } else {
            unSetOnClickListener();
            closeQuickSetting();
        }
        initSlidingAreaText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initUi() {
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_quick_gesture_title_bar);
        mTitleBar.openBackView();
        mTitleBar.setTitle(R.string.pg_appmanager_quick_gesture_name);
        mSlidingArea = (RelativeLayout) findViewById(R.id.slid_area);
        mSlidingTime = (RelativeLayout) findViewById(R.id.allow_slid_time);
        mNoReadMessageOpen = (RelativeLayout) findViewById(R.id.no_read_message_content);
        mRecentlyContactOPen = (RelativeLayout) findViewById(R.id.recently_contact_content);
        mPrivacyContactOpen = (RelativeLayout) findViewById(R.id.privacy_contact_content);
        mStrengthenModeView = (RelativeLayout)findViewById(R.id.strengthen_slid_mode);
        mSlidingTimeTv = (TextView) findViewById(R.id.allow_slid_time_item_cotentTV);
        mSlidAreaTv = (TextView) findViewById(R.id.slid_area_item_cotentTV);
        mActivityRootView = (RelativeLayout) findViewById(R.id.quick_gesture_seting);
        mNoReadMessageOpenCK = (ImageView) findViewById(R.id.no_read_message_check);
        mRecentlyContactOpenCK = (ImageView) findViewById(R.id.recently_contact_check);
        mPrivacyContactOpenCK = (ImageView) findViewById(R.id.privacy_contact_check);
        mStrengthModeOpenCk = (ImageView)findViewById(R.id.strengthen_mode_switch_check);
    }

    private void initSlidingAreaText() {
        /**
         * init sliding time
         */
        // just home
        if (mPre.getSlideTimeJustHome()) {
            mSlidingTimeTv
                    .setText(R.string.pg_appmanager_quick_gesture_slide_time_just_home_text);
        }
        // home and all app
        if (mPre.getSlideTimeAllAppAndHome()) {
            mSlidingTimeTv
                    .setText(R.string.pg_appmanager_quick_gesture_slide_time_home_and_all_app_text);
        }
        setSlidingAreaSetting();
    }

    private void setSlidingAreaSetting() {
        /**
         * init sliding area
         */
        mSlidAreaTv.setText(getSlidingAreaShowString());
        SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting", slidingArea);
    }

    private String getSlidingAreaShowString() {
        StringBuilder sb = new StringBuilder();
        if (mPre.getDialogRadioLeftBottom()) {
            sb.append(this.getResources().getString(
                    R.string.pg_appmanager_quick_gesture_option_dialog_radio_left_bottom_text)
                    + ",");
            slidingArea = slidingArea + "+leftd";
        }
        if (mPre.getDialogRadioRightBottom()) {
            sb.append(this.getResources().getString(
                    R.string.pg_appmanager_quick_gesture_option_dialog_radio_right_bottom_text)
                    + ",");
            slidingArea = slidingArea + "+rightd";
        }
        if (mPre.getDialogRadioLeftCenter()) {
            sb.append(this.getResources().getString(
                    R.string.pg_appmanager_quick_gesture_option_dialog_radio_left_center_text)
                    + ",");
            slidingArea = slidingArea + "+leftm";
        }
        if (mPre.getDialogRadioRightCenter()) {
            sb.append(this.getResources().getString(
                    R.string.pg_appmanager_quick_gesture_option_dialog_radio_right_center_text)
                    + ",");
            slidingArea = slidingArea + "+rightm";
        }

        if (sb != null && sb.length() > 0) {
            sb.setCharAt(sb.length() - 1, ' ');
        }
        return sb.toString();
    }

    private void initChexkBox() {
        mNoReadMessageFlag = mPre.getSwitchOpenNoReadMessageTip();
        mRecentlyContactFlag = mPre.getSwitchOpenRecentlyContact();
        mPrivacyContactFlag = mPre.getSwitchOpenPrivacyContactMessageTip();
        mStrengthenModeFlag = mPre.getSwitchOpenStrengthenMode();
        
        // no read message switch
        if (mNoReadMessageFlag) {
            mNoReadMessageOpenCK.setImageResource(R.drawable.switch_on);
        } else {
            mNoReadMessageOpenCK.setImageResource(R.drawable.switch_off);
        }
        // recently contact swtich
        if (mRecentlyContactFlag) {
            mRecentlyContactOpenCK.setImageResource(R.drawable.switch_on);
        } else {
            mRecentlyContactOpenCK.setImageResource(R.drawable.switch_off);
        }
        // privacy contact switch
        if (mPrivacyContactFlag) {
            mPrivacyContactOpenCK.setImageResource(R.drawable.switch_on);
        } else {
            mPrivacyContactOpenCK.setImageResource(R.drawable.switch_off);
        }
        //strengthen mode
        if(mStrengthenModeFlag){
            mStrengthModeOpenCk.setImageResource(R.drawable.switch_on);
        }else {
            mStrengthModeOpenCk.setImageResource(R.drawable.switch_off);
        }
    }

    private void closeQuickSetting() {
        if (mNoReadMessageOpenCK != null) {
            mNoReadMessageOpenCK.setImageResource(R.drawable.switch_off);
        }
        if (mRecentlyContactOpenCK != null) {
            mRecentlyContactOpenCK.setImageResource(R.drawable.switch_off);
        }
        if (mPrivacyContactOpenCK != null) {
            mPrivacyContactOpenCK.setImageResource(R.drawable.switch_off);
        }
        if(mStrengthModeOpenCk != null){
            mStrengthModeOpenCk.setImageResource(R.drawable.switch_off);
        }
    }

    private void setOnClickListener() {
        mSlidingArea.setOnClickListener(this);
        mSlidingTime.setOnClickListener(this);
        mNoReadMessageOpen.setOnClickListener(this);
        mRecentlyContactOPen.setOnClickListener(this);
        mPrivacyContactOpen.setOnClickListener(this);
        mStrengthenModeView.setOnClickListener(this);
    }

    private void unSetOnClickListener() {
        mSlidingArea.setOnClickListener(null);
        mSlidingTime.setOnClickListener(null);
        mNoReadMessageOpen.setOnClickListener(null);
        mRecentlyContactOPen.setOnClickListener(null);
        mPrivacyContactOpen.setOnClickListener(null);
        mStrengthenModeView.setOnClickListener(null);
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

    class DialogRadioBean {
        String name;
        boolean isCheck;
    }

    // sliding area setting dialog
    private void showSettingDialog(boolean flag) {
        if (mAlarmDialog == null) {
            mAlarmDialog = new QuickGestureRadioSeekBarDialog(this);
        }
        mAlarmDialog.setShowRadioListView(flag);
        mAlarmDialog
                .setTitle(R.string.pg_appmanager_quick_gesture_option_sliding_area_location_title);
        mAlarmDialog.setSeekBarTextVisibility(false);
        mAlarmDialog.setSeekbarTextProgressVisibility(false);
        mAlarmDialog.setSeekBarProgressValue(mPre.getQuickGestureDialogSeekBarValue());
        mAlarmDialog.setLeftBottomOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // left bottom
                boolean leftBottomStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftBottom;
                if (leftBottomStatus) {
                    mAlarmDialog.setLeftBottomBackgroud(QuickGestureSettingActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom = false;
                } else {
                    mAlarmDialog.setLeftBottomBackgroud(QuickGestureSettingActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom = true;
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureSettingActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_LEFT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setRightBottomOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // right bottom
                boolean rightBottomStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightBottom;
                if (rightBottomStatus) {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom = false;
                    mAlarmDialog.setRightBottomBackgroud(QuickGestureSettingActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom = true;
                    mAlarmDialog.setRightBottomBackgroud(QuickGestureSettingActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureSettingActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_RIGHT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setLeftCenterOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // left center
                boolean leftCenterStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftCenter;
                if (leftCenterStatus) {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter = false;
                    mAlarmDialog.setLeftCenterBackgroud(QuickGestureSettingActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter = true;
                    mAlarmDialog.setLeftCenterBackgroud(QuickGestureSettingActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureSettingActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_LEFT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setRightCenterOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // right center
                boolean rightCenterStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightCenter;
                if (rightCenterStatus) {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter = false;
                    mAlarmDialog.setRightCenterBackgroud(QuickGestureSettingActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter = true;
                    mAlarmDialog.setRightCenterBackgroud(QuickGestureSettingActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureSettingActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_RIGHT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {

            @Override
            public void onClick(int progress) {
                isSureBt = true;
                boolean mLeftBottom = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftBottom;
                boolean mRightBottm = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightBottom;
                boolean mLeftCenter = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftCenter;
                boolean mRightCenter = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightCenter;
                if (mLeftBottom || mRightBottm || mLeftCenter || mRightCenter) {
                    // save progress value
                    mPre.setQuickGestureDialogSeekBarValue(QuickGestureManager
                            .getInstance(getApplicationContext()).mSlidAreaSize);
                    // save sliding area value
                    mPre.setDialogRadioLeftBottom(mLeftBottom);
                    mPre.setDialogRadioRightBottom(mRightBottm);
                    mPre.setDialogRadioLeftCenter(mLeftCenter);
                    mPre.setDialogRadioRightCenter(mRightCenter);
                    QuickGestureManager.getInstance(QuickGestureSettingActivity.this).resetSlidAreaSize();
                    // update area background color
                    updateFloatWindowBackGroudColor();
                    setSlidingAreaSetting();
                    if (mAlarmDialog != null) {
                        mAlarmDialog.dismiss();
                        // FloatWindowHelper.mEditQuickAreaFlag = false;
                        // mAlarmDialogFlag = false;
                        // updateFloatWindowBackGroudColor();
                    }
                } else {
                    Toast.makeText(
                            QuickGestureSettingActivity.this,
                            QuickGestureSettingActivity.this
                                    .getResources()
                                    .getString(
                                            R.string.pg_appmanager_quick_gesture_option_dialog_radio_toast_text),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAlarmDialog.setCancelable(true);
        mAlarmDialog.show();
        mAlarmDialogFlag = true;
        updateFloatWindowBackGroudColor();
    }

    // update backgroud color
    private void updateFloatWindowBackGroudColor() {
        FloatWindowHelper
                .updateFloatWindowBackgroudColor(this, FloatWindowHelper.mEditQuickAreaFlag);
        // FloatWindowHelper.createFloatWindow(QuickGestureActivity.this,
        // AppMasterPreference
        // .getInstance(getApplicationContext()).getQuickGestureDialogSeekBarValue());
    }

    // sliding time setting diallg
    private void showSlideShowTimeSettingDialog() {
        if (mSlideTimeDialog == null) {
            mSlideTimeDialog = new QuickGestureSlideTimeDialog(this);
        }
        mSlideTimeDialog.setFreeDisturbVisibility(true);
        mSlideTimeDialog
                .setFreeDisturbText(R.string.pg_appmanager_quick_gesture_slide_time_no_disturb_text);
        mSlideTimeDialog.setTitle(R.string.pg_appmanager_quick_gesture_option_able_sliding_time);
        if (mFreeApps != null) {
            mFreeApps.clear();
        }
        mFreeApps = getFreeDisturbApps();
        Collections.sort(mFreeApps, new NameComparator());
        mSlideTimeAdapter = new FreeDisturbSlideTimeAdapter(this,
                mFreeApps);
        mSlideTimeDialog.setUpdateFilterAppClickListener(this);
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
                // just home
                if (mSlideTimeDialog.getJustHometCheckStatus()) {
                    SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                            "slidetime_launcher");
                    mSlidingTimeTv
                            .setText(R.string.pg_appmanager_quick_gesture_slide_time_just_home_text);
                }
                // home and all app
                if (mSlideTimeDialog.getAppHomeCheckStatus()) {
                    SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                            "launcher+apps");
                    mSlidingTimeTv
                            .setText(R.string.pg_appmanager_quick_gesture_slide_time_home_and_all_app_text);
                }
                AppMasterPreference.getInstance(QuickGestureSettingActivity.this).setSlideTimeJustHome(
                        mSlideTimeDialog.getJustHometCheckStatus());
                AppMasterPreference.getInstance(QuickGestureSettingActivity.this)
                        .setSlideTimeAllAppAndHome(mSlideTimeDialog.getAppHomeCheckStatus());
                // update catch value
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isJustHome = mSlideTimeDialog
                        .getJustHometCheckStatus();
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isAppsAndHome = mSlideTimeDialog
                        .getAppHomeCheckStatus();
                mSlideTimeDialog.dismiss();
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
                mSlideTimeDialog.setIsEdit(true);
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

    // filter app list dialog
    private void showAllAppDialog() {
        final QuickGestureFilterAppDialog freeDisturbApp = new QuickGestureFilterAppDialog(
                this, 1);
        freeDisturbApp.setTitle(R.string.pg_appmanager_quick_gesture_select_free_disturb_app_text);
        freeDisturbApp.setRightBt(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (freeDisturbApp != null) {
                    // add filter app
                    final List<BaseInfo> addFreeAppNames = freeDisturbApp.getAddFreePackageName();
                    final List<BaseInfo> removeFreeAppNames = freeDisturbApp
                            .getRemoveFreePackageName();

                    if (addFreeAppNames != null && addFreeAppNames.size() > 0) {
                        for (BaseInfo object : addFreeAppNames) {
                            QuickGsturebAppInfo string = (QuickGsturebAppInfo) object;
                            AppMasterPreference.getInstance(QuickGestureSettingActivity.this)
                                    .setFreeDisturbAppPackageNameAdd(string.packageName);
                            SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1,
                                    "qssetting", "noslideapps_" + string.packageName);
                        }
                    }
                    if (removeFreeAppNames != null && removeFreeAppNames.size() > 0) {
                        for (Object object : removeFreeAppNames) {
                            QuickGsturebAppInfo string = (QuickGsturebAppInfo) object;
                            AppMasterPreference.getInstance(QuickGestureSettingActivity.this)
                                    .setFreeDisturbAppPackageNameRemove(string.packageName);
                        }
                    }
                    freeDisturbApp.dismiss();
                }
                showSlideShowTimeSettingDialog();
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

    // get filter apps
    private List<QuickGsturebAppInfo> getFreeDisturbApps() {
        List<QuickGsturebAppInfo> freeDisturbApp = new ArrayList<QuickGsturebAppInfo>();
        // add item
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

        @SuppressLint("InflateParams")
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
                            AppMasterPreference.getInstance(QuickGestureSettingActivity.this)
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

    
    // 计算根布局与屏幕高的差值
    private int screenSpace() {
        @SuppressWarnings("deprecation")
        int height = ((WindowManager) QuickGestureSettingActivity.this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
        int rootHeight = mActivityRootView.getRootView().getHeight();
        if (mActivityRootView != null) {
            int temp = Math.abs(rootHeight - height);
            return temp;
        } else {
            return 0;
        }
    }

    @Override
    public void onClick(View arg0) {
        int flag = arg0.getId();
        switch (flag) {
            case R.id.slid_area:
                SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                        "area_cli");
                FloatWindowHelper.mEditQuickAreaFlag = true;
                showSettingDialog(true);
                AppMasterPreference.getInstance(QuickGestureSettingActivity.this)
                        .setRootViewAndWindowHeighSpace(screenSpace());
                QuickGestureManager.getInstance(QuickGestureSettingActivity.this).screenSpace = screenSpace();
                mActivityRootView.getViewTreeObserver()
                        .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                AppMasterPreference.getInstance(QuickGestureSettingActivity.this)
                                        .setRootViewAndWindowHeighSpace(screenSpace());
                                QuickGestureManager.getInstance(QuickGestureSettingActivity.this).screenSpace = screenSpace();
                                int value = QuickGestureManager
                                        .getInstance(getApplicationContext()).mSlidAreaSize;
                                FloatWindowHelper.updateView(QuickGestureSettingActivity.this, value);
                            }
                        });
                break;
            case R.id.allow_slid_time:
                SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                        "slidetime");
                showSlideShowTimeSettingDialog();
                break;
            case R.id.no_read_message_content:
                if (!mNoReadMessageFlag) {
                    SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                            "message_open");
                    mPre.setSwitchOpenNoReadMessageTip(true);
                    mNoReadMessageOpenCK.setImageResource(R.drawable.switch_on);
                    mNoReadMessageFlag = true;
                    // checkout system database no read message
                    if (QuickGestureManager.getInstance(QuickGestureSettingActivity.this).isMessageRead) {
                        QuickGestureManager.getInstance(QuickGestureSettingActivity.this).isMessageRead = false;
                        AppMasterPreference.getInstance(QuickGestureSettingActivity.this)
                                .setMessageIsRedTip(false);
                    }
                    checkNoReadMessage();
                } else {
                    SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                            "message_close");
                    mPre.setSwitchOpenNoReadMessageTip(false);
                    mNoReadMessageOpenCK.setImageResource(R.drawable.switch_off);
                    mNoReadMessageFlag = false;
                }
                break;
            case R.id.recently_contact_content:
                if (!mRecentlyContactFlag) {
                    SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                            "recent_open");
                    mPre.setSwitchOpenRecentlyContact(true);
                    mRecentlyContactOpenCK.setImageResource(R.drawable.switch_on);
                    mRecentlyContactFlag = true;
                    checkNoReadCallLog();
                } else {
                    SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                            "recent_close");
                    mPre.setSwitchOpenRecentlyContact(false);
                    mRecentlyContactOpenCK.setImageResource(R.drawable.switch_off);
                    mRecentlyContactFlag = false;
                }
                break;
            case R.id.privacy_contact_content:
                if (!mPrivacyContactFlag) {
                    SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                            "primessage_open");
                    mPre.setSwitchOpenPrivacyContactMessageTip(true);
                    mPrivacyContactOpenCK.setImageResource(R.drawable.switch_on);
                    mPrivacyContactFlag = true;
                } else {
                    SDKWrapper.addEvent(QuickGestureSettingActivity.this, SDKWrapper.P1, "qssetting",
                            "primessage_close");
                    mPre.setSwitchOpenPrivacyContactMessageTip(false);
                    mPrivacyContactOpenCK.setImageResource(R.drawable.switch_off);
                    mPrivacyContactFlag = false;
                }
                break;
            case R.id.strengthen_slid_mode:
                if(mStrengthenModeFlag){
                    mPre.setEverCloseWhiteDot(true);
                    mPre.setSwitchOpenStrengthenMode(false);
                    mStrengthenModeFlag = false;
                    mStrengthModeOpenCk.setImageResource(R.drawable.switch_off);
                }else {
                    mPre.setSwitchOpenStrengthenMode(true);
                    mStrengthenModeFlag = true;
                    mStrengthModeOpenCk.setImageResource(R.drawable.switch_on);
                }
                switchStrengthMode();
                break;
        }

    }

    @Override
    public void updateFilterAppClickListener() {
        getEditFreeDisturbAppInfo(false);
        mSlideTimeAdapter.notifyDataSetChanged();
    }

    private void checkNoReadMessage() {
        AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                QuickGestureManager.getInstance(QuickGestureSettingActivity.this).mMessages = PrivacyContactUtils
                        .getSysMessage(QuickGestureSettingActivity.this,
                                QuickGestureSettingActivity.this.getContentResolver(),
                                "read=0 AND type=1", null, false);
                if (QuickGestureManager.getInstance(QuickGestureSettingActivity.this).mMessages != null
                        && QuickGestureManager.getInstance(QuickGestureSettingActivity.this).mMessages
                                .size() > 0) {
                    QuickGestureManager.getInstance(QuickGestureSettingActivity.this).isShowSysNoReadMessage = true;
                    FloatWindowHelper
                            .removeShowReadTipWindow(QuickGestureSettingActivity.this);
                }
            }
        });
    }

    private void checkNoReadCallLog() {
        AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

            @Override
            public void run() {
                String selection = Calls.TYPE + "=? and " + Calls.NEW + "=?";
                String[] selectionArgs = new String[] {
                        String.valueOf(Calls.MISSED_TYPE), String.valueOf(1)
                };
                QuickGestureManager.getInstance(QuickGestureSettingActivity.this).mCallLogs = PrivacyContactUtils
                        .getSysCallLog(QuickGestureSettingActivity.this,
                                QuickGestureSettingActivity.this.getContentResolver(),
                                selection,
                                selectionArgs);
                if (QuickGestureManager.getInstance(QuickGestureSettingActivity.this).mCallLogs != null
                        && QuickGestureManager.getInstance(QuickGestureSettingActivity.this).mCallLogs
                                .size() > 0) {
                    QuickGestureManager.getInstance(QuickGestureSettingActivity.this).isShowSysNoReadMessage = true;
                    FloatWindowHelper
                            .removeShowReadTipWindow(QuickGestureSettingActivity.this);
                }
            }
        });
    }

    
    /**
     * switch strength mode open
     */
    private void switchStrengthMode(){
        if(mStrengthenModeFlag){
            FloatWindowHelper.createWhiteFloatView(this);
        }else{
            FloatWindowHelper.removeWhiteFloatView(this);
            mPre.setWhiteFloatViewCoordinate(0, 0);
        }
    }
}
