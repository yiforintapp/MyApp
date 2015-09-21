package com.leo.appmaster.msgcenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;

/**
 * 消息中心列表
 * Created by Jasper on 2015/9/10.
 */
public class MsgCenterActivity extends BaseActivity implements
        View.OnClickListener,
        AdapterView.OnItemClickListener {
    private CommonTitleBar mTitleBar;

    private ListView mMessageLv;
    private View mEmptyView;
    private TextView mFeedbackTv;
    private MsgCenterAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_center);

        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTitleBar.setTitle(R.string.msg_center_title);
        mTitleBar.setBackArrowVisibility(View.VISIBLE);
        mTitleBar.setBackViewListener(this);

        mMessageLv = (ListView) findViewById(R.id.msg_center_lv);
        mMessageLv.setOnItemClickListener(this);

        mEmptyView = findViewById(R.id.msg_center_empty_ll);

        mFeedbackTv = (TextView) findViewById(R.id.msg_center_feedback_tv);
        mFeedbackTv.setOnClickListener(this);

        mAdapter = new MsgCenterAdapter(mMessageLv, mEmptyView);
        mMessageLv.setAdapter(mAdapter);

        ThreadManager.executeOnNetworkThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterFetchJob.checkCacheAndRequest(null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_title_back:
                finish();
                break;
            case R.id.msg_center_feedback_tv:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "Infofb", "Infofb_cnts");
                Intent feedback = new Intent(this, FeedbackActivity.class);
                startActivity(feedback);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Message msg = (Message) mAdapter.getItem(position);
        if (msg == null) return;

        String eventId = null;
        if (msg.isCategoryFaq()) {
            eventId = "faq";
        } else if (msg.isCategoryActivity()) {
            eventId = "act";
        }
        if (eventId != null) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, eventId, msg.title);
        }
        MsgCenterBrowserActivity.startMsgCenterWeb(this, msg.title, msg.jumpUrl, msg.isCategoryUpdate());
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterTable table = new MsgCenterTable();
                table.readMessage(msg);
            }
        });
    }
}
