package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.model.WordNewAdviceItemInfo;
import com.zlf.appmaster.model.WordNewAdviceInfo;
import com.zlf.appmaster.utils.TimeUtil;
import com.zlf.imageloader.DisplayImageOptions;
import com.zlf.imageloader.ImageLoader;
import com.zlf.imageloader.core.FadeInBitmapDisplayer;
import com.zlf.imageloader.core.ImageScaleType;

import java.util.List;

public class PinnedHeaderExpandableAdapter extends  BaseExpandableListAdapter {
	private List<List<WordNewAdviceItemInfo>> mChildrenData;
	private List<WordNewAdviceInfo> mGroupData;
	private Context mContext;
	private LayoutInflater mInflater;
	private DisplayImageOptions commonOption;
	private BitmapFactory.Options options;
	private int mType;
	
	public PinnedHeaderExpandableAdapter(List<List<WordNewAdviceItemInfo>> childrenData
			,List<WordNewAdviceInfo> groupData, Context context, int type){
		this.mGroupData = groupData;
		this.mChildrenData = childrenData;
		this.mContext = context;
		this.mType = type;
		mInflater = LayoutInflater.from(this.mContext);
		options = new BitmapFactory.Options();
		// 主题使用565配置
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		if (commonOption == null) {
			commonOption = new DisplayImageOptions.Builder()
					.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
					.showImageOnLoading(R.drawable.online_theme_loading)
					.showImageOnFail(R.drawable.online_theme_loading_failed)
					.displayer(new FadeInBitmapDisplayer(500))
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.considerExifParams(true)
					.decodingOptions(options)
					.build();
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mChildrenData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
//		View view = null;
//        if (convertView != null) {
//            view = convertView;
//        } else {
//            view = createChildrenView();
//        }
		ChildHolder childHolder;
		if (convertView == null) {
			childHolder = new ChildHolder();
			convertView = mInflater.inflate(R.layout.child_item, null);
			childHolder.mDeal = (TextView) convertView.findViewById(R.id.buy_sale);
			childHolder.mRealName = (TextView) convertView.findViewById(R.id.name);
			childHolder.mTime = (TextView) convertView.findViewById(R.id.time);
			childHolder.mEnterPoint = (TextView) convertView.findViewById(R.id.ruchang);
			childHolder.mProfit = (TextView) convertView.findViewById(R.id.zhiying);
			childHolder.mLose = (TextView) convertView.findViewById(R.id.zhisun);
			childHolder.mRemark = (TextView) convertView.findViewById(R.id.style);
			convertView.setTag(childHolder);
		} else {
			childHolder = (ChildHolder) convertView.getTag();
		}

		WordNewAdviceItemInfo info = mChildrenData.get(groupPosition).get(childPosition);
		if ("sell".equals(info.getDeal())) {
			childHolder.mDeal.setText(mContext.getResources().getString(R.string.stock_buy));
			childHolder.mDeal.setTextColor(mContext.getResources().getColor(R.color.stock_rise));
		} else if ("buy".equals(info.getDeal())) {
			childHolder.mDeal.setText(mContext.getResources().getString(R.string.stock_sell));
			childHolder.mDeal.setTextColor(mContext.getResources().getColor(R.color.stock_slumped));
		}
		childHolder.mRealName.setText(info.getRealName());
		if (mType == 0) {
			childHolder.mRealName.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.child_item_bg_one));
		} else if (mType == 1) {
			childHolder.mRealName.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.child_item_bg_two));
		}
		childHolder.mTime.setText(TimeUtil.getSimpleTime(Long.parseLong(info.getTime())));
		childHolder.mEnterPoint.setText(info.getEnterPoint());
		childHolder.mProfit.setText(info.getProfit());
		childHolder.mLose.setText(info.getLose());
		childHolder.mRemark.setText(info.getRemark());

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mChildrenData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroupData.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroupData.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupHolder groupHolder;
		if (convertView == null) {
			groupHolder = new GroupHolder();
			convertView = mInflater.inflate(R.layout.group, null);
			groupHolder.mName = (TextView) convertView.findViewById(R.id.admin);
			groupHolder.mIv = (ImageView) convertView.findViewById(R.id.admin_iv);
			groupHolder.mContent = (TextView) convertView.findViewById(R.id.content);
			groupHolder.mCount = (TextView) convertView.findViewById(R.id.groupto);
			convertView.setTag(groupHolder);
		} else {
			groupHolder = (GroupHolder) convertView.getTag();
		}
		WordNewAdviceInfo info = mGroupData.get(groupPosition);
		groupHolder.mName.setText(info.getName());
		if (!TextUtils.isEmpty(info.getIcon())) {
			ImageLoader.getInstance().displayImage(Constants.ZHIBO_ADMIN_IMG_DOMAIN.concat(info.getIcon()),
					groupHolder.mIv, commonOption);
		}
		groupHolder.mContent.setText(info.getDesc());
		groupHolder.mCount.setText(String.valueOf(mChildrenData.get(groupPosition).size()));
		if (mType == 0) {
			groupHolder.mCount.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.circle_shape_one));
		} else if (mType == 1) {
			groupHolder.mCount.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.circle_shape_two));
		}
		return convertView;
	}

	class GroupHolder {
		public TextView mName;
		public ImageView mIv;
		public TextView mContent;
		public TextView mCount;
	}

	class ChildHolder {
		public TextView mDeal;
		public TextView mRealName;
		public TextView mTime;
		public TextView mEnterPoint;
		public TextView mProfit;
		public TextView mLose;
		public TextView mRemark;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}


}
