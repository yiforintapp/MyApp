package com.leo.appmaster.callfilter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_filter_setting);

        initUI();
        fillData();
    }

    private void fillData() {
        boolean isFilterSelected = PreferenceTable.getInstance().
                getBoolean(CallFilterConstants.SETTING_FILTER_FLAG, true);
        if (isFilterSelected) {
            checkBox.setImageResource(R.drawable.switch_on);
        } else {
            checkBox.setImageResource(R.drawable.switch_off);
        }

        boolean isNotiSelected = PreferenceTable.getInstance().
                getBoolean(CallFilterConstants.SETTING_NOTI_FLAG, true);
        if (isNotiSelected) {
            checkBoxTwo.setImageResource(R.drawable.switch_on);
        } else {
            checkBoxTwo.setImageResource(R.drawable.switch_off);
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
                boolean isFilterSelected = PreferenceTable.getInstance().
                        getBoolean(CallFilterConstants.SETTING_FILTER_FLAG, true);
                CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);

                if (!isFilterSelected) {
//                    PreferenceTable.getInstance().
//                            putBoolean(CallFilterConstants.SETTING_FILTER_FLAG, true);
                    checkBox.setImageResource(R.drawable.switch_on);
                    cmp.setFilterOpenState(true);
                } else {
//                    PreferenceTable.getInstance().
//                            putBoolean(CallFilterConstants.SETTING_FILTER_FLAG, false);
                    checkBox.setImageResource(R.drawable.switch_off);
                    cmp.setFilterOpenState(false);
                }
                break;
            case R.id.content_item_noti:
                CallFilterContextManagerImpl cmpi = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                boolean isNotiSelected = PreferenceTable.getInstance().
                        getBoolean(CallFilterConstants.SETTING_NOTI_FLAG, true);
                if (!isNotiSelected) {
//                    PreferenceTable.getInstance().
//                            putBoolean(CallFilterConstants.SETTING_NOTI_FLAG, true);
                    checkBoxTwo.setImageResource(R.drawable.switch_on);
                    cmpi.setFilterNotiOpState(true);
                } else {
//                    PreferenceTable.getInstance().
//                            putBoolean(CallFilterConstants.SETTING_NOTI_FLAG, false);
                    checkBoxTwo.setImageResource(R.drawable.switch_off);
                    cmpi.setFilterNotiOpState(false);
                }
                break;
        }
    }


}
