package com.leo.appmaster.appmanage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.GestureLayout.IGestureListener;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.ui.BatteryChartsView;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CustomViewPager;
import com.leo.appmaster.utils.TextFormater;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AppDetailActivity extends Activity implements
		OnPageChangeListener, OnClickListener, IGestureListener{

	public static final String EXTRA_LOAD_PKG = "load_pkg_name";

	private TextView mTvUseInfo;
    private TextView mTvPermissionInfo;
    private CustomViewPager mViewPager;
    private ImageView mIvIcon;
	
    private RelativeLayout mBatteryInfoLayout;
    private BatteryChartsView mBatteryCharts;
	
    private LinearLayout mTitle;
    private LinearLayout mAppBaseInfoLayout;
    private ImageView mAppIcon;
    private TextView mAppName;
    private TextView mAppVersion;
    private TextView mAppMemory;
    private TextView mAppUsedFlow;
    private GestureLayout mGestureLayout;
    
    private Button mUninstall;
    private Button mStop;

    private LinearLayout mPager1;
    private AppInfoBaseLayout mPager2;

    private Bitmap mAppBaseInfoLayoutbg;
	
	private AppDetailInfo mAppInfo;
	
	private Date mCurrdate;
	private Calendar mCalendar = Calendar.getInstance();;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_detail);
		handleIntent();
		initUI();
	}

	private void handleIntent() {
		Intent intent = this.getIntent();
		String pkg = intent.getStringExtra(EXTRA_LOAD_PKG);
		mAppInfo = AppLoadEngine.getInstance(this).loadAppDetailInfo(pkg);
	}

	private void initUI() {
		mTvUseInfo = (TextView) findViewById(R.id.tv_app_use_info);
		mTvPermissionInfo = (TextView) findViewById(R.id.tv_app_permission_info);
		mTvUseInfo.setOnClickListener(this);
		mTvPermissionInfo.setOnClickListener(this);
		
		mViewPager = (CustomViewPager) findViewById(R.id.pager_app_detail);
		mAppBaseInfoLayout = (LinearLayout) findViewById(R.id.app_base_info);
		mTitle = (LinearLayout) findViewById(R.id.activity_title);
		mTitle.setOnClickListener(this);
        mAppIcon = (ImageView) findViewById(R.id.app_info_icon);
        mAppIcon.setImageDrawable(mAppInfo.getAppIcon());
        mAppName = (TextView) findViewById(R.id.app_name);
        mAppName.setText(mAppInfo.getAppLabel());
        mAppVersion = (TextView) findViewById(R.id.version_name);
        mAppVersion.setText(mAppInfo.getVersionName());
        mAppMemory = (TextView) findViewById(R.id.app_memory);
        mAppMemory.setText(String.format(getString(R.string.app_memory), mAppInfo.getCacheInfo().getCodeSize()));
        mAppUsedFlow = (TextView) findViewById(R.id.app_used_flow);
        mAppUsedFlow.setText(String.format(getString(R.string.app_used_flow), TextFormater.dataSizeFormat(mAppInfo.getTrafficInfo().getReceivedData() + mAppInfo.getTrafficInfo().getMtransmittedData())));
        
        mUninstall = (Button) findViewById(R.id.uninstall_app);
        mUninstall.setOnClickListener(this);
        mStop = (Button) findViewById(R.id.stop_app);
        mStop.setOnClickListener(this);
        
		mPager1 = (LinearLayout)this.getLayoutInflater().inflate(R.layout.activity_user_details, null);
		mPager2 = (AppInfoBaseLayout) this.getLayoutInflater().inflate(	R.layout.pager_app_use_info, null);
        mBatteryCharts = (BatteryChartsView) mPager1.findViewById(R.id.battery_charts);
        mBatteryCharts.setPowerPercent((float)mAppInfo.getPowerComsuPercent() / 100);
		mPager2.setAppDetailInfo(mAppInfo);
		mViewPager.setAdapter(new AppdetailAdapter());
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setScanScroll(false);
		mViewPager.requestDisallowInterceptTouchEvent(true);
		
		mGestureLayout = (GestureLayout) mPager1.findViewById(R.id.gesture_layout);
		mGestureLayout.setListener(this);
		setAppInfoBackground(mAppInfo.getAppIcon());
		mCurrdate = mCalendar.getTime();
		setWeekString(mCurrdate);
	}

	private void setAppInfoBackground(Drawable drawable) {
	    float d = (float)3 / 2/*mAppBaseInfoLayout.getWidth() / mAppBaseInfoLayout.getHeight()*/;
	    
	    int w = (int)(drawable.getIntrinsicHeight() * d / 2);
	    int h = drawable.getIntrinsicHeight() / 2;
	    mAppBaseInfoLayoutbg = Bitmap.createBitmap(
	            w,
                h,
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(mAppBaseInfoLayoutbg);
        canvas.drawColor(Color.WHITE);
        drawable.setBounds(-(drawable.getIntrinsicWidth()  - w) / 2, -(drawable.getIntrinsicHeight() - h) / 2, (drawable.getIntrinsicWidth()  - w) / 2 + w, (drawable.getIntrinsicHeight() - h) / 2 + h);
        drawable.draw(canvas);
        canvas.drawColor(Color.argb(100, 0, 0, 0));
        
        mAppBaseInfoLayout.setBackgroundDrawable(new BitmapDrawable(mAppBaseInfoLayoutbg));

	}

	
	private void setWeekString(Date currDate) {
	    String newMessageInfo;
	    
        TextView sun = (TextView) mPager1.findViewById(R.id.sunday);
        TextView mon = (TextView) mPager1.findViewById(R.id.monday);
        TextView tue = (TextView) mPager1.findViewById(R.id.tuesday);
        TextView wed = (TextView) mPager1.findViewById(R.id.wednesday);
        TextView thu = (TextView) mPager1.findViewById(R.id.thursday);
        TextView fri = (TextView) mPager1.findViewById(R.id.friday);
        TextView sat = (TextView) mPager1.findViewById(R.id.saturday);
        
        SimpleDateFormat format = new SimpleDateFormat("dd/MM");
        Calendar calendar = mCalendar; 
        calendar.setTime(currDate);
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK); 
        calendar.add(Calendar.DATE, -(dayIndex - 1));
        Date date = calendar.getTime();
        String text= format.format(date);
        dayIndex = calendar.get(Calendar.DAY_OF_WEEK); 
        int count = 7;
        while (count > 0) {
            count--;
            switch (dayIndex) {
                case 1:
                    newMessageInfo = "S " + "<font color='#cccccc'><b>" +  format.format(date) + "</b></font>";
                    sun.setText(Html.fromHtml(newMessageInfo));
                    break;
                case 2:
                    newMessageInfo = "M " + "<font color='#cccccc'><b>" +  format.format(date) + "</b></font>";
                    mon.setText(Html.fromHtml(newMessageInfo));
                    break;
                case 3:
                    newMessageInfo = "T " + "<font color='#cccccc'><b>" +  format.format(date) + "</b></font>";
                    tue.setText(Html.fromHtml(newMessageInfo));
                    break;
                case 4:
                    newMessageInfo = "W " + "<font color='#cccccc'><b>" +  format.format(date) + "</b></font>";
                    wed.setText(Html.fromHtml(newMessageInfo));
                    break;
                case 5:
                    newMessageInfo = "T " + "<font color='#cccccc'><b>" +  format.format(date) + "</b></font>";
                    thu.setText(Html.fromHtml(newMessageInfo));
                    break;
                case 6:
                    newMessageInfo = "F " + "<font color='#cccccc'><b>" +  format.format(date) + "</b></font>";
                    fri.setText(Html.fromHtml(newMessageInfo));
                    break;
                case 7:
                    newMessageInfo = "S " + "<font color='#cccccc'><b>" +  format.format(date) + "</b></font>";
                    sat.setText(Html.fromHtml(newMessageInfo));
                    break;
                default:
                    break;
            }
            calendar.add(Calendar.DATE, 1);
            dayIndex = calendar.get(Calendar.DAY_OF_WEEK); 
            date = calendar.getTime();
        }
        calendar.setTime(currDate);
	}
	
	private class AppdetailAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			if (position == 0) {
				view = mPager1;
			} else {
				view = mPager2;
			}
			container.addView(view, position);
			return view;
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {

	}

	@Override
	public void onClick(View v) {
	    switch (v.getId()) {
	        case R.id.activity_title:
	            onBackPressed();
	            break;
            case R.id.uninstall_app:
                RemoveApp(mAppInfo.getPkg());
                break;
            case R.id.stop_app:
                ProcessCleaner cleaner = new ProcessCleaner((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE));
                cleaner.cleanProcess(mAppInfo.getPkg());
                break;
            case R.id.tv_app_use_info:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_app_permission_info:
                mViewPager.setCurrentItem(1);
                break;
                
                
            default:
                break;
        }
		
	}
	private void RemoveApp(String packageName){
        Uri uri = Uri.fromParts("package", packageName, null);
        Intent intentdel = new Intent(Intent.ACTION_DELETE, uri);
        startActivity(intentdel);    
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mAppBaseInfoLayoutbg != null) {
            mAppBaseInfoLayoutbg.recycle();
            mAppBaseInfoLayoutbg = null;
        }
    }

    @Override
    public void onScroll(int direction) {
        if (direction > 0) { //left
            if (mCurrdate.equals( mCalendar.getTime())) {
                return;
            }
            mCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            setWeekString(mCalendar.getTime());
        } else if (direction < 0) { //right
            mCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            setWeekString(mCalendar.getTime());
        }
        
    }
	
	
}
