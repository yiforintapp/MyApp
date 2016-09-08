package com.zlf.appmaster.msgcenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.db.MsgCenterTable;
import com.zlf.appmaster.home.BaseActivity;
import com.zlf.appmaster.home.HomeMainActivity;
import com.zlf.appmaster.schedule.MsgCenterFetchJob;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.imageloader.ImageLoader;

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
    private RippleView mFeedbackTv;
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

        mFeedbackTv = (RippleView) findViewById(R.id.msg_center_feedback_tv);
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_title_back:
                finish();
                break;
            case R.id.msg_center_feedback_tv:
                Intent feedback = new Intent(this, HomeMainActivity.class);
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
