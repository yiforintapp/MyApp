package com.leo.appmaster.callfilter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.privacycontact.CircleImageView;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOWithSingleCheckboxDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qili on 15-10-10.
 */
public class CallFilterFragmentAdapter extends BaseAdapter {
    private static final long DAY_COUNT = 86400000;
    private List<CallFilterInfo> mList;
    private String mFlag;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private List<ContactBean> mSysContacts;

    public CallFilterFragmentAdapter(Context mContext) {
        this.mContext = mContext;
        mList = new ArrayList<CallFilterInfo>();
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        BlackListHolder holder;
        if (convertView == null) {

            convertView = layoutInflater.inflate(R.layout.fragment_call_filter_item, null);

            holder = new BlackListHolder();
            holder.imageView = (CircleImageView) convertView.findViewById(R.id.iv_icon);
            holder.title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.desc = (TextView) convertView.findViewById(R.id.tv_desc);
            holder.time = (TextView) convertView.findViewById(R.id.tv_call_time);
            holder.filternum = (TextView) convertView.findViewById(R.id.tv_call_times);

            convertView.setTag(holder);
        } else {
            holder = (BlackListHolder) convertView.getTag();
        }

        RippleView view = (RippleView) convertView;
        view.setNeedLongClick(true);


        CallFilterInfo info = mList.get(i);
        String numberName = info.numberName;
        String number = info.number;
        int filterType = info.filterType;
        long time = info.getTimeLong();
        int filternum = info.getFilterCount();
        if (filternum < 1) {
            filternum = 1;
        }
        if (info.getIcon() != null) {
            holder.imageView.setImageBitmap(info.getIcon());
        } else {
            holder.imageView.setImageResource(R.drawable.default_user_avatar);
        }

        holder.time.setText(getTime(time));
        holder.filternum.setText(filternum + "");

        if (Utilities.isEmpty(numberName) || !checkIsSysContact(number, mSysContacts)) {
            if (filterType == 0) {
                holder.title.setText(number);
                holder.desc.setText(number);
            } else {
                String string;
                if (filterType == CallFilterConstants.MK_CRANK) {
                    string = mContext.getString(R.string.filter_number_type_saorao);
                } else if (filterType == CallFilterConstants.MK_ADVERTISE) {
                    string = mContext.getString(R.string.filter_number_type_ad);
                } else {
                    string = mContext.getString(R.string.filter_number_type_zhapian);
                }
                holder.title.setText(string);
                holder.desc.setText(number);
            }
        } else {
            holder.title.setText(numberName);
            holder.desc.setText(number);
        }

        return convertView;
    }

    private String getTime(long time) {
        boolean showDay = false;
        boolean showYear = false;
        SimpleDateFormat finalFormat;
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String thatYear = yearFormat.format(time);
        String toYear = yearFormat.format(System.currentTimeMillis());
        if (!thatYear.equals(toYear)) {
            showYear = true;
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        String thatDay = dayFormat.format(time);
        String toDay = dayFormat.format(System.currentTimeMillis());
        if (!thatDay.equals(toDay)) {
            showDay = true;
        }


        if (showYear) {
            finalFormat = new SimpleDateFormat("yyyy-MM-dd hh:mma");
        } else if (showDay) {
            finalFormat = new SimpleDateFormat("MM-dd hh:mma");
        } else {
            finalFormat = new SimpleDateFormat("hh:mma");
        }

        String finalString = finalFormat.format(time);

        return finalString;
    }


    public static class BlackListHolder {
        CircleImageView imageView;
        TextView title;
        TextView desc;
        TextView filternum;
        TextView time;
    }

    public void setData(List<CallFilterInfo> infoList, List<ContactBean> mSysList) {
        mList = infoList;
        mSysContacts = mSysList;
        if (mList.size() < 1) {
            CallFilterMainActivity callFilterMainActivity =
                    (CallFilterMainActivity) mContext;
            callFilterMainActivity.callFilterShowEmpty();
        }
        notifyDataSetChanged();
    }

    public void setFlag(String fromWhere) {
        mFlag = fromWhere;
    }

    private boolean checkIsSysContact(String number, List<ContactBean> mSysList) {
        if (mSysList != null && mSysList.size() > 0) {
            String formatNumber = PrivacyContactUtils.formatePhoneNumber(number);
            for (int i = 0; i < mSysContacts.size(); i++) {
                String sysNumber = mSysContacts.get(i).getContactNumber();
                if (TextUtils.isEmpty(sysNumber)) {
                    continue;
                }
                if (sysNumber.contains(formatNumber)) {
                    return true;
                }
            }
        }
        return false;
    }


}
