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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/4.
 */
public class WordChatFragment extends BaseFragment implements View.OnClickListener {

    public static final String SEVLET_TYPE = "proname";
    public static final String SEND = "sendtextmsg";
    public static final String PARAMS_TEXT = "sendtext";
    public static final String PARAMS_NAME = "sendname";
    public static final String PARAMS_TYPE = "type";

    private static final int SHOW_NUM_PER_TIME = 20;
    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;
    public static final int LOAD_DATA_TYPE = 1;
    public static final int LOAD_MORE_TYPE = 2;
    public static final String BASE_URL = Constants.WORD_DOMAIN +
            Constants.WORD_SERVLET + Constants.WORD_CHAT_MARK;
    public static final String LOAD_DATA = BASE_URL;
    private int mNowPage = 1;
    private WordChatFragmentAdapter chatAdapter;
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

    private String name, text, type;
    private long mClickTime;


    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<WordChatFragment> mActivityReference;

        public DataHandler(WordChatFragment activity) {
            super();
            mActivityReference = new WeakReference<WordChatFragment>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WordChatFragment fragment = mActivityReference.get();
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
            item.setDate(System.currentTimeMillis() + "");
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
        return R.layout.word_chat_fragment;
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
        chatAdapter = new WordChatFragmentAdapter(mActivity);
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
        mClickTime = System.currentTimeMillis();
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
        } else {
            mNowPage += 1;
        }
        url = LOAD_DATA + mNowPage + Constants.WORD_TYPE + "1";

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
                            if (array != null && array.length() > 0) {
                                for (int i = 0; i < array.length(); i++) {
                                    ChatItem item = new ChatItem();

                                    JSONObject itemObject = array.getJSONObject(i);
                                    item.setDate(itemObject.getString("date"));
                                    String content = itemObject.getString("content");
                                    item.setText(content);
                                    item.setName(itemObject.getString("name"));

                                    LeoLog.d("testTime", "time is : " + itemObject.getString("seconds"));
                                    String time = itemObject.getString("seconds");
                                    long a;
                                    if (!Utilities.isEmpty(time)) {
                                        a = Long.valueOf(time) * 1000;
                                    } else {
                                        time = "1478502755";
                                        a = Long.valueOf(time) * 1000;
                                    }
                                    item.setDate(a + "");


                                    if (!Utilities.isEmpty(content)) {
                                        items.add(item);
                                    }
                                }


                                if (null != items) {
                                    int len = array.length();

                                    if (type == LOAD_DATA_TYPE) {
                                        if (items.size() > 0) {
                                            mDataList.clear();
                                            mListView.setVisibility(View.VISIBLE);
                                            mDataList.addAll(items);
                                            Collections.sort(mDataList, COMPARATOR);
                                            chatAdapter.setList(mDataList);
                                            chatAdapter.notifyDataSetChanged();
                                            mListView.setSelection(mDataList.size() - 1);
                                            onLoaded(NORMAL_TYPE);
                                            if (len < SHOW_NUM_PER_TIME || mNowPage >= 5) {
                                                mListView.setPullRefreshEnable(false);
                                            } else {
                                                mListView.setPullRefreshEnable(true);
                                            }
                                        } else {
                                            onLoaded(ERROR_TYPE);
                                        }

                                    } else {
                                        if (items.size() > 0) {

                                            int addBefore = mDataList.size();
                                            LeoLog.d("CHAT", "addBefore : " + addBefore);

                                            mListView.setVisibility(View.VISIBLE);
                                            mDataList.addAll(items);
                                            Collections.sort(mDataList, COMPARATOR);
                                            chatAdapter.setList(mDataList);
                                            chatAdapter.notifyDataSetChanged();
                                            mListView.setSelection(items.size());
                                            if (len < SHOW_NUM_PER_TIME || mNowPage >= 5) {
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
                            } else {
                                mListView.setPullRefreshEnable(false);
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
                long now = System.currentTimeMillis();
                if(now - mClickTime > 1000){
                    readySendMessage();
                    mClickTime = now;
                }
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
        String url = Constants.WORD_DOMAIN + "appwork";
        Map<String, String> params = new HashMap<String, String>();
        params.put(SEVLET_TYPE, SEND);

        text = mEdText.getText().toString().trim();
        name = LeoSettings.getString(PrefConst.USER_NAME, "");
        type = "1";

        params.put(PARAMS_TEXT, text);
        params.put(PARAMS_NAME, name);
        params.put(PARAMS_TYPE, type);

        LeoLog.d("CHAT", "url is : " + url);
        LeoLog.d("CHAT", "text is : " + mEdText.getText().toString().trim());
        LeoLog.d("CHAT", "name is : " + LeoSettings.getString(PrefConst.USER_NAME, ""));
        LeoLog.d("CHAT", "type is : " + type);
        int requestCode = PostStringRequestUtil.request(mActivity, url, params);
        Message message = new Message();
        message.obj = requestCode;
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
    }


//    private String getNowDate() {
//        SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date currentDate = new Date(System.currentTimeMillis());
//        String failDate = dateFormate.format(currentDate);
//        return failDate;
//    }

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
