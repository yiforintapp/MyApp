package com.leo.appmaster.ui;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class LockImageView extends ImageView {
	private RectF mRect;

	private boolean mLocked;
	private Bitmap mLockBitmap;
	private Paint mPaint;
	private float mScaleX, mScaleY;
	private Matrix mMatrix;
	private float mLockWidth, mLockHeight;
	private float mLockX, mLockY;

	private Bitmap mSourceBitmap, mGaryBitmap;

	public LockImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (mSourceBitmap == null) {
			Drawable d = this.getDrawable();
			mSourceBitmap = ((BitmapDrawable) d).getBitmap();
		}

		if (mLocked) {
			if (mRect == null) {
				mRect = new RectF();
				int width = this.getMeasuredWidth();
				int height = this.getMeasuredHeight();
				mRect.left = 0;
				mRect.top = 0;
				mRect.right = width;
				mRect.bottom = height;
			}

			if (mGaryBitmap == null) {
				mGaryBitmap = mSourceBitmap.copy(mSourceBitmap.getConfig(),
						true);
				int red, green, blue, alpha, agr;
				int pixel;
				for (int i = 0; i < mGaryBitmap.getWidth(); i++) {
					for (int j = 0; j < mGaryBitmap.getHeight(); j++) {
						pixel = mGaryBitmap.getPixel(i, j);
						agr = (Color.red(pixel) + Color.green(pixel) + Color
								.blue(pixel)) / 3;

						alpha = (int) (Color.alpha(pixel));
						red = (int) (Color.red(pixel) * 0.35);
						green = (int) (Color.green(pixel) * 0.35);
						blue = (int) (Color.blue(pixel) * 0.35);

						pixel = Color.argb(alpha, red, green, blue);
						// pixel = Color.argb(alpha, agr, agr, agr);
						mGaryBitmap.setPixel(i, j, pixel);
					}
				}
			}
			this.setImageBitmap(mGaryBitmap);
			super.onDraw(canvas);

			if (mLockBitmap == null) {
				mLockBitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.lock_icon);

				mLockWidth = getResources().getDimensionPixelSize(
						R.dimen.lock_icon_width);
				mLockHeight = getResources().getDimensionPixelSize(
						R.dimen.lock_icon_height);

				mLockX = getResources().getDimensionPixelSize(
						R.dimen.lock_icon_X);
				mLockY = getResources().getDimensionPixelSize(
						R.dimen.lock_icon_Y);

				mScaleX = mLockWidth / mLockBitmap.getWidth();
				mScaleY = mLockHeight / mLockBitmap.getHeight();

				mMatrix = new Matrix();
				mMatrix.setScale(mScaleX, mScaleY, mLockBitmap.getWidth() / 2,
						mLockBitmap.getHeight() / 2);
			}

			canvas.save();
			canvas.translate(mLockX, mLockY);
			canvas.drawBitmap(mLockBitmap, mMatrix, mPaint);
			canvas.restore();
		} else {
			this.setImageBitmap(mSourceBitmap);
			super.onDraw(canvas);
		}
	}

	public void setLocked(boolean locked) {
		mLocked = locked;
		invalidate();
	}
}
