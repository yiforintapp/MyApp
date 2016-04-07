package com.leo.appmaster.feedback;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.home.MenuFaqBrowserActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
import com.leo.appmaster.utils.DeviceUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LeoUrls;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends BaseActivity implements OnClickListener,
        OnFocusChangeListener {

    private static final String EMAIL_EXPRESSION = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    public static final String TAG = "FeedbackActivity";
    private static final boolean DBG = true;
    private boolean mCanCommit;
    private View mBtnCommit;
    private EditText mEditContent;
    private EditText mEditEmail;
    private ImageView mEmailImg;
    private View mEmailLayout;
    private TextView mCategory;
    private ImageView mCategoryImg;
    private View mCategoryLayout;
    private CommonToolbar mTitleBar;
    private View mProblemView;

    private boolean mNeedHide = false;
    private LEOChoiceDialog mCategoryDialog;
    private ListView mCategoryListView;
    private LEOMessageDialog mMessageDialog;

    private TextView mToEmailTextView;

    private final static int[] sCategoryIds = {
            R.string.home_tab_wifi, R.string.home_tab_lost,
            R.string.home_tab_instruder, R.string.category_lock, R.string.hp_hide_video,
            R.string.hp_hide_img, R.string.hp_device_power, R.string.call_filter_name, R.string.category_other,
    };

    private final static int[] sNewCategoryIds = {
            R.string.home_tab_lost,
            R.string.home_tab_instruder, R.string.category_lock, R.string.hp_hide_video,
            R.string.hp_hide_img,  R.string.call_filter_name, R.string.category_other,
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

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.getBooleanExtra("isFromIntruderProtectionForbiden", false)) {

            if (mNeedHide) {
                mCategory.setText(mCategories.get(1));
                mCategoryPos = 1;
                mCategory.setTag(1);
            } else {
                mCategory.setText(mCategories.get(2));
                mCategoryPos = 2;
                mCategory.setTag(1);
            }

        }
    }

    private void initEmails() {
        final AccountManager am = AccountManager.get(getApplicationContext());
        final Account[] accounts = am.getAccounts();
        List<String> as = new ArrayList<String>();

        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].name != null && !accounts[i].name.startsWith("com.contapps")) {
                as.add(accounts[i].name);
            }
        }

        for (int i = 0; i < as.size(); i++) {
            LeoLog.i("fb", "a = " + as.get(i).toString());
            if (as.get(i) != null && (as.get(i)).matches(EMAIL_EXPRESSION) && !mEmails.contains(as.get(i))) {
                mEmails.add(as.get(i));
            }
        }
    }

    private void initUi() {
        mTitleBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTitleBar.setToolbarTitle(R.string.feedback);
        mTitleBar.setOptionImageResource(R.drawable.mode_done);
        mTitleBar.setOptionMenuVisible(true);
        mTitleBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mCanCommit) return;
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
                    if (DBG) {
                        LeoLog.i(TAG, "反馈提交的数据：");
                        LeoLog.i(TAG, "----------------邮箱--" + mEditEmail.getText().toString());
                        LeoLog.i(TAG, "----------------问题类型--" + mCategory.getText().toString());
                        LeoLog.i(TAG, "----------------其他--" + mEditContent.getText().toString());
                    }
                } else {
                    Toast.makeText(FeedbackActivity.this,
                            FeedbackActivity.this.getResources().getText(R.string.feedback_error), Toast.LENGTH_SHORT)
                            .show();
                }
                SDKWrapper.addEvent(FeedbackActivity.this, SDKWrapper.P1, "setting", "fb_submit");
            }
        });
        mBtnCommit = mTitleBar.getOptionImageView();
        mEditContent = (EditText) findViewById(R.id.feedback_content);
        mEmailLayout = findViewById(R.id.feedback_email_layout);
        mEditEmail = (EditText) findViewById(R.id.feedback_email);
        mEditEmail.setOnFocusChangeListener(this);

        mEmailImg = (ImageView) findViewById(R.id.feedback_email_arrow);
        mEmailImg.setOnClickListener(this);
        mEmailImg.setVisibility(mEmails.size() > 1 ? View.VISIBLE : View.GONE);

        mCategoryLayout = findViewById(R.id.feedback_category_layout);
        mCategoryLayout.setOnClickListener(this);
        mCategory = (TextView) findViewById(R.id.feedback_category_title);

        mCategoryImg = (ImageView) findViewById(R.id.feedback_category_arrow);

        mNeedHide = LeoSettings.getBoolean(PrefConst.KEY_NEED_HIDE_BATTERY_FLOW_AND_WIFI, false);
        if (mNeedHide) {
            for (int i = 0; i < sNewCategoryIds.length; i++) {
                mCategories.add(getString(sNewCategoryIds[i]));
            }
        } else {
            for (int i = 0; i < sCategoryIds.length; i++) {
                mCategories.add(getString(sCategoryIds[i]));
            }
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

        mToEmailTextView = (TextView) findViewById(R.id.feedback_call_us_contact_type_1);
        try {
            SpannableString content = new SpannableString(mToEmailTextView.getText().toString());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            content.setSpan(new ForegroundColorSpan(Color.BLUE), 0, content.length(), 0);
            mToEmailTextView.setText(content);
            mToEmailTextView.setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mProblemView = findViewById(R.id.normal_problem);
        mProblemView.setOnClickListener(this);
    }

    private void checkPendingData() {
        SharedPreferences perference = PreferenceManager.getDefaultSharedPreferences(this);
        mEditContent.setText(perference.getString(FeedbackHelper.KEY_CONTENT, ""));
        String email = "";
        if (mEmails.size() > 0) {
            email = mEmails.get(0);
        }
        String currentEmail = perference.getString(FeedbackHelper.KEY_EMAIL, email);
        mEditEmail.setText(currentEmail.isEmpty() ? email : currentEmail);

        Intent intent = getIntent();
        boolean isFromSecurHelp = false;
        if (intent != null) {
            isFromSecurHelp = intent.getBooleanExtra(PhoneSecurityConstants.SECUR_HELP_TO_FEEDBACK, false);
            if (isFromSecurHelp) {
                if (mNeedHide) {
                    mCategoryPos = 0;
                } else {
                    mCategoryPos = 1;
                }

                intent.removeExtra(PhoneSecurityConstants.SECUR_HELP_TO_FEEDBACK);
            }
        }
        if (!isFromSecurHelp) {
            try {
                mCategoryPos = perference.getInt(FeedbackHelper.KEY_CATEGORY, -1);
            } catch (Exception e) {
            }
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
        mCanCommit = commitable;
        mBtnCommit.setEnabled(commitable);
        mTitleBar.setOptionImageResource(R.drawable.mode_done);
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
        if (email.matches(EMAIL_EXPRESSION)) {
            perference.edit()
                    .putString(FeedbackHelper.KEY_CONTENT, mEditContent.getText().toString())
                    .putString(FeedbackHelper.KEY_EMAIL, email)
                    .putInt(FeedbackHelper.KEY_CATEGORY, mCategoryPos).apply();
        } else {
            perference.edit()
                    .putString(FeedbackHelper.KEY_CONTENT, mEditContent.getText().toString())
                    .putInt(FeedbackHelper.KEY_CATEGORY, mCategoryPos).apply();
        }
    }

    ;

    @Override
    protected void onResume() {
        super.onResume();
        handleIntent();
        checkCommitable();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "setting", "fb_enter");
    }

    /*跳转到用户反馈处理*/
    private void initIntentHandler() {
        Intent intent = this.getIntent();

        fromSecurHelpHandler(intent);
    }

    /*来自防盗帮助页面的处理*/
    private void fromSecurHelpHandler(Intent intent) {
        boolean isFromSecurHelp = intent.getBooleanExtra(PhoneSecurityConstants.SECUR_HELP_TO_FEEDBACK, false);
        if (isFromSecurHelp) {
            /*用户反馈自动填充的内容*/
            String feedbkContent = getResources().getString(R.string.secur_feedbk_content);
            mEditContent.setText(feedbkContent);
            /*防盗问题*/
            String feedbkQue = getResources().getString(R.string.secur_feedbk_type);
            mCategory.setText(feedbkQue);
            mCategory.setTag(feedbkQue);
            mCategoryPos = 3;
            intent.removeExtra(PhoneSecurityConstants.SECUR_HELP_TO_FEEDBACK);
        }
    }

    @Override
    public void onClick(View v) {
        hideIME();
        if (v == mCategoryLayout) {
            showQuesCategoryDialog();
        } else if (v == mBtnCommit) {

        } else if (v == mEmailImg) {
            showEmailListDialog();
        } else if (v == mProblemView) {

            SDKWrapper.addEvent(FeedbackActivity.this, SDKWrapper.P1, "feedback", "faq");

            String faqtitle = getString(R.string.menu_left_item_problem);
            String country = DeviceUtil.getCountry();
            country = Utilities.exChange(country);
            int version = PhoneInfo.getVersionCode(this);
            String language = DeviceUtil.getLanguage();

            String url = LeoUrls.FAR_REQUEST + "/"
                    + country + "/" + language + "/" + version + ".html";
            LeoLog.d("testFaq", "url : " + url);


            MenuFaqBrowserActivity.startMenuFaqWeb(this, faqtitle, url, true);
        } else if (v == mToEmailTextView) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "feedback", "mail");
            LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            Intent data=new Intent(Intent.ACTION_SENDTO);
            data.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            data.setData(Uri.parse("mailto:".concat(mToEmailTextView.getText().toString())));
//            data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
            data.putExtra(Intent.EXTRA_TEXT, " ");
            try {
                String[] datas = Utilities.getEmailInfo(FeedbackActivity.this,
                        mToEmailTextView.getText().toString()); // 浏览器信息
                int count = Integer.parseInt(datas[0]);
                if (count == 1) {
                    mLockManager.filterPackage(datas[1], 1000);
                }
                mLockManager.filterSelfOneMinites();
                startActivity(data);
            } catch (Exception e) {
            }
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
        if (v == mEditEmail) {
            mEmailLayout.setBackgroundResource(hasFocus ? R.drawable.text_box_active
                    : R.drawable.text_box_normal);
        }
    }

    /**
     * show question selection dialog
     */
    private void showQuesCategoryDialog() {
        if (mCategoryDialog == null) {
            mCategoryDialog = new LEOChoiceDialog(this);
        }
        try {
            ListView itemsListView = mCategoryDialog.getItemsListView();
            LayoutParams layoutParams = itemsListView.getLayoutParams();
            layoutParams.height = DipPixelUtil.dip2px(this, 300);
        } catch (Throwable e) {
        }
        mCategoryDialog.setTitle(getResources().getString(R.string.feedback_category_tip));
        mCategoryDialog.setItemsWithDefaultStyle(mCategories, mCategoryPos);
        mCategoryDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCategory.setText(mCategories.get(position));
                mCategoryPos = position;
                mCategory.setTag(1);
                mCategoryDialog.dismiss();
                checkCommitable();
            }
        });
        mCategoryDialog.show();
    }

//    class CategoryListAdapter extends BaseAdapter {
//
//        private LayoutInflater inflater;
//
//        public CategoryListAdapter(Context ctx) {
//            inflater = LayoutInflater.from(ctx);
//        }
//
//        @Override
//        public int getCount() {
//            return mCategories.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return mCategories.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            Holder holder;
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.item_ask_times_to_catch, parent, false);
//                holder = new Holder();
//                holder.name = (TextView) convertView.findViewById(R.id.tv_item);
//                holder.selecte = (Button) convertView.findViewById(R.id.cb_selected);
//                convertView.setTag(holder);
//            } else {
//                holder = (Holder) convertView.getTag();
//            }
//            holder.name.setText(mCategories.get(position));
//
//            if (mCategoryPos >= 0 && position == mCategoryPos) {
//                holder.selecte.setVisibility(View.VISIBLE);
//            } else {
//                holder.selecte.setVisibility(View.GONE);
//            }
//            return convertView;
//        }
//    }

    /**
     * show email selection dialog
     * private void showQuesCategoryDialog() {
     * if (mCategoryDialog == null) {
     * mCategoryDialog = new LEOChoiceDialog(this);
     * }
     * //        TextView mTitle = (TextView) mCategoryDialog.findViewById(R.id.dlg_title);
     * //        mTitle.setText(getResources().getString(R.string.feedback_category_tip));
     * mCategoryDialog.setItemsWithDefaultStyle(mCategories,mCategoryPos);
     * mCategoryDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {
     *
     * @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     * mCategory.setText(mCategories.get(position));
     * mCategoryPos = position;
     * mCategory.setTag(1);
     * mCategoryDialog.dismiss();
     * checkCommitable();
     * }
     * });
     * mCategoryDialog.show();
     * }
     */
//    private void showQuesCategoryDialog() {
//        if (mCategoryDialog == null) {
//            mCategoryDialog = new LEOChoiceDialog(this);
//        }
//        mCategoryDialog.setTitle(getResources().getString(R.string.feedback_category_tip));
//        mCategoryDialog.setItemsWithDefaultStyle(mCategories,mCategoryPos);
//        mCategoryDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mCategory.setText(mCategories.get(position));
//                mCategoryPos = position;
//                mCategory.setTag(1);
//                mCategoryDialog.dismiss();
//                checkCommitable();
//            }
//        });
//        mCategoryDialog.show();
//    }
    private void showEmailListDialog() {
        int size = mEmails.size();
        if (size > 0) {
            if (mCategoryDialog == null) {
                mCategoryDialog = new LEOChoiceDialog(FeedbackActivity.this);
            } else {
                if (mCategoryDialog.isShowing()) {
                    return;
                }
            }
            mCategoryDialog.setTitle(getResources().getString(R.string.feedback_email_guide));
            int pos = 0;
            Editable current = mEditEmail.getText();
            if (current != null) {
                String email = current.toString();
                pos = mEmails.indexOf(email);
            }
            if (pos < 0 || pos > mEmails.size() - 1) {
                pos = 0;
            }
            mCategoryDialog.setItemsWithDefaultStyle(mEmails, pos);
            mCategoryDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    mEditEmail.setText(mEmails.get(position));
                    mCategoryDialog.dismiss();
                    checkCommitable();
                }
            });
            // ListAdapter adapter = new
            // EmailListAdapter(FeedbackActivity.this);
            // mCategoryListView.setAdapter(adapter);
            // View cancel = mCategoryDialog.findViewById(R.id.dlg_bottom_btn);
            // cancel.setOnClickListener(new OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // mCategoryDialog.dismiss();
            // }
            // });
            mCategoryDialog.show();
        }
    }

//    class EmailListAdapter extends BaseAdapter {
//
//        private LayoutInflater inflater;
//
//        public EmailListAdapter(Context ctx) {
//            inflater = LayoutInflater.from(ctx);
//        }
//
//        @Override
//        public int getCount() {
//            return mEmails.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return mEmails.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            Holder holder;
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.item_common_select, parent, false);
//                holder = new Holder();
//                holder.name = (TextView) convertView.findViewById(R.id.tv_item);
//                holder.selecte = (Button) convertView.findViewById(R.id.bt_selected);
//                convertView.setTag(holder);
//            } else {
//                holder = (Holder) convertView.getTag();
//            }
//            holder.name.setText(mEmails.get(position));
//
//            if (!mEditEmail.getText().toString().isEmpty()
//                    && mEditEmail.getText().toString().equals(mEmails.get(position))) {
//                holder.selecte.setPressed(true);
//            } else {
//                holder.selecte.setPressed(false);
//            }
//            return convertView;
//        }
//    }

    public static class Holder {
        TextView name;
        Button selecte;
    }
}
