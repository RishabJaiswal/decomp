package com.decomp.comp.decomp


import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import com.decomp.comp.decomp.utils.invisible
import com.decomp.comp.decomp.utils.visible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_screen.*
import java.io.File
import java.util.*

/**
 * Created by Rishab on 17-10-2015.
 */
class CompGallery : AppCompatActivity(), View.OnClickListener {
    private var taskFragment: CompressTaskFragment? = null

    internal lateinit var imgAdapter: ImageAdapter
    internal lateinit var imageLruCache: ImageLruCache
    lateinit var metrics: DisplayMetrics
    private lateinit var fm: FragmentManager
    private var retainFragment: RetainFragment? = null
    private lateinit var interstitialAd: InterstitialAd
    private lateinit var adRequest: AdRequest

    public var totalCompImgs: Int = 0
    private var status: Int = 0
    private var compDir: String? = null
    internal lateinit var compImgs: Array<File>
    var isSharingOrDeleting = false
    var isDoneWithDialog = false //checks if users is sharing images

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)
        //setting transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //getWindow().setSharedElementExitTransition(new ChangeImageTransform(this, null));
            //getWindow().setAllowEnterTransitionOverlap(true);
        }
        initialize()
        if (savedInstanceState != null)
            isDoneWithDialog = savedInstanceState.getBoolean("isDoneWithDialog")

        val intent = intent
        if (intent.getBooleanExtra("isCompressing", false)) {
            taskFragment = fm.findFragmentByTag("task") as CompressTaskFragment?
            if (taskFragment == null && !isDoneWithDialog) {
                taskFragment = CompressTaskFragment.getInstance()
                taskFragment!!.setData(isDoneWithDialog)
                taskFragment!!.show(fm.beginTransaction(), "task")
            }
        } else
            isDoneWithDialog = true
    }

    override fun onResume() {
        super.onResume()
        compImgs = File(compDir).listFiles()
        Arrays.sort(compImgs)
        retainFragment = supportFragmentManager.findFragmentByTag("data") as RetainFragment?

        if (retainFragment == null) {
            retainFragment = RetainFragment()
            supportFragmentManager.beginTransaction().add(retainFragment!!, "data").commit()
        }
        isSharingOrDeleting = retainFragment!!.isSharingOrDeleting
        status = retainFragment!!.status

        if (isDoneWithDialog || taskFragment != null && taskFragment!!.compressTask.status == AsyncTask.Status.FINISHED) {
            totalCompImgs = compImgs.size
            imgAdapter = ImageAdapter(this, compImgs)
            if (isSharingOrDeleting) {
                imgAdapter.selFiles = retainFragment!!.selFiles
                imgAdapter.isChecked = retainFragment!!.isChecked
                if (retainFragment!!.status == 1)
                    delFab.invisible()
                else
                    shareFab.invisible()
                selAllCb.visibility = View.VISIBLE
                if (retainFragment!!.selFiles.size == compImgs.size)
                    selAllCb.isChecked = true
            } else if (selAllCb.visibility == View.VISIBLE) {
                if (selAllCb.isChecked)
                    selAllCb.isChecked = false
                selAllCb.visibility = View.INVISIBLE
                delFab.visible()
            }
            imgRecView.adapter = imgAdapter
        }

        if (isDoneWithDialog && compImgs.size == 0) {
            delFab.visible()
            shareFab.visible()
            noImages.visible()
        }
    }

    override fun onBackPressed() {
        if (isSharingOrDeleting) {
            stopShareNDel()
        } else if (!isDoneWithDialog) {
        } else {
            if (interstitialAd.isLoaded)
                interstitialAd.show()
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDoneWithDialog", isDoneWithDialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        retainFragment!!.isSharingOrDeleting = isSharingOrDeleting
        if (isSharingOrDeleting) {
            retainFragment!!.isChecked = imgAdapter.isChecked
            retainFragment!!.selFiles = imgAdapter.selFiles
            retainFragment!!.status = status
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return true
    }

    private fun initialize() {
        compDir = getSharedPreferences("dir", Context.MODE_PRIVATE).getString("dir", null)
        metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        fm = supportFragmentManager

        //showing ads
        val flurryExtras = Bundle(1)
        // Set an extra to enable Flurry SDK debug logs
        adRequest = AdRequest.Builder().build()
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(R.string.interstitial_adunit)
        interstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (isDoneWithDialog);
                //interstitialAd.show();
            }
        }
        interstitialAd.loadAd(adRequest)

        //toolbar and up affordance and action bar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //recyclerView, adapter and grid layout manager
        val gridManager = GridLayoutManager(this, 3)
        imgRecView.layoutManager = gridManager
        setImgRecyclerClickListener()

        //select all check box
        selAllCb.setOnClickListener(this)

        //share FAB
        shareFab.setOnClickListener(this)
        delFab.setOnClickListener(this)

        //bitmap LRU cache
        val maxSize = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        imageLruCache = ImageLruCache(maxSize / 8)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.selAllCb -> {

                val isChecked = selAllCb.isChecked
                imgAdapter.selFiles.clear()
                if (isChecked) {
                    Arrays.fill(imgAdapter.isChecked, true)
                    imgAdapter.notifyDataSetChanged()
                    imgAdapter.selFiles.addAll(Arrays.asList(*compImgs))
                } else {
                    Arrays.fill(imgAdapter.isChecked, false)
                    imgAdapter.notifyDataSetChanged()
                }
            }


            R.id.shareFab -> {
                shareOrDelImgs(R.id.shareFab)
                status = 1
            }

            R.id.delFab -> {
                shareOrDelImgs(R.id.delFab)
                status = 0
            }
        }
    }

    private fun shareOrDelImgs(id: Int) {
        if (!isSharingOrDeleting) {
            selAllCb.visibility = View.VISIBLE
            if (id == R.id.shareFab) {
                delFab.invisible()
            } else {
                shareFab.invisible()
            }
            isSharingOrDeleting = true
            imgAdapter.isChecked = BooleanArray(compImgs.size)
            imgAdapter.selFiles = ArrayList()
            imgAdapter.notifyDataSetChanged()
        } else if (id == R.id.shareFab) {
            if (imgAdapter.selFiles.size == 0) {
                Snackbar.make(coord, R.string.noImageSelected, Snackbar.LENGTH_SHORT).show()
                return
            }
            val filesToShare = ArrayList<Uri>()
            val i = Intent()
            i.action = Intent.ACTION_SEND_MULTIPLE
            i.type = "image/*"
            for (file in imgAdapter.selFiles)
                filesToShare.add(getUriFromFile(file))
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToShare)
            startActivity(Intent.createChooser(i, getString(R.string.share)))
            retainFragment!!.isSharingOrDeleting = false
        } else if (id == R.id.delFab) {
            if (imgAdapter.selFiles.size == 0) {
                Snackbar.make(coord, R.string.noImageSelected, Snackbar.LENGTH_SHORT).show()
                return
            }
            val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
            val size = imgAdapter.selFiles.size
            var imgs = "1 " + getString(R.string.image_selected)
            if (size > 1)
                imgs = size.toString() + " " + getString(R.string.images_selected)
            builder.setTitle(imgs)
            builder.setMessage(R.string.deleteImages)
            val finalImgs = imgs
            builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
                var total = size
                val progressDialog = ProgressDialog(this@CompGallery, R.style.MyAlertDialogStyle)
                progressDialog.setTitle(getString(R.string.deleting))
                progressDialog.isIndeterminate = true
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                for (file in imgAdapter.selFiles) {
                    if (file.delete())
                        total--
                }
                compImgs = File(compDir).listFiles()
                imgAdapter.compImgs = compImgs
                progressDialog.dismiss()
                if (total == 0)
                    Snackbar.make(coord, getString(R.string.deleted) + finalImgs, Snackbar.LENGTH_LONG).show()
                else
                    Snackbar.make(coord, R.string.unableToDelete, Snackbar.LENGTH_LONG).show()
                stopShareNDel()
                if (compImgs.size == 0) {
                    delFab.invisible()
                    shareFab.invisible()
                    noImages.visible()
                }
            }
            builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
            val alertDialog = builder.create()
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        }
    }

    private fun getUriFromFile(file: File): Uri {
        return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".file.provider", file)
    }

    private fun stopShareNDel() {
        isSharingOrDeleting = false
        if (selAllCb.isChecked)
            selAllCb.isChecked = false
        selAllCb.visibility = View.INVISIBLE
        imgAdapter.selFiles = null
        imgAdapter.isChecked = null
        imgAdapter.notifyDataSetChanged()
        if (delFab.visibility == View.INVISIBLE)
            delFab.visible()
        else
            shareFab.visible()
    }

    private fun setImgRecyclerClickListener() {
        //setting recycler dialogView click listener as user can select items now;
        imgRecView.addOnItemTouchListener(RecyclerViewItemClickListener(this,
                object : RecyclerViewItemClickListener.OnItemClickListener {
                    lateinit var cb: CheckBox

                    override fun onItemClick(view: View, position: Int) {
                        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f)
                        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f)
                        scaleDownX.duration = 100
                        scaleDownY.duration = 100
                        val scaleDown = AnimatorSet()
                        scaleDown.interpolator = FastOutLinearInInterpolator()
                        scaleDown.play(scaleDownX).with(scaleDownY)
                        scaleDown.start()

                        scaleDown.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animator: Animator) {

                            }

                            override fun onAnimationEnd(animator: Animator) {
                                if (isSharingOrDeleting) {
                                    cb = view.findViewById<View>(R.id.checkBox) as CheckBox
                                    if (cb.isChecked) {
                                        imgAdapter.selFiles.remove(imgAdapter.compImgs[position])
                                        imgAdapter.isChecked[position] = false
                                        if (selAllCb.isChecked)
                                            selAllCb.isChecked = false
                                    } else {
                                        imgAdapter.selFiles.add(imgAdapter.compImgs[position])
                                        imgAdapter.isChecked[position] = true
                                        if (imgAdapter.selFiles.size == compImgs.size)
                                            selAllCb.isChecked = true
                                    }
                                    cb.isChecked = !cb.isChecked
                                } else {
                                    val i = Intent(this@CompGallery, ImagePager::class.java)
                                    i.putExtra("filepath", imgAdapter.compImgs[position].absolutePath)
                                    startActivity(i)
                                    imageLruCache.remove(imgAdapter.compImgs[position].absolutePath)
                                }

                            }

                            override fun onAnimationCancel(animator: Animator) {

                            }

                            override fun onAnimationRepeat(animator: Animator) {

                            }
                        })
                    }
                }))
    }

    companion object {

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

}

