package com.leo.appmaster.lockertheme;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.appwall.AppWallActivity;
import com.leo.appmaster.constants.Constants;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.AppLockerThemeBean;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class LockerTheme extends Activity{
	private ListView listTheme;
/*	private List<AppLockerThemeBean> onlintThemes;*/
	private List<AppLockerThemeBean> mThemes;
	private boolean flagGp=false;//判断是否存在GP
	private LEOAlarmDialog mAlarmDialog;
	private AppLockerThemeBean itemTheme;
	private SharedPreferences sharedPreferences; 
	private int number=0;
	private void initUI(){
		CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		title.setTitle(R.string.lockerTheme);
		title.openBackView();
		listTheme=(ListView) findViewById(R.id.themeLV);
		
		sharedPreferences = getSharedPreferences("lockerTheme", Context.MODE_WORLD_WRITEABLE); //私有数据
	}
	@Override
	protected void onCreate(Bundle arg0) {	
		super.onCreate(arg0);
		boolean flagPackge=false;
		setContentView(R.layout.activity_locker_theme);
		initUI();
		mThemes=new ArrayList<AppLockerThemeBean>();
		//定向主题
		Intent intent=this.getIntent();
		String temp=intent.getStringExtra("theme_package");
		if(temp!=null&&temp.equals("")){
			for (int i = 0; i < mThemes.size(); i++) {
				if(mThemes.get(i).getPackageName().equals(temp)){
				number=i;
				}
			}
		}else{

		}
		
/*
* ------------------------------------------构造数据----------------------------------------------------------
*/
		AppLockerThemeBean applockerTheme=new AppLockerThemeBean();
		applockerTheme.setThemeImage(R.drawable.select_all_press);
		applockerTheme.setThemeName("主题");
		String[] url=new String[2];
		url[1]="http://down2.iinmobi.com/group1/M01/09/73/ooYBAFRh45eAGQVaACCBUDfiyfw938.apk";
		url[0]="com.mah.calldetailscreen";
		applockerTheme.setUrl(url);
		//applockerTheme.setPackageName("com.mah.calldetailscreen");
		applockerTheme.setFlagName("在线");
		/*//------------------默认
		AppLockerThemeBean applockerTheme1=new AppLockerThemeBean();
		applockerTheme1.setThemeImage(R.drawable.select_all_press);
		applockerTheme1.setThemeName("默认主题");
		String[] url1=new String[2];
		url1[1]="http://down2.iinmobi.com/group1/M01/09/73/ooYBAFRh45eAGQVaACCBUDfiyfw938.apk";
		url1[0]="com.mah.calldetailscreen";
		applockerTheme1.setUrl(url1);		
		applockerTheme1.setPackageName("com.mah.calldetailscreen");
		applockerTheme1.setFlagName("默认");
		mThemes.add(applockerTheme1);	
		*/
		for (int i = 0; i <10; i++) {		
			applockerTheme.setPackageName("com.leo.theme"+i);
			mThemes.add(applockerTheme);
		}
/*
* ----------------------------------------------------------------------------------------------------------------------
*/
		listTheme.setAdapter(new LockerThemeAdapter(this,mThemes));
		/*
		 * OnlineTheme
		 */
		listTheme.setSelection(number);//定向跳转
		listTheme.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
			
				Toast.makeText(LockerTheme.this, "在线主题"+(arg2+1),Toast.LENGTH_SHORT).show();
			/*itemTheme=(AppLockerThemeBean) arg0.getItemAtPosition(arg2);
				String[] urls=itemTheme.getUrl();
				showAlarmDialog(null,null);
				if(mThemes.get(arg2).getFlagName().equals((String)getResources().getText(R.string.onlinetheme))){
					for (int i = 0; i < urls.length; i++) {
						boolean flag = AppwallHttpUtil.isHttpUrl(urls[i]);
						if (flagGp) {
							if (!flag) {
								AppwallHttpUtil.requestGp(LockerTheme.this,urls[i]);
								break;
							}
						} else {
							AppwallHttpUtil.requestUrl(LockerTheme.this, urls[i]);
							break;
						}
					}						
				}else if(mThemes.get(arg2).getFlagName().equals((String)getResources().getText(R.string.localtheme))){						
					
					 * 卸载主题
					 * 
					 * Uri uninstallUri = Uri.fromParts("package", "xxx", null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uninstallUri); 
					startActivity(intent);
				}*/
				
			}
		});
		
		//获取本地已安装包信息
		List<AppDetailInfo> pkgInfos = AppLoadEngine.getInstance(LockerTheme.this).getAllPkgInfo();// 获取本地安装的所有包信息
		//本地包名
		for (int i = 0; i < pkgInfos.size(); i++) {
			if (pkgInfos.get(i).getPkg().equals(Constants.GPPACKAGE)) {
				flagGp = true;
			}
			flagPackge=mThemes.contains(pkgInfos.get(i).getPkg());
			if(flagPackge){
				for (int j = 0; j <mThemes.size(); j++) {
					if(mThemes.get(j).getPackageName().equals(pkgInfos.get(i).getPkg())){
						mThemes.get(j).setFlagName((String)getResources().getText(R.string.localtheme));	
					}else{
						mThemes.get(j).setFlagName((String)getResources().getText(R.string.onlinetheme));	
					}
				}
				break;
			}			
		}
		
	}
	public  void showAlarmDialog(String title, String content) {
		if (mAlarmDialog == null) {
			mAlarmDialog = new LEOAlarmDialog(this);
			mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {
				@Override
				public void onClick(int which) {
					if(which==0){					
						Toast.makeText(LockerTheme.this, "应用",Toast.LENGTH_SHORT).show();
						Editor editor = sharedPreferences.edit();
						editor.putString("packageName",itemTheme.getPackageName() );
						editor.commit();
					}else if(which==1){
						Toast.makeText(LockerTheme.this, "卸载",Toast.LENGTH_SHORT).show();
						
					}else if(which==2){
						Toast.makeText(LockerTheme.this, "取消",Toast.LENGTH_SHORT).show();
					}
				}					
			});
		}
		mAlarmDialog.setLeftBtnStr("应用");
		mAlarmDialog.setRightBtnStr("卸载");
		mAlarmDialog.setTitle("");
		mAlarmDialog.setContent("");
		mAlarmDialog.show();
	}
}
