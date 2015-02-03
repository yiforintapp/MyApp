package com.leo.appmaster.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.leo.analytics.LeoAgent;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.FolderItemInfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.Display;
import android.view.WindowManager;

public final class Utilities {

	private static final int MAX_ICON = 4;

	public static Drawable getFolderScalePicture(Context context,
			List<AppItemInfo> folderList, int type) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Resources res = context.getResources();
		int iconWidth = res.getDimensionPixelSize(R.dimen.app_size);
		Bitmap folderPic = Bitmap.createBitmap(iconWidth, iconWidth,
				Bitmap.Config.ARGB_8888);
		if (type == FolderItemInfo.FOLDER_BUSINESS_APP) {
			BitmapDrawable resault = (BitmapDrawable) res
					.getDrawable(R.drawable.folder_icon_recommend);

			AppMasterPreference pref = AppMasterPreference.getInstance(context);
			String online = pref.getOnlineBusinessSerialNumber();
			String local = pref.getLocalBusinessSerialNumber();
			if (online != null && !online.equals(local)) {
				Bitmap newTipBitamp = BitmapFactory.decodeResource(res,
						R.drawable.folder_new_icon);
				int tipSize = newTipBitamp.getWidth();

				Canvas canvas = new Canvas(folderPic);
				canvas.drawBitmap(resault.getBitmap(), 0, 0, paint);
				canvas.drawBitmap(newTipBitamp, iconWidth - tipSize, 0, paint);

				String newTag = context.getString(R.string.folder_new);

				Rect rect = new Rect();
				paint.setTextSize(tipSize / 2.8f);
				paint.getTextBounds(newTag, 0, newTag.length(), rect);
				int textWidth = rect.width();
				int textHeight = rect.height();

				float x = iconWidth - tipSize + (tipSize - textWidth) / 2;
				float y = (tipSize - textHeight) / 2 + tipSize / 5.5f;
				paint.setColor(Color.WHITE);
				canvas.drawText(newTag, x, y, paint);

				BitmapDrawable dd = new BitmapDrawable(res, folderPic);
				return dd;
			} else {
				return resault;
			}
		}
		int size = folderList.size();
		Drawable folderBg;
		if (type == FolderItemInfo.FOLDER_BACKUP_RESTORE) {
			if (folderList == null || folderList.isEmpty()) {
				folderBg = res
						.getDrawable(R.drawable.backup_folder_empty_bg_icon);
			} else {
				folderBg = res.getDrawable(R.drawable.common_folder_bg_icon);
			}
		} else {
			folderBg = res.getDrawable(R.drawable.common_folder_bg_icon);
		}

		folderBg.setAlpha(255);
		final int picWidth = folderBg.getIntrinsicWidth();
		final int picHeight = folderBg.getIntrinsicHeight();

		Canvas canvas = new Canvas(folderPic);

		canvas.save();
		if (iconWidth != picWidth || iconWidth != picHeight) {
			canvas.scale((float) iconWidth / picWidth, (float) iconWidth
					/ picHeight);
		}
		folderBg.setBounds(0, 0, picWidth, picHeight);
		folderBg.draw(canvas);
		canvas.restore();

		int row_num = 2;
		int folder_icon_x = (int) (iconWidth / 9.5);
		int folder_icon_y = folder_icon_x;
		int folder_icon_h = folder_icon_x;
		int folder_icon_v = folder_icon_x;
		int folder_icon_size = (iconWidth - (row_num + 1) * folder_icon_x)
				/ row_num;

		int x = folder_icon_x;
		int y = folder_icon_y;

		for (int i = 0; i < folderList.size(); i++) {
			if (i >= MAX_ICON)
				break;
			BaseInfo info = folderList.get(i);
			int originW = info.icon.getIntrinsicWidth();
			int originH = info.icon.getIntrinsicHeight();
			float scaleW = ((float) folder_icon_size) / originW;
			float scaleH = ((float) folder_icon_size) / originH;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleW, scaleH);
			BitmapDrawable bd = (BitmapDrawable) info.icon;
			Bitmap s = Bitmap.createBitmap(bd.getBitmap(), 0, 0, originW,
					originH, matrix, true);

			// as the content is already sorted, just draw them by order
			int cellX = i / 2;
			int cellY = i / 2;
			if (i == 0) {
				cellX = 0;
				cellY = 0;
			} else if (i == 1) {
				cellX = 1;
				cellY = 0;
			} else if (i == 2) {
				cellX = 0;
				cellY = 1;
			} else if (i == 1) {
				cellX = 1;
				cellY = 1;
			}

			x = folder_icon_x + cellX * (folder_icon_size + folder_icon_h);
			y = folder_icon_y + cellY * (folder_icon_size + folder_icon_v);
			canvas.drawBitmap(s, (float) x, (float) y, null);

			s.recycle();
		}

		BitmapDrawable dd = new BitmapDrawable(res, folderPic);
		return dd;
	}

	public static int[] getScreenSize(Context ctx) {
		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		Display displayer = wm.getDefaultDisplay();
		int size[] = new int[2];
		size[0] = displayer.getWidth();
		size[1] = displayer.getHeight();
		return size;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			return toHex(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return "NUHC";
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}

	private final static String HEX = "0123456789ABCDEF";

	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
	}

	public static boolean isPackageInsalled(Context context, String packageName) {
		if (packageName == null)
			return false;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageInfo(packageName, 0);
			if (info != null) {
				return true;
			}
		} catch (NameNotFoundException e) {
			return false;
		}
		return false;
	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}

		return false;
	}

	public static String trimString(String s) {
		return s.replaceAll("\u00A0", "").trim();
	}

	public static String getURL(String suffix){
	    return "http://" + LeoAgent.getBestServerDomain() + suffix;
	}
	
}
