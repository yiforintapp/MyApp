
package com.leo.appmaster.battery;


import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.animation.ThreeDimensionalRotationAnimation;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.fragment.PretendAppBeautyFragment;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.ui.WaveView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

public class BatteryMainActivity extends BaseFragmentActivity implements OnClickListener {
    private final String TAG = "BatterMainActivity";
    private boolean DBG = true;
    private CommonToolbar mCtbMain;
    private RelativeLayout mRlContent;
    private Fragment mFrgmResult;
    private GridView mGvApps;
    private ArrayList<BatteryComsuption> mListBatteryComsuptions;
    private RippleView mRvBoost;
    private final int APPS_COLUMNS = 5;
    private AppsAdapter mAdapter;
    private ProgressBar mPbLoading;
    private RelativeLayout mRlEmpty;
    private RelativeLayout mRlLoadingOrEmpty;
    private BatteryManager mBtrManager;
    private TextView mTvListTitle;
    private RelativeLayout mRlWholeBattery;
    private RelativeLayout mRlWholeShield;
    private ImageView mIvShield;
    private WaveView mWvBattery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_manage);
        mBtrManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
        initUI();
    }

    
    private void initUI() {
        mRlWholeBattery = (RelativeLayout) findViewById(R.id.rl_wholebattery);
        mWvBattery = (WaveView) findViewById(R.id.wv_battery);
        mWvBattery.setWaveColor(0xff00ccff);
        mWvBattery.setPercent(30);
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
                Intent intent = new Intent(BatteryMainActivity.this, BatterySettingActivity.class);
                startActivity(intent);
            }
        });
        mCtbMain.setOptionImageResource(R.drawable.setup_icon);
        mCtbMain.setOptionMenuVisible(true);
        mGvApps = (GridView) findViewById(R.id.gv_apps);
        mGvApps.setNumColumns(APPS_COLUMNS);
        mAdapter = new AppsAdapter();
        mRlContent = (RelativeLayout) findViewById(R.id.rl_content);
        mRvBoost = (RippleView) findViewById(R.id.rv_accelerate);
        mRvBoost.setOnClickListener(this);
        mGvApps.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
//        if (mFrgmResult != null && mFrgmResult.isVisible()) {
//            FragmentManager fm = getSupportFragmentManager();
//            FragmentTransaction transaction = fm.beginTransaction();  
//            transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
//            transaction.remove(mFrgmResult);
//            transaction.commit();
//            showLoading();
//            loadData();
//        } else {
            super.onBackPressed();
//        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    public void showLoading() {
        mRlLoadingOrEmpty.setVisibility(View.VISIBLE);
        mPbLoading.setVisibility(View.VISIBLE);
        mRlEmpty.setVisibility(View.GONE);
        mTvListTitle.setText(R.string.batterymanage_tip_loading);
    }
    
    public void hideLoadingOrEmpty() {
        mRlLoadingOrEmpty.setVisibility(View.GONE);
        if (mListBatteryComsuptions != null) {
            mTvListTitle.setText(String.format(getString(R.string.batterymanage_label), mListBatteryComsuptions.size()));
        }
    }
    
    public void showEmpty() {
        mPbLoading.setVisibility(View.GONE);
        mRlLoadingOrEmpty.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mBtrManager.updateBatteryPageState(true);
        LeoLog.i(TAG, "onResume");
        if (mFrgmResult != null && mFrgmResult.isVisible()) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();  
            transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
            transaction.remove(mFrgmResult);
            transaction.commit();
        }
        showLoading();
        loadData();
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
        mAdapter.fillData(mListBatteryComsuptions);
        if (mListBatteryComsuptions == null || mListBatteryComsuptions.size() == 0) {
            showEmpty();
        } else {
            hideLoadingOrEmpty();
        }
        mAdapter.notifyDataSetChanged();
    }
    
    
    @Override
    public void onClick(View v) {
         switch (v.getId()) {
            case R.id.rv_accelerate:
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
        startBoostAnimation(0);
    }

    private void startFlipAnimation() {
        final float centerX = mIvShield.getWidth() / 2.0f;  
        final float centerY = mIvShield.getHeight() / 2.0f;  
  
        final ThreeDimensionalRotationAnimation rotation = new ThreeDimensionalRotationAnimation(0, 180,  
                centerX, centerY, 0.0f, true);  
        rotation.setDuration(2000);  
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
            }
        });
        rotation.setFillAfter(false);  
        rotation.setInterpolator(new AccelerateInterpolator());  
        mIvShield.startAnimation(rotation);  
    }

    
    protected void startTranslateAnim() {
        float initialX = mIvShield.getX();
        float initialY = mIvShield.getY();
        float initialW = mIvShield.getWidth();
        float initialH = mIvShield.getHeight();
        PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat("x", initialX, 0f);
        PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat("y", initialY, 0f);
        PropertyValuesHolder holderScaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.5f);
        PropertyValuesHolder holderScaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.5f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mIvShield, holderX, holderY, holderScaleX, holderScaleY);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LeoLog.i(TAG, "x :" + mIvShield.getX());
                LeoLog.i(TAG, "y :" + mIvShield.getY());
                LeoLog.i(TAG, "sx :" + mIvShield.getScaleX());
                LeoLog.i(TAG, "sy :" + mIvShield.getScaleX());
            }
        });
        anim.setDuration(1000);
        anim.start();
    }

    private void startBoostAnimation(final int startIndex) {
        if (mGvApps.getCount() <= startIndex) {
            startBatteryDismissAnim();
            return;
        }

        final long iconDisappearTime = 200;
        final long iconGapTime = 80;
        final long rowUpTime = 120;
        final int rotateDegree = 180;

        int columnNum = mGvApps.getNumColumns();

        final int count = Math.min(columnNum, mGvApps.getCount()-startIndex);
        // LeoLog.d(TAG, "startIndex="+startIndex+"; count="+count);
        for (int i=0 ; i<count ; i++) {
            final View vv = mGvApps.getChildAt(i+startIndex);
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
                                //TODO 一定要与xml里面的vertical spacing值一致！
                                int vSpacing = DipPixelUtil.dip2px(BatteryMainActivity.this, 10);
                                float rowHeight = vv.getHeight() + vSpacing;
                                mGvApps.animate().setDuration(rowUpTime).translationYBy(-rowHeight);
                                startBoostAnimation(startIndex+count);
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
            return convertView;
        }
    }

    class Holder {
        ImageView iv_appicon;
    }
}
