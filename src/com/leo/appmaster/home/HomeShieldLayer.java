package com.leo.appmaster.home;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.AnimLayer;

/**
 * Created by RunLee on 2016/3/31.
 */
public class HomeShieldLayer extends AnimLayer {
    private BitmapDrawable mCenterShield;
    private BitmapDrawable mLeftShield;
    private BitmapDrawable mRightShield;

    public HomeShieldLayer(View view) {
        super(view);
        Resources res = mParent.getResources();
        mCenterShield = (BitmapDrawable) res.getDrawable(R.drawable.shield_blue_top);
        mLeftShield = (BitmapDrawable) res.getDrawable(R.drawable.shield_blue_left);
        mRightShield = (BitmapDrawable) res.getDrawable(R.drawable.shield_blue_right);
    }

    @Override
    protected void draw(Canvas canvas) {

        //中间盾牌
        //左边盾牌
        //右边盾牌
    }
}
