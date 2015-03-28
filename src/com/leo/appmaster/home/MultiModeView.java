
package com.leo.appmaster.home;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockModeEditActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CirclePageIndicator;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class MultiModeView extends RelativeLayout implements OnClickListener {

    private CirclePageIndicator mIndicator;
    private ViewPager mViewPager;
    private View mPagerContainer;
    private PagerAdapter mAdapter;
    private List<View> mViews;
    private ImageView mIvAdd;
    private View mSelected;
    private View mHolder;
    private LockManager mLockManager;

    public MultiModeView(Context context) {
        super(context);
    }

    public MultiModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLockManager = LockManager.getInstatnce();
    }

    @Override
    protected void onAttachedToWindow() {
        LeoEventBus.getDefaultBus().register(this);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDetachedFromWindow();
    }

    public void onEventMainThread(LockModeEvent event) {
        fillUI();
        // mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.multi_mode_view, this, true);
        mPagerContainer = view.findViewById(R.id.pager_container);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(LockManager.getInstatnce().getLockMode().size());
        mViewPager.setPageMargin(DipPixelUtil.dip2px(getContext(), 46));
        mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        mIvAdd = (ImageView) view.findViewById(R.id.img_add);
        mIvAdd.setOnClickListener(this);
        this.setOnClickListener(this);
        mPagerContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.dispatchTouchEvent(event);
                // return false;
            }
        });
        fillUI();
        super.onFinishInflate();
    }

    private void fillUI() {
        mViews = new ArrayList<View>();
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        List<LockMode> list = LockManager.getInstatnce().getLockMode();
        for (LockMode lockMode : list) {
            View view = mInflater.inflate(R.layout.mode_page_item, mViewPager, false);
            mHolder = view.findViewById(R.id.mode_holder);
            TextView modeIcon = (TextView) view.findViewById(R.id.tv_lock_mode_icon);
            ImageView selectedImg = (ImageView) view.findViewById(R.id.img_selected);
            TextView name = (TextView) view.findViewById(R.id.tv_mode_name);
            name.setText(lockMode.modeName);
            if (lockMode.isCurrentUsed) {
                modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(), BitmapUtils
                        .createGaryBitmap(lockMode.modeIcon))));
                selectedImg.setVisibility(View.VISIBLE);
                mSelected = mHolder;
            } else {
                modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(),
                        lockMode.modeIcon)));
                selectedImg.setVisibility(View.GONE);
            }
            mHolder.setTag(lockMode);
            mHolder.setOnClickListener(this);
            mViews.add(view);
        }
        mAdapter = new ModeAdapter(getContext());
        mViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewPager);
        moveToCurItem();
    }

    public void show() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
            fillUI();
        }
    }

    private void addLockMode() {
        Intent intent = new Intent(this.getContext(), LockModeEditActivity.class);
        intent.putExtra("mode_name", getContext().getString(R.string.new_mode));
        intent.putExtra("new_mode", true);
        getContext().startActivity(intent);
    }

    private void moveToCurItem() {
        LockManager lm = LockManager.getInstatnce();
        List<LockMode> modeList = lm.getLockMode();
        int index = 0;
        for (int i = 0; i < modeList.size(); i++) {
            if (modeList.get(i).isCurrentUsed) {
                index = i;
                break;
            }
        }
        mViewPager.setCurrentItem(index, true);
    }

    public void hide() {
        if (getVisibility() == View.VISIBLE) {
            setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.mode_holder) {
            LockMode mode = (LockMode) v.getTag();
            if (v == mSelected)
                return;

            int position = mViews.indexOf(v.getParent());
            int curPosition = mViewPager.getCurrentItem();

            if (position == curPosition) {
                LockMode lastSelectedMode = (LockMode) mSelected.getTag();
                TextView modeIcon = (TextView) mSelected.findViewById(R.id.tv_lock_mode_icon);
                ImageView selectedImg = (ImageView) mSelected.findViewById(R.id.img_selected);
                selectedImg.setVisibility(View.GONE);
                modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(),
                        lastSelectedMode.modeIcon)));

                modeIcon = (TextView) v.findViewById(R.id.tv_lock_mode_icon);
                selectedImg = (ImageView) v.findViewById(R.id.img_selected);
                selectedImg.setVisibility(View.VISIBLE);
                modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(), BitmapUtils
                        .createGaryBitmap(mode.modeIcon))));

                mSelected = v;

//                if (mode.defaultFlag == 1 && !mode.haveEverOpened) {
//                    mode.haveEverOpened = true;
//                    mLockManager.setCurrentLockMode(mode);
//                    startRcommendLock();
//                } else {
                    mLockManager.setCurrentLockMode(mode);
//                }
                    SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "modeschage", "home");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LeoEventBus.getDefaultBus()
                                .post(
                                        new LockModeEvent(EventId.EVENT_MODE_CHANGE,
                                                "multi mode page selectd"));
                    }
                }).start();
            }
            postDelayed(new Runnable() {

                @Override
                public void run() {
                    hide();
                }
            }, 200);

        }

        if (v == this) {
            hide();
        } else if (v == mIvAdd) {
            addLockMode();
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "modesadd", "home");
        }
    }

    private void startRcommendLock() {
        Intent intent = new Intent(getContext(), RecommentAppLockListActivity.class);
        getContext().startActivity(intent);
    }

    class ModeAdapter extends PagerAdapter {

        LayoutInflater mInflater;

        public ModeAdapter(Context ctx) {
            mInflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViews.get(position);
            container.addView(view);
            return view;
        }
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }
    
}
