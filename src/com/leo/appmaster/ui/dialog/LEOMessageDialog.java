
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView;

public class LEOMessageDialog extends LEOBaseDialog {
    public static final String TAG = "XLOneButtonDialog";

    private Context mContext;
    private TextView mTitle;
    private TextView mContent;
    private TextView mBottomBtn;
    private ImageView mIcon;
    private DialogInterface.OnClickListener mBottomBtnListener = null;
    private RippleView mRvBlue;

    public LEOMessageDialog(Context context) {
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

    public void setBottomBtnListener(DialogInterface.OnClickListener bListener) {
        if (bListener != null) {
            mBottomBtnListener = bListener;
            mRvBlue.setTag(bListener);
            mRvBlue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogInterface.OnClickListener listener = (DialogInterface.OnClickListener) view
                            .getTag();
                    try {
                        listener.onClick(LEOMessageDialog.this, 0);
                    } catch (Exception e) {
                    }
                }
            });
//            mRvBlue.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//
//                @Override
//                public void onRippleComplete(RippleView arg0) {
//                    DialogInterface.OnClickListener listener = (DialogInterface.OnClickListener) arg0
//                            .getTag();
//                    try {
//                        listener.onClick(LEOMessageDialog.this, 0);
//                    } catch (Exception e) {
//                    }
//                }
//            });
        }
    }

//    public void setIcon(Drawable drawable) {
//        if (drawable != null) {
////            mLeftIcon.setVisibility(View.VISIBLE);
////            mLeftIcon.setImageDrawable(drawable);
//        }
//    }

    public void setContentGravity(int gravity) {
        mContent.setGravity(gravity);
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(R.layout.dialog_message_single_done, null);

//        mLeftIcon = (ImageView) dlgView.findViewById(R.id.dlg_left_icon);
//        mLeftIcon.setVisibility(View.GONE);
        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mContent = (TextView) dlgView.findViewById(R.id.dlg_content);
        mIcon = (ImageView) dlgView.findViewById(R.id.dlg_icon);
        mRvBlue = (RippleView) dlgView.findViewById(R.id.rv_blue);
        mBottomBtn = (TextView) dlgView.findViewById(R.id.dlg_bottom_btn);
        mBottomBtn.setVisibility(View.VISIBLE);
        if (mBottomBtnListener == null) {
            setBottomBtnListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dlg, int arg1) {
                    dlg.dismiss();
                }
            });
        }
        setContentView(dlgView);
    }
}
