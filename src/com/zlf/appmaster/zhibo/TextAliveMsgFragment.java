package com.zlf.appmaster.zhibo;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.TextAliveInfo;
import com.zlf.appmaster.model.WordChatItem;
import com.zlf.appmaster.model.WordNewAdviceInfo;
import com.zlf.appmaster.model.WordNewAdviceItemInfo;
import com.zlf.appmaster.ui.PinnedHeaderExpandableListView;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.ui.dialog.AdviceDialog;
import com.zlf.appmaster.ui.dialog.TextAliveAdviceDialog;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.PrefConst;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/11/29.
 */
public class TextAliveMsgFragment extends BaseFragment implements View.OnClickListener,TextAliveAdapter.ClickCallBack {
    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;
    public static final String ADMIN = "admin";

    private TextAliveAdviceDialog mDialog;

    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;
    private XListView mListView;
    private TextAliveAdapter mAdapter;
    private List<TextAliveInfo> mList;
    private View mNoPermissionLayout;
    private TextView mRemindCall;


    public static final String BASE_URL = Constants.WORD_DOMAIN +
            Constants.WORD_SERVLET + Constants.TEXT_ALIVE_ADVICE;
    private String mType;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_text_live_msg;
    }

    @Override
    protected void onInitUI() {
        initViews();
//        if (mType.equals(LeoSettings.getString(PrefConst.USER_ROOM, ""))
//                || ADMIN.equals(LeoSettings.getString(PrefConst.USER_ROOM, ""))) {
//            mNoPermissionLayout.setVisibility(View.GONE);
            initData();
//        } else {
//            mNoPermissionLayout.setVisibility(View.VISIBLE);
//            mListView.setVisibility(View.GONE);
//        }
    }

    private void initViews() {
        mRemindCall = (TextView) findViewById(R.id.remind_call);
        mRemindCall.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        mRemindCall.getPaint().setAntiAlias(true);//抗锯齿
        mRemindCall.setOnClickListener(this);
        mNoPermissionLayout = findViewById(R.id.no_permission_layout);
        mProgressBar = (CircularProgressView) findViewById(R.id.content_loading);
        mEmptyView = findViewById(R.id.empty_view);
        mRefreshView = (RippleView) findViewById(R.id.refresh_button);
        mRefreshView.setOnClickListener(this);
        mListView = (XListView) findViewById(R.id.text_live_msg_listview);
        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                requestData();
            }

            @Override
            public void onLoadMore() {
            }
        });
        mList = new ArrayList<TextAliveInfo>();
        mAdapter = new TextAliveAdapter(mActivity, mList,this);
        mListView.setAdapter(mAdapter);

    }

    private void refreshLisrByButton() {
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        requestData();
    }

    public void setType(String type) {
        this.mType = type;
    }

    private void initData() {
        mProgressBar.setVisibility(View.VISIBLE);
        requestData();
    }

    private void onLoaded(int type) {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mProgressBar.setVisibility(View.GONE);
        if (type == ERROR_TYPE) {
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * 请求数据
     */
    private void requestData() {
        String url;
        url = BASE_URL + Constants.WORD_TYPE + mType;
        UniversalRequest.requestUrlWithTimeOut("Tag", mActivity, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        onLoaded(ERROR_TYPE);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        mList.clear();
                        JSONObject jsonObject = (JSONObject) object;
                        try {
                            if (jsonObject != null) {
                                if (!jsonObject.isNull("in")) {
                                    JSONArray jsonArray = jsonObject.getJSONArray("in");
                                    parseJson(jsonArray);
                                }
                                if (mAdapter != null) {
                                    mAdapter.notifyDataSetChanged();
                                }
                                onLoaded(NORMAL_TYPE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            onLoaded(ERROR_TYPE);
                        }

                    }
                }, false, 0, false);
    }

    private void parseJson(JSONArray jsonArray) {
        try {
            TextAliveInfo info;
            Log.d("testL","jsonArray.length() : " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                info = new TextAliveInfo();
                if (!jsonObject1.isNull("username")) {
                    info.setmSendMan(jsonObject1.getString("username"));
                }
                if (!jsonObject1.isNull("deal")) {
                    info.setmPlan(jsonObject1.getString("deal"));
                }
                if (!jsonObject1.isNull("category")) {
                    info.setmPlanType(jsonObject1.getString("category"));
                }
                if (!jsonObject1.isNull("created_time")) {
                    info.setmSendTime(jsonObject1.getString("created_time"));
                }
                if (!jsonObject1.isNull("status")) {
                    info.setmStatus(jsonObject1.getString("status"));
                }
                if (!jsonObject1.isNull("enterpoint")) {
                    info.setmTargetEnter(jsonObject1.getString("enterpoint"));
                }
                if (!jsonObject1.isNull("profit_1")) {
                    info.setmProfitOne(jsonObject1.getString("profit_1"));
                }
                if (!jsonObject1.isNull("profit_2")) {
                    info.setmProfitTwo(jsonObject1.getString("profit_2"));
                }
                if (!jsonObject1.isNull("profit_3")) {
                    info.setmProfitThree(jsonObject1.getString("profit_3"));
                }
                if (!jsonObject1.isNull("lose")) {
                    info.setmLose(jsonObject1.getString("lose"));
                }
                if (!jsonObject1.isNull("profit_status_1")) {
                    info.setmProfitStatusOne(jsonObject1.getString("profit_status_1"));
                }
                if (!jsonObject1.isNull("profit_status_2")) {
                    info.setmProfitStatusTwo(jsonObject1.getString("profit_status_2"));
                }
                if (!jsonObject1.isNull("profit_status_3")) {
                    info.setmProfitStatusThree(jsonObject1.getString("profit_status_3"));
                }
                mList.add(info);
            }
            Log.d("testL","mList.size() : " + mList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh_button:
                refreshLisrByButton();
                break;
            case R.id.remind_call:
                try {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    Uri data = Uri.parse("tel:" + Constants.CLIENT_PHONE);
                    intent.setData(data);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    @Override
    public void click(View v) {
        String positionString = v.getTag().toString();
        int position = Integer.parseInt(positionString);
        mDialog = new TextAliveAdviceDialog(mActivity);
        mDialog.setInfo(mList.get(position));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
