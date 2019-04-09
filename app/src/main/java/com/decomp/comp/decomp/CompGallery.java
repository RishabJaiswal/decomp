package com.decomp.comp.decomp;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rishab on 17-10-2015.
 */
public class CompGallery extends AppCompatActivity implements View.OnClickListener {
    RecyclerView imgRecycler;
    FloatingActionButton shareFab, delFab;
    CheckBox selAllCb;
    Toolbar toolbar;
    CompressTaskFragment taskFragment;
    CoordinatorLayout coord;
    TextView noImgsTv;

    ImageAdapter imgAdapter;
    ImageLruCache imageLruCache;
    DisplayMetrics metrics;
    FragmentManager fm;
    RetainFragment retainFragment;
    InterstitialAd interstitialAd;
    AdRequest adRequest;

    int totalCompImgs, status;
    String compDir;
    File[] compImgs;
    boolean isSharingOrDeleting = false, isDoneWithDialog = false; //checks if users is sharing images

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        //setting transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //getWindow().setSharedElementExitTransition(new ChangeImageTransform(this, null));
            //getWindow().setAllowEnterTransitionOverlap(true);
        }
        initialize();
        if (savedInstanceState != null)
            isDoneWithDialog = savedInstanceState.getBoolean("isDoneWithDialog");

        Intent intent = getIntent();
        if (intent.getBooleanExtra("isCompressing", false)) {
            taskFragment = (CompressTaskFragment) fm.findFragmentByTag("task");
            if (taskFragment == null && !isDoneWithDialog) {
                taskFragment = CompressTaskFragment.getInstance();
                taskFragment.setData(isDoneWithDialog);
                taskFragment.show(fm.beginTransaction(), "task");
            }
        } else
            isDoneWithDialog = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        compImgs = new File(compDir).listFiles();
        Arrays.sort(compImgs);
        retainFragment = (RetainFragment) getSupportFragmentManager().findFragmentByTag("data");

        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            getSupportFragmentManager().beginTransaction().add(retainFragment, "data").commit();
        }
        isSharingOrDeleting = retainFragment.isSharingOrDeleting;
        status = retainFragment.status;

        if (isDoneWithDialog || (taskFragment != null && taskFragment.compressTask.getStatus().equals(AsyncTask.Status.FINISHED))) {
            totalCompImgs = compImgs.length;
            imgAdapter = new ImageAdapter(this, compImgs);
            if (isSharingOrDeleting) {
                imgAdapter.selFiles = retainFragment.selFiles;
                imgAdapter.isChecked = retainFragment.isChecked;
                if (retainFragment.status == 1)
                    delFab.setVisibility(View.INVISIBLE);
                else
                    shareFab.setVisibility(View.INVISIBLE);
                selAllCb.setVisibility(View.VISIBLE);
                if (retainFragment.selFiles.size() == compImgs.length)
                    selAllCb.setChecked(true);
            } else if (selAllCb.getVisibility() == View.VISIBLE) {
                if (selAllCb.isChecked())
                    selAllCb.setChecked(false);
                selAllCb.setVisibility(View.INVISIBLE);
                delFab.setVisibility(View.VISIBLE);
            }
            imgRecycler.setAdapter(imgAdapter);
        }

        if (isDoneWithDialog && compImgs.length == 0) {
            delFab.setVisibility(View.INVISIBLE);
            shareFab.setVisibility(View.INVISIBLE);
            noImgsTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (isSharingOrDeleting) {
            stopShareNDel();
        } else if (!isDoneWithDialog) {
        } else {
            if (interstitialAd.isLoaded())
                interstitialAd.show();
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDoneWithDialog", isDoneWithDialog);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        retainFragment.isSharingOrDeleting = isSharingOrDeleting;
        if (isSharingOrDeleting) {
            retainFragment.isChecked = imgAdapter.isChecked;
            retainFragment.selFiles = imgAdapter.selFiles;
            retainFragment.status = status;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    private void initialize() {
        compDir = getSharedPreferences("dir", Context.MODE_PRIVATE).getString("dir", null);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        fm = getSupportFragmentManager();
        coord = (CoordinatorLayout) findViewById(R.id.coord);
        noImgsTv = (TextView) findViewById(R.id.noImages);

        //showing ads
        Bundle flurryExtras = new Bundle(1);
        // Set an extra to enable Flurry SDK debug logs
        adRequest = new AdRequest.Builder().build();
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_adunit));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (isDoneWithDialog) ;
                //interstitialAd.show();
            }
        });
        interstitialAd.loadAd(adRequest);

        //toolbar and up affordance and action bar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //recyclerView, adapter and grid layout manager
        imgRecycler = findViewById(R.id.imgRecView);
        final GridLayoutManager gridManager = new GridLayoutManager(this, 3);
        imgRecycler.setLayoutManager(gridManager);
        //imgRecycler.addItemDecoration(new SpacesItemDecoration(CompGallery.this));
        setImgRecyclerClickListener();
        /*imgRecycler.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //imgRecycler.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int viewWidth = imgRecycler.getMeasuredWidth();
                        float cardViewWidth = (int) ((100 * (metrics.density * 160f)) / 160);
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        gridManager.setSpanCount(newSpanCount);
                        gridManager.requestLayout();
                    }
                });*/

        //select all check box
        selAllCb = findViewById(R.id.selAllCb);
        selAllCb.setOnClickListener(this);

        //share FAB
        shareFab = (FloatingActionButton) findViewById(R.id.shareFab);
        delFab = (FloatingActionButton) findViewById(R.id.delFab);
        shareFab.setOnClickListener(this);
        delFab.setOnClickListener(this);

        //bitmap LRU cache
        int maxSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
        imageLruCache = new ImageLruCache(maxSize / 8);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.selAllCb:

                final boolean isChecked = selAllCb.isChecked();
                imgAdapter.selFiles.clear();
                if (isChecked) {
                    Arrays.fill(imgAdapter.isChecked, true);
                    imgAdapter.notifyDataSetChanged();
                    imgAdapter.selFiles.addAll(Arrays.asList(compImgs));
                } else {
                    Arrays.fill(imgAdapter.isChecked, false);
                    imgAdapter.notifyDataSetChanged();
                }
                break;


            case R.id.shareFab:
                shareOrDelImgs(R.id.shareFab);
                status = 1;
                break;

            case R.id.delFab:
                shareOrDelImgs(R.id.delFab);
                status = 0;
                break;
        }
    }

    private void shareOrDelImgs(int id) {
        if (!isSharingOrDeleting) {
            selAllCb.setVisibility(View.VISIBLE);
            if (id == R.id.shareFab) {
                delFab.setVisibility(View.INVISIBLE);
            } else {
                shareFab.setVisibility(View.INVISIBLE);
            }
            isSharingOrDeleting = true;
            imgAdapter.isChecked = new boolean[compImgs.length];
            imgAdapter.selFiles = new ArrayList<>();
            imgAdapter.notifyDataSetChanged();
        } else if (id == R.id.shareFab) {
            if (imgAdapter.selFiles.size() == 0) {
                Snackbar.make(coord, R.string.noImageSelected, Snackbar.LENGTH_SHORT).show();
                return;
            }
            ArrayList<Uri> filesToShare = new ArrayList<Uri>();
            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND_MULTIPLE);
            i.setType("image/*");
            for (File file : imgAdapter.selFiles)
                filesToShare.add(Uri.fromFile(file));
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToShare);
            startActivity(Intent.createChooser(i, getString(R.string.share)));
            retainFragment.isSharingOrDeleting = false;
        } else if (id == R.id.delFab) {
            if (imgAdapter.selFiles.size() == 0) {
                Snackbar.make(coord, R.string.noImageSelected, Snackbar.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            final int size = imgAdapter.selFiles.size();
            String imgs = "1 " + getString(R.string.image_selected);
            if (size > 1)
                imgs = size + " " + getString(R.string.images_selected);
            builder.setTitle(imgs);
            builder.setMessage(R.string.deleteImages);
            final String finalImgs = imgs;
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int total = size;
                    ProgressDialog progressDialog = new ProgressDialog(CompGallery.this, R.style.MyAlertDialogStyle);
                    progressDialog.setTitle(getString(R.string.deleting));
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    for (File file : imgAdapter.selFiles) {
                        if (file.delete())
                            total--;
                    }
                    compImgs = new File(compDir).listFiles();
                    imgAdapter.compImgs = compImgs;
                    progressDialog.dismiss();
                    if (total == 0)
                        Snackbar.make(coord, getString(R.string.deleted) + finalImgs, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(coord, R.string.unableToDelete, Snackbar.LENGTH_LONG).show();
                    stopShareNDel();
                    if (compImgs.length == 0) {
                        delFab.setVisibility(View.INVISIBLE);
                        shareFab.setVisibility(View.INVISIBLE);
                        noImgsTv.setVisibility(View.VISIBLE);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private void stopShareNDel() {
        isSharingOrDeleting = false;
        if (selAllCb.isChecked())
            selAllCb.setChecked(false);
        selAllCb.setVisibility(View.INVISIBLE);
        imgAdapter.selFiles = null;
        imgAdapter.isChecked = null;
        imgAdapter.notifyDataSetChanged();
        if (delFab.getVisibility() == View.INVISIBLE)
            delFab.setVisibility(View.VISIBLE);
        else
            shareFab.setVisibility(View.VISIBLE);
    }

    private void setImgRecyclerClickListener() {
        //setting recycler dialogView click listener as user can select items now;
        imgRecycler.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    CheckBox cb;

                    @Override
                    public void onItemClick(final View view, final int position) {
                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f);
                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f);
                        scaleDownX.setDuration(100);
                        scaleDownY.setDuration(100);
                        AnimatorSet scaleDown = new AnimatorSet();
                        scaleDown.setInterpolator(new FastOutLinearInInterpolator());
                        scaleDown.play(scaleDownX).with(scaleDownY);
                        scaleDown.start();

                        scaleDown.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                if (isSharingOrDeleting) {
                                    cb = (CheckBox) view.findViewById(R.id.checkBox);
                                    if (cb.isChecked()) {
                                        imgAdapter.selFiles.remove(imgAdapter.compImgs[position]);
                                        imgAdapter.isChecked[position] = false;
                                        if (selAllCb.isChecked())
                                            selAllCb.setChecked(false);
                                    } else {
                                        imgAdapter.selFiles.add(imgAdapter.compImgs[position]);
                                        imgAdapter.isChecked[position] = true;
                                        if (imgAdapter.selFiles.size() == compImgs.length)
                                            selAllCb.setChecked(true);
                                    }
                                    cb.setChecked(!cb.isChecked());
                                } else {
                                    Intent i = new Intent(CompGallery.this, ImagePager.class);
                                    i.putExtra("filepath", imgAdapter.compImgs[position].getAbsolutePath());
                                    startActivity(i);
                                    imageLruCache.remove(imgAdapter.compImgs[position].getAbsolutePath());
                                }

                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                    }
                }));
    }

}

