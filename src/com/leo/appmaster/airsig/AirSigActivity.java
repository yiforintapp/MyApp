package com.leo.appmaster.airsig;


import com.airsig.airsigengmulti.ASEngine;
import com.leo.appmaster.R;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.airsig.airsigsdk.ASSetting;
import com.leo.appmaster.airsig.airsigutils.EventLogger;
import com.leo.appmaster.sdk.BaseActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

public class AirSigActivity extends BaseActivity implements View.OnClickListener {

    private static final boolean HIDE_BUTTON2 = true;
    private static final boolean VERIFY_USE_FRAGMENT = true;

    public static final String SHARED_PREFERENCE_SETTINGS = "SHARED_PREFERENCE_SETTINGS";
    public static final String SPREF_KEY_MODE = "SPREF_KEY_MODE";
    public static final String CUSTOM_DB_LOCATION = null;

    public static final int SPREF_VAL_MODE_HIGH_SECURE = 0;
    public static final int SPREF_VAL_MODE_NORMAL = 1;
    public static final int SPREF_VAL_MODE_CONTROL = 2;
    public static final int SPREF_VAL_MODE_DEFAULT = SPREF_VAL_MODE_NORMAL;

    private Button mButtonTraining;
    private Button mButtonVerify;
    private Button mButtonClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airsig_activity_select);

        initAirSig();

        mButtonTraining = (Button) findViewById(R.id.button_set_signature);
        mButtonTraining.setOnClickListener(this);
        mButtonVerify = (Button) findViewById(R.id.button_verify_signature);
        mButtonVerify.setOnClickListener(this);
        mButtonClear = (Button) findViewById(R.id.button_clean_db);
        mButtonClear.setOnClickListener(this);
    }

    private void initAirSig() {
        ASSetting setting = new ASSetting();
        setting.engineParameters = ASEngine.ASEngineParameters.Unlock;


        ASGui.getSharedInstance(getApplicationContext(), CUSTOM_DB_LOCATION, setting, null); // Database is in /data/data/...
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void showMessage(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_set_signature:
                ASGui.getSharedInstance().showTrainingActivity(1, new ASGui.OnTrainingResultListener() {
                    @Override
                    public void onResult(boolean isRetrain, boolean success, ASEngine.ASAction action) {
                        showMessage((isRetrain ? "Re-set Signature" + ", " : "")
                                + (success ? "Completed" : "Not Completed"));
                    }

                });
                break;
            case R.id.button_verify_signature:

                break;
            case R.id.button_clean_db:
                ASGui.getSharedInstance().deleteSignature(1);
                ASEngine.getSharedInstance().getAction(1, new ASEngine.OnGetActionResultListener() {
                    @Override
                    public void onResult(final ASEngine.ASAction action, final ASEngine.ASError error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (null != action && action.strength != ASEngine.ASStrength.ASStrengthNoData) {
                                    showMessage("delete Signature fail");
                                } else {
                                    showMessage("delete Signature done");
                                }
                            }
                        });
                    }
                });
                break;
        }
    }

}
