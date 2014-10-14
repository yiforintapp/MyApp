package com.leo.appmaster.ui;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.leo.appmaster.model.BaseInfo;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class LeoGridView extends GridView {

	public interface AnimEndListener {
		public void onAnimEnd();
	}
	private LeoGridBaseAdapter mDragAdapter;
	private int mNumColumns;
	private int mColumnWidth;
	private boolean mNumColumnsSet;
	private int mHorizontalSpacing;
	private int mVerticalSpacing;
	private AnimEndListener mAnimEndListener;

	public void setOnAnimEndListener(AnimEndListener listener) {
		mAnimEndListener = listener;
	}

	public LeoGridView(Context context) {
		this(context, null);
	}

	public LeoGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LeoGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		if (!mNumColumnsSet) {
			mNumColumns = AUTO_FIT;
		}
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}

	public void removeItemAnimation(final int position, BaseInfo removeApp) {
		mDragAdapter.removeItem(position);
		final ViewTreeObserver observer = getViewTreeObserver();
		observer.addOnPreDrawListener(new OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				observer.removeOnPreDrawListener(this);
				animateReorder(position, getLastVisiblePosition() + 1);
				return true;
			}
		});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);

		if (adapter instanceof LeoGridBaseAdapter) {
			mDragAdapter = (LeoGridBaseAdapter) adapter;
		} else {
			throw new IllegalStateException(
					"the adapter must be implements DragGridAdapter");
		}
	}

	@Override
	public void setNumColumns(int numColumns) {
		super.setNumColumns(numColumns);
		mNumColumnsSet = true;
		this.mNumColumns = numColumns;
	}

	@Override
	public void setColumnWidth(int columnWidth) {
		super.setColumnWidth(columnWidth);
		mColumnWidth = columnWidth;
	}

	@Override
	public void setHorizontalSpacing(int horizontalSpacing) {
		super.setHorizontalSpacing(horizontalSpacing);
		this.mHorizontalSpacing = horizontalSpacing;
	}

	@Override
	public void setVerticalSpacing(int verticalSpacing) {
		super.setVerticalSpacing(verticalSpacing);
		this.mVerticalSpacing = verticalSpacing;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mNumColumns == AUTO_FIT) {
			int numFittedColumns;
			if (mColumnWidth > 0) {
				int gridWidth = Math.max(MeasureSpec.getSize(widthMeasureSpec)
						- getPaddingLeft() - getPaddingRight(), 0);
				numFittedColumns = gridWidth / mColumnWidth;
				if (numFittedColumns > 0) {
					while (numFittedColumns != 1) {
						if (numFittedColumns * mColumnWidth
								+ (numFittedColumns - 1) * mHorizontalSpacing > gridWidth) {
							numFittedColumns--;
						} else {
							break;
						}
					}
				} else {
					numFittedColumns = 1;
				}
			} else {
				numFittedColumns = 2;
			}
			mNumColumns = numFittedColumns;
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private AnimatorSet createTranslationAnimations(View view, float startX,
			float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
				startX, endX);
		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
				startY, endY);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY);
		return animSetXY;
	}

	private void animateReorder(final int oldPosition, final int newPosition) {
		boolean isForward = newPosition > oldPosition;
		List<Animator> resultList = new LinkedList<Animator>();
		if (isForward) {
			for (int pos = oldPosition; pos < newPosition; pos++) {
				View view = getChildAt(pos - getFirstVisiblePosition());
				if ((pos + 1) % mNumColumns == 0 && (pos + 1) != newPosition) {
					resultList.add(createTranslationAnimations(view,
							-(view.getWidth() + mHorizontalSpacing)
									* (mNumColumns - 1), 0, view.getHeight()
									+ mVerticalSpacing, 0));
				} else {
					resultList.add(createTranslationAnimations(view,
							view.getWidth() + mHorizontalSpacing, 0, 0, 0));
				}
			}

		} else {
			for (int pos = oldPosition; pos > newPosition; pos--) {
				View view = getChildAt(pos - getFirstVisiblePosition());
				if ((pos) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view,
							(view.getWidth() + mHorizontalSpacing)
									* (mNumColumns - 1), 0, -view.getHeight()
									- mVerticalSpacing, 0));
				} else {
					resultList.add(createTranslationAnimations(view,
							-view.getWidth() - mHorizontalSpacing, 0, 0, 0));
				}
			}
		}

		AnimatorSet resultSet = new AnimatorSet();
		resultSet.playTogether(resultList);
		// resultSet.playSequentially(resultList);
		resultSet.setDuration(500);
		resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (mAnimEndListener != null) {
					mAnimEndListener.onAnimEnd();
				}
			}
		});
		resultSet.start();
	}

	class YScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (distanceY != 0 && distanceX != 0) {

			}
			if (Math.abs(distanceY) >= Math.abs(distanceX)) {
				return true;
			}
			return false;
		}
	}
}
