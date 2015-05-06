
package com.leo.appmaster.feedback;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;


public class FeedbackActivity extends BaseActivity implements OnClickListener, OnFocusChangeListener {

    private static final String EMAIL_EXPRESSION = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    
    private View mBtnCommit;
    private EditText mEditContent;
    private EditText mEditEmail;
    private ImageView mEmailImg;
    private View mEmailLayout;
    private TextView mCategory;
    private ImageView mCategoryImg;
    private View mCategoryLayout;
    private CommonTitleBar mTitleBar;
    
    private Dialog mCategoryDialog;
    private ListView mCategoryListView;
    private LEOMessageDialog mMessageDialog;

    private final static int[] sCategoryIds = {
        R.string.category_lock, R.string.pravicy_protect,
        R.string.app_manager, R.string.category_other
    };

    private final ArrayList<String> mCategories = new ArrayList<String>();
    private final ArrayList<String> mEmails = new ArrayList<String>();
    
    private int mCategoryPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initEmails();
        
        initUi();

        // check if any data not submitted
        checkPendingData();
    }

    private void initEmails() {
        AccountManager am = AccountManager.get(getApplicationContext());
        Account[] accounts = am.getAccounts();
        for(Account a : accounts) {
            if(a.name != null && a.name.matches(EMAIL_EXPRESSION) && !mEmails.contains(a.name)) {
                mEmails.add(a.name);
            }
        }
    }

    private void initUi() {
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTitleBar.setTitle(R.string.feedback);
        mTitleBar.openBackView();
        mTitleBar.setOptionImage(R.drawable.mode_done);
        mTitleBar.setOptionListener(this);
        mBtnCommit = mTitleBar.getOptionImageView();
        
        mEditContent = (EditText) findViewById(R.id.feedback_content);
        mEmailLayout = findViewById(R.id.feedback_email_layout);
        mEditEmail = (EditText) findViewById(R.id.feedback_email);
        mEditEmail.setOnFocusChangeListener(this);
        mEmailImg =  (ImageView) findViewById(R.id.feedback_email_arrow);
        mEmailImg.setOnClickListener(this);
        mEmailImg.setVisibility(mEmails.size() > 1 ? View.VISIBLE : View.GONE);
        
        mCategoryLayout = findViewById(R.id.feedback_category_layout);
        mCategoryLayout.setOnClickListener(this);
        mCategory = (TextView) findViewById(R.id.feedback_category_title);
        mCategoryImg = (ImageView) findViewById(R.id.feedback_category_arrow);
        for (int i = 0; i < sCategoryIds.length; i++) {
            mCategories.add(getString(sCategoryIds[i]));
        }
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                checkCommitable();
            }
        };
        mEditEmail.addTextChangedListener(textWatcher);
        mEditContent.addTextChangedListener(textWatcher);
        mEditContent.requestFocus();
    }

    private void checkPendingData() {
        SharedPreferences perference = PreferenceManager.getDefaultSharedPreferences(this);
        mEditContent.setText(perference.getString(FeedbackHelper.KEY_CONTENT, ""));
        String email = "";
        if(mEmails.size() > 0) {
            email = mEmails.get(0);
        }
        String currentEmail = perference.getString(FeedbackHelper.KEY_EMAIL, email);
        mEditEmail.setText(currentEmail.isEmpty() ? email : currentEmail);
        try {
            mCategoryPos = perference.getInt(FeedbackHelper.KEY_CATEGORY, -1);
        } catch(Exception e) {
           
        }
        if (mCategoryPos >= 0 && mCategoryPos < mCategories.size()) {
            mCategory.setText(mCategories.get(mCategoryPos));
            mCategory.setTag(1);
        }
    }

    private void checkCommitable() {
        boolean commitable = true;
        if (mCategory.getTag() == null) {
            commitable = false;
        } else {
            String email = mEditEmail.getText().toString().trim();
            if (email.isEmpty()) {
                commitable = false;
            } else {
                String content = mEditContent.getText().toString().trim();
                if (content.isEmpty()) {
                    commitable = false;
                }
            }
        }
        mBtnCommit.setEnabled(commitable);
        mTitleBar.setOptionImage(commitable ? R.drawable.mode_done
                : R.drawable.undone);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMessageDialog != null) {
            mMessageDialog.dismiss();
            mMessageDialog = null;
        }
        SharedPreferences perference = PreferenceManager.getDefaultSharedPreferences(this);
        String email = mEditEmail.getText().toString().trim();
        if(email.matches(EMAIL_EXPRESSION)) {
            perference.edit().putString(FeedbackHelper.KEY_CONTENT, mEditContent.getText().toString())
            .putString(FeedbackHelper.KEY_EMAIL, email)
            .putInt(FeedbackHelper.KEY_CATEGORY, mCategoryPos).commit();
        } else {
            perference.edit().putString(FeedbackHelper.KEY_CONTENT, mEditContent.getText().toString())
            .putInt(FeedbackHelper.KEY_CATEGORY, mCategoryPos).commit();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        checkCommitable();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "setting", "fb_enter");
    }

    @Override
    public void onClick(View v) {
        hideIME();
        if (v == mCategoryLayout) {
            showQuesCategoryDialog();
        } else if (v == mBtnCommit) {
            if (mEditEmail.getText().toString().trim().matches(EMAIL_EXPRESSION)) {
                FeedbackHelper.getInstance().tryCommit(mCategory.getText().toString(),
                        mEditEmail.getText().toString().trim(),
                        mEditContent.getText().toString().trim());
                showMessageDialog(getString(R.string.feedback_success_title),
                        getString(R.string.feedback_success_content));
                mEditEmail.setText(mEditEmail.getText().toString());
                mEditContent.setText(mEditContent.getText().toString());
                mCategory.setText(mCategory.getText().toString());
                mCategory.setTag(1);
            } else {
                Toast.makeText(FeedbackActivity.this,
                        this.getResources().getText(R.string.feedback_error), Toast.LENGTH_SHORT)
                        .show();
            }
            SDKWrapper.addEvent(this, SDKWrapper.P1, "setting", "fb_submit");
        } else if( v == mEmailImg) {
            showEmailListDialog();
        }
    }

    private void hideIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditEmail.getWindowToken(), 0);
    }

    private void showMessageDialog(String title, String message) {
        if (mMessageDialog == null) {
            mMessageDialog = new LEOMessageDialog(this);
            mMessageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mCategoryPos = -1;
                    mEditContent.setText("");
                    mCategory.setText("");
                    mCategory.setTag(null);
                    finish();
                }
            });
        }
        mMessageDialog.setTitle(title);
        mMessageDialog.setContent(message);
        mMessageDialog.show();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v == mEditEmail) {
            mEmailLayout.setBackgroundResource(hasFocus ? R.drawable.text_box_active: R.drawable.text_box_normal);
        }
    }
    
    /**
     * show  question selection dialog
     */
    private void showQuesCategoryDialog(){
        if (mCategoryDialog == null) {
            mCategoryDialog = new LEOBaseDialog(FeedbackActivity.this);
            mCategoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mCategoryDialog.setContentView(R.layout.dialog_common_list_select);
            mCategoryDialog.findViewById(R.id.no_list).setVisibility(View.GONE);
        }
        TextView mTitle = (TextView) mCategoryDialog.findViewById(R.id.dlg_title);
        mTitle.setText(getResources().getString(R.string.feedback_category_tip));
        
        mCategoryListView = (ListView) mCategoryDialog.findViewById(R.id.item_list);
        mCategoryListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                mCategory.setText(mCategories.get(position));
                mCategoryPos = position;
                mCategory.setTag(1);
                mCategoryDialog.dismiss();
                checkCommitable();
            }
        });
        ListAdapter adapter = new CategoryListAdapter(FeedbackActivity.this);
        mCategoryListView.setAdapter(adapter);
        
        View cancel = mCategoryDialog.findViewById(R.id.dlg_bottom_btn);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoryDialog.dismiss();
            }
        });
        mCategoryDialog.show();
    }
    
    class CategoryListAdapter extends BaseAdapter{

        private LayoutInflater inflater;
        
        public CategoryListAdapter(Context ctx) {
            inflater = LayoutInflater.from(ctx);
        }
        
        @Override
        public int getCount() {
            return mCategories.size();
        }

        @Override
        public Object getItem(int position) {
            return mCategories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_common_select, parent, false);
                holder = new Holder();
                holder.name = (TextView) convertView.findViewById(R.id.tv_item_content);
                holder.selecte = (ImageView) convertView.findViewById(R.id.iv_selected);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.name.setText(mCategories.get(position));
            
            if(mCategoryPos >=0 && position == mCategoryPos ){
                    holder.selecte.setVisibility(View.VISIBLE);
            }else{
                    holder.selecte.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
    /**
     * show  email selection dialog
     */
    private void showEmailListDialog(){
        if (mCategoryDialog == null) {
            mCategoryDialog = new LEOBaseDialog(FeedbackActivity.this);
            mCategoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mCategoryDialog.setContentView(R.layout.dialog_common_list_select);
            mCategoryDialog.findViewById(R.id.no_list).setVisibility(View.GONE);
        }
        TextView mTitle = (TextView) mCategoryDialog.findViewById(R.id.dlg_title);
        mTitle.setText(getResources().getString(R.string.feed_back_email_guide));
        
        mCategoryListView = (ListView) mCategoryDialog.findViewById(R.id.item_list);
        mCategoryListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                mEditEmail.setText(mEmails.get(position));
                mCategoryDialog.dismiss();
                checkCommitable();
            }
        });
        ListAdapter adapter = new EmailListAdapter(FeedbackActivity.this);
        mCategoryListView.setAdapter(adapter);
        
        View cancel = mCategoryDialog.findViewById(R.id.dlg_bottom_btn);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoryDialog.dismiss();
            }
        });
        mCategoryDialog.show();
    }
    
    class EmailListAdapter extends BaseAdapter{

        private LayoutInflater inflater;
        
        public EmailListAdapter(Context ctx) {
            inflater = LayoutInflater.from(ctx);
        }
        
        @Override
        public int getCount() {
            return mEmails.size();
        }

        @Override
        public Object getItem(int position) {
            return mEmails.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_common_select, parent, false);
                holder = new Holder();
                holder.name = (TextView) convertView.findViewById(R.id.tv_item_content);
                holder.selecte = (ImageView) convertView.findViewById(R.id.iv_selected);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.name.setText(mEmails.get(position));
            
            if(!mEditEmail.getText().toString().isEmpty() && mEditEmail.getText().toString().equals(mEmails.get(position))){
                holder.selecte.setVisibility(View.VISIBLE);
            }else{
                holder.selecte.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
    public static class Holder {
        TextView name;
        ImageView selecte;
    }
}
