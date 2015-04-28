
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.MonthTrafficSetting;
import com.leo.appmaster.ui.dialog.MonthTrafficSetting.OnDiaogClickListener;
import com.leo.appmaster.utils.LeoLog;

public class QuickGestureActivity extends BaseActivity implements OnItemClickListener,
        OnCheckedChangeListener {
    private ListView mQuickGestureLV;
    private CommonTitleBar mTitleBar;
    private QuickGestureAdapter mAdapter;
    private List<QuickGestureSettingBean> mQuickGestureSettingOption;
    private AppMasterPreference mPre;
    private MonthTrafficSetting mAlarmDialog;
    private TextView second_tv_setting;
    private AppMasterPreference sp_notice_flow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_gesture);
        mQuickGestureSettingOption = new ArrayList<QuickGestureSettingBean>();
        mPre = AppMasterPreference.getInstance(this);
        initUi();
        fillSettingData();
        mAdapter = new QuickGestureAdapter(this);
        mQuickGestureLV.setAdapter(mAdapter);
    }

    private void initUi() {
        mQuickGestureLV = (ListView) findViewById(R.id.quick_gesture_lv);
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_quick_gesture_title_bar);
        mTitleBar.openBackView();
        mQuickGestureLV.setOnItemClickListener(this);
    }

    private void fillSettingData() {
        QuickGestureSettingBean gestureSettingOpenGesture = new QuickGestureSettingBean();
        gestureSettingOpenGesture.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_open_quick_gesture));
        gestureSettingOpenGesture.setCheck(mPre.getSwitchOpenQuickGesture());
        mQuickGestureSettingOption.add(gestureSettingOpenGesture);
        QuickGestureSettingBean gestureSettingSwitchSetting = new QuickGestureSettingBean();
        gestureSettingSwitchSetting.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_switch_setting));
        mQuickGestureSettingOption.add(gestureSettingSwitchSetting);
        QuickGestureSettingBean gestureSettingGestureTheme = new QuickGestureSettingBean();
        gestureSettingGestureTheme.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_gesture_theme_title));
        mQuickGestureSettingOption.add(gestureSettingGestureTheme);
        QuickGestureSettingBean gestureSettingSlidingAreaLocation = new QuickGestureSettingBean();
        gestureSettingSlidingAreaLocation.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_sliding_area_location_title));
        mQuickGestureSettingOption.add(gestureSettingSlidingAreaLocation);
        QuickGestureSettingBean gestureSettingNoReadMessage = new QuickGestureSettingBean();
        gestureSettingNoReadMessage.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_no_read_message_tip));
        gestureSettingOpenGesture.setCheck(mPre.getSwitchOpenNoReadMessageTip());
        mQuickGestureSettingOption.add(gestureSettingNoReadMessage);
        QuickGestureSettingBean gestureSettingRecentlyContact = new QuickGestureSettingBean();
        gestureSettingRecentlyContact.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_recently_contact));
        gestureSettingOpenGesture.setCheck(mPre.getSwitchOpenRecentlyContact());
        mQuickGestureSettingOption.add(gestureSettingRecentlyContact);
        QuickGestureSettingBean gestureSettingContactMessagTip = new QuickGestureSettingBean();
        gestureSettingContactMessagTip.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_privacy_contact_message_tip));
        gestureSettingOpenGesture.setCheck(mPre.getSwitchOpenPrivacyContactMessageTip());
        mQuickGestureSettingOption.add(gestureSettingContactMessagTip);
        QuickGestureSettingBean gestureSettingAbleSlidingTime = new QuickGestureSettingBean();
        gestureSettingAbleSlidingTime.setName(this.getResources().getString(
                R.string.pg_appmanager_quick_gesture_option_able_sliding_time));
        mQuickGestureSettingOption.add(gestureSettingAbleSlidingTime);

    }

    private class QuickGestureAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;
        private Context mContext;
        private int count = 0;

        public QuickGestureAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
            mContext = context;
        }

        @Override
        public int getCount() {
            return mQuickGestureSettingOption.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mQuickGestureSettingOption.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        class ViewHolder {
            ImageView imageView;
            CheckBox switchView;
            TextView title;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup arg2) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_quick_gesture_item, null);
                vh.imageView = (ImageView) convertView.findViewById(R.id.quick_gesture_option_IV);
                vh.switchView = (CheckBox) convertView.findViewById(R.id.quick_gesture_check);
                vh.title = (TextView) convertView.findViewById(R.id.quick_gesture_item_nameTV);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.switchView.setTag(position);
            QuickGestureSettingBean bean = mQuickGestureSettingOption.get(position);
            vh.title.setText(bean.getName());
            if (position == 0 || position == 4
                    || position == 5
                    || position == 6) {
                vh.switchView.setChecked(bean.isCheck());
                vh.switchView.setVisibility(View.VISIBLE);
                // if (position == 0) {
                // Log.e("##########", "显示0" + position);
                // } else if (position == 4) {
                // Log.e("##########", "显示4" + position);
                // } else if (position == 4) {
                // Log.e("##########", "显示5" + position);
                // } else if (position == 6) {
                // Log.e("##########", "显示6" + position);
                // }
                // Log.e("##########",
                // bean.getName()+":"+bean.isCheck()+"----"+mQuickGestureSettingOption.size());
            } else {
                vh.switchView.setVisibility(View.GONE);
            }
            vh.switchView.setOnCheckedChangeListener(QuickGestureActivity.this);
            return convertView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 == 1) {
            Log.e("##########", "1:" + arg2);
        } else if (arg2 == 2) {
            Log.e("##########", "2:" + arg2);
        } else if (arg2 == 3) {
            showSettingDialog();
        } else if (arg2 == 7) {
            Log.e("##########", "7:" + arg2);
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        if ((Integer) arg0.getTag() == 0) {
            Log.e("##########", "++1:" + arg1);
            mPre.setSwitchOpenQuickGesture(arg1);
        } else if ((Integer) arg0.getTag() == 4) {
            mPre.setSwitchOpenNoReadMessageTip(arg1);
        } else if ((Integer) arg0.getTag() == 5) {

            mPre.setSwitchOpenRecentlyContact(arg1);
        } else if ((Integer) arg0.getTag() == 6) {
            mPre.setSwitchOpenPrivacyContactMessageTip(arg1);
        }
    }

    private void showSettingDialog() {
        mAlarmDialog = new MonthTrafficSetting(this);
        mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {

            @Override
            public void onClick(int progress) {

                if (!sp_notice_flow.getFinishNotice()) {
                    if (sp_notice_flow.getAlotNotice()) {
                        if (sp_notice_flow.getFlowSettingBar() < progress) {
                            sp_notice_flow.setAlotNotice(false);
                        }
                    }
                }
                if (progress == 0) {
                    sp_notice_flow.setFlowSettingBar(1);
                    second_tv_setting.setText(1 + "%");
                } else {
                    sp_notice_flow.setFlowSettingBar(progress);
                    second_tv_setting.setText(progress + "%");
                }

            }
        });
        mAlarmDialog.show();
    }

}
