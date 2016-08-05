package com.zlf.appmaster.stocknews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.model.news.NewsDetail;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.ui.stock.PdfReadProgressDialog;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.TimeUtil;

import java.io.File;


/**
 * 公告详情
 */
public class AnnouncementDetailActivity extends Activity {

    public static final String INTENT_FLAG_DATA = "intent_flag_data";
	
	private final static String TAG = "NewsDetailActivity";
    private Context mContext;
	private ProgressBar mProgressBar;

    private NewsFlashItem mData;
    private String mPdfPath;

    private TextView mTitleView;
    private TextView mTimeView;
    private Button mBtnShowPdfDetail;   // 查看pdf详情

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_announcement_detail);
        mContext = this;
        mData = (NewsFlashItem)getIntent().getSerializableExtra(INTENT_FLAG_DATA);

        initViews();
		
        initData();
	}
	
	private void initViews(){
        ((TextView)findViewById(R.id.title)).setText("个股公告");
        mBtnShowPdfDetail = (Button)findViewById(R.id.see_announcement_detail);
        mBtnShowPdfDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mPdfPath)){
                    new PdfReadProgressDialog(mContext, mPdfPath, String.valueOf(mData.getId())).start();
                }

            }
        });
		mProgressBar = (ProgressBar)findViewById(R.id.content_loading);

        mTitleView = (TextView)findViewById(R.id.detail_view_title);
        mTimeView = (TextView)findViewById(R.id.detail_view_time);
	}

    private void initData(){
        if (null != mData){
            mPdfPath = NewsClient.getInstance(mContext).getAnnouncementDetailPath(mData.getId());
            QLog.i(TAG, "pdf path:" + mPdfPath);

            mTitleView.setText(mData.getTitle());

            mTimeView.setText(TimeUtil.getYearAndDay(mData.getTime()));
        }

    }

    /**
     * Get PDF file Intent
     */
    public Intent getPdfFileIntent(String path){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        Uri uri = Uri.fromFile(new File(path));
        i.setDataAndType(uri, "application/pdf");
        return i;
    }

	public void setContent(NewsDetail newsDetail){

	}



	@Override
	protected void onResume() {
		super.onResume();
//		MobclickAgent.onPageStart(TAG);
//		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
//		MobclickAgent.onPageEnd(TAG); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
//		MobclickAgent.onPause(this);
	}
	

    public void onBack(View view){
        finish();
    }

}
