
package com.leo.appmaster.phoneSecurity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.leo.appmaster.privacycontact.CircleImageView;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.ContactSideBar;
import com.leo.appmaster.privacycontact.ContactSideBar.OnTouchingLetterChangedListener;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEORoundProgressDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class AddSecurityNumberActivity extends BaseActivity implements OnItemClickListener, OnClickListener {
    private static final String TAG = "AddSecurityNumberActivity";
    private ListView mListContact;
    private ContactAdapter mContactAdapter;
    private List<ContactBean> mPhoneContact;
    private ContactSideBar mContactSideBar;
    private CommonToolbar mTtileBar;
    private List<ContactBean> mAddPrivacyContact;
    private Handler mHandler;
    private LEORoundProgressDialog mProgressDialog;
    private ProgressBar mProgressBar;
    private TextView mDialog;
    private boolean mLogFlag = false;
    private LinearLayout mDefaultText;
    private Button mAddButton;
    private EditText mInputEdit;
    private View mAddRip;
    private SecurAddFromMsmHandler mSecurNumHandler = new SecurAddFromMsmHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_security_number);
        mDefaultText = (LinearLayout) findViewById(R.id.add_contact_default_tv);
        mTtileBar = (CommonToolbar) findViewById(R.id.add_privacy_contact_title_bar);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mTtileBar.setOptionMenuVisible(false);
        mTtileBar.setToolbarTitle(R.string.secur_add_number_title);
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
//        MaterialRippleLayout.on(mAddButton)
//                .rippleColor(getResources().getColor(R.color.button_blue_ripple))
//                .rippleAlpha(0.1f)
//                .rippleHover(true)
//                .create();
        mAddRip =  findViewById(R.id.sec_add_number_RP);

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

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int currentValue = msg.what;
                if (currentValue >= mAddPrivacyContact.size()) {
                    if (!mLogFlag) {
                        if (mProgressDialog != null) {
                            mProgressDialog.cancel();
                        }
                        AddSecurityNumberActivity.this.finish();
                    } else {
                        if (mProgressDialog != null) {
                            mProgressDialog.cancel();
                        }
                        mLogFlag = false;
                    }
                } else {
                    mProgressDialog.setProgress(currentValue);
                }
                super.handleMessage(msg);
            }
        };
        mContactSideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mContactAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListContact.setSelection(position);
                }
            }
        });
//        AddContactAsyncTask addContacctTask = new AddContactAsyncTask();
//        addContacctTask.execute(true);
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
        mHandler = null;
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
        if (result == 1) {
            //输入的为本机号码
            Toast.makeText(this, getResources().getString(R.string.secur_add_self_number_tip), Toast.LENGTH_SHORT).show();
        } else if (result == 2) {
            //添加成功
            AddSecurityNumberActivity.this.finish();
        } else {
            //添加失败
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sec_add_number_BT:
                String number = mInputEdit.getText().toString();
                if (!Utilities.isEmpty(number)) {
                    addSecurNumberHandler(number);
                }
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_tel_input");
                break;
        }

    }

    @SuppressLint("CutPasteId")
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
        @SuppressLint("DefaultLocale")
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
                        List<ContactBean> calls = (List<ContactBean>) msg.obj;
                        if (mPhoneContact != null) {
                            mPhoneContact.clear();
                        }
                        mPhoneContact = calls;
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
    private class AddContactAsyncTask extends AsyncTask<Boolean, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mContactSideBar.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Boolean... arg0) {
            boolean flag = arg0[0];
            if (flag) {
                mPhoneContact =
                        PrivacyContactUtils.getSysContact(AddSecurityNumberActivity.this, null, null, false);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mPhoneContact != null && mPhoneContact.size() > 0) {
                mDefaultText.setVisibility(View.GONE);
                mContactSideBar.setVisibility(View.VISIBLE);
            } else {
                mDefaultText.setVisibility(View.VISIBLE);

            }
            mProgressBar.setVisibility(View.GONE);
            mContactAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }

    private void showProgressDialog(int maxValue, int currentValue) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEORoundProgressDialog(this);
        }
        String title = getResources().getString(R.string.privacy_contact_progress_dialog_title);
        String content = getResources().getString(R.string.privacy_contact_progress_dialog_content);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(content);
        mProgressDialog.setMax(maxValue);
        mProgressDialog.setProgress(currentValue);
        mProgressDialog.setCustomProgressTextVisiable(true);
        mProgressDialog.setButtonVisiable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    /*添加防盗号码处理*/
    private void addSecurNumberHandler(String number) {
        LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        ContactBean addContact = new ContactBean();
        addContact.setContactName(number);
        addContact.setContactNumber(number);
        String fromateNumber = PrivacyContactUtils.formatePhoneNumber(number);
        for (ContactBean contactBean : mPhoneContact) {
            if (contactBean.getContactNumber().contains(fromateNumber)) {
                addContact.setContactName(contactBean.getContactName());
                break;
            }
        }
        int result = mgr.addPhoneSecurityNumber(addContact);
        if (result == 1) {
            //输入的为本机号码
            Toast.makeText(this, getResources().getString(R.string.secur_add_self_number_tip), Toast.LENGTH_SHORT).show();
        } else if (result == 2) {
            //添加成功
            AddSecurityNumberActivity.this.finish();
        } else {
            //添加失败
        }

    }

}
