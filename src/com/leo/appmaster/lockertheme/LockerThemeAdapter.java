package com.leo.appmaster.lockertheme;

import java.util.List;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.model.ThemeInfo;
import com.leo.appmaster.model.AppWallBean;
import com.leo.appmaster.utils.LeoLog;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LockerThemeAdapter extends BaseAdapter {
	private List<ThemeInfo> themes;
	private LayoutInflater layoutInflater;

	public LockerThemeAdapter(Context context, List<ThemeInfo> themes) {
		this.themes = themes;
		this.layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return themes != null ? themes.size() : 0;
	}

	@Override
	public Object getItem(int arg0) {

		return themes != null ? themes.get(arg0) : null;
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	class ViewHolder {
		ImageView image;
		ImageView selectedView;
		TextView themeName, tag;

	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ViewHolder viewHolder = null;

		if (arg1 == null) {
			viewHolder = new ViewHolder();
			arg1 = layoutInflater.inflate(R.layout.list_item_lockerthem, null);
			viewHolder.themeName = (TextView) arg1
					.findViewById(R.id.lockerThemName);
			viewHolder.image = (ImageView) arg1
					.findViewById(R.id.theme_preview);
			viewHolder.tag = (TextView) arg1.findViewById(R.id.flagTV);
			viewHolder.selectedView = (ImageView) arg1
					.findViewById(R.id.visibilityIV);
			arg1.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) arg1.getTag();
		}
		ThemeInfo theme = themes.get(arg0);

		if (theme.themeName == null || theme.themeName.equals("")) {
			viewHolder.themeName.setText("");
		} else {
			viewHolder.themeName.setText(theme.themeName);
		}
		if (theme.themeType == Constants.THEME_TYPE_ONLINE) {
			if (theme.tag == Constants.THEME_TAG_NEW) {
				viewHolder.tag
						.setBackgroundResource(R.drawable.theme_tag_new);
				viewHolder.tag.setText(R.string.theme_tag_new);
			} else if (theme.tag == Constants.THEME_TAG_HOT) {
				viewHolder.tag
						.setBackgroundResource(R.drawable.theme_tag_hot);
				viewHolder.tag.setText(R.string.theme_tag_hot);
			} else {
				viewHolder.tag.setVisibility(View.INVISIBLE);
			}

			ImageLoader.getInstance().displayImage(theme.previewUrl,
					viewHolder.image);

		} else {
			viewHolder.image.setImageDrawable(theme.themeImage);
			viewHolder.tag.setVisibility(View.INVISIBLE);
		}

		if (theme.curUsedTheme) {
			viewHolder.selectedView.setVisibility(View.VISIBLE);
		} else {
			viewHolder.selectedView.setVisibility(View.GONE);
		}

		return arg1;
	}
}