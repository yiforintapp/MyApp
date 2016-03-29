package com.leo.appmaster.phoneSecurity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;


import java.util.List;

/**
 * Created by runlee on 15-10-17.
 */
public class InstructListAdapter extends BaseAdapter {
    public static final String FLAG_INSTR_LIST = "INSTRUCT_LIST";
    public static final String FLAG_OPEN_SUC_INSTR_LIST = "OPEN_SUC_INSTR_LIST";
    public static final String FLAG_NO_OPEN_SUC_INSTR_LIST = "NO_OPEN_SUC_INSTR_LIST";

    private List mList;
    private Context mContext;
    private String mFlag;
    private LayoutInflater mInflate;

    public InstructListAdapter(Context context, List list, String flag) {
        mInflate = LayoutInflater.from(context);
        mList = list;
        mContext = context;
        mFlag = flag;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        ImageView image, instrImage, tipIconImage, selectImage;
        TextView title;
        TextView content;
        TextView instrTxt;
        Button openBt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            vh = new ViewHolder();
            if (FLAG_INSTR_LIST.equals(mFlag)) {
                convertView = mInflate.inflate(R.layout.security_instruct_item, null);
                vh.image = (ImageView) convertView.findViewById(R.id.image);
                vh.title = (TextView) convertView.findViewById(R.id.title);
                vh.content = (TextView) convertView.findViewById(R.id.content);
                vh.selectImage = (ImageView) convertView.findViewById(R.id.image_tip_icon);
                vh.openBt = (Button) convertView.findViewById(R.id.sec_add_number_BT);
            } else if (FLAG_OPEN_SUC_INSTR_LIST.equals(mFlag)
                    || FLAG_NO_OPEN_SUC_INSTR_LIST.equals(mFlag)) {
                convertView = mInflate.inflate(R.layout.phone_secur_oper_item, null);
                vh.instrImage = (ImageView) convertView.findViewById(R.id.image_instr);
                vh.tipIconImage = (ImageView) convertView.findViewById(R.id.image_tip_icon);
                vh.instrTxt = (TextView) convertView.findViewById(R.id.instr_txt);
            }
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        InstructModel instructModel = (InstructModel) mList.get(position);
        if (FLAG_INSTR_LIST.equals(mFlag)) {
            vh.image.setImageResource(instructModel.image);
            vh.title.setText(mContext.getResources().getString(instructModel.title));
            vh.content.setText(mContext.getResources().getString(instructModel.content));
            if (instructModel.isSelect) {
                vh.selectImage.setVisibility(View.VISIBLE);
                vh.openBt.setVisibility(View.VISIBLE);
                vh.openBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                        ComponentName component = new ComponentName(mContext,
                                DeviceReceiver.class);
                        mLockManager.filterSelfOneMinites();
                        mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                component);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                mContext.getString(R.string.device_admin_extra));
                        try {
                            mContext.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        PhoneSecurityManager.getInstance(mContext).setIsAdvOpenTip(true);
                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft", "theft_open");
                    }
                });
            } else {
                vh.selectImage.setVisibility(View.GONE);
                vh.openBt.setVisibility(View.GONE);
            }
        } else if (FLAG_OPEN_SUC_INSTR_LIST.equals(mFlag)
                || FLAG_NO_OPEN_SUC_INSTR_LIST.equals(mFlag)) {
            vh.instrImage.setImageResource(instructModel.image);
            vh.tipIconImage.setImageResource(instructModel.selectImage);
            vh.instrTxt.setText(mContext.getResources().getString(instructModel.content));
        }
        return convertView;
    }

    public void setData(List<InstructModel> list) {
        mList = list;
    }
}
