
package com.leo.appmaster.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseIntArray;
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
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.appmanage.UninstallActivity;
import com.leo.appmaster.cleanmemory.HomeBoostActivity;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.home.DeskProxyActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.QuickHelperUtils;
import com.leo.appmaster.videohide.VideoHideMainActivity;

public class QuickHelperActivity extends BaseActivity {
    private List<Integer> mIndexCategoryStart;
    private List<Integer> mIndexCategoryEnd;
    private SparseIntArray mMapDrawableToName;
    private SparseIntArray mMapDrawableToDesc;
    private static Integer[] mDrawableBoost;
    private static Integer[] mDrawablePrivacy;
    private static Integer[] mDrawablePrivacy2;
    private static Integer[] mDrawableSystemManage;
    private static Integer[] mDrawableSystemManage2;
    private static Integer[] mDrawableAppJoy;
    
    private List<Integer[]> mFinalDrawableArray;
    private List<Integer> mFinalDrawableIds;
    
    private CommonToolbar mCtb;
    private ListView mLvQuickHelperList;
    private LayoutInflater mInflater;

//    private static final int TOPMOST_CLASS_LAST_ONE_POSITION;
//    private static final int FIRST_CLASS_LAST_ONE_POSITION;
//    private static final int SECOND_CLASS_LAST_ONE_POSITION;
//    private static final int THIRD_CLASS_LAST_ONE_POSITION;
//
//    private static final int TOPMOST_CLASS_FIRST_ONE_POSITION;
//    private static final int FIRST_CLASS_FIRST_ONE_POSITION;
//    private static final int SECOND_CLASS_FIRST_ONE_POSITION;
//    private static final int THIRD_CLASS_FIRST_ONE_POSITION;
//
//    private static final int POSITION_GAME_BOOST;
//    private static final int POSITION_CALL_FILTER;
//    private static final int POSITION_BOOST;
//    private static final int POSITION_IMAGE_HIDE;
//    private static final int POSITION_VIDEO_HIEDE;
//    private static final int POSITION_APP_UNSTALL;
//    private static final int POSITION_APP_BACKUP;
//    private static final int POSITION_FLOW;
//    private static final int POSITION_ELEC;
//    private static final int POSITION_WIFI;
//    private static final int POSITION_INTRUDER;
//    private static final int POSITION_SECRET_CALL;
////    private static final int POSITION_SECRET_MSG;
//    private static final int POSITION_APPJOY;

    static {
        mDrawableBoost = new Integer[] {
                R.drawable.qh_gamebox_icon
        };
        mDrawablePrivacy = new Integer[] {
                R.drawable.qh_image_icon, R.drawable.qh_video_icon,
                R.drawable.qh_intruder_icon, R.drawable.qh_call_filter, 
                R.drawable.qh_privacy_contact, R.drawable.qh_wifi_icon,
        };
        mDrawablePrivacy2 = new Integer[] {
                R.drawable.qh_image_icon, R.drawable.qh_video_icon,
                R.drawable.qh_intruder_icon, R.drawable.qh_call_filter, 
                R.drawable.qh_wifi_icon,
        };
        mDrawableSystemManage = new Integer[] {
                R.drawable.qh_uninstall_icon, R.drawable.qh_backup_icon,
                R.drawable.qh_flow_icon, R.drawable.qh_battery_icon, 
                R.drawable.qh_speedup_icon,
        };
        mDrawableSystemManage2 = new Integer[] {
                R.drawable.qh_flow_icon, R.drawable.qh_battery_icon, 
                R.drawable.qh_speedup_icon,
        };
        mDrawableAppJoy = new Integer[] {
                R.drawable.qh_appjoy_icon
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quickhelper);
        putInOrder();
        SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1, "assistant", "assistant_enter");
        init();
    }

    private void putInOrder() {
        mMapDrawableToName = new SparseIntArray();
        mMapDrawableToName.put(R.drawable.qh_gamebox_icon, R.string.game_box_one);
        mMapDrawableToName.put(R.drawable.qh_image_icon, R.string.quick_helper_pic_hide);
        mMapDrawableToName.put(R.drawable.qh_video_icon, R.string.quick_helper_video_hide);
        mMapDrawableToName.put(R.drawable.qh_intruder_icon, R.string.quick_helper_intruder);
        mMapDrawableToName.put(R.drawable.qh_call_filter, R.string.quick_helper_callfilter);
        mMapDrawableToName.put(R.drawable.qh_privacy_contact, R.string.privacy_contacts);
        mMapDrawableToName.put(R.drawable.qh_wifi_icon, R.string.quick_helper_wifi_safety);
        mMapDrawableToName.put(R.drawable.qh_uninstall_icon, R.string.quick_helper_app_uninstall);
        mMapDrawableToName.put(R.drawable.qh_backup_icon, R.string.quick_helper_app_backup);
        mMapDrawableToName.put(R.drawable.qh_flow_icon, R.string.quick_helper_flow_manage);
        mMapDrawableToName.put(R.drawable.qh_battery_icon, R.string.quick_helper_elec_manage);
        mMapDrawableToName.put(R.drawable.qh_speedup_icon, R.string.accelerate);
        mMapDrawableToName.put(R.drawable.qh_appjoy_icon, R.string.desk_ad_name);
        
        mMapDrawableToDesc = new SparseIntArray();
        mMapDrawableToDesc.put(R.drawable.qh_gamebox_icon, R.string.game_box_one);
        mMapDrawableToDesc.put(R.drawable.qh_image_icon, R.string.quick_helper_desc_pic_hide);
        mMapDrawableToDesc.put(R.drawable.qh_video_icon, R.string.quick_helper_desc_video_hide);
        mMapDrawableToDesc.put(R.drawable.qh_intruder_icon, R.string.quick_helper_desc_intruder);
        mMapDrawableToDesc.put(R.drawable.qh_call_filter, R.string.quick_helper_desc_callfilter);
        mMapDrawableToDesc.put(R.drawable.qh_privacy_contact, R.string.quick_helper_desc_call);
        mMapDrawableToDesc.put(R.drawable.qh_wifi_icon, R.string.quick_helper_desc_wifi);
        mMapDrawableToDesc.put(R.drawable.qh_uninstall_icon, R.string.quick_helper_desc_uninstall);
        mMapDrawableToDesc.put(R.drawable.qh_backup_icon, R.string.quick_helper_desc_backup);
        mMapDrawableToDesc.put(R.drawable.qh_flow_icon, R.string.quick_helper_desc_flow);
        mMapDrawableToDesc.put(R.drawable.qh_battery_icon, R.string.quick_helper_desc_elec);
        mMapDrawableToDesc.put(R.drawable.qh_speedup_icon, R.string.quick_helper_desc_boost);
        mMapDrawableToDesc.put(R.drawable.qh_appjoy_icon, R.string.quick_helper_desc_appjoy);
        
        mFinalDrawableArray = new ArrayList<Integer[]>();
        mFinalDrawableIds = new ArrayList<Integer>();
        if (AppMasterPreference.getInstance(this).getIsNeedCutBackupUninstallAndPrivacyContact()) {
            mFinalDrawableArray.add(mDrawablePrivacy2);
            mFinalDrawableArray.add(mDrawableSystemManage2);
        } else {
            mFinalDrawableArray.add(mDrawablePrivacy);
            mFinalDrawableArray.add(mDrawableSystemManage);
        }
//        mFinalDrawableArray.add(mDrawableBoost);
        mFinalDrawableArray.add(mDrawableAppJoy);
        
        mIndexCategoryStart = new ArrayList<Integer>();
        mIndexCategoryEnd = new ArrayList<Integer>();
        int accountBeforeCurrentI = 0;
        for (int i = 0; i < mFinalDrawableArray.size(); i++) {
            for (int j = 0; j < mFinalDrawableArray.get(i).length; j++) {
                Integer[] integers = mFinalDrawableArray.get(i);
                mFinalDrawableIds.add(integers[j]);
            }
            mIndexCategoryStart.add(accountBeforeCurrentI);
            accountBeforeCurrentI += mFinalDrawableArray.get(i).length;
            mIndexCategoryEnd.add(accountBeforeCurrentI - 1);
        }
    }

    private void init() {
//        final boolean showGameBox = SDKWrapper.isGameBoxAvailable(this);
        mInflater = LayoutInflater.from(this);
        mCtb = (CommonToolbar) findViewById(R.id.ctb_quickhelper_title);
        mCtb.setToolbarTitle(R.string.hp_helper_shot);
        mCtb.setOptionMenuVisible(false);
        mLvQuickHelperList = (ListView) findViewById(R.id.lv_quickhelperlist);
        View header = mInflater.inflate(R.layout.headerview_quickhelper, null);
        mLvQuickHelperList.addHeaderView(header, null, false);
        mLvQuickHelperList.setAdapter(new BaseAdapter() {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = mInflater.inflate(R.layout.item_quick_helper_list, null);
                TextView tvClass = (TextView) view.findViewById(R.id.tv_class);
                LinearLayout llClass = (LinearLayout) view.findViewById(R.id.ll_class);
                RippleView rvAdd = (RippleView) view.findViewById(R.id.rv_add);
                ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
                TextView tvName = (TextView) view.findViewById(R.id.tv_name);
                TextView tvDesc = (TextView) view.findViewById(R.id.tv_desc);
                View line = view.findViewById(R.id.v_line);
                if (mIndexCategoryEnd.contains(position)) {
                    line.setVisibility(View.GONE);
                }
                final int drawableId = mFinalDrawableIds.get(position);
                if (mIndexCategoryStart.contains(position)) {
                    if (Arrays.asList(mDrawableBoost).contains(drawableId)) {
                        tvClass.setText(R.string.up_list_swifty_title);
                    } else if (Arrays.asList(mDrawablePrivacy).contains(drawableId)) {
                        tvClass.setText(R.string.class_privacy_protection);
                    } else if (Arrays.asList(mDrawableSystemManage).contains(drawableId)) {
                        tvClass.setText(R.string.class_system_manage);
                    } else if (Arrays.asList(mDrawableAppJoy).contains(drawableId)) {
                        tvClass.setText(R.string.class_happy_app);
                    } 
                } else {
                    llClass.setVisibility(View.GONE);
                }
//                if (position == TOPMOST_CLASS_FIRST_ONE_POSITION) {
//                    tvClass.setText(R.string.up_list_swifty_title);
//                } else if (position == FIRST_CLASS_FIRST_ONE_POSITION) {
//                    tvClass.setText(R.string.class_privacy_protection);
//                } else if (position == SECOND_CLASS_FIRST_ONE_POSITION) {
//                    tvClass.setText(R.string.class_system_manage);
//                } else if (position == THIRD_CLASS_FIRST_ONE_POSITION) {
//                    tvClass.setText(R.string.class_happy_app);
//                } else {
//                    llClass.setVisibility(View.GONE);
//                }
                ivIcon.setBackgroundResource(drawableId);
                tvName.setText(getResources().getString(mMapDrawableToName.get(drawableId)));
                tvDesc.setText(getResources().getString(mMapDrawableToDesc.get(drawableId)));
                final PreferenceTable table = PreferenceTable.getInstance();
                rvAdd.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = null;
                        int id = (int) getItemId(position);
                        switch (id) {
                            /*case R.drawable.qh_gamebox_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_gameboost");
                                SDKWrapper.createGameBoxIcons(QuickHelperActivity.this.getApplicationContext());
                                break;*/
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
//                                    QuickHelperUtils.createQuickHelper(getResources().getString(mMapDrawableToName.get(drawableId)), drawableId, intent, QuickHelperActivity.this);
//                                }
                                break;
                            // 图片隐藏
                            case R.drawable.qh_image_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_hidepic");
                                intent = new Intent(AppMasterApplication.getInstance(), ImageHideMainActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mMapDrawableToName.get(drawableId)), drawableId, intent, QuickHelperActivity.this);
                                table.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, true);
                                break;
                            // 视频隐藏
                            case R.drawable.qh_video_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_hidevid");
                                intent = new Intent(AppMasterApplication.getInstance(), VideoHideMainActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                table.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_VID, true);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_VIDEO_HIEDE]), mHelperResourceIDs[POSITION_VIDEO_HIEDE], intent, QuickHelperActivity.this);
                                break;
                            // 应用卸载
                            case R.drawable.qh_uninstall_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_uninstall");
                                intent = new Intent(AppMasterApplication.getInstance(), UninstallActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_APP_UNSTALL]), mHelperResourceIDs[POSITION_APP_UNSTALL], intent, QuickHelperActivity.this);
                                break;
                            // 应用备份 (免密码)
                            case R.drawable.qh_backup_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_backup");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_BACKUP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_APP_BACKUP]), mHelperResourceIDs[POSITION_APP_BACKUP], intent, QuickHelperActivity.this);
                                break;
                            // 流量管理 (免密码)
                            case R.drawable.qh_flow_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_dataflow");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_FLOW);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_FLOW]), mHelperResourceIDs[POSITION_FLOW], intent, QuickHelperActivity.this);
                                break;
                            // 电量管理 (免密码)
                            case R.drawable.qh_battery_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_power");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_ELEC);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_ELEC]), mHelperResourceIDs[POSITION_ELEC], intent, QuickHelperActivity.this);
                                break;
                             // 骚扰拦截 (免密码)
                            case R.drawable.qh_call_filter:
//                             SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1, "assistant", "assistant_wifi");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_CALL_FILTER);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_CALL_FILTER]), mHelperResourceIDs[POSITION_CALL_FILTER], intent, QuickHelperActivity.this);
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,"assistant", "block_cnts");
                                table.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_CALLFILTER, true);
                                break;
                            // WIFI安全 (免密码)
                            case R.drawable.qh_wifi_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_wifi");
                                intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_WIFI);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_WIFI]), mHelperResourceIDs[POSITION_WIFI], intent, QuickHelperActivity.this);
                                table.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_WIFI_SECURITY, true);
                                break;
                            // 入侵者防护
                            case R.drawable.qh_intruder_icon:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_intruder");
                                intent = new Intent(AppMasterApplication.getInstance(), IntruderprotectionActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_INTRUDER]), mHelperResourceIDs[POSITION_INTRUDER], intent, QuickHelperActivity.this);
                                break;
                            // 隐私通话
                            case R.drawable.qh_privacy_contact:
                                SDKWrapper.addEvent(QuickHelperActivity.this, SDKWrapper.P1,
                                        "assistant", "assistant_call");
                                intent = new Intent(AppMasterApplication.getInstance(), PrivacyContactActivity.class);
                                intent.putExtra("from_quickhelper", true);
                                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT, PrivacyContactUtils.TO_PRIVACY_CONTACT_FLAG);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                QuickHelperUtils.createQuickHelper(getResources().getString(mHelperNames[POSITION_SECRET_CALL]), mHelperResourceIDs[POSITION_SECRET_CALL], intent, QuickHelperActivity.this);
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
                                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_AD);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                break;
                            default:
                                break;
                        }
                        QuickHelperUtils.createQuickHelper(getResources().getString(mMapDrawableToName.get(drawableId)), drawableId, intent, QuickHelperActivity.this);
                        Toast.makeText(QuickHelperActivity.this, R.string.quick_help_add_toast, Toast.LENGTH_SHORT).show();
                    }
                });
                return view;
            }

            @Override
            public long getItemId(int position) {
                return mFinalDrawableIds.get(position);
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public int getCount() {
                return mFinalDrawableIds.size();
            }
        });
    }
}
