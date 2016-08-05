package com.zlf.appmaster.ui.stock;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.zlf.appmaster.utils.QLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * pdf下载进度框
 * Created by Huang on 2015/7/8.
 */
public class PdfReadProgressDialog extends ProgressDialog {
    private final static String TAG = PdfReadProgressDialog.class.getSimpleName();
    private String mPdfPath;
    private String mCacheID="default";    // 缓存ID

    private Context mContext;

    public PdfReadProgressDialog(Context context, String pdfPath, String cacheId) {
        super(context);
        mContext = context;
        mPdfPath = pdfPath;
        mCacheID = cacheId;
    }

    public void start(){

        if (openFile(createDir(mCacheID+".pdf"))){
            QLog.i(TAG,"open on disk");
        }
        else {  // 本地没有缓存则从网络上下载

            this.setMessage("正在下载数据，请稍等....");
            this.setIndeterminate(false);
            this.setMax(10000);
            this.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.setCancelable(true);

            DownloadFile downloadFile = new DownloadFile();
            downloadFile.execute(mPdfPath, createDir(mCacheID+".pdf"));
        }

    }


    private String createDir(String filename) {
        File sdcardDir = Environment.getExternalStorageDirectory();

        String path = sdcardDir.getPath() + "/QiniuPdfCache";
        File path1 = new File(path);
        if (!path1.exists())

            path1.mkdirs();
        path = path + "/" + filename;
        return path;
    }


    private boolean openFile(String filePath){
        if (new File(filePath).exists()){
//            Uri uri = Uri.parse(filePath);
//            Intent intent = new Intent(mContext,
//                    MuPDFActivity.class);
//            intent.setAction(Intent.ACTION_VIEW);
//            intent.setData(uri);
//            mContext.startActivity(intent);
            return true;
        }
        else {
            return false;
        }
    }




    // 通常，AsyncTask子类在activity类中进行声明
    // 这样，你可以很容易在这里修改UI线程
    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                URL url = new URL(sUrl[0]);
                String cachePath = sUrl[1];
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("Accept-Encoding", "identity"); // 默认情况下,HttpURLConnection使用gzip方式获取,要获取文件长度要添加这行代码
                connection.connect();

                int fileLength = connection.getContentLength();
                if (fileLength != 0 && fileLength != -1){
                    publishProgress(0, fileLength); // 设置初始进度范围
                }

                // 下载文件
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(cachePath);

                byte data[] = new byte[1024];
                int total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(1, total);
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                // 下载完成 open
                openFile(cachePath);
            } catch (Exception e) {
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PdfReadProgressDialog.this.show();
        }

        /**
         * 参数1标识是否开始下载，如果是参数2标识下载进度
         * @param progress
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            if (progress[0] == 0){
                PdfReadProgressDialog.this.setMax(progress[1]);
                //PdfReadProgressDialog.this.setMax(5307933);
                QLog.i(TAG,"progress file length:"+progress[1]);
            }
            else if (progress[0] == 1){
                PdfReadProgressDialog.this.setProgress(progress[1]);
                //QLog.i(TAG,"progress:"+progress[1]);
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            PdfReadProgressDialog.this.cancel();
        }
    }
}
