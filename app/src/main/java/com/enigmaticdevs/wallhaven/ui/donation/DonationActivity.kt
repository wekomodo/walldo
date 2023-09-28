package com.enigmaticdevs.wallhaven.ui.donation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.android.billingclient.api.ProductDetails
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.billing.Sku
import com.enigmaticdevs.wallhaven.databinding.ActivityDonationBinding
import com.enigmaticdevs.wallhaven.util.SpacingItemDecoration
import com.enigmaticdevs.wallhaven.util.livedata.observeEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DonationActivity : AppCompatActivity() ,DonationAdapter.ItemEventCallback  {
    private lateinit var binding: ActivityDonationBinding
    private val viewModel: DonationViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val donationAdapter = DonationAdapter(this)
        binding.donationRecyclerView.apply {
            adapter = donationAdapter
            layoutManager = LinearLayoutManager(this@DonationActivity).apply {
                addItemDecoration(SpacingItemDecoration(this@DonationActivity, R.dimen.keyline_7))
            }
        }

        viewModel.productDetailsLiveData.observe(this) { productDetails ->
            val sortedConsumableProductDetails = Sku.CONSUMABLE_PRODUCTS
                .mapNotNull { productDetails[it] }
                .sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros }
            donationAdapter.submitList(sortedConsumableProductDetails)
        }
        viewModel.purchaseCompleteLiveData.observeEvent(this) { showThanksDialog() }

    }

    private fun showThanksDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_thanks, null)
        val animationView = view.findViewById<LottieAnimationView>(R.id.trophy_animation_view)
        animationView.setOnClickListener { animationView.playAnimation() }
        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton(R.string.you_are_welcome) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    override fun onProductDetailsClick(productDetails: ProductDetails) {
        viewModel.makePurchase(this, productDetails)
    }
}