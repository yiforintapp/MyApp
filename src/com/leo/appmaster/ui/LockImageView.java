package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.leo.appmaster.R;

public class LockImageView extends ImageView {

//	private RectF mRect;
	private boolean mLocked;
	private boolean mRecommend;
	private boolean mDefaultRecommend;
	private Paint mPaint;
	private Matrix mMatrix;
	private float mLockX, mLockY;
//	private Drawable mGrayDrawable;
	private PaintFlagsDrawFilter mDrawFilter;
	
	private int mLockIconWidth;
	private int mLockIconHeight;
	
	private int mDefLockIconWidth;
    private int mDefLockIconHeight;

	public LockImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);

		mLockX = getResources().getDimensionPixelSize(R.dimen.lock_icon_X);
		mLockY = getResources().getDimensionPixelSize(R.dimen.lock_icon_Y);
		
		mLockIconWidth = getResources().getDimensionPixelSize(R.dimen.lock_icon_width);
		mLockIconHeight = getResources().getDimensionPixelSize(R.dimen.lock_icon_height);
		
		mDefLockIconWidth = getResources().getDimensionPixelSize(R.dimen.default_lock_icon_width);
		mDefLockIconHeight = getResources().getDimensionPixelSize(R.dimen.default_lock_icon_width);
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
	    super.onDraw(canvas);
	    if (mLocked || mRecommend || mDefaultRecommend) {
	        if (mDrawFilter == null) {
	            mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
	                    | Paint.FILTER_BITMAP_FLAG);
	        }
	        canvas.setDrawFilter(mDrawFilter);
	        canvas.save();
            canvas.translate(mLockX, mLockY);
            
            Bitmap lockFlag = null;
            if (mLocked) {
                lockFlag = BitmapHolder.getLockBitmap(getContext());
                initMatrix(lockFlag, mLockIconWidth, mLockIconHeight);
            } else if (mRecommend) {
                lockFlag = BitmapHolder.getRecommendBitmap(getContext());
                initMatrix(lockFlag, mLockIconWidth, mLockIconHeight); 
            } else {
                lockFlag = BitmapHolder.getDefaultBitmap(getContext());
                initMatrix(lockFlag, mDefLockIconWidth, mDefLockIconHeight); 
            }
            canvas.drawBitmap(lockFlag, mMatrix, mPaint);
            canvas.restore();
	    }
	}
	
	private void initMatrix(Bitmap bitmap, int width, int height) {
	    if (mMatrix != null) return;
	    
        float scaleX = (float) width / bitmap.getWidth();
        float scaleY = (float) width / bitmap.getHeight();

        mMatrix = new Matrix();
        mMatrix.setScale(scaleX, scaleY, bitmap.getWidth() / 2, bitmap.getHeight() / 2); 
	}
    
//    private void setGrayBitmap() {
//        Drawable d = getDrawable();
//        Bitmap bitmap = null;
//        if (!(d instanceof BitmapDrawable)) {
//            bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),
//                    d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
//            Canvas c = new Canvas(bitmap);
//            d.draw(c);
//        } else {
//            bitmap = ((BitmapDrawable) d).getBitmap();
//        }
//        
//        Bitmap graBitmap = bitmap.copy(bitmap.getConfig(), true);
//        int red, green, blue, alpha;
//        int pixel;
//        for (int i = 0; i < graBitmap.getWidth(); i++) {
//            for (int j = 0; j < graBitmap.getHeight(); j++) {
//                pixel = graBitmap.getPixel(i, j);
//
//                alpha = (int) (Color.alpha(pixel));
//                red = (int) (Color.red(pixel) * 0.5);
//                green = (int) (Color.green(pixel) * 0.5);
//                blue = (int) (Color.blue(pixel) * 0.5);
//
//                pixel = Color.argb(alpha, red, green, blue);
//                graBitmap.setPixel(i, j, pixel);
//            }
//        }
//        
//    }

    public void setLocked(final boolean locked) {
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
		private static Bitmap mLockBitmap, mRecommendBitmap,mDefaultRecommend;

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
		public static Bitmap getDefaultBitmap(Context ctx){
		    if(mDefaultRecommend == null){
		        mDefaultRecommend = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.select_icon);
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
