
package com.leo.appmaster.home;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LocationLockEditActivity;
import com.leo.appmaster.applocker.LockModeEditActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.TimeLockEditActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CirclePageIndicator;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

public class MultiModeView extends RelativeLayout implements OnClickListener {

    private static final String SHOW_NOW = "mode changed_show_now";
    private static final String START_FROM_ADD = "startFromadd";
    private CirclePageIndicator mIndicator;
    private ViewPager mViewPager;
    private View mPagerContainer;
    private PagerAdapter mAdapter;
    private List<View> mViews;
    private TextView mModeNameTv;
    private ImageView mIvAdd;
    private View mSelected;
    private View mHolder;
    private LockManager mLockManager;
    private int currModePosition;
    private Bitmap grayBitmap;

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
        LeoLog.d("testMultiModeView", "event.eventMsg : " + event.eventMsg);
        if (event.eventMsg.equals(SHOW_NOW) && getVisibility() == View.VISIBLE) {
            backgroundAnimtion();
            fillUI(true);
        }
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.multi_mode_view, this, true);
        mModeNameTv = (TextView) view.findViewById(R.id.mode_name_tv);
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
            }
        });
        fillUI(true);
        super.onFinishInflate();
    }

    private void fillUI(boolean showAnimation) {
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
                mSelected = mHolder;
                currModePosition = list.indexOf(lockMode);
                mIvAdd.post(new Runnable() {
                    @Override
                    public void run() {
                        grayBitmap = BitmapUtils.createGaryBitmap(((LockMode) mSelected.getTag()).modeIcon);
                    }
                });
            }
            selectedImg.setVisibility(View.GONE);
            modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(),
                    lockMode.modeIcon)));

            mHolder.setTag(lockMode);
            mHolder.setOnClickListener(this);
            mHolder.setOnTouchListener(new ModeTouchListener());
            mViews.add(view);
        }
        mAdapter = new ModeAdapter(getContext(), showAnimation);
        mViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewPager);
        moveToCurItem();
    }

    public void show() {

        mModeNameTv.setVisibility(View.INVISIBLE);
        mIvAdd.setVisibility(View.INVISIBLE);
        backgroundAnimtion();
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
        fillUI(true);
    }

    private void backgroundAnimtion() {
        ValueAnimator bgAnim = ObjectAnimator.ofFloat(this, "alpha", 0, 1.0f);
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0f, 1.0f);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mModeNameTv.setVisibility(View.VISIBLE);
                mIvAdd.setVisibility(View.VISIBLE);
            }
        });
        alphaAnimator.setStartDelay(400);
        alphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currValue = (Float) animation.getAnimatedValue();
                mModeNameTv.setAlpha(currValue);
                mIvAdd.setAlpha(currValue);
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playTogether(bgAnim, alphaAnimator);
        set.setDuration(300);
        set.start();
    }

    private void addLockMode() {
        Intent intent = new Intent(this.getContext(), LockModeEditActivity.class);
        intent.setAction(START_FROM_ADD);
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
        if (v.getId() == R.id.mode_holder && v == mSelected) {
            hide();
        }
        if (v == this) {
            hide();
        } else if (v == mIvAdd) {
            addLockMode();
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "modesadd", "home");
            // TODO
            hide();
        }
    }

    private void startRcommendLock() {
        Intent intent = new Intent(getContext(), RecommentAppLockListActivity.class);
        getContext().startActivity(intent);
    }

    class ModeAdapter extends PagerAdapter {

        private LayoutInflater mInflater;
        private boolean showAnimation;
        Set<Integer> poSet = new HashSet<Integer>();

        public ModeAdapter(Context ctx) {
            mInflater = LayoutInflater.from(ctx);
        }

        public ModeAdapter(Context ctx, boolean showAnimation) {
            this.showAnimation = showAnimation;
            int currNextPostion = currModePosition + 1 >= mViews.size() ? currModePosition
                    : currModePosition + 1;
            int currPrePosition = currModePosition - 1 < 0 ? 0 : currModePosition - 1;
            poSet.add(currNextPostion);
            poSet.add(currPrePosition);
            poSet.add(currModePosition);
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
            try {
                container.removeView(mViews.get(position));
            } catch (Exception e) {
                try {
                    container.removeView(mViews.get(position - 1));
                } catch (Exception e2) {
                }
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViews.get(position);

            if (showAnimation && poSet.contains(position)) {
                ballAnimtion(view).start();
            }
            container.addView(view);
            return view;
        }
    }

    /**
     * get the mode ball appear animation
     * 
     * @param view
     * @return
     */
    private AnimatorSet ballAnimtion(final View view) {
        final View holder = view.findViewById(R.id.mode_holder);
        final TextView modeIcon = (TextView) view.findViewById(R.id.tv_lock_mode_icon);
        final ImageView selectedImg = (ImageView) view.findViewById(R.id.img_selected);
        final TextView modeName = (TextView) view.findViewById(R.id.tv_mode_name);
        modeName.setAlpha(0);

        ValueAnimator ballAnim1 = ValueAnimator.ofFloat(0.5f, 1.05f).setDuration(300);
        ballAnim1.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (Float) animation.getAnimatedValue();
                holder.setScaleX(currentValue);
                holder.setScaleY(currentValue);
            }
        });
        ValueAnimator ballAnim2 = ValueAnimator.ofFloat(1.0f, 0.95f, 1.0f).setDuration(300);
        ballAnim2.addUpdateListener(new AnimatorUpdateListener() {
            boolean flag = true;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (Float) animation.getAnimatedValue();
                holder.setScaleX(currentValue);
                holder.setScaleY(currentValue);
                if (flag && mSelected == holder && animation.getCurrentPlayTime() > 200
                        && grayBitmap != null) {
                    modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(), grayBitmap)));
                    flag = false;
                }
            }
        });

        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0f, 1.0f).setDuration(300);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mSelected == holder) {
                    selectedImg.setVisibility(View.VISIBLE);
                }
            }
        });
        alphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currValue = (Float) animation.getAnimatedValue();
                if (mSelected == holder) {
                    selectedImg.setAlpha(currValue);
                }
                modeName.setAlpha(currValue);
            }
        });

        AnimatorSet part = new AnimatorSet();
        part.playTogether(ballAnim2, alphaAnimator);
        AnimatorSet set = new AnimatorSet();
        set.play(ballAnim1).before(part);
        return set;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

    private class ModeTouchListener implements View.OnTouchListener {
        ImageView selectedImg;
        TextView modeIcon;

        @SuppressWarnings("deprecation")
        @Override
        public boolean onTouch(final View view, MotionEvent event) {
            int action = event.getAction();
            int position = mViews.indexOf(view.getParent());
            int curPosition = mViewPager.getCurrentItem();
            if (view.getId() == R.id.mode_holder) {
                if (view != mSelected && curPosition == position) {
                    final LockMode mode = (LockMode) view.getTag();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            view.setScaleX(0.9f);
                            view.setScaleY(0.9f);
                            break;
                        case MotionEvent.ACTION_UP:
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);
                            LockMode lastSelectedMode = (LockMode) mSelected.getTag();
                            modeIcon = (TextView) mSelected.findViewById(R.id.tv_lock_mode_icon);
                            selectedImg = (ImageView) mSelected.findViewById(R.id.img_selected);
                            selectedImg.setVisibility(View.GONE);
                            modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(),
                                    lastSelectedMode.modeIcon)));

                            modeIcon = (TextView) view.findViewById(R.id.tv_lock_mode_icon);
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mode != null && mode.modeIcon != null) {
                                        modeIcon.setBackgroundDrawable((new BitmapDrawable(
                                                getResources(),
                                                BitmapUtils.createGaryBitmap(mode.modeIcon))));
                                    }
                                    selectedImg.setVisibility(View.VISIBLE);
                                }
                            });
                            selectedImg = (ImageView) view.findViewById(R.id.img_selected);

                            mSelected = view;
                            mLockManager.setCurrentLockMode(mode, true);
                            checkLockTip();
                            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "modeschage", "home");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    LeoEventBus.getDefaultBus().post(
                                            new LockModeEvent(EventId.EVENT_MODE_CHANGE,
                                                    "multi mode page selectd"));
                                }
                            }).start();

                            disappearAnim(curPosition, selectedImg);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            }
            return false;
        }
    }
    
    private void checkLockTip() {
        int switchCount = AppMasterPreference.getInstance(this.getContext()).getSwitchModeCount();
        switchCount++;
        AppMasterPreference.getInstance(this.getContext()).setSwitchModeCount(switchCount);
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
                        this.getContext());
                dialog.setTitle(R.string.time_location_lock_tip_title);
                String tip = this.getContext().getString(R.string.time_location_lock_tip_content);
                dialog.setContent(tip);
                dialog.setLeftBtnStr(this.getContext().getString(R.string.cancel));
                dialog.setMiddleBtnStr(this.getContext().getString(R.string.lock_mode_time));
                dialog.setRightBtnStr(this.getContext().getString(R.string.lock_mode_location));
                dialog.setRightBtnBackground(R.drawable.manager_mode_lock_third_button_selecter);
                dialog.setOnClickListener(new LEOThreeButtonDialog.OnDiaogClickListener() {
                    @Override
                    public void onClick(int which) {
                        Intent intent = null;
                        if (which == 0) {
                            // cancel
                        } else if (which == 1) {
                            // new time lock
                            intent = new Intent(getContext(), TimeLockEditActivity.class);
                            intent.putExtra("new_time_lock", true);
                            intent.putExtra("from_dialog", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);
                        } else if (which == 2) {
                            // new location lock
                            intent = new Intent(getContext(), LocationLockEditActivity.class);
                            intent.putExtra("new_location_lock", true);
                            intent.putExtra("from_dialog", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);
                        }
                    }
                });
//                dialog.getWindow().setType(
//                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
            } else {
                if (timeLockCount == 0 && locationLockCount != 0) {
                    // show time lock btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(this.getContext());
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = this.getContext().getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(this.getContext().getString(R.string.lock_mode_time));
                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
                    dialog.setLeftBtnStr(this.getContext().getString(R.string.cancel));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            Intent intent = null;
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                intent = new Intent(getContext(), TimeLockEditActivity.class);
                                intent.putExtra("new_time_lock", true);
                                intent.putExtra("from_dialog", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(intent);
                            }

                        }
                    });
                    dialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();

                } else if (timeLockCount != 0 && locationLockCount == 0) {
                    // show lcaotion btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(this.getContext());
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = this.getContext().getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(this.getContext().getString(R.string.lock_mode_location));
                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
                    dialog.setLeftBtnStr(this.getContext().getString(R.string.cancel));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                Intent intent = new Intent(getContext(), LocationLockEditActivity.class);
                                intent.putExtra("new_location_lock", true);
                                intent.putExtra("from_dialog", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(intent);
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

    /**
     * this mode ball disapper animation
     * 
     * @param curPosition
     * @param selectedImg
     */
    private void disappearAnim(int curPosition, View selectedImg) {
        View prePage = mViews.get(curPosition == 0 ? 0 : curPosition - 1);
        View nextPage = mViews
                .get(curPosition == mViews.size() - 1 ? curPosition : curPosition + 1);
        final TextView prePageModeName = (TextView) prePage.findViewById(R.id.tv_mode_name);
        final TextView nextPageModeName = (TextView) nextPage.findViewById(R.id.tv_mode_name);
        final TextView thisModeName = (TextView) mViews.get(curPosition).findViewById(
                R.id.tv_mode_name);

        ValueAnimator textAnim = ValueAnimator.ofFloat(0f);
        textAnim.setDuration(200);
        textAnim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                prePageModeName.setAlpha(value);
                nextPageModeName.setAlpha(value);
                thisModeName.setAlpha(value);
                mModeNameTv.setAlpha(value);
            }
        });
        ObjectAnimator thisViewAlpha = ObjectAnimator.ofFloat(MultiModeView.this, "alpha", 0f)
                .setDuration(400);
        ObjectAnimator selectImgAlpha = ObjectAnimator.ofFloat(selectedImg, "alpha", 0f)
                .setDuration(400);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hide();
            }
        });
        set.playTogether(thisViewAlpha, selectImgAlpha, textAnim);
        set.setStartDelay(400);
        set.start();
    }

}
