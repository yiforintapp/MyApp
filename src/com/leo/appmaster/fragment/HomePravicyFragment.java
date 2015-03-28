
package com.leo.appmaster.fragment;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyLevelChangeEvent;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacy.PrivacyHelper.Level;
import com.leo.appmaster.privacy.PrivacyLevelView;
import com.leo.appmaster.privacy.PrivacySuggestActivity;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.videohide.VideoHideMainActivity;

public class HomePravicyFragment extends BaseFragment implements OnClickListener, Selectable {

    private PrivacyLevelView mPrivacyLevel;
    private View mPrivacyMessage;
    private View mPrivacyCall;
    private View mHidePic;
    private View mHideVideo;
    private View mPrivacyMessageIcon;
    private View mPrivacyCallIcon;
    private View mHidePicIcon;
    private View mHideVideoIcon;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LeoEventBus.getDefaultBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        onScrolling();
    }
    
    public void onEventMainThread(PrivacyLevelChangeEvent event) {
        onLevelChange(PrivacyHelper.getInstance(mActivity).getCurLevelColor().toIntColor());
    }

    private void onLevelChange(int color) {
        if (mPrivacyLevel != null) {
            mPrivacyLevel.invalidate(color);
            mHidePicIcon.setBackgroundColor(color);
            mHideVideoIcon.setBackgroundColor(color);
            mPrivacyMessageIcon.setBackgroundColor(color);
            mPrivacyCallIcon.setBackgroundColor(color);
        }
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_home_privacy;
    }

    @Override
    protected void onInitUI() {
        mPrivacyLevel = (PrivacyLevelView) findViewById(R.id.privacy_level);
        mPrivacyLevel.setOnClickListener(this);

        mHidePic = findViewById(R.id.privacy_pic_layout);
        mHidePic.setOnClickListener(this);
        mHidePicIcon = mHidePic.findViewById(R.id.privacy_pic_img);

        mHideVideo = findViewById(R.id.privacy_video_layout);
        mHideVideo.setOnClickListener(this);
        mHideVideoIcon = mHideVideo.findViewById(R.id.privacy_video_img);

        mPrivacyMessage = findViewById(R.id.privacy_sms_layout);
        mPrivacyMessage.setOnClickListener(this);
        mPrivacyMessageIcon = mPrivacyMessage.findViewById(R.id.privacy_sms_img);

        mPrivacyCall = findViewById(R.id.privacy_call_layout);
        mPrivacyCall.setOnClickListener(this);
        mPrivacyCallIcon = mPrivacyCall.findViewById(R.id.privacy_call_img);

        onLevelChange(PrivacyHelper.getInstance(mActivity).getCurLevelColor().toIntColor());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        int flag = view.getId();
        Intent intent = null;
        switch (flag) {
            case R.id.privacy_sms_layout:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "primesg");
                intent = new Intent(getActivity(),
                        PrivacyContactActivity.class);
                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                        PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
                try {
                    startActivity(intent);
                    /* sdk market */
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "privacyview", "mesg");
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
                break;
            case R.id.privacy_call_layout:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "pricall");
                intent = new Intent(getActivity(),
                        PrivacyContactActivity.class);
                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                        PrivacyContactUtils.TO_PRIVACY_CALL_FLAG);
                try {
                    startActivity(intent);
                    /* sdk market */
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "privacyview", "call");
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
                break;
            case R.id.privacy_pic_layout:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "hidpic");
                intent = new Intent(getActivity(), ImageHideMainActivity.class);
                startActivity(intent);
                break;
            case R.id.privacy_video_layout:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "hidvideo");
                intent = new Intent(getActivity(), VideoHideMainActivity.class);
                startActivity(intent);
                break;
            case R.id.privacy_level:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "privacylevel");
                Level level = PrivacyHelper.getInstance(mActivity).getPrivacyLevel();
                if (level == Level.LEVEL_FIVE) {
                    Toast.makeText(mActivity, R.string.privacy_suggest_perfect_toast,
                            Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(getActivity(), PrivacySuggestActivity.class);
                    startActivity(intent);
                }
            default:
                break;
        }
    }

    @Override
    public void onBackgroundChanged(int color) {
        super.onBackgroundChanged(color);
        onLevelChange(color);
    }

    @Override
    public void onSelected() {
        if(mPrivacyLevel != null) {
            mPrivacyLevel.palyAnim();
        }
    }

    @Override
    public void onScrolling() {

    }
}
