
package com.leo.appmaster.quickgestures;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.leo.appmaster.applocker.service.StatusBarEventService;

public class IswipeUpdateNotifiProxyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int type = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                StatusBarEventService.EVENT_ISWIPE_UPDATE_NOTIFICATION);
        if (type != StatusBarEventService.EVENT_EMPTY
                && type == StatusBarEventService.EVENT_ISWIPE_UPDATE_NOTIFICATION) {
            Intent intentService = new Intent(this,
                    StatusBarEventService.class);
            intentService.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                    type);
            startService(intentService);
        }
        finish();
    }
}
