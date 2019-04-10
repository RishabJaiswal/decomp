package com.decomp.comp.decomp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by Rishab on 17-10-2015.
 */
public class CompressAsyncTask extends AsyncTask<File, Integer, String> {
    private TextView compSizeTv, counterTv;
    private ProgressBar pb;
    private AlertDialog dialog;
    private CompressTaskFragment taskFragment;

    private SharedPreferences dir;
    private CompressingAdapter compressingAdapter;
    //BitmapFactory.Options options;

    private byte[] compImgBytes;
    private int i = 0, compFactor;
    private long compSize = 0;
    private ByteArrayOutputStream baos;

    public CompressAsyncTask(CompressTaskFragment taskFragment,
                             AlertDialog dialog, View dialogView, int compF, CompressingAdapter cmpAdpter) {
        this.taskFragment = taskFragment;

        pb = (ProgressBar) dialogView.findViewById(R.id.progress);
        counterTv = (TextView) dialogView.findViewById(R.id.counter);
        compSizeTv = (TextView) dialogView.findViewById(R.id.compSize);
        compressingAdapter = cmpAdpter;

        dir = taskFragment.getContext().getSharedPreferences("dir", Context.MODE_PRIVATE);
        this.dialog = dialog;
        compFactor = compF;

        baos = new ByteArrayOutputStream();
        //options = new BitmapFactory.Options();
        //options.inMutable = true;
    }

    @Override
    protected void onPreExecute() {
        if (!taskFragment.isDoneWithDialog) {
            pb.setMax(taskFragment.imgs.length);
            pb.setInterpolator(new AccelerateDecelerateInterpolator());
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (getStatus().equals(Status.RUNNING) || getStatus().equals(Status.PENDING)) {
                        cancel(false);
                        if (isCancelled()) {
                            dialog.cancel();
                        }
                    }
                    taskFragment.isDoneWithDialog = true;
                }
            });
        }
    }

    @Override
    protected String doInBackground(File... imgs) {
        //long imgsName = dir.getLong("imgName", Long.MAX_VALUE);
        Bitmap img;
        for (; i < imgs.length; i++) {
            if (compressingAdapter.isCompressed[i])
                continue;
            try {
                baos.reset();
                String filePath = imgs[i].getAbsolutePath();
                img = BitmapFactory.decodeFile(filePath);
                img.compress(Bitmap.CompressFormat.JPEG, compFactor, baos);
                if (baos == null)
                    continue;
                compImgBytes = baos.toByteArray();
                if (compImgBytes.length == 0)
                    continue;

                OutputStream out1 = new BufferedOutputStream(new FileOutputStream(new File(dir.getString("dir", null) + File.separator +
                        String.valueOf(Calendar.getInstance().getTimeInMillis()) + "." + filePath.substring(filePath.lastIndexOf(".") + 1))));
                out1.write(compImgBytes);
                //if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB && ImageLruCache.canUseForInBitmap(img,options))
                //  options.inBitmap = img;
                compSize += compImgBytes.length;
                publishProgress(i);
            } catch (IOException e) {
                cancel(true);
                dialog.dismiss();
            }
        }
        return String.valueOf(imgs.length);
    }

    @Override
    protected void onProgressUpdate(Integer... value) {
        pb.incrementProgressBy(1);
        compressingAdapter.isCompressed[value[0]] = true;
        compressingAdapter.compSizes[value[0]] = compImgBytes.length;
        compressingAdapter.notifyItemChanged(value[0]);
        counterTv.setText(i + "/" + taskFragment.imgs.length);
    }

    @Override
    protected void onPostExecute(String result) {
        if (!taskFragment.isDoneWithDialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.done);

        CompGallery compGallery = ((CompGallery) taskFragment.getActivity());
        compSizeTv.setText(compGallery.getString(R.string.new_total) + " " +
                CompressTaskFragment.converter(compSize, 0));
        compGallery.compImgs = new File(dir.getString("dir", "")).listFiles();
        Arrays.sort(compGallery.compImgs);
        compGallery.totalCompImgs = compGallery.compImgs.length;
        compGallery.imgAdapter = new ImageAdapter(taskFragment.getActivity(), compGallery.compImgs);
        compGallery.imgRecycler.setAdapter(compGallery.imgAdapter);

        //sending data to analytics
       /* AnalyticsApplication application = (AnalyticsApplication) compGallery.getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Compression")
                .setAction("Compressed")
                .setValue(Long.parseLong(result))
                .build());*/
        Bundle checkinEvent = new Bundle();
        checkinEvent.putLong(FirebaseAnalytics.Param.VALUE, Long.parseLong(result));
        FirebaseAnalytics.getInstance(this.taskFragment.getContext()).logEvent("images_Compressed", checkinEvent);
    }
}
