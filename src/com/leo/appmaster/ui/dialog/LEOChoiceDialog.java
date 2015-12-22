package com.leo.appmaster.ui.dialog;


import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;

public class LEOChoiceDialog extends LEOBaseDialog {
    private Context mContext;
    private ListView mLvChoices;
    private TextView mTvTitle;
    private boolean mNeedCheckbox = true;

	public LEOChoiceDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public LEOChoiceDialog(Context context, int theme) {
		super(context, theme);
	}

	public LEOChoiceDialog(Context context) {
		super(context ,R.style.bt_dialog);
		mContext = context;
		initUI();
	}	
	
	public void setTitleGravity(int gravity) {
	    mTvTitle.setGravity(gravity);
	}
	
	private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(R.layout.dialog_ask_times_to_catch, null);
        mTvTitle = (TextView) dlgView.findViewById(R.id.tv_ask);
        mLvChoices = (ListView) dlgView.findViewById(R.id.lv_choices);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = this.getWindow();
        window.setContentView(dlgView);
        window.setLayout(DipPixelUtil.dip2px(mContext, 280), LayoutParams.WRAP_CONTENT);
        setCanceledOnTouchOutside(true);
    }
	
	 public void setTitle(String titleStr) {
	        if (titleStr != null) {
	            mTvTitle.setText(titleStr);
	        } else {
	            mTvTitle.setText(R.string.tips);
	        }
	    }
	 
	 public void setAdapter(BaseAdapter adapter){
	     mLvChoices.setAdapter(adapter);
	 }
	 
	 public void setNeedCheckbox(boolean flag) {
	     mNeedCheckbox = flag;
	 }
	 
	 public void setItemsWithDefaultStyle(final List<String> itemList ,final int currentIndex){
	     if(itemList == null) return;
	     mLvChoices.setAdapter(new BaseAdapter() {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Holder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_ask_times_to_catch, parent,false);
                    holder = new Holder();
                    holder.name = (TextView) convertView.findViewById(R.id.tv_item);
                    holder.selecte = (CheckBox) convertView.findViewById(R.id.cb_selected);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.name.setText(itemList.get(position));
                if (!mNeedCheckbox) {
                    holder.selecte.setVisibility(View.GONE);
                    return convertView;
                }
                if(currentIndex == position){
                    holder.selecte.setChecked(true);
                }else{
                    holder.selecte.setChecked(false);
                }
                return convertView;
            }
            @Override
            public long getItemId(int arg0) {
                return 0;
            }
            @Override
            public Object getItem(int arg0) {
                return null;
            }
            @Override
            public int getCount() {
                return itemList.size();
            }
        });
	 }
	 
	 public ListView getItemsListView(){
	     return mLvChoices;
	 }
	 
	 public static class Holder {
	        TextView name;
	        CheckBox selecte;
	    }
}
