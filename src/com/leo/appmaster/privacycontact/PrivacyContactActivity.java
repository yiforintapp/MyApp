
package com.leo.appmaster.privacycontact;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.eventbus.event.PrivacyMessageEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.IconPagerAdapter;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.utils.Utilities;

import java.util.List;

public class PrivacyContactActivity extends BaseFragmentActivity implements OnClickListener {
    private static final String TAG = "PrivacyContactActivity";

    private static final int INDEX_CALL_LOG;
    private static final int INDEX_MESSAGE;
    private static final int INDEX_CONTACTS;

    static {
        // 4.4及其以上不显示短信tab
        if (Build.VERSION.SDK_INT < 19) {
            INDEX_MESSAGE = 0;
            INDEX_CALL_LOG = 1;
            INDEX_CONTACTS = 2;
        } else {
            INDEX_MESSAGE = -1;
            INDEX_CALL_LOG = 0;
            INDEX_CONTACTS = 1;
        }
    }

    private CommonToolbar mTtileBar;
    private LeoPagerTab mPrivacyContactPagerTab;
    private ViewPager mPrivacyContactViewPager;
    private HomeFragmentHoler[] mFragmentHolders = new HomeFragmentHoler[INDEX_CONTACTS + 1];
    private boolean mIsEditModel = false;
    private int pagerPosition;
    private AddPrivacyContactDialog mAddPrivacyContact;
    private boolean mCallLogTip = false;
    private boolean mBackFlag = false;
    /* swipe到隐私联系人页面 */
    public static final int EVENT_ISWIPE_TO_PRIVACY_CONTACT = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_contact_main);
        
        initUI();
        LeoEventBus.getDefaultBus().register(this);
        // PrivacyContactManager.getInstance(mContext).mIsOpenPrivacyContact =
        // true;
        iswipToPrivacyContactHandler();
        toPrivacyContactHandler();
    }

    private void iswipToPrivacyContactHandler() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        if (amp.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
            finish();
            String flag = getIntent().getStringExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT);
            Intent intent = new Intent(this, LockSettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, EVENT_ISWIPE_TO_PRIVACY_CONTACT);
            if (PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG.equals(flag)) {
                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                        PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
            } else if (PrivacyContactUtils.TO_PRIVACY_CALL_FLAG.equals(flag)) {
                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                        PrivacyContactUtils.TO_PRIVACY_CALL_FLAG);
            }
            startActivity(intent);
        }
    }

//    @Override
//    public void onEnterAnimationComplete() {
//        super.onEnterAnimationComplete();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
        // uninitLoadData();
    }

    private void initUI() {
        mTtileBar = (CommonToolbar) findViewById(R.id.privacy_contact_title_bar);
        mTtileBar.setToolbarColorResource(R.color.ctc);
        mTtileBar.setNavigationClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mBackFlag) {
                    LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                    lm.filterPackage(getPackageName(), 500);
                    Intent intent = new Intent(PrivacyContactActivity.this, HomeActivity.class);
                    try {
                        startActivity(intent);
                        PrivacyContactActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    PrivacyContactActivity.this.finish();
                }
            }
        });
        mPrivacyContactPagerTab = (LeoPagerTab)
                findViewById(R.id.privacy_contact_tab_indicator);
        mPrivacyContactViewPager = (ViewPager)
                findViewById(R.id.privacy_contact_viewpager);
        initFragment();
        PrivacyContactAdapter adapter = new PrivacyContactAdapter(getSupportFragmentManager());
        mPrivacyContactViewPager.setAdapter(adapter);
        mPrivacyContactViewPager.setOffscreenPageLimit(2);
        mPrivacyContactPagerTab.setViewPager(mPrivacyContactViewPager);
        mPrivacyContactPagerTab.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                pagerPosition = position;
                if (position == INDEX_MESSAGE) {
                    mTtileBar.setOptionMenuVisible(false);
                    if (AppMasterPreference.getInstance(PrivacyContactActivity.this)
                            .getMessageNoReadCount() <= 0) {
                        mFragmentHolders[INDEX_MESSAGE].redTip = false;
                        mPrivacyContactPagerTab.notifyDataSetChanged();
                    }
                }
                if (position == INDEX_CALL_LOG) {
                    mTtileBar.setOptionMenuVisible(false);
                    if (AppMasterPreference.getInstance(PrivacyContactActivity.this)
                            .getCallLogNoReadCount() <= 0) {
                        mCallLogTip = false;
                        mFragmentHolders[INDEX_CALL_LOG].redTip = false;
                        mPrivacyContactPagerTab.notifyDataSetChanged();
                    }
                }
                if (position == INDEX_CONTACTS) {
                    updateTitle();
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void toPrivacyContactHandler() {
        String fromPrivacyFlag = getIntent().getStringExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT);
        if (Utilities.isEmpty(fromPrivacyFlag)) {
            fromPrivacyFlag = PrivacyContactUtils.TO_PRIVACY_CONTACT_TAB;
        }
        mBackFlag = getIntent().getBooleanExtra("message_call_notifi", false);
        if (PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG.equals(fromPrivacyFlag)) {
            if(getIntent().getBooleanExtra("from_quickhelper", false)){
                SDKWrapper.addEvent(this, SDKWrapper.P1,
                        "assistant", "sms_cnts");
            }
            mTtileBar.setToolbarTitle(R.string.privacy_sms);
            mPrivacyContactPagerTab.setCurrentItem(0);
            pagerPosition = 0;
            /* sdk market */
            SDKWrapper.addEvent(this, SDKWrapper.P1, "privacyview", "statusmesg");
        } else if (PrivacyContactUtils.TO_PRIVACY_CALL_FLAG.equals(fromPrivacyFlag)) {
            if(getIntent().getBooleanExtra("from_quickhelper", false)){
                SDKWrapper.addEvent(this, SDKWrapper.P1,
                        "assistant", "call_cnts");
            }
            mTtileBar.setToolbarTitle(R.string.privacy_call);
            mPrivacyContactPagerTab.setCurrentItem(1);
            pagerPosition = 1;
            /* sdk market */
            SDKWrapper.addEvent(this, SDKWrapper.P1, "privacyview", "statuscall");
        } else {
            mTtileBar.setToolbarTitle(R.string.privacy_contacts);
            pagerPosition = 2;
            mPrivacyContactPagerTab.setCurrentItem(2);
            updateTitle();
        }
        if (!mIsEditModel && pagerPosition == 2) {
            updateTitle();
        }
//        getIntent().removeExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT);
//        getIntent().removeExtra("message_call_notifi");
    }

    /**
     * dont change this method
     *
     * @param event
     */
    public void onEventMainThread(PrivacyMessageEvent event) {
        if (PrivacyContactUtils.FROM_MESSAGE_EVENT.equals(event.eventMsg)) {
            mIsEditModel = true;
            mPrivacyContactPagerTab.setVisibility(View.GONE);
            mPrivacyContactViewPager.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    return true;
                }
            });
            // 短信删除
            mTtileBar.setOptionMenuVisible(true);
            mTtileBar.setOptionImageResource(R.drawable.sms_delete);
//            mTtileBar.findViewById(R.id.tv_option_image).setBackgroundResource(
//                    R.drawable.privacy_title_bt_selecter);
            // 删除
            mTtileBar.setOptionClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyEditFloatEvent(
                                    PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE));

                }
            });
            // 短信恢复
//            mTtileBar.findViewById(R.id.message_restore).setVisibility(View.VISIBLE);
//            mTtileBar.findViewById(R.id.message_restore_icon).setBackgroundResource(
//                    R.drawable.recovery_icon);
//            mTtileBar.findViewById(R.id.message_restore).setBackgroundResource(
//                    R.drawable.privacy_title_bt_selecter);
            mTtileBar.setSecOptionMenuVisible(true);
            mTtileBar.setSecOptionImageResource(R.drawable.recovery_icon);

            // 恢复
            mTtileBar.setSecOptionClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyEditFloatEvent(
                                    PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE));

                }
            });
            mTtileBar.setNavigationClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mTtileBar.setSecOptionMenuVisible(false);
                    chanageEditModel();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyEditFloatEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
                }
            });
        } else if (PrivacyContactUtils.FROM_CALL_LOG_EVENT.equals(event.eventMsg)) {
            mIsEditModel = true;
            mPrivacyContactPagerTab.setVisibility(View.GONE);
            mPrivacyContactViewPager.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    return true;
                }
            });
            // 通话记录编辑模式
            mTtileBar.setOptionMenuVisible(true);
            mTtileBar.setOptionImageResource(R.drawable.sms_delete);
            mTtileBar.setOptionClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyEditFloatEvent(
                                    PrivacyContactUtils.CALL_LOG_EDIT_MODEL_OPERATION_DELETE));

                }
            });
            mTtileBar.setNavigationClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    chanageEditModel();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyEditFloatEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
                }
            });
        } else if (PrivacyContactUtils.FROM_CONTACT_EVENT.equals(event.eventMsg)) {
            mIsEditModel = true;
            mPrivacyContactPagerTab.setVisibility(View.GONE);
            mPrivacyContactViewPager.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    return true;
                }
            });
            // 联系人编辑模式
            mTtileBar.setOptionImageResource(R.drawable.sms_delete);

            mTtileBar.setOptionClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyEditFloatEvent(
                                    PrivacyContactUtils.CONTACT_EDIT_MODEL_OPERATION_DELETE));
                }
            });
            mTtileBar.setNavigationClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    chanageEditModel();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyEditFloatEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
                }
            });
        } else if (PrivacyContactUtils.EDIT_MODEL_RESTOR_TO_SMS_CANCEL.equals(event.eventMsg)) {
            // 导入完成结束编辑状态
            if (pagerPosition == 0) {
                mTtileBar.setSecOptionMenuVisible(false);
            }
            chanageEditModel();
        } else if (PrivacyContactUtils.ADD_CONTACT_FROM_CONTACT_NO_REPEAT_EVENT
                .equals(event.eventMsg)) {
            Toast.makeText(PrivacyContactActivity.this,
                    getResources().getString(R.string.privacy_add_contact_toast),
                    Toast.LENGTH_SHORT).show();
        } else if (PrivacyContactUtils.FROM_CONTACT_NO_SELECT_EVENT.equals(event.eventMsg)) {
            setNoSelectImage();
        } else if (PrivacyContactUtils.FROM_MESSAGE_NO_SELECT_EVENT.equals(event.eventMsg)) {
            setNoSelectImage();
//            mTtileBar.findViewById(R.id.message_restore).setVisibility(View.VISIBLE);
//            mTtileBar.findViewById(R.id.message_restore_icon).setBackgroundResource(
//                    R.drawable.un_recovery_icon);
//            mTtileBar.findViewById(R.id.message_restore).setOnClickListener(null);
//            mTtileBar.findViewById(R.id.message_restore).setBackgroundResource(0);
            mTtileBar.setSecOptionMenuVisible(true);
            mTtileBar.setSecOptionImageResource(0);
            mTtileBar.setSecOptionImageResource(R.drawable.un_recovery_icon);
            mTtileBar.setSecOptionClickListener(null);
            mTtileBar.setNavigationClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mTtileBar.setSecOptionMenuVisible(false);
                    chanageEditModel();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyEditFloatEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
                }
            });

        }

    }

    public void setNoSelectImage() {
        mIsEditModel = true;
        mPrivacyContactPagerTab.setVisibility(View.GONE);
        mPrivacyContactViewPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        });
        mTtileBar.setOptionMenuVisible(true);
        mTtileBar.setOptionImageResource(R.drawable.un_delete);
        mTtileBar.setOptionClickListener(null);
//        mTtileBar.findViewById(R.id.tv_option_image).setBackgroundResource(0);
        mTtileBar.setNavigationClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                chanageEditModel();
                LeoEventBus.getDefaultBus().post(
                        new PrivacyEditFloatEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
            }
        });
    }

    public void onEventMainThread(PrivacyEditFloatEvent event) {
        if (PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION
                .equals(event.editModel)) {
            // if (pagerPosition != 1) {
            mFragmentHolders[INDEX_CALL_LOG].redTip = true;
            mPrivacyContactPagerTab.notifyDataSetChanged();
            mCallLogTip = true;
            // }
        } else if (PrivacyContactUtils.PRIVACY_RECEIVER_MESSAGE_NOTIFICATION
                .equals(event.editModel)) {
            // if (pagerPosition != 0) {
            if (AppMasterPreference.getInstance(this).getMessageNoReadCount() > 0) {
                mFragmentHolders[INDEX_MESSAGE].redTip = true;
                mPrivacyContactPagerTab.notifyDataSetChanged();
            }
        } else if (PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CANCEL_RED_TIP_EVENT
                .equals(event.editModel)) {
            int msmNoReadCount = AppMasterPreference.getInstance(PrivacyContactActivity.this).getMessageNoReadCount();
            if (msmNoReadCount <= 0) {
                mFragmentHolders[INDEX_MESSAGE].redTip = false;
                mPrivacyContactPagerTab.notifyDataSetChanged();
            }
        } else if (PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CALL_LOG_CANCEL_RED_TIP_EVENT
                .equals(event.editModel)) {
            int callNoReadCount = AppMasterPreference.getInstance(PrivacyContactActivity.this).getCallLogNoReadCount();
            if (callNoReadCount <= 0) {
                mFragmentHolders[INDEX_CALL_LOG].redTip = false;
                mPrivacyContactPagerTab.notifyDataSetChanged();
            }
        }
    }

    // 去除编辑模式
    private void chanageEditModel() {
        mPrivacyContactPagerTab.setVisibility(View.VISIBLE);
        if (pagerPosition != 2) {
            mTtileBar.setOptionMenuVisible(false);
        }
        mPrivacyContactViewPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return false;
            }
        });
        mIsEditModel = false;
        mTtileBar.setNavigationClickListener(null);
        updateTitle();
    }

    // 显示添加按钮
    private void updateTitle() {
        if (pagerPosition == INDEX_CONTACTS) {
            mTtileBar.setOptionMenuVisible(true);
        }
        mTtileBar.setOptionImageResource(R.drawable.add_contacts_btn);

        mTtileBar.setOptionClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showAddContentDialog();
            }
        });
    }

    private void initFragment() {
        int index = 0;
        HomeFragmentHoler holder = null;
        if (Build.VERSION.SDK_INT < 19) {
            /**
             * Message
             */
            holder = new HomeFragmentHoler();
            holder.title = this.getString(R.string.privacy_contact_message);
            PrivacyMessageFragment messageFragment = new PrivacyMessageFragment();
            // messageFragment.setContent(holder.title);
            holder.fragment = messageFragment;
            mFragmentHolders[index] = holder;
            if (AppMasterPreference.getInstance(this).getMessageNoReadCount() > 0) {
                mFragmentHolders[index].redTip = true;
            }
            index++;
        }
        /**
         * CallLog
         */
        holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.privacy_contact_calllog);
        PrivacyCalllogFragment pravicyCalllogFragment = new PrivacyCalllogFragment();
        // pravicyCalllogFragment.setContent(holder.title);
        holder.fragment = pravicyCalllogFragment;
        mFragmentHolders[index] = holder;
        if (AppMasterPreference.getInstance(this).getCallLogNoReadCount() > 0) {
            mFragmentHolders[index].redTip = true;
        }
        index++;

        /**
         * Contact
         */
        holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.privacy_contact_contact);
        PrivacyContactFragment appManagerFragment = new
                PrivacyContactFragment();
        // appManagerFragment.setContent(holder.title);
        holder.fragment = appManagerFragment;
        mFragmentHolders[index] = holder;

        FragmentManager fm = getSupportFragmentManager();
        try {
            FragmentTransaction ft = fm.beginTransaction();
            List<Fragment> list = fm.getFragments();
            if (list != null) {
                for (Fragment f : fm.getFragments()) {
                    ft.remove(f);
                }
            }
            ft.commit();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrivacyContactManager.getInstance(PrivacyContactActivity.this).mIsOpenPrivacyContact = true;
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsEditModel) {
            if (pagerPosition == 0) {
                mTtileBar.setSecOptionMenuVisible(false);
            }
            chanageEditModel();
            LeoEventBus.getDefaultBus().post(
                    new PrivacyEditFloatEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
        } else {
            if (mBackFlag) {
                LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                lm.filterPackage(getPackageName(), 500);
                Intent intent = new Intent(PrivacyContactActivity.this, HomeActivity.class);
                try {
                    startActivity(intent);
                    PrivacyContactActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onClick(View v) {
    }

    class HomeFragmentHoler {
        String title;
        boolean redTip;
        BaseFragment fragment;
    }

    class PrivacyContactAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
        public PrivacyContactAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentHolders[position].fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentHolders[position].title;
        }

        @Override
        public int getCount() {
            return mFragmentHolders.length;
        }

        @Override
        public int getIconResId(int index) {
            return 0;
        }

        @Override
        public boolean getRedTip(int position) {
            return mFragmentHolders[position].redTip;
        }

    }

    public void showAddContentDialog() {

        if (mAddPrivacyContact == null) {
            mAddPrivacyContact = new AddPrivacyContactDialog(this);
        }
        // 通话记录添加
        mAddPrivacyContact.setCallLogListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(PrivacyContactActivity.this,
                        AddFromCallLogListActivity.class);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
                mAddPrivacyContact.cancel();
            }
        });
        // 联系人列表添加
        mAddPrivacyContact.setContactListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(PrivacyContactActivity.this,
                        AddFromContactListActivity.class);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
                mAddPrivacyContact.cancel();
            }
        });
        // 短信列表添加
        mAddPrivacyContact.setSmsListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(PrivacyContactActivity.this,
                        AddFromMessageListActivity.class);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
                mAddPrivacyContact.cancel();
            }
        });
        // 手动输入
        mAddPrivacyContact.setInputListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /* SDK */
                SDKWrapper.addEvent(PrivacyContactActivity.this, SDKWrapper.P1, "contactsadd",
                        "handadd");
                Intent intent = new Intent(PrivacyContactActivity.this,
                        PrivacyContactInputActivity.class);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
                mAddPrivacyContact.cancel();
            }
        });
        mAddPrivacyContact.show();
    }

    private void scrollToContactList() {
    }
}
