package com.leo.appmaster.callfilter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.mgr.CallFilterManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;
import com.leo.appmaster.ui.showTrafficTip;
import com.leo.appmaster.ui.showTrafficTip.OnDiaogClickListener;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class AskAddToBlacklistActivity extends BaseActivity {
    private final String  TAG = "AskAddToBlacklistActivity";
    private CallFilterManager mCmp;
    private MultiChoicesWitchSummaryDialog mDialogAskAddWithSmrMark;
    private MultiChoicesWitchSummaryDialog mDialogAskAddWithSmr;
    private LEOAlarmDialog mDialogAskAdd;
    private showTrafficTip mFlowTipDialog;
    private MultiChoicesWitchSummaryDialog mDialogTooShort;
    private String mPhoneNumber = "";
    public static final int TYPE_SHOW_NO_OFFHOOK = 1;
    public static final int TYPE_SHOW_ASK_WITHOUT_MARK = 2;
    public static final int TYPE_SHOW_ASK_WITH_MARK = 3;
    public static final int TYPE_SHOW_TOO_SHORT = 4;
    public static final String EXTRA_NUMBER = "number";
    public static final String EXTRA_FILTERTYPE_ARRAY = "filterTip";
    public static final String EXTRA_WHAT_TO_SHOW = "which";
    public static final String EXTRA_FLOW_TIPS = "flow_tips";
    public static final int CASE_ASK_WHEN_NO_OFFHOOK = 1;
    public static final int CASE_ASK_WITHOUT_MARK = 2;
    public static final int CASE_ASK_WITH_MARK = 3;
    public static final int CASE_ASK_WHEN_TOO_SHORT = 4;
    public static final int CASE_ALERT_FLOW = 5;

    private LEOAlarmDialog mShareDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_add_to_blacklist);
        mCmp = (CallFilterManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        LeoLog.i(TAG, "on Create");
        Intent intent = getIntent();
        handleIntent(intent);
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        handleIntent(intent);
//    }
    
    private void handleIntent(Intent intent) {
        mPhoneNumber = intent.getStringExtra(EXTRA_NUMBER);
        int intExtra = intent.getIntExtra(EXTRA_WHAT_TO_SHOW, -1);
        int[] filterTip = intent.getIntArrayExtra(EXTRA_FILTERTYPE_ARRAY);
        String flowTip = intent.getStringExtra(EXTRA_FLOW_TIPS);
        switch (intExtra) {
            case CASE_ASK_WHEN_NO_OFFHOOK:
                showAskAddWhenNoOffHook(filterTip);
                break;
            case CASE_ASK_WITHOUT_MARK:
                showAskAddBlackWithoutMark(filterTip);
                break;
            case CASE_ASK_WITH_MARK:
                showAskAddBlackWithMark(filterTip);
                break;
            case CASE_ASK_WHEN_TOO_SHORT:
                showTooShortDialog();
                break;
            case CASE_ALERT_FLOW:
                showFlowAlertDialog(flowTip);
                LeoLog.i("testtt", "show flow tip");
                break;
            default:
                this.finish();
                break;
        }
    }

    @Override
    public void finish() {
        LeoLog.i("testdata", "finished");
        super.finish();
    }

    private void showFlowAlertDialog(String tip) {
        if (mFlowTipDialog == null) {
            mFlowTipDialog = new showTrafficTip(this);
        }
        if (mFlowTipDialog.isShowing()) {
            return;
        }
        mFlowTipDialog.setTitle(R.string.traffic_used_lot);
        mFlowTipDialog.setContent(tip);
        mFlowTipDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AskAddToBlacklistActivity.this.finish();
            }
        });
        mFlowTipDialog.setOnClickListener(new OnDiaogClickListener() {
          @Override
          public void onClick(int which) {
              if (which == 0) {
                  SDKWrapper.addEvent(AskAddToBlacklistActivity.this, SDKWrapper.P1, "datapage", "data_cnts_notify");
                  // 关闭网络
                  setMobileNetUnable();
              }
              mFlowTipDialog.cancel();
          }
      });
        mFlowTipDialog.show();
    }
    
    private void showTooShortDialog() {
        long durationMax = mCmp.getCallDurationMax();
        mDialogTooShort = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(mPhoneNumber, this, true, 0, false);
        mDialogTooShort.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AskAddToBlacklistActivity.this.finish();
            }
        });
        String summaryF = String.format(getResources().getString(R.string.call_filter_ask_add_to_blacklist), (int) (Math.ceil(durationMax / 1000)));
        mDialogTooShort.setContent(summaryF);
        mDialogTooShort.setRightBtnListener(new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                BlackListInfo info = new BlackListInfo();
                String name = PrivacyContactUtils.getContactNameFromNumber(getContentResolver(), mPhoneNumber);
                if (!Utilities.isEmpty(name)) {
                    info.name = name;
                }
                int nowItemPosition = mDialogTooShort.getNowItemPosition();
                info.number = mPhoneNumber;
                switch (nowItemPosition) {
                    case 0:
                        info.markType = CallFilterConstants.MK_CRANK;
                        break;
                    case 1:
                        info.markType = CallFilterConstants.MK_ADVERTISE;
                        break;
                    case 2:
                        info.markType = CallFilterConstants.MK_FRAUD;
                        break;
                    default:
                        break;
                }
                LeoLog.i("asdfasdfasfdasdf",mPhoneNumber+":markType="+ info.markType);
                infost.add(info);
                boolean inerFlag = mCmp.addBlackList(infost, false);
                if (!inerFlag) {
                    CallFilterHelper cm = CallFilterHelper.getInstance(AskAddToBlacklistActivity.this);
                    cm.addBlackFailTip();
                }
                notiUpdateBlackList();
                Toast.makeText(AskAddToBlacklistActivity.this, getResources().getString(R.string.mark_number_from_list), Toast.LENGTH_SHORT).show();
                mDialogTooShort.dismiss();
                AskAddToBlacklistActivity.this.finish();
                showShareDialog();
            }
        });
        mDialogTooShort.show();
    }

    private void showAskAddBlackWithMark(int[] filterTip) {
        mDialogAskAddWithSmrMark = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(mPhoneNumber, this, true, 0, false);
        mDialogAskAddWithSmrMark.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AskAddToBlacklistActivity.this.finish();
            }
        });
        String summaryS = this.getResources().getString(R.string.call_filter_confirm_ask_mark_summary);
        String mark = this.getResources().getString(R.string.call_filter_black_list_tab);
        switch (filterTip[3]) {
            case CallFilterConstants.MK_CRANK:
                mark = this.getResources().getString(R.string.call_filter_mark_as_sr);
                break;
            case CallFilterConstants.MK_ADVERTISE:
                mark = this.getResources().getString(R.string.call_filter_mark_as_tx);
                break;
            case CallFilterConstants.MK_FRAUD:
                mark = this.getResources().getString(R.string.call_filter_mark_as_zp);
                break;
            default:
                break;
        }
        String summaryF = String.format(summaryS, filterTip[2], mark);
        mDialogAskAddWithSmrMark.setContent(summaryF);
        mDialogAskAddWithSmrMark.setRightBtnListener(new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                BlackListInfo info = new BlackListInfo();
                String name = PrivacyContactUtils.getContactNameFromNumber(getContentResolver(), mPhoneNumber);
                if (!Utilities.isEmpty(name)) {
                    info.name = name;
                }
                info.markType = CallFilterConstants.MK_BLACK_LIST;
                int nowItemPosition = mDialogAskAddWithSmrMark.getNowItemPosition();
                info.number = mPhoneNumber;
                switch (nowItemPosition) {
                    case 0:
                        info.markType = CallFilterConstants.MK_CRANK;
                        break;
                    case 1:
                        info.markType = CallFilterConstants.MK_ADVERTISE;
                        break;
                    case 2:
                        info.markType = CallFilterConstants.MK_FRAUD;
                        break;
                    default:
                        break;
                }
                infost.add(info);
                boolean inerFlag = mCmp.addBlackList(infost, false);
                if (!inerFlag) {
                    CallFilterHelper cm = CallFilterHelper.getInstance(AskAddToBlacklistActivity.this);
                    cm.addBlackFailTip();
                }
                notiUpdateBlackList();
                Toast.makeText(AskAddToBlacklistActivity.this, getResources().getString(R.string.mark_number_from_list), Toast.LENGTH_SHORT).show();
                mDialogAskAddWithSmrMark.dismiss();
                SDKWrapper.addEvent(AskAddToBlacklistActivity.this, SDKWrapper.P1, "block", "calling_mark&block");
                AskAddToBlacklistActivity.this.finish();
                showShareDialog();
            }
        });
        mDialogAskAddWithSmrMark.show();
    }


    private void showAskAddBlackWithoutMark(int[] filterTip) {
        mDialogAskAddWithSmr = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(mPhoneNumber, this, true, 0, false);
        mDialogAskAddWithSmr.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AskAddToBlacklistActivity.this.finish();
            }
        });
        String summaryS = this.getResources().getString(R.string.call_filter_confirm_add_to_blacklist_summary);
        String summaryF = String.format(summaryS, filterTip[2]);
        mDialogAskAddWithSmr.setContent(summaryF);
        mDialogAskAddWithSmr.setRightBtnListener(new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                BlackListInfo info = new BlackListInfo();
                String name = PrivacyContactUtils.getContactNameFromNumber(getContentResolver(), mPhoneNumber);
                if (!Utilities.isEmpty(name)) {
                    info.name = name;
                }
                int nowItemPosition = mDialogAskAddWithSmr.getNowItemPosition();
                info.number = mPhoneNumber;
                info.markType = CallFilterConstants.MK_BLACK_LIST;
                switch (nowItemPosition) {
                    case 0:
                        info.markType = CallFilterConstants.MK_CRANK;
                        break;
                    case 1:
                        info.markType = CallFilterConstants.MK_ADVERTISE;
                        break;
                    case 2:
                        info.markType = CallFilterConstants.MK_FRAUD;
                        break;
                    default:
                        break;
                }
                infost.add(info);
                boolean inerFlag = mCmp.addBlackList(infost, false);
                if (!inerFlag) {
                    CallFilterHelper cm = CallFilterHelper.getInstance(AskAddToBlacklistActivity.this);
                    cm.addBlackFailTip();
                }
                notiUpdateBlackList();
                Toast.makeText(AskAddToBlacklistActivity.this, getResources().getString(R.string.add_black_list_done), Toast.LENGTH_SHORT).show();
                mDialogAskAddWithSmr.dismiss();
                AskAddToBlacklistActivity.this.finish();
                showShareDialog();
            }
        });
        mDialogAskAddWithSmr.show();
    }

    private void showAskAddWhenNoOffHook(int[] filterTip) {
        mDialogAskAdd = CallFIlterUIHelper.getInstance().getConfirmAddToBlacklistDialog(this, mPhoneNumber, String.valueOf(filterTip[2]));
        mDialogAskAdd.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AskAddToBlacklistActivity.this.finish();
            }
        });
        mDialogAskAdd.setRightBtnListener(new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                BlackListInfo info = new BlackListInfo();
                String name = PrivacyContactUtils.getContactNameFromNumber(getContentResolver(), mPhoneNumber);
                if (!Utilities.isEmpty(name)) {
                    info.name = name;
                }
                info.number = mPhoneNumber;
                info.markType = CallFilterConstants.MK_BLACK_LIST;
                infost.add(info);
                boolean inerFlag =  mCmp.addBlackList(infost, false);
                if (!inerFlag) {
                    CallFilterHelper cm = CallFilterHelper.getInstance(AskAddToBlacklistActivity.this);
                    cm.addBlackFailTip();
                }
                notiUpdateBlackList();
                Toast.makeText(AskAddToBlacklistActivity.this, getResources().getString(R.string.add_black_list_done), Toast.LENGTH_SHORT).show();
                mDialogAskAdd.dismiss();
                SDKWrapper.addEvent(AskAddToBlacklistActivity.this, SDKWrapper.P1, "block", "calling_block");
                AskAddToBlacklistActivity.this.finish();
                showShareDialog();
            }
        });
        mDialogAskAdd.show();
    }

    public void notiUpdateBlackList() {
        int id = EventId.EVENT_LOAD_BLCAK_ID;
        String msg = CallFilterConstants.EVENT_MSG_LOAD_BLACK;
        CommonEvent event = new CommonEvent(id, msg);
        LeoEventBus.getDefaultBus().post(event);
    }

    public final void setMobileNetUnable() {
        // LeoLog.d("ServiceTraffic", "关闭网络咯！！");
        if (android.os.Build.VERSION.SDK_INT > 19) {
            try {
                mLockManager.filterSelfOneMinites();
                mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                this.finish();//TODO need finish?
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Object[] arg = null;
            try {
                boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled", arg);
                if (isMobileDataEnable) {
                    invokeBooleanArgMethod("setMobileDataEnabled", false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean invokeMethod(String methodName, Object[] arg)
            throws Exception {

        ConnectivityManager mConnectivityManager = (ConnectivityManager) 
                getSystemService(Context.CONNECTIVITY_SERVICE);

        Class ownerClass = mConnectivityManager.getClass();

        Class[] argsClass = null;
        if (arg != null) {
            argsClass = new Class[1];
            argsClass[0] = arg.getClass();
        }

        Method method = ownerClass.getMethod(methodName, argsClass);

        Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

        return isOpen;
    }

    public Object invokeBooleanArgMethod(String methodName, boolean value)
            throws Exception {

        ConnectivityManager mConnectivityManager = (ConnectivityManager) 
                getSystemService(Context.CONNECTIVITY_SERVICE);

        Class ownerClass = mConnectivityManager.getClass();

        Class[] argsClass = new Class[1];
        argsClass[0] = boolean.class;

        Method method = ownerClass.getMethod(methodName, argsClass);

        return method.invoke(mConnectivityManager, value);
    }

    private  void showShareDialog() {

        if (mShareDialog == null) {
            mShareDialog = new LEOAlarmDialog(this);
            mShareDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mShareDialog != null) {
                        mShareDialog = null;
                    }
                }
            });
        }
        String title = getString(R.string.addto_blacklist_share_dialog_title);
        final PreferenceTable sharePreferenceTable = PreferenceTable.getInstance();
        boolean isDialogContentEmpty = TextUtils.isEmpty(
                sharePreferenceTable.getString(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_DIALOG_CONTENT));
        String content = "";
        if (!isDialogContentEmpty) {
            content = sharePreferenceTable.getString(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_DIALOG_CONTENT);
        } else {
            content = getString(R.string.addto_blacklist_share_dialog_content);
        }
        String shareButton = getString(R.string.share_dialog_btn_query);
        String cancelButton = getString(R.string.share_dialog_query_btn_cancel);
        mShareDialog.setTitle(title);
        mShareDialog.setContent(content);
        mShareDialog.setLeftBtnStr(cancelButton);
        mShareDialog.setRightBtnStr(shareButton);
        mShareDialog.setLeftBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mShareDialog != null && mShareDialog.isShowing()) {
                    mShareDialog.dismiss();
                    mShareDialog = null;
                }
            }
        });
        mShareDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mShareDialog != null && mShareDialog.isShowing()) {
                    mShareDialog.dismiss();
                    mShareDialog = null;
                }
                shareApps(sharePreferenceTable);
            }
        });
        if (mShareDialog.getWindow() != null) {
            mShareDialog.getWindow().setWindowAnimations(R.style.dialogAnim);
        }
        mShareDialog.show();
    }


    /** 分享应用 */
    private  void shareApps(PreferenceTable preferenceTable) {
        LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        mLockManager.filterSelfOneMinites();
        boolean isContentEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_CONTENT));
        boolean isUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_URL));

        StringBuilder shareBuilder = new StringBuilder();
        if (!isContentEmpty && !isUrlEmpty) {
            try {
                shareBuilder.append(String.format(preferenceTable.getString(
                                PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_CONTENT),
                        "13027964843", preferenceTable.getString(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_URL)));

            } catch (Exception e) {
                shareBuilder.append(getResources().getString(
                        R.string.addto_blacklist_share_content, "13027964843", Constants.DEFAULT_SHARE_URL));
            }
        } else {
            shareBuilder.append(getResources().getString(
                    R.string.addto_blacklist_share_content, "13027964843", Constants.DEFAULT_SHARE_URL));
        }
        Utilities.toShareApp(shareBuilder.toString(), getTitle().toString(), this);
    }
}
