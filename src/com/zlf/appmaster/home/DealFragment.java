package com.zlf.appmaster.home;

import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.QStringRequest;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.PrefConst;
import com.zlf.appmaster.utils.Utilities;
import com.zlf.appmaster.utils.Utils;
import com.zlf.appmaster.utils.VolleyTool;

/**
 * Created by Administrator on 2016/7/19.
 */
public class DealFragment extends BaseFragment implements View.OnClickListener {

    public final static String TAG = "DealFragment";
    public final static String CHECK_CJLH = "check_cjlh";
    public final static long TWO_DAY = 86400000 * 2;
    private TextView mTvJump;
    private String mPackageName;
    private String mDownloadUrl;


    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_deal;
    }

    @Override
    protected void onInitUI() {
        mTvJump = (TextView) findViewById(R.id.tv_changjiang);
        setListener();

        getSp();
    }

    private void getSp() {
        long mm = LeoSettings.getLong(PrefConst.KEY_CJLH_GET_TIME, 0);
        long now = System.currentTimeMillis();
        LeoLog.d(TAG, "mm : " + mm + " , now : " + now);
        if (now - mm > TWO_DAY) {
            getPckNameAndUrl();
            LeoSettings.setLong(PrefConst.KEY_CJLH_GET_TIME,now);
        } else {
            mPackageName = LeoSettings.getString(PrefConst.KEY_CJLH_PCK_NAME,Constants.CJLH_PACKAGENAME);
            mDownloadUrl = LeoSettings.getString(PrefConst.KEY_CJLH_DOWNLOAD_URL,Constants.CJLH_DOWNLOAD_URL);
            LeoLog.d(TAG, "mPackageName : " + mPackageName + " , mDownloadUrl : " + mDownloadUrl);
        }
    }

    private void getPckNameAndUrl() {
        String url = Constants.ADDRESS + Constants.SEVLET + Constants.DATA + CHECK_CJLH;
        LeoLog.d(TAG, "check update url is : " + url);

        QStringRequest stringRequest = new QStringRequest(
                Request.Method.GET, url, null, new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                LeoLog.d(TAG, "check update requestFinished version is : " + s);
                String[] strings = s.split(";");
                mPackageName = strings[0];
                mDownloadUrl = strings[1];
                if(!Utilities.isEmpty(mPackageName)){
                    LeoSettings.setString(PrefConst.KEY_CJLH_PCK_NAME,mPackageName);
                }

                if(!Utilities.isEmpty(mDownloadUrl)){
                    LeoSettings.setString(PrefConst.KEY_CJLH_DOWNLOAD_URL,mDownloadUrl);
                }
            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                LeoLog.d(TAG, "check update err");
            }

        });

        // callAll的时候使用
        VolleyTool.getInstance(mActivity).getRequestQueue()
                .add(stringRequest);
    }

    private void setListener() {
        mTvJump.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_changjiang:

               jumpToCJLH();
                break;
        }
    }

    private void jumpToCJLH() {
        Utils.startAPP(mPackageName, mActivity, mDownloadUrl);
    }


}
