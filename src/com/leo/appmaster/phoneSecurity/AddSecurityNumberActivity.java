
package com.leo.appmaster.phoneSecurity;

import android.content.Intent;
import android.graphics.Paint;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private EditText mInputEdit;
    private View mAddRip;
    private SecurAddFromMsmHandler mSecurNumHandler = new SecurAddFromMsmHandler();
    private CheckBox mCheckB;
    //短信备份复选框是否勾选
    private boolean mIsCheckB = true;
    private Button mOpenSecurBt;
    private Animation mAddSecurNumberAnim;
    private TextView mTopTipInstrTv;
    private boolean mIsModfyNum;
    private boolean mIsInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_security_number);
        Intent intent = this.getIntent();
        String extData = intent.getStringExtra(EXTERNAL_DATA);
        mDefaultText = (LinearLayout) findViewById(R.id.add_contact_default_tv);
        mTtileBar = (CommonToolbar) findViewById(R.id.add_privacy_contact_title_bar);
        mTtileBar.setToolbarColorResource(R.color.ctc);
        mTtileBar.setOptionMenuVisible(false);
        mOpenSecurBt = (Button) findViewById(R.id.add_bt);
        mOpenSecurBt.setOnClickListener(this);
        if (TextUtils.isEmpty(extData)) {
            mTtileBar.setToolbarTitle(R.string.secur_add_number_title);
            mIsModfyNum = false;
        } else {
            mTtileBar.setToolbarTitle(extData);
            mOpenSecurBt.setText(this.getResources().getString(R.string.secur_mody_mum_sure));
            mIsModfyNum = true;
        }
        mPhoneContact = new ArrayList<ContactBean>();
        mAddPrivacyContact = new ArrayList<ContactBean>();
        mListContact = (ListView) findViewById(R.id.add_contactLV);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(this.getResources().getLayout(R.layout.add_secur_num_header), null);
        mListContact.addHeaderView(inflate);
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
        mCheckB = (CheckBox) findViewById(R.id.checkBx);
        mIsCheckB = mCheckB.isChecked();
        mCheckB.setOnCheckedChangeListener(this);
        mInputEdit = (EditText) findViewById(R.id.sec_input_numberEV);
        mTopTipInstrTv = (TextView) findViewById(R.id.top_tip_instr_tv);
        mTopTipInstrTv.setOnClickListener(this);
        mTopTipInstrTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
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
        ContactBean contact = mPhoneContact.get(position);
        if (!TextUtils.isEmpty(contact.getContactNumber())) {
            mInputEdit.setText(contact.getContactNumber());
            mIsInput = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_bt:
                addSecurNumHandler();
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_tel_enable");
                break;
            case R.id.top_tip_instr_tv:
                Intent intentDet = new Intent(AddSecurityNumberActivity.this, SecurityDetailActivity.class);
                try {
                    startActivity(intentDet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                if (mIsInput) {
                    //点击联系人输入
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_tel_choose");
                } else {
                    //手动输入
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_tel_input");
                }
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
                    if (!mIsModfyNum) {
                        intent.putExtra(PhoneSecurityActivity.FROM_SECUR_INTENT, PhoneSecurityActivity.FROM_ADD_NUM_MSM);
                    }
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
                    if (!mIsModfyNum) {
                        intent.putExtra(PhoneSecurityActivity.FROM_SECUR_INTENT, PhoneSecurityActivity.FROM_ADD_NUM_NO_MSM);
                    }
                }
                try {
                    startActivity(intent);
                    AddSecurityNumberActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mIsModfyNum = false;
                }

            } else {
                return;
            }
        } else {
            //number is empty
            shakeAddSecurButton(mInputEdit);
            PhoneSecurityUtils.setVibrate(this, 200);
            String toastTip = getResources().getString(R.string.no_add_secur_number_toast_tip);
            Toast.makeText(this, toastTip, Toast.LENGTH_SHORT).show();
        }
        mIsInput = false;
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
                if (isChecked == false) {
                    SDKWrapper.addEvent(AddSecurityNumberActivity.this, SDKWrapper.P1, "theft", "theft_backup_off");
                }
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

    /*未填写防盗号码，摇晃动画*/
    private void shakeAddSecurButton(View view) {

        if (mAddSecurNumberAnim == null) {
            mAddSecurNumberAnim = AnimationUtils.loadAnimation(this,
                    R.anim.left_right_shake);
        } else {
            mAddSecurNumberAnim.cancel();
        }
        view.startAnimation(mAddSecurNumberAnim);
    }
}
