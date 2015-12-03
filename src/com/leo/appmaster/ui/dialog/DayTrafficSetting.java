
package com.leo.appmaster.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.DayTrafficSetEvent;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.DayTrafficInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;

public class DayTrafficSetting extends LEOBaseDialog implements OnItemClickListener {
    public static final String DAY_TRAFFIC_SETTING = "day_traffic_setting";
    private Context mContext;
    private ListView daytraffic_lv;
    private TextView cancel_button;
    private List<DayTrafficInfo> mList;
    // record the current checked radio number
    private RippleView mRvBlue;
    private int checkedIndex = -1;
    private String[] itemsEn = {
            "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th",
            "13th",
            "14th", "15th", "16th", "17th", "18th", "19th", "20th", "21st", "22nd", "23rd",
            "24th", "25th", "26th", "27th", "28th", "29th", "30th", "31st"
    };

    private String[] itemsZh = {
            "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日", "11日", "12日", "13日",
            "14日",
            "15日", "16日", "17日", "18日", "19日", "20日", "21日", "22日", "23日", "24日", "25日", "26日",
            "27日", "28日",
            "29日", "30日", "31日",
    };

    private String language;
    private int renewday;
    LayoutInflater mInflater;
    private MyAdapter mAdapter;
    private AppMasterPreference sp_notice_flow;
    private OnDayDiaogClickListener mListener;
    private Resources resources;

    public interface OnDayDiaogClickListener {
        public void onClick();
    }

    public DayTrafficSetting(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        sp_notice_flow = AppMasterPreference.getInstance(mContext);
        initUI();
    }

    private void initUI() {
        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "datapage", "cycle");

        language = AppwallHttpUtil.getLanguage();
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_day_setting, null);
        mList = new ArrayList<DayTrafficInfo>();
        mList = fillList();
        mInflater = getLayoutInflater();
        resources = AppMasterApplication.getInstance().getResources();
        cancel_button = (TextView) dlgView.findViewById(R.id.cancel_button);
        mRvBlue = (RippleView) dlgView.findViewById(R.id.rv_blue);
        daytraffic_lv = (ListView) dlgView.findViewById(R.id.daytraffic_lv);
        mAdapter = new MyAdapter(mList);
        daytraffic_lv.setAdapter(mAdapter);
        daytraffic_lv.setOnItemClickListener(this);
        daytraffic_lv.setSelection(renewday - 1);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {
                    mListener.onClick();
                }
                dialog.dismiss();
            }
        };

        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    private List<DayTrafficInfo> fillList() {
        List<DayTrafficInfo> list = new ArrayList<DayTrafficInfo>();
        DayTrafficInfo info;
        renewday = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getDataCutDay();
        for (int i = 0; i < itemsEn.length; i++) {
            info = new DayTrafficInfo();
            if (i + 1 == renewday) {
                info.setCheck(true);
            } else {
                info.setCheck(false);
            }
            if (language.equals("zh")) {
                info.setDay(itemsZh[i]);
            } else {
                info.setDay(itemsEn[i]);
            }
            list.add(info);
        }
        return list;
    }

    public class MyAdapter extends BaseAdapter {
        List<DayTrafficInfo> list;

        public MyAdapter(List<DayTrafficInfo> abcList) {
            this.list = abcList;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrafficHolder mHolder;
            if (convertView == null) {
                mHolder = new TrafficHolder();
                convertView = mInflater.inflate(R.layout.traffic_day_list, parent, false);
                mHolder.tv_showday = (TextView) convertView.findViewById(R.id.tv_showday);
                mHolder.iv_showday = (ImageView) convertView.findViewById(R.id.iv_showday);
                convertView.setTag(mHolder);
            } else {
                mHolder = (TrafficHolder) convertView.getTag();
            }
            // mHolder.iv_showday.setId(position);

            // 让子控件button失去焦点 这样不会覆盖掉item的焦点 否则点击item 不会触发响应即onItemClick失效
            // mHolder.iv_showday.setFocusable(false);//无此句点击item无响应的
            // mHolder.iv_showday.setChecked(position == checkedIndex);
            // mHolder.iv_showday.setOnCheckedChangeListener(new
            // CompoundButton.OnCheckedChangeListener() {
            // @Override
            // public void onCheckedChanged(CompoundButton buttonView, boolean
            // isChecked) {
            // if(isChecked){
            // //set pre radio button
            // if(checkedIndex != -1)
            // {
            // int childId = checkedIndex -
            // daytraffic_lv.getFirstVisiblePosition();
            // if(childId >= 0){
            // View item = daytraffic_lv.getChildAt(childId);
            // if(item != null){
            // RadioButton rb = (RadioButton)item.findViewById(checkedIndex);
            // if(rb != null)
            // rb.setChecked(false);
            // }
            // }
            // }
            // //set cur radio button
            // checkedIndex = buttonView.getId();
            // }
            // }
            // });

            DayTrafficInfo mInfo = list.get(position);
            if (mInfo.isCheck()) {
                mHolder.iv_showday.setImageResource(R.drawable.dialog_check_on);
            } else {
                mHolder.iv_showday.setImageResource(R.drawable.dialog_check_off);
            }
            mHolder.tv_showday.setText(mInfo.getDay());

            return convertView;
        }
    }

    class TrafficHolder {
        TextView tv_showday;
        ImageView iv_showday;
    }

    public void setOnClickListener(OnDayDiaogClickListener listener) {
        mListener = listener;
    }

    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        mRvBlue.setTag(rListener);
        mRvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
                        .getTag();
                lListener.onClick(DayTrafficSetting.this, 1);
            }
        });
//        mRvBlue.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//            @Override
//            public void onRippleComplete(RippleView arg0) {
//                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
//                        .getTag();
//                lListener.onClick(DayTrafficSetting.this, 1);
//            }
//        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // LeoLog.d("DayTraffic", "lv.getFirstVisiblePosition() : " +
        // parent.getFirstVisiblePosition());
        //
        // ListView lv = (ListView) parent;
        // if (checkedIndex != position) {
        // { // 定位到现在处于点击状态的radio
        // int childId = checkedIndex - lv.getFirstVisiblePosition();
        // if (childId >= 0) { // 如果checked =true的radio在显示的窗口内，改变其状态为false
        // View item = lv.getChildAt(childId);
        // if (item != null) {
        // ImageView rb = (ImageView) item.findViewById(checkedIndex);
        // if (rb != null)
        // rb.setImageResource(R.drawable.unradio_buttons);
        // }
        // }
        // // 将当前点击的radio的checked变为true
        // ImageView rb1 = (ImageView) view.findViewById(position);
        // if (rb1 != null)
        // rb1.setImageResource(R.drawable.radio_buttons);
        // checkedIndex = position;
        // }
        // }
        // ImageView iv_show = (ImageView) view.findViewById(R.id.iv_showday);
        // DayTrafficInfo mInfo = mList.get(renewday -1 );
        // DayTrafficInfo mInfo = (DayTrafficInfo) view.getTag();
        // mList.clear();
        // for(int i = 0;i<mList.size();i++){
        // DayTrafficInfo mInfo = mList.get(i);
        // if( i== position){
        // mInfo.setCheck(true);
        // }else {
        // mInfo.setCheck(false);
        // }
        // mList.add(mInfo);
        // }
        int renewDaym = position + 1;
        ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).setDataCutDay(renewDaym);
//        sp_notice_flow.setRenewDay(renewDaym);

        LeoLog.d("eventbustest", "send eventbus!");
        LeoEventBus.getDefaultBus().post(
                new DayTrafficSetEvent(DAY_TRAFFIC_SETTING));

        this.dismiss();
    }

}
