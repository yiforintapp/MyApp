package com.leo.appmaster.intruderprotection;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.sdk.BaseActivity;

/**
 * Created by chenfs on 16-3-14.
 */
public class IntruderSettingActivity extends BaseActivity implements View.OnClickListener {
    private Button mBtt1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_setting);
        mBtt1 = (Button) findViewById(R.id.bt_t1);
        mBtt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DevicePolicyManager dm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                ComponentName mAdminName = new ComponentName(IntruderSettingActivity.this, DeviceReceiver.class);
                if (dm != null) {
                    if (!dm.isAdminActive(mAdminName)) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
    }
}
