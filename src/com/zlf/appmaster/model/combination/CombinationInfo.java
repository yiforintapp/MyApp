package com.zlf.appmaster.model.combination;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.zlf.appmaster.db.stock.ContactsTableOld;
import com.zlf.appmaster.model.topic.TopicItem;
import com.zlf.appmaster.utils.TimeUtil;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by yu on 15-1-28.
 */
public class CombinationInfo implements Serializable,Comparable {
    private static final String TAG = "CombinationInfo";

    /**
     * 正在创建
     */
    public static int CREATING = 0;

    /**
     *  待运行
     */
    public static int TO_RUN = 1;

    /**
     * 正在运行
     */
    public static int RUNNING = 2;

    /**
     * 运行结束
     */
    public static int FINISH = 3;

    /**
     * 调仓未成交
     */
    public static int REPOSING = 4;

    private String name;
    private String desc;//组合宣言
    private float gain;
    private int followNum;
    private long id;
    private String admin;
    private String OHeadImg;//组合管理员头像

    private long startTime,stopTime;
    private int runningFlag;
    private int deadline;
    private int restTime;
    private float dayGain;
    private float targetGain;
    private boolean isFollowed;
    private long adminUin;
    private int cycle;//周期
    private List<TopicItem> topicItems;

    public String getOHeadImg() {
        return OHeadImg;
    }

    public void setOHeadImg(String OHeadImg) {
        this.OHeadImg = OHeadImg;
    }

    public long getAdminUin() {
        return adminUin;
    }

    public void setAdminUin(long adminUin) {
        this.adminUin = adminUin;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean isFollowed) {
        this.isFollowed = isFollowed;
    }


    public String getDeadlineFormat(){
        return "期限"+deadline+"天";
    }

    public String getRestTimeFormat(){
        return "距运行结束剩余"+restTime+"天";
    }
    public String getRestTimeFormat2(){
        return "距结束剩余"+restTime+"天";
    }

    public String getRestTimeFormat3(){
        return "剩余"+restTime+"天";
    }

    public String getStopTimeFormat(){
        return "已结束"+ TimeUtil.getYearAndDay(stopTime);
    }

    /**
     * 获取有效期
     * @return
     */
    public String getTimeOfValidity(){
        return String.format("有效期：%s至%s",
                TimeUtil.getYearAndDay(getStartTime()),
                TimeUtil.getYearAndDay(getStopTime()));
    }

    public String getFollowFormat(){
        return getFollowNum()+"人跟随";
    }

    public String getGainFormat(){
        return String.format("%.2f%%", getGain() * 100f);
    }

    public String getDayGainFormat(){
        if (runningFlag == FINISH){
            return "--";
        }
        return String.format("%.2f%%", getDayGain() * 100f);
    }

    public String getTargetGainFormat(){
        return String.format("%.2f%%",getTargetGain()*100f);
    }

    public int getDeadline() {
        return deadline;
    }

    public int getRestTime() {
        return restTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    public float getTargetGain() {
        return targetGain;
    }

    public void setTargetGain(float targetGain) {
        this.targetGain = targetGain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }
    public float getDayGain() {
        return dayGain;
    }
    public void setDayGain(float dayGain) {
        this.dayGain = dayGain;
    }
    public int getFollowNum() {
        return followNum;
    }

    public void setFollowNum(int followNum) {
        this.followNum = followNum;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAdminFormat(){
        return "基金经理"+getAdmin();
    }
    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public int getRunningFlag() {
        return runningFlag;
    }

    public void setRunningFlag(int runningFlag) {
        this.runningFlag = runningFlag;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }
    public List<TopicItem> getTopicItems() {
        return topicItems;
    }

    public void setTopicItems(List<TopicItem> topicItems) {
        this.topicItems = topicItems;
    }

    public static CombinationInfo resolveItem(JSONObject itemObject,
                                              String adminUin, String adminName,
                                              ContactsTableOld table, long curTime, long uin){
        CombinationInfo info = new CombinationInfo();

        try {
            info.setId(itemObject.getLong("Id"));

            info.setName(itemObject.optString("Name"));
            info.setFollowNum(itemObject.optInt("FollowCount"));

            info.setDeadline(itemObject.optInt("RunningDuration"));
            info.setStartTime(itemObject.optLong("StartTime"));
            info.setStopTime(itemObject.optLong("StopTime"));
            info.setRunningFlag(itemObject.optInt("RunningFlag"));


            //期限
            info.setCycle(itemObject.getInt("Cycle"));

            //剩余时间
            if (curTime < info.getStopTime()){
                long ms = info.getStopTime()-curTime;
                info.setRestTime((int)(ms/(1000*60*60*24)));
            }else {
                info.setRestTime(0);
            }

            info.setGain((float) itemObject.optDouble("TProfit"));
            info.setDayGain((float) itemObject.optDouble("DProfit"));
            info.setTargetGain((float) itemObject.optDouble("TargetProfit"));


            info.setAdmin(itemObject.optString("Cname"));
            info.setAdminUin(itemObject.optLong("Cuin"));
            info.setOHeadImg(itemObject.optString("OHeadImg"));

            if (info.getAdminUin() == 0 && !TextUtils.isEmpty(adminUin)){
                info.setAdminUin(Long.valueOf(adminUin));
            }

            if (TextUtils.isEmpty(info.getAdmin()) && !TextUtils.isEmpty(adminName)){
                info.setAdmin(adminName);
            }

            info.setFollowed(table.isFollowed(info.getId(), info.getAdminUin(), uin));

            // 所属题材
            List<TopicItem> topicItems = new ArrayList<TopicItem>();
            JSONArray topicJSONArray = itemObject.optJSONArray("Topics");
            if (null != topicJSONArray){
                int lenTopicArray = topicJSONArray.length();
                for (int i = 0; i < lenTopicArray; i++){
                    TopicItem item = new TopicItem();
                    JSONObject perItem = topicJSONArray.getJSONObject(i);
                    item.setID(perItem.getString("Topic"));
                    topicItems.add(item);
                }
                info.setTopicItems(topicItems);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static ArrayList<CombinationInfo> resolveList(Context context, JSONObject object){
        return resolveListAndSetName(context,object,"","");
    }

    public static ArrayList<CombinationInfo> resolveListAndSetName(Context context, JSONObject object, String adminUin, String adminName){
        ContactsTableOld table = new ContactsTableOld(context);
        String uinString = Utils.getAccountUin(context);
        long uin = 0;
        if (!TextUtils.isEmpty(uinString)) {
            uin = Long.valueOf(uinString);
        }
        ArrayList<CombinationInfo> data = new ArrayList<CombinationInfo>();
        try {
//            QLog.i(TAG,"object:"+object);
            /**
             dayProfit: 0,
             */
            long curTime = object.optLong("time");

            JSONArray jsonArray = object.optJSONArray("data");
            for (int i=0;i<jsonArray.length();i++){
                JSONObject itemObject = jsonArray.getJSONObject(i);
                data.add(resolveItem(itemObject,adminUin,adminName,table,curTime,uin));
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

//        //按时间排序
//        Collections.sort(data);

        return data;
    }

    public static String getIdsFromCursor(Cursor c){
        if (c != null) {
            StringBuffer ids = new StringBuffer();
            while (c.moveToNext()) {
                ids.append(c.getLong(0)+",");
            }
            return ids.toString();
        }

        return "";
    }


    @Override
    public int compareTo(Object another) {
        CombinationInfo info = (CombinationInfo)another;
        return info.getStartTime() < getStartTime() ? -1 : (info.getStartTime() == getStartTime() ? 0 : 1);
    }


}
