package com.zlf.appmaster.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.QStringRequest;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.Utils;
import com.zlf.appmaster.utils.VolleyTool;


public class DayNewsDetailActivity extends Activity {
	
	private final static String TAG = "NewsDetailActivity";
	private final static String DAYNEWS = "daynews";
	private WebView mWebView;
	private CircularProgressView mProgressBar;
	private CommonToolbar mToolBar;
	private String mNewsID;
	private String mNewsTitle;
	private String mNewsTime;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daynews_detail);
		initViews();
		handleIntent();
		requestData();
	}

	private void requestData() {
		if (Utils.GetNetWorkStatus(DayNewsDetailActivity.this)) {
			mProgressBar.setVisibility(View.VISIBLE);
			String url = Constants.ADDRESS + Constants.APPSERVLET + Constants.DATA + DAYNEWS + "&id=" + mNewsID;
			LeoLog.d("testnewsJson" , "item url : " + url);

			QStringRequest stringRequest = new QStringRequest(
					Request.Method.GET, url, null, new Response.Listener<String>() {
				@Override
				public void onResponse(String string) {
					setContent(string);
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					mProgressBar.setVisibility(View.GONE);
				}
			});
			// callAll的时候使用
			VolleyTool.getInstance(this).getRequestQueue()
					.add(stringRequest);
		} else {
			Toast.makeText(this, getResources().getString(R.string.network_unconnected),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mNewsID = intent.getStringExtra(Constants.DAYNEWS_DETAILS_ID);
		mNewsTitle = intent.getStringExtra(Constants.DAYNEWS_DETAILS_TITLE);
		mNewsTime = intent.getStringExtra(Constants.DAYNEWS_DETAILS_TIME);
		LeoLog.d("testnewsJson","id : " + mNewsID + " , mNewsTitle : " + mNewsTitle + " , mNewsTime : " + mNewsTime);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initViews(){
		mToolBar = (CommonToolbar) findViewById(R.id.fb_toolbar);
		mToolBar.setToolbarTitle(getResources().getString(R.string.zlf_day_news_title));

		mProgressBar = (CircularProgressView)findViewById(R.id.content_loading);
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


	}

	public void setContent(String string){
       // int splitLineWidth = DisplayUtil.px2dip(this,mWebView.getWidth()) - DisplayUtil.px2dip(this, 60);

		String newstitle = "<p style='font-size:20px'><font color='#333333'>"
				+ mNewsTitle + "</font></p>";
		String resourcetime = "<p align=right style='font-size:14.6px'><font color='#666666'>"
				+ "" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ AppUtil.getDateTime(Long.parseLong(mNewsTime),3) +  "</font></p>"
				//+ "<HR align=center width="+splitLineWidth+" color=#000000 noShade size=1>";
                + "<HR align=center paddingLeft=15 color=#ececec noShade size=1>";


		String content = newstitle + resourcetime + string;
		Utils.setWebViewLayout(mWebView, content);
		mProgressBar.setVisibility(View.GONE);
		mWebView.loadDataWithBaseURL(null, content, "text/html",
				"UTF-8", null);
	}
	

	
	@Override
	public void onBackPressed() {
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
