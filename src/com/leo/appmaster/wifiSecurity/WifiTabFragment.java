package com.leo.appmaster.wifiSecurity;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

/**
 * @author qili
 */
public class WifiTabFragment extends Fragment {
    private static final int PING_LOST = 30 * 1000;
    public final static int CONNECT_STATUS = 1;
    public final static int SECOND_CONNECT = 2;
    public final static int PASSWORD_TYPE = 3;
    public final static int WIFI_SAFETY = 4;
    public final static int GO_TO_RESULT_PAGE = 5;
    public final static int STOP_SCAN = 6;
    public final static int CHECK_PING_STATE = 7;
    String[] PING_DEFAULT_HOST = {
            "www.google.com",
            "www.apple.com",
            "www.bing.com",
            "www.microsoft.com"
    };

    private View rootOne, rootTwo, rootThree, rootFour;
    private View mRootView;
    private WifiSecurityActivity mActivity;
    private View mOneLoad, mTwoLoad, mThreeLoad, mFourLoad;
    private ImageView mOneLoadDone, mTwoLoadDone, mThreeLoadDone, mFourLoadDone;
    private boolean isConnect;
    private WifiSecurityManager mWifiManger;
    private boolean isInteruptScan = false;
    private boolean mOneState = false;
    private boolean mTwoState = false;
    private boolean mThreeState = false;
    private boolean mFourSate = false;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            if (!isInteruptScan && mActivity != null) {
                switch (msg.what) {
                    case CONNECT_STATUS:
                        mOneState = (Boolean) msg.obj;
                        cancelLoading(CONNECT_STATUS, mOneState);
                        break;
                    case SECOND_CONNECT:
                        mTwoState = (Boolean) msg.obj;
                        cancelLoading(SECOND_CONNECT, mTwoState);
                        break;
                    case PASSWORD_TYPE:
                        int mPasswordType = (Integer) msg.obj;
                        if (mPasswordType == 0) {
                            mThreeState = false;
                            cancelLoading(PASSWORD_TYPE, mThreeState);
                        } else {
                            mThreeState = true;
                            cancelLoading(PASSWORD_TYPE, mThreeState);
                        }
                        break;
                    case WIFI_SAFETY:
                        //路由安全状态与密码状态一致
                        mFourSate = mThreeState;
                        cancelLoading(WIFI_SAFETY, mFourSate);
                        break;
                    case GO_TO_RESULT_PAGE:
                        mWifiManger.setWifiScanState(true);
                        mActivity.loadFinish();
                        break;
                    case CHECK_PING_STATE:
                        if (pingNumDone == 0 && !pingResult) {
                            mWifiManger.destoryPing();
                            stopScan();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (WifiSecurityActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mActivity != null) {
            mWifiManger = mActivity.getManger();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wifi_tab_fragment, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRootView = view;

        rootOne = mRootView.findViewById(R.id.wifi_is_connect);
        rootTwo = mRootView.findViewById(R.id.wifi_second_connect);
        rootThree = mRootView.findViewById(R.id.wifi_ssl);
        rootFour = mRootView.findViewById(R.id.wifi_pas_type);

        mOneLoad = mRootView.findViewById(R.id.wifi_is_connect_icon);
        mOneLoadDone = (ImageView) mRootView.findViewById(R.id.wifi_is_connect_icon_done);
        mTwoLoad = mRootView.findViewById(R.id.wifi_second_connect_icon);
        mTwoLoadDone = (ImageView) mRootView.findViewById(R.id.wifi_second_connect_icon_done);
        mThreeLoad = mRootView.findViewById(R.id.wifi_ssl_icon);
        mThreeLoadDone = (ImageView) mRootView.findViewById(R.id.wifi_ssl_icon_done);
        mFourLoad = mRootView.findViewById(R.id.wifi_pas_type_icon);
        mFourLoadDone = (ImageView) mRootView.findViewById(R.id.wifi_pas_type_icon_done);

        mRootView.setVisibility(View.INVISIBLE);
    }

    public void startLoading(int type) {
        if (!isInteruptScan) {
            if (type == CONNECT_STATUS) {
                rootOne.setVisibility(View.VISIBLE);
                mOneLoad.setVisibility(View.VISIBLE);
                loadData(CONNECT_STATUS);
            } else if (type == SECOND_CONNECT) {
                rootTwo.setVisibility(View.VISIBLE);
                mTwoLoad.setVisibility(View.VISIBLE);
                loadData(SECOND_CONNECT);
            } else if (type == PASSWORD_TYPE) {
                rootThree.setVisibility(View.VISIBLE);
                mThreeLoad.setVisibility(View.VISIBLE);
                loadData(PASSWORD_TYPE);
            } else {
                rootFour.setVisibility(View.VISIBLE);
                mFourLoad.setVisibility(View.VISIBLE);
                loadData(WIFI_SAFETY);
            }
        }
    }

//    public void setLoading(View view) {
//        view.setVisibility(View.VISIBLE);
//    }

    private void loadData(int type) {

        if (type == CONNECT_STATUS) {
            checkIsConnect();
        } else if (type == SECOND_CONNECT) {
            pingAll();
        } else if (type == PASSWORD_TYPE) {
            checkPassWordType();
        } else {
            checkIsSafe();
        }

    }

    private void pingAll() {

        //超过7500ms断开ping
        Message msg = new Message();
        msg.what = CHECK_PING_STATE;
        mHandler.sendMessageDelayed(msg, PING_LOST);

        for (int i = 0; i < PING_DEFAULT_HOST.length; i++) {
            String host = PING_DEFAULT_HOST[i];
            pingOneHost(host);
        }

    }

    private boolean isPingOk = false;
    private boolean pingResult = false;
    private int pingNumDone = 0;

    private void pingOneHost(final String host) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                isPingOk = mWifiManger.pingOneHost(host);
                LeoLog.d("fragmentping", "host done " + host);
                checkIsPingOK();
            }
        });
    }

    private void checkIsPingOK() {
        LeoLog.d("fragmentping", "pingNumDone " + pingNumDone);
        if (isPingOk && pingNumDone == 0) {
            mWifiManger.destoryPing();
            checkIsSecondConnect();
            pingResult = true;
            pingNumDone++;
        } else {
            LeoLog.d("fragmentping", "no go");
        }
    }

    public boolean getPingOk() {
        return pingResult;
    }
//    private boolean checkHost() {
//        //超过7500ms断开ping
//        Message msg = new Message();
//        msg.what = CHECK_PING_STATE;
//        mHandler.sendMessageDelayed(msg, 7500);
//
//        String host;
//        String hosts = PreferenceTable.getInstance().getString(PINGHOST);
//        LeoLog.d("testPingHost", "hosts is : " + hosts);
//        boolean firstPing = false;
//        if (hosts != null) {
//            String[] strings = hosts.split(";");
//            host = strings[0];
//        } else {
//            firstPing = true;
//            host = PING_DEFAULT_HOST[0];
//        }
//
////        fix
//        boolean isrightHost = isRightHost(host);
//        if (!isrightHost) {
//            host = PING_DEFAULT_HOST[0];
//            hosts = makeHosts();
//        }
//
//        LeoLog.d("testPingHost", "ping host:" + host);
//        isConnect = mWifiManger.pingOneHost(host);
//
//        if (!isConnect) {
//            if (firstPing) {
//                String newHosts = makeHosts();
//                LeoLog.d("testPingHost", "firstPing hosts : " + newHosts);
//                changeHostFirst(newHosts);
//            } else {
//                changeHostFirst(hosts);
//            }
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    private boolean isRightHost(String host) {
//        for (int i = 0; i < PING_DEFAULT_HOST.length; i++) {
//            String defult = PING_DEFAULT_HOST[i];
//            if (defult.equals(host)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private String makeHosts() {
//        String Strings = "";
//        for (int i = 0; i < PING_DEFAULT_HOST.length; i++) {
//            if (i == 0) {
//                Strings = PING_DEFAULT_HOST[0];
//            } else {
//                Strings = Strings + ";" + PING_DEFAULT_HOST[i];
//            }
//        }
//        return Strings;
//    }
//
//    private void changeHostFirst(String hosts) {
//        String[] strings = hosts.split(";");
//        String newString = "";
//        String a1 = strings[0];
//        LeoLog.d("testPingHost", "a1 is : " + a1);
//        for (int i = 0; i < strings.length; i++) {
//            if (i != 0) {
//                if (i == 1) {
//                    newString = strings[1];
//                } else {
//                    newString = newString + ";" + strings[i];
//                }
//            }
//        }
//        newString = newString + ";" + a1;
//        LeoLog.d("testPingHost", "newString is : " + newString);
//        PreferenceTable.getInstance().putString(PINGHOST, newString);
//    }

    private void checkIsSafe() {
        boolean isSafe = true;
        Message msg = new Message();
        msg.what = WIFI_SAFETY;
        msg.obj = isSafe;
        mHandler.sendMessageDelayed(msg, 1500);
    }

    /*
        SECURITY_NONE = 0;
        SECURITY_WEP = 1;
        SECURITY_PSK = 2;
        SECURITY_EAP = 3;
     */
    private void checkPassWordType() {
        int type = mWifiManger.getWifiSafety();
        Message msg = new Message();
        msg.what = PASSWORD_TYPE;
        msg.obj = type;
        mHandler.sendMessageDelayed(msg, 1500);
    }

    private void stopScan() {
        dismissTab(WifiSecurityActivity.WIFI_CHANGE);
        mActivity.showSelectWifiDialog(AppMasterApplication.getInstance().getString(R.string.can_not_connect_wifi));
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "wifi_rst", "wifi_rst_netbreak");
    }

    private void checkIsSecondConnect() {
        boolean isSecondConeect = true;
        Message msg = new Message();
        msg.what = SECOND_CONNECT;
        msg.obj = isSecondConeect;
        mHandler.sendMessageDelayed(msg, 500);
    }

    private void checkIsConnect() {
        boolean isConnect = mWifiManger.getIsWifi();
        Message msg = new Message();
        msg.what = CONNECT_STATUS;
        msg.obj = isConnect;
        mHandler.sendMessageDelayed(msg, 1500);
    }

    public void cancelLoading(int type, boolean flag) {
        if (type == CONNECT_STATUS) {
            mOneLoad.clearAnimation();
            setImage(mOneLoad, mOneLoadDone, flag);
            startLoading(SECOND_CONNECT);
        } else if (type == SECOND_CONNECT) {
            mTwoLoad.clearAnimation();
            setImage(mTwoLoad, mTwoLoadDone, flag);
            startLoading(PASSWORD_TYPE);
        } else if (type == PASSWORD_TYPE) {
            mThreeLoad.clearAnimation();
            setImage(mThreeLoad, mThreeLoadDone, flag);
            startLoading(WIFI_SAFETY);
        } else if (type == WIFI_SAFETY) {
            mFourLoad.clearAnimation();
            setImage(mFourLoad, mFourLoadDone, flag);
            loadDone();
        } else {

            mOneLoadDone.setVisibility(View.INVISIBLE);
            mTwoLoadDone.setVisibility(View.INVISIBLE);
            mThreeLoadDone.setVisibility(View.INVISIBLE);
            mFourLoadDone.setVisibility(View.INVISIBLE);

            mRootView.setVisibility(View.INVISIBLE);
        }
    }

    private void loadDone() {
        mActivity.setCanNotClick();
        Message msg = new Message();
        msg.what = GO_TO_RESULT_PAGE;
        mHandler.sendMessageDelayed(msg, 500);
    }

    public void setImage(View view, ImageView viewDone, boolean flag) {
        view.setVisibility(View.GONE);
        viewDone.setVisibility(View.VISIBLE);
        if (flag) {
            viewDone.setImageResource(R.drawable.wifi_complete);
        } else {
            viewDone.setImageResource(R.drawable.wifi_error);
        }
    }

    public void dismissTab(final int dismissType) {
        if (mRootView.getVisibility() == View.INVISIBLE) return;

        isInteruptScan = true;
        cancelLoading(0, false);
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.anim_up_to_down);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (dismissType == WifiSecurityActivity.BACK_PRESS ||
                        dismissType == WifiSecurityActivity.GO_TO_SETTING ||
                        dismissType == WifiSecurityActivity.WIFI_CHANGE) {
                    SDKWrapper.
                            addEvent(mActivity,
                                    SDKWrapper.P1, "wifi_scan", "wifi_scan_interupt");
                    mActivity.showMoveDown();
                    mWifiManger.destoryPing();
                }
                mActivity.cancelLineAnimation();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (dismissType == WifiSecurityActivity.SCAN_DONE) {
                    //goto resultFragment
                    boolean isWifiSafe = true;
                    if (!mThreeState || !mFourSate) {
                        isWifiSafe = false;
                    }

                    mActivity.scanDone(isWifiSafe, isConnect, pingResult,
                            mOneState, mTwoState, mThreeState, mFourSate);
                    mActivity.resultViewAnimation();

                } else if (dismissType == WifiSecurityActivity.BACK_PRESS) {
                    mActivity.finish();
                } else if (dismissType == WifiSecurityActivity.GO_TO_SETTING) {
                    Intent intent = new Intent(mActivity, WifiSettingActivity.class);
                    mActivity.startActivity(intent);
                }

                isPingOk = false;
                pingNumDone = 0;

                mHandler.removeCallbacksAndMessages(null);
                mActivity.setScanState(false);
                mRootView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRootView.startAnimation(animation);
    }

    public void setStartScan() {
        isInteruptScan = false;
    }

    public void showTab() {
        if (mRootView == null || mRootView.getVisibility() == View.VISIBLE) return;
        if (!isAdded()) return;

        mRootView.setVisibility(View.VISIBLE);
        setStartScan();

        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.anim_down_to_up);
        mRootView.startAnimation(animation);
        showItemAnimation();
    }

    private void showItemAnimation() {
        int screenHeight = mActivity.getWindowManager().getDefaultDisplay().getHeight();
        //1
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(rootOne,
                "y", screenHeight - rootOne.getHeight(), rootOne.getTop());
        anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                long Duration = valueAnimator.getCurrentPlayTime();
                if (Duration > 300 && rootOne.getVisibility() == View.INVISIBLE) {
                    rootOne.setVisibility(View.VISIBLE);
                }
            }
        });
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mOneLoad.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startLoading(CONNECT_STATUS);
            }
        });
        anim1.setDuration(600);
        //2
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(rootTwo,
                "y", screenHeight - rootTwo.getHeight(), rootTwo.getTop());
        anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                long Duration = valueAnimator.getCurrentPlayTime();
                if (Duration > 450 && rootTwo.getVisibility() == View.INVISIBLE) {
                    rootTwo.setVisibility(View.VISIBLE);
                }
            }
        });
        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mTwoLoad.setVisibility(View.INVISIBLE);
            }
        });
        anim2.setDuration(900);
        //3
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(rootThree,
                "y", screenHeight - rootThree.getHeight(), rootThree.getTop());
        anim3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                long Duration = valueAnimator.getCurrentPlayTime();
                if (Duration > 600 && rootThree.getVisibility() == View.INVISIBLE) {
                    rootThree.setVisibility(View.VISIBLE);
                }
            }
        });
        anim3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mThreeLoad.setVisibility(View.INVISIBLE);
            }
        });
        anim3.setDuration(1200);
        //4
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(rootFour,
                "y", screenHeight - rootFour.getHeight(), rootFour.getTop());
        anim4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                long Duration = valueAnimator.getCurrentPlayTime();
                if (Duration > 750 && rootFour.getVisibility() == View.INVISIBLE) {
                    rootFour.setVisibility(View.VISIBLE);
                }
            }
        });
        anim4.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFourLoad.setVisibility(View.INVISIBLE);
            }
        });
        anim4.setDuration(1500);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(anim1).with(anim2);
        animSet.play(anim2).with(anim3);
        animSet.play(anim3).with(anim4);
        animSet.start();
    }


    public boolean isTabShowing() {
        return mRootView.getVisibility() == View.VISIBLE;
    }

}
