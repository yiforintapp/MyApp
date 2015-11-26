
package com.leo.appmaster.videohide;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.provider.MediaStore.Video;
import android.view.View;

import com.leo.appmaster.ThreadManager;

public class AsyncLoadImage {
    private Map<String, SoftReference<Drawable>> cacheMap = new HashMap<String, SoftReference<Drawable>>();
    private ExecutorService executorService = ThreadManager.getAsyncExecutor();
    private Handler handler = new Handler();
    
    public synchronized void cancel() {
        executorService.shutdown();
    }

    public synchronized Drawable loadImage(final View imageView, final String path,
            final ImageCallback callback) {
        if (cacheMap.containsKey(path)) {
            SoftReference<Drawable> softReference = cacheMap.get(path);
            if (softReference != null && softReference.get() != null) {
                Drawable drawable = softReference.get();
                return drawable;
            }
        }
       if(!executorService.isShutdown()) {
           executorService.submit(new Runnable() {

               @Override
               public void run() {

                   Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,
                           Video.Thumbnails.FULL_SCREEN_KIND);
                 if(bitmap != null){
                   final Drawable drawable = new BitmapDrawable(bitmap);
                   if (drawable != null) {
                       cacheMap.put(path, new SoftReference<Drawable>(drawable));
                       handler.post(new Runnable() {

                           @Override
                           public void run() {
                               callback.imageLoader(drawable);

                           }
                       });
                   }
                 }else{
                     final Drawable drawable = null;
                     if (drawable == null) {
                         cacheMap.put(path, new SoftReference<Drawable>(drawable));
                         handler.post(new Runnable() {

                             @Override
                             public void run() {
                                 callback.imageLoader(drawable);

                             }
                         });
                     }
                 }
               }
           });

       }
        return null;
    }

    public interface ImageCallback {
        public void imageLoader(Drawable drawable);
    }
}
