package com.zlf.appmaster.client;

import android.content.Context;

import com.zlf.appmaster.bean.StockFriend;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.db.stock.NewsRecentTable;
import com.zlf.appmaster.db.stock.StockFavoriteTable;
import com.zlf.appmaster.model.news.NewsDetail;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.model.news.NewsRecentItem;
import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsClient {
	public static final int STOCK_NEWS_LABEL_NEWS = 1;
	public static final int STOCK_NEWS_LABEL_ANNOUNCEMENT = 2;
	public static final int STOCK_NEWS_LABEL_REPORT = 3;

	//private final static String TAG = "NewsClient";
	private Context mContext;

	private static NewsClient mInstance = null;
	private NewsClient(Context context) {
		mContext = context;
	}

	public static NewsClient getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new NewsClient(context);
		}
		return mInstance;
	}

	// 获取新闻内容
    private static final String PATH_NEWS_BY_CID = UrlConstants.RequestHQURLString + "/QiNiuApp/core/news.newsContent8Id.do?";
	// 获取公告内容
	private static final String PATH_ANN_BY_CID = UrlConstants.RequestHQURLString + "/QiNiuApp/core/news.noticeContent8Id.do?";
	// 获取研报内容
	private static final String PATH_REPORT_BY_CID = UrlConstants.RequestHQURLString + "/QiNiuApp/core/news.reportContent8Id.do?";

//	// 获取个股公告
//	private static final String PATH_STOCK_ANNOUCENMENT = UrlConstants.RequsetBaseURLString + "/QiNiuApp/core/IOSNews.getAfMessageList.do?";
//    private static final String PATH_STOCK_ANNOUCENMENT_DETAIL = UrlConstants.RequsetBaseURLString + "/QiNiuApp/core/IOSNews.getAfficheContent.do?";

	//新闻收藏 -- 分享
	private static final String PATH_NEWS_SHARE = UrlConstants.RequestHQURLString + "/QiNiuApp/core/share.news.do?";

	//快讯 -- 获取标题
	private static final String PATH_NEWS_GET_TITLE = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSNews.getNews8ids.do?";
	// 获取新闻列表
	private static final String PATH_NEWS_GET_LIST = UrlConstants.RequestHQURLString + "/QiNiuApp/core/news.page.do?";

	// 获取订阅的新闻列表
	private static final String PATH_NEWS_SUBSCRIBE_LIST = UrlConstants.RequestHQURLString + "/QiNiuApp/core/news.classify.do?";

	// 获取最新一条新闻
	private static final String PATH_NEWS_NOTICE = UrlConstants.RequestHQURLString + "/QiNiuApp/core/news.notice.do?";


	public void requestNewsNotice(final OnRequestListener requestListener){
		UniversalRequest.requestBackground(mContext, PATH_NEWS_NOTICE, null, new OnRequestListener() {

			@Override
			public void onError(int errorCode, String errorString) {
				// TODO Auto-generated method stub
				if (null != requestListener)
					requestListener.onError(errorCode, errorString);
			}

			@Override
			public void onDataFinish(Object object) {
				// TODO Auto-generated method stub
				if (null != requestListener)
					requestListener.onDataFinish(object);
			}
		});
	}

    /**
     * 请求具体的新闻内容
     */
    public void requestNewsByCid(long mNewsCid,final OnRequestListener requestListener){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.os", "2");
        param.put("param.news_id", String.valueOf(mNewsCid));

        UniversalRequest.requestUrl(mContext, PATH_NEWS_BY_CID, param, new OnRequestListener() {

			@Override
			public void onError(int errorCode, String errorString) {
				// TODO Auto-generated method stub
				requestListener.onError(errorCode, errorString);
			}

			@Override
			public void onDataFinish(Object object) {
				// TODO Auto-generated method stub
				JSONObject response = (JSONObject) object;
				NewsDetail newsDetail;
				try {
					newsDetail = NewsDetail.resolveJsonObject(response);

					if (requestListener != null)
						requestListener.onDataFinish(newsDetail);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

    }

	/**
	 * 根据新闻id获取标题
	 * @param requestListener
	 * @param items
	 */
	public void requestNewsTitle(final OnRequestListener requestListener,ArrayList<NewsFlashItem> items){
		Map<String, String> param = new HashMap<String, String>();
        StringBuffer data = new StringBuffer();
        int i=0;
        for (NewsFlashItem item:items){
            if (i != 0){
                data.append(",");
            }
            i++;
            data.append(item.getId());

        }
        param.put("param.data",data.toString());

        UniversalRequest.requestUrl(mContext, PATH_NEWS_GET_TITLE, param, requestListener);
	}


    // 获取公告pdf的下载路径
    public String getAnnouncementDetailPath(long contentID){
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("param.notice_id", String.valueOf(contentID));
        UniversalRequest.addHeaderWithoutSessionId(mContext, param);
        String url = PATH_ANN_BY_CID+UniversalRequest.getMapString(param);

        return url;
    }



	public void requestStockReportByContentID(long contentID, final OnRequestListener requestListener){

		Map<String, String> param = new HashMap<String, String>();
		param.put("param.os", "2");
		param.put("param.news_id", String.valueOf(contentID));
		UniversalRequest.requestUrl(mContext, PATH_REPORT_BY_CID, param, new OnRequestListener() {

			@Override
			public void onError(int errorCode, String errorString) {
				// TODO Auto-generated method stub
				requestListener.onError(errorCode, errorString);
			}

			@Override
			public void onDataFinish(Object object) {
				// TODO Auto-generated method stub
				JSONObject response = (JSONObject) object;
				try {
					NewsDetail newsDetail = NewsDetail.resolveJsonObject(response);
					if (requestListener != null)
						requestListener.onDataFinish(newsDetail);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	/**
	 * 分享到股票大厅
	 * @param newsId
	 * @param newsType
	 * @param say
	 * @param requestListener
	 */
	public void requestShareToChatRoom(long newsId, int newsType, String say, final OnRequestListener requestListener) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.newsId", Long.toString(newsId));
		param.put("param.newsType", Integer.toString(newsType));
		param.put("param.toIds", "66_");
		say = UniversalRequest.chineseEncode(say);
		param.put("param.say", say);
		UniversalRequest.requestUrl(mContext, PATH_NEWS_SHARE, param, requestListener);
	}

	/**
	 * 分享给股友
	 * @param newsId
	 * @param newsType
	 * @param friends
	 * @param say
	 * @param requestListener
	 */
	public void requestShareToStockFriend(long newsId, int newsType, ArrayList<StockFriend> friends, String say, final OnRequestListener requestListener) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.newsId", Long.toString(newsId));
		param.put("param.newsType", Integer.toString(newsType));
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < friends.size(); i++) {
			if (i > 0 ) {
				buffer.append(",");
			}
			buffer.append("65_");
			buffer.append(friends.get(i).getUin());
		}

		param.put("param.toIds",buffer.toString());
		say = UniversalRequest.chineseEncode(say);
		param.put("param.say", say);

		UniversalRequest.requestUrl(mContext, PATH_NEWS_SHARE, param, requestListener);
	}

	/**
	 * 根据类型请求新闻列表
	 * @param key
	 * @param label
	 * @param maxId
	 * @param minFilterId
	 * @param lastRequestTime
	 * @param requestListener
	 */
	public void requestNewsList(String key, int label, long maxId, long minFilterId, long lastRequestTime, int pageSize, final OnRequestListener requestListener){
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.classify", key);
		param.put("param.label", String.valueOf(label));
		param.put("param.max_id", String.valueOf(maxId));
		param.put("param.min_filter_id", String.valueOf(minFilterId));
		param.put("param.last_request_time", String.valueOf(lastRequestTime));
		param.put("param.page_size", String.valueOf(pageSize));
		long uin = Utils.getAccountLongUin(mContext);
		param.put("param.head_uin", String.valueOf(uin));
		if (uin == 0){	// 未登录用户上传自选股信息，获取自选股新闻
			StockFavoriteTable stockFavoriteTable = new StockFavoriteTable(mContext);
			List<StockFavoriteItem> items = stockFavoriteTable.getStockFavoriteItems();
			String stockFavorites = "";
			for (StockFavoriteItem item:items){
				stockFavorites += item.getStockCode();
				stockFavorites +=",";
			}
			param.put("param.stock_ids", stockFavorites);
		}
		else{

		}
		UniversalRequest.requestUrl(mContext, PATH_NEWS_GET_LIST, param, requestListener);
	}
	// 个股请求新闻公告研报等
	public void requestStockNewsList(final String stockCode, final int label, final long maxId,
									 long minFilterId, long lastRequestTime, int pageSize, final OnRequestListener requestListener){
		final String newsKey = "10-"+ stockCode;
		requestNewsList(newsKey, label, maxId, minFilterId, lastRequestTime, pageSize, new OnRequestListener() {
			@Override
			public void onDataFinish(Object object) {
				JSONObject response = (JSONObject) object;
				try {
					List<NewsFlashItem> reportArray = NewsFlashItem.resolveNewsArray(newsKey, response);
					if (requestListener != null) {
						requestListener.onDataFinish(reportArray);
						if (maxId == 0) {    // 只缓存最新的几条数据
							int cacheID = -1;
							switch (label) {
								case STOCK_NEWS_LABEL_NEWS:
									cacheID = StockJsonCache.CACHEID_EXTRA_INFO_NEWS;
									break;
								case STOCK_NEWS_LABEL_ANNOUNCEMENT:
									cacheID = StockJsonCache.CACHEID_EXTRA_INFO_ANNOUCEMENT;
									break;
								case STOCK_NEWS_LABEL_REPORT:
									cacheID = StockJsonCache.CACHEID_EXTRA_INFO_REPORT;
									break;
								default:
									break;
							}
							StockJsonCache.saveToFile(stockCode, mContext, cacheID, response);
						}

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(int errorCode, String errorString) {
				requestListener.onError(errorCode, errorString);
			}
		});
	}


	public void requestNewsSubscribeList(long lcp, final OnRequestListener requestListener){
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.lcp", String.valueOf(lcp));
		UniversalRequest.requestUrl(mContext, PATH_NEWS_SUBSCRIBE_LIST, param, new OnRequestListener() {
			@Override
			public void onDataFinish(Object object) {
				JSONObject response = (JSONObject) object;
				try {
					List<NewsRecentItem> items = NewsRecentItem.resolveArray(response);

					if (null != items) {
						NewsRecentTable newsRecentTable = new NewsRecentTable(mContext);
						newsRecentTable.saveItems(items);	// 更新缓存中的相应数据
						items = newsRecentTable.getItems();
					}

					if (null != requestListener){
						requestListener.onDataFinish(items);

					}

				}catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (null != requestListener) {
						requestListener.onError(0, "json解析错误");
					}
				}
			}

			@Override
			public void onError(int errorCode, String errorString) {
				if (null != requestListener) {
					requestListener.onError(errorCode, errorString);
				}
			}
		});
	}
}
