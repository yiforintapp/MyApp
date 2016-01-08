package com.leo.appmaster.callfilter;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent.OnFinished;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;


public class AskAddToBlacklistActivity extends BaseActivity {
    private CallFilterContextManager mCmp;
    private MultiChoicesWitchSummaryDialog mDialogAskAddWithSmrMark;
    private MultiChoicesWitchSummaryDialog mDialogAskAddWithSmr;
    private LEOAlarmDialog mDialogAskAdd;
    private MultiChoicesWitchSummaryDialog mDialogTooShort;
    private String mPhoneNumber = "";
    public static final int TYPE_SHOW_NO_OFFHOOK = 1;
    public static final int TYPE_SHOW_ASK_WITHOUT_MARK = 2;
    public static final int TYPE_SHOW_ASK_WITH_MARK = 3;
    public static final int TYPE_SHOW_TOO_SHORT = 4;
    public static final String EXTRA_NUMBER = "number";
    public static final String EXTRA_FILTERTYPE_ARRAY = "filterTip";
    public static final String EXTRA_WHAT_TO_SHOW = "which";
    public static final int CASE_ASK_WHEN_NO_OFFHOOK = 1;
    public static final int CASE_ASK_WITHOUT_MARK = 2;
    public static final int CASE_ASK_WITH_MARK = 3;
    public static final int CASE_ASK_WHEN_TOO_SHORT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_add_to_blacklist);
        mCmp = (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mPhoneNumber = intent.getStringExtra(EXTRA_NUMBER);
        int intExtra = intent.getIntExtra(EXTRA_WHAT_TO_SHOW, -1);
        int[] filterTip = intent.getIntArrayExtra(EXTRA_FILTERTYPE_ARRAY);
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
                BlackListInfo infot = new BlackListInfo();
                String name = PrivacyContactUtils.getContactNameFromNumber(getContentResolver(), mPhoneNumber);
                if (!Utilities.isEmpty(name)) {
                    infot.setNumberName(name);
                }
                int nowItemPosition = mDialogTooShort.getNowItemPosition();
                infot.setNumber(mPhoneNumber);
                switch (nowItemPosition) {
                    case 0:
                        infot.setLocHandlerType(CallFilterConstants.FILTER_CALL_TYPE);
                        break;
                    case 1:
                        infot.setLocHandlerType(CallFilterConstants.AD_SALE_TYPE);
                        break;
                    case 2:
                        infot.setLocHandlerType(CallFilterConstants.CHEAT_NUM_TYPE);
                        break;
                    default:
                        break;
                }
                infost.add(infot);
                boolean inerFlag = mCmp.addBlackList(infost, false);
                if (!inerFlag) {
                    CallFilterManager cm = CallFilterManager.getInstance(AskAddToBlacklistActivity.this);
                    cm.addBlackFailTip();
                }
                notiUpdateBlackList();
                Toast.makeText(AskAddToBlacklistActivity.this, getResources().getString(R.string.mark_number_from_list), Toast.LENGTH_SHORT).show();
                mDialogTooShort.dismiss();
                AskAddToBlacklistActivity.this.finish();
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
            case CallFilterConstants.FILTER_CALL_TYPE:
                mark = this.getResources().getString(R.string.call_filter_mark_as_sr);
                break;
            case CallFilterConstants.AD_SALE_TYPE:
                mark = this.getResources().getString(R.string.call_filter_mark_as_tx);
                break;
            case CallFilterConstants.CHEAT_NUM_TYPE:
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
                BlackListInfo infot = new BlackListInfo();
                String name = PrivacyContactUtils.getContactNameFromNumber(getContentResolver(), mPhoneNumber);
                if (!Utilities.isEmpty(name)) {
                    infot.setNumberName(name);
                }
                infot.setLocHandlerType(CallFilterConstants.BLACK_LIST_TYP);
                int nowItemPosition = mDialogAskAddWithSmrMark.getNowItemPosition();
                infot.setNumber(mPhoneNumber);
                switch (nowItemPosition) {
                    case 0:
                        infot.setLocHandlerType(CallFilterConstants.FILTER_CALL_TYPE);
                        break;
                    case 1:
                        infot.setLocHandlerType(CallFilterConstants.AD_SALE_TYPE);
                        break;
                    case 2:
                        infot.setLocHandlerType(CallFilterConstants.CHEAT_NUM_TYPE);
                        break;
                    default:
                        break;
                }
                infost.add(infot);
                boolean inerFlag = mCmp.addBlackList(infost, false);
                if (!inerFlag) {
                    CallFilterManager cm = CallFilterManager.getInstance(AskAddToBlacklistActivity.this);
                    cm.addBlackFailTip();
                }
                notiUpdateBlackList();
                Toast.makeText(AskAddToBlacklistActivity.this, getResources().getString(R.string.mark_number_from_list), Toast.LENGTH_SHORT).show();
                mDialogAskAddWithSmrMark.dismiss();
                AskAddToBlacklistActivity.this.finish();
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
                BlackListInfo infot = new BlackListInfo();
                String name = PrivacyContactUtils.getContactNameFromNumber(getContentResolver(), mPhoneNumber);
                if (!Utilities.isEmpty(name)) {
                    infot.setNumberName(name);
                }
                int nowItemPosition = mDialogAskAddWithSmr.getNowItemPosition();
                infot.setNumber(mPhoneNumber);
                infot.setLocHandlerType(CallFilterConstants.BLACK_LIST_TYP);
                switch (nowItemPosition) {
                    case 0:
                        infot.setLocHandlerType(CallFilterConstants.FILTER_CALL_TYPE);
                        break;
                    case 1:
                        infot.setLocHandlerType(CallFilterConstants.AD_SALE_TYPE);
                        break;
                    case 2:
                        infot.setLocHandlerType(CallFilterConstants.CHEAT_NUM_TYPE);
                        break;
                    default:
                        break;
                }
                infost.add(infot);
                boolean inerFlag = mCmp.addBlackList(infost, false);
                if (!inerFlag) {
                    CallFilterManager cm = CallFilterManager.getInstance(AskAddToBlacklistActivity.this);
                    cm.addBlackFailTip();
                }
                notiUpdateBlackList();
                Toast.makeText(AskAddToBlacklistActivity.this, getResources().getString(R.string.add_black_list_done), Toast.LENGTH_SHORT).show();
                mDialogAskAddWithSmr.dismiss();
                AskAddToBlacklistActivity.this.finish();
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
                BlackListInfo infot = new BlackListInfo();
                String name = PrivacyContactUtils.getContactNameFromNumber(getContentResolver(), mPhoneNumber);
                if (!Utilities.isEmpty(name)) {
                    infot.setNumberName(name);
                }
                infot.setNumber(mPhoneNumber);
                infot.setLocHandlerType(CallFilterConstants.BLACK_LIST_TYP);
                infost.add(infot);
                boolean inerFlag =  mCmp.addBlackList(infost, false);
                if (!inerFlag) {
                    CallFilterManager cm = CallFilterManager.getInstance(AskAddToBlacklistActivity.this);
                    cm.addBlackFailTip();
                }
                notiUpdateBlackList();
                Toast.makeText(AskAddToBlacklistActivity.this, getResources().getString(R.string.add_black_list_done), Toast.LENGTH_SHORT).show();
                mDialogAskAdd.dismiss();
                AskAddToBlacklistActivity.this.finish();
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


//    private class MyAdapter extends BaseAdapter {
//        private Context mContext;
//
//        public MyAdapter(Context ctx) {
//            this.mContext = ctx;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = getLayoutInflater().inflate(R.layout.traffic_day_list, parent, false);
//            }
//            TextView tvContent = (TextView) convertView.findViewById(R.id.tv_showday);
//            ImageView ivCheckBox = (ImageView) convertView.findViewById(R.id.iv_showday);
//            tvContent.setText(strings[position]);
//
//            if (nowItemPosition == position) {
//                ivCheckBox.setImageResource(R.drawable.dialog_check_on);
//            } else {
//                ivCheckBox.setImageResource(R.drawable.dialog_check_off);
//            }
//            return convertView;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public int getCount() {
//            return strings.length;
//        }
//
//    }


//    private void showCheckFailed() {
//        mLlSummary = (LinearLayout) findViewById(R.id.ll_summary);
//        mTitle = (TextView) findViewById(R.id.dlg_title);
//        mSummary = (TextView) findViewById(R.id.tv_summary);
//        mLvMain = (ListView) findViewById(R.id.lv_main);
//        mAdapter = new MyAdapter(this);
//        mRvRight = (RippleView) findViewById(R.id.rv_dialog_blue_button);
//        mRvLeft = (RippleView) findViewById(R.id.rv_dialog_whitle_button);
//        mLeftBtn = (TextView) findViewById(R.id.dlg_left_btn);
//        mRightBtn = (TextView) findViewById(R.id.dlg_right_btn);
//        
//        
//        
//        
//    }


}
