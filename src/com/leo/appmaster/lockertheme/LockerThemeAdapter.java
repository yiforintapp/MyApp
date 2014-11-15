package com.leo.appmaster.lockertheme;

import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppLockerThemeBean;
import com.leo.appmaster.model.AppWallBean;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LockerThemeAdapter extends BaseAdapter {
	private Context context;
	private List<AppLockerThemeBean> themes;
	private LayoutInflater layoutInflater;

	public LockerThemeAdapter(Context context, List<AppLockerThemeBean> themes) {
		this.context = context;
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
		View image;
		TextView themeName,flagName;
		
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (arg1 == null) {
			viewHolder = new ViewHolder();
			arg1 = layoutInflater.inflate(R.layout.list_item_lockerthem, null);
			viewHolder.themeName = (TextView) arg1.findViewById(R.id.themName);
			viewHolder.image=(View) arg1.findViewById(R.id.themLT);
			viewHolder.flagName=(TextView) arg1.findViewById(R.id.flagTV);
			arg1.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) arg1.getTag();
		}
		if (arg0 % 2 == 0) {
			arg1.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.backup_list_item_one));
		} else {
			arg1.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.backup_list_item_two));
		}
		AppLockerThemeBean theme= themes.get(arg0);
		viewHolder.themeName.setText(theme.getThemeName());
		viewHolder.image.setBackgroundResource(theme.getThemeImage());
		viewHolder.flagName.setText(theme.getFlagName());
		//viewHolder.image.setImageResource(R.drawable.backedup_icon);
		/*ImageLoader.getInstance().displayImage(imageUri, viewHolder.image,
				options);*/
		return arg1;
	}
}