
package com.zlf.appmaster.login;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ui.CommonToolbar;


public class ProtocolActivity extends Activity {
    private CommonToolbar mToolBar;
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol);
        mToolBar = (CommonToolbar) findViewById(R.id.appwallTB);
        mToolBar.setToolbarTitle(R.string.personal_use);

        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/zlf_use_rule.html");
    }


}
