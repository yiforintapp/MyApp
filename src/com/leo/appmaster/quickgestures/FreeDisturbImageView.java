
package com.leo.appmaster.quickgestures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.leo.appmaster.R;

public class FreeDisturbImageView extends ImageView {
    private RectF mRect;
    private boolean mLocked;
    private boolean mRecommend;
    private boolean mDefaultRecommend;
    private Paint mPaint;
    private Matrix mMatrix;
    private float mLockX, mLockY;
    private Bitmap mSourceBitmap, mGaryBitmap;
    private PaintFlagsDrawFilter mDrawFilter;

    public FreeDisturbImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mLockX = getResources().getDimensionPixelSize(R.dimen.lock_icon_X);
        mLockY = getResources().getDimensionPixelSize(R.dimen.lock_icon_Y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Add protect to avoid OutOfMemory error temply
        try {
            if (mSourceBitmap == null) {
                Drawable d = this.getDrawable();
                mSourceBitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),
                        d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                Canvas c = new Canvas(mSourceBitmap);
                d.draw(c);
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
                if (mDrawFilter == null) {
                    mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                            | Paint.FILTER_BITMAP_FLAG);
                    canvas.setDrawFilter(mDrawFilter);
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
                            red = (int) (Color.red(pixel) * 0.5);
                            green = (int) (Color.green(pixel) * 0.5);
                            blue = (int) (Color.blue(pixel) * 0.5);

                            pixel = Color.argb(alpha, red, green, blue);
                            mGaryBitmap.setPixel(i, j, pixel);
                        }
                    }
                }
                this.setImageBitmap(mGaryBitmap);
                super.onDraw(canvas);
                canvas.save();
                canvas.translate(mLockX, mLockY);
                Bitmap lockBitmap = BitmapHolder.getLockBitmap(getContext());
                if (mMatrix == null) {
                    int lockWidth = getResources().getDimensionPixelSize(
                            R.dimen.lock_icon_width);
                    int lockHeight = getResources().getDimensionPixelSize(
                            R.dimen.lock_icon_height);

                    float scaleX = (float) lockWidth / lockBitmap.getWidth();
                    float scaleY = (float) lockHeight / lockBitmap.getHeight();

                    mMatrix = new Matrix();
                    mMatrix.setScale(scaleX, scaleY, lockBitmap.getWidth() / 2,
                            lockBitmap.getHeight() / 2);
                }
                canvas.drawBitmap(lockBitmap, mMatrix, mPaint);
                canvas.restore();
            } else if (mRecommend) {
                if (mRect == null) {
                    mRect = new RectF();
                    int width = this.getMeasuredWidth();
                    int height = this.getMeasuredHeight();
                    mRect.left = 0;
                    mRect.top = 0;
                    mRect.right = width;
                    mRect.bottom = height;
                }
                if (mDrawFilter == null) {
                    mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                            | Paint.FILTER_BITMAP_FLAG);
                    canvas.setDrawFilter(mDrawFilter);
                }

                this.setImageBitmap(mSourceBitmap);
                super.onDraw(canvas);
                canvas.save();
                canvas.translate(mLockX, mLockY);
                Bitmap recommendBitmap = BitmapHolder
                        .getRecommendBitmap(getContext());
                if (mMatrix == null) {
                    int lockWidth = getResources().getDimensionPixelSize(
                            R.dimen.lock_icon_width);
                    int lockHeight = getResources().getDimensionPixelSize(
                            R.dimen.lock_icon_height);

                    float scaleX = (float) lockWidth / recommendBitmap.getWidth();
                    float scaleY = (float) lockHeight / recommendBitmap.getHeight();

                    mMatrix = new Matrix();
                    mMatrix.setScale(scaleX, scaleY,
                            recommendBitmap.getWidth() / 2,
                            recommendBitmap.getHeight() / 2);
                }
                canvas.drawBitmap(recommendBitmap, mMatrix, mPaint);
                canvas.restore();
            } else if (mDefaultRecommend) {
                if (mRect == null) {
                    mRect = new RectF();
                    int width = this.getMeasuredWidth();
                    int height = this.getMeasuredHeight();
                    mRect.left = 0;
                    mRect.top = 0;
                    mRect.right = width;
                    mRect.bottom = height;
                }
                if (mDrawFilter == null) {
                    mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                            | Paint.FILTER_BITMAP_FLAG);
                    canvas.setDrawFilter(mDrawFilter);
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
                            red = (int) (Color.red(pixel) * 0.5);
                            green = (int) (Color.green(pixel) * 0.5);
                            blue = (int) (Color.blue(pixel) * 0.5);

                            pixel = Color.argb(alpha, red, green, blue);
                            mGaryBitmap.setPixel(i, j, pixel);
                        }
                    }
                }
                this.setImageBitmap(mGaryBitmap);
                super.onDraw(canvas);
                canvas.save();
                canvas.translate(mLockX, mLockY);
                Bitmap lockBitmap = BitmapHolder.getDefaultBitmap(getContext());
                if (mMatrix == null) {
                    int lockWidth = getResources().getDimensionPixelSize(
                            R.dimen.default_lock_icon_width);
                    int lockHeight = getResources().getDimensionPixelSize(
                            R.dimen.lock_icon_height);

                    float scaleX = (float) lockWidth / lockBitmap.getWidth();
                    float scaleY = (float) lockHeight / lockBitmap.getHeight();

                    mMatrix = new Matrix();
                    mMatrix.setScale(scaleX, scaleY, lockBitmap.getWidth() / 2,
                            lockBitmap.getHeight() / 2);
                }
                canvas.drawBitmap(lockBitmap, mMatrix, mPaint);
                canvas.restore();

            } else {
                this.setImageBitmap(mSourceBitmap);
                super.onDraw(canvas);
            }
        } catch (Exception e) {

        }
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
        invalidate();
    }

    public void setRecommend(boolean recommend) {
        mRecommend = recommend;
    }

    public void setDefaultRecommendApp(boolean defaultApp) {
        mDefaultRecommend = defaultApp;
        invalidate();
    }

    private static class BitmapHolder {
        private static Bitmap mLockBitmap, mRecommendBitmap, mDefaultRecommend;

        public static Bitmap getLockBitmap(Context ctx) {
            if (mLockBitmap == null) {
                mLockBitmap = BitmapFactory.decodeResource(ctx.getResources(),
                        R.drawable.lock_icon);
            }

            return mLockBitmap;
        }

        public static Bitmap getRecommendBitmap(Context ctx) {
            if (mRecommendBitmap == null) {
                mRecommendBitmap = BitmapFactory.decodeResource(
                        ctx.getResources(), R.drawable.star_icon);
            }

            return mRecommendBitmap;
        }

        public static Bitmap getDefaultBitmap(Context ctx) {
            if (mDefaultRecommend == null) {
                mDefaultRecommend = BitmapFactory.decodeResource(ctx.getResources(),
                        R.drawable.select_icon);
            }
            return mDefaultRecommend;
        }

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

}
