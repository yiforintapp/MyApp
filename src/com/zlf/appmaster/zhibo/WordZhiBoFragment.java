package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.WordChatItem;
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
 * Created by Administrator on 2016/11/2.
 */
public class WordZhiBoFragment extends BaseFragment implements View.OnClickListener {

    public static final String SEVLET_TYPE = "proname";
    public static final String SEND = "text_zhibo_sendask";
    public static final String PARAMS_TEXT = "text";
    public static final String PARAMS_NAME = "username";
    public static final String PARAMS_TYPE = "type";
    public static final String PARAMS_PHONE = "phone";
    public static final String PARAMS_C_TYPE = "c_type";
    public static final String C_TYPE_ALL = "0";
    public static final String C_TYPE_O = "1";
    public static final String C_TYPE_S = "2";
    public static final String C_TYPE_C = "3";


    private WordZhiboFragmentAdapter mAdapter;
    private List<WordChatItem> mDataList;

    private XListView mListView;
    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;

    private static final int SHOW_NUM_PER_TIME = 20;
    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;
    public static final int LOAD_DATA_TYPE = 1;
    public static final int LOAD_MORE_TYPE = 2;
    public static final String BASE_URL = Constants.WORD_DOMAIN +
            Constants.WORD_SERVLET + Constants.WORD_ZHIBO_MARK;
    public static final String LOAD_DATA = BASE_URL;
    private int mNowPage = 1;

    private LinearLayout mAllLayout;
    private View mAllView;
    private LinearLayout mOilLayout;
    private View mOilView;
    private LinearLayout mSilverLayout;
    private View mSilverView;
    private LinearLayout mCopperLayout;
    private View mCopperView;

    private int mIndex;
    private TextView mSendButton;
    private EditText mEdText;
    private DataHandler mHandler;
    private String mC_Type = "0";


    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<WordZhiBoFragment> mActivityReference;

        public DataHandler(WordZhiBoFragment activity) {
            super();
            mActivityReference = new WeakReference<WordZhiBoFragment>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WordZhiBoFragment fragment = mActivityReference.get();
            if (fragment == null) {
                return;
            }


            int code = Integer.parseInt(msg.obj.toString());
            fragment.makeDeal(code);
        }

    }

    private void makeDeal(int code) {
        if (code == 1) {
            mEdText.setText("");
            InputMethodManager imm =  (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) {
                imm.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(),
                        0);
            }
            showToast(mActivity.getString(R.string.word_fragment_submit_text));
        } else {
            showToast(mActivity.getString(R.string.can_not_send));
        }
    }

    private void initViews() {

        mHandler = new DataHandler(this);
        mAllLayout = (LinearLayout) findViewById(R.id.word_all_layout);
        mAllView = (View) findViewById(R.id.word_all_view);
        mOilLayout = (LinearLayout) findViewById(R.id.word_oil_layout);
        mOilView = (View) findViewById(R.id.word_oil_view);
        mSilverLayout = (LinearLayout) findViewById(R.id.word_silver_layout);
        mSilverView = (View) findViewById(R.id.word_silver_view);
        mCopperLayout = (LinearLayout) findViewById(R.id.word_copper_layout);
        mCopperView = (View) findViewById(R.id.word_copper_view);
        mEdText = (EditText) findViewById(R.id.ed_fb);
        mSendButton = (TextView) findViewById(R.id.tv_submit);
        mSendButton.setOnClickListener(this);
        mAllLayout.setOnClickListener(this);
        mOilLayout.setOnClickListener(this);
        mSilverLayout.setOnClickListener(this);
        mCopperLayout.setOnClickListener(this);
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

        mDataList = new ArrayList<WordChatItem>();
        mAdapter = new WordZhiboFragmentAdapter(mActivity);
        mListView.setAdapter(mAdapter);


        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                requestData(LOAD_DATA_TYPE);
            }

            @Override
            public void onLoadMore() {
                requestData(LOAD_MORE_TYPE);
            }
        });


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
        } else {
            mNowPage += 1;
        }
        url = LOAD_DATA + mNowPage + Constants.WORD_TYPE + "1";
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

                        List<WordChatItem> items = new ArrayList<WordChatItem>();
                        JSONArray array = (JSONArray) object;

                        try {
                            if (array != null && array.length() > 0) {
                                for (int i = 0; i < array.length(); i++) {
                                    WordChatItem item = new WordChatItem();

                                    JSONObject itemObject = array.getJSONObject(i);
                                    item.setCName(itemObject.getString("c_name"));
                                    item.setTName(itemObject.getString("t_name"));
                                    item.setMsg(itemObject.getString("msg"));
                                    item.setAnswer(itemObject.getString("answer"));
                                    String askTime = itemObject.getString("ask_time");
                                    item.setAskTime(String.valueOf(askTime));
                                    if (!TextUtils.isEmpty(askTime)) {
                                        long a = Long.valueOf(askTime) * 1000;
                                        item.setAskTime(String.valueOf(a));
                                    }
                                    String answerTime = itemObject.getString("answer_time");
                                    long a;
                                    if (!Utilities.isEmpty(answerTime)) {
                                        a = Long.valueOf(answerTime) * 1000;
                                    } else {
                                        answerTime = "1478502755";
                                        a = Long.valueOf(answerTime) * 1000;
                                    }
                                    item.setAnswerTime(String.valueOf(a));
                                    items.add(item);
                                }

                                if (null != items) {
                                    int len = items.size();

                                    if (type == LOAD_DATA_TYPE) {
                                        if (len > 0) {
                                            mDataList.clear();
                                            mListView.setVisibility(View.VISIBLE);
                                            mDataList.addAll(items);
                                            Collections.sort(mDataList, COMPARATOR);
                                            mAdapter.setList(mDataList);
                                            mAdapter.notifyDataSetChanged();
                                            mListView.setSelection(0);
                                            onLoaded(NORMAL_TYPE);
                                            if (len < SHOW_NUM_PER_TIME || mNowPage >= 5) {
                                                mListView.setPullLoadEnable(false);
                                            } else {
                                                mListView.setPullLoadEnable(true);
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
                                            mAdapter.setList(mDataList);
                                            mAdapter.notifyDataSetChanged();
                                            mListView.setSelection(mDataList.size() - items.size());
                                            if (len < SHOW_NUM_PER_TIME || mNowPage >= 5) {
                                                mListView.setPullLoadEnable(false);
                                            } else {
                                                mListView.setPullLoadEnable(true);
                                            }
                                        } else {
                                            mListView.setPullLoadEnable(false);
                                        }
                                    }
                                    onLoaded(NORMAL_TYPE);
                                }
                            } else {
                                mListView.setPullLoadEnable(false);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.word_all_layout:
                if (mIndex == 0) {
                    return;
                }
                changeTabBg(0);
                break;
            case R.id.word_oil_layout:
                if (mIndex == 1) {
                    return;
                }
                changeTabBg(1);
                break;
            case R.id.word_silver_layout:
                if (mIndex == 2) {
                    return;
                }
                changeTabBg(2);
                break;
            case R.id.word_copper_layout:
                if (mIndex == 3) {
                    return;
                }
                changeTabBg(3);
                break;
            case R.id.tv_submit:
                readySendMessage();
                break;
        }
    }

    private void changeTabBg(int position) {
        switch (position) {
            case 0:
                mAllView.setVisibility(View.VISIBLE);
                changeUnSelectBg(mIndex);
                mIndex = 0;
                mC_Type = C_TYPE_ALL;
                break;
            case 1:
                mOilView.setVisibility(View.VISIBLE);
                changeUnSelectBg(mIndex);
                mIndex = 1;
                mC_Type = C_TYPE_O;
                break;
            case 2:
                mSilverView.setVisibility(View.VISIBLE);
                changeUnSelectBg(mIndex);
                mIndex = 2;
                mC_Type = C_TYPE_S;
                break;
            case 3:
                mCopperView.setVisibility(View.VISIBLE);
                changeUnSelectBg(mIndex);
                mIndex = 3;
                mC_Type = C_TYPE_C;
                break;
        }
    }

    private void changeUnSelectBg(int position) {
        switch (position) {
            case 0:
                mAllView.setVisibility(View.INVISIBLE);
                break;
            case 1:
                mOilView.setVisibility(View.INVISIBLE);
                break;
            case 2:
                mSilverView.setVisibility(View.INVISIBLE);
                break;
            case 3:
                mCopperView.setVisibility(View.INVISIBLE);
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

        String text = mEdText.getText().toString().trim();
        String name = LeoSettings.getString(PrefConst.USER_NAME, "");
        String phone = LeoSettings.getString(PrefConst.USER_PHONE, "");
        String type = "1";
        String c_type = mC_Type;

        params.put(PARAMS_TEXT, text);
        params.put(PARAMS_NAME, name);
        params.put(PARAMS_TYPE, type);
        params.put(PARAMS_PHONE, phone);
        params.put(PARAMS_C_TYPE, c_type);

        LeoLog.d("CHAT", "url is : " + url);
        LeoLog.d("CHAT", "text is : " + text);
        LeoLog.d("CHAT", "name is : " + name);
        LeoLog.d("CHAT", "phone is : " + phone);

        int requestCode = PostStringRequestUtil.request(mActivity, url, params);
        Message message = new Message();
        message.obj = requestCode;
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
    }

    public void showToast(String text) {
        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_zhibo_word;
    }

    @Override
    protected void onInitUI() {
        initViews();
        initData();
    }

    private static final Comparator<WordChatItem> COMPARATOR = new Comparator<WordChatItem>() {
        @Override
        public int compare(WordChatItem lhs, WordChatItem rhs) {

            return rhs.getAnswerTime().compareTo(lhs.getAnswerTime());
        }
    };
}
