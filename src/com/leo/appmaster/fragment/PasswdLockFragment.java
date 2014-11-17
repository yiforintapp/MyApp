package com.leo.appmaster.fragment;

import java.io.BufferedInputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.InputStream;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.theme.LeoResources;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.utils.AppUtil;

public class PasswdLockFragment extends LockFragment implements OnClickListener, OnTouchListener {

    private ImageView mAppIcon;
    private ImageView mAppIconTop;
    private ImageView mAppIconBottom;

    private ImageView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv0;
    private ImageView tv1Bottom, tv2Bottom, tv3Bottom, tv4Bottom, tv5Bottom, tv6Bottom, tv7Bottom, tv8Bottom, tv9Bottom, tv0Bottom;
	private ImageView iv_delete;
	private ImageView mTvPasswd1, mTvPasswd2, mTvPasswd3, mTvPasswd4;
	private TextView mPasswdTip, mPasswdHint;
	private String mTempPasswd = "";
	
	private String[] mPasswds = {"","","",""};
    private RelativeLayout mIconLayout;
    private Animation mShake;
	
    private static final int BUTTON_STATE_NORMAL = 0;
    private static final int BUTTON_STATE_PRESS = 1;    

    private int mButtonState = BUTTON_STATE_NORMAL;
    
    private String mThemepkgName = AppMasterApplication.getSelectedTheme();;
	/*---------------for theme----------------*/
    private Resources mThemeRes;
    private int mDigitalPressAnimRes = 0;
    private int mLayoutBgRes = 0;
    private int mDigitalBgNormalRes = 0;
    private int mDigitalBgActiveRes = 0;
    private int mBottomIconRes = 0;
    private int mTopIconRes = 0;
    
    private Drawable mPasswdNormalDrawable;
    private Drawable mPasswdActiveDrawable;
    /*-------------------end-------------------*/
    
	@Override
	protected int layoutResourceId() {
		return R.layout.fragment_lock_passwd;
	}

	@Override
	protected void onInitUI() {
	    
		tv1 = (ImageView) findViewById(R.id.tv_1_top);
		tv2 = (ImageView) findViewById(R.id.tv_2_top);
		tv3 = (ImageView) findViewById(R.id.tv_3_top);
		tv4 = (ImageView) findViewById(R.id.tv_4_top);
		tv5 = (ImageView) findViewById(R.id.tv_5_top);
		tv6 = (ImageView) findViewById(R.id.tv_6_top);
		tv7 = (ImageView) findViewById(R.id.tv_7_top);
		tv8 = (ImageView) findViewById(R.id.tv_8_top);
		tv9 = (ImageView) findViewById(R.id.tv_9_top);
		tv0 = (ImageView) findViewById(R.id.tv_0_top);
		iv_delete = (ImageView) findViewById(R.id.tv_delete);

		tv1.setOnClickListener(this);
		tv2.setOnClickListener(this);
		tv3.setOnClickListener(this);
		tv4.setOnClickListener(this);
		tv5.setOnClickListener(this);
		tv6.setOnClickListener(this);
		tv7.setOnClickListener(this);
		tv8.setOnClickListener(this);
		tv9.setOnClickListener(this);
		tv0.setOnClickListener(this);
		iv_delete.setOnClickListener(this);
		iv_delete.setEnabled(false);

        tv1Bottom = (ImageView) findViewById(R.id.tv_1_bottom);
        tv2Bottom = (ImageView) findViewById(R.id.tv_2_bottom);
        tv3Bottom = (ImageView) findViewById(R.id.tv_3_bottom);
        tv4Bottom = (ImageView) findViewById(R.id.tv_4_bottom);
        tv5Bottom = (ImageView) findViewById(R.id.tv_5_bottom);
        tv6Bottom = (ImageView) findViewById(R.id.tv_6_bottom);
        tv7Bottom = (ImageView) findViewById(R.id.tv_7_bottom);
        tv8Bottom = (ImageView) findViewById(R.id.tv_8_bottom);
        tv9Bottom = (ImageView) findViewById(R.id.tv_9_bottom);
        tv0Bottom = (ImageView) findViewById(R.id.tv_0_bottom);
		
        tv1.setOnTouchListener(this);
        tv2.setOnTouchListener(this);
        tv3.setOnTouchListener(this);
        tv4.setOnTouchListener(this);
        tv5.setOnTouchListener(this);
        tv6.setOnTouchListener(this);
        tv7.setOnTouchListener(this);
        tv8.setOnTouchListener(this);
        tv9.setOnTouchListener(this);
        tv0.setOnTouchListener(this);
        iv_delete.setOnTouchListener(this);
		
		mTvPasswd1 = (ImageView) findViewById(R.id.tv_passwd_1);
		mTvPasswd2 = (ImageView) findViewById(R.id.tv_passwd_2);
		mTvPasswd3 = (ImageView) findViewById(R.id.tv_passwd_3);
		mTvPasswd4 = (ImageView) findViewById(R.id.tv_passwd_4);
		mPasswdNormalDrawable = getResources().getDrawable(R.drawable.password_displayed_normal);
		mPasswdActiveDrawable = getResources().getDrawable(R.drawable.password_displayed_active);
		mPasswdHint = (TextView) findViewById(R.id.tv_passwd_hint);
		mPasswdTip = (TextView) findViewById(R.id.tv_passwd_input_tip);
		mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
	    mIconLayout = (RelativeLayout)findViewById(R.id.iv_app_icon_layout);
		//for multi theme
		initAnimResource();
		
		initPasswdIcon();
		
		String passwdtip = AppMasterPreference.getInstance(mActivity)
				.getPasswdTip();
		if (passwdtip == null || passwdtip.trim().equals("")) {
			mPasswdHint.setVisibility(View.GONE);
		} else {
			mPasswdHint.setText(mActivity.getString(R.string.passwd_hint_tip)
					+ passwdtip);
		}

		if (mPackageName != null) {
		    mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
		    mAppIcon.setImageDrawable(AppUtil.getDrawable(
					mActivity.getPackageManager(), mPackageName));
		    mAppIcon.setVisibility(View.VISIBLE);
			
	         if (needChangeTheme()) {
                 mAppIconTop = (ImageView) findViewById(R.id.iv_app_icon_top);
                 mAppIconBottom = (ImageView) findViewById(R.id.iv_app_icon_bottom);
                 mAppIconTop.setVisibility(View.VISIBLE);
                 mAppIconBottom.setVisibility(View.VISIBLE);
                 if (mThemeRes != null) {
                     if (mTopIconRes > 0) {
                         mAppIconTop.setBackgroundDrawable(mThemeRes.getDrawable(mTopIconRes));
                     }
                     if (mBottomIconRes > 0) {
                         mAppIconBottom.setBackgroundDrawable(mThemeRes.getDrawable(mBottomIconRes));
                     }
                 }
            }
		}

		clearPasswd();

	}

	private void initPasswdIcon() {
        mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawable);
        mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawable);
        mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawable);
        mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawable);
	}
	
	private void initAnimResource() {
	    if (!needChangeTheme()) return;
	   Context themeContext = getThemepkgConotext(mThemepkgName);//com.leo.appmaster:drawable/multi_theme_lock_bg
	   mThemeRes = themeContext.getResources();
	   
	   mLayoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_bg");
       if (mLayoutBgRes <= 0) {
           mLayoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "general_bg");  
       }
       mDigitalBgNormalRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_bg_normal");
       mDigitalBgActiveRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_bg_active");
       mDigitalPressAnimRes = ThemeUtils.getValueByResourceName(themeContext, "anim", "digital_press_anim");
       mTopIconRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "top_icon");
       mBottomIconRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "bottom_icon");
       
       
       if (mThemeRes != null) {
           if (mLayoutBgRes > 0) {
               RelativeLayout layout =  (RelativeLayout) getActivity().findViewById(R.id.activity_lock_layout);
               Drawable bgDrawable = mThemeRes.getDrawable(mLayoutBgRes);
               layout.setBackgroundDrawable(bgDrawable);
           }
           
           if (mDigitalBgNormalRes > 0) {
               Drawable normalDrawable = mThemeRes.getDrawable(mDigitalBgNormalRes);
             tv1Bottom.setBackgroundDrawable(normalDrawable);
             tv2Bottom.setBackgroundDrawable(normalDrawable);
             tv3Bottom.setBackgroundDrawable(normalDrawable);
             tv4Bottom.setBackgroundDrawable(normalDrawable);
             tv5Bottom.setBackgroundDrawable(normalDrawable);
             tv6Bottom.setBackgroundDrawable(normalDrawable);
             tv7Bottom.setBackgroundDrawable(normalDrawable);
             tv8Bottom.setBackgroundDrawable(normalDrawable);
             tv9Bottom.setBackgroundDrawable(normalDrawable);
             tv0Bottom.setBackgroundDrawable(normalDrawable);
           }
           
           int digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_0_normal");
           Drawable digitalDrawable = mThemeRes.getDrawable(digital);
           tv0.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_1_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv1.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_2_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv2.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_3_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv3.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_4_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv4.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_5_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv5.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_6_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv6.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_7_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv7.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_8_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv8.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_9_normal");
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv9.setImageDrawable(digitalDrawable);
           
           //get password resource
           int passwordNormalRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "password_displayed_normal");
           if (passwordNormalRes > 0) {
               mPasswdNormalDrawable = mThemeRes.getDrawable(passwordNormalRes);
           }
           int passwordActiveRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "password_displayed_active");
           if (passwordActiveRes > 0) {
               mPasswdActiveDrawable = mThemeRes.getDrawable(passwordActiveRes);
           }
       }

	}
	
	
	
	
    @Override
    public void onPause() {
        if (needChangeTheme()) {
//            for (int i = 0; i < mGifDrawables.length; i++) {
//                mGifDrawables[i].stop();
//            }
            if (mDigitalBgNormalRes > 0) {
                Drawable normalDrawable = mThemeRes.getDrawable(mDigitalBgNormalRes);
              tv1Bottom.setBackgroundDrawable(normalDrawable);
              tv2Bottom.setBackgroundDrawable(normalDrawable);
              tv3Bottom.setBackgroundDrawable(normalDrawable);
              tv4Bottom.setBackgroundDrawable(normalDrawable);
              tv5Bottom.setBackgroundDrawable(normalDrawable);
              tv6Bottom.setBackgroundDrawable(normalDrawable);
              tv7Bottom.setBackgroundDrawable(normalDrawable);
              tv8Bottom.setBackgroundDrawable(normalDrawable);
              tv9Bottom.setBackgroundDrawable(normalDrawable);
              tv0Bottom.setBackgroundDrawable(normalDrawable);
            }
        } else {
            tv1Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv2Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv3Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv4Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv5Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv6Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv7Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv8Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv9Bottom.setImageResource(R.drawable.digital_bg_normal);
            tv0Bottom.setImageResource(R.drawable.digital_bg_normal);
        }


        super.onPause();
    }

    @Override
    public void onDestroyView() {
//        for (int i = 0; i < mGifDrawables.length; i++) {
//            mGifDrawables[i].stop();
//            mGifDrawables[i].recycle();
//        }
        super.onDestroyView();
    }

    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_1_top:
			inputPasswd(1 + "");
			break;
		case R.id.tv_2_top:
			inputPasswd(2 + "");
			break;
		case R.id.tv_3_top:
			inputPasswd(3 + "");
			break;
		case R.id.tv_4_top:
			inputPasswd(4 + "");
			break;
		case R.id.tv_5_top:
			inputPasswd(5 + "");
			break;
		case R.id.tv_6_top:
			inputPasswd(6 + "");
			break;
		case R.id.tv_7_top:
			inputPasswd(7 + "");
			break;
		case R.id.tv_8_top:
			inputPasswd(8 + "");
			break;
		case R.id.tv_9_top:
			inputPasswd(9 + "");
			break;
		case R.id.tv_0_top:
			inputPasswd(0 + "");
			break;
		case R.id.tv_delete:
			deletePasswd();
			break;
		case R.id.tv_ok:
			checkPasswd();
			break;
		default:
			break;
		}
	}

	private void checkPasswd() {
		mInputCount++;
		AppMasterPreference pref = AppMasterPreference.getInstance(mActivity);

		if (pref.getPassword().equals(mTempPasswd)) {
			((LockScreenActivity)mActivity).onUnlockSucceed();
		} else {
			if (mInputCount >= mMaxInput) {
				mInputCount = 0;
				mPasswdTip.setText(R.string.passwd_hint);
				((LockScreenActivity) mActivity).onUolockOutcount();
			} else {
				mPasswdTip.setText(String.format(
						mActivity.getString(R.string.input_error_tip),
						mInputCount + "", (mMaxInput - mInputCount) + ""));
			}
			clearPasswd();
			shakeIcon();
		}
	}

	private void clearPasswd() {
		mTvPasswd1.postDelayed(new Runnable() {
			@Override
			public void run() {
			    mTempPasswd = "";
				mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawable);
				mPasswds[0] = "";
				mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawable);
                mPasswds[1] = "";
				mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawable);
                mPasswds[2] = "";
				mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawable);
                mPasswds[3] = "";
				
			}
		}, 300);
	}

	private void deletePasswd() {
		if (!mPasswds[3].equals("")) {
		    mPasswds[3] = "";
			mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawable);
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mPasswds[2].equals("")) {
		    mPasswds[2] = "";
			mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawable);
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mPasswds[1].equals("")) {
		    mPasswds[1] = "";
			mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawable);
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mPasswds[0].equals("")) {
            mPasswds[0] = "";
            mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawable);
			iv_delete.setEnabled(false);
			mTempPasswd = "";
		}

	}

	private void inputPasswd(String s) {
		if (mPasswds[0].equals("")) {
		    mPasswds[0] = "*";
			mTvPasswd1.setBackgroundDrawable(mPasswdActiveDrawable);
			iv_delete.setEnabled(true);
			mTempPasswd = s;
		} else if (mPasswds[1].equals("")) {
	          mPasswds[1] = "*";
			mTvPasswd2.setBackgroundDrawable(mPasswdActiveDrawable);
			mTempPasswd = mTempPasswd + s;
		} else if (mPasswds[2].equals("")) {
	          mPasswds[2] = "*";
			mTvPasswd3.setBackgroundDrawable(mPasswdActiveDrawable);
			mTempPasswd = mTempPasswd + s;
		} else if (mPasswds[3].equals("")) {
	        mPasswds[3] = "*";
			mTvPasswd4.setBackgroundDrawable(mPasswdActiveDrawable);
			mTempPasswd = mTempPasswd + s;

			checkPasswd();
		}

	}

	@Override
	public void onNewIntent(Intent intent) {

	}

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mButtonState = BUTTON_STATE_PRESS;
            if (needChangeTheme()) {
                changeDigitalResourceByThem(view,mButtonState);
            } else {
                changeDigitalResource(view,mButtonState);
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            mButtonState = BUTTON_STATE_NORMAL;
            if (needChangeTheme()) {
                changeDigitalResourceByThem(view,mButtonState);
            } else {
                changeDigitalResource(view,mButtonState);
            }
            break;
        }

        return false;
    }
    
    private void changeDigitalResourceByThem(final View view, int state) {
        AnimationDrawable animDrawable = null;
        Drawable activeDrawable = null;
        ImageView bottomView;
        
        if (mThemeRes != null) {
            if (mDigitalPressAnimRes > 0) {
                animDrawable = (AnimationDrawable)mThemeRes.getDrawable(mDigitalPressAnimRes);
            } else {
                activeDrawable = mThemeRes.getDrawable(mDigitalBgActiveRes);
            }
        }

        switch (view.getId()) {
        case R.id.tv_1_top:
            bottomView = tv1Bottom;
            break;
        case R.id.tv_2_top:
            bottomView = tv2Bottom;
            break;
        case R.id.tv_3_top:
            bottomView = tv3Bottom;
            break;
        case R.id.tv_4_top:
            bottomView = tv4Bottom;
            break;
        case R.id.tv_5_top:
            bottomView = tv5Bottom;
            break;
        case R.id.tv_6_top:
            bottomView = tv6Bottom;
            break;
        case R.id.tv_7_top:
            bottomView = tv7Bottom;
            break;
        case R.id.tv_8_top:
            bottomView = tv8Bottom;
            break;
        case R.id.tv_9_top:
            bottomView = tv9Bottom;
            break;
        case R.id.tv_0_top:
            bottomView = tv0Bottom;
            break;
        default:
            bottomView = tv0Bottom;
            return;
        }
        if (state == BUTTON_STATE_PRESS) {
            Drawable drawble = bottomView.getDrawable();
            if (drawble instanceof AnimationDrawable) {
                if (((AnimationDrawable)drawble).isRunning()) {
                    ((AnimationDrawable)drawble).stop();
                    ((AnimationDrawable)drawble).start();
                } else {
                    ((AnimationDrawable)drawble).start();
                }
            } else {
                if (animDrawable != null) {
                    bottomView.setImageDrawable((animDrawable));
                    animDrawable.start();
                } else {
                    bottomView.setImageDrawable((activeDrawable));
                }
            }
        } else if (state == BUTTON_STATE_NORMAL) {
            if (mDigitalBgNormalRes > 0) {
                bottomView.setImageDrawable(mThemeRes.getDrawable(mDigitalBgNormalRes));
            }            
//            view.setBackgroundResource(R.drawable.digital_bg_normal);
        }
    }
    
    private void changeDigitalResource(final View view, int state) {
        ImageView bottomView;
        switch (view.getId()) {
        case R.id.tv_1_top:
            bottomView = tv1Bottom;
            break;
        case R.id.tv_2_top:
            bottomView = tv2Bottom;
            break;
        case R.id.tv_3_top:
            bottomView = tv3Bottom;
            break;
        case R.id.tv_4_top:
            bottomView = tv4Bottom;
            break;
        case R.id.tv_5_top:
            bottomView = tv5Bottom;
            break;
        case R.id.tv_6_top:
            bottomView = tv6Bottom;
            break;
        case R.id.tv_7_top:
            bottomView = tv7Bottom;
            break;
        case R.id.tv_8_top:
            bottomView = tv8Bottom;
            break;
        case R.id.tv_9_top:
            bottomView = tv9Bottom;
            break;
        case R.id.tv_0_top:
            bottomView = tv0Bottom;
            break;
        default:
            bottomView = tv0Bottom;
            return;
        }
        if (state == BUTTON_STATE_PRESS) {
           bottomView.setBackgroundResource(R.drawable.digital_bg_active);
        } else if (state == BUTTON_STATE_NORMAL) {
           bottomView.setBackgroundResource(R.drawable.digital_bg_normal);
        }
    }
    
    private Context getThemepkgConotext(String pkgName) {
        Context context= AppMasterApplication.getInstance();
        
        Context themeContext = LeoResources.getThemeContext(context,  pkgName);
        return themeContext;
    }

    private boolean needChangeTheme() {
        return  ThemeUtils.checkThemeNeed(getActivity()) && (mFrom == LockFragment.FROM_OTHER || mFrom == LockFragment.FROM_SCREEN_ON);
    }
    
    private void shakeIcon() {
        if (mShake == null) {
            mShake = AnimationUtils.loadAnimation(mActivity,
                    R.anim.left_right_shake);
        }
        mIconLayout.startAnimation(mShake);
    }
    
}
