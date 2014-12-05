package com.leo.appmaster.appmanage.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class FolderTitleGalleryAdapter extends BaseAdapter {

	@Override
	public final View getView(int i, View reusableView, ViewGroup viewGroup) {
		FolderTitleGallery coverFlow = (FolderTitleGallery) viewGroup;

		View wrappedView = null;
		FolderTitleGalleryItemWrapper coverFlowItem;

		if (reusableView != null) {
			coverFlowItem = (FolderTitleGalleryItemWrapper) reusableView;
			wrappedView = coverFlowItem.getChildAt(0);
			coverFlowItem.removeAllViews();
		} else {
			coverFlowItem = new FolderTitleGalleryItemWrapper(
					viewGroup.getContext());
		}

		wrappedView = this.getCoverFlowItem(i, wrappedView, viewGroup);

		if (wrappedView == null) {
			throw new NullPointerException(
					"getCoverFlowItem() was expected to return a view, but null was returned.");
		}

		final boolean isReflectionEnabled = coverFlow.isReflectionEnabled();
		coverFlowItem.setReflectionEnabled(isReflectionEnabled);

		if (isReflectionEnabled) {
			coverFlowItem.setReflectionGap(coverFlow.getReflectionGap());
			coverFlowItem.setReflectionRatio(coverFlow.getReflectionRatio());
		}

		coverFlowItem.addView(wrappedView);
		coverFlowItem.setLayoutParams(wrappedView.getLayoutParams());

		return coverFlowItem;
	}

	public abstract View getCoverFlowItem(int position, View reusableView,
			ViewGroup parent);
}
