package com.zlf.appmaster.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.hometab.SelectStockActivity;
import com.zlf.appmaster.model.SelectStockInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/17.
 */
public class SwapListAdapter extends BaseAdapter {

    private ArrayList<SelectStockInfo> mList;
    private Context mContext;
    private LayoutInflater layoutInflater;


    public SwapListAdapter(Context context, ArrayList<SelectStockInfo> themes) {
        mContext = context;
        this.mList = themes;
        this.layoutInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {

        return mList != null ? mList.get(arg0) : null;
    }

    @Override
    public long getItemId(int arg0) {

        return arg0;
    }

    public static class ViewHolder {
        TextView name;
        ImageView mCheckBox;
        ImageView mSwapBtn;
        LinearLayout mParent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        convertView = showNormalTypeItem(position, convertView);

        return convertView;
    }

    private View showNormalTypeItem(int position, View convertView) {
        ViewHolder viewHolder;
        if (mList.get(position).mType != SelectStockInfo.TITLE) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.swap_select_item, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.swap_tv);
            viewHolder.mCheckBox = (ImageView) convertView.findViewById(R.id.swap_checkbox);
            viewHolder.mSwapBtn = (ImageView) convertView.findViewById(R.id.swap_btn);
            viewHolder.mParent = (LinearLayout) convertView.findViewById(R.id.parent);
            convertView.setTag(viewHolder);

            viewHolder.name.setText(mList.get(position).mStockIndex.getName());
            viewHolder.mCheckBox.setOnClickListener(new ClickListener(position, viewHolder.mParent, viewHolder.mCheckBox));
            viewHolder.mSwapBtn.setOnClickListener(new ClickListener(position, viewHolder.mParent, viewHolder.mCheckBox));
            if (mList.get(position).mType == SelectStockInfo.CURRENT_SELECT) {
                if (position == 1) {
                    viewHolder.mSwapBtn.setVisibility(View.GONE);
                } else {
                    viewHolder.mSwapBtn.setVisibility(View.VISIBLE);
                }
                viewHolder.mCheckBox.setImageResource(R.drawable.stock_select);
            } else {
                viewHolder.mSwapBtn.setVisibility(View.GONE);
                viewHolder.mCheckBox.setImageResource(R.drawable.stock_unselect);
            }

        } else {
            convertView = layoutInflater.inflate(R.layout.swap_select_title, null);
            if (position != 0) {
                TextView title = (TextView) convertView.findViewById(R.id.title_left);
                title.setText("可选行情");
                TextView titleRight = (TextView) convertView.findViewById(R.id.title_right);
                titleRight.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    class ClickListener implements View.OnClickListener {

        private int position;
        private LinearLayout mParent;
        private ImageView mCheckBox;

        ClickListener(int pos, LinearLayout layout, ImageView checkBox) {
            position = pos;
            mParent = layout;
            mCheckBox = checkBox;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.swap_checkbox:
                    if (SelectStockInfo.OTHERS == mList.get(position).mType) {
                        mCheckBox.setImageResource(R.drawable.stock_select);
                    } else {
                        mCheckBox.setImageResource(R.drawable.stock_unselect);
                    }
//                    deletePattern(mParent, position);
                    ((SelectStockActivity) mContext).deleteList(position, mList.get(position).mType);
                    break;
                case R.id.swap_btn:
                    ((SelectStockActivity) mContext).sortList(position);
                    break;
            }
        }
    }

    private void deletePattern(final View view, final int position) {

        Animation.AnimationListener al = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ((SelectStockActivity) mContext).deleteList(position, mList.get(position).mType);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        collapse(view, al);

    }

    private void collapse(final View view, Animation.AnimationListener al) {
        final int originHeight = view.getMeasuredHeight();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1.0f) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = originHeight - (int) (originHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        if (al != null) {
            animation.setAnimationListener(al);
        }
        animation.setDuration(300);
        view.startAnimation(animation);
    }

}
