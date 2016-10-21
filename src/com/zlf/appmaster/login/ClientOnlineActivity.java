
package com.zlf.appmaster.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.utils.LeoLog;


public class ClientOnlineActivity extends Activity {
    private CommonToolbar mToolBar;
    private WebView webView;
    private WebSettings mWebSettings;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientonline);
        mToolBar = (CommonToolbar) findViewById(R.id.appwallTB);
        mToolBar.setToolbarTitle(R.string.deal_product);


        webView = (WebView) findViewById(R.id.webView);
        mWebSettings = webView.getSettings();

        mWebSettings.setDefaultTextEncodingName("utf-8");
        mWebSettings.setJavaScriptEnabled(true);
//        mWebSettings.setSupportZoom(true);
//        mWebSettings.setBuiltInZoomControls(true);
//        mWebSettings.setUseWideViewPort(true);
//        mWebSettings.setDisplayZoomControls(false);
//        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        mWebSettings.setLoadWithOverviewMode(true);

//        webView.addJavascriptInterface(getHtmlObject(), "jsObj");
        webView.loadUrl("file:///android_asset/iinndd.html");
    }


//    private Object getHtmlObject() {
//        Object insertObj = new Object() {
//            @JavascriptInterface
//            public void changeHtml(final int i) {
//                LeoLog.d("testwebview","js get webview : " + i);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String url;
//                        if(i == 1){
//                            url = "file:///android_asset/cj_oil_2.html";
//                        }else if(i ==2 ){
//                            url = "file:///android_asset/cj_ag.html";
//                        }else if(i == 3){
//                            url = "file:///android_asset/cj_cu_2.html";
//                        }else{
//                            url = "file:///android_asset/dragon_oil_2.html";
//                        }
//                        webView.loadUrl(url);
//                    }
//                });
//            }
//
//        };
//
//        return insertObj;
//    }
}
