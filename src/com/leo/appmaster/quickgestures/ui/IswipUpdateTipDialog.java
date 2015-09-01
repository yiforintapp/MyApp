
package com.leo.appmaster.quickgestures.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.Utils;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

public class IswipUpdateTipDialog extends LEOBaseDialog {
    private static final String HOMEACTIVITY = "homeactivity";
    private Context mContext;
    private TextView mTitle;
    private TextView mContent;
    private TextView mLeftBt;
    private TextView mRightBt;
    String mDialgFlag;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public IswipUpdateTipDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.iswip_update_tip_dialog, null);
        mTitle = (TextView) dlgView.findViewById(R.id.iswip_tip_title_text);
        mContent = (TextView) dlgView.findViewById(R.id.iswip_tip_content_text);
        mLeftBt = (TextView) dlgView.findViewById(R.id.iswip_left_bt);
        mRightBt = (TextView) dlgView.findViewById(R.id.iswip_right_bt);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
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

    public void setVisiblilyTitle(boolean flag) {
        if (flag) {
            mTitle.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.GONE);
        }
    }

    public void setContextText(String content) {
        if (!Utilities.isEmpty(content)) {
            mContent.setText(content);
        }
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

    public void setRightButtonText(String right) {
        if (!Utilities.isEmpty(right)) {
            mRightBt.setText(right);
        }
    }

    public void setLeftListener(android.view.View.OnClickListener listent) {
        mLeftBt.setOnClickListener(listent);
    }

    public void setRightListener(android.view.View.OnClickListener listent) {
        mRightBt.setOnClickListener(listent);
    }

    @Override
    public void dismiss() {
        boolean noEmpty = !Utilities.isEmpty(mDialgFlag);
        boolean flagEquals = HOMEACTIVITY.equals(mDialgFlag);
        if (noEmpty && flagEquals) {
            /* 主页弹出的对话框初始化弹出标志值 */
            ISwipUpdateRequestManager.getInstance(mContext).cancelShowIswipUpdate();
            mDialgFlag = null;
        }
        super.dismiss();
    }
}
