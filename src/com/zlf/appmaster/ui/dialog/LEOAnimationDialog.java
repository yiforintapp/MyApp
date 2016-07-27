
package com.zlf.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.ui.dialog.LEOBaseDialog;
import com.zlf.tools.animator.Animator;
import com.zlf.tools.animator.AnimatorListenerAdapter;
import com.zlf.tools.animator.AnimatorSet;
import com.zlf.tools.animator.ObjectAnimator;

public class LEOAnimationDialog extends LEOBaseDialog {
    public static final String TAG = "XLOneButtonDialog";
    private static final int SHOW_ANIMATION = 20;
    private static final int SHOW_ANIMATION_DELAY = 1000;
    private static final int HAND_MOVE_DOWN = 21;
    private static final int HAND_MOVE_UP = 22;
    private static final int CLICK_DELAY = 500;
    private static final int MOVE_UP_DELAY = 1000;
    private Context mContext;
    private TextView mTitle;
    private TextView mContent;
    private TextView mBottomBtn;
    private ImageView mIcon;
    private ImageView mMoveHand;
    private OnClickListener mBottomBtnListener = null;
    private RippleView mRvBlue;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_ANIMATION:
                    startAnimation();
                    break;
                case HAND_MOVE_DOWN:
                    startHandMoveDown();
                    break;
                case HAND_MOVE_UP:
                    startHandMoveUp();
                    break;

            }
        }

    };


    public LEOAnimationDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
        prepareToShow();
    }

    private void prepareToShow() {
        mHandler.sendEmptyMessageDelayed(SHOW_ANIMATION, SHOW_ANIMATION_DELAY);
    }

    private void startHandMoveUp() {
        mIcon.setImageResource(R.drawable.uninstall_guide_phone1);
        ObjectAnimator handmoveY = ObjectAnimator.ofFloat(mMoveHand,
                "y", mMoveHand.getY(), mMoveHand.getY() - mMoveHand.getHeight() * 3 / 4);
        handmoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animationClick(true);
            }
        });
        handmoveY.setDuration(600);
        handmoveY.start();
    }

    private void startHandMoveDown() {
        mIcon.setImageResource(R.drawable.uninstall_guide_phone2);
        ObjectAnimator handmoveY = ObjectAnimator.ofFloat(mMoveHand,
                "y", mMoveHand.getY(), mMoveHand.getY() + mMoveHand.getHeight() * 3 / 4);
        handmoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animationClick(false);
            }
        });
        handmoveY.setDuration(600);
        handmoveY.start();
    }

    private void startAnimation() {
        animationClick(true);
    }

    private void animationClick(final boolean firstClick) {
        ObjectAnimator handClickX = ObjectAnimator.ofFloat(mMoveHand,
                "scaleX", 1f, 0.8f, 1f);
        ObjectAnimator handClickY = ObjectAnimator.ofFloat(mMoveHand,
                "scaleY", 1f, 0.8f, 1f);
        AnimatorSet setclick = new AnimatorSet();
        setclick.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (firstClick) {
                    mHandler.sendEmptyMessageDelayed(HAND_MOVE_DOWN, CLICK_DELAY);
                } else {
                    mHandler.sendEmptyMessageDelayed(HAND_MOVE_UP, MOVE_UP_DELAY);
                }
            }
        });
        setclick.setDuration(400);
        setclick.play(handClickX).with(handClickY);
        setclick.start();
    }

    public void setTitle(String titleStr) {
        if (titleStr != null) {
            mTitle.setText(titleStr);
        } else {
            mTitle.setText(R.string.tips);
        }
    }

    public void setContent(String titleStr) {
        if (titleStr != null) {
            mContent.setText(titleStr);
        }
    }

    public void setBottomBtnStr(String bottomStr) {
        if (bottomStr != null)
            mBottomBtn.setText(bottomStr);
    }

    public void setDialogIcon(int resID) {
        mIcon.setImageResource(resID);
    }

    public void setDialogIconLayout(LayoutParams params) {
        mIcon.setLayoutParams(params);
    }

    public LayoutParams getDialogIcomLayout() {
        return mIcon.getLayoutParams();
    }

    public void setBottomBtnListener(OnClickListener bListener) {
        if (bListener != null) {
            mBottomBtnListener = bListener;
            mRvBlue.setTag(bListener);
            mRvBlue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OnClickListener listener = (OnClickListener) view
                            .getTag();
                    try {
                        listener.onClick(LEOAnimationDialog.this, 0);
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(R.layout.dialog_anima_single_done, null);
        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mContent = (TextView) dlgView.findViewById(R.id.dlg_content);
        mIcon = (ImageView) dlgView.findViewById(R.id.dlg_icon);
        mMoveHand = (ImageView) dlgView.findViewById(R.id.move_hand_iv);
        mRvBlue = (RippleView) dlgView.findViewById(R.id.rv_blue);
        mBottomBtn = (TextView) dlgView.findViewById(R.id.dlg_bottom_btn);
        mBottomBtn.setVisibility(View.VISIBLE);
        if (mBottomBtnListener == null) {
            setBottomBtnListener(new OnClickListener() {
                @Override
                public void onClick(DialogInterface dlg, int arg1) {
                    dlg.dismiss();
                }
            });
        }
        setContentView(dlgView);
    }
}
