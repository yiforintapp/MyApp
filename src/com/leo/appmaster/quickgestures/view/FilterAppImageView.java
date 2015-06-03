
package com.leo.appmaster.quickgestures.view;

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
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.utils.LeoLog;

public class FilterAppImageView extends ImageView {
    private RectF mRect;
    private boolean mDefaultRecommend;
    private Paint mPaint;
    private Matrix mMatrix;
    private float mLockX, mLockY;
    private Bitmap mSourceBitmap, mGaryBitmap;
    private PaintFlagsDrawFilter mDrawFilter;
    private QuickGsturebAppInfo changeInfo;

    public FilterAppImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mLockX = 0;
        mLockY = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (mSourceBitmap == null && changeInfo == null) {
                Drawable d = null;
                d = this.getDrawable();
                LeoLog.d("FilterAppImageView", "正常getDrawable");
                mSourceBitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),
                        d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                Canvas c = new Canvas(mSourceBitmap);
                d.draw(c);
            } else {
                if (changeInfo != null) {
                    Drawable d = null;
                    LeoLog.d("FilterAppImageView", "点击View，刷新状态");
                    d = selectIconOrNot();
                    mSourceBitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),
                            d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    Canvas c = new Canvas(mSourceBitmap);
                    d.draw(c);
                    changeInfo = null;
                }
            }
            if (mDefaultRecommend) {
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
                    int red, green, blue, alpha;
                    int pixel;
                    for (int i = 0; i < mGaryBitmap.getWidth(); i++) {
                        for (int j = 0; j < mGaryBitmap.getHeight(); j++) {
                            pixel = mGaryBitmap.getPixel(i, j);

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
                            R.dimen.quick_gesture_free_distureb_app_select_icon_width);
                    int lockHeight = getResources().getDimensionPixelSize(
                            R.dimen.quick_gesture_free_distureb_app_select_icon_width);

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

    private Drawable selectIconOrNot() {
        // 名字为label的，有无选中？
        Drawable rightIcon = null;
        if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.BLUETOOTH)) {
            // 蓝牙状态
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_bluetooth_pre);
                LeoLog.d("FilterAppImageView", "点击蓝牙 -- On");
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_bluetooth);
                LeoLog.d("FilterAppImageView", "点击蓝牙 -- Off");
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.FLASHLIGHT)) {
            // 手电筒状态
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_flashlight_pre);
                LeoLog.d("FilterAppImageView", "点击手电筒 -- On");
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_flashlight);
                LeoLog.d("FilterAppImageView", "点击手电筒 -- Off");
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.WLAN)) {
            // Wifi状态
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_wifi_pre);
                LeoLog.d("FilterAppImageView", "点击Wifi -- On");
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_wifi);
                LeoLog.d("FilterAppImageView", "点击Wifi -- Off");
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.CRAME)) {
            // Crame状态
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_camera);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_camera_dis);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.SOUND)) {
            // Sound状态
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_volume_max);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_volume);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.LIGHT)) {
            // 亮度状态
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_brightness_min);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_brightness);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.SPEEDUP)) {
            // 加速
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_speed_up);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_speed_up);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.SWITCHSET)) {
            // 手势设置
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_set);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_set_dis);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.SETTING)) {
            // 系统设置
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_gestureset_pre);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_gestureset);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.GPS)) {
            // GPS
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_gps_pre);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_gps);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.FLYMODE)) {
            // 飞行模式
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_flightmode_pre);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_flightmode);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.ROTATION)) {
            // 屏幕旋转
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_rotation_pre);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_rotation);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.MOBILEDATA)) {
            // 移动数据
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_data_pre);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_data);
            }
        } else if (changeInfo.swtichIdentiName.equals(QuickSwitchManager.HOME)) {
            // 桌面
            if (mDefaultRecommend) {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_home);
            } else {
                rightIcon = mContext.getResources().getDrawable(
                        R.drawable.switch_home_dis);
            }
        }
        return rightIcon;
    }

    public void setDefaultRecommendApp(boolean defaultApp) {
        mDefaultRecommend = defaultApp;
        invalidate();
    }

    private static class BitmapHolder {
        private static Bitmap mDefaultRecommend;

        public static Bitmap getDefaultBitmap(Context ctx) {
            if (mDefaultRecommend == null) {
                mDefaultRecommend = BitmapFactory.decodeResource(ctx.getResources(),
                        R.drawable.switch_select);
            }
            return mDefaultRecommend;
        }
    }

    public void setDefaultRecommendApp(QuickGsturebAppInfo selectInfl, boolean defaultApp) {
        LeoLog.d("FilterAppImageView", "点击按钮，刷新了哦！");
        mDefaultRecommend = defaultApp;
        changeInfo = selectInfl;
        invalidate();
    }
}
