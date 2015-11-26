package com.leo.appmaster.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.leo.appmaster.Constants;

public class AppwallHttpUtil {
	public static InputStream requestByPost(String path,
			Map<String, String> param, String charset) {
		InputStream is = null;

		BasicHttpParams httpParams;
		httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams,
				Constants.REQUEST_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, Constants.SO_TIMEOUT);
		try {
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpPost request = new HttpPost(path);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			Set<Map.Entry<String, String>> es = param.entrySet();
			for (Entry<String, String> entry : es) {
				NameValuePair p = new BasicNameValuePair(entry.getKey(),
						entry.getValue());
				parameters.add(p);
			}
			HttpEntity entity = new UrlEncodedFormEntity(parameters, charset);
			request.setEntity(entity);
			HttpResponse response = client.execute(request);
			// 获取结果码
			if (response.getStatusLine().getStatusCode() == 200) {
				// 获取输入流
				is = response.getEntity().getContent();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}

	public static String getJsonByInputStream(InputStream is, String charsetName) {
		String info = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, charsetName));
			String temp = null;
			StringBuilder sb = new StringBuilder();
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
			}
			info = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return info;
	}

	// 获取系统当前语言代码
	public static String getLanguage() { 
		return Locale.getDefault().getLanguage();
	}
	//获取系统当前国家代码
	public static String getCountry() { 
        return Locale.getDefault().getCountry();
    }

	// 重命名路径
	public static boolean RenameFilePath(String filePath, String newFileName) {
		if (filePath == null || newFileName == null) {
			LeoLog.e("RenameFile", "Rename: null parameter");
			return false;
		}

		File file = new File(filePath);
		try {
			if (file.isFile()) {
				boolean ret = file.renameTo(new File(newFileName));
				LeoLog.e("RenameFile", ret + " to rename file");
				return ret;
			} else {
				return false;
			}
		} catch (SecurityException e) {
			LeoLog.e("RenameFile", "Fail to rename file," + e.toString());
		}
		return false;
	}

	// 访问浏览器
	// 访问网址
	public static void requestUrl(Context context, String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try {
	        context.startActivity(intent);
		} catch (Exception e) {
		}
	}

	// 访问GooglPlay
	public static void requestGp(Context context, String packageGp) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=" + packageGp));
		intent.setPackage(Constants.GP_PACKAGE);
	      try {
	          context.startActivity(intent);
	        } catch (Exception e) {
	        }
	}

	// 判断字符串
	public static boolean isHttpUrl(String str) {
		boolean flag = false;
		flag = str.startsWith("http");
		return flag;
	}

}
