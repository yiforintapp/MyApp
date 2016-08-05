package com.zlf.appmaster.stocknews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.SyncClient;
import com.zlf.appmaster.db.stock.NewsFavoriteTable;
import com.zlf.appmaster.model.news.NewsDetail;
import com.zlf.appmaster.model.sync.SyncBaseBean;
import com.zlf.appmaster.model.sync.SyncOperator;
import com.zlf.appmaster.model.sync.SyncRequest;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;
import com.zlf.appmaster.utils.QConstants;
import com.zlf.appmaster.utils.QToast;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

//import cn.sharesdk.framework.Platform;
//import cn.sharesdk.framework.Platform.ShareParams;
//import cn.sharesdk.framework.PlatformActionListener;
//import cn.sharesdk.framework.ShareSDK;
//import cn.sharesdk.sina.weibo.SinaWeibo;
//import cn.sharesdk.tencent.qq.QQ;
//import cn.sharesdk.wechat.friends.Wechat;
//import cn.sharesdk.wechat.moments.WechatMoments;

public class NewsDetailActivity extends Activity {
	
	public static final int SHARE_FLAG_SUCCESS = 1;
	public static final int SHARE_FLAG_CANCEL = 2;
	public static final int SHARE_FLAG_FAIL = 3;
	
	public static final String KEY_INTENT_NEWS_ID = "news_id";
	public static final String KEY_INTENT_NEWS_CATALOGUE = "news_catalogue";
	public static final String KEY_INTENT_NEWS_TYPE = "news_type";  // 后缀类型
	/**
	 * 类型定义与服务器交互的值一致
	 */
	public static final int NEWS_TYPE_NORMAL = 1;
	public static final int NEWS_TYPE_ANNOUCEMENT = 11;
	public static final int NEWS_TYPE_REPORT = 12;

	
	private final static String TAG = "NewsDetailActivity";
	private NewsClient mNewsClient;
	private WebView mWebView;
	private TextView mNewsCagalogue;
	private ProgressBar mProgressBar;
	private int mNewsType;
	private long mNewsID;
   // private String mNewsCid;
	private View mShareView,mFavoriteView,mSettingView;
	private ImageView mFavoriteIconView;
	
	private NewsDetail mNewsDetail;
	private boolean mInitFavoriteStockFlag;
	private Context mContext;
	private MyHandler mHandler;
	private String mShareUrl;
	private boolean isShareing;
	private NewsFavoriteTable mTable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_detail);
		initViews();
		
		mContext = this;
		mHandler = new MyHandler(this);

		mTable = new NewsFavoriteTable(mContext);

		Intent intent = getIntent();
		mNewsID = intent.getLongExtra(KEY_INTENT_NEWS_ID, 0);
		mNewsType = intent.getIntExtra(KEY_INTENT_NEWS_TYPE, NEWS_TYPE_NORMAL);

		mNewsCagalogue.setText(intent.getStringExtra(KEY_INTENT_NEWS_CATALOGUE));
//		mLiveRecordingUtil = LiveRecordingUtil.getInstance();

		if (Utils.GetNetWorkStatus(NewsDetailActivity.this)) {

			mProgressBar.setVisibility(View.VISIBLE);
			// 拉取新闻
			mNewsClient = NewsClient.getInstance(this);
			
            if(mNewsType == NEWS_TYPE_ANNOUCEMENT){
				/*mNewsClient.requestStockAnnoucementByContentID(mNewsID, new OnRequestListener() {
					
					@Override
					public void onDataFinish(Object object) {
						// TODO Auto-generated method stub
						mNewsDetail = (NewsDetail) object;
						setContent(mNewsDetail);
						setActionBarIconVisibility(View.VISIBLE);
					}

					@Override
					public void onError(int errorCode, String errorString) {
						// TODO Auto-generated method stub
						mProgressBar.setVisibility(View.GONE);
						UrlConstants.showUrlErrorCode(NewsDetailActivity.this, errorCode);
						
					}
				});*/
			}
			else if (mNewsType == NEWS_TYPE_REPORT){
				mNewsClient.requestStockReportByContentID(mNewsID, new OnRequestListener() {

					@Override
					public void onDataFinish(Object object) {
						// TODO Auto-generated method stub
						mNewsDetail = (NewsDetail) object;
						setContent(mNewsDetail);
						setActionBarIconVisibility(View.VISIBLE);
					}

					@Override
					public void onError(int errorCode, String errorString) {
						// TODO Auto-generated method stub
						mProgressBar.setVisibility(View.GONE);
						UrlConstants.showUrlErrorCode(NewsDetailActivity.this, errorCode);

					}
				});
			}
			else {
                mNewsClient.requestNewsByCid(mNewsID, new OnRequestListener() {

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        mNewsDetail = (NewsDetail) object;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setContent(mNewsDetail);
								setActionBarIconVisibility(View.VISIBLE);
							}
						});
                    }

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        mProgressBar.setVisibility(View.GONE);
                        UrlConstants.showUrlErrorCode(NewsDetailActivity.this, errorCode);

                    }
                });
            }

		} else {
			Toast.makeText(this, getResources().getString(R.string.network_unconnected),
					Toast.LENGTH_SHORT).show();
		}

	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initViews(){
		mNewsCagalogue = (TextView)findViewById(R.id.title);
		mProgressBar = (ProgressBar)findViewById(R.id.content_loading);

		mShareView = findViewById(R.id.layout_share);
		mShareView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
//				share();
			}
		});
		mFavoriteView = findViewById(R.id.layout_favorite);
		mFavoriteView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String uin = Utils.getAccountUin(mContext);
				if (TextUtils.isEmpty(uin)){        // 需要注册用户才能添加
					/*Intent intent = new Intent(mContext, EntryPopActivity.class);
					mContext.startActivity(intent);*/
//					showEntryFengNiuPopwindow(view);
				}
				else {
					onFavorite();
				}

			}
		});

		mFavoriteIconView = (ImageView)findViewById(R.id.iv_icon);


		mWebView = (WebView)findViewById(R.id.news_detail_view);
		// webView.setInitialScale(100);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setDefaultTextEncodingName("UTF-8");
		webSettings.setBuiltInZoomControls(false);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		
		// 回调本地代码
		webSettings.setJavaScriptEnabled(true);

		//modify
		mWebView.addJavascriptInterface(new JsObject(), "Stock");
		mSettingView = findViewById(R.id.layout_my_setting);
		mSettingView.setVisibility(View.GONE);
	}

//	private void showEntryFengNiuPopwindow(View view) {
//		backgroundAlpha(0.4f);
//		EntryAppPopupWindow entryAppPopupWindow;
//		entryAppPopupWindow = new EntryAppPopupWindow(mContext,this);
//		entryAppPopupWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//
//		entryAppPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//			@Override
//			public void onDismiss() {
//				backgroundAlpha(1f);
//			}
//		});
//	}

	private void backgroundAlpha(float alpha) {
		WindowManager.LayoutParams lp=  getWindow().getAttributes();
		lp.alpha = alpha;
		getWindow().setAttributes(lp);
	}

	class  JsObject {
		@JavascriptInterface
		public void showDetail(String response) {
			goStockDetailInfo(response);
		}
	}

	public void setContent(NewsDetail newsDetail){
		mInitFavoriteStockFlag = newsDetail.isFavorite();

       // int splitLineWidth = DisplayUtil.px2dip(this,mWebView.getWidth()) - DisplayUtil.px2dip(this, 60);

		String newstitle = "<p style='font-size:20px'><font color='#333333'>"
				+ newsDetail.getTitle() + "</font></p>";
		String resourcetime = "<p align=right style='font-size:14.6px'><font color='#666666'>"
				+ newsDetail.getMedia() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ newsDetail.getTime() +  "</font></p>"
				//+ "<HR align=center width="+splitLineWidth+" color=#000000 noShade size=1>";
                + "<HR align=center paddingLeft=15 color=#ececec noShade size=1>";

		String content = newstitle + resourcetime + newsDetail.getContent();
		Utils.setWebViewLayout(mWebView, content);
		mProgressBar.setVisibility(View.GONE);
		mWebView.loadDataWithBaseURL(null, content, "text/html",
				"UTF-8", null);

		//是否收藏
		NewsFavoriteTable table = new NewsFavoriteTable(mContext);
		mNewsDetail.setIsFavorite(table.isFavorite(mNewsID,mNewsType));
	}
	
	/**
	 *	跳转至个股详情信息
	 */
	public void goStockDetailInfo(String response){
		try {
//			if(mLiveRecordingUtil.isLiveRecording()) {
//				JSONObject jsonObject = new JSONObject(response);
//				String stockId = jsonObject.getString("stockId");
//				String stockName = jsonObject.getString("stockName");
//
//				Intent intent = new Intent(NewsDetailActivity.this, LiveAnchorLectureActivity.class);
//				Bundle mBundle = new Bundle();
//				mLiveRecordingUtil.setStockName(stockName);
//				mLiveRecordingUtil.setStockCode(stockId);
//				mLiveRecordingUtil.setStock(true);
//				intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				intent.putExtras(mBundle);
//				startActivity(intent);
//			}else{
				JSONObject jsonObject = new JSONObject(response);
				String stockId = jsonObject.getString("stockId");
				String stockName = jsonObject.getString("stockName");

				Intent intent = new Intent(NewsDetailActivity.this, StockTradeDetailActivity.class);
				intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, stockName);
				intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, stockId);
				startActivity(intent);
//			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setActionBarIconVisibility(int visibility){
		if(visibility == View.VISIBLE){	// 处理收藏状态图标
			handleFavoriteBtn(mNewsDetail.isFavorite());
			mSettingView.setVisibility(View.VISIBLE);
		}else {
			mSettingView.setVisibility(View.GONE);
		}
	}


	private  void handleFavoriteBtn(boolean isFavorite){
		mNewsDetail.setIsFavorite(isFavorite);

		if(isFavorite){
			mFavoriteIconView.setImageResource(R.drawable.news_icon_favorite_yes);
		}
		else{
			mFavoriteIconView.setImageResource(R.drawable.news_icon_favorite_no);
		}
	}
	
	/**
	 * 回传值
	 */
	private void goBackResult(){
		if(null != mNewsDetail){
			if(mInitFavoriteStockFlag != mNewsDetail.isFavorite())	// 有改变则回传该信息
				setResult(QConstants.RESULTCODE_UPDATE_MYFAVORITES);
		}	
	}
	public void onBack(View view){
		goBackResult();
		finish();
	}
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		goBackResult();
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		MobclickAgent.onResume(this);
//		LiveOperationControlAgent.getInstance().onResume(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
//		MobclickAgent.onPause(this);
//		LiveOperationControlAgent.getInstance().onPause(this);

	}
	
	

	private void onFavorite(){
		final boolean curFavoriteFlag = !mNewsDetail.isFavorite();
		int favoriteType;
		if (mNewsType == NEWS_TYPE_REPORT){
			favoriteType = 2;
		}
		else {
			favoriteType = 1;
		}

		mTable.saveNewsFavorite(mNewsID, favoriteType, curFavoriteFlag);
		handleFavoriteBtn(curFavoriteFlag);
		if(curFavoriteFlag){
			QToast.showShortTime(NewsDetailActivity.this, "收藏成功");
		}else {
			QToast.showShortTime(NewsDetailActivity.this, "取消收藏成功");
		}
	}
	
//	//分享
//	private void share() {
//
//		if (null != mNewsDetail){
//			if (mNewsDetail.getClassify() == NEWS_TYPE_REPORT){	// 分享研报
//				mShareUrl = String.format("http://share.fengniuzhibo.com:8080/QiNiuWeb/core/share.report.do?"
//						+ "param.report_id=%d",mNewsID);
//			}
//			else{
//				mShareUrl = String.format("http://share.fengniuzhibo.com:8080/QiNiuWeb/core/share.news.do?"
//						+ "param.news_id=%d",mNewsID);
//			}
//
//			//微信好友、朋友圈、微博好友。。
//			final ShareDialog shareDialog = new ShareDialog(mContext);
//			shareDialog.setIsShareToExternal(true);
//
//			shareDialog.setOnShareListener(new OnShareListener() {
//
//				@Override
//				public void onShare(int flag) {
//					if (!shareDialog.isShowing()) {
//						QLog.i(TAG, "防止重复点击");
//						return;
//					}
//					shareDialog.cancel();
//
//					shareToPlatform(flag, mContext, mShareUrl, mNewsDetail.getTitle(),
//							mNewsDetail.getBreviary(), new OnSharePlatformListener());
//				}
//			});
//			shareDialog.show();
//		}
//	}

//	/**
//	 * 分享到第三方平台
//	 * @param platformFlag
//	 * @param context
//	 * @param url
//	 * @param title
//	 * @param content
//	 * @param listener
//	 */
//	public static void shareToPlatform(int platformFlag, Context context, String url, String title,
//									   String content, PlatformActionListener listener){
//
//		switch (platformFlag) {
//			case ShareDialog.SHARE_FLAG_WEIXIN:
//				shareToWeChat(context, url, title, content, listener);
//				break;
//			case ShareDialog.SHARE_FLAG_WEIXINMOMENTS:
//				shareToWeChatMoments(context, url, title, content, listener);
//
//				break;
//			case ShareDialog.SHARE_FLAG_SINAWEIBO:
//				shareToSinaWeibo(context, url, title, content, listener);
//				break;
//
//			case ShareDialog.SHARE_FLAG_QQ:
//				shareToQQ(context, url, title, content, listener);
//				//图文 titleUrl
//				break;
//
//			/*case ShareDialog.SHARE_FLAG_STOCKFRIEND:
//				shareToStockFriend();
//				break;
//
//			case ShareDialog.SHARE_FLAG_STOCKCHATROOM:
//				shareToStockChatRoom();
//				break;*/
//
//			default:
//				break;
//		}
//
//	}


//	private void shareToStockFriend() {
//		Intent intent = new Intent(mContext,NewsShareStockFriendActivity.class);
//		intent.putExtra(NewsShareStockFriendActivity.INTENT_EXTRA_NEWSID, mNewsID);
//		intent.putExtra(NewsShareStockFriendActivity.INTENT_EXTRA_NEWSTYPE, mNewsType);
//		intent.putExtra(NewsShareStockFriendActivity.INTENT_EXTRA_NEWSCONTENT, mNewsDetail.getBreviary());
//		intent.putExtra(NewsShareStockFriendActivity.INTENT_EXTRA_NEWSSTOCKCODE, "");
//		startActivity(intent);
//	}
//
//	private void shareToStockChatRoom() {
//		final NewsShareDialog dialog = new NewsShareDialog(mContext);
//		dialog.setShareTitle(getResources().getString(R.string.defeat_share_title_chatroom));
//		dialog.setContentAndStock(mNewsDetail.getBreviary(), "");
//		dialog.setOnSendListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (isShareing) {
//					return;
//				}
//				isShareing = true;
//				mNewsClient.requestShareToChatRoom(mNewsID, mNewsType, dialog.getLeaveMsg(), new OnRequestListener() {
//
//					@Override
//					public void onError(int errorCode, String errorString) {
//						isShareing = false;
//						QToast.showShortTime(mContext, "服务器繁忙，请稍后再试");
//						dialog.cancel();
//					}
//
//					@Override
//					public void onDataFinish(Object object) {
//						isShareing = false;
//
//						/*// 保存至IM数据库
//						try {
//							JSONObject data = ((JSONObject) object).getJSONObject("data");
//							ChatTableTool.saveSendShareInfoToStockHall(mContext,  data.toString(), MessageItem.MSG_DATA_TYPE_SHARE_NEWS);
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}*/
//
//						//分享成功
//						QToast.showShortTime(mContext, "分享成功");
//						dialog.cancel();
//					}
//				});
//
//			}
//		});
//		dialog.show();
//	}
	
//	class OnSharePlatformListener implements PlatformActionListener{
//
//		@Override
//		public void onCancel(Platform arg0, int arg1) {
//			mHandler.sendEmptyMessage(SHARE_FLAG_CANCEL);
//			QLog.i(TAG, "分享取消");
//		}
//
//		@Override
//		public void onComplete(Platform arg0, int arg1,
//				HashMap<String, Object> arg2) {
//			mHandler.sendEmptyMessage(SHARE_FLAG_SUCCESS);
//			QLog.i(TAG, "分享成功");
//		}
//
//		@Override
//		public void onError(Platform arg0, int arg1, Throwable arg2) {
//			arg2.printStackTrace();
//			mHandler.sendEmptyMessage(SHARE_FLAG_FAIL);
//			QLog.i(TAG, "分享失败");
//		}
//
//	}
	
//	private static void shareToQQ(Context context, String url, String title,
//								  String content, PlatformActionListener listener) {//有问题 没有连接
//		Platform platform = ShareSDK.getPlatform(QQ.NAME);
//		ShareParams sp = new ShareParams();
//		sp.setTitle(title);
//		sp.setText(content);
//		QLog.i(TAG, ""+content+" "+url);
//		sp.setTitleUrl(url);// 标题的超链接
//		sp.setImageUrl("http://www.51qiniu.com/images/news_share_logo.png");//要替换一个网络图片
//
//		sp.setShareType(Platform.SHARE_IMAGE);
//		platform.setPlatformActionListener(listener);
//		platform.share(sp);
//	}
	
//	private static void shareToSinaWeibo(Context context, String url, String title,
//										 String content, PlatformActionListener listener) {
//		Platform platform = ShareSDK.getPlatform(context, SinaWeibo.NAME);
//		//连接可直接加在文本后面
//		if (null != platform) {
//			ShareParams sp = new ShareParams();
//			sp.setTitle(title);
//			sp.setText(content+" "+url);
//
//			sp.setShareType(Platform.SHARE_TEXT);
//			platform.setPlatformActionListener(listener);
//			platform.share(sp);
//		}
//	}
//
//	private static void shareToWeChat(Context context, String url, String title,
//									  String content, PlatformActionListener listener) {
//		Platform platform = ShareSDK.getPlatform(Wechat.NAME);
//		//分享网页形式 url
//		ShareParams sp = new ShareParams();
//		sp.setTitle(title);
//		sp.setText(content);
//		sp.setUrl(url);
//
//		sp.setShareType(Platform.SHARE_WEBPAGE);
//		platform.setPlatformActionListener(listener);
//		platform.share(sp);
//	}
//
//	private static void shareToWeChatMoments(Context context, String url, String title,
//											 String content, PlatformActionListener listener) {
//		Platform platform = ShareSDK.getPlatform(context, WechatMoments.NAME);
//
//		//分享网页形式 url
//		ShareParams sp = new ShareParams();
//		sp.setTitle(title);
//		sp.setText(content);
//		sp.setUrl(url);
//
//		sp.setShareType(Platform.SHARE_WEBPAGE);
//		platform.setPlatformActionListener(listener);
//		platform.share(sp);
//	}
	
	
	static class MyHandler extends Handler {
		WeakReference<NewsDetailActivity> mReference;
		
		public MyHandler(NewsDetailActivity activity) {
			mReference = new WeakReference<NewsDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			NewsDetailActivity activity = mReference.get();
			switch (msg.what) {
			case SHARE_FLAG_CANCEL:
				break;
				
			case SHARE_FLAG_SUCCESS:
				QToast.showShortTime(activity, "分享成功");
				break;
				
			case SHARE_FLAG_FAIL:
				QToast.showShortTime(activity, "分享失败,请稍后再试");
				break;
				
			default:
				break;
			}
			
		}
		
	}

	@Override
	protected void onDestroy() {
		/**
		 * 同步
		 */
		SyncRequest syncRequest = new SyncRequest(mContext, SyncBaseBean.SYNC_KEY_BOOKMARKS);
		syncRequest.addOperator(SyncOperator.ID_NORMAL_FAVORITE_ADD);
		syncRequest.addOperator(SyncOperator.ID_NORMAL_FAVORITE_DEL);
		syncRequest.commit();
		SyncClient.getInstance(mContext).requestSyncA(syncRequest, null);

		super.onDestroy();
	}


}
