package com.leo.appmaster.applocker;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.utils.Utilities;

public class PasswdProtectActivity extends BaseActivity implements
        OnClickListener {

    private CommonToolbar mTtileBar;

    private View mSpinnerQuestions;
    private EditText mQuestion, mAnwser;
    private ScrollView mScrollView;
    private Handler mHandler = new Handler();
    private View mLayoutQues;
    private LEOChoiceDialog mQuesDialog;

    private List<String> mCategories;
    private String mSelectQues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwd_protect);
        initUI();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initUI() {
        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle(R.string.passwd_protect);
        mTtileBar.setOptionImageResource(R.drawable.mode_done);
        mTtileBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String qusetion = mQuestion.getText().toString();
                String answer = mAnwser.getText().toString();
                String passwdHint = AppMasterPreference.getInstance(PasswdProtectActivity.this)
                        .getPasswdTip();
                hideIME();
                boolean noQuestion = qusetion == null || qusetion.trim().equals("");
                boolean noAnswer = answer == null || answer.equals("");
                if (noQuestion && noAnswer) {
                    qusetion = answer = "";
                } else if (noQuestion && !noAnswer) {
                    Toast.makeText(PasswdProtectActivity.this, R.string.qusetion_cant_null,
                            Toast.LENGTH_SHORT).show();
                    return;
                } else if (!noQuestion && noAnswer) {
                    Toast.makeText(PasswdProtectActivity.this, R.string.aneser_cant_null,
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (qusetion.length() > 40) {
                        Toast.makeText(PasswdProtectActivity.this, R.string.question_charsize_tip,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (answer.length() > 40) {
                        Toast.makeText(PasswdProtectActivity.this, R.string.anwser_charsize_tip,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (qusetion == null || qusetion.trim().equals("")) {
                    qusetion = answer = "";
                }
                AppMasterPreference.getInstance(PasswdProtectActivity.this).savePasswdProtect(qusetion,
                        answer, passwdHint);
                SDKWrapper.addEvent(PasswdProtectActivity.this, SDKWrapper.P1, "first", "setpwdp_submit");
                Toast.makeText(PasswdProtectActivity.this, R.string.pp_success, Toast.LENGTH_SHORT)
                        .show();
                // Handler handler = new Handler();
                // handler.postDelayed(new Runnable() {
                //
                // @Override
                // public void run() {
                onBackPressed();
                // }
                // }, 500);

            }
        });
        mTtileBar.setOptionMenuVisible(true);
//        mTtileBar.setNavigationClickListener(listener)
        mTtileBar.setNavigationClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getApplicationContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    mTtileBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideIME();
                            onBackPressed();
                        }
                    }, 300);
                } else {
                    onBackPressed();
                }

            }
        });

        final String[] entrys = getResources().getStringArray(
                R.array.default_psw_protect_entrys_new);
        mCategories = Arrays.asList(entrys);
        mSpinnerQuestions = findViewById(R.id.tv_spinner);
        mSpinnerQuestions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuesDialog == null) {
                    mQuesDialog = new LEOChoiceDialog(PasswdProtectActivity.this);
                }
                mQuesDialog.setTitle(getResources().getString(R.string.input_qusetion));
                int index = 0;
                if (mSelectQues != null) {
                    for (int i = 0; i < mCategories.size(); i++) {
                        if (mCategories.get(i).equals(mSelectQues)) {
                            index = i;
                        }
                    }
                } else {
                    for (int i = 0; i < mCategories.size(); i++) {

                        String question = AppMasterPreference.getInstance(PasswdProtectActivity.this)
                                .getPpQuestion();
                        //检验问题是否为旧版本的病句
                        String[] oldStrings = getResources().getStringArray(
                                R.array.default_psw_protect_entrys);
                        boolean isOldString = Utilities.makeContrast(oldStrings, question);
                        if (isOldString) {
                            question = Utilities.replaceOldString(entrys, oldStrings, question);
                        }

                        if (mCategories.get(i).equals(question)) {
                            index = i;
                        }
                    }
                }
                mQuesDialog.setItemsWithDefaultStyle(mCategories, index);
                mQuesDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        mQuestion.setText(mCategories.get(position));
                        mQuestion.selectAll();
                        mQuestion.setSelection(0);
//                        CharSequence text = mQuestion.getText();
//                        if (text instanceof Spannable) {
//                             Spannable spanText = (Spannable)text;
//                             Selection.setSelection(spanText, 0);
//                         }
                        mSelectQues = mCategories.get(position);
                        mQuesDialog.dismiss();
                    }
                });
                mQuesDialog.show();
            }
        });

        mLayoutQues = findViewById(R.id.layout_questions);
        mQuestion = (EditText) findViewById(R.id.et_question);
//        mQuestion.setHorizontalScrollBarEnabled(true);
//        mQuestion.setMovementMethod(new ScrollingMovementMethod()); 
        mQuestion.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFucus) {
                if (isFucus) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.fullScroll(View.FOCUS_UP);
                        }
                    }, 100);
                }
                mLayoutQues.setBackgroundResource(isFucus ? R.drawable.text_box_active
                        : R.drawable.text_box_normal);
            }
        });

        mAnwser = (EditText) findViewById(R.id.et_anwser);
        // 使得密保问题获得焦点，弹出键盘，并使光标置于最后
        // PasswdProtectActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mAnwser.requestFocus();
        /**
         * delayed show the softinput ,because the page may not finished loading
         * ,and the above method will up the titlebar ,so conflict
         */
        mAnwser.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) PasswdProtectActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mAnwser, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        mAnwser.setOnClickListener(this);
        mAnwser.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFucus) {
                if (isFucus) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    }, 100);
                }
            }
        });

        mScrollView = (ScrollView) findViewById(R.id.scroll);
        String question = AppMasterPreference.getInstance(this).getPpQuestion();

        if (!TextUtils.isEmpty(question)) {
            //检验问题是否为旧版本的病句
            String[] oldStrings = getResources().getStringArray(
                    R.array.default_psw_protect_entrys);
            boolean isOldString = Utilities.makeContrast(oldStrings, question);
            if (isOldString) {
                question = Utilities.replaceOldString(entrys, oldStrings, question);
            }

            mQuestion.setText(question);
            mQuestion.selectAll();
        } else {
            mQuestion.setText(mCategories.get(0));
            mQuestion.selectAll();
        }
        mQuestion.setSelection(0);
//        CharSequence text = mQuestion.getText();
//        if (text instanceof Spannable) {
//             Spannable spanText = (Spannable)text;
//             Selection.setSelection(spanText, 0);
//         }
        
        String answer = AppMasterPreference.getInstance(this).getPpAnwser();
        if (question != null) {
            mAnwser.setText(answer);
            CharSequence astext = mAnwser.getText();
            if (astext instanceof Spannable) {
                Spannable spanText = (Spannable) astext;
                Selection.setSelection(spanText, astext.length());
            }
        }
    }

//    private String replaceOldString(String[] newStrings, String[] oldStrings, String question) {
//
//        if (question.equals(oldStrings[0])) {
//            question = newStrings[4];
//        } else if (question.equals(oldStrings[1])) {
//            question = newStrings[5];
//        } else if (question.equals(oldStrings[2])) {
//            question = newStrings[6];
//        } else {
//            question = newStrings[7];
//        }
//
//        return question;
//    }
//
//    private boolean makeContrast(String[] oldStrings, String question) {
//        boolean isOldString = false;
//        for (int i = 0; i < oldStrings.length; i++) {
//            String string = oldStrings[i];
//            if (string.equals(question)) {
//                isOldString = true;
//            }
//        }
//        return isOldString;
//    }

    private void hideIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAnwser.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        if (v == mAnwser) {
            if (mAnwser.isFocused()) {
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 500);
            }
        }
    }

    @Override
    public void onBackPressed() {
        hideIME();
        boolean toHome = getIntent().getBooleanExtra("to_home", false);
        boolean toLockList = getIntent().getBooleanExtra("to_lock_list", false);
        if (toHome) {
            mLockManager.filterPackage(getPackageName(), 500);
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else if (toLockList) {
            Intent intent = new Intent(this,
                    AppLockListActivity.class);
            this.startActivity(intent);
        } /*
           * else if (quickMode) { LockManager lm = LockManager.getInstatnce();
           * List<LockMode> modeList = lm.getLockMode(); for (LockMode lockMode
           * : modeList) { if (lockMode.modeId == quickModeId) {
           * showModeActiveTip(lockMode); break; } } }
           */
        try {
            super.onBackPressed();
        } catch (Exception e) {
        }
    }

    /**
     * show the tip when mode success activating
     */
    /*
     * private void showModeActiveTip(LockMode mode) { View mTipView =
     * LayoutInflater.from(this).inflate(R.layout.lock_mode_active_tip, null);
     * TextView mActiveText = (TextView)
     * mTipView.findViewById(R.id.active_text);
     * mActiveText.setText(this.getString(R.string.mode_change, mode.modeName));
     * Toast toast = new Toast(this); toast.setView(mTipView);
     * toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0,
     * 0); toast.setDuration(Toast.LENGTH_SHORT); toast.show(); }
     */

    class QuesListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public QuesListAdapter(Context ctx) {
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

            if (mSelectQues != null) {
                if (mCategories.get(position).equals(mSelectQues)) {
                    holder.selecte.setVisibility(View.VISIBLE);
                } else {
                    holder.selecte.setVisibility(View.GONE);
                }
            } else {
                if (mCategories.get(position)
                        .equals(AppMasterPreference.getInstance(PasswdProtectActivity.this)
                                .getPpQuestion())) {
                    holder.selecte.setVisibility(View.VISIBLE);
                } else {
                    holder.selecte.setVisibility(View.GONE);
                }
            }
            return convertView;
        }
    }

    public static class Holder {
        TextView name;
        ImageView selecte;
    }

}