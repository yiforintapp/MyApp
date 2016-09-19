package com.zlf.appmaster.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.zlf.appmaster.R;
import com.zlf.appmaster.home.HomeMainActivity;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.ui.WheelView;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/9/19.
 */
public class StockSelectDialog extends LEOBaseDialog {

    private WheelView mWv;
    private HomeMainActivity mContext;
    private int mPosition;
    private RippleView mRvBlue;


    public StockSelectDialog(Context context) {
        super(context, R.style.LoginProgressDialog);
        mContext = (HomeMainActivity) context;
        View dlgView = LayoutInflater.from(context.getApplicationContext()).inflate(
                R.layout.stock_select_dialog, null);
        setContentView(dlgView);
        mWv = (WheelView) dlgView.findViewById(R.id.wheel_view_wv);
        mRvBlue = (RippleView) dlgView.findViewById(R.id.rv_blue);
    }

    public void setData(String[] data, int position) {
        mWv.setOffset(2);
        mWv.setItems(Arrays.asList(data));
        mPosition = position;
        mWv.setSeletion(position);
        mWv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                mPosition = selectedIndex - 2;
            }
        });
    }

    public void setBottomBtnListener(OnClickListener bListener) {
        if (bListener != null) {
            mRvBlue.setTag(bListener);
            mRvBlue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OnClickListener listener = (OnClickListener) view
                            .getTag();
                    try {
                        mContext.getHomeTabFragment().setSelectStock(mPosition);
                        listener.onClick(StockSelectDialog.this, 0);
                        dismiss();
                    } catch (Exception e) {
                    }
                }
            });
        }
    }


}
