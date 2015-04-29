
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;

import android.app.Activity;
import android.os.Bundle;
import com.leo.appmaster.quickgestures.view.QuickGestureLayout;
import com.leo.appmaster.quickgestures.view.QuickGestureLayout.LayoutParams;

import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

public class QuickGesturePopup extends Activity {

    QuickGestureLayout qgLayout1, qgLayout2;
    private ImageView iv0;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private ImageView iv4;
    private ImageView iv5;
    private ImageView iv6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture);
        qgLayout1 = (QuickGestureLayout) findViewById(R.id.qg_layout1);
        qgLayout2 = (QuickGestureLayout) findViewById(R.id.qg_layout2);

        fillQg1();
//        fillQg2();
    }

    private void fillQg1() {
        LayoutParams params = null;
        iv0 = new ImageView(this);
        iv0.setImageResource(R.drawable.add_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 0;
        iv0.setLayoutParams(params);
        qgLayout1.addView(iv0);

        iv1 = new ImageView(this);
        iv1.setImageResource(R.drawable.app_done_bnt);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 1;
        iv1.setLayoutParams(params);
        qgLayout1.addView(iv1);

        iv2 = new ImageView(this);
        iv2.setImageResource(R.drawable.app_delete_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 2;
        iv2.setLayoutParams(params);
        qgLayout1.addView(iv2);

        iv3 = new ImageView(this);
        iv3.setImageResource(R.drawable.add_mode_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 3;
        iv3.setLayoutParams(params);
        qgLayout1.addView(iv3);

        iv4 = new ImageView(this);
        iv4.setImageResource(R.drawable.app_data_usage_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 4;
        iv4.setLayoutParams(params);
        qgLayout1.addView(iv4);

        iv5 = new ImageView(this);
        iv5.setImageResource(R.drawable.app_all_backuped);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 5;
        iv5.setLayoutParams(params);
        qgLayout1.addView(iv5);

        iv6 = new ImageView(this);
        iv6.setImageResource(R.drawable.add_contacts_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 6;
        iv6.setLayoutParams(params);
        iv6.setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                v.startDrag(null, new DragShadowBuilder(v), v, 0);
                return true;
            }
        });
        qgLayout1.addView(iv6);
//        qgLayout1.setRotation(-45);
    }

    private void fillQg2() {
        LayoutParams params = null;
        iv0 = new ImageView(this);
        iv0.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 0;
        iv0.setLayoutParams(params);
        qgLayout2.addView(iv0);

        iv1 = new ImageView(this);
        iv1.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 1;
        iv1.setLayoutParams(params);
        qgLayout2.addView(iv1);

        iv2 = new ImageView(this);
        iv2.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 2;
        iv2.setLayoutParams(params);
        qgLayout2.addView(iv2);

        iv3 = new ImageView(this);
        iv3.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 3;
        iv3.setLayoutParams(params);
        qgLayout2.addView(iv3);

        iv4 = new ImageView(this);
        iv4.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 4;
        iv4.setLayoutParams(params);
        qgLayout2.addView(iv4);

        iv5 = new ImageView(this);
        iv5.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 5;
        iv5.setLayoutParams(params);
        qgLayout2.addView(iv5);

        iv6 = new ImageView(this);
        iv6.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 6;
        iv6.setLayoutParams(params);
        qgLayout2.addView(iv6);
        qgLayout2.setRotation(-315);
    }

    public void removeItem(View v) {
        qgLayout1.removeView(iv2);
    }

    public void addItem(View v) {
        qgLayout1.addView(iv2);
    }
}
