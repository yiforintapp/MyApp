
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView;

public class LEODoubleChoicesDialog extends LEOBaseDialog {
    public static final String TAG = "Dialogggg";

    private Context mContext;

    private TextView mTitle;
    private TextView mContent;
    private TextView mLeftBtn;
    private TextView mRightBtn;

    private CheckBox mCBFromCorner;
    private CheckBox mCBWhiteDot;

    private RippleView mRvRight;
    private RippleView mRvLeft;

    public LEODoubleChoicesDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
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
            public void onClick(View arg0) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvLeft
                        .getTag();
                lListener.onClick(LEODoubleChoicesDialog.this, 0);
            }
        });
    }

    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        mRvRight.setTag(rListener);
        mRvRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvRight
                        .getTag();
                lListener.onClick(LEODoubleChoicesDialog.this, 1);
            }
        });
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_double_choices, null);
        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mContent = (TextView) dlgView.findViewById(R.id.dlg_content);

        mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);

        mRvRight = (RippleView) dlgView.findViewById(R.id.rv_dialog_blue_button);
        mRvLeft = (RippleView) dlgView.findViewById(R.id.rv_dialog_white_button);

        mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);
        mCBFromCorner = (CheckBox) dlgView.findViewById(R.id.cb_dialog_area);
        mCBWhiteDot = (CheckBox) dlgView.findViewById(R.id.cb_dialog_whitedot);

        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setContentGravity(int gravity) {
        mContent.setGravity(gravity);
    }

    public void setSureButtonText(String mText) {
        if (mText != null) {
            mRightBtn.setText(mText);
        }
    }

    public void setLeftBtnVisibility(boolean flag) {
        if (!flag) {
            mLeftBtn.setVisibility(View.GONE);
        }
    }

    public void setRightBtnParam(float width, float height) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mRvRight
                .getLayoutParams();
        linearParams.height = (int) height;
        linearParams.width = (int) width;
        mRvRight.setLayoutParams(linearParams);
    }
}
