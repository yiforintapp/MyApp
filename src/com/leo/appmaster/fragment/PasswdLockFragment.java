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
import com.leo.appmaster.lockertheme.ResourceName;
import com.leo.appmaster.theme.LeoResources;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.utils.AppUtil;

public class PasswdLockFragment extends LockFragment implements OnClickListener, OnTouchListener {

    private ImageView mAppIcon;
    private ImageView mAppIconTop;
    private ImageView mAppIconBottom;

    private ImageView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv0;
    private ImageView tv1Bottom, tv2Bottom, tv3Bottom, tv4Bottom, tv5Bottom, tv6Bottom, tv7Bottom, tv8Bottom, tv9Bottom, tv0Bottom;
	private ImageView iv_delete,iv_delete_bottom;
	private ImageView mTvPasswd1, mTvPasswd2, mTvPasswd3, mTvPasswd4;
	private TextView mPasswdTip, mPasswdHint;
	private String mTempPasswd = "";
	
	private String[] mPasswds = {"","","",""};
    private RelativeLayout mIconLayout;
    private Animation mShake;
	
    private static final int BUTTON_STATE_NORMAL = 0;
    private static final int BUTTON_STATE_PRESS = 1;    

    private int mButtonState = BUTTON_STATE_NORMAL;
    
    private String mThemepkgName = AppMasterApplication.getSelectedTheme();
	/*---------------for theme----------------*/
    private Resources mThemeRes;
    private int mDigitalPressAnimRes = 0;
    private int mLayoutBgRes = 0;
    private int mDigitalBgNormalRes = 0;
    private int mDigitalBgActiveRes = 0;
    private int mBottomIconRes = 0;
    private int mTopIconRes = 0;
    private int mDeleteNormalRes = 0;
    
    private Drawable mPasswdNormalDrawable;
    private Drawable mPasswdActiveDrawable;
    
    private Drawable[] mPasswdNormalDrawables = new Drawable[4];
    private Drawable[] mPasswdActiveDrawables = new Drawable[4];
    
    private Drawable[] mDigitalBgNormalDrawables = new  Drawable[10];
    private Drawable[] mDigitalBgActiveDrawables = new  Drawable[10];
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
		iv_delete_bottom = (ImageView) findViewById(R.id.tv_delete_bottom);

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
//        mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawable);
//        mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawable);
//        mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawable);
//        mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawable);
        if(mPasswdNormalDrawables[0] != null) {
            mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawables[0]);
        } else {
              mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawable);
        }
        if(mPasswdNormalDrawables[1] != null) {
            mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawables[1]);
        } else {
              mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawable);
        }
        if(mPasswdNormalDrawables[2] != null) {
            mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawables[2]);
        } else {
              mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawable);
        }
        if(mPasswdNormalDrawables[3] != null) {
            mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawables[3]);
        } else {
              mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawable);
        }
	}
	
	private void initAnimResource() {
	    if (!needChangeTheme()) return;
	   Context themeContext = getThemepkgConotext(mThemepkgName);//com.leo.appmaster:drawable/multi_theme_lock_bg
	   mThemeRes = themeContext.getResources();
	   
	   mLayoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_BG);
       if (mLayoutBgRes <= 0) {
           mLayoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_GENERAL_BG);
       }
       mDigitalBgNormalRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",ResourceName.THEME_DIGITAL_BUTTON_NOMAL_BG);
       mDigitalBgActiveRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_BUTTON_ACTIVE_BG);
       mDigitalPressAnimRes = ThemeUtils.getValueByResourceName(themeContext, "anim", ResourceName.THEME_DIGITAL_PRESSANIM);
       mTopIconRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_TOP_ICON);
       mBottomIconRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_BOTTOM_ICON);
       mDeleteNormalRes =  ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DELETE);
       
       if (mThemeRes != null) {
           if (mDeleteNormalRes > 0) {
               iv_delete_bottom.setImageDrawable(mThemeRes.getDrawable(mDeleteNormalRes));
           }
           
           if (mLayoutBgRes > 0) {
               RelativeLayout layout =  (RelativeLayout) getActivity().findViewById(R.id.activity_lock_layout);
               Drawable bgDrawable = mThemeRes.getDrawable(mLayoutBgRes);
               layout.setBackgroundDrawable(bgDrawable);
           }
           
           if (mDigitalBgNormalRes > 0) {
               Drawable normalDrawable = mThemeRes.getDrawable(mDigitalBgNormalRes);
             tv1Bottom.setImageDrawable(normalDrawable);
             tv2Bottom.setImageDrawable(normalDrawable);
             tv3Bottom.setImageDrawable(normalDrawable);
             tv4Bottom.setImageDrawable(normalDrawable);
             tv5Bottom.setImageDrawable(normalDrawable);
             tv6Bottom.setImageDrawable(normalDrawable);
             tv7Bottom.setImageDrawable(normalDrawable);
             tv8Bottom.setImageDrawable(normalDrawable);
             tv9Bottom.setImageDrawable(normalDrawable);
             tv0Bottom.setImageDrawable(normalDrawable);
           } else if (ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_0_bg_normal") > 0){
               getButtonMulticRes(themeContext);
           }
           
           int digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_0);
           Drawable digitalDrawable = mThemeRes.getDrawable(digital);
           tv0.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_1);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv1.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_2);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv2.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_3);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv3.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_4);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv4.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_5);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv5.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_6);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv6.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_7);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv7.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_8);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv8.setImageDrawable(digitalDrawable);
           digital = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_DIGITAL_9);
           digitalDrawable = mThemeRes.getDrawable(digital);
           tv9.setImageDrawable(digitalDrawable);
           
           //get password resource
           int passwordNormalRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_PASSWD_NORMAL);
           if (passwordNormalRes > 0) {
               mPasswdNormalDrawable = mThemeRes.getDrawable(passwordNormalRes);
           } else {
               int res = 0;
               for (int i = 0; i < mPasswdNormalDrawables.length; i++) {
                    res = ThemeUtils.getValueByResourceName(themeContext, "drawable", "password_displayed_normal_"+(i+1));
                    if(res > 0) {
                        mPasswdNormalDrawables[i] = mThemeRes.getDrawable(res);
                    }
               }
           }
           int passwordActiveRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", ResourceName.THEME_PASSWD_ACTIVE);
           if (passwordActiveRes > 0) {
               mPasswdActiveDrawable = mThemeRes.getDrawable(passwordActiveRes);
           } else {
               int res = 0;
               for (int i = 0; i < mPasswdActiveDrawables.length; i++) {
                   res = ThemeUtils.getValueByResourceName(themeContext, "drawable", "password_displayed_active_"+(i+1));
                   if(res > 0) {
                       mPasswdActiveDrawables[i] = mThemeRes.getDrawable(res);
                   }
               }
           }
       }

	}
	
	private void getButtonMulticRes(Context themeContext){
	    int length = mDigitalBgNormalDrawables.length;
	    int buttonNormalRes;
	    for (int i = 0; i < length; i++) {
	        buttonNormalRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_"+i+"_bg_normal");
	           if (buttonNormalRes > 0) {
	               mDigitalBgNormalDrawables[i] = mThemeRes.getDrawable(buttonNormalRes);
	           }
        }

	    resetDigitalByDrawables();

        for (int i = 0; i < length; i++) {
            buttonNormalRes = ThemeUtils.getValueByResourceName(themeContext, "drawable", "digital_"+i+"_bg_active");
               if (buttonNormalRes > 0) {
                   mDigitalBgActiveDrawables[i] = mThemeRes.getDrawable(buttonNormalRes);
               }
        }
        
	}
	
	private void resetDigitalByDrawables() {
	       if (mDigitalBgNormalDrawables[0] != null) {
	            tv0Bottom.setImageDrawable(mDigitalBgNormalDrawables[0]);
	        }
	        if (mDigitalBgNormalDrawables[1] != null) {
	            tv1Bottom.setImageDrawable(mDigitalBgNormalDrawables[1]);
	        }
	        if (mDigitalBgNormalDrawables[2] != null) {
	            tv2Bottom.setImageDrawable(mDigitalBgNormalDrawables[2]);
	        }
	        if (mDigitalBgNormalDrawables[3] != null) {
	            tv3Bottom.setImageDrawable(mDigitalBgNormalDrawables[3]);
	        }
	        if (mDigitalBgNormalDrawables[4] != null) {
	            tv4Bottom.setImageDrawable(mDigitalBgNormalDrawables[4]);
	        }
	        if (mDigitalBgNormalDrawables[5] != null) {
	            tv5Bottom.setImageDrawable(mDigitalBgNormalDrawables[5]);
	        }
	        if (mDigitalBgNormalDrawables[6] != null) {
	            tv6Bottom.setImageDrawable(mDigitalBgNormalDrawables[6]);
	        }
	        if (mDigitalBgNormalDrawables[7] != null) {
	            tv7Bottom.setImageDrawable(mDigitalBgNormalDrawables[7]);
	        }
	        if (mDigitalBgNormalDrawables[8] != null) {
	            tv8Bottom.setImageDrawable(mDigitalBgNormalDrawables[8]);
	        }
	        if (mDigitalBgNormalDrawables[9] != null) {
	            tv9Bottom.setImageDrawable(mDigitalBgNormalDrawables[9]);
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
              tv1Bottom.setImageDrawable(normalDrawable);
              tv2Bottom.setImageDrawable(normalDrawable);
              tv3Bottom.setImageDrawable(normalDrawable);
              tv4Bottom.setImageDrawable(normalDrawable);
              tv5Bottom.setImageDrawable(normalDrawable);
              tv6Bottom.setImageDrawable(normalDrawable);
              tv7Bottom.setImageDrawable(normalDrawable);
              tv8Bottom.setImageDrawable(normalDrawable);
              tv9Bottom.setImageDrawable(normalDrawable);
              tv0Bottom.setImageDrawable(normalDrawable);
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
//				mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawable);
				mPasswds[0] = "";
//				mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawable);
                mPasswds[1] = "";
//				mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawable);
                mPasswds[2] = "";
//				mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawable);
                mPasswds[3] = "";
                initPasswdIcon();
			}
		}, 300);
	}

	private void deletePasswd() {
		if (!mPasswds[3].equals("")) {
		    mPasswds[3] = "";
	        if(mPasswdNormalDrawables[3] != null) {
	            mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawables[3]);
	        } else {
	              mTvPasswd4.setBackgroundDrawable(mPasswdNormalDrawable);
	        }
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mPasswds[2].equals("")) {
		    mPasswds[2] = "";
	        if(mPasswdNormalDrawables[2] != null) {
	            mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawables[2]);
	        } else {
	              mTvPasswd3.setBackgroundDrawable(mPasswdNormalDrawable);
	        }
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mPasswds[1].equals("")) {
		    mPasswds[1] = "";
            if(mPasswdNormalDrawables[1] != null) {
                mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawables[1]);
            } else {
                  mTvPasswd2.setBackgroundDrawable(mPasswdNormalDrawable);
            }
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mPasswds[0].equals("")) {
            mPasswds[0] = "";
            if(mPasswdNormalDrawables[0] != null) {
                mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawables[0]);
            } else {
                  mTvPasswd1.setBackgroundDrawable(mPasswdNormalDrawable);
            }
			iv_delete.setEnabled(false);
			mTempPasswd = "";
		}

	}

	private void inputPasswd(String s) {
		if (mPasswds[0].equals("")) {
		    mPasswds[0] = "*";
		    if(mPasswdActiveDrawables[0] != null) {
                mTvPasswd1.setBackgroundDrawable(mPasswdActiveDrawables[0]);
		    } else {
		          mTvPasswd1.setBackgroundDrawable(mPasswdActiveDrawable);
		    }
			iv_delete.setEnabled(true);
			mTempPasswd = s;
		} else if (mPasswds[1].equals("")) {
	          mPasswds[1] = "*";
	          if(mPasswdActiveDrawables[1] != null) {
	                mTvPasswd2.setBackgroundDrawable(mPasswdActiveDrawables[1]);
	            } else {
	                mTvPasswd2.setBackgroundDrawable(mPasswdActiveDrawable);
	            }
			mTempPasswd = mTempPasswd + s;
		} else if (mPasswds[2].equals("")) {
	          mPasswds[2] = "*";
	          if(mPasswdActiveDrawables[2] != null) {
	              mTvPasswd3.setBackgroundDrawable(mPasswdActiveDrawables[2]);
	          } else {
	              mTvPasswd3.setBackgroundDrawable(mPasswdActiveDrawable);
	          }
			mTempPasswd = mTempPasswd + s;
		} else if (mPasswds[3].equals("")) {
	        mPasswds[3] = "*";
	        if(mPasswdActiveDrawables[3] != null) {
	            mTvPasswd4.setBackgroundDrawable(mPasswdActiveDrawables[3]);
	        } else {
	            mTvPasswd4.setBackgroundDrawable(mPasswdActiveDrawable);
	        }
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
        Drawable normalDrawable = null;
        ImageView bottomView;
        
        if (mThemeRes != null) {
            if (mDigitalPressAnimRes > 0) {
                animDrawable = (AnimationDrawable)mThemeRes.getDrawable(mDigitalPressAnimRes);
            } else if (mDigitalBgActiveRes > 0) {
                activeDrawable = mThemeRes.getDrawable(mDigitalBgActiveRes);
            }
        }

        switch (view.getId()) {
        case R.id.tv_1_top:
            bottomView = tv1Bottom;
            if (mDigitalBgActiveDrawables[1] != null) {
                activeDrawable = mDigitalBgActiveDrawables[1];
            }
            if (mDigitalBgNormalDrawables[1] != null) {
                normalDrawable = mDigitalBgNormalDrawables[1];
            }
            break;
        case R.id.tv_2_top:
            bottomView = tv2Bottom;
            if (mDigitalBgActiveDrawables[2] != null) {
                activeDrawable = mDigitalBgActiveDrawables[2];
            }
            if (mDigitalBgNormalDrawables[2] != null) {
                normalDrawable = mDigitalBgNormalDrawables[2];
            }
            break;
        case R.id.tv_3_top:
            bottomView = tv3Bottom;
            if (mDigitalBgActiveDrawables[3] != null) {
                activeDrawable = mDigitalBgActiveDrawables[3];
            }
            if (mDigitalBgNormalDrawables[3] != null) {
                normalDrawable = mDigitalBgNormalDrawables[3];
            }
            break;
        case R.id.tv_4_top:
            bottomView = tv4Bottom;
            if (mDigitalBgActiveDrawables[4] != null) {
                activeDrawable = mDigitalBgActiveDrawables[4];
            }
            if (mDigitalBgNormalDrawables[4] != null) {
                normalDrawable = mDigitalBgNormalDrawables[4];
            }
            break;
        case R.id.tv_5_top:
            bottomView = tv5Bottom;
            if (mDigitalBgActiveDrawables[5] != null) {
                activeDrawable = mDigitalBgActiveDrawables[5];
            }
            if (mDigitalBgNormalDrawables[5] != null) {
                normalDrawable = mDigitalBgNormalDrawables[5];
            }
            break;
        case R.id.tv_6_top:
            bottomView = tv6Bottom;
            if (mDigitalBgActiveDrawables[6] != null) {
                activeDrawable = mDigitalBgActiveDrawables[6];
            }
            if (mDigitalBgNormalDrawables[6] != null) {
                normalDrawable = mDigitalBgNormalDrawables[6];
            }
            break;
        case R.id.tv_7_top:
            bottomView = tv7Bottom;
            if (mDigitalBgActiveDrawables[7] != null) {
                activeDrawable = mDigitalBgActiveDrawables[7];
            }
            if (mDigitalBgNormalDrawables[7] != null) {
                normalDrawable = mDigitalBgNormalDrawables[7];
            }
            break;
        case R.id.tv_8_top:
            bottomView = tv8Bottom;
            if (mDigitalBgActiveDrawables[8] != null) {
                activeDrawable = mDigitalBgActiveDrawables[8];
            }
            if (mDigitalBgNormalDrawables[8] != null) {
                normalDrawable = mDigitalBgNormalDrawables[8];
            }
            break;
        case R.id.tv_9_top:
            bottomView = tv9Bottom;
            if (mDigitalBgActiveDrawables[9] != null) {
                activeDrawable = mDigitalBgActiveDrawables[9];
            }
            if (mDigitalBgNormalDrawables[9] != null) {
                normalDrawable = mDigitalBgNormalDrawables[9];
            }
            break;
        case R.id.tv_0_top:
            bottomView = tv0Bottom;
            if (mDigitalBgActiveDrawables[0] != null) {
                activeDrawable = mDigitalBgActiveDrawables[0];
            }
            if (mDigitalBgNormalDrawables[0] != null) {
                normalDrawable = mDigitalBgNormalDrawables[0];
            }
            break;
        default:
            bottomView = tv0Bottom;
            if (mDigitalBgActiveDrawables[0] != null) {
                activeDrawable = mDigitalBgActiveDrawables[0];
            }
            if (mDigitalBgNormalDrawables[0] != null) {
                normalDrawable = mDigitalBgNormalDrawables[0];
            }
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
            Drawable digitalDrawable =bottomView.getDrawable();
            if (mDigitalBgNormalRes > 0 && !(digitalDrawable instanceof AnimationDrawable)) {
                bottomView.setImageDrawable(mThemeRes.getDrawable(mDigitalBgNormalRes));
            } else if (mDigitalBgNormalRes <= 0 && !(digitalDrawable instanceof AnimationDrawable)) {
                bottomView.setImageDrawable(normalDrawable);
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
           bottomView.setImageResource(R.drawable.digital_bg_active);
        } else if (state == BUTTON_STATE_NORMAL) {
           bottomView.setImageResource(R.drawable.digital_bg_normal);
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
