
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.Editable;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.ManagerFlowUtils;

public class MonthDaySetting extends LEOBaseDialog {
    private Context mContext;
    private EditText first_ed, second_ed;
    private TextView sure_button;
    private AppMasterPreference sp_notice_flow;
    private int itselfmonthuse = 0, monthtraffic = 0;
    private RippleView mRvBlue;

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
        mRvBlue = (RippleView) dlgView.findViewById(R.id.rv_dialog_blue_button);
//        int monthUsedData = (int) (sp_notice_flow.getMonthGprsAll() / 1024 * 1024);
//        first_ed.setText(sp_notice_flow.getItselfMonthTraffic()/1024/1024 + "");
//        first_ed.setText(sp_notice_flow.getItselfMonthTraffic() / 1024 + "");
        long monthTraffic = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getMonthUsed();
        String useMb = ManagerFlowUtils.refreshTraffic_home_app(monthTraffic);
        if (useMb != null && useMb.endsWith("KB")) {
            useMb = "0";
        } else if (useMb != null && useMb.endsWith("MB")) {
            try {
                useMb = useMb.substring(0, useMb.length() - 2);
                useMb = String.valueOf((int)Math.rint(Double.parseDouble(useMb))); //四舍五入取整
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        first_ed.setText(useMb);
        second_ed.setText(((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getMonthTotalTraffic() + "");
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
                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "datapage", "changedata");
                        itselfmonthuse = edittext_one;
                    }
                    sp_notice_flow.setItselfMonthTraffic(itselfmonthuse * 1024);

                    int edittext = Integer.parseInt(edittextString);
                    boolean isSwtich = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                            getOverDataSwitch();
//                    boolean isSwtich = sp_notice_flow.getFlowSetting();
                    if (edittextString.isEmpty() || edittext == 0) {
                        monthtraffic = 0;
                        ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                                setOverDataSwitch(false);
//                        sp_notice_flow.setFlowSetting(false);
                    } else {
                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "datapage", "plansetting");
                        monthtraffic = edittext;
                        if (monthtraffic > 0 && !isSwtich) {
                            // 打开超额提醒
                            ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                                    setOverDataSwitch(true);
//                            sp_notice_flow.setFlowSetting(true);
                        }
                    }
                    ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                            setMonthTotalTraffic(monthtraffic);
//                    sp_notice_flow.setTotalTraffic(monthtraffic);

                    float settingMonthTraffi = monthtraffic;
                    float settingMonthTraffiKb = monthtraffic * 1024;
                    float mItselfSetTrafficKb = itselfmonthuse * 1024;
                    int settingBar = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                            getOverDataInvokePercent();
                    float monthUsedTraffic = sp_notice_flow.getMonthGprsAll() / 1024;
                    float bili = 0;
                    if (mItselfSetTrafficKb > 0) {
                        if (settingMonthTraffi > 0) {
                            bili = mItselfSetTrafficKb * 100 / settingMonthTraffiKb;
                        } else {
                            bili = 0;
                        }
                        LeoLog.d("MonthDaySetting", "bili is : " + bili);
                        if (settingMonthTraffiKb < mItselfSetTrafficKb) {
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
                            bili = monthUsedTraffic * 100 / settingMonthTraffiKb;
                        } else {
                            bili = 0;
                        }
                        LeoLog.d("MonthDaySetting", "else bili is : " + bili);
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
        mRvBlue.setTag(rListener);
        mRvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
                        .getTag();
                try {
                    lListener.onClick(MonthDaySetting.this, 1);
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
//                    lListener.onClick(MonthDaySetting.this, 1);
//                } catch (Exception e) {
//                }
//            }
//        });
    }

}
