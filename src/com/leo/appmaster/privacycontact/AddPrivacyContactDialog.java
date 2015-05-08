
package com.leo.appmaster.privacycontact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

public class AddPrivacyContactDialog extends LEOBaseDialog {
    public static final String TAG = "XLAlarmDialog";

    private Context mContext;

    private View mAddFromSms;
    private View mAddFromCall;
    private View mAddFromContact;
    private View mAddFromInput;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public AddPrivacyContactDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context;
        initUI();
    }

    public void setSmsListener(View.OnClickListener listener) {
        mAddFromSms.setOnClickListener(listener);
    }

    public void setCallLogListener(View.OnClickListener listener) {
        mAddFromCall.setOnClickListener(listener);
    }

    public void setContactListener(View.OnClickListener listener) {
        mAddFromContact.setOnClickListener(listener);
    }

    public void setInputListener(View.OnClickListener listener) {
        mAddFromInput.setOnClickListener(listener);
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(R.layout.add_privacy_contacts_dialog,
                null);
        mAddFromCall = dlgView.findViewById(R.id.dlg_add_call);
        mAddFromContact = dlgView.findViewById(R.id.dlg_add_contact);
        mAddFromInput =  dlgView.findViewById(R.id.dlg_add_input);
        mAddFromSms = dlgView.findViewById(R.id.dlg_add_sms);
        setCanceledOnTouchOutside(true);
        setContentView(dlgView);
    }

}
