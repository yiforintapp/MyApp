package com.zlf.appmaster.stocknews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.SyncClient;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.db.stock.NewsFavoriteTable;
import com.zlf.appmaster.model.news.NewsDetail;
import com.zlf.appmaster.model.sync.SyncBaseBean;
import com.zlf.appmaster.model.sync.SyncOperator;
import com.zlf.appmaster.model.sync.SyncRequest;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.QConstants;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class testWebViewActivity extends Activity {
	
	public static final String KEY_INTENT_NEWS_ID = "news_id";
	public static final String KEY_INTENT_NEWS_CATALOGUE = "news_catalogue";
	public static final String KEY_INTENT_NEWS_TYPE = "news_type";  // 后缀类型
	/**
	 * 类型定义与服务器交互的值一致
	 */
	public static final int NEWS_TYPE_NORMAL = 1;

	private WebView mWebView;
	private TextView mNewsCagalogue;

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_activity_news_detail);
		initViews();
		
		mContext = this;
		Intent intent = getIntent();

		getHtmlByUrl();

	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initViews(){


		mWebView = (WebView)findViewById(R.id.news_detail_view);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setDefaultTextEncodingName("UTF-8");
		webSettings.setBuiltInZoomControls(false);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

		// 回调本地代码
		webSettings.setJavaScriptEnabled(true);

		webSettings.setAllowContentAccess(true);
		webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
		webSettings.setAllowFileAccess(true);
		webSettings.setLoadsImagesAutomatically(true);

		webSettings.setLoadWithOverviewMode(true);



		mWebView.requestFocus();
		mWebView.requestFocusFromTouch();
		mWebView.setFocusable(true);
		mWebView.setFocusableInTouchMode(true);

		mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setDrawingCacheEnabled(true);
		// 屏蔽长按事件
		mWebView.setOnCreateContextMenuListener(null);



		//加载需要显示的网页
		mWebView.loadUrl("http://120.24.14.76:1688/work?proname=ffffff");
		//设置Web视图
		mWebView.setWebViewClient(new HelloWebViewClient ());
	}

	public void getHtmlByUrl() {



	}

//	@Override
//	//设置回退
//	//覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
//			mWebView.goBack(); //goBack()表示返回WebView的上一页面
//			return true;
//		}
//		return false;
//	}


	//Web视图
	private class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}





	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();

	}
	
	


	@Override
	protected void onDestroy() {

		super.onDestroy();
	}


}
