package com.decomp.comp.decomp;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by Rishab on 20-10-2015.
 */
public class AsyncDrawable extends BitmapDrawable
{
    ImgLoadAsynTask imgAsyncTask;
    public AsyncDrawable(Resources res, Bitmap imgHolder, ImgLoadAsynTask imgLoadAsynTask)
    {
        super(res, imgHolder);
        imgAsyncTask = imgLoadAsynTask;
    }

    public ImgLoadAsynTask getImgLoadTask()
    {
        return imgAsyncTask;
    }
}