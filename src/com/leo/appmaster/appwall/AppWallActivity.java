package com.leo.appmaster.appwall;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.AppWallBean;
import com.leo.appmaster.model.AppWallUrlBean;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class AppWallActivity extends BaseActivity implements OnItemClickListener {
	private ListView appwallLV;
	private CommonTitleBar mTtileBar;
	private Button button;
	private TextView text;
	private DisplayImageOptions options; 
	private static final String DATAPATH = "http://test.leostat.com/appmaster/appwall";//数据的url
	//private static final String IMAGEPATH = "http://c.hiphotos.baidu.com/image/w%3D310/";//图片的url
	private static final String CHARSETLOCAL = "utf-8";// 本地
	private static final String CHARSETSERVICE = "utf-8";// 服务端
	private DownloadManager downloadManager;
	private List<AppWallBean> all;
	private ProgressDialog _processBar;
	private void init() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.appwallTB);
		mTtileBar.setTitle(R.string.app_wall);
		mTtileBar.openBackView();
		mTtileBar.setOptionTextVisibility(View.INVISIBLE);
		appwallLV = (ListView) findViewById(R.id.appwallLV);
		button=(Button) findViewById(R.id.restartBT);
		text=(TextView) findViewById(R.id.textView1);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_appwall_activity);
		all=new ArrayList<AppWallBean>();
		String flag=null;
	      _processBar=new ProgressDialog(AppWallActivity.this);	
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_launcher)
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		init();
			// 访问服务器
			MyAsyncTask task = new MyAsyncTask();
			// task.execute(DATAPATH,AppwallHttpUtil.getLanguage(),getString(R.string.channel_code));
			task.execute(DATAPATH, AppwallHttpUtil.getLanguage(), "001a");
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {	
		AppWallBean app=(AppWallBean) arg0.getItemAtPosition(arg2);
		List<AppWallUrlBean> urls=app.getDownload();
		AppWallUrlBean appUrl=null;
		String[] tempStr=new String[2];
		List<String[]>  sort=new ArrayList<String[]>();
		String urlStr=null;
		Uri url=null;

		for (int i = 0; i < urls.size(); i++) {
		    appUrl=urls.get(i);
			tempStr[0]=appUrl.getId();
		    tempStr[1]=appUrl.getUrl();//首先访问
			sort.add(tempStr);
		}
		int  number=sort.size();
		if(number>=2){
			for (int i = 0; i < number; i++) {
				try {
					urlStr=sort.get(i)[1];
					requestUrl(urlStr);
					break;//访问成功直接跳出
				} catch (Exception e) {				
					continue;//访问失败，继续循环访问
				}				
			}			
		}else if(number>0&&number<=1){
			urlStr=sort.get(0)[1];
			requestUrl(urlStr);
		}else{
			LeoLog.i("","*************Not URL！");			
		}
		
		
	}
	//访问网址
	public void requestUrl(String url){
		 Uri uri = Uri.parse(url);
		 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);		
	}
	// 创建线程异步加载
	private class MyAsyncTask extends AsyncTask<String, Void, String> {
		private boolean flag;
		InputStream is = null;			
		@Override
		protected String doInBackground(String... params) {
	
			String data = null;
			  String path=params[0]; 
			  String language=params[1]; 
			  String code=params[2];
			   Map<String,String> map=new HashMap<String,String>(); 
			   map.put("language_type", language);
			  map.put("market_id",code);  	
      
					is = AppwallHttpUtil.requestByPost(path, map, CHARSETLOCAL);
					if(is!=null) {
						  data=AppwallHttpUtil.getJsonByInputStream(is, CHARSETSERVICE);			
						   }
			return data;
		}
		@Override
		protected void onPostExecute(String result) {
			boolean flag=false;
			_processBar.dismiss();
			if (result != null&&!result.equals("")) {
				List<AppWallBean> apps=getJson(result);//从服务器解析
				appwallLV.setVisibility(View.VISIBLE);			
				button.setVisibility(View.GONE);
				text.setVisibility(View.GONE);	
					all = new ArrayList<AppWallBean>();//对比后本地没有的应用				
				List<AppDetailInfo> pkgInfos=AppLoadEngine.getInstance(AppWallActivity.this).getAllPkgInfo();//获取本地安装的所有包信息
				List<String> pkgName=new ArrayList<String>();
					//获取包名
				StringBuilder sb=new StringBuilder();
				for (int i = 0; i <pkgInfos.size(); i++) {						
						pkgName.add(pkgInfos.get(i).getPkg());
				}							
			// 判断已存在包名
				for (int i = 0; i < apps.size(); i++) {
					//判断是否相同
					flag=pkgName.contains(apps.get(i).getAppPackageName());
					if(!flag){
						all.add(apps.get(i));
					}
				}			
						AppWallAdapter adapter = new AppWallAdapter(AppWallActivity.this, all);
						appwallLV.setAdapter(adapter);
				appwallLV.setOnItemClickListener(AppWallActivity.this);
			} else {
		    
				appwallLV.setVisibility(View.GONE);
				button.setVisibility(View.VISIBLE);
				text.setVisibility(View.VISIBLE);	
				button.setOnClickListener(new OnClickListener() {				
					@Override
					public void onClick(View arg0) {
						
								MyAsyncTask task = new MyAsyncTask();					 
								// task.execute(DATAPATH,AppwallHttpUtil.getLanguage(),getString(R.string.channel_code));
								task.execute(DATAPATH,AppwallHttpUtil.getLanguage(),"001a");						 
					}
				});
			}
		}
		@Override
		protected void onPreExecute() {
			 _processBar = ProgressDialog.show(AppWallActivity.this, "", "正在获取信息，请稍候！");
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
			 if(arg0 % 2 == 0) {     
				 arg1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.backup_list_item_one));
		        } else {  
		        	arg1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.backup_list_item_two));
		        }
			AppWallBean app = apps.get(arg0);
			viewHolder.image.setImageResource(R.drawable.backedup_icon);
			 viewHolder.textName.setText(app.getAppName());
			 viewHolder.textDesc.setText(app.getAppDesc());
			String imageUri=app.getImage();		
			ImageLoader.getInstance().displayImage(imageUri, viewHolder.image,options);
			return arg1;
		}
	}
	// 解析
	private List<AppWallBean> getJson(String data) {
		List<AppWallBean> all = new ArrayList<AppWallBean>();
	
		String appIcon = null;
		String appName = null;
		String appDesc = null;
		String linkUrl=null;
		String maketId=null;
		String appPackageName = null;	
		
		try {
			
			JSONObject jo = new JSONObject(data);
			JSONArray array = jo.getJSONArray("data");
			
			for (int i = 0; i < array.length(); i++) {
				List<AppWallUrlBean> urls=new ArrayList<AppWallUrlBean>();
				AppWallBean app = new AppWallBean();
				JSONObject json = (JSONObject) array.get(i);
				appIcon = json.getString("app_img_url");
				appName = json.getString("app_name");
				appDesc = json.getString("app_describe");
				JSONArray linkAddress=json.getJSONArray("linkAddress");
				for (int j = 0; j <linkAddress.length(); j++) {
						JSONObject  jsonLink=(JSONObject) linkAddress.get(j);
						AppWallUrlBean awu=new AppWallUrlBean();
						linkUrl=jsonLink.getString("link_url");
						maketId=jsonLink.getString("market_id");
						awu.setId(maketId);
						awu.setUrl(linkUrl);
						urls.add(awu); 						
				}			
				try {
					appPackageName=json.getString("app_package_name");
					app.setAppPackageName(appPackageName);
				} catch (Exception e) {
					e.printStackTrace();
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
