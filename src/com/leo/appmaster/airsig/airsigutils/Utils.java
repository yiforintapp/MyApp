package com.leo.appmaster.airsig.airsigutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

public class Utils {
	
	private static SharedPreferences mPref;
	private static final String AIRSIG_SHARED_PREFERENCE_SAVED_EVENTS = "AIRSIG_SHARED_PREFERENCE_SAVED_EVENTS";

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
    
    public static void blinkView(final View view) {
    	int duration = 300;
    	
    	new Handler().postDelayed(new Runnable() {
    	    public void run() {
    	    	view.setAlpha(0f);
    	    }
    	}, duration);
    	new Handler().postDelayed(new Runnable() {
    	    public void run() {
    	    	view.setAlpha(1f);
    	    }
    	}, duration * 2);
    	new Handler().postDelayed(new Runnable() {
    	    public void run() {
    	    	view.setAlpha(0f);
    	    }
    	}, duration * 3);
    	new Handler().postDelayed(new Runnable() {
    	    public void run() {
    	    	view.setAlpha(1f);
    	    }
    	}, duration * 4);
    	new Handler().postDelayed(new Runnable() {
    	    public void run() {
    	    	view.setAlpha(0f);
    	    }
    	}, duration * 5);
    	new Handler().postDelayed(new Runnable() {
    	    public void run() {
    	    	view.setAlpha(1f);
    	    }
    	}, duration * 6);
    }
    
    public static void saveBoolean(Context context, String key, boolean value) {
    	if (mPref == null) {
    		mPref = context.getSharedPreferences(AIRSIG_SHARED_PREFERENCE_SAVED_EVENTS, Context.MODE_PRIVATE);
    	}
    	SharedPreferences.Editor editor = mPref.edit();
		editor.putBoolean(key, value);
		editor.commit();
    }
    
    public static boolean getSavedBoolean(Context context, String key, boolean defaultValue) {
    	if (mPref == null) {
    		mPref = context.getSharedPreferences(AIRSIG_SHARED_PREFERENCE_SAVED_EVENTS, Context.MODE_PRIVATE);
    	}
    	
    	return mPref.getBoolean(key, defaultValue);
    }
}
