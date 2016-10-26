
package com.zlf.appmaster.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.utils.LeoLog;


public class ClientOnlineActivity extends Activity {
    private CommonToolbar mToolBar;
    private WebView webView;
    private WebSettings mWebSettings;
//    private Button mButton;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientonline);
        mToolBar = (CommonToolbar) findViewById(R.id.appwallTB);
        mToolBar.setToolbarTitle(R.string.client_online);

//        mButton = (Button) findViewById(R.id.click);
//        mButton.setOnClickListener(new View.OnClickListener() {
//
//            @TargetApi(Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onClick(View v) {
//                webView.loadUrl("javascript:btnClick()");
//            }
//        });

        webView = (WebView) findViewById(R.id.webView);
        mWebSettings = webView.getSettings();

        mWebSettings.setDefaultTextEncodingName("utf-8");
        mWebSettings.setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {});
//        webView.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                webView.loadUrl("javascript:btnClick()");
//            }
//        });

//        mWebSettings.setSupportZoom(true);
//        mWebSettings.setBuiltInZoomControls(true);
//        mWebSettings.setUseWideViewPort(true);
//        mWebSettings.setDisplayZoomControls(false);
//        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        mWebSettings.setLoadWithOverviewMode(true);

        webView.addJavascriptInterface(getHtmlObject(), "jsObj");
        webView.loadUrl("file:///android_asset/iinndd.html");

    }



    private Object getHtmlObject() {
        Object insertObj = new Object() {
            @JavascriptInterface
            public void call() {
                LeoLog.d("testwebview","call");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            Uri data = Uri.parse("tel:" + Constants.CLIENT_PHONE);
                            intent.setData(data);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }

        };

        return insertObj;
    }
}
