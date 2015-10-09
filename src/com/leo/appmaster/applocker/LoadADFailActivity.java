
package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LoadAdFailEvent;
import com.leo.appmaster.sdk.BaseActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoadADFailActivity extends BaseActivity implements OnClickListener {
    private OnClickRollAgainListener mRollAgainListener;
    public static String LOAD_FAIL_EVENT_MESSAGE = "load_fail_roll_again";
    private Button mRollAgainBt;

    public interface OnClickRollAgainListener {
        public void onClickRollAgain();
    }

    public void setOnClickRollAgainListener(OnClickRollAgainListener listener) {
        mRollAgainListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_ad_fail_activity);
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initUI() {
        mRollAgainBt = (Button) findViewById(R.id.superman_btn_rollagain);
        mRollAgainBt.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        switch (id) {
            case R.id.superman_btn_rollagain:
                LeoEventBus.getDefaultBus().post(new LoadAdFailEvent(
                        EventId.EVENT_LOAD_FAIL_ROLL_AGAIN, LOAD_FAIL_EVENT_MESSAGE));
                LoadADFailActivity.this.finish();
                break;
            default:
                break;
        }
    }
}
