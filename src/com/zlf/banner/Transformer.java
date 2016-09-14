package com.zlf.banner;

import android.support.v4.view.ViewPager.PageTransformer;

import com.zlf.banner.transformer.AccordionTransformer;
import com.zlf.banner.transformer.BackgroundToForegroundTransformer;
import com.zlf.banner.transformer.CubeInTransformer;
import com.zlf.banner.transformer.CubeOutTransformer;
import com.zlf.banner.transformer.DefaultTransformer;
import com.zlf.banner.transformer.DepthPageTransformer;
import com.zlf.banner.transformer.FlipHorizontalTransformer;
import com.zlf.banner.transformer.FlipVerticalTransformer;
import com.zlf.banner.transformer.ForegroundToBackgroundTransformer;
import com.zlf.banner.transformer.RotateDownTransformer;
import com.zlf.banner.transformer.RotateUpTransformer;
import com.zlf.banner.transformer.ScaleInOutTransformer;
import com.zlf.banner.transformer.StackTransformer;
import com.zlf.banner.transformer.TabletTransformer;
import com.zlf.banner.transformer.ZoomInTransformer;
import com.zlf.banner.transformer.ZoomOutSlideTransformer;
import com.zlf.banner.transformer.ZoomOutTranformer;

public class Transformer {
    public static Class<? extends PageTransformer> Default = DefaultTransformer.class;
    public static Class<? extends PageTransformer> Accordion = AccordionTransformer.class;
    public static Class<? extends PageTransformer> BackgroundToForeground = BackgroundToForegroundTransformer.class;
    public static Class<? extends PageTransformer> ForegroundToBackground = ForegroundToBackgroundTransformer.class;
    public static Class<? extends PageTransformer> CubeIn = CubeInTransformer.class;
    public static Class<? extends PageTransformer> CubeOut = CubeOutTransformer.class;
    public static Class<? extends PageTransformer> DepthPage = DepthPageTransformer.class;
    public static Class<? extends PageTransformer> FlipHorizontal = FlipHorizontalTransformer.class;
    public static Class<? extends PageTransformer> FlipVertical = FlipVerticalTransformer.class;
    public static Class<? extends PageTransformer> RotateDown = RotateDownTransformer.class;
    public static Class<? extends PageTransformer> RotateUp = RotateUpTransformer.class;
    public static Class<? extends PageTransformer> ScaleInOut = ScaleInOutTransformer.class;
    public static Class<? extends PageTransformer> Stack = StackTransformer.class;
    public static Class<? extends PageTransformer> Tablet = TabletTransformer.class;
    public static Class<? extends PageTransformer> ZoomIn = ZoomInTransformer.class;
    public static Class<? extends PageTransformer> ZoomOut = ZoomOutTranformer.class;
    public static Class<? extends PageTransformer> ZoomOutSlide = ZoomOutSlideTransformer.class;
}
