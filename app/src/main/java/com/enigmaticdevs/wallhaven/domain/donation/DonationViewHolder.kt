package com.enigmaticdevs.wallhaven.domain.donation

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.ui.donation.DonationAdapter

class DonationViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {



    fun bind(
        productDetails: ProductDetails,
        callback: DonationAdapter.ItemEventCallback
    ) {
        with(itemView) {
            val title = itemView.findViewById<TextView>(R.id.sku_title_text_view)
            title.text = productDetails.title.dropLastWhile { it != '(' }.dropLast(1)
            val description = itemView.findViewById<TextView>(R.id.sku_description_text_view)
            description.text = productDetails.description
            val price = itemView.findViewById<TextView>(R.id.sku_price_text_view)
            price.text = productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?:"Unavailable"
            setOnClickListener { callback.onProductDetailsClick(productDetails) }
        }
    }
}