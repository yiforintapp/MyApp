package com.leo.appmaster.callfilter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;

/**
 * Created by qili on 15-12-19.
 */
public class CallFilterSettingActivity extends BaseActivity implements View.OnClickListener {
    private CommonToolbar mTitleBar;
    private ImageView checkBox;
    private ImageView checkBoxTwo;
    private RippleView rpBtn;
    private RippleView rpBtnTwo;
    private TextView mFilterTv;
//    private boolean mNotiStatusWhenSwitch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_filter_setting);

        initUI();
        fillData();
    }

    private void fillData() {
        CallFilterContextManager cmp = (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        boolean isFilterSelected = cmp.getFilterOpenState();
        if (isFilterSelected) {
            checkBox.setImageResource(R.drawable.switch_on);
            mFilterTv.setText(R.string.call_filter_setting_one_desc);
        } else {
            checkBox.setImageResource(R.drawable.switch_off);
            mFilterTv.setText(R.string.call_fil_set_close);
        }

        boolean isNotiSelected = cmp.getFilterNotiOpState();
        if (isNotiSelected && isFilterSelected) {
            checkBoxTwo.setImageResource(R.drawable.switch_on);
//            mNotiStatusWhenSwitch = true;
        } else {
            checkBoxTwo.setImageResource(R.drawable.switch_off);
//            mNotiStatusWhenSwitch = false;
        }
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.wifi_setting_title_bar);
        mTitleBar.setToolbarTitle(R.string.call_filter_setting_title);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionMenuVisible(false);

        rpBtn = (RippleView) findViewById(R.id.content_item_filter);
        rpBtn.setOnClickListener(this);
        rpBtnTwo = (RippleView) findViewById(R.id.content_item_noti);
        rpBtnTwo.setOnClickListener(this);

        checkBox = (ImageView) findViewById(R.id.iv_filter);
        checkBoxTwo = (ImageView) findViewById(R.id.iv_noti);
        mFilterTv = (TextView) findViewById(R.id.filter_desc);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.content_item_filter:

                CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                boolean isFilterSelected = cmp.getFilterOpenState();
                if (!isFilterSelected) {
                    //拦截从关到开
//                    PreferenceTable.getInstance().
//                            putBoolean(CallFilterConstants.SETTING_FILTER_FLAG, true);
                    checkBox.setImageResource(R.drawable.switch_on);
                    mFilterTv.setText(R.string.call_filter_setting_one_desc);
                    cmp.setFilterOpenState(true);
                    if (cmp.getFilterNotiOpState()) {
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "settings_on");
                        checkBoxTwo.setImageResource(R.drawable.switch_on);
                    } else {
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "settings_off");
                        checkBoxTwo.setImageResource(R.drawable.switch_off);
                    }
                } else {
                    //拦截从开到关
//                    PreferenceTable.getInstance().
//                            putBoolean(CallFilterConstants.SETTING_FILTER_FLAG, false);
                    checkBox.setImageResource(R.drawable.switch_off);
                    mFilterTv.setText(R.string.call_fil_set_close);
                    cmp.setFilterOpenState(false);
                    checkBoxTwo.setImageResource(R.drawable.switch_off);
                }
                break;
            case R.id.content_item_noti:
                CallFilterContextManagerImpl cmpi = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                boolean isNotiSelected = cmpi.getFilterNotiOpState();
                boolean isFilter = cmpi.getFilterOpenState();
                if (!isNotiSelected && isFilter) {
//                    PreferenceTable.getInstance().
//                            putBoolean(CallFilterConstants.SETTING_NOTI_FLAG, true);
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "settings_notify_on");
                    checkBoxTwo.setImageResource(R.drawable.switch_on);
                    cmpi.setFilterNotiOpState(true);
                } else {
//                    PreferenceTable.getInstance().
//                            putBoolean(CallFilterConstants.SETTING_NOTI_FLAG, false);
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "settings_notify_off");
                    checkBoxTwo.setImageResource(R.drawable.switch_off);
                    cmpi.setFilterNotiOpState(false);
                }
                break;
        }
    }


}
