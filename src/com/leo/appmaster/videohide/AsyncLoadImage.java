
package com.leo.appmaster.videohide;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.provider.MediaStore.Video;
import android.view.View;
import android.widget.ImageView;

public class AsyncLoadImage {
    private Map<String, SoftReference<Drawable>> cacheMap = new HashMap<String, SoftReference<Drawable>>();
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private Handler handler = new Handler();

    public Drawable loadImage(final ImageView imageView, final String path,
            final ImageCallback callback) {
        if (cacheMap.containsKey(path)) {
            SoftReference<Drawable> softReference = cacheMap.get(path);
            if (softReference.get() != null) {
                Drawable drawable = softReference.get();
                return drawable;
            }
        }

        executorService.submit(new Runnable() {

            @Override
            public void run() {

                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,
                        Video.Thumbnails.FULL_SCREEN_KIND);
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

            }
        });

        return null;
    }

    public interface ImageCallback {
        public void imageLoader(Drawable drawable);
    }
}
