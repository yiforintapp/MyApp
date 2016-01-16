
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

    private TextView mTvLeftTime;
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

        mTvLeftTime = (TextView) findViewById(R.id.left_time);
        mTvTime = (TextView) findViewById(R.id.right_time);

        if (newState != null) {
            process(mChangeType, newState, mRemainTime);
        }

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
            notifyUI(mChangeType, true);
        } else if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_OUT)) {
            notifyUI(mChangeType, false);
        } else if (mChangeType.equals(BatteryManagerImpl.UPDATE_UP)) {
            notifyUI(mChangeType, true);
        } else {
            notifyUI(mChangeType, false);
        }


    }

    public void notifyUI(String type, boolean isCharing) {

        if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_IN) ||
                mChangeType.equals(BatteryManagerImpl.UPDATE_UP)) {
            setTime(mRemainTime);
        } else {
            noTime();
        }

        if (isCharing) {

        } else {

        }

    }

    private void noTime() {
        mTvLeftTime.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);
    }


    public void setTime(int second) {

        LeoLog.d("testBatteryView", "second : " + second);

        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }


        String hString, dString;
        hString = String.valueOf(h);
        dString = String.valueOf(d);

        LeoLog.d("testBatteryView", "hString : " + hString + "dString : " + dString);

        if (hString.equals("0")) {
            if (!dString.equals("0")) {
                String text = mActivity.getString(R.string.screen_protect_time_right_two, dString);
                mTvLeftTime.setVisibility(View.VISIBLE);
                mTvTime.setVisibility(View.VISIBLE);
                mTvTime.setText(Html.fromHtml(text));
            } else {
                if (newState.level == 100) {
                    mTvLeftTime.setText(mActivity.getString(R.string.screen_protect_charing_text_four));
                    mTvTime.setVisibility(View.GONE);
                } else {
                    mTvLeftTime.setVisibility(View.GONE);
                    mTvTime.setVisibility(View.GONE);
                }
            }
        } else {
            String text = mActivity.getString(R.string.screen_protect_time_right, hString, dString);
            mTvLeftTime.setVisibility(View.VISIBLE);
            mTvTime.setVisibility(View.VISIBLE);
            mTvTime.setText(Html.fromHtml(text));
        }

    }
}
