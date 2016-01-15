package com.leo.appmaster.home;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页上拉列表Adaper
 * Created by Jasper on 2015/10/8.
 */
public class HomeMoreAdapter extends BaseAdapter {
    private static final int ITEM_LABEL = 1;

    private static final int IDX_LABEL = 0;

    private static final int[] SWIFTY = new int[]{
            R.string.up_list_swifty_title,
            R.string.hp_helper_iswipe
    };

    private static final int[] ARRAY_MEDIA = new int[]{
            R.string.hp_media_label,
            R.string.hp_hide_img,
            R.string.hp_hide_video
    };

    private static final int[] ARRAY_CONTACT_SINGLE = new int[]{
            R.string.class_privacy_protection,
            R.string.home_tab_lost,
            R.string.privacy_contacts
    };

    private static final int[] ARRAY_CONTACT = new int[]{
            R.string.hp_contact_lable,
            R.string.call_filter_name,
            R.string.hp_contact_call,
            R.string.hp_contact_sms
    };

    private static final int[] ARRAY_APP = new int[]{
            R.string.hp_app_manage_label,
            R.string.hp_app_manage_del,
            R.string.hp_app_manage_back
    };

    private static final int[] ARRAY_DEVICE = new int[]{
            R.string.hp_device_label,
            R.string.hp_device_gprs,
            R.string.hp_device_power
    };

    private static final int[] ARRAY_DEVICE_51 = new int[]{
            R.string.hp_device_label,
            R.string.hp_device_gprs
    };

    private static final int[] ARRAY_HELPER = new int[]{
            R.string.hp_helper_label,
            R.string.hp_helper_shot
    };
    private static final int ID_RES_HIDE_IMG = R.drawable.ic_up_hide_img;
    private static final int ID_RES_HIDE_VIDEO = R.drawable.ic_up_hide_video;
    private static final int ID_RES_CONTACT_CALL = R.drawable.ic_up_contact_call;
    private static final int ID_RES_CONTACT_SMS = R.drawable.ic_up_contact_sns;
    private static final int ID_RES_PHONE_LOST = R.drawable.ic_up_phone_lost;
    private static final int ID_RES_INTERCEPT = R.drawable.intercept;
    private static final int ID_RES_CONTACT = R.drawable.ic_up_contact;
    private static final int ID_RES_APP_DEL = R.drawable.ic_up_del;
    private static final int ID_RES_APP_BACK = R.drawable.ic_up_back;
    private static final int ID_RES_DEVICE_GPRS = R.drawable.ic_up_gprs;
    private static final int ID_RES_DEVICE_POWER = R.drawable.ic_up_power;
    private static final int ID_RES_HELPER_SHOT = R.drawable.ic_up_short;
    private static final int ID_RES_HELPER_ISWIPE = R.drawable.ic_up_iswipe;

    private SparseIntArray mStrArray;
    private List<Integer> mLabelArray;

    private LayoutInflater mInflater;

    private Context mContext;
    private SparseIntArray mDrawableArray;

    public HomeMoreAdapter() {
        mContext = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(mContext);
        mStrArray = new SparseIntArray();
        mLabelArray = new ArrayList<Integer>();
        mDrawableArray = new SparseIntArray();

        List<int[]> arrayList = new ArrayList<int[]>();
        arrayList.add(SWIFTY);
        arrayList.add(ARRAY_MEDIA);
        arrayList.add(ARRAY_CONTACT_SINGLE);
        arrayList.add(ARRAY_APP);
        arrayList.add(ARRAY_DEVICE);
        arrayList.add(ARRAY_HELPER);

        mDrawableArray.put(R.string.hp_helper_iswipe, ID_RES_HELPER_ISWIPE);
        mDrawableArray.put(R.string.hp_hide_img, ID_RES_HIDE_IMG);
        mDrawableArray.put(R.string.hp_hide_video, ID_RES_HIDE_VIDEO);
        mDrawableArray.put(R.string.home_tab_lost, ID_RES_PHONE_LOST);
        mDrawableArray.put(R.string.privacy_contacts, ID_RES_CONTACT);
        mDrawableArray.put(R.string.hp_app_manage_del, ID_RES_APP_DEL);
        mDrawableArray.put(R.string.hp_app_manage_back, ID_RES_APP_BACK);
        mDrawableArray.put(R.string.hp_device_gprs, ID_RES_DEVICE_GPRS);
        mDrawableArray.put(R.string.hp_device_power, ID_RES_DEVICE_POWER);
        mDrawableArray.put(R.string.hp_helper_shot, ID_RES_HELPER_SHOT);

        int num = 0;
        for (int[] array : arrayList) {
            for (int i = 0; i < array.length; i++) {
                int value = array[i];
                mStrArray.put(num++, value);
            }
            mLabelArray.add(mStrArray.size() - array.length);
        }

    }

    @Override
    public int getCount() {
        return mStrArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return mStrArray.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLabel(position)) {
            return ITEM_LABEL;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MoreHolder holder = null;
        if (convertView == null) {
            if (getItemViewType(position) == ITEM_LABEL) {

                holder = new MoreHolder();
                convertView = mInflater.inflate(R.layout.home_more_label_item, null);

                holder.textView = (TextView) convertView.findViewById(R.id.more_label_tv);
                convertView.setTag(R.layout.home_more_label_item, holder);
            } else {
                holder = new MoreHolder();
                convertView = mInflater.inflate(R.layout.home_more_item, null);

                holder.textView = (TextView) convertView.findViewById(R.id.more_label_tv);
                holder.imageView = (ImageView) convertView.findViewById(R.id.more_icon_iv);
                holder.divider = convertView.findViewById(R.id.more_divider);
                holder.readTip = (ImageView) convertView.findViewById(R.id.more_red_tip_iv);

                convertView.setTag(R.layout.home_more_item, holder);
            }
        } else {
            if (getItemViewType(position) == ITEM_LABEL) {
                holder = (MoreHolder) convertView.getTag(R.layout.home_more_label_item);
            } else {
                holder = (MoreHolder) convertView.getTag(R.layout.home_more_item);
            }
        }

        if (holder == null) return convertView;

        int stringId = mStrArray.get(position);
        holder.textView.setText(stringId);
        if (getItemViewType(position) != ITEM_LABEL) {
            checkReddotVisibility(position, holder);
        }
        if (!isLabel(position)) {
            if (isLabel(position + 1)) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    private void checkReddotVisibility(int position, MoreHolder holder) {
        int stringId = mStrArray.get(position);
        int drawableId = mDrawableArray.get(stringId);
        if (holder.imageView != null) {
            holder.imageView.setImageResource(drawableId);
        }

        Context context = AppMasterApplication.getInstance();
        AppMasterPreference preference = AppMasterPreference.getInstance(context);
        if (drawableId == ID_RES_CONTACT) {
            int callCount = preference.getCallLogNoReadCount();
            int msgCount = preference.getMessageNoReadCount();
           /*4.4以上不去做短信操作*/
            boolean isLessLeve19 = PrivacyContactUtils.isLessApiLeve19();
            if (!isLessLeve19) {
                if (msgCount > 0) {
                    msgCount = 0;
                    preference.setMessageNoReadCount(msgCount);
                }
            }
            if (callCount > 0 || msgCount > 0) {
                holder.readTip.setVisibility(View.VISIBLE);
            } else {
                holder.readTip.setVisibility(View.GONE);
            }
        } else if (drawableId == ID_RES_CONTACT_CALL) {
            int callCount = preference.getCallLogNoReadCount();
            int msgCount = preference.getMessageNoReadCount();
            if (callCount > 0) {
                holder.readTip.setVisibility(View.VISIBLE);
            } else if (msgCount <= 0) {
                holder.readTip.setVisibility(View.GONE);
            }
        } else if (drawableId == ID_RES_CONTACT_SMS) {
            int callCount = preference.getCallLogNoReadCount();
            int msgCount = preference.getMessageNoReadCount();
            if (msgCount > 0) {
                holder.readTip.setVisibility(View.VISIBLE);
            } else if (callCount <= 0) {
                holder.readTip.setVisibility(View.GONE);
            }
        } else if (drawableId == ID_RES_HIDE_IMG) {
            PreferenceTable preferenceTable = PreferenceTable.getInstance();
            boolean picReddotExist = preferenceTable.getBoolean(PrefConst.KEY_PIC_REDDOT_EXIST, false);
            holder.readTip.setVisibility(picReddotExist ? View.VISIBLE : View.GONE);
        } else if (drawableId == ID_RES_HIDE_VIDEO) {
            PreferenceTable preferenceTable = PreferenceTable.getInstance();
            boolean vidReddotExist = preferenceTable.getBoolean(PrefConst.KEY_VID_REDDOT_EXIST, false);
            holder.readTip.setVisibility(vidReddotExist ? View.VISIBLE : View.GONE);
        } else {
            holder.readTip.setVisibility(View.GONE);
        }
    }

    private boolean isLabel(int position) {
        try {
            return mLabelArray.contains(position);
        } catch (Exception e) {
            return false;
        }
    }

    private static class MoreHolder {
        TextView textView;
        ImageView imageView;
        View divider;
        ImageView readTip;
    }
}
