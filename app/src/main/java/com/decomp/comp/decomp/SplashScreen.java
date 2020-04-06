package com.decomp.comp.decomp;


import android.Manifest;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.decomp.comp.decomp.features.compressing.CompressingImagesActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity implements View.OnClickListener {
    private SeekBar seekBar;

    private Intent intent;
    private LayoutInflater li;

    private boolean goingToComp, compClicked;
    private ArrayList<Image> images;
    private int progress = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        initialize();

        if (savedInstanceState != null) {
            goingToComp = savedInstanceState.getBoolean("goingToComp");
            compClicked = savedInstanceState.getBoolean("compClicked");
            if (goingToComp) {
                images = savedInstanceState.getParcelableArrayList("images");
                progress = savedInstanceState.getInt("progress");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (compClicked && goingToComp)
            showCompDialog(progress);

        //setting directory for compressed images
        if (!Environment.isExternalStorageRemovable() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //createFolder();
            checkPermissions();
        } else if (Environment.isExternalStorageRemovable()) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //createFolder();
                checkPermissions();
            } else {
                findViewById(R.id.selImgFab).setVisibility(View.INVISIBLE);
                findViewById(R.id.compGalFab).setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_changePermissions).setVisibility(View.INVISIBLE);
                Snackbar.make(findViewById(R.id.splshScrnlayout), "External media storage not found", Snackbar.LENGTH_LONG).show();
            }
        } else
            Snackbar.make(findViewById(R.id.splshScrnlayout), "There is a media storage problem", Snackbar.LENGTH_LONG).show();

        //setting decomped files count
        ((TextView) findViewById(R.id.decomped_count)).setText(String.valueOf(
                getSharedPreferences(getString(R.string.pref_user_data), MODE_PRIVATE)
                        .getLong(getString(R.string.pref_decompCount), 0)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("goingToComp", goingToComp);
        outState.putBoolean("compClicked", compClicked);
        if (goingToComp) {
            outState.putParcelableArrayList("images", images);
            outState.putInt("progress", seekBar.getProgress());
        }
    }

    private void initialize() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
        li = LayoutInflater.from(this);
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieBackground);
        lottieAnimationView.setMaxFrame(70);
        lottieAnimationView.playAnimation();

        //setting ad
        final AdView adView = new AdView(this);
        FrameLayout adViewParent = findViewById(R.id.ad_container);
        adViewParent.addView(adView);
        adView.setAdSize(getAdSize());
        adView.setAdUnitId(getString(R.string.splash_screen_ad_unit));
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                //adding adview
               /* FrameLayout adViewParent = findViewById(R.id.adViewParent);
                FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                flp.gravity = Gravity.CENTER;
                adView.setLayoutParams(flp);
                adViewParent.addView(adView);*/

            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d("Ad  failed", String.valueOf(i));
            }
        });
        //setting infoFab adding click listeners
        findViewById(R.id.mye_promo_fab).setOnClickListener(this);
        findViewById(R.id.compGalFab).setOnClickListener(this);
        findViewById(R.id.selImgFab).setOnClickListener(this);
        findViewById(R.id.btn_changePermissions).setOnClickListener(this);
        findViewById(R.id.infoFab).setOnClickListener(this);

        //setting typeface
        Typeface bold = ResourcesCompat.getFont(this, R.font.roboto_bold);
        ((TextView) findViewById(R.id.title)).setTypeface(bold);
        ((TextView) findViewById(R.id.title_comp)).setTypeface(bold);
        ((TextView) findViewById(R.id.decomped_count)).setTypeface(bold);
        ((TextView) findViewById(R.id.decomped_count_text)).setTypeface(bold);
    }

    private AdSize getAdSize() {
        // Step 3 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        //you can also pass your selected width here in dp
        int adWidth = (int) (widthPixels / density);
        //return the optimal size depends on your orientation (landscape or portrait)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createFolder();
            findViewById(R.id.selImgFab).setVisibility(View.VISIBLE);
            findViewById(R.id.compGalFab).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_changePermissions).setVisibility(View.INVISIBLE);
        } else {
            showRationale();
            findViewById(R.id.selImgFab).setVisibility(View.INVISIBLE);
            findViewById(R.id.compGalFab).setVisibility(View.INVISIBLE);
            findViewById(R.id.btn_changePermissions).setVisibility(View.VISIBLE);
        }
    }

    private void openPermissions() {
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            goingToComp = true;
        }
    }

    private void createFolder() {
        String extStrDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        extStrDir += File.separator + "decomp";
        File decompDir = new File(extStrDir);
        if (!decompDir.exists()) {
            if (decompDir.mkdir())
                Snackbar.make(findViewById(R.id.splshScrnlayout), R.string.dir_decomp_created, Snackbar.LENGTH_LONG).show();
            else
                Snackbar.make(findViewById(R.id.splshScrnlayout), R.string.storage_issue, Snackbar.LENGTH_LONG).show();
        }

        SharedPreferences dirPref = getSharedPreferences("dir", MODE_PRIVATE);
        dirPref.edit().putString("dir", extStrDir).apply();
        try {
            int deCompedImages = new File(extStrDir).listFiles().length;
            if (deCompedImages == 0)
                dirPref.edit().putLong("imgName", Long.MAX_VALUE).apply();
            else
                getSharedPreferences(getString(R.string.pref_user_data), MODE_PRIVATE).edit()
                        .putLong(getString(R.string.pref_decompCount), deCompedImages).apply();
            ((TextView) findViewById(R.id.decomped_count)).setText(String.valueOf(deCompedImages));
        } catch (Exception e) {

        }
    }

    private void checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.selImgFab).setVisibility(View.INVISIBLE);
            findViewById(R.id.compGalFab).setVisibility(View.INVISIBLE);
            findViewById(R.id.btn_changePermissions).setVisibility(View.VISIBLE);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showRationale();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        } else {
            findViewById(R.id.selImgFab).setVisibility(View.VISIBLE);
            findViewById(R.id.compGalFab).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_changePermissions).setVisibility(View.INVISIBLE);
            createFolder();
        }
    }

    private void showRationale() {
        Snackbar.make(findViewById(R.id.splshScrnlayout), "Cannot access directories. Access required " +
                "to store compressed images", Snackbar.LENGTH_LONG)
                .setAction(R.string.action_settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openPermissions();
                    }
                }).show();
    }

    private void showCompDialog(int progress) {
        int noImgs = images.size();
        String img = null;
        if (noImgs == 1)
            img = getString(R.string.image_selected);
        else if (noImgs > 1)
            img = getString(R.string.images_selected);

        View dialogView = li.inflate(R.layout.compressing_dialog, null);
        seekBar = dialogView.findViewById(R.id.seekBar);
        TextView instruction = dialogView.findViewById(R.id.instruct);
        seekBar.setProgress(progress);

        //building and showing dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this, R.style.MyAlertDialogStyle);
        builder.setTitle(images.size() + " " + img);
        instruction.setText(R.string.set_quality);
        builder.setPositiveButton(R.string.statCompressingBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                intent = new Intent(getApplicationContext(), CompressingImagesActivity.class);
                intent.putExtra("isCompressing", true);
                intent.putExtra("compFactor", seekBar.getProgress());
                intent.putParcelableArrayListExtra("images", images);
                goingToComp = false;
                compClicked = false;
                dialogInterface.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation(
                                    SplashScreen.this).toBundle());
                } else
                    startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goingToComp = false;
                compClicked = false;
                dialogInterface.cancel();
            }
        });
        AlertDialog compressingDialog = builder.create();
        compressingDialog.setCanceledOnTouchOutside(false);
        compressingDialog.setView(dialogView, 16, 16, 16, 0);
        compressingDialog.show();
    }

    private AlertDialog buildInfoDialog() {
        //alert dialog's view
        View infoDialogView = li.inflate(R.layout.info, null);
        Typeface regular = ResourcesCompat.getFont(this, R.font.roboto_regular);
        ((TextView) infoDialogView.findViewById(R.id.intro)).setTypeface(regular);
        ((TextView) infoDialogView.findViewById(R.id.i1)).setTypeface(regular);
        ((TextView) infoDialogView.findViewById(R.id.i3)).setTypeface(regular);

        //AlertDialog to show when users clicks infoFab
        AlertDialog.Builder infoBuilder = new AlertDialog.Builder(SplashScreen.this, R.style.MyAlertDialogStyle);
        infoBuilder.setTitle(R.string.app_name);
        infoBuilder.setIcon(R.mipmap.launcher);
        infoBuilder.setPositiveButton(R.string.ok, null);
        AlertDialog infoDialog = infoBuilder.create();
        infoDialog.setView(infoDialogView);
        return infoDialog;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.infoFab: {
                buildInfoDialog().show();
                break;
            }
            case R.id.selImgFab: {
                compClicked = true;
                Intent intent = new Intent(SplashScreen.this, AlbumSelectActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 100);
                startActivityForResult(intent, Constants.REQUEST_CODE);
                break;
            }
            case R.id.compGalFab: {
                Intent i = new Intent(getApplicationContext(), CompGallery.class);
                startActivity(i);
                break;
            }
            case R.id.btn_changePermissions: {
                openPermissions();
            }
            case R.id.mye_promo_fab: {
                View myeView = getLayoutInflater().inflate(R.layout.promotion, null);
                myeView.findViewById(R.id.install_mye_button).setOnClickListener(this);
                myeView.findViewById(R.id.know_more).setOnClickListener(this);
                new AlertDialog.Builder(SplashScreen.this)
                        .setView(myeView)
                        .create()
                        .show();
                break;
            }
            case R.id.install_mye_button: {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.owl.noteowl")));
                break;
            }
            case R.id.know_more: {

                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=xkYkOyni4fE&t=3s")));
                break;
            }
        }

    }
}
