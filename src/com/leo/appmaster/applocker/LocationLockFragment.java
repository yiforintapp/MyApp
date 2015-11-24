
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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LocationLockEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.RippleView1;

public class LocationLockFragment extends BaseFragment implements OnClickListener,
        OnItemClickListener, OnItemLongClickListener, Editable{

    private ListView mLockListView;
    private View mListHeader;
    private List<LocationLock> mLocationLockList;
    private LocationLockAdapter mLocationLockAdapter;
    private boolean mEditing;

    private CommonTitleBar mTitleBar;
    private View mLockGuideView;
    private ImageView mLockGuideIcon;
    private TextView mLockGuideText;
    private RippleView1 mUserKnowBtn;
    private Animation mGuidAnimation;
    private boolean mGuideOpen = false;
    private int mSelectCount = 0;

    private LockManager mLockManager;
    private View mHeadContentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // judge whether setted location lock mode

        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        mLocationLockList = mLockManager.getLocationLock();
        if (mLocationLockList.size() > 0) {
            AppMasterPreference.getInstance(mActivity).setLocationLockModeSetOver(true);
        }
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_lock_mode;
    }

    @Override
    protected void onInitUI() {
        mLockGuideView = findViewById(R.id.lock_mode_guide);
        mLockGuideIcon = (ImageView) mLockGuideView.findViewById(R.id.lock_guide_icon);
        mLockGuideText = (TextView) mLockGuideView.findViewById(R.id.lock_guide_text);

        mUserKnowBtn = (RippleView1) mLockGuideView.findViewById(R.id.mode_user_know_button);

        mTitleBar = ((LockModeActivity) mActivity).getActivityCommonTitleBar();

        mLockListView = (ListView) findViewById(R.id.mode_list);
        mLockListView.setOnItemClickListener(this);
        mLockListView.setOnItemLongClickListener(this);

        // if don't pack up the guide page and have not been set location lock
        // mode
        if (!AppMasterPreference.getInstance(mActivity).getLocationLockModeGuideClicked() &&
                !AppMasterPreference.getInstance(mActivity).getLocationLockModeSetOVer()) {
            showGuidePage();
        }

        mListHeader = LayoutInflater.from(mActivity).inflate(R.layout.lock_mode_item_header,
                mLockListView, false);
        mHeadContentView = mListHeader.findViewById(R.id.head_content);
        mHeadContentView.setOnClickListener(this);
        MaterialRippleLayout.on(mHeadContentView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleHover(true)
                .create();
        TextView tv = (TextView) mListHeader.findViewById(R.id.tv_add_more);
        tv.setText(R.string.add_new_location_lock);
        mLockListView.addHeaderView(mListHeader);
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
        mLocationLockList = mLockManager.getLocationLock();
        Collections.sort(mLocationLockList, new LocationLockComparator());
        mLocationLockAdapter = new LocationLockAdapter(mActivity);
        mLockListView.setAdapter(mLocationLockAdapter);

    }

    @Override
    public void onDestroyView() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroyView();
    }

    public void onEventMainThread(LocationLockEvent event) {
        mLocationLockList = mLockManager.getLocationLock();
        mLocationLockAdapter.notifyDataSetChanged();
        // cancle guide page
        if (mLocationLockList.size() == 1) {
            mLockGuideView.setVisibility(View.INVISIBLE);
            mLockListView.setVisibility(View.VISIBLE);
            mGuideOpen = false;
        }
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
                        mSelectCount++;
                        iv.setImageResource(R.drawable.select);
                    } else {
                        mSelectCount--;
                        iv.setImageResource(R.drawable.unselect);
                    }
                    ((LockModeActivity) mActivity).onSelectItemChanged(mSelectCount);
                } else {
                    locationLock.using = !locationLock.using;
                    if (locationLock.using) {
                        iv.setImageResource(R.drawable.switch_on);
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "local", "open");
                    } else {
                        iv.setImageResource(R.drawable.switch_off);
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "local", "close");
                    }
                    mLockManager.openLocationLock(locationLock, locationLock.using);
                }
                break;
            case R.id.mode_user_know_button:
                AppMasterPreference.getInstance(mActivity).setLocationLockModeGuideClicked(true);
                removeGuidePage();
                /** set the help tip action **/
                mTitleBar.setOptionImage(R.drawable.tips_icon);
                mTitleBar.setOptionImageVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.help_tip_show);
                mTitleBar.setOptionAnimation(animation);
                mTitleBar.setOptionListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lockGuide();
                    }
                });
                break;
            case R.id.head_content:
                addLocationTime();
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
//            addLocationTime();
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

//    @Override
//    public void onRippleComplete(RippleView rippleView) {
//        if (mUserKnowBtn == rippleView) {
//            AppMasterPreference.getInstance(mActivity).setLocationLockModeGuideClicked(true);
//            removeGuidePage();
//            /** set the help tip action **/
//            mTitleBar.setOptionImage(R.drawable.tips_icon);
//            mTitleBar.setOptionImageVisibility(View.VISIBLE);
//            Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.help_tip_show);
//            mTitleBar.setOptionAnimation(animation);
//            mTitleBar.setOptionListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    lockGuide();
//                }
//            });
//        }
//    }

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
        if (position != 0) {
            ((LockModeActivity) mActivity).onEditMode(2);
            mLockListView.setOnItemClickListener(null);
            mEditing = true;
            mLockListView.removeHeaderView(mListHeader);
            for (LocationLock lock : mLocationLockList) {
                lock.selected = false;
            }
            mLocationLockAdapter.notifyDataSetChanged();
        }
        return false;
    }

    @Override
    public void onFinishEditMode() {
        mEditing = false;
        mLockListView.setOnItemClickListener(this);
        mLockListView.addHeaderView(mListHeader);
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
        for (LocationLock lock : deleteList) {
            mLockManager.removeLocationLock(lock);
        }
        mSelectCount = 0;
        ((LockModeActivity) mActivity).disableOptionImage();
        mLocationLockAdapter.notifyDataSetChanged();
    }

    /**
     * about lock mode guide
     **/
    public void lockGuide() {
        if (mGuideOpen) {
            removeGuidePage();
        } else {
            showGuidePage();
        }
    }

    private void showGuidePage() {
        mLockGuideView.setVisibility(View.VISIBLE);
        mLockListView.setVisibility(View.INVISIBLE);
        mLockGuideIcon.setImageResource(R.drawable.modes_tips_position);
        mLockGuideText.setText(R.string.location_lock_mode_guide_content);
        mUserKnowBtn.setOnClickListener(this);
        // if ever pack up guide page then next time guide page should
        // appearance as animation
        if (AppMasterPreference.getInstance(mActivity).getLocationLockModeGuideClicked()) {
            mGuidAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.lock_mode_guide_in);
            mLockGuideView.startAnimation(mGuidAnimation);
        }
        mGuideOpen = true;
    }

    private void removeGuidePage() {
        mLockGuideView.setVisibility(View.INVISIBLE);
        mLockListView.setVisibility(View.VISIBLE);
        mGuidAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.lock_mode_guide_out);
        mLockGuideView.startAnimation(mGuidAnimation);
        mGuideOpen = false;
    }

    /**
     * open : true
     */
    public boolean getGuideOpenState() {
        return this.mGuideOpen;
    }
}
