package com.enigmaticdevs.wallhaven.ui.upgrade

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.databinding.ActivityUpgradeBinding
import com.enigmaticdevs.wallhaven.util.customToast
import com.enigmaticdevs.wallhaven.util.livedata.observeEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UpgradeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityUpgradeBinding
    private val billingViewModel: UpgradeViewModel by viewModels()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpgradeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAnalytics = Firebase.analytics
        billingViewModel.canPurchaseLiveData.observe(this) { canPurchase ->
            binding.upgradeGoPro.isVisible = canPurchase
        }

        billingViewModel.proLiveData.observe(this) {
            if (it?.entitled == true) {
                showThanksDialog()
            }
        }

        binding.upgradeGoPro.setOnClickListener {
            observeBillingResponse()
            billingViewModel.makePurchase(this)
        }

        binding.upgradeRestorePurchase.setOnClickListener {
            observeBillingResponse()
            billingViewModel.restorePurchase()
        }
        binding.toolbar4.setNavigationOnClickListener{
            finish()
        }
    }

    private fun observeBillingResponse() {
        billingViewModel.billingMessageLiveData.observeEvent(this) { customToast(this,it) }
        billingViewModel.billingErrorLiveData.observeEvent(this) {
            firebaseAnalytics.logEvent("billing_error") {
                param("response_code", "${it.responseCode}")
                param("debug_message", it.debugMessage)
            }
        }
    }

    private fun showThanksDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_thanks, null)
        val animationView = view.findViewById<LottieAnimationView>(R.id.trophy_animation_view)
        animationView.setOnClickListener { animationView.playAnimation() }
        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton("You're Welcome") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setOnDismissListener { finish() }
            .create()
            .show()
    }
}