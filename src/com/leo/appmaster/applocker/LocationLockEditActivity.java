
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.NumberPicker.Formatter;
import com.leo.appmaster.applocker.NumberPicker.OnValueChangeListener;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LocationLockEvent;
import com.leo.appmaster.eventbus.event.TimeLockEvent;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
import com.leo.appmaster.ui.dialog.LeoSingleLinesInputDialog;
import com.leo.appmaster.utils.WifiAdmin;

public class LocationLockEditActivity extends BaseActivity implements
        OnClickListener, OnValueChangeListener, Formatter {

    public LayoutInflater mInflater;
    // private LeoSingleLinesInputDialog mModeNameDiglog;
    private LEOAlarmDialog mMakeSureChange;
    private LEOBaseDialog mModeListDialog;
    private ListView mModeList;
    private EditText mEtTimeLockName;
    private TextView mTvSsid, mTvEnterMode, mTvQuitMode;
    private View mIvBack, mIvSave;
    private View mLyaoutWifi, mLayoutEnterMode, mLayoutQuitMode;

    private boolean mNewLocationLock;
    private long mTimeLockId;
    private boolean mEdited;
    private String mLockName;

    private LocationLock mEditLocationLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_lock_edit);
        handleIntent();
        initUI();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mNewLocationLock = intent.getBooleanExtra("new_location_lock", false);
        mTimeLockId = intent.getLongExtra("location_lock_id", -1l);
        mEditLocationLock = new LocationLock();
        if (mNewLocationLock) {
            LockMode mode = getUnlockAllMode();
            mEditLocationLock.entranceModeId = mode.modeId;
            mEditLocationLock.entranceModeName = mode.modeName;
            mEditLocationLock.quitModeId = mode.modeId;
            mEditLocationLock.quitModeName = mode.modeName;
            mEditLocationLock.name = "";
            mEditLocationLock.ssid = "";
            mEditLocationLock.using = false;
        } else {
            List<LocationLock> locationList = LockManager.getInstatnce().getLocationLock();
            for (LocationLock locationLock : locationList) {
                if (locationLock.id == mTimeLockId) {
                    mEditLocationLock.id = locationLock.id;
                    mEditLocationLock.entranceModeId = locationLock.entranceModeId;
                    mEditLocationLock.entranceModeName = locationLock.entranceModeName;
                    mEditLocationLock.quitModeId = locationLock.quitModeId;
                    mEditLocationLock.quitModeName = locationLock.quitModeName;
                    mEditLocationLock.name = locationLock.name;
                    mEditLocationLock.ssid = locationLock.ssid;
                    mEditLocationLock.selected = mEditLocationLock.selected;
                    mEditLocationLock.using = locationLock.using;
                    break;
                }
            }
        }
        mLockName = mEditLocationLock.name;
    }

    private void initUI() {
        mInflater = LayoutInflater.from(this);
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mIvSave = findViewById(R.id.iv_edit_finish);
        mIvSave.setOnClickListener(this);

        mLyaoutWifi = findViewById(R.id.layout_ssid_name);
        mLyaoutWifi.setOnClickListener(this);
        mLayoutEnterMode = findViewById(R.id.layout_enter_name);
        mLayoutEnterMode.setOnClickListener(this);
        mLayoutQuitMode = findViewById(R.id.layout_quit_name);
        mLayoutQuitMode.setOnClickListener(this);

        mEtTimeLockName = (EditText) findViewById(R.id.et_name);
        mEtTimeLockName.setText(mEditLocationLock.name);
        mEtTimeLockName.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        mTvSsid = (TextView) findViewById(R.id.tv_ssid_name);
        mTvEnterMode = (TextView) findViewById(R.id.tv_enter_name);
        mTvQuitMode = (TextView) findViewById(R.id.tv_quit_name);

        mTvSsid.setText(mEditLocationLock.ssid);
        mTvEnterMode.setText(mEditLocationLock.entranceModeName);
        mTvQuitMode.setText(mEditLocationLock.quitModeName);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_edit_finish:
                saveLocationLock();
                break;
            case R.id.layout_ssid_name:
                showWifiList();
                break;
            case R.id.layout_enter_name:
                showModeList(0);
                break;
            case R.id.layout_quit_name:
                showModeList(1);
                break;
            default:
                break;
        }
    }

    private LockMode getUnlockAllMode() {
        List<LockMode> modeList = LockManager.getInstatnce().getLockMode();
        for (LockMode lockMode : modeList) {
            if (lockMode.defaultFlag == 0) {
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
        if (mNewLocationLock) {
            showSaveTip();
        } else {
            String name = mEtTimeLockName.getText().toString();
            if (!TextUtils.equals(name, mLockName)) {
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
            mMakeSureChange.setTitle(getString(R.string.location_lock_save_hint));
            mMakeSureChange.setContent(getString(R.string.mode_save_ask,
                    getString(R.string.lock_mode_location)));
            mMakeSureChange.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    if (which == 0) {
                        finish();
                    } else if (which == 1) {
                        saveLocationLock();
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

    private void showModeList(final int which) {
        if (mModeListDialog == null) {
            mModeListDialog = new LEOBaseDialog(this);
            mModeListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mModeListDialog.setContentView(R.layout.dialog_mode_list_select);
            mModeList = (ListView) mModeListDialog.findViewById(R.id.mode_list);
            View cancel = mModeListDialog.findViewById(R.id.dlg_bottom_btn);
            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mModeListDialog.dismiss();
                }
            });
        }

        mModeList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LockManager lm = LockManager.getInstatnce();
                List<LockMode> modeList = lm.getLockMode();
                LockMode selectedMode = modeList.get(position);
                if (which == 0) {
                    mEditLocationLock.entranceModeId = selectedMode.modeId;
                    mEditLocationLock.entranceModeName = selectedMode.modeName;
                    mTvEnterMode.setText(mEditLocationLock.entranceModeName);

                } else {
                    mEditLocationLock.quitModeId = selectedMode.modeId;
                    mEditLocationLock.quitModeName = selectedMode.modeName;
                    mTvQuitMode.setText(mEditLocationLock.quitModeName);
                }
                mModeListDialog.dismiss();
                mEdited = true;

                if (selectedMode.defaultFlag == 1 && !selectedMode.haveEverOpened) {
                    Intent intent = new Intent(LocationLockEditActivity.this,
                            RecommentAppLockListActivity.class);
                    intent.putExtra("target", -1);
                    startActivity(intent);
                    selectedMode.haveEverOpened = true;
                    lm.updateMode(selectedMode);
                }
            }
        });

        ListAdapter adapter = new ModeListAdapter(this, which);
        mModeList.setAdapter(adapter);

        mModeListDialog.show();
    }

    private void showWifiList() {
        if (mModeListDialog == null) {
            mModeListDialog = new LEOBaseDialog(this);
            mModeListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mModeListDialog.setContentView(R.layout.dialog_mode_list_select);
            mModeList = (ListView) mModeListDialog.findViewById(R.id.mode_list);
            View cancel = mModeListDialog.findViewById(R.id.dlg_bottom_btn);
            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mModeListDialog.dismiss();
                }
            });
        }
        mModeList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiListAdapter adapter = (WifiListAdapter) parent.getAdapter();
                String ssid = adapter.getWifiList().get(position);
                mEditLocationLock.ssid = ssid;
                mTvSsid.setText(ssid);
                mModeListDialog.dismiss();
                mEdited = true;
            }
        });

        List<String> wifiList = new ArrayList<String>();
        WifiAdmin wa = new WifiAdmin(this);
        List<ScanResult> list = wa.getWifiList();
        if (list != null && !list.isEmpty()) {
            for (ScanResult scanResult : list) {
                if (!scanResult.SSID.equals("")) {
                    wifiList.add(scanResult.SSID);
                }
            }
        }
        ListAdapter adapter = new WifiListAdapter(this, wifiList);
        mModeList.setAdapter(adapter);

        mModeListDialog.show();
    }

    private void saveLocationLock() {
        LockManager lm = LockManager.getInstatnce();

        // name
        String name = mEtTimeLockName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            shakeView(mEtTimeLockName);
            Toast.makeText(LocationLockEditActivity.this, R.string.please_input_name, 0).show();
            return;
        } else {
            mEditLocationLock.name = name;
        }

        // ssid
        if (TextUtils.isEmpty(mEditLocationLock.ssid)) {
            shakeView(mTvSsid);
            Toast.makeText(LocationLockEditActivity.this, R.string.please_selset_wifi, 0).show();
            return;
        }

        if (mNewLocationLock) {
            mEditLocationLock.using = true;
            lm.addLocationLock(mEditLocationLock);
            Toast.makeText(
                    this,
                    this.getString(R.string.lock_change,
                            this.getString(R.string.lock_mode_location),
                            mEditLocationLock.name),
                    Toast.LENGTH_SHORT).show();
        } else {
            lm.updateLocationLock(mEditLocationLock);
            Toast.makeText(this, R.string.save_successful, Toast.LENGTH_SHORT).show();
        }
        LeoEventBus.getDefaultBus().post(
                new LocationLockEvent(EventId.EVENT_LOCATION_LOCK_CHANGE, "location lock changed"));

        finish();
    }

    private void shakeView(View v) {
        Animation shake = AnimationUtils.loadAnimation(this,
                R.anim.left_right_shake);
        v.startAnimation(shake);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }

    @Override
    public String format(int value) {
        return value + "";
    }

    class ModeListAdapter extends BaseAdapter {

        private LockManager lm;
        private LayoutInflater inflater;
        private int which;

        public ModeListAdapter(Context ctx, int which) {
            lm = LockManager.getInstatnce();
            inflater = LayoutInflater.from(ctx);
            this.which = which;
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
            LockMode mode = lm.getLockMode().get(position);
            if (which == 0) {
                if (mode.modeId == mEditLocationLock.entranceModeId) {
                    holder.selecte.setVisibility(View.VISIBLE);
                } else {
                    holder.selecte.setVisibility(View.GONE);
                }
            } else {
                if (mode.modeId == mEditLocationLock.quitModeId) {
                    holder.selecte.setVisibility(View.VISIBLE);
                } else {
                    holder.selecte.setVisibility(View.GONE);
                }
            }

            return convertView;
        }
    }

    class WifiListAdapter extends BaseAdapter {

        private LockManager lm;
        private LayoutInflater inflater;
        private List<String> wifiList;

        public WifiListAdapter(Context ctx, List<String> list) {
            lm = LockManager.getInstatnce();
            inflater = LayoutInflater.from(ctx);
            wifiList = list;
        }

        public List<String> getWifiList() {
            return wifiList;
        }

        @Override
        public int getCount() {
            return wifiList.size();
        }

        @Override
        public Object getItem(int position) {
            return wifiList.get(position);
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

            if (TextUtils.equals(mEditLocationLock.ssid, wifiList.get(position))) {
                holder.selecte.setVisibility(View.VISIBLE);
            } else {
                holder.selecte.setVisibility(View.INVISIBLE);
            }

            holder.name.setText(wifiList.get(position));

            return convertView;
        }
    }

    public static class Holder {
        TextView name;
        ImageView selecte;
    }
}
