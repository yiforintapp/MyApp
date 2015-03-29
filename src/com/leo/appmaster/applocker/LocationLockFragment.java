
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LocationLockEvent;
import com.leo.appmaster.eventbus.event.TimeLockEvent;
import com.leo.appmaster.fragment.BaseFragment;

public class LocationLockFragment extends BaseFragment implements OnClickListener,
        OnItemClickListener, OnItemLongClickListener, Editable {

    private ListView mModeListView;
    private View mListHeader;
    private List<LocationLock> mLocationLockList;
    private LocationLockAdapter mLocationLockAdapter;
    private boolean mEditing;

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
        tv.setText(R.string.add_new_location_lock);
        mModeListView.addHeaderView(mListHeader);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LeoEventBus.getDefaultBus().register(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        loadModes();
        super.onResume();
    }

    private void loadModes() {
        mLocationLockList = LockManager.getInstatnce().getLocationLock();
        Collections.sort(mLocationLockList, new LocationLockComparator());
        mLocationLockAdapter = new LocationLockAdapter(mActivity);
        mModeListView.setAdapter(mLocationLockAdapter);

    }

    @Override
    public void onDestroyView() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroyView();
    }

    public void onEventMainThread(LocationLockEvent event) {
        mLocationLockList = LockManager.getInstatnce().getLocationLock();
        mLocationLockAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        LocationLock locationLock = null;
        switch (view.getId()) {
            case R.id.tv_select:
                locationLock = (LocationLock) view.getTag();
                ImageView iv = (ImageView) view;
                if (mEditing) {
                    locationLock.selected = !locationLock.selected;
                    if (locationLock.selected) {
                        iv.setImageResource(R.drawable.select);
                    } else {
                        iv.setImageResource(R.drawable.unselect);
                    }
                } else {
                    locationLock.using = !locationLock.using;
                    if (locationLock.using) {
                        iv.setImageResource(R.drawable.switch_on);
                    } else {
                        iv.setImageResource(R.drawable.switch_off);
                    }
                    LockManager.getInstatnce().openLocationLock(locationLock, locationLock.using);
                }
                break;

            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            addLocationTime();
        } else {
            LocationLock locationLock = mLocationLockList.get(position - 1);
            editLocationLock(locationLock, false);
        }
    }

    private void addLocationTime() {
        Intent intent = new Intent(mActivity, LocationLockEditActivity.class);
        intent.putExtra("new_location_lock", true);
        startActivity(intent);
    }

    private void editLocationLock(LocationLock lockMode, boolean addNewMode) {
        Intent intent = new Intent(mActivity, LocationLockEditActivity.class);
        if (addNewMode) {
            intent.putExtra("new_location_lock", true);
        } else {
            intent.putExtra("location_lock_id", lockMode.id);
        }
        startActivity(intent);
    }

    class LocationLockAdapter extends BaseAdapter {

        LayoutInflater mInflater;

        public LocationLockAdapter(Context ctx) {
            mInflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return mLocationLockList.size();
        }

        @Override
        public Object getItem(int position) {
            return mLocationLockList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TimeLockHolder holder;
            if (convertView == null) {
                holder = new TimeLockHolder();
                convertView = mInflater.inflate(R.layout.item_time_lock, parent, false);
                convertView.findViewById(R.id.tv_time).setVisibility(View.INVISIBLE);
                holder.locationLockName = (TextView) convertView
                        .findViewById(R.id.tv_time_lock_name);
                holder.ssid = (TextView) convertView.findViewById(R.id.tv_repeat_mode);
                holder.select = (ImageView) convertView.findViewById(R.id.tv_select);
                convertView.setTag(holder);
            } else {
                holder = (TimeLockHolder) convertView.getTag();
            }

            holder.locationLock = mLocationLockList.get(position);
            holder.locationLockName.setText(holder.locationLock.name);
            holder.ssid.setText(holder.locationLock.ssid);
            if (!mEditing) {
                if (holder.locationLock.using) {
                    holder.select.setImageResource(R.drawable.switch_on);
                } else {
                    holder.select.setImageResource(R.drawable.switch_off);
                }
            } else {
                if (holder.locationLock.selected) {
                    holder.select.setImageResource(R.drawable.select);
                } else {
                    holder.select.setImageResource(R.drawable.unselect);
                }
            }
            holder.select.setOnClickListener(LocationLockFragment.this);
            holder.select.setTag(holder.locationLock);
            return convertView;
        }
    }

    class TimeLockHolder {
        TextView locationLockName;
        TextView ssid;
        ImageView select;
        LocationLock locationLock;
    }

    class LocationLockComparator implements Comparator<LocationLock> {

        @Override
        public int compare(LocationLock lhs, LocationLock rhs) {
            if (lhs.using && !rhs.using) {
                return 0;
            } else if (!lhs.using && rhs.using) {
                return 1;
            }
            return 0;
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ((LockModeActivity) mActivity).onEditMode(2);
        mModeListView.setOnItemClickListener(null);
        mEditing = true;
        mLocationLockAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public void onFinishEditMode() {
        mEditing = false;
        mModeListView.setOnItemClickListener(this);
        mLocationLockAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChangeItem() {
        List<LocationLock> deleteList = new ArrayList<LocationLock>();
        for (LocationLock lock : mLocationLockList) {
            if (lock.selected) {
                deleteList.add(lock);
            }
        }
        LockManager lm = LockManager.getInstatnce();
        for (LocationLock lock : deleteList) {
            lm.removeLocationLock(lock);
        }
        mLocationLockAdapter.notifyDataSetChanged();
    }
}
