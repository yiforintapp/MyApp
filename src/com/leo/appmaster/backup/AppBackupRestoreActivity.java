
package com.leo.appmaster.backup;

import com.leo.appmaster.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class AppBackupRestoreActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);
        initUI();
    }

    private void initUI() {
       ListView list = (ListView)findViewById(R.id.backup_list);
       AppBackupAdapter adapter = new AppBackupAdapter();
       list.setAdapter(adapter);
    }

}
