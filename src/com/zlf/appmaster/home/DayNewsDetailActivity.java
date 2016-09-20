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
					LeoLog.d("testnewsJson" , "return string : " + string);
					string = "<p><strong>　　一、基本面</strong></p>\n" +
							"<p>　　1、法国巴黎银行：预计美联储本周将加息25个基点至0.75%</p>\n" +
							"<p>　　料美联储22日将加息25个基点至0.75%;料耶伦将通过偏向鸽派立场的妥协措施，获得对加息的支持，措施包括&ldquo;安抚性措辞&rdquo;、下调终端联邦基金利率并暗示年内不会再加息;此举或使金融市场动荡，但若美联储暗示明年3月前不会再加息，且长期利率下调，那么金融市场将很快恢复稳定。</p>\n" +
							"<p>　　2、 9月19日北京时间晚上22点公布的美国9月NAHB房产市场指数，实际值：65，前值：59 ，预期值：60。实际值大于预期值，利空金银。</p>\n" +
							"<p>　　3、美银美林：预计美联储将在12月份加息，以及明年加息两次;欧洲央行将量化宽松计划延长6个月。</p>\n" +
							"<p align=\\\"center\\\"><img src='http://www.zlf1688.com/d/file/zxpl/fyrp/87f40b35fb6df7a09ba703e7529bc5af.png' title='金银多头后市未必一片坦途.jpg' alt='金银多头后市未必一片坦途.jpg'   border='0' vspace='0' width='100%'/></p>\n" +
							"<p align=\\\"center\\\">(长江银60分钟图)</p>\n" +
							"<p><strong>　　二、市场聚焦</strong></p>\n" +
							"<p>　　【今日重点关注的财经数据与事件】2016年9月20日 周二</p>\n" +
							"<p>　　① 09:30 澳洲联储公布9月货币政策会议纪要</p>\n" +
							"<p>　　② 14:00 德国8月PPI月率、瑞士8月贸易帐</p>\n" +
							"<p>　　③ 20:30 美国8月新屋开工总数年化、美国8月营建许可总数</p>\n" +
							"<p>　　④ 次日04:30 美国至9月16日当周API原油库存</p>\n" +
							"<p align=\\\"center\\\"><img alt=\\\"白银踌躇不前等加息\\\" width=\\\"550\\\" height=\\\"276\\\" src=\\\"http://www.zlf1688.com/d/file/zxpl/fyrp/3a6c379f26e183e48de09afd6ad0940f.png\\\" /></p>\n" +
							"<p align=\\\"center\\\">白银价格支撑阻力位</p>\n" +
							"<p><strong>　　三、今日建议</strong></p>\n" +
							"<p>　　兆利丰金服分析师观点：尽管美国经济数据有所好转，但是市场依然并未对周内美联储加息给予足够预期，美指因此承压，银价因此反弹。预计在本周的美联储决议前，银价缺乏明确走势。本周的美联储决议才是银市的重头戏。</p>\n" +
							"<p align=\\\"center\\\"><img alt=\\\"白银踌躇不前等加息\\\" width=\\\"545\\\" height=\\\"307\\\" src=\\\"http://www.zlf1688.com/d/file/zxpl/fyrp/87f40b35fb6df7a09ba703e7529bc5af.png\\\" /></p>\n" +
							"<p align=\\\"center\\\">(长江银60分钟图)</p>\n" +
							"<p>　　技术面：小时图上看，均线呈多头排列，5日均线上穿10日均线形成金叉，同事MACD绿柱放量，若指数受布林带中轨压制有向下趋势，可考虑高空。</p>\n" +
							"<p>　　操作策略：高空为主，若长江银价格在4120附近受阻，考虑摘牌空单，止盈4080附近，止损4140附近。建议仅供参考!</p>\n";
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
