
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.HttpRequestAgent.RequestListener;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LockThemeChangeEvent;
import com.leo.appmaster.lockertheme.ThemeJsonObjectParser;
import com.leo.appmaster.model.ThemeItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.mobvista.sdk.m.core.entity.Campaign;

public class UFOActivity extends BaseActivity implements ImageLoadingListener {

    private boolean mHasPlayed = false;// 是否播放过动画，开始播放后置为true，以后每次WindowFocusChanged后就不播放动画了，
    private boolean mHasGetLoadResult = false;// 是否已经得到拉取广告的结果，用于控制loading的实时结束
    private boolean mIsADLoaded = false;// 是否从MobvistaEngine的接口中得到结果
    private boolean mIsThemeLoaded =false;
    private AnimationDrawable mUFODrawable;//
    private AnimationDrawable mAlienDrawable;
    // 广告素材
    private MobvistaEngine mAdEngine;
    // 主题
    private List<String> mHideThemeList;
    private ThemeItemInfo mChosenTheme;
    private ImageView mThemDialogBg;
    private String mThemeName;
    private Button mBtnUseTheme;
    private TextView mTvThemeName;
//    private RelativeLayout mADDialog;
//    private RelativeLayout mThemeDialog;
    
    private ImageView mClose;
    private RelativeLayout mWholeUFO;
    private RelativeLayout mDialog;
    private ImageView mAlien;
    private ImageView mUFO;
    private ImageView mLongLight;
    private ImageView mCircleLight;
    private ImageView mSplashLight;
    private CountDownTimer mCdt;
    private Button mInstall;
    private boolean mIsShowTheme = false;
    // 动画参数
    private float mUFOW;
    private float mUFOH;
    private float mWindowW;
    private float mWindowH;
    private int RANDOM_NUMERATOR=1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_ufo);
        InitUI();
        toLoad();
    };
    
    @Override
    protected void onStart() {
        super.onStart();
        
    }
    
    private void toLoad() {
        double themeChanceAfterUFO = (double)AppMasterPreference.getInstance(this).getThemeChanceAfterUFO();
        int ran = (int) (Math.random() * themeChanceAfterUFO + 1d);
//        ran=2;
        if (ran == RANDOM_NUMERATOR&&themeChanceAfterUFO!=0) {
            mIsShowTheme = true;
            loadTheme();
            loadAD();
        } else {
            loadAD();
            
        }
    }

    private void loadTheme() {
        LeoLog.e("poha","loading themeeeeeeeeeeeeeee...");
        mHideThemeList = AppMasterPreference.getInstance(this).getHideThemeList();
        HttpRequestAgent.getInstance(this).loadOnlineTheme(mHideThemeList, new ThemeListener(this));
    }

    private static class ThemeListener extends RequestListener<UFOActivity> {
        public ThemeListener(UFOActivity outerContext) {
            super(outerContext);
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            final UFOActivity ufoActivity = getOuterContext();
            if(ufoActivity==null){
                return;
            }
            List<ThemeItemInfo> list = ThemeJsonObjectParser
                    .parserJsonObject(ufoActivity, response);
            List<ThemeItemInfo> listBackup = new ArrayList<ThemeItemInfo>();
            listBackup.addAll(list);
//            Toast.makeText(ufoActivity, "firstly listBackup.size="+listBackup.size(), 0).show();
            
            for(int i=0;i<list.size();i++){
                LeoLog.e("poha", list.get(i).packageName+"          "+list.get(i).themeName);
            }
//            LeoLog.e("poha", list.size()+"起始list size");  
            if (list != null) {
                List<String> mHideThemes;
                mHideThemes = AppMasterPreference.getInstance(ufoActivity).getHideThemeList();
                for(int i =0;i<mHideThemes.size();i++){
                    LeoLog.e("poha", mHideThemes.get(i)+"                   hide");  
                    for(int j=0;j<list.size();j++){
                        if(list.get(j).packageName.equals(mHideThemes.get(i))){
                            LeoLog.e("poha", "removed");  
                            list.remove(j);
                        }
                    }
                }
                
                for(int i=0;i<list.size();i++){
                    LeoLog.e("poha", list.get(i).packageName+"     已经筛选掉在线主题列表中本地已有     "+list.get(i).themeName);
                }
                
//                LeoLog.e("poha", list.size()+"最终list size");  
//                if(list.size()==0){
//                    list=listBackup;
//                    if(list.size()==0){
//                        return;
//                    }
//                    
////                    for(int i=0;i<list.size();i++){
////                        LeoLog.e("poha", list.get(i).packageName+"     只有本地主题，在线没有主题     "+list.get(i).themeName);
////                    }
//                    
//                    
//                    for(int i=0;i<list.size();i++){
//                        if(list.get(i).packageName.equals(AppMasterApplication.getSelectedTheme())){
//                            list.remove(i);
//                           break;
//                        }
//                    }
//                }
                
//                for(int i=0;i<list.size();i++){
//                    LeoLog.e("poha", list.get(i).packageName+"     已经筛选掉本地已经再使用的     "+list.get(i).themeName);
//                }
                
                
                if(list.size()==0){
                    return;
                }
              
                double size = (double) list.size();
                int ran = (int) (Math.random() * size);
                
//                if(mHideThemes.contains(mChosenTheme.packageName)){
//                    Toast.makeText(UFOActivity.this, "这个主题已经本地有了", 0).show();
//                    AppMasterApplication.setSharedPreferencesValue(mChosenTheme.packageName);
//                    LeoEventBus.getDefaultBus().post(new LockThemeChangeEvent());
//                    UFOActivity.this.finish();
//                }
                LeoLog.e("poha", list.get(ran).themeName+"     最终选择的主题     "+list.get(ran).themeName);
//                ran = 1;
//                list.clear();
//                list.addAll(listBackup);
//                list=listBackup;
                ufoActivity.mThemeName = list.get(ran).themeName;
                ufoActivity.mChosenTheme = list.get(ran);
//                Toast.makeText(ufoActivity, "finally size="+list.size()+"。."+listBackup.size()+"。。chosenTheme=="+ufoActivity.mThemeName, 0).show();
                ufoActivity.loadADPic(list.get(ran).previewUrl, new ImageSize(290, 160),
                        ufoActivity.mThemDialogBg);
                LeoLog.e("poha", "to load Pic");
                
                ThreadManager.executeOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        ufoActivity.mTvThemeName.setText(ufoActivity.mThemeName);
                        LeoLog.e("poha", "set text");
                        ufoActivity.initButton();
                        LeoLog.e("poha", "init button");
                    }
                });
                
            }
        }
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    }
    
    private void initButton(){
//        mBtnUseTheme = (Button) findViewById(R.id.btn_usetheme);
        mBtnUseTheme.setText(UFOActivity.this.getResources().getString(R.string.ufo_theme_down));
//        mBtnUseTheme.setText("asdfasdfa");
        mBtnUseTheme.setAllCaps(false);
        List<String> mHideThemes;
        mHideThemes = AppMasterPreference.getInstance(UFOActivity.this).getHideThemeList();
//        if(mHideThemes.contains(mChosenTheme.packageName))
//        {
//            mBtnUseTheme.setText(UFOActivity.this.getResources().getString(R.string.ufo_theme_use));
//        }
        mBtnUseTheme.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKWrapper.addEvent(UFOActivity.this, SDKWrapper.P1, "ad_cli",
                        "adv_cnts_alTP");
                LeoLog.e("xxx", "clicked");
                List<String> mHideThemes;
                mHideThemes = AppMasterPreference.getInstance(UFOActivity.this).getHideThemeList();
                if(mHideThemes.contains(mChosenTheme.packageName)){
                    mBtnUseTheme.setText(UFOActivity.this.getResources().getString(R.string.ufo_theme_use));
                    AppMasterApplication.setSharedPreferencesValue(mChosenTheme.packageName);
                    LeoEventBus.getDefaultBus().post(new LockThemeChangeEvent());
                    Toast.makeText(UFOActivity.this, UFOActivity.this.getResources().getString(R.string.ufo_theme_use_toast), 0).show();
                    UFOActivity.this.finish();
                }
                else{
                    ThemeItemInfo bean = new ThemeItemInfo();
                    if (AppUtil.appInstalled(UFOActivity.this, Constants.GP_PACKAGE)) {
                        try {
                            AppwallHttpUtil.requestGp(UFOActivity.this, mChosenTheme.packageName);
                        } catch (Exception e) {
                            AppwallHttpUtil.requestUrl(UFOActivity.this,
                                    mChosenTheme.downloadUrl);
                        }
                    }else {
                        AppwallHttpUtil.requestUrl(UFOActivity.this,
                                mChosenTheme.downloadUrl);
                    }
                    UFOActivity.this.finish();
                }
            }
        });
    }

    private void loadAD() {
        LeoLog.e("poha","loading ad...");
        mAdEngine = MobvistaEngine.getInstance(this);
        mAdEngine.loadMobvista(Constants.UNIT_ID_58, new MobvistaListener() {
            @Override
            public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                if (code == MobvistaEngine.ERR_OK&&campaign!=null) {
                    mIsADLoaded = true;
                    LeoLog.e("poha","ad loaded!");
                    loadADPic(campaign.getIconUrl(),
                            new ImageSize(DipPixelUtil.dip2px(UFOActivity.this, 48), DipPixelUtil
                                    .dip2px(UFOActivity.this, 48)),
                            (ImageView) mDialog.findViewById(R.id.iv_ufo_ad_icon));
                    loadADPic(campaign.getImageUrl(),
                            new ImageSize(DipPixelUtil.dip2px(UFOActivity.this, 302), DipPixelUtil
                                    .dip2px(UFOActivity.this, 158)),
                            (ImageView) mDialog.findViewById(R.id.iv_appbg_ufo));

                    TextView appname = (TextView) mDialog.findViewById(R.id.tv_appname_ufo);
                    appname.setText(campaign.getAppName());
                    TextView appdesc = (TextView) mDialog.findViewById(R.id.tv_appdesc_ufo);
                    appdesc.setText(campaign.getAppDesc());
                    Button call = (Button) mDialog.findViewById(R.id.btn_ufo_dialog_install);
                    call.setText(campaign.getAdCall());
                    mAdEngine.registerView(Constants.UNIT_ID_58, call);
                }
            }

            @Override
            public void onMobvistaClick(Campaign campaign, String unitID) {
                UFOActivity.this.finish();
                SDKWrapper.addEvent(UFOActivity.this, SDKWrapper.P1, "ad_cli", "adv_cnts_alAP");
                SDKWrapper.addEvent(UFOActivity.this, SDKWrapper.P1, "ad_cli", "draw_gp");
                AppMasterPreference.getInstance(UFOActivity.this).setAdEtClickTime(
                        System.currentTimeMillis());
            }
        });
    }

    private void loadADPic(String url, ImageSize size, final ImageView v) {
        ImageLoader.getInstance().loadImage(
                url, size, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (loadedImage != null) {
                            v.setImageBitmap(loadedImage);
                            if (mIsShowTheme&&mChosenTheme!=null) {
                                mIsThemeLoaded = true;
                                LeoLog.e("poha", "主题图片已经下载好了！");
                            }
                        }
                    }
                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }
                });
    }

    private void InitUI() {
        mTvThemeName=(TextView) findViewById(R.id.tv_ThemedialogName);
        mClose = (ImageView) findViewById(R.id.iv_close_ufo);
        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UFOActivity.this.finish();
            }
        });
        findViewById(R.id.iv_ufo_theme_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UFOActivity.this.finish();
            }
        });
        mWholeUFO = (RelativeLayout) findViewById(R.id.rl_ufo_withalien);
        mUFO = (ImageView) mWholeUFO.findViewById(R.id.iv_ufo);
        mAlien = (ImageView) mWholeUFO.findViewById(R.id.iv_alien);
        mDialog = (RelativeLayout) findViewById(R.id.rl_ADdialog);
        mInstall = (Button) findViewById(R.id.btn_ufo_dialog_install);
        // mNativeAd.registerView(mInstall, null);
        mThemDialogBg = (ImageView) findViewById(R.id.iv_ThemedialogBg);
        mLongLight = (ImageView) findViewById(R.id.iv_longlight);
        mCircleLight = (ImageView) findViewById(R.id.iv_circlelight);
        mSplashLight = (ImageView) findViewById(R.id.iv_splashlight);
        WindowManager wm = this.getWindowManager();
        mWindowW = wm.getDefaultDisplay().getWidth();
        mWindowH = wm.getDefaultDisplay().getHeight();
        mBtnUseTheme = (Button) findViewById(R.id.btn_usetheme);
        mBtnUseTheme.setText(UFOActivity.this.getResources().getString(R.string.ufo_theme_down));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!mHasPlayed) {
            mHasPlayed = true;
            mUFOH = mWholeUFO.getHeight();
            mUFOW = mWholeUFO.getWidth();
            playUFOFly();
        }
    }

    private void playUFOFly() {
        mWholeUFO.setVisibility(View.VISIBLE);
        float ufoStartX = mWholeUFO.getX();
        float ufoStartY = mWholeUFO.getY();

        float xWhenBorder = mWindowW - mUFOW;// UFO在最右边时的x
        float xWhenMiddle = (mWindowW - mUFOW) / 2;// UFO最终时刻的x，使得UFO在水平方向中间

        float yWhenFinal = mWindowH / 4 - mUFOH / 2;// UFO最终时刻的y，使得UFO在屏幕竖直方向1/4处
        float yWhenTop = 0f;// UFO在最顶上的y

        PropertyValuesHolder ufoX = PropertyValuesHolder.ofFloat("x",
                0.13f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.14f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.16f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.21f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.27f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.34f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.43f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.53f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.64f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.74f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.73f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.69f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.64f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.59f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.55f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.53f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.51f*mWindowW-0.5f*mWholeUFO.getWidth(),
                0.50f*mWindowW-0.5f*mWholeUFO.getWidth()
//                (xWhenBorder - ufoStartX) / 4,
//                2 * (xWhenBorder - ufoStartX) / 4,
//                3 * (xWhenBorder - ufoStartX) / 4,
//                4 * (xWhenBorder - ufoStartX) / 4,
//                xWhenMiddle + 4 * (xWhenBorder - xWhenMiddle) / 5,
//                xWhenMiddle + 3 * (xWhenBorder - xWhenMiddle) / 5,
//                xWhenMiddle + 2 * (xWhenBorder - xWhenMiddle) / 5,
//                xWhenMiddle + 1 * (xWhenBorder - xWhenMiddle) / 5, 
//                xWhenMiddle
                );
        PropertyValuesHolder ufoY = PropertyValuesHolder.ofFloat("y",
                0.24f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.24f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.23f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.22f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.21f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.20f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.18f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.17f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.16f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.18f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.23f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.28f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.31f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.33f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.35f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.36f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.36f*mWindowH-0.5f*mWholeUFO.getHeight(),
                0.36f*mWindowH-0.5f*mWholeUFO.getHeight()
//                ufoStartY,
//                4 * (yWhenTop + ufoStartY) / 7,
//                yWhenTop,
//                4 * (yWhenTop + ufoStartY) / 7,
//                ufoStartY,
//                ufoStartY + 1 * (yWhenFinal - ufoStartY) / 4,
//                ufoStartY + 2 * (yWhenFinal - ufoStartY) / 4,
//                ufoStartY + 3 * (yWhenFinal - ufoStartY) / 4,
//                yWhenFinal
                );
        PropertyValuesHolder ufoSX = PropertyValuesHolder.ofFloat("scaleX", 0.1f, 0.15f,0.2f,0.25f, 0.3f, 0.35f, 0.4f,0.45f,
                0.5f, 0.55f,1f, 1f, 1f, 1f ,1f,1f,1f,1f);
        PropertyValuesHolder ufoSY = PropertyValuesHolder.ofFloat("scaleY", 0.1f, 0.15f,0.2f,0.25f, 0.3f, 0.35f, 0.4f,0.45f,
                0.5f, 0.55f,1f, 1f, 1f, 1f ,1f,1f,1f,1f);

        final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mWholeUFO, ufoX,
                ufoY, ufoSX, ufoSY);
        animator.setDuration(2000);
        animator.start();

        animator.addListener(new MyEndAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator.clearAllAnimations();
                startLoading();
            }
        });
    }

    private void startLoading() {
        float y1 = mWholeUFO.getY();
        int detay2 = DipPixelUtil.dip2px(UFOActivity.this, 10);
        PropertyValuesHolder ufoY2 = PropertyValuesHolder.ofFloat("y", y1, y1 + detay2, y1,
                y1 - detay2, y1, y1 + detay2, y1, y1 - detay2, y1, y1 - detay2, y1);
        ObjectAnimator animator2 = ObjectAnimator.ofPropertyValuesHolder(mWholeUFO, ufoY2);
        animator2.setDuration(6000);
        animator2.start();

        mUFO.setImageResource(R.anim.ufo_light);
        mUFODrawable = (AnimationDrawable) mUFO.getDrawable();
        mAlien.setImageResource(R.anim.alien_closeeyes);
        mAlienDrawable = (AnimationDrawable) mAlien.getDrawable();
        mUFODrawable.start();
        mAlienDrawable.start();
        // boolean hasGetLoadResult = false;
        mCdt = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (((mIsADLoaded&&!mIsShowTheme)||(mIsThemeLoaded&&mIsShowTheme)) && !mHasGetLoadResult) {
                    if (mCdt != null) {
                        mCdt.onFinish();
                        LeoLog.e("poha", "mIsADLoaded="+mIsADLoaded+"...mIsShowTheme="+mIsThemeLoaded+"...mIsThemeLoaded="+mIsThemeLoaded);
                        mCdt.cancel();
                    }
                    mHasGetLoadResult = true;
                }
            }

            public void onFinish() {
                mAlienDrawable.stop();
                if (!mHasGetLoadResult) {
                    //没有任何结果出来
                    if (!mIsADLoaded&&!mIsThemeLoaded) {
                     
                        mUFODrawable.stop();
//                        mWholeUFO.setVisibility(View.INVISIBLE);
                        mLongLight.setVisibility(View.INVISIBLE);
                        mCircleLight.setVisibility(View.INVISIBLE);
//                        mAlien.setVisibility(View.INVISIBLE);
                        onNothingToShow();
                    }
                    else {
                        showResult();
                    }// else2 end
                }// if1 end
                mHasGetLoadResult = true;
            }
        }.start();
    }
    
    protected void onNothingToShow() {
        float sx = mWholeUFO.getX();
        
        PropertyValuesHolder ufodismiss = PropertyValuesHolder.ofFloat("x", sx + 50,sx + 100,sx + 150,sx + 200,mWindowW+100);
        ObjectAnimator animator2 = ObjectAnimator.ofPropertyValuesHolder(mWholeUFO, ufodismiss);
        animator2.setDuration(1000);
        animator2.addListener(new MyEndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.rl_ufo_rootview).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UFOActivity.this.finish();
                            }
                        });
                SDKWrapper.addEvent(UFOActivity.this, SDKWrapper.P1, "ad_act",
                        "adv_shws_alNA");
                findViewById(R.id.rl_ADdialog_nodata).setVisibility(View.VISIBLE);
                Button rag = (Button) findViewById(R.id.btn_rollagain);
                rag.setAllCaps(false);
                rag.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(UFOActivity.this, UFOActivity.class);
//                        UFOActivity.this.finish();
//                        startActivity(intent);
                        SDKWrapper.addEvent(UFOActivity.this, SDKWrapper.P1, "ad_cli",
                                "adv_cnts_alNA");
                        mWholeUFO.setY(DipPixelUtil.dip2px(UFOActivity.this, 30));
                        mWholeUFO.setX(0);
                        mWholeUFO.setVisibility(View.INVISIBLE);
                        findViewById(R.id.rl_ADdialog_nodata).setVisibility(View.INVISIBLE);
                        mIsADLoaded=false;
                        mIsThemeLoaded=false;
                        mHasPlayed = false;// 是否播放过动画，开始播放后置为true，以后每次WindowFocusChanged后就不播放动画了，
                        mHasGetLoadResult = false;
                        UFOActivity.this.onWindowFocusChanged(true);
                        toLoad();
                        
                    }
                });
            }
        });
        animator2.start();
    }

    protected void showResult() {
        RelativeLayout finalView;
        if (mIsShowTheme&&mIsThemeLoaded) {
            finalView = (RelativeLayout) findViewById(R.id.rl_Themedialog_root);
            SDKWrapper.addEvent(UFOActivity.this, SDKWrapper.P1, "ad_act",
                    "adv_shws_alTP");
        }
        else{
            finalView = (RelativeLayout) findViewById(R.id.rl_ADdialog);
            SDKWrapper.addEvent(UFOActivity.this, SDKWrapper.P1, "ad_act",
                    "adv_shws_alAP");
        }
        finalView.setVisibility(View.VISIBLE);
        finalView.setPivotX(finalView.getWidth() / 2);
        finalView.setPivotY(0);
        finalView.setY(mWholeUFO.getY() + mWholeUFO.getHeight()
                + DipPixelUtil.dip2px(UFOActivity.this, 5));
        float xWhenMiddle = (mWindowW - finalView.getWidth()) / 2;
        float yWhenMiddle = (mWindowH - finalView.getHeight()) / 2;
        // 广告对话框的出现动画
        PropertyValuesHolder dialogy = PropertyValuesHolder.ofFloat(
                "y",
                Math.min(finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 5), mWindowH
                        - finalView.getHeight()),
                Math.min(finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 15), mWindowH
                        - finalView.getHeight()),
                Math.min(finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 25), mWindowH
                        - finalView.getHeight()),
                Math.min(finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 55), mWindowH
                        - finalView.getHeight()),
                Math.min(finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 75), mWindowH
                        - finalView.getHeight()),
                Math.min(finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 95), mWindowH
                        - finalView.getHeight()),
                yWhenMiddle
                        - (3 / (float) 4)
                        * (yWhenMiddle - Math.min(
                                finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 95),
                                mWindowH - finalView.getHeight())),
                yWhenMiddle
                        - (2 / (float) 4)
                        * (yWhenMiddle - Math.min(
                                finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 95),
                                mWindowH - finalView.getHeight())),
                yWhenMiddle
                        - (1 / (float) 4)
                        * (yWhenMiddle - Math.min(
                                finalView.getY() + DipPixelUtil.dip2px(UFOActivity.this, 95),
                                mWindowH - finalView.getHeight())),
                yWhenMiddle);
        PropertyValuesHolder dialogscalex = PropertyValuesHolder.ofFloat("scaleX", 0.1f, 0.1f,
                0.1f, 0.1f, 0.1f, 0.4f, 0.6f, 0.8f, 0.9f, 1f);
        PropertyValuesHolder dialogscaley = PropertyValuesHolder.ofFloat("scaleY", 0.1f, 0.1f,
                0.1f, 0.1f, 0.1f, 0.4f, 0.6f, 0.8f, 0.9f, 1f);
        PropertyValuesHolder dialogalpha = PropertyValuesHolder.ofFloat("Alpha", 0.3f, 0.3f, 0.5f,
                0.7f, 1f, 1f, 1, 1f, 1f, 1f);
        ObjectAnimator animator2 = ObjectAnimator.ofPropertyValuesHolder(finalView, dialogscalex,
                dialogscaley, dialogy, dialogalpha);
        animator2.setDuration(2000);
        animator2.start();
        animator2.addListener(new MyEndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mUFODrawable.stop();
                mWholeUFO.setVisibility(View.INVISIBLE);
                mSplashLight.setVisibility(View.INVISIBLE);
                mLongLight.setVisibility(View.INVISIBLE);
                mCircleLight.setVisibility(View.INVISIBLE);
                findViewById(R.id.rl_ufo_rootview).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UFOActivity.this.finish();
                            }
                        });
            }
        });
        // 同时出现的UFO灯光动画
        mLongLight.setVisibility(View.VISIBLE);
        mCircleLight.setVisibility(View.VISIBLE);
        mSplashLight.setVisibility(View.VISIBLE);
        mSplashLight.setY(mWholeUFO.getY() + mWholeUFO.getHeight()
                - (DipPixelUtil.dip2px(UFOActivity.this, 30)));
        mLongLight.setY(mWholeUFO.getY() + mWholeUFO.getHeight()
                - (DipPixelUtil.dip2px(UFOActivity.this, 30)));
        mCircleLight.setY(mSplashLight.getY() + mSplashLight.getHeight()
                - (float) (mCircleLight.getHeight() / 2));
        mLongLight.setPivotX(0.5f);
        mLongLight.setPivotY(0);
        mSplashLight.setPivotX(mSplashLight.getWidth() / 2);

        PropertyValuesHolder splashLightScaleXAnim = PropertyValuesHolder.ofFloat("scaleX", 0.2f,
                0.4f, 0.6f, 0.8f, 1.0f);
        ObjectAnimator animator3 = ObjectAnimator.ofPropertyValuesHolder(mSplashLight,
                splashLightScaleXAnim);
        animator3.setDuration(700);
        animator3.start();
        // mCircleLight.setPivotX(mCircleLight.getWidth()/2);
        // mCircleLight.setPivotY(mCircleLight.getHeight()/2);
        PropertyValuesHolder circleLightScaleXAnim = PropertyValuesHolder.ofFloat("scaleX", 0.2f,
                0.4f, 0.6f, 0.8f, 1.0f);
        ObjectAnimator animator4 = ObjectAnimator.ofPropertyValuesHolder(mCircleLight,
                circleLightScaleXAnim);
        animator4.setDuration(700);
        animator4.start();
    }

    private abstract class MyEndAnimatorListener implements AnimatorListener
    {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

    }

    @Override
    public void finish() {
        if (mCdt != null) {
            mCdt.cancel();
        }
        super.finish();
        overridePendingTransition(DEFAULT_KEYS_DISABLE, DEFAULT_KEYS_DISABLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAdEngine!=null){
        mAdEngine.release(Constants.UNIT_ID_58);
        }
//        LockScreenActivity.interupAinimation = false;
        overridePendingTransition(DEFAULT_KEYS_DISABLE, DEFAULT_KEYS_DISABLE);
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }
}
