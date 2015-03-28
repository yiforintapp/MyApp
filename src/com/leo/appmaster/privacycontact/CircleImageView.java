
package com.leo.appmaster.privacycontact;

import com.leo.appmaster.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircleImageView extends ImageView {

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 1;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    // private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();

    // private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius;

    private boolean mReady;
    private boolean mSetupPending;
    private Matrix mMatrix;
    private Paint mPaint;
    private String mAnswerType;

    public CircleImageView(Context context) {
        super(context);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setScaleType(SCALE_TYPE);
        //
        // TypedArray a = context.obtainStyledAttributes(attrs,
        // R.styleable.CircleImageView, defStyle,
        // 0);
        //
        // mBorderWidth =
        // a.getDimensionPixelSize(R.styleable.CircleImageView_border_width,
        // DEFAULT_BORDER_WIDTH);

        // a.recycle();

        mReady = true;

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.",
                    scaleType));
        }
    }

    public void setAnswerType(String type) {
        mAnswerType = type;
    }

    public void setAnswerStatus(String status) {
        mAnswerType = status;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius, mBitmapPaint);
        canvas.save();
        canvas.translate(
                getResources().getDimension(R.dimen.privacy_contact_circleimage_red_tip_x),
                getResources().getDimension(R.dimen.privacy_contact_circleimage_red_tip_y));
        Bitmap lockBitmap = null;
        if (mAnswerType != null && !"".equals(mAnswerType)) {
            if (PrivacyContactUtils.NORMAL_ANSWER_TYPE.equals(mAnswerType)) {
                lockBitmap = BitmapHolder.getNormalBitmap(getContext());
            } else if (PrivacyContactUtils.HANG_UP_ANSWER_TYPE.equals(mAnswerType)) {
                lockBitmap = BitmapHolder.getHangUpBitmap(getContext());
            } else if (PrivacyContactUtils.RED_TIP.equals(mAnswerType)) {
                lockBitmap = BitmapHolder.getRedTipBitmap(getContext());
            }
            if (mMatrix == null) {
                int lockWidth = getResources().getInteger(
                        R.integer.privacy_contact_red_tip_lockWidth);
                int lockHeight = getResources().getInteger(
                        R.integer.privacy_contact_red_tip_lockHeight);

                float scaleX = (float) lockWidth / lockBitmap.getWidth();
                float scaleY = (float) lockHeight / lockBitmap.getHeight();

                mMatrix = new Matrix();
                mMatrix.setScale(scaleX, scaleY, lockBitmap.getWidth() / 2,
                        lockBitmap.getHeight() / 2);
            }
            canvas.drawBitmap(lockBitmap, mMatrix, mPaint);
        }

    }

    private static class BitmapHolder {
        private static Bitmap mNormalBitmap, mHangUpBitmap;

        public static Bitmap getNormalBitmap(Context ctx) {
            if (mNormalBitmap == null) {
                mNormalBitmap = BitmapFactory.decodeResource(ctx.getResources(),
                        R.drawable.lock_icon);
            }

            return mNormalBitmap;
        }

        public static Bitmap getHangUpBitmap(Context ctx) {
            if (mHangUpBitmap == null) {
                mHangUpBitmap = BitmapFactory.decodeResource(
                        ctx.getResources(), R.drawable.star_icon);
            }

            return mHangUpBitmap;
        }

        public static Bitmap getRedTipBitmap(Context ctx) {
            if (mHangUpBitmap == null) {
                mHangUpBitmap = BitmapFactory.decodeResource(
                        ctx.getResources(), R.drawable.red_tip);
            }

            return mHangUpBitmap;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION,
                        BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void setup() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(Color.WHITE);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        mBorderRect.set(0, 0, getWidth(), getHeight());
        mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width() - mBorderWidth,
                mBorderRect.height() - mBorderWidth);
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2);

        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth, (int) (dy + 0.5f)
                + mBorderWidth);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }

}
