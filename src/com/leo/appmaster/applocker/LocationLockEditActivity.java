
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LocationLockEvent;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.utils.WifiAdmin;

public class LocationLockEditActivity extends BaseActivity implements
        OnClickListener, OnValueChangeListener, Formatter {

    public LayoutInflater mInflater;
    // private LeoSingleLinesInputDialog mModeNameDiglog;
    private LEOAlarmDialog mMakeSureChange;
    private LEOChoiceDialog mModeListDialog;
    private LEOBaseDialog mWifiListDialog;
    private ListView mModeList;
    private EditText mEtTimeLockName;
    private TextView mTvSsid, mTvEnterMode, mTvQuitMode, mNoWifiTv;
    //    private View mIvBack, mIvSave;
    private View mLyaoutWifi, mLayoutEnterMode, mLayoutQuitMode;

    private boolean mNewLocationLock;
    private long mTimeLockId;
    private boolean mEdited;
    private String mLockName;
    private boolean mFromDialog;

    private LocationLock mEditLocationLock;
    private LockManager mLockManager;
    private CommonToolbar mTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_lock_edit);
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        handleIntent();
        initUI();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mNewLocationLock = intent.getBooleanExtra("new_location_lock", false);
        mTimeLockId = intent.getLongExtra("location_lock_id", -1l);
        mFromDialog = intent.getBooleanExtra("from_dialog", false);
        mEditLocationLock = new LocationLock();
        if (mNewLocationLock) {
            LockMode mode = getHomeMode();
            mEditLocationLock.entranceModeId = mode.modeId;
            mEditLocationLock.entranceModeName = mode.modeName;
            mEditLocationLock.quitModeId = mode.modeId;
            mEditLocationLock.quitModeName = mode.modeName;
            mEditLocationLock.name = "";
            mEditLocationLock.ssid = "";
            mEditLocationLock.using = false;
        } else {
            List<LocationLock> locationList = mLockManager.getLocationLock();
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

        mTitleBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTitleBar.setToolbarTitle(R.string.lock_mode_location);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionMenuVisible(true);
        mTitleBar.setOptionClickListener(this);
        mTitleBar.setOptionImageResource(R.drawable.mode_done);
        mTitleBar.setNavigationClickListener(this);

//        mIvBack = findViewById(R.id.iv_back);
//        mIvBack.setOnClickListener(this);
//        mIvSave = findViewById(R.id.iv_edit_finish);
//        mIvSave.setOnClickListener(this);

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
//                onBackPressed();
                break;
            case R.id.iv_edit_finish:
//                saveLocationLock();
                break;
            case R.id.ct_option_1_rl:
                saveLocationLock();
                break;
            case R.id.ct_back_rl:
                onBackPressed();
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

//    mCategoryDialog.setTitle(getResources().getString(R.string.feedback_category_tip));
//    mCategoryDialog.setItemsWithDefaultStyle(mCategories,mCategoryPos);
//    mCategoryDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            mCategory.setText(mCategories.get(position));
//            mCategoryPos = position;
//            mCategory.setTag(1);
//            mCategoryDialog.dismiss();
//            checkCommitable();
//        }
//    });
//    mCategoryDialog.show();


//    if (convertView == null) {
//        convertView = inflater.inflate(R.layout.item_time_lock_select, parent, false);
//        holder = new Holder();
//        holder.name = (TextView) convertView.findViewById(R.id.tv_time_lock_name);
//        holder.selecte = (ImageView) convertView.findViewById(R.id.iv_selected);
//        convertView.setTag(holder);
//    } else {
//        holder = (Holder) convertView.getTag();
//    }
//
//    holder.name.setText(lm.getLockMode().get(position).modeName);
//    LockMode mode = lm.getLockMode().get(position);
//    if (which == 0) {
//        if (mode.modeId == mEditLocationLock.entranceModeId) {
//            holder.selecte.setVisibility(View.VISIBLE);
//        } else {
//            holder.selecte.setVisibility(View.GONE);
//        }
//    } else {
//        if (mode.modeId == mEditLocationLock.quitModeId) {
//            holder.selecte.setVisibility(View.VISIBLE);
//        } else {
//            holder.selecte.setVisibility(View.GONE);
//        }
//    }

    private void showModeList(final int which) {
        if (mModeListDialog == null) {
            mModeListDialog = new LEOChoiceDialog(this);
        }
        mModeListDialog.setTitle(getResources().getString(R.string.select_mode));
        LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        List<String> names = new ArrayList<String>();
        int index = -1;
        for (int i = 0; i < lm.getLockMode().size(); i++) {
            names.add(lm.getLockMode().get(i).modeName);
            switch (which) {
                case 0:
                    if (lm.getLockMode().get(i).modeId == mEditLocationLock.entranceModeId) {
                        index = i;
                    }
                    break;
                case 1:
                    if (lm.getLockMode().get(i).modeId == mEditLocationLock.quitModeId) {
                        index = i;
                    }
                    break;
                default:
                    break;
            }
        }
        mModeListDialog.setItemsWithDefaultStyle(names, index);
        mModeListDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<LockMode> modeList = mLockManager.getLockMode();
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
                    mLockManager.updateMode(selectedMode);
                }
            }
        });
        mModeListDialog.show();
    }
//            mModeListDialog = new LEOBaseDialog(this, R.style.bt_dialog);
//            mModeListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            mModeListDialog.setContentView(R.layout.dialog_mode_list_select);
//            mModeList = (ListView) mModeListDialog.findViewById(R.id.mode_list);
//            mNoWifiTv = (TextView) mModeListDialog.findViewById(R.id.no_wifi);
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
//        mModeList.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                List<LockMode> modeList = mLockManager.getLockMode();
//                LockMode selectedMode = modeList.get(position);
//                if (which == 0) {
//                    mEditLocationLock.entranceModeId = selectedMode.modeId;
//                    mEditLocationLock.entranceModeName = selectedMode.modeName;
//                    mTvEnterMode.setText(mEditLocationLock.entranceModeName);
//
//                } else {
//                    mEditLocationLock.quitModeId = selectedMode.modeId;
//                    mEditLocationLock.quitModeName = selectedMode.modeName;
//                    mTvQuitMode.setText(mEditLocationLock.quitModeName);
//                }
//                mModeListDialog.dismiss();
//                mEdited = true;
//
//                if (selectedMode.defaultFlag == 1 && !selectedMode.haveEverOpened) {
//                    Intent intent = new Intent(LocationLockEditActivity.this,
//                            RecommentAppLockListActivity.class);
//                    intent.putExtra("target", -1);
//                    startActivity(intent);
//                    selectedMode.haveEverOpened = true;
//                    mLockManager.updateMode(selectedMode);
//                }
//            }
//        });
//
//        if (mNoWifiTv != null) {
//            mNoWifiTv.setVisibility(View.GONE);
//        }
//
//        ListAdapter adapter = new ModeListAdapter(this, which);
//        mModeList.setAdapter(adapter);
//
//        mModeListDialog.show();
//    }

    private void showWifiList() {
        if (mWifiListDialog == null) {
            mWifiListDialog = new LEOBaseDialog(this, R.style.bt_dialog);
            mWifiListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mWifiListDialog.setContentView(R.layout.dialog_mode_list_select);
            View container = mWifiListDialog.findViewById(R.id.mode_list_container);
            mModeList = (ListView) container.findViewById(R.id.mode_list);
            mNoWifiTv = (TextView) container.findViewById(R.id.no_wifi);
            View cancel = mWifiListDialog.findViewById(R.id.dlg_bottom_btn);
            RippleView rippView = (RippleView) mWifiListDialog.findViewById(R.id.dlg_bottom_btn_ripp);
            rippView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWifiListDialog.dismiss();
                }
            });
//            cancel.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mWifiListDialog.dismiss();
//                }
//            });
        }
        TextView mTitle = (TextView) mWifiListDialog.findViewById(R.id.dlg_title);
        mTitle.setText(getResources().getString(R.string.select_wifi_mode));
        mModeList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiListAdapter adapter = (WifiListAdapter) parent.getAdapter();
                String ssid = adapter.getWifiList().get(position);
                mEditLocationLock.ssid = ssid;
                mTvSsid.setText(ssid);
                mWifiListDialog.dismiss();
                mEdited = true;
            }
        });

        List<String> wifiList = new ArrayList<String>();
//        WifiAdmin wa = new WifiAdmin(this);
        WifiAdmin wa = WifiAdmin.getInstance(this);
        wa.startScan();
        List<ScanResult> list = wa.getWifiList();
        if (list != null && !list.isEmpty()) {
            for (ScanResult scanResult : list) {
                if (!scanResult.SSID.equals("")) {
                    wifiList.add(scanResult.SSID);
                }
            }
        }
        if (wifiList != null && wifiList.size() > 0) {
            if (mNoWifiTv != null) {
                mNoWifiTv.setVisibility(View.GONE);
            }
            ListAdapter adapter = new WifiListAdapter(this, wifiList);
            mModeList.setAdapter(adapter);
        } else {
            if (mNoWifiTv != null) {
                mNoWifiTv.setVisibility(View.VISIBLE);
            }
            ListAdapter adapter = new WifiListAdapter(this, wifiList);
            mModeList.setAdapter(adapter);
        }
        mWifiListDialog.show();
    }


    private void saveLocationLock() {
        // name
        String name = mEtTimeLockName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            shakeView(mEtTimeLockName);
            Toast.makeText(LocationLockEditActivity.this,
                    R.string.please_input_name, Toast.LENGTH_SHORT).show();
            return;
        } else {
            mEditLocationLock.name = name;
            SDKWrapper.addEvent(this, SDKWrapper.P1, "local", "local");
        }

        // ssid
        if (TextUtils.isEmpty(mEditLocationLock.ssid)) {
            shakeView(mTvSsid);
            Toast.makeText(LocationLockEditActivity.this,
                    R.string.please_selset_wifi, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mNewLocationLock) {
            mEditLocationLock.using = true;
            mLockManager.addLocationLock(mEditLocationLock);
            Toast.makeText(
                    this,
                    this.getString(R.string.lock_change,
                            this.getString(R.string.lock_mode_location),
                            mEditLocationLock.name),
                    Toast.LENGTH_SHORT).show();
            if (mFromDialog) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "local", "dialog");
            }
        } else {
            mLockManager.updateLocationLock(mEditLocationLock);
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

    //TODO
    class ModeListAdapter extends BaseAdapter {

        private LockManager lm;
        private LayoutInflater inflater;
        private int which;

        public ModeListAdapter(Context ctx, int which) {
            lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
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
