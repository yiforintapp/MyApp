package com.leo.appmaster.callfilter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.schedule.BlackUploadFetchJob;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class TestDemo extends Activity implements View.OnClickListener {
    private static final int CALLS_COUNT = 10;
    private Button b1;
    private Button b2;
    private Button b3;
    private Button b4;
    private Button b5;
    private Button b6;
    private Button b7;
    private Button b8;
    private Button b9;
    private int mI = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_demo);
        b1 = (Button) findViewById(R.id.B1);
        b1.setOnClickListener(this);
        b2 = (Button) findViewById(R.id.B2);
        b2.setOnClickListener(this);
        b3 = (Button) findViewById(R.id.B3);
        b3.setOnClickListener(this);
        b4 = (Button) findViewById(R.id.B4);
        b4.setOnClickListener(this);
        b5 = (Button) findViewById(R.id.B5);
        b5.setOnClickListener(this);
        b6 = (Button) findViewById(R.id.B6);
        b6.setOnClickListener(this);
        b7 = (Button) findViewById(R.id.B7);
        b7.setOnClickListener(this);
        b8 = (Button) findViewById(R.id.B8);
        b8.setOnClickListener(this);
        b9 = (Button) findViewById(R.id.B9);
        b9.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.B1:
                CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                pm.setFilterUserNumber(1000);
                pm.setFilterTipFroUser(3000);
                List<BlackListInfo> infos1 = new ArrayList<BlackListInfo>();
                BlackListInfo info = new BlackListInfo();
                info.setNumber("18790729990");
                info.setAddBlackNumber(2258);
                info.setMarkerType(0);
                info.setMarkerNumber(30);
                infos1.add(info);
                CallFilterManager cm = CallFilterManager.getInstance(AppMasterApplication.getInstance());
                cm.addFilterFroParse(infos1);
                pm.setCallDurationMax(7000);
                pm.setStraNotiTipParam(2);
                break;
            case R.id.B2:
                new BlackUploadFetchJob().startImmediately(true);
                break;
            case R.id.B3:
                CallFilterContextManagerImpl lsm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                int user = lsm.getFilterUserNumber();
                int tipUser = lsm.getFilterTipFroUser();
                long dration = lsm.getCallDurationMax();
                int showTipPar = lsm.getBlackMarkTipParam();
                String path = lsm.getSerBlackFilePath();
                int notiTipPar = lsm.getStraNotiTipParam();
                int blackCount = lsm.getSerBlackTipCount();
                int markCount = lsm.getSerMarkTipCount();
                StringBuilder sb = new StringBuilder();
                sb.append("user = " + user);
                sb.append("tipUser = " + tipUser);
                sb.append("dration = " + dration);
                sb.append("showTipPar = " + showTipPar);
                sb.append("path = " + path);
                sb.append("notiTipPar = " + notiTipPar);
                sb.append("blackCount = " + blackCount);
                sb.append("markCount = " + markCount);
                Toast.makeText(TestDemo.this, sb.toString(), Toast.LENGTH_LONG).show();
                break;
            case R.id.B4:
                List<BlackListInfo> blacks = new ArrayList<BlackListInfo>();
                for (int i = 0; i <= 9; i++) {
                    BlackListInfo black = new BlackListInfo();
                    black.setNumber("121212278" + i);
                    black.setFiltUpState(CallFilterConstants.FIL_UP);
                    black.setLocHandler(CallFilterConstants.LOC_HD);
                    black.setUploadState(CallFilterConstants.UPLOAD);
                    blacks.add(black);
                }
                CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                cmp.addBlackList(blacks, true);
                break;
            case R.id.B5:
                CallFilterContextManagerImpl cmps = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                int i = 1;
                //已上传的拦截名单
                List<BlackListInfo> filInfos = cmps.getUpBlackListLimit(1);
                List<BlackListInfo> tmpFilInfos = new ArrayList<BlackListInfo>();
                if (filInfos != null && filInfos.size() > 0) {
                    for (BlackListInfo info1 : filInfos) {
                        if (info1.getFiltUpState() == CallFilterConstants.FIL_UP) {
                            tmpFilInfos.add(info1);
                        }
                    }
                }
                if (tmpFilInfos != null && tmpFilInfos.size() > 0) {
                    for (BlackListInfo info2 : tmpFilInfos) {
                        BlackListInfo black = new BlackListInfo();
                        black.setNumber(info2.getNumber());
                        black.setFiltUpState(CallFilterConstants.FIL_UP_NO);
                        Context context = AppMasterApplication.getInstance();
                        CallFilterManager.getInstance(context).updateUpBlack(black);
                    }
                }
                break;
            case R.id.B6:
                List<BlackListInfo> blacksSer = CallFilterManager.getInstance(TestDemo.this).getSerBlackList();
                if (blacksSer != null) {
                    StringBuilder sbw = new StringBuilder();
                    for (BlackListInfo infos : blacksSer) {
                        sbw.append(infos.getNumber() + ":黑人=" + infos.getAddBlackNumber() + "：标记人=" + infos.getMarkerNumber() + ":类型=" + infos.getMarkerType() + "\n");
                    }
                    Toast.makeText(TestDemo.this, sbw.toString(), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.B7:
                Uri uri = CallFilterConstants.BLACK_LIST_URI;
                String sortOrder = CallFilterConstants.BLACK_ID + " " + CallFilterConstants.DESC;
                StringBuilder sb1 = new StringBuilder();
                sb1.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
                sb1.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ? ");
                String selects = sb1.toString();
                String[] selectArgs = new String[]{String.valueOf(CallFilterConstants.LOC_HD),
                        String.valueOf(CallFilterConstants.REMOVE)};
                List<BlackListInfo> blackss = CallFilterUtils.getBlackList(uri, null, selects, selectArgs, sortOrder);
                StringBuilder b = new StringBuilder();
                for (BlackListInfo black : blackss) {
                    b.append("num=" + black.getNumber() + ":loc=" + black.getLocHandler() + ":locTyp=" + black.getLocHandlerType() + ":remove=" + black.getRemoveState() + "\n");
                }
                Toast.makeText(TestDemo.this, "" + b.toString(), Toast.LENGTH_LONG).show();

                break;
            case R.id.B8:
                CallFilterContextManagerImpl LM = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                CallFilterInfo info8 = new CallFilterInfo();
                info8.setNumber("18790729990");
                info8.setTimeLong(System.currentTimeMillis() + 2000);
                info8.setCallType(1);
                info8.setDuration(1200);
                LM.insertCallToSys(info8);
                break;
            case R.id.B9:
                StringBuilder sbName = new StringBuilder();
                String countryId = Utilities.getCountryID(AppMasterApplication.getInstance());
                sbName.append(countryId);
                sbName.append(CallFilterConstants.GZIP);
                StringBuilder sb12 = new StringBuilder();
                sb12.append(CallFilterUtils.getBlackPath());
                sb12.append(sbName.toString());
                String filePath = sb12.toString();
                CallFilterUtils.parseBlactList(filePath);
//            String[] CALL_LOG_PROJECTION = new String[] {
//                    CallLog.Calls._ID,
//                    CallLog.Calls.NUMBER,
//                    CallLog.Calls.DATE,
//                    CallLog.Calls.DURATION,
//                    CallLog.Calls.TYPE,
//                    CallLog.Calls.CACHED_NAME,
//                    CallLog.Calls.CACHED_NUMBER_TYPE,
//                    CallLog.Calls.CACHED_NUMBER_LABEL};
//            String selection = "0==0) GROUP BY (" + CallLog.Calls.NUMBER;
//
//         Cursor c = TestDemo.this.getContentResolver().query(CallLog.Calls.CONTENT_URI, CALL_LOG_PROJECTION,selection, null,CallLog.Calls.DEFAULT_SORT_ORDER);
//           if(c!=null){
//               Toast.makeText(TestDemo.this,"shu="+c.getCount(),Toast.LENGTH_LONG).show();
//           }else{
//               Toast.makeText(TestDemo.this,"0",Toast.LENGTH_LONG).show();
//           }
//                Context mContext = TestDemo.this;
//                final MultiChoicesWitchSummaryDialog mDialogAskAddWithSmrMark = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary("789", mContext, true, 0, true);
//                String summaryS = mContext.getResources().getString(R.string.call_filter_confirm_ask_mark_summary);
//                String mark = mContext.getResources().getString(R.string.call_filter_black_list_tab);
//                String summaryF = String.format(summaryS, 1000, mark);
//                mDialogAskAddWithSmrMark.setContent(summaryF);
//                mDialogAskAddWithSmrMark.setRightBtnListener(new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
//                        BlackListInfo infot = new BlackListInfo();
//                        infot.setLocHandlerType(CallFilterConstants.BLACK_LIST_TYP);
//                        int nowItemPosition = mDialogAskAddWithSmrMark.getNowItemPosition();
//                        switch (nowItemPosition) {
//                            case 0:
//                                infot.setLocHandlerType(CallFilterConstants.FILTER_CALL_TYPE);
//                                break;
//                            case 1:
//                                infot.setLocHandlerType(CallFilterConstants.AD_SALE_TYPE);
//                                break;
//                            case 2:
//                                infot.setLocHandlerType(CallFilterConstants.CHEAT_NUM_TYPE);
//                                break;
//                            default:
//                                break;
//                        }
//                        infost.add(infot);
//                        mDialogAskAddWithSmrMark.dismiss();
//                    }
//                });
//                mDialogAskAddWithSmrMark.show();
                break;
        }
    }
}
