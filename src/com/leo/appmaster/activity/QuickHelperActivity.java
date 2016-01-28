
package com.leo.appmaster.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.appmanage.UninstallActivity;
import com.leo.appmaster.cleanmemory.HomeBoostActivity;
import com.leo.appmaster.home.DeskProxyActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.QuickHelperUtils;
import com.leo.appmaster.videohide.VideoHideMainActivity;

public class QuickHelperActivity extends BaseActivity {
    private static int[] mHelperResourceIDs;

    private static int[] mHelperNames;

    private static int[] mHelperDescs;

    private CommonToolbar mCtb;
    private ListView mLvQuickHelperList;
    private LayoutInflater mInflater;

    private static final int TOPMOST_CLASS_LAST_ONE_POSITION;
    private static final int FIRST_CLASS_LAST_ONE_POSITION;
    private static final int SECOND_CLASS_LAST_ONE_POSITION;
    private static final int THIRD_CLASS_LAST_ONE_POSITION;

    private static final int TOPMOST_CLASS_FIRST_ONE_POSITION;
    private static final int FIRST_CLASS_FIRST_ONE_POSITION;
    private static final int SECOND_CLASS_FIRST_ONE_POSITION;
    private static final int THIRD_CLASS_FIRST_ONE_POSITION;

    private static final int POSITION_GAME_BOOST;
    private static final int POSITION_CALL_FILTER;
    private static final int POSITION_BOOST;
    private static final int POSITION_IMAGE_HIDE;
    private static final int POSITION_VIDEO_HIEDE;
    private static final int POSITION_APP_UNSTALL;
    private static final int POSITION_APP_BACKUP;
    private static final int POSITION_FLOW;
    private static final int POSITION_ELEC;
    private static final int POSITION_WIFI;
    private static final int POSITION_INTRUDER;
    private static final int POSITION_SECRET_CALL;
//    private static final int POSITION_SECRET_MSG;
    private static final int POSITION_APPJOY;

    static {
//        if (Build.VERSION.SDK_INT > 21) {    
//            mHelperResourceIDs = new int[]{
//                    R.drawable.qh_image_icon, R.drawable.qh_video_icon,
//                    R.drawable.qh_intruder_icon,R.drawable.ic_launcher,
//                    R.drawable.qh_privacy_contact, R.drawable.qh_wifi_icon,
//                    R.drawable.qh_uninstall_icon, R.drawable.qh_backup_icon,
//                    R.drawable.qh_flow_icon,R.drawable.qh_battery_icon,
//                    R.drawable.qh_speedup_icon,
//                    R.drawable.qh_appjoy_icon
//            };
//            mHelperNames = new int[]
//            {
//                    R.string.quick_helper_pic_hide, R.string.quick_helper_video_hide,
//                    R.string.quick_helper_intruder, R.string.quick_helper_callfilter,
//                    R.string.privacy_contacts,
//                    R.string.quick_helper_wifi_safety,
//                    R.string.quick_helper_app_uninstall, R.string.quick_helper_app_backup,
//                    R.string.quick_helper_flow_manage,R.string.quick_helper_elec_manage,
//                    R.string.accelerate, R.string.desk_ad_name
//            };
//
//            mHelperDescs = new int[]
//            {
//                    R.string.app_name,
//                    R.string.quick_helper_desc_pic_hide, R.string.quick_helper_desc_video_hide,
//                    R.string.quick_helper_desc_intruder, R.string.quick_helper_desc_call,
//                    R.string.quick_helper_desc_wifi,
//                    R.string.quick_helper_desc_uninstall, R.string.quick_helper_desc_backup,
//                    R.string.quick_helper_desc_flow,R.string.quick_helper_desc_elec,
//                    R.string.quick_helper_desc_boost, R.string.quick_helper_desc_appjoy
//            };
//
//            FIRST_CLASS_LAST_ONE_POSITION = 4;
//            SECOND_CLASS_LAST_ONE_POSITION = 8;
//            THIRD_CLASS_LAST_ONE_POSITION = 9;
//
//            FIRST_CLASS_FIRST_ONE_POSITION = 0;
//            SECOND_CLASS_FIRST_ONE_POSITION = 5;
//            THIRD_CLASS_FIRST_ONE_POSITION = 9;
//
//            POSITION_IMAGE_HIDE = 0;
//            POSITION_VIDEO_HIEDE = 1;
//            POSITION_INTRUDER = 2;
//            POSITION_SECRET_CALL = 3;
//            POSITION_WIFI = 4;
//            POSITION_APP_UNSTALL = 5;
//            POSITION_APP_BACKUP = 6;
//            POSITION_FLOW = 7;
//            POSITION_BOOST = 9;
//            POSITION_APPJOY = 10;
//            POSITION_ELEC = 8;
//            } else {
            mHelperResourceIDs = new int[]{
                    R.drawable.ic_up_iswipe, /* TODO 更换为游戏加速图标 */
                    R.drawable.qh_image_icon, R.drawable.qh_video_icon,
                    R.drawable.qh_intruder_icon,R.drawable.qh_call_filter,
                    R.drawable.qh_privacy_contact, R.drawable.qh_wifi_icon,
                    R.drawable.qh_uninstall_icon, R.drawable.qh_backup_icon,
                    R.drawable.qh_flow_icon,
                    R.drawable.qh_battery_icon, R.drawable.qh_speedup_icon,
                    R.drawable.qh_appjoy_icon
            };

            mHelperNames = new int[]
            {
                    R.string.game_box_one,
                    R.string.quick_helper_pic_hide, R.string.quick_helper_video_hide,
                    R.string.quick_helper_intruder, R.string.quick_helper_callfilter, R.string.privacy_contacts,
                    R.string.quick_helper_wifi_safety,
                    R.string.quick_helper_app_uninstall, R.string.quick_helper_app_backup,
                    R.string.quick_helper_flow_manage, R.string.quick_helper_elec_manage,
                    R.string.accelerate, R.string.desk_ad_name
            };

            mHelperDescs = new int[]
            {
                    R.string.game_box_one, /* TODO 换成正确的描述 */
                    R.string.quick_helper_desc_pic_hide, R.string.quick_helper_desc_video_hide,
                    R.string.quick_helper_desc_intruder, R.string.quick_helper_desc_callfilter, R.string.quick_helper_desc_call,
                    R.string.quick_helper_desc_wifi,
                    R.string.quick_helper_desc_uninstall, R.string.quick_helper_desc_backup,
                    R.string.quick_helper_desc_flow, R.string.quick_helper_desc_elec,
                    R.string.quick_helper_desc_boost, R.string.quick_helper_desc_appjoy
            };

            TOPMOST_CLASS_LAST_ONE_POSITION = 0;
            FIRST_CLASS_LAST_ONE_POSITION = 6;
            SECOND_CLASS_LAST_ONE_POSITION = 11;
            THIRD_CLASS_LAST_ONE_POSITION = 12;

            TOPMOST_CLASS_FIRST_ONE_POSITION = 0;
            FIRST_CLASS_FIRST_ONE_POSITION = 1;
            SECOND_CLASS_FIRST_ONE_POSITION = 7;
            THIRD_CLASS_FIRST_ONE_POSITION = 12;

            POSITION_GAME_BOOST = 0;
            POSITION_IMAGE_HIDE = POSITION_GAME_BOOST + 1;
            POSITION_VIDEO_HIEDE = POSITION_IMAGE_HIDE + 1;
            POSITION_INTRUDER = POSITION_VIDEO_HIEDE + 1;
            POSITION_CALL_FILTER = POSITION_INTRUDER + 1;
            POSITION_SECRET_CALL = POSITION_CALL_FILTER + 1;
            POSITION_WIFI = POSITION_SECRET_CALL + 1;
            POSITION_APP_UNSTALL = POSITION_WIFI + 1;
            POSITION_APP_BACKUP = POSITION_APP_UNSTALL + 1;
            POSITION_FLOW = POSITION_APP_BACKUP + 1;
            POSITION_ELEC = POSITION_FLOW + 1;
            POSITION_BOOST = POSITION_ELEC + 1;
            POSITION_APPJOY = POSITION_BOOST + 1;
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quickhelper);
        SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1, "assistant", "assistant_enter");
        init();
    }

    private void init() {
        final boolean showGameBox = SDKWrapper.isGameBoxAvailable(this);
        mInflater = LayoutInflater.from(this);
        mCtb = (CommonToolbar) findViewById(R.id.ctb_quickhelper_title);
        mCtb.setToolbarTitle(R.string.hp_helper_shot);
        mCtb.setOptionMenuVisible(false);
        mLvQuickHelperList = (ListView) findViewById(R.id.lv_quickhelperlist);
        View header = mInflater.inflate(R.layout.headerview_quickhelper, null);
        mLvQuickHelperList.addHeaderView(header);
        mLvQuickHelperList.setAdapter(new BaseAdapter() {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                int realPosition = position;
                if (!showGameBox) {
                    realPosition += 1;
                }
                View view = mInflater.inflate(R.layout.item_quick_helper_list, null);
                TextView tvClass = (TextView) view.findViewById(R.id.tv_class);
                LinearLayout llClass = (LinearLayout) view.findViewById(R.id.ll_class);
                RippleView rvAdd = (RippleView) view.findViewById(R.id.rv_add);
                ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
                TextView tvName = (TextView) view.findViewById(R.id.tv_name);
                TextView tvDesc = (TextView) view.findViewById(R.id.tv_desc);
                View line = view.findViewById(R.id.v_line);
                if (realPosition == TOPMOST_CLASS_LAST_ONE_POSITION
                        ||realPosition == FIRST_CLASS_LAST_ONE_POSITION
                        || realPosition == SECOND_CLASS_LAST_ONE_POSITION
                        || realPosition == THIRD_CLASS_LAST_ONE_POSITION) {
                    line.setVisibility(View.GONE);
                }
                if (realPosition == TOPMOST_CLASS_FIRST_ONE_POSITION) {
                    tvClass.setText(R.string.up_list_swifty_title);
                } else if (realPosition == FIRST_CLASS_FIRST_ONE_POSITION) {
                    tvClass.setText(R.string.class_privacy_protection);
                } else if (realPosition == SECOND_CLASS_FIRST_ONE_POSITION) {
                    tvClass.setText(R.string.class_system_manage);
                } else if (realPosition == THIRD_CLASS_FIRST_ONE_POSITION) {
                    tvClass.setText(R.string.class_happy_app);
                } else {
                    llClass.setVisibility(View.GONE);
                }
                ivIcon.setBackgroundResource(mHelperResourceIDs[realPosition]);
                tvName.setText(getResources().getString(mHelperNames[realPosition]));
                tvDesc.setText(getResources().getString(mHelperDescs[realPosition]));
                final int finalRealPosition = realPosition;
                rvAdd.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent;
                        int id = (int) getItemId(finalRealPosition);
                        switch (id) {
                            case R.drawable.ic_up_iswipe:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_gameboost");
                                SDKWrapper.createGameBoxIcons(QuickHelperActivity.this.getApplicationContext());
                                break;
                            // 桌面加速 (免密码)
                            case R.drawable.qh_speedup_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_accelerate");
//                                boolean isInstalllIswipe = ISwipUpdateRequestManager
//                                        .isInstallIsiwpe(AppMasterApplication.getInstance());
//                                if (!isBoostCreat) {  
                                    intent = new Intent(AppMasterApplication.getInstance(), HomeBoostActivity.class);
                                    intent.putExtra("from_quickhelper", true);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_BOOST]), mHelperResourceIDs[POSITION_BOOST], intent, QuickHelperActivity.this);
//                                }
                                break;
                            // 图片隐藏
                            case R.drawable.qh_image_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_hidepic");
                                intent = new Intent(AppMasterApplication.getInstance(), ImageHideMainActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_IMAGE_HIDE]), mHelperResourceIDs[POSITION_IMAGE_HIDE], intent, QuickHelperActivity.this);
                                break;
                            // 视频隐藏
                            case R.drawable.qh_video_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_hidevid");
                                intent = new Intent(AppMasterApplication.getInstance(), VideoHideMainActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_VIDEO_HIEDE]), mHelperResourceIDs[POSITION_VIDEO_HIEDE], intent, QuickHelperActivity.this);
                                break;
                            // 应用卸载
                            case R.drawable.qh_uninstall_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_uninstall");
                                intent = new Intent(AppMasterApplication.getInstance(), UninstallActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_APP_UNSTALL]), mHelperResourceIDs[POSITION_APP_UNSTALL], intent, QuickHelperActivity.this);
                                break;
                            // 应用备份 (免密码)
                            case R.drawable.qh_backup_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_backup");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.mBackup);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_APP_BACKUP]), mHelperResourceIDs[POSITION_APP_BACKUP], intent, QuickHelperActivity.this);
                                break;
                            // 流量管理 (免密码)
                            case R.drawable.qh_flow_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_dataflow");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.mFlow);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_FLOW]), mHelperResourceIDs[POSITION_FLOW], intent, QuickHelperActivity.this);
                                break;
                            // 电量管理 (免密码)
                            case R.drawable.qh_battery_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_power");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.mElec);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_ELEC]), mHelperResourceIDs[POSITION_ELEC], intent, QuickHelperActivity.this);
                                break;
                             // 骚扰拦截 (免密码)
                            case R.drawable.qh_call_filter:
//                             SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1, "assistant", "assistant_wifi");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.mCallfilter);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_CALL_FILTER]), mHelperResourceIDs[POSITION_CALL_FILTER], intent, QuickHelperActivity.this);
                                break;
                            // WIFI安全 (免密码)
                            case R.drawable.qh_wifi_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_wifi");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.mWifi);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_WIFI]), mHelperResourceIDs[POSITION_WIFI], intent, QuickHelperActivity.this);
                                break;
                            // 入侵者防护
                            case R.drawable.qh_intruder_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_intruder");
                                intent = new Intent(AppMasterApplication.getInstance(), IntruderprotectionActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_INTRUDER]), mHelperResourceIDs[POSITION_INTRUDER], intent, QuickHelperActivity.this);
                                break;
                            // 隐私通话
                            case R.drawable.qh_privacy_contact:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_call");
                                intent = new Intent(AppMasterApplication.getInstance(), PrivacyContactActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT, PrivacyContactUtils.TO_PRIVACY_CONTACT_FLAG);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_SECRET_CALL]), mHelperResourceIDs[POSITION_SECRET_CALL], intent, QuickHelperActivity.this);
                                break;
//                            // 隐私短信
//                            case R.drawable.qh_sms_icon:
//                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
//                                        "assistant", "assistant_sms");
//                                intent = new Intent(AppMasterApplication.getInstance(), PrivacyContactActivity.class);
//                                intent.putExtra("from_quickhelper", true);
//                                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT, PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_SECRET_MSG]), mHelperResourceIDs[POSITION_SECRET_MSG], intent, QuickHelperActivity.this);
//                                break;
                            // 欢乐APP (免密码)
                            case R.drawable.qh_appjoy_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_appjoy");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.mAd);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_APPJOY]), mHelperResourceIDs[POSITION_APPJOY], intent, QuickHelperActivity.this);
                                break;
                            default:
                                break;
                        }
                        Toast.makeText(QuickHelperActivity.this, R.string.quick_help_add_toast, Toast.LENGTH_SHORT).show();
                    }
                });
                return view;
            }

            @Override
            public long getItemId(int position) {
                return mHelperResourceIDs[position];
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public int getCount() {
                if (showGameBox) {
                    return mHelperResourceIDs.length;
                } else {
                    return mHelperResourceIDs.length-1;
                }
            }
        });
    }
}
