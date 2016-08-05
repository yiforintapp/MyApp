package com.zlf.appmaster.stocktrade;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.utils.QLog;


/**
 * Created by Huang on 2015/5/5.
 */
public class StockTradeDetailNewsActivity extends FragmentActivity {
    private final static String TAG = StockTradeDetailNewsActivity.class.getSimpleName();
    public final static String INTENT_FLAG_STOCK_CODE ="intent_flag_stock_code";
    public final static String INTENT_FLAG_LABEL = "intent_flag_LABEL";
    public final static String INTENT_FLAG_LIVE = "intent_flag_is_live";

    private int mNewsType;
    private boolean isLiveDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        mNewsType = getIntent().getIntExtra(INTENT_FLAG_LABEL, NewsClient.STOCK_NEWS_LABEL_NEWS);
        QLog.i(TAG,"newsType:"+mNewsType);
        String titleStr = "个股新闻";
        if (mNewsType == NewsClient.STOCK_NEWS_LABEL_ANNOUNCEMENT){
            titleStr = "个股公告";
        }
        else if (mNewsType == NewsClient.STOCK_NEWS_LABEL_REPORT){
            titleStr = "个股研报";
        }

        ((TextView)findViewById(R.id.title)).setText(titleStr);
        isLiveDisplay = getIntent().getBooleanExtra(INTENT_FLAG_LIVE, false);
        if(isLiveDisplay){
            findViewById(R.id.live_space).setVisibility(View.VISIBLE);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new StockTradeDetailNewsFragment();
        fragmentTransaction.add(R.id.fragment_container, fragment, "");
        Bundle bundle = new Bundle();
        bundle.putInt(INTENT_FLAG_LABEL, mNewsType);
        fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void onBack(View view){
        finish();
    }
}
