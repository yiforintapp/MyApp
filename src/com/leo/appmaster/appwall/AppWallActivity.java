package com.leo.appmaster.appwall;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.AppWallBean;
import com.leo.appmaster.model.AppWallUrlBean;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.RoundedBitmapDisplayer;

public class AppWallActivity extends BaseActivity implements
		OnItemClickListener {
	private boolean flagGp = false;
	private ListView appwallLV;
	private CommonTitleBar mTtileBar;
	private Button button;
	private TextView text;
	private DisplayImageOptions options;
	private static final String DATAPATH = "http://api.leostat.com/appmaster/appwall";// 数据的url
	public static final String GPPACKAGE = "com.android.vending";// GP包名
	private static final String CHARSETLOCAL = "utf-8";// 本地
	private static final String CHARSETSERVICE = "utf-8";// 服务端
	private AppWallDialog p;
	private List<AppWallBean> all;// 去重后的应用
	private List<AppWallBean> temp;

	private void init() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.appwallTB);
		mTtileBar.setTitle(R.string.app_wall);
		mTtileBar.openBackView();
		mTtileBar.setOptionTextVisibility(View.INVISIBLE);
		appwallLV = (ListView) findViewById(R.id.appwallLV);
		button = (Button) findViewById(R.id.restartBT);
		text = (TextView) findViewById(R.id.textView1);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_appwall_activity);
		all = new ArrayList<AppWallBean>();
		p = new AppWallDialog(this);
		Window window=p.getWindow();
	    WindowManager.LayoutParams lp = window.getAttributes();   
        lp.alpha = 0.5f;// 透明度   
        lp.dimAmount = 0.0f;// 黑暗度   
        window.setAttributes(lp);   
		String flag = null;
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.photo_bg_loding)
				.showImageForEmptyUri(R.drawable.photo_bg_loding)
				.showImageOnFail(R.drawable.photo_bg_loding).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		init();
		// 访问服务器
		MyAsyncTask task = new MyAsyncTask();
		 task.execute(DATAPATH,AppwallHttpUtil.getLanguage(),getString(R.string.channel_code));
		//task.execute(DATAPATH, AppwallHttpUtil.getLanguage(), "002a");
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		AppWallBean app = (AppWallBean) arg0.getItemAtPosition(arg2);
		List<AppWallUrlBean> urls = app.getDownload();
		AppWallUrlBean appUrl = null;
		
		List<String[]> sort = new ArrayList<String[]>();
		String urlStr = null;
		Uri url = null;
		for (int i = 0; i < urls.size(); i++) {
			appUrl = urls.get(i);
			String[] tempStr = new String[2];
			tempStr[0] = appUrl.getId();
			tempStr[1] = appUrl.getUrl();// 首先访问
			sort.add(tempStr);
		}
		int number = sort.size();
		if (number >= 2) {
			for (int i = 0; i < number; i++) {
				try {
					urlStr = sort.get(i)[1];
					if (i == 0) {
						if (flagGp) {
							LeoLog.i("run","********************"+urlStr);
							requestGp(AppWallActivity.this, urlStr);
							break;// 访问成功直接跳出
						} else {
							continue;// 访问失败，继续循环访问
						}
					} else {
						requestUrl(urlStr);
					}
				} catch (Exception e) {
					// continue;//访问失败，继续循环访问
				}
			}
		} else if (number > 0 && number <= 1) {
			urlStr = sort.get(0)[1];

			try {
				requestUrl(urlStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LeoLog.i("", "*************Not URL！");
		}
	}

	// 访问网址
	public void requestUrl(String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}

	// 访问GooglPlay
	public void requestGp(Context context, String packageGp) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=" + packageGp));
		intent.setPackage(GPPACKAGE);
		context.startActivity(intent);
	}

	// 创建线程异步加载
	private class MyAsyncTask extends AsyncTask<String, Void, String> {
		private boolean flag;
		InputStream is = null;

		@Override
		protected String doInBackground(String... params) {

			String data = null;
			String path = params[0];
			String language = params[1];
			String code = params[2];
			Map<String, String> map = new HashMap<String, String>();
			map.put("language_type", language);
			map.put("market_id", code);
			is = AppwallHttpUtil.requestByPost(path, map, CHARSETLOCAL);
			if (is != null) {
				data = AppwallHttpUtil.getJsonByInputStream(is, CHARSETSERVICE);
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			boolean flag = false;
			p.dismiss();
			if (result != null && !result.equals("")) {
				List<AppWallBean> apps = getJson(result);// 从服务器解析
				appwallLV.setVisibility(View.VISIBLE);
				button.setVisibility(View.GONE);
				text.setVisibility(View.GONE);
				all = new ArrayList<AppWallBean>();// 对比后本地没有的应用
				temp = new ArrayList<AppWallBean>();
				List<AppDetailInfo> pkgInfos = AppLoadEngine.getInstance(
						AppWallActivity.this).getAllPkgInfo();// 获取本地安装的所有包信息
				List<String> pkgName = new ArrayList<String>();
				// 获取包名
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < pkgInfos.size(); i++) {
					if (pkgInfos.get(i).packageName.equals("com.android.vending")) {
						flagGp = true;
					}
					pkgName.add(pkgInfos.get(i).packageName);

				}

				// 判断已存在包名
				for (int i = 0; i < apps.size(); i++) {
					// 判断是否相同
					flag = pkgName.contains(apps.get(i).getAppPackageName());
					if (!flag) {
						all.add(apps.get(i));
					}
				}
				// 获取all中前十条记录
				for (int i = 0; i < all.size(); i++) {
					if (i < 10) {
						temp.add(all.get(i));
					} else {
						break;
					}
				}
				AppWallAdapter adapter = new AppWallAdapter(
						AppWallActivity.this, temp);
				appwallLV.setAdapter(adapter);
				appwallLV.setOnItemClickListener(AppWallActivity.this);
			} else {
				appwallLV.setVisibility(View.GONE);
				button.setVisibility(View.VISIBLE);
				text.setVisibility(View.VISIBLE);
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						button.setVisibility(View.GONE);
						text.setVisibility(View.GONE);
						MyAsyncTask task = new MyAsyncTask();
						 task.execute(DATAPATH,AppwallHttpUtil.getLanguage(),getString(R.string.channel_code));
						/*task.execute(DATAPATH, AppwallHttpUtil.getLanguage(),
								"001a");*/
					}
				});
			}
		}

		@Override
		protected void onPreExecute() {
			p.show();
			super.onPreExecute();
		}
	}

	// 适配器
	class AppWallAdapter extends BaseAdapter {
		private Context context;
		private List<AppWallBean> apps;
		private LayoutInflater layoutInflater;

		public AppWallAdapter(Context context, List<AppWallBean> apps) {
			this.context = context;
			this.apps = apps;
			this.layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return apps != null ? apps.size() : 0;
		}

		@Override
		public Object getItem(int arg0) {

			return apps != null ? apps.get(arg0) : null;
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		class ViewHolder {
			ImageView image;
			TextView textName, textDesc, textUrl;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ViewHolder viewHolder = null;
			if (arg1 == null) {
				viewHolder = new ViewHolder();
				arg1 = layoutInflater.inflate(R.layout.item_appwall, null);
				viewHolder.image = (ImageView) arg1
						.findViewById(R.id.appwallIV);
				viewHolder.textName = (TextView) arg1
						.findViewById(R.id.appwallNameTV);
				viewHolder.textDesc = (TextView) arg1
						.findViewById(R.id.appwallDescTV);
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
			AppWallBean app = apps.get(arg0);
			viewHolder.image.setImageResource(R.drawable.backedup_icon);
			viewHolder.textName.setText(app.getAppName());
			viewHolder.textDesc.setText(app.getAppDesc());
			String imageUri = app.getImage();
			ImageLoader.getInstance().displayImage(imageUri, viewHolder.image,
					options);
			return arg1;
		}
	}

	// 解析
	private List<AppWallBean> getJson(String data) {
		List<AppWallBean> all = new ArrayList<AppWallBean>();

		String appIcon = null;
		String appName = null;
		String appDesc = null;
		String linkUrl = null;
		String maketId = null;
		String appPackageName = null;

		try {

			JSONObject jo = new JSONObject(data);
			JSONArray array = jo.getJSONArray("data");

			for (int i = 0; i < array.length(); i++) {
				List<AppWallUrlBean> urls = new ArrayList<AppWallUrlBean>();
				AppWallBean app = new AppWallBean();
				JSONObject json = (JSONObject) array.get(i);
				appIcon = json.getString("app_img_url");
				appName = json.getString("app_name");
				appDesc = json.getString("app_describe");
				JSONArray linkAddress = json.getJSONArray("linkAddress");
				for (int j = 0; j < linkAddress.length(); j++) {
					JSONObject jsonLink = (JSONObject) linkAddress.get(j);
					AppWallUrlBean awu = new AppWallUrlBean();
					linkUrl = jsonLink.getString("link_url");
					maketId = jsonLink.getString("market_id");
					awu.setId(maketId);
					awu.setUrl(linkUrl);
					urls.add(awu);
				}
				try {
					appPackageName = json.getString("app_package_name");
					app.setAppPackageName(appPackageName);
				} catch (Exception e) {
					e.printStackTrace();
					// 如果失败就给包名赋空字符串，保证程序正常运行
					appPackageName = "";
					app.setAppPackageName(appPackageName);
				}
				app.setImage(appIcon);
				app.setAppName(appName);
				app.setAppDesc(appDesc);
				app.setDownload(urls);
				all.add(app);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return all;
	}

}
