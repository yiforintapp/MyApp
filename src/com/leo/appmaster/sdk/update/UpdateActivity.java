
package com.leo.appmaster.sdk.update;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leo.analytics.LeoAgent;
import com.leo.analytics.update.IUIHelper;
import com.leo.analytics.update.UpdateManager;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.home.GooglePlayGuideActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;

public class UpdateActivity extends BaseActivity implements OnStateChangeListener {

    private final static String TAG = UpdateActivity.class.getSimpleName();
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

    private boolean mForce = false;

    private boolean mUserCancel = false;

    public UpdateActivity() {
        mProgressHandler = new ProgressHandler(this);
        mManager = LeoAgent.getUpdateManager();
        mUIHelper = UIHelper.getInstance(this);
        mUIHelper.setOnProgressListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mForce && mUIType == IUIHelper.TYPE_CHECK_NEED_UPDATE
                && mParam == UpdateManager.FORCE_UPDATE) {
            mUserCancel = true;
            mManager.onCancelUpdate();
        }
        // send progress when downloading
        switch (mUIType) {
            case IUIHelper.TYPE_DOWNLOADING:
                if (!mUserCancel) {
                    mUIHelper.sendDownloadNotification(mUIHelper.getProgress());
                }
                break;
            case IUIHelper.TYPE_DOWNLOAD_FAILED:
                if (!mUserCancel) {
                    mUIHelper.sendDownloadFailedNotification();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUIHelper.unregisterOnProgressListener(this);
    }

    @Override
    protected void onResume() {
        mUserCancel = false;
        mUIType = mUIHelper.getLayoutType();
        mParam = mUIHelper.getLayoutParam();
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
                    mForce = true;
                } /* normal or force update */
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
            // case IUIHelper.BACK_DOWNLOAD_DONE:
            // showNeedUpdate();
            // break;
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
        RippleView RvRetry = (RippleView) findViewById(R.id.rv_dialog_blue_button);
        RvRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mManager.onRetryDownload();
                // finish();
            }
        });
        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel));

        RippleView RvCancel = (RippleView) findViewById(R.id.rv_dialog_whitle_button);
        RvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mUserCancel = true;
                mManager.onCancelDownload();
                mLockManager.filterAll(1000);
                if (mParam == UpdateManager.FORCE_UPDATE) {
                    finish();
                    AppMasterApplication.getInstance().exitApplication();
                } else {
                    finish();
                }
            }
        });
    }

    private void showForceUpdate() {
        /* sdk mark */
        SDKWrapper.addEvent(this, SDKWrapper.P1, "update", "pop_up");
        String appName = getString(R.string.app_name);
        String version = mManager.getVersion();
        String feature = mManager.getFeatureString();
        int size = mManager.getSize();
        float fsize = (float) size / 1024 / 1024;
        setContentView(R.layout.dialog_force_update_alarm);
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        Spanned msgText = Html.fromHtml(getString(R.string.update_datail_msg, appName, version,
                fsize, feature));
        tvMsg.setText(msgText);
        tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        TextView tvYes = (TextView) findViewById(R.id.dlg_bottom_btn);
        tvYes.setText(getString(R.string.do_update));
        RippleView rvBlue = (RippleView) findViewById(R.id.rv_blue);
        rvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* sdk mark */
                SDKWrapper.addEvent(UpdateActivity.this, SDKWrapper.P1, "update", "sure");
                mManager.onConfirmDownload();
            }
        });
    }

    ///
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
        RippleView rvCancel = (RippleView) findViewById(R.id.rv_white);
        rvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mUIHelper.cancelDownloadNotification();
                mManager.onCancelDownload();
                mLockManager.filterAll(1000);
                mUserCancel = true;
                finish();
            }
        });
        TextView tvHide = (TextView) findViewById(R.id.dlg_right_btn);
        tvHide.setText(getString(R.string.hide_download_window));
        RippleView rvHide = (RippleView) findViewById(R.id.rv_blue);
        rvHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                LeoLog.d(TAG,
                        "sendDownloadNotification in showDownloading, click hide window");
                mUIHelper.sendDownloadNotification(mProgress);
                mLockManager.filterAll(1000);
                finish();
            }
        });
    }

    ///
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

        RippleView rvCancel = (RippleView) findViewById(R.id.rv_blue);
        rvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mUserCancel = true;
                mUIHelper.cancelDownloadNotification();
                mManager.onCancelDownload();
                finish();
                AppMasterApplication.getInstance().exitApplication();
            }
        });
    }

    ///
    private void showNoUpdate() {
        setContentView(R.layout.dialog_message_single_done);
        TextView title = (TextView) findViewById(R.id.dlg_title);
        title.setText(getString(R.string.tips_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.update_no_need));
        TextView tv = (TextView) findViewById(R.id.dlg_bottom_btn);
        RippleView rvBlue = (RippleView) findViewById(R.id.rv_blue);
        rvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    ///
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

    ///
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
                SDKWrapper.checkUpdate();
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

    ///
    private void showChecking() {
        setContentView(R.layout.dialog_progress_sdk);
        TextView tvHint = (TextView) findViewById(R.id.dlg_title);
        tvHint.setText(getString(R.string.check_update));
        TextView tvContent = (TextView) findViewById(R.id.dlg_content);
        tvContent.setText(getString(R.string.checking_update_msg));
        TextView tvCancel = (TextView) findViewById(R.id.dlg_bottom_btn);
        RippleView rvCancel = (RippleView) findViewById(R.id.rv_blue);
        rvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mManager == null) {
                    return;
                }
                mUserCancel = true;
                mManager.onCancelCheck();
                finish();
            }
        });
    }

    ///
    private void showNeedUpdate() {
        /* sdk mark */
        SDKWrapper.addEvent(this, SDKWrapper.P1, "update", "pop_up");
        mUIHelper.cancelUpdateNotification();
        String appName = getString(R.string.app_name);
        String version = mManager.getVersion();
        String feature = mManager.getFeatureString();
        int size = mManager.getSize();
        float fsize = (float) size / 1024 / 1024;
        setContentView(R.layout.dialog_update_alarm);

        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        Spanned msgText = Html.fromHtml(getString(R.string.update_datail_msg, appName, version,
                fsize, feature));
        tvMsg.setText(msgText);
        tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());

        TextView tvYes = (TextView) findViewById(R.id.dlg_right_btn);
        tvYes.setText(getString(R.string.do_update));

        RippleView rvBlue = (RippleView) findViewById(R.id.rv_blue);
        rvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKWrapper.addEvent(UpdateActivity.this, SDKWrapper.P1, "update", "sure");
//                if (AppUtil.appInstalled(UpdateActivity.this,
//                        Constants.GP_PACKAGE)) {
//                    mLockManager.filterSelfOneMinites();
//                }
                //jump where just lock one min
                mLockManager.filterSelfOneMinites();
                mManager.onConfirmDownload();
            }
        });
        TextView tvNo = (TextView) findViewById(R.id.dlg_left_btn);
        tvNo.setText(getString(R.string.ignore_update));
        RippleView rvWhite = (RippleView) findViewById(R.id.rv_white);
        rvWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKWrapper.addEvent(UpdateActivity.this, SDKWrapper.P1, "update", "cancel");
                mUserCancel = true;
                mManager.onCancelUpdate();
                LeoLog.i("UpdateActivity", "加锁应用：" + mLockManager.getLastPackage());
                mLockManager.filterAll(1000);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        mLockManager.filterAll(1000);
        super.onBackPressed();
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
                        mUserCancel = true;
                        mManager.onCancelUpdate();
                    } else if (mParam == UpdateManager.FORCE_UPDATE) {
                        mUserCancel = true;
                        /* this is a force update */
                        mManager.onCancelUpdate();
                        finish();
                        AppMasterApplication.getInstance().exitApplication();
                    }
                    /* sdk mark */
                    SDKWrapper.addEvent(UpdateActivity.this, SDKWrapper.P1, "update", "cancel");
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
                    mUserCancel = true;
                    mManager.onCancelCheck();
                    break;
                case IUIHelper.TYPE_DOWNLOAD_FAILED:
                    mUserCancel = true;
                    mManager.onCancelDownload();
                    if (mParam == UpdateManager.FORCE_UPDATE) {
                        finish();
                        AppMasterApplication.getInstance().exitApplication();
                    } else {
                        finish();
                    }
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
    }

    ;

    @Override
    public void onShowProgressOnStatusBar() {
        finish();
    }

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

    @Override
    public void onNotifyUpdateChannel(int channel) {
        String channelStr = "unknown";
        if (channel == IUIHelper.APP_MARKET) {
            channelStr = "GP";
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Intent intent2 = new Intent(
//                            UpdateActivity.this,
//                            GooglePlayGuideActivity.class);
//                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent2);
//                }
//            }, 200);
        } else if (channel == IUIHelper.DIRECT_DOWNLOAD) {
            channelStr = "link";
        }
        SDKWrapper.addEvent(this, SDKWrapper.P1, "update_channel", channelStr);
    }

}
