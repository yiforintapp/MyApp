
package com.leo.appmaster.update;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;
import com.leoers.leoanalytics.update.IUIHelper;
import com.leoers.leoanalytics.update.UpdateManager;

public class UpdateActivity extends BaseActivity implements OnStateChangeListener {

    private final static String TAG = UpdateActivity.class.getSimpleName();
    private Intent mIntent = null;
    private int mUIType = IUIHelper.TYPE_CHECKING;
    private int mParam = 0;
    private UpdateManager mManager = null;
    private Handler mProgressHandler = null;
    private UIHelper mUIHelper = null;

    private int mProgress = 0;
    private int mComplete = 0;
    private int mTotal = 0;

    private final static int MSG_UPDATE_PROGRESS = 1;
    private final static int MSG_NOTIFY_LAYOUT = 2;

    public UpdateActivity() {
        mProgressHandler = new ProgressHandler(this);
        mManager = UpdateManager.getInstance(this);
        mUIHelper = UIHelper.getInstance(this);
        mUIHelper.setOnProgressListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntent = getIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mIntent = intent;
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        // mUIType = mIntent.getIntExtra(IUIHelper.LAYOUT_TYPE,
        // IUIHelper.TYPE_CHECKING);
        // mParam = mIntent.getIntExtra(IUIHelper.LAYOUT_PARAM,
        // IUIHelper.TYPE_CHECKING);
        mUIType = mUIHelper.getLayoutType();
        mParam = mUIHelper.getLayoutParam();
        LeoLog.e(TAG, "mUIType=" + mUIType);
        showView(mUIType, mParam);
        super.onResume();
    }

    private void showView(int type, int param) {
        switch (type) {
            case IUIHelper.TYPE_CHECKING:
                showChecking();
                break;
            case IUIHelper.TYPE_CHECK_NEED_UPDATE:
                if (param == UpdateManager.NORMAL_UPDATE) {
                    showNeedUpdate();
                } else if (param == UpdateManager.FORCE_UPDATE) {
                    showForceUpdate();
                }/* normal or force update */
                break;
            case IUIHelper.TYPE_CHECK_NO_UPDATE:
                showNoUpdate();
                break;
            case IUIHelper.TYPE_CHECK_FAILED:
                showCheckFailed();
                break;
            case IUIHelper.TYPE_NO_NETWORK:
                showNoNetwork();
                break;
            case IUIHelper.TYPE_DOWNLOADING:
                mTotal = mManager.getTotalSize();
                mProgress = mUIHelper.getProgress();
                LeoLog.d(TAG, "TYPE_DOWNLOADING total=" + mTotal);
                if (param == UpdateManager.FORCE_UPDATE) {
                    showForceDownloading();
                } else if (param == UpdateManager.NORMAL_UPDATE) {
                    showDownloading();
                }
                break;
            case IUIHelper.TYPE_DOWNLOAD_FAILED:
                showDownloadFailed();
                break;
        }
    }

    private void showDownloadFailed() {
        mUIHelper.cancelDownloadFailedNotification();
        setContentView(R.layout.dialog_alarm);
        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(getString(R.string.tips_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.download_error));
        TextView tvRetry = (TextView) findViewById(R.id.dlg_right_btn);
        tvRetry.setText(getString(R.string.retry));
        tvRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mManager.onRetryDownload();
                // finish();
            }
        });
        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel));
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mManager.onCancelDownload();
                if (mParam == UpdateManager.FORCE_UPDATE) {
                    AppMasterApplication.getInstance().exitApplication();
                } else {
                    finish();
                }
            }
        });
    }

    private void showForceUpdate() {
        String appName = getString(R.string.app_name);
        String version = mManager.getVersion();
        Spanned feature = mManager.getFeature();
        int size = mManager.getSize();
        float fsize = (float) size / 1024 / 1024;
        setContentView(R.layout.dialog_message);
        TextView tvId = (TextView) findViewById(R.id.dlg_title);
        tvId.setText(getString(R.string.update_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.update_datail_msg, appName, version,
                fsize, feature));
        tvMsg.setLayoutParams(new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        ImageView iv = (ImageView) findViewById(R.id.dlg_left_icon);
        iv.setVisibility(View.INVISIBLE);
        TextView tvYes = (TextView) findViewById(R.id.dlg_bottom_btn);
        tvYes.setText(getString(R.string.do_update));
        tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.onConfirmDownload();
                // finish(); DO NOT finish here, download UI need it
            }
        });
    }

    private void showDownloading() {
        mProgress = mUIHelper.getProgress();
        mComplete = mUIHelper.getComplete();
        mTotal = mUIHelper.getTotal();
        LeoLog.d(TAG, "showDownloading, p=" + mProgress + "; c=" + mComplete
                + "; t=" + mTotal);
        mUIHelper.cancelDownloadNotification();
        String appName = getString(R.string.app_name);
        String downloadTip = getString(R.string.downloading, appName);
        setContentView(R.layout.dialog_progress_alarm_sdk);
        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(downloadTip);
        TextView tvContent = (TextView) findViewById(R.id.dlg_single_content);
        tvContent.setText(getString(R.string.app_name));
        ProgressBar pb = (ProgressBar) findViewById(R.id.dlg_pro);
        LeoLog.d(TAG, "mProgress=" + mProgress);
        pb.setProgress(mProgress);
        pb.setMax(100);
        TextView tvSize = (TextView) findViewById(R.id.dlg_pro_state);
        tvSize.setText(getString(R.string.downloaded_size,
                (float) mComplete / 1024 / 1024, (float) mTotal / 1024 / 1024));

        long c = mComplete, t = mTotal;
        TextView tvPercent = (TextView) findViewById(R.id.dlg_pro_percent);
        tvPercent.setText(c * 100 / t + "%");

        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel_download));
        tvCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mUIHelper.cancelDownloadNotification();
                mManager.onCancelDownload();
                finish();
            }
        });

        TextView tvHide = (TextView) findViewById(R.id.dlg_right_btn);
        tvHide.setText(getString(R.string.hide_download_window));
        tvHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                LeoLog.d(TAG,
                        "sendDownloadNotification in showDownloading, click hide window");
                mUIHelper.sendDownloadNotification(mProgress);
                finish();
            }
        });

    }

    private void showForceDownloading() {
        mUIHelper.cancelDownloadNotification();
        String appName = getString(R.string.app_name);
        String downloadTip = getString(R.string.downloading, appName);
        setContentView(R.layout.dialog_progress_message_sdk);
        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(downloadTip);
        TextView tvContent = (TextView) findViewById(R.id.dlg_single_content);
        tvContent.setText(appName);
        ProgressBar pb = (ProgressBar) findViewById(R.id.dlg_pro);
        LeoLog.d(TAG, "mProgress=" + mProgress);
        pb.setProgress(mProgress);
        pb.setMax(100);
        TextView tvSize = (TextView) findViewById(R.id.dlg_pro_state);
        tvSize.setText(getString(R.string.downloaded_size,
                (float) mComplete / 1024 / 1024, (float) mTotal / 1024 / 1024));

        long c = mComplete, t = mTotal;
        TextView tvPercent = (TextView) findViewById(R.id.dlg_pro_percent);
        tvPercent.setText(c * 100 / t + "%");

        TextView tvCancel = (TextView) findViewById(R.id.dlg_bottom_btn);
        tvCancel.setText(getString(R.string.cancel_download));
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mUIHelper.cancelDownloadNotification();
                mManager.onCancelDownload();
                AppMasterApplication.getInstance().exitApplication();
            }
        });
    }

    private void showNoUpdate() {
        setContentView(R.layout.dialog_message);
        ImageView iv = (ImageView) findViewById(R.id.dlg_left_icon);
        iv.setVisibility(View.INVISIBLE);
        TextView title = (TextView) findViewById(R.id.dlg_title);
        title.setText(getString(R.string.tips_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.update_no_need));
        tvMsg.setLayoutParams(new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView tv = (TextView) findViewById(R.id.dlg_bottom_btn);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    private void showNoNetwork() {
        setContentView(R.layout.dialog_message);
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.network_error_msg));
        TextView tvBtn = (TextView) findViewById(R.id.dlg_bottom_btn);
        tvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    private void showCheckFailed() {
        setContentView(R.layout.dialog_alarm);
        TextView title = (TextView) findViewById(R.id.dlg_title);
        title.setText(getString(R.string.tips_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.network_busy_msg));
        TextView retry = (TextView) findViewById(R.id.dlg_right_btn);
        retry.setText(getString(R.string.retry));
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                LeoStat.checkUpdate();
            }
        });
        TextView cancel = (TextView) findViewById(R.id.dlg_left_btn);
        cancel.setText(getString(R.string.cancel));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    private void showChecking() {
        setContentView(R.layout.dialog_progress_sdk);
        TextView tvHint = (TextView) findViewById(R.id.dlg_title);
        tvHint.setText(getString(R.string.check_update));
        TextView tvContent = (TextView) findViewById(R.id.dlg_content);
        tvContent.setText(getString(R.string.checking_update_msg));
        TextView tvCancel = (TextView) findViewById(R.id.dlg_bottom_btn);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.onCancelCheck();
                finish();
            }
        });
    }

    private void showNeedUpdate() {
        mUIHelper.cancelUpdateNotification();
        String appName = getString(R.string.app_name);
        String version = mManager.getVersion();
        Spanned feature = mManager.getFeature();
        int size = mManager.getSize();
        float fsize = (float) size / 1024 / 1024;
        setContentView(R.layout.dialog_alarm);
        TextView tvId = (TextView) findViewById(R.id.dlg_title);
        tvId.setText(getString(R.string.update_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.update_datail_msg, appName, version,
                fsize, feature));
        TextView tvYes = (TextView) findViewById(R.id.dlg_right_btn);
        tvYes.setText(getString(R.string.do_update));
        tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.onConfirmDownload();
                // finish(); do not finish, downloading UI need the activity
            }
        });
        TextView tvNo = (TextView) findViewById(R.id.dlg_left_btn);
        tvNo.setText(getString(R.string.ignore_update));
        tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.onCancelUpdate();
                finish();
            }

        });
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (mUIType) {
                case IUIHelper.TYPE_CHECK_NEED_UPDATE:
                    if (mParam == UpdateManager.NORMAL_UPDATE) {
                        mManager.onCancelUpdate();
                    } else if (mParam == UpdateManager.FORCE_UPDATE) {
                        /* this is a force update */
                        AppMasterApplication.getInstance().exitApplication();
                    }
                    break;
                case IUIHelper.TYPE_DOWNLOADING:
                    if (mParam == UpdateManager.FORCE_UPDATE) {
                        return true;
                    } else {
                        mUIHelper.sendDownloadNotification(mProgress);
                        finish();
                    }
                    break;
                case IUIHelper.TYPE_CHECKING:
                    mManager.onCancelCheck();
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    static class ProgressHandler extends Handler {
        WeakReference<UpdateActivity> mActivity;

        public ProgressHandler(UpdateActivity activity) {
            mActivity = new WeakReference<UpdateActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateActivity theActivity = mActivity.get();
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    if (msg.arg1 == msg.arg2) {
                        // LeoLog.d(TAG,
                        // "cancel notification and finish UpdateActivity");
                        theActivity.mUIHelper.cancelDownloadNotification();
                        theActivity.finish();
                        return;
                    }/* no matter in which UI, finish when download done */
                    if (theActivity.mUIType == IUIHelper.TYPE_DOWNLOADING) {
                        theActivity.mComplete = msg.arg1;
                        theActivity.mTotal = msg.arg2;
                        long c = msg.arg1;
                        long t = msg.arg2;
                        theActivity.mProgress = (t == 0) ? 0 : (int) (c * 100 / t);
                        // LeoLog.d(TAG, "mProgress = " +
                        // theActivity.mProgress);

                        ProgressBar pb = (ProgressBar) theActivity
                                .findViewById(R.id.dlg_pro);
                        pb.setProgress(theActivity.mProgress);
                        pb.setMax(100);
                        TextView tvSize = (TextView) theActivity
                                .findViewById(R.id.dlg_pro_state);
                        tvSize.setText(theActivity.getString(
                                R.string.downloaded_size,
                                (float) msg.arg1 / 1024 / 1024,
                                (float) msg.arg2 / 1024 / 1024));
                        TextView tvPercent = (TextView) theActivity
                                .findViewById(R.id.dlg_pro_percent);
                        tvPercent.setText(theActivity.mProgress + "%");
                    }
                    break;
                case MSG_NOTIFY_LAYOUT:
                    if (msg.arg1 == IUIHelper.TYPE_DISMISS) {
                        theActivity.finish();
                    } else {
                        LeoLog.d(TAG, "MSG_NOTIFY_LAYOUT: type=" + msg.arg1
                                + "; param=" + msg.arg2);
                        theActivity.showView(msg.arg1, msg.arg2);
                    }
                    break;
            }
        }
    };

    @Override
    public void onProgress(int progress, int total) {
        // LeoLog.d(TAG, "onProgress call back, progress=" + progress +
        // "; total="
        // + total);
        mProgressHandler.obtainMessage(MSG_UPDATE_PROGRESS, progress, total)
                .sendToTarget();
    }

    @Override
    public void onChangeState(int type, int param) {
        mUIType = type;
        mParam = param;
        mProgressHandler.obtainMessage(MSG_NOTIFY_LAYOUT, type, param)
                .sendToTarget();
    }
}
