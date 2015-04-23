
package com.leo.appmaster.applocker;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;

import com.android.internal.content.NativeLibraryHelper.Handle;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.LeoPopMenu.LayoutStyles;

public class PasswdProtectActivity extends BaseActivity implements
        OnClickListener {

    private CommonTitleBar mTtileBar;

    private View mSpinnerQuestions;
    private EditText mQuestion, mAnwser;
    private TextView mSave;
    private ScrollView mScrollView;
    private LeoPopMenu mLeoPopMenu;
    private Handler mHandler = new Handler();

    private List<String> mCategories;

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
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle(R.string.passwd_protect);
        // mTtileBar.openBackView();
        mTtileBar.setBackViewListener(new OnClickListener() {

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

        String[] entrys = getResources().getStringArray(
                R.array.default_psw_protect_entrys);
        mCategories = Arrays.asList(entrys);
        mSpinnerQuestions = findViewById(R.id.tv_spinner);
        mSpinnerQuestions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeoPopMenu == null) {
                    mLeoPopMenu = new LeoPopMenu();
                    mLeoPopMenu.setPopMenuItems(mCategories);
                    mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
                            mQuestion.setText(mCategories.get(position));
                            mQuestion.selectAll();
                            mLeoPopMenu.dismissSnapshotList();
                        }
                    });
                }
                LayoutStyles styles = new LayoutStyles();
                styles.width = LayoutParams.MATCH_PARENT;
                styles.height = LayoutParams.WRAP_CONTENT;
                styles.animation = R.style.PopupListAnimUpDown;
                mLeoPopMenu
                        .showPopMenu(PasswdProtectActivity.this, mSpinnerQuestions, styles, null);
            }
        });
        mQuestion = (EditText) findViewById(R.id.et_question);
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
            }
        });
        mAnwser = (EditText) findViewById(R.id.et_anwser);
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
        mSave = (TextView) findViewById(R.id.tv_save);
        mSave.setOnClickListener(this);

        mScrollView = (ScrollView) findViewById(R.id.scroll);
        String question = AppMasterPreference.getInstance(this).getPpQuestion();
        if (!TextUtils.isEmpty(question)) {
            mQuestion.setText(question);
            mQuestion.selectAll();
        } else {
            mQuestion.setText(mCategories.get(0));
            mQuestion.selectAll();
        }
        String answer = AppMasterPreference.getInstance(this).getPpAnwser();
        if (question != null) {
            mAnwser.setText(answer);
        }

    }

    private void hideIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAnwser.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        String qusetion = mQuestion.getText().toString();
        String answer = mAnwser.getText().toString();
        String passwdHint = AppMasterPreference.getInstance(this)
                .getPasswdTip();
        if (v == mSave) {
            hideIME();
            boolean noQuestion = qusetion == null || qusetion.trim().equals("");
            boolean noAnswer = answer == null || answer.equals("");
            if (noQuestion && noAnswer) {
                qusetion = answer = "";
            } else if (noQuestion && !noAnswer) {
                Toast.makeText(this, R.string.qusetion_cant_null,
                        Toast.LENGTH_SHORT).show();
                return;
            } else if (!noQuestion && noAnswer) {
                Toast.makeText(this, R.string.aneser_cant_null,
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (qusetion.length() > 40) {
                    Toast.makeText(this, R.string.question_charsize_tip,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (answer.length() > 40) {
                    Toast.makeText(this, R.string.anwser_charsize_tip,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (qusetion == null || qusetion.trim().equals("")) {
                qusetion = answer = "";
            }
            AppMasterPreference.getInstance(this).savePasswdProtect(qusetion,
                    answer, passwdHint);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "first", "setpwdp_submit");
            Toast.makeText(this, R.string.pp_success, Toast.LENGTH_SHORT)
                    .show();
            // Handler handler = new Handler();
            // handler.postDelayed(new Runnable() {
            //
            // @Override
            // public void run() {
            onBackPressed();
            // }
            // }, 500);
        } else if (v == mAnwser) {
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
        boolean quickMode = getIntent().getBooleanExtra("quick_mode", false);
        int quickModeId = getIntent().getIntExtra("mode_id", -1);
        if (toHome) {
            LockManager.getInstatnce().timeFilter(getPackageName(), 500);
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else if (toLockList) {
            Intent intent = new Intent(this,
                    AppLockListActivity.class);
            this.startActivity(intent);
        } else if (quickMode) {
            LockManager lm = LockManager.getInstatnce();
            List<LockMode> modeList = lm.getLockMode();
            for (LockMode lockMode : modeList) {
                if (lockMode.modeId == quickModeId) {
                    showModeActiveTip(lockMode);
                    break;
                }
            }
        }
        super.onBackPressed();
    }

    /**
     * show the tip when mode success activating
     */
    private void showModeActiveTip(LockMode mode) {
        View mTipView = LayoutInflater.from(this).inflate(R.layout.lock_mode_active_tip, null);
        TextView mActiveText = (TextView) mTipView.findViewById(R.id.active_text);
        mActiveText.setText(this.getString(R.string.mode_change, mode.modeName));
        Toast toast = new Toast(this);
        toast.setView(mTipView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
