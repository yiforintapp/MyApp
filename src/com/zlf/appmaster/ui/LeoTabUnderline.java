
package com.zlf.appmaster.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.zlf.appmaster.R;

/**
 * Draws a line for each page. The current page line is colored differently than
 * the unselected page lines.
 */
public class LeoTabUnderline extends View {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ViewPager mViewPager;
    private int mScrollState;
    private int mCurrentPage;
    private float mPositionOffset;

    public LeoTabUnderline(Context context) {
        this(context, null);
    }

    public LeoTabUnderline(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.vpiUnderlinePageIndicatorStyle);
    }

    public LeoTabUnderline(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode())
            return;

        final Resources res = getResources();

        // Load defaults from resources
        final int defaultSelectedColor = res
                .getColor(R.color.default_underline_indicator_selected_color);

        // Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeoTabUnderline,
                defStyle, 0);

        setSelectedColor(a.getColor(R.styleable.LeoTabUnderline_selectedColor,
                defaultSelectedColor));

        Drawable background = a.getDrawable(R.styleable.LeoTabUnderline_android_background);
        if (background != null) {
            setBackgroundDrawable(background);
        }
        a.recycle();
    }

    public int getSelectedColor() {
        return mPaint.getColor();
    }

    public void setSelectedColor(int selectedColor) {
        mPaint.setColor(selectedColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }
        final int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }

        final int paddingLeft = getPaddingLeft();
        final float pageWidth = (getWidth() - paddingLeft - getPaddingRight()) / (1f * count);
        final float left = paddingLeft + pageWidth * (mCurrentPage + mPositionOffset);
        final float right = left + pageWidth;
        final float top = getPaddingTop();
        final float bottom = getHeight() - getPaddingBottom();
        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    public void setViewPager(ViewPager viewPager) {
        if (mViewPager == viewPager) {
            return;
        }
        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = viewPager;
        invalidate();
    }

    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    public void setCurrentItem(int item) {
        mCurrentPage = item;
        invalidate();
    }

    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPage = position;
        mPositionOffset = positionOffset;
        invalidate();
    }

    public void onPageSelected(int position) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position;
            mPositionOffset = 0;
            invalidate();
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            
            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            mCurrentPage = savedState.currentPage;
            requestLayout();
        } catch (Exception e) {
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

    }
    
    
}
