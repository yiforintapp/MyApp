package com.leo.appmaster.phoneSecurity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.mgr.impl.PrivacyContactManagerImpl;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;

public class SecurityDetailActivity extends Activity implements View.OnClickListener {

    private CommonToolbar mComBar;
    private ListView mListView;
    private Button mBtn;
    private InstructListAdapter mAdapter;
    private LEOAlarmDialog mBackupInstrDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_detail);
        initUI();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.addEvent(this,SDKWrapper.P1,"theft1","theft_order");
    }

    private void initUI() {
        mComBar = (CommonToolbar) findViewById(R.id.title_bar);
        mListView = (ListView) findViewById(R.id.instruct_list);
        mBtn = (Button) findViewById(R.id.secur_bt);
        mBtn.setOnClickListener(this);
    }

    private void initData() {
        mComBar.setToolbarTitle(R.string.secur_deta_title_bar);
        mComBar.setToolbarColorResource(R.color.ctc);
        ArrayList<InstructModel> instructs = loadInstructList();
        if (instructs != null) {
            mAdapter = new InstructListAdapter(this, instructs, InstructListAdapter.FLAG_INSTR_LIST);
        }
        mAdapter.setIsShowEndDetail(true);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.secur_bt:
                //backup instruct button
                backupInstructsDialog();
                SDKWrapper.addEvent(SecurityDetailActivity.this,SDKWrapper.P1,"theft1","theft_backup");
                break;
            default:
                break;
        }
    }

    // 加载指令列表数据
    private ArrayList<InstructModel> loadInstructList() {
        ArrayList<InstructModel> instructs = new ArrayList<InstructModel>();
        int oneKeyImage = R.drawable.theft_onekey;
        int oneKey = R.string.phone_security_instruct_onkey;
        int oneKeyContent = R.string.phone_security_instruct_onkey_description;
        instructs.add(new InstructModel(oneKeyImage, oneKeyContent, oneKey, false));

        int locateImage = R.drawable.theft_location;
        int locatePostion = R.string.phone_security_instruct_track;
        int locatePostionContent = R.string.phone_security_instruct_track_description;
        instructs.add(new InstructModel(locateImage, locatePostionContent, locatePostion, false));

        int alertImage = R.drawable.theft_alert;
        int alert = R.string.phone_security_instruct_alert;
        int alertContent = R.string.phone_security_instruct_alert_description;
        instructs.add(new InstructModel(alertImage, alertContent, alert, false));

        int alertOffImage = R.drawable.theft_closealert;
        int alertOff = R.string.phone_security_instruct_alert_off;
        int alertOffContent = R.string.phone_security_instruct_alert_off_description;
        instructs.add(new InstructModel(alertOffImage, alertOffContent, alertOff, false));

        int formateDataImage = R.drawable.theft_format;
        int formateData = R.string.phone_security_instruct_formatedata;
        int formateDataContent = R.string.phone_security_instruct_formatedata_description;
        instructs.add(new InstructModel(formateDataImage, formateDataContent, formateData, false));

        int lockImage = R.drawable.theft_lock;
        int lockPhone = R.string.phone_security_instruct_lock;
        int lockPhoneContent = R.string.phone_security_instruct_lock_description;
        instructs.add(new InstructModel(lockImage, lockPhoneContent, lockPhone, false));

        return instructs;
    }

    private void backupInstructsDialog() {

        if (mBackupInstrDialog == null) {
            mBackupInstrDialog = new LEOAlarmDialog(this);
            mBackupInstrDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mBackupInstrDialog != null) {
                        mBackupInstrDialog = null;
                    }

                }
            });
        }
        mBackupInstrDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                boolean isExistSim = mgr.getIsExistSim();
                if (isExistSim) {
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            final LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                            PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                            String numberNmae = lostMgr.getPhoneSecurityNumber();
                            if (!Utilities.isEmpty(numberNmae)) {
                                String[] number = numberNmae.split(":");
                                if (number != null) {
                                    mgr.sendMessage(number[1], getSendMessageInstructs(), MTKSendMsmHandler.BACKUP_SECUR_INSTRUCT_ID);
                                }
                            }
                            if (mBackupInstrDialog != null) {
                                mBackupInstrDialog.cancel();
                            }
                        }
                    });
                } else {
                    String failStr = SecurityDetailActivity.this.getResources().getString(
                            R.string.privacy_message_item_send_message_fail);
                    Toast.makeText(SecurityDetailActivity.this, failStr, Toast.LENGTH_SHORT).show();
                    if (mBackupInstrDialog != null) {
                        mBackupInstrDialog.cancel();
                    }
                }
            }
        });
        String content = getString(R.string.backup_instr_dialog_content);
        mBackupInstrDialog.setDialogIconVisibility(false);
        mBackupInstrDialog.setContent(content);
        mBackupInstrDialog.show();
    }

    /*获取发送短信的指令集介绍*/
    private String getSendMessageInstructs() {
        String content = getResources().getString(R.string.secur_backup_msm);
        return content;
    }

}
