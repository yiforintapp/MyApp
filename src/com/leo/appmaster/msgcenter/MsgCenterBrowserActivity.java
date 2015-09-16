package com.leo.appmaster.msgcenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.leo.appmaster.R;
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.sdk.BaseBrowserActivity;
import com.leo.appmaster.sdk.push.ui.WebViewActivity;
import com.leo.appmaster.ui.CommonTitleBar;

public class MsgCenterBrowserActivity extends BaseBrowserActivity implements
        View.OnClickListener {
    private static final String KEY_URL = "key_url";
    private static final String KEY_TITLE = "key_title";
    private static final String KEY_UPDATE = "key_update";

    private static final String JSBRIDGE = "jsbridge";
    private static final String HOST_MSGCENTER = "msgcenter";
    private static final String PATH_WEBVIEW = "/webview";
    private static final String PARAMS_URL = "url";

    private CommonTitleBar mTitleBar;

    private String mUrl;
    private String mLocalUrl;
    // 是否是更新日志
    private boolean mIsUpdate;

    /**
     * 启动消息中心二级页面
     * @param context
     * @param title
     * @param url
     * @param isUpdate 是否是更新日志
     */
    public static void startMsgCenterWeb(Context context, String title, String url, boolean isUpdate) {
        Intent intent = new Intent(context, MsgCenterBrowserActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_UPDATE, isUpdate);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mc_browser);

        mUrl = getIntent().getStringExtra(KEY_URL);
        if (TextUtils.isEmpty(mUrl)) {
            finish();
            return;
        }
        mIsUpdate = getIntent().getBooleanExtra(KEY_UPDATE, false);

        String title = getIntent().getStringExtra(KEY_TITLE);
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTitleBar.setTitle(title);
        mTitleBar.setBackArrowVisibility(View.VISIBLE);
        mTitleBar.setOptionImage(R.drawable.ic_msg_center_refresh);
        mTitleBar.setBackViewListener(this);
        mTitleBar.setOptionListener(this);

        if (mIsUpdate) {
            // 更新日志从本地获取
            String urlName = MsgCenterFetchJob.getFileName(mUrl) + ".html";
            String path = MsgCenterFetchJob.getFilePath(urlName);
            File file = new File(path);
            if (file.exists()) {
                mLocalUrl = "file:///" + path;
                getWebView().loadUrl(mLocalUrl);
            } else {
                getWebView().loadUrl(mUrl);
            }
        } else {
            getWebView().loadUrl(mUrl);
        }
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
                mWebView.reload();
                break;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        String schema = uri.getScheme();
        if (JSBRIDGE.equals(schema)) {
            String host = uri.getHost();
            String path = uri.getPath();
            if (HOST_MSGCENTER.equals(host) && PATH_WEBVIEW.equals(path)) {
                String paramsUrl = uri.getQueryParameter(PARAMS_URL);
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.WEB_URL, paramsUrl);
                startActivity(intent);

                return true;
            }
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
