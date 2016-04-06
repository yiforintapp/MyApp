
package com.leo.appmaster.appmanage;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.DayTrafficSetEvent;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.dialog.DayTrafficSetting;
import com.leo.appmaster.ui.dialog.DayTrafficSetting.OnDayDiaogClickListener;
import com.leo.appmaster.ui.dialog.MonthTrafficSetting;
import com.leo.appmaster.ui.dialog.MonthTrafficSetting.OnDiaogClickListener;
import com.leo.appmaster.utils.LeoLog;

public class TrafficSetting extends BaseActivity implements OnClickListener {

    public static final String DAY_TRAFFIC_SETTING = "day_traffic_setting";

    private CommonToolbar mTitleBar;
    private ImageView iv_show_off;
    private AppMasterPreference sp_notice_flow;
    private TextView second_tv_setting, thrid_tv_setting;
    private View first_content, second_content, thrid_content;
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
        mSwtich = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getOverDataSwitch();
//        mSwtich = sp_notice_flow.getFlowSetting();
        if (mSwtich) {
            iv_show_off.setImageResource(R.drawable.switch_on);
        } else {
            iv_show_off.setImageResource(R.drawable.switch_off);
        }
    }

    private void init() {


        sp_notice_flow = AppMasterPreference.getInstance(this);

        mTitleBar = (CommonToolbar) findViewById(R.id.flow_set_title_bar);
        mTitleBar.setToolbarTitle(R.string.flow_settting_name);
        mTitleBar.setToolbarColorResource(R.color.ctc);
        mTitleBar.setOptionMenuVisible(false);

//        mTtileBar = (CommonTitleBar) findViewById(R.id.flow_set__title_bar);
//        mTtileBar.setTitle(R.string.flow_settting_name);
//        mTtileBar.openBackView();
        first_content = findViewById(R.id.first_content);
        first_content.setOnClickListener(this);
        MaterialRippleLayout.on(first_content)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleHover(true)
                .create();
        iv_show_off = (ImageView) findViewById(R.id.iv_show_off);

        second_content = findViewById(R.id.second_content);
        second_content.setOnClickListener(this);
        second_tv_setting = (TextView) findViewById(R.id.second_tv_setting);
        second_tv_setting.setText(((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getOverDataInvokePercent() + "%");
        MaterialRippleLayout.on(second_content)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleHover(true)
                .create();

        thrid_content = findViewById(R.id.thrid_content);
        thrid_content.setOnClickListener(this);
        thrid_tv_setting = (TextView) findViewById(R.id.thrid_tv_setting);
        thrid_tv_setting.setText("" + ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getDataCutDay());
        MaterialRippleLayout.on(thrid_content)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleHover(true)
                .create();


//        iv_show_off.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.iv_show_on:
//                iv_show_on.setVisibility(View.GONE);
//                iv_show_off.setVisibility(View.VISIBLE);
//                ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
//                        setOverDataSwitch(false);
//                SDKWrapper.addEvent(this, SDKWrapper.P1, "datapage", "closealert");
//                break;
            case R.id.first_content:
                changeState();
//                iv_show_off.setVisibility(View.GONE);
//                iv_show_on.setVisibility(View.VISIBLE);
//                ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
//                        setOverDataSwitch(true);
                break;
            case R.id.second_content:
                showSettingDialog();
                break;
            case R.id.thrid_content:
                showMonthDayDialog1();
                break;
        }
    }

    private void changeState() {
        boolean isOpen = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getOverDataSwitch();
        if (isOpen) {
            ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                    setOverDataSwitch(false);
            iv_show_off.setImageResource(R.drawable.switch_off);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "datapage", "closealert");
        } else {
            ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                    setOverDataSwitch(true);
            iv_show_off.setImageResource(R.drawable.switch_on);
        }
    }

    public void onEventMainThread(DayTrafficSetEvent event) {
        if (DAY_TRAFFIC_SETTING.equals(event.eventMsg)) {
//            thrid_tv_setting.setText(sp_notice_flow.getRenewDay());
            String renewDay = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                    getDataCutDay() + "";
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

                if (!sp_notice_flow.getFinishNotice()) {
                    if (sp_notice_flow.getAlotNotice()) {
                        if (((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                                getOverDataInvokePercent() < progress) {
                            sp_notice_flow.setAlotNotice(false);
                        }
                    }
                }


                LeoLog.d("testDialog", "progress is : " + progress);
                if (progress == 0) {
                    ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                            setOverDataInvokePercent(1);
//                    sp_notice_flow.setFlowSettingBar(1);
                    second_tv_setting.setText(1 + "%");
                } else {
                    ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                            setOverDataInvokePercent(progress);
//                    sp_notice_flow.setFlowSettingBar(progress);
                    second_tv_setting.setText(progress + "%");
                }

            }
        });
        mAlarmDialog.show();
    }
}
