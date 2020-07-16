package com.decomp.comp.decomp;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.decomp.comp.decomp.features.home.TaskType;

import java.io.File;

/**
 * Created by Rishab on 20-10-2015.
 */

public class ImgLoadAsynTask extends AsyncTask<File, Void, Bitmap> {
    ImageView imageView;
    Context context;
    ImageLruCache imageLruCache;
    int imgWidth;
    Bitmap orgBmp;
    TaskType taskType;

    public ImgLoadAsynTask(Context cntxt, ImageView imgeView, int imgWdt) {
        imageView = imgeView;
        context = cntxt;
        if (cntxt.getClass() == CompGallery.class)
            imageLruCache = ((CompGallery) cntxt).imageLruCache;
        else if (cntxt.getClass() == ImagePager.class)
            imageLruCache = ((ImagePager) cntxt).imageLruCache;
        imgWidth = imgWdt;
    }

    public ImgLoadAsynTask(Context cntxt, ImageView imgeView, int imgWdt, TaskType taskType, ImageLruCache lruCache) {
        imageView = imgeView;
        context = cntxt;
        imageLruCache = lruCache;
        imgWidth = imgWdt;
        this.taskType = taskType;
    }

    @Override
    protected Bitmap doInBackground(File... files) {
        Bitmap bmp;
        String path = files[0].getAbsolutePath();
        if (taskType != null) {
            bmp = getThumbnail(path);
        } else {
            bmp = decodeSampledBitmapFromFile(path, imageLruCache);
        }
        try {
            if (bmp != null)
                imageLruCache.put(path, bmp);
        } catch (Exception e) {
            if (bmp == null) {
            }
        }
        orgBmp = bmp;
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bmp) {
        if (isCancelled())
            bmp = null;
        if (imageView != null && bmp != null && (this == getTask(imageView))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                imageView.setAlpha(0f);
                imageView.setImageBitmap(bmp);
                ObjectAnimator anim = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
                anim.setDuration(300);
                anim.start();
            } else
                imageView.setImageBitmap(bmp);
        }
    }

    private static ImgLoadAsynTask getTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getImgLoadTask();
            }
        }
        return null;
    }


    Bitmap getThumbnail(String filepath) {
        if (taskType == TaskType.COMPRESS_IMAGE) {
            return getImageThumbnail(filepath);
        } else if (taskType == TaskType.COMPRESS_VIDEO ||
                taskType == TaskType.RECORD_SCREEN) {
            return getVideoThumbnail(filepath);
        } else {
            return null;
        }
    }

    Bitmap getImageThumbnail(String filepath) {
        return ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(filepath),
                imgWidth,
                imgWidth);
    }

    Bitmap getVideoThumbnail(String filepath) {
        return ThumbnailUtils.createVideoThumbnail(
                filepath,
                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND
        );
    }

    private Bitmap decodeSampledBitmapFromFile(String filename, ImageLruCache cache) {
        Bitmap bmp;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, filename);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            addInBitmapOptions(options, cache);
        bmp = BitmapFactory.decodeFile(filename, options);
        return bmp;
    }

    private void addInBitmapOptions(BitmapFactory.Options options, ImageLruCache cache) {
        options.inMutable = true;
        if (cache != null) {
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);
            if (inBitmap != null)
                options.inBitmap = inBitmap;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, String filename) {
        options.inJustDecodeBounds = true;
        int reqWidth = imgWidth;
        int reqHeight = reqWidth;
        BitmapFactory.decodeFile(filename, options);

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        options.inJustDecodeBounds = false;
        return inSampleSize;
    }
}
