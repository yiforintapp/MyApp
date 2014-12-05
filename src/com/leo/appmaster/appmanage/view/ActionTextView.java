package com.leo.appmaster.appmanage.view;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.HolographicOutlineHelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TextView;

public class ActionTextView extends TextView implements IViewPressed {
	private DecorateAction mAction;

	private Bitmap mPressedOrFocusedBackground;
	private boolean mStayPressed;
	private final Canvas mTempCanvas = new Canvas();
	private final Rect mTempRect = new Rect();
	private int mPressedGlowColor;
	private int mPressedOutlineColor;
	private int mFocusedGlowColor;
	private int mFocusedOutlineColor;
	private boolean mDidInvalidateForPressedState;
	public boolean shouldDrawBlur = true;

	private boolean isHide;
	private boolean isAlphaAnimating = false;

	public ActionTextView(Context context) {
		super(context);
	} 

	public ActionTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ActionView, 0, 0);
		int actionType = a.getInt(R.styleable.ActionView_action, 0);
		init(actionType);
	}

	public ActionTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ActionView, defStyle, 0);
		int actionType = a.getInt(R.styleable.ActionView_action, 0);
		init(actionType);
	}

	private void init(int type) {
		if (type == DecorateAction.ACTION_STATUS) {
			mAction = new StatusAction();
			int offsetX = getContext().getResources().getDimensionPixelSize(
					R.dimen.new_install_prompt_offsetx);
			int offsetY = getContext().getResources().getDimensionPixelSize(
					R.dimen.new_install_prompt_offsety);
			((StatusAction) mAction).setNewInstallPromptOffsetX(offsetX);
			((StatusAction) mAction).setNewInstallPromptOffsetY(offsetY);
		} else {
			mAction = new NoneAction();
		}
		mFocusedOutlineColor = mFocusedGlowColor = mPressedOutlineColor = mPressedGlowColor = getContext()
				.getResources().getColor(R.color.pressed_view_color);
	}

	@Override
	public void draw(Canvas canvas) {
		if ((isHide || isAlphaAnimating)) {
			super.draw(canvas);
		} else {
			super.draw(canvas);
		}

		mAction.draw(canvas, this);

	}

	public Object getActionInfo() {
		return getTag();
	}

	public void setScrollXY(int scrollX, int scrollY) {
		mScrollX = scrollX;
		mScrollY = scrollY;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Call the superclass onTouchEvent first, because sometimes it changes
		// the state to
		// isPressed() on an ACTION_UP
		mAction.onTouchEventCheck(event);
		boolean result = super.onTouchEvent(event);
		if (shouldDrawBlur
		/*
		 * && (LauncherPreferenceHelper.AppListMode ==
		 * LauncherPreferenceHelper.MODE_GRID_NORMAL ||
		 * LauncherPreferenceHelper.AppListMode ==
		 * LauncherPreferenceHelper.MODE_INDEX_NORMAL)
		 */) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// So that the pressed outline is visible immediately when
				// isPressed() is true,
				// we pre-create it on ACTION_DOWN (it takes a small but
				// perceptible
				// amount of time
				// to create it)
				if (mPressedOrFocusedBackground == null) {
					mPressedOrFocusedBackground = createGlowingOutline(
							mTempCanvas, mPressedGlowColor,
							mPressedOutlineColor);
				}
				// Invalidate so the pressed state is visible, or set a flag
				// so we
				// know that we
				// have to call invalidate as soon as the state is "pressed"
				if (isPressed()) {
					mDidInvalidateForPressedState = true;
					setCellLayoutPressedOrFocusedIcon();
				} else {
					mDidInvalidateForPressedState = false;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				// If we've touched down and up on an item, and it's still
				// not
				// "pressed", then
				// destroy the pressed outline
				if (!isPressed()) {
					releasePressedBackground();
				}
				break;
			}
		}

		return result;
	}

	private void releasePressedBackground() {
		if (mPressedOrFocusedBackground != null) {
			mPressedOrFocusedBackground.recycle();
		}
		mPressedOrFocusedBackground = null;
	}

	@Override
	protected void drawableStateChanged() {
		if (shouldDrawBlur) {
			if (isPressed()) {
				// In this case, we have already created the pressed outline on
				// ACTION_DOWN,
				// so we just need to do an invalidate to trigger draw
				if (!mDidInvalidateForPressedState) {
					setCellLayoutPressedOrFocusedIcon();
				}
			} else {
				// Otherwise, either clear the pressed/focused background, or
				// create
				// a background
				// for the focused state
				final boolean backgroundEmptyBefore = mPressedOrFocusedBackground == null;
				if (!mStayPressed) {
					releasePressedBackground();
				}
				if (isFocused()) {
					if (getLayout() == null) {
						// In some cases, we get focus before we have been layed
						// out. Set the
						// background to null so that it will get created when
						// the
						// view is drawn.
						releasePressedBackground();
					} else {
						mPressedOrFocusedBackground = createGlowingOutline(
								mTempCanvas, mFocusedGlowColor,
								mFocusedOutlineColor);
					}
					mStayPressed = false;
					setCellLayoutPressedOrFocusedIcon();
				}
				final boolean backgroundEmptyNow = mPressedOrFocusedBackground == null;
				if (!backgroundEmptyBefore && backgroundEmptyNow) {
					setCellLayoutPressedOrFocusedIcon();
				}
			}
		}
		super.drawableStateChanged();
	}

	public void setStayPressed(boolean stayPressed) {
		mStayPressed = stayPressed;
		if (!stayPressed) {
			releasePressedBackground();
		}
		setCellLayoutPressedOrFocusedIcon();
	}

	private void setCellLayoutPressedOrFocusedIcon() {
		ViewParent parent = getParent();
		// while (parent != null) {
		// if (parent instanceof DragLayer) {
		// DragLayer root = (DragLayer) parent;
		// if (root != null) {
		// root.setPressedOrFocusedIcon((mPressedOrFocusedBackground != null) ?
		// this
		// : null);
		// return;
		// }
		// }
		// parent = parent.getParent();
		// }

	}

	public void clearPressedOrFocusedBackground() {
		releasePressedBackground();
		setCellLayoutPressedOrFocusedIcon();
	}

	public Bitmap getPressedOrFocusedBackground() {
		return mPressedOrFocusedBackground;
	}

	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize
	 * the drop location. Responsibility for the bitmap is transferred to the
	 * caller.
	 */
	private Bitmap createGlowingOutline(Canvas canvas, int outlineColor,
			int glowColor) {
		final int padding = 20;
		final Bitmap b = Bitmap.createBitmap(getWidth() + padding, getHeight()
				+ padding, Bitmap.Config.ARGB_8888);

		canvas.setBitmap(b);
		drawWithPadding(canvas, padding);
		HolographicOutlineHelper.getInstance()
				.applyExtraThickExpensiveOutlineWithBlur(b, canvas, glowColor,
						outlineColor);
		if (AppMasterApplication.isAboveICS()) {
			canvas.setBitmap(null);
		}

		return b;
	}

	/**
	 * Draw this BubbleTextView into the given Canvas.
	 * 
	 * @param destCanvas
	 *            the canvas to draw on
	 * @param padding
	 *            the horizontal and vertical padding to use when drawing
	 */
	private void drawWithPadding(Canvas destCanvas, int padding) {
		final Rect clipRect = mTempRect;
		getDrawingRect(clipRect);

		// adjust the clip rect so that we don't include the text label
		clipRect.bottom = getExtendedPaddingTop() + getLayout().getLineTop(0);

		// Draw the View into the bitmap.
		// The translate of scrollX and scrollY is necessary when drawing
		// TextViews, because
		// they set scrollX and scrollY to large values to achieve centered text
		int count = destCanvas.save();
		destCanvas.translate(-getScrollX() + padding / 2, -getScrollY()
				+ padding / 2);
		destCanvas.clipRect(clipRect, Op.REPLACE);
		draw(destCanvas);
		destCanvas.restoreToCount(count);
	}

	public void setHide(boolean isHidden) {
		isHide = isHidden;
		isAlphaAnimating = false;
		postInvalidate();
	}

	public DecorateAction getDecorateAction() {
		return mAction;
	}

	@Override
	public int getPressedOrFocusedBackgroundPadding() {
		// TODO Auto-generated method stub
		return 0;
	}
}
