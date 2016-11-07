package com.zlf.appmaster.zhibo;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.ChatItem;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.NetWorkUtil;
import com.zlf.appmaster.utils.PostStringRequestUtil;
import com.zlf.appmaster.utils.PrefConst;
import com.zlf.appmaster.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/15.
 */
public class ZhiBoChatFragment extends BaseFragment implements View.OnClickListener {
    public static final String SEVLET_TYPE = "proname";
    public static final String SEND = "sendmsg";
    public static final String PARAMS_TEXT = "sendtext";
    public static final String PARAMS_NAME = "sendname";
    public static final String PARAMS_TIME = "sendtime";

    private static final int SHOW_NUM_PER_TIME = 20;
    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;
    public static final int LOAD_DATA_TYPE = 1;
    public static final int LOAD_MORE_TYPE = 2;
    public static final String BASE_URL = Constants.CHAT_DOMAIN +
            Constants.CHAT_SERVLET + Constants.CHAT_MARK;
    public static final String LOAD_DATA = BASE_URL + "chat_new";
    public static final String LOAD_MORE = BASE_URL + "chat_more&page=";
    private int mNowPage = 1;
    private ChatFragmentAdapter chatAdapter;
    private List<StockIndex> mIndexData;
    private List<StockIndex> mForeignIndexData;
    private StockQuotationsClient mStockClient;

    private DataHandler mHandler;

    private XListView mListView;
    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;
    private TextView mSendButton;
    private List<ChatItem> mDataList;
    private EditText mEdText;

    private String name, text, time;


    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<ZhiBoChatFragment> mActivityReference;

        public DataHandler(ZhiBoChatFragment activity) {
            super();
            mActivityReference = new WeakReference<ZhiBoChatFragment>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ZhiBoChatFragment fragment = mActivityReference.get();
            if (fragment == null) {
                return;
            }


            int code = Integer.parseInt(msg.obj.toString());
            LeoLog.d("CHAT", "msg code is : " + code);
            fragment.makeDeal(code);
        }

    }

    private void makeDeal(int code) {
        if (code == 1) {
            mEdText.setText("");
            ChatItem item = new ChatItem();
            item.setDate(time);
            item.setText(text);
            item.setName("APP_" + name);
            mDataList.add(item);
            Collections.sort(mDataList, COMPARATOR);
            chatAdapter.setList(mDataList);
            chatAdapter.notifyDataSetChanged();
            mListView.setSelection(mDataList.size() - 1);

        } else {
            showToast(mActivity.getString(R.string.can_not_send));
        }
    }


    @Override
    protected int layoutResourceId() {
        return R.layout.zhibo_chat_fragment;
    }

    @Override
    protected void onInitUI() {
        mStockClient = StockQuotationsClient.getInstance(mActivity);
        initViews();
        initData();
    }

    private void initViews() {
        mHandler = new DataHandler(this);
        mListView = (XListView) findViewById(R.id.quotations_content_list);
        mProgressBar = (CircularProgressView) findViewById(R.id.content_loading);
        mEmptyView = findViewById(R.id.empty_view);
        mRefreshView = (RippleView) findViewById(R.id.refresh_button);
        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLisrByButton();
            }
        });

        mSendButton = (TextView) findViewById(R.id.tv_submit);
        mSendButton.setOnClickListener(this);
        mEdText = (EditText) findViewById(R.id.ed_fb);

        mIndexData = new ArrayList<StockIndex>();
        mForeignIndexData = new ArrayList<StockIndex>();
        chatAdapter = new ChatFragmentAdapter(mActivity);
        mListView.setAdapter(chatAdapter);

        mDataList = new ArrayList<ChatItem>();

        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                requestData(LOAD_MORE_TYPE);
            }

            @Override
            public void onLoadMore() {
                requestData(LOAD_DATA_TYPE);
            }
        });

        mListView.setHeaderViewReady(mActivity.getString(R.string.loading_text_release));
        mListView.setHeaderViewNormal(mActivity.getString(R.string.loading_text));
        mListView.setFooterViewNormal(mActivity.getString(R.string.pls_enter_get_new));
    }

    private void refreshLisrByButton() {
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        requestData(LOAD_DATA_TYPE);
    }

    private void initData() {
        mProgressBar.setVisibility(View.VISIBLE);
        requestData(LOAD_DATA_TYPE);
    }

    private void onLoaded(int type) {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mProgressBar.setVisibility(View.GONE);
        if (type == ERROR_TYPE) {
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 请求数据
     */
    private void requestData(final int type) {
        LeoLog.d("CHAT", "now page : " + mNowPage);
        String url;
        if (type == LOAD_DATA_TYPE) {
            mNowPage = 1;
            url = LOAD_DATA;
        } else {
            mNowPage += 1;
            url = LOAD_MORE + mNowPage;
        }
        LeoLog.d("CHAT", "request page : " + mNowPage);
        LeoLog.d("CHAT", "url : " + url);

        UniversalRequest.requestNewUrlWithTimeOut("Tag", mActivity, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        onLoaded(ERROR_TYPE);
                    }

                    @Override
                    public void onDataFinish(Object object) {

                        List<ChatItem> items = new ArrayList<ChatItem>();
                        JSONArray array = (JSONArray) object;

                        try {
                            for (int i = 0; i < array.length(); i++) {
                                ChatItem item = new ChatItem();

                                JSONObject itemObject = array.getJSONObject(i);
                                item.setDate(itemObject.getString("date"));
                                String content = itemObject.getString("content");
                                item.setText(content);
                                item.setName(itemObject.getString("name"));
                                if (!Utilities.isEmpty(content)) {
                                    items.add(item);
                                }
                            }


                            if (null != items) {
                                int len = items.size();

                                if (type == LOAD_DATA_TYPE) {
                                    if (len > 0) {
                                        mDataList.clear();
                                        mListView.setVisibility(View.VISIBLE);
                                        mDataList.addAll(items);
                                        Collections.sort(mDataList, COMPARATOR);
                                        chatAdapter.setList(mDataList);
                                        chatAdapter.notifyDataSetChanged();
                                        mListView.setSelection(mDataList.size() - 1);
                                        onLoaded(NORMAL_TYPE);
                                        if (mNowPage >= 5) {
                                            mListView.setPullRefreshEnable(false);
                                        } else {
                                            mListView.setPullRefreshEnable(true);
                                        }
                                    } else {
                                        onLoaded(ERROR_TYPE);
                                    }

                                } else {
                                    if (len > 0) {

                                        int addBefore = mDataList.size();
                                        LeoLog.d("CHAT", "addBefore : " + addBefore);

                                        mListView.setVisibility(View.VISIBLE);
                                        mDataList.addAll(items);
                                        Collections.sort(mDataList, COMPARATOR);
                                        chatAdapter.setList(mDataList);
                                        chatAdapter.notifyDataSetChanged();
                                        mListView.setSelection(len);
//                                        if (len < SHOW_NUM_PER_TIME || mNowPage >= 5) {
                                        if (mNowPage >= 5) {
                                            mListView.setPullRefreshEnable(false);
                                        } else {
                                            mListView.setPullRefreshEnable(true);
                                        }
                                    } else {
                                        mListView.setPullRefreshEnable(false);
                                    }
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


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_submit:
                readySendMessage();
                break;
        }
    }

    private void readySendMessage() {

        if (NetWorkUtil.isNetworkAvailable(mActivity)) {
            if (TextUtils.isEmpty(mEdText.getText().toString().trim())) {
                showToast(mActivity.getString(R.string.pls_enter_text));
            } else {
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        sendMessage();
                    }
                });
            }
        } else {
            showToast(mActivity.getString(R.string.can_not_send));
        }

    }

    private void sendMessage() {
//        String url = Constants.ADDRESS + "work";
//        LeoLog.d("FeedbackActivity", "url : " + url);
//
//        Map<String, String> params = new HashMap<String, String>();
//        params.put(Constants.FEEDBACK_TYPE, "feedback");
//        params.put(Constants.FEEDBACK_CONTENT, "我去 测试用的");
//        params.put(Constants.FEEDBACK_CONTACT, "535666786@qq.com");
        String url = Constants.CHAT_DOMAIN + "appwork";
        Map<String, String> params = new HashMap<String, String>();
        params.put(SEVLET_TYPE, SEND);

        text = mEdText.getText().toString().trim();
        name = LeoSettings.getString(PrefConst.USER_NAME, "");
        time = getNowDate();

        params.put(PARAMS_TEXT, text);
        params.put(PARAMS_NAME, name);
        params.put(PARAMS_TIME, time);

        LeoLog.d("CHAT", "url is : " + url);
        LeoLog.d("CHAT", "text is : " + mEdText.getText().toString().trim());
        LeoLog.d("CHAT", "name is : " + LeoSettings.getString(PrefConst.USER_NAME, ""));
        LeoLog.d("CHAT", "time is : " + getNowDate());
        int requestCode = PostStringRequestUtil.request(mActivity, url, params);
        Message message = new Message();
        message.obj = requestCode;
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
    }


    private String getNowDate() {
        SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDate = new Date(System.currentTimeMillis());
        String failDate = dateFormate.format(currentDate);
        return failDate;
    }

    private static final Comparator<ChatItem> COMPARATOR = new Comparator<ChatItem>() {
        @Override
        public int compare(ChatItem lhs, ChatItem rhs) {
            return lhs.getDate().compareTo(rhs.getDate());
        }
    };

    public void showToast(String text) {
        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }
}

