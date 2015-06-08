
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.util.Log;
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
import com.leo.appmaster.quickgestures.ui.QuickGestureSlideTimeDialog.UpdateFilterAppClickListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;

/**
 * QuickGestureActivity
 * 
 * @author run
 */
public class QuickGestureActivity extends BaseActivity implements OnTouchListener, OnClickListener,
        UpdateFilterAppClickListener {
    private CommonTitleBar mTitleBar;
    private List<QuickGestureSettingBean> mQuickGestureSettingOption;
    private AppMasterPreference mPre;
    private QuickGestureRadioSeekBarDialog mAlarmDialog;
    private QuickGestureSlideTimeDialog mSlideTimeDialog;
    private boolean mAlarmDialogFlag = false;
    private List<QuickGsturebAppInfo> mFreeApps;
    private FreeDisturbSlideTimeAdapter mSlideTimeAdapter;
    private TextView mLeftTopView, mLeftBottomView, mRightTopView, mRightBottomView,
            mSlidingTimeTv, mSlidAreaTv;
    private RelativeLayout mTipRL, mOpenQuick, mSlidingArea, mSlidingTime, mNoReadMessageOpen,
            mRecentlyContactOPen, mPrivacyContactOpen;
    private ImageView mHandImage, mArrowImage, mQuickOpenCK, mNoReadMessageOpenCK,
            mRecentlyContactOpenCK, mPrivacyContactOpenCK;
    private boolean mFlag, mOpenQuickFlag, mNoReadMessageFlag, mRecentlyContactFlag,
            mPrivacyContactFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_gesture);
        mPre = AppMasterPreference.getInstance(this);
        initUi();
        // LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    private void initUi() {
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_quick_gesture_title_bar);
        mTitleBar.openBackView();
        mTipRL = (RelativeLayout) findViewById(R.id.quick_tipRL);
        mLeftTopView = (TextView) findViewById(R.id.gesture_left_tips_top_tv);
        mLeftBottomView = (TextView) findViewById(R.id.gesture_left_tips_bottom);
        mRightTopView = (TextView) findViewById(R.id.gesture_right_tips_top_tv);
        mRightBottomView = (TextView) findViewById(R.id.gesture_right_tips_bottom);
        mHandImage = (ImageView) findViewById(R.id.gesture_handIV);
        mArrowImage = (ImageView) findViewById(R.id.gesture_arrowIV);
        mOpenQuick = (RelativeLayout) findViewById(R.id.open_quick);
        mSlidingArea = (RelativeLayout) findViewById(R.id.slid_area);
        mSlidingTime = (RelativeLayout) findViewById(R.id.allow_slid_time);
        mNoReadMessageOpen = (RelativeLayout) findViewById(R.id.no_read_message_content);
        mRecentlyContactOPen = (RelativeLayout) findViewById(R.id.recently_contact_content);
        mPrivacyContactOpen = (RelativeLayout) findViewById(R.id.privacy_contact_content);
        mQuickOpenCK = (ImageView) findViewById(R.id.open_quick_gesture_check);
        mNoReadMessageOpenCK = (ImageView) findViewById(R.id.no_read_message_check);
        mRecentlyContactOpenCK = (ImageView) findViewById(R.id.recently_contact_check);
        mPrivacyContactOpenCK = (ImageView) findViewById(R.id.privacy_contact_check);
        mSlidingTimeTv = (TextView) findViewById(R.id.allow_slid_time_item_cotentTV);
        mSlidAreaTv = (TextView) findViewById(R.id.slid_area_item_cotentTV);
        initQuickGestureOpen();
        if (mPre.getSwitchOpenQuickGesture()) {
            initChexkBox();
            setOnClickListener();
        }
        initSlidingAreaText();
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

    private void initQuickGestureOpen() {
        // open quick gesture
        mOpenQuick.setOnClickListener(this);
        mOpenQuickFlag = mPre.getSwitchOpenQuickGesture();
        if (mOpenQuickFlag) {
            mQuickOpenCK.setImageResource(R.drawable.switch_on);
        } else {
            mQuickOpenCK.setImageResource(R.drawable.switch_off);
        }
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
    }

    private String getSlidingAreaShowString() {
        StringBuilder sb = new StringBuilder();
        if (mPre.getDialogRadioLeftBottom()) {
            sb.append(this.getResources().getString(
                    R.string.pg_appmanager_quick_gesture_option_dialog_radio_left_bottom_text)
                    + ",");
        }
        if (mPre.getDialogRadioRightBottom()) {
            sb.append(this.getResources().getString(
                    R.string.pg_appmanager_quick_gesture_option_dialog_radio_right_bottom_text)
                    + ",");
        }
        if (mPre.getDialogRadioLeftCenter()) {
            sb.append(this.getResources().getString(
                    R.string.pg_appmanager_quick_gesture_option_dialog_radio_left_center_text)
                    + ",");
        }
        if (mPre.getDialogRadioRightCenter()) {
            sb.append(this.getResources().getString(
                    R.string.pg_appmanager_quick_gesture_option_dialog_radio_right_center_text)
                    + ",");
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
    }

    private void closeQuickSetting() {
        // TODO Auto-generated method stub
        if (mNoReadMessageOpenCK != null) {
            mNoReadMessageOpenCK.setImageResource(R.drawable.switch_off);
        }
        if (mRecentlyContactOpenCK != null) {
            mRecentlyContactOpenCK.setImageResource(R.drawable.switch_off);
        }
        if (mPrivacyContactOpenCK != null) {
            mPrivacyContactOpenCK.setImageResource(R.drawable.switch_off);
        }
    }

    private void setOnClickListener() {
        mSlidingArea.setOnClickListener(this);
        mSlidingTime.setOnClickListener(this);
        mNoReadMessageOpen.setOnClickListener(this);
        mRecentlyContactOPen.setOnClickListener(this);
        mPrivacyContactOpen.setOnClickListener(this);
    }

    private void unSetOnClickListener() {
        mSlidingArea.setOnClickListener(null);
        mSlidingTime.setOnClickListener(null);
        mNoReadMessageOpen.setOnClickListener(null);
        mRecentlyContactOPen.setOnClickListener(null);
        mPrivacyContactOpen.setOnClickListener(null);
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
        Drawable selectIcon = this.getResources().getDrawable(R.drawable.select);
        Drawable unselectIcon = this.getResources().getDrawable(R.drawable.unselect);
        mAlarmDialog.setLeftBottomOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // left bottom
                // boolean leftBottomStatus = mPre.getDialogRadioLeftBottom();
                boolean leftBottomStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftBottom;
                if (leftBottomStatus) {
                    // mPre.setDialogRadioLeftBottom(false);
                    mAlarmDialog.setLeftBottomBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom = false;
                } else {
                    // mPre.setDialogRadioLeftBottom(true);
                    mAlarmDialog.setLeftBottomBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom = true;
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_LEFT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setRightBottomOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // right bottom
                // boolean rightBottomStatus = mPre.getDialogRadioRightBottom();
                boolean rightBottomStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightBottom;
                if (rightBottomStatus) {
                    // mPre.setDialogRadioRightBottom(false);
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom = false;
                    mAlarmDialog.setRightBottomBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    // mPre.setDialogRadioRightBottom(true);
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom = true;
                    mAlarmDialog.setRightBottomBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_RIGHT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setLeftCenterOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // left center
                // boolean leftCenterStatus = mPre.getDialogRadioLeftCenter();
                boolean leftCenterStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftCenter;
                if (leftCenterStatus) {
                    // mPre.setDialogRadioLeftCenter(false);
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter = false;
                    mAlarmDialog.setLeftCenterBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    // mPre.setDialogRadioLeftCenter(true);
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter = true;
                    mAlarmDialog.setLeftCenterBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_LEFT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setRightCenterOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // right center
                // boolean rightCenterStatus = mPre.getDialogRadioRightCenter();
                boolean rightCenterStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightCenter;
                if (rightCenterStatus) {
                    // mPre.setDialogRadioRightCenter(false);
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter = false;
                    mAlarmDialog.setRightCenterBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    // mPre.setDialogRadioRightCenter(true);
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter = true;
                    mAlarmDialog.setRightCenterBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_RIGHT_SLIDE_AREA);
            }
        });
        // TODO
        mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {

            @Override
            public void onClick(int progress) {
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
                    mPre.setQuickGestureDialogSeekBarValue(mAlarmDialog
                            .getSeekBarProgressValue());
                    // save sliding area value
                    mPre.setDialogRadioLeftBottom(mLeftBottom);
                    mPre.setDialogRadioRightBottom(mRightBottm);
                    mPre.setDialogRadioLeftCenter(mLeftCenter);
                    mPre.setDialogRadioRightCenter(mRightCenter);
                    QuickGestureManager.getInstance(QuickGestureActivity.this).resetSlidAreaSize();
                    // update area background color
                    updateFloatWindowBackGroudColor();
                    setSlidingAreaSetting();
                    if (mAlarmDialog != null) {
                        mAlarmDialog.dismiss();
                        FloatWindowHelper.mEditQuickAreaFlag = false;
                        mAlarmDialogFlag = false;
                        updateFloatWindowBackGroudColor();
                    }
                } else {
                    Toast.makeText(
                            QuickGestureActivity.this,
                            QuickGestureActivity.this
                                    .getResources()
                                    .getString(
                                            R.string.pg_appmanager_quick_gesture_option_dialog_radio_toast_text),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAlarmDialog.setCancelable(false);
        mAlarmDialog.show();
        mAlarmDialogFlag = true;
        updateFloatWindowBackGroudColor();
    }

    // update backgroud color
    private void updateFloatWindowBackGroudColor() {
        FloatWindowHelper
                .updateFloatWindowBackgroudColor(FloatWindowHelper.mEditQuickAreaFlag);
        FloatWindowHelper.createFloatWindow(QuickGestureActivity.this, AppMasterPreference
                .getInstance(getApplicationContext()).getQuickGestureDialogSeekBarValue());
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
        for (QuickGsturebAppInfo i : mFreeApps) {
            Log.e("#####", "" + i.label);

        }
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
                    mSlidingTimeTv
                            .setText(R.string.pg_appmanager_quick_gesture_slide_time_just_home_text);
                }
                // home and all app
                if (mSlideTimeDialog.getAppHomeCheckStatus()) {
                    mSlidingTimeTv
                            .setText(R.string.pg_appmanager_quick_gesture_slide_time_home_and_all_app_text);
                }
                AppMasterPreference.getInstance(QuickGestureActivity.this).setSlideTimeJustHome(
                        mSlideTimeDialog.getJustHometCheckStatus());
                AppMasterPreference.getInstance(QuickGestureActivity.this)
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
        int viewId = view.getId();
        int width = view.getWidth();
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
                        switch (viewId) {
                            case R.id.gesture_left_tips_top_tv:
                                intent.putExtra("show_orientation", 0);
                                break;
                            case R.id.gesture_left_tips_bottom:
                                intent.putExtra("show_orientation", 0);
                                break;
                            case R.id.gesture_right_tips_top_tv:
                                intent.putExtra("show_orientation", 2);
                                break;
                            case R.id.gesture_right_tips_bottom:
                                intent.putExtra("show_orientation", 2);
                                break;
                        }
                        try {
                            AppMasterApplication.getInstance().startActivity(intent);
                            AppMasterPreference.getInstance(this).setFristSlidingTip(true);
                            if (mQuickOpenCK != null) {
                                mQuickOpenCK.setImageResource(R.drawable.switch_on);
                                mPre.setSwitchOpenQuickGesture(true);
                                mQuickOpenCK.setImageResource(R.drawable.switch_on);
                                mOpenQuickFlag = true;
                                QuickGestureManager.getInstance(QuickGestureActivity.this)
                                        .startFloatWindow();
                                setOnClickListener();
                                initChexkBox();
                                // checkNoReadCallLog();
                                // checkNoReadMessage();
                                // init quick gesture data
                                QuickGestureManager.getInstance(getApplicationContext()).init();
                            }
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
        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(alpha);
        view.setAnimation(animation);
        animation.start();
    }

    private void gestureTranslationAnim(final View view1, final View view2) {
        view1.clearAnimation();
        view2.clearAnimation();
        AnimatorSet animatorSet = new AnimatorSet();
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

    @Override
    public void onClick(View arg0) {
        int flag = arg0.getId();
        switch (flag) {
            case R.id.open_quick:
                if (mOpenQuickFlag) {
                    mQuickOpenCK.setImageResource(R.drawable.switch_off);
                    mPre.setSwitchOpenQuickGesture(false);
                    mOpenQuickFlag = false;
                    unSetOnClickListener();
                    closeQuickSetting();
                    QuickGestureManager.getInstance(this).stopFloatWindow();
                    FloatWindowHelper.removeAllFloatWindow(QuickGestureActivity.this);
                    // uninit quick gesture data
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                        @Override
                        public void run() {
                            QuickGestureManager.getInstance(getApplicationContext()).unInit();
                        }
                    });
                } else {
                    mPre.setSwitchOpenQuickGesture(true);
                    mQuickOpenCK.setImageResource(R.drawable.switch_on);
                    mOpenQuickFlag = true;
                    QuickGestureManager.getInstance(QuickGestureActivity.this)
                            .startFloatWindow();
                    setOnClickListener();
                    initChexkBox();
                    // init quick gesture data
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            QuickGestureManager.getInstance(getApplicationContext()).init();
                        }
                    });
                }
                break;
            case R.id.slid_area:
                FloatWindowHelper.mEditQuickAreaFlag = true;
                showSettingDialog(true);
                break;
            case R.id.allow_slid_time:
                showSlideShowTimeSettingDialog();
                break;
            case R.id.no_read_message_content:
                if (!mNoReadMessageFlag) {
                    mPre.setSwitchOpenNoReadMessageTip(true);
                    mNoReadMessageOpenCK.setImageResource(R.drawable.switch_on);
                    mNoReadMessageFlag = true;
                    // checkout system database no read message
                    checkNoReadMessage();
                } else {
                    mPre.setSwitchOpenNoReadMessageTip(false);
                    mNoReadMessageOpenCK.setImageResource(R.drawable.switch_off);
                    mNoReadMessageFlag = false;
                }
                break;
            case R.id.recently_contact_content:
                if (!mRecentlyContactFlag) {
                    mPre.setSwitchOpenRecentlyContact(true);
                    mRecentlyContactOpenCK.setImageResource(R.drawable.switch_on);
                    mRecentlyContactFlag = true;
                    checkNoReadCallLog();
                } else {
                    mPre.setSwitchOpenRecentlyContact(false);
                    mRecentlyContactOpenCK.setImageResource(R.drawable.switch_off);
                    mRecentlyContactFlag = false;
                }
                break;
            case R.id.privacy_contact_content:
                if (!mPrivacyContactFlag) {
                    mPre.setSwitchOpenPrivacyContactMessageTip(true);
                    mPrivacyContactOpenCK.setImageResource(R.drawable.switch_on);
                    mPrivacyContactFlag = true;
                } else {
                    mPre.setSwitchOpenPrivacyContactMessageTip(false);
                    mPrivacyContactOpenCK.setImageResource(R.drawable.switch_off);
                    mPrivacyContactFlag = false;
                }
                break;
        }

    }

    @Override
    public void updateFilterAppClickListener() {
        // TODO Auto-generated method stub
        getEditFreeDisturbAppInfo(false);
        mSlideTimeAdapter.notifyDataSetChanged();
    }

    private void checkNoReadMessage() {
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
                    QuickGestureManager.getInstance(QuickGestureActivity.this).isShowSysNoReadMessage = true;
                    FloatWindowHelper
                            .removeShowReadTipWindow(QuickGestureActivity.this);
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
                QuickGestureManager.getInstance(QuickGestureActivity.this).mCallLogs = PrivacyContactUtils
                        .getSysCallLog(QuickGestureActivity.this,
                                QuickGestureActivity.this.getContentResolver(),
                                selection,
                                selectionArgs);
                if (QuickGestureManager.getInstance(QuickGestureActivity.this).mCallLogs != null
                        && QuickGestureManager.getInstance(QuickGestureActivity.this).mCallLogs
                                .size() > 0) {
                    QuickGestureManager.getInstance(QuickGestureActivity.this).isShowSysNoReadMessage = true;
                    FloatWindowHelper
                            .removeShowReadTipWindow(QuickGestureActivity.this);
                }
            }
        });
    }
}
