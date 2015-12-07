package com.leo.appmaster.msgcenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.leo.appmaster.R;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.sdk.BaseBrowserActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.ui.WebViewActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.utils.LeoLog;

public class MsgCenterBrowserActivity extends BaseBrowserActivity implements
        View.OnClickListener {
    private static final String TAG = "MsgCenterBrowserActivity";

    private CommonToolbar mTitleBar;

    private String mTitle;
    private String mUrl;
    private String mLocalUrl;
    // 是否是更新日志
    private boolean mIsUpdate;

    /**
     * 启动消息中心二级页面
     *
     * @param context
     * @param title
     * @param url
     * @param isUpdate 是否是更新日志
     */
    public static void startMsgCenterWeb(Context context, String title, String url, boolean isUpdate) {
        Intent intent = new Intent(context, MsgCenterBrowserActivity.class);
        intent.putExtra(MsgConsts.KEY_URL, url);
        intent.putExtra(MsgConsts.KEY_TITLE, title);
        intent.putExtra(MsgConsts.KEY_UPDATE, isUpdate);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mc_browser);

        mUrl = getIntent().getStringExtra(MsgConsts.KEY_URL);
        if (TextUtils.isEmpty(mUrl)) {
            finish();
            return;
        }
        mIsUpdate = getIntent().getBooleanExtra(MsgConsts.KEY_UPDATE, false);

        mTitle = getIntent().getStringExtra(MsgConsts.KEY_TITLE);
        mTitleBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTitleBar.setToolbarTitle(mTitle);
        mTitleBar.setOptionMenuVisible(true);
        mTitleBar.setOptionImageResource(R.drawable.ic_msg_center_refresh);
        mTitleBar.setOptionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.reload();
            }
        });

        if (mIsUpdate) {
            // 更新日志从本地获取
            String urlName = MsgCenterFetchJob.getFileName(mUrl) + ".html";
            String path = MsgCenterFetchJob.getFilePath(urlName);
            File file = new File(path);
            if (file.exists()) {
                mLocalUrl = "file:///" + path;
                getWebView().loadUrl(mLocalUrl);
                mTitleBar.setOptionMenuVisible(false);
            } else {
                getWebView().loadUrl(mUrl);
                mTitleBar.setOptionMenuVisible(true);
            }
        } else {
            getWebView().loadUrl(mUrl);
            mTitleBar.setOptionMenuVisible(true);
        }
        LeoLog.i(TAG, "url : " + mUrl);
    }

    @Override
    protected WebView getWebView() {
        return (WebView) findViewById(R.id.mc_browser_web);
    }

    @Override
    protected ProgressBar getLoadingView() {
        return (ProgressBar) findViewById(R.id.mc_progress);
    }

    @Override
    protected View getErrorView() {
        return findViewById(R.id.mc_error_ll);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_title_back:
                finish();
                break;
            case R.id.tv_option_image:
//                SDKWrapper.addEvent(this, SDKWrapper.P1, "upd", "upd_cnts");
                mWebView.reload();
                break;
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (getWebView().getVisibility() == View.VISIBLE) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "InfoGet", "get_dataOK");
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        String schema = uri.getScheme();
        if (!MsgConsts.JSBRIDGE.equals(schema)) {
            return super.shouldOverrideUrlLoading(view, url);
        }
        String host = uri.getHost();
        String path = uri.getPath();
        if (!MsgConsts.HOST_MSGCENTER.equals(host)) {
            return super.shouldOverrideUrlLoading(view, url);
        }
        String pUrl = uri.getQueryParameter(MsgConsts.PARAMS_URL);
        if (MsgConsts.PATH_WEBVIEW.equals(path)) {
            // 打开webview，3.0支持以前
            SDKWrapper.addEvent(this, SDKWrapper.P1, "InfoJump_cnts", "act_" + mTitle);
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.WEB_URL, pUrl);
            startActivity(intent);

            return true;
        } else if (MsgConsts.PATH_DOWNLOAD.equals(path)) {
            // 下载，3.1开始支持
            LeoLog.i(TAG, "shouldOverrideUrlLoading, download url: " + pUrl);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(pUrl));
                startActivity(intent);

                mLockManager.filterSelfOneMinites();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (MsgConsts.PATH_NATIVE_APP.equals(path)) {
            // 通用页面, 3.1开始支持
            LeoLog.i(TAG, "shouldOverrideUrlLoading, nativeapp url: " + pUrl);
            try {
                Intent intent = Intent.parseUri(pUrl, 0);
                startActivity(intent);

                mLockManager.filterSelfOneMinites();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return true;
        } else if (MsgConsts.PATH_FACEBOOK.equals(path)) {
            // facebook，3.1开始支持
            try {
                MsgUtil.openFacebook(pUrl, this);
                mLockManager.filterSelfOneMinites();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (MsgConsts.PATH_GOOGLEPLAY.equals(path)) {
            // googleplay，3.1开始支持
            try {
                MsgUtil.openGooglePlay(pUrl, this);
                mLockManager.filterSelfOneMinites();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (MsgConsts.PATH_PROMOTION.equals(path)) {
            // App推广，有App打开App，没有App跳GP下载，没有Gp直接打开浏览器下载
            // 3.1.1开始支持
            String pkg = uri.getQueryParameter(MsgConsts.PARAMS_PKG);
            boolean success = false;
            try {
                success = MsgUtil.openPromotion(this, pUrl, pkg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (success) {
                mLockManager.filterSelfOneMinites();
            }
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    protected WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (!mIsUpdate) {
            return super.shouldInterceptRequest(view, url);
        }

        String fileName = null;
        int pIndex = url.lastIndexOf("/");
        if (pIndex > 0 && pIndex < url.length() - 1) {
            fileName = url.substring(pIndex + 1);
        }
        if (TextUtils.isEmpty(fileName)) {
            return super.shouldInterceptRequest(view, url);
        }

        String mimeType;
        if (fileName.contains(".js")) {
            mimeType = "text/javascript";
        } else if (fileName.contains(".css")) {
            mimeType = "text/css";
        } else if (fileName.contains(".png")) {
            mimeType = "image/png";
        } else if (fileName.contains(".jpg")) {
            mimeType = "image/jpeg";
        } else if (fileName.contains(".gif")) {
            mimeType = "image/gif";
        } else {
            mimeType = "text/html";
        }

        String resName = MsgCenterFetchJob.getFileName(mUrl) + ".zip";
        String zipPath = MsgCenterFetchJob.getFilePath(resName);
        File zf = new File(zipPath);
        if (!zf.exists()) {
            return super.shouldInterceptRequest(view, url);
        }
        try {
            ZipFile zipFile = new ZipFile(zf);
            Enumeration enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                if (zipEntry.isDirectory()) continue;

                String entryName = zipEntry.getName();
                if (entryName.equals(fileName)) {
                    return new WebResourceResponse(mimeType, "utf-8", zipFile.getInputStream(zipEntry));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.shouldInterceptRequest(view, url);
    }

}
