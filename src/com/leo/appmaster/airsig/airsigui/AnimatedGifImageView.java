package com.leo.appmaster.airsig.airsigui;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class AnimatedGifImageView extends ImageView {
	public static enum TYPE {
		FIT_CENTER, STREACH_TO_FIT, AS_IS
	};

	public AnimatedGifImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AnimatedGifImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AnimatedGifImageView(Context context) {
		super(context);
	}

	boolean animatedGifImage = false;
	private InputStream is = null;
	private Movie mMovie = null;
	private long mMovieStart = 0;
	private int mStartTime = -1, mEndTime = -1, mPauseAtEndTime = 0;
	private TYPE mType = TYPE.FIT_CENTER;
	private float mCustomScale = 0f;

	public void setAnimatedGif(int rawResourceId, TYPE streachType) {
		setAnimatedGif(rawResourceId, streachType, 0);
	}
	
	public void setAnimatedGif(int rawResourceId, float scale) {
		setAnimatedGif(rawResourceId, TYPE.AS_IS, scale);
	}
	
	public void setAnimatedGif(int rawResourceId, TYPE streachType, float scale) {
		setImageBitmap(null);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		mType = streachType;
		mCustomScale = scale * getResources().getDisplayMetrics().density / 2;
		animatedGifImage = true;
		is = getContext().getResources().openRawResource(rawResourceId);
		try {
			mMovie = Movie.decodeStream(is);
		} catch (Exception e) {
			e.printStackTrace();
			byte[] array = streamToBytes(is);
			mMovie = Movie.decodeByteArray(array, 0, array.length);
		}
		p = new Paint();
		resetAnimatedGifPlayTime();
	}

	public void setAnimatedGif(String filePath, TYPE streachType) throws FileNotFoundException {
		setImageBitmap(null);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		mType = streachType;
		mCustomScale = 0f;
		animatedGifImage = true;
		InputStream is;
		try {
			mMovie = Movie.decodeFile(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			is = new FileInputStream(filePath);
			byte[] array = streamToBytes(is);
			mMovie = Movie.decodeByteArray(array, 0, array.length);
		}
		p = new Paint();
		resetAnimatedGifPlayTime();
	}
	
	public void setAnimatedGif(byte[] byteArray, TYPE streachType) throws FileNotFoundException {
		setImageBitmap(null);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		mType = streachType;
		mCustomScale = 0f;
		animatedGifImage = true;

		try {
			mMovie = Movie.decodeByteArray(byteArray, 0, byteArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		p = new Paint();
		resetAnimatedGifPlayTime();
	}
	
	public void restartAnimatedGif() {
		if (animatedGifImage) {
			mMovieStart = 0;
			invalidate();
		}
	}
	
	public void setAnimatedGifPlayTime(int startTime, int endTime, int pauseAtEndTime) {
		mStartTime = startTime;
		mEndTime = endTime;
		mPauseAtEndTime = pauseAtEndTime;
		restartAnimatedGif();
	}
	
	public void resetAnimatedGifPlayTime() {
		mStartTime = -1;
		mEndTime = -1;
		mPauseAtEndTime = 0;
		restartAnimatedGif();
	}
	
	public int getAnimatedGifLength() {
		if (mMovie != null) {
			return mMovie.duration();
		} else {
			return 0;
		}
	}

	@Override
	public void setImageResource(int resId) {
		animatedGifImage = false;
		mMovie = null;
		super.setImageResource(resId);
	}

	@Override
	public void setImageURI(Uri uri) {
		animatedGifImage = false;
		mMovie = null;
		super.setImageURI(uri);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		animatedGifImage = false;
		mMovie = null;
		super.setImageDrawable(drawable);
	}

	Paint p;
	private float mScaleH = 1f, mScaleW = 1f;
	private int mMeasuredMovieWidth;
	private int mMeasuredMovieHeight;
	private float mLeft;
	private float mTop;

	private static byte[] streamToBytes(InputStream is) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = is.read(buffer)) >= 0) {
				os.write(buffer, 0, len);
			}
		} catch (java.io.IOException e) {
		}
		return os.toByteArray();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mMovie != null) {
			int movieWidth = mMovie.width();
			int movieHeight = mMovie.height();
			/*
			 * Calculate horizontal scaling
			 */
			int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);
			float scaleW = 1f, scaleH = 1f;
			if (measureModeWidth != MeasureSpec.UNSPECIFIED) {
				int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
				scaleW = (float) maximumWidth / (float) movieWidth;
			}

			/*
			 * calculate vertical scaling
			 */
			int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);

			if (measureModeHeight != MeasureSpec.UNSPECIFIED) {
				int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
				scaleH = (float) maximumHeight / (float) movieHeight;
			}

			/*
			 * calculate overall scale
			 */
			switch (mType) {
			case FIT_CENTER:
				mScaleH = mScaleW = Math.min(scaleH, scaleW);
				break;
			case AS_IS:
				mScaleH = mScaleW = 1f;
				break;
			case STREACH_TO_FIT:
				mScaleH = scaleH;
				mScaleW = scaleW;
				break;
			}
			
			if (mCustomScale > 0) {
				mScaleH *= mCustomScale;
				mScaleW *= mCustomScale;
			}

			mMeasuredMovieWidth = (int) (movieWidth * mScaleW);
			mMeasuredMovieHeight = (int) (movieHeight * mScaleH);

			setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);

		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
		mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (animatedGifImage) {
			long now = android.os.SystemClock.uptimeMillis();
			if (mMovieStart == 0) { // first time
				mMovieStart = now;
			}
			if (mMovie != null) {
				p.setAntiAlias(true);
				p.setDither(true);
				int dur = mMovie.duration();
				if (dur == 0) {
					dur = 1000;
				}
				if (mStartTime >= 0 && mEndTime >= 0 && mEndTime >= mStartTime) {
					dur = mEndTime - mStartTime;
				}
				int relTime = (int) ((now - mMovieStart) % (dur + mPauseAtEndTime)) + (mStartTime > 0 ? mStartTime : 0);
				if (mEndTime >= 0 && relTime > mEndTime) {
					relTime = mEndTime;
				} else if (relTime > mMovie.duration()) {
					relTime = mMovie.duration();
				}
				mMovie.setTime(relTime);
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				canvas.scale(mScaleW, mScaleH);
				mMovie.draw(canvas, mLeft / mScaleW, mTop / mScaleH);
				canvas.restore();
				invalidate();
			}
		}

	}

}
