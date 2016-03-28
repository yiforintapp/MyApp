
package com.leo.appmaster.home;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
import com.leo.appmaster.utils.Utilities;

public class AdvanceProtectTipDialog extends LEOBaseDialog {
    public static final String ADVANCE_PROTECT_TIP_DIALOG = "advance_protect_tip_dialog";
    private Context mContext;
    private TextView mTitle;
    private TextView mContent;
    private TextView mLeftBt;
    private TextView mRightBt;
    private ImageView mContentImage;
    private LinearLayout mLinearLayout;
    String mDialgFlag;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public AdvanceProtectTipDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.advance_protect_tip_dialog, null);
        mTitle = (TextView) dlgView.findViewById(R.id.iswip_tip_title_text);
        mContent = (TextView) dlgView.findViewById(R.id.iswip_tip_content_text);
        mLeftBt = (TextView) dlgView.findViewById(R.id.iswip_left_bt);
        mRightBt = (TextView) dlgView.findViewById(R.id.iswip_right_bt);
        mContentImage = (ImageView) dlgView.findViewById(R.id.iswip_tip_title_pic);
        mLinearLayout = (LinearLayout) dlgView.findViewById(R.id.iswipe_update_tip_dialog);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setContentImage(int image) {
        mContentImage.setImageResource(image);
    }

    public void setIswipeUpdateDialogBackground(int background) {
        mLinearLayout.setBackgroundResource(background);
    }

    public void setFlag(String flag) {
        if (!Utilities.isEmpty(flag)) {
            mDialgFlag = flag;
        }
    }

    public void setTitleText(String title) {
        if (!Utilities.isEmpty(title)) {
            mTitle.setText(title);
        }
    }

    public void setTitleTextColor(int id) {
        mTitle.setTextColor(id);
    }

    public void setVisiblilyTitle(boolean flag) {
        if (flag) {
            mTitle.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.GONE);
        }
    }

    public void setContentText(String content) {
        if (!Utilities.isEmpty(content)) {
            mContent.setText(content);
        }
    }
    public void setContentTextId(int content) {
            mContent.setText(content);
    }

    public void setContentTextColor(int id) {
        mContent.setTextColor(ColorStateList.valueOf(id));
    }

    public void setVisiblilyContent(boolean flag) {
        if (flag) {
            mContent.setVisibility(View.VISIBLE);
        } else {
            mContent.setVisibility(View.GONE);
        }
    }

    public void setLeftButtonText(String left) {
        if (!Utilities.isEmpty(left)) {
            mLeftBt.setText(left);
        }

    }

    public void setLeftButtonTextColor(int id) {
        mLeftBt.setTextColor(id);
    }

    public void setLeftOnKeyListener(android.view.View.OnKeyListener listener) {
        mLeftBt.setOnKeyListener(listener);
    }

    public void setRightButtonText(String right) {
        if (!Utilities.isEmpty(right)) {
            mRightBt.setText(right);
        }
    }

    public void setRightButtonTextColor(int id) {
        mRightBt.setTextColor(id);
    }

    public void setLeftListener(android.view.View.OnClickListener listent) {
        mLeftBt.setOnClickListener(listent);
    }

    public void setRightListener(android.view.View.OnClickListener listent) {
        mRightBt.setOnClickListener(listent);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
