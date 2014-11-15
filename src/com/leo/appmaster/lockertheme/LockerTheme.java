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
import android.content.pm.PackageManager.NameNotFoundException;
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

public class LockerTheme extends Activity {
	private ListView listTheme;
	private List<AppLockerThemeBean> mThemes;
	private List<String> localThemes;
	private boolean flagGp=false;//判断是否存在GP
	private LEOAlarmDialog mAlarmDialog;
	private AppLockerThemeBean itemTheme;
	private SharedPreferences sharedPreferences; 
	private int number=0;
	private static final  String  PREFERENCESPACKAGE="com.android.vending";//默认主题
	private void initUI(){
		CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		title.setTitle(R.string.lockerTheme);
		title.openBackView();
		listTheme=(ListView) findViewById(R.id.themeLV);	
		sharedPreferences = getSharedPreferences("lockerTheme", Context.MODE_WORLD_WRITEABLE); 
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		boolean flagPackge = false;
		setContentView(R.layout.activity_locker_theme);
		initUI();
		localThemes=new ArrayList<String>();
		mThemes=new ArrayList<AppLockerThemeBean>();
/*
* ------------------------------------------构造数据----------------------------------------------------------
*/
		//Theme1
		AppLockerThemeBean moonnightTheme=new AppLockerThemeBean();
		moonnightTheme.setThemeImage(this.getResources().getDrawable(R.drawable.moonnight_theme));
		moonnightTheme.setThemeName((String)this.getResources().getText(R.string.moonightTheme));
		String[] moonnightUrl=new String[2];
		moonnightUrl[1]="http://down2.iinmobi.com/group1/M01/09/73/ooYBAFRh45eAGQVaACCBUDfiyfw938.apk";
		moonnightUrl[0]="com.mah.calldetailscreen";
		moonnightTheme.setUrl(moonnightUrl);
		moonnightTheme.setPackageName("com.mah.calldetailscreen");
		moonnightTheme.setFlagName((String)this.getResources().getText(R.string.onlinetheme));
		moonnightTheme.setIsVisibility(Constants.VISIBLE);
		mThemes.add(moonnightTheme);
		//Theme2
		AppLockerThemeBean orangeTheme=new AppLockerThemeBean();
		orangeTheme.setThemeImage(this.getResources().getDrawable(R.drawable.orange_theme));
		orangeTheme.setThemeName((String)this.getResources().getText(R.string.orangeTheme));
		String[] orangeUrl=new String[2];
		orangeUrl[1]="http://down2.iinmobi.com/group1/M01/09/73/ooYBAFRh45eAGQVaACCBUDfiyfw938.apk";
		orangeUrl[0]="com.mah.calldetailscreen";
		orangeTheme.setUrl(orangeUrl);
		orangeTheme.setPackageName("com.mah.calldetailscreen");
		orangeTheme.setFlagName((String)this.getResources().getText(R.string.onlinetheme));
		orangeTheme.setIsVisibility(Constants.GONE);
		mThemes.add(orangeTheme);
		//Theme3
		AppLockerThemeBean paradoxTheme=new AppLockerThemeBean();
		paradoxTheme.setThemeImage(this.getResources().getDrawable(R.drawable.paradox_theme));
		paradoxTheme.setThemeName((String)this.getResources().getText(R.string.ParadoxTheme));
		String[] paradoxUrl=new String[2];
		paradoxUrl[1]="http://down2.iinmobi.com/group1/M01/09/73/ooYBAFRh45eAGQVaACCBUDfiyfw938.apk";
		paradoxUrl[0]="com.mah.calldetailscreen";
		paradoxTheme.setUrl(orangeUrl);
		paradoxTheme.setPackageName("com.mah.calldetailscreen");
		paradoxTheme.setFlagName((String)this.getResources().getText(R.string.onlinetheme));
		paradoxTheme.setIsVisibility(Constants.GONE);
		mThemes.add(paradoxTheme);
/*
* ----------------------------------------------------------------------------------------------------------------------
*/
		listTheme.setAdapter(new LockerThemeAdapter(this,mThemes));
		// 定向主题
		Intent intent = this.getIntent();
		String temp = intent.getStringExtra("theme_package");
		if (temp != null && !temp.equals("")) {
			for (int i = 0; i < mThemes.size(); i++) {
				if (mThemes.get(i).getPackageName().equals(temp)) {
					number = i;
				}
			}
		} else {
			number = 0;
		}
		listTheme.setAdapter(new LockerThemeAdapter(this, mThemes));
		/*
		 * OnlineTheme
		 */
		listTheme.setSelection(number);//Item定向跳转
		listTheme.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {		
			//Toast.makeText(LockerTheme.this, "在线主题"+(arg2+1),Toast.LENGTH_SHORT).show();
			itemTheme=(AppLockerThemeBean) arg0.getItemAtPosition(arg2);
				String[] urls=itemTheme.getUrl();			
				if(mThemes.get(arg2).getFlagName().equals((String)getResources().getText(R.string.onlinetheme))){
					for (int i = 0; i < urls.length; i++) {
						boolean flag = AppwallHttpUtil.isHttpUrl(urls[i]);
						if (flagGp) {
							if (!flag) {
								AppwallHttpUtil.requestGp(LockerTheme.this,urls[i]);
								break;
							}
						} else if(flag) {
							AppwallHttpUtil.requestUrl(LockerTheme.this, urls[i]);
							break;
						}
					}						
				}else if(mThemes.get(arg2).getFlagName().equals((String)getResources().getText(R.string.localtheme))){						
					showAlarmDialog(null,null);
				}				
			}
		});
		getTeme();
	}

	public void showAlarmDialog(String title, String content) {
		if (mAlarmDialog == null) {
			mAlarmDialog = new LEOAlarmDialog(this);
			mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {
				@Override
				public void onClick(int which) {
					if(which==0){					
					//	Toast.makeText(LockerTheme.this, "应用",Toast.LENGTH_SHORT).show();

						Editor editor = sharedPreferences.edit();
						editor.putString("packageName",
								itemTheme.getPackageName());
						editor.commit();
					}else if(which==1){
					//	Toast.makeText(LockerTheme.this, "卸载",Toast.LENGTH_SHORT).show();
						// 卸载主题
					        Uri uri = Uri.fromParts("package", itemTheme.getPackageName(), null);
					        Intent intent= new Intent(Intent.ACTION_DELETE, uri);
					        startActivity(intent);   
					}else if(which==2){
						Toast.makeText(LockerTheme.this, "取消",Toast.LENGTH_SHORT).show();
						mAlarmDialog.dismiss();
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
	@Override
	protected void onResume() {
		super.onResume();
		getTeme();
	}
	//获取主题
	public void getTeme(){
		boolean flagPackge=false;
		//获取本地已安装包信息
		List<AppDetailInfo> pkgInfos = AppLoadEngine.getInstance(LockerTheme.this).getAllPkgInfo();// 获取本地安装的所有包信息
		//本地包名
		for (int i = 0; i < pkgInfos.size(); i++) {
			if (pkgInfos.get(i).getPkg().equals(Constants.GPPACKAGE)) {
				flagGp = true;
			}
			//flagPackge=mThemes.contains(pkgInfos.get(i).getPkg());
			flagPackge=pkgInfos.get(i).getPkg().startsWith("com.leo.theme");
			if(flagPackge){
				localThemes.add(pkgInfos.get(i).getPkg());
			}
		}
		for (int i = 0; i <localThemes.size(); i++) {
			Context saveContext=null;
			try {
				saveContext=LockerTheme.this.createPackageContext(localThemes.get(i), Context.CONTEXT_IGNORE_SECURITY);
			} catch (NameNotFoundException e1) {
				LeoLog.i("Context","getContext error");
			}
			boolean flag=mThemes.contains(localThemes.get(i));
			if(flag){
				for (int j = 0; j <mThemes.size(); j++) {
					if(mThemes.get(j).getPackageName().equals(localThemes.get(i))){
						mThemes.get(j).setFlagName((String)getResources().getText(R.string.localtheme));	
						mThemes.get(j).setThemeImage(saveContext.getResources().getDrawable(R.drawable.splash_icon));
					}else{
						mThemes.get(j).setFlagName((String)getResources().getText(R.string.onlinetheme));	
						mThemes.get(j).setThemeImage(LockerTheme.this.getResources().getDrawable(R.drawable.select_all_press));
					}
				}
			}else{
				AppLockerThemeBean tempTheme=new AppLockerThemeBean();
				tempTheme.setFlagName((String)saveContext.getResources().getText(R.string.localtheme));
				tempTheme.setThemeImage(saveContext.getResources().getDrawable(R.drawable.splash_icon));
				tempTheme.setThemeName("");
				tempTheme.setPackageName(localThemes.get(i));
				mThemes.add(tempTheme);
			}
		
		}
		//标记当前使用的主题
		String sharedPackageName=sharedPreferences.getString("packageName",PREFERENCESPACKAGE );
		for (int i = 0; i <mThemes.size() ; i++) {
				if(mThemes.get(i).getPackageName().equals(sharedPackageName))
				{
					mThemes.get(i).setIsVisibility(Constants.VISIBLE);
				}else{
					mThemes.get(i).setIsVisibility(Constants.GONE);
				}
		}
			
	}
}
