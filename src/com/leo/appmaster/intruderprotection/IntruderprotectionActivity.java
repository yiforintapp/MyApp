
package com.leo.appmaster.intruderprotection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.RippleView.OnRippleCompleteListener;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.PauseOnScrollListener;

public class IntruderprotectionActivity extends Activity {
    private ListView mLvPhotos;
    // private IntruderPhotoTable mIpt;
    private CommonToolbar mctb;
    private BaseAdapter mAdapter;
    private ArrayList<IntruderPhotoInfo> mInfos;
    private ArrayList<IntruderPhotoInfo> mInfosSorted;
    private IntrudeSecurityManager mImanager;
    private List<Integer> mCurrentDayFirstPhotoIndex;
    private boolean mHasAddHeader = false;
    private View mHeader;
    private TextView mTvTimes;
    private RelativeLayout mNoPic;
    private ImageLoader mImageLoader;
    private int VIEW_TYPE_NEED_TIMESTAMP = 1;
    private int VIEW_TYPE_WITHOUT_TIMESTAMP = 0;
    private PrivacyDataManager mPDManager;
    private DisplayImageOptions mImageOptions;
    private LEOChoiceDialog mDialog;
    private static final int VIEW_TYPE_ACCOUNT = 2;
    private int[] mTimes = {1 , 2 , 3 , 5};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_protection);
        if(getIntent().getBooleanExtra("from_quickhelper", false)){
            SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                    "assistant", "intruder_cnts");
        }
        
        mImanager = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        mPDManager = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        handlerIntent();
        init();
        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                "intruder", "intruder_enter");
        
    }

    private void handlerIntent() {
        try {
            Intent i = getIntent();
            String from = i.getStringExtra("from");
            if (from != null && from != "") {
                SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                        "push_refresh", "push_Intruder_cnts");
            }
        } catch (Exception e) {

        }
        // #Intent;component=com.leo.appmaster/.intruderprotection.IntruderprotectionActivity;S.from=push;end
    }

    /**
     * create时的初始化(不需要变化的UI)
     */
    private void init() {
//        BitmapFactory.Options option = new BitmapFactory.Options();
//        option.inPreferredConfig = Config.RGB_565;
//        option.inSampleSize = 4;
        
        mNoPic = (RelativeLayout) findViewById(R.id.rl_nopic);
        // 标题栏
        mctb = (CommonToolbar) findViewById(R.id.ctb_at_intruder);
        mctb.setOptionImageResource(R.drawable.clean_intruder);
        mctb.setOptionMenuVisible(true);
        mctb.setToolbarTitle(R.string.home_tab_instruder);
        // 主listview
        mLvPhotos = (ListView) findViewById(R.id.lv_main_instruderP);
        // 头布局
        mHeader = LayoutInflater.from(this).inflate(R.layout.header_intruderprotection, null);
        LeoLog.i("poha", "Is headerview added? : " + mHasAddHeader + "   count::  "
                        + mLvPhotos.getHeaderViewsCount());
        if (!mHasAddHeader && mLvPhotos.getHeaderViewsCount() == 0) {
            LeoLog.i("poha", "headerView  add");
            mHasAddHeader = true;
            mLvPhotos.addHeaderView(mHeader);
        }
        mImageLoader = ImageLoader.getInstance();
        mImageOptions = new DisplayImageOptions.Builder()
//        .bitmapConfig(Bitmap.Config.RGB_565)
//        .delayBeforeLoading(100)
//        .cacheInMemory(true)
//        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
//        .showImageOnLoading(R.drawable.photo_bg_loding)
        .showImageOnLoading(R.drawable.online_theme_loading)
        .showImageOnFail(R.drawable.online_theme_loading_failed)
        .displayer(new FadeInBitmapDisplayer(500))
        .cacheInMemory(true).cacheOnDisk(true)
        .considerExifParams(true)
        .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();// 重新查询数据库，做与数据相关的UI更新
        updateAll();// 更新与数据库无关的UI
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mImageLoader != null) {
            mImageLoader.clearMemoryCache();
        }
    }

    private void updateData() {
        LeoLog.i("poha", "initData!!!");
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mInfos = mImanager.getPhotoInfoList();
                if (mInfos != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // LeoLog.i("poha",
                            // "after query header count ="+mLvPhotos.getHeaderViewsCount());
                            onQueryFinished();
                            // 保证头布局的显示 保证数据的清空，
                            ListAdapter adapter = mLvPhotos.getAdapter();
                            if (adapter == null) {
                                // LeoLog.i("poha",
                                // "after query adapter is null  !!! ");
                                mLvPhotos.setAdapter(null);
                                mctb.setOptionClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mImanager.clearAllPhotos();
                                        mInfosSorted.clear();
                                    }
                                }
                                        );
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateTimesToCatch() {
        try {
            LeoLog.i("poha", "update Times To Catch!");
            mTvTimes = (TextView) mHeader.findViewById(R.id.tv_fail_times_to_catch);
            String failtimesTipsS = getResources().getString(
                    R.string.intruder_to_catch_fail_times_tip);
            String failtimesTipsD = String.format(failtimesTipsS, mImanager.getTimesForTakePhoto());
            mTvTimes.setText(Html.fromHtml(failtimesTipsD));
            throw(new Exception());
        } catch (Exception e) {
        }
    }

    static  class ViewWithTimeStampHolder {
        BottomCropImage ivIntruderPic;
        RelativeLayout rlMask;
        TextView tvTimeStamp;
    }

 static  class ViewWithoutTimeStampHolder {
        BottomCropImage ivIntruderPic;
        RelativeLayout rlMask;
    }

    /**
     * 查询数据库后的操作
     */
    private void onQueryFinished() {
        LeoLog.i("poha", mInfos.size() + ":: size of DB");
        sortInfos();//排序，按照时间先后
        getIndexOfFirstPhotoOneDay();//获得各天第一张该显示的照片（即当天最晚一张）的角标
        // ListView的adapter，保证只使用mInfosSorted，不要重新查询
//        mInfosSorted.get(0).setFilePath("dfasdfad");
        mAdapter = new BaseAdapter() {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View viewWithoutTimeStamp = null;
                View viewWithTimeStamp = null;
                int itemViewType = getItemViewType(position);
                if(itemViewType == VIEW_TYPE_NEED_TIMESTAMP){
                    ViewWithTimeStampHolder holder1 = null;
                    if(convertView == null){
                        holder1 = new ViewWithTimeStampHolder();
                        viewWithTimeStamp = LayoutInflater.from(IntruderprotectionActivity.this).inflate(
                                R.layout.item_intruder_photo, null);
                        holder1.tvTimeStamp =  (TextView) viewWithTimeStamp.findViewById(R.id.tv_timestamp);
                        holder1.rlMask =  (RelativeLayout) viewWithTimeStamp.findViewById(R.id.rl_mask);
                        holder1.ivIntruderPic =  (BottomCropImage) viewWithTimeStamp.findViewById(R.id.btuv_photo);
                        viewWithTimeStamp.setTag(holder1);
                        convertView = viewWithTimeStamp;
                    }else{
                        holder1 = (ViewWithTimeStampHolder)convertView.getTag();
                    }
                    //时间戳
                    String timeStamp = mInfosSorted.get(position).getTimeStamp();
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT);
                    int day = 0;
                    int month = 0;
                    int year = 0;
                    try {
                        Calendar ci = Calendar.getInstance();
                        Date date = sdf.parse(timeStamp);
                        ci.setTime(date);
                        day = ci.get(Calendar.DAY_OF_MONTH);
                        LeoLog.i("poha", "position::" + position + "---------" + "timeStamp::"+ mInfosSorted.get(position).getTimeStamp());
                        month = ci.get(Calendar.MONTH) + 1;
                        year = ci.get(Calendar.YEAR);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String s = year + "/" + month + "/" + day;
                    holder1.tvTimeStamp.setText(s);
                    //加载照片
                    String filePath = mInfosSorted.get(position).getFilePath();
                    final ImageView pic1 = holder1.ivIntruderPic;
                    mImageLoader.displayImage("file:///" + filePath, pic1, mImageOptions);
                    pic1.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(IntruderprotectionActivity.this,
                                    IntruderGalleryActivity.class);
                            intent.putExtra("current_position", position);
                            SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                    "intruder", "intruder_view_timeline");
                            startActivity(intent);
                        }
                    });
                    //图标和应用名
                    PackageManager pm = getPackageManager();
                    try {
                        Drawable applicationIcon = AppUtil.getAppIcon(pm, mInfosSorted.get(position).getFromAppPackage());
                        ImageView iv2 = (ImageView) (holder1.rlMask.findViewById(R.id.iv_intruder_appicon));
                        iv2.setImageDrawable(applicationIcon);
                        TextView tv2 = (TextView) (holder1.rlMask.findViewById(R.id.tv_intruder_apptimestamp));
                        String timeStampToAMPM = timeStampToAMPM(mInfosSorted.get(position).getTimeStamp());
                        tv2.setText(timeStampToAMPM);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //不带时间戳的view
                }else if (itemViewType == VIEW_TYPE_WITHOUT_TIMESTAMP) {
                    ViewWithoutTimeStampHolder holder2 = null;
                    if(convertView == null){
                        holder2 = new ViewWithoutTimeStampHolder();
                        viewWithoutTimeStamp = LayoutInflater.from(IntruderprotectionActivity.this).inflate(
                                R.layout.item_intruder_photo_no_timestamp, null);
                        holder2.rlMask =  (RelativeLayout) viewWithoutTimeStamp.findViewById(R.id.rl_mask);
                        holder2.ivIntruderPic =  (BottomCropImage) viewWithoutTimeStamp.findViewById(R.id.btuv_photo);
                        viewWithoutTimeStamp.setTag(holder2);
                        convertView = viewWithoutTimeStamp;
                    }else{
                        holder2 = (ViewWithoutTimeStampHolder)convertView.getTag();
                    }
                    String filePath = mInfosSorted.get(position).getFilePath();
                    final ImageView pic2 = holder2.ivIntruderPic;
                    mImageLoader.displayImage("file:///" + filePath, pic2, mImageOptions);
                    pic2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(IntruderprotectionActivity.this,
                                    IntruderGalleryActivity.class);
                            intent.putExtra("current_position", position);
                            SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                    "intruder", "intruder_view_timeline");
                            startActivity(intent);
                        }
                    });
                    //图标和应用名
                    PackageManager pm = getPackageManager();
                    try {
                        Drawable applicationIcon = AppUtil.getAppIcon(pm, mInfosSorted.get(position).getFromAppPackage());
                        ImageView iv2 = (ImageView) (holder2.rlMask.findViewById(R.id.iv_intruder_appicon));
                        iv2.setImageDrawable(applicationIcon);
                        TextView tv2 = (TextView) (holder2.rlMask.findViewById(R.id.tv_intruder_apptimestamp));
                        String timeStampToAMPM = timeStampToAMPM(mInfosSorted.get(position).getTimeStamp());
                        tv2.setText(timeStampToAMPM);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                LeoLog.i("poha", "position = "+position+"size = "+mInfosSorted.size());
                if(position == mInfosSorted.size() - 1){
                    convertView.findViewById(R.id.v_the_last_one_line).setVisibility(View.VISIBLE);
                    LeoLog.i("poha", "visable");
                }
                return convertView;
            }
            @Override
            public int getViewTypeCount() {
                return VIEW_TYPE_ACCOUNT;
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
                return mInfosSorted.size();
            }
            @Override
            public int getItemViewType(int position) {
                if ((mCurrentDayFirstPhotoIndex != null) && (mCurrentDayFirstPhotoIndex.contains(position))) {
                    return VIEW_TYPE_NEED_TIMESTAMP ;
                }else{
                    return VIEW_TYPE_WITHOUT_TIMESTAMP;
                }
            }
        };
            mLvPhotos.setAdapter(mAdapter);
            mLvPhotos.setOnScrollListener(new PauseOnScrollListener(mImageLoader, true, false));
            //显示木有图片时的UI
            showUiWhenNoPic();
            //清除所有照片
            initCleanButton();
    }
    
    private void initCleanButton() {
        mctb.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                        "intruder", "intruder_clear");
                LEOAlarmDialog dialog = new LEOAlarmDialog(IntruderprotectionActivity.this);
                String askforsure = getString(R.string.sure_clean);
                dialog.setContent(askforsure);
                dialog.setRightBtnStr(IntruderprotectionActivity.this.getString(R.string.makesure));
                dialog.setLeftBtnStr(IntruderprotectionActivity.this.getString(R.string.cancel));
                dialog.setRightBtnListener(new DialogInterface.OnClickListener()  {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mImanager.clearAllPhotos();
                        final ArrayList<IntruderPhotoInfo> temp = new ArrayList<IntruderPhotoInfo>();
                        temp.addAll(mInfosSorted);
                        ThreadManager.executeOnFileThread(new Runnable() {
                            @Override
                            public void run() {
                                LeoLog.i("poha", "in run!!!");
                                LeoLog.i("poha", "temp size before delete all :"+temp.size());
                                for(int i = 0 ; i<temp.size() ; i++){
                                    String filePath = temp.get(i).getFilePath();
                                    LeoLog.i("poha", "i = "+i+"    path = "+filePath);
                                    FileOperationUtil.deleteFile(filePath);
                                    FileOperationUtil.deleteFileMediaEntry(filePath, IntruderprotectionActivity.this);
                                    LeoLog.i("poha", "delete pic");
                                }
                            }
                        });
                        mInfosSorted.clear();
                        mAdapter.notifyDataSetChanged();
                        showUiWhenNoPic();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    private void showUiWhenNoPic() {
        mHeader.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(mInfosSorted.size()==0||mInfosSorted == null){
                    int height = mLvPhotos.getHeight();
                    int height2 = mHeader.getHeight();
                    RelativeLayout.LayoutParams rr = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height-height2);
                    mNoPic.setLayoutParams(rr);
                    rr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    LeoLog.i("poha", mNoPic.getHeight()+": view height ...........1 and 2 ="+ height+" and "+height2);
                    mNoPic.setVisibility(View.VISIBLE);
                    TextView today = (TextView) mNoPic.findViewById(R.id.  tv_timestamp_nopic);
                    Date date=new Date();
                    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
                    String time=format.format(date); 
                    today.setText(time);
                }else{
                    mNoPic.setVisibility(View.INVISIBLE);
                }
                mHeader.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void getIndexOfFirstPhotoOneDay() {
        mCurrentDayFirstPhotoIndex = new ArrayList<Integer>();
        if (mInfosSorted == null)
            return;
        mCurrentDayFirstPhotoIndex.add(0);
        Date date1;
        Date date2;
        for (int i = 1; i < mInfosSorted.size(); i++) {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT);
            String timeStamp1 = mInfosSorted.get(i).getTimeStamp();
            String timeStamp2 = mInfosSorted.get(i - 1).getTimeStamp();
            try {
                date1 = sdf.parse(timeStamp1);
                date2 = sdf.parse(timeStamp2);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
            if (date1 != null && date2 != null && date1.getDay() != date2.getDay()) {
                mCurrentDayFirstPhotoIndex.add(i);
            }
        }
    }
    /**
     * 从源info列表按业务需求排序。
     */
    private void sortInfos() {
        mInfosSorted = mImanager.sortInfosByTimeStamp(mInfos);
        for (int i = 0; i < mInfosSorted.size(); i++) {
            LeoLog.i("poha", "the sorted infos:" + i + ":  timeStamp"
                    + mInfosSorted.get(i).getTimeStamp());
        }
    }
    /**
     * 每次进入界面需要重新刷新的操作(与数据库无关)
     */
    private void updateAll() {
        // 头布局——防护开关
        updateSwtch();
        // 拍照所需的解锁失败次数的文本提示
        updateTimesToCatch();
        // 更改拍照所需的解锁失败次数
        RippleView btChangeTimes = (RippleView) mHeader.findViewById(R.id.rv_change_times);
        btChangeTimes.setOnRippleCompleteListener(new OnRippleCompleteListener() {
            @Override
            public void onRippleComplete(RippleView rippleView) {
                SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                        "intruder", "intruder_modify");
                showChangeTimesDialog();
            }
        });
    }
    
    private void showChangeTimesDialog() {
        if(mDialog == null){
            mDialog = new LEOChoiceDialog(IntruderprotectionActivity.this);
        }
        mDialog.setTitle(getResources().getString(R.string.ask_for_times_for_catch));
        String times = getResources().getString(R.string.times_choose);
        List<String> timesArray = new ArrayList<String>();
        for(int i = 0 ; i < mTimes.length; i++){
            timesArray.add(String.format(times, mTimes[i]));
        }
        
        int currentTimes = mImanager.getTimesForTakePhoto();
        int currentIndex = -1;
        switch (currentTimes) {
            case 1:
                currentIndex = 0;
                break;
            case 2:
                currentIndex = 1;
                break;
            case 3:
                currentIndex = 2;
                break;
            case 5:
                currentIndex = 3;
                break;
            default:
                break;
        }
        mDialog.setItemsWithDefaultStyle(timesArray, currentIndex);
        mDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mImanager.setTimesForTakePhoto(1);
                        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_"+1);
                        updateTimesToCatch();
                        break;
                    case 1:
                        mImanager.setTimesForTakePhoto(2);
                        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_"+2);
                        updateTimesToCatch();
                        break;
                    case 2:
                        mImanager.setTimesForTakePhoto(3);
                        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_"+3);
                        updateTimesToCatch();
                        break;
                    case 3:
                        mImanager.setTimesForTakePhoto(5);
                        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_"+5);
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
    
    private String timeStampToAMPM(String timeStamp){
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
            ampm= ci.get(Calendar.AM_PM);
            minute = ci.get(Calendar.MINUTE);
        } catch (ParseException e1) {
            return timeStamp;
        }
        String strHour = "";
        String strMinute = "";
        if(hour<10){
            strHour = "0"+hour;
        }else{
            strHour = hour+"";
        }
        if(minute<10){
            strMinute = "0"+minute;
        }else{
            strMinute = minute+"";
        }
        String fts;
        if(ampm==Calendar.AM){
            fts = strHour+":"+strMinute+"AM";
        }else if(ampm==Calendar.PM){
            fts = strHour+":"+strMinute+"PM";
        }else{
            fts = strHour+":"+strMinute;
        }
        return fts;
    }

    private void updateSwtch() {
        LeoLog.i("poha_catch", "updateSwitch!!");
        final ImageView needle = (ImageView) mHeader.findViewById(R.id.iv_switch_needle);
        final ImageView red = (ImageView) mHeader.findViewById(R.id.iv_redlight);
        final ImageView green = (ImageView) mHeader.findViewById(R.id.iv_greenlight);
        final ImageView button = (ImageView) mHeader.findViewById(R.id.iv_switch_button);
        if (mImanager.getIntruderMode()) {
            needle.setImageResource(R.drawable.intruder_catch_switch_needle_right);
            green.setImageResource(R.drawable.intruder_catch_switch_green);
            red.setImageResource(R.drawable.intruder_catch_switch_grey);
        } else {
            needle.setImageResource(R.drawable.intruder_catch_switch_needle);
            green.setImageResource(R.drawable.intruder_catch_switch_grey);
            red.setImageResource(R.drawable.intruder_catch_switch_red);
        }
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BuildProperties.isApiLevel14()){
                    Toast.makeText(IntruderprotectionActivity.this, getResources().getString(R.string.unavailable), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mImanager.getIntruderMode()) {
                    mImanager.switchIntruderMode(false);
                    Toast.makeText(IntruderprotectionActivity.this, getString(R.string.intruder_close), Toast.LENGTH_SHORT).show();
                    final RotateAnimation animation = new RotateAnimation(0f, -180f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setDuration(300);
                    needle.setAnimation(animation);
                    animation.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            button.setClickable(false);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            needle.clearAnimation();
                            needle.setImageResource(R.drawable.intruder_catch_switch_needle);
                                green.setImageResource(R.drawable.intruder_catch_switch_grey);
                                red.setImageResource(R.drawable.intruder_catch_switch_red);
                                button.setClickable(true);
                        }
                    });
                    needle.startAnimation(animation);       
                } else {
                    mImanager.switchIntruderMode(true);
                    Toast.makeText(IntruderprotectionActivity.this,
                            getString(R.string.intruder_open), Toast.LENGTH_SHORT).show();
                    final RotateAnimation animation = new RotateAnimation(0f, 180f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setDuration(300);
//                    animation.setFillAfter(true);
                    needle.setAnimation(animation);
                    animation.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            button.setClickable(false);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            needle.clearAnimation();
                            needle.setImageResource(R.drawable.intruder_catch_switch_needle_right);
                            green.setImageResource(R.drawable.intruder_catch_switch_green);
                            red.setImageResource(R.drawable.intruder_catch_switch_grey);
                            button.setClickable(true);
                        }
                    });
                    needle.startAnimation(animation);
                }
            }
        });
    }
}
