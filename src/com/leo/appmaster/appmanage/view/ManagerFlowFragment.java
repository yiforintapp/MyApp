
package com.leo.appmaster.appmanage.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.ui.LineView;
import com.leo.appmaster.ui.LineView.BackUpCallBack;
import com.leo.appmaster.ui.RoundProgressBar;
import com.leo.appmaster.ui.dialog.MonthDaySetting;
import com.leo.appmaster.ui.dialog.MonthDaySetting.OnTrafficDialogClickListener;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.ManagerFlowUtils;

public class ManagerFlowFragment extends BaseFragment implements OnClickListener{
    private static final int CHANGE_TEXT = 0;
    private ProgressBar pb_loading;
    private int progress = 0;
    private RoundProgressBar roundProgressBar;
    private HorizontalScrollView horizontalScroll;
    private TextView tv_total_ll, tv_normal_ll, tv_remainder_ll, tv_from_donghua;
    private LineView lineView;
    private View flow_all_content;
    private TextView flow_setting;
    private MonthDaySetting mTrafficSettingDialog;
    private String month = "";
    private int days = 0;
    private FlowAsyncTask flowAsyncTask;
    private AppMasterPreference preferences;

    private ArrayList<String> test;
    private ArrayList<Integer> dataList;
    private ArrayList<ArrayList<Integer>> dataLists;
    private String today_ymd;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CHANGE_TEXT:
                    int mProgress = (Integer) msg.obj;
                    tv_from_donghua.setText(mProgress + "%");
                    break;
            }
        };
    };

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_manager_flow;
    }

    @Override
    protected void onInitUI() {
        // LeoEventBus.getDefaultBus().register(this);
        tv_from_donghua = (TextView) findViewById(R.id.tv_from_donghua);
        roundProgressBar = (RoundProgressBar) findViewById(R.id.roundProgressBar);
        flow_all_content = findViewById(R.id.flow_all_content);
        flow_setting = (TextView) findViewById(R.id.flow_setting);
        flow_setting.setOnClickListener(this);
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
        lineView = (LineView) findViewById(R.id.line_view);
        horizontalScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        tv_total_ll = (TextView) findViewById(R.id.tv_total_ll);
        tv_normal_ll = (TextView) findViewById(R.id.tv_normal_ll);
        tv_remainder_ll = (TextView) findViewById(R.id.tv_remainder_ll);
        pb_loading.setVisibility(View.VISIBLE);

        preferences = AppMasterPreference.getInstance(mActivity);

        test = new ArrayList<String>();
        dataList = new ArrayList<Integer>();
        dataLists = new ArrayList<ArrayList<Integer>>();

        flowAsyncTask = new FlowAsyncTask();
        flowAsyncTask.execute("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class FlowAsyncTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... params) {
            initDate();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            pb_loading.setVisibility(View.INVISIBLE);
            flow_all_content.setVisibility(View.VISIBLE);
            show_donghua();
            initflowchart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        show_donghua();
        getListViewData();
    }

    public void show_donghua() {
        new Thread() {
            public void run() {

                progress = 0;

                long TaoCanTraffic = preferences.getTotalTraffic();
                long TaoCanTrafficKb = preferences.getTotalTraffic() * 1024;
                long MonthUsedItSelf = preferences.getItselfMonthTraffic();
                long MonthUsedRecord = preferences.getMonthGprsAll() / 1024;
                
                int bili = 0;
//                LeoLog.d("testfuckflow", "TaoCanTraffic : " + TaoCanTraffic);
//                LeoLog.d("testfuckflow", "MonthUsedItSelf : " + MonthUsedItSelf);
                
                if (TaoCanTraffic < 1) {
                    bili = 0;
                } else {
                    if(MonthUsedItSelf > 0){
                        bili = (int) (MonthUsedItSelf * 100 / TaoCanTrafficKb);
//                        LeoLog.d("testfuckflow", "MonthUsedItSelf > 0 : " + bili);
                    }else {
                        bili = (int) (MonthUsedRecord * 100 / TaoCanTrafficKb);
//                        LeoLog.d("testfuckflow", "else : " + bili);
                    }
                }
                while (progress <= bili) {
                    progress += 1;

                    if (bili == 0) {
                        progress = 0;
                    }

                    if(progress > 100){
                        progress = 100;
                        break;
                    }
                    
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    roundProgressBar.setProgress(progress);
                    Message msg = Message.obtain();
                    msg.what = CHANGE_TEXT;
                    msg.obj = progress;
                    handler.sendMessage(msg);
                }
            };
        }.start();
    }

    protected void initDate() {
        String language = AppwallHttpUtil.getLanguage();
        if (ManagerFlowUtils.getNowMonth() > 9) {
            month = "" + ManagerFlowUtils.getNowMonth();
        } else {
            month = "0" + ManagerFlowUtils.getNowMonth();
        }
        days = ManagerFlowUtils.getCurrentMonthDay();
        today_ymd = ManagerFlowUtils.getNowTime();
        // 流量图的日期列
        for (int i = 0; i < days; i++) {
            if (language.equals("zh")) {
                if (ManagerFlowUtils.getDayOfMonth() == i + 1) {
                    test.add("-   今日   -");
                    continue;
                }
                if (i < 9) {
                    test.add(month + "/" + "0" + (i + 1));
                } else {
                    test.add(month + "/" + (i + 1));
                }
            } else {
                if (ManagerFlowUtils.getDayOfMonth() == i + 1) {
                    test.add("-   Today   -");
                    continue;
                }
                if (i < 9) {
                    test.add("0" + (i + 1) + "/" + month);
                } else {
                    test.add((i + 1) + "/" + month);
                }

            }
        }
        
        // 每天的流量点
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        Cursor cursor1 = mActivity.getContentResolver().query(Constants.MONTH_TRAFFIC_URI, null,
                "year=? and month=?", new String[] {
                        String.valueOf(ManagerFlowUtils.getNowYear()),
                        String.valueOf(ManagerFlowUtils.getNowMonth())
                }, null);
        if (cursor1 != null) {
            while (cursor1.moveToNext()) {
                int day = cursor1.getInt(6);
                int gprs = (int) ManagerFlowUtils.BToKb(cursor1.getFloat(2));
                map.put(day, gprs);
            }
            cursor1.close();
        }

        for (int i = 0; i < days; i++) {
            Integer value = map.get(i + 1);
            if (value == null) {
                dataList.add(0);
            } else {
                dataList.add(value);
            }
        }
        dataLists.add(dataList);
    }

    public void initflowchart() {
        lineView.setBottomTextList(test);
        lineView.setDrawDotLine(false, new BackUpCallBack() {
            @Override
            public void beforeBackup(int max) {
                horizontalScroll.scrollTo(max, 0);
            }
        });
        lineView.setShowPopup(LineView.SHOW_POPUPS_All);
        lineView.setDataList(dataLists);
        getListViewData();
    }

    private void getListViewData() {

        float today_flow = 0;
        today_ymd = ManagerFlowUtils.getNowTime();
        Cursor mCursor = mActivity.getContentResolver().query(Constants.MONTH_TRAFFIC_URI, null,
                "daytime=?",
                new String[] {
                    today_ymd
                }, null);
        if (mCursor != null) {
            if (mCursor.moveToNext()) {
                today_flow = mCursor.getFloat(2);
            }
            mCursor.close();
        }
        
        //今日流量
        String today_flow_String = ManagerFlowUtils.refreshTraffic_home_app(today_flow);
        tv_normal_ll.setText(today_flow_String);
        
        //本月流量
        long mThisMonthTraffic = preferences.getMonthGprsAll();
        long mThisMonthItselfTraffi = preferences.getItselfMonthTraffic() ;
        if(mThisMonthItselfTraffi > 0){
            tv_total_ll.setText(ManagerFlowUtils.refreshTraffic_home_app_KB(mThisMonthItselfTraffi));
        }else {
            tv_total_ll.setText(ManagerFlowUtils.refreshTraffic_home_app(mThisMonthTraffic));
        }
        
        //剩余流量
        long mTaoCanMB = preferences.getTotalTraffic();
        long mTaoCanKB = mTaoCanMB * 1024;
        if (mTaoCanMB < 1) {
            tv_remainder_ll.setText("---");
        } else {
            if(mThisMonthItselfTraffi > 0){
                tv_remainder_ll.setText(ManagerFlowUtils.refreshTraffic_home_app_KB(mTaoCanKB
                        -  mThisMonthItselfTraffi));
            }else {
                tv_remainder_ll.setText(ManagerFlowUtils.refreshTraffic_home_app_KB(mTaoCanKB
                        - (mThisMonthTraffic / 1024)));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flow_setting:
                showTrafficSetting();
                break;
        }
    }

    private void showTrafficSetting() {
        mTrafficSettingDialog = new MonthDaySetting(mActivity);
        mTrafficSettingDialog.setOnClickListener(new OnTrafficDialogClickListener() {

            @Override
            public void onClick() {
                show_donghua();
                getListViewData();
            }
        });

        mTrafficSettingDialog.show();
    }


}
