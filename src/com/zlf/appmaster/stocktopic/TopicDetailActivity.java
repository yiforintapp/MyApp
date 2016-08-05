package com.zlf.appmaster.stocktopic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.db.stock.TopicFavoriteTable;
import com.zlf.appmaster.utils.LiveRecordingUtil;

/**
 * Created by Huang on 2015/6/15.
 */
public class TopicDetailActivity extends FragmentActivity {
    public final static String INTENT_FLAG_TOPIC_ID = "intent_flag_topic_id";
    public final static String INTENT_FLAG_TOPIC_NAME = "intent_flag_topic_name";
    public final static String INTENT_FLAG_TOPIC_DAYS = "intent_flag_topic_days";
    /*  public final static  int INTENT_5TH_DAYS = 2;
    public final static  int INTENT_20TH_DAYS = 3;
    public final static  int INTENT_60TH_DAYS = 4;
    public final static  int INTENT_TODAY = 1;*/
    public final static  int INTENT_5TH_DAYS = 5;
    public final static  int INTENT_20TH_DAYS = 20;
    public final static  int INTENT_60TH_DAYS = 60;
    public final static  int INTENT_TODAY = 0;


    private final static String TAG = TopicDetailActivity.class.getSimpleName();
    private String mTopicID;
    private String mTopicName;
    private int mTopicDays;


    private TopicFavoriteTable mTopicFavoriteTable;
/*    private TextView mTopicFavoriteTV;
    private boolean mIsFavorite;
    private boolean mInitFavoriteFlag;*/
//    private LiveRecordingUtil mLiveRecordingUtil;
//    private QAVAnchorClient mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        mTopicID = getIntent().getStringExtra(INTENT_FLAG_TOPIC_ID);
        mTopicName = getIntent().getStringExtra(INTENT_FLAG_TOPIC_NAME);
        mTopicDays = getIntent().getIntExtra(INTENT_FLAG_TOPIC_DAYS, INTENT_TODAY);
//        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
        initViews();
        initData();
    }

    private void initViews(){

        ((TextView)findViewById(R.id.title)).setText(mTopicName);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new TopicDetailStockFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_FLAG_TOPIC_ID, mTopicID);
        bundle.putInt(INTENT_FLAG_TOPIC_DAYS, mTopicDays);
        fragment.setArguments(bundle);
        fragmentTransaction.add(R.id.fragment_container, fragment, "");
        fragmentTransaction.commit();


//        if(mLiveRecordingUtil.isLiveRecording()) {
//            findViewById(R.id.live_space).setVisibility(View.VISIBLE);
//        }

       /* mTopicFavoriteTV = (TextView) findViewById(R.id.tv_title_next);
        mTopicFavoriteTV.setVisibility(View.VISIBLE);
        updateFavoriteBtn();
        mTopicFavoriteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsFavorite = !mIsFavorite;
                onStockFavoriteHandle(view);
            }
        });*/
    }

    private void initData(){
        mTopicFavoriteTable = new TopicFavoriteTable(this);
       /* mInitFavoriteFlag = mTopicFavoriteTable.isFavorite(mTopicID);
        mIsFavorite = mInitFavoriteFlag;*/
    }


    public void onStockFavoriteHandle(View view) {

        /*if (mIsFavorite){
            // 数据库中增加
            mTopicFavoriteTable.addByLocal(mTopicID);
        }
        else {
            // 数据库中删除
            mTopicFavoriteTable.deleteByLocal(mTopicID);
        }

        // 更新按钮状态
        updateFavoriteBtn();*/

    }

    // 加入/删除自选按钮更新提示
    private void updateFavoriteBtn(){
      /*  if(mIsFavorite) {
            mTopicFavoriteTV.setText(R.string.stock_favorite_delete);
        }
        else {
            mTopicFavoriteTV.setText(R.string.stock_favorite_add);
        }*/
    }

    public void onBack(View v){
        goBackResult();
        finish();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        goBackResult();
        super.onBackPressed();
    }
    /**
     * 回传值
     */
    private void goBackResult(){
        /*if(mInitFavoriteFlag != mIsFavorite){// 有改变则回传该信息
            QLog.i(TAG, "mInitFavoriteFlag change");
            setResult(QConstants.RESULTCODE_UPDATE_TOPICFAVORITE);
          //  LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(TopicFavoriteFragment.ACTION_TOPIC_SELECT_CHANGED));
        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();
//        LiveOperationControlAgent.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        LiveOperationControlAgent.getInstance().onPause(this);
    }
}
