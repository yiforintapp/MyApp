package com.leo.appmaster.appmanage.view;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.view.LeoHomeGallery.IHomeGalleryListener;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class FolderView extends RelativeLayout implements OnClickListener,
		IHomeGalleryListener, OnItemSelectedListener {

	public static class ItemHolder {
		public String itmeTitle;
		public View pagerView;
	}

	private LayoutInflater mInlater;
	private View mBackView;
	private FolderTitleGallery mTitleCoverFlowView;
	private LeoHomeGallery mViewPager;

	private ArrayList<String> mItemTitles;
	private ArrayList<View> mPagerViews;
	private TitleAdapter mTitleAdapter;

	private int mCurPosition;

	public FolderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mItemTitles = new ArrayList<String>();
		mPagerViews = new ArrayList<View>();

		mTitleAdapter = new TitleAdapter();
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
		 mTitleCoverFlowView.setUnselectedAlpha(0.5f);
		mTitleCoverFlowView.setUnselectedSaturation(1.0f);
		mTitleCoverFlowView.setUnselectedScale(0.6f);
		mTitleCoverFlowView.setMaxRotation(0);
		mTitleCoverFlowView
				.setScaleDownGravity(FolderTitleGallery.SCALEDOWN_GRAVITY_BOTTOM);
		mTitleCoverFlowView
				.setActionDistance(FolderTitleGallery.ACTION_DISTANCE_AUTO);
		mTitleCoverFlowView.setOnItemSelectedListener(this);

		mViewPager = (LeoHomeGallery) findViewById(R.id.folder_pager);
		mViewPager.setHomeGalleryListener(this);
	}

	public void fillUI(List<ItemHolder> holder, int currentItem) {
		if (holder != null) {
			mItemTitles.clear();
			mPagerViews.clear();
			for (ItemHolder itemHolder : holder) {
				mItemTitles.add(itemHolder.itmeTitle);
				mPagerViews.add(itemHolder.pagerView);
			}

			mTitleAdapter.notifyDataSetChanged();

			mViewPager.removeAllViews();
			for (View view : mPagerViews) {
				mViewPager.addView(view);
			}
			mTitleCoverFlowView.setSelection(currentItem);
			mViewPager.setCurScreen(currentItem);

			mCurPosition = currentItem;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back_arrow:
			((Activity) getContext()).finish();
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
				reusableView.setLayoutParams(new FolderTitleGallery.LayoutParams(
						260, 90));
			}
			TextView textView = (TextView) reusableView;

			textView.setTextColor(Color.WHITE);
			textView.setTextSize(22);
			textView.setText(mItemTitles.get(position));
			return reusableView;
		}
	}

	@Override
	public void onGalleryScreenChanged(View aView, int aScreen) {
	}

	@Override
	public void onGalleryScreenChangeComplete(View aView, int aScreen) {
		if (mCurPosition == aScreen)
			return;
		int temp = mCurPosition;
		mCurPosition = aScreen;
		if (aScreen > temp) {
			mTitleCoverFlowView.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
		} else {
			mTitleCoverFlowView.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
		}
	}

	@Override
	public void onXChange(int aDelta) {

	}

	@Override
	public void onScrollXChanged(int aScrollX) {

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if (mCurPosition != arg2) {
			mViewPager.snapToScreen(arg2, true);
			mCurPosition = arg2;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

}
