package com.leo.appmaster.applocker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;

public class PasswdTipActivity extends BaseActivity implements OnClickListener {
    CommonToolbar mTitleBar;
	EditText mEtTip;
	View  mTvMakesure;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passwd_tip);
		initUI();
	}

	private void initUI() {
		mEtTip = (EditText) findViewById(R.id.et_passwd_tip);
		mTitleBar = (CommonToolbar) findViewById(R.id.commonTitleBar1);
		mTitleBar.setToolbarTitle(R.string.passwd_notify);
//		mTitleBar.openBackView();
		mTitleBar.setOptionImageResource(R.drawable.mode_done);
		mTitleBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                String tip = mEtTip.getText().toString().trim();
                AppMasterPreference ap = AppMasterPreference.getInstance(PasswdTipActivity.this);
                String q = ap.getPpQuestion();
                String a = ap.getPpAnwser();
                AppMasterPreference.getInstance(PasswdTipActivity.this).savePasswdProtect(q, a, tip);
                Toast.makeText(PasswdTipActivity.this, R.string.set_success, 0).show();
                hideIME();
				SDKWrapper.addEvent(PasswdTipActivity.this, SDKWrapper.P1, "settings", "pwdn");
                mEtTip.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();                    
                    }
                }, 300);
            }
        });
		mTitleBar.setOptionMenuVisible(true);
		String tip = AppMasterPreference.getInstance(this).getPasswdTip();
		if (tip != null) {
			mEtTip.setText(tip);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

    private void hideIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtTip.getWindowToken(), 0);
    }
	
	@Override
	public void onClick(View v) {
	}

}
