package com.leo.appmaster.appmanage.view;

import android.widget.AdapterView.OnItemClickListener;

import com.leo.appmaster.fragment.BaseFragment;

public abstract class BaseFolderFragment extends BaseFragment {
	public static int FOLER_TYPE_FLOW = 1;
	public static int FOLER_TYPE_CAPACITY = 2;
	public static int FOLER_TYPE_BACKUP = 3;
	public static int FOLER_TYPE_RECOMMEND = 4;
	protected int mType;

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}

}
