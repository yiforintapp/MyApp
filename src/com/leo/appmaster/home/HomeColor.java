
package com.leo.appmaster.home;

import android.content.res.Resources;
import android.graphics.Color;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

public class HomeColor {

    public static class ColorHolder {
        public int red;
        public int green;
        public int blue;

        public ColorHolder() {
        }

        public ColorHolder(int red, int green, int blue) {
            super();
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int toIntColor() {
            return Color.rgb(red, green, blue);
        }

        public int toIntColorWithAlpha(int alpha) {
            return Color.argb(alpha, red, green, blue);
        }
    }

    public static int sLevel1ColorUp;
    public static int sLevel2ColorUp;
    public static int sLevel3ColorUp;
    public static int sLevel4ColorUp;
    public static int sLevel5ColorUp;
    public static int sLevel6ColorUp;
    public static int sLevel1ColorDown;
    public static int sLevel2ColorDown;
    public static int sLevel3ColorDown;
    public static int sLevel4ColorDown;
    public static int sLevel5ColorDown;
    public static int sLevel6ColorDown;

    //wifi page
    public static int WIFI_NORMAL;
    public static int UNSAFE_PAGE_START;
    public static int UNSAFE_PAGE_END;
    public static int SAFE_PAGE_START;
    public static int SAFE_PAGE_END;

    static {
        Resources res = AppMasterApplication.getInstance().getResources();
        sLevel1ColorUp = res.getColor(R.color.home_bg_level1_up);
        sLevel1ColorDown = res.getColor(R.color.home_bg_level1_down);

        sLevel2ColorUp = res.getColor(R.color.home_bg_level2_up);
        sLevel2ColorDown = res.getColor(R.color.home_bg_level2_dwon);
        sLevel3ColorUp = res.getColor(R.color.home_bg_level3_up);
        sLevel3ColorDown = res.getColor(R.color.home_bg_level3_down);
        sLevel4ColorUp = res.getColor(R.color.home_bg_level4_up);
        sLevel4ColorDown = res.getColor(R.color.home_bg_level4_down);
        sLevel5ColorUp = res.getColor(R.color.home_bg_level5_up);
        sLevel5ColorDown = res.getColor(R.color.home_bg_level5_down);

        sLevel6ColorUp = res.getColor(R.color.home_bg_level6_up);
        sLevel6ColorDown = res.getColor(R.color.home_bg_level6_down);
        //wifi pagehuangqili

        WIFI_NORMAL = res.getColor(R.color.wifi_bg_color);
        UNSAFE_PAGE_START = res.getColor(R.color.wifi_result_unsafe_start);
        UNSAFE_PAGE_END = res.getColor(R.color.wifi_result_unsafe_end);
        SAFE_PAGE_START = res.getColor(R.color.wifi_result_safe_start);
        SAFE_PAGE_END = res.getColor(R.color.wifi_result_safe_end);

    }

}
