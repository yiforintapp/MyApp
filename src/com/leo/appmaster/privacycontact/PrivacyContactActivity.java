
package com.leo.appmaster.privacycontact;

import java.util.List;

import android.content.Intent;
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
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyDeletEditEvent;
import com.leo.appmaster.eventbus.event.PrivacyMessageEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.IconPagerAdapter;
import com.leo.appmaster.ui.LeoPagerTab;

public class PrivacyContactActivity extends BaseFragmentActivity implements OnClickListener {

    private CommonTitleBar mTtileBar;
    private LeoPagerTab mPrivacyContactPagerTab;
    private ViewPager mPrivacyContactViewPager;
    private HomeFragmentHoler[] mFragmentHolders = new HomeFragmentHoler[3];
    private boolean mIsEditModel = false;
    private int pagerPosition;
    private AddPrivacyContactDialog mAddPrivacyContact;
    private boolean mCallLogTip = false;
    private boolean mBackFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_contact_main);
        initUI();
        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
    }

    private void initUI() {
        mTtileBar = (CommonTitleBar) findViewById(R.id.privacy_contact_title_bar);
        mTtileBar.setBackViewListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mBackFlag) {
                    LockManager.getInstatnce().timeFilter(getPackageName(), 500);
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
        Intent intent = getIntent();
        String fromPrivacyFlag = intent.getStringExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT);
        mBackFlag = intent.getBooleanExtra("message_call_notifi", false);
        if (PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG.equals(fromPrivacyFlag)) {
            mTtileBar.setTitle(R.string.privacy_sms);
            mPrivacyContactPagerTab.setCurrentItem(0);
            pagerPosition = 0;
            /* sdk market */
            SDKWrapper.addEvent(this, SDKWrapper.P1, "privacyview", "statusmesg");
        } else if (PrivacyContactUtils.TO_PRIVACY_CALL_FLAG.equals(fromPrivacyFlag)) {
            mTtileBar.setTitle(R.string.privacy_call);
            mPrivacyContactPagerTab.setCurrentItem(1);
            pagerPosition = 1;
            /* sdk market */
            SDKWrapper.addEvent(this, SDKWrapper.P1, "privacyview", "statuscall");
        } else {
            mTtileBar.setTitle(R.string.privacy_contacts);
            mPrivacyContactPagerTab.setCurrentItem(2);
            updateTitle();
        }
        if (!mIsEditModel && pagerPosition == 2) {
            updateTitle();
        }

        mPrivacyContactPagerTab.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                pagerPosition = position;
                if (position == 0) {
                    mTtileBar.setOptionImageVisibility(View.INVISIBLE);
                    if (AppMasterPreference.getInstance(PrivacyContactActivity.this)
                            .getMessageNoReadCount() <= 0) {
                        mFragmentHolders[0].redTip = false;
                        mPrivacyContactPagerTab.notifyDataSetChanged();
                    }
                }
                if (position == 1) {
                    mTtileBar.setOptionImageVisibility(View.INVISIBLE);
                    if (AppMasterPreference.getInstance(PrivacyContactActivity.this)
                            .getCallLogNoReadCount() <= 0) {
                        mCallLogTip = false;
                        mFragmentHolders[1].redTip = false;
                        mPrivacyContactPagerTab.notifyDataSetChanged();
                    }
                }
                if (position == 2) {
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
            mTtileBar.setOptionImageVisibility(View.VISIBLE);
            mTtileBar.setOptionImage(R.drawable.sms_delete);
            mTtileBar.findViewById(R.id.tv_option_image).setBackgroundResource(
                    R.drawable.privacy_title_bt_selecter);
            // 删除
            mTtileBar.setOptionListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyDeletEditEvent(
                                    PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE));

                }
            });
            // 短信恢复
            mTtileBar.findViewById(R.id.message_restore).setVisibility(View.VISIBLE);
            mTtileBar.findViewById(R.id.message_restore_icon).setBackgroundResource(
                    R.drawable.recovery_icon);
            mTtileBar.findViewById(R.id.message_restore).setBackgroundResource(
                    R.drawable.privacy_title_bt_selecter);
            // 恢复
            mTtileBar.findViewById(R.id.message_restore).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyDeletEditEvent(
                                    PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE));

                }
            });
            mTtileBar.setBackViewListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mTtileBar.setHelpSettingVisiblity(View.GONE);
                    chanageEditModel();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyDeletEditEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
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
            mTtileBar.setOptionImageVisibility(View.VISIBLE);
            mTtileBar.setOptionImage(R.drawable.sms_delete);
            mTtileBar.findViewById(R.id.tv_option_image).setBackgroundResource(
                    R.drawable.privacy_title_bt_selecter);
            mTtileBar.setOptionListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyDeletEditEvent(
                                    PrivacyContactUtils.CALL_LOG_EDIT_MODEL_OPERATION_DELETE));

                }
            });
            mTtileBar.setBackViewListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    chanageEditModel();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyDeletEditEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
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
            mTtileBar.setOptionImage(R.drawable.sms_delete);
            mTtileBar.findViewById(R.id.tv_option_image).setBackgroundResource(
                    R.drawable.privacy_title_bt_selecter);
            mTtileBar.setOptionListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyDeletEditEvent(
                                    PrivacyContactUtils.CONTACT_EDIT_MODEL_OPERATION_DELETE));

                }
            });
            mTtileBar.setBackViewListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    chanageEditModel();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyDeletEditEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
                }
            });
        } else if (PrivacyContactUtils.EDIT_MODEL_RESTOR_TO_SMS_CANCEL.equals(event.eventMsg)) {
            // 导入完成结束编辑状态
            if (pagerPosition == 0) {
                mTtileBar.setHelpSettingVisiblity(View.GONE);
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
            mTtileBar.findViewById(R.id.message_restore).setVisibility(View.VISIBLE);
            mTtileBar.findViewById(R.id.message_restore_icon).setBackgroundResource(
                    R.drawable.un_recovery_icon);
            mTtileBar.findViewById(R.id.message_restore).setOnClickListener(null);
            mTtileBar.findViewById(R.id.message_restore).setBackgroundResource(0);
            mTtileBar.setBackViewListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mTtileBar.setHelpSettingVisiblity(View.GONE);
                    chanageEditModel();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyDeletEditEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
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
        mTtileBar.setOptionImageVisibility(View.VISIBLE);
        mTtileBar.setOptionImage(R.drawable.un_delete);
        mTtileBar.setOptionListener(null);
        mTtileBar.findViewById(R.id.tv_option_image).setBackgroundResource(0);
        mTtileBar.setBackViewListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                chanageEditModel();
                LeoEventBus.getDefaultBus().post(
                        new PrivacyDeletEditEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
            }
        });
    }

    public void onEventMainThread(PrivacyDeletEditEvent event) {
        if (PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION
                .equals(event.editModel)) {
            // if (pagerPosition != 1) {
            mFragmentHolders[1].redTip = true;
            mPrivacyContactPagerTab.notifyDataSetChanged();
            mCallLogTip = true;
            // }
        } else if (PrivacyContactUtils.PRIVACY_RECEIVER_MESSAGE_NOTIFICATION
                .equals(event.editModel)) {
            // if (pagerPosition != 0) {
            if (AppMasterPreference.getInstance(this).getMessageNoReadCount() > 0) {
                mFragmentHolders[0].redTip = true;
                mPrivacyContactPagerTab.notifyDataSetChanged();
            }
        } else if (PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CANCEL_RED_TIP_EVENT
                .equals(event.editModel)) {
            mFragmentHolders[0].redTip = false;
            mPrivacyContactPagerTab.notifyDataSetChanged();
        } else if (PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CALL_LOG_CANCEL_RED_TIP_EVENT
                .equals(event.editModel)) {
            mFragmentHolders[1].redTip = false;
            mPrivacyContactPagerTab.notifyDataSetChanged();
        }
    }

    // 去除编辑模式
    private void chanageEditModel() {
        mPrivacyContactPagerTab.setVisibility(View.VISIBLE);
        if (pagerPosition != 2) {
            mTtileBar.setOptionImageVisibility(View.INVISIBLE);
        }
        mPrivacyContactViewPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return false;
            }
        });
        mIsEditModel = false;
        mTtileBar.openBackView();
        updateTitle();
    }

    // 显示添加按钮
    private void updateTitle() {
        mTtileBar.findViewById(R.id.message_restore).setVisibility(View.GONE);
        if (pagerPosition == 2) {
            mTtileBar.setOptionImageVisibility(View.VISIBLE);
        }
        mTtileBar.setOptionImage(R.drawable.add_contacts_btn);
        mTtileBar.findViewById(R.id.tv_option_image).setBackgroundResource(
                R.drawable.privacy_title_bt_selecter);
        mTtileBar.setOptionListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showAddContentDialog();
            }
        });
    }

    private void initFragment() {
        /**
         * Message
         */
        HomeFragmentHoler holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.privacy_contact_message);
        PrivacyMessageFragment messageFragment = new PrivacyMessageFragment();
        messageFragment.setContent(holder.title);
        holder.fragment = messageFragment;
        mFragmentHolders[0] = holder;
        if (AppMasterPreference.getInstance(this).getMessageNoReadCount() > 0) {
            mFragmentHolders[0].redTip = true;
        }
        /**
         * CallLog
         */
        holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.privacy_contact_calllog);
        PrivacyCalllogFragment pravicyCalllogFragment = new PrivacyCalllogFragment();
        pravicyCalllogFragment.setContent(holder.title);
        holder.fragment = pravicyCalllogFragment;
        mFragmentHolders[1] = holder;
        if (AppMasterPreference.getInstance(this).getCallLogNoReadCount() > 0) {
            mFragmentHolders[1].redTip = true;
        }
        /**
         * Contact
         */
        holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.privacy_contact_contact);
        PrivacyContactFragment appManagerFragment = new
                PrivacyContactFragment();
        appManagerFragment.setContent(holder.title);
        holder.fragment = appManagerFragment;
        mFragmentHolders[2] = holder;

        // AM-614, remove cached fragments
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
                mTtileBar.setHelpSettingVisiblity(View.GONE);
            }
            chanageEditModel();
            LeoEventBus.getDefaultBus().post(
                    new PrivacyDeletEditEvent(PrivacyContactUtils.CANCEL_EDIT_MODEL));
        } else {
            if (mBackFlag) {
                LockManager.getInstatnce().timeFilter(getPackageName(), 500);
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
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean getRedTip(int position) {
            // TODO Auto-generated method stub
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

}
