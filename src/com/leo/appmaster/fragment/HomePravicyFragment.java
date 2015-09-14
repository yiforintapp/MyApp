
package com.leo.appmaster.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.eventbus.event.PrivacyLevelChangeEvent;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacy.PrivacyHelper.Level;
import com.leo.appmaster.privacy.PrivacyLevelView;
import com.leo.appmaster.privacy.PrivacyProposalLayout;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;
import com.mobvista.sdk.m.core.entity.Campaign;

public class HomePravicyFragment extends BaseFragment implements OnClickListener, Selectable,
        PrivacyLevelView.ScanningListener {

    private PrivacyLevelView mPrivacyLevel;
    private View mPrivacyMessage;
    private View mPrivacyCall;
    private View mHidePic;
    private View mHideVideo;
    private View mPrivacyMessageIcon;
    private View mPrivacyCallIcon;
    private View mHidePicIcon;
    private View mHideVideoIcon;
    private TipTextView mCallLogTv, mMessageTv;
    private PrivacyProposalLayout mProposalView;
    private AppMasterPreference mPreference;
    // 广告素材
    private MobvistaEngine mAdEngine;
    private boolean isFirstOpen = false;
    public static int mPrivicyAdSwitchOpen = -1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LeoEventBus.getDefaultBus().register(this);
        mPreference = AppMasterPreference.getInstance(getActivity());

        if (mActivity != null) {
            mProposalView = (PrivacyProposalLayout) mActivity
                    .findViewById(R.id.privacy_proposal_layout);
        }
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

    @Override
    public boolean onBackPressed() {
        if (mProposalView != null && mProposalView.isActive()) {
            mProposalView.close(true);
            return true;
        }

        return super.onBackPressed();
    }

    public void onEventMainThread(PrivacyLevelChangeEvent event) {
        onLevelChange(PrivacyHelper.getInstance(mActivity).getCurLevelColor().toIntColor());
    }

    public void onEventMainThread(PrivacyEditFloatEvent event) {
        if (PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CANCEL_RED_TIP_EVENT
                .equals(event.editModel)
                || PrivacyContactUtils.PRIVACY_RECEIVER_MESSAGE_NOTIFICATION
                        .equals(event.editModel)) {
            // 短信未查看
            isShowRedTip(mMessageTv, 0);
        } else if (PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CALL_LOG_CANCEL_RED_TIP_EVENT
                .equals(event.editModel)
                || PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION
                        .equals(event.editModel)
                || PrivacyContactUtils.PRIVACY_ALL_CALL_NOTIFICATION_HANG_UP
                        .equals(event.editModel)) {
            // 通话未查看
            isShowRedTip(mCallLogTv, 1);
        }
    }

    private void onLevelChange(int color) {
        if (mPrivacyLevel != null) {
            mPrivacyLevel.invalidate(color);
            mHidePicIcon.setBackgroundColor(color);
            mHideVideoIcon.setBackgroundColor(color);
            mPrivacyMessageIcon.setBackgroundColor(color);
            mPrivacyCallIcon.setBackgroundColor(color);
            if (mProposalView != null && mProposalView.isActive()) {
                mProposalView.onLevelChange(color, false);
            }
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
        mPrivacyLevel.setScanningListener(this);

        mHidePic = findViewById(R.id.privacy_pic_layout);
        mHidePic.setOnClickListener(this);
        mHidePicIcon = mHidePic.findViewById(R.id.privacy_pic_img);

        mHideVideo = findViewById(R.id.privacy_video_layout);
        mHideVideo.setOnClickListener(this);
        mHideVideoIcon = mHideVideo.findViewById(R.id.privacy_video_img);

        mPrivacyMessage = findViewById(R.id.privacy_sms_layout);
        mPrivacyMessage.setOnClickListener(this);
        mPrivacyMessageIcon = mPrivacyMessage.findViewById(R.id.privacy_sms_img);
        mMessageTv = (TipTextView) mPrivacyMessage.findViewById(R.id.privacy_sms_text);
        isShowRedTip(mMessageTv, 0);
        mPrivacyCall = findViewById(R.id.privacy_call_layout);
        mPrivacyCall.setOnClickListener(this);
        mPrivacyCallIcon = mPrivacyCall.findViewById(R.id.privacy_call_img);
        mCallLogTv = (TipTextView) mPrivacyCall.findViewById(R.id.privacy_call_text);
        isShowRedTip(mCallLogTv, 1);
        onLevelChange(PrivacyHelper.getInstance(mActivity).getCurLevelColor().toIntColor());

        // 默认是开，记得改回默认是关
        if (mPrivicyAdSwitchOpen == -1) {
            LeoLog.d("testPrivicyAd", "获取隐私防护广告开关");
            mPrivicyAdSwitchOpen = AppMasterPreference.getInstance(getActivity())
                    .getIsADAfterPrivacyProtectionOpen();
        }

        LeoLog.d("testPrivicyAd", "开关值是：" + mPrivicyAdSwitchOpen);

        // 开启广告位
        if (mPrivicyAdSwitchOpen == 1) {
            loadAD();
        }
    }

    @Override
    public void onDestroyView() {
        if (mAdEngine != null) {
            mAdEngine.release(getActivity());
        }
        super.onDestroyView();
    }

    private void isShowRedTip(TipTextView view, int flag) {
        int cunt = 0;
        if (flag == 0) {
            // 短信未读数
            cunt = mPreference.getMessageNoReadCount();
        } else if (flag == 1) {
            // 通话未读数
            cunt = mPreference.getCallLogNoReadCount();
        }
        if (cunt > 0) {
            view.showTip(true);
        } else {
            view.showTip(false);
        }
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
                    // PrivacyContactManager.getInstance(getActivity()).initLoadData();
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
                    // PrivacyContactManager.getInstance(getActivity()).initLoadData();
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
                    mPrivacyLevel.startScanning();
                }
            default:
                break;
        }
    }

    private void loadAD() {
        LeoLog.d("testPrivicyAd", "loadAd");
        mAdEngine = MobvistaEngine.getInstance();
        mAdEngine.loadMobvista(getActivity(), new MobvistaListener() {

            @Override
            public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                if (code == MobvistaEngine.ERR_OK) {
                    LeoLog.d("testPrivicyAd", "loadAd -- OK!");
                    ImageView adicon = (ImageView) mProposalView
                            .findViewById(R.id.privacy_ad_icon);
                    loadADPic(
                            campaign.getIconUrl(),
                            new ImageSize(DipPixelUtil.dip2px(mActivity, 48), DipPixelUtil
                                    .dip2px(mActivity, 48)), adicon);

                    // 名字
                    TextView adname = (TextView) mProposalView
                            .findViewById(R.id.privacy_ad_title);
                    adname.setText(campaign.getAppName());
                    // 描述
                    TextView addesc = (TextView) mProposalView
                            .findViewById(R.id.privacy_ad_description);
                    addesc.setText(campaign.getAppDesc());
                    // call
                    TextView adcall = (TextView) mProposalView
                            .findViewById(R.id.ad_download);

                    adcall.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    adcall.setText(campaign.getAdCall());
                    mAdEngine.registerView(getActivity(), adcall);

                    View adview = mProposalView.findViewById(R.id.privacy_ad_item);
                    adview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onMobvistaClick(Campaign campaign) {
                LockManager.getInstatnce().timeFilterSelf();
            }
        });
    }

    private void loadADPic(String url, ImageSize size, final ImageView v) {
        ImageLoader.getInstance().loadImage(
                url, size, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (loadedImage != null)
                        {
                            v.setImageBitmap(loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
    }

    @Override
    public void onBackgroundChanged(int color) {
        super.onBackgroundChanged(color);
        onLevelChange(color);
    }

    @Override
    public void onSelected(int position) {
        if (mPrivacyLevel != null) {
            mPrivacyLevel.palyAnim();
        }
    }

    @Override
    public void onScrolling() {
        mPrivacyLevel.cancelAnim(true);
    }

    @Override
    public void onScanningFinish() {
        if (mProposalView != null && mActivity instanceof HomeActivity
                && ((HomeActivity) mActivity).getCurrentPage() == 1) {
            ((HomeActivity) getActivity()).setAdIconInVisible();
            ((HomeActivity) getActivity()).setEnterPrivacySuggest(true);
            Rect rect = new Rect();
            mPrivacyLevel.getLevelRectOnScreen(rect);
            mProposalView.show(rect);
        }
    }
}
