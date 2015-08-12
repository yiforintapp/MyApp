
package com.leo.appmaster.privacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.videohide.VideoHideMainActivity;

public class PrivacySuggestActivity extends BaseActivity implements OnClickListener {

    private TextView mAppLockSuggest;
    private TextView mAppLockDes;
    private TextView mHidePicSuggest;
    private TextView mHidePicDes;
    private TextView mHideVideoSuggest;
    private TextView mHideVideoDes;
    private TextView mPrivacyContactSuggest;
    private TextView mPrivacyContactDes;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_privacy_suggest);

        CommonTitleBar ttileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        ttileBar.setTitle(R.string.privacy_suggest_title);
        ttileBar.openBackView();

        View appLock = findViewById(R.id.privacy_suggest_applock);
        mAppLockSuggest = (TextView) appLock.findViewById(R.id.suggest_applock_suggest);
        mAppLockDes = (TextView) appLock.findViewById(R.id.suggest_applock_description);
        appLock.setOnClickListener(this);
        View hidePic = findViewById(R.id.privacy_suggest_hide_pic);
        mHidePicSuggest = (TextView) hidePic.findViewById(R.id.suggest_hide_pic_suggest);
        mHidePicDes = (TextView) hidePic.findViewById(R.id.suggest_hide_pic_description);
        hidePic.setOnClickListener(this);
        View hideVideo = findViewById(R.id.privacy_suggest_hide_video);
        mHideVideoSuggest = (TextView) hideVideo.findViewById(R.id.suggest_hide_video_suggest);
        mHideVideoDes = (TextView) hideVideo.findViewById(R.id.suggest_hide_video_description);
        hideVideo.setOnClickListener(this);
        View privacyContact = findViewById(R.id.privacy_suggest_privacy_contact);
        mPrivacyContactSuggest = (TextView) privacyContact
                .findViewById(R.id.suggest_privacy_contact_suggest);
        mPrivacyContactDes = (TextView) privacyContact
                .findViewById(R.id.suggest_privacy_contact_description);
        privacyContact.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        PrivacyHelper ph = PrivacyHelper.getInstance(this);
        boolean appLockActive = ph.isVariableActived(PrivacyHelper.VARABLE_APP_LOCK);
        mAppLockSuggest.setText(appLockActive ? R.string.privacy_more_app_lock
                : R.string.privacy_no_app_lock);
        mAppLockDes.setText(appLockActive ? R.string.privacy_more_app_lock_des
                : R.string.privacy_no_app_lock_des);

        boolean hidePicActive = ph.isVariableActived(PrivacyHelper.VARABLE_HIDE_PIC);
        mHidePicSuggest.setText(hidePicActive ? R.string.privacy_more_pic_hide
                : R.string.privacy_no_pic_hide);
        mHidePicDes.setText(hidePicActive ? R.string.privacy_more_pic_hide_des
                : R.string.privacy_no_pic_hide_des);

        boolean hideVideoActive = ph.isVariableActived(PrivacyHelper.VARABLE_HIDE_VIDEO);
        mHideVideoSuggest.setText(hideVideoActive ? R.string.privacy_more_video_hide
                : R.string.privacy_no_video_hide);
        mHideVideoDes.setText(hideVideoActive ? R.string.privacy_more_video_hide_des
                : R.string.privacy_no_video_hide_des);

        boolean privacyContactActive = ph.isVariableActived(PrivacyHelper.VARABLE_PRIVACY_CONTACT);
        mPrivacyContactSuggest.setText(privacyContactActive ? R.string.privacy_more_privacy_contact
                : R.string.privacy_no_privacy_contact);
        mPrivacyContactDes.setText(privacyContactActive ? R.string.privacy_more_privacy_contact_des
                : R.string.privacy_no_privacy_contact_des);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.privacy_suggest_applock:
                LockManager lm = LockManager.getInstatnce();
                LockMode curMode = LockManager.getInstatnce().getCurLockMode();
                if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
                    intent = new Intent(this, RecommentAppLockListActivity.class);
                    intent.putExtra("target", 0);
                    startActivity(intent);
                    curMode.haveEverOpened = true;
                    lm.updateMode(curMode);
                } else {
                    intent = new Intent(this, AppLockListActivity.class);
                    startActivity(intent);
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "proposals", "applock");
                }
                break;
            case R.id.privacy_suggest_hide_pic:
                intent = new Intent(this, ImageHideMainActivity.class);
                startActivity(intent);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "proposals", "hideimage");
                break;
            case R.id.privacy_suggest_hide_video:
                intent = new Intent(this, VideoHideMainActivity.class);
                startActivity(intent);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "proposals", "hidevideo");
                break;
            case R.id.privacy_suggest_privacy_contact:
                intent = new Intent(this, PrivacyContactActivity.class);
                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                        PrivacyContactUtils.TO_PRIVACY_CONTACT_FLAG);
                startActivity(intent);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "proposals", "contacts");
                break;
            default:
                break;
        }
    }

}
