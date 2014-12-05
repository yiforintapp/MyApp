package com.leo.appmaster.appmanage;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.view.FolderShowView;
import com.leo.appmaster.appmanage.view.FolderShowView.ItemHolder;
import com.leo.appmaster.appmanage.view.LeoHomeGallery;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.FolderItemInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.Gallery;
import android.widget.ImageView;

public class FolderActivity extends Activity {

	FolderShowView mFolderView;

	private int mFromType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder);
		handleIntent();
		initUI();
		loadData();

	}

	private void loadData() {
		List<ItemHolder> holder = new ArrayList<FolderShowView.ItemHolder>();
		ItemHolder item;
		ImageView iv;

		item = new ItemHolder();
		item.itmeTitle = getString(R.string.folder_system_preset);
		iv = new ImageView(this);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		item = new ItemHolder();
		item.itmeTitle = getString(R.string.folder_running);
		iv = new ImageView(this);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		item = new ItemHolder();
		item.itmeTitle = getString(R.string.folder_restore);
		iv = new ImageView(this);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		item = new ItemHolder();
		item.itmeTitle = getString(R.string.folder_recommend);
		iv = new ImageView(this);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		mFolderView.fillUI(holder, mFromType);
	}

	private void handleIntent() {
		Intent intent = this.getIntent();
		mFromType = intent.getIntExtra("from_type",
				FolderItemInfo.FOLDER_SYSTEM_APP);
	}

	private void initUI() {
		mFolderView = (FolderShowView) findViewById(R.id.folder_view);
	}

}
