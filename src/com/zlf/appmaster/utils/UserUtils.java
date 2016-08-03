package com.zlf.appmaster.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Toast;

import com.zlf.appmaster.client.LoginClient;
import com.zlf.appmaster.db.stock.QiNiuDBHelper;

import java.util.List;

/**
 * 公共工具类
 * @author Deping Huang
 *
 */
public class UserUtils {
	
//	private static final String TAG = "Utils"; 
	
	/**
	 * 设置WebView的布局显示，如果详细内容包含table标签，则不设置webview为单列
	 * 
	 * @param webView
	 * @param content
	 * @return
	 */
	public static void setWebViewLayout(WebView webView, String content) {
		if (content.contains("<table")) {
			webView.getSettings().setLayoutAlgorithm(
					LayoutAlgorithm.NARROW_COLUMNS);
		} else {
			webView.getSettings().setLayoutAlgorithm(
					LayoutAlgorithm.SINGLE_COLUMN);
		}
	}
    
    /**
     * 用来判断服务是否运行.
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager)
		mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
       if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
    
    //清除本地登录信息
    public static void clearUserLoginInfo(Context mContext) {

/*    	//关闭心跳服务
    	if (isServiceRunning(mContext,KeepLiveService.class.getName())) {
    		Intent intent = new Intent(mContext,KeepLiveService.class);
        	mContext.stopService(intent);
		}
    	
    	// 释放后台im服务
		IMClient.getInstance(mContext).release();*/
		
		QiNiuDBHelper.getInstance(mContext).closeDB();
		
		//下次不自动登录
		Setting sp = new Setting(mContext);
		//删除sid uin
		sp.setSessionId("");
		sp.setUin("");

		//清空账号
		LoginClient.getInstance(mContext).clearAccountInfo();
	}

    /**
     *
     * @param context
     * @return
     */
    public static boolean checkLogin(View view, Activity activity, Context context){
        //没有登录则跳转到登录界面
        if (TextUtils.isEmpty(Utils.getAccountUin(context))){
//            Intent intent = new Intent(context,EntryActivity.class);
//            context.startActivity(intent);

            //context.startActivity(new Intent(context, EntryPopActivity.class));
            showEntryFengNiuPopwindow(view,context,activity);
            return  false;
        }

        return  true;
    }

    private static void showEntryFengNiuPopwindow(View view, Context context , final Activity activity) {
//        backgroundAlpha(activity,0.4f);
//        EntryAppPopupWindow entryAppPopupWindow;
//        entryAppPopupWindow = new EntryAppPopupWindow(context,activity);
//        entryAppPopupWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//
//        entryAppPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//        @Override
//        public void onDismiss() {
//            backgroundAlpha(activity,1f);
//        }
//    });
        QToast.show(context, "登录弹窗", Toast.LENGTH_SHORT);
    }

    private static void backgroundAlpha(Activity activity, float alpha) {
        WindowManager.LayoutParams lp=  activity.getWindow().getAttributes();
        lp.alpha = alpha;
        activity.getWindow().setAttributes(lp);
    }
}
