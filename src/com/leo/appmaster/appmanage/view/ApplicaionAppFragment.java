
package com.leo.appmaster.appmanage.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

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

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.appmanage.HotAppActivity;
import com.leo.appmaster.appmanage.business.BusinessJsonParser;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LoadFailUtils;
import com.leo.appmaster.utils.PhoneInfoStateManager;
import com.leo.appmaster.utils.TextFormater;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ApplicaionAppFragment extends BaseFragment implements OnClickListener,
        OnRefreshListener<ListView> {
    private int mCurrentPage = 1;
    private static final int MSG_LOAD_INIT_FAILED = 0;
    private static final int MSG_LOAD_INIT_SUCCESSED = 1;
    private static final int MSG_LOAD_PAGE_DATA_FAILED = 3;
    private static final int MSG_LOAD_PAGE_DATA_SUCCESS = 4;
    private static List<BusinessItemInfo> mRecommendDatas;

    private PullToRefreshListView mPullRefreshListView;
    private TextView tv_reload_app;
    private ListView lv_application;
    private ProgressBar mProgressBar;
    private View mRecommendHolder, mErrorView, mLayoutEmptyTip;
    private EventHandler mHandler;
    private DisplayImageOptions commonOption;
    private ImageLoader mImageLoader;
    private LayoutInflater mInflater;
    private RecommendAdapter mRecommendAdapter;
    private boolean mInitLoading;
    private boolean mInitDataLoadFinish;
    private boolean mHaveInitData;

    private static class EventHandler extends Handler {
        WeakReference<ApplicaionAppFragment> fragmemtHolder;

        public EventHandler(ApplicaionAppFragment applicaionAppFragment) {
            super();
            this.fragmemtHolder = new WeakReference<ApplicaionAppFragment>(
                    applicaionAppFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_INIT_FAILED:
                    if (fragmemtHolder.get() != null) {
                        fragmemtHolder.get().mPullRefreshListView.onRefreshComplete();
                        fragmemtHolder.get().onLoadInitAppFinish(false, null);
                    }
                    break;
                case MSG_LOAD_INIT_SUCCESSED:
                    if (fragmemtHolder.get() != null) {
                        fragmemtHolder.get().mPullRefreshListView.onRefreshComplete();
                        List<BusinessItemInfo> abc = (List<BusinessItemInfo>) msg.obj;
                        // LeoLog.d("loadBusinessRecomApp",
                        // "成功啦！看看list size是多少呢 ：" + abc.size());
                        fragmemtHolder.get().onLoadInitAppFinish(true, msg.obj);
                    }
                    break;
                case MSG_LOAD_PAGE_DATA_FAILED:
                    if (fragmemtHolder.get() != null) {
                        fragmemtHolder.get().mPullRefreshListView.onRefreshComplete();
                        fragmemtHolder.get().onLoadMoreBusinessDataFinish(false,
                                null);
                    }
                    break;
                case MSG_LOAD_PAGE_DATA_SUCCESS:
                    if (fragmemtHolder.get() != null) {
                        fragmemtHolder.get().mPullRefreshListView.onRefreshComplete();
                        List<BusinessItemInfo> loadList = (List<BusinessItemInfo>) msg.obj;
                        // LeoLog.d("loadBusinessRecomApp",
                        // "上拉加载成功啦！看看list size是多少呢 ：" + loadList.size());
                        fragmemtHolder.get().onLoadMoreBusinessDataFinish(true,
                                loadList);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_application_app;
    }

    @Override
    protected void onInitUI() {
        initUI();
        // if(mRecommendDatas.size() <1){

        AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                loadInitBusinessData();
            }
        });

        // }
    }

    private void initUI() {
        mRecommendDatas = new ArrayList<BusinessItemInfo>();
        commonOption = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.photo_bg_loding)
                .showImageForEmptyUri(R.drawable.photo_bg_loding)
                .showImageOnFail(R.drawable.photo_bg_loding).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .displayer((new RoundedBitmapDisplayer(20))).build();
        // (new BitmapDisplayer() {
        // @Override
        // public void display(Bitmap arg0, ImageAware arg1, LoadedFrom arg2) {
        // arg1.setImageBitmap(arg0);
        // // lv_game_app.setAdapter(mGameAdapter);
        // mRecommendAdapter.notifyDataSetChanged();
        // }
        // }).build();

        mImageLoader = ImageLoader.getInstance();

        mHandler = new EventHandler(this);
        mInflater = LayoutInflater.from(mActivity);

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list_app);
        mPullRefreshListView.setVisibility(View.GONE);
        mPullRefreshListView.setMode(Mode.PULL_FROM_END);
        mPullRefreshListView.setOnRefreshListener(this);
        lv_application = mPullRefreshListView.getRefreshableView();
        // Need to use the Actual ListView when registering for Context Menu
        registerForContextMenu(lv_application);
        mRecommendAdapter = new RecommendAdapter();
        lv_application.setAdapter(mRecommendAdapter);

        // lv_application = (ListView) findViewById(R.id.lv_application);
        mRecommendHolder = findViewById(R.id.content_application);
        mErrorView = findViewById(R.id.app_layout_load_error);
        mLayoutEmptyTip = findViewById(R.id.app_layout_empty);
        mProgressBar = (ProgressBar) findViewById(R.id.app_progressbar_loading);
        tv_reload_app = (TextView) findViewById(R.id.tv_reload_app);
        tv_reload_app.setOnClickListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
        }
    }

    private class RecommendAdapter extends BaseAdapter implements OnClickListener {

        public RecommendAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return mRecommendDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mRecommendDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BusinessAppHolder mViewHolder = new BusinessAppHolder();
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_application_app, null);
                mViewHolder.iv_image = (ImageView) convertView
                        .findViewById(R.id.iv_application_app);
                mViewHolder.tv_textName = (TextView) convertView
                        .findViewById(R.id.tv_application_name);
                mViewHolder.app_text_size = (TextView) convertView.findViewById(R.id.app_text_size);
                mViewHolder.tv_textDesc = (TextView) convertView
                        .findViewById(R.id.tv_application_desc);
                mViewHolder.rb_RatingBar = (RatingBar) convertView
                        .findViewById(R.id.rb_application_start);
                mViewHolder.iv_application_download = convertView
                        .findViewById(R.id.iv_application_download);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (BusinessAppHolder) convertView.getTag();
            }

            BusinessItemInfo info = mRecommendDatas.get(position);
            mViewHolder.iv_image.setImageResource(R.drawable.backedup_icon);
            mViewHolder.tv_textName.setText(info.label);
            mViewHolder.app_text_size.setText("(" + TextFormater
                    .getSizeFromKB(info.appSize) + ")");
            mViewHolder.tv_textDesc.setText(info.desc);
            mViewHolder.rb_RatingBar.setRating(info.rating);
            String imageUri = info.iconUrl;
            ImageLoader.getInstance().displayImage(imageUri, mViewHolder.iv_image,
                    commonOption);

            mViewHolder.iv_application_download.setTag(position + "");
            mViewHolder.iv_application_download.setOnClickListener(this);
            return convertView;

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_application_download:

                    int index = Integer.parseInt(v.getTag().toString());
                    BusinessItemInfo bif = mRecommendDatas.get(index);
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "hots", "app_" + bif.packageName);
                    LockManager.getInstatnce().timeFilterSelf();
                    if (PhoneInfoStateManager.isGooglePlayPkg()) {
                        if (AppUtil.appInstalled(mActivity,
                                Constants.GP_PACKAGE)) {
                            try {
                                AppUtil.downloadFromGp(mActivity,
                                        bif.packageName);
                            } catch (Exception e) {
                                AppUtil.downloadFromBrowser(mActivity,
                                        bif.appDownloadUrl);
                            }
                        } else {
                            AppUtil.downloadFromBrowser(mActivity,
                                    bif.appDownloadUrl);
                        }
                    } else {
                        if (bif.gpPriority == 1) {
                            if (AppUtil.appInstalled(mActivity,
                                    Constants.GP_PACKAGE)) {
                                try {
                                    AppUtil.downloadFromGp(mActivity,
                                            bif.packageName);
                                } catch (Exception e) {
                                    AppUtil.downloadFromBrowser(mActivity,
                                            bif.appDownloadUrl);
                                }
                            } else {
                                AppUtil.downloadFromBrowser(mActivity,
                                        bif.appDownloadUrl);
                            }
                        } else {
                            AppUtil.downloadFromBrowser(mActivity,
                                    bif.appDownloadUrl);
                        }
                    }
                    // add track
                    AppLoadEngine.getInstance(mActivity)
                            .getBusinessTracker().track(bif.packageName);

                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "app_cli_pn",
                            bif.packageName);

                    if (bif.containType == BusinessItemInfo.CONTAIN_APPLIST) {
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                                "app_cli_ps", "home");
                    } else if (bif.containType == BusinessItemInfo.CONTAIN_FLOW_SORT) {
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                                "app_cli_ps", "flow");
                    } else if (bif.containType == BusinessItemInfo.CONTAIN_CAPACITY_SORT) {
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                                "app_cli_ps", "capacity");
                    } else if (bif.containType == BusinessItemInfo.CONTAIN_BUSINESS_FOLDER) {
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                                "app_cli_ps", "new");
                    }

                    mRecommendAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    class BusinessAppHolder {
        View iv_application_download;
        ImageView iv_image;
        TextView tv_textName, tv_textDesc, app_text_size;
        RatingBar rb_RatingBar;
    }

    public void onLoadInitAppFinish(boolean succeed, Object object) {
        // 成功加载，消除红点
        HotAppActivity hotapp_activity = (HotAppActivity) mActivity;
        hotapp_activity.dimissRedTip();

        mRecommendHolder.setVisibility(View.VISIBLE);
        if (succeed) {
            mPullRefreshListView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.INVISIBLE);
            mRecommendDatas.clear();
            if (object != null) {
                List<BusinessItemInfo> list = (List<BusinessItemInfo>) object;
                for (BusinessItemInfo businessItemInfo : list) {
                    if (businessItemInfo.packageName != null) {
                        if (!filterLocalApp(businessItemInfo.packageName)) {
                            mRecommendDatas.add(businessItemInfo);
                        }
                    }
                }
            }
            // mRecommendDatas.clear();
            mRecommendAdapter.notifyDataSetChanged();
            if (mRecommendDatas.isEmpty()) {
                mPullRefreshListView.setVisibility(View.GONE);
                mHaveInitData = false;
                mLayoutEmptyTip.setVisibility(View.VISIBLE);
            } else {
                mHaveInitData = true;
                mLayoutEmptyTip.setVisibility(View.INVISIBLE);
            }

            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "app_rec", "new");

        } else {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "load_failed", "hot");
            mErrorView.setVisibility(View.VISIBLE);
            mLayoutEmptyTip.setVisibility(View.INVISIBLE);
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private boolean filterLocalApp(String pkg) {
        if (pkg == null)
            return false;
        ArrayList<AppItemInfo> appDetails = AppLoadEngine
                .getInstance(mActivity).getAllPkgInfo();

        for (AppItemInfo info : appDetails) {
            if (pkg.equals(info.packageName)) {
                return true;
            }
        }
        return false;
    }

    public void loadInitBusinessData() {
        if ((mInitDataLoadFinish && mHaveInitData) || mInitLoading)
            return;
        mRecommendDatas.clear();
        mInitLoading = true;
        HttpRequestAgent.getInstance(mActivity).loadBusinessRecomApp(1, 8,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response, boolean noModify) {
                        LeoLog.d("loadBusinessRecomApp", "response = "
                                + response);
                        List<BusinessItemInfo> list = BusinessJsonParser
                                .parserJsonObject(
                                        mActivity,
                                        response,
                                        BusinessItemInfo.CONTAIN_BUSINESS_FOLDER);
                        Message msg = mHandler.obtainMessage(
                                MSG_LOAD_INIT_SUCCESSED, list);
                        mHandler.sendMessage(msg);
                        mInitDataLoadFinish = true;
                        mInitLoading = false;
                    }
                }, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LeoLog.e("loadBusinessRecomApp", "onErrorResponse = "
                                + error.getMessage());
                        mHandler.sendEmptyMessage(MSG_LOAD_INIT_FAILED);
                        mInitLoading = false;
                        LoadFailUtils.sendLoadFail(
                                ApplicaionAppFragment.this.mActivity, "hot");
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_reload_app:
                doReload();
                break;
            default:
                break;
        }
    }

    private void doReload() {
        mRecommendHolder.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                loadInitBusinessData();
            }
        });
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {

        AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                loadMoreBusiness();
            }
        });

    }

    private void loadMoreBusiness() {

        if (mInitLoading) {
            LeoLog.d("loadBusinessRecomApp", "mInitLoading=true,RefreshComplete");
            mPullRefreshListView.onRefreshComplete();
            return;
        }

        if (mInitDataLoadFinish && !mHaveInitData) {
            LeoLog.d("loadBusinessRecomApp", "loadInitBusinessData");
            loadInitBusinessData();
            return;
        }

        HttpRequestAgent.getInstance(mActivity).loadBusinessRecomApp(
                mCurrentPage + 1, 8, new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response, boolean noModify) {
                        mCurrentPage++;
                        List<BusinessItemInfo> list = BusinessJsonParser
                                .parserJsonObject(
                                        mActivity,
                                        response,
                                        BusinessItemInfo.CONTAIN_BUSINESS_FOLDER);
                        Message msg = mHandler.obtainMessage(
                                MSG_LOAD_PAGE_DATA_SUCCESS, list);
                        mHandler.sendMessage(msg);
                    }
                }, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mHandler.sendEmptyMessage(MSG_LOAD_PAGE_DATA_FAILED);
                        LoadFailUtils.sendLoadFail(
                                ApplicaionAppFragment.this.mActivity, "hot");
                    }
                });
    }

    public void onLoadMoreBusinessDataFinish(boolean succeed, Object object) {
        if (succeed) {
            List<BusinessItemInfo> list = (List<BusinessItemInfo>) object;
            if (list == null || list.isEmpty()) {
                Toast.makeText(mActivity, R.string.no_more_business_app, 0)
                        .show();
            } else {
                addMoreOnlineTheme(list);
            }
        } else {
            Toast.makeText(mActivity, R.string.network_error_msg, 0).show();
        }
    }

    public void addMoreOnlineTheme(List<BusinessItemInfo> loadList) {
        boolean add;
        boolean newTheme = false;
        for (BusinessItemInfo businessAppInfo : loadList) {
            add = true;
            for (BusinessItemInfo appInfo : mRecommendDatas) {
                if (appInfo.packageName.equals(businessAppInfo.packageName)) {
                    if (appInfo.packageName != null) {
                        if (filterLocalApp(appInfo.packageName)) {
                            add = false;
                            break;
                        }
                    }
                }
            }
            if (add) {
                newTheme = true;
                mRecommendDatas.add(businessAppInfo);
            }
        }
        if (newTheme) {
            mLayoutEmptyTip.setVisibility(View.INVISIBLE);
            mRecommendAdapter.notifyDataSetChanged();
        } else {
            mRecommendAdapter.notifyDataSetChanged();
            Toast.makeText(mActivity, R.string.no_more_theme, 0).show();
        }
    }
}
