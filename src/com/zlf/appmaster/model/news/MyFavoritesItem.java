package com.zlf.appmaster.model.news;

import android.content.Context;

import com.zlf.appmaster.db.stock.NewsFavoriteTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyFavoritesItem {
	private long  time;			// 收藏时间
    private ArrayList<NewsFlashItem> news;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ArrayList<NewsFlashItem> getNews() {
        return news;
    }

    public void setNews(ArrayList<NewsFlashItem> news) {
        this.news = news;
    }

    public void addNewsItem(NewsFlashItem newsFlashItem){
        if (news == null){
            news = new ArrayList<NewsFlashItem>();
        }
        news.add(newsFlashItem);
    }

    public static List<MyFavoritesItem> resolveJSONObject(JSONObject reponse) throws JSONException {
		
		ArrayList<MyFavoritesItem> items = new ArrayList<MyFavoritesItem>();
		
//		JSONArray dataArray = reponse.getJSONArray("data");
//		int dataLen = dataArray.length();
//		for(int i = 0; i < dataLen; i++){
//			MyFavoritesItem item = new MyFavoritesItem();
//			JSONObject data = dataArray.getJSONObject(i);
//			item.setTitle(data.optString("title",""));
//			item.setTime(data.optLong("time",0));
//			item.setFavoritesID(data.optLong("id",0));
//			item.setType(data.optInt("type",0));
//			//item.setUserUin();	// 服务器未传该值
//			items.add(item);
//		}
		
		return items;
		
	}

    /**
     *  根据id设置新闻内容
     *  解析网络收藏
     *  合并网络收藏的
     */

    /**
     * 解析新闻ID列表
     * @param object
     * @return
     */
    public static ArrayList<NewsFlashItem> resolveNewsIdArrayAndSave(Object object, Context context){
        ArrayList<NewsFlashItem> items = new ArrayList<NewsFlashItem>();
        try{
            JSONObject jsonObject = (JSONObject)object;
            JSONArray array = jsonObject.getJSONArray("data");

            for (int i=0;i<array.length();i++){
                NewsFlashItem item = new NewsFlashItem();
                items.add(item);

                JSONObject itemObject = array.getJSONObject(i);
                item.setClassify(itemObject.getInt("type"));
                item.setId(itemObject.getLong("id"));
                item.setTime(itemObject.getLong("time"));
                item.setTitle(itemObject.getString("title"));
                item.setStockList(itemObject.getJSONArray("stockList"));
                item.setSummary(itemObject.optString("summary"));
            }

            //保存到本地
            NewsFavoriteTable table = new NewsFavoriteTable(context);
            table.saveNewsFlashItemArray(items);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return items;
    }

    /**
     * 两个list合并
     * 将日期相同的合在一起,b>>a
     * 这里会改变bList的值！
     * 输入：两个list都是有序的，且A最后一个时间大于等于B的第一个时间
     */
    public static void combinationList(ArrayList<MyFavoritesItem> aList, ArrayList<MyFavoritesItem> bList){
        if(aList == null){
            return;
        }

        if (aList.size() > 0){
            long timeB = bList.get(0).getTime();
            long timeA = aList.get(aList.size() - 1).getTime();
            if (timeA == timeB) {
                MyFavoritesItem lastItemOfA = aList.get(aList.size() - 1);
                for (NewsFlashItem newsFlashItem : bList.get(0).getNews()) {
                    lastItemOfA.addNewsItem(newsFlashItem);
                }
                bList.remove(0);
            }
        }
        aList.addAll(bList);
    }

    /**
     * 获取新闻个数
     */
    public static int getNewsNum(ArrayList<MyFavoritesItem> list){
        if (list.isEmpty()){
            return 0;
        }

        int newsNum = 0;
        for (MyFavoritesItem item:list){
            newsNum += item.getNews().size();
        }
        return newsNum;
    }


}
