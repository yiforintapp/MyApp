
package com.leo.appmaster.privacycontact;

import android.content.Context;
import android.os.AsyncTask;

public class LoadSysContactTask extends AsyncTask<String, String, Boolean> {
    private Context mContext;

    public LoadSysContactTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... arg0) {

        PrivacyContactManager pm = PrivacyContactManager.getInstance(mContext);
        pm.loadSysCallLog();
        pm.loadSysContacts();
        pm.loadSysMessage();
        return null;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
    }
}
