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
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
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
    private CommonToolbar mCommonBar;
    private RelativeLayout mSecurOpenRT;
    private LEOAlarmDialog mBackupInstrDialog;
    private RelativeLayout mSecurProtTip;
    private Button mHelpBt;
    private RelativeLayout mHelpRt;
    private ImageView mHelpIcon;
    private Animation mAddSecurNumberAnim;
    private RelativeLayout mHelpMsmPerRT;
    private Button mHelpFeedbackBt;
    private LinearLayout mKnowModelRt;
    private TextView mKnowMdContent;
    private TextView mKnowModelClick;
    private LEOAnimationDialog mAdvanceTipDialog;
    private LEOAlarmDialog mShareDialog;
    private ImageView mShowProtTimeLt;
    private TextView mDayTv;
    private TextView mHourTv;
    private ScrollView mSecurPhNumCv;
    private GridView mNormalGv;
    private GridView mAdvGv;
    private InstructListAdapter mNormalAdapter;
    private InstructListAdapter mAdvAdapter;
    private LEOAlarmDialog mSecurCloseDialg;
    private Button mCloseBt;
    private TextView mInstruTipTv;
    private Button mAdvBt;
    private Button mSecurNumModyBt;

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
        LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean isOpPro = lostMgr.isOpenAdvanceProtect();
        if (isOpPro) {
            List<InstructModel> instrData = loadAdvOpenInstructData(true);
            if (instrData != null && instrData.size() > 0) {
                mAdvAdapter.setData(instrData);
                mAdvAdapter.notifyDataSetChanged();
            }
            mAdvBt.setVisibility(View.GONE);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*界面UI显示处理*/
    private void showUIHanlder() {
        LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        int[] protectTime = securMgr.getPhoneProtectTime();
        mDayTv.setText(String.valueOf(protectTime[0]));
        mHourTv.setText(String.valueOf(protectTime[1]));
        securTimeAnim(mShowProtTimeLt);
//        mSecurPhNumCv.smoothScrollTo(SCROLL_X, SCROLL_Y);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        List<InstructModel> normalInstrs = loadNormalInstructData();
        if (normalInstrs != null && normalInstrs.size() > 0) {
            mNormalAdapter = new InstructListAdapter(this, normalInstrs, InstructListAdapter.FLAG_OPEN_SUC_INSTR_LIST);
            mNormalGv.setAdapter(mNormalAdapter);
        }
        List<InstructModel> advInstrs = loadAdvOpenInstructData(false);
        if (advInstrs != null && advInstrs.size() > 0) {
            mAdvAdapter = new InstructListAdapter(this, advInstrs, InstructListAdapter.FLAG_OPEN_SUC_INSTR_LIST);
            mAdvGv.setAdapter(mAdvAdapter);
        }
    }

    /**
     * 初始化UI
     */
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
                PhoneSecurityActivity.this.finish();
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

        //TODO 界面更新
        mSecurOpenRT = (RelativeLayout) findViewById(R.id.show_time_lt);
        /*显示保护时间UI*/
        mShowProtTimeLt = (ImageView) mSecurOpenRT.findViewById(R.id.show_time_anim);
        mSecurProtTip = (RelativeLayout) mSecurOpenRT.findViewById(R.id.secur_prot_rl);
        mDayTv = (TextView) mSecurProtTip.findViewById(R.id.secur_day_TV);
        mHourTv = (TextView) mSecurProtTip.findViewById(R.id.secur_hour_TV);
        /*手机防盗帮助页面，按钮UI*/
        mHelpBt = (Button) findViewById(R.id.help_bt);
        mHelpBt.setOnClickListener(this);
        /*帮助页显示*/
        mHelpRt = (RelativeLayout) findViewById(R.id.secur_help_RL);
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

        mNormalGv = (GridView) findViewById(R.id.secur_open_normal_lv);
        mAdvGv = (GridView) findViewById(R.id.secur_open_adv_lv);
        mCloseBt = (Button) findViewById(R.id.secur_colse_bt);
        mCloseBt.setOnClickListener(this);
        mInstruTipTv = (TextView) findViewById(R.id.secur_instru_tip_tv);
        mInstruTipTv.setOnClickListener(this);
        mAdvBt = (Button) findViewById(R.id.secur_open_adv);
        mAdvBt.setOnClickListener(this);
        mSecurNumModyBt = (Button) findViewById(R.id.secur_mody_bt);
        mSecurNumModyBt.setOnClickListener(this);
    }

    /**
     * 加载防盗指令基本功能数据
     */
    private List<InstructModel> loadNormalInstructData() {
        List<InstructModel> openInstrs = new ArrayList<InstructModel>();
        int lockImage = R.drawable.theft_lock;
        int lockContent = R.string.lock_name;
        int selectImage = R.drawable.wifi_complete;
        openInstrs.add(new InstructModel(lockImage, selectImage, lockContent));

        int locateImage = R.drawable.theft_location;
        int locateContent = R.string.locat_name;
        openInstrs.add(new InstructModel(locateImage, selectImage, locateContent));

        int alertImage = R.drawable.theft_alert;
        int alertContent = R.string.alert_name;
        openInstrs.add(new InstructModel(alertImage, selectImage, alertContent));


        int alertOffImage = R.drawable.theft_closealert;
        int alertOffContent = R.string.alert_off_name;
        openInstrs.add(new InstructModel(alertOffImage, selectImage, alertOffContent));

        return openInstrs;
    }

    /**
     * 加载防盗高级功能数据
     */
    private List<InstructModel> loadAdvOpenInstructData(boolean isOpenAdv) {
        List<InstructModel> noOpenInstrs = new ArrayList<InstructModel>();

        int selectImage = -1;
        if (isOpenAdv) {
            selectImage = R.drawable.wifi_complete;
        } else {
            selectImage = R.drawable.wifi_error;
        }
        int oneKeyImage = R.drawable.theft_onekey;
        int oneKeyContent = R.string.onkey_name;
        noOpenInstrs.add(new InstructModel(oneKeyImage, selectImage, oneKeyContent));

        int formateImage = R.drawable.theft_format;
        int formateContent = R.string.formate_name;
        noOpenInstrs.add(new InstructModel(formateImage, selectImage, formateContent));
        return noOpenInstrs;
    }

//
//    /**
//     * 设置手机防盗的UI显示情况
//     *
//     * @param flag1          开启起防盗，操作的流程LT,显示状态
//     * @param flag2          高级保护功能，操作流程RT,显示状态
//     * @param flag3          手机防盗'未打开'后顶部RT,显示状态
//     * @param flag4          手机防盗'打开'后顶部RT,显示状态
//     * @param flag5          添加防盗手机号LT,显示状态
//     * @param flag6          已经添加防盗手机号LT,显示状态
//     * @param flag7          手机防盗完成页面,显示状态
//     * @param isSendMsmCheck 备份短信指令
//     */
//    private void setSecurShowUI(boolean flag1, boolean flag2, boolean flag3, boolean flag4, boolean flag5, boolean flag6, boolean flag7, boolean isSendMsmCheck) {
//        mAdvanChekBox.setVisibility(View.GONE);
//        /*开启起防盗，操作的流程LT,显示状态*/
//        if (flag1) {
//            mOpenSecurLT.setVisibility(View.VISIBLE);
//        } else {
//            mOpenSecurLT.setVisibility(View.INVISIBLE);
//        }
//
//        /*高级保护功能，操作流程RT,显示状态*/
//        if (flag2) {
//            mAdvOpenRT.setVisibility(View.VISIBLE);
//            mAdvanChekBox.setVisibility(View.VISIBLE);
//        } else {
//            mAdvOpenRT.setVisibility(View.INVISIBLE);
//        }
//
//        /*手机防盗'未打开'后顶部RT,显示状态*/
//        if (flag3) {
//            mSecurNoOpenTopRT.setVisibility(View.VISIBLE);
//        } else {
//            mSecurNoOpenTopRT.setVisibility(View.GONE);
//        }
//        /*手机防盗'打开'后顶部RT,显示状态*/
//        if (flag4) {
//            mSecurOpenRT.setVisibility(View.VISIBLE);
//        } else {
//            mSecurOpenRT.setVisibility(View.GONE);
//        }
//        /*未添加防盗手机号LT,显示状态*/
//        if (flag5) {
//            mNoAddSecurNumber.setVisibility(View.VISIBLE);
//        } else {
//            mNoAddSecurNumber.setVisibility(View.INVISIBLE);
//        }
//        /*已经添加防盗手机号LT,显示状态*/
//        if (flag6) {
//            mAddSecurNumber.setVisibility(View.VISIBLE);
//        } else {
//            mAddSecurNumber.setVisibility(View.INVISIBLE);
//        }
//        /*手机防盗完成页面*/
//        if (flag7) {
//            mSecurFinishRT.setVisibility(View.VISIBLE);
//        } else {
//            mSecurFinishRT.setVisibility(View.INVISIBLE);
//        }
//        /*备份短信指令checkbox*/
//        if (isSendMsmCheck) {
//            mCheckBox.setVisibility(View.VISIBLE);
//        } else {
//            mCheckBox.setVisibility(View.GONE);
//        }
//
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.secur_bottom_BT:
//                final LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//                if (!isOpenSecurBt) {
//                    String securNumber = lostMgr.getPhoneSecurityNumber();
//                    if (mBottomNumber[0].equals(mCurrentProcNumber)) {
//                        /**第一步*/
//                        SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_frt_next");
//                        toProcTwo();
//                    } else if (mBottomNumber[1].equals(mCurrentProcNumber)) {
//                        /**第二步*/
//                        boolean isOpenProtect = lostMgr.isOpenAdvanceProtect();
//                        if (mAdvanProCheckStatus && !isOpenProtect) {
//                            startDeviceItent(true);
//                        } else {
//                            toSecurFinish();
//                        }
//                        PhoneSecurityManager psm = PhoneSecurityManager.getInstance(this);
//                        psm.setIsAdvOpenTip(true);
//                        /*打点上报*/
//                        if (mAdvanProCheckStatus) {
//                            SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_snd_protect");
//                        } else {
//                            SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_snd_noProtect");
//                        }
//                    } else if (mBottomNumber[2].equals(mCurrentProcNumber)) {
//                        /**第三步*/
//                        mSecurPhNumCv.smoothScrollTo(SCROLL_X, SCROLL_Y);
//                        boolean isOpenAdv = lostMgr.isOpenAdvanceProtect();
//                        if (!isOpenAdv) {
//                            loadInstructListData(true);
//                        } else {
//                            loadInstructListData(false);
//                        }
//                        mInstructAdapter.notifyDataSetInvalidated();
//                        if (isOpenAdv) {
//                            mNoAdvFinishTimeLt.setVisibility(View.GONE);
//                        } else {
//                            mNoAdvFinishTimeLt.setVisibility(View.VISIBLE);
//                        }
//                       /*是否添加了防盗号码*/
//                        String[] contact = null;
//                        if (!Utilities.isEmpty(securNumber)) {
//                            contact = securNumber.split(":");
//                        }
//                        setSecurShowUI(true, false, true, true, false, true, false, false);
//                        if (contact[0].equals(contact[1])) {
//                            mOpenSecurNumberTv.setVisibility(View.GONE);
//                            mOpenSecurNameTv.setText(contact[1]);
//                        } else {
//                            mOpenSecurNumberTv.setVisibility(View.VISIBLE);
//                            mOpenSecurNameTv.setText(contact[0]);
//                            mOpenSecurNumberTv.setText(contact[1]);
//                        }
//                        if (mButPointLt.getVisibility() == View.VISIBLE) {
//                            mButPointLt.setVisibility(View.GONE);
//                        }
//                        mButton.setText(getResources().getString(R.string.secur_open_bt));
//                        isOpenSecurBt = true;
//                        int[] protectTime = lostMgr.getPhoneProtectTime();
//                        mDayTv.setText(String.valueOf(protectTime[0]));
//                        mHourTv.setText(String.valueOf(protectTime[1]));
//                        if (!mBottomNumber[0].equals(mCurrentProcNumber)) {
//                            mCurrentProcNumber = mBottomNumber[0];
//                        }
//                        mNoAddSecurNumber.setVisibility(View.GONE);
//                        securTimeAnim(mShowProtTimeLt);
//                    }
//                } else {
//                    backupInstructsDialog();
//                    SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_backup");
//                }
//                break;
//            case R.id.secur_add_number_RT:
//                startAddSecurNumberIntent();
//                break;
//            case R.id.modify_secur_number_BT:
//                startAddSecurNumberIntent();
//                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft_use", "theft_cnts_changeContact");
//                break;
//            case R.id.secur_modify_bt:
//                startAddSecurNumberIntent();
//                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft_use", "theft_cnts_changeContact");
//                break;
//            case R.id.open_sucur_adv_bt:
//                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_instant");
//                startDeviceItent(false);
//                PhoneSecurityManager.getInstance(PhoneSecurityActivity.this).setIsAdvOpenTip(true);
//                break;
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
            case R.id.secur_colse_bt:
                showSecurCloseDialog();
                break;
            case R.id.secur_instru_tip_tv:
                Intent intentDet = new Intent(PhoneSecurityActivity.this, SecurityDetailActivity.class);
                try {
                    startActivity(intentDet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.secur_open_adv:
                startDeviceItent();
                break;
            case R.id.secur_mody_bt:
                startAddSecurNumberIntent();
                break;
            default:
                break;
        }
    }

    /**
     * 进入到添加防盗号码的电话列表
     */
    private void startAddSecurNumberIntent() {
        Intent intent = new Intent(PhoneSecurityActivity.this, AddSecurityNumberActivity.class);
        String extData = this.getResources().getString(R.string.secur_mody_num_title);
        intent.putExtra(AddSecurityNumberActivity.EXTERNAL_DATA, extData);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*跳转到设备管理器*/
    private void startDeviceItent() {
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
    }

    /*进入完成页面*/
//    private void toSecurFinish() {
//        LeoLog.i(TAG, "go to finish ！");
//        SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_suc_arv");
//        frmScanHandler();
//        LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//        boolean isOpenAdvan = lostMgr.isOpenAdvanceProtect();
//        if (isOpenAdvan) {
//            loadCompOpenInstructs();
//        } else {
//            loadNoOpenInstructData(false);
//        }
//        mBottonNumberView2.setViewBackGroundColor(getResources().getColor(R.color.cb));
//        mCurrentProcNumber = mBottomNumber[2];
////        setSecurShowUI(false, false, false, false, false, false, true, false);
//        mOperFinish.setImageResource(R.drawable.theft_step);
//        mAdvanChekBox.setVisibility(View.GONE);
//        mOperTwoPoint.setImageResource(R.drawable.theft_step_point);
//        mCommonBar.setToolbarTitle(R.string.secur_open_suc);
//        mButton.setText(getResources().getString(R.string.secur_finish_bt_text));
//        if (isOpenAdvan) {
//            mFinishTipTv.setText(getResources().getString(R.string.secur_finish_complate_text));
//            mFinishTipIV.setImageResource(R.drawable.theft_complete_img);
//        } else {
//            mFinishTipTv.setText(getResources().getString(R.string.secur_finish_no_complate_tip));
//            mFinishTipIV.setImageResource(R.drawable.theft_function_img);
//            showShareDialog();
//        }
//        LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//        /*设置手机防盗为开启状态*/
//        mgr.setUsePhoneSecurity(true);
//        /*设置开启保护的时间*/
//        mgr.setOpenSecurityTime();
//
//        PhoneSecurityManager psm = PhoneSecurityManager.getInstance(this);
//        if (psm.isIsAdvOpenTip()) {
//            psm.setIsAdvOpenTip(false);
//        }
//    }

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

//    /*到开启步骤2*/
//    private void toProcTwo() {
//        loadNoOpenInstructData(true);
//        loadOpenInstructData();
//        final LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//        boolean isOpenProtect = lostMgr.isOpenAdvanceProtect();
//        String securNumber = lostMgr.getPhoneSecurityNumber();
//        boolean isExistSecurNumber = false;
//        if (mBottomNumber[0].equals(mCurrentProcNumber)) {
//            LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//            boolean isExistSim = mgr.getIsExistSim();
//            if (isExistSim) {
//                ThreadManager.executeOnAsyncThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mCheckStatus) {
//                            PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
//                            String numberNmae = lostMgr.getPhoneSecurityNumber();
//                            if (!Utilities.isEmpty(numberNmae)) {
//                                String[] number = numberNmae.split(":");
//                                if (number != null) {
//                                    mgr.sendMessage(number[1], getSendMessageInstructs(), MTKSendMsmHandler.BACKUP_SECUR_INSTRUCT_ID);
//                                }
//                            }
//                        }
//                    }
//                });
//            } else {
//                String failStr = this.getResources().getString(
//                        R.string.privacy_message_item_send_message_fail);
//                Toast.makeText(this, failStr, Toast.LENGTH_SHORT).show();
//            }
//        }
//        if (!Utilities.isEmpty(securNumber)) {
//            isExistSecurNumber = true;
//        }
//        if (isExistSecurNumber) {
//            if (isOpenProtect) {
//               /*进入完成界面*/
//                toSecurFinish();
//            } else {
//                mOperTwoPoint.setImageResource(R.drawable.theft_step_point);
//                mBottonNumberView2.setViewBackGroundColor(getResources().getColor(R.color.cb));
////                setSecurShowUI(false, true, false, false, false, false, false, false);
//                isShowAdvProtectUi = true;
//                mCurrentProcNumber = mBottomNumber[1];
//                mCommonBar.setToolbarTitle(R.string.secur_advan_title);
//                mAdvanChekBox.setVisibility(View.VISIBLE);
//                LeoLog.i(TAG, "go to 2 !");
//                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_snd_arv");
//            }
//        } else {
//            shakeAddSecurButton(mAddNumberBt);
//            PhoneSecurityUtils.setVibrate(PhoneSecurityActivity.this, 200);
//            String toastTip = getResources().getString(R.string.no_add_secur_number_toast_tip);
//            mNoSecurNumTip.setVisibility(View.VISIBLE);
//            Toast.makeText(PhoneSecurityActivity.this, toastTip, Toast.LENGTH_SHORT).show();
//            SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_frt_nextNoTel");
//        }
//    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        switch (buttonView.getId()) {
//            case R.id.phone_security_number_backup_CB:
//                mCheckStatus = isChecked;
//                break;
//            case R.id.open_advance_CB:
//                mAdvanProCheckStatus = isChecked;
//                break;
//        }
    }

//    private void backupInstructsDialog() {
//
//        if (mBackupInstrDialog == null) {
//            mBackupInstrDialog = new LEOAlarmDialog(this);
//            mBackupInstrDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    if (mBackupInstrDialog != null) {
//                        mBackupInstrDialog = null;
//                    }
//
//                }
//            });
//        }
//        mBackupInstrDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//                boolean isExistSim = mgr.getIsExistSim();
//                if (isExistSim) {
//                    ThreadManager.executeOnAsyncThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            final LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//                            PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
//                            String numberNmae = lostMgr.getPhoneSecurityNumber();
//                            if (!Utilities.isEmpty(numberNmae)) {
//                                String[] number = numberNmae.split(":");
//                                if (number != null) {
//                                    mgr.sendMessage(number[1], getSendMessageInstructs(), MTKSendMsmHandler.BACKUP_SECUR_INSTRUCT_ID);
//                                }
//                            }
//                            if (mBackupInstrDialog != null) {
//                                mBackupInstrDialog.cancel();
//                            }
//                        }
//                    });
//                } else {
//                    String failStr = PhoneSecurityActivity.this.getResources().getString(
//                            R.string.privacy_message_item_send_message_fail);
//                    Toast.makeText(PhoneSecurityActivity.this, failStr, Toast.LENGTH_SHORT).show();
//                    if (mBackupInstrDialog != null) {
//                        mBackupInstrDialog.cancel();
//                    }
//                }
//            }
//        });
//        String content = getString(R.string.backup_instr_dialog_content);
//        mBackupInstrDialog.setDialogIconVisibility(false);
//        mBackupInstrDialog.setContent(content);
//        mBackupInstrDialog.show();
//    }

    /*从步骤2返回到步骤1*/
//    private void returnToProcOne() {
//        mCommonBar.setToolbarTitle(R.string.phone_security_open);
//        mOperTwoPoint.setImageResource(R.drawable.theft_step_point_dis);
//        mBottonNumberView2.setViewBackGroundColor(getResources().getColor(R.color.c5));
//        LostSecurityManagerImpl securityManager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//                     /*是否添加了防盗号码*/
//        String securNumber = securityManager.getPhoneSecurityNumber();
//        String[] contact = null;
//        if (!Utilities.isEmpty(securNumber)) {
//            contact = securNumber.split(":");
//        }
//        if (mButPointLt.getVisibility() == View.GONE) {
//            mButPointLt.setVisibility(View.VISIBLE);
//        }
////        setSecurShowUI(true, false, true, false, false, true, false, true);
//        mAddSecurNumber.setVisibility(View.VISIBLE);
//        if (contact != null) {
//            if (contact[0].equals(contact[1])) {
//                mNoNameSecurNumber.setVisibility(View.VISIBLE);
//                mExistSecurName.setVisibility(View.GONE);
//                mNoNameSecurNumber.setText(contact[1]);
//            } else {
//                mNoNameSecurNumber.setVisibility(View.GONE);
//                mExistSecurName.setVisibility(View.VISIBLE);
//                mSecurName.setText(contact[0]);
//                mSecurNumber.setText(contact[1]);
//            }
//        }
//        mButton.setText(getResources().getString(R.string.secur_bottom_bt_text));
//        mCurrentProcNumber = "1";
//        mAdvanChekBox.setVisibility(View.GONE);
//        LeoLog.i(TAG, "go to 1 !");
//        SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_frt_arv");
//    }

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
//    private void openAdvanceProtectDialogTip() {
//        if (mAdvanceTipDialog == null) {
//            mAdvanceTipDialog = new LEOAnimationDialog(this);
//            mAdvanceTipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    if (mAdvanceTipDialog != null) {
//                        mAdvanceTipDialog = null;
//                        showShareDialog();
//                    }
//                }
//            });
//        }
//        String content = getString(R.string.prot_open_suc_tip_cnt);
//        mAdvanceTipDialog.setContent(content);
//        mAdvanceTipDialog.show();
//        PhoneSecurityManager.getInstance(PhoneSecurityActivity.this).setIsAdvOpenTip(false);
//    }

//    private void showShareDialog() {
//        if (mPreference.getBoolean(PrefConst.PHONE_SECURITY_SHOW, false)) {
//            return;
//        }
//        if (mShareDialog == null) {
//            mShareDialog = new LEOAlarmDialog(PhoneSecurityActivity.this);
//            mShareDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    if (mShareDialog != null) {
//                        mShareDialog = null;
//                    }
//                }
//            });
//        }
//        String content = getString(R.string.phone_share_dialog_content);
//        String shareButton = getString(R.string.share_dialog_btn_query);
//        String cancelButton = getString(R.string.share_dialog_query_btn_cancel);
//        mShareDialog.setContent(content);
//        mShareDialog.setLeftBtnStr(cancelButton);
//        mShareDialog.setRightBtnStr(shareButton);
//        mShareDialog.setLeftBtnListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                SDKWrapper.addEvent(PhoneSecurityActivity.this, SDKWrapper.P1, "theft", "theft_noShare");
//                if (mShareDialog != null && mShareDialog.isShowing()) {
//                    mShareDialog.dismiss();
//                    mShareDialog = null;
//                }
//            }
//        });
//        mShareDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (mShareDialog != null && mShareDialog.isShowing()) {
//                    mShareDialog.dismiss();
//                    mShareDialog = null;
//                }
//                shareApps();
//            }
//        });
//        mShareDialog.show();
//        mPreference.putBoolean(PrefConst.PHONE_SECURITY_SHOW, true);
//    }

    /**
     * 分享应用
     */
    private void shareApps() {
        SDKWrapper.addEvent(PhoneSecurityActivity.this, SDKWrapper.P1, "theft", "theft_share");
        mLockManager.filterSelfOneMinites();
        LeoPreference shareLeoPreference = LeoPreference.getInstance();
        boolean isContentEmpty = TextUtils.isEmpty(
                shareLeoPreference.getString(PrefConst.KEY_PHONE_SHARE_CONTENT));
        boolean isUrlEmpty = TextUtils.isEmpty(
                shareLeoPreference.getString(PrefConst.KEY_PHONE_SHARE_URL));

        StringBuilder shareBuilder = new StringBuilder();
        if (!isContentEmpty && !isUrlEmpty) {
            shareBuilder.append(shareLeoPreference.getString(PrefConst.KEY_PHONE_SHARE_CONTENT))
                    .append(" ")
                    .append(shareLeoPreference.getString(PrefConst.KEY_PHONE_SHARE_URL));
        } else {
            shareBuilder.append(getResources().getString(R.string.phone_share_content))
                    .append(" ")
                    .append(Constants.DEFAULT_SHARE_URL);
        }
        Utilities.toShareApp(shareBuilder.toString(), getTitle().toString(), PhoneSecurityActivity.this);
    }

    /**
     * 关闭防盗弹框
     */
    private void showSecurCloseDialog() {
        if (mSecurCloseDialg == null) {
            mSecurCloseDialg = new LEOAlarmDialog(this);
        }
        mSecurCloseDialg.setContent(this.getResources().getString(R.string.secur_close_tip_content));
        mSecurCloseDialg.setLeftBtnStr(this.getResources().getString(R.string.secur_close_tip_bt_cancel));
        mSecurCloseDialg.setRightBtnStr(this.getResources().getString(R.string.secur_close_tip_bt_sure));
        mSecurCloseDialg.setLeftBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PhoneSecurityActivity.this, "cancel", Toast.LENGTH_SHORT).show();
                if (mSecurCloseDialg != null) {
                    mSecurCloseDialg.dismiss();
                }
            }
        });
        mSecurCloseDialg.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                manager.setUsePhoneSecurity(false);
                Intent intent = new Intent(PhoneSecurityActivity.this, PhoneSecurityGuideActivity.class);
                intent.putExtra(PhoneSecurityConstants.KEY_FORM_HOME_SECUR, true);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mSecurCloseDialg != null) {
                    mSecurCloseDialg.dismiss();
                }
                finish();
            }
        });
        mSecurCloseDialg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mSecurCloseDialg != null) {
                    mSecurCloseDialg = null;
                }
            }
        });
        if (!isFinishing()) {
            if (mSecurCloseDialg != null) {
                mSecurCloseDialg.show();
            }
        }
    }

}
