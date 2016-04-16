package com.leo.appmaster.ad;

import android.graphics.Bitmap;
import android.view.View;

import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 用于LOCK界面大图加载。
 * ImageLoader自身有限制：当同时发起多个url一样的load请求的时候，只有一个请求会在完成后
 * 回调onLoadingComplete，其他请求会回调onLoadingCancelled。对于LOCK界面的大图的特殊
 * 需求需要我们自己实现可以同时load同样url的功能。
 * Created by stone on 16/3/16.
 */
public class PreviewImageFetcher {

    private static final String TAG = "PreviewImageFetcher";

    private HashMap<String, Bitmap> mBitmaps;
    private HashMap<String, List<ImageFetcherListener>> mListeners;

    public interface ImageFetcherListener {
        public void onBitmapLoadStarted (String url);
        public void onBitmapLoadDone (String url, Bitmap loadedBitmap);
        public void onBitmapLoadFailed (String url);
        public void onBitmapLoadCancelled (String url);
    }

    public PreviewImageFetcher() {
        mBitmaps = new HashMap<String, Bitmap>();
        mListeners = new HashMap<String, List<ImageFetcherListener>>();
    }

    public Bitmap getBitmap (String url) {
        return mBitmaps.get(url);
    }

    public void loadBitmap (String url, ImageFetcherListener listener) {
        if (listener == null) {
            return;
        }
        listener.onBitmapLoadStarted(url);

        Bitmap bitmap = mBitmaps.get(url);
        if (bitmap != null) {
            listener.onBitmapLoadDone(url, bitmap);
        } else {
            loadWithImageLoader(url, listener);
        }
    }

    /***
     * 清除url对应的bitmap
     * @param url 图片地址
     */
    public void recycleBitmap (String url) {
        Bitmap bitmap = mBitmaps.get(url);
        if (bitmap != null) {
            bitmap.recycle();
        }
        mBitmaps.remove(url);
    }

    /***
     * 清除所有bitmap
     */
    public void destroy () {
        mListeners.clear();
        for (String key:mBitmaps.keySet()) {
            Bitmap bitmap = mBitmaps.get(key);
            if (bitmap != null) {
                bitmap.recycle();
				LeoLog.e("llb", bitmap.toString() + "|" +bitmap.isRecycled());
			}
        }
        mBitmaps.clear();
    }

    private void loadWithImageLoader (String url, final ImageFetcherListener listener) {
        List<ImageFetcherListener> listenerList = mListeners.get(url);
        if (listenerList == null) {
            listenerList = new ArrayList<ImageFetcherListener>();
            mListeners.put(url, listenerList);
        }
        listenerList.add(listener);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoader.getInstance().loadImage(url, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                LeoLog.d(TAG, "[onLoadingStarted] " + imageUri);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                LeoLog.d(TAG, "[onLoadingFailed] " + imageUri);
                List<ImageFetcherListener> listenerList = mListeners.remove(imageUri);
                if (listenerList == null) {
                    return;
                }
                for (ImageFetcherListener listenerInner : listenerList) {
                    listenerInner.onBitmapLoadFailed(imageUri);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                LeoLog.d(TAG, "[onLoadingComplete] " + imageUri);
                mBitmaps.put(imageUri, loadedImage);
                List<ImageFetcherListener> listenerList = mListeners.remove(imageUri);
                if (listenerList == null) {
                    return;
                }
                for (ImageFetcherListener listenerInner : listenerList) {
                    listenerInner.onBitmapLoadDone(imageUri, loadedImage);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                LeoLog.d(TAG, "[onLoadingCancelled] " + imageUri);
                List<ImageFetcherListener> listenerList = mListeners.remove(imageUri);
                if (listenerList == null) {
                    return;
                }
                for (ImageFetcherListener listenerInner : listenerList) {
                    listenerInner.onBitmapLoadCancelled(imageUri);
                }
            }
        });
    }
}
