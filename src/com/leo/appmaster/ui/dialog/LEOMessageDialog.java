
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import com.leo.appmaster.R;

public class LEOMessageDialog extends LEOBaseDialog {
    public static final String TAG = "XLOneButtonDialog";

    private Context mContext;
//    private ImageView mLeftIcon;
    private TextView mTitle;
    private TextView mContent;
    private TextView mBottomBtn;
    private ImageView mIcon;
    private DialogInterface.OnClickListener mBottomBtnListener = null;

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

    public void setDialogIcon(int resID){
           mIcon.setImageResource(resID);
    }
    
    public void setDialogIconLayout(LayoutParams params){
        mIcon.setLayoutParams(params);
    }
    
    public LayoutParams getDialogIcomLayout(){
        return  mIcon.getLayoutParams();
    }
    
    public void setBottomBtnListener(DialogInterface.OnClickListener bListener) {
        if (bListener != null) {
            mBottomBtnListener = bListener;
            mBottomBtn.setTag(bListener);
            mBottomBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    DialogInterface.OnClickListener listener = (DialogInterface.OnClickListener) arg0
                            .getTag();
                    listener.onClick(LEOMessageDialog.this, 0);
                }
            });
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
