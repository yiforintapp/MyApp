
package com.leo.appmaster.battery;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.impl.BatteryManagerImpl;
import com.leo.appmaster.utils.LeoLog;

public class BatteryViewFragment extends BaseFragment {
    private View mMoveContent;
    private View mTimeContent;
    private TextView mTvLevel;
    private TextView mTvStatus;
    private TextView mTvBigTime;
    private TextView mTvSmallLeft;
    private TextView mTvSmallRight;
    private TextView mTvTime;

    private BatteryManagerImpl.BatteryState newState;
    private String mChangeType = BatteryManagerImpl.SHOW_TYPE_IN;
    private int mRemainTime;

    @Override
    protected int layoutResourceId() {
        return R.layout.activity_battery_view_new;
    }

    @Override
    protected void onInitUI() {

        LeoLog.d("testBatteryView", "INIT UI");
        mMoveContent = findViewById(R.id.move_content);
        mTimeContent = findViewById(R.id.time_content);

        mTvLevel = (TextView) findViewById(R.id.battery_num);
        mTvStatus = (TextView) findViewById(R.id.battery_status);

        mTvBigTime = (TextView) findViewById(R.id.battery_num);
        mTvSmallLeft = (TextView) findViewById(R.id.battery_num);
        mTvSmallRight = (TextView) findViewById(R.id.battery_num);
        mTvTime = (TextView) findViewById(R.id.right_time);

        if(newState != null){
            process(mChangeType, newState, mRemainTime);
        }
        fillTime("17", "33");
    }

    private void fillTime(String s, String s1) {
        String string = s;
        String string2 = s1;
        String text = mActivity.getString(R.string.screen_protect_time_right, string, string2);
        mTvTime.setText(Html.fromHtml(text));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void initCreate(String type, BatteryManager.BatteryState state, int remainTime) {
        mChangeType = type;
        newState = state;
        mRemainTime = remainTime;
    }

    public void process(String type, BatteryManager.BatteryState state, int remainTime) {
        mChangeType = type;
        newState = state;
        mRemainTime = remainTime;

        if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_IN)) {
            notifyUI(true);
        } else if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_OUT)) {
            notifyUI(false);
        } else if (mChangeType.equals(BatteryManagerImpl.UPDATE_UP)) {
            notifyUI(true);
        } else {
            notifyUI(true);
        }


    }

    public void notifyUI(boolean isCharing) {

        if (isCharing) {
            mTvLevel.setVisibility(View.VISIBLE);
            mTvLevel.setText("电量 : " + newState.level);
        } else {
            mTvLevel.setVisibility(View.GONE);
        }


    }

}
