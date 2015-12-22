package com.leo.appmaster.callfilter;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.DayTrafficSetting.MyAdapter;

public class CallFilterRecordActivity extends BaseActivity implements OnClickListener {
    private TextView mTvTitleName;
    private TextView mTvTitleNumber;
    private ListView mLvMain;
    private RippleView mRvBack;
    private ArrayList<String> mRecordTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callfilter_record);
        initUI();
    }

    private void initUI() {
        mTvTitleName = (TextView) findViewById(R.id.tv_callfilter_record_title_name);
        mTvTitleName.setText("李狗蛋");//TODO
        mTvTitleNumber = (TextView) findViewById(R.id.tv_callfilter_record_title_number);
        mTvTitleNumber.setText("13421372770");//TODO
        mLvMain = (ListView) findViewById(R.id.lv_callfilter_record_main);
        mRvBack = (RippleView) findViewById(R.id.rv_back);
        mRvBack.setOnClickListener(this);
        mRecordTime = new ArrayList<String>();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        //TODO
        mRecordTime.clear();
        mRecordTime.add("4:43 am");
        mRecordTime.add("5:10 am");
        mRecordTime.add("6:28 am");
        mRecordTime.add("9:28 am");
        mLvMain.setAdapter(new MyAdapter());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rv_back:
                finish();
                break;

            default:
                break;
        }
    }
    
    
    
    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mRecordTime.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder holder = null;
            if (convertView == null) {
                holder = new ItemHolder();
                View view = LayoutInflater.from(CallFilterRecordActivity.this).inflate(R.layout.item_callfilter_record, null);
                holder.tv = (TextView) view.findViewById(R.id.tv_record_time);
                view.setTag(holder);
                convertView = view;
            } else {
                holder = (ItemHolder) convertView.getTag();
            }
            holder.tv.setText(mRecordTime.get(position));
            return convertView;
        }
    }
    
    class ItemHolder{
        public TextView tv;
    }
}
