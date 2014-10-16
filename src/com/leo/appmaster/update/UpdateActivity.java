
package com.leo.appmaster.update;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leoers.leoanalytics.LeoStat;
import com.leoers.leoanalytics.update.OnProgressListener;
import com.leoers.leoanalytics.update.UpdateManager;

public class UpdateActivity extends Activity implements OnProgressListener {

    private final static String TAG = UpdateActivity.class.getSimpleName();
    private Intent mIntent = null;
    private int mUIType = UpdateManager.TYPE_CHECKING;
    private int mParam = 0;
    private UpdateManager mManager = null;
    private Handler mProgressHandler = null;

    private int mProgress = 100;
    private int mComplete = 0;
    private int mTotal = 0;
    private boolean mIsNotifying = false;
    private final static int DOWNLOAD_NOTIFICATION_ID = 1001;
    /* mActionDownloadWindow must set value before build notification */
    private static String mActionDownloadWindow = "";

    public UpdateActivity() {
        mProgressHandler = new ProgressHandler(this);
        mManager = UpdateManager.getInstance(this);
        mManager.registerProgressListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntent = getIntent();
        mActionDownloadWindow = getPackageName() + UpdateManager.LEO_SDK_DOWNLOAD_TAG;
        buildDownloadNotification();
        Log.e(TAG, "onCreate");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "onNewIntent");
        mIntent = intent;
        Log.e(TAG, "onResume");
        mUIType = mIntent.getIntExtra(UpdateManager.LAYOUT_TYPE,
                UpdateManager.TYPE_CHECKING);
        mParam = mIntent.getIntExtra(UpdateManager.LAYOUT_PARAM,
                UpdateManager.TYPE_CHECKING);
        Log.e(TAG, "mUIType=" + mUIType);
        showView(mUIType, mParam);
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onStart");
        super.onStart();
        onNewIntent(getIntent());
    }

    //
    // @Override
    // protected void onResume() {
    // super.onResume();
    // }

    private void showView(int type, int param) {
        switch (type) {
            case UpdateManager.TYPE_CHECKING:
                showChecking();
                break;
            case UpdateManager.TYPE_CHECK_NEED_UPDATE:
                if (param == UpdateManager.NORMAL_UPDATE) {
                    showNeedUpdate();
                } else if (param == UpdateManager.FORCE_UPDATE) {
                    showForceUpdate();
                }/* normal or force update */
                break;
            case UpdateManager.TYPE_CHECK_NO_UPDATE:
                showNoUpdate();
                break;
            case UpdateManager.TYPE_CHECK_FAILED:
                showCheckFailed();
                break;
            case UpdateManager.TYPE_NO_NETWORK:
                showNoNetwork();
                break;
            case UpdateManager.TYPE_DOWNLOADING:
                mTotal = mManager.getTotalSize();
                if (param == UpdateManager.FORCE_UPDATE) {
                    showForceDownloading();
                } else if (param == UpdateManager.NORMAL_UPDATE) {
                    showDownloading();
                }
                break;
            case UpdateManager.TYPE_DOWNLOAD_FAILED:
                showDownloadFailed();
                break;
        }
    }

    /* stone: UI done */
    private void showDownloadFailed() {
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
                finish();
            }
        });
        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel));
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mManager.onCancelDownload();
                finish();
            }
        });
    }

    /* stone: UI done */
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
        tvMsg.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        ImageView iv = (ImageView) findViewById(R.id.dlg_left_icon);
        iv.setVisibility(View.INVISIBLE);
        TextView tvYes = (TextView) findViewById(R.id.dlg_bottom_btn);
        tvYes.setText(getString(R.string.do_update));
        tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.onConfirmDownload();
                finish();
            }
        });
    }

    /* stone: UI done */
    private void showDownloading() {
        mIsNotifying = false;
        nm.cancel(DOWNLOAD_NOTIFICATION_ID);
        String appName = getString(R.string.app_name);
        String downloadTip = getString(R.string.downloading, appName);
        setContentView(R.layout.dialog_progress_alarm_sdk);
        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(downloadTip);
        TextView tvContent = (TextView) findViewById(R.id.dlg_single_content);
        tvContent.setText(getString(R.string.app_name));
        ProgressBar pb = (ProgressBar) findViewById(R.id.dlg_pro);
        pb.setProgress(mProgress);
        pb.setMax(100);
        TextView tvSize = (TextView) findViewById(R.id.dlg_pro_state);
        tvSize.setText(getString(R.string.downloaded_size,
                (float) mComplete / 1024 / 1024,
                (float) mTotal / 1024 / 1024));

        long c = mComplete, t = mTotal;
        TextView tvPercent = (TextView) findViewById(R.id.dlg_pro_percent);
        tvPercent.setText(c * 100 / t + "%");

        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel_download));
        tvCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mIsNotifying = false;
                mManager.onCancelDownload();
                nm.cancel(DOWNLOAD_NOTIFICATION_ID);
                finish();
                // TODO: exit whole application
                if (mParam == UpdateManager.FORCE_UPDATE) {
                }
            }
        });
        if (mParam == UpdateManager.FORCE_UPDATE) {
            tvCancel.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            tvCancel.setBackgroundResource(R.drawable.button_bg_grey);
        }

        TextView tvHide = (TextView) findViewById(R.id.dlg_right_btn);
        tvHide.setText(getString(R.string.hide_download_window));
        if (mParam != UpdateManager.FORCE_UPDATE) {
            tvHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mIsNotifying = true;
                    sendDownloadNotification(mProgress);
                    finish();
                }
            });
        } else {
            tvHide.setVisibility(View.INVISIBLE);
        }
    }

    private void showForceDownloading() {
        mIsNotifying = false;
        nm.cancel(DOWNLOAD_NOTIFICATION_ID);
        String appName = getString(R.string.app_name);
        String downloadTip = getString(R.string.downloading, appName);
        setContentView(R.layout.dialog_progress_message_sdk);
        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(downloadTip);
        TextView tvContent = (TextView) findViewById(R.id.dlg_single_content);
        tvContent.setText(appName);
        ProgressBar pb = (ProgressBar) findViewById(R.id.dlg_pro);
        pb.setProgress(mProgress);
        pb.setMax(100);
        TextView tvSize = (TextView) findViewById(R.id.dlg_pro_state);
        tvSize.setText(getString(R.string.downloaded_size,
                (float) mComplete / 1024 / 1024,
                (float) mTotal / 1024 / 1024));

        long c = mComplete, t = mTotal;
        TextView tvPercent = (TextView) findViewById(R.id.dlg_pro_percent);
        tvPercent.setText(c * 100 / t + "%");

        TextView tvCancel = (TextView) findViewById(R.id.dlg_bottom_btn);
        tvCancel.setText(getString(R.string.cancel_download));
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mIsNotifying = false;
                mManager.onCancelDownload();
                nm.cancel(DOWNLOAD_NOTIFICATION_ID);
                // TODO: exit the whole application
                finish();
            }
        });
    }

    /* stone: UI done */
    private void showNoUpdate() {
        setContentView(R.layout.dialog_message);
        ImageView iv = (ImageView) findViewById(R.id.dlg_left_icon);
        iv.setVisibility(View.INVISIBLE);
        TextView title = (TextView) findViewById(R.id.dlg_title);
        title.setText(getString(R.string.tips_title));
        TextView tvMsg = (TextView) findViewById(R.id.dlg_content);
        tvMsg.setText(getString(R.string.update_no_need));
        tvMsg.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView tv = (TextView) findViewById(R.id.dlg_bottom_btn);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    /* stone: UI done */
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

    /* stone: UI done */
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

    /* stone: UI done */
    private void showChecking() {
        setContentView(R.layout.dialog_progress_sdk);
        TextView tvHint = (TextView) findViewById(R.id.dlg_pro_hint);
        tvHint.setText(getString(R.string.check_update));
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
                finish();
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

    private NotificationManager nm = null;
    private RemoteViews rv = null;
    private Notification dNotification = null;

    @SuppressWarnings("deprecation")
    private void buildDownloadNotification() {
        String appName = getString(R.string.app_name);
        String downloadTip = getString(R.string.downloading, appName);
        CharSequence from = appName;
        CharSequence message = downloadTip;
        nm = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        rv = new RemoteViews(this.getPackageName(),
                R.layout.sdk_notification_download);
        rv.setTextViewText(R.id.tv_title, downloadTip);
        Intent intent = new Intent(mActionDownloadWindow);
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0,
                intent, 0);
        dNotification = new Notification(R.drawable.ic_launcher,
                downloadTip, System.currentTimeMillis());
        dNotification.setLatestEventInfo(this, from, message, contentIntent);
        dNotification.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
    }

    private void sendDownloadNotification(int progress) {
        // Log.d(TAG, "sending Download Notification, isNotifying="
        // + mIsNotifying);
        if (mIsNotifying) {
            rv.setProgressBar(R.id.pb_download, 100, progress, false);
            rv.setTextViewText(R.id.tv_progress, progress + "%");
            dNotification.contentView = rv;
            nm.notify(DOWNLOAD_NOTIFICATION_ID, dNotification);
        }
    }

    private boolean isOutOfBounds(Activity context, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        final View decorView = context.getWindow().getDecorView();
        return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop))
                || (y > (decorView.getHeight() + slop));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && isOutOfBounds(this, event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (mUIType) {
            case UpdateManager.TYPE_CHECK_NEED_UPDATE:
                if (mParam == UpdateManager.NORMAL_UPDATE) {
                    mManager.onCancelUpdate();
                } else if (mParam == UpdateManager.FORCE_UPDATE) {
                    /* this is a force update, can not cancel by back key */
                    return true;
                }
                break;
            case UpdateManager.TYPE_DOWNLOADING:
                mIsNotifying = true;
                break;
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
                case 1:
                    if (theActivity.mUIType == UpdateManager.TYPE_DOWNLOADING) {
                        theActivity.mComplete = msg.arg1;
                        theActivity.mTotal = msg.arg2;
                        long c = msg.arg1;
                        long t = msg.arg2;
                        theActivity.mProgress = (int) (c * 100 / t);
                        // Log.d(TAG, "mProgress = " + theActivity.mProgress);
                        if (theActivity.mIsNotifying) {
                            theActivity.sendDownloadNotification(theActivity.mProgress);
                        } else {
                            ProgressBar pb = (ProgressBar) theActivity.findViewById(R.id.dlg_pro);
                            pb.setProgress(theActivity.mProgress);
                            pb.setMax(100);
                            TextView tvSize = (TextView) theActivity.findViewById(R.id.dlg_pro_state);
                            tvSize.setText(theActivity.getString(R.string.downloaded_size,
                                    (float) msg.arg1 / 1024 / 1024,
                                    (float) msg.arg2 / 1024 / 1024));
                            TextView tvPercent = (TextView) theActivity.findViewById(R.id.dlg_pro_percent);
                            tvPercent.setText(theActivity.mProgress + "%");
                        }
                        if (msg.arg1 == msg.arg2) {
                            theActivity.nm.cancel(DOWNLOAD_NOTIFICATION_ID);
                            Log.e(TAG,
                                    "cancel notification and finish UpdateActivity");
                            theActivity.finish();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onProgress(int progress, int max) {
        mProgressHandler.obtainMessage(1, progress, max).sendToTarget();
    }
}
