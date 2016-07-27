package com.zlf.appmaster.utils;

import android.content.Context;
import android.content.Intent;

 public class QuickHelperUtils {
    
    public static void createQuickHelper(String name , int iconResId , Intent intent , Context context){
        Intent makeShortcut = new Intent( "com.android.launcher.action.INSTALL_SHORTCUT");
        makeShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME , name);
        Intent.ShortcutIconResource IconRes = Intent.ShortcutIconResource.fromContext(context,iconResId );
        makeShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, IconRes);
        makeShortcut.putExtra("duplicate", false);
        makeShortcut.putExtra("from_shortcut", true);
        makeShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        context.sendBroadcast(makeShortcut);
    }
}
 
 
 
 
// Intent intent;
// Intent makeSHortcut;
// Intent.ShortcutIconResource appwallIconRes;
// int id = (int)getItemId(position);
 
//case R.drawable.qh_image_icon:
// intent = new Intent(AppMasterApplication.getInstance(),
//         ImageHideMainActivity.class);
// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//         | Intent.FLAG_ACTIVITY_CLEAR_TASK);
// makeSHortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
// makeSHortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME , getResources().getString(mHelperNames[POSITION_IMAGE_HIDE]));
// appwallIconRes = Intent.ShortcutIconResource.fromContext(QuickHelperActivity.this, mHelperResourceIDs[POSITION_IMAGE_HIDE]);
// makeSHortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,appwallIconRes);
// makeSHortcut.putExtra("duplicate", false);
// makeSHortcut.putExtra("from_shortcut", true);
// makeSHortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
//         intent);
// // makeShortcut8.set
// sendBroadcast(makeSHortcut);
// break;