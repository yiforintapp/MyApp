package com.leo.appmaster.intruderprotection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.AppUtil;

public class WaterMarkUtils {

    /**
     * 将源bitmap打上透明的水印，包含时间 入侵的应用的图标 pg的logo
     * @param photoBitmap
     * @param timeStamp
     * @param packageName
     * @param context
     * @return
     */
    public static Bitmap createIntruderPhoto(Bitmap photoBitmap, String timeStamp,
            String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        Drawable appIcon = null;
        Bitmap iconBitmap = null;
        Canvas canvas = null;
        Drawable pgIcon = null;
        Bitmap pgiconBitmap = null;
        //将timestamp的字符串转成所需要的格式
        SimpleDateFormat sdf = new SimpleDateFormat(
                Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT);
        Calendar ci = Calendar.getInstance();
        Date date = null;
        int hour = 0;
        int minute = 0;
        int ampm = 0;
        try {
            date = sdf.parse(timeStamp);
            ci.setTime(date);
            hour = ci.get(Calendar.HOUR);
            ampm= ci.get(Calendar.AM_PM);
            minute = ci.get(Calendar.MINUTE);
        } catch (ParseException e1) {
            return photoBitmap;
        }
        String strHour = "";
        String strMinute = "";
        if(hour<10){
            strHour = "0"+hour;
        }else{
            strHour = hour+"";
        }
        if(minute<10){
            strMinute = "0"+minute;
        }else{
            strMinute = minute+"";
        }
        String fts;
        if(ampm==Calendar.AM){
            fts = strHour+":"+strMinute+"AM";
        }else if(ampm==Calendar.PM){
            fts = strHour+":"+strMinute+"PM";
        }else{
            fts = strHour+":"+strMinute;
        }
        //获得并画出入侵的应用的水印
        float fitRate = photoBitmap.getWidth() / 240f;
        try {
            List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
            appIcon = pm.getApplicationIcon(packageName);
        } catch (Exception e) {
            return photoBitmap;
        }
        if (appIcon != null) {
            iconBitmap = Bitmap.createBitmap((int) (25f * fitRate), (int) (25f * fitRate),
                    Config.RGB_565);
            canvas = new Canvas(iconBitmap);
            try{
                appIcon.setBounds(0, (int) (2f * fitRate), (int) (25f * fitRate), (int) (25f * fitRate));
            }
            catch(Exception e){
            }
            appIcon.draw(canvas);
        }
        Canvas canvas2 = new Canvas(photoBitmap);
        Paint p1 = new Paint();
        p1.setColor(0x000000);
        p1.setAlpha(50);
        canvas2.drawRect(0, 0, photoBitmap.getWidth(), 29f * fitRate, p1);
        Paint p = new Paint();
        p.setAlpha(150);
        p.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
        canvas2.drawBitmap(iconBitmap, 0, 0, p);
        canvas2.save(Canvas.ALL_SAVE_FLAG);
        canvas2.restore();
        //画出时间和pg的名称
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        textPaint.setTextSize((int) (14f * fitRate));

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setColor(Color.WHITE);
        textPaint.setAlpha(120);
        canvas2.drawText(fts, (int) (35f * fitRate), (int) (18f * fitRate), textPaint);
        canvas2.drawText("LEO Privacy Guard", photoBitmap.getWidth() - (int) (124f * fitRate),
                photoBitmap.getHeight() - (int) (6f * fitRate), textPaint);
        //画出pg的logo
        pgIcon = context.getResources().getDrawable(R.drawable.watermarker_logo);
        if (pgIcon != null) {
            Canvas c3;
            Paint pgp = new Paint();
            pgp.setColor(Color.WHITE);
            pgp.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
            pgp.setAlpha(120);
            pgiconBitmap = Bitmap.createBitmap((int) (14f * fitRate), (int) (14f * fitRate),
                    Config.RGB_565);
            c3 = new Canvas(pgiconBitmap);
            pgIcon.setBounds(0, 0, (int) (14f * fitRate), (int) (14f * fitRate));
            pgIcon.draw(c3);
            canvas2.drawBitmap(pgiconBitmap, photoBitmap.getWidth() - (140f * fitRate),
                    photoBitmap.getHeight() - (18f * fitRate), pgp);
            // canvas = new Canvas(iconBitmap);
            // appIcon.setBounds(0, 0, (int)(30f*fitRate), (int)(30f*fitRate));
            // appIcon.draw(canvas);
            pgiconBitmap.recycle();
            iconBitmap.recycle();
        }
        return photoBitmap;
    }
}
