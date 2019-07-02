package com.decomp.comp.decomp;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.darsh.multipleimageselect.models.Image;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rishab on 06-11-2015.
 */

public class CompressTaskFragment extends DialogFragment {
    RecyclerView compressingRecycler;
    AlertDialog dialog;

    CompressAsyncTask compressTask;
    CompressingAdapter compressingAdapter;
    Intent intent;

    File[] imgs;
    boolean isDoneWithDialog;
    int compFact;
    long totalSize;
    static String[] b = {"B", "KB", "MB", "GB", "TB"};

    public static CompressTaskFragment getInstance() {
        CompressTaskFragment taskFragment = new CompressTaskFragment();
        return taskFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        intent = getActivity().getIntent();
        compFact = intent.getIntExtra("compFactor", 50);

        File file;
        ArrayList<Image> images = intent.getParcelableArrayListExtra("images");
        ArrayList<File> origFiles = new ArrayList<File>();
        imgs = new File[images.size()];
        for (Image image : images) {
            file = new File(image.path);
            origFiles.add(file);
            totalSize += file.length();
        }
        origFiles.toArray(imgs);

        //dialogView to inflate in the dialog and pass to compressAsync Task
        View dialogView = getLayoutInflater().inflate(R.layout.progress_layout, null);
        Typeface regularFont = ResourcesCompat.getFont(getActivity(), R.font.roboto_regular);
        ((TextView) dialogView.findViewById(R.id.totalSize)).setTypeface(regularFont);
        ((TextView) dialogView.findViewById(R.id.counter)).setTypeface(regularFont);
        ((TextView) dialogView.findViewById(R.id.compSize)).setTypeface(regularFont);
        ((TextView) dialogView.findViewById(R.id.compressingTxtTv)).setTypeface(regularFont);
        ((TextView) dialogView.findViewById(R.id.totalSize)).setText(getString(R.string.total) + " " + converter(totalSize, 0));

        //compressing recycler view and its adapter
        compressingRecycler = dialogView.findViewById(R.id.compressingRecView);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        compressingRecycler.setHasFixedSize(true);
        compressingRecycler.setLayoutManager(lm);
        compressingAdapter = new CompressingAdapter(getActivity(), imgs);
        compressingRecycler.setAdapter(compressingAdapter);


        //dialog to show the compression progress
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        dialog = builder.create();
        dialog.setView(dialogView, 0, 0, 0, 0);

        compressTask = new CompressAsyncTask(this, dialog, dialogView, compFact, compressingAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            compressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imgs);
        else
            compressTask.execute(imgs);

        this.setCancelable(false);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return dialog;
    }

    public void setData(boolean isDoneWithDialog) {
        this.isDoneWithDialog = isDoneWithDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        CompGallery compGallery = ((CompGallery) getActivity());
        compGallery.isDoneWithDialog = true;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    public static String converter(float mag, int unit) {
        if (mag >= 1024)
            return converter((mag / 1024), unit + 1);
        return String.format("%.1f", mag) + " " + b[unit];
    }

}
