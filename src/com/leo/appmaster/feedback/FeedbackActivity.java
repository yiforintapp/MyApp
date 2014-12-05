
package com.leo.appmaster.feedback;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.LeoPopMenu.LayoutStyles;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
import com.leoers.leoanalytics.LeoStat;

public class FeedbackActivity extends BaseActivity implements OnClickListener {

    private View mBtnCommit;
    private EditText mEditContent;
    private EditText mEditEmail;
    private TextView mCategory;
    private ImageView mCategoryImg;
    private View mCategoryLayout;

    private LeoPopMenu mLeoPopMenu;

    private LEOMessageDialog mMessageDialog;

    private final static int[] sCategoryIds = {
            R.string.category_lock, R.string.category_boost, R.string.category_backup,
            R.string.category_hide, R.string.category_other, R.string.category_suggest
    };

    private final ArrayList<String> mCategories = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initUi();

        // check if any data not submitted
        checkPendingData();
    }

    private void initUi() {
        CommonTitleBar titleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        titleBar.setTitle(R.string.feedback);
        titleBar.openBackView();
        mEditContent = (EditText) findViewById(R.id.feedback_content);
        mEditEmail = (EditText) findViewById(R.id.feedback_email);
        mCategoryLayout = findViewById(R.id.feedback_category_layout);
        mCategoryLayout.setOnClickListener(this);
        mCategory = (TextView) findViewById(R.id.feedback_category_title);
        mCategoryImg = (ImageView) findViewById(R.id.feedback_category_arrow);
        mBtnCommit = findViewById(R.id.feedback_commit);
        mBtnCommit.setOnClickListener(this);
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
        mEditEmail.setText(perference.getString(FeedbackHelper.KEY_EMAIL, ""));
        String category = perference.getString(FeedbackHelper.KEY_CATEGORY, "");
        if (!category.isEmpty()) {
            mCategory.setText(category);
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
        mBtnCommit.setBackgroundResource(commitable ? R.drawable.check_update_button
                : R.drawable.update_btn_disable_bg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMessageDialog != null) {
            mMessageDialog.dismiss();
            mMessageDialog = null;
        }
        SharedPreferences perference = PreferenceManager.getDefaultSharedPreferences(this);
        String category = mCategory.getTag() == null ? "" : mCategory.getText().toString();
        perference.edit().putString(FeedbackHelper.KEY_CONTENT, mEditContent.getText().toString())
                .putString(FeedbackHelper.KEY_EMAIL, mEditEmail.getText().toString())
                .putString(FeedbackHelper.KEY_CATEGORY, category).commit();
    };

    @Override
    protected void onResume() {
        super.onResume();
        checkCommitable();
        SDKWrapper.addEvent(this, LeoStat.P1, "setting", "fb_enter");
    }

    @Override
    public void onClick(View v) {
        hideIME();
        if (v == mCategoryLayout) {
            if (mLeoPopMenu == null) {
                mLeoPopMenu = new LeoPopMenu();
                mLeoPopMenu.setPopMenuItems(mCategories);
                mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        mCategory.setText(mCategories.get(position));
                        mCategory.setTag(1);
                        mLeoPopMenu.dismissSnapshotList();
                        checkCommitable();
                    }
                });
            }
            LayoutStyles styles = new LayoutStyles();
            styles.width = LayoutParams.MATCH_PARENT;
            styles.height = LayoutParams.WRAP_CONTENT;
            styles.animation = R.style.PopupListAnimUpDown;
            mLeoPopMenu.showPopMenu(this, mCategory, styles, new OnDismissListener() {
                @Override
                public void onDismiss() {
                    mCategoryImg.setImageResource(R.drawable.choose_normal);
                }
            });
            mCategoryImg.setImageResource(R.drawable.choose_active);
        } else if (v == mBtnCommit) {
            if (mEditEmail.getText().toString().trim().contains("@")) {
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
            SDKWrapper.addEvent(this, LeoStat.P1, "setting", "fb_submit");
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
                    mEditContent.setText("");
                    mEditEmail.setText("");
                    mCategory.setText(R.string.feedback_category_tip);
                    mCategory.setTag(null);
                    finish();
                }
            });
        }
        mMessageDialog.setTitle(title);
        mMessageDialog.setContent(message);
        mMessageDialog.show();
    }
}
