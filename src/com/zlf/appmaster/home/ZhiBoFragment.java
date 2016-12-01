package com.zlf.appmaster.home;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.zhibo.VideoLiveActivity;
import com.zlf.appmaster.zhibo.WordLiveActivity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/10/26.
 */
public class ZhiBoFragment extends BaseFragment implements View.OnClickListener {

    private RippleView mRippleLayout;
    private RippleView mWordRippleLayoutOne;
    private RippleView mWordRippleLayoutTwo;
    private TextView mTitleOne;
    private TextView mNameOne;
    private TextView mTitleTwo;
    private TextView mNameTwo;

    public static final String BASE_URL = Constants.WORD_DOMAIN +
            Constants.WORD_SERVLET + Constants.WORD_ZHIBO_TITLE_MARK;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_zhibo;
    }

    @Override
    protected void onInitUI() {
        mRippleLayout = (RippleView) findViewById(R.id.zhibo_layout);
        mRippleLayout.setOnClickListener(this);
        mWordRippleLayoutOne = (RippleView) findViewById(R.id.word_zhibo_layout_one);
        mWordRippleLayoutOne.setOnClickListener(this);
        mWordRippleLayoutTwo = (RippleView) findViewById(R.id.word_zhibo_layout_two);
        mWordRippleLayoutTwo.setOnClickListener(this);
        mTitleOne = (TextView) findViewById(R.id.word_title);
        mNameOne = (TextView) findViewById(R.id.word_person);
        mTitleTwo = (TextView) findViewById(R.id.word_title_two);
        mNameTwo = (TextView) findViewById(R.id.word_person_two);
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
                                StringBuilder roomOneTeacherBuilder = new StringBuilder();
                                StringBuilder roomTwoTeacherBuilder = new StringBuilder();
                                String name;
                                String online;
                                String room;
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    name = jsonObject.getString("name");
                                    online = jsonObject.getString("online");
                                    room = jsonObject.getString("room");
                                    if ("1".equals(room) && "1".equals(online)) {
                                        roomOneTeacherBuilder.append(name).append("、");
                                    } else if ("2".equals(room) && "1".equals(online)) {
                                        roomTwoTeacherBuilder.append(name).append(" 、");
                                    }
                                }
                                String roomOneTeacher = roomOneTeacherBuilder.toString();
                                String roomTwoTeacher = roomTwoTeacherBuilder.toString();
                                LeoSettings.setString(Constants.ZHIBO_ROOM_ONE_TEACHER,
                                        roomOneTeacher.substring(0, roomOneTeacher.length() - 1));
                                LeoSettings.setString(Constants.ZHIBO_ROOM_TWO_TEACHER,
                                        roomTwoTeacher.substring(0, roomTwoTeacher.length() - 1));
                            }
                            if (!object1.isNull("roomtop")) {
                                JSONArray jsonArray = object1.getJSONArray("roomtop");
                                for (int i = 0; i < jsonArray.length(); i ++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    if ("1".equals(jsonObject.getString("room"))) {
                                        LeoSettings.setString(Constants.ZHIBO_ROOM_ONE_TOPIC, jsonObject.getString("title"));
                                    } else {
                                        LeoSettings.setString(Constants.ZHIBO_ROOM_TWO_TOPIC, jsonObject.getString("title"));
                                    }
                                }
                            }
                            loadUI();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, false, 0, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUI();
        requestData();
    }

    private void loadUI() {
        mTitleOne.setText(LeoSettings.getString(Constants.ZHIBO_ROOM_ONE_TOPIC,
                getResources().getString(R.string.zhibo_room_one_topic)));
        mTitleTwo.setText(LeoSettings.getString(Constants.ZHIBO_ROOM_TWO_TOPIC,
                getResources().getString(R.string.zhibo_room_two_topic)));
        mNameOne.setText(LeoSettings.getString(Constants.ZHIBO_ROOM_ONE_TEACHER,
                getResources().getString(R.string.zhibo_room_one_teacher)));
        mNameTwo.setText(LeoSettings.getString(Constants.ZHIBO_ROOM_TWO_TEACHER,
                getResources().getString(R.string.zhibo_room_two_teacher)));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.zhibo_layout:
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, VideoLiveActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.putExtra(LoginActivity.FROM_LIVE_BTN, true);
                    startActivity(intent);
                }
                break;
            case R.id.word_zhibo_layout_one:
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, WordLiveActivity.class);
                    intent.putExtra(WordLiveActivity.ZHIBO_TYPE, WordLiveActivity.TYPE_ONE);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN, true);
                    intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN_TYPE, WordLiveActivity.TYPE_ONE);
                    startActivity(intent);
                }
                break;
            case R.id.word_zhibo_layout_two:
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, WordLiveActivity.class);
                    intent.putExtra(WordLiveActivity.ZHIBO_TYPE, WordLiveActivity.TYPE_TWO);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN, true);
                    intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN_TYPE, WordLiveActivity.TYPE_TWO);
                    startActivity(intent);
                }
                break;
        }
    }
}
