
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;

public class LockModeFragment extends BaseFragment implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener, Editable {

    private ListView mModeListView;
    private View mListHeader;
    private List<LockMode> mModeList;
    private ModeAdapter mModeAdapter;

    private boolean mEditing;

    private LEOAlarmDialog mMakeSureChange;
    private int mSelectCount;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_lock_mode;
    }

    @Override
    protected void onInitUI() {
        mModeListView = (ListView) findViewById(R.id.mode_list);
        mModeListView.setOnItemClickListener(this);
        mModeListView.setOnItemLongClickListener(this);
        mListHeader = LayoutInflater.from(mActivity).inflate(R.layout.lock_mode_item_header,
                mModeListView, false);
        TextView tv = (TextView) mListHeader.findViewById(R.id.tv_add_more);
        tv.setText(R.string.add_mode);
        mModeListView.addHeaderView(mListHeader);

        loadModes();
        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadModes() {
        mModeList = LockManager.getInstatnce().getLockMode();
        mModeAdapter = new ModeAdapter(mActivity);
        mModeListView.setAdapter(mModeAdapter);

    }

    public void onEventMainThread(LockModeEvent event) {
        loadModes();
        mModeAdapter.notifyDataSetChanged();
    }

    private void showSelectDeleteDialog(final ImageView iv, final LockMode mode, final int resault) {
        if (mMakeSureChange == null) {
            mMakeSureChange = new LEOAlarmDialog(mActivity);
        }
        mMakeSureChange.setTitle(getString(R.string.lock_mode_delete_tip_title));

        if (resault == 0) {
            mMakeSureChange.setContent(getString(R.string.lock_mode_delete_tip_time));
        } else if (resault == 1) {
            mMakeSureChange.setContent(getString(R.string.lock_mode_delete_tip_location));
        } else if (resault == 2) {
            mMakeSureChange.setContent(getString(R.string.lock_mode_delete_tip_both));
        }

        mMakeSureChange.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 0) {

                } else if (which == 1) {
                    mode.selected = !mode.selected;
                    if (mode.selected) {
                        mSelectCount++;
                        iv.setImageResource(R.drawable.select);
                    } else {
                        mSelectCount--;
                        iv.setImageResource(R.drawable.unselect);
                    }
                    ((LockModeActivity) mActivity).onSelectItemChanged(mSelectCount);
                }
            }
        });
        mMakeSureChange.show();
    }

    private void showDeleteDialog(final List<LockMode> deleteList) {
        if (mMakeSureChange == null) {
            mMakeSureChange = new LEOAlarmDialog(mActivity);
        }
        mMakeSureChange.setTitle(getString(R.string.lock_mode_delete_tip_title));
        mMakeSureChange.setContent(getString(R.string.mode_delete_tip));

        mMakeSureChange.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 0) {

                } else if (which == 1) {
                    LockManager lm = LockManager.getInstatnce();
                    for (LockMode lockMode : deleteList) {
                        lm.removeLockMode(lockMode);
                    }
                    mSelectCount = 0;
                    ((LockModeActivity) mActivity).disableOptionImage();
                    mModeAdapter.notifyDataSetChanged();
                }
            }
        });
        mMakeSureChange.show();
    }

    private void showCurModeDeleteDialog(final LockMode mode, final List<LockMode> deleteList) {
        if (mMakeSureChange == null) {
            mMakeSureChange = new LEOAlarmDialog(mActivity);
        }
        mMakeSureChange.setTitle(getString(R.string.lock_mode_delete_tip_title));
        mMakeSureChange.setContent(getString(R.string.cur_mode_delete_tip, mode.modeName));

        mMakeSureChange.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                LockManager lm = LockManager.getInstatnce();
                if (which == 0) {
                    deleteList.remove(mode);
                    mode.selected = !mode.selected;
                } else if (which == 1) {

                }
                for (LockMode lockMode : deleteList) {
                    lm.removeLockMode(lockMode);
                }
                mSelectCount = 0;
                ((LockModeActivity) mActivity).disableOptionImage();
                mModeAdapter.notifyDataSetChanged();
            }
        });
        mMakeSureChange.show();
    }

    private void showShortcut(final LockMode mode) {
        if (mMakeSureChange == null) {
            mMakeSureChange = new LEOAlarmDialog(mActivity);
        }
        mMakeSureChange.setTitle(getString(R.string.create_mode_shortcut_title));
        mMakeSureChange.setContent(getString(R.string.create_mode_shortcut_content));
        mMakeSureChange.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 0) {
                } else if (which == 1) {
                    Toast.makeText(mActivity,
                            mActivity.getString(R.string.create_mode_shortcut_tip, mode.modeName),
                            Toast.LENGTH_SHORT).show();
                    installLockModeShortcut(mode);
                }
            }
        });
        mMakeSureChange.show();
    }

    @Override
    public void onDestroyView() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        LockMode mode = null;
        switch (view.getId()) {
            case R.id.img_mode_link:
                mode = (LockMode) view.getTag();
                showShortcut(mode);
                break;
            case R.id.iv_sort_select:
                mode = (LockMode) view.getTag();
                ImageView iv = (ImageView) view;
                if (mEditing) {
                    int checkResault = checkModeUsing(mode);
                    if (!mode.selected && checkResault != -1) {
                        showSelectDeleteDialog(iv, mode, checkResault);
                    } else {
                        mode.selected = !mode.selected;
                        if (mode.selected) {
                            mSelectCount++;
                            iv.setImageResource(R.drawable.select);
                        } else {
                            iv.setImageResource(R.drawable.unselect);
                            mSelectCount--;
                        }
                        ((LockModeActivity) mActivity).onSelectItemChanged(mSelectCount);
                    }

                } else {
                    LockManager.getInstatnce().setCurrentLockMode(mode, true);
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "modeschage", "modes");
                    Toast.makeText(mActivity,
                            mActivity.getString(R.string.mode_change, mode.modeName),
                            Toast.LENGTH_SHORT).show();
                    checkLockTip();
                    mModeAdapter.notifyDataSetChanged();
                }
                break;

            default:
                break;
        }

    }
    
    private void checkLockTip() {
        int switchCount = AppMasterPreference.getInstance(mActivity).getSwitchModeCount();
        switchCount++;
        AppMasterPreference.getInstance(mActivity).setSwitchModeCount(switchCount);
        LockManager lm = LockManager.getInstatnce();
        List<TimeLock> timeLockList = lm.getTimeLock();
        List<LocationLock> locationLockList = lm.getLocationLock();
        if (switchCount == 6) {
            // TODO show tip
            int timeLockCount = timeLockList.size();
            int locationLockCount = locationLockList.size();

            if (timeLockCount == 0 && locationLockCount == 0) {
                // show three btn dialog
                LEOThreeButtonDialog dialog = new LEOThreeButtonDialog(
                        mActivity);
                dialog.setTitle(R.string.time_location_lock_tip_title);
                String tip = mActivity.getString(R.string.time_location_lock_tip_content);
                dialog.setContent(tip);
                dialog.setLeftBtnStr(mActivity.getString(R.string.cancel));
                dialog.setMiddleBtnStr(mActivity.getString(R.string.lock_mode_time));
                dialog.setRightBtnStr(mActivity.getString(R.string.lock_mode_location));
                dialog.setRightBtnBackground(R.drawable.manager_mode_lock_third_button_selecter);
                dialog.setOnClickListener(new LEOThreeButtonDialog.OnDiaogClickListener() {
                    @Override
                    public void onClick(int which) {
                        Intent intent = null;
                        if (which == 0) {
                            // cancel
                        } else if (which == 1) {
                            // new time lock
                            intent = new Intent(mActivity, TimeLockEditActivity.class);
                            intent.putExtra("new_time_lock", true);
                            intent.putExtra("from_dialog", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mActivity.startActivity(intent);
                        } else if (which == 2) {
                            // new location lock
                            intent = new Intent(mActivity, LocationLockEditActivity.class);
                            intent.putExtra("new_location_lock", true);
                            intent.putExtra("from_dialog", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mActivity.startActivity(intent);
                        }
                    }
                });
//                dialog.getWindow().setType(
//                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
            } else {
                if (timeLockCount == 0 && locationLockCount != 0) {
                    // show time lock btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(mActivity);
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = mActivity.getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(mActivity.getString(R.string.lock_mode_time));
                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
                    dialog.setLeftBtnStr(mActivity.getString(R.string.cancel));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            Intent intent = null;
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                intent = new Intent(mActivity, TimeLockEditActivity.class);
                                intent.putExtra("new_time_lock", true);
                                intent.putExtra("from_dialog", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mActivity.startActivity(intent);
                            }

                        }
                    });
                    dialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();

                } else if (timeLockCount != 0 && locationLockCount == 0) {
                    // show lcaotion btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(mActivity);
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = mActivity.getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(mActivity.getString(R.string.lock_mode_location));
                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
                    dialog.setLeftBtnStr(mActivity.getString(R.string.cancel));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                Intent intent = new Intent(mActivity, LocationLockEditActivity.class);
                                intent.putExtra("new_location_lock", true);
                                intent.putExtra("from_dialog", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mActivity.startActivity(intent);
                            }

                        }
                    });
//                    dialog.getWindow().setType(
//                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();
                }
            }
        }
    }
    
    

    private int checkModeUsing(LockMode lockMode) {
        int resault = -1;
        LockManager lm = LockManager.getInstatnce();
        List<TimeLock> timeList = lm.getTimeLock();
        List<LocationLock> locationList = lm.getLocationLock();
        for (TimeLock timeLock : timeList) {
            if (timeLock.lockModeId == lockMode.modeId) {
                resault = 0;
                break;
            }
        }

        for (LocationLock locationLock : locationList) {
            if (locationLock.entranceModeId == lockMode.modeId
                    || locationLock.quitModeId == lockMode.modeId) {
                if (resault == 0) {
                    resault = 2;
                } else {
                    resault = 1;
                }
                break;
            }
        }
        return resault;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (position == 0) {
            addLockMode();
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "modesadd", "modes");
        } else {
            LockMode lockMode = mModeList.get(position - 1);
            if (lockMode.defaultFlag == 1) {
                if (!lockMode.haveEverOpened) {
                    Intent intent = new Intent(mActivity, RecommentAppLockListActivity.class);
                    intent.putExtra("target", 0);
                    startActivity(intent);
                    lockMode.haveEverOpened = true;
                    LockManager.getInstatnce().updateMode(lockMode);
                    return;
                }
            }
            editLockMode(lockMode, false);
        }
    }

    private void addLockMode() {
        Intent intent = new Intent(mActivity, LockModeEditActivity.class);
        intent.putExtra("mode_name", getString(R.string.new_mode));
        intent.putExtra("new_mode", true);
        startActivity(intent);
    }

    private void installLockModeShortcut(LockMode lockMode) {
        Intent shortcutIntent = null;
        if (lockMode.defaultFlag == 0) {
            shortcutIntent = new Intent(mActivity, UnlockAllModeProxyActivity.class);
        } else if (lockMode.defaultFlag == 1) {
            shortcutIntent = new Intent(mActivity, VisitorModeProxyActivity.class);
        } else if (lockMode.defaultFlag == 2) {
            shortcutIntent = new Intent(mActivity, OfficeModeProxyActivity.class);
        } else if (lockMode.defaultFlag == 3) {
            shortcutIntent = new Intent(mActivity, FamilyModeProxyActivity.class);
        } else {
            shortcutIntent = new Intent(mActivity, LockScreenActivity.class);
        }
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shortcutIntent.putExtra("quick_lock_mode", true);
        shortcutIntent.putExtra("lock_mode_id", lockMode.modeId);
        shortcutIntent.putExtra("lock_mode_name", lockMode.modeName);

        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, lockMode.modeName);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        /*
         * 0: unlock all; 1: visitor mode; 2: office mode; 3: family mode; -1:
         * other
         */
        ShortcutIconResource iconRes = null;
        if (lockMode.defaultFlag == 0) {
            iconRes = Intent.ShortcutIconResource
                    .fromContext(mActivity, R.drawable.lock_mode_unlock);
        } else if (lockMode.defaultFlag == 1) {
            iconRes = Intent.ShortcutIconResource
                    .fromContext(mActivity, R.drawable.lock_mode_visitor_desktop);
        } else if (lockMode.defaultFlag == 2) {
            iconRes = Intent.ShortcutIconResource
                    .fromContext(mActivity, R.drawable.lock_mode_office);
        } else if (lockMode.defaultFlag == 3) {
            iconRes = Intent.ShortcutIconResource
                    .fromContext(mActivity, R.drawable.lock_mode_family_desktop);
        } else {
            iconRes = Intent.ShortcutIconResource
                    .fromContext(mActivity, R.drawable.lock_mode_default_desktop);
        }
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        shortcut.putExtra("duplicate", false);
        shortcut.putExtra("from_shortcut", true);
        mActivity.sendBroadcast(shortcut);

        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "shortcuts", lockMode.modeName);
    }

    private void editLockMode(LockMode lockMode, boolean addNewMode) {
        Intent intent = new Intent(mActivity, LockModeEditActivity.class);
        if (addNewMode) {
            intent.putExtra("mode_name", mActivity.getString(R.string.new_mode));
            intent.putExtra("mode_id", -1);
        } else {
            intent.putExtra("mode_name", lockMode.modeName);
            intent.putExtra("mode_id", lockMode.modeId);
        }
        intent.putExtra("new_mode", addNewMode);
        startActivity(intent);
    }

    class ModeAdapter extends BaseAdapter {

        LayoutInflater mInflater;

        public ModeAdapter(Context ctx) {
            mInflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return mModeList.size();
        }

        @Override
        public Object getItem(int position) {
            return mModeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ModeHolder holder;
            if (convertView == null) {
                holder = new ModeHolder();
                convertView = mInflater.inflate(R.layout.item_lock_mode, parent, false);
                holder.modeName = (TextView) convertView.findViewById(R.id.tv_lock_mode_name);
                holder.modeLink = (ImageView) convertView.findViewById(R.id.img_mode_link);
                holder.modeSelect = (ImageView) convertView.findViewById(R.id.iv_sort_select);
                convertView.setTag(holder);
            } else {
                holder = (ModeHolder) convertView.getTag();
            }
            holder.lockMode = mModeList.get(position);
            holder.modeName.setText(holder.lockMode.modeName);

            if (mEditing) {
                holder.modeLink.setVisibility(View.INVISIBLE);
                if (holder.lockMode.defaultFlag != -1) {
                    holder.modeSelect.setVisibility(View.INVISIBLE);
                } else {
                    holder.modeSelect.setVisibility(View.VISIBLE);
                    if (holder.lockMode.selected) {
                        holder.modeSelect.setImageResource(R.drawable.select);
                    } else {
                        holder.modeSelect.setImageResource(R.drawable.unselect);
                    }
                }
            } else {
                holder.modeLink.setVisibility(View.VISIBLE);
                holder.modeSelect.setVisibility(View.VISIBLE);
                if (holder.lockMode.isCurrentUsed) {
                    holder.modeSelect.setImageResource(R.drawable.radio_buttons);
                } else {
                    holder.modeSelect.setImageResource(R.drawable.unradio_buttons);
                }
            }

            holder.modeSelect.setOnClickListener(LockModeFragment.this);
            holder.modeLink.setOnClickListener(LockModeFragment.this);

            holder.modeLink.setTag(holder.lockMode);
            holder.modeSelect.setTag(holder.lockMode);
            return convertView;
        }
    }

    class ModeHolder {
        TextView modeName;
        ImageView modeLink;
        ImageView modeSelect;

        LockMode lockMode;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            ((LockModeActivity) mActivity).onEditMode(0);
            mModeListView.setOnItemClickListener(null);
            mModeListView.removeHeaderView(mListHeader);
            mEditing = true;
            for (LockMode lock : mModeList) {
                lock.selected = false;
            }
            mModeAdapter.notifyDataSetChanged();
        }
        return false;
    }

    @Override
    public void onFinishEditMode() {
        mEditing = false;
        mModeListView.setOnItemClickListener(this);
        mModeListView.addHeaderView(mListHeader);
        mModeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChangeItem() {
        List<LockMode> deleteList = new ArrayList<LockMode>();
        for (LockMode lock : mModeList) {
            if (lock.selected) {
                deleteList.add(lock);
            }
        }

        if (deleteList.size() > 0) {
            LockManager lm = LockManager.getInstatnce();
            if (deleteList.contains(lm.getCurLockMode())) {
                showCurModeDeleteDialog(lm.getCurLockMode(), deleteList);
            } else {
                showDeleteDialog(deleteList);
            }
        }
    }
}
