package com.decomp.comp.decomp;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import androidx.collection.LruCache;

/**
 * Created by Rishab on 31-10-2015.
 */
public class ImageLruCache extends LruCache<String, Bitmap> {
    Set<SoftReference<Bitmap>> reusableBitmaps = null;

    public ImageLruCache(int maxSize) {
        super(maxSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            reusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return (value.getRowBytes() * value.getHeight()) / 1024;
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            reusableBitmaps.add(new SoftReference<Bitmap>(oldValue));
        }
    }

    public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bmp = null;
        if (reusableBitmaps != null && !reusableBitmaps.isEmpty()) {
            synchronized (reusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = reusableBitmaps.iterator();
                Bitmap item;
                while (iterator.hasNext()) {
                    item = iterator.next().get();
                    if (item != null && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap.
                        if (canUseForInBitmap(item, options)) {
                            bmp = item;
                            // Remove from reusable set so it can't be used again.
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        return bmp;
    }

    static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= (candidate.getRowBytes() * candidate.getHeight());
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888)
            return 4;
        else if (config == Bitmap.Config.RGB_565)
            return 2;
        else if (config == Bitmap.Config.ARGB_4444)
            return 2;
        else if (config == Bitmap.Config.ALPHA_8)
            return 1;
        return 1;
    }
}
