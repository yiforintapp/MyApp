
package com.leo.appmaster.applocker;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ZhiWenActivity extends Activity implements OnClickListener {
    private final static int ZHIWENWEIZHUANG = 3;
    private TextView tv_zhiwen_title, tv_zhiwen_jieshao;
    private ImageView iv_zhiwen_tips, zhiwen_bang, show_slowly_iv, iv_zhiwen_click;
    private AppMasterPreference sp_zhiwen_weizhuang;
    // three click
    long[] mHits = new long[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weizhuang_zhiwen);
        init();
    }

    private void init() {
        sp_zhiwen_weizhuang = AppMasterPreference.getInstance(this);
        
        tv_zhiwen_title = (TextView) findViewById(R.id.tv_zhiwen_title);
        tv_zhiwen_title.setVisibility(View.VISIBLE);

        tv_zhiwen_jieshao = (TextView) findViewById(R.id.tv_zhiwen_jieshao);
        tv_zhiwen_jieshao.setVisibility(View.VISIBLE);

        iv_zhiwen_tips = (ImageView) findViewById(R.id.iv_zhiwen_tips);
        iv_zhiwen_tips.setVisibility(View.VISIBLE);

        iv_zhiwen_click = (ImageView) findViewById(R.id.iv_zhiwen_click);
        iv_zhiwen_click.setOnClickListener(this);
        show_slowly_iv = (ImageView) findViewById(R.id.show_slowly_iv);
        zhiwen_bang = (ImageView) findViewById(R.id.zhiwen_bang);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_zhiwen_click:
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 800)) {
                    Toast.makeText(this, getString(R.string.weizhuang_setting_ok), 0).show();
                    sp_zhiwen_weizhuang.setPretendLock(ZHIWENWEIZHUANG);
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
