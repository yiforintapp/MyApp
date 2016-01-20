
package com.leo.appmaster.battery;


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.animation.ThreeDimensionalRotationAnimation;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BatteryViewEvent;
import com.leo.appmaster.intruderprotection.IntruderCatchedActivity;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.BatteryManager.BatteryState;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.WaveView;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

public class BatteryMainActivity extends BaseFragmentActivity implements OnClickListener {
    private final String TAG = "BatterMainActivity";
    private TextView mTvEmpty;
    private CommonToolbar mCtbMain;
    private TextView mTvPercentValue;
    private BatteryAppGridView mGvApps;
    private ArrayList<BatteryComsuption> mListBatteryComsuptions;
    private RippleView mRvBoost;
    private final int APPS_COLUMNS = 6;
    private AppsAdapter mAdapter;
    private int mListAmount = 0;
    private ProgressBar mPbLoading;
    private RelativeLayout mRlEmpty;
    private RelativeLayout mRlLoadingOrEmpty;
    private BatteryManager mBtrManager;
    private TextView mTvListTitle;
    private TextView mTvComplete;
    private RelativeLayout mRlTopAnimLayout;
    private RelativeLayout mRlWholeBattery;
    private ImageView mIvShield;
    private boolean mIsResultShowed = false;
    private WaveView mWvBattery;
    private BatteryBoostResultFragment mFrgmResult;
    private TextView mTvBoostedNumber;
    private final int TOP_TRANSLUCENT_HEIGHT = DipPixelUtil.dip2px(this, 160);
    private final int TRANSLATE_ANIM_DURATION = 600;
    private ImageView mIvLittleBattery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_manage);
        mBtrManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
        LeoEventBus.getDefaultBus().register(this);
        initUI();
    }

    public void onEventMainThread(BatteryViewEvent event) {
        BatteryState state = event.state;
//        state.p//0 没充电 其他都充电
        if (state.plugged == 0) {
            mIvLittleBattery.setImageResource(R.drawable.batterymanage_littlebattery_normal);
        } else {
            mIvLittleBattery.setImageResource(R.drawable.batterymanage_littlebattery);
        }
        mTvPercentValue.setText(state.level + "");             
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
    }
    
    private void initUI() {
        mTvBoostedNumber = (TextView) findViewById(R.id.tv_boost_complete_number);
        if (mFrgmResult == null) {
            mFrgmResult = new BatteryBoostResultFragment();
        }
        mIvLittleBattery = (ImageView) findViewById(R.id.iv_littlebattery);
        mRlTopAnimLayout = (RelativeLayout) findViewById(R.id.rl_anim_layout);
        mTvComplete = (TextView) findViewById(R.id.tv_boost_complete);
        mTvPercentValue = (TextView) findViewById(R.id.tv_percent_value);
        mTvEmpty = (TextView) findViewById(R.id.tv_empty);
        mRlWholeBattery = (RelativeLayout) findViewById(R.id.rl_wholebattery);
        mWvBattery = (WaveView) findViewById(R.id.wv_battery);
        mWvBattery.setWaveColor(0xff00ccff);
//        mWvBattery.setWave2Color(0xff0091ff);
        mWvBattery.setWave2Color(0xff00ccff);
        mIvShield= (ImageView) findViewById(R.id.iv_shield);
        mTvListTitle = (TextView) findViewById(R.id.tv_list_title);
        mRlLoadingOrEmpty = (RelativeLayout) findViewById(R.id.rl_empty_or_loading);
        mPbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        mRlEmpty = (RelativeLayout) findViewById(R.id.rl_empty);
        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_battery);
        mCtbMain.setToolbarTitle(R.string.hp_device_power);
        mCtbMain.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKWrapper.addEvent(BatteryMainActivity.this, SDKWrapper.P1,
                        "batterypage", "setting");
                Intent intent = new Intent(BatteryMainActivity.this, BatterySettingActivity.class);
                startActivity(intent);
            }
        });
        mCtbMain.setOptionImageResource(R.drawable.setup_icon);
        mCtbMain.setOptionMenuVisible(true);
        mGvApps = (BatteryAppGridView) findViewById(R.id.gv_apps);
        mGvApps.setNumColumns(APPS_COLUMNS);
        mAdapter = new AppsAdapter();
        mRvBoost = (RippleView) findViewById(R.id.rv_accelerate);
        mRvBoost.setOnClickListener(this);
        mGvApps.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mIsResultShowed) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "batterypage", "promote_back");
        }
            super.onBackPressed();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    public void showLoading() {
        mRvBoost.setEnabled(false);
        mRvBoost.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_shape_disable));
        mRlLoadingOrEmpty.setVisibility(View.VISIBLE);
        mPbLoading.setVisibility(View.VISIBLE);
        mRlEmpty.setVisibility(View.GONE);
        mTvListTitle.setText(R.string.batterymanage_tip_loading);
    }
    
    public void hideLoadingOrEmpty() {
        mRlLoadingOrEmpty.setVisibility(View.GONE);
        if (mListBatteryComsuptions != null) {
            mTvListTitle.setText(Html.fromHtml(getString(R.string.batterymanage_label_old, mListBatteryComsuptions.size())));
        }
    }
    
    public void showEmpty() {
        mTvListTitle.setText(R.string.batterymanage_tip_nothing_to_boost);
        mPbLoading.setVisibility(View.GONE);
        mRlEmpty.setVisibility(View.VISIBLE);
        mRlLoadingOrEmpty.setVisibility(View.VISIBLE);
    }
    
    
    
    @Override
    public void onResume() {
        super.onResume();
        mTvPercentValue.setText(mBtrManager.getBatteryLevel() + "");
        if (mBtrManager.getIsCharing()) {
            mIvLittleBattery.setImageResource(R.drawable.batterymanage_littlebattery);
        } else {
            mIvLittleBattery.setImageResource(R.drawable.batterymanage_littlebattery_normal);
        }
        mWvBattery.setPercent(mBtrManager.getBatteryLevel());
        mWvBattery.setFactorA(15f);
//        mWvBattery.setPercent(5);
        mBtrManager.updateBatteryPageState(true);
        LeoLog.i(TAG, "onResume");
//        if (mFrgmResult != null && mFrgmResult.isVisible()) {
//            FragmentManager fm = getSupportFragmentManager();
//            FragmentTransaction transaction = fm.beginTransaction();  
//            transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
//            transaction.remove(mFrgmResult);
//            transaction.commit();
//        }
        showLoading();
        if (mBtrManager.shouldEnableCleanFunction()) {
//        if (true) {
            //不在上一次清理的两分钟内 可以重新load应用列表和清理加速
//            mRvBoost.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_btn_shape));
//            mRvBoost.setEnabled(true);
            LeoLog.i(TAG, "set green and enable");
            loadData();
        } else {
            //在上一次清理的两分钟内 UI做特殊显示
            mRvBoost.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_shape_disable));
            mRvBoost.setEnabled(false);
            LeoLog.i(TAG, "set grey and disanable");
            ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showEmpty();
                    mTvListTitle.setText(R.string.batterymanage_tip_nothing_to_boost);
                    mTvEmpty.setText(R.string.batterymanage_tip_nothing_to_boost);
                    if (!mIsResultShowed) {
                        mListAmount = 0;
                        startBatteryDismissAnim();
                        SDKWrapper.addEvent(BatteryMainActivity.this, SDKWrapper.P1, "batterypage", "promote_redirect");
                        mIsResultShowed = true;
                    }
                }
            }, 1000);//TODO 假loading的持续时间
        }
    }

    
    @Override
    protected void onPause() {
        super.onPause();
        mBtrManager.updateBatteryPageState(false);
    }

    private void loadData() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mListBatteryComsuptions = (ArrayList<BatteryComsuption>) mBtrManager.getBatteryDrainApps();
                runOnUiThread(new Runnable() {
                    public void run() {
                        onDataLoaded();
                    }
                });
            }
        });
    }

    private void onDataLoaded() {
        mRvBoost.setEnabled(true);
        mRvBoost.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_btn_shape));
        mAdapter.fillData(mListBatteryComsuptions);
        if (mListBatteryComsuptions == null || mListBatteryComsuptions.size() == 0) {
            showEmpty();
        } else {
            mListAmount = mListBatteryComsuptions.size();
            SDKWrapper.addEvent(this, SDKWrapper.P1, "batterypage", "backapps_" + mListBatteryComsuptions.size());
            hideLoadingOrEmpty();
        }
        mAdapter.notifyDataSetChanged();
    }
    
    
    @Override
    public void onClick(View v) {
         switch (v.getId()) {
            case R.id.rv_accelerate:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "batterypage", "savepower");
                startBoost();
                break;
            default:
                break;
        }
    }

    private void startBoost() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mBtrManager.killBatteryDrainApps();
            }
        });
        prepareBoostAnimation();
    }

    private void startFlipAnimation() {
        final float centerX = mIvShield.getWidth() / 2.0f;  
        final float centerY = mIvShield.getHeight() / 2.0f;  
  
        final ThreeDimensionalRotationAnimation rotation = new ThreeDimensionalRotationAnimation(-90, 0,  
                centerX, DipPixelUtil.dip2px(this, 26), 0.0f, true);  
        rotation.setDuration(680);  
        rotation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                startTranslateAnim();
                startShortenTopLayoutAnim();
                startShowCompleteAnim();
                showResultFragment();
            }
        });
        rotation.setFillAfter(false);  
        mIvShield.startAnimation(rotation);  
    }


    private void startShortenTopLayoutAnim() {
        LeoLog.i("tempp", "start shorten anim");
        int height = mRlTopAnimLayout.getHeight();
        PropertyValuesHolder holderShorten = PropertyValuesHolder.ofInt("dfads", height, (int)(height * 0.7));
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mRlTopAnimLayout, holderShorten);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams layoutParams = mRlTopAnimLayout.getLayoutParams();
                layoutParams.height = (Integer) animation.getAnimatedValue();
                LeoLog.i("tempp", "heigh = " + mRlTopAnimLayout.getHeight());
                mRlTopAnimLayout.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(TRANSLATE_ANIM_DURATION);
        anim.start();
    }

    protected void showResultFragment() {
        SDKWrapper.addEvent(this, SDKWrapper.P1, "batterypage", "promote");
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
        if (mFrgmResult == null) {
            mFrgmResult = new BatteryBoostResultFragment();
        }
        transaction.replace(R.id.rl_result_layout, mFrgmResult);
        transaction.commit();
        mIsResultShowed = true;
    }

    protected void startShowCompleteAnim() {
        mTvComplete.setVisibility(View.VISIBLE);
        mTvBoostedNumber.setVisibility(View.VISIBLE);
        if (mListAmount != 0) {
            mTvBoostedNumber.setText(String.format(getString(R.string.batterymanage_boosted_number), mListAmount));
        } else {
            mTvBoostedNumber.setText(getString(R.string.batterymanage_tip_nothing_to_boost));
        }
//        batterymanage_boosted_number
        PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mTvComplete, alphaHolder);
        ObjectAnimator anim2 = ObjectAnimator.ofPropertyValuesHolder(mTvBoostedNumber, alphaHolder);
        AnimatorSet as = new AnimatorSet();
        as.playTogether(anim, anim2);
        as.setDuration(600);
        as.start();
    }

    protected void startTranslateAnim() {
        mIvShield.setVisibility(View.VISIBLE);
        float initialX = mIvShield.getX();
        float initialY = mIvShield.getY();
        PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat("x", initialX, DipPixelUtil.dip2px(this, 15));
//        PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat("top", initialY, DipPixelUtil.dip2px(this, 10));
        PropertyValuesHolder holderScaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.66f);
        PropertyValuesHolder holderScaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.66f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mIvShield, holderX,  holderScaleX, holderScaleY);
        anim.setDuration(TRANSLATE_ANIM_DURATION);
        anim.start();
    }

    private void prepareBoostAnimation() {
        final int remainItemCount = mListBatteryComsuptions.size();

        // 禁止拖动
        mGvApps.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }

        });

        // 设置GridLayoutAnimation
        float height = 150.0f;
        View vv = mGvApps.getChildAt(0);
        if (vv != null) {
            //TODO 需要与layout中的vertical spacing保持一致！
            height = vv.getHeight() + DipPixelUtil.dip2px(BatteryMainActivity.this, 0);
//            height = vv.getHeight() + mGvApps.getVerticalSpacing();
            LeoLog.d(TAG, "height = " + height);
        }
        TranslateAnimation ta = new TranslateAnimation(0.0f, 0.0f, height, 0.0f);
        ta.setInterpolator(new AccelerateInterpolator());
        ta.setDuration(120);
        GridLayoutAnimationController gac = new GridLayoutAnimationController(ta, 0, 0);
        gac.setDirection(GridLayoutAnimationController.DIRECTION_LEFT_TO_RIGHT);
        mGvApps.setLayoutAnimation(gac);

        if (mGvApps.computeVerticalScrollOffset() == 0) {
            startBoostAnimation(remainItemCount);
        } else {
            // 移动回顶端
            mGvApps.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                            && mGvApps.computeVerticalScrollOffset() == 0) {
                        startBoostAnimation(remainItemCount);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });
            mGvApps.setSmoothScrollbarEnabled(true);
            mGvApps.smoothScrollToPosition(0);
        }
    }


    private void startBoostAnimation(final int remainCount) {
        if (remainCount <= 0) {
            // 杀死app动画完成
            if (!mIsResultShowed) {
                startBatteryDismissAnim();
                mIsResultShowed = true;
            }
            return;
        }

        final long iconDisappearTime = 200;
        final long iconGapTime = 80;
        final int rotateDegree = 180;

        final int columnNum = mGvApps.getNumColumns();

        final int count = Math.min(columnNum, remainCount);
        // LeoLog.d(TAG, "startIndex="+startIndex+"; count="+count);
        for (int i=0 ; i<count ; i++) {
            final View vv = mGvApps.getChildAt(i);   //mGvApps.getChildAt(i+startIndex);
            // LeoLog.d(TAG, "index="+(i+startIndex));
            final boolean isLastIcon = (i == count-1);
            vv.animate().setDuration(iconDisappearTime)
                    .setStartDelay(i*iconGapTime)
                    .rotation(rotateDegree)
                    .scaleX(0.0f)
                    .scaleY(0.0f)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            super.onAnimationEnd(animation);
                            vv.setAlpha(0.0f);
                            if (isLastIcon) {
                                for (int j=0;j<count;j++) {
                                    mListBatteryComsuptions.remove(0);
                                }
                                mGvApps.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                        mGvApps.startLayoutAnimation();
                                        mGvApps.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startBoostAnimation(remainCount - count);
                                            }
                                        }, 5);
                                    }
                                }, 125);
                            }
                        }
                    });
        }
    }

    
    private void startBatteryDismissAnim() {
        PropertyValuesHolder holderAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mRlWholeBattery, holderAlpha);
        anim.setDuration(400);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                startFlipAnimation();
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        anim.start();
    }

    class AppsAdapter extends BaseAdapter {
        private List<BatteryComsuption> mList;
        LayoutInflater mInflater;
        
        public void fillData(ArrayList<BatteryComsuption> list) {
            mList = list;
        }

        public AppsAdapter() {
            mInflater = LayoutInflater.from(BatteryMainActivity.this);
        }
        
        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getItem(int position) {
            return getView(position, null, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                holder = new Holder();
                convertView = mInflater.inflate(R.layout.item_battery_app, null);
                holder.iv_appicon = (ImageView) convertView.findViewById(R.id.iv_app);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            if (mList != null && mList.size() != 0) {
                holder.iv_appicon.setImageDrawable(mList.get(position).getIcon());
            }
            convertView.setRotation(0.0f);
            convertView.setScaleX(1.0f);
            convertView.setScaleY(1.0f);
            convertView.setAlpha(1.0f);
            return convertView;
        }
    }

    class Holder {
        ImageView iv_appicon;
    }
}
