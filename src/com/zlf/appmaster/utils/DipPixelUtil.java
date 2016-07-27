
package com.zlf.appmaster.utils;

import android.content.Context;

/**
 * <p>
 */
public class DipPixelUtil
{

    /**
     * convert dip to px
     * 
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        // AM-1800 java.lang.NullPointerException: Attempt to invoke virtual
        // method 'android.content.res.Resources
        // android.content.Context.getResources()' on a null object reference
        final float scale;
        try {
            scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * convert px to dip
     * 
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
