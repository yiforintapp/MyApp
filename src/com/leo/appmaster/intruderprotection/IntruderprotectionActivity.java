
package com.leo.appmaster.intruderprotection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.battery.BatteryMainActivity;
import com.leo.appmaster.callfilter.CallFIlterUIHelper;
import com.leo.appmaster.callfilter.CallFilterToast;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PropertyInfoUtil;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.PauseOnScrollListener;

public class IntruderprotectionActivity extends BaseActivity {
    private ListView mLvPhotos;
    private CommonToolbar mctb;
    private BaseAdapter mAdapter;
    private ArrayList<IntruderPhotoInfo> mInfos;
    private ArrayList<IntruderPhotoInfo> mInfosSorted;
    private IntrudeSecurityManager mImanager;
    private List<Integer> mCurrentDayFirstPhotoIndex;
    private LEOAlarmDialog mOpenForbinDialog;
    private LEOAlarmDialog mCleanConfirmDialog;
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
    private boolean mIsFromScan = false;
    private int[] mTimes = {
            1, 2, 3, 5
    };
    private final int TIMES_1 = 1;
    private final int TIMES_2 = 2;
    private final int TIMES_3 = 3;
    private final int TIMES_4 = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_protection);
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                    "assistant", "intruder_cnts");
        }
//        Intent i = new Intent(this,LockScreenActivity.class);
//        startActivity(i);

        mImanager = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        mPDManager = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        handlerIntent();
        init();
        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                "intruder", "intruder_enter");
//        long totalMemory2 = PropertyInfoUtil.getTotalMemory2(this);
//        long availMemory = PropertyInfoUtil.getAvailMemory(this);
//        Toast.makeText(this, "availMemory = "+availMemory+"       totalMemory = "+totalMemory2, 1).show()
//        CallFIlterUIHelper.getInstance().showReceiveCallNotification("13510261550");
//        Intent i = new Intent(this, BatteryMainActivity.class);
//        startActivity(i);
    }

    /**
     * 根据intent附带的信息做的处理
     */
    private void handlerIntent() {
        // 如果intent包括from，表示从push调起这个activity，打点
        try {
            Intent i = getIntent();
            String from = i.getStringExtra("from");
            if (from != null && from != "") {
                SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                        "push_refresh", "push_Intruder_cnts");
            }
            boolean isFromScan = i.getBooleanExtra(Constants.EXTRA_IS_FROM_SCAN, false);
            if (isFromScan) {
                mIsFromScan = true;
            }
        } catch (Exception e) {

        }
        // #Intent;component=com.leo.appmaster/.intruderprotection.IntruderprotectionActivity;S.from=push;end
    }

    /**
     * create时的初始化(不需要变化的UI，引用)
     */
    private void init() {
        mNoPic = (RelativeLayout) findViewById(R.id.rl_nopic);
        // 标题栏
        mctb = (CommonToolbar) findViewById(R.id.ctb_at_intruder);
        mctb.setOptionImageResource(R.drawable.clean_intruder);
        mctb.setSecOptionMenuVisible(true);
        mctb.setSecOptionImageResource(R.drawable.ic_launcher);
        mctb.setSecOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntruderprotectionActivity.this,IntruderSettingActivity.class);
                startActivity(intent);
            }
        });
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
        // 初始化imageloader
        mImageLoader = ImageLoader.getInstance();
        mImageOptions = new DisplayImageOptions.Builder()
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
        if (mImageLoader != null) {
            try {
                mImageLoader.clearMemoryCache();
            } catch (Exception e) {
            }
        }
    }

    private void protectHeaderViewIfError() {
        ListAdapter adapter = mLvPhotos.getAdapter();
        if (adapter == null) {
            mLvPhotos.setAdapter(null);
            mctb.setOptionClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImanager.clearAllPhotos();
                    mInfosSorted.clear();
                }
            });
        }
    }

    /**
     * 更新数据，从数据库查询，
     */
    private void updateData() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mInfos = mImanager.getPhotoInfoList();
                if (mInfos != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 做与数据库查询结果有关的操作
                            onQueryFinished();
                            // 保证头布局的显示 保证数据的清空，
                            protectHeaderViewIfError();
                        }
                    });
                }
            }
        });
    }

    /**
     * 更新抓拍到入侵者XX次的显示
     */
    private void updateTimesToCatch() {
        mTvTimes = (TextView) mHeader.findViewById(R.id.tv_fail_times_to_catch);
        String failtimesTipsS = getResources().getString(
                R.string.intruder_to_catch_fail_times_tip);
        String failtimesTipsD = String.format(failtimesTipsS, mImanager.getTimesForTakePhoto());
        mTvTimes.setText(Html.fromHtml(failtimesTipsD));
    }

    /**
     * @author chenfs 带时间戳的listView子项布局的holder类
     */
    static class ViewWithTimeStampHolder {
        BottomCropImage ivIntruderPic;
        RelativeLayout rlMask;
        TextView tvTimeStamp;
    }

    /**
     * @author chenfs 不带时间戳的listView子项布局的holder类
     */
    static class ViewWithoutTimeStampHolder {
        BottomCropImage ivIntruderPic;
        RelativeLayout rlMask;
    }

    private void initListViewItemWithTimeStamp(final int position, ViewWithTimeStampHolder holder1) {
        // 时间戳
        String timeStamp = mInfosSorted.get(position).getTimeStamp();
        String ymdFomatString = toYMDFomatString(timeStamp);
        holder1.tvTimeStamp.setText(ymdFomatString);
        // 加载照片
        String filePath = mInfosSorted.get(position).getFilePath();
        final ImageView pic1 = holder1.ivIntruderPic;
        mImageLoader.displayImage("file:///" + filePath, pic1, mImageOptions);
        pic1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 照片在点击后将进入大图浏览
                Intent intent = new Intent(IntruderprotectionActivity.this,
                        IntruderGalleryActivity.class);
                intent.putExtra("current_position", position);
                SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                        "intruder", "intruder_view_timeline");
                startActivity(intent);
            }
        });
        // 图片加入显示图标和应用名的UI
        PackageManager pm = getPackageManager();
        try {
            Drawable applicationIcon = AppUtil.getAppIcon(pm,
                    mInfosSorted.get(position).getFromAppPackage());
            ImageView iv2 = (ImageView) (holder1.rlMask
                    .findViewById(R.id.iv_intruder_appicon));
            iv2.setImageDrawable(applicationIcon);
            TextView tv2 = (TextView) (holder1.rlMask
                    .findViewById(R.id.tv_intruder_apptimestamp));
            String timeStampToAMPM = timeStampToAMPM(mInfosSorted.get(position)
                    .getTimeStamp());
            tv2.setText(timeStampToAMPM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListViewItem(final int position, ViewWithoutTimeStampHolder holder2) {
        // 显示照片
        String filePath = mInfosSorted.get(position).getFilePath();
        final ImageView pic2 = holder2.ivIntruderPic;
        mImageLoader.displayImage("file:///" + filePath, pic2, mImageOptions);
        pic2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 照片在点击后将进入大图浏览
                Intent intent = new Intent(IntruderprotectionActivity.this,
                        IntruderGalleryActivity.class);
                intent.putExtra("current_position", position);
                SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                        "intruder", "intruder_view_timeline");
                startActivity(intent);
            }
        });
        // 图标和应用名
        PackageManager pm = getPackageManager();
        try {
            Drawable applicationIcon = AppUtil.getAppIcon(pm,
                    mInfosSorted.get(position).getFromAppPackage());
            ImageView iv2 = (ImageView) (holder2.rlMask
                    .findViewById(R.id.iv_intruder_appicon));
            iv2.setImageDrawable(applicationIcon);
            TextView tv2 = (TextView) (holder2.rlMask
                    .findViewById(R.id.tv_intruder_apptimestamp));
            String timeStampToAMPM = timeStampToAMPM(mInfosSorted.get(position)
                    .getTimeStamp());
            tv2.setText(timeStampToAMPM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询数据库后的操作
     */
    private void onQueryFinished() {
        LeoLog.i("poha", mInfos.size() + ":: size of DB");
        sortInfos();// 排序，按照时间先后
        getIndexOfFirstPhotoOneDay();// 获得各天第一张该显示的照片（即当天最晚一张）的角标
        // ListView的adapter，保证只使用mInfosSorted，不要重新查询
        if (mAdapter == null) {
            mAdapter = new BaseAdapter() {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View viewWithoutTimeStamp = null;
                    View viewWithTimeStamp = null;
                    int itemViewType = getItemViewType(position);
                    // 带时间戳的item布局
                    if (itemViewType == VIEW_TYPE_NEED_TIMESTAMP) {
                        ViewWithTimeStampHolder holder1 = null;
                        if (convertView == null) {
                            holder1 = new ViewWithTimeStampHolder();
                            viewWithTimeStamp = LayoutInflater
                                    .from(IntruderprotectionActivity.this).inflate(
                                            R.layout.item_intruder_photo, null);
                            holder1.tvTimeStamp = (TextView) viewWithTimeStamp
                                    .findViewById(R.id.tv_timestamp);
                            holder1.rlMask = (RelativeLayout) viewWithTimeStamp
                                    .findViewById(R.id.rl_mask);
                            holder1.ivIntruderPic = (BottomCropImage) viewWithTimeStamp
                                    .findViewById(R.id.btuv_photo);
                            viewWithTimeStamp.setTag(holder1);
                            convertView = viewWithTimeStamp;
                        } else {
                            holder1 = (ViewWithTimeStampHolder) convertView.getTag();
                        }
                        initListViewItemWithTimeStamp(position, holder1);
                        // ------------------------不带时间戳的item布局------------------------------
                    } else if (itemViewType == VIEW_TYPE_WITHOUT_TIMESTAMP) {
                        ViewWithoutTimeStampHolder holder2 = null;
                        // 获取view
                        if (convertView == null) {
                            holder2 = new ViewWithoutTimeStampHolder();
                            viewWithoutTimeStamp = LayoutInflater.from(
                                    IntruderprotectionActivity.this).inflate(
                                    R.layout.item_intruder_photo_no_timestamp, null);
                            holder2.rlMask = (RelativeLayout) viewWithoutTimeStamp
                                    .findViewById(R.id.rl_mask);
                            holder2.ivIntruderPic = (BottomCropImage) viewWithoutTimeStamp
                                    .findViewById(R.id.btuv_photo);
                            viewWithoutTimeStamp.setTag(holder2);
                            convertView = viewWithoutTimeStamp;
                        } else {
                            holder2 = (ViewWithoutTimeStampHolder) convertView.getTag();
                        }
                        initListViewItem(position, holder2);
                    }
                    LeoLog.i("poha", "position = " + position + "size = " + mInfosSorted.size());
                    if (position == mInfosSorted.size() - 1) {
                        convertView.findViewById(R.id.v_the_last_one_line).setVisibility(
                                View.VISIBLE);
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
                    if ((mCurrentDayFirstPhotoIndex != null)
                            && (mCurrentDayFirstPhotoIndex.contains(position))) {
                        return VIEW_TYPE_NEED_TIMESTAMP;
                    } else {
                        return VIEW_TYPE_WITHOUT_TIMESTAMP;
                    }
                }
            };
            mLvPhotos.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        mLvPhotos.setOnScrollListener(new PauseOnScrollListener(mImageLoader, true, false));
        // 显示木有图片时的UI
        showUiWhenNoPic();
        // 清除所有照片的按钮的初始化
        initCleanButton();
    }

    /**
     * 将数据库记录的timestamp字段内容转为“年/月/日”这样的形式的字符串
     * 
     * @param timestamp
     * @return
     */
    private String toYMDFomatString(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT);
        int day = 0;
        int month = 0;
        int year = 0;
        try {
            Calendar ci = Calendar.getInstance();
            Date date = sdf.parse(timestamp);
            ci.setTime(date);
            day = ci.get(Calendar.DAY_OF_MONTH);
            month = ci.get(Calendar.MONTH) + 1;
            year = ci.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String s = new StringBuilder(year + "").append("/").append(month + "").append("/")
                .append(day + "").toString();
        // String s = year + "/" + month + "/" + day;
        return s;
    }

    /**
     * 为清理按钮添加功能
     */
    private void initCleanButton() {
        if (!mImanager.getIsIntruderSecurityAvailable()) {
            mctb.setOptionMenuVisible(false);
            return;
        }
        mctb.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
                SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                        "intruder", "intruder_clear");
                
                if(mInfosSorted !=null && mInfosSorted.size() == 0) {
                    Toast.makeText(IntruderprotectionActivity.this, R.string.intruder_nobody_tips, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (mCleanConfirmDialog == null) {
                    mCleanConfirmDialog = new LEOAlarmDialog(IntruderprotectionActivity.this);
                }
                String askforsure = getString(R.string.sure_clean);
                mCleanConfirmDialog.setContent(askforsure);
                mCleanConfirmDialog.setRightBtnStr(IntruderprotectionActivity.this
                        .getString(R.string.makesure));
                mCleanConfirmDialog.setLeftBtnStr(IntruderprotectionActivity.this
                        .getString(R.string.cancel));
                mCleanConfirmDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击后首先清除入侵者防护的数据库记录
                        mImanager.clearAllPhotos();
                        // 然后清除实际的图片文件，并且更新媒体数据库
                        final ArrayList<IntruderPhotoInfo> temp = new ArrayList<IntruderPhotoInfo>();
                        temp.addAll(mInfosSorted);
                        ThreadManager.executeOnFileThread(new Runnable() {
                            @Override
                            public void run() {
                                LeoLog.i("poha", "in run!!!");
                                LeoLog.i("poha", "temp size before delete all :" + temp.size());
                                for (int i = 0; i < temp.size(); i++) {
                                    String filePath = temp.get(i).getFilePath();
                                    LeoLog.i("poha", "i = " + i + "    path = " + filePath);
                                    FileOperationUtil.deleteFile(filePath);
                                    FileOperationUtil.deleteFileMediaEntry(filePath,
                                            IntruderprotectionActivity.this);
                                    LeoLog.i("poha", "delete pic");
                                }
                            }
                        });
                        // 清空当前activity所使用的查询后的数据库记录结果
                        mInfosSorted.clear();
                        // 通知更新
                        mAdapter.notifyDataSetChanged();
                        // 图片已经清空，展示没有图片时的默认UI
                        showUiWhenNoPic();
                        dialog.dismiss();
                    }
                });
                mCleanConfirmDialog.show();
            }
        });
    }

    /**
     * 没有照片时UI展示
     */
    private void showUiWhenNoPic() {
        // 为头布局增加OnGlobalLayoutListener，使得能够确保获得view的寬高信息，用于UI布局
        mHeader.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mInfosSorted == null || mInfosSorted.size() == 0) {
                    int height = mLvPhotos.getHeight();
                    int height2 = mHeader.getHeight();
                    // TODO 注意以后布局更改后，mNoPic
                    // 的父容器如果不是relativelayout，需要重新修改LayoutParams为正确的LayoutParams
                    RelativeLayout.LayoutParams rr = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, height - height2);
                    mNoPic.setLayoutParams(rr);
                    rr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    LeoLog.i("poha", mNoPic.getHeight() + ": view height ...........1 and 2 ="
                            + height + " and " + height2);
                    mNoPic.setVisibility(View.VISIBLE);
                    TextView today = (TextView) mNoPic.findViewById(R.id.tv_timestamp_nopic);
                    Date date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    String time = format.format(date);
                    today.setText(time);
                } else {
                    mNoPic.setVisibility(View.INVISIBLE);
                }
                // 获取信息后注销监听
                mHeader.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    // 根据timestamp获得每一天第一次出现先的记录角标，用于做出时间轴效果
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
        if (!mImanager.getIsIntruderSecurityAvailable()) {
            btChangeTimes.setEnabled(false);
            return;
        } else {
            btChangeTimes.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                            "intruder", "intruder_modify");
                    showChangeTimesDialog();
                }
            });
        }
        // btChangeTimes.setOnRippleCompleteListener(new
        // OnRippleCompleteListener() {
        // @Override
        // public void onRippleComplete(RippleView rippleView) {
        // SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
        // "intruder", "intruder_modify");
        // showChangeTimesDialog();
        // }
        // });
    }

    // 显示改变失败XX次拍照的对话框
    private void showChangeTimesDialog() {
        if (mDialog == null) {
            mDialog = new LEOChoiceDialog(IntruderprotectionActivity.this);
        }
        mDialog.setTitle(getResources().getString(R.string.ask_for_times_for_catch));
        String times = getResources().getString(R.string.times_choose);
        List<String> timesArray = new ArrayList<String>();
        for (int i = 0; i < mTimes.length; i++) {
            timesArray.add(String.format(times, mTimes[i]));
        }

        int currentTimes = mImanager.getTimesForTakePhoto();
        int currentIndex = -1;
        switch (currentTimes) {
            case TIMES_1:
                currentIndex = 0;
                break;
            case TIMES_2:
                currentIndex = 1;
                break;
            case TIMES_3:
                currentIndex = 2;
                break;
            case TIMES_4:
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
                        mImanager.setTimesForTakePhoto(TIMES_1);
                        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_" + TIMES_1);
                        updateTimesToCatch();
                        break;
                    case 1:
                        mImanager.setTimesForTakePhoto(TIMES_2);
                        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_" + TIMES_2);
                        updateTimesToCatch();
                        break;
                    case 2:
                        mImanager.setTimesForTakePhoto(TIMES_3);
                        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_" + TIMES_3);
                        updateTimesToCatch();
                        break;
                    case 3:
                        mImanager.setTimesForTakePhoto(TIMES_4);
                        SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_" + TIMES_4);
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

    /**
     * 将时间轴转化为XX：XXAM/PM的形式
     * 
     * @param timeStamp
     * @return
     */
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

    // 更新入侵者防护的开关
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
                if (!mImanager.getIsIntruderSecurityAvailable()) {
                    showForbitDialog();
                    // Toast.makeText(IntruderprotectionActivity.this,
                    // getResources().getString(R.string.unavailable),
                    // Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mImanager.getIntruderMode()) {
                    mImanager.switchIntruderMode(false);
                    Toast.makeText(IntruderprotectionActivity.this,
                            getString(R.string.intruder_close), Toast.LENGTH_SHORT).show();
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
                    if (mIsFromScan) {
//                    if (true) {
                        ShowToast.showGetScoreToast(IntrudeSecurityManager.VALUE_SCORE,
                                IntruderprotectionActivity.this);
                        mIsFromScan = false;
                    } else {
                        Toast.makeText(IntruderprotectionActivity.this, getString(R.string.intruder_open), Toast.LENGTH_SHORT).show();
                    }
                    final RotateAnimation animation = new RotateAnimation(0f, 180f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setDuration(300);
                    // animation.setFillAfter(true);
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

    protected void showForbitDialog() {
        if (mOpenForbinDialog == null) {
            mOpenForbinDialog = new LEOAlarmDialog(this);
        }
        mOpenForbinDialog.setContent(getResources().getString(
                R.string.intruderprotection_forbit_content));
        mOpenForbinDialog.setRightBtnStr(getResources().getString(
                R.string.secur_help_feedback_tip_button));
        mOpenForbinDialog.setLeftBtnStr(getResources().getString(
                R.string.no_image_hide_dialog_button));
        mOpenForbinDialog.setRightBtnListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(IntruderprotectionActivity.this, FeedbackActivity.class);
                intent.putExtra("isFromIntruderProtectionForbiden", true);
                startActivity(intent);
                mOpenForbinDialog.dismiss();
            }
        });
        mOpenForbinDialog.show();
    }
}
