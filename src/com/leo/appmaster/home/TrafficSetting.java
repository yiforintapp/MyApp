
package com.leo.appmaster.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.eventbus.event.DayTrafficSetEvent;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.DayTrafficSetting;
import com.leo.appmaster.ui.dialog.DayTrafficSetting.OnDayDiaogClickListener;
import com.leo.appmaster.ui.dialog.MonthTrafficSetting;
import com.leo.appmaster.ui.dialog.MonthTrafficSetting.OnDiaogClickListener;
import com.leo.appmaster.utils.LeoLog;

public class TrafficSetting extends Activity implements OnClickListener {
    
    public static final String DAY_TRAFFIC_SETTING = "day_traffic_setting";
    
    private CommonTitleBar mTtileBar;
    private ImageView iv_show_on, iv_show_off;
    private AppMasterPreference sp_notice_flow;
    private TextView second_tv_setting, thrid_tv_setting;
    private View second_content, thrid_content;
    private boolean mSwtich;
    private MonthTrafficSetting mAlarmDialog;
    private DayTrafficSetting mDayTrafficDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_manager_flow_setting);
        initUI();
    }

    private void initUI() {
        LeoEventBus.getDefaultBus().register(this);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "datapage", "setting");
        init();
        judgeSwtich();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void judgeSwtich() {
        mSwtich = sp_notice_flow.getFlowSetting();
        if (mSwtich) {
            iv_show_off.setVisibility(View.GONE);
            iv_show_on.setVisibility(View.VISIBLE);
        } else {
            iv_show_on.setVisibility(View.GONE);
            iv_show_off.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        sp_notice_flow = AppMasterPreference.getInstance(this);
        mTtileBar = (CommonTitleBar) findViewById(R.id.flow_set__title_bar);
        mTtileBar.setTitle(R.string.flow_settting_name);
        mTtileBar.openBackView();

        iv_show_on = (ImageView) findViewById(R.id.iv_show_on);
        iv_show_off = (ImageView) findViewById(R.id.iv_show_off);
        second_content = findViewById(R.id.second_content);
        second_tv_setting = (TextView) findViewById(R.id.second_tv_setting);
        thrid_content = findViewById(R.id.thrid_content);
        thrid_tv_setting = (TextView) findViewById(R.id.thrid_tv_setting);
        thrid_tv_setting.setText(""+sp_notice_flow.getRenewDay());

        iv_show_on.setOnClickListener(this);
        iv_show_off.setOnClickListener(this);
        second_content.setOnClickListener(this);
        thrid_content.setOnClickListener(this);
        second_tv_setting.setText(sp_notice_flow.getFlowSettingBar() + "%");
//        thrid_tv_setting.setText(sp_notice_flow.getMonthDayClean() + "");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_show_on:
                iv_show_on.setVisibility(View.GONE);
                iv_show_off.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "你关闭了流量超额提醒", 0).show();
                sp_notice_flow.setFlowSetting(false);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "datapage", "closealert");
                break;
            case R.id.iv_show_off:
                iv_show_off.setVisibility(View.GONE);
                iv_show_on.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "你开启了流量超额提醒", 0).show();
                sp_notice_flow.setFlowSetting(true);
                break;
            case R.id.second_content:
                showSettingDialog();
                break;
            case R.id.thrid_content:
                showMonthDayDialog1();
                break;
        }
    }

    public void onEventMainThread(DayTrafficSetEvent event) {
        if (DAY_TRAFFIC_SETTING.equals(event.eventMsg)) {
//            thrid_tv_setting.setText(sp_notice_flow.getRenewDay());
            String renewDay = sp_notice_flow.getRenewDay()+"";
            thrid_tv_setting.setText(renewDay);
        } 
    }
    
    private void showMonthDayDialog1() {
        mDayTrafficDialog = new DayTrafficSetting(this);
        mDayTrafficDialog.setOnClickListener(new OnDayDiaogClickListener() {
            @Override
            public void onClick() {
            }
        });
        mDayTrafficDialog.show();
    }

    private void showSettingDialog() {
        mAlarmDialog = new MonthTrafficSetting(this);
        mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {

            @Override
            public void onClick(int progress) {
                
                if(!sp_notice_flow.getFinishNotice()){
                    if(sp_notice_flow.getAlotNotice()){
                        if(sp_notice_flow.getFlowSettingBar() < progress){
                            sp_notice_flow.setAlotNotice(false);
                        }
                    }
                }
                
                
                LeoLog.d("testDialog", "progress is : " + progress);
                if(progress == 0){
                    sp_notice_flow.setFlowSettingBar(1);
                    second_tv_setting.setText(1 + "%");
                }else {
                    sp_notice_flow.setFlowSettingBar(progress);
                    second_tv_setting.setText(progress + "%");
                }

            }
        });
        mAlarmDialog.show();
    }
}
