
package com.zlf.appmaster.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.WindowManager;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.utils.LeoLog;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {

    public static Bitmap createGaryBitmap(Bitmap source) {
        if (source == null)
            return null;
        Bitmap mGaryBitmap = source.copy(source.getConfig(), true);
        int red, green, blue, alpha;
        int pixel;
        for (int i = 0; i < mGaryBitmap.getWidth(); i++) {
            for (int j = 0; j < mGaryBitmap.getHeight(); j++) {
                pixel = mGaryBitmap.getPixel(i, j);

                alpha = (int) (Color.alpha(pixel));
                red = (int) (Color.red(pixel) * 0.35);
                green = (int) (Color.green(pixel) * 0.35);
                blue = (int) (Color.blue(pixel) * 0.35);

                pixel = Color.argb(alpha, red, green, blue);
                mGaryBitmap.setPixel(i, j, pixel);
            }
        }
        return mGaryBitmap;
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable != null && drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    public static BitmapDrawable bitmapToDrawable(Bitmap bitamp) {
        return new BitmapDrawable(bitamp);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Drawable getRoundedCornerBitmap(BitmapDrawable bitmapDrawable, float roundPx) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return new BitmapDrawable(AppMasterApplication.getInstance().getResources(), bitmap);
    }

    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
        final int reflectionGap = 4;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, h / 2, w,
                h / 2, matrix, false);

        Bitmap bitmapWithReflection = Bitmap.createBitmap(w, (h + h / 2),
                Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint deafalutPaint = new Paint();
        canvas.drawRect(0, h, w, h + reflectionGap, deafalutPaint);

        canvas.drawBitmap(reflectionImage, 0, h + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
                0x00ffffff, TileMode.CLAMP);
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, h, w, bitmapWithReflection.getHeight()
                + reflectionGap, paint);

        return bitmapWithReflection;
    }

    public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        // drawable转换成bitmap
        Bitmap oldbmp = drawableToBitmap(drawable);
        // 创建操作图片用的Matrix对象
        Matrix matrix = new Matrix();
        // 计算缩放比例
        float sx = ((float) w / width);
        float sy = ((float) h / height);
        // 设置缩放比例
        matrix.postScale(sx, sy);
        // 建立新的bitmap，其内容是对原bitmap的缩放后的图
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
                matrix, true);
        return new BitmapDrawable(newbmp);
    }
    
    public static Bitmap bytes2BimapWithScale(byte[] data, Context context) {
        BitmapFactory.Options option = new Options();
        option.inJustDecodeBounds = true;
        Bitmap bitmapt = BitmapFactory.decodeByteArray(data, 0, data.length, option);
        int outHeight = option.outHeight;
        int outWidth = option.outWidth;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int windowW = wm.getDefaultDisplay().getWidth();
        int windowH = wm.getDefaultDisplay().getHeight();
        float scaleW = (float) outWidth / (float) windowW;
        float scaleH = (float) outHeight / (float) windowH;
        float maxScale = Math.max(scaleW, scaleH);
        LeoLog.i("test1", "window W = " + windowW);
        LeoLog.i("test1", "window H = " + windowH + "scaleW = " + scaleW + "...scaleH = " + scaleH + " minScale = " + maxScale);
        option.inJustDecodeBounds = false;
        if (maxScale > 1) {
            option.inSampleSize = (int) maxScale;
            LeoLog.i("test1", "final inSampleSize = " + option.inSampleSize);
        }
        option.inPreferredConfig = Config.RGB_565;
        option.inMutable = true;
        bitmapt = BitmapFactory.decodeByteArray(data, 0, data.length, option);
        LeoLog.i("test1", "final decode !! ");
        return bitmapt;
    }



}
