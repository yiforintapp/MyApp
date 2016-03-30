
package com.leo.appmaster.phoneSecurity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.mgr.impl.PrivacyContactManagerImpl;
import com.leo.appmaster.privacycontact.CircleImageView;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.ContactSideBar;
import com.leo.appmaster.privacycontact.ContactSideBar.OnTouchingLetterChangedListener;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class AddSecurityNumberActivity extends BaseActivity implements OnItemClickListener, OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "AddSecurityNumberActivity";
    public static final String EXTERNAL_DATA = "EXTERNAL_DATA";
    private ListView mListContact;
    private ContactAdapter mContactAdapter;
    private List<ContactBean> mPhoneContact;
    private ContactSideBar mContactSideBar;
    private CommonToolbar mTtileBar;
    private List<ContactBean> mAddPrivacyContact;
    private ProgressBar mProgressBar;
    private TextView mDialog;
    private LinearLayout mDefaultText;
    private Button mAddButton;
    private EditText mInputEdit;
    private View mAddRip;
    private SecurAddFromMsmHandler mSecurNumHandler = new SecurAddFromMsmHandler();
    private CheckBox mCheckB;
    //短信备份复选框是否勾选
    private boolean mIsCheckB = true;
    private Button mOpenSecurBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_security_number);
        Intent intent = this.getIntent();
        String extData = intent.getStringExtra(EXTERNAL_DATA);
        mDefaultText = (LinearLayout) findViewById(R.id.add_contact_default_tv);
        mTtileBar = (CommonToolbar) findViewById(R.id.add_privacy_contact_title_bar);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mTtileBar.setOptionMenuVisible(false);
        mOpenSecurBt = (Button) findViewById(R.id.add_bt);
        mOpenSecurBt.setOnClickListener(this);
        if (TextUtils.isEmpty(extData)) {
            mTtileBar.setToolbarTitle(R.string.secur_add_number_title);
        } else {
            mTtileBar.setToolbarTitle(extData);
            mOpenSecurBt.setText(this.getResources().getString(R.string.secur_mody_sure_bt));
        }
        mPhoneContact = new ArrayList<ContactBean>();
        mAddPrivacyContact = new ArrayList<ContactBean>();
        mListContact = (ListView) findViewById(R.id.add_contactLV);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
        /**
         * 初始化sidebar
         */
        mContactSideBar = (ContactSideBar) findViewById(R.id.contact_sidrbar);
        mDialog = (TextView) findViewById(R.id.contact_dialog);
        mContactSideBar.setTextView(mDialog);
        mContactAdapter = new ContactAdapter();
        mListContact.setAdapter(mContactAdapter);
        mListContact.setOnItemClickListener(this);
        mAddButton = (Button) findViewById(R.id.sec_add_number_BT);
        mAddRip = findViewById(R.id.sec_add_number_RP);
        mCheckB = (CheckBox) findViewById(R.id.checkBx);
        mIsCheckB = mCheckB.isChecked();
        mCheckB.setOnCheckedChangeListener(this);
        mInputEdit = (EditText) findViewById(R.id.sec_input_numberEV);
        LostSecurityManagerImpl securityManager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        /*是否添加了防盗号码*/
        String securNumber = securityManager.getPhoneSecurityNumber();
        String[] contact = null;
        if (!Utilities.isEmpty(securNumber)) {
            contact = securNumber.split(":");
        }
        if (contact != null) {
            mInputEdit.setText(contact[1]);
        }
        String number = mInputEdit.getText().toString();
        if (!Utilities.isEmpty(number)) {
            mAddButton.setClickable(true);
            mAddButton.setOnClickListener(AddSecurityNumberActivity.this);
            mAddButton.setBackgroundResource(R.drawable.selector_dialog_blue_button);
            mInputEdit.setClickable(true);
            mAddButton.setTextColor(getResources().getColor(R.color.white));
            mAddRip.setClickable(true);
            mAddRip.setEnabled(true);
        } else {
            mAddButton.setBackgroundResource(R.drawable.grey_btn_selector);
            mAddButton.setClickable(false);
            mInputEdit.setClickable(false);
            mAddButton.setTextColor(getResources().getColor(R.color.c5));
            mAddRip.setClickable(false);
            mAddRip.setEnabled(false);
        }

        mContactSideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mContactAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListContact.setSelection(position);
                }
            }
        });
        sendMsgHandler();
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String number = mInputEdit.getText().toString();
                if (Utilities.isEmpty(number)) {
                    mAddButton.setClickable(false);
                    mAddButton.setBackgroundResource(R.drawable.grey_btn_selector);
                    mInputEdit.setClickable(false);
                    mAddButton.setTextColor(getResources().getColor(R.color.c5));
                    mAddRip.setClickable(false);
                    mAddRip.setEnabled(false);
                } else {
                    mAddButton.setClickable(true);
                    mAddButton.setOnClickListener(AddSecurityNumberActivity.this);
                    mAddButton.setBackgroundResource(R.drawable.selector_dialog_blue_button);
                    mInputEdit.setClickable(true);
                    mAddButton.setTextColor(getResources().getColor(R.color.white));
                    mAddRip.setClickable(true);
                    mAddRip.setEnabled(true);
                }
            }

            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mInputEdit.addTextChangedListener(watcher);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
        mAddPrivacyContact.clear();
        mListContact.post(new Runnable() {
            @Override
            public void run() {
                for (ContactBean contact : mPhoneContact) {
                    if (contact.isCheck()) {
                        contact.setCheck(false);
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_tel_choose");
        ContactBean contact = mPhoneContact.get(position);
        LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        int result = mgr.addPhoneSecurityNumber(contact);
        if (result == PhoneSecurityConstants.ADD_SECUR_NUMBER_SELT) {
            //输入的为本机号码
            Toast.makeText(this, getResources().getString(R.string.secur_add_self_number_tip), Toast.LENGTH_SHORT).show();
        } else if (result == PhoneSecurityConstants.ADD_SECUR_NUMBER_SUCESS) {
            //添加成功
            AddSecurityNumberActivity.this.finish();
        } else {
            //添加失败
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_bt:
                addSecurNumHandler();
                break;
            default:
                break;
        }

    }

    /**
     * 添加预留号码处理
     */
    private void addSecurNumHandler() {

        String number = mInputEdit.getText().toString();
        if (!Utilities.isEmpty(number)) {
            boolean flag = addSecurNumberHandler(number);
            if (flag) {
                Intent intent = new Intent(AddSecurityNumberActivity.this, PhoneSecurityActivity.class);

                LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                //设置手机防盗为开启状态
                mgr.setUsePhoneSecurity(true);
                //设置开启保护的时间
                mgr.setOpenSecurityTime();

                //指定备份
                final String sendNum = number;
                boolean isExistSim = mgr.getIsExistSim();
                if (isExistSim && mIsCheckB) {
                    intent.putExtra(PhoneSecurityActivity.FROM_SECUR_INTENT,PhoneSecurityActivity.FROM_ADD_NUM_MSM);
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                            mgr.sendMessage(sendNum, getSendMessageInstructs(), MTKSendMsmHandler.BACKUP_SECUR_INSTRUCT_ID);
                        }
                    });
                } else {
                    if (mIsCheckB) {
                        String failStr = this.getResources().getString(
                                R.string.privacy_message_item_send_message_fail);
                        Toast.makeText(this, failStr, Toast.LENGTH_SHORT).show();
                    }
                    intent.putExtra(PhoneSecurityActivity.FROM_SECUR_INTENT,PhoneSecurityActivity.FROM_ADD_NUM_NO_MSM);
                }
                try {
                    startActivity(intent);
                    AddSecurityNumberActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                return;
            }
        } else {
            //number is empty
        }
    }

    /*获取发送短信的指令集介绍*/
    private String getSendMessageInstructs() {
        String content = getResources().getString(R.string.secur_backup_msm);
        return content;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkBx:
                mIsCheckB = isChecked;
                break;
            default:
                break;
        }
    }

    private class ContactAdapter extends BaseAdapter implements SectionIndexer {
        LayoutInflater relativelayout;

        public ContactAdapter() {
            relativelayout = LayoutInflater.from(AddSecurityNumberActivity.this);
        }

        @Override
        public int getCount() {

            return (mPhoneContact != null) ? mPhoneContact.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return mPhoneContact.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        class ViewHolder {
            TextView name, number, sortLetter;
            ImageView checkImage;
            CircleImageView contactIcon;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = relativelayout.inflate(R.layout.activity_add_privacy_contact_item,
                        null);
                vh.name = (TextView) convertView.findViewById(R.id.contact_item_nameTV);
                vh.number = (TextView) convertView.findViewById(R.id.contact_item_numberTV);
                vh.checkImage = (ImageView) convertView
                        .findViewById(R.id.contact_item_check_typeIV);
                vh.sortLetter = (TextView) convertView
                        .findViewById(R.id.add_from_contact_sort_letter);
                vh.contactIcon = (CircleImageView) convertView.findViewById(R.id.contactIV);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            ContactBean mb = mPhoneContact.get(position);
            // mContactIcon.setVisibility(View.VISIBLE);
            // 通过position获取分类的首字母
            // int section = getSectionForPosition(position);
            // 通过section来获取第一次出现字母的位置
            // int sectionPosition = getPositionForSection(section);
            // if (position == sectionPosition) {
            // vh.sortLetter.setVisibility(View.VISIBLE);
            // vh.sortLetter.setText(mb.getSortLetter());
            // } else {
            // vh.sortLetter.setVisibility(View.GONE);
            // }
            vh.checkImage.setVisibility(View.GONE);
            vh.name.setText(mb.getContactName());
            vh.number.setText(mb.getContactNumber());
            // vh.imageView.setImageBitmap(mb.getContactIcon());
            if (mb.getContactIcon() != null) {
                vh.contactIcon.setImageBitmap(mb.getContactIcon());
            } else {
                vh.contactIcon.setImageResource(R.drawable.default_user_avatar);
            }
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        /**
         * 根据分类的首字母的其第一次出现该首字母的位置
         */
        @Override
        public int getPositionForSection(int sectionIndex) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mPhoneContact.get(i).getSortLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == sectionIndex) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母
         */
        @Override
        public int getSectionForPosition(int position) {
            return mPhoneContact.get(position).getSortLetter().charAt(0);
        }
    }

    private class SecurAddFromMsmHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PrivacyContactUtils.MSG_ADD_SECURITY_CONTACT:
                    if (msg.obj != null) {
                        LeoLog.i(TAG, "load  contacts list finish !");
                        List<ContactBean> contacts = (List<ContactBean>) msg.obj;
                        if (mPhoneContact != null) {
                            mPhoneContact.clear();
                        }
                        mPhoneContact = contacts;
                        try {
                            if (mPhoneContact != null && mPhoneContact.size() > 0) {
                                mDefaultText.setVisibility(View.GONE);
                                mContactSideBar.setVisibility(View.VISIBLE);
                            } else {
                                mDefaultText.setVisibility(View.VISIBLE);

                            }
                            mProgressBar.setVisibility(View.GONE);
                            mContactAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void sendMsgHandler() {
        if (mSecurNumHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mContactSideBar.setVisibility(View.GONE);
                    List<ContactBean> contactsList = PrivacyContactUtils.getSysContact(AddSecurityNumberActivity.this, null, null, false);
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_ADD_SECURITY_CONTACT;
                    msg.obj = contactsList;
                    mSecurNumHandler.sendMessage(msg);
                }
            });
        }
    }


    /*添加防盗号码处理*/
    private boolean addSecurNumberHandler(String number) {
        boolean flag = false;
        LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        ContactBean addContact = new ContactBean();
        /*格式化号码*/
        addContact.setContactName(number);
        addContact.setContactNumber(number);
        String fromateNumber = PrivacyContactUtils.formatePhoneNumber(number);
        for (ContactBean contactBean : mPhoneContact) {
            String contactNumber = contactBean.getContactNumber();
            contactNumber = PrivacyContactUtils.simpleFromateNumber(contactNumber);
            if (contactNumber.contains(fromateNumber)) {
                addContact.setContactName(contactBean.getContactName());
                break;
            }
        }

        int result = mgr.addPhoneSecurityNumber(addContact);
        if (result == PhoneSecurityConstants.ADD_SECUR_NUMBER_SELT) {
            //输入的为本机号码
            Toast.makeText(this, getResources().getString(R.string.secur_add_self_number_tip), Toast.LENGTH_SHORT).show();
        } else if (result == PhoneSecurityConstants.ADD_SECUR_NUMBER_SUCESS) {
            flag = true;
            //添加成功
        } else {
            //添加失败
        }
        return flag;
    }
}
