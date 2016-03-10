package com.leo.appmaster.phoneSecurity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.intruderprotection.ShowToast;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.mgr.impl.PrivacyContactManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAnimationDialog;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class PhoneSecurityActivity extends BaseActivity implements OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "PhoneSecurityActivity";
    private static final int SCROLL_X = 0;
    private static final int SCROLL_Y = 0;

    /*底部进度数字*/
    public String[] mBottomNumber = new String[]{"1", "2", "3"};

    /*当前开启进度号*/
    private String mCurrentProcNumber = "1";
    private boolean isShowAdvProtectUi;
    /*是否为备份远程控制指令按钮*/
    private boolean isOpenSecurBt;
    /*当前开启进度号*/
    private boolean mCheckStatus, mAdvanProCheckStatus;

    private CommonToolbar mCommonBar;
    private SecurityNumberView mBottonNumberView1, mBottonNumberView2;
    private ListView mInstructLV;
    private GridView mNoOpenSucLV, mOpenSucLV, mNoOpenSecurLV;
    private List<InstructModel> mInstructs, mOpenInstrs, mNoOpenInstrs, mCompOpenInstrs;
    private InstructListAdapter mInstructAdapter;
    private Button mButton, mAddNumberBt, mModityNumberBt, mOpenSecurModifyBt;
    private CheckBox mCheckBox, mAdvanChekBox;
    private LinearLayout mOpenSecurLT, mNoAddSecurNumber, mAddSecurNumber, mSecurAdvaTipOpenLt, mExistSecurName;
    private RelativeLayout mSecurNoOpenTopRT, mSecurOpenRT;
    private ScrollView mAdvOpenRT, mSecurFinishRT;
    private TextView mAddSucNumberTip, mSecurName, mSecurNumber, mNoNameSecurNumber;
    private TextView mOpenSecurNameTv, mOpenSecurNumberTv;
    private ImageView mOperOnePoint, mOperTwoPoint, mOperFinish, mNoSecurNumTip;
    private LinearLayout mButPointLt;
    private TextView mDayTv, mHourTv, mFinishTipTv;
    private LinearLayout mNoAdvFinishTimeLt;
    private Button mOpenSecurAdvBt;
    private LEOAlarmDialog mBackupInstrDialog;
    private ImageView mFinishTipIV;
    private ImageView mShowProtTimeLt;
    private Button mHelpBt;
    private RelativeLayout mHelpRt, mSecurAddNumber;
    private ImageView mHelpIcon;
    private Animation mAddSecurNumberAnim;
    private RelativeLayout mHelpMsmPerRT;
    private Button mHelpFeedbackBt;
    private LinearLayout mKnowModelRt;
    private TextView mKnowMdContent;
    private TextView mKnowModelClick;
    private ScrollView mSecurPhNumCv;
    private LEOAnimationDialog mAdvanceTipDialog;

    private LEOAlarmDialog mShareDialog;
    private PreferenceTable mPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_security);
        initUI();
        initData();
    }

    @Override
    protected void onResume() {
        showUIHanlder();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mBottomNumber[1].equals(mCurrentProcNumber)) {
            returnToProcOne();
        } else {
            super.onBackPressed();
        }
    }

    /*界面UI显示处理*/
    private void showUIHanlder() {
        LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        /*如果未开启手机防盗*/
        boolean isSecurOpen = securMgr.isUsePhoneSecurity();
        /*是否添加了防盗号码*/
        String securNumber = securMgr.getPhoneSecurityNumber();

        String[] contact = null;
        if (!Utilities.isEmpty(securNumber)) {
            contact = securNumber.split(":");
        }
        if (isSecurOpen
                && !mBottomNumber[2].equals(mCurrentProcNumber)) {
            LeoLog.i(TAG, "开启了防盗");
            boolean isOpenAdv = securMgr.isOpenAdvanceProtect();
            if (!isOpenAdv) {
                loadInstructListData(true);
            } else {
                loadInstructListData(false);
            }
            mInstructAdapter.notifyDataSetInvalidated();
            setSecurShowUI(true, false, true, true, false, true, false, false);

            if (contact[0].equals(contact[1])) {
                mOpenSecurNumberTv.setVisibility(View.GONE);
                mOpenSecurNameTv.setText(contact[1]);
            } else {
                mOpenSecurNumberTv.setVisibility(View.VISIBLE);
                mOpenSecurNameTv.setText(contact[0]);
                mOpenSecurNumberTv.setText(contact[1]);
            }
            if (mButPointLt.getVisibility() == View.VISIBLE) {
                mButPointLt.setVisibility(View.GONE);
            }
            mButton.setText(getResources().getString(R.string.secur_open_bt));
            isOpenSecurBt = true;
            int[] protectTime = securMgr.getPhoneProtectTime();
            mDayTv.setText(String.valueOf(protectTime[0]));
            mHourTv.setText(String.valueOf(protectTime[1]));
            if (isOpenAdv) {
                mNoAdvFinishTimeLt.setVisibility(View.GONE);
                /*开启高级保护弹窗操作*/
                boolean isAdvOpen = PhoneSecurityManager.getInstance(this).isIsAdvOpenTip();
                if (isAdvOpen) {
                    openAdvanceProtectDialogTip();
                }
            } else {
                mNoAdvFinishTimeLt.setVisibility(View.VISIBLE);
               /*开启高级保护弹窗操作*/
                PhoneSecurityManager psm = PhoneSecurityManager.getInstance(PhoneSecurityActivity.this);
                if (psm.isIsAdvOpenTip()) {
                    psm.setIsAdvOpenTip(false);
                }
            }
            mNoAddSecurNumber.setVisibility(View.GONE);
            securTimeAnim(mShowProtTimeLt);
        } else if (contact != null && contact.length > 0) {
            LeoLog.i(TAG, "保存了防盗");
            if (mBottomNumber[1].equals(mCurrentProcNumber)) {
                /*开启高级保护弹窗操作*/
                PhoneSecurityManager psm = PhoneSecurityManager.getInstance(this);
                boolean isAdvTip = psm.isIsAdvOpenTip();
                boolean isOpenAdv = securMgr.isOpenAdvanceProtect();
                if (isAdvTip && isOpenAdv) {
                    openAdvanceProtectDialogTip();
                } else {
                    if (isOpenAdv) {
                        psm.setIsAdvOpenTip(false);
                    }
                }
                toProcTwo();
            } else if (mBottomNumber[2].equals(mCurrentProcNumber)) {
                toSecurFinish();
            } else {
                if (mButPointLt.getVisibility() == View.GONE) {
                    mButPointLt.setVisibility(View.VISIBLE);
                }
                setSecurShowUI(true, false, true, false, false, true, false, true);
                mAddSecurNumber.setVisibility(View.VISIBLE);
                if (contact[0].equals(contact[1])) {
                    mNoNameSecurNumber.setVisibility(View.VISIBLE);
                    mExistSecurName.setVisibility(View.GONE);
                    mNoNameSecurNumber.setText(contact[1]);
                } else {
                    mNoNameSecurNumber.setVisibility(View.GONE);
                    mExistSecurName.setVisibility(View.VISIBLE);
                    mSecurName.setText(contact[0]);
                    mSecurNumber.setText(contact[1]);
                }
                mButton.setText(getResources().getString(R.string.secur_bottom_bt_text));
            }
        } else {
            toProcOne();
        }
        mSecurPhNumCv.smoothScrollTo(SCROLL_X, SCROLL_Y);
    }

    /*进入设置第一步*/
    private void toProcOne() {
          /*1.开启防盗*/
        loadInstructListData(false);
        if (mButPointLt.getVisibility() == View.GONE) {
            mButPointLt.setVisibility(View.VISIBLE);
        }
        setSecurShowUI(true, false, true, false, true, false, false, true);
        mButton.setText(getResources().getString(R.string.secur_bottom_bt_text));
        LeoLog.i(TAG, "go to 1 !");
        SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_frt_arv");
    }

    /*初始化数据*/
    private void initData() {
        mCheckStatus = mCheckBox.isChecked();
        mAdvanProCheckStatus = mAdvanChekBox.isChecked();
        float textSize = getResources().getDimension(R.dimen.secur_bottom_text_size);
        mBottonNumberView1.setView(mBottomNumber[0], textSize, getResources().getColor(R.color.cb), getResources().getColor(R.color.white));
        mBottonNumberView2.setView(mBottomNumber[1], textSize, getResources().getColor(R.color.c5), getResources().getColor(R.color.white));
    }

    /*初始化UI*/
    private void initUI() {
        mCommonBar = (CommonToolbar) findViewById(R.id.phone_security_commonbar);
        mCommonBar.setToolbarTitle(R.string.phone_security_open);
        mCommonBar.setToolbarColorResource(R.color.cb);
        mCommonBar.setOptionMenuVisible(true);
        mCommonBar.setOptionImageResource(R.drawable.help_icon_n);
        mHelpIcon = mCommonBar.getOptionImageView();
        mCommonBar.setNavigationClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomNumber[1].equals(mCurrentProcNumber)) {
                    returnToProcOne();
                } else {
                    PhoneSecurityActivity.this.finish();
                }
            }
        });
        mCommonBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHelpRt.getVisibility() == View.VISIBLE) {
                    mHelpRt.setVisibility(View.GONE);
                    mHelpRt.startAnimation(AnimationUtils
                            .loadAnimation(PhoneSecurityActivity.this, R.anim.lock_mode_guide_out));
                } else {
                    unuseSecurHandler();
                    mHelpRt.setVisibility(View.VISIBLE);
                    mHelpRt.startAnimation(AnimationUtils
                            .loadAnimation(PhoneSecurityActivity.this, R.anim.lock_mode_guide_in));
                }
            }
        });
        mPreference = PreferenceTable.getInstance();

        mBottonNumberView1 = (SecurityNumberView) findViewById(R.id.phone_security_operation_one);
        mBottonNumberView2 = (SecurityNumberView) findViewById(R.id.phone_security_operation_two);
        mInstructLV = (ListView) findViewById(R.id.secur_instr_LV);
        mOpenSucLV = (GridView) findViewById(R.id.open_suc_LV);
        mNoOpenSucLV = (GridView) findViewById(R.id.no_open_suc_LV);
        mButton = (Button) findViewById(R.id.secur_bottom_BT);
        mButton.setOnClickListener(this);


        mAddNumberBt = (Button) findViewById(R.id.phone_security_add_number_BT);
        mCheckBox = (CheckBox) findViewById(R.id.phone_security_number_backup_CB);
        mCheckBox.setOnCheckedChangeListener(this);
        /*开启起防盗，操作的流程LT*/
        mOpenSecurLT = (LinearLayout) findViewById(R.id.security_phone_number_LT);
        /*高级保护功能，操作流程RT*/
        mAdvOpenRT = (ScrollView) findViewById(R.id.advance_security_RL);
        /*手机防盗'未打开'后顶部RT*/
        mSecurNoOpenTopRT = (RelativeLayout) findViewById(R.id.phone_security_top_RL);
        /*手机防盗'打开'后顶部RT*/
        mSecurOpenRT = (RelativeLayout) findViewById(R.id.secur_open_suc_RL);
        /*未添加防盗手机号LT*/
        mNoAddSecurNumber = (LinearLayout) findViewById(R.id.phone_security_no_add_number_LT);

        /*已经添加防盗手机号LT*/
        mAddSecurNumber = (LinearLayout) findViewById(R.id.phone_security_add_number_LT);
        mAddSucNumberTip = (TextView) findViewById(R.id.phone_security_add_number_title_TV);
        mSecurName = (TextView) findViewById(R.id.secur_name);
        mSecurNumber = (TextView) findViewById(R.id.secur_number);
        mNoNameSecurNumber = (TextView) findViewById(R.id.secur_no_name);
        mExistSecurName = (LinearLayout) findViewById(R.id.secur_exitst_name);
        /*修改号码按钮*/
        mModityNumberBt = (Button) findViewById(R.id.modify_secur_number_BT);
        mModityNumberBt.setOnClickListener(this);

        /*步骤1的点图*/
        mOperOnePoint = (ImageView) findViewById(R.id.secur_oper_one_point);
        /*步骤2的点图*/
        mOperTwoPoint = (ImageView) findViewById(R.id.secur_oper_two_point);
         /*步骤完成*/
        mOperFinish = (ImageView) findViewById(R.id.secur_oper_finish);
        /*未预留号码时感叹号提示*/
        mNoSecurNumTip = (ImageView) findViewById(R.id.phone_security_add_number_IV);
        /*高级保护CheckBox*/
        mAdvanChekBox = (CheckBox) findViewById(R.id.open_advance_CB);
        mAdvanChekBox.setOnCheckedChangeListener(this);
        /*手机防盗完成UI*/
        mSecurFinishRT = (ScrollView) findViewById(R.id.secur_finish_RL);
        mNoOpenSecurLV = (GridView) findViewById(R.id.secur_finish_LV);
        /*保护时间旁边的提示开启高级保护按钮*/
        mSecurAdvaTipOpenLt = (LinearLayout) findViewById(R.id.secur_open_tip_adv_LT);
        mOpenSecurNameTv = (TextView) findViewById(R.id.secur_number_name_TV);
        mOpenSecurNumberTv = (TextView) findViewById(R.id.secur_number_TV);
        mOpenSecurModifyBt = (Button) findViewById(R.id.secur_modify_bt);
        mOpenSecurModifyBt.setOnClickListener(this);

        mButPointLt = (LinearLayout) findViewById(R.id.security_number_LT);
        mDayTv = (TextView) findViewById(R.id.secur_day_TV);
        mHourTv = (TextView) findViewById(R.id.secur_hour_TV);
        mOpenSecurAdvBt = (Button) findViewById(R.id.open_sucur_adv_bt);
        mOpenSecurAdvBt.setOnClickListener(this);

        mNoAdvFinishTimeLt = (LinearLayout) findViewById(R.id.secur_open_tip_adv_LT);
        /*完成界面提示ui*/
        mFinishTipTv = (TextView) findViewById(R.id.secur_open_tip_TV);
        /*完成界面图提示UI*/
        mFinishTipIV = (ImageView) findViewById(R.id.secur_finish_IV);
        /*显示保护时间UI*/
        mShowProtTimeLt = (ImageView) findViewById(R.id.show_time_anim);
        /*手机防盗帮助页面，按钮UI*/
        mHelpBt = (Button) findViewById(R.id.help_bt);
        mHelpBt.setOnClickListener(this);
        /*帮助页显示*/
        mHelpRt = (RelativeLayout) findViewById(R.id.secur_help_RL);
        /*添加亲友号码UI*/
        mSecurAddNumber = (RelativeLayout) findViewById(R.id.secur_add_number_RT);
        mSecurAddNumber.setOnClickListener(this);
        /*帮助页短信权限提示*/
        mHelpMsmPerRT = (RelativeLayout) findViewById(R.id.no_know_model_help_tip_RT);
        mHelpFeedbackBt = (Button) findViewById(R.id.know_feekback_bt);
        mHelpFeedbackBt.setOnClickListener(this);
        //已知需要手动打开短信权限机型提示UI
        mKnowModelRt = (LinearLayout) findViewById(R.id.secur_know_msm_LT);
        mKnowMdContent = (TextView) findViewById(R.id.secur_know_msm_content);
        mKnowModelClick = (TextView) findViewById(R.id.secur_know_msm_click);
        mKnowModelClick.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mKnowModelClick.setOnClickListener(this);
        mSecurPhNumCv = (ScrollView) findViewById(R.id.secur_phone_nub_sc);
    }

    /*完全开启指令*/
    private void loadCompOpenInstructs() {
        if (mCompOpenInstrs == null) {
            mCompOpenInstrs = new ArrayList<InstructModel>();
        }
        mCompOpenInstrs.clear();
        int oneKeyImage = R.drawable.theft_onekey;
        int selectImage = R.drawable.wifi_complete;
        int oneKeyContent = R.string.onkey_name;
        mCompOpenInstrs.add(new InstructModel(oneKeyImage, selectImage, oneKeyContent));

        int locateImage = R.drawable.theft_location;
        int locatePostionContent = R.string.locat_name;
        mCompOpenInstrs.add(new InstructModel(locateImage, selectImage, locatePostionContent));

        int alertImage = R.drawable.theft_alert;
        int alertContent = R.string.alert_name;
        mCompOpenInstrs.add(new InstructModel(alertImage, selectImage, alertContent));

        int alertOffImage = R.drawable.theft_closealert;
        int alertOffContent = R.string.alert_off_name;
        mCompOpenInstrs.add(new InstructModel(alertOffImage, selectImage, alertOffContent));


        int formateDataImage = R.drawable.theft_format;
        int formateDataContent = R.string.formate_name;
        mCompOpenInstrs.add(new InstructModel(formateDataImage, selectImage, formateDataContent));

        int lockImage = R.drawable.theft_lock;
        int lockPhoneContent = R.string.lock_name;
        mCompOpenInstrs.add(new InstructModel(lockImage, selectImage, lockPhoneContent));

        mInstructAdapter = new InstructListAdapter(this, mCompOpenInstrs, InstructListAdapter.FLAG_OPEN_SUC_INSTR_LIST);
        mNoOpenSecurLV.setAdapter(mInstructAdapter);
    }

    /**
     * 加载指令列表数据
     */
    private void loadInstructListData(boolean flag) {

        if (mInstructs == null) {
            mInstructs = new ArrayList<InstructModel>();
        }
        mInstructs.clear();
        int oneKeyImage = R.drawable.theft_onekey;
        int oneKey = R.string.phone_security_instruct_onkey;
        int oneKeyContent = R.string.phone_security_instruct_onkey_description;
        boolean isSelect;
        if (flag) {
            isSelect = true;
        } else {
            isSelect = false;
        }
        mInstructs.add(new InstructModel(oneKeyImage, oneKeyContent, oneKey, isSelect));

        int locateImage = R.drawable.theft_location;
        int locatePostion = R.string.phone_security_instruct_track;
        int locatePostionContent = R.string.phone_security_instruct_track_description;
        mInstructs.add(new InstructModel(locateImage, locatePostionContent, locatePostion, false));

        int alertImage = R.drawable.theft_alert;
        int alert = R.string.phone_security_instruct_alert;
        int alertContent = R.string.phone_security_instruct_alert_description;
        mInstructs.add(new InstructModel(alertImage, alertContent, alert, false));

        int alertOffImage = R.drawable.theft_closealert;
        int alertOff = R.string.phone_security_instruct_alert_off;
        int alertOffContent = R.string.phone_security_instruct_alert_off_description;
        mInstructs.add(new InstructModel(alertOffImage, alertOffContent, alertOff, false));

        int formateDataImage = R.drawable.theft_format;
        int formateData = R.string.phone_security_instruct_formatedata;
        int formateDataContent = R.string.phone_security_instruct_formatedata_description;

        mInstructs.add(new InstructModel(formateDataImage, formateDataContent, formateData, isSelect));

        int lockImage = R.drawable.theft_lock;
        int lockPhone = R.string.phone_security_instruct_lock;
        int lockPhoneContent = R.string.phone_security_instruct_lock_description;
        mInstructs.add(new InstructModel(lockImage, lockPhoneContent, lockPhone, false));
        mInstructAdapter = new InstructListAdapter(this, mInstructs, InstructListAdapter.FLAG_INSTR_LIST);
        mInstructLV.setAdapter(mInstructAdapter);

    }

    /*加载已开启的防盗指令数据*/
    private void loadOpenInstructData() {
        if (mOpenInstrs == null) {
            mOpenInstrs = new ArrayList<InstructModel>();
        }
        mOpenInstrs.clear();
        int lockImage = R.drawable.theft_lock;
        int lockContent = R.string.lock_name;
        int selectImage = R.drawable.wifi_complete;
        mOpenInstrs.add(new InstructModel(lockImage, selectImage, lockContent));

        int locateImage = R.drawable.theft_location;
        int locateContent = R.string.locat_name;
        mOpenInstrs.add(new InstructModel(locateImage, selectImage, locateContent));

        int alertImage = R.drawable.theft_alert;
        int alertContent = R.string.alert_name;
        mOpenInstrs.add(new InstructModel(alertImage, selectImage, alertContent));


        int alertOffImage = R.drawable.theft_closealert;
        int alertOffContent = R.string.alert_off_name;
        mOpenInstrs.add(new InstructModel(alertOffImage, selectImage, alertOffContent));

        mInstructAdapter = new InstructListAdapter(this, mOpenInstrs, InstructListAdapter.FLAG_OPEN_SUC_INSTR_LIST);
        mOpenSucLV.setAdapter(mInstructAdapter);

    }

    /**
     * 加载未开启的防盗指令数据
     *
     * @param flag,false:记载完成界面ui，true：加载高级保护界面ui
     */
    private void loadNoOpenInstructData(boolean flag) {
        if (mNoOpenInstrs == null) {
            mNoOpenInstrs = new ArrayList<InstructModel>();
        }
        mNoOpenInstrs.clear();
        int selectImage = R.drawable.wifi_error;
        int oneKeyImage = R.drawable.theft_onekey;
        int oneKeyContent = R.string.onkey_name;
        mNoOpenInstrs.add(new InstructModel(oneKeyImage, selectImage, oneKeyContent));

        int formateImage = R.drawable.theft_format;
        int formateContent = R.string.formate_name;
        mNoOpenInstrs.add(new InstructModel(formateImage, selectImage, formateContent));
        if (flag) {
            mInstructAdapter = new InstructListAdapter(this, mNoOpenInstrs, InstructListAdapter.FLAG_OPEN_SUC_INSTR_LIST);
            mNoOpenSucLV.setAdapter(mInstructAdapter);
        } else {
            mInstructAdapter = new InstructListAdapter(this, mNoOpenInstrs, InstructListAdapter.FLAG_OPEN_SUC_INSTR_LIST);
            mNoOpenSecurLV.setAdapter(mInstructAdapter);
        }
    }


    /**
     * 设置手机防盗的UI显示情况
     *
     * @param flag1          开启起防盗，操作的流程LT,显示状态
     * @param flag2          高级保护功能，操作流程RT,显示状态
     * @param flag3          手机防盗'未打开'后顶部RT,显示状态
     * @param flag4          手机防盗'打开'后顶部RT,显示状态
     * @param flag5          添加防盗手机号LT,显示状态
     * @param flag6          已经添加防盗手机号LT,显示状态
     * @param flag7          手机防盗完成页面,显示状态
     * @param isSendMsmCheck 备份短信指令
     */
    private void setSecurShowUI(boolean flag1, boolean flag2, boolean flag3, boolean flag4, boolean flag5, boolean flag6, boolean flag7, boolean isSendMsmCheck) {
        mAdvanChekBox.setVisibility(View.GONE);
        /*开启起防盗，操作的流程LT,显示状态*/
        if (flag1) {
            mOpenSecurLT.setVisibility(View.VISIBLE);
        } else {
            mOpenSecurLT.setVisibility(View.INVISIBLE);
        }

        /*高级保护功能，操作流程RT,显示状态*/
        if (flag2) {
            mAdvOpenRT.setVisibility(View.VISIBLE);
            mAdvanChekBox.setVisibility(View.VISIBLE);
        } else {
            mAdvOpenRT.setVisibility(View.INVISIBLE);
        }

        /*手机防盗'未打开'后顶部RT,显示状态*/
        if (flag3) {
            mSecurNoOpenTopRT.setVisibility(View.VISIBLE);
        } else {
            mSecurNoOpenTopRT.setVisibility(View.GONE);
        }
        /*手机防盗'打开'后顶部RT,显示状态*/
        if (flag4) {
            mSecurOpenRT.setVisibility(View.VISIBLE);
        } else {
            mSecurOpenRT.setVisibility(View.GONE);
        }
        /*未添加防盗手机号LT,显示状态*/
        if (flag5) {
            mNoAddSecurNumber.setVisibility(View.VISIBLE);
        } else {
            mNoAddSecurNumber.setVisibility(View.INVISIBLE);
        }
        /*已经添加防盗手机号LT,显示状态*/
        if (flag6) {
            mAddSecurNumber.setVisibility(View.VISIBLE);
        } else {
            mAddSecurNumber.setVisibility(View.INVISIBLE);
        }
        /*手机防盗完成页面*/
        if (flag7) {
            mSecurFinishRT.setVisibility(View.VISIBLE);
        } else {
            mSecurFinishRT.setVisibility(View.INVISIBLE);
        }
        /*备份短信指令checkbox*/
        if (isSendMsmCheck) {
            mCheckBox.setVisibility(View.VISIBLE);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.secur_bottom_BT:
                final LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                if (!isOpenSecurBt) {
                    String securNumber = lostMgr.getPhoneSecurityNumber();
                    if (mBottomNumber[0].equals(mCurrentProcNumber)) {
                        /**第一步*/
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_frt_next");
                        toProcTwo();
                    } else if (mBottomNumber[1].equals(mCurrentProcNumber)) {
                        /**第二步*/
                        boolean isOpenProtect = lostMgr.isOpenAdvanceProtect();
                        if (mAdvanProCheckStatus && !isOpenProtect) {
                            startDeviceItent(true);
                        } else {
                            toSecurFinish();
                        }
                        PhoneSecurityManager psm = PhoneSecurityManager.getInstance(this);
                        psm.setIsAdvOpenTip(true);
                        /*打点上报*/
                        if (mAdvanProCheckStatus) {
                            SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_snd_protect");
                        } else {
                            SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_snd_noProtect");
                        }
                    } else if (mBottomNumber[2].equals(mCurrentProcNumber)) {
                        /**第三步*/
                        mSecurPhNumCv.smoothScrollTo(SCROLL_X, SCROLL_Y);
                        boolean isOpenAdv = lostMgr.isOpenAdvanceProtect();
                        if (!isOpenAdv) {
                            loadInstructListData(true);
                        } else {
                            loadInstructListData(false);
                        }
                        mInstructAdapter.notifyDataSetInvalidated();
                        if (isOpenAdv) {
                            mNoAdvFinishTimeLt.setVisibility(View.GONE);
                        } else {
                            mNoAdvFinishTimeLt.setVisibility(View.VISIBLE);
                        }
                       /*是否添加了防盗号码*/
                        String[] contact = null;
                        if (!Utilities.isEmpty(securNumber)) {
                            contact = securNumber.split(":");
                        }
                        setSecurShowUI(true, false, true, true, false, true, false, false);
                        if (contact[0].equals(contact[1])) {
                            mOpenSecurNumberTv.setVisibility(View.GONE);
                            mOpenSecurNameTv.setText(contact[1]);
                        } else {
                            mOpenSecurNumberTv.setVisibility(View.VISIBLE);
                            mOpenSecurNameTv.setText(contact[0]);
                            mOpenSecurNumberTv.setText(contact[1]);
                        }
                        if (mButPointLt.getVisibility() == View.VISIBLE) {
                            mButPointLt.setVisibility(View.GONE);
                        }
                        mButton.setText(getResources().getString(R.string.secur_open_bt));
                        isOpenSecurBt = true;
                        int[] protectTime = lostMgr.getPhoneProtectTime();
                        mDayTv.setText(String.valueOf(protectTime[0]));
                        mHourTv.setText(String.valueOf(protectTime[1]));
                        if (!mBottomNumber[0].equals(mCurrentProcNumber)) {
                            mCurrentProcNumber = mBottomNumber[0];
                        }
                        mNoAddSecurNumber.setVisibility(View.GONE);
                        securTimeAnim(mShowProtTimeLt);
                    }
                } else {
                    backupInstructsDialog();
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_backup");
                }
                break;
            case R.id.secur_add_number_RT:
                startAddSecurNumberIntent();
                break;
            case R.id.modify_secur_number_BT:
                startAddSecurNumberIntent();
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft_use", "theft_cnts_changeContact");
                break;
            case R.id.secur_modify_bt:
                startAddSecurNumberIntent();
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft_use", "theft_cnts_changeContact");
                break;
            case R.id.open_sucur_adv_bt:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_instant");
                startDeviceItent(false);
                PhoneSecurityManager.getInstance(PhoneSecurityActivity.this).setIsAdvOpenTip(true);
                break;
            case R.id.help_bt:
                if (mHelpRt.getVisibility() == View.VISIBLE) {
                    mHelpRt.setVisibility(View.GONE);
                    mHelpRt.startAnimation(AnimationUtils
                            .loadAnimation(PhoneSecurityActivity.this, R.anim.lock_mode_guide_out));
                    Animation animation =
                            AnimationUtils.loadAnimation(PhoneSecurityActivity.this,
                                    R.anim.help_tip_show);
                    mHelpIcon.startAnimation(animation);
                } else {
                    unuseSecurHandler();
                    mHelpRt.setVisibility(View.VISIBLE);
                    mHelpRt.startAnimation(AnimationUtils
                            .loadAnimation(PhoneSecurityActivity.this, R.anim.lock_mode_guide_in));
                }
                break;
            case R.id.know_feekback_bt:
                Intent intent = new Intent(this, FeedbackActivity.class);
                intent.putExtra(PhoneSecurityConstants.SECUR_HELP_TO_FEEDBACK, true);
                startActivity(intent);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_feedback_clk_$" + BuildProperties.getPoneModel());
                break;
            case R.id.secur_know_msm_click:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_authoriz_clk");
                /*点击立即授权*/
                new MsmPermisGuideList().executeGuide();
                break;
            default:
                break;
        }
    }

    /*进入到添加防盗号码的电话列表*/
    private void startAddSecurNumberIntent() {
        Intent intent = new Intent(PhoneSecurityActivity.this, AddSecurityNumberActivity.class);
        startActivity(intent);
    }

    /*获取发送短信的指令集介绍*/
    private String getSendMessageInstructs() {
        String content = getResources().getString(R.string.secur_backup_msm);
        return content;
    }

    /*跳转到设备管理器*/
    private void startDeviceItent(boolean flag) {
        LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean isOpPro = lostMgr.isOpenAdvanceProtect();
        LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        Intent intent = null;
        ComponentName component = new ComponentName(this,
                DeviceReceiver.class);
        mLockManager.filterSelfOneMinites();
        mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
        intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                component);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.device_admin_extra));
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean isOpenAdvan = lostMgr.isOpenAdvanceProtect();
        if (flag) {
            /*进入开启设备管理器并且激活才进入开启步骤3,否则为2*/
            if (isOpenAdvan) {
                mCurrentProcNumber = mBottomNumber[2];
            }
        }
        if (isOpenAdvan) {
            mFinishTipTv.setText(getResources().getString(R.string.secur_finish_complate_text));
            mFinishTipIV.setImageResource(R.drawable.theft_complete_img);
        } else {
            mFinishTipTv.setText(getResources().getString(R.string.secur_finish_no_complate_tip));
            mFinishTipIV.setImageResource(R.drawable.theft_function_img);
        }
    }

    /*进入完成页面*/
    private void toSecurFinish() {
        LeoLog.i(TAG, "go to finish ！");
        SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_suc_arv");
        frmScanHandler();
        LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean isOpenAdvan = lostMgr.isOpenAdvanceProtect();
        if (isOpenAdvan) {
            loadCompOpenInstructs();
        } else {
            loadNoOpenInstructData(false);
        }
        mBottonNumberView2.setViewBackGroundColor(getResources().getColor(R.color.cb));
        mCurrentProcNumber = mBottomNumber[2];
        setSecurShowUI(false, false, false, false, false, false, true, false);
        mOperFinish.setImageResource(R.drawable.theft_step);
        mAdvanChekBox.setVisibility(View.GONE);
        mOperTwoPoint.setImageResource(R.drawable.theft_step_point);
        mCommonBar.setToolbarTitle(R.string.secur_open_suc);
        mButton.setText(getResources().getString(R.string.secur_finish_bt_text));
        if (isOpenAdvan) {
            mFinishTipTv.setText(getResources().getString(R.string.secur_finish_complate_text));
            mFinishTipIV.setImageResource(R.drawable.theft_complete_img);
        } else {
            mFinishTipTv.setText(getResources().getString(R.string.secur_finish_no_complate_tip));
            mFinishTipIV.setImageResource(R.drawable.theft_function_img);
            showShareDialog();
        }
        LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        /*设置手机防盗为开启状态*/
        mgr.setUsePhoneSecurity(true);
        /*设置开启保护的时间*/
        mgr.setOpenSecurityTime();

        PhoneSecurityManager psm = PhoneSecurityManager.getInstance(this);
        if (psm.isIsAdvOpenTip()) {
            psm.setIsAdvOpenTip(false);
        }
    }

    /*来自扫描页面,加分提示处理*/
    private void frmScanHandler() {
        PhoneSecurityManager psm = PhoneSecurityManager.getInstance(this);
        boolean isFromScanTmp = psm.getIsFromScan();
        if (isFromScanTmp) {
            ShowToast.showGetScoreToast(PhoneSecurityConstants.PHONE_SECURITY_SCORE, this);
        }
    }


    /*保护时间动画*/
    private void securTimeAnim(View view) {
        view.clearAnimation();
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.secur_protec_time_anim);
        view.startAnimation(operatingAnim);

    }

    /*到开启步骤2*/
    private void toProcTwo() {
        loadNoOpenInstructData(true);
        loadOpenInstructData();
        final LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean isOpenProtect = lostMgr.isOpenAdvanceProtect();
        String securNumber = lostMgr.getPhoneSecurityNumber();
        boolean isExistSecurNumber = false;
        if (mBottomNumber[0].equals(mCurrentProcNumber)) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    if (mCheckStatus) {
                        PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                        String numberNmae = lostMgr.getPhoneSecurityNumber();
                        if (!Utilities.isEmpty(numberNmae)) {
                            String[] number = numberNmae.split(":");
                            if (number != null) {
                                mgr.sendMessage(number[1], getSendMessageInstructs(),MTKSendMsmHandler.BACKUP_SECUR_INSTRUCT_ID);
                            }
                        }
                    }
                }
            });
        }
        if (!Utilities.isEmpty(securNumber)) {
            isExistSecurNumber = true;
        }
        if (isExistSecurNumber) {
            if (isOpenProtect) {
               /*进入完成界面*/
                toSecurFinish();
            } else {
                mOperTwoPoint.setImageResource(R.drawable.theft_step_point);
                mBottonNumberView2.setViewBackGroundColor(getResources().getColor(R.color.cb));
                setSecurShowUI(false, true, false, false, false, false, false, false);
                isShowAdvProtectUi = true;
                mCurrentProcNumber = mBottomNumber[1];
                mCommonBar.setToolbarTitle(R.string.secur_advan_title);
                mAdvanChekBox.setVisibility(View.VISIBLE);
                LeoLog.i(TAG, "go to 2 !");
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_snd_arv");
            }
        } else {
            shakeAddSecurButton(mAddNumberBt);
            PhoneSecurityUtils.setVibrate(PhoneSecurityActivity.this, 200);
            String toastTip = getResources().getString(R.string.no_add_secur_number_toast_tip);
            mNoSecurNumTip.setVisibility(View.VISIBLE);
            Toast.makeText(PhoneSecurityActivity.this, toastTip, Toast.LENGTH_SHORT).show();
            SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_frt_nextNoTel");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.phone_security_number_backup_CB:
                mCheckStatus = isChecked;
                break;
            case R.id.open_advance_CB:
                mAdvanProCheckStatus = isChecked;
                break;
        }
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
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        final LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                        PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                        String numberNmae = lostMgr.getPhoneSecurityNumber();
                        if (!Utilities.isEmpty(numberNmae)) {
                            String[] number = numberNmae.split(":");
                            if (number != null) {
                                mgr.sendMessage(number[1], getSendMessageInstructs(),MTKSendMsmHandler.BACKUP_SECUR_INSTRUCT_ID);
                            }
                        }
                        if (mBackupInstrDialog != null) {
                            mBackupInstrDialog.cancel();
                        }
                    }
                });
            }
        });
        String content = getString(R.string.backup_instr_dialog_content);
        mBackupInstrDialog.setDialogIconVisibility(false);
        mBackupInstrDialog.setContent(content);
        mBackupInstrDialog.show();
    }

    /*从步骤2返回到步骤1*/
    private void returnToProcOne() {
        mCommonBar.setToolbarTitle(R.string.phone_security_open);
        mOperTwoPoint.setImageResource(R.drawable.theft_step_point_dis);
        mBottonNumberView2.setViewBackGroundColor(getResources().getColor(R.color.c5));
        LostSecurityManagerImpl securityManager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                     /*是否添加了防盗号码*/
        String securNumber = securityManager.getPhoneSecurityNumber();
        String[] contact = null;
        if (!Utilities.isEmpty(securNumber)) {
            contact = securNumber.split(":");
        }
        if (mButPointLt.getVisibility() == View.GONE) {
            mButPointLt.setVisibility(View.VISIBLE);
        }
        setSecurShowUI(true, false, true, false, false, true, false, true);
        mAddSecurNumber.setVisibility(View.VISIBLE);
        if (contact != null) {
            if (contact[0].equals(contact[1])) {
                mNoNameSecurNumber.setVisibility(View.VISIBLE);
                mExistSecurName.setVisibility(View.GONE);
                mNoNameSecurNumber.setText(contact[1]);
            } else {
                mNoNameSecurNumber.setVisibility(View.GONE);
                mExistSecurName.setVisibility(View.VISIBLE);
                mSecurName.setText(contact[0]);
                mSecurNumber.setText(contact[1]);
            }
        }
        mButton.setText(getResources().getString(R.string.secur_bottom_bt_text));
        mCurrentProcNumber = "1";
        mAdvanChekBox.setVisibility(View.GONE);
        LeoLog.i(TAG, "go to 1 !");
        SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_frt_arv");
    }

    /*未填写防盗号码，摇晃动画*/
    private void shakeAddSecurButton(View view) {

        if (mAddSecurNumberAnim == null) {
            mAddSecurNumberAnim = AnimationUtils.loadAnimation(this,
                    R.anim.left_right_shake);
        }
        view.startAnimation(mAddSecurNumberAnim);
    }

    /*防盗不能使用处理*/
    private void unuseSecurHandler() {
        /*是否为已知需要手动开启权限的机型*/
        int result = new MsmPermisGuideList().isMsmPermisListModel(this);
        boolean isKnowMsmPerModel = false;
        if (result >= 0) {
            isKnowMsmPerModel = true;
        }
        if (!isKnowMsmPerModel) {
            boolean isSamsung = BuildProperties.isSamSungModel();
            if (!isSamsung) {
                mHelpMsmPerRT.setVisibility(View.VISIBLE);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_feedback");
            } else {
                mHelpMsmPerRT.setVisibility(View.GONE);
            }
            mKnowModelRt.setVisibility(View.GONE);
        } else {
            mKnowModelRt.setVisibility(View.VISIBLE);
            mHelpMsmPerRT.setVisibility(View.GONE);
            String content = new MsmPermisGuideList().getModelString();
            mKnowMdContent.setText(content);
        }
    }

    /*开启高级保护弹窗*/
    private void openAdvanceProtectDialogTip() {
        if (mAdvanceTipDialog == null) {
            mAdvanceTipDialog = new LEOAnimationDialog(this);
            mAdvanceTipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mAdvanceTipDialog != null) {
                        mAdvanceTipDialog = null;
                        showShareDialog();
                    }
                }
            });
        }
        String content = getString(R.string.prot_open_suc_tip_cnt);
        mAdvanceTipDialog.setContent(content);
        mAdvanceTipDialog.show();
        PhoneSecurityManager.getInstance(PhoneSecurityActivity.this).setIsAdvOpenTip(false);
    }

    private void showShareDialog() {
       if (mPreference.getBoolean(PrefConst.PHONE_SECURITY_SHOW, false)) {
           return;
       }
       if (mShareDialog == null) {
           mShareDialog = new LEOAlarmDialog(PhoneSecurityActivity.this);
           mShareDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
               @Override
               public void onDismiss(DialogInterface dialog) {
                   if (mShareDialog != null) {
                       mShareDialog = null;
                   }
               }
           });
       }
       String content = getString(R.string.phone_share_dialog_content);
       String shareButton = getString(R.string.share_dialog_btn_query);
       String cancelButton = getString(R.string.share_dialog_query_btn_cancel);
       mShareDialog.setContent(content);
       mShareDialog.setLeftBtnStr(cancelButton);
       mShareDialog.setRightBtnStr(shareButton);
       mShareDialog.setLeftBtnListener(new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialogInterface, int i) {
               SDKWrapper.addEvent(PhoneSecurityActivity.this, SDKWrapper.P1, "theft", "theft_noShare");
               if (mShareDialog != null && mShareDialog.isShowing()) {
                   mShareDialog.dismiss();
                   mShareDialog = null;
               }
           }
       });
       mShareDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialogInterface, int i) {
               if (mShareDialog != null && mShareDialog.isShowing()) {
                   mShareDialog.dismiss();
                   mShareDialog = null;
               }
               shareApps();
           }
       });
       mShareDialog.show();
       mPreference.putBoolean(PrefConst.PHONE_SECURITY_SHOW, true);
    }

    /** 分享应用 */
    private void shareApps() {
        SDKWrapper.addEvent(PhoneSecurityActivity.this, SDKWrapper.P1, "theft", "theft_share");
        mLockManager.filterSelfOneMinites();
        PreferenceTable sharePreferenceTable = PreferenceTable.getInstance();
        boolean isContentEmpty = TextUtils.isEmpty(
                sharePreferenceTable.getString(PrefConst.KEY_PHONE_SHARE_CONTENT));
        boolean isUrlEmpty = TextUtils.isEmpty(
                sharePreferenceTable.getString(PrefConst.KEY_PHONE_SHARE_URL));

        StringBuilder shareBuilder = new StringBuilder();
        if (!isContentEmpty && !isUrlEmpty) {
            shareBuilder.append(sharePreferenceTable.getString(PrefConst.KEY_PHONE_SHARE_CONTENT))
                        .append(" ")
                        .append(sharePreferenceTable.getString(PrefConst.KEY_PHONE_SHARE_URL));
        } else {
            shareBuilder.append(getResources().getString(R.string.phone_share_content))
                        .append(" ")
                        .append(Constants.DEFAULT_SHARE_URL);
        }
        Utilities.toShareApp(shareBuilder.toString(), getTitle().toString(), PhoneSecurityActivity.this);
    }

}
