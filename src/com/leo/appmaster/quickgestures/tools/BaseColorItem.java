package com.leo.appmaster.quickgestures.tools;

import com.leo.appmaster.AppMasterApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BaseColorItem {
//	private Bitmap mBmp;
	private int mAvgColorR;
	private int mAvgColorG;
	private int mAvgColorB;
	
	private int mResId;

	private int mAvgColorLab[];

	public BaseColorItem(int resId) {
	    mResId = resId;
		genAverageColor();
	}
	
	public Drawable getDrawable() {
	    if (mResId > 0) {
	        Context ctx = AppMasterApplication.getInstance();
	        return ctx.getResources().getDrawable(mResId);
	    }
	    
	    return null;
	}
	
	public int getDrawableId() {
	    return mResId;
	}

	private void genAverageColor() {
	    BitmapDrawable drawable = (BitmapDrawable) getDrawable();
		int rgb[] = bmp2rgb(drawable.getBitmap());
		this.mAvgColorR = rgb[0];
		this.mAvgColorG = rgb[1];
		this.mAvgColorB = rgb[2];
		this.mAvgColorLab = rgb2lab(mAvgColorR, mAvgColorG, mAvgColorB);

		// Log.d("ColorBaseItem RGB", Integer.toString(mAvgColorR)+" "+
		// Integer.toString(mAvgColorG)+" "+ Integer.toString(mAvgColorB));
		// Log.d("ColorBaseItem Lab", Integer.toString(mAvgColorLab[0])+" "+
		// Integer.toString(mAvgColorLab[1])+" "+
		// Integer.toString(mAvgColorLab[2]));

	}

	public int[] bmp2rgb(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		long numOfPixel = 0;
		long sumR = 0;
		long sumG = 0;
		long sumB = 0;
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				int color = bmp.getPixel(w, h);
				int alpha = Color.alpha(color);
				if (alpha == 255) {
					sumR += Color.red(color);
					sumG += Color.green(color);
					sumB += Color.blue(color);
					numOfPixel++;
				}
			}
		}

		int[] ret = new int[3];
		if (numOfPixel != 0) {
			ret[0] = (int) (sumR / numOfPixel);
			ret[1] = (int) (sumG / numOfPixel);
			ret[2] = (int) (sumB / numOfPixel);
		}
		return ret;

	}

	public int[] rgb2lab(int R, int G, int B) {
		// http://www.brucelindbloom.com

		float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
		float Ls, as, bs;
		float eps = 216.f / 24389.f;
		float k = 24389.f / 27.f;

		float Xr = 0.964221f; // reference white D50
		float Yr = 1.0f;
		float Zr = 0.825211f;

		// RGB to XYZ
		r = R / 255.f; // R 0..1
		g = G / 255.f; // G 0..1
		b = B / 255.f; // B 0..1

		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r / 12;
		else
			r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

		if (g <= 0.04045)
			g = g / 12;
		else
			g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

		if (b <= 0.04045)
			b = b / 12;
		else
			b = (float) Math.pow((b + 0.055) / 1.055, 2.4);

		X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
		Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
		Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

		// XYZ to Lab
		xr = X / Xr;
		yr = Y / Yr;
		zr = Z / Zr;

		if (xr > eps)
			fx = (float) Math.pow(xr, 1 / 3.);
		else
			fx = (float) ((k * xr + 16.) / 116.);

		if (yr > eps)
			fy = (float) Math.pow(yr, 1 / 3.);
		else
			fy = (float) ((k * yr + 16.) / 116.);

		if (zr > eps)
			fz = (float) Math.pow(zr, 1 / 3.);
		else
			fz = (float) ((k * zr + 16.) / 116);

		Ls = (116 * fy) - 16;
		as = 500 * (fx - fy);
		bs = 200 * (fy - fz);

		int[] lab = new int[3];

		lab[0] = (int) (2.55 * Ls + .5);
		lab[1] = (int) (as + .5);
		lab[2] = (int) (bs + .5);
		return lab;
	}

	public int getMatchScore(Bitmap target) {
		// UE method or RD method?
		return doMatchedMethodRD(target);
	}

	private int doMatchedMethodRD(Bitmap target) {
		final int[] targetLab = ColorMatcher.cachedTargetLab;
        if(targetLab[0] == -1 && targetLab[1] == -1 && targetLab[2] == -1) {
    		int targetRGB[] = bmp2rgb(target);
    		int[] lab = rgb2lab(targetRGB[0], targetRGB[1], targetRGB[2]);
    		targetLab[0] = lab[0];
    		targetLab[1] = lab[1];
    		targetLab[2] = lab[2];
        }
		int ret = (int) (Math.pow((mAvgColorLab[0] - targetLab[0]), 2.0)
				+ Math.pow((mAvgColorLab[1] - targetLab[1]), 2.0) + Math.pow(
				(mAvgColorLab[2] - targetLab[2]), 2.0));

		return ret;
	}

}
