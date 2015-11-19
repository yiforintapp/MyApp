package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView1;

public class LEOThreeButtonDialog extends LEOBaseDialog {
    public static final String TAG = "XLAlarmDialog";

    private Context mContext;

    private TextView mTitle, mContent, mLeftBtn, mRightBtn, mMidBtn;
    private ImageView mHeadIcon;
    private Object mUserData;
    private RippleView1 mRvWhite, mRvBlue, mRvRed;

    private OnDiaogClickListener mListener;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public LEOThreeButtonDialog(Context context) {
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

    public void setTitleVisiable(boolean visiable) {
        mTitle.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }

    public void setTitle(String titleStr) {
        if (titleStr != null) {
            mTitle.setText(titleStr);
        } else {
            mTitle.setText(R.string.tips);
        }
    }


    public void setContentVisiable(boolean visiable) {
        mContent.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }

    public void setContent(String titleStr) {
        if (titleStr != null) {
            mContent.setText(titleStr);
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

    public void setMiddleBtnStr(String lStr) {
        if (lStr != null) {
            mMidBtn.setText(lStr);
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

    public void setRightBtnVisiable(int visiable) {
        mRightBtn.setVisibility(visiable);
//        if(visiable != View.VISIBLE){
//            setSecondBtnBackground(R.drawable.manager_right_contact_button_selecter);
//        }
    }


    public void setLeftBtnListener(DialogInterface.OnClickListener lListener) {
        mRvWhite.setTag(lListener);
        mRvWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvWhite
                        .getTag();
                try {
                    lListener.onClick(LEOThreeButtonDialog.this, 0);
                } catch (Exception e) {
                }
            }
        });
//	    mRvWhite.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//			@Override
//			public void onRippleComplete(RippleView arg0) {
//				DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvWhite
//						.getTag();
//                try {
//                    lListener.onClick(LEOThreeButtonDialog.this, 0);
//                } catch (Exception e) {
//                }
//			}
//		});
    }

    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        mRvRed.setTag(rListener);
        mRvRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvRed
                        .getTag();
                try {
                    lListener.onClick(LEOThreeButtonDialog.this, 2);
                } catch (Exception e) {
                }
            }
        });
//		mRvRed.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//
//			@Override
//			public void onRippleComplete(RippleView arg0) {
//				DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvRed
//						.getTag();
//                try {
//                    lListener.onClick(LEOThreeButtonDialog.this, 2);
//                } catch (Exception e) {
//                }
//			}
//		});
    }

    public void setMidBtnListener(DialogInterface.OnClickListener rListener) {
        mRvBlue.setTag(rListener);
        mRvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
                        .getTag();
                try {
                    lListener.onClick(LEOThreeButtonDialog.this, 1);
                } catch (Exception e) {
                }
            }
        });
//        mRvBlue.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//
//            @Override
//            public void onRippleComplete(RippleView arg0) {
//                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
//                        .getTag();
//                try {
//                    lListener.onClick(LEOThreeButtonDialog.this, 1);
//                } catch (Exception e) {
//                }
//            }
//        });
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_alarm_three_button, null);

        mRvWhite = (RippleView1) dlgView.findViewById(R.id.rv_dialog_whitle_button);
        mRvBlue = (RippleView1) dlgView.findViewById(R.id.rv_dialog_blue_button);
        mRvRed = (RippleView1) dlgView.findViewById(R.id.rv_dialog_Red_button);

        mHeadIcon = (ImageView) dlgView.findViewById(R.id.dlg_icon);
        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mContent = (TextView) dlgView.findViewById(R.id.dlg_content);

        mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);
        mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);
        mMidBtn = (TextView) dlgView.findViewById(R.id.dlg_mid_btn);

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
        setMidBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setContentGravity(int gravity) {
        mContent.setGravity(gravity);
    }

    public void setOnClickListener(OnDiaogClickListener listener) {
        mListener = listener;
    }

    public void setSecondBtnBackground(int background) {
        mMidBtn.setBackgroundResource(background);
    }

    public void setDialogIconDrawable(Drawable drawable) {
        mHeadIcon.setImageDrawable(drawable);
    }
}
