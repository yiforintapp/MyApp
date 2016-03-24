
package com.leo.appmaster.intruderprotection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.receiver.DeviceReceiverNewOne;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAnimationDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageDownloader;
import com.leo.imageloader.core.PauseOnScrollListener;

public class IntruderprotectionActivity extends BaseActivity {
    private Dialog mMultiUsesDialog;
    private LEOAlarmDialog mOpenForbinDialog;
    private final int REQUEST_CODE_TO_REQUEST_ADMIN = 1;
    private LEOAnimationDialog mMessageDialog;
    private ListView mLvPhotos;
    private CommonToolbar mctb;
    private BaseAdapter mAdapter;
    private ArrayList<IntruderPhotoInfo> mInfos;
    private ArrayList<IntruderPhotoInfo> mInfosSorted;
    private IntrudeSecurityManager mImanager;
    private List<Integer> mCurrentDayFirstPhotoIndex;
//    private LEOAlarmDialog mOpenForbinDialog;
    private LEOAlarmDialog mAskOpenDeviceAdminDialog;
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
//    private int[] mTimes = {
//            1, 2, 3, 5
//    };
//    private final int TIMES_1 = 1;
//    private final int TIMES_2 = 2;
//    private final int TIMES_3 = 3;
//    private final int TIMES_4 = 5;
    private RippleView mRvOpenSystLockProt;
    private RelativeLayout mRlTipContent;
    private LinearLayout mLlGuide;
    private LinearLayout mLlGuideFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_protection);
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
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
        mctb.setSecOptionImageResource(R.drawable.setup_icon);
        mctb.setSecOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntruderprotectionActivity.this, IntruderSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("isPgInner",true);
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
            mRvOpenSystLockProt = (RippleView) mHeader.findViewById(R.id.rv_open);
            mRlTipContent = (RelativeLayout) mHeader.findViewById(R.id.rl_intrudercatch_tip);
            if (mImanager.getSystIntruderProtecionSwitch()) {
                mRlTipContent.setVisibility(View.GONE);
            }
            mLlGuide = (LinearLayout) mHeader.findViewById(R.id.ll_guide_tip);
            mLlGuideFinished = (LinearLayout) mHeader.findViewById(R.id.ll_guide_finished);
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
        if (mMultiUsesDialog != null) {
            mMultiUsesDialog.dismiss();
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * 更新抓拍到入侵者XX次的显示
     */
//    private void updateTimesToCatch() {
//        mTvTimes = (TextView) mHeader.findViewById(R.id.tv_fail_times_to_catch);
//        String failtimesTipsS = getResources().getString(
//                R.string.intruder_to_catch_fail_times_tip);
//        String failtimesTipsD = String.format(failtimesTipsS, mImanager.getTimesForTakePhoto());
//        mTvTimes.setText(Html.fromHtml(failtimesTipsD));
//    }

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
        String uri = ImageDownloader.Scheme.CRYPTO.wrap(filePath);
        final ImageView pic1 = holder1.ivIntruderPic;
        mImageLoader.displayImage(uri, pic1, mImageOptions);
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
        String packageName = mInfosSorted.get(position).getFromAppPackage();
        Drawable applicationIcon;
        try {
            if (IntrudeSecurityManager.ICON_SYSTEM.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.intruder_system_icon);
            } else if (SwitchGroup.WIFI_SWITCH.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.lock_wifi);
            } else if (SwitchGroup.BLUE_TOOTH_SWITCH.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.lock_bluetooth);
            } else {
                applicationIcon = AppUtil.getAppIcon(pm, packageName);
            }
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
        String uri = ImageDownloader.Scheme.CRYPTO.wrap(filePath);
        mImageLoader.displayImage(uri, pic2, mImageOptions);
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
        String packageName = mInfosSorted.get(position).getFromAppPackage();
        Drawable applicationIcon;
        try {
            if (IntrudeSecurityManager.ICON_SYSTEM.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.intruder_system_icon);
            } else if (SwitchGroup.WIFI_SWITCH.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.lock_wifi);
            } else if (SwitchGroup.BLUE_TOOTH_SWITCH.equals(packageName)) {
                applicationIcon = getResources().getDrawable(R.drawable.lock_bluetooth);
            } else {
                applicationIcon = AppUtil.getAppIcon(pm, packageName);
            }
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

                if (mInfosSorted != null && mInfosSorted.size() == 0) {
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

        updateTipStatus();
//        RippleView open = (RippleView) mHeader.findViewById(R.id.rv_open);
//        open.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mRvOpenSystLockProt
//            }
//        });
        // 头布局——防护开关
//        updateSwtch();
//        // 拍照所需的解锁失败次数的文本提示
//        updateTimesToCatch();
        // 更改拍照所需的解锁失败次数
//        if (!mImanager.getIsIntruderSecurityAvailable()) {
//            btChangeTimes.setEnabled(false);
//            return;
//        } else {
//            btChangeTimes.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1,
//                            "intruder", "intruder_modify");
////                    showChangeTimesDialog();
//                }
//            });
//        }
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

    private void updateTipStatus() {
        if (!mImanager.getSystIntruderProtecionSwitch()) {
            mRlTipContent.setVisibility(View.GONE);

            SDKWrapper.addEvent(this, SDKWrapper.P1, "intruder", "intruder_screen_sh");

            mLlGuideFinished.setVisibility(View.INVISIBLE);
            mLlGuide.setVisibility(View.VISIBLE);

            mRvOpenSystLockProt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1, "intruder", "intruder_screen_cli");

                    if (!mImanager.getIsIntruderSecurityAvailable()) {
                        mMultiUsesDialog = ShowAboutIntruderDialogHelper.showForbitDialog(IntruderprotectionActivity.this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(IntruderprotectionActivity.this, FeedbackActivity.class);
                                intent.putExtra("isFromIntruderProtectionForbiden", true);
                                startActivity(intent);
                                mMultiUsesDialog.dismiss();
                            }
                        });
                    } else {
                        if (DeviceReceiverNewOne.isActive(IntruderprotectionActivity.this)) {
                            changeToGuideFinishedLayout();
                        } else {
                            mMultiUsesDialog = ShowAboutIntruderDialogHelper.showAskOpenDeviceAdminDialog(IntruderprotectionActivity.this, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestDeviceAdmin();
                                    mMultiUsesDialog.dismiss();
                                }
                            });
                        }
                    }
                }
            });
        } else {
            if (mRlTipContent.getVisibility() == View.GONE) {
                mLlGuideFinished.setVisibility(View.VISIBLE);
                mLlGuide.setVisibility(View.INVISIBLE);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_TO_REQUEST_ADMIN == requestCode && DeviceReceiverNewOne.isActive(IntruderprotectionActivity.this)) {
            mImanager.setSystIntruderProtectionSwitch(true);
            changeToGuideFinishedLayout();
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
                    AppMasterPreference.getInstance(IntruderprotectionActivity.this)
                            .setAdvanceProtectOpenSuccessDialogTip(false);
                }
            });
        }
        String content = getString(R.string.prot_open_suc_tip_cnt);
        mMessageDialog.setContent(content);
        mMessageDialog.show();
    }

//    private void showAskOpenDeviceAdminDialog() {
//        if (mAskOpenDeviceAdminDialog == null) {
//            mAskOpenDeviceAdminDialog = new LEOAlarmDialog(IntruderprotectionActivity.this);
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
        mLockManager.filterSelfOneMinites();
        mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
        ComponentName mAdminName = new ComponentName(IntruderprotectionActivity.this, DeviceReceiverNewOne.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
        startActivityForResult(intent, REQUEST_CODE_TO_REQUEST_ADMIN);
    }

    private void changeToGuideFinishedLayout() {
        if (mImanager.getIsIntruderSecurityAvailable()) {

            SDKWrapper.addEvent(IntruderprotectionActivity.this, SDKWrapper.P1, "intruder", "intruder_screen_on");

            mImanager.setSystIntruderProtectionSwitch(true);
            mRlTipContent.setVisibility(View.GONE);
            mLlGuide.setVisibility(View.INVISIBLE);
            mLlGuideFinished.setVisibility(View.VISIBLE);
            TranslateAnimation tla = new TranslateAnimation(mRlTipContent.getWidth(),0,0,0);
            mLlGuideFinished.setAnimation(tla);
            tla.setDuration(500);
            mLlGuideFinished.startAnimation(tla);
        }
//        else {
//            showForbitDialog();
//        }
    }

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
//                Intent intent = new Intent(IntruderprotectionActivity.this, FeedbackActivity.class);
//                intent.putExtra("isFromIntruderProtectionForbiden", true);
//                startActivity(intent);
//                mOpenForbinDialog.dismiss();
//            }
//        });
//        mOpenForbinDialog.show();
//    }



    /**
     * 将时间轴转化为XX：XXAM/PM的形式
     *
     * @param timeStamp
     * @return
     */
    private String timeStampToAMPM(String timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT);
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

}
