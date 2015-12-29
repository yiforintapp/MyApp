package com.leo.appmaster.callfilter;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
    private int mI = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_demo);
        b1 = (Button) findViewById(R.id.B1);
        b1.setOnClickListener(this);
        b2 = (Button) findViewById(R.id.B2);
        b2.setOnClickListener(this);
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
                BlackUploadFetchJob.startWork();
                break;
//            case:
//            CallFilterContextManagerImpl mp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
//            List<BlackListInfo> list = new ArrayList<BlackListInfo>();
//            for (int i = 0; i < 10; i++) {
//                BlackListInfo info = CallFilterUtils.getBlackListInfo(-1, "110" + i, "测试", 0, null,
//                        null, 23, 25, 0, 1, 1, 0, 1, -1);
//                list.add(info);
//            }
//            mp.addBlackList(list, false);
//            break;
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
                sb.append("user = "+user);
                sb.append("tipUser = "+tipUser);
                sb.append("dration = "+dration);
                sb.append("showTipPar = "+showTipPar);
                sb.append("path = "+path);
                sb.append("notiTipPar = "+notiTipPar);
                sb.append("blackCount = "+blackCount);
                sb.append("markCount = "+markCount);


                Toast.makeText(TestDemo.this,sb.toString(),Toast.LENGTH_LONG).show();
                break;
        }
    }
}
