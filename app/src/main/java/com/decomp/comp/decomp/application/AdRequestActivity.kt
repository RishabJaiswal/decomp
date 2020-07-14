package com.decomp.comp.decomp.application

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.utils.extensions.gone
import com.decomp.comp.decomp.utils.extensions.logError
import com.decomp.comp.decomp.utils.extensions.showShortToast
import com.decomp.comp.decomp.utils.extensions.visible
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.activity_ad_request.*

class AdRequestActivity : BaseActivity(), View.OnClickListener {

    private lateinit var rewardedAd: RewardedAd
    private lateinit var adLoadCallback: RewardedAdLoadCallback
    private lateinit var adShowCallback: RewardedAdCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_request)
        initAdCallbacks()
        btn_watch_ad.setOnClickListener(this)
    }

    private fun initAdCallbacks() {

        //ad loading callbacks
        adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
                if (rewardedAd.isLoaded) {
                    rewardedAd.show(this@AdRequestActivity, adShowCallback)
                }
            }

            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                logError(
                        tag = "AdRequest Error: $errorCode",
                        message = "Failed to load rewarded ad"
                )
                showAdError()
            }
        }

        //ad showing callbacks
        adShowCallback = object : RewardedAdCallback() {
            override fun onRewardedAdOpened() {
                // Ad opened.
                pb_ad_load.gone()
                btn_watch_ad.visible()
            }

            override fun onRewardedAdClosed() {
                // Ad closed.
                closeScreenWithResult(Activity.RESULT_CANCELED)
            }

            override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                // User earned reward.
                closeScreenWithResult(Activity.RESULT_OK)
            }

            override fun onRewardedAdFailedToShow(errorCode: Int) {
                // Ad failed to display.
                logError(
                        tag = "AdRequest Error: $errorCode",
                        message = "Failed to show rewarded ad"
                )
                showAdError()
            }
        }
    }

    private fun showAdError() {
        pb_ad_load.gone()
        btn_watch_ad.visible()
        showShortToast(R.string.error_ad_load)
        closeScreenWithResult(Activity.RESULT_FIRST_USER)
    }

    private fun closeScreenWithResult(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_watch_ad -> {
                //load ad
                rewardedAd = RewardedAd(this, getString(R.string.support_dev_ad_unit_id))
                rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
                pb_ad_load.visible()
                btn_watch_ad.gone()
            }
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AdRequestActivity::class.java)
        }
    }
}
