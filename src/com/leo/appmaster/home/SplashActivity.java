
package com.leo.appmaster.home;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.EdgeEffectCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CirclePageIndicator;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;

public class SplashActivity extends BaseActivity implements OnPageChangeListener {

    public static final int MSG_LAUNCH_HOME_ACTIVITY = 1000;

    private Handler mEventHandler;
    
    /* Guide page stuff begin */
    private ViewPager mViewPager;    
    /* pages */
    private ArrayList<View> mPageViews;
    private GuideItemView mPageBackgroundView;
    /* footer indicators */
    private CirclePageIndicator mIndicator;
    private ViewGroup mMain;
    /* color for each page */
    private int[] mPageColors = new int[4];
    private EdgeEffectCompat leftEdge;
    private EdgeEffectCompat rightEdge;
    /* Guide page stuff end */
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoLog.d("SplashActivity", "onCreate");
        setContentView(R.layout.activity_splash_guide);
        
        mEventHandler = new EventHandler();
        startInitTask();
        LeoEventBus.getDefaultBus().register(this, 2);
    }

    @Override
    protected void onDestroy() {
        LeoLog.d("SplashActivity", "onDestroy");
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
        mEventHandler = null;
    }

    @Override
    protected void onResume() {
        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
        mEventHandler.sendEmptyMessageDelayed(MSG_LAUNCH_HOME_ACTIVITY, 1000);
        LockManager lm = LockManager.getInstatnce();
        lm.clearFilterList();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onEvent(AppUnlockEvent event) {
        if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_SUCCESSFULLY) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AppMasterApplication.getInstance().startActivity(intent);
            this.finish();
        } else if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_CANCELED) {
            this.finish();
        } else if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_OUTCOUNT) {
        }
    }

    private class EventHandler extends Handler {

        public EventHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LAUNCH_HOME_ACTIVITY:
                    if(AppMasterPreference.getInstance(SplashActivity.this).getFirstUse()){
                        boolean guidNotShown = mMain == null || mMain.getVisibility() != View.VISIBLE;
                        if(guidNotShown) {
                            showGuide();
                        }
                    }else{
                        startHome();
                    }
                    break;

                default:
                    break;
            }}
    }
    
    private void startHome() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        if (amp.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            LeoLog.d("Track Lock Screen", "apply lockscreen form SplashActivity");
            LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL,
                    getPackageName(), true, null);          
            amp.setDoubleCheck(null);
        } else {
            Intent intent = new Intent(this, LockSettingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void startInitTask() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // get recommend app lock list
                final AppMasterPreference pref = AppMasterPreference
                        .getInstance(getApplicationContext());
                long lastPull = pref.getLastLocklistPullTime();
                long interval = pref.getPullInterval();
                if (interval < (System.currentTimeMillis() - lastPull)
                        && NetWorkUtil.isNetworkAvailable(SplashActivity.this)) {
                    HttpRequestAgent.getInstance(getApplicationContext())
                            .getAppLockList(new Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response, boolean noModify) {
                                    JSONArray list;
                                    ArrayList<String> lockList = new ArrayList<String>();
                                    long next_pull;
                                    JSONObject data;
                                    try {
                                        data = response.getJSONObject("data");
                                        list = data.getJSONArray("list");
                                        for (int i = 0; i < list.length(); i++) {
                                            lockList.add(list.getString(i));
                                        }
                                        next_pull = data.getLong("next_pull");
                                        LeoLog.d("next_pull = " + next_pull
                                                + " lockList = ",
                                                lockList.toString());

                                        pref.setPullInterval(next_pull * 24
                                                * 60 * 60 * 1000);
                                        pref.setLastLocklistPullTime(System
                                                .currentTimeMillis());
                                        Intent intent = new Intent(
                                                AppLoadEngine.ACTION_RECOMMEND_LIST_CHANGE);
                                        intent.putStringArrayListExtra(
                                                Intent.EXTRA_PACKAGES, lockList);
                                        SplashActivity.this
                                                .sendBroadcast(intent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            }, new ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    LeoLog.d("Pull Lock list",
                                            error.getMessage());
                                }
                            });
                }
            }
        }).start();
    }
    
    /* add for Guide Screen begin */
    private void showGuide(){
        mPageColors[0] = getResources().getColor(R.color.guide_page1_background_color);
        mPageColors[1] = getResources().getColor(R.color.guide_page2_background_color);
        mPageColors[2] = getResources().getColor(R.color.guide_page3_background_color);
        mPageColors[3] = getResources().getColor(R.color.guide_page4_background_color);
        
        LayoutInflater inflater = getLayoutInflater();    
        mPageViews = new ArrayList<View>();
        mPageBackgroundView = (GuideItemView) findViewById(R.id.guide_bg_view);
        mPageBackgroundView.initBackgroundColor(mPageColors[0]);
        ImageView bigImage = null;
        TextView tvTitle = null;
        TextView tvContent = null;
        /* page1 */
        ViewGroup page1 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page1.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.page_1));
        tvTitle = (TextView) page1.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.guide_page1_title));
        tvContent = (TextView) page1.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.guide_page1_content));
        mPageViews.add(page1);
        /* page2 */
        ViewGroup page2 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page2.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.page_2));
        tvTitle = (TextView) page2.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.guide_page2_title));
        tvContent = (TextView) page2.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.guide_page2_content));
        mPageViews.add(page2);
        /* page3 */
        ViewGroup page3 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page3.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.page_3));
        tvTitle = (TextView) page3.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.guide_page3_title));
        tvContent = (TextView) page3.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.guide_page3_content));
        mPageViews.add(page3);
        /* page4 */
        ViewGroup page4 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page4.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.page_4));
        tvTitle = (TextView) page4.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.guide_page4_title));
        tvContent = (TextView) page4.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.guide_page4_content));
        mPageViews.add(page4);
        
        mMain = (ViewGroup)findViewById(R.id.layout_guide);
        mViewPager = (ViewPager)mMain.findViewById(R.id.guide_viewpager);
        initViewPagerEdges(mViewPager);

        mMain.setVisibility(View.VISIBLE);
        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setDuration(1000);
        mMain.startAnimation(aa);
        
        mViewPager.setAdapter(new GuidePageAdapter());
        mIndicator = (CirclePageIndicator) findViewById(R.id.splash_indicator);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setOnPageChangeListener(this);
        
        Button button = (Button)   mPageViews.get(mPageViews.size()-1).findViewById(R.id.button_guide);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppMasterPreference.getInstance(SplashActivity.this).setFirstUse(false);
                startHome();
            }
        });
    }

    class GuidePageAdapter extends PagerAdapter {    
         
        @Override    
        public int getCount() {    
            return mPageViews.size();    
        }    
    
        @Override    
        public boolean isViewFromObject(View arg0, Object arg1) {    
            return arg0 == arg1;    
        }    
    
        @Override    
        public int getItemPosition(Object object) {    
            return super.getItemPosition(object);    
        }    
    
        @Override    
        public void destroyItem(View arg0, int arg1, Object arg2) {    
            ((ViewPager) arg0).removeView(mPageViews.get(arg1));    
        }    
    
        @Override    
        public Object instantiateItem(View arg0, int arg1) {    
            ((ViewPager) arg0).addView(mPageViews.get(arg1));      
            return mPageViews.get(arg1);    
        }    
    
        @Override    
        public void restoreState(Parcelable arg0, ClassLoader arg1) {    
        }    
    
        @Override    
        public Parcelable saveState() {    
            return null;    
        }    
    
        @Override    
        public void startUpdate(View arg0) {    
        }    
    
        @Override    
        public void finishUpdate(View arg0) {    
        }    
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if(arg1 > 0.0f && arg0+1<mPageViews.size()){
            int newColor = caculateNewColor(
                    mPageColors[arg0], mPageColors[arg0+1], arg1);
            mPageBackgroundView.setCurrentColor(newColor);
        }
        /* disable edges scrolling effect */
        if (leftEdge != null && rightEdge != null) {
            leftEdge.finish();
            rightEdge.finish();
            leftEdge.setSize(0, 0);
            rightEdge.setSize(0, 0);
        }
        if (mViewPager != null) {
            mViewPager.invalidate();
        }
    }

    @Override
    public void onPageSelected(int arg0) {
    }
    
    private int caculateNewColor(int originColor, int targetColor, float position){
        int originRGB[] = {Color.red(originColor), Color.green(originColor), Color.blue(originColor)};
        int targetRGB[] = {Color.red(targetColor), Color.green(targetColor), Color.blue(targetColor)};
        int newRGB[] = new int[3];
        for(int i = 0;i<3;i++){
            newRGB[i] = (int) (originRGB[i] + (targetRGB[i] - originRGB[i]) * position);
        }
        return Color.rgb(newRGB[0], newRGB[1], newRGB[2]);
    }
    
    private void initViewPagerEdges(ViewPager viewPager) {
        try {
            Field leftEdgeField = viewPager.getClass().getDeclaredField("mLeftEdge");
            Field rightEdgeField = viewPager.getClass().getDeclaredField("mRightEdge");
            if (leftEdgeField != null && rightEdgeField != null) {
                leftEdgeField.setAccessible(true);
                rightEdgeField.setAccessible(true);
                leftEdge = (EdgeEffectCompat) leftEdgeField.get(viewPager);
                rightEdge = (EdgeEffectCompat) rightEdgeField.get(viewPager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
      
   /* add for Guide Screen end */
}
