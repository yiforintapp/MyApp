package com.leo.appmaster.ui;

import java.util.List;
import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leoers.leoanalytics.LeoStat;

public class LeoPopMenu {

	private PopupWindow mLeoPopMenu;

	private List<String> mItems;

	private OnItemClickListener mPopItemClickListener;

	private MenuListAdapter mAdapter;
	
	private boolean mIsItemHTMLFormatted = false;

	private int mAnimaStyle;

	/**
	 * @param aContext
	 * @param anchorView
	 *            显示tab列表
	 */
	public void showPopMenu(Activity activity, View anchorView) {
		if (mLeoPopMenu != null) {
			if (mLeoPopMenu.isShowing()) {
				return;
			}
			mLeoPopMenu = null;
		}

		View convertView = buildTabListLayout();

		mLeoPopMenu = new PopupWindow(convertView, 360, 
		        LayoutParams.WRAP_CONTENT, true);
		mLeoPopMenu.setFocusable(true);
		mLeoPopMenu.setOutsideTouchable(true);
		mLeoPopMenu.setBackgroundDrawable(AppMasterApplication.getInstance()
				.getResources().getDrawable(R.drawable.popup_menu_bg));
		mLeoPopMenu.setAnimationStyle(mAnimaStyle);
		mLeoPopMenu.update();
		mLeoPopMenu.showAsDropDown(anchorView, 0, 0);
	}

	public void setAnimation(int animaStyle) {
		mAnimaStyle = animaStyle;
	}
	
	/**
	 * call this to set mIsSpanedItem true when your item is HTML style format string
	 * */
	public void setItemSpaned(boolean flag){
	    mIsItemHTMLFormatted = flag;
	}
	
	public void setOnDismiss(OnDismissListener l){
        if (mLeoPopMenu != null) {
            mLeoPopMenu.setOnDismissListener(l);
        }
	}

	public void dismissSnapshotList() {
		if (mLeoPopMenu != null) {
			mLeoPopMenu.dismiss();
			mLeoPopMenu = null;
		}
	}

	private View buildTabListLayout() {
		LayoutInflater inflater = LayoutInflater.from(AppMasterApplication
				.getInstance());
		LinearLayout convertView = (LinearLayout) inflater.inflate(
				R.layout.popmenu_window_list_layout, null);
		ListView listView = (ListView) convertView.findViewById(R.id.menu_list);

		listView.setOnItemClickListener(mPopItemClickListener);

		mAdapter = new MenuListAdapter(mItems);
		listView.setAdapter(mAdapter);
		return convertView;
	}

	public void setPopMenuItems(List<String> items) {
		mItems = items;
	}

	public void setPopItemClickListener(OnItemClickListener listener) {
		mPopItemClickListener = listener;
	}

	static class Holder {
		public TextView mItemName;
	}

	private class MenuListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<String> mListItems;
		private Holder mHolder;

		private MenuListAdapter(List<String> itemList) {
			mListItems = itemList;
			inflater = LayoutInflater.from(AppMasterApplication.getInstance());
		}

		@Override
		public int getCount() {
			if (mListItems != null) {
				return mListItems.size();
			} else {
				return 0;
			}

		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView != null) {
				mHolder = (Holder) convertView.getTag();
			} else {
				mHolder = new Holder();
				convertView = inflater.inflate(
						R.layout.popmenu_window_list_item, null);
				mHolder.mItemName = (TextView) convertView
						.findViewById(R.id.menu_text);
				convertView.setTag(mHolder);
			}
			
            if (mIsItemHTMLFormatted) {
                Spanned itemText = Html.fromHtml(mListItems.get(position));
                mHolder.mItemName.setText(itemText);
            } else {
                mHolder.mItemName.setText(mListItems.get(position));
            }

			return convertView;
		}
	}
}
