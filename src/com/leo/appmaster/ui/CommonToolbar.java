package com.leo.appmaster.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;

/**
 * 通用标题栏，可设置背景色，包含如下元素
 * 1、返回按钮
 * 2、右边通用按钮
 * 3、标题
 * Created by Jasper on 2015/10/14.
 */
public class CommonToolbar extends RelativeLayout implements View.OnClickListener {
    private ImageView mBackArrow;
    private View mOptionView;
    private ImageView mOptionImg;
    private TextView mTitle;

    private View mOption2View;
    private ImageView mOption2Img;

    private View mNavigationView;

    private OnClickListener mListener;
    private OnClickListener mOptionListener;
    private OnClickListener mSecOptionListener;

    public CommonToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CommonToolbar(Context context) {
        this(context, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.common_toolbar, this, true);
        mNavigationView = findViewById(R.id.ct_back_rl);
        mNavigationView.setOnClickListener(this);
//        if (mNavigationView instanceof RippleView) {
//            ((RippleView) mNavigationView).setOnRippleCompleteListener(this);
//        } else {
//            mNavigationView.setOnClickListener(this);
//        }

        mBackArrow = (ImageView) findViewById(R.id.ct_back_arrow_iv);

        mOptionView = findViewById(R.id.ct_option_1_rl);
        mOptionView.setOnClickListener(this);
//        if (mOptionView instanceof RippleView) {
//            ((RippleView) mOptionView).setOnRippleCompleteListener(this);
//        } else {
//            mOptionView.setOnClickListener(this);
//        }
        mOptionImg = (ImageView) findViewById(R.id.ct_option_iv1);

        mTitle = (TextView) findViewById(R.id.ct_title_tv);

        mOption2View = findViewById(R.id.ct_option_2_rl);
        mOption2View.setOnClickListener(this);
//        if (mOption2View instanceof RippleView) {
//            ((RippleView) mOption2View).setOnRippleCompleteListener(this);
//        } else {
//            mOption2View.setOnClickListener(this);
//        }
        mOption2Img = (ImageView) findViewById(R.id.ct_option_iv2);
    }

    public void setToolbarColorResource(int resource) {
        setBackgroundResource(resource);
    }

    public void setOptionMenuVisible(boolean visible) {
        mOptionView.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setSecOptionMenuVisible(boolean visible) {
        mOption2View.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setToolbarColor(int color) {
        setBackgroundColor(color);
    }

    public void setNavigationClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public void setNavigationLogoResource(int imageResource) {
        mBackArrow.setImageResource(imageResource);
    }

    /**
     * 设置第一个菜单选中监听
     *
     * @param listener
     */
    public void setOptionClickListener(OnClickListener listener) {
        mOptionListener = listener;
//        mOptionView.setOnClickListener(listener);
    }

    /**
     * 设置第一个菜单资源
     *
     * @param imageResource
     */
    public void setOptionImageResource(int imageResource) {
        mOptionImg.setImageResource(imageResource);
    }

    public ImageView getOptionImageView() {
        return mOptionImg;
    }

    /**
     * 设置第二个菜单监听
     *
     * @param listener
     */
    public void setSecOptionClickListener(OnClickListener listener) {
        mSecOptionListener = listener;
//        mOption2View.setOnClickListener(listener);
    }

    /**
     * 设置第二个菜单资源
     *
     * @param imageResource
     */
    public void setSecOptionImageResource(int imageResource) {
        mOption2Img.setImageResource(imageResource);
    }

    public void setToolbarTitle(int stringId) {
        mTitle.setText(stringId);
    }

    public void setToolbarTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ct_back_rl:
                if (mListener != null) {
                    mListener.onClick(v);
                } else {
                    Context context = getContext();
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }
                break;
            case R.id.ct_option_1_rl:
                if (mOptionListener != null) {
                    mOptionListener.onClick(v);
                }
                break;
            case R.id.ct_option_2_rl:
                if (mSecOptionListener != null) {
                    mSecOptionListener.onClick(v);
                }
                break;
        }
    }

}
