
package com.leo.appmaster.appmanage.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.HttpRequestAgent.RequestListener;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.extra.AppWallBean;
import com.leo.appmaster.model.extra.AppWallUrlBean;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LoadFailUtils;
import com.leo.appmaster.utils.TextFormater;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.RoundedBitmapDisplayer;

public class GameAppFragment2 extends BaseFragment implements OnRefreshListener<ListView> {
    private static final int MSG_LOAD_INIT_FAILED = 0;
    private static final int MSG_LOAD_INIT_SUCCESSED = 1;
    private static final int MSG_LOAD_MORE_SUCCESSED = 2;
//    private static final String DATAPATH = "/appmaster/appwall";
    public static final String GPPACKAGE = "com.android.vending";
    private static final String TAG = "GameAppFragment";
    private GameHandler mHandler;
    private ListView lv_game_app;
    private GameAppAdapter2 mGameAdapter;
    private View game_layout_load_error, app_game_empty_view;
    private TextView button;
    // private TextView text;
    private boolean flagGp = false;
    private DisplayImageOptions options;
    private ProgressBar mProgressBar;
    private List<AppWallBean> all;
    private List<AppWallBean> temp;
    private PullToRefreshListView mPullRefreshListView;

    private static class GameHandler extends Handler {
        WeakReference<GameAppFragment2> fragmemtHolder;

        public GameHandler(GameAppFragment2 applicaionAppFragment) {
            super();
            this.fragmemtHolder = new WeakReference<GameAppFragment2>(
                    applicaionAppFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_INIT_FAILED:
                    if (fragmemtHolder.get() != null) {
                        fragmemtHolder.get().onLoadGameAppFinish(false, null);
                    }
                    break;
                case MSG_LOAD_INIT_SUCCESSED:
                    if (fragmemtHolder.get() != null) {
                        List<AppWallBean> abc = (List<AppWallBean>) msg.obj;
                        fragmemtHolder.get().onLoadGameAppFinish(true, abc);
                    }
                    break;
                case MSG_LOAD_MORE_SUCCESSED:
                    if (fragmemtHolder.get() != null) {
                        fragmemtHolder.get().mPullRefreshListView.onRefreshComplete();
                        Toast.makeText(AppMasterApplication.getInstance(), R.string.no_more_business_app, 0)
                                .show();
                    }

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_game_app;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImageLoader.getInstance().clearMemoryCache();
    }

    private void onLoadGameAppFinish(boolean isGetData, List<AppWallBean> list) {
        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "hots", "game");
        boolean flag = false;
        if (isGetData) {
            // list.clear();
            if (list.isEmpty()) {
                mProgressBar.setVisibility(View.GONE);
                mPullRefreshListView.setVisibility(View.GONE);
                game_layout_load_error.setVisibility(View.GONE);
                app_game_empty_view.setVisibility(View.VISIBLE);
                return;
            }

            List<AppItemInfo> pkgInfos = AppLoadEngine.getInstance(
                    mActivity).getAllPkgInfo();
            List<String> pkgName = new ArrayList<String>();
            for (int i = 0; i < pkgInfos.size(); i++) {
                if (pkgInfos.get(i).packageName.equals("com.android.vending")) {
                    flagGp = true;
                }
                pkgName.add(pkgInfos.get(i).packageName);
            }
            for (int i = 0; i < list.size(); i++) {
                flag = pkgName.contains(list.get(i).getAppPackageName());
                if (!flag) {
                    all.add(list.get(i));
                }
            }
            LeoLog.d(TAG, "loadDataFinish! the ListSize is :" + all.size());
            for (int i = 0; i < all.size(); i++) {
                if (i < 20) {
                    temp.add(all.get(i));
                } else {
                    break;
                }
            }
            mProgressBar.setVisibility(View.GONE);
            mPullRefreshListView.setVisibility(View.VISIBLE);
            game_layout_load_error.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mPullRefreshListView.setVisibility(View.GONE);
            game_layout_load_error.setVisibility(View.VISIBLE);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    game_layout_load_error.setVisibility(View.GONE);
                    
                    loadGameData();
                }
            });

        }
    }

    @Override
    protected void onInitUI() {
        initUI();

        loadGameData();
    }

    private void loadGameData() {
        LoadGameLisener listener = new LoadGameLisener(this);
        HttpRequestAgent.getInstance(mActivity).loadGameData(listener, listener);
    }
    /*
     * 对系统语言上传到服务器作出理（主要对中文简体和繁体中文）
     *"zh":中文简体，”zh_(地区)“：繁体中文
     */
//        private String getPostLanguage() {
//            String requestLanguage;
//            String language = AppwallHttpUtil.getLanguage();
//            String country = AppwallHttpUtil.getCountry();
//            if ("zh".equalsIgnoreCase(language)) {
//                if ("CN".equalsIgnoreCase(country)) {
//                    requestLanguage = language;
//                } else {
//                    requestLanguage = language + "_" + country;
//                }
//            } else {
//                requestLanguage = language;
//            }
////            Log.d(Constants.RUN_TAG, "sys_language:" +requestLanguage);
//            return requestLanguage;
//        }
    private void initUI() {
        all = new ArrayList<AppWallBean>();
        temp = new ArrayList<AppWallBean>();
        mHandler = new GameHandler(this);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.photo_bg_loding)
                .showImageForEmptyUri(R.drawable.photo_bg_loding)
                .showImageOnFail(R.drawable.photo_bg_loding).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .displayer((new RoundedBitmapDisplayer(20))).build();

        // lv_game_app = (ListView) findViewById(R.id.lv_game_app);
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_game_app);
        mPullRefreshListView.setVisibility(View.GONE);
        mPullRefreshListView.setMode(Mode.PULL_FROM_END);
        mPullRefreshListView.setOnRefreshListener(this);
        lv_game_app = mPullRefreshListView.getRefreshableView();
        // Need to use the Actual ListView when registering for Context Menu
        registerForContextMenu(lv_game_app);

        mGameAdapter = new GameAppAdapter2(
                mActivity, temp);
        lv_game_app.setAdapter(mGameAdapter);

        app_game_empty_view = findViewById(R.id.app_game_empty_view);
        game_layout_load_error = findViewById(R.id.game_layout_load_error);
        button = (TextView) findViewById(R.id.restartBT);
        mProgressBar = (ProgressBar) findViewById(R.id.game_progressbar_loading);

    }

    public class GameAppAdapter2 extends BaseAdapter implements OnClickListener {
        private List<AppWallBean> apps;
        private LayoutInflater layoutInflater;

        public GameAppAdapter2(Context context, List<AppWallBean> apps) {
            this.apps = apps;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return apps != null ? apps.size() : 0;
        }

        @Override
        public Object getItem(int arg0) {

            return apps != null ? apps.get(arg0) : null;
        }

        @Override
        public long getItemId(int arg0) {

            return arg0;
        }

        class ViewHolder {
            View appwall_right;
            ImageView image;
            TextView textName, textDesc, textUrl, text_size;
            RatingBar rbRatingBar;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            ViewHolder viewHolder = null;
            if (arg1 == null) {
                viewHolder = new ViewHolder();
                arg1 = layoutInflater.inflate(R.layout.item_appwall, null);
                viewHolder.image = (ImageView) arg1
                        .findViewById(R.id.appwallIV);
                viewHolder.textName = (TextView) arg1
                        .findViewById(R.id.appwallNameTV);
                viewHolder.text_size = (TextView) arg1.findViewById(R.id.text_size);
                viewHolder.textDesc = (TextView) arg1
                        .findViewById(R.id.appwallDescTV);
                viewHolder.rbRatingBar = (RatingBar) arg1.findViewById(R.id.rbRatingBar);
                viewHolder.appwall_right = arg1.findViewById(R.id.appwall_left);
                arg1.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) arg1.getTag();
            }
            AppWallBean app = apps.get(arg0);
            viewHolder.image.setImageResource(R.drawable.backedup_icon);
            viewHolder.textName.setText(app.getAppName());
            viewHolder.text_size.setText("(" + TextFormater
                    .getSizeFromKB(app.getAppsize()) + ")");
            viewHolder.textDesc.setText(app.getAppDesc());
            viewHolder.rbRatingBar.setRating(app.getRating());
            String imageUri = app.getImage();
            ImageLoader.getInstance().displayImage(imageUri, viewHolder.image,
                    options);

            viewHolder.appwall_right.setTag(arg0 + "");
            viewHolder.appwall_right.setOnClickListener(this);
            return arg1;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.appwall_left:
                    int index = Integer.parseInt(v.getTag().toString());
                    AppWallBean appwallBean = apps.get(index);
                    List<AppWallUrlBean> urls = appwallBean.getDownload();
                    AppWallUrlBean appUrl = null;
                    List<String[]> sort = new ArrayList<String[]>();
                    String urlStr = null;
                    for (int i = 0; i < urls.size(); i++) {
                        appUrl = urls.get(i);
                        String[] tempStr = new String[2];
                        tempStr[0] = appUrl.getId();
                        tempStr[1] = appUrl.getUrl();
                        sort.add(tempStr);
                    }
                    mLockManager.filterSelfOneMinites();
                    int number = sort.size();
                    if (number >= 2) {
                        for (int i = 0; i < number; i++) {
                            try {
                                urlStr = sort.get(i)[1];
                                if (i == 0) {
                                    if (flagGp) {
                                        requestGp(mActivity, urlStr);
                                        break;
                                    } else {
                                        continue;
                                    }
                                } else {
                                    requestUrl(urlStr);
                                }
                            } catch (Exception e) {
                            }
                        }
                    } else if (number > 0 && number <= 1) {
                        urlStr = sort.get(0)[1];

                        try {
                            requestUrl(urlStr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        LeoLog.d("", "*************Not URL！");
                    }

                    /* SDK Event Mark */
                    String packageName = all.get(index).getDownload().get(0).getUrl();
                    if (packageName != null && !packageName.equals("")) {
                        SDKWrapper
                                .addEvent(mActivity, SDKWrapper.P1, "hot_cli", "game_"
                                        + packageName);
                    } else {
                        // String urlPageName =
                        // all.get(index).getDownload().get(1).getUrl();
                        // if (urlPageName != null && !urlPageName.equals("")) {
                        // String urlName = toUrlgetPackageName(urlPageName);
                        // SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                        // "home_app_rec", urlName);
                        // }
                    }
                    break;
            }
        }
    }

    public void requestUrl(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (Exception e) {          
        }
    }

    public void requestGp(Context context, String packageGp) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + packageGp));
        intent.setPackage(GPPACKAGE);
        try {
            context.startActivity(intent);
        } catch (Exception e) {          
        }
    }

    public String toUrlgetPackageName(String url) {
        return url.substring(url.lastIndexOf("?id=") + 4);
    }
    private List<AppWallBean> getJson(String data) {

        List<AppWallBean> all = new ArrayList<AppWallBean>();

        String appIcon = null;
        String appName = null;
        String appDesc = null;
        String linkUrl = null;
        String maketId = null;
        String appPackageName = null;
        long appsize = 0;
        float rating = 0;
        try {

            JSONObject jo = new JSONObject(data);
            JSONArray array = jo.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                List<AppWallUrlBean> urls = new ArrayList<AppWallUrlBean>();
                AppWallBean app = new AppWallBean();
                JSONObject json = (JSONObject) array.get(i);
                appIcon = json.getString("app_img_url");
                appName = json.getString("app_name");
                appDesc = json.getString("app_describe");
                rating = Float.parseFloat(json.getString("review_score"));
                appsize = Long.parseLong(json.getString("size"));

                JSONArray linkAddress = json.getJSONArray("linkAddress");
                for (int j = 0; j < linkAddress.length(); j++) {
                    JSONObject jsonLink = (JSONObject) linkAddress.get(j);
                    AppWallUrlBean awu = new AppWallUrlBean();
                    linkUrl = jsonLink.getString("link_url");
                    maketId = jsonLink.getString("market_id");
                    awu.setId(maketId);
                    awu.setUrl(linkUrl);
                    urls.add(awu);
                }
                try {
                    appPackageName = json.getString("app_package_name");
                    app.setAppPackageName(appPackageName);
                } catch (Exception e) {
                    e.printStackTrace();
                    appPackageName = "";
                    app.setAppPackageName(appPackageName);
                }
                app.setImage(appIcon);
                app.setAppName(appName);
                app.setAppDesc(appDesc);
                app.setDownload(urls);
                app.setAppsize(appsize);
                app.setRating(rating);
                all.add(app);
            }
        } catch (Exception e) {
        }
        return all;
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {

        sendtoLoad();
    }

    private void sendtoLoad() {
        mHandler.sendEmptyMessageDelayed(MSG_LOAD_MORE_SUCCESSED, 2000);
    }
    
    private static class LoadGameLisener extends RequestListener<GameAppFragment2> {
        
        public LoadGameLisener(GameAppFragment2 outerContext) {
            super(outerContext);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.d(TAG, "load game error." + error == null ? "" : error.getMessage());
            GameAppFragment2 outerContext = getOuterContext();
            
            Context context = AppMasterApplication.getInstance();
            LoadFailUtils.sendLoadFail(context, "games");

            if (outerContext == null) return;
            
            outerContext.mHandler.sendEmptyMessage(MSG_LOAD_INIT_FAILED);
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            LeoLog.d(TAG, "load game success. response: " + (response == null ? "" : response.toString()) +
                    " | noMidify: " + noMidify);

            GameAppFragment2 outerContext = getOuterContext();
            if (response == null) {
                Context context = AppMasterApplication.getInstance();
                LoadFailUtils.sendLoadFail(context, "games");
                if (outerContext != null) {
                    outerContext.mHandler.sendEmptyMessage(MSG_LOAD_INIT_FAILED);
                }
                return;
            } else {
                if (outerContext == null) return;

                List<AppWallBean> apps = outerContext.getJson(response.toString());
                Message msg = outerContext.mHandler.obtainMessage( MSG_LOAD_INIT_SUCCESSED, apps);
                outerContext.mHandler.sendMessage(msg);
            }
        }
        
    }

}
