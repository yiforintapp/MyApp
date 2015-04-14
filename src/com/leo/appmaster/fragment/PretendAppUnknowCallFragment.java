
package com.leo.appmaster.fragment;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

public class PretendAppUnknowCallFragment extends PretendFragment implements OnTouchListener {
    private float iv_guaduan_top, iv_guaduan_right, iv_guaduan_bottom, iv_duanxin_left,
    iv_duanxin_bottom, iv_duanxin_right, iv_jieting_top, iv_jieting_left,
    iv_jieting_bottom;
    private ImageView iv_jieting, iv_guaduan, iv_duanxin;
    private View  bottom_view;
    
    @Override
    protected int layoutResourceId() {
//        return R.layout.activity_unknowcall_show_first;
        return R.layout.activity_weizhuang_firstin;
    }

    @Override
    protected void onInitUI() {
//        bottom_view = findViewById(R.id.bottom_view);
//        bottom_view.setOnTouchListener(this);
//
//        iv_jieting = (ImageView) findViewById(R.id.iv_jieting);
//        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
//        iv_duanxin = (ImageView) findViewById(R.id.iv_duanxin);
//        
//        getThreeButton();
    }
    
    private void getThreeButton() {
        ViewTreeObserver guaduan = iv_guaduan.getViewTreeObserver();
        guaduan.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_guaduan_top = iv_guaduan.getTop();
                iv_guaduan_right = iv_guaduan.getRight();
                iv_guaduan_bottom = iv_guaduan.getBottom();
                LeoLog.d("testFragment", "iv_guaduan_top is : " + iv_guaduan_top
                        + "--iv_guaduan_bottom is : " + iv_guaduan_bottom + "--iv_guaduan_right is : "
                        + iv_guaduan_right);
                // 成功调用一次后，移除 Hook 方法，防止被反复调用
                // removeGlobalOnLayoutListener() 方法在 API 16 后不再使用
                // 使用新方法 removeOnGlobalLayoutListener() 代替
                iv_guaduan.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } 
        });
        
        ViewTreeObserver duanxin = iv_duanxin.getViewTreeObserver();
        duanxin.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_duanxin_left = iv_duanxin.getLeft();
                iv_duanxin_bottom = iv_duanxin.getBottom();
                iv_duanxin_right = iv_duanxin.getRight();
                LeoLog.d("testFragment", "iv_duanxin_left is : " + iv_duanxin_left
                        + "--iv_duanxin_bottom is : " + iv_duanxin_bottom + "--iv_duanxin_right is : "
                        + iv_duanxin_right);
                // 成功调用一次后，移除 Hook 方法，防止被反复调用
                // removeGlobalOnLayoutListener() 方法在 API 16 后不再使用
                // 使用新方法 removeOnGlobalLayoutListener() 代替
                iv_duanxin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } 
        });
        
        ViewTreeObserver jieting = iv_jieting.getViewTreeObserver();
        jieting.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_jieting_top = iv_jieting.getTop();
                iv_jieting_left = iv_jieting.getLeft();
                iv_jieting_bottom = iv_jieting.getBottom();
                LeoLog.d("testFragment", "iv_jieting_top is : " + iv_jieting_top
                        + "--iv_jieting_left is : " + iv_jieting_left + "--iv_jieting_bottom is : "
                        + iv_jieting_bottom);
                // 成功调用一次后，移除 Hook 方法，防止被反复调用
                // removeGlobalOnLayoutListener() 方法在 API 16 后不再使用
                // 使用新方法 removeOnGlobalLayoutListener() 代替
                iv_jieting.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } 
        });
    }

    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

}
