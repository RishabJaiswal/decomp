package com.decomp.comp.decomp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

/**
 * Created by Rishab on 22-10-2015.
 */
public class CompressingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    File[] orgImgs;
    Context cntxt;
    String kb = " kb";
    private File currFile;
    boolean isCompressed[];
    long[] compSizes;
    private ImageLruCache imageLruCache;
    private CompGallery compGallery;
    private int wtHt;
    private long orgSize;
    private AdRequest adRequest;
    private final int AD_THRESHOLD = 5;
    private final int AD_TYPE = 1;
    private final int NO_AD_TYPE = 0;

    public CompressingAdapter(Context context, File[] orgImgss)
    {
        orgImgs = orgImgss;
        cntxt = context;
        compGallery = ((CompGallery) cntxt);
        imageLruCache = compGallery.imageLruCache;
        isCompressed = new boolean[orgImgs.length];
        compSizes = new long[orgImgs.length];
        /*wtHt = (int) ((80 * (compGallery.metrics.density * 160f)) / 160);*/
        wtHt = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, compGallery.metrics);
        //ad request;
        adRequest = new AdRequest.Builder().build();

        //updating decomped images values
        String prefKey = compGallery.getResources().getString(R.string.pref_decompCount);
        String preferenceFileName = compGallery.getResources().getString(R.string.pref_user_data);
        SharedPreferences sharedPreferences = compGallery.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putLong(prefKey, sharedPreferences.getLong(prefKey, 0) + orgImgss.length)
                .apply();
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position != 0 && (position + 1) % (AD_THRESHOLD + 1) == 0)
            return AD_TYPE;
        return NO_AD_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.LayoutParams rlp = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
        if (viewType == AD_TYPE)
        {
            View view = LayoutInflater.from(cntxt).inflate(R.layout.compress_dialog_native_ad, null, false);
            view.setLayoutParams(rlp);
            return new AdHolder(view);
        }
        View view = LayoutInflater.from(cntxt).inflate(R.layout.compressing_view, null, false);
        view.setLayoutParams(rlp);
        return new ItemHolder(view, cntxt);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHoldolder, int pos)
    {
        if (viewHoldolder.getItemViewType() == NO_AD_TYPE)
        {
            ItemHolder holder = (ItemHolder) viewHoldolder;
            int position = pos - (pos / (AD_THRESHOLD + 1));
            currFile = orgImgs[position];
            loadBitmap(holder.thumbnail, currFile);
            orgSize = currFile.length();
            holder.fileNameTv.setText(currFile.getName());
            holder.origSizeTv.setText(cntxt.getString(R.string.original) + " " +
                    CompressTaskFragment.converter(orgSize, 0));
            if (isCompressed[position])
            {
                holder.compPb.setVisibility(View.GONE);
                if (orgSize > compSizes[position])
                    holder.compStatusTv.setText(R.string.compressed);
                else if (orgSize < compSizes[position])
                    holder.compStatusTv.setText(R.string.enhanced);
                else
                    holder.compStatusTv.setText(R.string.no_change);
                holder.compSizeTv.setText(cntxt.getString(R.string.newText) + " " +
                        CompressTaskFragment.converter(compSizes[position], 0));
                holder.done.setVisibility(View.VISIBLE);
                holder.done.playAnimation();
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return orgImgs.length + orgImgs.length / AD_THRESHOLD;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder)
    {
        if (viewHolder.getItemViewType() == NO_AD_TYPE)
        {
            ItemHolder holder = (ItemHolder) viewHolder;
            holder.compPb.setVisibility(View.VISIBLE);
            holder.compStatusTv.setText(R.string.compressing);
            holder.compSizeTv.setText(R.string.newText);
            holder.done.setVisibility(View.INVISIBLE);
        }
    }

    private void loadBitmap(ImageView imageView, File file)
    {
        Bitmap bmp;
        synchronized (imageLruCache)
        {
            bmp = imageLruCache.get(file.getAbsolutePath());
        }
        if (bmp != null)
        {
            imageView.setImageBitmap(bmp);
            return;
        }
        ImgLoadAsynTask task = new ImgLoadAsynTask(cntxt, imageView, wtHt);
        AsyncDrawable asyncDrawable = new AsyncDrawable(cntxt.getResources(), null, task);
        //task.execute(file);
        imageView.setImageDrawable(asyncDrawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
        else
            task.execute(file);

    }

    //native add
    private class AdHolder extends RecyclerView.ViewHolder
    {
        AdView adView;

        AdHolder(View itemVIew)
        {
            super(itemVIew);
            adView = itemVIew.findViewById(R.id.nativeAdView);
            adView.loadAd(adRequest);
        }
    }
}

class ItemHolder extends RecyclerView.ViewHolder
{
    ImageView thumbnail;
    LottieAnimationView done;
    TextView fileNameTv, compStatusTv, origSizeTv, compSizeTv;
    ProgressBar compPb;

    ItemHolder(View itemView, Context cntxt)
    {
        super(itemView);
        thumbnail = itemView.findViewById(R.id.thumbnail);
        done = itemView.findViewById(R.id.doneIv);
        fileNameTv = itemView.findViewById(R.id.originalName);
        compStatusTv = itemView.findViewById(R.id.compressingTv);
        origSizeTv = itemView.findViewById(R.id.original);
        compSizeTv = itemView.findViewById(R.id.compressed);
        compPb = itemView.findViewById(R.id.compressingPb);
        //setting type face
        Typeface regular = ResourcesCompat.getFont(cntxt, R.font.roboto_regular);
        fileNameTv.setTypeface(ResourcesCompat.getFont(cntxt, R.font.roboto_bold));
        compStatusTv.setTypeface(ResourcesCompat.getFont(cntxt, R.font.roboto_light));
        origSizeTv.setTypeface(regular);
        compSizeTv.setTypeface(regular);
    }
}
