
package com.leo.appmaster.home;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class MultiModeView extends RelativeLayout implements OnClickListener {

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
       //fillUI(false);
        // mAdapter.notifyDataSetChanged();
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
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
            
            mModeNameTv.setVisibility(View.INVISIBLE);
            mIvAdd.setVisibility(View.INVISIBLE);
            ValueAnimator colorAnim = ObjectAnimator.ofFloat(this, "alpha", 0, 1.0f);
            colorAnim.setDuration(300);
            colorAnim.start();
            
            fillUI(true);
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
         if (v.getId() == R.id.mode_holder && v == mSelected) {
               hide();
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

        private LayoutInflater mInflater;
        private boolean showAnimation;
        private TextView modeIcon;
        private ImageView selectedImg;
        private boolean flag = true;
        private Handler myHandler;

        public ModeAdapter(Context ctx) {
            mInflater = LayoutInflater.from(ctx);
        }

        public ModeAdapter(Context ctx, boolean showAnimation) {
            this.showAnimation = showAnimation;
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
            final View view = mViews.get(position);
            final View holder = view.findViewById(R.id.mode_holder);
            final TextView modeName = (TextView) view.findViewById(R.id.tv_mode_name);
            modeName.setAlpha(0);

            if (mSelected == holder) {
                modeIcon = (TextView) view.findViewById(R.id.tv_lock_mode_icon);
                selectedImg = (ImageView) view.findViewById(R.id.img_selected);
            }
            if (showAnimation) {
                myHandler = new Handler();
                ValueAnimator ballAnim = ValueAnimator.ofFloat(0.5f, 1.05f, 0.98f, 1.0f);
                ballAnim.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float currentValue = (Float) animation.getAnimatedValue();
                        view.setScaleX(currentValue);
                        view.setScaleY(currentValue);
                        if (flag && mSelected == holder && animation.getCurrentPlayTime() > 300) {
                            // add gray shadow
                            myHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    selectedImg.setVisibility(View.VISIBLE);
                                    modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(), BitmapUtils
                                                    .createGaryBitmap(((LockMode) mSelected.getTag()).modeIcon))));
                                }
                            });
                            flag = false;
                        }
                    }
                });

                ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0f, 1.0f);
                alphaAnimator.setStartDelay(320);
                alphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float currValue = (Float) animation.getAnimatedValue();
                        selectedImg.setAlpha(currValue);
                        mModeNameTv.setVisibility(View.VISIBLE);
                        mIvAdd.setVisibility(View.VISIBLE);
                        mModeNameTv.setAlpha(currValue);
                        mIvAdd.setAlpha(currValue);
                        modeName.setAlpha(currValue);
                    }
                });
                AnimatorSet set = new AnimatorSet();
                set.playTogether(ballAnim, alphaAnimator);
                set.setDuration(700);
                set.start();
            }
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
    
    private class ModeTouchListener implements View.OnTouchListener {
        ImageView selectedImg ;
        TextView modeIcon ;
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
                                        modeIcon.setBackgroundDrawable((new BitmapDrawable(getResources(),
                                                BitmapUtils .createGaryBitmap(mode.modeIcon))));
                                        selectedImg.setVisibility(View.VISIBLE);
                                    }
                                });
                                selectedImg = (ImageView) view.findViewById(R.id.img_selected);

                                mSelected = view;
                                mLockManager.setCurrentLockMode(mode, true);
                                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "modeschage", "home");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LeoEventBus.getDefaultBus().post(
                                                        new LockModeEvent(EventId.EVENT_MODE_CHANGE,"multi mode page selectd"));
                                    }
                                }).start();
                                
                               disappearAnim(curPosition,selectedImg);
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
    
    private void disappearAnim(int curPosition,View selectedImg){
        View prePage = mViews.get(curPosition==0?0:curPosition-1);
        View nextPage = mViews.get(curPosition==mViews.size()-1?curPosition:curPosition+1); 
        final TextView prePageModeName = (TextView)prePage.findViewById(R.id.tv_mode_name);
        final TextView nextPageModeName = (TextView)nextPage.findViewById(R.id.tv_mode_name);
        final TextView thisModeName = (TextView)mViews.get(curPosition).findViewById(R.id.tv_mode_name);
        
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
        ObjectAnimator thisViewAlpha = ObjectAnimator.ofFloat(MultiModeView.this, "alpha", 0f).setDuration(500);
        ObjectAnimator selectImgAlpha = ObjectAnimator.ofFloat(selectedImg, "alpha",0f).setDuration(500);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                hide();
            }
        });
        set.playTogether(thisViewAlpha,selectImgAlpha,textAnim);
        set.setStartDelay(300);
        set.start();
    }

}
