
package com.leo.appmaster.applocker.gesture;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.manager.ChangeThemeManager;
import com.leo.appmaster.lockertheme.ResourceName;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.theme.LeoResources;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LockPatternUtils;

/**
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen. Is also capable of displaying a static
 * pattern in "in progress", "wrong" or "correct" states.
 *
 * @author way
 */
public class LockPatternView extends ViewGroup {
    private static final String TAG = "LockPatternView";
    // Aspect to use when rendering this view
    private static final int ASPECT_SQUARE = 0; // View will be the minimum of
    // width/height
    private static final int ASPECT_LOCK_WIDTH = 1; // Fixed width; height will
    // be minimum of (w,h)
    private static final int ASPECT_LOCK_HEIGHT = 2; // Fixed height; width will
    // be minimum of (w,h)
    private Drawable themDrawable = null;
    private static final boolean PROFILE_DRAWING = false;
    private boolean mDrawingProfilingStarted = false;

    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();

    // TODO: make this common with PhoneWindow
    static final int STATUS_BAR_HEIGHT = 25;

    private static final int BUTTON_STATE_NAMAL = 0;
    private static final int BUTTON_STATE_DOWN = 1;
    private static final int BUTTON_STATE_UP = 2;

    private int mGestureState = BUTTON_STATE_NAMAL;

    private boolean isFromLockScreenActivity = false;
    private boolean isHideLine = false;

    /**
     * How many milliseconds we spend animating each circle of a lock pattern if
     * the animating mode is set. The entire animation should take this constant
     * * the length of the pattern to complete.
     */
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;

    private OnPatternListener mOnPatternListener;
    private ArrayList<Cell> mPattern = new ArrayList<Cell>(9);

    /**
     * Lookup table for the circles of the pattern we are currently drawing.
     * This will be the cells of the complete pattern unless we are animating,
     * in which case we use this to hold the cells we are drawing for the in
     * progress animation.
     */
    private boolean[][] mPatternDrawLookup = new boolean[3][3];

    /**
     * the in progress point: - during interaction: where the user's finger is -
     * during animation: the current tip of the animating line
     */
    private float mInProgressX = -1;
    private float mInProgressY = -1;

    private long mAnimatingPeriodStart;

    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mEnableHapticFeedback = true;
    private boolean mPatternInProgress = false;

    private float mDiameterFactor = 0.10f; // TODO: move to attrs
    private float mHitFactor = 0.6f;

    private float mSquareWidth;
    private float mSquareHeight;

    private Bitmap mBitmapCircleDefault;
    private Bitmap mBitmapCircleGreen;
    private Bitmap mBitmapCircleRed;

    private final Path mCurrentPath = new Path();
    private final Rect mInvalidate = new Rect();

    private int mBitmapWidth;
    private int mBitmapHeight;

    private int mAspect;
    private final Matrix mCircleMatrix = new Matrix();

    private String mThemepkgName = AppMasterApplication.getSelectedTheme();
    private Resources mThemeRes = null;
    private int mGesturePressAnimRes = 0;
    private int mGestureLeftRes = 0;
    private int mGestureVerticalRes = 0;
    private int mGestureRightRes = 0;
    private Drawable mGestureLeftDrawable;
    private Drawable mGestureRightDrawable;
    private Drawable mGestureVerticalDrawable;

    private int mGestureLeftAnimRes = 0;
    private int mGestureVerticalAnimRes = 0;
    private int mGestureRightAnimRes = 0;
    private ImageView[] mButtonViews = new ImageView[9];
    private AnimationDrawable[] mPressAnimDrawable = new AnimationDrawable[9];
    private AnimationDrawable mUpAnimDrawable;
    private int[] mButtonViewRes = {
            0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    private int mNormalViewRes = 0;
    private int mGestureLineColorRes = 0;
    private int mColor = 0xcc00d2ff;

    private Cell mLastParten;
    private Cell mCurrParten;

    private int mLockMode = LockManager.LOCK_MODE_PURE;
    private Context mContext;

    /**
     * Represents a cell in the 3 X 3 matrix of the unlock pattern view.
     */
    public static class Cell {
        int row;
        int column;

        // keep # objects limited to 9
        static Cell[][] sCells = new Cell[3][3];

        static {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    sCells[i][j] = new Cell(i, j);
                }
            }
        }

        /**
         * @param row    The row of the cell.
         * @param column The column of the cell.
         */
        private Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        /**
         * @param row    The row of the cell.
         * @param column The column of the cell.
         */
        public static synchronized Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > 2) {
                throw new IllegalArgumentException("row must be in range 0-2");
            }
            if (column < 0 || column > 2) {
                throw new IllegalArgumentException(
                        "column must be in range 0-2");
            }
        }

        public String toString() {
            return "(row=" + row + ",clmn=" + column + ")";
        }
    }

    /**
     * How to display the current pattern.
     */
    public enum DisplayMode {

        /**
         * The pattern drawn is correct (i.e draw it in a friendly color)
         */
        Correct,

        /**
         * Animate the pattern (for demo, and help).
         */
        Animate,

        /**
         * The pattern is wrong (i.e draw a foreboding color)
         */
        Wrong
    }

    /**
     * The call back interface for detecting patterns entered by the user.
     */
    public static interface OnPatternListener {

        /**
         * A new pattern has begun.
         */
        void onPatternStart();

        /**
         * The pattern was cleared.
         */
        void onPatternCleared();

        /**
         * The user extended the pattern currently being drawn by one cell.
         *
         * @param pattern The pattern with newly added cell.
         */
        void onPatternCellAdded(List<Cell> pattern);

        /**
         * A pattern was detected from the user.
         *
         * @param pattern The pattern.
         */
        void onPatternDetected(List<Cell> pattern);
    }

    public LockPatternView(Context context) {
        this(context, null);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        isHideLine = AppMasterPreference.getInstance(context).getIsHideLine();

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LockPatternView);

        final String aspect = a.getString(R.styleable.LockPatternView_aspect);

        if ("square".equals(aspect)) {
            mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            mAspect = ASPECT_LOCK_WIDTH;
        } else if ("lock_height".equals(aspect)) {
            mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            mAspect = ASPECT_SQUARE;
        }

        setClickable(true);

        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setColor(0xcc00d2ff);
//        mPathPaint.setColor(Color.WHITE); // TODO this should be from the style
//        mPathPaint.setAlpha(mStrokeAlpha);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);

        mBitmapCircleDefault = getBitmapFor(R.drawable.gesture_point_bg);
        mBitmapCircleGreen = getBitmapFor(R.drawable.gesture_point);
        mBitmapCircleRed = getBitmapFor(R.drawable.gesture_pattern_selected_wrong);
        // for (Bitmap bitmap : bitmaps) {
        // mBitmapWidth = Math.max(mBitmapWidth, bitmap.getWidth());
        // mBitmapHeight = Math.max(mBitmapHeight, bitmap.getHeight());
        // }
        mBitmapWidth = (int) mContext.getResources()
                .getDimension(R.dimen.lock_pattern_button_width);
        mBitmapHeight = mBitmapWidth;
        a.recycle();

        initChildView();
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    /**
     * @return Whether the view is in stealth mode.
     */
    public boolean isInStealthMode() {
        return mInStealthMode;
    }

    /**
     * @return Whether the view has tactile feedback enabled.
     */
    public boolean isTactileFeedbackEnabled() {
        return mEnableHapticFeedback;
    }

    /**
     * Set whether the view is in stealth mode. If true, there will be no
     * visible feedback as the user enters the pattern.
     *
     * @param inStealthMode Whether in stealth mode.
     */
    public void setInStealthMode(boolean inStealthMode) {
        mInStealthMode = inStealthMode;
    }

    /**
     * Set whether the view will use tactile feedback. If true, there will be
     * tactile feedback as the user enters the pattern.
     *
     * @param tactileFeedbackEnabled Whether tactile feedback is enabled
     */
    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    /**
     * Set the call back for pattern detection.
     *
     * @param onPatternListener The call back.
     */
    public void setOnPatternListener(OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }

    /**
     * Set the pattern explicitely (rather than waiting for the user to input a
     * pattern).
     *
     * @param displayMode How to display the pattern.
     * @param pattern     The pattern.
     */
    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
            mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }

        setDisplayMode(displayMode);
    }

    /**
     * Set the display mode of the current pattern. This can be useful, for
     * instance, after detecting a pattern to tell this view whether change the
     * in progress result to correct or wrong.
     *
     * @param displayMode The display mode.
     */
    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException(
                        "you must have a pattern to "
                                + "animate if you want to set the display mode to animate");
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            final Cell first = mPattern.get(0);
            mInProgressX = getCenterXForColumn(first.getColumn());
            mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
        }
        invalidate();
    }

    private void notifyCellAdded() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern);
        }
        if (mPattern.size() == 1) {
            mLastParten = mCurrParten = mPattern.get(0);
        } else if (mPattern.size() > 1) {
            mLastParten = mPattern.get(mPattern.size() - 2);
            mCurrParten = mPattern.get(mPattern.size() - 1);
        }
    }

    private void notifyPatternStarted() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternDetected(mPattern);
        }
    }

    private void notifyPatternCleared() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
    }

    /**
     * Clear the pattern.
     */
    public void clearPattern() {
        resetPattern();
    }

    /**
     * Reset all pattern state.
     */
    private void resetPattern() {
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    /**
     * Clear the pattern lookup table.
     */
    private void clearPatternDrawLookup() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mPatternDrawLookup[i][j] = false;
            }
        }

    }

    /**
     *
     */
    private void clearGifAnimation() {
        if (!needChangeTheme())
            return;
        for (AnimationDrawable button : mPressAnimDrawable) {
            if (button != null) {
                button.stop();
            }
        }
        if (mThemeRes != null) {
            if (mButtonViewRes[0] <= 0) {
                for (int i = 0; i < mButtonViews.length; i++) {
                    mButtonViews[i].setBackgroundDrawable(mThemeRes.getDrawable(mNormalViewRes));
                }
            } else {
                for (int i = 0; i < mButtonViews.length; i++) {
                    mButtonViews[i].setBackgroundDrawable(mThemeRes.getDrawable(mButtonViewRes[i]));
                }
            }
        }
    }

    public void cleangifResource() {
        if (!needChangeTheme())
            return;
        clearGifAnimation();
        //
        // for (GifDrawable button : mGifDrawables) {
        // button.recycle();
        // }
    }

    /**
     * Disable input (for instance when displaying a message that will timeout
     * so user doesn't get view into messy state).
     */
    public void disableInput() {
        mInputEnabled = false;
    }

    /**
     * Enable input.
     */
    public void enableInput() {
        mInputEnabled = true;
    }

    private void initChildView() {
        int length = mButtonViews.length;
        int width = (int) mContext.getResources().getDimension(R.dimen.lock_pattern_button_width);
//        dadian

        if (mContext instanceof LockScreenActivity) {
            mLockMode = ((LockScreenActivity) mContext).getFromType();
        }

        if (needChangeTheme()) {
            Context themeContext = getThemepkgConotext(mThemepkgName);
            mThemeRes = themeContext.getResources();
            mGesturePressAnimRes = ThemeUtils.getValueByResourceName(themeContext, "anim",
                    ResourceName.THEME_PRESS_ANIM);
            mGestureLeftAnimRes = ThemeUtils.getValueByResourceName(themeContext, "anim",
                    ResourceName.THEME_LEFT_ANIM);
            mGestureRightAnimRes = ThemeUtils.getValueByResourceName(themeContext, "anim",
                    ResourceName.THEME_RIGHT_ANIM);
            mGestureVerticalAnimRes = ThemeUtils.getValueByResourceName(themeContext, "anim",
                    ResourceName.THEME_VERTICAL_ANIM);
            mGestureLeftRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                    ResourceName.THEME_LEFT_ACTIVE);
            if (mGestureLeftRes > 0) {
                mGestureLeftDrawable = mThemeRes.getDrawable(mGestureLeftRes);
            }
            mGestureVerticalRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                    ResourceName.THEME_VERTICAL_ACTIVE);
            if (mGestureVerticalRes > 0) {
                mGestureVerticalDrawable = mThemeRes.getDrawable(mGestureVerticalRes);
            }
            mGestureRightRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                    ResourceName.THEME_GESTRUE_RIGHT_ACTIVE);
            if (mGestureRightRes > 0) {
                mGestureRightDrawable = mThemeRes.getDrawable(mGestureRightRes);
            }
            int upAnimationRes = ThemeUtils.getValueByResourceName(themeContext, "anim",
                    ResourceName.THEME_GESTRUE_UP_ANIM);
            if (upAnimationRes > 0) {
                mUpAnimDrawable = (AnimationDrawable) mThemeRes.getDrawable(upAnimationRes);
            }

            mGestureLineColorRes = ThemeUtils.getValueByResourceName(themeContext, "color",
                    ResourceName.THEME_GESTRUE_LINE_COLOR);
            if (mGestureLineColorRes > 0) {
                mColor = mThemeRes.getColor(mGestureLineColorRes);
            }
            initGestureButtonByTmeme(themeContext);
        } else {
            for (int i = 0; i < length; i++) {
                mButtonViews[i] = new ImageView(mContext);
                mButtonViews[i].setScaleType(ImageView.ScaleType.FIT_XY);
                mButtonViews[i].setLayoutParams(new Gallery.LayoutParams(width, width));
                mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
                addView(mButtonViews[i]);
            }
        }

    }

    private void initGestureButtonByTmeme(Context themeContext) {
        int length = mButtonViews.length;
        int width = (int) mContext.getResources().getDimension(R.dimen.lock_pattern_button_width);

        mNormalViewRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                ResourceName.THEME_GESTRUE_NORMAL);

        for (int i = 0; i < mButtonViewRes.length; i++) {
            mButtonViewRes[i] = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                    "gesture_" + (i + 1) + "_normal");
        }

        for (int i = 0; i < length; i++) {
            mButtonViews[i] = new ImageView(mContext);
            mButtonViews[i].setScaleType(ImageView.ScaleType.FIT_XY);
            mButtonViews[i].setLayoutParams(new Gallery.LayoutParams(width, width));
            // mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
            if (mGesturePressAnimRes > 0) {
                mPressAnimDrawable[i] = (AnimationDrawable) mThemeRes
                        .getDrawable(mGesturePressAnimRes);
            }
            if (mButtonViewRes[i] > 0) {
                mButtonViews[i].setBackgroundDrawable(mThemeRes.getDrawable(mButtonViewRes[i]));
            } else if (mNormalViewRes > 0) {
                mButtonViews[i].setBackgroundDrawable(mThemeRes.getDrawable(mNormalViewRes));
            } else {
                mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
            }
            addView(mButtonViews[i]);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int width = w - getPaddingLeft() - getPaddingRight();
        mSquareWidth = width / 3.0f;

        final int height = h - getPaddingTop() - getPaddingBottom();
        mSquareHeight = height / 3.0f;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return 3 * mBitmapWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return 3 * mBitmapWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        switch (mAspect) {
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_WIDTH:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
        }

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(viewWidth, viewHeight);
    }

    /**
     * Determines whether the point x, y will add a new point to the current
     * pattern (in addition to finding the cell, also makes heuristic choices
     * such as filling in gaps based on current pattern).
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null) {

            // check for gaps in existing pattern
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.row - lastCell.row;
                int dColumn = cell.column - lastCell.column;

                int fillInRow = lastCell.row;
                int fillInColumn = lastCell.column;

                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.row + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.column + ((dColumn > 0) ? 1 : -1);
                }

                fillInGapCell = Cell.of(fillInRow, fillInColumn);
            }

            if (fillInGapCell != null
                    && !mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column]) {
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            if (mEnableHapticFeedback) {
                try {
                    performHapticFeedback(
                            HapticFeedbackConstants.VIRTUAL_KEY,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                } catch (Error e) {

                }
            }
            return cell;
        }
        return null;
    }

    private void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        notifyCellAdded();
    }

    // helper method to find which cell a point maps to
    private Cell checkForNewHit(float x, float y) {

        final int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        final int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }

        if (mPatternDrawLookup[rowHit][columnHit]) {
            return null;
        }
        return Cell.of(rowHit, columnHit);
    }

    /**
     * Helper method to find the row that y falls into.
     *
     * @param y The y coordinate
     * @return The row that y falls in, or -1 if it falls in no row.
     */
    private int getRowHit(float y) {

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;

        float offset = getPaddingTop() + (squareHeight - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {

            final float hitTop = offset + squareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper method to find the column x fallis into.
     *
     * @param x The x coordinate.
     * @return The column that x falls in, or -1 if it falls in no column.
     */
    private int getColumnHit(float x) {
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;

        float offset = getPaddingLeft() + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {

            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isUnlockSuccess) {
            return false;
        }

        if (!mInputEnabled || !isEnabled()) {
            mGestureState = BUTTON_STATE_NAMAL;
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isStartDrawing = true;

                mGestureState = BUTTON_STATE_DOWN;
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                mGestureState = BUTTON_STATE_UP;
                clearGifAnimation();
                handleActionUp(event);

                isStartDrawing = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                isStartDrawing = false;

                mGestureState = BUTTON_STATE_UP;
                resetPattern();
                mPatternInProgress = false;
                clearGifAnimation();
                notifyPatternCleared();
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
        }
        return false;
    }

    private void handleActionMove(MotionEvent event) {
        // Handle all recent motion events so we don't skip any cells even when
        // the device
        // is busy...
        // AM-678
        try {
            final int historySize = event.getHistorySize();
            for (int i = 0; i < historySize + 1; i++) {
                final float x = i < historySize ? event.getHistoricalX(i) : event
                        .getX();
                final float y = i < historySize ? event.getHistoricalY(i) : event
                        .getY();
                final int patternSizePreHitDetect = mPattern.size();
                Cell hitCell = detectAndAddHit(x, y);
                final int patternSize = mPattern.size();
                if (hitCell != null && patternSize == 1) {
                    mPatternInProgress = true;
                    notifyPatternStarted();
                }
                // note current x and y for rubber banding of in progress
                // patterns
                final float dx = Math.abs(x - mInProgressX);
                final float dy = Math.abs(y - mInProgressY);
                if (dx + dy > mSquareWidth * 0.04f) {
                    float oldX = mInProgressX;
                    float oldY = mInProgressY;

                    mInProgressX = x;
                    mInProgressY = y;

                    if (mPatternInProgress && patternSize > 0) {
                        final Cell[] pattern = mPattern.toArray(new Cell[0]);
                        final float radius = mSquareWidth * mDiameterFactor * 0.5f;

                        final Cell lastCell = pattern[patternSize - 1];

                        float startX = getCenterXForColumn(lastCell.column);
                        float startY = getCenterYForRow(lastCell.row);

                        float left;
                        float top;
                        float right;
                        float bottom;

                        final Rect invalidateRect = mInvalidate;

                        if (startX < x) {
                            left = startX;
                            right = x;
                        } else {
                            left = x;
                            right = startX;
                        }

                        if (startY < y) {
                            top = startY;
                            bottom = y;
                        } else {
                            top = y;
                            bottom = startY;
                        }

                        // Invalidate between the pattern's last cell and the
                        // current location
                        invalidateRect.set((int) (left - radius * 2),
                                (int) (top - radius * 2), (int) (right + radius * 2),
                                (int) (bottom + radius * 2));

                        if (startX < oldX) {
                            left = startX;
                            right = oldX;
                        } else {
                            left = oldX;
                            right = startX;
                        }

                        if (startY < oldY) {
                            top = startY;
                            bottom = oldY;
                        } else {
                            top = oldY;
                            bottom = startY;
                        }

                        // Invalidate between the pattern's last cell and the
                        // previous location
                        invalidateRect.union((int) (left - radius * 2),
                                (int) (top - radius * 2), (int) (right + radius * 2),
                                (int) (bottom + radius * 2));

                        // Invalidate between the pattern's new cell and the
                        // pattern's previous cell
                        if (hitCell != null) {
                            startX = getCenterXForColumn(hitCell.column);
                            startY = getCenterYForRow(hitCell.row);

                            if (patternSize >= 2) {
                                // (re-using hitcell for old cell)
                                hitCell = pattern[patternSize - 1
                                        - (patternSize - patternSizePreHitDetect)];
                                oldX = getCenterXForColumn(hitCell.column);
                                oldY = getCenterYForRow(hitCell.row);

                                if (startX < oldX) {
                                    left = startX;
                                    right = oldX;
                                } else {
                                    left = oldX;
                                    right = startX;
                                }

                                if (startY < oldY) {
                                    top = startY;
                                    bottom = oldY;
                                } else {
                                    top = oldY;
                                    bottom = startY;
                                }
                            } else {
                                left = right = startX;
                                top = bottom = startY;
                            }

                            final float widthOffset = mSquareWidth / 2f;
                            final float heightOffset = mSquareHeight / 2f;

                            invalidateRect.set((int) (left - widthOffset),
                                    (int) (top - heightOffset),
                                    (int) (right + widthOffset),
                                    (int) (bottom + heightOffset));
                        }
                        invalidate(invalidateRect);
                    } else {
                        invalidate();
                    }
                }
            }
        } catch (Exception e) {

        }

    }

    private boolean isUnlockSuccess = false;
    private boolean isStartDrawing = false;

    public void setIsUnlockSuccess(boolean mFlag) {
        isUnlockSuccess = mFlag;
    }

    public boolean getIsStartDrawing() {
        return isStartDrawing;
    }

    private void handleActionUp(MotionEvent event) {
        // report pattern detected
        if (!mPattern.isEmpty()) {
            mPatternInProgress = false;
            //3.1 , 解锁成功后，没有换图标就解锁成功进入了
            invalidate();
            notifyPatternDetected();
        }
        if (PROFILE_DRAWING) {
            if (mDrawingProfilingStarted) {
                Debug.stopMethodTracing();
                mDrawingProfilingStarted = false;
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();

        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else {
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if (hitCell != null) {
            final float startX = getCenterXForColumn(hitCell.column);
            final float startY = getCenterYForRow(hitCell.row);

            final float widthOffset = mSquareWidth / 2f;
            final float heightOffset = mSquareHeight / 2f;

            invalidate((int) (startX - widthOffset),
                    (int) (startY - heightOffset),
                    (int) (startX + widthOffset), (int) (startY + heightOffset));
        }
        mInProgressX = x;
        mInProgressY = y;
        if (PROFILE_DRAWING) {
            if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("LockPatternDrawing");
                mDrawingProfilingStarted = true;
            }
        }
    }

    private float getCenterXForColumn(int column) {
        return getPaddingLeft() + column * mSquareWidth + mSquareWidth / 2f;
    }

    private float getCenterYForRow(int row) {
        return getPaddingTop() + row * mSquareHeight + mSquareHeight / 2f;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final ArrayList<Cell> pattern = mPattern;
        // 已连接上的icon数
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        if (mPatternDisplayMode == DisplayMode.Animate) {

            // figure out which circles to draw

            // + 1 so we pause on complete pattern
            final int oneCycle = (count + 1) * MILLIS_PER_CIRCLE_ANIMATING;
            final int spotInCycle = (int) (SystemClock.elapsedRealtime() - mAnimatingPeriodStart)
                    % oneCycle;
            final int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;

            clearPatternDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                final Cell cell = pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }

            // figure out in progress portion of ghosting line

            final boolean needToUpdateInProgressPoint = numCircles > 0
                    && numCircles < count;

            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING))
                        / MILLIS_PER_CIRCLE_ANIMATING;

                final Cell currentCell = pattern.get(numCircles - 1);
                final float centerX = getCenterXForColumn(currentCell.column);
                final float centerY = getCenterYForRow(currentCell.row);

                final Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle
                        * (getCenterXForColumn(nextCell.column) - centerX);
                final float dy = percentageOfNextCircle
                        * (getCenterYForRow(nextCell.row) - centerY);
                mInProgressX = centerX + dx;
                mInProgressY = centerY + dy;
            }
            // TODO: Infinite loop here...
            invalidate();
        }

        final float squareWidth = mSquareWidth;

        //dadian
//        float radius = (squareWidth * mDiameterFactor) * 1.3f;
        float radius = (squareWidth * mDiameterFactor) * 1f;
        mPathPaint.setStrokeWidth(radius);

        final Path currentPath = mCurrentPath;
        // rewind当前path，清除掉所有直线，曲线，但是保留它内部的数据结构，以便更好的重新使用
        currentPath.rewind();

        // TODO: the path should be created and cached every time we hit-detect
        // a cell
        // only the last segment of the path should be computed here
        // draw the path of the pattern (unless the user is in progress, and
        // we are in stealth mode)
        final boolean drawPath = (!mInStealthMode || mPatternDisplayMode == DisplayMode.Wrong);

        // draw the arrows associated with the path (unless the user is in
        // progress, and
        // we are in stealth mode)
        boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
        mPaint.setFilterBitmap(true); // draw with higher quality since we
        // render with transforms

        // draw the lines && hideline
        if (isFromLockScreenActivity) {
            if (!isHideLine) {
                drawPath(canvas, pattern, count, drawLookup, currentPath, drawPath);
            }
        } else {
            drawPath(canvas, pattern, count, drawLookup, currentPath, drawPath);
        }

        // draw the circles
        // final int paddingTop = getPaddingTop();
        // final int paddingLeft = getPaddingLeft();
        //
        // for (int i = 0; i < 3; i++) {
        // float topY = paddingTop + i * squareHeight;
        // // float centerY = mPaddingTop + i * mSquareHeight + (mSquareHeight
        // // / 2);
        // for (int j = 0; j < 3; j++) {
        // float leftX = paddingLeft + j * squareWidth;
        // drawCircle(canvas, (int) leftX, (int) topY, drawLookup[i][j]);
        // }
        // }

        mPaint.setFilterBitmap(oldFlag); // restore default flag
        if (needChangeTheme()) {
            setCirclesResource(drawLookup);
        } else {
            int length = mButtonViews.length;
            for (int i = 0; i < length; i++) {
                if (!drawLookup[i / 3][i % 3]
                        || (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
                    mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
//                } else if (mPatternInProgress) {
                } else {
                    if (themDrawable == null) {
                        themDrawable = ChangeThemeManager.getChrismasThemeDrawbleBySlotId(ChangeThemeManager.BG_LOCKSCREEN_GESTURE_DOT, mContext);
                    }
//                    Drawable drawable = ChangeThemeManager.getChrismasThemeDrawbleBySlotId(ChangeThemeManager.BG_LOCKSCREEN_GESTURE_DOT, mContext);
                    if (themDrawable != null && isFromLockScreenActivity) {
                        mButtonViews[i].setBackgroundDrawable(themDrawable);
                    } else {
                        mButtonViews[i].setBackgroundResource(R.drawable.gesture_point);
                    }
                }/*
                  * else if (mPatternDisplayMode == DisplayMode.Wrong) {
                  * mButtonViews[i]
                  * .setBackgroundResource(R.drawable.gesture_pattern_selected_wrong
                  * ); } else if (mPatternDisplayMode == DisplayMode.Correct ||
                  * mPatternDisplayMode == DisplayMode.Animate) {
                  * mButtonViews[i]
                  * .setBackgroundResource(R.drawable.gesture_point_bg); } else
                  * { mButtonViews[i].setBackgroundResource(R.drawable.
                  * gesture_point_bg); }
                  */
            }
        }
        super.dispatchDraw(canvas);
    }

    private void drawPath(Canvas canvas, final ArrayList<Cell> pattern, final int count,
                          final boolean[][] drawLookup, final Path currentPath, final boolean drawPath) {
        if (drawPath) {
            boolean anyCircles = false;
            for (int i = 0; i < count; i++) {
                Cell cell = pattern.get(i);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[cell.row][cell.column]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.column);
                float centerY = getCenterYForRow(cell.row);
                if (i == 0) {
                    currentPath.moveTo(centerX, centerY);
                } else {
                    currentPath.lineTo(centerX, centerY);
                }
            }

            // add last in progress section
            if ((mPatternInProgress || mPatternDisplayMode == DisplayMode.Animate)
                    && anyCircles) {
                currentPath.lineTo(mInProgressX, mInProgressY);
            }
            // chang the line color in different DisplayMode
            if (mPatternDisplayMode == DisplayMode.Wrong) {
//                LeoLog.i("testWhenRed", "red");
                mPathPaint.setColor(0x7fbb0000);
            } else {
                mPathPaint.setColor(mColor);
                //dadian
//                mPathPaint.setColor(0xcc00d2ff);
            }

            canvas.drawPath(currentPath, mPathPaint);
            // 轨迹断裂bug
            invalidate();
        }
    }

    private void setCirclesResource(boolean[][] partOfPattern) {
        try {
            int length = mButtonViews.length;

            for (int i = 0; i < length; i++) {

                // if (!partOfPattern[i / 3][ i % 3] || (mInStealthMode &&
                // mPatternDisplayMode != DisplayMode.Wrong)) {
                // mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
                // } else if (mPatternInProgress) {
                // mButtonViews[i].setBackgroundResource(R.drawable.gesture_point);
                // } else if (mPatternDisplayMode == DisplayMode.Wrong) {
                // mButtonViews[i].setBackgroundResource(R.drawable.gesture_pattern_selected_wrong);
                // } else if (mPatternDisplayMode == DisplayMode.Correct ||
                // mPatternDisplayMode == DisplayMode.Animate) {
                // mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
                // } else {
                // mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
                // }

                if (!partOfPattern[i / 3][i % 3]) {
                    // mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
                } else if (mGestureState == BUTTON_STATE_DOWN || mPatternInProgress) {
                    // mButtonViews[i].setBackgroundResource(R.drawable.gesture_point);
                    if (mLastParten != null && mCurrParten != null) {
                        if (mLastParten == mCurrParten && mCurrParten.getRow() == i / 3
                                && mCurrParten.getColumn() == i % 3) {

                            if (!mPressAnimDrawable[i].isRunning()) {
                                mButtonViews[i].setBackgroundDrawable(mPressAnimDrawable[i]);
                                mPressAnimDrawable[i].start();
                            }
                        } else {
                            if (mLastParten.getRow() == i / 3 && mLastParten.getColumn() == i % 3) {

                                if (mLastParten.getColumn() == mCurrParten.getColumn()) {
                                    mPressAnimDrawable[i].stop();
                                    if (mGestureVerticalAnimRes > 0) {
                                        AnimationDrawable anim = (AnimationDrawable) mThemeRes
                                                .getDrawable(mGestureVerticalAnimRes);
                                        mButtonViews[i].setBackgroundDrawable(anim);
                                        anim.start();
                                    } else if (mGestureVerticalDrawable != null) {
                                        mButtonViews[i]
                                                .setBackgroundDrawable(mGestureVerticalDrawable);
                                    } else {

                                    }
                                } else if (mLastParten.getColumn() < mCurrParten.getColumn()) {
                                    mPressAnimDrawable[i].stop();
                                    if (mGestureRightAnimRes > 0) {
                                        AnimationDrawable anim = (AnimationDrawable) mThemeRes
                                                .getDrawable(mGestureRightAnimRes);
                                        mButtonViews[i].setBackgroundDrawable(anim);
                                        anim.start();
                                    } else if (mGestureRightDrawable != null) {
                                        mButtonViews[i]
                                                .setBackgroundDrawable(mGestureRightDrawable);
                                    } else {

                                    }
                                } else if (mLastParten.getColumn() > mCurrParten.getColumn()) {
                                    mPressAnimDrawable[i].stop();
                                    if (mGestureLeftAnimRes > 0) {
                                        AnimationDrawable anim = (AnimationDrawable) mThemeRes
                                                .getDrawable(mGestureLeftAnimRes);
                                        mButtonViews[i].setBackgroundDrawable(anim);
                                        anim.start();
                                    } else if (mGestureLeftDrawable != null) {
                                        mButtonViews[i].setBackgroundDrawable(mGestureLeftDrawable);
                                    } else {

                                    }
                                }
                                mLastParten = mCurrParten;
                            }
                            if (mCurrParten.getRow() == i / 3 && mCurrParten.getColumn() == i % 3) {
                                if (!mPressAnimDrawable[i].isRunning()) {
                                    mButtonViews[i].setBackgroundDrawable(mPressAnimDrawable[i]);
                                    mPressAnimDrawable[i].start();
                                }
                            }
                        }
                    }

                } else if (mGestureState == BUTTON_STATE_UP) {
                    // mButtonViews[i].setBackgroundResource(R.drawable.gesture_point_bg);
                    if (mCurrParten.getRow() == i / 3 && mCurrParten.getColumn() == i % 3) {
                        if (mUpAnimDrawable != null) {
                            mButtonViews[i].setImageDrawable(mUpAnimDrawable);
                            mUpAnimDrawable.start();
                        }
                    }
                }

            }
        } catch (Exception e) {

        }
    }

    /**
     * @param canvas
     * @param leftX
     * @param topY
     * @param partOfPattern Whether this circle is part of the pattern.
     */
    private void drawCircle(Canvas canvas, int leftX, int topY,
                            boolean partOfPattern) {
        Bitmap outerCircle;
        Bitmap innerCircle = null;

        if (!partOfPattern
                || (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
            // unselected circle
            outerCircle = mBitmapCircleDefault;
            innerCircle = null;
        } else if (mPatternInProgress) {
            // user is in middle of drawing a pattern
            outerCircle = mBitmapCircleDefault;
            innerCircle = mBitmapCircleGreen;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            // the pattern is wrong
            outerCircle = mBitmapCircleDefault;
            innerCircle = mBitmapCircleRed;
        } else if (mPatternDisplayMode == DisplayMode.Correct
                || mPatternDisplayMode == DisplayMode.Animate) {
            // the pattern is correct
            outerCircle = mBitmapCircleDefault;
            innerCircle = mBitmapCircleGreen;
        } else {
            throw new IllegalStateException("unknown display mode "
                    + mPatternDisplayMode);
        }

        final int width = mBitmapWidth;
        final int height = mBitmapHeight;

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        int offsetX = (int) ((squareWidth - width) / 2f);
        int offsetY = (int) ((squareHeight - height) / 2f);

        // Allow circles to shrink if the view is too small to hold them.
        float sx = Math.min(mSquareWidth / mBitmapWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mBitmapHeight, 1.0f);

        mCircleMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mCircleMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mCircleMatrix.preScale(sx * 0.65f, sy * 0.65f);
        mCircleMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);

        canvas.drawBitmap(outerCircle, mCircleMatrix, mPaint);
        if (innerCircle != null)
            canvas.drawBitmap(innerCircle, mCircleMatrix, mPaint);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState,
                LockPatternUtils.patternToString(mPattern),
                mPatternDisplayMode.ordinal(), mInputEnabled, mInStealthMode,
                mEnableHapticFeedback);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // AM-432, add protect
        try {
            if (state instanceof SavedState) {
                final SavedState ss = (SavedState) state;
                super.onRestoreInstanceState(ss.getSuperState());
                setPattern(DisplayMode.Correct,
                        LockPatternUtils.stringToPattern(ss.getSerializedPattern()));
                mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
                mInputEnabled = ss.isInputEnabled();
                mInStealthMode = ss.isInStealthMode();
                mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
            } else {
                super.onRestoreInstanceState(state);
            }
        } catch (Exception e) {

        }
    }

    /**
     * The parecelable for saving and restoring a lock pattern view.
     */
    private static class SavedState extends BaseSavedState {

        private final String mSerializedPattern;
        private final int mDisplayMode;
        private final boolean mInputEnabled;
        private final boolean mInStealthMode;
        private final boolean mTactileFeedbackEnabled;

        /**
         * Constructor called from {@link LockPatternView#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, String serializedPattern,
                           int displayMode, boolean inputEnabled, boolean inStealthMode,
                           boolean tactileFeedbackEnabled) {
            super(superState);
            mSerializedPattern = serializedPattern;
            mDisplayMode = displayMode;
            mInputEnabled = inputEnabled;
            mInStealthMode = inStealthMode;
            mTactileFeedbackEnabled = tactileFeedbackEnabled;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mSerializedPattern = in.readString();
            mDisplayMode = in.readInt();
            mInputEnabled = (Boolean) in.readValue(null);
            mInStealthMode = (Boolean) in.readValue(null);
            mTactileFeedbackEnabled = (Boolean) in.readValue(null);
        }

        public String getSerializedPattern() {
            return mSerializedPattern;
        }

        public int getDisplayMode() {
            return mDisplayMode;
        }

        public boolean isInputEnabled() {
            return mInputEnabled;
        }

        public boolean isInStealthMode() {
            return mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled() {
            return mTactileFeedbackEnabled;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mSerializedPattern);
            dest.writeInt(mDisplayMode);
            dest.writeValue(mInputEnabled);
            dest.writeValue(mInStealthMode);
            dest.writeValue(mTactileFeedbackEnabled);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int paddingTop = getPaddingTop();
        final int paddingLeft = getPaddingLeft();
        int cWidth = getChildAt(0).getMeasuredWidth();
        int cHeight = getChildAt(0).getMeasuredHeight();
        for (int i = 0; i < 3; i++) {
            float topY = paddingTop + i * mSquareHeight;
            topY += (mSquareHeight - cHeight) / 2;
            for (int j = 0; j < 3; j++) {
                float leftX = paddingLeft + j * mSquareWidth;
                View childView = getChildAt(i * 3 + j);
                cWidth = childView.getMeasuredWidth();
                cHeight = childView.getMeasuredHeight();
                leftX += (mSquareWidth - cWidth) / 2;

                childView.layout((int) leftX, (int) topY, (int) leftX + cWidth, (int) topY
                        + cHeight);
                ;
            }
        }
    }

    public void setLockMode(int from) {
        mLockMode = from;
    }

    private Context getThemepkgConotext(String pkgName) {
        Context context = AppMasterApplication.getInstance();

        Context themeContext = LeoResources.getThemeContext(context, pkgName);
        return themeContext;
    }

    private boolean needChangeTheme() {
        return ThemeUtils.checkThemeNeed(mContext)
                && (mLockMode == LockManager.LOCK_MODE_FULL);
    }

    public void setIsFromLockScreenActivity(boolean isture) {
        this.isFromLockScreenActivity = isture;
    }

    public void resetIfHideLine() {
        isHideLine = AppMasterPreference.getInstance(mContext).getIsHideLine();
    }

}
