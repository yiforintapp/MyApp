package com.zlf.appmaster.home;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.https.HttpsUtils;
import com.zlf.appmaster.login.ClientOnlineActivity;
import com.zlf.appmaster.login.FeedbackActivity;
import com.zlf.appmaster.login.InfoModifyActivity;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.setting.SettingActivity;
import com.zlf.appmaster.ui.CommonSettingItem;
import com.zlf.appmaster.update.UpdateActivity;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.PrefConst;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/19.
 */
public class PersonalFragment extends BaseFragment implements View.OnClickListener {

    private RelativeLayout mLogin;

    private CommonSettingItem mModify;
    private CommonSettingItem mFeedback;
    private CommonSettingItem mClient;
//    private CommonSettingItem mAbout;
//    private CommonSettingItem mRule;
    private CommonSettingItem mSetting;
    private CommonSettingItem mUpdate;

    private TextView mClickLogin;
    private RelativeLayout mLoginIv;
    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_personal;
    }

    @Override
    protected void onInitUI() {
        mLogin = (RelativeLayout) findViewById(R.id.login);
        mClickLogin = (TextView) mLogin.findViewById(R.id.tv_title);
        mLoginIv = (RelativeLayout) mLogin.findViewById(R.id.rl_content_tip);
        mModify = (CommonSettingItem) findViewById(R.id.modify);
//        mAbout = (CommonSettingItem) findViewById(R.id.about);
//        mRule = (CommonSettingItem) findViewById(R.id.rule);
        mSetting = (CommonSettingItem) findViewById(R.id.setting);
        mFeedback = (CommonSettingItem) findViewById(R.id.feedback);
        mUpdate = (CommonSettingItem) findViewById(R.id.update);
        mClient = (CommonSettingItem) findViewById(R.id.client);

        mClient.setIcon(R.drawable.mxxxx_icon_about);
        mFeedback.setIcon(R.drawable.menu_feedbacks_icon);
//        mAbout.setIcon(R.drawable.menu_about_icon);
        mModify.setIcon(R.drawable.ic_mine_wdzh);
//        mRule.setIcon(R.drawable.ic_mine_xx);
        mSetting.setIcon(R.drawable.ic_mine_sz);
        mUpdate.setIcon(R.drawable.icon_update);


        mClient.setTitle(mActivity.getString(R.string.client_online));
        mModify.setTitle(mActivity.getString(R.string.personal_modify));
        mFeedback.setTitle(mActivity.getString(R.string.fb_toolbar));
//        mAbout.setTitle(mActivity.getString(R.string.personal_about));
//        mRule.setTitle(mActivity.getString(R.string.personal_use));
        mSetting.setTitle(mActivity.getString(R.string.personal_setting));
        mUpdate.setTitle(mActivity.getString(R.string.check_update));

        View line_two = mSetting.findViewById(R.id.line_2);
        line_two.setVisibility(View.VISIBLE);

        setListener();
    }

    private void setListener() {
        mLogin.setOnClickListener(this);

        mModify.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, InfoModifyActivity.class);
                mActivity.startActivity(intent);
            }
        });
        mFeedback.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, FeedbackActivity.class);
                startActivity(intent);
            }
        });
        mUpdate.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });
        mSetting.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, SettingActivity.class));
//                ThreadManager.executeOnAsyncThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        doHttps();
//                    }
//                });
            }
        });
        mClient.setRippleViewOnClickLinstener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, ClientOnlineActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isLogin();
    }

    private void isLogin() {
        String userName = LeoSettings.getString(PrefConst.USER_NAME, "");
        if (!TextUtils.isEmpty(userName)) {
            mClickLogin.setText(userName);
            mLoginIv.setVisibility(View.GONE);
            mModify.setVisibility(View.VISIBLE);
            mLogin.setEnabled(false);
        } else {
            mClickLogin.setText(getResources().getString(R.string.click_login));
            mLoginIv.setVisibility(View.VISIBLE);
            mModify.setVisibility(View.GONE);
            mLogin.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.login:
                intent = new Intent(mActivity, LoginActivity.class);
                mActivity.startActivity(intent);
                break;
        }
    }

    private void checkUpdate() {
        Intent intent = new Intent(mActivity, UpdateActivity.class);
        intent.putExtra(UpdateActivity.UPDATETYPE,UpdateActivity.CHECK_UPDATE);
        startActivity(intent);
    }

    private String doHttps(){
        LeoLog.d("testHttps","doHttps");


        String result = "";
        InputStream caInput = null;
        try{
//            caInput = mActivity.getResources().getAssets().open("tomcat.cer");
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            Certificate ca;
//            try{
//                ca = cf.generateCertificate(caInput);
//            }finally {
//                caInput.close();
//            }
//            String keyStoreType = KeyStore.getDefaultType();
//            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
//            keyStore.load(null, null);
//            keyStore.setCertificateEntry("ca",ca);
//
//            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);

            HttpClient httpClient = HttpsUtils.getNewHttpClient();
//            Scheme schema = new Scheme("https", socketFactory, 443);
//            httpClient.getConnectionManager().getSchemeRegistry().register(schema);

            HttpPost httpPost = new HttpPost("https://test.com:8443/Webase_GD/appwork?proname=SSL");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("type","1"));
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = httpResponse.getEntity();
                if(entity != null){
                    LeoLog.d("testHttps","ALL GOOD");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LeoLog.d("testHttps","catch fuck");
        }

        return "";



    }

}
