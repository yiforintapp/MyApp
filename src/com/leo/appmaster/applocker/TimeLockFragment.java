
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.TimeLockEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.ui.CommonTitleBar;

public class TimeLockFragment extends BaseFragment implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener, Editable {

    private ListView mLockListView;
    private View mListHeader;
    
    private CommonTitleBar mTitleBar;
    private View mLockGuideView;
    private ImageView mLockGuideIcon;
    private TextView mLockGuideText;
    private Button mUserKnowBtn;
    private Animation mGuidAnimation;
    private  boolean mGuideOpen = false;
    
    private List<TimeLock> mTimeLockList;
    private TimeLockAdapter mTimeLockAdapter;
    private boolean mEditing;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //judge whether  setted  time lock mode
        mTimeLockList = LockManager.getInstatnce().getTimeLock();
        if(mTimeLockList.size()>0){
            AppMasterPreference.getInstance(mActivity).setTimeLockModeSetOver(true);
        }
    }
    
    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_lock_mode;
    }

    @Override
    protected void onInitUI() {
        mLockGuideView = findViewById(R.id.lock_mode_guide);
        mLockGuideIcon = (ImageView)mLockGuideView.findViewById(R.id.lock_guide_icon);
        mLockGuideText = (TextView) mLockGuideView.findViewById(R.id.lock_guide_text);
        mUserKnowBtn = (Button) mLockGuideView.findViewById(R.id.mode_user_know_button);
        mTitleBar =  ((LockModeActivity)mActivity).getActivityCommonTitleBar();
        
        mLockListView = (ListView) findViewById(R.id.mode_list);
        mLockListView.setOnItemClickListener(this);
        mLockListView.setOnItemLongClickListener(this);
        
        // judge whether click i know button 
       if(!AppMasterPreference.getInstance(mActivity).getTimeLockModeGuideClicked() && mTimeLockList.size()<=0 && 
               !AppMasterPreference.getInstance(mActivity).getTimeLockModeSetOVer()){
           showGuidePage();
      }

        mListHeader = LayoutInflater.from(mActivity).inflate(R.layout.lock_mode_item_header,
                mLockListView, false);
        TextView tv = (TextView) mListHeader.findViewById(R.id.tv_add_more);
        tv.setText(R.string.add_new_time_lock);
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
        mTimeLockList = LockManager.getInstatnce().getTimeLock();
        Collections.sort(mTimeLockList, new TimeLockComparator());
        mTimeLockAdapter = new TimeLockAdapter(mActivity);
        mLockListView.setAdapter(mTimeLockAdapter);

    }

    @Override
    public void onDestroyView() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroyView();
    }

    public void onEventMainThread(TimeLockEvent event) {
        mTimeLockList = LockManager.getInstatnce().getTimeLock();
        mTimeLockAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        TimeLock timeLock = null;
        switch (view.getId()) {
            case R.id.tv_select:
                timeLock = (TimeLock) view.getTag();
                ImageView iv = (ImageView) view;

                if (mEditing) {
                    timeLock.selected = !timeLock.selected;
                    if (timeLock.selected) {
                        iv.setImageResource(R.drawable.select);
                    } else {
                        iv.setImageResource(R.drawable.unselect);
                    }
                } else {
                    if (timeLock.using) {
                        timeLock.using = false;
                    } else {
                        timeLock.using = true;
                    }
                    if (timeLock.using) {
                        iv.setImageResource(R.drawable.switch_on);
                    } else {
                        iv.setImageResource(R.drawable.switch_off);
                    }
                    LockManager.getInstatnce().openTimeLock(timeLock, timeLock.using);
                }

                mTimeLockAdapter.notifyDataSetChanged();
                break;
            case R.id.mode_user_know_button:
                AppMasterPreference.getInstance(mActivity).setTimeLockModeGuideClicked(true);
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
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            addTimeLock();
        } else {
            TimeLock timeLock = mTimeLockList.get(position - 1);
            editTimeLock(timeLock, false);
        }

    }

    private void addTimeLock() {
        Intent intent = new Intent(mActivity, TimeLockEditActivity.class);
        intent.putExtra("new_time_lock", true);
        startActivity(intent);
    }

    private void editTimeLock(TimeLock lockMode, boolean addNewMode) {
        Intent intent = new Intent(mActivity, TimeLockEditActivity.class);
        if (addNewMode) {
            intent.putExtra("new_time_lock", true);
        } else {
            intent.putExtra("time_lock_id", lockMode.id);
        }
        startActivity(intent);
    }

    class TimeLockAdapter extends BaseAdapter {

        LayoutInflater mInflater;

        public TimeLockAdapter(Context ctx) {
            mInflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return mTimeLockList.size();
        }

        @Override
        public Object getItem(int position) {
            return mTimeLockList.get(position);
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
                holder.timeLockName = (TextView) convertView.findViewById(R.id.tv_time_lock_name);
                holder.time = (TextView) convertView.findViewById(R.id.tv_time);
                holder.repeat = (TextView) convertView.findViewById(R.id.tv_repeat_mode);
                holder.select = (ImageView) convertView.findViewById(R.id.tv_select);
                convertView.setTag(holder);
            } else {
                holder = (TimeLockHolder) convertView.getTag();
            }
            holder.timeLock = mTimeLockList.get(position);
            holder.timeLockName.setText(holder.timeLock.name);
            holder.time.setText(holder.timeLock.time.toString());

            String repeat = holder.timeLock.repeatMode.toString();
            if (!TextUtils.isEmpty(repeat)) {
                holder.repeat.setText(repeat);
            } else {
                holder.repeat.setText(getText(R.string.no_repeat));
            }

            if (mEditing) {
                if (holder.timeLock.selected) {
                    holder.select.setImageResource(R.drawable.select);
                } else {
                    holder.select.setImageResource(R.drawable.unselect);
                }
            } else {
                if (holder.timeLock.using) {
                    holder.select.setImageResource(R.drawable.switch_on);
                } else {
                    holder.select.setImageResource(R.drawable.switch_off);
                }
            }

            holder.select.setOnClickListener(TimeLockFragment.this);
            holder.select.setTag(holder.timeLock);
            return convertView;
        }
    }

    class TimeLockHolder {
        TextView timeLockName;
        TextView time;
        TextView repeat;
        ImageView select;
        TimeLock timeLock;
    }

    class TimeLockComparator implements Comparator<TimeLock> {

        @Override
        public int compare(TimeLock lhs, TimeLock rhs) {
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
            ((LockModeActivity) mActivity).onEditMode(1);
            mLockListView.setOnItemClickListener(null);
            mEditing = true;
            mLockListView.removeHeaderView(mListHeader);
            mTimeLockAdapter.notifyDataSetChanged();
        }
        return false;
    }

    @Override
    public void onFinishEditMode() {
        mEditing = false;
        mLockListView.setOnItemClickListener(this);
        mLockListView.addHeaderView(mListHeader);
        mTimeLockAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChangeItem() {
        List<TimeLock> deleteList = new ArrayList<TimeLock>();
        for (TimeLock lock : mTimeLockList) {
            if (lock.selected) {
                deleteList.add(lock);
            }
        }
        LockManager lm = LockManager.getInstatnce();
        for (TimeLock lock : deleteList) {
            lm.removeTimeLock(lock);
        }
        mTimeLockAdapter.notifyDataSetChanged();
    }

    /** about lock mode guide **/
    public void lockGuide() {
        if (mGuideOpen) {
            removeGuidePage();
        } else {
            showGuidePage();
        }
    }

    private void showGuidePage() {
        mLockListView.setVisibility(View.INVISIBLE);
        mLockGuideView.setVisibility(View.VISIBLE);
        mLockGuideIcon.setImageResource(R.drawable.modes_tips_time);
        mLockGuideText.setText(R.string.time_lock_mode_guide_content);
        mUserKnowBtn.setOnClickListener(this);
        // if ever pack up guide page then  next time guide page should appearance as animation
        if (AppMasterPreference.getInstance(mActivity).getTimeLockModeGuideClicked()) {
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
