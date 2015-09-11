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
import com.leo.appmaster.sdk.BaseActivity;
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
        mMessageLv.setEmptyView(mEmptyView);

        mFeedbackTv = (TextView) findViewById(R.id.msg_center_feedback_tv);
        mFeedbackTv.setOnClickListener(this);

        mAdapter = new MsgCenterAdapter();
        mMessageLv.setAdapter(mAdapter);
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
                Intent feedback = new Intent(this, FeedbackActivity.class);
                startActivity(feedback);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Message msg = (Message) mAdapter.getItem(position);
        if (msg == null) return;

        MsgCenterBrowserActivity.startMsgCenterWeb(this, msg.title, msg.jumpUrl);
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterTable table = new MsgCenterTable();
                table.readMessage(msg);
            }
        });
    }
}
