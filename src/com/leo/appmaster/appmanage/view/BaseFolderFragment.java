package com.leo.appmaster.appmanage.view;

import android.widget.AdapterView.OnItemClickListener;

import com.leo.appmaster.fragment.BaseFragment;

public abstract class BaseFolderFragment extends BaseFragment {
	public static int FOLER_TYPE_FLOW = 0;
	public static int FOLER_TYPE_CAPACITY = 1;
	public static int FOLER_TYPE_BACKUP = 2;
	public static int FOLER_TYPE_RECOMMEND = 3;
	protected int mType;
	protected OnItemClickListener mFolderItemClickListener;

	abstract void setItemClickListener(OnItemClickListener itemClickListener);

}
