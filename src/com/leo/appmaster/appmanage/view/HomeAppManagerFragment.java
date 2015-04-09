
package com.leo.appmaster.appmanage.view;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.EleActivity;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.appmanage.HotAppActivity;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.backup.AppBackupRestoreManager.AppBackupDataListener;
import com.leo.appmaster.backup.AppDeleteAdapter;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.fragment.Selectable;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.RoundProgressBar;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.TextFormater;

public class HomeAppManagerFragment extends BaseFragment implements OnClickListener,
        AppBackupDataListener, AppChangeListener, Selectable {
    public static final String MESSAGE_BACKUP_SUCCESS = "message_backup_success";
    public static final String MESSAGE_DELETE_APP = "message_delete_app";
    public static final String MESSAGE_ADD_APP = "message_add_app";
    public static final String DAY_TRAFFIC_SETTING = "day_traffic_setting";
    public static final int DONGHUA_CHANGE_TEXT = 0;
    public static final int DONGHUA_SHOW_BEGIN = 1;
    public static boolean isClean = false;
    public static boolean isShowIng = false;
    private boolean curFastThanset = false;
    
    // private boolean isReNewFragment = false;

    private AppMasterPreference sp_homeAppManager;
    public long firstShowTime;
    public boolean isCleanning = false;
    private Handler mHandler = new Handler();
    private View mHeadView;
    private RoundProgressBar roundProgressBar;
    private ProgressBar pb_loading;
    private View bg_show_xz_bf, bg_show_ll, bg_show_dl, bg_show_hotapp, two_text_content,
            content_donghua_ok;
    private ListView list_delete;
    private AppBackupRestoreManager mDeleteManager;
    private AppDeleteAdapter mDeleteAdapter;
    private ImageView iv_donghua, app_hot_tip_icon;
    private TextView tv_installed_app, tv_ap_data, tv_backup_num,
            tv_app_manager_installed, tv_app_manager_space, tv_from_big_donghua;
    // private int InstalledApps = 0;
    public static final String BACKUP_PATH = "appmaster/backup/";
    private ProcessCleaner mCleaner;
    private boolean mAllowClean;
    private long mLastUsedMem;
    private long mTotalMem;
    private long mCleanMem;
    private HomeAppAsyncTask homeAppManagerTask;
    private Resources resources;
    private SpannableStringBuilder installedAppsSpan;
    private SpannableStringBuilder allAppsSizeSpan;
    private SpannableStringBuilder backUpSpan;
    private ArrayList<AppItemInfo> forTextList;
    private ArrayList<AppItemInfo> DeleteDataList;
    private String deleteDataAllSize;
    private int mProgress = 0;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DONGHUA_CHANGE_TEXT:
                    int Progress = (Integer) msg.obj;
                    tv_from_big_donghua.setText(Progress + "%");
                    break;
                case DONGHUA_SHOW_BEGIN:
                    int progress = (Integer) msg.obj;
                    tv_from_big_donghua.setText(progress + "%");
                    break;
            }
        };
    };

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_home_appmanager;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sp_homeAppManager.getHomeFragmentRedTip()) {
            app_hot_tip_icon.setVisibility(View.VISIBLE);
        } else {
            app_hot_tip_icon.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isClean = false;
    }

    @Override
    protected void onInitUI() {
        LeoEventBus.getDefaultBus().register(this);
        sp_homeAppManager = AppMasterPreference.getInstance(mActivity);
        DeleteDataList = new ArrayList<AppItemInfo>();
        homeAppManagerTask = new HomeAppAsyncTask();
        homeAppManagerTask.execute("");
    }

    class HomeAppAsyncTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            InitFourButton();
            InitHeadView();
        }

        @Override
        protected Object doInBackground(Object... params) {
            loadData();
            // mDeleteManager.prepareDate_delete();
            onDataReady();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            
            pb_loading.setVisibility(View.GONE);
            list_delete.setVisibility(View.VISIBLE);
            fillData();
            setListView();
            
            if(curFastThanset){
//              LeoLog.d("testfuckdelete", "onPostExecute ，show动画咯");
                isShowIng = true;
                curFastThanset = false;
                showdonghua();
            }
            
        }
    }

    public void onEventMainThread(BackupEvent event) {
        if (MESSAGE_BACKUP_SUCCESS.equals(event.eventMsg)) {
            int RestoreListSize = getRestoreList();
            backUpSpan = setTextColor(
                    resources.getString(R.string.first_backups_app), ""
                            + RestoreListSize, resources.getString(R.string.first_backups_app)
                            + RestoreListSize);
            tv_backup_num.setText(backUpSpan);
        } else if (MESSAGE_DELETE_APP.equals(event.eventMsg)) {
            loadData();
            fillData();
        } else if (MESSAGE_ADD_APP.equals(event.eventMsg)) {
            loadData();
            fillData();
        }
    }

    public void showdonghua() {
        new Thread() {
            public void run() {
                int startProgress = 0;
                while (mProgress > startProgress) {
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startProgress += 1;
                    roundProgressBar.setProgress(startProgress);
                    Message msg = Message.obtain();
                    msg.what = DONGHUA_CHANGE_TEXT;
                    msg.obj = startProgress;
                    handler.sendMessage(msg);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isShowIng = false;
            };
        }.start();
    }

    private void InitHeadView() {
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
        app_hot_tip_icon = (ImageView) findViewById(R.id.app_hot_tip_icon);
        if (sp_homeAppManager.getHomeFragmentRedTip()) {
            app_hot_tip_icon.setVisibility(View.VISIBLE);
        } else {
            app_hot_tip_icon.setVisibility(View.GONE);
        }
        list_delete = (ListView) findViewById(R.id.list_backup_home_fragment);
        LayoutInflater mFactory = LayoutInflater.from(mActivity);
        mHeadView = mFactory.inflate(R.layout.app_manager_first_page_logo, null);
        iv_donghua = (ImageView) mHeadView.findViewById(R.id.iv_donghua);
        iv_donghua.setOnClickListener(this);

        content_donghua_ok = mHeadView.findViewById(R.id.content_donghua_ok);
        two_text_content = mHeadView.findViewById(R.id.two_text_content);
        tv_from_big_donghua = (TextView) mHeadView.findViewById(R.id.tv_from_big_donghua);
        roundProgressBar = (RoundProgressBar) mHeadView.findViewById(R.id.mProgressBar);

        tv_installed_app = (TextView) mHeadView.findViewById(R.id.tv_installed_app);
        tv_ap_data = (TextView) mHeadView.findViewById(R.id.tv_ap_data);
        tv_backup_num = (TextView) mHeadView.findViewById(R.id.tv_backup_num);
        // tv_app_manager_installed = (TextView)
        // mHeadView.findViewById(R.id.tv_app_manager_installed);
        // tv_app_manager_space = (TextView)
        // mHeadView.findViewById(R.id.tv_app_manager_space);
        mCleaner = ProcessCleaner.getInstance(mActivity);

        mDeleteManager = AppMasterApplication.getInstance().getBuckupManager();
        mDeleteManager.registerBackupListener(this);
        mDeleteAdapter = new AppDeleteAdapter(mDeleteManager, HomeAppManagerFragment.this.mActivity);
        resources = AppMasterApplication.getInstance().getResources();
    }

    public void fillData() {
        tv_installed_app.setText(installedAppsSpan);
        tv_ap_data.setText(allAppsSizeSpan);
        tv_backup_num.setText(backUpSpan);

        // tv_app_manager_installed.setText(resources.getString(R.string.first_user_app)
        // + forTextList.size());
        // LeoLog.d("nowtest", "forTextList" + forTextList.size());
        // tv_app_manager_space.setText(resources.getString(R.string.first_used_space)
        // + deleteDataAllSize);
    }

    public void setListView() {
        list_delete.addHeaderView(mHeadView, null, false);
        list_delete.setAdapter(mDeleteAdapter);
    }

    private void loadData() {
        // countAppNum();
        // bottom two TextView
        forTextList = DeleteDataList = (ArrayList<AppItemInfo>) mDeleteManager.getDeleteList()
                .clone();
        deleteDataAllSize = countTotalSpace(DeleteDataList);

        installedAppsSpan = setTextColor(
                resources.getString(R.string.first_user_app), ""
                        + forTextList.size(),
                resources.getString(R.string.first_user_app) + forTextList.size());

        allAppsSizeSpan = setTextColor(resources.getString(R.string.first_used_space),
                deleteDataAllSize,
                resources.getString(R.string.first_used_space)
                        + deleteDataAllSize);

        int RestoreListSize = getRestoreList();
        backUpSpan = setTextColor(
                resources.getString(R.string.first_backups_app), ""
                        + RestoreListSize, resources.getString(R.string.first_backups_app)
                        + RestoreListSize);

        // clean View
        cleanView();
        cleanMemory();
    }

    private void cleanView() {
        mLastUsedMem = mCleaner.getUsedMem();// mTvUsedMemory.setText(TextFormater.dataSizeFormat(mLastUsedMem));
        mTotalMem = mCleaner.getTotalMem();// mTvTotalMemory.setText("/" +
                                           // TextFormater.dataSizeFormat(mTotalMem));
        mProgress = (int) (mLastUsedMem * 100 / mTotalMem);
    }

    private SpannableStringBuilder setTextColor(String startWord, String endWord, String totalWord) {
        int start = startWord.length();
        int end = endWord.length();
        String str = totalWord;
        SpannableStringBuilder style = new SpannableStringBuilder(str);
        // SpannableStringBuilder实现CharSequence接口
        style.setSpan(new ForegroundColorSpan(Color.parseColor("#a7a7a7")), 0, start,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new ForegroundColorSpan(Color.parseColor("#4285f4")), start, start + end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return style;
    }

    private void InitFourButton() {
        bg_show_xz_bf = findViewById(R.id.bg_show_xz_bf);
        bg_show_ll = findViewById(R.id.bg_show_ll);
        bg_show_dl = findViewById(R.id.bg_show_dl);
        bg_show_hotapp = findViewById(R.id.bg_show_hotapp);

        bg_show_xz_bf.setOnClickListener(this);
        bg_show_ll.setOnClickListener(this);
        bg_show_dl.setOnClickListener(this);
        bg_show_hotapp.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDeleteManager.unregisterBackupListener(this);
        LeoEventBus.getDefaultBus().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bg_show_xz_bf:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "backup");
                Intent intent = new Intent(mActivity, BackUpActivity.class);
                startActivity(intent);
                break;
            case R.id.bg_show_ll:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "data");
                Intent mIntent = new Intent(mActivity, FlowActivity.class);
                startActivity(mIntent);
                break;
            case R.id.bg_show_dl:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "battery");
                Intent dlIntent = new Intent(mActivity, EleActivity.class);
                startActivity(dlIntent);
                break;
            case R.id.bg_show_hotapp:
                app_hot_tip_icon.setVisibility(View.GONE);
                sp_homeAppManager.setHomeFragmentRedTip(false);
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "hot");
                Intent nIntent = new Intent(mActivity, HotAppActivity.class);
                startActivity(nIntent);
                break;
            case R.id.iv_donghua:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "newboost");
                if (!isCleanning) {
                    cleanMem();
                }
                break;
        }
    }

    private void cleanMem() {
        // mAllowClean = mCleaner.allowClean();
        isCleanning = true;
        if (!isClean) {
            donghua_show_clean();
            // cleanMemory();
        } else {
            Toast.makeText(mActivity, R.string.the_best_status_toast, 1).show();
            isCleanning = false;
        }
    }

    private void donghua_show_clean() {
        new Thread() {
            public void run() {
                int endPoint = 0;
                while (mProgress > 0) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mProgress -= 1;
                    roundProgressBar.setProgress(mProgress);
                    Message msg = Message.obtain();
                    msg.what = DONGHUA_SHOW_BEGIN;
                    msg.obj = mProgress;
                    handler.sendMessage(msg);
                }
            };
        }.start();

        ScaleAnimation show = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                0.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        show.setDuration(800);

        show.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                two_text_content.setVisibility(View.GONE);
                showOkButton();
            }
        });

        two_text_content.setAnimation(show);
    }

    protected void showOkButton() {
        content_donghua_ok.setVisibility(View.VISIBLE);
        ScaleAnimation show = new ScaleAnimation(0.0f, 1.3f, 0.0f,
                1.3f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        show.setDuration(400);
        show.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ScaleAnimation show = new ScaleAnimation(1.3f, 1.0f, 1.3f,
                        1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                show.setDuration(300);
                show.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        isClean = true;
                        isCleanning = false;
                        String s = "";
                        if (mCleanMem == 0) {
                            s = mActivity.getString(R.string.home_app_manager_mem_clean_one);
                        } else {
                            s = mActivity.getString(R.string.home_app_manager_mem_clean,
                                    TextFormater.dataSizeFormat(mCleanMem));
                        }

                        Toast.makeText(mActivity, s, 0).show();
                    }
                });
                content_donghua_ok.setAnimation(show);
            }
        });
        content_donghua_ok.setAnimation(show);
    }

    private void cleanMemory() {
        mCleaner.tryClean(mActivity);
        long curUsedMem = mCleaner.getUsedMem();
        mCleanMem = Math.abs(mLastUsedMem - curUsedMem);
        // mAllowClean = false;
    }

    @Override
    public void onDataReady() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateDataList();
            }
        });

    }

    private void updateDataList() {
        mDeleteAdapter.updateData();
    }

    @Override
    public void onDataUpdate() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateDataList();
            }
        });
    }

    @Override
    public void onBackupProcessChanged(int doneNum, int totalNum, String currentApp) {

    }

    @Override
    public void onBackupFinish(boolean success, int successNum, int totalNum, String message) {

    }

    @Override
    public void onApkDeleted(boolean success) {

    }

    public synchronized int getRestoreList() {
        int i = 0;
        try {
            String path = getBackupPath();
            if (path != null) {
                File backupDir = new File(path);
                File[] fs = backupDir.listFiles();
                for (File f : fs) {
                    String fPath = f.getAbsolutePath();
                    if (f.isFile() && fPath.endsWith(".apk")) {
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            LeoLog.e("Exception", e.getMessage());
        }
        return i;

    }

    public String getBackupPath() {
        if (isSDReady()) {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            path += BACKUP_PATH;
            File backupDir = new File(path);
            if (!backupDir.exists()) {
                boolean success = backupDir.mkdirs();
                if (!success) {
                    return null;
                }
            }
            return path;
        }
        return null;
    }

    private boolean isSDReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    private String countTotalSpace(ArrayList<AppItemInfo> deleteList) {
        String mTotalSizeString = "";
        long all = 0;
        for (int i = 0; i < deleteList.size(); i++) {
            AppItemInfo info = deleteList.get(i);
            if (info != null) {
                long a = info.cacheInfo.total;
                all = all + a;
            }
        }
        mTotalSizeString = mDeleteManager.convertToSizeString(all);
        return mTotalSizeString;
    }

    @Override
    public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {

    }

    @Override
    public void onSelected() {
        
        // LeoLog.d("HomeAppManagerFragment", "回来走不走这");
        if (!isClean && !isShowIng) {
            isShowIng = true;
            showdonghua();
        }
        
        curFastThanset = true;
    }

    @Override
    public void onScrolling() {

    }

}
