package com.zlf.appmaster.ui.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.TextAliveInfo;
import com.zlf.appmaster.utils.TimeUtil;

/**
 * Created by Administrator on 2016/11/4.
 */
public class TextAliveAdviceDialog {

    private ImageView mClose;
    private TextView mTvTime;
    private TextView mTvProduct;
    private TextView mTvDirection;
    private TextView mTvStatus;
    private TextView mTvEnter;
    private TextView mTvFP;
    private TextView mTvSP;
    private TextView mTvTP;
    private TextView mTvLose;
    private TextView mTvStatusEnter;
    private TextView mTvStatusFP;
    private TextView mTvStatusSP;
    private TextView mTvStatusTP;
    private TextView mTvStatusLose;

    private Context context;
    private android.app.AlertDialog ad;
    private Window window;
    private TextAliveInfo mItem;

    public TextAliveAdviceDialog(Context context) {
        this.context=context;
        ad=new android.app.AlertDialog.Builder(context).create();
        ad.show();
        //关键在下面的两行,使用window.setContentView,替换整个对话框窗口的布局
        window = ad.getWindow();
        window.setContentView(R.layout.dialog_text_alive_advice);
        initView();
    }

    private void initView() {
        mClose = (ImageView) window.findViewById(R.id.iv_close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mTvTime = (TextView) window.findViewById(R.id.set_time);
        mTvProduct = (TextView) window.findViewById(R.id.set_product);
        mTvDirection = (TextView) window.findViewById(R.id.set_direction);
        mTvStatus = (TextView) window.findViewById(R.id.set_status);
        mTvEnter = (TextView) window.findViewById(R.id.set_enter);
        mTvFP = (TextView) window.findViewById(R.id.set_f_p);
        mTvSP = (TextView) window.findViewById(R.id.set_s_p);
        mTvTP = (TextView) window.findViewById(R.id.set_t_p);
        mTvLose = (TextView) window.findViewById(R.id.set_lose);
        mTvStatusEnter = (TextView) window.findViewById(R.id.set_status_enter);
        mTvStatusFP = (TextView) window.findViewById(R.id.set_status_f_p);
        mTvStatusSP = (TextView) window.findViewById(R.id.set_status_s_p);
        mTvStatusTP = (TextView) window.findViewById(R.id.set_status_t_p);
        mTvStatusLose = (TextView) window.findViewById(R.id.set_status_lose);
    }


    public void setInfo(TextAliveInfo textAliveInfo) {
        this.mItem = textAliveInfo;
        mTvTime.setText(mItem.getmSendTime());
        mTvProduct.setText(mItem.getmPlanType());
        mTvDirection.setText(mItem.getmPlan());
        mTvStatus.setText(mItem.getmStatus());
        mTvFP.setText(mItem.getmProfitOne());
        mTvSP.setText(mItem.getmProfitTwo());
        mTvTP.setText(mItem.getmProfitThree());
        mTvEnter.setText(mItem.getmTargetEnter());
        mTvStatusFP.setText(mItem.getmProfitStatusOne());
        mTvStatusSP.setText(mItem.getmProfitStatusTwo());
        mTvStatusTP.setText(mItem.getmProfitStatusThree());
        mTvLose.setText(mItem.getmLose());
    }

    public void dismiss() {
        ad.dismiss();
    }

    public boolean isShowing(){
        return ad.isShowing();
    }
}
