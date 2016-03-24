package com.leo.appmaster.intruderprotection;

import android.animation.LayoutTransition;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ad.ADEngineWrapper;
import com.leo.appmaster.ad.WrappedCampaign;
import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.receiver.DeviceReceiverNewOne;
import com.leo.appmaster.cloud.crypto.ImageEncryptInputStream;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.DeviceAdminEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.UserPresentEvent;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.imagehide.ImageGridActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.imagehide.PhotoAibum;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.FiveStarsLayout;
import com.leo.appmaster.ui.ResizableImageView;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAnimationDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageScaleType;
import com.leo.imageloader.utils.IoUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class IntruderCatchedActivity extends BaseActivity implements View.OnClickListener {
    private final int REQUEST_CODE_TO_REQUEST_ADMIN = 1;
    private LEOAnimationDialog mMessageDialog;
    private List<PhotoAibum> mAlbumList = null;
    private ImageLoader mImageLoader;
    private String mPkgName;
    private ListView mLvMain;
    private RelativeLayout mRlTipContent;
    private ArrayList<IntruderPhotoInfo> mSrcInfos;
    private ArrayList<IntruderPhotoInfo> mInfosSorted;
    private IntrudeSecurityManager mISManager;
    private final int NEWEST_PHOTO_NUMBER = 3;
    private BottomCropImage mIvNewestPhoto;
    private LinearLayout mLlMainMask;
    private TextView mTvNewestCatchTip;
    private ImageView mIvAppIntruded;
    private ScrollView mSvMain;
    private TextView mTvTimesToCatch;
    private TextView mTvTotalTimes;
    private RelativeLayout mRlNopic;
    private RippleView mRvClose;
    private TextView mTvOthers;
    private LEOChoiceDialog mDialog;
    private RelativeLayout mRvHeader;
    private RippleView mRvRating;
    private LEOAlarmDialog mOpenForbinDialog;
    private int[] mTimes = {
            1, 2, 3, 5
    };
    private RippleView mRvChange;
    private RippleView mRvMore;
    private RelativeLayout mRlNewest;
    private PreferenceTable mPt;
    private LinearLayout mLlGuide;
    private LinearLayout mLlGuideFinished;
    private LinearLayout mLlChangeTimes;
    private LEOAlarmDialog mAskOpenDeviceAdminDialog;
    private static final int TIMES_TO_CATCH_1 = 1;
    private static final int TIMES_TO_CATCH_2 = 2;
    private static final int TIMES_TO_CATCH_3 = 3;
    private static final int TIMES_TO_CATCH_4 = 5;
    private boolean mNeedIntoHomeWhenFinish = false;
    private FiveStarsLayout mLayout;
    private RippleView mRvSetting;
    private RippleView mRvOpen;
    private FrameLayout mShareLayout; // 分享layout
    private TextView mShareText;  //分享按钮
    private LinearLayout mSwiftylayout; //swifty卡片
    private TextView mSwiftyTitle;
    private ImageView mSwiftyImg;
    private TextView mSwiftyContent;
    private RippleView mSwiftyBtnLt;
    private final String TAG = "IntruderCatchedActivity";
	private static int mAdSource = ADEngineWrapper.SOURCE_MOB; // 默认值
    private Dialog mMultiUsesDialog;
    private boolean mShouldLoadAd = false;
    private boolean mIsUserPresent = false;

    private LinearLayout mFiveStarLayout;
    // 3.2 add advertise
    private static final String INTRUDER_AD_ID = Constants.UNIT_ID_244;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch_intruder);
        Intent intent = getIntent();
        mPt = PreferenceTable.getInstance();
        mISManager = (IntrudeSecurityManager) MgrContext
                .getManager(MgrContext.MGR_INTRUDE_SECURITY);
        mPkgName = intent.getStringExtra("pkgname");
        mLockManager.filterPackage(mPkgName, 5000);
        init();

		mAdSource = AppMasterPreference.getInstance(this).getInvaderAdConfig();
        LeoEventBus.getDefaultBus().register(this);
//        Intent i = new Intent(this, GradeTipActivity.class);
//        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 3.3.2 将广告加载/释放在onStart/onStop中做
        loadAd();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLayout != null) {
            mLayout.stopAnim();
        }
        if (mShouldLoadAd) {
            ADEngineWrapper.getInstance(this).releaseAd(mAdSource, INTRUDER_AD_ID);
        }
//        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTipStatus();
        SDKWrapper.addEvent(IntruderCatchedActivity.this, SDKWrapper.P1,
                "intruder", "intruder_capture");
        mPt.putBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, false);
        updateData();// 重新查询数据库，做与数据相关的UI更新
        updateAll();// 更新与数据库无关的UI
        mNeedIntoHomeWhenFinish = getIntent().getBooleanExtra("needIntoHomeWhenFinish", false);
        LeoLog.i("IntruderCatchedActivity", "mNeedIntoHomeWhenFinish = " + mNeedIntoHomeWhenFinish);
    }

    @Override
    public void onBackPressed() {
        if (mIsUserPresent) {
            try {
                Intent intent=new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            } catch (Exception e) {
                finish();
            }
        } else if (getPackageName().equals(mPkgName) && mNeedIntoHomeWhenFinish) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            mLockManager.filterPackage(mPkgName, 1000);
            startActivity(intent);
        }
//        else if (mIsUserPresent) {
//            Intent intent=new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            startActivity(intent);
//        }
//        else if ("from_systemlock".equals(mPkgName)) {
//            finish();
//        }
        else {
            mLockManager.filterPackage(mPkgName, 2000);
        }
        finish();
    }

    /**
     * 每次onResume进入界面需要重新刷新的操作(与数据库无关)
     */
    private void updateAll() {
        // 更新总的抓拍次数
        updateCatchTimes();
        // 更新抓拍所需的解锁失败次数
        updateTimesToCatch();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_TO_REQUEST_ADMIN == requestCode && DeviceReceiverNewOne.isActive(IntruderCatchedActivity.this)) {
            mISManager.setSystIntruderProtectionSwitch(true);
//            changeToGuideFinishedLayout();
            openAdvanceProtectDialogHandler();
        }
    }

    private void openAdvanceProtectDialogHandler() {
        boolean isTip = AppMasterPreference.getInstance(this)
                .getAdvanceProtectOpenSuccessDialogTip();
        if (isTip) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_dcnts", "gd_dput_real");
            openAdvanceProtectDialogTip();
        }
    }

    private void openAdvanceProtectDialogTip() {
        if (mMessageDialog == null) {
            mMessageDialog = new LEOAnimationDialog(this);
            mMessageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mMessageDialog != null) {
                        mMessageDialog = null;
                    }
                    AppMasterPreference.getInstance(IntruderCatchedActivity.this)
                            .setAdvanceProtectOpenSuccessDialogTip(false);
                }
            });
        }
        String content = getString(R.string.prot_open_suc_tip_cnt);
        mMessageDialog.setContent(content);
        mMessageDialog.show();
    }

    /**
     * 更新总的抓拍次数
     */
    private void updateCatchTimes() {
        String times1 = getString(R.string.intruder_times_of_catch);
        String times2 = String.format(times1, mISManager.getCatchTimes());
        mTvTotalTimes.setText(Html.fromHtml(times2));

        if (mISManager.getCatchTimes() >= 3) {
            mShareLayout.setVisibility(View.VISIBLE);
        } else {
            mShareLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 每次onResume进入界面后需要查询数据库然后更新的操作，
     */
    private void updateData() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mSrcInfos = mISManager.getPhotoInfoList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onQueryFinished();
                    }
                });
            }
        });
    }

    private String timeStampToAMPM(String timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT);
        Calendar ci = Calendar.getInstance();
        Date date = null;
        int hour = 0;
        int minute = 0;
        int ampm = 0;
        try {
            date = sdf.parse(timeStamp);
            ci.setTime(date);
            hour = ci.get(Calendar.HOUR);
            ampm = ci.get(Calendar.AM_PM);
            minute = ci.get(Calendar.MINUTE);
        } catch (ParseException e1) {
            return timeStamp;
        }
        String strHour = "";
        String strMinute = "";
        if (hour < 10) {
            strHour = "0" + hour;
        } else {
            strHour = hour + "";
        }
        if (minute < 10) {
            strMinute = "0" + minute;
        } else {
            strMinute = minute + "";
        }
        String fts;
        if (ampm == Calendar.AM) {
            fts = strHour + ":" + strMinute + "AM";
        } else if (ampm == Calendar.PM) {
            fts = strHour + ":" + strMinute + "PM";
        } else {
            fts = strHour + ":" + strMinute;
        }
        return fts;
    }

    /**
     * create时的初始化(不需要变化的UI)
     */
    private void init() {
        mRlTipContent = (RelativeLayout) findViewById(R.id.rl_intrudercatch_tip);
        mLlGuide = (LinearLayout) findViewById(R.id.ll_guide_tip);
        mLlGuideFinished = (LinearLayout) findViewById(R.id.ll_guide_finished);
        mLlChangeTimes = (LinearLayout) findViewById(R.id.ll_change_times);
        mRvOpen = (RippleView) findViewById(R.id.rv_open);
        mRvOpen.setOnClickListener(this);
        mRvSetting = (RippleView) findViewById(R.id.rv_setting);
        mRvSetting.setOnClickListener(this);
        mRvHeader = (RelativeLayout) findViewById(R.id.rl_header);
        mSvMain = (ScrollView) findViewById(R.id.sv_intrudercatch_main);
        mRlNopic = (RelativeLayout) findViewById(R.id.rl_nopic);
        mLlMainMask = (LinearLayout) findViewById(R.id.ll_main_mask);
        mRvRating = (RippleView) findViewById(R.id.rv_fivestars);
        mRvRating.setOnClickListener(this);
        mRlNewest = (RelativeLayout) findViewById(R.id.rl_newest);
        mTvOthers = (TextView) findViewById(R.id.tv_others);
        mRvClose = (RippleView) findViewById(R.id.rv_close);
        mRvClose.setOnClickListener(this);
        mTvTotalTimes = (TextView) findViewById(R.id.tv_times_of_catch);

        mRvChange = (RippleView) findViewById(R.id.rv_change_times);
        mRvChange.setOnClickListener(this);

        mTvTimesToCatch = (TextView) findViewById(R.id.tv_times_to_catch);

        // 初始化imageloader，需要时可以设置option
        mImageLoader = ImageLoader.getInstance();

        mIvAppIntruded = (ImageView) findViewById(R.id.iv_app_intruded);
        mTvNewestCatchTip = (TextView) findViewById(R.id.newest_catch_tip);
        mIvNewestPhoto = (BottomCropImage) findViewById(R.id.iv_newest_photo);
        mRvMore = (RippleView) findViewById(R.id.rv_more);
        mRvMore.setOnClickListener(this);
        mLvMain = (ListView) findViewById(R.id.lv_mainlist);
        mLayout = (FiveStarsLayout) findViewById(R.id.fsl_fivestars);
        mFiveStarLayout = (LinearLayout) findViewById(R.id.ll_fivestars_layout);
        mFiveStarLayout.setOnClickListener(this);

        mShareLayout = (FrameLayout) findViewById(R.id.share_layout);
        mShareText = (TextView) findViewById(R.id.share_text);
        mShareText.setText(Html.fromHtml(getResources().getString(
                R.string.intruder_share_dialog_content)));
        mShareLayout.setOnClickListener(this);
        initSwiftyLayout();
    }

    /* 3.2 advertise stuff - begin */
    private void loadAd() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        mShouldLoadAd = (amp.getADIntruder() == 1);
        if (mShouldLoadAd) {
			ADEngineWrapper.getInstance(this).loadAd(mAdSource, INTRUDER_AD_ID, new ADEngineWrapper.WrappedAdListener() {
				@Override
				public void onWrappedAdLoadFinished(int code, WrappedCampaign campaign, String msg) {
					if (code == MobvistaEngine.ERR_OK) {
						LeoLog.d("IntruderAd", "onMobvistaFinished: " + campaign.getAppName());
						sAdImageListener = new AdPreviewLoaderListener(IntruderCatchedActivity.this, campaign);
						mImageLoader.loadImage(campaign.getImageUrl(), sAdImageListener);
					}
				}

				@Override
				public void onWrappedAdClick(WrappedCampaign campaign, String unitID) {
					LeoLog.d("IntruderAd", "onMobvistaClick");
					SDKWrapper.addEvent(IntruderCatchedActivity.this, "max_ad", SDKWrapper.P1, "ad_click", "ad pos: " + unitID + " click", mAdSource, null);
					SDKWrapper.addEvent(IntruderCatchedActivity.this, 0,
							"ad_cli", "adv_cnts_capture");
					LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
					lm.filterPackage(getPackageName(), 1000);
				}
			});
            /*MobvistaEngine.getInstance(this).loadMobvista(INTRUDER_AD_ID, new MobvistaEngine.MobvistaListener() {

                @Override
                public void onMobvistaFinished(int code, final Campaign campaign, String msg) {
                    if (code == MobvistaEngine.ERR_OK) {
                        LeoLog.d("IntruderAd", "onMobvistaFinished: " + campaign.getAppName());
                        sAdImageListener = new AdPreviewLoaderListener(IntruderCatchedActivity.this, campaign);
                        mImageLoader.loadImage(campaign.getImageUrl(), sAdImageListener);
                    }
                }

                @Override
                public void onMobvistaClick(Campaign campaign, String unitID) {
                    LeoLog.d("IntruderAd", "onMobvistaClick");
                    SDKWrapper.addEvent(IntruderCatchedActivity.this, 0,
                            "ad_cli", "adv_cnts_capture");
                    LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                    lm.filterSelfOneMinites();
                }
            });*/
        }
    }

    /**
     * 新需求：当广告大图加载完成之后再展示广告
     */
    public static class AdPreviewLoaderListener implements ImageLoadingListener {
        WeakReference<IntruderCatchedActivity> mActivity;
		WrappedCampaign mCampaign;

        public AdPreviewLoaderListener(IntruderCatchedActivity fragment, final WrappedCampaign campaign) {
            mActivity = new WeakReference<IntruderCatchedActivity>(fragment);
            mCampaign = campaign;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
			SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + INTRUDER_AD_ID + " prepare for load image", mAdSource,  null);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + INTRUDER_AD_ID + " load image failed", mAdSource, null);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            IntruderCatchedActivity activity = mActivity.get();
            if (loadedImage != null && activity != null) {
				SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + INTRUDER_AD_ID + " image size: " + loadedImage.getByteCount(), mAdSource, null);
                LeoLog.d("IntruderAd", "[IntruderCatchedActivity] onLoadingComplete -> " + imageUri);
                activity.initAdLayout(activity.findViewById(R.id.ad_content),
                        mCampaign, INTRUDER_AD_ID, loadedImage);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    private static AdPreviewLoaderListener sAdImageListener;

    private void initAdLayout(View rootView, WrappedCampaign campaign, String unitId, Bitmap previewImage) {
        if (previewImage == null || previewImage.isRecycled()) {
            return;
        }

        ViewParent vg = rootView.getParent();
        if (vg.getClass().equals(ViewGroup.class)) {
            ((ViewGroup) vg).setLayoutTransition(new LayoutTransition());
        }
        View adView = rootView.findViewById(R.id.ad_content);
        TextView tvTitle = (TextView) adView.findViewById(R.id.item_title);
        tvTitle.setText(campaign.getAppName());
        ResizableImageView preview = (ResizableImageView) adView.findViewById(R.id.item_ad_preview);
        TextView summary = (TextView) adView.findViewById(R.id.item_summary);
        summary.setText(campaign.getDescription());
        Button btnCTA = (Button) adView.findViewById(R.id.ad_result_cta);
        btnCTA.setText(campaign.getAdCall());
        preview.setImageBitmap(previewImage);
        adView.setVisibility(View.VISIBLE);
		ADEngineWrapper.getInstance(this).registerView(mAdSource, adView, INTRUDER_AD_ID);
        //MobvistaEngine.getInstance(this).registerView(INTRUDER_AD_ID, adView);
        SDKWrapper.addEvent(IntruderCatchedActivity.this, 0,
                "ad_act", "adv_shws_capture");
		SDKWrapper.addEvent(AppMasterApplication.getInstance(), "max_ad", SDKWrapper.P1, "ad_show", "ad pos: " + INTRUDER_AD_ID + " adShow", mAdSource, null);
    }
    /* 3.2 advertise stuff - end */

    private void showChangeTimesDialog() {
        if (mDialog == null) {
            mDialog = new LEOChoiceDialog(IntruderCatchedActivity.this);
        }
        String times = getResources().getString(R.string.times_choose);
        List<String> timesArray = new ArrayList<String>();
        for (int i = 0; i < mTimes.length; i++) {
            timesArray.add(String.format(times, mTimes[i]));
        }

        int currentTimes = mISManager.getTimesForTakePhoto();
        int currentIndex = -1;
        switch (currentTimes) {
            case TIMES_TO_CATCH_1:
                currentIndex = 0;
                break;
            case TIMES_TO_CATCH_2:
                currentIndex = 1;
                break;
            case TIMES_TO_CATCH_3:
                currentIndex = 2;
                break;
            case TIMES_TO_CATCH_4:
                currentIndex = 3;
                break;
            default:
                break;
        }
        mDialog.setTitle(getResources().getString(R.string.ask_for_times_for_catch));
        mDialog.setItemsWithDefaultStyle(timesArray, currentIndex);
        mDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mISManager.setTimesForTakePhoto(TIMES_TO_CATCH_1);
                        updateTimesToCatch();
                        break;
                    case 1:
                        mISManager.setTimesForTakePhoto(TIMES_TO_CATCH_2);
                        updateTimesToCatch();
                        break;
                    case 2:
                        mISManager.setTimesForTakePhoto(TIMES_TO_CATCH_3);
                        updateTimesToCatch();
                        break;
                    case 3:
                        mISManager.setTimesForTakePhoto(TIMES_TO_CATCH_4);
                        updateTimesToCatch();
                        break;
                    default:
                        break;
                }
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    private void updateTimesToCatch() {
        String string1 = getString(R.string.intruder_to_catch_fail_times_tip);
        String s2 = String.format(string1, mISManager.getTimesForTakePhoto());
        mTvTimesToCatch.setText(Html.fromHtml(s2));
    }

    private void sortInfos() {
        mInfosSorted = mISManager.sortInfosByTimeStamp(mSrcInfos);
        if (!isLateastValid()) {
            IntruderPhotoInfo intruderPhotoInfo = new IntruderPhotoInfo("Lateast", "", "");
            mInfosSorted.add(0, intruderPhotoInfo);
        }
    }

    /**
     * 最新的抓拍是否合法：过期或被删除
     * @return
     */
    private boolean isLateastValid() {
        if (mInfosSorted == null || mInfosSorted.isEmpty()) {
            LeoLog.i("IntruderCatchedActivity", "is lastestValid : mInfos is null or empty!");
            return false;
        }

        IntruderPhotoInfo photoInfo = mInfosSorted.get(0);
        if (photoInfo == null || TextUtils.isEmpty(photoInfo.getFilePath())) {
            LeoLog.i("IntruderCatchedActivity", "is lastestValid : index 0's photoInfo is null or empty!");
            return false;
        }

        long savedHash = mPt.getLong(PrefConst.KEY_LATEAST_PATH, -1);
        LeoLog.i("IntruderCatchedActivity", "is lastestValid : preference last one hashCode = "+savedHash);
        LeoLog.i("IntruderCatchedActivity", "is lastestValid : index 0's photoInf hashCode = "+photoInfo.getFilePath().hashCode());
        return savedHash == photoInfo.getFilePath().hashCode();

    }

    /**
     * 查询完数据库后执行的操作
     */
    private void onQueryFinished() {
        LeoLog.i(TAG, "onQueryFinished");
        // 排序数据库的结果，按照时间排序
        sortInfos();
        final PackageManager pm = getPackageManager();
//        if (mInfosSorted != null && mInfosSorted.size() != 0) {
        if (isLateastValid() && mRlNewest != null) {
            // 如果记录有效，显示第一张大图
            mRlNewest.setVisibility(View.VISIBLE);
            mRlNopic.setVisibility(View.INVISIBLE);
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    final Bitmap bitmap;
                    InputStream inputStream = null;
                    try {
                        String path = mInfosSorted.get(0).getFilePath();
//                        String uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
                        inputStream = new ImageEncryptInputStream(path);
                        bitmap = BitmapFactory.decodeStream(inputStream, null, options);
//                        bitmap = BitmapFactory.decodeFile(mInfosSorted.get(0).getFilePath(), options);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        return;
                    } finally {
                        IoUtils.closeSilently(inputStream);
                    }
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mIvNewestPhoto.setImageBitmap(bitmap);
                            mIvNewestPhoto.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 点击后进入大图浏览
                                    startGallery(0);
                                }
                            });
                            // 大图上面的遮盖蒙层，图标和时间
                            mLlMainMask.setVisibility(View.VISIBLE);
                            ImageView mainIcon = (ImageView) mLlMainMask
                                    .findViewById(R.id.iv_appicon);
                            String pkg = mInfosSorted.get(0).getFromAppPackage();
                            Drawable applicationIcon = null;
                            if (IntrudeSecurityManager.ICON_SYSTEM.equals(pkg)) {
                                applicationIcon = getResources().getDrawable(R.drawable.intruder_system_icon);
                            } else {
                                applicationIcon = AppUtil.getAppIcon(pm, pkg);
                            }
                            if (applicationIcon != null) {
                                mainIcon.setImageDrawable(applicationIcon);
                            }
                            TextView mainTimestamp = (TextView) mLlMainMask
                                    .findViewById(R.id.tv_timestamp);
                            String timeStampToAMPM = timeStampToAMPM(mInfosSorted.get(0)
                                    .getTimeStamp());
                            mainTimestamp.setText(timeStampToAMPM);
                        }
                    });
                }
            });


//            ThreadManager.getUiThreadHandler().post(new Runnable() {
//                @Override
//                public void run() {
//                    mImageLoader.loadImage("file:///" + mInfosSorted.get(0).getFilePath(),
//                            new ImageLoadingListener() {
//                                @Override
//                                public void onLoadingStarted(String imageUri, View view) {
//                                    LeoLog.i(TAG, "onLoadingStarted");
//                                }
//
//                                @Override
//                                public void onLoadingFailed(String imageUri, View view,
//                                                            FailReason failReason) {
//                                    LeoLog.i(TAG, "onLoadingFailed");
//                                }
//
//                                @Override
//                                public void onLoadingComplete(final String imageUri, View view,
//                                                              Bitmap loadedImage) {
//                                    LeoLog.i(TAG, "onLoadingComplete");
//                                    mIvNewestPhoto.setImageBitmap(loadedImage);
//                                    mIvNewestPhoto.setOnClickListener(new OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            // 点击后进入大图浏览
//                                            startGallery(0);
//                                        }
//                                    });
//                                    // 大图上面的遮盖蒙层，图标和时间
//                                    mLlMainMask.setVisibility(View.VISIBLE);
//                                    ImageView mainIcon = (ImageView) mLlMainMask
//                                            .findViewById(R.id.iv_appicon);
//                                    Drawable applicationIcon = AppUtil.getAppIcon(pm, mInfosSorted
//                                            .get(0).getFromAppPackage());
//                                    if (applicationIcon != null) {
//                                        mainIcon.setImageDrawable(applicationIcon);
//                                    }
//                                    TextView mainTimestamp = (TextView) mLlMainMask
//                                            .findViewById(R.id.tv_timestamp);
//                                    String timeStampToAMPM = timeStampToAMPM(mInfosSorted.get(0)
//                                            .getTimeStamp());
//                                    mainTimestamp.setText(timeStampToAMPM);
//                                }
//
//                                @Override
//                                public void onLoadingCancelled(String imageUri, View view) {
//                                }
//                            });
//                }
//            });
            // XX想偷看XXX的文案
            updateFirstPhotoTips();
        } else {
            // 如果没有记录，下方就现实没有图片的默认图
            mRlNewest.setVisibility(View.INVISIBLE);
            mRlNopic.setVisibility(View.VISIBLE);
        }
        // 如果记录不止一条，将显示下方的其他照片部分
        if (mInfosSorted.size() >= 2) {
            showOtherPhotos();
        } else {
            // 记录只有一条，下方没有照片显示，布局的visibility改为gone
            mLvMain.setVisibility(View.GONE);
            mTvOthers.setVisibility(View.GONE);
        }
        // 设置listView的高度
        ListAdapter listAdapter = mLvMain.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = DipPixelUtil.dip2px(IntruderCatchedActivity.this, 140)
                * listAdapter.getCount();
        ViewGroup.LayoutParams params = mLvMain.getLayoutParams();
        int count = listAdapter.getCount() - 1;
        params.height = totalHeight + (mLvMain.getDividerHeight() * count);
        mLvMain.setLayoutParams(params);

        // 如果记录的数量大于4,显示“更多”的按钮
        if (mInfosSorted.size() > 4) {
            LeoLog.i("poha", "gone or visiable ? mInfosSorted.size = " + mInfosSorted.size());
            mRvMore.setVisibility(View.VISIBLE);
        } else {
            // 如果没有超过4条记录，不显示“更多”按钮
            mRvMore.setVisibility(View.GONE);
        }
        // 这里让头布局获得焦点，使得每次进入界面时显示界面的上部分，解决一进入界面就聚焦在下方listview部分的问题
        mSvMain.scrollTo(0,0);
        mRvHeader.setFocusable(true);
        mRvHeader.setFocusableInTouchMode(true);
        mRvHeader.requestFocus();
    }



    /**
     * 显示下方更多照片部分的操作
     */
    private void showOtherPhotos() {
        mTvOthers.setVisibility(View.VISIBLE);
        mLvMain.setVisibility(View.VISIBLE);
        mLvMain.setAdapter(new BaseAdapter() {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (convertView == null) {
                    view = View.inflate(IntruderCatchedActivity.this,
                            R.layout.item_photo_in_catch, null);
                }
                final LinearLayout llMask = (LinearLayout) view.findViewById(R.id.ll_mask);
                final BottomCropImage ivv = (BottomCropImage) view
                        .findViewById(R.id.iv_intruder_more);
                final String filePath = mInfosSorted.get(position + 1).getFilePath();
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;

                        Bitmap bitmap = null;
                        InputStream inputStream = null;
                        try {
                            inputStream = new ImageEncryptInputStream(filePath);
                            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            return;
                        } finally {
                            IoUtils.closeSilently(inputStream);
                        }
                        final Bitmap imgBitmap = bitmap;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivv.setImageBitmap(imgBitmap);
                                ivv.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startGallery(position + 1);
                                    }
                                });
                            }
                        });
                    }
                });

                PackageManager pm = getPackageManager();
                try {
                    String pkg = mInfosSorted.get(position + 1).getFromAppPackage();
                    Drawable applicationIcon = null;
                    if (IntrudeSecurityManager.ICON_SYSTEM.equals(pkg)) {
                        applicationIcon = getResources().getDrawable(R.drawable.intruder_system_icon);
                    } else if (SwitchGroup.WIFI_SWITCH.equals(pkg)) {
                        applicationIcon = getResources().getDrawable(R.drawable.lock_wifi);
                    } else if (SwitchGroup.BLUE_TOOTH_SWITCH.equals(pkg)) {
                        applicationIcon = getResources().getDrawable(R.drawable.lock_bluetooth);
                    } else {
                        applicationIcon = AppUtil.getAppIcon(pm, pkg);
                    }
                    ImageView iv2 = (ImageView) (llMask.findViewById(R.id.iv_appicon));
                    iv2.setImageDrawable(applicationIcon);
                    // TextView tv2 = (TextView)
                    // (llMask.findViewById(R.id.tv_timestamp));
                    // tv2.setText(mInfosSorted.get(position+1).getTimeStamp());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return view;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public int getCount() {
                if (mInfosSorted == null)
                    return 0;
                return Math.min(NEWEST_PHOTO_NUMBER, mInfosSorted.size() - 1);
            }
        });
    }

    private void startGallery(int position) {
        mLockManager.filterPackage(getPackageName(),1000);
        Intent intent = new Intent(IntruderCatchedActivity.this, IntruderGalleryActivity.class);
        intent.putExtra("current_position", position);
        SDKWrapper.addEvent(IntruderCatchedActivity.this, SDKWrapper.P1,
                "intruder", "intruder_view_capture");
        startActivity(intent);
    }

    /**
     * XXX想要偷看XXX应用的提示
     */
    private void updateFirstPhotoTips() {
        try {
            PackageManager pm = getPackageManager();
            String packageName = mInfosSorted.get(0).getFromAppPackage();
            String label;
            Drawable applicationIcon;
            if (IntrudeSecurityManager.ICON_SYSTEM.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.intruder_system_icon);
                label = getString(R.string.mobile_phone);
            } else if (SwitchGroup.BLUE_TOOTH_SWITCH.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.lock_bluetooth);
                label = getString(R.string.app_lock_list_switch_bluetooth);
            } else if (SwitchGroup.WIFI_SWITCH.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.lock_wifi);
                label = getString(R.string.app_lock_list_switch_wifi);
            } else {
                applicationIcon = AppUtil.getAppIcon(pm, packageName);
                label = AppUtil.getAppLabel(pm, packageName);
            }
            mIvAppIntruded.setImageDrawable(applicationIcon);
            String newestCatchTipsS = getResources().getString(R.string.newest_catch_tip);

            String newestCatchTipsD = String.format(newestCatchTipsS, label);
            mTvNewestCatchTip.setText(newestCatchTipsD);
        } catch (Exception e) {
        }
    }

    public void onEventMainThread(UserPresentEvent event) {
        if (event.getEventId() == EventId.EVENT_USER_PRESENT_ID) {
            mIsUserPresent = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
        }
        if (mMultiUsesDialog != null) {
            mMultiUsesDialog.dismiss();
        }
        LeoEventBus.getDefaultBus().unregister(this);
    }

    private void initSwiftyLayout() {
        ViewStub viewStub = (ViewStub) findViewById(R.id.swifty_stub);
        if (viewStub == null) {
            return;
        }

        PreferenceTable preferenceTable = PreferenceTable.getInstance();

        boolean isContentEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_INTRUDER_SWIFTY_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_INTRUDER_SWIFTY_IMG_URL));

        boolean isTypeEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_INTRUDER_SWIFTY_TYPE));

        boolean isGpUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_INTRUDER_SWIFTY_GP_URL));

        boolean isBrowserUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_INTRUDER_SWIFTY_URL));

        boolean isUrlEmpty = isGpUrlEmpty && isBrowserUrlEmpty; //判断两个地址是否都为空

        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
            View include = viewStub.inflate();
            mSwiftyTitle = (TextView) include.findViewById(R.id.item_title);
            mSwiftyImg = (ImageView) include.findViewById(R.id.swifty_img);
            mSwiftyContent = (TextView) include.findViewById(R.id.swifty_content);
            mSwiftyBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
            mSwiftyBtnLt.setOnClickListener(this);
            mSwiftyContent.setText(preferenceTable.getString(PrefConst.KEY_INTRUDER_SWIFTY_CONTENT));
            String imgUrl = preferenceTable.getString(PrefConst.KEY_INTRUDER_SWIFTY_IMG_URL);
            mImageLoader.displayImage(imgUrl, mSwiftyImg, getOptions(R.drawable.online_theme_loading));
            boolean isTitleEmpty = TextUtils.isEmpty(
                    preferenceTable.getString(PrefConst.KEY_INTRUDER_SWIFTY_TITLE));
            if (!isTitleEmpty) {
                mSwiftyTitle.setText(preferenceTable.getString(
                        PrefConst.KEY_INTRUDER_SWIFTY_TITLE));
            }
        }

    }

    public DisplayImageOptions getOptions(int drawble) {  //需要提供默认图
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(drawble)
                .showImageForEmptyUri(drawble)
                .showImageOnFail(drawble)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();

        return options;
    }



    private void updateTipStatus() {
        boolean isOpen = mISManager.getSystIntruderProtecionSwitch();
        boolean isDeviceAdmin = DeviceReceiverNewOne.isActive(IntruderCatchedActivity.this);
        if (isOpen) {
            mLlChangeTimes.setVisibility(View.VISIBLE);
            mLlGuide.setVisibility(View.GONE);
            mLlGuideFinished.setVisibility(View.GONE);
        } else {
            mLlGuide.setVisibility(View.GONE);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "intruder", "intruder_cap_scr_sh");
            mLlChangeTimes.setVisibility(View.VISIBLE);
            mLlGuideFinished.setVisibility(View.GONE);
        }
    }

//    private void changeToGuideFinishedLayout() {
//        SDKWrapper.addEvent(IntruderCatchedActivity.this, SDKWrapper.P1, "intruder", "intruder_cap_scr_on");
//        if (mISManager.getIsIntruderSecurityAvailable()) {
//            mISManager.setSystIntruderProtectionSwitch(true);
//            mLlGuide.setVisibility(View.GONE);
//            mLlChangeTimes.setVisibility(View.GONE);
//            mLlGuideFinished.setVisibility(View.VISIBLE);
//            TranslateAnimation tla = new TranslateAnimation(mRlTipContent.getWidth(),0,0,0);
//            mLlGuideFinished.setAnimation(tla);
//            tla.setDuration(500);
//            mLlGuideFinished.startAnimation(tla);
//        } else {
//            mMultiUsesDialog = ShowAboutIntruderDialogHelper.showForbitDialog(this, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent intent = new Intent(IntruderCatchedActivity.this, FeedbackActivity.class);
//                    intent.putExtra("isFromIntruderProtectionForbiden", true);
//                    startActivity(intent);
//                    mMultiUsesDialog.dismiss();
//                }
//            });
//        }
//    }

//    protected void showForbitDialog() {
//        if (mOpenForbinDialog == null) {
//            mOpenForbinDialog = new LEOAlarmDialog(this);
//        }
//        mOpenForbinDialog.setContent(getResources().getString(
//                R.string.intruderprotection_forbit_content));
//        mOpenForbinDialog.setRightBtnStr(getResources().getString(
//                R.string.secur_help_feedback_tip_button));
//        mOpenForbinDialog.setLeftBtnStr(getResources().getString(
//                R.string.no_image_hide_dialog_button));
//        mOpenForbinDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent intent = new Intent(IntruderCatchedActivity.this, FeedbackActivity.class);
//                intent.putExtra("isFromIntruderProtectionForbiden", true);
//                startActivity(intent);
//                mOpenForbinDialog.dismiss();
//            }
//        });
//        mOpenForbinDialog.show();
//    }

//    private void showAskOpenDeviceAdminDialog() {
//        if (mAskOpenDeviceAdminDialog == null) {
//            mAskOpenDeviceAdminDialog = new LEOAlarmDialog(IntruderCatchedActivity.this);
//        }
//        if (mAskOpenDeviceAdminDialog.isShowing()) {
//            return;
//        }
//        mAskOpenDeviceAdminDialog.setTitle(R.string.intruder_setting_title_1);
//        mAskOpenDeviceAdminDialog.setContent(getString(R.string.intruder_device_admin_guide_content));
//        mAskOpenDeviceAdminDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                requestDeviceAdmin();
//                mAskOpenDeviceAdminDialog.dismiss();
//            }
//        });
//        mAskOpenDeviceAdminDialog.show();
//    }

    private void requestDeviceAdmin() {
//        mLockManager.filterSelfOneMinites();
        mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
        ComponentName mAdminName = new ComponentName(IntruderCatchedActivity.this, DeviceReceiverNewOne.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
        startActivityForResult(intent, REQUEST_CODE_TO_REQUEST_ADMIN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rv_open:
                SDKWrapper.addEvent(IntruderCatchedActivity.this, SDKWrapper.P1,"intruder", "intruder_cap_scr_cli");
                if (DeviceReceiverNewOne.isActive(IntruderCatchedActivity.this)) {
//                    changeToGuideFinishedLayout();
                } else {
                    mMultiUsesDialog = ShowAboutIntruderDialogHelper.showAskOpenDeviceAdminDialog(IntruderCatchedActivity.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestDeviceAdmin();
                            mMultiUsesDialog.dismiss();
                        }
                    });
                }
                break;
            case R.id.rv_close:
                SDKWrapper.addEvent(IntruderCatchedActivity.this, SDKWrapper.P1,
                        "intruder", "intruder_capture_quit");
                onBackPressed();
                break;
            case R.id.rv_setting:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "intruder", "intruder_cap_setting");
                Intent intent2 = new Intent(IntruderCatchedActivity.this,IntruderSettingActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent2.putExtra("isPgInner",false);
                mLockManager.filterPackage(getPackageName(),1000);
                startActivity(intent2);
                break;
            case R.id.rv_change_times:
                showChangeTimesDialog();
                break;
            case R.id.rv_fivestars:
            case R.id.ll_fivestars_layout:
                SDKWrapper.addEvent(IntruderCatchedActivity.this, SDKWrapper.P1,
                        "intruder", "intruder_capture_rank");
                mLockManager.filterPackage(getPackageName(), 1000);
                mPt.putBoolean(PrefConst.KEY_HAS_GRADE, true);
                Utilities.goFiveStar(IntruderCatchedActivity.this, false, false);
                break;
            case R.id.rv_more:
                // “更多”按钮点击后，将进入图片隐藏功能中的的对应相册
                SDKWrapper.addEvent(IntruderCatchedActivity.this, SDKWrapper.P1,
                        "intruder", "intruder_capture_more");
                long cc1 = System.currentTimeMillis();
                int index = 0;
                mAlbumList = ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).getHidePicAlbum("");

                String parent = null;
                for (int i = 0; i < mInfosSorted.size(); i++) {
                    IntruderPhotoInfo intruderPhotoInfo = mInfosSorted.get(i);
                    String filePath = intruderPhotoInfo.getFilePath();
                    if (filePath == null || filePath.equals("Lateast")) {
                        continue;
                    } else {
                        File file = new File(filePath);
                        parent = file.getParent();
                        if (!TextUtils.isEmpty(parent)) {
                            break;
                        }
                    }
                }
                if (parent == null) {
                    Intent intent = new Intent(IntruderCatchedActivity.this, ImageHideMainActivity.class);
                    startActivity(intent);
                    return;
                }
                for (int i = 0; i < mAlbumList.size(); i++) {
                    PhotoAibum photoAibum = mAlbumList.get(i);
                    if (parent.equals(photoAibum.getDirPath())) {
                        index = i;
                        break;
                    }
                }

                long cc2 = System.currentTimeMillis();
                LeoLog.i("catch_poha", "cc2 -cc1 :" + (cc2 - cc1));
                try {
                    Intent intent = new Intent(IntruderCatchedActivity.this, ImageGridActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", mAlbumList.get(index));
                    intent.putExtras(bundle);
                    intent.putExtra("fromIntruderMore", true);
                    intent.putExtra("mode", ImageGridActivity.CANCEL_HIDE_MODE);
                    startActivity(intent);
                    // IntruderCatchedActivity.this.finish();
                    LeoLog.i("catch_poha", "cc3 -cc2 :" + (System.currentTimeMillis() - cc2));
                } catch (Throwable t) {
                    Intent intent = new Intent(IntruderCatchedActivity.this,
                            ImageHideMainActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.share_layout:  // 分享
                SDKWrapper.addEvent(IntruderCatchedActivity.this, SDKWrapper.P1,
                        "intruder", "share_cnts");
                mLockManager.filterPackage(getPackageName(), 1000);
                PreferenceTable sharePreferenceTable = PreferenceTable.getInstance();
                boolean isContentEmpty = TextUtils.isEmpty(
                        sharePreferenceTable.getString(PrefConst.KEY_INTRUDER_SHARE_CONTENT));
                boolean isUrlEmpty = TextUtils.isEmpty(
                        sharePreferenceTable.getString(PrefConst.KEY_INTRUDER_SHARE_URL));

                StringBuilder shareBuilder = new StringBuilder();
                if (!isContentEmpty && !isUrlEmpty) {
                    try {
                        shareBuilder.append(String.format(sharePreferenceTable.getString(
                                PrefConst.KEY_INTRUDER_SHARE_CONTENT),  mISManager.getCatchTimes()))
                                .append(" ")
                                .append(sharePreferenceTable.getString(PrefConst.KEY_INTRUDER_SHARE_URL));
                    } catch (Exception e) {
                        shareBuilder.append(sharePreferenceTable.getString(
                                PrefConst.KEY_INTRUDER_SHARE_CONTENT))
                                .append(" ")
                                .append(sharePreferenceTable.getString(PrefConst.KEY_INTRUDER_SHARE_URL));
                    }
                } else {
                    shareBuilder.append(getResources().getString(
                                R.string.intruder_share_content, mISManager.getCatchTimes()))
                                .append(" ")
                                .append(Constants.DEFAULT_SHARE_URL);
                }
                Utilities.toShareApp(shareBuilder.toString(), getTitle().toString(), IntruderCatchedActivity.this);

                break;
            case R.id.item_btn_rv:  // 点击swifty卡片
                mLockManager.filterPackage(getPackageName(), 1000);
                PreferenceTable preferenceTable = PreferenceTable.getInstance();
                Utilities.selectType(preferenceTable, PrefConst.KEY_INTRUDER_SWIFTY_TYPE,
                        PrefConst.KEY_INTRUDER_SWIFTY_GP_URL, PrefConst.KEY_INTRUDER_SWIFTY_URL,
                        "", IntruderCatchedActivity.this);
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mLayout != null) {
            mLayout.startAnim();
        }
    }

}
