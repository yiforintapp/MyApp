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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.LeoHomePopMenu;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAnimationDialog;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class PhoneSecurityActivity extends BaseActivity implements OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "PhoneSecurityActivity";

    public static final String FROM_SECUR_INTENT = "FROM_SECUR_INTENT";
    public static final int FROM_SECUR_GUIDE_INTENT = 1;
    public static final int FROM_ADD_NUM_MSM = 2;
    public static final int FROM_ADD_NUM_NO_MSM = 3;

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
    //    private Button mCloseBt;
    private TextView mInstruTipTv;
    private Button mAdvBt;
    private Button mSecurNumModyBt;
    private TextView mSecurNumTv;
    private LeoPreference mPreference;
    private boolean mIsOpenBtResum = false;
    private LeoHomePopMenu mLeoPopMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_security);
        fromIntentHandler();
        initUI();
        initData();
    }

    private void fromIntentHandler() {
        Intent intent = this.getIntent();
        int fromId = intent.getIntExtra(FROM_SECUR_INTENT, 0);
        LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        switch (fromId) {
            case FROM_SECUR_GUIDE_INTENT:
                String toastStrGuide = this.getResources().getString(R.string.secur_open_sucess_toast);
                openSecurToast(toastStrGuide);
                if (securMgr.isOpenAdvanceProtect()) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "theft1", "theft_suc");
                }
                break;
            case FROM_ADD_NUM_MSM:
                String toastStrMsm = this.getResources().getString(R.string.secur_open_sucess_send_msm_toast);
                openSecurToast(toastStrMsm);
                break;
            case FROM_ADD_NUM_NO_MSM:
                String toastStrNoMsm = this.getResources().getString(R.string.secur_open_sucess_toast);
                openSecurToast(toastStrNoMsm);
                break;
            default:
                break;
        }
    }

    private void openSecurToast(String toastStr) {
        Toast.makeText(this, toastStr, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        onResumHandler();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * onResum中处理
     */
    private void onResumHandler() {
        LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        int[] protectTime = securMgr.getPhoneProtectTime();
        mDayTv.setText(String.valueOf(protectTime[0]));
        mHourTv.setText(String.valueOf(protectTime[1]));
        securTimeAnim(mShowProtTimeLt);
        String securNumber = securMgr.getPhoneSecurityNumber();
        String[] contact = null;
        if (!Utilities.isEmpty(securNumber)) {
            contact = securNumber.split(":");
            mSecurNumTv.setText(contact[1]);
        }
        boolean isOpPro = securMgr.isOpenAdvanceProtect();
        if (isOpPro) {
            List<InstructModel> instrData = loadAdvOpenInstructData(true);
            if (instrData != null && instrData.size() > 0) {
                mAdvAdapter.setData(instrData);
                mAdvAdapter.notifyDataSetChanged();
            }
            mAdvBt.setVisibility(View.GONE);
            if (mIsOpenBtResum) {
                openAdvanceProtectDialogTip();
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft1", "theft_auth_suc");
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft1", "theft_suc");
            }
        } else {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "theft1", "theft_auth");
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mPreference = LeoPreference.getInstance();
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
        showShareDialog();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        mCommonBar = (CommonToolbar) findViewById(R.id.phone_security_commonbar);
        mCommonBar.setToolbarTitle(R.string.phone_security_open);
        mCommonBar.setToolbarColorResource(R.color.ctc);
        mCommonBar.setOptionMenuVisible(true);
        mCommonBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initSettingMenu();
                mLeoPopMenu.setPopMenuItems(PhoneSecurityActivity.this, getRightMenuItems(), getRightMenuIcons());
                mLeoPopMenu.showPopMenu(PhoneSecurityActivity.this, mCommonBar.getSecOptionImageView(), null, null);
            }
        });
        mCommonBar.setSecOptionMenuVisible(true);
        mCommonBar.setSecOptionImageResource(R.drawable.help_icon_n);
        mHelpIcon = mCommonBar.getOptionImageView();
        mCommonBar.setNavigationClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneSecurityActivity.this.finish();
            }
        });
        mCommonBar.setSecOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHelpRt.getVisibility() == View.VISIBLE) {
                    mSecurPhNumCv.setVisibility(View.VISIBLE);
                    mHelpRt.setVisibility(View.GONE);
                    mHelpRt.startAnimation(AnimationUtils
                            .loadAnimation(PhoneSecurityActivity.this, R.anim.lock_mode_guide_out));
                } else {
                    unuseSecurHandler();
                    mHelpRt.setVisibility(View.VISIBLE);
                    mSecurPhNumCv.setVisibility(View.GONE);
                    mHelpRt.startAnimation(AnimationUtils
                            .loadAnimation(PhoneSecurityActivity.this, R.anim.lock_mode_guide_in));
                }
            }
        });

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
        mNormalGv.setOnItemClickListener(this);
        mAdvGv = (GridView) findViewById(R.id.secur_open_adv_lv);
        mAdvGv.setOnItemClickListener(this);
//        mCloseBt = (Button) findViewById(R.id.secur_colse_bt);
//        mCloseBt.setOnClickListener(this);
        mInstruTipTv = (TextView) findViewById(R.id.secur_instru_tip_tv);
        mInstruTipTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mInstruTipTv.setOnClickListener(this);
        mAdvBt = (Button) findViewById(R.id.secur_open_adv);
        mAdvBt.setOnClickListener(this);
        mSecurNumModyBt = (Button) findViewById(R.id.secur_mody_bt);
        mSecurNumModyBt.setOnClickListener(this);
        mSecurNumTv = (TextView) findViewById(R.id.secur_num_tv);
    }

    private List<String> getRightMenuItems() {
        List<String> listItems = new ArrayList<String>();
        listItems.add(this.getString(R.string.secur_close));
        return listItems;
    }

    private List<Integer> getRightMenuIcons() {
        List<Integer> icons = new ArrayList<Integer>();
        icons.add(R.drawable.settings);
        return icons;
    }

    private void initSettingMenu() {
        if (mLeoPopMenu != null) return;
        mLeoPopMenu = new LeoHomePopMenu();
        mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
        mLeoPopMenu.setPopItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    showSecurCloseDialog();
                }
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLeoPopMenu.dismissSnapshotList();
                    }
                }, 500);
            }
        });
        mLeoPopMenu.setListViewDivider(null);
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
            selectImage = R.drawable.icon_antitheft_ok;
        } else {
            selectImage = R.drawable.icon_antitheft_unok;
        }
        int oneKeyImage = R.drawable.theft_onekey;
        int oneKeyContent = R.string.onkey_name;
        noOpenInstrs.add(new InstructModel(oneKeyImage, selectImage, oneKeyContent));

        int formateImage = R.drawable.theft_format;
        int formateContent = R.string.formate_name;
        noOpenInstrs.add(new InstructModel(formateImage, selectImage, formateContent));
        return noOpenInstrs;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.help_bt:
                if (mHelpRt.getVisibility() == View.VISIBLE) {
                    mHelpRt.setVisibility(View.GONE);
                    mSecurPhNumCv.setVisibility(View.VISIBLE);
                    mHelpRt.startAnimation(AnimationUtils
                            .loadAnimation(PhoneSecurityActivity.this, R.anim.lock_mode_guide_out));
                    Animation animation =
                            AnimationUtils.loadAnimation(PhoneSecurityActivity.this,
                                    R.anim.help_tip_show);
                    mHelpIcon.startAnimation(animation);
                } else {
                    unuseSecurHandler();
                    mHelpRt.setVisibility(View.VISIBLE);
                    mSecurPhNumCv.setVisibility(View.GONE);
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
            case R.id.secur_instru_tip_tv:
                startSecurInstrDetailActivity();
                break;
            case R.id.secur_open_adv:
                mIsOpenBtResum = true;
                startDeviceItent();
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft1", "theft_auth_cli");
                break;
            case R.id.secur_mody_bt:
                startAddSecurNumberIntent();
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft1", "theft_tel_change");
                break;
            default:
                break;
        }
    }

    private void startSecurInstrDetailActivity() {
        Intent intentDet = new Intent(PhoneSecurityActivity.this, SecurityDetailActivity.class);
        try {
            startActivity(intentDet);
        } catch (Exception e) {
            e.printStackTrace();
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

    /*保护时间动画*/
    private void securTimeAnim(View view) {
        view.clearAnimation();
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.secur_protec_time_anim);
        view.startAnimation(operatingAnim);

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
                if (mSecurCloseDialg != null) {
                    mSecurCloseDialg.dismiss();
                }
            }
        });
        mSecurCloseDialg.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SDKWrapper.addEvent(PhoneSecurityActivity.this,SDKWrapper.P1,"theft1","theft_off");
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

    private void showShareDialog() {
        boolean isShareTip = mPreference.getBoolean(PrefConst.PHONE_SECURITY_SHOW, false);
        if (isShareTip) {
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
        if (!this.isFinishing()) {
            mShareDialog.show();
        }
        mPreference.putBoolean(PrefConst.PHONE_SECURITY_SHOW, true);
    }

    /**
     * 开启高级保护弹窗
     */
    private void openAdvanceProtectDialogTip() {
        boolean isTip = PhoneSecurityManager.getInstance(PhoneSecurityActivity.this).isIsAdvOpenTip();
        if (isTip) {
            return;
        }
        if (mAdvanceTipDialog == null) {
            mAdvanceTipDialog = new LEOAnimationDialog(this);
            mAdvanceTipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mAdvanceTipDialog != null) {
                        mAdvanceTipDialog = null;
                        mIsOpenBtResum = false;
                    }
                }
            });
        }
        String content = getString(R.string.prot_open_suc_tip_cnt);
        mAdvanceTipDialog.setContent(content);
        if (!this.isFinishing() && mAdvanceTipDialog != null) {
            mAdvanceTipDialog.show();
            mIsOpenBtResum = false;
        }
        PhoneSecurityManager.getInstance(PhoneSecurityActivity.this).setIsAdvOpenTip(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int rootId = parent.getId();
        if (rootId == R.id.secur_open_normal_lv) {
            startSecurInstrDetailActivity();
        } else if (rootId == R.id.secur_open_adv_lv) {
            LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            boolean isOpPro = securMgr.isOpenAdvanceProtect();
            if (!isOpPro) {
                mIsOpenBtResum = true;
                startDeviceItent();
            } else {
                startSecurInstrDetailActivity();
            }
        }
    }
}
