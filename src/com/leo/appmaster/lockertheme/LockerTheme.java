package com.leo.appmaster.lockertheme;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
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
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class LockerTheme extends Activity{
	private ListView listTheme;
	private List<AppLockerThemeBean> onlintThemes;
	private List<AppLockerThemeBean> localThemes;
	private boolean flagGp=false;//判断是否存在GP
	private void initUI(){
		CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		title.setTitle(R.string.lockerTheme);
		title.openBackView();
		listTheme=(ListView) findViewById(R.id.themeLV);
	}
	@Override
	protected void onCreate(Bundle arg0) {	
		super.onCreate(arg0);
		setContentView(R.layout.activity_locker_theme);
		onlintThemes=new ArrayList<AppLockerThemeBean>();
		localThemes=new ArrayList<AppLockerThemeBean>();
		initUI();
	/*
	 * ------------------------------------------构造数据----------------------------------------------------------
	 */
		AppLockerThemeBean applockerTheme=new AppLockerThemeBean();
		int[] imageTheme1=new int[2];
		imageTheme1[0]=R.drawable.select_all_press;
		imageTheme1[1]=R.drawable.press_select_all_normal;
		applockerTheme.setThemeImage(imageTheme1);
		applockerTheme.setThemeName("主题");
		String[] url=new String[2];
		url[0]="http://www.baidu.com";
		url[1]="www.xiaomi.com";
		applockerTheme.setUrl(url);
		//------------------默认
		AppLockerThemeBean applockerTheme1=new AppLockerThemeBean();
		int[] imageTheme=new int[2];
		imageTheme[0]=R.drawable.select_all_press;
		imageTheme[1]=R.drawable.press_select_all_normal;
		applockerTheme1.setThemeImage(imageTheme);
		applockerTheme1.setThemeName("默认主题");
		String[] url1=new String[2];
		url1[0]="http://down2.iinmobi.com/group1/M01/09/73/ooYBAFRh45eAGQVaACCBUDfiyfw938.apk";
		url1[1]="www.xiaomi.com";
		applockerTheme1.setUrl(url1);		
		localThemes.add(applockerTheme1);	
		for (int i = 0; i <8; i++) {		
			localThemes.add(applockerTheme);
		}
		
		/*
		 * ----------------------------------------------------------------------------------------------------------------------
		 */
		listTheme.setAdapter(new LockerThemeAdapter(this,localThemes));
		/*
		 * OnlineTheme
		 */
		listTheme.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(LockerTheme.this, "在线主题"+(arg2+1),Toast.LENGTH_SHORT).show();
				AppLockerThemeBean theme=(AppLockerThemeBean) arg0.getItemAtPosition(arg2);
				String[] urls=theme.getUrl();
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
				
			}
		});
		
		//获取本地已安装包信息
		List<AppDetailInfo> pkgInfos = AppLoadEngine.getInstance(LockerTheme.this).getAllPkgInfo();// 获取本地安装的所有包信息
		// 获取包名
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pkgInfos.size(); i++) {
			if (pkgInfos.get(i).getPkg().equals(Constants.GPPACKAGE)) {
				flagGp = true;
			}
		}
	}

}
