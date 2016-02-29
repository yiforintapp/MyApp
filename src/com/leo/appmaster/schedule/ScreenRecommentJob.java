package com.leo.appmaster.schedule;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Contacts;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.battery.BatteryAppItem;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.imageloader.utils.IoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jasper on 2016/2/26.
 */
public class ScreenRecommentJob extends FetchScheduleJob {
    private static final String TAG = "ScreenRecommentJob";

    // 3G通话
    private static final String KEY_CALL = "call";
    // wifi上网
    private static final String KEY_NET = "net";
    // 玩应用
    private static final String KEY_VIDEO = "video";

    // 包名
    private static final String DATA_PKG = "pkg";
    // icon地址
    private static final String DATA_ICON = "icon";
    // 跳转地址
    private static final String DATA_URL = "url";
    // 显示名称
    private static final String DATA_NAME = "name";

    private static HashMap<String, List<BatteryAppItem>> sRecommendData;

    public static void initialize() {
        loadAndParseData();
    }

    @Override
    protected void work() {
        Context context = AppMasterApplication.getInstance();
        FetchScheduleListener listener = newJsonObjListener();
        HttpRequestAgent.getInstance(context).loadBatteryRecommendList(listener, listener);
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        if (response == null) {
            LeoLog.d(TAG, "onFetchSuccess, response is null.");
            return;
        }

        JSONObject object = null;
        try {
            object = (JSONObject) response;
        } catch (Exception e) {
            LeoLog.d(TAG, "onFetchSuccess, response is not json array format." + e.getMessage());
            return;
        }

        // 3G通话
        try {
            parseCategoryData(object);
        } catch (Exception e) {
            LeoLog.e(TAG, "onFetchSuccess, parseCategoryData ex. " + e.getMessage());
            return;
        }
        // 以字符串的形式存储
        PreferenceTable.getInstance().putString(PrefConst.KEY_SS_RECOMMEND_LIST, response.toString());
    }

    private static int parseJsonArrayAndCache(String key, JSONArray array, boolean isApp)
            throws JSONException {
        List<BatteryAppItem> appItemList = new ArrayList<BatteryAppItem>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj == null) {
                continue;
            }
            BatteryAppItem item = parseJson(obj, isApp);
            if (item != null) {
                appItemList.add(item);
            }
        }

        if (sRecommendData == null) {
            sRecommendData = new HashMap<String, List<BatteryAppItem>>();
        }

        sRecommendData.put(key, appItemList);
        return appItemList.size();
    }

    private static BatteryAppItem parseJson(JSONObject object, boolean isApp) {
        BatteryAppItem appItem = new BatteryAppItem();

        try {
            appItem.actionUrl = object.getString(DATA_URL);
            appItem.iconUrl = object.getString(DATA_ICON);
            appItem.name = object.getString(DATA_NAME);
            appItem.pkg = object.getString(DATA_PKG);
        } catch (JSONException e) {
            LeoLog.e(TAG, "parseJson json exception: object: " + object + " | " + e.getMessage());
            return null;
        }

        if (TextUtils.isEmpty(appItem.pkg) && TextUtils.isEmpty(appItem.actionUrl)) {
            return null;
        }

        Context ctx = AppMasterApplication.getInstance();
        if (isApp && (TextUtils.isEmpty(appItem.pkg) || !AppUtil.appInstalled(ctx, appItem.pkg))) {
            return null;
        }

        boolean itemIsValid = valideItem(appItem);
        if (!itemIsValid) {
            return null;
        }

        return appItem;
    }

    private static boolean valideItem(BatteryAppItem appItem) {
        return appItem != null && !TextUtils.isEmpty(appItem.name)/* && !TextUtils.isEmpty(appItem.iconUrl)*/;
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    public static List<BatteryAppItem> getBatteryCallList() {
        List<BatteryAppItem> list = getBatteryListInner(KEY_CALL);

//        Intent sms = new Intent(Intent.ACTION_VIEW);
//        sms.setType("vnd.android-dir/mms-sms");
//        BatteryAppItem smsItem = queryIntentInfo(sms);
//        if (smsItem != null) {
//            list.add(0, smsItem);
//        }
//
//        Intent contact = new Intent();
//        contact.setAction(Intent.ACTION_VIEW);
//        contact.setData(Contacts.People.CONTENT_URI);
//        BatteryAppItem contactItem = queryIntentInfo(contact);
//        if (contactItem != null) {
//            list.add(0, contactItem);
//        }
//
//        Intent call = new Intent(Intent.ACTION_CALL_BUTTON);
//        BatteryAppItem callItem = queryIntentInfo(call);
//        if (callItem != null) {
//            list.add(0, callItem);
//        }
        return list;
    }

    public static List<BatteryAppItem> getBatteryNetList() {
        List<BatteryAppItem> list = getBatteryListInner(KEY_NET);

//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse("http://www.google.com"));
//        PackageManager pm = AppMasterApplication.getInstance().getPackageManager();
//        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
//        if (resolveInfos == null || resolveInfos.size() == 0) {
//            return list;
//        }
//
//        ResolveInfo chrome = null;
//        for (ResolveInfo resolveInfo : resolveInfos) {
//            if (Constants.CHROME_PACKAGE_NAME.equals(resolveInfo.activityInfo.packageName)) {
//                chrome = resolveInfo;
//                break;
//            }
//        }
//        if (chrome == null) {
//            chrome = resolveInfos.get(0);
//        }
//        BatteryAppItem appItem = new BatteryAppItem();
//        appItem.name = (String) chrome.activityInfo.loadLabel(pm);
//        appItem.pkg = chrome.activityInfo.packageName;
//        list.add(0, appItem);

        return list;
    }

    public static List<BatteryAppItem> getBatteryVideoList() {
        return getBatteryListInner(KEY_VIDEO);
    }

    private static BatteryAppItem queryIntentInfo(Intent intent) {
        PackageManager pm = AppMasterApplication.getInstance().getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        if (resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            BatteryAppItem callItem = new BatteryAppItem();
            callItem.pkg = resolveInfo.activityInfo.packageName;
            callItem.name = (String) resolveInfo.activityInfo.loadLabel(pm);

            return callItem;
        }

        return null;
    }

    private static List<BatteryAppItem> getBatteryListInner(String key) {
        if (sRecommendData != null && sRecommendData.size() > 0) {
            return sRecommendData.get(key);
        }

        loadAndParseData();
        return sRecommendData.get(key);
    }

    private static void loadAndParseData() {
        String dataString = PreferenceTable.getInstance().getString(PrefConst.KEY_SS_RECOMMEND_LIST);
        if (!TextUtils.isEmpty(dataString)) {
            try {
                JSONObject object = new JSONObject(dataString);
                parseCategoryData(object);
                return;
            } catch (JSONException e) {
                LeoLog.e(TAG, "parse category data json exception." + e.getMessage());
            } catch (RuntimeException e) {
                LeoLog.e(TAG, "parse category data RuntimeException." + e.getMessage());
            }
        }

        List<BatteryAppItem> callList = new ArrayList<BatteryAppItem>();
        List<BatteryAppItem> netList = new ArrayList<BatteryAppItem>();
        List<BatteryAppItem> videoList = new ArrayList<BatteryAppItem>();
        Context context = AppMasterApplication.getInstance();
        BufferedReader reader = null;
        try {
            InputStream inputStream = context.getAssets().open("ss_recomm");
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] array = line.split("---");

                boolean isApp = false;
                if (KEY_CALL.equals(array[0]) || KEY_VIDEO.equals(array[0])) {
                    isApp = true;
                }
                BatteryAppItem appItem = parseJson(new JSONObject(array[1]), isApp);
                if (appItem == null) {
                    continue;
                }
                if (KEY_CALL.equals(array[0])) {
                    callList.add(appItem);
                } else if (KEY_NET.equals(array[0])) {
                    netList.add(appItem);
                } else {
                    videoList.add(appItem);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(reader);
        }
        if (sRecommendData == null) {
            sRecommendData = new HashMap<String, List<BatteryAppItem>>();
        }
        sRecommendData.put(KEY_CALL, callList);
        sRecommendData.put(KEY_NET, netList);
        sRecommendData.put(KEY_VIDEO, videoList);
    }

    private static void parseCategoryData(JSONObject object) throws JSONException, RuntimeException {
        if (object == null) {
            return;
        }
        String call = null;
        call = object.getString(KEY_CALL);
        JSONArray callArray = new JSONArray(call);
        parseJsonArrayAndCache(KEY_CALL, callArray, true);

        String net = null;
        net = object.getString(KEY_NET);
        JSONArray netArray = new JSONArray(net);
        parseJsonArrayAndCache(KEY_NET, netArray, false);

        String video = null;
        video = object.getString(KEY_VIDEO);
        JSONArray videoArray = new JSONArray(video);
        int size = parseJsonArrayAndCache(KEY_VIDEO, videoArray, true);
        if (size == 0) {
            throw new RuntimeException("video list must not be empty.");
        }
    }

}
