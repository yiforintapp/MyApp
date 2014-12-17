package com.leo.appmaster.appmanage.view;

import com.leo.appmaster.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.SpinnerAdapter;

public class FolderTitleGallery extends Gallery {

	public static final int ACTION_DISTANCE_AUTO = Integer.MAX_VALUE;

	public static final float SCALEDOWN_GRAVITY_TOP = 0.0f;

	public static final float SCALEDOWN_GRAVITY_CENTER = 0.5f;

	public static final float SCALEDOWN_GRAVITY_BOTTOM = 1.0f;

	private float reflectionRatio = 0.4f;

	private int reflectionGap = 20;

	private boolean reflectionEnabled = false;
	private float unselectedAlpha;

	private Camera transformationCamera;
	private int maxRotation = 75;
	private float unselectedScale;
	private float scaleDownGravity = SCALEDOWN_GRAVITY_CENTER;
	private int actionDistance;
	private float unselectedSaturation;

	public FolderTitleGallery(Context context) {
		super(context);
		this.initialize();
	}

	public FolderTitleGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initialize();
		this.applyXmlAttributes(attrs);
	}

	public FolderTitleGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.initialize();
		this.applyXmlAttributes(attrs);
	}

	private void initialize() {
		this.transformationCamera = new Camera();
		this.setSpacing(0);
	}

	private void applyXmlAttributes(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.FolderTitleGallery);

		this.actionDistance = a.getInteger(
				R.styleable.FolderTitleGallery_actionDistance,
				ACTION_DISTANCE_AUTO);
		this.scaleDownGravity = a.getFloat(
				R.styleable.FolderTitleGallery_scaleDownGravity, 1.0f);
		this.maxRotation = a.getInteger(
				R.styleable.FolderTitleGallery_maxRotation, 45);
		this.unselectedAlpha = a.getFloat(
				R.styleable.FolderTitleGallery_unselectedAlpha, 0.3f);
		this.unselectedSaturation = a.getFloat(
				R.styleable.FolderTitleGallery_unselectedSaturation, 0.0f);
		this.unselectedScale = a.getFloat(
				R.styleable.FolderTitleGallery_unselectedScale, 0.75f);
	}

	public float getReflectionRatio() {
		return reflectionRatio;
	}

	public void setReflectionRatio(float reflectionRatio) {
		if (reflectionRatio <= 0 || reflectionRatio > 0.5f) {
			throw new IllegalArgumentException(
					"reflectionRatio may only be in the interval (0, 0.5]");
		}

		this.reflectionRatio = reflectionRatio;

		if (this.getAdapter() != null) {
			((FolderTitleGalleryAdapter) this.getAdapter())
					.notifyDataSetChanged();
		}
	}

	public int getReflectionGap() {
		return reflectionGap;
	}

	public void setReflectionGap(int reflectionGap) {
		this.reflectionGap = reflectionGap;

		if (this.getAdapter() != null) {
			((FolderTitleGalleryAdapter) this.getAdapter())
					.notifyDataSetChanged();
		}
	}

	public boolean isReflectionEnabled() {
		return reflectionEnabled;
	}

	public void setReflectionEnabled(boolean reflectionEnabled) {
		this.reflectionEnabled = reflectionEnabled;

		if (this.getAdapter() != null) {
			((FolderTitleGalleryAdapter) this.getAdapter())
					.notifyDataSetChanged();
		}
	}

	/**
	 * Use this to provide a {@link FolderTitleGalleryAdapter} to the coverflow.
	 * This method will throw an {@link ClassCastException} if the passed
	 * adapter does not subclass {@link FolderTitleGalleryAdapter}.
	 * 
	 * @param adapter
	 */
	@Override
	public void setAdapter(SpinnerAdapter adapter) {
		if (!(adapter instanceof FolderTitleGalleryAdapter)) {
			throw new ClassCastException(
					FolderTitleGallery.class.getSimpleName()
							+ " only works in conjunction with a "
							+ FolderTitleGalleryAdapter.class.getSimpleName());
		}

		super.setAdapter(adapter);
	}

	/**
	 * Returns the maximum rotation that is applied to items left and right of
	 * the center of the coverflow.
	 * 
	 * @return
	 */
	public int getMaxRotation() {
		return maxRotation;
	}

	/**
	 * Sets the maximum rotation that is applied to items left and right of the
	 * center of the coverflow.
	 * 
	 * @param maxRotation
	 */
	public void setMaxRotation(int maxRotation) {
		this.maxRotation = maxRotation;
	}

	public float getUnselectedAlpha() {
		return this.unselectedAlpha;
	}

	public float getUnselectedScale() {
		return unselectedScale;
	}

	public void setUnselectedScale(float unselectedScale) {
		this.unselectedScale = unselectedScale;
	}

	public float getScaleDownGravity() {
		return scaleDownGravity;
	}

	public void setScaleDownGravity(float scaleDownGravity) {
		this.scaleDownGravity = scaleDownGravity;
	}

	public int getActionDistance() {
		return actionDistance;
	}

	public void setActionDistance(int actionDistance) {
		this.actionDistance = actionDistance;
	}

	@Override
	public void setUnselectedAlpha(float unselectedAlpha) {
		super.setUnselectedAlpha(unselectedAlpha);
		this.unselectedAlpha = unselectedAlpha;
	}

	public float getUnselectedSaturation() {
		return unselectedSaturation;
	}

	public void setUnselectedSaturation(float unselectedSaturation) {
		this.unselectedSaturation = unselectedSaturation;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		// We can cast here because FancyCoverFlowAdapter only creates wrappers.
		FolderTitleGalleryItemWrapper item = (FolderTitleGalleryItemWrapper) child;

		// Since Jelly Bean childs won't get invalidated automatically, needs to
		// be added for the smooth coverflow animation
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			item.invalidate();
		}

		final int coverFlowWidth = this.getWidth();
		final int coverFlowCenter = coverFlowWidth / 2;
		final int childWidth = item.getWidth();
		final int childHeight = item.getHeight();
		final int childCenter = item.getLeft() + childWidth / 2;

		// Use coverflow width when its defined as automatic.
		final int actionDistance = (this.actionDistance == ACTION_DISTANCE_AUTO) ? (int) ((coverFlowWidth + childWidth) / 2.0f)
				: this.actionDistance;

		// Calculate the abstract amount for all effects.
		final float effectsAmount = Math.min(
				1.0f,
				Math.max(-1.0f, (1.0f / actionDistance)
						* (childCenter - coverFlowCenter)));

		// Clear previous transformations and set transformation type (matrix +
		// alpha).
		t.clear();
		t.setTransformationType(Transformation.TYPE_BOTH);

		// Alpha
		if (this.unselectedAlpha != 1) {
			final float alphaAmount = (this.unselectedAlpha - 1)
					* Math.abs(effectsAmount) + 1;
			t.setAlpha(alphaAmount);
		}

		// Saturation
		if (this.unselectedSaturation != 1) {
			// Pass over saturation to the wrapper.
			final float saturationAmount = (this.unselectedSaturation - 1)
					* Math.abs(effectsAmount) + 1;
			item.setSaturation(saturationAmount);
		}

		final Matrix imageMatrix = t.getMatrix();

		// Apply rotation.
		if (this.maxRotation != 0) {
			final int rotationAngle = (int) (-effectsAmount * this.maxRotation);
			this.transformationCamera.save();
			this.transformationCamera.rotateY(rotationAngle);
			this.transformationCamera.getMatrix(imageMatrix);
			this.transformationCamera.restore();
		}

		// Zoom.
		if (this.unselectedScale != 1) {
			final float zoomAmount = (this.unselectedScale - 1)
					* Math.abs(effectsAmount) + 1;
			// Calculate the scale anchor (y anchor can be altered)
			final float translateX = childWidth / 2.0f;
			final float translateY = childHeight * this.scaleDownGravity;
			imageMatrix.preTranslate(-translateX, -translateY);
			imageMatrix.postScale(zoomAmount, zoomAmount);
			imageMatrix.postTranslate(translateX, translateY);
		}

		return true;
	}

	public static class LayoutParams extends Gallery.LayoutParams {
		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}
}
