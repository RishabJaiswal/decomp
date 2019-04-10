package com.decomp.comp.decomp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rishab on 17-10-2015.
 */
public class ImageAdapter extends RecyclerView.Adapter<ThumbnailHolder> {
    File[] compImgs;
    Context cntxt;
    LayoutInflater li;
    List<File> selFiles;
    ImageLruCache imageLruCache;
    int wtHt;
    CompGallery compGallery;
    boolean isChecked[];
    int margin;

    public ImageAdapter(Context context, File[] imgs) {
        cntxt = context;
        li = LayoutInflater.from(context);
        compGallery = ((CompGallery) context);
        imageLruCache = compGallery.imageLruCache;
        compImgs = imgs;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        wtHt = displayMetrics.widthPixels / 3;
        margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, displayMetrics);
    }

    @Override
    public ThumbnailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ThumbnailHolder th = new ThumbnailHolder(li.inflate(R.layout.thumbnail, null));
        RecyclerView.LayoutParams rlp = new RecyclerView.LayoutParams(wtHt, wtHt);
        th.itemView.setLayoutParams(rlp);
        th.itemView.setPadding(margin, margin, margin, margin);
        return th;
    }

    @Override
    public void onBindViewHolder(final ThumbnailHolder holder, final int position) {
        loadBitmap(holder.thumbnail, compImgs[position]);

        //setting checkbox for selected files
        if (compGallery.isSharingOrDeleting) {
            holder.checkBox.setVisibility(View.VISIBLE);
            if (isChecked[position])
                holder.checkBox.setChecked(true);
            else
                holder.checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return compImgs.length;
    }

    @Override
    public void onViewRecycled(ThumbnailHolder holder) {
        super.onViewRecycled(holder);
        holder.checkBox.setVisibility(View.INVISIBLE);
    }

    private void loadBitmap(ImageView imageView, File file) {
        Bitmap bmp;
        synchronized (imageLruCache) {
            bmp = imageLruCache.get(file.getAbsolutePath());
        }
        if (bmp != null) {
            imageView.setImageBitmap(bmp);
            return;
        }
        ImgLoadAsynTask task = new ImgLoadAsynTask(cntxt, imageView, wtHt);
        AsyncDrawable asyncDrawable = new AsyncDrawable(cntxt.getResources(), null, task);
        imageView.setImageDrawable(asyncDrawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
        else
            task.execute(file);
    }

    private boolean isTaskCanelled(ImageView imageView) {
        if (imageView != null)
            return true;
        return true;
    }
}

class ThumbnailHolder extends RecyclerView.ViewHolder {
    ImageView thumbnail;
    CheckBox checkBox;

    public ThumbnailHolder(final View thumbnailView) {
        super(thumbnailView);
        thumbnail = thumbnailView.findViewById(R.id.thumbnail);
        checkBox = thumbnailView.findViewById(R.id.checkBox);
    }
}