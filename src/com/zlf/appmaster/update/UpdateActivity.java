
package com.zlf.appmaster.update;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.mgr.MgrContext;
import com.zlf.appmaster.mgr.UpdateManager;
import com.zlf.appmaster.mgr.impl.UpdateManagerImpl;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.LeoLog;


public class UpdateActivity extends Activity implements View.OnClickListener {

    private final static String TAG = UpdateActivity.class.getSimpleName();
    public final static String UPDATETYPE = "updatetype";
    public final static int MSG_UPDATE_PROGRESS = 1;
    public final static int MSG_DOWNLOAD_ERR = 2;
    public final static int MSG_APK_NOT_GOOD = 3;

    public final static int CHECK_UPDATE = 1;
    public final static int NO_NEED_UPDATE = 2;
    public final static int CHECK_FAILE = 3;
    public final static int NO_NETWORK = 4;
    public final static int FIND_NEW_VERSION = 5;
    public final static int DOWN_LOAD = 6;
    public final static int DOWN_LOAD_ERR = 7;
    public final static int APK_NOT_GOOD = 8;

    private String mFilePath = Environment.getExternalStorageDirectory()
            .getPath()
            + File.separator
            + ".zlf"
            + File.separator
            + "update"
            + File.separator;

    private String mDownloadUrl;
    private int mDownloadSize;
    private String mDownloadVersion;

    private int mProgress = 0;
    private int mComplete = 0;
    private int mTotal = 0;

    private int mNowType = 0;
    private static boolean mUserCancel = false;

    private UpdateManager updateManager;
    private ProgressHandler mProgressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog_progress_sdk);

        updateManager = (UpdateManager) MgrContext.getManager(MgrContext.MGR_UPDATE);
        mProgressHandler = new ProgressHandler(this);

        handleIntent();
        showView(mNowType);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mNowType = intent.getIntExtra(UPDATETYPE, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateManager.cancelDownload();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showView(int type) {
        switch (type) {
            case CHECK_UPDATE:
                showChecking();
                break;
            case NO_NEED_UPDATE:
                showNoNeedUpdate();
                break;
            case CHECK_FAILE:
                showCheckFailed();
                break;
            case NO_NETWORK:
                showNoNetwork();
                break;
            case FIND_NEW_VERSION:
                showNeedUpdate();
                break;
            case DOWN_LOAD:
                showDownloading();
                break;
            case DOWN_LOAD_ERR:
                showDownloadFailed();
                break;
            case APK_NOT_GOOD:
                showApkIsNotGood();
                break;
        }
    }

    private void showNoNeedUpdate() {
        setContentView(R.layout.dialog_message_single_done);
        TextView title = (TextView) findViewById(R.id.dlg_title);
        title.setText(getString(R.string.tips_title));
        title.setVisibility(View.VISIBLE);
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.update_no_need));
        RippleView rvBlue = (RippleView) findViewById(R.id.rv_blue);
        rvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }


    private void showNeedUpdate() {
        setContentView(R.layout.dialog_update_alarm);

        TextView title = (TextView) findViewById(R.id.dlg_title);
        title.setText(this.getString(R.string.find_new_version));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(this.getString(R.string.need_update_text));

        TextView tvYes = (TextView) findViewById(R.id.dlg_right_btn);
        tvYes.setText(getString(R.string.do_update));

        RippleView rvBlue = (RippleView) findViewById(R.id.rv_blue);
        rvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateManager.cancelCancelDownload();
                showView(DOWN_LOAD);
            }
        });
        TextView tvNo = (TextView) findViewById(R.id.dlg_left_btn);
        tvNo.setText(getString(R.string.ignore_update));
        RippleView rvWhite = (RippleView) findViewById(R.id.rv_white);
        rvWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showDownloadFailed() {

        setContentView(R.layout.dialog_alarm);
        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(getString(R.string.tips_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.download_error));
        TextView tvRetry = (TextView) findViewById(R.id.dlg_right_btn);
        tvRetry.setText(getString(R.string.retry));
        RippleView RvRetry = (RippleView) findViewById(R.id.rv_dialog_blue_button);
        RvRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showView(DOWN_LOAD);
            }
        });

        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel));

        RippleView RvCancel = (RippleView) findViewById(R.id.rv_dialog_whitle_button);
        RvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mUserCancel = true;
                finish();
            }
        });
    }

    private void showApkIsNotGood() {

        setContentView(R.layout.dialog_alarm);
        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(getString(R.string.tips_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.apk_fail));
        TextView tvRetry = (TextView) findViewById(R.id.dlg_right_btn);
        tvRetry.setText(getString(R.string.retry));
        RippleView RvRetry = (RippleView) findViewById(R.id.rv_dialog_blue_button);
        RvRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showView(DOWN_LOAD);
            }
        });

        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel));

        RippleView RvCancel = (RippleView) findViewById(R.id.rv_dialog_whitle_button);
        RvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mUserCancel = true;
                finish();
            }
        });
    }


    private void showDownloading() {

        String appName = getString(R.string.app_name);
        String downloadTip = getString(R.string.downloading, appName);
        setContentView(R.layout.dialog_progress_alarm_sdk);

        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(downloadTip);

        TextView tvContent = (TextView) findViewById(R.id.dlg_single_content);
        tvContent.setText(getString(R.string.app_name));

        final ProgressBar pb = (ProgressBar) findViewById(R.id.dlg_pro);
        pb.setProgress(mProgress);
        pb.setMax(100);

        final TextView tvPercent = (TextView) findViewById(R.id.dlg_pro_percent);
        tvPercent.setText(mProgress + "%");

        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel_download));
        RippleView rvCancel = (RippleView) findViewById(R.id.rv_white);
        rvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mUserCancel = true;
                updateManager.cancelDownload();
                finish();
            }
        });

        TextView tvHide = (TextView) findViewById(R.id.dlg_right_btn);
        tvHide.setText(getString(R.string.cancel_download));
        RippleView rvHide = (RippleView) findViewById(R.id.rv_blue);
        rvHide.setVisibility(View.GONE);


//        String url = "http://cdn.leomaster.com/am/appmaster_v4.0.1_2016-08-24-15-41_release_upgrade_release_4.0.1_lock_gp.apk";
//        final String filePath = "/storage/emulated/0/.zlf/update/appmaster_4.0.1.apk";
        final String filePath = mFilePath + "zlf_gold_" + mDownloadVersion + ".apk";
        updateManager.startDownload(mDownloadUrl, filePath, mDownloadSize, mComplete, new UpdateManagerImpl.DownLoadListener() {

            @Override
            public void Progress(int completeSize, int endPos) {
                if (mProgressHandler != null) {
                    mProgressHandler.obtainMessage(MSG_UPDATE_PROGRESS, completeSize, endPos)
                            .sendToTarget();
                }
            }

            @Override
            public void DownloadErr() {
                if (mProgressHandler != null) {
                    mProgressHandler.obtainMessage(MSG_DOWNLOAD_ERR)
                            .sendToTarget();
                }
            }

            @Override
            public void LoadDone() {
                if (AppUtil.checkApkIsGood(UpdateActivity.this, filePath).isEmpty()) {
                    LeoLog.d(TAG, "mFileAbsName + " + filePath
                            + "----下载的apk包不能用，无法获取versionName，删除apk");
                    AppUtil.cleanApk(filePath);

                    UpdateActivity.this.mProgress = 0;
                    UpdateActivity.this.mComplete = 0;
                    if(mProgressHandler != null){
                        mProgressHandler.obtainMessage(MSG_APK_NOT_GOOD)
                                .sendToTarget();
                    }

                } else {
                    updateManager.installApk(filePath);
                    finish();
                }

            }
        });

    }


    //    没网络
    private void showNoNetwork() {
        setContentView(R.layout.dialog_message_single_done);
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.network_error_msg));
        TextView tvBtn = (TextView) findViewById(R.id.dlg_bottom_btn);
        RippleView rvBlue = (RippleView) findViewById(R.id.rv_blue);
        rvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    //    检查更新失败
    private void showCheckFailed() {
        setContentView(R.layout.dialog_alarm);
        TextView title = (TextView) findViewById(R.id.dlg_title);
        title.setText(getString(R.string.tips_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.network_busy_msg));
        TextView retry = (TextView) findViewById(R.id.dlg_right_btn);
        retry.setText(getString(R.string.retry));

        RippleView rvRetry = (RippleView) findViewById(R.id.rv_dialog_blue_button);
        rvRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showView(CHECK_UPDATE);
            }
        });

        TextView cancel = (TextView) findViewById(R.id.dlg_left_btn);
        RippleView rvCancel = (RippleView) findViewById(R.id.rv_dialog_whitle_button);
        cancel.setText(getString(R.string.cancel));
        rvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    //检查更新中...
    private void showChecking() {
        setContentView(R.layout.dialog_progress_sdk);
        RippleView rvCancel = (RippleView) findViewById(R.id.rv_blue);
        rvCancel.setOnClickListener(this);
        rvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateManager == null) {
                    return;
                }
                mUserCancel = true;
                finish();
            }
        });


        boolean hasInternet = AppUtil.hasInternet(UpdateActivity.this);
        if (hasInternet) {
            updateManager.checkUpdate(new OnRequestListener() {

                @Override
                public void onDataFinish(Object object) {
                    String currentVersionName = UpdateActivity.this.getString(R.string.version_name);
                    String target = (String) object;
                    String[] targets = target.split(";");
                    LeoLog.d("update", "UpdateActivity response : " + target);
                    mDownloadVersion = targets[0];
                    mDownloadSize = Integer.parseInt(targets[1]);
                    mDownloadUrl = targets[2];

                    if (currentVersionName.equalsIgnoreCase(mDownloadVersion)) {
                        //匹配
                        showView(NO_NEED_UPDATE);
                    } else {
                        //版本不匹配，升级
                        if (targets.length==3) {
                            showView(FIND_NEW_VERSION);
                        } else {
                            //error
                            showView(CHECK_FAILE);
                        }
                    }

                }

                @Override
                public void onError(int errorcode, String errorString) {
                    showView(CHECK_FAILE);
                }

            });
        } else {
            showView(NO_NETWORK);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateManager.cancelDownload();
    }

    private boolean isOutOfBounds(Activity context, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(context)
                .getScaledWindowTouchSlop();
        final View decorView = context.getWindow().getDecorView();
        return (x < -slop) || (y < -slop)
                || (x > (decorView.getWidth() + slop))
                || (y > (decorView.getHeight() + slop));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && isOutOfBounds(this, event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }


    private class ProgressHandler extends Handler {
        WeakReference<UpdateActivity> mActivity;

        public ProgressHandler(UpdateActivity activity) {
            mActivity = new WeakReference<UpdateActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateActivity theActivity = mActivity.get();
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    /* no matter in which UI, finish when download done */
                    if (msg.arg1 == msg.arg2) {
                        LeoLog.d("UpdateManager", "load done");
                        theActivity.finish();
                        return;
                    } else {
                        theActivity.mComplete = msg.arg1;
                        theActivity.mTotal = msg.arg2;
                        long c = msg.arg1;
                        long t = msg.arg2;
                        LeoLog.d("UpdateManager", "Progress now : " + c + " , tol : " + t);

                        theActivity.mProgress = (t == 0) ? 0 : (int) (c * 100 / t);
                        theActivity.mComplete = msg.arg1;

                        ProgressBar pb = (ProgressBar) theActivity
                                .findViewById(R.id.dlg_pro);
                        pb.setProgress(theActivity.mProgress);
                        pb.setMax(100);
                        TextView tvPercent = (TextView) theActivity
                                .findViewById(R.id.dlg_pro_percent);
                        tvPercent.setText(theActivity.mProgress + "%");
                    }
                    break;
                case MSG_DOWNLOAD_ERR:
                    showView(DOWN_LOAD_ERR);
                    break;
                case MSG_APK_NOT_GOOD:
                    showView(APK_NOT_GOOD);
                    break;
            }
        }
    }


}
