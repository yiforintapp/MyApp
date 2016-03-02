package com.leo.appmaster.phoneSecurity;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by runlee on 15-10-16.
 */
public class PhoneSecurityUtils {
    public static String TAG = "PhoneSecurityUtils";
    private static File mLocationLogile;
    /**
     * 获取GoogleMap位置Uri
     *
     * @return
     */
    public static String getGoogleMapLocationUri() {
        LostSecurityManagerImpl lm = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        Location loc = lm.getLocation(PhoneSecurityConstants.LOCA_ID_MSM);
        if (loc != null) {
            LeoLog.i(TAG, "纬度：" + loc.getLatitude() + "----经度：" + loc.getLongitude());
            String language = Locale.getDefault().getLanguage();
            StringBuilder googleMapUri = null;
            if (language.equalsIgnoreCase(PhoneSecurityConstants.ZH)) {
                googleMapUri = new StringBuilder(PhoneSecurityConstants.GOOGLE_MAP_URI_CN);
            } else {
                googleMapUri = new StringBuilder(PhoneSecurityConstants.GOOGLE_MAP_URI);
            }

            googleMapUri.append("?mrt=loc&q=");
            googleMapUri.append(loc.getLatitude());
            googleMapUri.append("%2C");
            googleMapUri.append(loc.getLongitude());
            return googleMapUri.toString();
        } else {
            LeoLog.i(TAG, "获取地理位置为空");
            return null;
        }
    }

    /**
     * 解析上次查询保存的电话号码对应的短信条数
     */
    public static List<ContactBean> parseSaveNumberToCount(String string) {
        if (Utilities.isEmpty(string)) {
            return null;
        }
        List<ContactBean> contacts = new ArrayList<ContactBean>();
        String[] number_count = string.split(",");
        for (String str : number_count) {
            String[] number = str.split(":");
            ContactBean contact = new ContactBean();
            contact.setContactNumber(number[0]);
            contact.setCount(Integer.parseInt(number[1]));
            contacts.add(contact);
        }
        return contacts;
    }

    public static String formatePhoneNumber(String number) {
        String reverseStr = null;
        if (number != null) {
            int strLength = number.length();
            if (strLength >= 7) {
                String tempStr = stringReverse(number);
                String subNumber = tempStr.substring(0, 7);
                reverseStr = stringReverse(subNumber);
            } else {
                reverseStr = number;
            }
        }
        return reverseStr;
    }

    /**
     * String reverse
     */
    public static String stringReverse(String str) {
        StringBuffer sbf = new StringBuffer(str);
        return sbf.reverse().toString();
    }

    /**
     * 获取系统内存大小
     */
    public static String getTotalRam() {
        String str1 = "/proc/meminfo";
        String str2 = "";
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            while ((str2 = localBufferedReader.readLine()) != null) {
                String ram = str2.replace(" ", "");
                String[] ramSize = ram.split(":");
                return ramSize[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取CPU名字
     */

    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取SD卡大小
     *
     * @return 0, 为总大小，1,可用大小
     */
    public static long[] getSDCardMemory() {
        long[] sdCardInfo = new long[2];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long bCount = sf.getBlockCount();
            long availBlocks = sf.getAvailableBlocks();

            sdCardInfo[0] = bSize * bCount;//总大小
            sdCardInfo[1] = bSize * availBlocks;//可用大小
        }
        return sdCardInfo;
    }

    /**
     * 获取机型
     */
    public static String getPhoneModel() {
        return BuildProperties.getPoneModel();
    }

    /**
     * 获取屏幕分辨率
     */
    public static String getScreenPix(Context context) {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;
        LeoLog.i(TAG, "分辨率：" + (width + "*" + height));
        return width + "*" + height;
    }

    /**
     * 获取屏幕的物理尺寸
     */
    public static double getScreenPhysicalSize(Context ctx) {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
        double diagonalPixels = Math.sqrt(Math.pow(mDisplayMetrics.widthPixels, 2) + Math.pow(mDisplayMetrics.heightPixels, 2));
        return diagonalPixels / (160 * mDisplayMetrics.density);
    }

    /**
     * 经纬度反向解析成地址
     */
    public static String getAddress(Context context, Location location, double latitude, double longitude) {
        double lat = -1;
        double lon = -1;
        LostSecurityManagerImpl lm = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        Location loc = lm.getLocation(PhoneSecurityConstants.LOCA_ID_PARSE_LOC);
        if (latitude > 0) {
            lat = latitude;
        } else {
            lat = loc.getLatitude();
        }

        if (longitude > 0) {
            lon = longitude;
        } else {
            lon = loc.getLongitude();
        }

        String address = "";
        String url = String.format("http://ditu.google.cn/maps/geo?output=csv&key=abcdef&q=" + lat + "," + lon);
        URL myURL = null;
        URLConnection httpsConn = null;
        try {
            myURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
        try {
            httpsConn = (URLConnection) myURL.openConnection();
            if (httpsConn != null) {
                InputStream inputStream = httpsConn.getInputStream();
                InputStreamReader insr = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader br = new BufferedReader(insr);
                String data = null;
                if ((data = br.readLine()) != null) {
                    System.out.println(data);
                    String[] retList = data.split(",");
                    if (retList.length > 2 && ("200".equals(retList[0]))) {
                        address = retList[2];
                        address = address.replace("\"", "");
                    } else {
                        address = "";
                    }
                }
                insr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return address;
    }

    /*手机震动*/
    public static void setVibrate(Context context, long milliseconds) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    /*对经纬度进行解析并翻译成中文地址*/
    public static String showLocation(Location location) {
        String locate = null;
        try {
            //组装反向地理编码的接口地址
            StringBuilder url = new StringBuilder();
            url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
            url.append(location.getLatitude()).append(",");
            url.append(location.getLongitude());
            url.append("&sensor=false");
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url.toString());
            //在请求头信息头中指定语言，保证服务器会返回中文数据
//                    httpGet.addHeader("Accept-Language", "zh-CN");
            HttpResponse httpResponse = httpclient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = httpResponse.getEntity();
                String response = EntityUtils.toString(entity, "utf-8");

                JSONObject jsonObject = new JSONObject(response);
                //获取results节点下的位置信息
                JSONArray resultArray = jsonObject.getJSONArray("results");

                if (resultArray.length() > 0) {
                    JSONObject subObject = resultArray.getJSONObject(0);
                    //取出格式化后的位置信息
                    String address = subObject.getString("formatted_address");
                    String[] locatName = address.split(",");
                    locate = locatName[1];
                    LeoLog.i(TAG, "地址=" + locatName[1]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return locate;
    }
    public static String getLocateProvider(LocationManager locateManager){
        Criteria criteria = new Criteria();
        /*设置为最大精度*/
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        /*不要求海拔信息*/
        criteria.setAltitudeRequired(false);
        /*不要求方位信息*/
        criteria.setBearingRequired(false);
        /*是否允许付费*/
        criteria.setCostAllowed(true);
        /*对电量的要求*/
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locateManager.getBestProvider(criteria, true);
        return provider;
    }


    public static void writeToFile(String message) {
        if (!LeoLog.isSDCardAvaible())
            return;

        try {
            if (mLocationLogile == null) {
                File dir = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + File.separator + "run_test");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                mLocationLogile = new File(dir.getAbsolutePath() + File.separator
                        + "location.txt");
            }

            if (!mLocationLogile.exists()) {
                mLocationLogile.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        BufferedWriter buf = null;
        try {
            // use BufferedWriter for performance, true to set append to file
            // flag
            buf = new BufferedWriter(new FileWriter(mLocationLogile, false));
            buf.append(message);
        } catch (IOException e) {
        } finally {
            try {
                buf.close();
            } catch (IOException e) {
            }
        }
    }

}
