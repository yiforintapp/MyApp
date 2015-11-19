package com.leo.appmaster.msgcenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.PushInvoke;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView1;
import com.leo.imageloader.ImageLoader;

/**
 * 消息中心列表
 * Created by Jasper on 2015/9/10.
 */
public class MsgCenterActivity extends BaseActivity implements
        View.OnClickListener,
        AdapterView.OnItemClickListener{
    private CommonToolbar mToolbar;

    private ListView mMessageLv;
    private View mEmptyView;
    private RippleView1 mFeedbackTv;
    private MsgCenterAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_center);

        mToolbar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mToolbar.setToolbarTitle(R.string.msg_center_title);
        mToolbar.setOptionMenuVisible(false);

        mMessageLv = (ListView) findViewById(R.id.msg_center_lv);
        mMessageLv.setOnItemClickListener(this);

        mEmptyView = findViewById(R.id.msg_center_empty_ll);

        mFeedbackTv = (RippleView1) findViewById(R.id.msg_center_feedback_tv);
        mFeedbackTv.setOnClickListener(this);

        mAdapter = new MsgCenterAdapter(mMessageLv, mEmptyView);
        mMessageLv.setAdapter(mAdapter);

        ThreadManager.executeOnNetworkThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterFetchJob.checkCacheAndRequest(null);
            }
        });
        handlerIntent();
    }
    

    private void handlerIntent() {
        Intent intent=this.getIntent();
        boolean isFormPush=intent.getBooleanExtra(PushInvoke.PUSH_GOTO_MSGCENTER, false);
        if(isFormPush){
            SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh", "InfoCtr_push");
            intent.removeExtra(PushInvoke.PUSH_GOTO_MSGCENTER);
        }
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

        String descPrefix = null;
        if (msg.isCategoryFaq()) {
            descPrefix = "faq_";
        } else if (msg.isCategoryActivity()) {
            descPrefix = "act_";
        } else if (msg.isCategoryUpdate()) {
            descPrefix = "upd_";
        }
        if (descPrefix != null) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "InfoContent_cnts", descPrefix + msg.title);
        }
        MsgCenterBrowserActivity.startMsgCenterWeb(this, msg.title, msg.jumpUrl, msg.isCategoryUpdate());
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterTable table = new MsgCenterTable();
                table.readMessage(msg);
            }
        });
        try {
            final MsgCenterAdapter.MsgCenterHolder holder = (MsgCenterAdapter.MsgCenterHolder) view.getTag();
            if (holder != null) {
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.unread.setVisibility(View.GONE);
                        holder.title.setPadding(0, 0, 0, 0);
                    }
                }, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLoader.getInstance().clearMemoryCache();
    }

}
