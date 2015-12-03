
package com.leo.appmaster.appmanage.view;

import java.util.ArrayList;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.ui.LineView;
import com.leo.appmaster.ui.LineView.BackUpCallBack;
import com.leo.appmaster.ui.MulticolorRoundProgressBar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.MonthDaySetting;
import com.leo.appmaster.ui.dialog.MonthDaySetting.OnTrafficDialogClickListener;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import android.view.View.OnClickListener;
import com.leo.appmaster.utils.ManagerFlowUtils;

public class ManagerFlowFragment extends BaseFragment {
    private static final int CHANGE_TEXT = 0;
    private static final int CHANGE_LIST = 1;
    private ProgressBar pb_loading;
    private int progress = 0;
    private int bili = 0;
    private MulticolorRoundProgressBar roundProgressBar;
    private HorizontalScrollView horizontalScroll;
    private TextView tv_total_ll, tv_normal_ll, tv_remainder_ll, tv_from_donghua, mFlowUseTipText;
    private ImageView mProgressBg;
    private LineView lineView;
    private View flow_all_content;
    private RippleView flow_setting;
    private MonthDaySetting mTrafficSettingDialog;
    private String month = "";
    private int days = 0;
    private AppMasterPreference preferences;

    private ArrayList<String> test;
    private ArrayList<Integer> dataList;
    private ArrayList<ArrayList<Integer>> dataLists;
    private String today_ymd;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == CHANGE_TEXT) {
                int mProgress = (Integer) msg.obj;
                tv_from_donghua.setText(mProgress + "%");
                roundProgressBar.setProgress(mProgress);
                updateProgress();
            } else if (msg.what == CHANGE_LIST) {
                pb_loading.setVisibility(View.INVISIBLE);
                flow_all_content.setVisibility(View.VISIBLE);
                show_donghua();
                initflowchart();
            }
        }
    };

    @Override
    protected int layoutResourceId() {
        Display display = mActivity.getWindowManager().getDefaultDisplay(); //Activity#getWindowManager() 
        int width = display.getWidth();
        int height = display.getHeight();
        Log.i("ManagerFlowFragment", width + " " + height);
        return R.layout.fragment_manager_flow;
    }

    @Override
    protected void onInitUI() {
        // LeoEventBus.getDefaultBus().register(this);
        tv_from_donghua = (TextView) findViewById(R.id.tv_from_donghua);
        roundProgressBar = (MulticolorRoundProgressBar) findViewById(R.id.roundProgressBar);
        flow_all_content = findViewById(R.id.flow_all_content);

        flow_setting = (RippleView) findViewById(R.id.flow_setting);
        flow_setting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showTrafficSetting();
            }
        });
//        flow_setting.setOnRippleCompleteListener(this);

        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
        lineView = (LineView) findViewById(R.id.line_view);
        horizontalScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        tv_total_ll = (TextView) findViewById(R.id.tv_total_ll);
        tv_normal_ll = (TextView) findViewById(R.id.tv_normal_ll);
        tv_remainder_ll = (TextView) findViewById(R.id.tv_remainder_ll);
        pb_loading.setVisibility(View.VISIBLE);

        mProgressBg = (ImageView) findViewById(R.id.iv_donghua_flow);
        mFlowUseTipText = (TextView) findViewById(R.id.flow_use_tip_tv);

        preferences = AppMasterPreference.getInstance(mActivity);
        test = new ArrayList<String>();
        dataList = new ArrayList<Integer>();
        dataLists = new ArrayList<ArrayList<Integer>>();

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                initDate(); // 采用异步线程获取数据
            }
        });
    }

    @Override
    public void onDestroy() {

        if (handler != null) {
            handler.removeMessages(CHANGE_TEXT);
            handler.removeMessages(CHANGE_LIST);
            handler = null;
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        getListViewData();
    }

    public void show_donghua() {
        progress = 0;

        long TaoCanTraffic = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getMonthTotalTraffic();
        long TaoCanTrafficKb = TaoCanTraffic * 1024;
        long MonthUsedItSelf = preferences.getItselfMonthTraffic();
        long MonthUsedRecord = preferences.getMonthGprsAll() / 1024;
//        LeoLog.d("testfuckflow", "TaoCanTraffic : " + TaoCanTraffic);
//        LeoLog.d("testfuckflow", "MonthUsedItSelf : " + MonthUsedItSelf);

        if (TaoCanTraffic < 1) {
            bili = 0;
        } else {
            if (MonthUsedItSelf > 0) {
                bili = (int) (MonthUsedItSelf * 100 / TaoCanTrafficKb);
                LeoLog.d("testfuckflow", "MonthUsedItSelf > 0 : " + bili);
            } else {
                bili = (int) (MonthUsedRecord * 100 / TaoCanTrafficKb);
                LeoLog.d("testfuckflow", "else : " + bili);
            }
        }
        checkOverFlowSetting();
        updateProgress();
    }

    /**
     * if over flow,show over percent
     */
    private void updateProgress() {
        if (bili > 100) {
            if (tv_from_donghua != null) {
                tv_from_donghua.setText(bili - 100 + "%");
            }
        } else {
            if (progress >= bili && bili != 0) {
                return;
            }
            progress++;
            if (bili == 0) {
                progress = 0;
            }
            Message msg = Message.obtain();
            msg.what = CHANGE_TEXT;
            msg.obj = progress;
            handler.sendMessageDelayed(msg, 10);
        }
    }

    /**
     * check if over flow,if over then change bg and text , and hide the roudProgressBar
     */
    private void checkOverFlowSetting() {
        if (bili > 100) {
            mProgressBg.setImageResource(R.drawable.flow_over_bg);
            mFlowUseTipText.setText(getResources().getString(R.string.traffic_over_text));
            roundProgressBar.setProgress(0);
        } else {
            mProgressBg.setImageResource(R.drawable.app_run_bg);
            mFlowUseTipText.setText(getResources().getString(R.string.traffic_progress_bar));
        }
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
        dataList = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getEveryDayTraffic();
//        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//        Cursor cursor1 = mActivity.getContentResolver().query(Constants.MONTH_TRAFFIC_URI, null,
//                "year=? and month=?", new String[]{
//                        String.valueOf(ManagerFlowUtils.getNowYear()),
//                        String.valueOf(ManagerFlowUtils.getNowMonth())
//                }, null);
//        if (cursor1 != null) {
//            while (cursor1.moveToNext()) {
//                int day = cursor1.getInt(6);
//                int gprs = (int) ManagerFlowUtils.BToKb(cursor1.getFloat(2));
//                map.put(day, gprs);
//            }
//            cursor1.close();
//        }
//
//        for (int i = 0; i < days; i++) {
//            Integer value = map.get(i + 1);
//            if (value == null) {
//                dataList.add(0);
//            } else {
//                dataList.add(value);
//            }
//        }
        dataLists.add(dataList);

        if (handler != null) {
            Message message = handler.obtainMessage();
            message.what = CHANGE_LIST;
            handler.sendMessage(message);
        }
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

        float today_flow = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getTodayUsed();

//        float today_flow = 0;
//        today_ymd = ManagerFlowUtils.getNowTime();
//        Cursor mCursor = mContext.getContentResolver().query(Constants.MONTH_TRAFFIC_URI, null,
//                "daytime=?",
//                new String[]{
//                        today_ymd
//                }, null);
//        if (mCursor != null) {
//            if (mCursor.moveToNext()) {
//                today_flow = mCursor.getFloat(2);
//            }
//            mCursor.close();
//        }

        //今日流量
        String today_flow_String = ManagerFlowUtils.refreshTraffic_home_app(today_flow);
        tv_normal_ll.setText(today_flow_String);

        //本月流量
        long monthTraffic = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getMonthUsed();
        tv_total_ll.setText(ManagerFlowUtils.refreshTraffic_home_app(monthTraffic));


//        long mThisMonthTraffic = preferences.getMonthGprsAll();
//        long mThisMonthItselfTraffi = preferences.getItselfMonthTraffic();
//        if (mThisMonthItselfTraffi > 0) {
//            tv_total_ll.setText(ManagerFlowUtils.refreshTraffic_home_app_KB(mThisMonthItselfTraffi));
//        } else {
//            tv_total_ll.setText(ManagerFlowUtils.refreshTraffic_home_app(mThisMonthTraffic));
//        }

        //剩余流量
        long mTaoCanMB = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getMonthTotalTraffic();
//        long mTaoCanMB = preferences.getTotalTraffic();
        long mTaoCanKB = mTaoCanMB * 1024;
        if (mTaoCanMB < 1) {
            tv_remainder_ll.setText("---");
        } else {
            tv_remainder_ll.setText(ManagerFlowUtils.
                    refreshTraffic_home_app_KB(mTaoCanKB - (monthTraffic / 1024)));
//            if (mThisMonthItselfTraffi > 0) {
//                tv_remainder_ll.setText(ManagerFlowUtils.refreshTraffic_home_app_KB(mTaoCanKB
//                        - mThisMonthItselfTraffi));
//            } else {
//                tv_remainder_ll.setText(ManagerFlowUtils.refreshTraffic_home_app_KB(mTaoCanKB
//                        - (mThisMonthTraffic / 1024)));
//            }
        }
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.flow_setting:
//                showTrafficSetting();
//                break;
//        }
//    }

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
