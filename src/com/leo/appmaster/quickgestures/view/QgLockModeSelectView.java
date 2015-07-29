
package com.leo.appmaster.quickgestures.view;

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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.FamilyModeProxyActivity;
import com.leo.appmaster.applocker.LockModeEditActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.OfficeModeProxyActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.UnlockAllModeProxyActivity;
import com.leo.appmaster.applocker.VisitorModeProxyActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.quickgestures.ui.QuickGesturePopupActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CirclePageIndicator;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

public class QgLockModeSelectView extends RelativeLayout implements OnClickListener {

    private CirclePageIndicator mIndicator;
    private ViewPager mViewPager;
    private View mPagerContainer;
    private PagerAdapter mAdapter;
    private List<View> mViews;
    private ImageView mIvClose;
    private View mSelected;
    private View mHolder;
    private int currModePosition;

    public QgLockModeSelectView(Context context) {
        super(context);
    }

    public QgLockModeSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.qg_lock_mode_select_view, this, true);
        mPagerContainer = view.findViewById(R.id.pager_container);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(LockManager.getInstatnce().getLockMode().size());
        mViewPager.setPageMargin(DipPixelUtil.dip2px(getContext(), 46));
        mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        mIvClose = (ImageView) view.findViewById(R.id.img_cancel);
        mIvClose.setOnClickListener(this);
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

    @SuppressWarnings("deprecation")
    private void fillUI(boolean showAnimation) {
        mViews = new ArrayList<View>();
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        List<LockMode> list = LockManager.getInstatnce().getLockMode();
        for (LockMode lockMode : list) {
            View view = mInflater.inflate(R.layout.qg_mode_page_item, mViewPager, false);
            mHolder = view.findViewById(R.id.mode_holder);
            TextView modeIcon = (TextView) view.findViewById(R.id.tv_lock_mode_icon);
            ImageView selectedImg = (ImageView) view.findViewById(R.id.img_selected);
            TextView name = (TextView) view.findViewById(R.id.tv_mode_name);
            name.setText(lockMode.modeName);

            if (lockMode.isCurrentUsed) {
                mSelected = mHolder;
                currModePosition = list.indexOf(lockMode);
                Bitmap grayBitmap = BitmapUtils.createGaryBitmap(lockMode.modeIcon);
                modeIcon.setBackgroundDrawable(new BitmapDrawable(getResources(),
                        grayBitmap));
                selectedImg.setVisibility(View.VISIBLE);
            } else {
                modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(),
                        lockMode.modeIcon)));
                selectedImg.setVisibility(View.GONE);
            }
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
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
            animteViews();
            fillUI(true);
        }
    }

    private void animteViews() {
        ObjectAnimator alphaAnimator1 = ObjectAnimator.ofFloat(mIvClose, "alpha", 0, 1.0f);
        ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(mIndicator, "alpha", 0, 1.0f);
        AnimatorSet as = new AnimatorSet();
        as.playTogether(alphaAnimator1, alphaAnimator2);
        as.setDuration(400);
        as.start();
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

        QuickGesturePopupActivity activity = (QuickGesturePopupActivity) getContext();
        activity.onModeSelectViewClosed();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.mode_holder && v == mSelected) {
            hide();
        }
        if (v == this) {
            hide();
        } else if (v == mIvClose) {
            hide();
        }
    }

    class ModeAdapter extends PagerAdapter {

        private boolean showAnimation;
        Set<Integer> poSet = new HashSet<Integer>();

        public ModeAdapter(Context ctx, boolean showAnimation) {
            this.showAnimation = showAnimation;
            int currNextPostion = currModePosition + 1 >= mViews.size() ? currModePosition
                    : currModePosition + 1;
            int currPrePosition = currModePosition - 1 < 0 ? 0 : currModePosition - 1;
            poSet.add(currNextPostion);
            poSet.add(currPrePosition);
            poSet.add(currModePosition);
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
                getBallAnimtion(view).start();
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
    private AnimatorSet getBallAnimtion(final View view) {
        ObjectAnimator alphaAniamtor = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleXAniamtor = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.1f, 1f);
        ObjectAnimator scaleYAniamtor = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.1f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.playTogether(alphaAniamtor, scaleXAniamtor, scaleYAniamtor);
        return set;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

    private void scaleClickItem(View view, boolean down) {
        if (down) {
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX",
                    1.0f, 0.9f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY",
                    1.0f, 0.9f);
            AnimatorSet as = new AnimatorSet();
            as.setDuration(100);
            as.playTogether(scaleXAnimator, scaleYAnimator);
            as.start();
        } else {
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX",
                    0.9f, 1.0f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY",
                    0.9f, 1.0f);
            AnimatorSet as = new AnimatorSet();
            as.setDuration(100);
            as.playTogether(scaleXAnimator, scaleYAnimator);
            as.start();
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
                            mSelected = view;
                            LockMode lastSelectedMode = (LockMode) mSelected.getTag();
                            LockManager lm = LockManager.getInstatnce();
                            if (lastSelectedMode == lm.getCurLockMode()) {
                                modeIcon = (TextView) mSelected
                                        .findViewById(R.id.tv_lock_mode_icon);
                                selectedImg = (ImageView) mSelected.findViewById(R.id.img_selected);
                                selectedImg.setVisibility(View.GONE);
                                modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(),
                                        lastSelectedMode.modeIcon)));

                                modeIcon = (TextView) view.findViewById(R.id.tv_lock_mode_icon);
                                post(new Runnable() {
                                    @Override
                                    public void run() {
                                        modeIcon.setBackgroundDrawable((new BitmapDrawable(
                                                getResources(),
                                                BitmapUtils.createGaryBitmap(mode.modeIcon))));
                                        selectedImg.setVisibility(View.VISIBLE);
                                    }
                                });
                                selectedImg = (ImageView) view.findViewById(R.id.img_selected);
                                disappearAnim(curPosition, selectedImg);
                            } else {
                                Intent intent = null;
                                intent = new Intent(getContext(), LockScreenActivity.class);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("quick_lock_mode", true);
                                intent.putExtra("lock_mode_id", mode.modeId);
                                intent.putExtra("lock_mode_name", mode.modeName);
                                getContext().startActivity(intent);
                            }
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

    /**
     * this mode ball disapper animation
     * 
     * @param curPosition
     * @param selectedImg
     */
    private void disappearAnim(int curPosition, final View selectedImg) {
        View prePage = mViews.get(curPosition == 0 ? 0 : curPosition - 1);
        View nextPage = mViews
                .get(curPosition == mViews.size() - 1 ? curPosition : curPosition + 1);
        final TextView prePageModeName = (TextView) prePage.findViewById(R.id.tv_mode_name);
        final TextView nextPageModeName = (TextView) nextPage.findViewById(R.id.tv_mode_name);
        final TextView thisModeName = (TextView) mViews.get(curPosition).findViewById(
                R.id.tv_mode_name);

        ValueAnimator textAnim = ValueAnimator.ofFloat(1f, 0f);
        textAnim.setDuration(200);
        textAnim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                prePageModeName.setAlpha(value);
                nextPageModeName.setAlpha(value);
                thisModeName.setAlpha(value);
            }
        });
        ObjectAnimator thisViewAlpha = ObjectAnimator.ofFloat(QgLockModeSelectView.this, "alpha",
                1.0f, 0f)
                .setDuration(400);
        ObjectAnimator selectImgAlpha = ObjectAnimator.ofFloat(selectedImg, "alpha", 1.0f, 0f)
                .setDuration(400);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hide();
                setAlpha(1.0f);
                selectedImg.setAlpha(1.0f);
            }
        });
        set.playTogether(thisViewAlpha, selectImgAlpha, textAnim);
        set.setStartDelay(400);
        set.start();
    }

}
