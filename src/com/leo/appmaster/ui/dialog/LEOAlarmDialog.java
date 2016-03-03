
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView;

public class LEOAlarmDialog extends LEOBaseDialog {
    public static final String TAG = "XLAlarmDialog";

    private Context mContext;

    private TextView mTitle;
    private TextView mContent;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    private Object mUserData;
    private ImageView mDialogIcon;
    private RippleView mRvRight;
    private RippleView mRvLeft;

    private OnDiaogClickListener mListener;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public LEOAlarmDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    public void setUserData(Object userData) {
        mUserData = userData;
    }

    public Object getUserData() {
        return mUserData;
    }

    public void setTitle(String titleStr) {
        if (titleStr != null) {
            mTitle.setText(titleStr);
        } else {
            mTitle.setText(R.string.tips);
        }
    }

    public void setTitleVisiable(boolean visiable) {
        if (visiable) {
            mTitle.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.GONE);
            mDialogIcon.getLayoutParams();
        }
    }

    public void setContent(String titleStr) {
        if (titleStr != null) {
            mContent.setText(titleStr);
        }
    }

    public void setContentVisiable(boolean visiable) {
        if (visiable) {
            mContent.setVisibility(View.VISIBLE);
        } else {
            mContent.setVisibility(View.GONE);
        }
    }

    public void setContentLineSpacing(int lineSpace) {
        mContent.setLineSpacing(lineSpace, 1);
    }

    public void setContent(SpannableString text) {
        if (text != null) {
            mContent.setText(text);
        }
    }

    public void setSpanContent(SpannableString titleStr) {
        if (titleStr != null) {
            mContent.setText(titleStr);
        }
    }

    public void setLeftBtnStr(String lStr) {
        if (lStr != null) {
            mLeftBtn.setText(lStr);
        }
    }

    public void setRightBtnStr(String rStr) {
        if (rStr != null) {
            mRightBtn.setText(rStr);
        }
    }

    public void setRightBtnBackground(Drawable drawable) {
        if (drawable != null) {
            mRightBtn.setBackgroundDrawable(drawable);
        }
    }

    public void setRightBtnBackground(int resid) {
        mRightBtn.setBackgroundResource(resid);
    }

    public void setRightBtnTextColor(int color) {
        mRightBtn.setTextColor(color);
    }

    public void setLeftBtnListener(DialogInterface.OnClickListener lListener) {
        mRvLeft.setTag(lListener);
        mRvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvLeft
                        .getTag();
                try {
                    lListener.onClick(LEOAlarmDialog.this, 0);
                } catch (Exception e) {
                }
            }
        });
//        mRvLeft.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//
//            @Override
//            public void onRippleComplete(RippleView arg0) {
//                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvLeft
//                        .getTag();
//                try {
//                    lListener.onClick(LEOAlarmDialog.this, 0);
//                } catch (Exception e) {
//                }
//            }
//        });
    }

    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        mRvRight.setTag(rListener);
        mRvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvRight
                        .getTag();
                try {
                    lListener.onClick(LEOAlarmDialog.this, 1);
                } catch (Exception e) {
                }
            }
        });
//        mRvRight.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//
//            @Override
//            public void onRippleComplete(RippleView arg0) {
//                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvRight
//                        .getTag();
//                try {
//                    lListener.onClick(LEOAlarmDialog.this, 1);
//                } catch (Exception e) {
//                }
//            }
//        });
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_alarm, null);

        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mContent = (TextView) dlgView.findViewById(R.id.dlg_content);
        mContent.setMovementMethod(ScrollingMovementMethod.getInstance()) ;
        mRvRight = (RippleView) dlgView.findViewById(R.id.rv_dialog_blue_button);
        mRvLeft = (RippleView) dlgView.findViewById(R.id.rv_dialog_whitle_button);
        mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);
        mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);
        mDialogIcon = (ImageView) dlgView.findViewById(R.id.dlg_icon);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {
                    mListener.onClick(arg1);
                }
                dialog.dismiss();
            }
        };
        setLeftBtnListener(listener);
        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setDialogIcon(int id) {
        mDialogIcon.setImageResource(id);
    }

    public void setDialogIconDrawable(Drawable drawable) {
        mDialogIcon.setImageDrawable(drawable);
    }


    public void setContentGravity(int gravity) {
        mContent.setGravity(gravity);
    }


    public void setSureButtonText(String mText) {
        if (mText != null) {
            mRightBtn.setText(mText);
        }
    }

    public void setOnClickListener(OnDiaogClickListener listener) {
        mListener = listener;
    }

    public void setLeftBtnVisibility(boolean flag) {
        if (!flag) {
            mLeftBtn.setVisibility(View.GONE);
            mRvLeft.setVisibility(View.GONE);
        }
    }

    public void setRightBtnParam(float width, float height) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mRvRight
                .getLayoutParams();

        linearParams.height = (int) height;
        linearParams.width = (int) width;
        mRvRight.setLayoutParams(linearParams);
    }

    public void setRightBtnParam(float width, float height, boolean isLeftGone) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mRvRight
                .getLayoutParams();
        linearParams.gravity = Gravity.CENTER_HORIZONTAL;
        linearParams.height = (int) height;
        linearParams.width = (int) width;
        mRvRight.setLayoutParams(linearParams);
    }

    public void setDialogIconVisibility(boolean flag) {
        if (flag) {
            mDialogIcon.setVisibility(View.VISIBLE);
        } else {
            mDialogIcon.setVisibility(View.GONE);
        }
    }
}
