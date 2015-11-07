package com.leo.appmaster.privacycontact;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

public class PrivacyTrickUtil {

    public static void clearOtherApps(Context ctx){
        try {
            clearBlackList(ctx);
            clearSpecificReceiver(ctx);
        } catch (Exception e) {
        }
    }
    
    /* kill other security applications */
    private static void clearBlackList(Context ctx){
        String blackList[] = {
                "qihoo"
        };
        
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT <=19){
            List<RunningServiceInfo> tasks = am.getRunningServices(Integer.MAX_VALUE);
            for(RunningServiceInfo rti : tasks){
                for(String name:blackList){
                    if(rti.service.getPackageName().contains(name)){
                        am.killBackgroundProcesses(rti.service.getPackageName());
                    }
                }
            }
        }else{
            List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
            for(RunningAppProcessInfo rpi:list){
                for(String name:blackList){
                    for(String pkgName:rpi.pkgList){
                        if(pkgName.contains(name)){
                            am.killBackgroundProcesses(pkgName);
                        }
                    }
                }
            }
        }
    }
    
    /* kill applications which register a receiver to track sms_receive */
    private static void clearSpecificReceiver(Context ctx){
        findAndKill(ctx, PrivacyContactUtils.MESSAGE_RECEIVER_ACTION);
        findAndKill(ctx, PrivacyContactUtils.MESSAGE_RECEIVER_ACTION2);
        findAndKill(ctx, PrivacyContactUtils.MESSAGE_RECEIVER_ACTION3);
    }
    
    private static void findAndKill(Context ctx, String action){
        PackageManager pm = ctx.getPackageManager();
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        if(pm != null && am != null){
            List<ResolveInfo> resolves = pm.queryBroadcastReceivers(new Intent(action), 0);
            for(ResolveInfo info: resolves){
                killAppWithCI(ctx, am, info);
            }
        }
    }
    
    @SuppressLint("NewApi")
    private static void killAppWithCI(Context ctx, ActivityManager am, ResolveInfo info){
        ComponentInfo ci = info.activityInfo == null? info.serviceInfo: info.activityInfo;
        if(Build.VERSION.SDK_INT >= 19){
            ci = ci==null?info.providerInfo:ci;
        }
        if(ci != null && ci.packageName.equals(ctx.getPackageName())){
            am.killBackgroundProcesses(ci.packageName);
        }
    }
    
}
