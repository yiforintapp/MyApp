package com.leo.appmaster.appmanage.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.AppListActivity;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.model.FolderItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LeoAppViewPager;
import com.leo.appmaster.utils.LeoLog;


public class FolderView extends RelativeLayout implements OnClickListener,
		OnItemSelectedListener, LeoAppViewPager.OnPageChangeListener {

	public static class ItemHolder {
		public String itmeTitle;
		public View pagerView;
	}

	private Context mContext;
	private FolderLayer mFolderLayer;

	private FolderPagerAdapter mPagerAdapter;
	private List<BaseFolderFragment> mFragmentList;
	private LayoutInflater mInlater;
	private View mBackView;
	private FolderTitleGallery mTitleCoverFlowView;
	private LeoAppViewPager mViewPager;
	private ArrayList<String> mItemTitles;
	private TitleAdapter mTitleAdapter;
	private int mCurPosition;

	private int mFolderTitleHeight, mFolderTitleWidth;
	
	private boolean mFirstShow = true;

	public FolderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mItemTitles = new ArrayList<String>();
		mFragmentList = new ArrayList<BaseFolderFragment>();
		mTitleAdapter = new TitleAdapter();

		Resources res = mContext.getResources();
		mFolderTitleHeight = res
				.getDimensionPixelSize(R.dimen.folder_title_height);
		mFolderTitleWidth = res
				.getDimensionPixelSize(R.dimen.folder_title_width);
	}
	
	@Override
	@RemotableViewMethod
	public void setVisibility(int visibility) {
	    super.setVisibility(visibility);
	    mFirstShow = true;
	}

	@Override
	protected void onFinishInflate() {
		mInlater = LayoutInflater.from(getContext());
		mInlater.inflate(R.layout.folder_page_view, this, true);
		initUI();
		super.onFinishInflate();
	}

	private void initUI() {

		mBackView = findViewById(R.id.iv_back_arrow);
		mBackView.setOnClickListener(this);
		mTitleCoverFlowView = (FolderTitleGallery) findViewById(R.id.title_cover_flow);
		mTitleCoverFlowView.setAdapter(mTitleAdapter);
		// mTitleCoverFlowView.setUnselectedAlpha(0.1f);
		mTitleCoverFlowView.setUnselectedSaturation(1.0f);
		mTitleCoverFlowView.setUnselectedScale(0.6f);
		mTitleCoverFlowView.setMaxRotation(0);
		mTitleCoverFlowView.setScaleDownGravity(0.75f);
		mTitleCoverFlowView
				.setActionDistance(FolderTitleGallery.ACTION_DISTANCE_AUTO);
		mTitleCoverFlowView.setOnItemSelectedListener(this);

		fillTitle();

		// add four fragment
		BaseFolderFragment businessFragment = new BusinessAppFragment();
		businessFragment.setType(BaseFolderFragment.FOLER_TYPE_RECOMMEND);
		BaseFolderFragment backupFragment = new CommonFolderFragment();
		backupFragment.setType(BaseFolderFragment.FOLER_TYPE_BACKUP);
		BaseFolderFragment flowFragment = new CommonFolderFragment();
		flowFragment.setType(BaseFolderFragment.FOLER_TYPE_FLOW);
		BaseFolderFragment capacityFragment = new CommonFolderFragment();
		capacityFragment.setType(BaseFolderFragment.FOLER_TYPE_CAPACITY);

		FragmentManager fm = ((FragmentActivity) mContext)
				.getSupportFragmentManager();
		// AM-614, remove cached fragments
		try {
			FragmentTransaction ft = fm.beginTransaction();
			List<Fragment> list = fm.getFragments();
			if (list != null) {
				for (Fragment f : fm.getFragments()) {
					ft.remove(f);
				}
			}
			ft.commit();
		} catch (Exception e) {

		}

		mFragmentList.add(businessFragment);
		mFragmentList.add(flowFragment);
		mFragmentList.add(capacityFragment);
		mFragmentList.add(backupFragment);
		
		mPagerAdapter = new FolderPagerAdapter(fm);

		mViewPager = (LeoAppViewPager) findViewById(R.id.folder_pager);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(1);
		mViewPager.setOnPageChangeListener(this);

		mViewPager.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mFolderLayer.closeFloder();
			}
		});
	}

	public void fillTitle() {
		List<ItemHolder> holder = new ArrayList<FolderView.ItemHolder>();
		ItemHolder item;
		ImageView iv;

		item = new ItemHolder();
		item.itmeTitle = mContext.getString(R.string.folder_recommend);
		iv = new ImageView(mContext);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		item = new ItemHolder();
		item.itmeTitle = mContext.getString(R.string.folder_sort_flow);
		iv = new ImageView(mContext);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		item = new ItemHolder();
		item.itmeTitle = mContext.getString(R.string.folder_sort_capacity);
		iv = new ImageView(mContext);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);
		
		item = new ItemHolder();
		item.itmeTitle = mContext.getString(R.string.folder_backup_restore);
		iv = new ImageView(mContext);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);
		
		if (holder != null) {
			mItemTitles.clear();
			for (ItemHolder itemHolder : holder) {
				mItemTitles.add(itemHolder.itmeTitle);
			}
			mTitleAdapter.notifyDataSetChanged();
		}
	}

	public void updateData(int folderFlowSort, List<AppItemInfo> contentData,
			List<BusinessItemInfo> reccommendData) {
		if (folderFlowSort < mFragmentList.size()) {
			((CommonFolderFragment) mFragmentList.get(folderFlowSort))
					.updateData(contentData, reccommendData);
		}
	}

	public void setCurrentPosition(int currentItem) {
		mCurPosition = currentItem;
		mTitleCoverFlowView.setSelection(currentItem);
		mViewPager.setCurrentItem(currentItem, false);
	}

	public void setFolderLayer(FolderLayer layer) {
		mFolderLayer = layer;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back_arrow:
			// ((Activity) getContext()).finish();
			mFolderLayer.closeFloder();
			break;

		default:
			break;
		}
	}

	private class TitleAdapter extends FolderTitleGalleryAdapter {
		@Override
		public int getCount() {
			return mItemTitles.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mItemTitles.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getCoverFlowItem(int position, View reusableView,
				ViewGroup parent) {
			if (reusableView == null) {
				reusableView = new TextView(getContext());
				reusableView
						.setLayoutParams(new FolderTitleGallery.LayoutParams(
								mFolderTitleWidth, mFolderTitleHeight));
			}
			TextView textView = (TextView) reusableView;

			textView.setSingleLine(true);
			textView.setTextColor(Color.WHITE);
			textView.setTextSize(22);
			textView.setText(mItemTitles.get(position));
			return reusableView;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
	    mViewPager.interceptVerticalEvent(true);
		if (arg0 == FolderItemInfo.FOLDER_BACKUP_RESTORE) {
		    if(!mFirstShow) {
		          SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ub_restore", "glide");
		    }
		} else if (arg0 == FolderItemInfo.FOLDER_FLOW_SORT) {
            if (!mFirstShow) {
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ub_liuliang", "glide");
            }
            if (AppBusinessManager.getInstance(mContext).hasBusinessData(BusinessItemInfo.CONTAIN_FLOW_SORT)) {
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "app_rec", "flow");
            }
		} else if (arg0 == FolderItemInfo.FOLDER_CAPACITY_SORT) {
            if (!mFirstShow) {
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ub_space", "glide");
            }
            if (AppBusinessManager.getInstance(mContext).hasBusinessData(BusinessItemInfo.CONTAIN_CAPACITY_SORT)) {
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "app_rec", "capacity");
            }
		} else if (arg0 == FolderItemInfo.FOLDER_BUSINESS_APP) {
            if (!mFirstShow) {
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ub_newapp", "glide");
            }
			mViewPager.interceptVerticalEvent(false);
		}

		
		if (arg0 == 0) {
			AppMasterPreference pref = AppMasterPreference
					.getInstance(mContext);
			String online = pref.getOnlineBusinessSerialNumber();
			String local = pref.getLocalBusinessSerialNumber();
			if (online != null && !online.equals(local)) {
				pref.setLocalBusinessSerialNumber(online);
				pref.setHomeBusinessTipClick(true);
				((AppListActivity) mContext).mHandler.postDelayed(
						new Runnable() {
							@Override
							public void run() {
								((AppListActivity) mContext).fillAppListData();
							}
						}, 2000);
			}

			((AppListActivity) mContext).mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					BusinessAppFragment ba = (BusinessAppFragment) mFragmentList
							.get(0);
					ba.loadInitBusinessData();
				}
			}, 1200);

		}

		if (mCurPosition == arg0)
			return;
		int temp = mCurPosition;
		mCurPosition = arg0;
		if (arg0 > temp) {
			mTitleCoverFlowView.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
		} else {
			mTitleCoverFlowView.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
		}
		
		mFirstShow = false;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if (mCurPosition != arg2) {
			mCurPosition = arg2;
			mViewPager.setCurrentItem(arg2, true);
		}
	}

	class FolderPagerAdapter extends FragmentPagerAdapter {

		public FolderPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragmentList.get(position);
		}

		@Override
		public int getCount() {
			return mFragmentList.size();
		}
	}

}
