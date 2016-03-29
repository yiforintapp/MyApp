package com.leo.appmaster.phoneSecurity;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonToolbar;

import java.util.ArrayList;

public class SecurityDetailActivity extends Activity implements View.OnClickListener {

    private CommonToolbar mComBar;
    private ListView mListView;
    private Button mBtn;
    private InstructListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_detail);
        initUI();
        initData();
    }

    private void initUI() {
        mComBar = (CommonToolbar) findViewById(R.id.title_bar);
        mListView = (ListView) findViewById(R.id.instruct_list);
        mBtn = (Button) findViewById(R.id.secur_bt);
        mBtn.setOnClickListener(this);
    }

    private void initData() {
        mComBar.setToolbarTitle(R.string.secur_deta_title_bar);
        mComBar.setToolbarColorResource(R.color.cb);
        ArrayList<InstructModel> instructs = loadInstructList();
        if (instructs != null) {
            mAdapter = new InstructListAdapter(this, instructs, InstructListAdapter.FLAG_INSTR_LIST);
        }
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.secur_bt:
                //backup instruct button
                break;
            default:
                break;
        }
    }

    // 加载指令列表数据
    private ArrayList<InstructModel> loadInstructList() {
        ArrayList<InstructModel> instructs = new ArrayList<InstructModel>();
        int oneKeyImage = R.drawable.theft_onekey;
        int oneKey = R.string.phone_security_instruct_onkey;
        int oneKeyContent = R.string.phone_security_instruct_onkey_description;
        instructs.add(new InstructModel(oneKeyImage, oneKeyContent, oneKey, false));

        int locateImage = R.drawable.theft_location;
        int locatePostion = R.string.phone_security_instruct_track;
        int locatePostionContent = R.string.phone_security_instruct_track_description;
        instructs.add(new InstructModel(locateImage, locatePostionContent, locatePostion, false));

        int alertImage = R.drawable.theft_alert;
        int alert = R.string.phone_security_instruct_alert;
        int alertContent = R.string.phone_security_instruct_alert_description;
        instructs.add(new InstructModel(alertImage, alertContent, alert, false));

        int alertOffImage = R.drawable.theft_closealert;
        int alertOff = R.string.phone_security_instruct_alert_off;
        int alertOffContent = R.string.phone_security_instruct_alert_off_description;
        instructs.add(new InstructModel(alertOffImage, alertOffContent, alertOff, false));

        int formateDataImage = R.drawable.theft_format;
        int formateData = R.string.phone_security_instruct_formatedata;
        int formateDataContent = R.string.phone_security_instruct_formatedata_description;
        instructs.add(new InstructModel(formateDataImage, formateDataContent, formateData, false));

        int lockImage = R.drawable.theft_lock;
        int lockPhone = R.string.phone_security_instruct_lock;
        int lockPhoneContent = R.string.phone_security_instruct_lock_description;
        instructs.add(new InstructModel(lockImage, lockPhoneContent, lockPhone, false));

        return instructs;
    }


}