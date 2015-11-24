
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.DayView.DayOfWeek;
import com.leo.appmaster.applocker.NumberPicker.Formatter;
import com.leo.appmaster.applocker.NumberPicker.OnValueChangeListener;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.applocker.model.TimeLock.RepeatTime;
import com.leo.appmaster.applocker.model.TimeLock.TimePoint;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.TimeLockEvent;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;

public class TimeLockEditActivity extends BaseActivity implements
        OnClickListener, OnValueChangeListener, Formatter, OnItemClickListener {

    public LayoutInflater mInflater;
    private LEOAlarmDialog mMakeSureChange;
    private LEOChoiceDialog mModeListDialog;
//    private TextView mTvTitle;
    private EditText mEtTimeLockName;
    private NumberPicker mNpHour, mNpMinitue;
    private TextView mTvRepeat;
    private DayOfWeekSelectedView mDayOfWeekView;
    private View mModeSelectLayout;
    private TextView mTvName;
//    private View mIvSave;
//    private View mIvBack;

    private boolean mOpenRepeat = false;
    private boolean mShowModeList;
    private boolean mNewTimeLock;
    private long mTimeLockId;
    private boolean mEdited;
    private boolean mFromDialog;

    private TimeLock mEditTimeLock;
    private String mLockName;
    private CommonToolbar mTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lock_edit);
        handleIntent();
        initUI();
        loadData();
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTitleBar.setToolbarTitle(R.string.lock_mode_time);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionMenuVisible(true);
        mTitleBar.setOptionClickListener(this);
        mTitleBar.setOptionImageResource(R.drawable.mode_done);
        mTitleBar.setNavigationClickListener(this);

        mInflater = LayoutInflater.from(this);

//        mIvBack = findViewById(R.id.iv_back);
//        mIvBack.setOnClickListener(this);

//        mTvTitle = (TextView) findViewById(R.id.mode_name_tv);
//        mTvTitle.setText(R.string.lock_mode_time);

        mEtTimeLockName = (EditText) findViewById(R.id.et_name);
        mTvRepeat = (TextView) findViewById(R.id.switch_repeat);
        mTvRepeat.setOnClickListener(this);
        mDayOfWeekView = (DayOfWeekSelectedView) findViewById(R.id.day_of_week_layout);
        mModeSelectLayout = findViewById(R.id.layout_mode_name);
        mModeSelectLayout.setOnClickListener(this);
        mTvName = (TextView) findViewById(R.id.tv_mode_name);

//        mIvSave = findViewById(R.id.iv_edit_finish);
//        mIvSave.setOnClickListener(this);

        mNpHour = (NumberPicker) findViewById(R.id.np_hour);
        mNpMinitue = (NumberPicker) findViewById(R.id.np_minitue);

        mNpHour.setMaxValue(23);
        mNpHour.setOnValueChangedListener(this);
        mNpHour.setFormatter(this);
        mNpMinitue.setMaxValue(59);
        mNpMinitue.setOnValueChangedListener(this);
        mNpMinitue.setFormatter(this);

        mEtTimeLockName.setText(mEditTimeLock.name);
        mTvName.setText(mEditTimeLock.lockModeName);
        mNpHour.setValue(mEditTimeLock.time.hour);
        mNpMinitue.setValue(mEditTimeLock.time.minute);
        if (mEditTimeLock.repeatMode.repeatSet != 0) {
            showRepeat(true);
            byte[] repeats = mEditTimeLock.repeatMode.getAllRepeatDayOfWeek();
            for (byte day : repeats) {
                mDayOfWeekView.selectDay(day);
            }
        }

    }

    private void handleIntent() {
        Intent intent = getIntent();
        mNewTimeLock = intent.getBooleanExtra("new_time_lock", false);
        mTimeLockId = intent.getLongExtra("time_lock_id", -1l);
        mFromDialog = intent.getBooleanExtra("from_dialog", false);
        mEditTimeLock = new TimeLock();
        if (mNewTimeLock) {
            LockMode mode = getHomeMode();
            mEditTimeLock.lockModeId = mode.modeId;
            mEditTimeLock.lockModeName = mode.modeName;
            mEditTimeLock.time = new TimePoint((short) 0, (short) 0);
            mEditTimeLock.repeatMode = new RepeatTime((byte) 0);
            mEditTimeLock.using = false;
        } else {
            List<TimeLock> timeList = mLockManager.getTimeLock();
            for (TimeLock timeLock : timeList) {
                if (timeLock.id == mTimeLockId) {
                    mEditTimeLock.id = timeLock.id;
                    mEditTimeLock.lockModeId = timeLock.lockModeId;
                    mEditTimeLock.lockModeName = timeLock.lockModeName;
                    mEditTimeLock.name = timeLock.name;
                    mEditTimeLock.repeatMode = new RepeatTime(timeLock.repeatMode.repeatSet);
                    mEditTimeLock.time = new TimePoint(timeLock.time.hour, timeLock.time.minute);
                    mEditTimeLock.using = timeLock.using;
                    break;
                }
            }
        }

        mLockName = mEditTimeLock.name;

    }

    @Deprecated
    private LockMode getUnlockAllMode() {
        List<LockMode> modeList = mLockManager.getLockMode();
        for (LockMode lockMode : modeList) {
            if (lockMode.defaultFlag == 0) {
                return lockMode;
            }
        }
        return null;
    }
    
    private LockMode getHomeMode() {
        List<LockMode> modeList = mLockManager.getLockMode();
        for (LockMode lockMode : modeList) {
            if (lockMode.defaultFlag == 3) {
                return lockMode;
            }
        }
        return null;
    }
    

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mNewTimeLock) {
            showSaveTip();
        } else {
            String name = mEtTimeLockName.getText().toString();
            if (!TextUtils.equals(name, mLockName)) {
                mEdited = true;
            }

            // mDayOfWeekView.get

            // repeat
            if (mOpenRepeat) {
                List<DayOfWeek> list = mDayOfWeekView.getSelectedDay();
                RepeatTime rt = new RepeatTime((byte) 0);

                for (DayOfWeek dayOfWeek : list) {
                    rt.addRepeatPoint((byte) dayOfWeek.dayOfWeek);
                }
                if (rt.repeatSet != mEditTimeLock.repeatMode.repeatSet)
                    mEdited = true;
            }

            if (mEdited) {
                showSaveTip();
            } else {
                super.onBackPressed();
            }
        }

    }

    private void showSaveTip() {
        if (mMakeSureChange == null) {
            mMakeSureChange = new LEOAlarmDialog(this);
            mMakeSureChange.setTitle(getString(R.string.time_lock_save_hint));
            mMakeSureChange.setContent(getString(R.string.mode_save_ask,
                    getString(R.string.lock_mode_time)));
            mMakeSureChange.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    if (which == 0) {
                        // to cancel
                        finish();
                    } else if (which == 1) {
                        // to save
                        saveTimeLock();
                    }
                }
            });
        }
        mMakeSureChange.show();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
    }

    private void loadData() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_back:
//                onBackPressed();
                break;
            case R.id.iv_edit_finish:
//                saveTimeLock();
                break;
            case R.id.ct_option_1_rl:
                saveTimeLock();
                break;
            case R.id.ct_back_rl:
                onBackPressed();
                break;
            case R.id.switch_repeat:
                mEdited = true;
                showRepeat(!mOpenRepeat);
                break;
            case R.id.layout_mode_name:
                showModeList(!mShowModeList);
                break;
            default:
                break;
        }
    }

    private void showModeList(boolean show) {
        if (mModeListDialog == null) {
            mModeListDialog = new LEOChoiceDialog(this);
        }
        mModeListDialog.setTitle(getResources().getString(R.string.select_mode));
        LockManager  lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        List<String> names = new ArrayList<String>();
        int index = -1;
        for(int i = 0 ; i < lm.getLockMode().size() ; i++){
            names.add(lm.getLockMode().get(i).modeName);
            if(lm.getLockMode().get(i).modeId == mEditTimeLock.lockModeId){
                index = i;
            }
        }
        mModeListDialog.setItemsWithDefaultStyle(names, index);
        mModeListDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<LockMode> modeList = mLockManager.getLockMode();
                LockMode selectedMode = modeList.get(position);
                mEditTimeLock.lockModeId = selectedMode.modeId;
                mEditTimeLock.lockModeName = selectedMode.modeName;
                mTvName.setText(mEditTimeLock.lockModeName);
                mModeListDialog.dismiss();
                mEdited = true;

                if (selectedMode.defaultFlag == 1 && !selectedMode.haveEverOpened) {
                    Intent intent = new Intent(TimeLockEditActivity.this,
                            RecommentAppLockListActivity.class);
                    intent.putExtra("target", -1);
                    startActivity(intent);
                    selectedMode.haveEverOpened = true;
                    mLockManager.updateMode(selectedMode);
                }
            }
        });
//    
//            mModeListDialog = new LEOBaseDialog(this,R.style.bt_dialog);
//            mModeListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            mModeListDialog.setContentView(R.layout.dialog_mode_list_select);
//            mModeList = (ListView) mModeListDialog.findViewById(R.id.mode_list);
//            mNoWifiTv = (TextView) mModeListDialog.findViewById(R.id.no_wifi);
//            mModeList.setOnItemClickListener(this);
//            View cancel = mModeListDialog.findViewById(R.id.dlg_bottom_btn);
//            cancel.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mModeListDialog.dismiss();
//                }
//            });
//        }
//        mNoWifiTv.setVisibility(View.GONE);
//        TextView mTitle = (TextView) mModeListDialog.findViewById(R.id.dlg_title);
//        mTitle.setText(getResources().getString(R.string.select_mode));
//        ListAdapter adapter = new ModeListAdapter(this);
//        mModeList.setAdapter(adapter);

        mModeListDialog.show();
    }

    private void showRepeat(boolean show) {
        if (show) {
            mTvRepeat.setBackgroundResource(R.drawable.select);
            mDayOfWeekView.setVisibility(View.VISIBLE);
        } else {
            mTvRepeat.setBackgroundResource(R.drawable.unselect);
            mDayOfWeekView.setVisibility(View.GONE);
        }
        mOpenRepeat = show;
    }

    private void saveTimeLock() {
        // name
        String name = mEtTimeLockName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            shakeName();
            Toast.makeText(this, R.string.please_input_name, Toast.LENGTH_SHORT).show();
            return;
        } else {
            mEditTimeLock.name = name;
            SDKWrapper.addEvent(this, SDKWrapper.P1, "time", "time");
        }

        // time
        mEditTimeLock.time.hour = (short) mNpHour.getValue();
        mEditTimeLock.time.minute = (short) mNpMinitue.getValue();

        // repeat
        if (mOpenRepeat) {
            List<DayOfWeek> list = mDayOfWeekView.getSelectedDay();
            mEditTimeLock.repeatMode.repeatSet = 0;
            for (DayOfWeek dayOfWeek : list) {
                mEditTimeLock.repeatMode.addRepeatPoint((byte) dayOfWeek.dayOfWeek);
            }
        } else {
            mEditTimeLock.repeatMode.repeatSet = 0;
        }

        // mode
        String modeName = mTvName.getText().toString();
        List<LockMode> modeList = mLockManager.getLockMode();
        for (LockMode lockMode : modeList) {
            if (TextUtils.equals(modeName, lockMode.modeName)) {
                mEditTimeLock.lockModeId = lockMode.modeId;
                break;
            }
        }

        if (mNewTimeLock) {
            mEditTimeLock.using = true;
            mLockManager.addTimeLock(mEditTimeLock);
            Toast.makeText(
                    this,
                    this.getString(R.string.lock_change, this.getString(R.string.lock_mode_time),
                            mEditTimeLock.name),
                    Toast.LENGTH_SHORT).show();
            if(mFromDialog){
                SDKWrapper.addEvent(this, SDKWrapper.P1, "time", "dialog");
            }
        } else {
            if (!mOpenRepeat) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                if (hour < mEditTimeLock.time.hour) {
                    mEditTimeLock.using = true;
                } else if (hour == mEditTimeLock.time.hour) {
                    if (minute < mEditTimeLock.time.minute) {
                        mEditTimeLock.using = true;
                    }
                }
            }
            mLockManager.updateTimeLock(mEditTimeLock);
            Toast.makeText(TimeLockEditActivity.this, R.string.save_successful, Toast.LENGTH_SHORT)
                    .show();
        }

        LeoEventBus.getDefaultBus().post(
                new TimeLockEvent(EventId.EVENT_TIME_LOCK_CHANGE, "time lock changed"));
        finish();
    }

    private void shakeName() {
        Animation shake = AnimationUtils.loadAnimation(this,
                R.anim.left_right_shake);
        mEtTimeLockName.startAnimation(shake);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        mEdited = true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<LockMode> modeList = mLockManager.getLockMode();
        LockMode selectedMode = modeList.get(position);
        mEditTimeLock.lockModeId = selectedMode.modeId;
        mEditTimeLock.lockModeName = selectedMode.modeName;
        mTvName.setText(mEditTimeLock.lockModeName);
        mModeListDialog.dismiss();
        mEdited = true;

        if (selectedMode.defaultFlag == 1 && !selectedMode.haveEverOpened) {
            Intent intent = new Intent(this,
                    RecommentAppLockListActivity.class);
            intent.putExtra("target", -1);
            startActivity(intent);
            selectedMode.haveEverOpened = true;
            mLockManager.updateMode(selectedMode);
        }
    }

    @Override
    public String format(int value) {
        return value + "";
    }

    class ModeListAdapter extends BaseAdapter {

        private LockManager lm;
        private LayoutInflater inflater;

        public ModeListAdapter(Context ctx) {
            lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            inflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return lm.getLockMode().size();
        }

        @Override
        public Object getItem(int position) {
            return lm.getLockMode().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_time_lock_select, parent, false);
                holder = new Holder();
                holder.name = (TextView) convertView.findViewById(R.id.tv_time_lock_name);
                holder.selecte = (ImageView) convertView.findViewById(R.id.iv_selected);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.name.setText(lm.getLockMode().get(position).modeName);

            if (lm.getLockMode().get(position).modeId == mEditTimeLock.lockModeId) {
                holder.selecte.setVisibility(View.VISIBLE);
            } else {
                holder.selecte.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    public static class Holder {
        TextView name;
        ImageView selecte;
    }
}
