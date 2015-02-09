
package com.leo.appmaster.home;

import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;

public class SplashActivity extends BaseActivity {

    public static final int MSG_LAUNCH_HOME_ACTIVITY = 1000;

    private Handler mEventHandler;
    private RelativeLayout mSplashRL;
    private ImageView mSplashButton;
    private ImageView mSplashLogo;
    private TextView mSplashUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mSplashRL = (RelativeLayout) findViewById(R.id.splash_parentRL);
        mEventHandler = new EventHandler(this);
        final AppMasterPreference pref = AppMasterPreference
                .getInstance(getApplicationContext());
        boolean splashFlag = pref.isFirstRuningPL();
        if (splashFlag) {
            mEventHandler.sendEmptyMessageDelayed(MSG_LAUNCH_HOME_ACTIVITY, 1000);
        } else {
            mSplashButton = (ImageView) findViewById(R.id.splash_logo_button);
            mSplashUrl = (TextView) findViewById(R.id.splash_url);
            mSplashUrl.setVisibility(View.VISIBLE);
            mSplashLogo = (ImageView) findViewById(R.id.imageView1);
            mSplashButton.setVisibility(View.VISIBLE);
            mSplashLogo.setVisibility(View.GONE);
            mSplashRL.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(HomeActivity.KEY_PLAY_ANIM, true);
                    SplashActivity.this.startActivity(intent);
                    SplashActivity.this.finish();
                    pref.setFirstRuningPL(true);
                }
            });
            mSplashUrl.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Uri uri = Uri
                            .parse("https://www.facebook.com/1709302419294051/photos/a.1711244589099834.1073741828.1709302419294051/1780341288856830/?type=1&theater");
                    Intent intentUri = new Intent(Intent.ACTION_VIEW, uri);
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    intent.putExtra(HomeActivity.KEY_PLAY_ANIM, true);
                    try {
                        SplashActivity.this.startActivity(intent);
                        SplashActivity.this.finish();
                        pref.setFirstRuningPL(true);
                        SplashActivity.this.startActivity(intentUri);
                    } catch (Exception e) {
                    }
                }
            });
        }
        startInitTask();
    }

    private static class EventHandler extends Handler {
        SplashActivity sa;

        public EventHandler(SplashActivity sa) {
            super();
            this.sa = sa;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LAUNCH_HOME_ACTIVITY:
                    Intent intent = new Intent(sa, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(HomeActivity.KEY_PLAY_ANIM, true);
                    sa.startActivity(intent);
                    sa.finish();
                    break;

                default:
                    break;
            }
        }
    }

    private void startInitTask() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // get recommend app lock list
                final AppMasterPreference pref = AppMasterPreference
                        .getInstance(getApplicationContext());
                long lastPull = pref.getLastLocklistPullTime();
                long interval = pref.getPullInterval();
                if (interval < (System.currentTimeMillis() - lastPull)
                        && NetWorkUtil.isNetworkAvailable(SplashActivity.this)) {
                    HttpRequestAgent.getInstance(getApplicationContext())
                            .getAppLockList(new Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response, boolean noModify) {
                                    JSONArray list;
                                    ArrayList<String> lockList = new ArrayList<String>();
                                    long next_pull;
                                    JSONObject data;
                                    try {
                                        data = response.getJSONObject("data");
                                        list = data.getJSONArray("list");
                                        for (int i = 0; i < list.length(); i++) {
                                            lockList.add(list.getString(i));
                                        }
                                        next_pull = data.getLong("next_pull");
                                        LeoLog.d("next_pull = " + next_pull
                                                + " lockList = ",
                                                lockList.toString());

                                        pref.setPullInterval(next_pull * 24
                                                * 60 * 60 * 1000);
                                        pref.setLastLocklistPullTime(System
                                                .currentTimeMillis());
                                        Intent intent = new Intent(
                                                AppLoadEngine.ACTION_RECOMMEND_LIST_CHANGE);
                                        intent.putStringArrayListExtra(
                                                Intent.EXTRA_PACKAGES, lockList);
                                        SplashActivity.this
                                                .sendBroadcast(intent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            }, new ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    LeoLog.d("Pull Lock list",
                                            error.getMessage());
                                }
                            });
                }
            }
        }).start();
    }
}
