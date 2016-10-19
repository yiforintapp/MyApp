package com.zlf.appmaster.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/13.
 */
public class SelectPopupWindow extends PopupWindow implements AdapterView.OnItemClickListener {

    private View conentView;
    private GridView mGridView;
    private MenuListAdapter mAdapter;
    private ArrayList<String> mItems;
    private int mCurrentIndex;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
         void itemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public SelectPopupWindow(final Activity context, ArrayList<String> items, int position) {
        mItems = items;
        mCurrentIndex = position;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.select_popup_window, null);
        mGridView = (GridView) conentView.findViewById(R.id.grid_view);

//        mListView.setOnItemClickListener(mPopItemClickListener);
        if (null == mAdapter) {
            mAdapter = new MenuListAdapter();
        }
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        // mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        // 设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimationPreview);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mOnItemClickListener.itemClick(position);
        dismiss();
    }

    static class Holder {
        public TextView mItemName;
        public View mLine;
    }

    private class MenuListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Holder mHolder;

        private MenuListAdapter() {
            inflater = LayoutInflater.from(AppMasterApplication.getInstance());
        }

        @Override
        public int getCount() {
            if (mItems != null) {
                return mItems.size();
            } else {
                return 0;
            }

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
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (convertView != null) {
                mHolder = (Holder) convertView.getTag();
            } else {
                mHolder = new Holder();
                convertView = inflater.inflate(R.layout.select_popup_item, null);
                mHolder.mItemName = (TextView) convertView.findViewById(R.id.select_popup_tv);
                mHolder.mLine = (View) convertView.findViewById(R.id.line);

                convertView.setTag(mHolder);
            }

            mHolder.mItemName.setText(mItems.get(position));
            if (mCurrentIndex == position) {
                mHolder.mItemName.setTextColor(Color.parseColor("#FFE9BA17"));
            }

            setLine(position, mHolder.mLine);

            return convertView;
        }
    }

    private void setLine(int position,View view){
        int i = mItems.size() % 3;
        if (position + i + 1 > mItems.size()) {
            //最后一行分割线隐藏
            view.setVisibility(View.GONE);
        }
    }

    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            this.showAsDropDown(parent, parent.getLayoutParams().width / 2, 18);
        } else {
            this.dismiss();
        }
    }
}
