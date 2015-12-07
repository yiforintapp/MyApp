
package com.leo.appmaster.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LeoPagerTab extends HorizontalScrollView implements PagerIndicator {
    private static final CharSequence EMPTY_TITLE = "";

    /**
     * Interface for a callback when the selected tab has been reselected.
     */
    public interface OnTabReselectedListener {
        /**
         * Callback when the selected tab has been reselected.
         *
         * @param position Position of the current center item.
         */
        void onTabReselected(int position);
    }

    private Runnable mTabSelector;
    private final OnClickListener mTabClickListener = new OnClickListener() {
        public void onClick(View view) {
            TabView tabView = (TabView) view;
            final int oldSelected = mViewPager.getCurrentItem();
            final int newSelected = tabView.getIndex();
            mViewPager.setCurrentItem(newSelected);
            if (oldSelected == newSelected && mTabReselectedListener != null) {
                mTabReselectedListener.onTabReselected(newSelected);
            }
        }
    };

    private LinearLayout mHolderLayout;
    private DeviderLinearLayout mTabLayout;
    private LeoTabUnderline mUnderlineLayout;

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;

    private int mMaxTabWidth;
    private int mSelectedTabIndex;

    private OnTabReselectedListener mTabReselectedListener;

    public LeoPagerTab(Context context) {
        this(context, null);
    }

    public LeoPagerTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

        mHolderLayout = new LinearLayout(context);
        mHolderLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0,
                1);
        mTabLayout = new DeviderLinearLayout(context, R.attr.vpiTabPageIndicatorStyle);
        mTabLayout.setLayoutParams(lp);
        mHolderLayout.addView(mTabLayout);

        lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, DipPixelUtil.dip2px(context,
                5));
        mUnderlineLayout = new LeoTabUnderline(context, attrs);
        mUnderlineLayout.setLayoutParams(lp);
        mHolderLayout.addView(mUnderlineLayout);

        addView(mHolderLayout, new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
    }

    public void setOnTabReselectedListener(OnTabReselectedListener listener) {
        mTabReselectedListener = listener;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
        setFillViewport(lockedExpanded);

        final int childCount = mTabLayout.getChildCount();
        if (childCount > 1
                && (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST)) {
            if (childCount > 2) {
                mMaxTabWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
            } else {
                mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
            }
        } else {
            mMaxTabWidth = -1;
        }

        final int oldWidth = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int newWidth = getMeasuredWidth();

        if (lockedExpanded && oldWidth != newWidth) {
            // Recenter the tab display if we're at a new (scrollable) size.
            setCurrentItem(mSelectedTabIndex);
        }
    }

    private void animateToTab(final int position) {
        final View tabView = mTabLayout.getChildAt(position);
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
        mTabSelector = new Runnable() {
            public void run() {
                final int scrollPos = tabView.getLeft() - (getWidth() - tabView.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                mTabSelector = null;
            }
        };
        post(mTabSelector);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTabSelector != null) {
            // Re-post the selector we saved
            post(mTabSelector);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

    private void addTab(int index, CharSequence text, int iconResId, boolean isRedTip) {
        final TabView tabView = new TabView(getContext());
        tabView.mIndex = index;
        tabView.setFocusable(true);
        tabView.setOnClickListener(mTabClickListener);
        tabView.setText(text);
//        tabView.setSingleLine();
        tabView.setEllipsize(TruncateAt.END);
        tabView.setTextSize(getResources().getInteger(R.integer.home_page_tab_text_size));
        tabView.setGravity(Gravity.CENTER);
        tabView.setPadding(10, 0, 10, 0);
        tabView.setBackgroundResource(R.drawable.home_tab_button_selecter);
        if (isRedTip) {
            tabView.setIsRedTip(true);
        }

        if (iconResId != 0) {

            int margin = getContext().getResources().getDimensionPixelSize(
                    R.dimen.text_icon_margin);
            tabView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
            tabView.setCompoundDrawablePadding(10);
            tabView.setPadding(margin, 0, 0, 0);
            tabView.setCompoundDrawablePadding(-margin);

        }

        mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, MATCH_PARENT, 1));
    }

    public void setTabSelected(int position) {
        int count = mTabLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            if (i == position) {
                ((TabView) mTabLayout.getChildAt(i)).setTabSelected(true);
            } else {
                ((TabView) mTabLayout.getChildAt(i)).setTabSelected(false);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        mUnderlineLayout.onPageScrollStateChanged(arg0);
        if (mListener != null) {
            mListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        mUnderlineLayout.onPageScrolled(arg0, arg1, arg2);
        if (mListener != null) {
            mListener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurrentItem(arg0);
        mUnderlineLayout.onPageSelected(arg0);
        if (mListener != null) {
            mListener.onPageSelected(arg0);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        final PagerAdapter adapter = view.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mUnderlineLayout.setViewPager(mViewPager);

        view.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mTabLayout.removeAllViews();
        PagerAdapter adapter = mViewPager.getAdapter();
        IconPagerAdapter iconAdapter = null;
        if (adapter instanceof IconPagerAdapter) {
            iconAdapter = (IconPagerAdapter) adapter;
        }
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            CharSequence title = adapter.getPageTitle(i);
            if (title == null) {
                title = EMPTY_TITLE;
            }
            int iconResId = 0;
            boolean isRedTip = false;
            if (iconAdapter != null) {
                iconResId = iconAdapter.getIconResId(i);
                isRedTip = iconAdapter.getRedTip(i);
            }
            addTab(i, title, iconResId, isRedTip);
        }
        if (mSelectedTabIndex > count) {
            mSelectedTabIndex = count - 1;
        }
        setCurrentItem(mSelectedTabIndex);
        requestLayout();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        mUnderlineLayout.setViewPager(mViewPager, initialPosition);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mSelectedTabIndex = item;
        mViewPager.setCurrentItem(item);
        final int tabCount = mTabLayout.getChildCount();
        for (int i = 0; i < tabCount; i++) {
            final View child = mTabLayout.getChildAt(i);
            final boolean isSelected = (i == item);
            child.setSelected(isSelected);
            if (isSelected) {
                animateToTab(item);
            }
        }

        setTabSelected(item);
    }

    @Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    public class TabView extends TextView {
        private int mIndex;
        private boolean mIsRedTip = false;

        public TabView(Context context) {
            super(context, null, R.attr.vpiTabPageIndicatorStyle);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            // Re-measure if we went beyond our maximum size.
            if (mMaxTabWidth > 0 && getMeasuredWidth() > mMaxTabWidth) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(mMaxTabWidth, MeasureSpec.EXACTLY),
                        heightMeasureSpec);
            }
        }

        // 设置是否画红点
        public void setIsRedTip(boolean flag) {
            mIsRedTip = flag;
            invalidate();
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mIsRedTip) {
                Resources res = getResources();
                int tabTextwdth = res.getInteger(
                        R.integer.privacy_contact_red_tip_tabTextwdth);
                int tabTexteight = res.getInteger(
                        R.integer.privacy_contact_red_tip_tabTexteight);

                TextPaint textPaint = getPaint();
                Drawable drawable = getCompoundDrawables()[0];
                float x;
                float y;
                if (Build.VERSION.SDK_INT < 19) {
                    x = res.getDimension(R.dimen.privacy_contact_red_tip_x);
                    y = res.getDimension(R.dimen.privacy_contact_red_tip_y);
                } else {
                    x = res.getDimension(R.dimen.privacy_contact_high_red_tip_x);
                    y = res.getDimension(R.dimen.privacy_contact_high_red_tip_y);
                }

                //隐私联系人，两个tab下左右滑动的时候，红点的位置变化很突兀
                float marginLeft = res.getDimension(R.dimen.tab_red_tip_m_left);
                CharSequence text1 = getText();
                if (text1 != null) {
                    float textWidth = textPaint.measureText(text1.toString());
                    x = (getPaddingLeft() + textWidth + getMeasuredWidth()) / 2 + marginLeft;
                    x = (float) Math.ceil(x);
                }

                if (drawable != null) {
                    LeoLog.d("testTabRed", "drawable != null");
                    CharSequence text = getText();
                    if (text != null) {
                        float textWidth = textPaint.measureText(text.toString());

                        float imgwidth = drawable.getIntrinsicWidth();
                        /** the text center is (W-(imgwidth+getPaddingLeft()))/2 **/
                        x = (imgwidth + getPaddingLeft() + textWidth + getMeasuredWidth()) / 2;
                        x = (float) Math.ceil(x);
                        if (x > this.getMeasuredWidth() - tabTextwdth) {
                            /**
                             * if the left not enougth ,then up the red tip
                             */
                            x = this.getMeasuredWidth() - tabTextwdth;
                            y = y - DipPixelUtil.dip2px(getContext(), 3);
                        }
                    }
                } else {
                    LeoLog.d("testTabRed", "drawable == null");
                }
                canvas.translate(x, y);
                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                Bitmap redTip = BitmapFactory.decodeResource(getResources(), R.drawable.red_dot);
                float scaleX = (float) tabTextwdth / redTip.getWidth();
                float scaleY = (float) tabTexteight / redTip.getHeight();
                Log.i("LeoPagerTab", redTip.getWidth() + " " + scaleX);
                Log.i("LeoPagerTab", redTip.getHeight() + " " + scaleY);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
                Matrix matrix = new Matrix();
                matrix.setScale(scaleX, scaleY);
              /*  matrix.setScale(scaleX, scaleY, redTip.getWidth() / 2,
                        redTip.getHeight() / 2);*/
                canvas.drawBitmap(redTip, matrix, paint);
                canvas.save();
            }
        }

        public int getIndex() {
            return mIndex;
        }

        public void setTabSelected(boolean selected) {
            if (selected) {
                this.setAlpha(1f);
                setScaleX(1f);
                setScaleY(1f);
            } else {
                this.setAlpha(0.7f);
                setScaleX(0.8f);
                setScaleY(0.8f);
            }
        }
    }

}
