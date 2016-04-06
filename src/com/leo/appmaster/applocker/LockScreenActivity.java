package com.leo.appmaster.applocker;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ad.ADEngineWrapper;
import com.leo.appmaster.ad.LEOAdManager;
import com.leo.appmaster.ad.PreviewImageFetcher;
import com.leo.appmaster.ad.WrappedCampaign;
import com.leo.appmaster.airsig.AirSigActivity;
import com.leo.appmaster.airsig.AirSigSettingActivity;
import com.leo.appmaster.animation.ColorEvaluator;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.applocker.manager.TaskChangeHandler;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.ProcessDetectorUsageStats;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.eventbus.event.LockThemeChangeEvent;
import com.leo.appmaster.eventbus.event.SubmaineAnimEvent;
import com.leo.appmaster.fragment.GestureLockFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdLockFragment;
import com.leo.appmaster.fragment.PretendAppBeautyFragment;
import com.leo.appmaster.fragment.PretendAppErrorFragment;
import com.leo.appmaster.fragment.PretendAppUnknowCallFragment5;
import com.leo.appmaster.fragment.PretendAppZhiWenFragment;
import com.leo.appmaster.fragment.PretendFragment;
import com.leo.appmaster.intruderprotection.CameraSurfacePreview;
import com.leo.appmaster.intruderprotection.IntruderCatchedActivity;
import com.leo.appmaster.intruderprotection.WaterMarkUtils;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.phoneSecurity.PhoneSecurityManager;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.ui.PushUIHelper;
import com.leo.appmaster.sdk.update.UIHelper;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.ui.BaseSelfDurationToast;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoCircleView;
import com.leo.appmaster.ui.LeoHomePopMenu;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.FastBlur;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.utils.Utilities;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LockScreenActivity extends BaseFragmentActivity implements
		OnClickListener, OnDiaogClickListener/*, EcoGallery.IGalleryScroll */ {

	public static final String TAG = "LockScreenActivity";
	//    private HomeWatcherReceiver mReceiver;
	private static final String mPrivateLockPck = "com.leo.appmaster";
	public static final String THEME_CHANGE = "lock_theme_change";
	public static final String EXTRA_LOCK_MODE = "extra_lock_type";
	public static final String EXTRA_UKLOCK_TYPE = "extra_unlock_type";
	public static final String EXTRA_LOCK_TITLE = "extra_lock_title";
	public static final String SHOW_NOW = "mode changed_show_now";
	public static final long CLICK_OVER_DAY = 24 * 1000 * 60 * 60;
	public static final int SHOW_RED_MAN = 1;
	public static final int LARGE_BANNER_HIDE = 2;
	public static final int AD_TYPE_SHAKE = 1;
	public static final int AD_TYPE_JUMP = 2;
	public static final int AD_TYPE_STAY = 3;
	//    private static boolean sHasClickGoGrantPermission = false;
	private static boolean mHasClickGoGrantPermission = false;
	public int SHOW_AD_TYPE = 0;
	private int mLockMode;
	private String mLockedPackage;
	private CommonTitleBar mTtileBar;
	private TextView mLockAppTitleView;
	private LockFragment mLockFragment;
	private Bitmap mAppBaseInfoLayoutbg;
	private LeoHomePopMenu mLeoPopMenu;
	private LeoDoubleLinesInputDialog mDialog;
	private LEOAlarmDialog mTipDialog;
	private EditText mEtQuestion, mEtAnwser;
	private RippleView mMrlGift;
	private RelativeLayout mRlNoPermission;
	private String mLockTitle;
	// private ImageView mThemeView;
	private ImageView mAdIcon, mAdIconRedTip;
	// private View switch_bottom_content;
	private ImageView mADAnimalEntry;
	private LeoPreference mPt;
	private BaseSelfDurationToast mPermissionGuideToast;
	private TextView mTvPermissionTip;
	private View mVPermissionTip;
	/**
	 * 大banner
	 */
//    private FrameLayout mBannerParent;
	private ViewPager mBannerContainer;
	private AdBannerAdapter mAdapterCycle;
	private LinkedHashMap<String, WrappedCampaign> mAdMap = new LinkedHashMap<String, WrappedCampaign>();
	private ArrayList<String> mAdUnitIdList = new ArrayList<String>();
	private ArrayList<MobvistaListener> mMobvistaListenerList = new ArrayList<MobvistaListener>();
	private static final String[] mBannerAdids = {LEOAdManager.UNIT_ID_LOCK, LEOAdManager.UNIT_ID_LOCK_1, LEOAdManager.UNIT_ID_LOCK_2};
	private boolean otherAdSwitcher = false;
	private boolean mDidLoadAd = false;
	//private String[] mBannerAdids = {"12346_00001"};

	private RelativeLayout mPretendLayout;
	private PretendFragment mPretendFragment;
	private IntrudeSecurityManager mISManager;
	private PrivacyDataManager mPDManager;

	private String mCleanRate;
	private TextView mText;
	private View mLockClean;
	private ActivityManager mAm;

	public boolean mRestartForThemeChanged;
	public boolean mQuickLockMode;
	public boolean mFromHome;
	public boolean mFromQuickGesture;
	public String mQuickModeName;
	public int mQuiclModeId;
	private RelativeLayout mLockLayout;
	private boolean mMissingDialogShowing;
	public static boolean sLockFilterFlag = false;
	private static AnimationDrawable adAnimation;
	public static boolean interupAinimation = false;
	private boolean clickShakeIcon = false;

	private static final boolean DBG = false;
	/* 用于测试时，指定显示的广告形式 */
	private static final int TEST_AD_NUMBER = 6;

	//能否拍照，每次进入界面才能够置为true，拍完置为false，保证每次进入界面只能拍一次，避免频繁拍照
	private boolean mCanTakePhoto = true;
	//照片是否已经保存完毕，保存完毕后置为true，true才能进入抓拍结果界面，每次拍照置为false //暂时不用
	public static boolean mIsPicSaved = false;

	public static boolean mHasTakePic = false;
	
	private View mAdView;


	private int mAdSource = ADEngineWrapper.SOURCE_MOB; // 默认值
	//
//    public boolean mIs

    // 记录广告是否点击，onStart后重置为false
    private boolean mForceLoad;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_RED_MAN:
                    mADAnimalEntry.setBackgroundResource(R.drawable.adanimation3);
                    AnimationDrawable redmanAnimation = (AnimationDrawable)
                            mADAnimalEntry.getBackground();
                    redmanAnimation.start();
                    break;
                case LARGE_BANNER_HIDE:
                    bannerHideAnim();
                    break;
            }
        }
    };

    public void setCanTakePhoto(boolean flag) {
        mCanTakePhoto = flag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long start = SystemClock.elapsedRealtime();
        setContentView(R.layout.activity_lock_layout);


//        registerHomeKeyReceiver()
		LeoLog.d(TAG, "TsCost, onCreate..." + (SystemClock.elapsedRealtime() - start));
		mISManager = (IntrudeSecurityManager) MgrContext
				.getManager(MgrContext.MGR_INTRUDE_SECURITY);
		mPt = LeoPreference.getInstance();
		mPDManager = (PrivacyDataManager) MgrContext
				.getManager(MgrContext.MGR_PRIVACY_DATA);
		mLockLayout = (RelativeLayout) findViewById(R.id.activity_lock_layout);
		handleIntent();
		mLockManager.setPauseScreenonLock(true);
		// for fix lock mode shortcut bug
		if (mQuickLockMode) {
			List<LockMode> modeList = mLockManager.getLockMode();
			LockMode mode = null;
			for (LockMode lockMode : modeList) {
				if (lockMode.modeId == mQuiclModeId) {
					mode = lockMode;
					break;
				}
			}
			if (mode != null) {
				if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
					if (mode.defaultFlag != -1) {
						Intent intent = new Intent(this, LockSettingActivity.class);
						intent.putExtra("from_quick_mode", true);
						intent.putExtra("just_finish", true);
						intent.putExtra("mode_id", mQuiclModeId);
						this.startActivity(intent);
					}
					finish();
					return;
				}
			} else {
				showModeMissedTip();
				return;
			}
		}

		initUI();
//        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mobvistaCheck();
//            }
//        }, 1500);
        checkCleanMem();
        LeoEventBus.getDefaultBus().register(this);
        checkOutcount();
    }

    private void mobvistaCheck() {
        // init方法统一放到MobvistaEngine里，by lishuai
        // mobvista ad
        // MobvistaAd.init(this, Constants.MOBVISTA_APPID,
        // Constants.MOBVISTA_APPKEY);
        // -----------------Mobvista Sdk--------------------

        // init wall controller
        // newAdWallController(Context context,String unitid, String fbid)
        // wallAd = MobvistaEngine.getInstance().createAdWallController(this);
//        wallAd = MobvistaEngine.getInstance(this).createAdWallController(this, Constants.UNIT_ID_63);
//        if (wallAd != null) {
//            // preload the wall data
//            wallAd.preloadWall();
//        }

		MobvistaEngine.getInstance(this).createAdWallController1(this, Constants.UNIT_ID_63);
    }

    public void takePicture(final CameraSurfacePreview view, final String packagename) {
        if (BuildProperties.isApiLevel14() || mLockMode != LockManager.LOCK_MODE_FULL) {
            return;
        }
        SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1,
                "intruder", "intruder_package_" + packagename);
        if (view != null && mCanTakePhoto) {
            view.takePicture(new PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] data, Camera camera) {
                    LeoLog.i("poha", "has taken!!!");
                    mCanTakePhoto = false;
                    LeoLog.i("poha", "pic taken!!  mCanTakePhoto :" + mCanTakePhoto + "mHasTakePic :" + mHasTakePic + "delay? :" + mPt.getBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, false));
                    mISManager.setCatchTimes(mISManager.getCatchTimes() + 1);
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            AppMasterApplication ama = AppMasterApplication.getInstance();
                            Bitmap bitmapt = null;
                            try {
                                bitmapt = BitmapUtils.bytes2BimapWithScale(data, LockScreenActivity.this);
                            } catch (Throwable e) {
                            }
                            //旋转原始bitmap到正确的方向

                            Matrix m = new Matrix();
                            int orientation = view.getCameraOrientation();
                            m.setRotate(180 - orientation, (float) bitmapt.getWidth() / 2, (float) bitmapt.getHeight() / 2);
                            bitmapt = Bitmap.createBitmap(bitmapt, 0, 0, bitmapt.getWidth(), bitmapt.getHeight(), m, true);
                            String timeStamp = new SimpleDateFormat(Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT).format(new Date());

                            //添加水印
                            bitmapt = WaterMarkUtils.createIntruderPhoto(bitmapt, timeStamp, packagename, ama);

                            //将bitmap压缩并保存
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmapt.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            byte[] finalBytes = baos.toByteArray();
                            File photoSavePath = getPhotoSavePath();
                            if (photoSavePath == null) {
                                return;
                            }
                            String finalPicPath = "";
                            try {
                                LeoLog.i("poha", photoSavePath + "::save Path");
                                FileOutputStream fos = new FileOutputStream(photoSavePath);
                                fos.write(finalBytes);
                                fos.close();
                                LeoLog.i("poha", "saved!!!");
                                // 隐藏图片
                                finalPicPath = mPDManager.onHidePic(photoSavePath.getPath(), null);
                                LeoLog.i("poha", "hiden!!!");
                                FileOperationUtil.saveFileMediaEntry(finalPicPath, ama);
                                FileOperationUtil.deleteImageMediaEntry(photoSavePath.getPath(), ama);
                                LeoLog.i("poha", "finally!!!");
                                mIsPicSaved = true;
                                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                                pdm.notifySecurityChange();
                            } catch (Exception e) {
                                LeoLog.i("poha", "exception!!   ..." + e.toString());
                                return;
                            }

                            IntruderPhotoInfo info = new IntruderPhotoInfo(finalPicPath, packagename, timeStamp);
                            mISManager.insertInfo(info);
                            mIsPicSaved = true;
                            mPt.putLong(PrefConst.KEY_LATEAST_PATH, finalPicPath.hashCode());
//                            mPt.putBoolean(PrefConst.KEY_HAS_LATEAST, true);
							LeoLog.i("poha", "after insert, before judge!!  mCanTakePhoto :" + mCanTakePhoto + "mHasTakePic :" + mHasTakePic + "delay? :" + mPt.getBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, false));
							if (mPt.getBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, false) && mIsPicSaved) {
								mPt.putBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, false);
								Intent intent = new Intent(getApplicationContext(), IntruderCatchedActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.putExtra("pkgname", mLockedPackage);
								mLockManager.filterPackage(getPackageName(), 1000);
								LeoLog.i("poha", "start Catch Activity");
								startActivity(intent);
								mHasTakePic = false;
								mIsPicSaved = false;
								LeoLog.i("poha", "delay!! has enter catch !!  mCanTakePhoto :" + mCanTakePhoto + "mHasTakePic :" + "delay? :" + mPt.getBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, false));
							}
							bitmapt.recycle();
						}
					});
					if (mLockFragment != null) {
						mLockFragment.removeCamera();
					}
				}
			});
		} else {
			LeoLog.i("poha", "view == null or can't take");
			if (mLockFragment != null) {
				ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
					@Override
					public void run() {
						LeoLog.i("poha", "fragment remove Camera");
						mLockFragment.removeCamera();
					}
				}, 2000);
			}
		}
	}

	// 获得抓拍照片保存的路径
	private File getPhotoSavePath() {
		File picDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(new Date());
		File dir = new File(picDir.getPath() + File.separator + "IntruderP");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return new File(dir + File.separator + "IMAGE_" + timeStamp + ".jpg");
	}

	@SuppressWarnings("deprecation")
	private void setMobvistaIcon() {
		if (mQuickLockMode) {
			return;
		}
		if (AppMasterPreference.getInstance(this).getIsLockAppWallOpen() > 0) {
			if (SHOW_AD_TYPE == AD_TYPE_SHAKE) {
				mAdIconRedTip.setVisibility(View.VISIBLE);
				mAdIcon.setBackgroundResource(R.drawable.adanimation2);
				adAnimation = (AnimationDrawable)
						mAdIcon.getBackground();
				adAnimation.start();

			} else { // jump
				mAdIconRedTip.setVisibility(View.GONE);
				if (SHOW_AD_TYPE == AD_TYPE_JUMP) {
					mAdIcon.setBackgroundResource(R.drawable.adanimation);
					adAnimation = (AnimationDrawable)
							mAdIcon.getBackground();
					adAnimation.start();
					LeoLog.e("testLockScreen", "jump going!");
				} else {
					mAdIcon.setBackgroundDrawable((this.getResources()
							.getDrawable(R.drawable.jump_1)));
					LeoLog.e("testLockScreen", "stay going!");
				}
			}
		}
	}

	private void showModeMissedTip() {
		mMissingDialogShowing = true;
		mTipDialog = new LEOAlarmDialog(this);
		mTipDialog.setTitle(R.string.tips_title);
		mTipDialog.setContent(getString(R.string.mode_missing));
		mTipDialog.setRightBtnStr(getString(R.string.lock_mode_guide_button_text));
		mTipDialog.setLeftBtnVisibility(false);
		mTipDialog.setRightBtnListener(new LEOAlarmDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				LockScreenActivity.this.finish();
			}
		});
		mTipDialog.show();
	}

	private void handleLoadAd() {
		//防止重新进入时图标透明度为0
		int type = AppMasterPreference.getInstance(LockScreenActivity.this).getLockType();
		if (type == LockFragment.LOCK_TYPE_PASSWD) {
			PasswdLockFragment plf = (PasswdLockFragment) mLockFragment;
			View icon = plf.getIconView();
			if (icon != null) {
				icon.setAlpha(1.0f);
			}
			View hint = plf.getPasswdHint();
			if (hint != null) {
				hint.setAlpha(1.0f);
			}
		} else {
			GestureLockFragment glf = (GestureLockFragment) mLockFragment;
			View icon = glf.getIconView();
			if (icon != null) {
				icon.setAlpha(1.0f);
			}
		}

		LeoLog.e("poha", AppMasterPreference.getInstance(this).getLockBannerADShowProbability()
				+ ":large banner show probability");

		if (AppMasterPreference.getInstance(this).getLockBannerADShowProbability() > 0
				&& NetWorkUtil.isNetworkAvailable(getApplicationContext()) && mBannerContainer != null
				&& mLockMode == LockManager.LOCK_MODE_FULL && getWindowWidth() > 240) {
			loadAD(mForceLoad);
		} else if (AppMasterPreference.getInstance(this).getADShowType() == 3
				&& NetWorkUtil.isNetworkAvailable(getApplicationContext()) && mADAnimalEntry != null
				&& mLockMode == LockManager.LOCK_MODE_FULL) {
			mADAnimalEntry.setVisibility(View.VISIBLE);
			if (SHOW_AD_TYPE != AD_TYPE_JUMP && SHOW_AD_TYPE != AD_TYPE_SHAKE) {
				startShakeRotateAnimation(true);
			}
		}

		setMobvistaIcon();
	}

	@Override
	protected void onResume() {
		LeoLog.d(TAG, "onResume...");
		mCanTakePhoto = true;
		whichTypeShow();
		LeoLog.d("HomeReceiver_Lock", "onresume! tryHideToast has clicked? = " + mHasClickGoGrantPermission);
//        if (mHasClickGoGrantPermission) {
//            tryHidePermissionGuideToast();//注意tryHidePermissionGuideToast要在tryShowNoPermissionTip方法之前
//        }

        boolean isAirsigOn = LeoSettings.getBoolean(AirSigActivity.AIRSIG_SWITCH, false);
        if (!isAirsigOn) {
            tryShowNoPermissionTip();
        }

		// handleLoadAd();

		// 每次返回界面时，隐藏下方虚拟键盘，解决华为部分手机上每次返回界面如果之前有虚拟键盘会上下振动的bug
		// getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		// handlePretendLock(); 貌似oncreate里的init方法已经执行了，容易曹成内存泄露
		if (!mMissingDialogShowing) {

			// boolean lockThemeGuid = checkNewTheme();

			if (mLockMode == LockManager.LOCK_MODE_FULL) {
                /*
                 * tell PushUIHelper than do not show dialog when lockscreen is
                 * shown
                 */
				PushUIHelper.getInstance(getApplicationContext()).setIsLockScreen(true);
			}
			AppMasterPreference.getInstance(this).setUnlocked(false);
		}
		int adShowNumber = AppMasterPreference.getInstance(LockScreenActivity.this)
				.getADShowType();
        /* 解决超人在有伪装情况下，提前运行动画的问题 */
		if (getPretendFragment() == null && adShowNumber == 5) {
			LeoEventBus.getDefaultBus().post(
					new SubmaineAnimEvent(EventId.EVENT_SUBMARINE_ANIM, "no_camouflage_lock"));
		}
		super.onResume();
		SDKWrapper.addEvent(this, SDKWrapper.P1, "tdau", "app");
	}

//    private void tryHidePermissionGuideToast() {
//        if (mPermissionGuideToast != null && mPermissionGuideToast.isShowing()) {
////            Toast.makeText(LockScreenActivity.this, "hide when on resume", Toast.LENGTH_SHORT).show();
//            mHasClickGoGrantPermission = false;
//            mPermissionGuideToast.hide();
//        }
//    }

	private void tryShowNoPermissionTip() {
		LeoLog.i("Tip", "try show");
		LeoLog.i("Tip", "Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
		LeoLog.i("Tip", "TaskDetectService.sDetectSpecial = " + TaskDetectService.sDetectSpecial);
		LeoLog.i("Tip", "BuildProperties.isLenoveModel() = " + BuildProperties.isLenoveModel());
//        if (1 == 1) {
		int[] size = Utilities.getScreenSize(LockScreenActivity.this);
//        Toast.makeText(LockScreenActivity.this, "size[1] = " + size[1], Toast.LENGTH_SHORT).show();
		if (Build.VERSION.SDK_INT >= 21 && !BuildProperties.isLenoveModel() && size[1] >= 800) {
			ProcessDetectorUsageStats state = new ProcessDetectorUsageStats();
//            if (2 == 2) {
			if (!state.checkAvailable()) {
				LeoLog.i("Tip", "unAvailable");
				if (mVPermissionTip == null) {
					ViewStub vs = (ViewStub) findViewById(R.id.vs_permission_tip);
					mVPermissionTip = vs.inflate();
				}
				mRlNoPermission = (RelativeLayout) mVPermissionTip.findViewById(R.id.rl_nopermission_tip);
				mRlNoPermission.setOnClickListener(LockScreenActivity.this);
				mRlNoPermission.setVisibility(View.VISIBLE);
				mTvPermissionTip = (TextView) mVPermissionTip.findViewById(R.id.tv_nopermission_tip);
				SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "gd_wcnts", "gd_tips_show");
				String tip = mPt.getString(PrefConst.KEY_APP_USAGE_STATE_GUIDE_STRING);
				if (!TextUtils.isEmpty(tip)) {
					mTvPermissionTip.setText(tip);
				}
			} else {
				if (mRlNoPermission != null) {
					mRlNoPermission.setVisibility(View.GONE);
				}
				if (mHasClickGoGrantPermission) {
					SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "gd_wcnts", "gd_tips_finish");
					mHasClickGoGrantPermission = false;
				}
			}
		} else {
			if (mRlNoPermission != null) {
				mRlNoPermission.setVisibility(View.GONE);
			}
			if (mHasClickGoGrantPermission) {
				SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "gd_wcnts", "gd_tips_finish");
				mHasClickGoGrantPermission = false;
			}
		}
	}

	private void whichTypeShow() {
		mHaveNewThings = AppMasterPreference.getInstance(this)
				.getIsADAppwallNeedUpdate();
		mLastTime = AppMasterPreference.getInstance(LockScreenActivity.this)
				.getAdClickTime();
		isClickJump = AppMasterPreference.getInstance(LockScreenActivity.this)
				.getJumpIcon();
		long mNowTime = System.currentTimeMillis();
		if ((mLastTime != 0 && mNowTime - mLastTime > CLICK_OVER_DAY) || mHaveNewThings) {// shake
			SHOW_AD_TYPE = AD_TYPE_SHAKE;
		} else { // jump
			if (!isClickJump && !clickShakeIcon) {
				SHOW_AD_TYPE = AD_TYPE_JUMP;
			} else {
				SHOW_AD_TYPE = AD_TYPE_STAY;
			}
		}
	}

	@SuppressLint("NewApi")
	private void startShakeRotateAnimation(boolean isFirstIn) {
		mADAnimalEntry.setBackgroundResource(R.drawable.alien_bg1);

		RotateAnimation reverse = new RotateAnimation(0f, 16f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
		reverse.setDuration(500);
		reverse.setFillAfter(true);
		if (isFirstIn) {
			reverse.setStartOffset(500);
		}
		mADAnimalEntry.setAnimation(reverse);
		reverse.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				RotateAnimation reverse1 = new RotateAnimation(16f, -14f,
						Animation.RELATIVE_TO_SELF,
						0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
				reverse1.setFillAfter(true);
				reverse1.setDuration(600);
				mADAnimalEntry.setAnimation(reverse1);
				reverse1.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						RotateAnimation reverse2 = new RotateAnimation(-14f, 12f,
								Animation.RELATIVE_TO_SELF,
								0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
						reverse2.setFillAfter(true);
						reverse2.setDuration(700);
						mADAnimalEntry.setAnimation(reverse2);
						reverse2.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {

							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								RotateAnimation tozero = new RotateAnimation(12f, 0f,
										Animation.RELATIVE_TO_SELF,
										0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
								tozero.setFillAfter(true);
								tozero.setDuration(750);
								mADAnimalEntry.setAnimation(tozero);
								tozero.setAnimationListener(new AnimationListener() {

									@Override
									public void onAnimationStart(Animation animation) {

									}

									@Override
									public void onAnimationRepeat(Animation animation) {

									}

									@Override
									public void onAnimationEnd(Animation animation) {
										interupAinimation = false;
										mADAnimalEntry.clearAnimation();
										Message msg = new Message();
										msg.what = SHOW_RED_MAN;
										mHandler.sendEmptyMessageAtTime(msg.what,
												500);
									}
								});
								if (interupAinimation) {
									mADAnimalEntry.clearAnimation();
									interupAinimation = false;
								} else {
									tozero.start();
								}
							}
						});
						if (interupAinimation) {
							mADAnimalEntry.clearAnimation();
							interupAinimation = false;
						} else {
							reverse2.start();
						}
					}
				});
				if (interupAinimation) {
					mADAnimalEntry.clearAnimation();
					interupAinimation = false;
				} else {
					reverse1.start();
				}
			}
		});
		reverse.start();
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		try {
			super.onRestoreInstanceState(savedInstanceState);
		} catch (Exception e) {
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		LeoLog.d(TAG, "onStart...");
		handleLoadAd();

		mForceLoad = false;
	}

	@Override
	protected void onPause() {
		PushUIHelper.getInstance(getApplicationContext())
				.setIsLockScreen(false);
		super.onPause();
		LeoLog.d(TAG, "onPause...");
	}

	/**
	 * <b>Note</b> if lock mode is changed from LockManager.LOCK_MODE_PURE to
	 * LockManager.LOCK_MODE_FULL, just restart screen pager
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		LeoLog.d(TAG, "onNewIntent...");
		if (mLockMode == LockManager.LOCK_MODE_PURE && intent.getIntExtra(EXTRA_LOCK_MODE,
				LockManager.LOCK_MODE_FULL) == LockManager.LOCK_MODE_FULL) {
			finish();
			startActivity(intent);

			return;
		}

		mQuickLockMode = intent.getBooleanExtra("quick_lock_mode", false);
		mFromQuickGesture = intent.getBooleanExtra("from_quick_gesture", false);
		if (!mFromQuickGesture) {
			mFromHome = intent.getBooleanExtra("from_home", false);
		}

		if (mQuickLockMode) {
			mQuickModeName = intent.getStringExtra("lock_mode_name");
			mQuiclModeId = intent.getIntExtra("lock_mode_id", -1);
			// home mode replace unlock-all mode
			if (mQuiclModeId == 0) {
				mQuiclModeId = 3;
			}
		}
		if (mQuickLockMode) {
			mLockedPackage = getPackageName();
			mTtileBar.setTitle(R.string.change_lock_mode);
		} else {
//            mTtileBar.setTitle(R.string.app_name);
            setTiltleBarInfo(getPackageName());
        }

        if (mLockedPackage == null) {
            mLockedPackage = getPackageName();
        }

        String newLockedPkg = intent.getStringExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG);
        if (!TextUtils.equals(newLockedPkg, mLockedPackage)) {
            mLockedPackage = newLockedPkg;
            if (mLockedPackage == null) {
                mLockedPackage = getPackageName();
            }
            if (mPretendFragment != null) {
                mPretendLayout.setVisibility(View.GONE);
                mLockLayout.setVisibility(View.VISIBLE);
            }

            // change background
            if (mLockMode == LockManager.LOCK_MODE_FULL) {
                if (!ThemeUtils.checkThemeNeed(this)) {

                    Drawable bd = getBd(mLockedPackage);
                    setAppInfoBackground(bd);
                } else {

                    int type = mLockFragment.getUnlockType();
                    if (type == AirSigSettingActivity.AIRSIG_UNLOCK) {
                        if (mLockFragment instanceof GestureLockFragment) {
                            LeoLog.d("testTheme", "go this GestureLockFragment");
                            GestureLockFragment fragment = (GestureLockFragment) mLockFragment;
                            fragment.changeBg(true, mLockedPackage);
                        } else {
                            LeoLog.d("testTheme", "go this PasswdLockFragment");
                            PasswdLockFragment fragment = (PasswdLockFragment) mLockFragment;
                            fragment.changeBg(true, mLockedPackage);
                        }
                    }

                }
                if (!mLockedPackage.equals(getPackageName())) {
                    createLoackAppInfoView(mLockedPackage);
                } else {
                    removeLoackAppInfoView();
                }
            }

            mLockFragment.onLockPackageChanged(mLockedPackage);
            LeoLog.d(TAG, "onNewIntent" + "     mToPackage = " + mLockedPackage);
            if (mPretendFragment == null) {
                // 解决Fragment内存泄露
                mPretendFragment = getPretendFragment();
            }

            if (mLockedPackage.equals(SwitchGroup.WIFI_SWITCH) ||
                    mLockedPackage.equals(SwitchGroup.BLUE_TOOTH_SWITCH)) {
                mPretendFragment = null;
            }

            boolean showPretend = !mPrivateLockPck.equals(mLockedPackage);
            if (mPretendFragment != null && showPretend) { // ph


                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction tans;
                mPretendLayout = (RelativeLayout) findViewById(R.id.pretend_layout);
                mLockLayout.setVisibility(View.GONE);
                mPretendLayout.setVisibility(View.VISIBLE);
                tans = fm.beginTransaction();
                tans.replace(R.id.pretend_layout, mPretendFragment);
                tans.commitAllowingStateLoss();
            }
            if (mPretendFragment != null && showPretend) {
                mLockLayout.setVisibility(View.GONE);
                mPretendLayout.setVisibility(View.VISIBLE);
                if (mPretendFragment instanceof PretendAppErrorFragment) {
                    String tip = "";
                    PackageManager pm = getPackageManager();
                    try {
                        String lab = AppUtil.getAppLabel(pm, mLockedPackage);
                        tip = getString(R.string.pretend_app_error, lab);
                    } catch (Exception e) {
                        tip = getString(R.string.weizhuang_error_notice);
                        e.printStackTrace();
                    }

                    ((PretendAppErrorFragment) mPretendFragment).setErrorTip(tip);
                }
            }
        }
        mLockFragment.setPackage(mLockedPackage);
        mLockFragment.onNewIntent();
        checkOutcount();
        super.onNewIntent(intent);
    }

    private void checkOutcount() {
//        int outcountTime = mLockManager.getOutcountTime(mLockedPackage);
//        if (outcountTime > 0) {
//            mLockManager.filterPackage(getPackageName(), 200);
//            Intent intent = new Intent(this, WaitActivity.class);
//            intent.putExtra("outcount_time", 10 - outcountTime / 1000);
//            startActivity(intent);
//        }
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mRestartForThemeChanged = intent.getBooleanExtra("from_theme_change", false);
		mQuickLockMode = intent.getBooleanExtra("quick_lock_mode", false);
		mFromHome = intent.getBooleanExtra("from_home", false);
		mFromQuickGesture = intent.getBooleanExtra("from_quick_gesture", false);
		if (mQuickLockMode) {
			mQuickModeName = intent.getStringExtra("lock_mode_name");
			mQuiclModeId = intent.getIntExtra("lock_mode_id", -1);

			// home mode replace unlock-all mode
			if (mQuiclModeId == 0) {
				mQuiclModeId = 3;
			}
		}
		if (mQuickLockMode) {
			mLockedPackage = getPackageName();
		} else {
			mLockedPackage = intent.getStringExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG);
		}

		if (mLockedPackage == null) {
			mLockedPackage = getPackageName();
		}

		mLockMode = intent.getIntExtra(EXTRA_LOCK_MODE,
				LockManager.LOCK_MODE_FULL);

		int type = AppMasterPreference.getInstance(this).getLockType();
		if (type == LockFragment.LOCK_TYPE_PASSWD) {
			mLockFragment = new PasswdLockFragment();
		} else {
			mLockFragment = new GestureLockFragment();
		}

		if (mLockMode == LockManager.LOCK_MODE_FULL) {
			if (!ThemeUtils.checkThemeNeed(this)) {


				Drawable bd = getBd(mLockedPackage);
				setAppInfoBackground(bd);

				if (mLockMode == LockManager.LOCK_MODE_FULL) {
					if (mTtileBar == null) {
						mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
					}
					if (!mQuickLockMode) {
						setTiltleBarInfo(getPackageName());
					}
				}
			} else {
				if (!mQuickLockMode) {
					if (mTtileBar == null) {
						mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
					}
					setTiltleBarInfo(getPackageName());
				}
			}
			if (!mLockedPackage.equals(getPackageName())) {
				createLoackAppInfoView(mLockedPackage);
			}
		}
		mLockTitle = intent.getStringExtra(EXTRA_LOCK_TITLE);
		mLockFragment.setLockMode(mLockMode);
		mLockFragment.setPackage(mLockedPackage);

        /* SDK: mark user what to unlock which app */
        if (mLockMode == LockManager.LOCK_MODE_FULL) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "access_locked_app",
                    mLockedPackage);
        }

        LeoLog.d("LockScreenActivity", "mToPackage = " + mLockedPackage);
    }


    private Drawable getBd(String mPackageName) {
        //wifi && blueTooth lock
        Drawable bd;
        if (mPackageName.equals(SwitchGroup.WIFI_SWITCH)) {
            bd = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_wifi);
        } else if (mPackageName.equals(SwitchGroup.BLUE_TOOTH_SWITCH)) {
            bd = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_bluetooth);
        } else {
            bd = AppUtil.getAppIcon(
                    getPackageManager(), mPackageName);
        }

        if (bd == null) {
            bd = AppUtil.getAppIcon(
                    getPackageManager(), getPackageName());
        }


        return bd;
    }

    public void setAppInfoBackground(Drawable drawable) {
        if (drawable != null) {
            int h = drawable.getIntrinsicHeight() * 9 / 10;
            int w = h * 3 / 5;
            if (h > 0 && w > 0) {
                mAppBaseInfoLayoutbg = Bitmap.createBitmap(w, h,
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mAppBaseInfoLayoutbg);
                canvas.drawColor(Color.WHITE);
                drawable.setBounds(-(drawable.getIntrinsicWidth() - w) / 2,
                        -(drawable.getIntrinsicHeight() - h) / 2,
                        (drawable.getIntrinsicWidth() - w) / 2 + w,
                        (drawable.getIntrinsicHeight() - h) / 2 + h);
                drawable.draw(canvas);
                canvas.drawColor(Color.argb(70, 0, 0, 0));
                try {
                    mAppBaseInfoLayoutbg = FastBlur.doBlur(mAppBaseInfoLayoutbg, 25, true);
                    mLockLayout.setBackgroundDrawable(new BitmapDrawable(mAppBaseInfoLayoutbg));
                } catch (Error e) {
                }
            }
        }
    }


    private void setQuickLockModeTiltleBarInfo(LockMode targetMode) {
        mTtileBar.setTitle(R.string.change_lock_mode);
        Drawable iconDraw = targetMode.getModeDrawable();
        if (iconDraw != null) {
            int w = getResources().getDimensionPixelSize(R.dimen.fragment_lock_tilte_icon_width);
            iconDraw.setBounds(0, 0, w, w);
            mTtileBar.getTitleView().setCompoundDrawables(/*new ScaleDrawable(iconDraw, Gravity.CENTER, w, w).getDrawable()*/iconDraw, null, null, null);
            mTtileBar.getTitleView().setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.fragment_lock_tilte_icon_space));
        }
    }

    private void setTiltleBarInfo(String pkg) {
        mTtileBar.setTitle(AppUtil.getAppLabel(getPackageManager(), pkg));
        Drawable iconDraw = AppUtil.getAppIconDrawble(pkg);
        if (iconDraw != null) {
            int w = getResources().getDimensionPixelSize(R.dimen.fragment_lock_tilte_icon_width);
            iconDraw.setBounds(0, 0, w, w);
            mTtileBar.getTitleView().setCompoundDrawables(/*new ScaleDrawable(iconDraw, Gravity.CENTER, w, w).getDrawable()*/iconDraw, null, null, null);
            mTtileBar.getTitleView().setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.fragment_lock_tilte_icon_space));
        }
    }

    private void createLoackAppInfoView(String pkg) {
        if (mLockAppTitleView == null) {
            mLockAppTitleView = new TextView(this);
            mLockAppTitleView.setClickable(false);
            mLockAppTitleView.setEllipsize(TextUtils.TruncateAt.END);
            mLockAppTitleView.setGravity(Gravity.CENTER);
            mLockAppTitleView.setPadding(DipPixelUtil.dip2px(this, 12), 0, DipPixelUtil.dip2px(this, 5), 0);
            mLockAppTitleView.setSingleLine();
            mLockAppTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            mLockAppTitleView.setTextColor(getResources().getColor(R.color.white));
            if (mTtileBar == null) {
                mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
            }
            mTtileBar.getTitleContainer().addView(mLockAppTitleView);
            mLockAppTitleView.setAlpha(0);
        }

        //wifi && bluetooth lock
        String text;
        Drawable iconDraw;
        if (mLockedPackage.equals(SwitchGroup.WIFI_SWITCH)) {
            text = this.getString(R.string.app_lock_list_switch_wifi);
            iconDraw = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_wifi);
        } else if (mLockedPackage.equals(SwitchGroup.BLUE_TOOTH_SWITCH)) {
            text = this.getString(R.string.app_lock_list_switch_bluetooth);
            iconDraw = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_bluetooth);
        } else {
            text = AppUtil.getAppLabel(getPackageManager(), pkg);
            iconDraw = AppUtil.getAppIconDrawble(pkg);
        }

        mLockAppTitleView.setText(text);

        if (iconDraw != null) {
            int w = getResources().getDimensionPixelSize(R.dimen.fragment_lock_tilte_icon_width);
            iconDraw.setBounds(0, 0, w, w);
            mLockAppTitleView.setCompoundDrawables(iconDraw, null, null, null);
            mLockAppTitleView.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.fragment_lock_tilte_icon_space));
        }
    }

    private void removeLoackAppInfoView() {
        if (mLockAppTitleView != null) {
            if (mTtileBar == null) {
                mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
            }
            mTtileBar.getTitleContainer().removeView(mLockAppTitleView);
            mLockAppTitleView = null;
            TextView tv = mTtileBar.getTitleView();
            if (tv != null) {
                tv.setAlpha(1);
            }
        }
    }

    private void setLockAppInfoViewVisible(boolean visible) {
        if (mLockAppTitleView != null) {
            TextView tv = mTtileBar.getTitleView();
            if (visible) {
                mLockAppTitleView.setAlpha(1);
                if (tv != null) {
                    tv.setAlpha(0);
                }
            } else {
                mLockAppTitleView.setAlpha(0);
                if (tv != null) {
                    tv.setAlpha(1);
                }
            }
        }
    }

    public void shakeIcon(Animation animation) {
        if (mLockAppTitleView != null && mBannerContainer.getCurrentItem() > 0) {
            mLockAppTitleView.startAnimation(animation);
        }
        if (mTtileBar.getTitleView().getAlpha() == 1 && mBannerContainer.getCurrentItem() > 0) {
            mTtileBar.getTitleView().startAnimation(animation);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLockManager.setPauseScreenonLock(false);
        if (mAppBaseInfoLayoutbg != null) {
            mAppBaseInfoLayoutbg.recycle();
            mAppBaseInfoLayoutbg = null;
        }

//        try {
//            if (wallAd != null) {
//                wallAd.release();
//                wallAd = null;
//            }
//        } catch (Exception e) {
//        }
        LeoLog.d(TAG, "onDestroy...");
        LeoEventBus.getDefaultBus().unregister(this);
        mLockFragment.setShowText(false);

    }


    @Override
    protected void onStop() {

        LeoLog.d(TAG, "onStop...");

        super.onStop();
        if (mLockFragment != null) {
            mLockFragment.onActivityStop();
        }

        /* AM-3907 规避多次添加广告 */
        try {
            ThreadManager.executeOnSubThread(new Runnable() {
                @Override
                public void run() {
                    // 避免产生anr，放到子线程
                    for (String id : mBannerAdids) {
                        LeoLog.d("LEOAdEngine", "id : =" + id + "; mDidLoadAd = " + mDidLoadAd);
                        //LEOAdEngine.getInstance(LockScreenActivity.this.getApplicationContext()).release(id);
                        // 3.3.2 封装Max与Mob SDK
                        if (mDidLoadAd) {
                            ADEngineWrapper.getInstance(LockScreenActivity.this.getApplicationContext()).releaseAd(mAdSource, id, mAdView);
                        }
                    }
                }
            });

            if (mImageFetcher != null) {
                mImageFetcher.destroy();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mBannerContainer != null) {
            try {
                mBannerContainer.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void finish() {
        LeoLog.d("LS2", "do finish");
        if (!isFinishing()) {
            super.finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LeoLog.d(TAG, "onRestart...");
        /**
         * dont change it, for lock theme
         */
        if (mRestartForThemeChanged) {
            Intent intent = getIntent();
            finish();
            mRestartForThemeChanged = false;
            intent.putExtra("from_theme_change", true);
            intent.putExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG, mLockedPackage);
            startActivity(intent);
        }
    }

    private void initUI() {
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        if (mLockMode == LockManager.LOCK_MODE_FULL) {
            mTtileBar.setBackArrowVisibility(View.GONE);

            if (mQuickLockMode) {
                List<LockMode> modes = mLockManager.getLockMode();
                LockMode targetMode = null;
                for (LockMode lockMode : modes) {
                    if (lockMode.modeId == mQuiclModeId) {
                        targetMode = lockMode;
                        break;
                    }
                }
                if (targetMode != null) {
                    setQuickLockModeTiltleBarInfo(targetMode);
                } else {
                    setTiltleBarInfo(getPackageName());
                }
            }
        } else {
            mTtileBar.setSelfBackPressListener(this);
            if (TextUtils.isEmpty(mLockTitle)) {
                mTtileBar.setTitle(R.string.app_lock);
                //解锁界面绘制介绍
                mLockFragment.setShowText(true);
            } else {
                mTtileBar.setTitle(mLockTitle);
            }
            mTtileBar.setHelpSettingVisiblity(View.INVISIBLE);
        }

        mTtileBar.setOptionImage(R.drawable.ic_toolbar_more);

        mTtileBar.setBackgroundResource(R.color.transparent);
        mTtileBar.setOptionImageVisibility(View.VISIBLE);
        mTtileBar.setOptionImagePadding(DipPixelUtil.dip2px(this, 5));
        mTtileBar.setOptionListener(this);

        mAdIconRedTip = (ImageView) findViewById(R.id.gift_red_tip);
        mMrlGift = (RippleView) findViewById(R.id.mr_gift);
        mMrlGift.setOnClickListener(this);

        mAdIcon = (ImageView) findViewById(R.id.icon_ad_layout);
        if (AppMasterPreference.getInstance(this).getIsLockAppWallOpen() > 0) {
            ((View) mAdIcon.getParent()).setVisibility(View.VISIBLE);
            mAdIcon.setVisibility(View.VISIBLE);
            mAdIcon.setOnClickListener(this);
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tans = fm.beginTransaction();
        tans.replace(R.id.fragment_contain, mLockFragment);
        tans.commit();

        handlePretendLock();

        initAD();
    }

    /* 初始化广告UI */
    private void initAD() {
//        mBannerParent = (FrameLayout) findViewById(R.id.large_adbanner_parent);
		mBannerContainer = (ViewPager) findViewById(R.id.large_adbanner_container);
		mBannerContainer.setPageMargin(getResources().getDimensionPixelSize(R.dimen.fragment_lock_large_banner_spacing));
		mBannerContainer.setOffscreenPageLimit(2);
		mBannerContainer.setOverScrollMode(View.OVER_SCROLL_NEVER);
	}

	private void loadAD(boolean forceLoad) {
		mDidLoadAd = true;
		mBannerContainer.setVisibility(View.VISIBLE);
		mAdUnitIdList.clear();
		mMobvistaListenerList.clear();
		mAdMap.clear();

		if (mImageFetcher != null) {
			mImageFetcher.destroy();
		}

		otherAdSwitcher = false;
		if (mAdapterCycle != null) {
			mAdapterCycle.clearView();
			mAdapterCycle = null;
//            mAdapterCycle.getViews().clear();
		}
		mBannerContainer.removeAllViews();
        /*调用sdk前，明确是哪个sdk，mobvista or max ? */
		AppMasterPreference sp = AppMasterPreference.getInstance(this.getApplicationContext());
		mAdSource = sp.getLockBannerAdConfig();
		long start = SystemClock.elapsedRealtime();
		asyncLoadAd(forceLoad);
		LeoLog.d(TAG, "TsCost, loadAD..." + (SystemClock.elapsedRealtime() - start));
	}

	/**
	 * 异步加载广告，等到图片loading结束后才显示处理
	 */
	private PreviewImageFetcher mImageFetcher;

	private class WpdListener implements ADEngineWrapper.WrappedAdListener {
		private String unitId;
		ADEngineWrapper wrapperAdEngine;

		public WpdListener(String unitId) {
			this.unitId = unitId;
			wrapperAdEngine = ADEngineWrapper.getInstance(LockScreenActivity.this);
		}

		/**
		 * 广告请求回调
		 *
		 * @param code     返回码，如ERR_PARAMS_NULL
		 * @param campaigns 请求成功的广告结构体，失败为null
		 * @param msg      请求失败sdk返回的描述，成功为null
		 */
		@Override
		public synchronized void onWrappedAdLoadFinished(int code, final List<WrappedCampaign> campaigns, String msg) {
			if (code != MobvistaEngine.ERR_OK) {
				return;
			}
			if (campaigns != null) {
				mAdMap.put(unitId, campaigns.get(0));

                        /* 开始load 广告大图 */
				LeoLog.d("LockScreenActivity_AD_DEBUG", "Ad Data for [" + unitId + "] ready: " + campaigns.get(0).getAppName());
				mImageFetcher.loadBitmap(campaigns.get(0).getImageUrl(), new PreviewImageFetcher.ImageFetcherListener() {
					@Override
					public void onBitmapLoadStarted(String url) {
						LeoLog.d("LockScreenActivity_AD_DEBUG", "[" + unitId + "]start to load preview for: " + url);

						SDKWrapper.addEvent(LockScreenActivity.this.getApplicationContext(),
								"max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + unitId + " prepare for load image", mAdSource, null);
					}

					@Override
					public void onBitmapLoadDone(final String url, final Bitmap loadedImage) {
						SDKWrapper.addEvent(LockScreenActivity.this.getApplicationContext(),
								"max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + unitId + " image size: " + loadedImage.getByteCount(), mAdSource, null);

						ThreadManager.executeOnUiThread(new Runnable() {
							@Override
							public void run() {
								{
									LeoLog.d("LockScreenActivity_AD_DEBUG", "[" + unitId + "]onLoadingComplete for: " + url);
									// AM-4043 加空指针保护
									if (loadedImage == null) {
										return;
									}
									WrappedCampaign wrappedCampaign = mAdMap.get(unitId);
									if (wrappedCampaign == null
											|| wrappedCampaign.getAppName() == null
											|| wrappedCampaign.getAppName().equalsIgnoreCase("")) {
										mAdMap.remove(unitId);
										return;
									}

									if (mAdUnitIdList == null) {
										mAdUnitIdList = new ArrayList<String>();
									}
									if (unitId.equals(mBannerAdids[0])) {
										mAdUnitIdList.add(0, unitId);
									} else {
										mAdUnitIdList.add(unitId);
									}

								/*校验此广告是否用第一个id来申请的*/
									if (unitId.equals(mBannerAdids[0])) {
                                    /*如果是则将其他两个广告显示开光打开*/
										otherAdSwitcher = true;
									}
                                /* 如果是第二或者第三个id申请的广告 则检查广告开关是否被打开了 */
									if (!unitId.equals(mBannerAdids[0])) {
                                    /*如果广告开关没有被打开，则直接退出*/
										if (!otherAdSwitcher) {
											return;
										}
                                    /*if (isRedundant(unitId, campaign)) {
                                        *//* 与之前的广告重复了，不添加到UI而且去掉广告数据 *//*
                                        removeAdData(unitId);
                                        return;
                                    }*/
									}

									if (mBannerContainer == null) {
										mBannerContainer = (ViewPager) findViewById(R.id.large_adbanner_container);
									}
									if (mAdapterCycle == null) {
										mBannerContainer.setVisibility(View.INVISIBLE);
                                    /* 去除重复广告之后再填充UI */
										removeRedundantAds();
										mAdapterCycle = new AdBannerAdapter(LockScreenActivity.this, mBannerContainer, mAdUnitIdList, wrapperAdEngine);
										mBannerContainer.setAdapter(mAdapterCycle);
										if ((int) (Math.random() * (10) + 1) <= AppMasterPreference.getInstance(LockScreenActivity.this).getLockBannerADShowProbability()) {
                                        /* 第一个广告直接出现 */
											mBannerContainer.setCurrentItem(1, false);
											mAdapterCycle.setLasterSlectedPage(1);
											mBannerContainer.setVisibility(View.VISIBLE);
											showAdAnimaiton(unitId);
											// 3.6版本，6秒后消失的逻辑去掉
											// delayBannerHideAnim();
											hideIconAndPswTips();

										} else {
                                        /* 广告隐藏在右边 */
											mBannerContainer.setVisibility(View.VISIBLE);
											mBannerContainer.setCurrentItem(0, false);
											mAdapterCycle.setLasterSlectedPage(0);
										}
									} else {
                                    /* AM-3907 规避多次添加广告 */
                                    /*if (mAdapterCycle.getCount() == mBannerAdids.length) {
                                        return;
                                    }*/
										removeRedundantAds();
                                    /* 如果没被去重干掉，再加进来 */
										if (mAdUnitIdList.contains(unitId)) {
											mAdapterCycle.addItem(unitId);
										}
									}

								}
							}
						});
					}

					@Override
					public void onBitmapLoadFailed(String url) {
						LeoLog.d("LockScreenActivity_AD_DEBUG", "onLoadingFailed for: " + url);
						SDKWrapper.addEvent(LockScreenActivity.this.getApplicationContext(),
								"max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + unitId + " load image failed", mAdSource, null);
					}

					@Override
					public void onBitmapLoadCancelled(String url) {
						LeoLog.d("LockScreenActivity_AD_DEBUG", "[" + unitId + "] onBitmapLoadCancelled for: " + url);
					}
				});

			}
		}

		/**
		 * 广告点击回调
		 *
		 * @param campaign
		 */
		@Override
		public void onWrappedAdClick(WrappedCampaign campaign, final String unitID) {
			SDKWrapper.addEvent(LockScreenActivity.this.getApplicationContext(),
					"max_ad", SDKWrapper.P1, "ad_click", "ad pos: " + unitID + " click", mAdSource, null);
			ThreadManager.executeOnUiThread(new Runnable() {
				@Override
				public void run() {
					mForceLoad = true;
					if (unitID != null && unitID.equals(mBannerAdids[0])) {
						otherAdSwitcher = false;
					}

					ADEngineWrapper.getInstance(LockScreenActivity.this.getApplicationContext()).removeMobAdData(mAdSource, unitID, mAdView);
					mAdapterCycle.clearView();
					mBannerContainer.removeAllViews();
					new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
                            /*boolean cacheEmpty = ADEngineWrapper.getInstance(LockScreenActivity.this.getApplicationContext()).isADCacheEmpty(mAdSource);
                            if (cacheEmpty) {
                                loadAD(true);
                            }*/
							loadAD(true);
						}
					}, 500);
					if (mAdapterCycle.getViews() == null) {
						mBannerContainer.setVisibility(View.GONE);
					}
					showIconAndPswTips();
				}
			});
		}
	}

	;

	private void asyncLoadAd(boolean forceLoad) {
		if (mImageFetcher == null) {
			mImageFetcher = new PreviewImageFetcher();
		}

		int[] sources = new int[mBannerAdids.length];
		Arrays.fill(sources, mAdSource);

		ADEngineWrapper.WrappedAdListener[] listeners = new ADEngineWrapper.WrappedAdListener[mBannerAdids.length];
		for (int i = 0; i < mBannerAdids.length; i++) {
			listeners[i] = new WpdListener(mBannerAdids[i]);
		}
		ADEngineWrapper.getInstance(this).loadAdBatch(sources, mBannerAdids, listeners, forceLoad);

	}

	private void removeRedundantAds() {
		ArrayList<String> list = new ArrayList<String>();
		for (String id : mAdUnitIdList) {
			WrappedCampaign wc = mAdMap.get(id);
			if (wc != null) {
				boolean found = false;
				for (String newId : list) {
					WrappedCampaign newWc = mAdMap.get(newId);
					if (newWc == null) {
						continue;
					}
					if (wc.getAppName().equalsIgnoreCase(newWc.getAppName())
							|| wc.getImageUrl().equalsIgnoreCase(newWc.getImageUrl())
							|| wc.getDescription().equalsIgnoreCase(newWc.getDescription())) {
						found = true;
						break;
					}
				}
				if (!found) {
					list.add(id);
				}
			}
		}

		mAdUnitIdList = list;
	}

	/**
	 * 去重添加
	 * @param unitId
	 * @param campaign
	 * @return 是否添加新的数据
	 */
    /*private boolean isRedundant(String unitId, Campaign campaign) {
        LeoLog.i("asyncLoadAd", "ad title = " + campaign.getAppName());
        for (String key : mAdMap.keySet()) {
            Campaign data = mAdMap.get(key);
            if (data.getAppName().equals(campaign.getAppName())
                    || data.getImageUrl().equals(campaign.getImageUrl())
                    || data.getAppDesc().equals(campaign.getAppDesc())) {
                return false;
            }
        }
        mAdMap.put(unitId, campaign);
        return true;
    }*/

	/**
	 * 去重添加
	 *
	 * @param unitId
	 * @return 是否与之前的广告重复
	 */
//    private boolean isRedundant(String unitId, WrappedCampaign campaign) {
//        for (String key : mAdMap.keySet()) {
//            LeoLog.i("asyncLoadAd", "ad title = " + campaign.getAppName());
//            if (key.equalsIgnoreCase(unitId)) {
//                continue;
//            }
//			WrappedCampaign data = mAdMap.get(key);
//            if (data.getAppName().equals(campaign.getAppName())
//                    || data.getImageUrl().equals(campaign.getImageUrl())
//                    || data.getDescription().equals(campaign.getDescription())) {
//                return true;
//            }
//        }
//        return false;
//    }

//    private void removeAdData(String unitId) {
//        if (mAdBitmapMap != null) {
//            mAdBitmapMap.remove(unitId);
//        }
//        if (mAdUnitIdList != null) {
//            mAdUnitIdList.remove(unitId);
//        }
//    }

    /*private void resistViewAllAd(LinkedHashMap<String, Campaign> adMap) {

        for (String key : adMap.keySet()) {
        }
    }*/
	private int findTargetViewIndex(String unitId) {
		int reValue = -1;

		if (unitId != null && mAdapterCycle != null && mAdapterCycle.getViews() != null) {
			LinkedList<View> localViews = mAdapterCycle.getViews();
			String unit;
			for (View v : localViews) {
				if (v != null) {
					unit = (String) v.findViewById(R.id.ad_title).getTag();
					if (unit != null && unit.equals(mBannerAdids[0])) {
						reValue = localViews.indexOf(v);
						break;
					}
				}
			}
		}

		return reValue;
	}

	private void showAdAnimaiton(String unitId) {
		int index = findTargetViewIndex(unitId);
		if (index == -1) {
			otherAdSwitcher = false;
			return;
		}
		View animView = mAdapterCycle.getViews().get(index);

		final int itemWidth = getResources().getDimensionPixelSize(R.dimen.fragment_lock_large_banner_out_width);
		final int itemHeight = getResources().getDimensionPixelSize(R.dimen.fragment_lock_large_banner_out_height);
		int offset = (getWindowWidth() - itemWidth) / 2;
        /*mBannerContainer*/
		animView.setX(getWindowWidth() - offset);
        /*mBannerContainer*/
		animView.setScaleX(0.7f);
        /*mBannerContainer*/
		animView.setScaleY(0.7f);

		ObjectAnimator animatorTrans1 = ObjectAnimator.ofFloat(/*mBannerContainer*/animView, "translationX", getWindowWidth() - offset, -offset);
		animatorTrans1.setDuration(500);
		ObjectAnimator animatorTrans2 = ObjectAnimator.ofFloat(/*mBannerContainer*/animView, "translationX", -offset, 0);

		ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(/*mBannerContainer*/animView, "scaleX", 0.7f, 1.0f);
		ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(/*mBannerContainer*/animView, "scaleY", 0.7f, 1.0f);
		animatorScaleX.setDuration(500);
		animatorScaleY.setDuration(500);

        /*mBannerContainer*/
		animView.setPivotX(0);
        /*mBannerContainer*/
		animView.setPivotY(itemHeight / 2);
		animatorScaleX.setInterpolator(new DecelerateInterpolator());
		animatorScaleY.setInterpolator(new DecelerateInterpolator());

		final AnimatorSet animatorSet = new AnimatorSet();

		animatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				mBannerContainer.setVisibility(View.VISIBLE);
				mBannerContainer.setCurrentItem(1, false);
				mAdapterCycle.setLasterSlectedPage(1);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
			}

		});

		animatorSet.play(animatorTrans2).with(animatorScaleX).with(animatorScaleY).after(animatorTrans1);
		animatorSet.setStartDelay(300);
		animatorSet.start();

	}

	private void hideIconAndPswTips() {
		//隐藏图标和密码提示
		int type = AppMasterPreference.getInstance(this).getLockType();
		if (type == LockFragment.LOCK_TYPE_PASSWD) {
			View icon = ((PasswdLockFragment) mLockFragment).getIconView();
			if (icon != null) {
				icon.setAlpha(0.0f);
			}
			View hint = ((PasswdLockFragment) mLockFragment).getPasswdHint();
			if (hint != null) {
				hint.setAlpha(0.0f);
			}
		} else {
			View icon = ((GestureLockFragment) mLockFragment).getIconView();
			if (icon != null) {
				icon.setAlpha(0.0f);
			}
		}
	}

	private void showIconAndPswTips() {
		//隐藏图标和密码提示
		int type = AppMasterPreference.getInstance(this).getLockType();
		if (type == LockFragment.LOCK_TYPE_PASSWD) {
			((PasswdLockFragment) mLockFragment).getIconView().animate().alpha(1.0f).setDuration(200);
			((PasswdLockFragment) mLockFragment).getPasswdHint().animate().alpha(1.0f).setDuration(200);
		} else {
			((GestureLockFragment) mLockFragment).getIconView().animate().alpha(1.0f).setDuration(200);
		}
	}

	private void delayBannerHideAnim() {
		mHandler.removeMessages(LARGE_BANNER_HIDE);

		Message msg = Message.obtain();
		msg.what = LARGE_BANNER_HIDE;
		mHandler.sendMessageDelayed(msg, 6000);
	}

	private void bannerHideAnim() {
		if (mBannerContainer.getChildCount() >= 2) {
			mBannerContainer.setCurrentItem(0, true);
		}
	}

	// handle pretend lock
	private void handlePretendLock() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction tans;
		mPretendLayout = (RelativeLayout) findViewById(R.id.pretend_layout);
		if (mPretendFragment == null) {
			// 解决Fragment内存泄露
			mPretendFragment = getPretendFragment();
		}

		if (mLockedPackage.equals(SwitchGroup.WIFI_SWITCH) ||
				mLockedPackage.equals(SwitchGroup.BLUE_TOOTH_SWITCH)) {
			mPretendFragment = null;
		}

		if (mPretendFragment != null && !mRestartForThemeChanged) {
			mLockLayout.setVisibility(View.GONE);
			mPretendLayout.setVisibility(View.VISIBLE);
			tans = fm.beginTransaction();
			tans.add(R.id.pretend_layout, mPretendFragment);

			tans.commit();
		} else {
			mLockLayout.setVisibility(View.VISIBLE);
			mPretendLayout.setVisibility(View.GONE);
		}
	}

	private PretendFragment getPretendFragment() {
		if (!mPrivateLockPck.equals(mLockedPackage) && !mQuickLockMode) {


//            LeoLog.d("testLockName", "mLockedPackage : " + mLockedPackage);
//            if (mLockedPackage.equals(SwitchGroup.WIFI_SWITCH) ||
//                    mLockedPackage.equals(SwitchGroup.BLUE_TOOTH_SWITCH)) {
//                return null;
//            }


			int pretendLock = AppMasterPreference.getInstance(this).getPretendLock();
			// pretendLock = 2;
			if (pretendLock == 1) { /* app error */
				SDKWrapper
						.addEvent(this, SDKWrapper.P1, "appcover", "apperror");
				PretendAppErrorFragment paf = new PretendAppErrorFragment();

				String tip = "";
				PackageManager pm = this.getPackageManager();
				try {
					String lab = AppUtil.getAppLabel(pm, mLockedPackage);
					tip = getString(R.string.pretend_app_error, lab);
				} catch (Exception e) {
					tip = getString(R.string.weizhuang_error_notice);
					e.printStackTrace();
				}
				paf.setErrorTip(tip);
				return paf;
			} else if (pretendLock == 2) {/* unknow call */
				SDKWrapper
						.addEvent(this, SDKWrapper.P1, "appcover", "unknowcall");
				PretendAppUnknowCallFragment5 unknowcall = new PretendAppUnknowCallFragment5();
				return unknowcall;
			} else if (pretendLock == 3) {/* fingerprint */
				SDKWrapper
						.addEvent(this, SDKWrapper.P1, "appcover", "fingerprint");
				PretendAppZhiWenFragment weizhuang = new PretendAppZhiWenFragment();
				return weizhuang;
			} else if (pretendLock == 4) {
				SDKWrapper
						.addEvent(this, SDKWrapper.P1, "appcover", "beauty");
				PretendAppBeautyFragment weizhuang = new PretendAppBeautyFragment();
				return weizhuang;
			}
		}
		return null;
	}

	public void onUnlockSucceed() {
		AppMasterPreference pref = AppMasterPreference.getInstance(this);
		if (mQuickLockMode) {
			List<LockMode> modeList = mLockManager.getLockMode();
			LockMode willLaunch = null;
			for (LockMode lockMode : modeList) {
				if (mQuiclModeId == lockMode.modeId) {
					willLaunch = lockMode;
					Log.i("tag", "falg ==" + lockMode.defaultFlag);
					break;
				}
			}

			if (willLaunch == null) {
				LockMode homeMode = null;
				for (LockMode lockMode : modeList) {
					if (lockMode.defaultFlag == 3) {
						homeMode = lockMode;
						break;
					}
				}
				willLaunch = homeMode;
			}
			if (willLaunch != null) {
				LockMode lockMode = mLockManager.getCurLockMode();
				mLockManager.setCurrentLockMode(willLaunch, true);
				checkLockTip();
				SDKWrapper.addEvent(this, SDKWrapper.P1, "modeschage", "launcher");
				/** mode change tip **/
				if (null != lockMode) {
					int currentModeFlag = lockMode.defaultFlag;
					showModeActiveTip(willLaunch.defaultFlag, currentModeFlag);
					SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "modeschage",
							"shortcuts");
				} else {
					showModeActiveTip(willLaunch);
				}
				LeoEventBus.getDefaultBus().post(
						new LockModeEvent(EventId.EVENT_MODE_CHANGE, "mode changed_show_now"));
			} else {
				// Toast.makeText(this, mQuickModeName + "模式不存在, 请重试",
				// 0).show();
			}

		} else {
			/**
			 * notify LockManager
			 */
			//解锁成功的时候 已经保存完照片了 ，直接进入抓拍界面
			if (mIsPicSaved) {


				LeoLog.d("testNumber", "mLockedPackage : " + mLockedPackage);
				if (mLockedPackage.equals(SwitchGroup.WIFI_SWITCH) ||
						mLockedPackage.equals(SwitchGroup.BLUE_TOOTH_SWITCH)) {
					LeoEventBus.getDefaultBus().post(new AppUnlockEvent(mLockedPackage, AppUnlockEvent.RESULT_UNLOCK_SUCCESSFULLY));
				} else {
					LeoEventBus.getDefaultBus().post(new AppUnlockEvent(mLockedPackage, AppUnlockEvent.RESULT_UNLOCK_CANCELED));
				}

				mLockManager.filterPackage(getPackageName(), 1000);
				Intent intent = new Intent(LockScreenActivity.this, IntruderCatchedActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("needIntoHomeWhenFinish", true);
				intent.putExtra("pkgname", mLockedPackage);
				startActivity(intent);
				mIsPicSaved = false;
				mHasTakePic = false;
				finish();
				return;
				//解锁成功的时候 还没有保存完照片，将延迟标记置为true，让保存的操作执行完后自己去进入抓拍界面
			} else {
				if (mHasTakePic) {
					mPt.putBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, true);
//                    mHasTakePic = false;
					LeoLog.i("poha", "set delay true");
				}
			}
			LeoEventBus.getDefaultBus().post(
					new AppUnlockEvent(mLockedPackage,
							AppUnlockEvent.RESULT_UNLOCK_SUCCESSFULLY));
			if (mLockMode == LockManager.LOCK_MODE_FULL) {
				if (AppMasterPreference.getInstance(LockScreenActivity.this)
						.isLockerClean()) {
					Toast mToast = new Toast(this);
					LayoutInflater mLayoutInflater = LayoutInflater
							.from(LockScreenActivity.this);
					mLockClean = mLayoutInflater.inflate(
							R.layout.activity_lockclean_toast, null);
					mText = (TextView) mLockClean.findViewById(R.id.textToast);
					String textResource = getResources().getString(
							R.string.locker_clean);
					String cleanRate = String.format(textResource, mCleanRate);
					mText.setText(cleanRate);
					mToast.setGravity(Gravity.BOTTOM, 0, 66);
					mToast.setDuration(Toast.LENGTH_LONG);
					mToast.setView(mLockClean);
					mToast.show();
				}

				pref.setUnlockCount(pref.getUnlockCount() + 1);
				// quick gesture unlock count
				pref.setNewUserUnlockCount(pref.getNewUserUnlockCount() + 1);
			} else if (mLockMode == LockManager.LOCK_MODE_PURE) {
			}

			SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "unlock", "done");

		}
		mLockManager.filterPackage(mLockedPackage, 1000);
		mTtileBar.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mFromHome) { // for fix bug: AM-1904
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					startActivity(intent);
				}
				// finish self
				finish();
			}
		}, 100);

		AppMasterPreference amp = AppMasterPreference.getInstance(LockScreenActivity.this);
		amp.setLockerScreenThemeGuide(true);
		amp.setUnlocked(true);
		amp.setDoubleCheck(null);

        /* 是否为强制升级 */
		boolean isForceUpdate = AppMasterPreference.getInstance(this).getPGIsForceUpdate();
		if (!isForceUpdate) {
			ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
				@Override
				public void run() {
					UIHelper.getInstance(AppMasterApplication.getInstance())
							.unlockSuccessUpdateTip(mLockedPackage);
				}
			}, 200);
		}
        /*手机防盗，锁定手机指令，如果用户解锁成功后初始化数据*/
		PhoneSecurityManager.getInstance(this).removeAllModeLockList();
	}

	private void checkLockTip() {
		int switchCount = AppMasterPreference.getInstance(this).getSwitchModeCount();
		switchCount++;
		AppMasterPreference.getInstance(this).setSwitchModeCount(switchCount);
		List<TimeLock> timeLockList = mLockManager.getTimeLock();
		List<LocationLock> locationLockList = mLockManager.getLocationLock();
		if (switchCount == 6) {

			int timeLockCount = timeLockList.size();
			int locationLockCount = locationLockList.size();

			if (timeLockCount == 0 && locationLockCount == 0) {
				// show three btn dialog
				LEOThreeButtonDialog dialog = new LEOThreeButtonDialog(
						AppMasterApplication.getInstance());
				dialog.setTitle(R.string.time_location_lock_tip_title);
				String tip = this.getString(R.string.time_location_lock_tip_content);
				dialog.setContent(tip);
				dialog.setLeftBtnStr(this.getString(R.string.cancel));
				dialog.setMiddleBtnStr(this.getString(R.string.lock_mode_time));
				dialog.setRightBtnStr(this.getString(R.string.lock_mode_location));
//                dialog.setRightBtnBackground(R.drawable.manager_mode_lock_third_button_selecter);
				dialog.setOnClickListener(new LEOThreeButtonDialog.OnDiaogClickListener() {
					@Override
					public void onClick(int which) {
						Intent intent = null;
						if (which == 0) {
							// cancel
						} else if (which == 1) {
							// new time lock
							intent = new Intent(AppMasterApplication.getInstance(),
									TimeLockEditActivity.class);
							intent.putExtra("new_time_lock", true);
							intent.putExtra("from_dialog", true);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							AppMasterApplication.getInstance().startActivity(intent);
						} else if (which == 2) {
							// new location lock
							intent = new Intent(AppMasterApplication.getInstance(),
									LocationLockEditActivity.class);
							intent.putExtra("new_location_lock", true);
							intent.putExtra("from_dialog", true);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							AppMasterApplication.getInstance().startActivity(intent);
						}
					}
				});
				dialog.getWindow().setType(
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				dialog.show();
			} else {
				if (timeLockCount == 0 && locationLockCount != 0) {
					// show time lock btn dialog
					LEOAlarmDialog dialog = new LEOAlarmDialog(AppMasterApplication.getInstance());
					dialog.setTitle(R.string.time_location_lock_tip_title);
					String tip = this.getString(R.string.time_location_lock_tip_content);
					dialog.setContent(tip);
					dialog.setRightBtnStr(this.getString(R.string.lock_mode_time));
//                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
					dialog.setLeftBtnStr(this.getString(R.string.cancel));
					dialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
						@Override
						public void onClick(int which) {
							Intent intent = null;
							if (which == 0) {
								// cancel
							} else if (which == 1) {
								// new time lock
								intent = new Intent(AppMasterApplication.getInstance(),
										TimeLockEditActivity.class);
								intent.putExtra("new_time_lock", true);
								intent.putExtra("from_dialog", true);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								AppMasterApplication.getInstance().startActivity(intent);
							}

						}
					});
					dialog.getWindow().setType(
							WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					dialog.show();

				} else if (timeLockCount != 0 && locationLockCount == 0) {
					// show lcaotion btn dialog
					LEOAlarmDialog dialog = new LEOAlarmDialog(AppMasterApplication.getInstance());
					dialog.setTitle(R.string.time_location_lock_tip_title);
					String tip = this.getString(R.string.time_location_lock_tip_content);
					dialog.setContent(tip);
					dialog.setRightBtnStr(this.getString(R.string.lock_mode_location));
//                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
					dialog.setLeftBtnStr(this.getString(R.string.cancel));

					dialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
						@Override
						public void onClick(int which) {
							if (which == 0) {
								// cancel
							} else if (which == 1) {
								// new time lock
								Intent intent = new Intent(AppMasterApplication.getInstance(),
										LocationLockEditActivity.class);
								intent.putExtra("new_location_lock", true);
								intent.putExtra("from_dialog", true);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								AppMasterApplication.getInstance().startActivity(intent);
							}

						}
					});
					dialog.getWindow().setType(
							WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					dialog.show();
				}
			}
		}
	}

	public void onUnlockOutcount() {
		/**
		 * notify LockManager
		 */
		LeoEventBus.getDefaultBus().post(
				new AppUnlockEvent(mLockedPackage, AppUnlockEvent.RESULT_UNLOCK_OUTCOUNT));
		AppMasterPreference.getInstance(this).setDoubleCheck(null);
		mLockManager.recordOutcountTask(mLockedPackage);

//        Intent intent = new Intent(this, WaitActivity.class);
//        intent.putExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG, mLockedPackage);
//        startActivity(intent);
		SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "unlock", "fail");
	}

	private void findPasswd() {
		mDialog = new LeoDoubleLinesInputDialog(this);
		mDialog.setTitle(R.string.pleas_input_anwser);
		mDialog.setFirstHead(R.string.passwd_question);
		mDialog.setSecondHead(R.string.passwd_anwser);
		mDialog.setOnClickListener(this);
		mEtQuestion = mDialog.getFirstEditText();
		mEtAnwser = mDialog.getSecondEditText();
		mEtQuestion.setFocusable(false);

		String question = AppMasterPreference.getInstance(this)
				.getPpQuestion();

		if (!Utilities.isEmpty(question)) {
			String[] oldStrings = getResources().getStringArray(
					R.array.default_psw_protect_entrys);
			boolean isOldString = Utilities.makeContrast(oldStrings, question);

			String[] entrys = getResources().getStringArray(
					R.array.default_psw_protect_entrys_new);
			if (isOldString) {
				question = Utilities.replaceOldString(entrys, oldStrings, question);
			}
		}

		mEtQuestion.setText(question);
		mDialog.show();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		// if (mLockMode == LockManager.LOCK_MODE_FULL) {
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		try {
			startActivity(intent);
		} catch (Exception e) {
		}
		/**
		 * notify LockManager
		 */
		LeoEventBus.getDefaultBus().post(
				new AppUnlockEvent(mLockedPackage, AppUnlockEvent.RESULT_UNLOCK_CANCELED));
		super.onBackPressed();

	}

//    private String getfilterTarget(Intent intent) {
//        if (intent == null) {
//            return null;
//        }
//        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, 0);
//        String filterTarget = null;
//        if (resolveInfos != null && resolveInfos.size() == 1) {
//            for (ResolveInfo resolveInfo : resolveInfos) {
//                String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
//                if (!TextUtils.isEmpty(pkgName)) {
//                    filterTarget = pkgName;
//                }
//            }
//        }
//        return filterTarget;
//    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_nopermission_tip:
				try {
//                    Intent home = new Intent(Intent.ACTION_MAIN);
//                    home.addCategory(Intent.CATEGORY_HOME);
//                    startActivity(home);
					Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
//                    sHasClickGoGrantPermission = true;
					mHasClickGoGrantPermission = true;
//                    startActivity(intent);
//                    mLockManager.filterSelfOneMinites();
//                    if (!TextUtils.isEmpty(filterTarget)) {
//                        mLockManager.filterPackage(filterTarget, Constants.TIME_FILTER_TARGET);
//                    }
					SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "gd_wcnts", "gd_tips_click");
//                    finish();
					ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {

						@Override
						public void run() {
							if (mPermissionGuideToast == null) {
								mPermissionGuideToast = new BaseSelfDurationToast(LockScreenActivity.this);
							}
							mPermissionGuideToast.setDuration(1000 * 5);
							if (Utilities.hasNavigationBar(LockScreenActivity.this)) {
								mPermissionGuideToast.setWindowAnimations(R.style.toast_guide_permission_navigationbar);
							} else {
								mPermissionGuideToast.setWindowAnimations(R.style.toast_guide_permission);
							}
							mPermissionGuideToast.setMatchParent();
							mPermissionGuideToast.setGravity(Gravity.BOTTOM, 0, DipPixelUtil.dip2px(LockScreenActivity.this, 14));
							View v = LayoutInflater.from(LockScreenActivity.this).inflate(R.layout.toast_permission_guide, null);
							ImageView ivClose = (ImageView) v.findViewById(R.id.iv_permission_guide_close);
							ivClose.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									if (mPermissionGuideToast != null) {
										mPermissionGuideToast.hide();
									}
								}
							});
							mPermissionGuideToast.setView(v);
							mPermissionGuideToast.show();
						}
					}, 200);
				} catch (Exception e) {
				}
//                finish();
				break;
			case R.id.tv_option_image_content:
				if (mLeoPopMenu == null) {
					mLeoPopMenu = new LeoHomePopMenu();
					mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view,
												int position, long id) {
							setPopWindowItemClick(position);
//                            mLeoPopMenu.dismissSnapshotList();
                            ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mLeoPopMenu.dismissSnapshotList();
                                }
                            }, 300);
                        }
                    });
                }
                mLeoPopMenu.setPopMenuItems(this, getPopMenuItems(), getMenuIcons());
                mLeoPopMenu.showPopMenu(this, mTtileBar.findViewById(R.id.tv_option_image), null, null);
                mLeoPopMenu.setListViewDivider(null);
                AppMasterPreference.getInstance(LockScreenActivity.this).setLockScreenMenuClicked(
                        true);
                mTtileBar.setOptionImage(R.drawable.ic_toolbar_more);
                break;
            case R.id.layout_title_back_arraow:
                onBackPressed();
                finish();
                break;
            // case R.id.img_layout_right:
            // sLockFilterFlag = true;
            // Intent intent = new Intent(LockScreenActivity.this,
            // LockerTheme.class);
            // SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1,
            // "theme_enter", "unlock");
            // AppMasterPreference amp =
            // AppMasterPreference.getInstance(this);
            // amp.setUnlocked(true);
            // amp.setDoubleCheck(null);
            // startActivityForResult(intent, 0);
            // amp.setLockerScreenThemeGuide(true);
            // break;
            case R.id.mr_gift:
                sLockFilterFlag = true;
                AppMasterPreference mAmp = AppMasterPreference.getInstance(this);
                mAmp.setUnlocked(true);
                mAmp.setDoubleCheck(null);
                // wallAd.clickWall();
                mAmp.setIsADAppwallNeedUpdate(false);
                try {
//                    Intent mWallIntent = wallAd.getWallIntent();
//                    startActivity(mWallIntent);
//                    wallAd = MobvistaEngine.getInstance(this).createAdWallController(this, Constants.UNIT_ID_63);
//                    wallAd.preloadWall();
//                    wallAd.clickWall();
					MobvistaEngine.getInstance(this).createAdWallController1(this, Constants.UNIT_ID_63);
                } catch (Exception e) {
                }
                if (!mHaveNewThings && !clickShakeIcon) {
                    AppMasterPreference.getInstance(LockScreenActivity.this).setJumpIcon(true);
                } else {
                    clickShakeIcon = true;
                }
                AppMasterPreference.getInstance(LockScreenActivity.this).setAdClickTime(
                        System.currentTimeMillis());
                // Send ad event for network available only
                if (NetWorkUtil.isNetworkAvailable(getApplicationContext())) {
                    SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1,
                            "ad_cli", "unlocktop");
                }
                break;
            default:
                break;
        }
    }

    /**
     * setting the menu item,if has password protect then add find password item
     *
     * @return
     */
    private List<String> getPopMenuItems() {
        List<String> listItems = new ArrayList<String>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_GESTURE) {
                listItems.add(resources.getString(R.string.find_gesture));
            } else if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_PASSWD) {
                listItems.add(resources.getString(R.string.find_passwd));
            }
        }

        int type = mLockFragment.getUnlockType();
        if (type == AirSigSettingActivity.NOMAL_UNLOCK) {
            listItems.add(resources.getString(R.string.unlock_theme));
            listItems.add(resources.getString(R.string.setting_hide_lockline));
        }

        listItems.add(resources.getString(R.string.help_setting_tip_title));

        return listItems;
    }

    private List<Integer> getMenuIcons() {
        List<Integer> icons = new ArrayList<Integer>();
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            icons.add(R.drawable.forget_password_icon);
        }

        int type = mLockFragment.getUnlockType();
        if (type == AirSigSettingActivity.NOMAL_UNLOCK) {
            icons.add(R.drawable.theme_icon_black);
            if (AppMasterPreference.getInstance(this).getIsHideLine()) {
                icons.add(R.drawable.show_locus_icon);
            } else {
                icons.add(R.drawable.hide_locus_icon);
            }
        }

        icons.add(R.drawable.help_tip_icon);
        return icons;
    }

    private void setPopWindowItemClick(int position) {
        int type = mLockFragment.getUnlockType();
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            if (type == AirSigSettingActivity.NOMAL_UNLOCK) {
                if (position == 0) {
                    findPasswd();
                } else if (position == 1) {
                    onMoveToTheme();
                } else if (position == 2) {
                    onHideLockLineClicked(position);
                } else {
                    onHelpItemClicked();
                }
            } else {
                if (position == 0) {
                    findPasswd();
                } else {
                    onHelpItemClicked();
                }
            }

        } else {

            if (type == AirSigSettingActivity.NOMAL_UNLOCK) {
                if (position == 0) {
                    onMoveToTheme();
                } else if (position == 1) {
                    onHideLockLineClicked(position);
                } else if (position == 2) {
                    onHelpItemClicked();
                }
            } else {
                if (position == 0) {
                    onHelpItemClicked();
                }
            }


        }
    }

    private void onMoveToTheme() {
        sLockFilterFlag = true;
        Intent intent = new Intent(LockScreenActivity.this,
                LockerTheme.class);
        intent.putExtra("fromLock", true);
        SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1,
                "theme_enter", "unlock");
        AppMasterPreference amp =
                AppMasterPreference.getInstance(this);
        amp.setUnlocked(true);
        amp.setDoubleCheck(null);
        startActivityForResult(intent, 0);
        amp.setLockerScreenThemeGuide(true);
    }

    private void onHideLockLineClicked(int position) {
        String tip;
        if (AppMasterPreference.getInstance(this).getIsHideLine()) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "trackhide", "unlock_off");
            mLeoPopMenu.updateItemIcon(position, R.drawable.hide_locus_icon);
            AppMasterPreference.getInstance(this).setHideLine(false);
            tip = getString(R.string.lock_line_visiable);
        } else {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "trackhide", "unlock_on");
            mLeoPopMenu.updateItemIcon(position, R.drawable.show_locus_icon);
            AppMasterPreference.getInstance(this).setHideLine(true);
            tip = getString(R.string.lock_line_hide);
        }
        if (mLockFragment instanceof GestureLockFragment) {
            ((GestureLockFragment) mLockFragment).reInvalideGestureView();
        }
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }

    private void onHelpItemClicked() {
        sLockFilterFlag = true;
        AppMasterPreference ampp = AppMasterPreference.getInstance(this);
        ampp.setLockerScreenThemeGuide(true);
        ampp.setUnlocked(true);
        ampp.setDoubleCheck(null);
        Intent helpSettingIntent = new Intent(LockScreenActivity.this,
                LockHelpSettingTip.class);
        helpSettingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            LockScreenActivity.this.startActivity(helpSettingIntent);
        } catch (Exception e) {
        }
        /* SDK Event Mark */
		SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "help", "help_tip");
	}

	@Override
	public void onClick(int which) {
		if (which == 1) {// make sure
			String anwser = AppMasterPreference.getInstance(this).getPpAnwser();
			if (anwser.equals(mEtAnwser.getText().toString())) {
				AppMasterPreference ampp = AppMasterPreference.getInstance(this);
				ampp.setUnlocked(true);
				ampp.setDoubleCheck(null);
				// goto reset passwd
				Intent intent = new Intent(this, LockSettingActivity.class);
				intent.putExtra(LockSettingActivity.RESET_PASSWD_FLAG, true);
				this.startActivity(intent);
				finish();
			} else {
				Toast.makeText(this, R.string.reinput_anwser, Toast.LENGTH_SHORT).show();
				mEtAnwser.setText("");
			}
		} else if (which == 0) { // cancel
			mDialog.dismiss();
		}
	}

	public int getFromType() {
		return mLockMode;
	}

	/**
	 * themeGuide
	 *
	 * @param view
	 * @param anim
	 */
//    private void themeGuide(View view, Animation anim) {
//        view.startAnimation(anim);
//    }

	/**
	 * check clean memory
	 */
	private void checkCleanMem() {
		if (AppMasterPreference.getInstance(LockScreenActivity.this)
				.isLockerClean()) {
			long totalMem = ProcessUtils.getTotalMem();
			long lastUsedMem = totalMem - ProcessUtils.getAvailableMem(this);
			cleanAllProcess();
			long curUsedMem = totalMem - ProcessUtils.getAvailableMem(this);
			long cleanMem = Math.abs(lastUsedMem - curUsedMem);
			double number = (double) cleanMem / lastUsedMem;
			int numberRate = (int) (number * 100);
			if (numberRate <= 0) {
				int random = (int) (Math.random() * 10 + 1);
				mCleanRate = random + "%";
			} else {
				int cleanNumber = numberRate;
				mCleanRate = cleanNumber + "%";
			}
		}
	}

	private void cleanAllProcess() {
		mAm = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> list = mAm.getRunningAppProcesses();
		List<String> launchers = getLauncherPkgs(this);
		for (RunningAppProcessInfo runningAppProcessInfo : list) {
			if (runningAppProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_CANT_SAVE_STATE) {
				if (!launchers.contains(runningAppProcessInfo.processName)) {
					mAm.killBackgroundProcesses(runningAppProcessInfo.processName);
				}
			}
		}
	}

	private List<String> getLauncherPkgs(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		List<String> pkgs = new ArrayList<String>();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
		for (ResolveInfo resolveInfo : apps) {
			ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
			String packageName = applicationInfo.packageName;
			pkgs.add(packageName);
		}
		if (mLockedPackage != null) {
			pkgs.add(mLockedPackage);
		}
		return pkgs;
	}

	/**
	 * dont change this method
	 *
	 * @param event
	 */
	public void onEventMainThread(LockThemeChangeEvent event) {
		mRestartForThemeChanged = true;
	}

	public void removePretendFrame() {
		mPretendLayout.setVisibility(View.GONE);
		mLockLayout.setVisibility(View.VISIBLE);

		final int adShowNumber = AppMasterPreference.getInstance(LockScreenActivity.this)
				.getADShowType();

        /* 发送伪装解锁成功指令，解决在有伪装情况下动画运行时机问题 */
		if (adShowNumber == 5) {
			LeoEventBus.getDefaultBus().post(
					new SubmaineAnimEvent(EventId.EVENT_SUBMARINE_ANIM, "camouflage_lock_success"));
		}
	}

	private float top, height;
	private LeoCircleView bgView;
	private ImageView modeIconOut;
	private ImageView modeIconIn;
	private ImageView modeIconDown;
	private TextView mActiveText;
	private Map<String, Object> currModeIconMap;
	private Map<String, Object> willModeIconMap;

	private boolean mHaveNewThings = false;

	private long mLastTime;
	private boolean isClickJump;

	/**
	 * show the tip when mode success activating
	 */
	private void showModeActiveTip(int willModeFlag, int currentModeFlag) {
		currModeIconMap = modeIconSwitch(currentModeFlag);
		willModeIconMap = modeIconSwitch(willModeFlag);

		View mTipView = LayoutInflater.from(this).inflate(R.layout.lock_mode_active_tip, null);
		mActiveText = (TextView) mTipView.findViewById(R.id.active_text);
		mActiveText.setText(this.getString(R.string.mode_change, mQuickModeName));
		mActiveText.setMaxLines(2);
		bgView = (LeoCircleView) mTipView.findViewById(R.id.mode_active_bg);
		modeIconIn = (ImageView) mTipView.findViewById(R.id.mode_active_in);
		modeIconOut = (ImageView) mTipView.findViewById(R.id.mode_active_out);
		modeIconDown = (ImageView) mTipView.findViewById(R.id.mode_active_down);
		modeIconIn.setImageResource((Integer) willModeIconMap.get("modeIcon"));
		modeIconOut.setImageResource((Integer) currModeIconMap.get("modeIcon"));
		modeIconDown.setImageResource((Integer) willModeIconMap.get("modeDown"));
		bgView.setColor(Color.parseColor(currModeIconMap.get("bgColor").toString()));

		ViewTreeObserver vto = modeIconOut.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				modeIconOut.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				top = modeIconOut.getTop();
				height = modeIconOut.getHeight();
				activeAnimation();
			}
		});

		Toast toast = new Toast(this);
		toast.setView(mTipView);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.show();
	}

	private void activeAnimation() {
		int bg_anim_time = 300;
		int in_anim_time = 400;
		int out_anim_time = 300;

		ValueAnimator colorAnim = ValueAnimator.ofObject(new ColorEvaluator(),
				currModeIconMap.get("bgColor"), willModeIconMap.get("bgColor"));
		colorAnim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int color = (Integer) animation.getAnimatedValue();
				bgView.setColor(color);
			}
		});
		colorAnim.setDuration(bg_anim_time);

		final float outLength = top + height;
		ValueAnimator outAnimator = ValueAnimator.ofFloat(0, outLength).setDuration(out_anim_time);
		outAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float curr = (Float) animation.getAnimatedValue();
				float percent = 1.0f - curr / outLength;
				modeIconOut.setTranslationY(curr);
				if (percent >= 0.6f) {
					modeIconOut.setAlpha(percent);
					modeIconOut.setScaleX(percent);
					modeIconOut.setScaleY(percent);
				}
			}
		});

		final float maxLength = 2 * top;
		final float upLength = top + height / 2;
		ValueAnimator inAnimator = ValueAnimator.ofFloat(-(upLength), top, 0f).setDuration(
				in_anim_time);
		inAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				modeIconIn.setVisibility(View.VISIBLE);
			}
		});
		inAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float curr = (Float) animation.getAnimatedValue();
				modeIconIn.setTranslationY(curr);
				curr = curr + upLength;
				float percent = curr / maxLength;
				if (percent > 1.0f) {
					percent = 1.0f;
				}
				modeIconIn.setAlpha(0.6f * percent + 0.4f);
				modeIconIn.setScaleX(0.3f * percent + 0.7f);
				modeIconIn.setScaleY(0.3f * percent + 0.7f);
			}
		});

		ValueAnimator downAnimator = ValueAnimator.ofFloat(0, 1.2f, 0.9f, 1.0f).setDuration(300);
		downAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				modeIconDown.setVisibility(View.VISIBLE);
			}
		});
		downAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float curr = (Float) animation.getAnimatedValue();
				modeIconDown.setScaleX(curr);
				modeIconDown.setScaleY(curr);
			}
		});
		downAnimator.setStartDelay(in_anim_time);

		AnimatorSet set = new AnimatorSet();
		set.setStartDelay(400);
		set.playTogether(colorAnim, outAnimator, inAnimator, downAnimator);
		set.start();
	}

	private Map<String, Object> modeIconSwitch(int modeFlag) {
		Map<String, Object> iconMap = new HashMap<String, Object>();
		switch (modeFlag) {
			case 1:
				iconMap.put("bgColor", "#5653b4");
				iconMap.put("modeIcon", R.drawable.visitor_mode);
				iconMap.put("modeDown", R.drawable.visitor_mode_done);
				break;
			case 3:
				iconMap.put("bgColor", "#ffa71c");
				iconMap.put("modeIcon", R.drawable.family_mode);
				iconMap.put("modeDown", R.drawable.family_mode_done);
				break;
			default:
				iconMap.put("bgColor", "#44c4f5");
				iconMap.put("modeIcon", R.drawable.default_mode);
				iconMap.put("modeDown", R.drawable.default_mode_done);
				break;
		}
		return iconMap;
	}

	/**
	 * show the defalut toast tip when mode success activating
	 */
	private void showModeActiveTip(LockMode mode) {
		Map<String, Object> willLaunchMap = modeIconSwitch(mode.defaultFlag);
		View mTipView = LayoutInflater.from(this).inflate(R.layout.lock_mode_active_tip, null);
		mActiveText = (TextView) mTipView.findViewById(R.id.active_text);
		bgView = (LeoCircleView) mTipView.findViewById(R.id.mode_active_bg);
		modeIconOut = (ImageView) mTipView.findViewById(R.id.mode_active_out);
		modeIconDown = (ImageView) mTipView.findViewById(R.id.mode_active_down);

		modeIconOut.setImageResource((Integer) willLaunchMap.get("modeIcon"));
		modeIconDown.setImageResource((Integer) willLaunchMap.get("modeDown"));
		modeIconDown.setVisibility(View.VISIBLE);
		bgView.setColor(Color.parseColor(willLaunchMap.get("bgColor").toString()));
		mActiveText.setText(this.getString(R.string.mode_change, mode.modeName));
		mActiveText.setMaxLines(2);

		Toast toast = new Toast(this);
		toast.setView(mTipView);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
	}

	private int getWindowWidth() {
//        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
//        return windowManager.getDefaultDisplay().getWidth();

		DisplayMetrics display = getResources().getDisplayMetrics();
		int mScreenWidth = display.widthPixels;// 获取屏幕分辨率宽度
		return mScreenWidth;
	}


/*    public void onScrollOffsetToDis(int offset){
        if (mBannerContainer != null && mBannerContainer.getSelectedItemPosition() % mAdItemCount == 0) {
                float alpha = 1 - (float)Math.abs(offset) * 2 / getWindowWidth();
                if (alpha > 0) {
                    int type = AppMasterPreference.getInstance(this).getLockType();
                    if (type == LockFragment.LOCK_TYPE_PASSWD) {
                        ((PasswdLockFragment)mLockFragment).getIconView().setAlpha(alpha);
                        ((PasswdLockFragment)mLockFragment).getPasswdHint().setAlpha(alpha);
                    } else {
                        ((GestureLockFragment)mLockFragment).getIconView().setAlpha(alpha);
                    }
                }
            } else {
                int type = AppMasterPreference.getInstance(this).getLockType();
                if (type == LockFragment.LOCK_TYPE_PASSWD) {
                    if (((PasswdLockFragment)mLockFragment).getIconView().getAlpha() > 0.0f) {
                        ((PasswdLockFragment)mLockFragment).getIconView().setAlpha(0.0f);
                        ((PasswdLockFragment)mLockFragment).getPasswdHint().setAlpha(0.0f);
                    }
                } else {
                    if (((GestureLockFragment)mLockFragment).getIconView().getAlpha() > 0.0f) {
                        ((GestureLockFragment)mLockFragment).getIconView().setAlpha(0.0f);
                    }
                }
        }

    }*/


	public class AdBannerAdapter extends PagerAdapter
			implements ViewPager.OnPageChangeListener {
		private LayoutInflater mInflater; //
		private LinkedList<View> mViews; //
		private ArrayList<String> mList; //
		private ViewPager mViewPager; //页面
		private int lasterSlectedPage = -1; //上一次选择的页面
		private ADEngineWrapper mWrapperEngine;

		public AdBannerAdapter(Context context, ViewPager viewPager,
							   ArrayList<String> list, ADEngineWrapper wrapperEngine) {
			mInflater = LayoutInflater.from(context);
			mViewPager = viewPager;
			mList = list;
			LeoLog.d("remove_redundant", "[AdBannerAdapter] array: " + Arrays.toString(mList.toArray()));
			mWrapperEngine = wrapperEngine;
			mViewPager.setOnPageChangeListener(this);
			synchronized (mViewPager) {
				if (list != null) {
					mViews = new LinkedList<View>();
					RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.lock_ad_item, null);
					view.setTag(mViews.size());
					mViews.add(view);
					view.setVisibility(View.INVISIBLE);

					for (String unitId : mList) {
						view = (RelativeLayout) mInflater.inflate(R.layout.lock_ad_item, null);
						view.setTag(mViews.size());
						boolean done = setItemViewContent(view, unitId);
						if (done) {
							SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "ad_cache", "adv_cache_picad" + mViews.size());
							mViews.add(view);
						}
					}

				}

			}

		}

		@Override
		public int getItemPosition(Object item) {
			for (int i = 0; i < mViews.size(); i++) {
				if (((View) item).equals(mViews.get(i))) {
					return i;
				}
			}
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return mViews.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (!mViews.isEmpty()) {
				container.removeView(mViews.get(position));
			}
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mViews.get(position);
			container.addView(view);
			return view;
		}

		public void setLasterSlectedPage(int index) {
			lasterSlectedPage = index;
		}

		public int getLasterSlectedPage() {
			return lasterSlectedPage;
		}

		/***
		 * 填充广告数据到VIEW中
		 *
		 * @param view
		 * @param unitId
		 * @return 是否成功填充
		 */
		private boolean setItemViewContent(RelativeLayout view, String unitId) {
			WrappedCampaign campaign = mAdMap.get(unitId);
			if (campaign == null) {
				return false;
			}

			Bitmap bitmap = mImageFetcher.getBitmap(campaign.getImageUrl());
			if (bitmap == null || bitmap.isRecycled()) {
				return false;
			}

			int index = findTargetViewIndex(unitId);
			if (index != -1) {
				mBannerContainer.setCurrentItem(index, false);
				mAdapterCycle.setLasterSlectedPage(index);

			}

			((ImageView) view.findViewById(R.id.ad_image)).setImageBitmap(bitmap);
			((TextView) view.findViewById(R.id.ad_title)).setText(campaign.getAppName());
			view.findViewById(R.id.ad_title).setTag(unitId);
			((TextView) view.findViewById(R.id.ad_details)).setText(campaign.getDescription());
			((TextView) view.findViewById(R.id.ad_install_button)).setText(campaign.getAdCall());
			final View clickArea = view.findViewById(R.id.click_area);
			mAdView = clickArea;
			if (mWrapperEngine != null) {
				mWrapperEngine.registerView(mAdSource, clickArea, unitId);
			}

			View leftArea = view.findViewById(R.id.left_click_area);
			leftArea.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					snapforClick((ViewGroup) v.getParent());
				}
			});

			View rightArea = view.findViewById(R.id.right_click_area);
			rightArea.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					snapforClick((ViewGroup) v.getParent());
				}
			});
			SDKWrapper.addEvent(AppMasterApplication.getInstance(), "max_ad", SDKWrapper.P1, "ad_show", "ad pos: " + unitId + " adShow", mAdSource, null);
			return true;
		}

		private void snapforClick(View v) {
			try {
				int index = ((Integer) v.getTag());
				if (mViewPager.getCurrentItem() != index) {
					mViewPager.setCurrentItem(index, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 实现ViewPager.OnPageChangeListener接口
		@Override
		public void onPageSelected(int position) {
			LeoLog.i("onPageSelected", "position=" + position);
			if (position != 1) {
				mHandler.removeMessages(LARGE_BANNER_HIDE);
			}
			if (position == 0) {
				setLockAppInfoViewVisible(false);
			} else {
				setLockAppInfoViewVisible(true);
				SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "ad_act", "adv_shws_picad" + position);
			}
			if (lasterSlectedPage >= 0) {
				if (lasterSlectedPage < position) {
					SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "ad_cli", "adv_cnts_picad_drawL");
				} else if (lasterSlectedPage > position) {
					SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "ad_cli", "adv_cnts_picad_drawR");
				}
			}
			lasterSlectedPage = position;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (position == 0) {
				float alpha = 1 - positionOffset;
				if (alpha > 0) {
					int type = AppMasterPreference.getInstance(LockScreenActivity.this).getLockType();
					if (type == LockFragment.LOCK_TYPE_PASSWD) {
						((PasswdLockFragment) mLockFragment).getIconView().setAlpha(alpha);
						((PasswdLockFragment) mLockFragment).getPasswdHint().setAlpha(alpha);
					} else {
						((GestureLockFragment) mLockFragment).getIconView().setAlpha(alpha);
					}
					if (mLockAppTitleView != null) {
						TextView tv = mTtileBar.getTitleView();
						if (tv != null) {
							tv.setAlpha(alpha);
						}
						mLockAppTitleView.setAlpha(1 - alpha);
					}
				} else {
					int type = AppMasterPreference.getInstance(LockScreenActivity.this).getLockType();
					if (type == LockFragment.LOCK_TYPE_PASSWD) {
						if (((PasswdLockFragment) mLockFragment).getIconView().getAlpha() > 0.0f) {
							((PasswdLockFragment) mLockFragment).getIconView().setAlpha(0.0f);
							((PasswdLockFragment) mLockFragment).getPasswdHint().setAlpha(0.0f);
						}
					} else {
						if (((GestureLockFragment) mLockFragment).getIconView().getAlpha() > 0.0f) {
							((GestureLockFragment) mLockFragment).getIconView().setAlpha(0.0f);
						}
					}
				}
			}

//            //解决刷新不良的问题
//            if (mBannerParent != null) {
//                mBannerParent.invalidate();
//            }
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		public void addItem(String unitId) {
            /* 移除已经存在的unitid */
			LeoLog.d("remove_redundant", "add: " + unitId + "; array=" + Arrays.toString(mList.toArray()));
			RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.lock_ad_item, null);
			if (!setItemViewContent(view, unitId)) {
				return;
			}
			view.setTag(mViews.size());
			SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "ad_cache", "adv_cache_picad" + mViews.size());
			if (!mViews.isEmpty() && unitId.equals(mBannerAdids[0])) {
				mViews.add(1, view);
			} else {
				mViews.add(view);
			}
			LeoLog.d("remove_redundant", "notifyDataSetChanged for: " + unitId + "; array=" + Arrays.toString(mList.toArray()));
			notifyDataSetChanged();
		}

		public LinkedList<View> getViews() {
			return mViews;
		}

		public void clearView() {
			LeoLog.d(TAG, "adapter clearView called");
			mViews.clear();
			mList.clear();
			notifyDataSetChanged();
		}

	}

//    public class HomeWatcherReceiver extends BroadcastReceiver {
//        private static final String LOG_TAG = "HomeReceiver";
//        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
//        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            LeoLog.d("HomeReceiver_Lock", "received! action = " + action);
//            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
//                // android.intent.action.CLOSE_SYSTEM_DIALOGS
//                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
//                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
//                    LeoLog.d("HomeReceiver_Lock", "received! tryHideToast has clicked? = " + mHasClickGoGrantPermission);
//                    if (mHasClickGoGrantPermission) {
//                        tryHidePermissionGuideToast();
//                    }
//                }
//            }
//
//        }
//
//    }

//    private void registerHomeKeyReceiver() {
//        LeoLog.d("lisHome", "registerHomeKeyReceiver");
//        mReceiver = new HomeWatcherReceiver();
//        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//
//        registerReceiver(mReceiver, homeFilter);
//    }
//
//    private void unregisterHomeKeyReceiver() {
//        LeoLog.d("lisHome", "unregisterHomeKeyReceiver");
//        if (null != mReceiver) {
//            unregisterReceiver(mReceiver);
//        }
//    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && mHasClickGoGrantPermission) {
//            LeoLog.d("HomeReceiver_Lock", "back pressed! tryHideToast has clicked? = " + mHasClickGoGrantPermission);
////            if (mHasClickGoGrantPermission) {
//            LeoLog.d("HomeReceiver_Lock", "hide -back");
//            tryHidePermissionGuideToast();
////            }
//            return false;
//        } else {
//            return super.onKeyDown(keyCode, event);
//        }
//    }


}
