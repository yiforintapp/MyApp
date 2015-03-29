
package com.leo.appmaster.ui.dialog;

import android.R.integer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.Editable;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
import com.leo.appmaster.utils.LeoLog;

public class MonthDaySetting extends LEOBaseDialog {
    private Context mContext;
    private EditText first_ed, second_ed;
    private TextView sure_button;
    private AppMasterPreference sp_notice_flow;
    private int itselfmonthuse = 0, monthtraffic = 0;

    private OnTrafficDialogClickListener mListener;

    public interface OnTrafficDialogClickListener {
        public void onClick();
    }

    public MonthDaySetting(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        sp_notice_flow = AppMasterPreference.getInstance(mContext);
        initUI();
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_monthday_setting, null);
        Resources resources = AppMasterApplication.getInstance().getResources();

        first_ed = (EditText) dlgView.findViewById(R.id.first_ed);
        second_ed = (EditText) dlgView.findViewById(R.id.second_ed);
        sure_button = (TextView) dlgView.findViewById(R.id.sure_button);

//        int monthUsedData = (int) (sp_notice_flow.getMonthGprsAll() / 1024 * 1024);
        first_ed.setText(sp_notice_flow.getItselfMonthTraffic()/1024/1024 + "");
        second_ed.setText(sp_notice_flow.getTotalTraffic() + "");
        Editable etext1 = first_ed.getText();
        Selection.setSelection(etext1, etext1.length());
        Editable etext2 = second_ed.getText();
        Selection.setSelection(etext2, etext2.length());

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {

                    String firstEdittext = first_ed.getText().toString();
                    if (firstEdittext.equals("") || firstEdittext.isEmpty()) {
                        firstEdittext = "0";
                    }

                    String edittextString = second_ed.getText().toString();
                    if (edittextString.equals("") || edittextString.isEmpty()) {
                        edittextString = "0";
                    }

                    int edittext_one = Integer.parseInt(firstEdittext);
                    if (firstEdittext.isEmpty() || edittext_one == 0) {
                        itselfmonthuse = 0;
                    } else {
                        itselfmonthuse = edittext_one;
                    }
                    sp_notice_flow.setItselfMonthTraffic(itselfmonthuse * 1024 * 1024);

                    int edittext = Integer.parseInt(edittextString);
                    boolean isSwtich = sp_notice_flow.getFlowSetting();
                    if (edittextString.isEmpty() || edittext == 0) {
                        monthtraffic = 0;
                        sp_notice_flow.setFlowSetting(false);
                    } else {
                        monthtraffic = edittext;
                        if (monthtraffic > 0 && !isSwtich) {
                            // 打开超额提醒
                            sp_notice_flow.setFlowSetting(true);
                        }
                    }
                    sp_notice_flow.setTotalTraffic(monthtraffic);

                    float settingMonthTraffi = monthtraffic * 1024 * 1024;
                    float mItselfSetTraffic = itselfmonthuse/1024/1024;
                    int settingBar = sp_notice_flow.getFlowSettingBar();
                    float monthUsedTraffic = sp_notice_flow.getMonthGprsAll();
                    float bili = 0;
                    // float bili = monthUsedTraffic*100 / settingMonthTraffi;

                    if (mItselfSetTraffic > 0) {
                        if (settingMonthTraffi > 0) {
                            bili = mItselfSetTraffic * 100 / settingMonthTraffi;
                        }else {
                            bili =  0;
                        }
                        if (settingMonthTraffi < mItselfSetTraffic) {
                            if (sp_notice_flow.getFinishNotice()) {
                                sp_notice_flow.setFinishNotice(false);
                            }
                        } else if (bili > settingBar) {
                            if (sp_notice_flow.getAlotNotice()) {
                                sp_notice_flow.setAlotNotice(false);
                            }
                        }
                    } else {
                        if (settingMonthTraffi > 0) {
                            bili = monthUsedTraffic * 100 / settingMonthTraffi;
                        }else {
                            bili =  0;
                        }
                        if (settingMonthTraffi < monthUsedTraffic) {
                            if (sp_notice_flow.getFinishNotice()) {
                                sp_notice_flow.setFinishNotice(false);
                            }
                        } else if (bili > settingBar) {
                            if (sp_notice_flow.getAlotNotice()) {
                                sp_notice_flow.setAlotNotice(false);
                            }
                        }
                    }

                    mListener.onClick();
                }
                dialog.dismiss();
            }
        };

        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setOnClickListener(OnTrafficDialogClickListener listener) {
        mListener = listener;
    }

    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        sure_button.setTag(rListener);
        sure_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) sure_button
                        .getTag();
                lListener.onClick(MonthDaySetting.this, 1);
            }
        });
    }

}
