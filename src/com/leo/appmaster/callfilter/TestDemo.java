package com.leo.appmaster.callfilter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.leo.analytics.update.a;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.schedule.BlackUploadFetchJob;
import com.leo.appmaster.schedule.DownBlackFileFetchJob;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class TestDemo extends Activity implements View.OnClickListener {
    private Button b1;
    private Button b2;
    private Button b3;
    private Button b4;
    private Button b5;
    private Button b6;
    private Button b7;
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.B1:
                CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                pm.setFilterUserNumber(50000);
                pm.setFilterTipFroUser(3000);
                BlackListInfo info = new BlackListInfo();
                info.setNumber("18790729990");
                info.setAddBlackNumber(2258);
                info.setMarkerType(0);
                info.setMarkerNumber(30);
                CallFilterManager cm = CallFilterManager.getInstance(AppMasterApplication.getInstance());
                cm.addFilterFroParse(info);
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
                    b.append("num=" + black.getNumber() + ":loc=" + black.getLocHandler() + ":locTyp=" + black.getLocHandlerType() + ":remove=" + black.getRemoveState()+"\n");
                }
                Toast.makeText(TestDemo.this, "" + b.toString(), Toast.LENGTH_LONG).show();

                break;
        }
    }
}
