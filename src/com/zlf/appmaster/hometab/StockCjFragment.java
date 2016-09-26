package com.zlf.appmaster.hometab;

import android.webkit.WebView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;

/**
 * Created by Administrator on 2016/9/26.
 */
public class StockCjFragment extends BaseFragment {

    private WebView webView;

    @Override
    protected int layoutResourceId() {
        return R.layout.info_stock;
    }

    @Override
    protected void onInitUI() {
        webView = (WebView) findViewById(R.id.webView);
        webView.setHorizontalScrollBarEnabled(false);//水平不显示
        webView.setVerticalScrollBarEnabled(false); //垂直不显示
        webView.loadUrl("file:///android_asset/zlf_cj_info.html");
    }
}
