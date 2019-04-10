package com.decomp.comp.decomp;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public class ImagePager extends AppCompatActivity implements View.OnClickListener {
    ImageView imgView;
    FloatingActionButton delFab, shareFab, rotateRightFab;

    ImageLruCache imageLruCache;
    ImgLoadAsynTask task;

    File file;
    String filepath;
    Bitmap bitmapOrg;
    FileOutputStream imgStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_pager);
        initialize();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    private void initialize() {
        if (Build.VERSION.SDK_INT < 16)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        imgView = (ImageView) findViewById(R.id.imgView);
        filepath = getIntent().getStringExtra("filepath");

        int maxSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
        imageLruCache = new ImageLruCache(maxSize / 8);
        loadBitmap();


        //ALl FABs
        delFab = (FloatingActionButton) findViewById(R.id.delFab);
        shareFab = (FloatingActionButton) findViewById(R.id.shareFab);
        rotateRightFab = (FloatingActionButton) findViewById(R.id.rotateFab);
        delFab.setOnClickListener(this);
        shareFab.setOnClickListener(this);
        rotateRightFab.setOnClickListener(this);
    }

    private void loadBitmap() {
        file = new File(filepath);
        synchronized (imageLruCache) {
            bitmapOrg = imageLruCache.get(file.getAbsolutePath());
        }
        if (bitmapOrg != null) {
            imgView.setImageBitmap(bitmapOrg);
            return;
        }
        task = new ImgLoadAsynTask(this, imgView, 300);
        AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), null, task);
        imgView.setImageDrawable(asyncDrawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
        else
            task.execute(file);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delFab:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                builder.setTitle("Delete");
                builder.setMessage("Delete this image?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (new File(filepath).delete()) {
                            dialogInterface.dismiss();
                            finish();
                        } else {
                            dialogInterface.dismiss();
                            Snackbar.make(findViewById(R.id.imageViewer), "Unable to delete file", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                break;

            case R.id.shareFab:
                Uri fileUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".file.provider",
                        new File(filepath));
                Intent i = new Intent();
                i.setAction(Intent.ACTION_SEND);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_STREAM, fileUri);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(i, "Share"));

                //log share event
                FirebaseAnalytics.getInstance(this).logEvent("images_being_shared", null);
                break;

            case R.id.rotateFab:
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                if (bitmapOrg == null)
                    bitmapOrg = task.orgBmp;
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg, bitmapOrg.getWidth(), bitmapOrg.getHeight(), true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                imgView.setImageBitmap(rotatedBitmap);
                bitmapOrg = rotatedBitmap;
                try {
                    imgStream = new FileOutputStream(filepath, false);
                    bitmapOrg.compress(Bitmap.CompressFormat.PNG, 100, imgStream);
                } catch (FileNotFoundException e) {
                }

                break;
        }
    }
}
