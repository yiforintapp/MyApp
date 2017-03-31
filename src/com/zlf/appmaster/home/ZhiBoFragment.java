package com.zlf.appmaster.home;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.PrefConst;
import com.zlf.appmaster.zhibo.VideoLiveActivity;
import com.zlf.appmaster.zhibo.WordLiveActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/10/26.
 */
public class ZhiBoFragment extends BaseFragment implements View.OnClickListener {

    public static final String mOnline = "1";
    public static final String mOffline = "0";
    private RippleView mTecChenEnter;
    private RippleView mTecHuEnter;
    private RippleView mTecLiaoEnter;
    private RippleView mTecLiuEnter;
    private RippleView mTecLuoEnter;
    private RippleView mTecXieEnter;
    private TextView mTecChenStatusTv;
    private TextView mTecHuStatusTv;
    private TextView mTecLiaoStatusTv;
    private TextView mTecLiuStatusTv;
    private TextView mTecLuoStatusTv;
    private TextView mTecXieStatusTv;

    private Toast mToast;
    private View mVideoArea;

    public static final String BASE_URL = Constants.WORD_DOMAIN +
            Constants.WORD_SERVLET + Constants.WORD_ZHIBO_TITLE_MARK;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_zhibo;
    }

    @Override
    protected void onInitUI() {

        mTecChenEnter = (RippleView) findViewById(R.id.tec_c_enter);
        mTecChenEnter.setOnClickListener(this);
        mTecHuEnter = (RippleView) findViewById(R.id.tec_hu_enter);
        mTecHuEnter.setOnClickListener(this);
        mTecLiaoEnter = (RippleView) findViewById(R.id.tec_liao_enter);
        mTecLiaoEnter.setOnClickListener(this);
        mTecLiuEnter = (RippleView) findViewById(R.id.tec_liu_enter);
        mTecLiuEnter.setOnClickListener(this);
        mTecLuoEnter = (RippleView) findViewById(R.id.tec_luo_enter);
        mTecLuoEnter.setOnClickListener(this);
        mTecXieEnter = (RippleView) findViewById(R.id.tec_xie_enter);
        mTecXieEnter.setOnClickListener(this);

        mTecChenStatusTv = (TextView) findViewById(R.id.tec_name);
        mTecHuStatusTv = (TextView) findViewById(R.id.tec_name2);
        mTecLiaoStatusTv = (TextView) findViewById(R.id.tec_name3);
        mTecLiuStatusTv = (TextView) findViewById(R.id.tec_name4);
        mTecLuoStatusTv = (TextView) findViewById(R.id.tec_name5);
        mTecXieStatusTv = (TextView) findViewById(R.id.tec_name6);

        mVideoArea = findViewById(R.id.video_area);
        mVideoArea.setOnClickListener(this);

        requestData();
    }

    private void requestData() {
        UniversalRequest.requestUrlWithTimeOut("Tag", mActivity, BASE_URL,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {

                    }

                    @Override
                    public void onDataFinish(Object object) {

                        JSONObject object1 = (JSONObject) object;
                        try {
                            if (!object1.isNull("roomtec")) {
                                JSONArray jsonArray = object1.getJSONArray("roomtec");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    setRoomStatus(jsonObject);
                                }
                            }
//                            if (!object1.isNull("roomtop")) {
//                                JSONArray jsonArray = object1.getJSONArray("roomtop");
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
//                                    if ("1".equals(jsonObject.getString("room"))) {
//                                        LeoSettings.setString(PrefConst.ZHIBO_ROOM_ONE_TOPIC, jsonObject.getString("title"));
//                                    } else {
//                                        LeoSettings.setString(PrefConst.ZHIBO_ROOM_TWO_TOPIC, jsonObject.getString("title"));
//                                    }
//                                }
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, false, 0, false);
    }

    private void setRoomStatus(JSONObject jsonObject) {
        try {
            String name = jsonObject.getString("name");
            String online = jsonObject.getString("online");

            if(!name.isEmpty()){
                if(name.equals("陈老师")){
                    if(online.equals(mOffline)){
                        mTecChenStatusTv.setText(getResources().getString(R.string.tec_c_offline));
                    }
                }else if(name.equals("胡老师")){
                    if(online.equals(mOffline)){
                        mTecHuStatusTv.setText(getResources().getString(R.string.tec_hu_offline));
                    }
                }else if(name.equals("廖老师")){
                    if(online.equals(mOffline)){
                        mTecLiaoStatusTv.setText(getResources().getString(R.string.tec_liao_offline));
                    }
                }else if(name.equals("刘老师")){
                    if(online.equals(mOffline)){
                        mTecLiuStatusTv.setText(getResources().getString(R.string.tec_liu_offline));
                    }
                }else if(name.equals("骆老师")){
                    if(online.equals(mOffline)){
                        mTecLuoStatusTv.setText(getResources().getString(R.string.tec_luo_offline));
                    }
                }else if(name.equals("谢老师")){
                    if(online.equals(mOffline)){
                        mTecXieStatusTv.setText(getResources().getString(R.string.tec_xie_offline));
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.tec_c_enter:
                String titleOne = getResources().getString(R.string.zhibo_room_one_topic);
                gotoDescPage(titleOne,WordLiveActivity.TYPE_ONE);
                break;
            case R.id.tec_hu_enter:
                String titleOneTwo = getResources().getString(R.string.zhibo_room_one_topic);
                gotoDescPage(titleOneTwo,WordLiveActivity.TYPE_ONE);
                break;
            case R.id.tec_liao_enter:
                String titleTwo = getResources().getString(R.string.zhibo_room_two_topic);
                gotoDescPage(titleTwo,WordLiveActivity.TYPE_TWO);
                break;
            case R.id.tec_liu_enter:
                String titleTwoTwo= getResources().getString(R.string.zhibo_room_two_topic);
                gotoDescPage(titleTwoTwo,WordLiveActivity.TYPE_TWO);
                break;
            case R.id.tec_luo_enter:
                String titleThree= getResources().getString(R.string.zhibo_room_thr_topic);
                gotoDescPage(titleThree,WordLiveActivity.TYPE_THREE);
                break;
            case R.id.tec_xie_enter:
                String titleThreeTwo= getResources().getString(R.string.zhibo_room_thr_topic);
                gotoDescPage(titleThreeTwo,WordLiveActivity.TYPE_THREE);
                break;
            case R.id.video_area:
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, VideoLiveActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.putExtra(LoginActivity.FROM_LIVE_BTN, true);
                    startActivity(intent);
                }
                break;
        }
    }

    private void gotoDescPage(String title, String type) {
        Intent intent;
        if (AppUtil.isLogin()) {
            intent = new Intent(mActivity, WordLiveActivity.class);
            intent.putExtra(WordLiveActivity.ZHIBO_TYPE, type);
            intent.putExtra(WordLiveActivity.ZHIBO_TITLE, title);
            startActivity(intent);
        } else if (!AppUtil.isLogin()) {
            intent = new Intent(mActivity, LoginActivity.class);
            intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN, true);
            intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN_TYPE, type);
            intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN_TITLE, title);
            startActivity(intent);
        }
    }

    private void showToast(String s) {
        if (mToast == null) {
            mToast = Toast.makeText(mActivity, s, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(s);
        }
        mToast.show();
    }
}
