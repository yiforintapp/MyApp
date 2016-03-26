package com.leo.appmaster.airsig.airsigutils;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

public class Utils {

	public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }
    
    public static int getActionBarHeight(Context context) {
    	TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
		}
		
		return 0;
    }
    
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    public static void animationChangeSize(final View target, final int targetWidth, final int targetHeight, final int duration) {
    	target.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    	final int originWidth = target.getMeasuredWidth();
    	final int originHeight = target.getMeasuredHeight();
    	final int diffWidth = (targetWidth > 0) ? (targetWidth - originHeight) : 0;
    	final int diffHeight = (targetHeight > 0) ? (targetHeight - originHeight) : 0;
    	final Interpolator interpolator = new FastOutSlowInInterpolator();
    	Animation am = new Animation() {
    		@Override
   		 	protected void applyTransformation(float interpolatedTime, Transformation t) {
    			if (diffWidth != 0) {
    				target.getLayoutParams().width = (int) (originWidth + diffWidth * interpolator.getInterpolation(interpolatedTime));
    			}
    			if (diffHeight != 0) {
    				target.getLayoutParams().height = (int) (originHeight + diffHeight * interpolator.getInterpolation(interpolatedTime));
    			}
    			target.requestLayout();
    		}
    		@Override
    		public boolean willChangeBounds() {
    			return true;
    		}
    	};
    	am.setDuration(duration);
    	target.startAnimation(am);
    }
}
