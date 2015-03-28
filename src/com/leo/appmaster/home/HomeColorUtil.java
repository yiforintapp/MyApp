
package com.leo.appmaster.home;

import android.graphics.Color;

public class HomeColorUtil {
    // 红色
    public static int DEFAULT_COLOR_RED = 0xff2f2f;
    public static ColorHolder sColorRed = new ColorHolder(0xff, 0x2f, 0x2f);
    // 橙色
    public static int DEFAULT_COLOR_ORANGE = 0xfeb900;
    public static ColorHolder sColorOrange = new ColorHolder(0xfe, 0xb9, 0x00);
    // 金色
    public static int DEFAULT_COLOR_GOLDEN = 0xffd700;
    public static ColorHolder sColorGolden = new ColorHolder(0xff, 0xd7, 0x00);
    // 绿色
    public static int DEFAULT_COLOR_GREEN = 0x26a93b;
    public static ColorHolder sColorGreen = new ColorHolder(0x26, 0xa9, 0x3b);
    // 蓝色
    public static int DEFAULT_COLOR_BLUE = 0x4285F4;
    public static ColorHolder sColorBlue = new ColorHolder(0x42, 0x85, 0xf4);

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
    }

}
