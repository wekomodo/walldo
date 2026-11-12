package com.enigmaticdevs.wallhaven.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

enum class RefreshResult { Owned, NotOwned, Pending, Error }

sealed interface BillingEvent {
    data object PurchaseSuccess : BillingEvent
    data object PurchasePending : BillingEvent
    data class PurchaseFailed(val message: String) : BillingEvent
}

/**
 * Play is the source of truth, DataStore (via [ProEntitlementStore]) is a cache.
 * Make this an app-wide singleton and give it an application-level scope.
 */

@Inject
class BillingRepository(
    context: Context,
    private val store: ProEntitlementStore,
    private val scope: CoroutineScope,
) : PurchasesUpdatedListener {

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    private val connectMutex = Mutex()
    private val productDetails = MutableStateFlow<ProductDetails?>(null)

    /** Localized price string for the paywall, null until loaded. */
    val priceText: Flow<String?> =
        productDetails.map { it?.oneTimePurchaseOfferDetails?.formattedPrice }

    val events: SharedFlow<BillingEvent> get() = _events
    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 4)

    // ---------------------------------------------------------------- connect

    private suspend fun connect(): Boolean = connectMutex.withLock {
        if (client.isReady) return true
        suspendCancellableCoroutine { cont: CancellableContinuation<Boolean> ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) {
                        cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                    }
                }

                // Auto service reconnection is enabled, nothing to do here.
                override fun onBillingServiceDisconnected() = Unit
            })
        }
    }

    // ---------------------------------------------- restore / refresh (Step 1)

    /**
     * Asks Play what the user owns and updates the cached flag.
     * Call on app start, on foreground, and from the "Restore purchases" button.
     */
    suspend fun refresh(): RefreshResult {
        if (!connect()) return RefreshResult.Error
        loadProductDetails()

        val result = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        // Never revoke on an error. Only a successful query may change the flag.
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return RefreshResult.Error
        }

        val pro = result.purchasesList.firstOrNull { PRODUCT_ID in it.products }
        return when (pro?.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                grant(pro)
                RefreshResult.Owned
            }
            Purchase.PurchaseState.PENDING -> RefreshResult.Pending
            else -> {
                store.setPro(false)
                RefreshResult.NotOwned
            }
        }
    }

    // ------------------------------------------------------- buying (Step 2)

    private suspend fun loadProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        val result = client.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            productDetails.value = result.productDetailsList?.firstOrNull()
        }
    }

    /** Opens the Play purchase sheet. The outcome arrives via [events]. */
    fun launchPurchase(activity: Activity) {
        scope.launch {
            if (!connect()) {
                _events.emit(BillingEvent.PurchaseFailed("Google Play is unavailable"))
                return@launch
            }
            if (productDetails.value == null) loadProductDetails()
            val details = productDetails.value
            if (details == null) {
                _events.emit(BillingEvent.PurchaseFailed("Product not available"))
                return@launch
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .build()
                    )
                )
                .build()

            val result = client.launchBillingFlow(activity, flowParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _events.emit(BillingEvent.PurchaseFailed(result.debugMessage))
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        scope.launch {
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases.orEmpty()
                        .filter { PRODUCT_ID in it.products }
                        .forEach { purchase ->
                            when (purchase.purchaseState) {
                                Purchase.PurchaseState.PURCHASED -> {
                                    grant(purchase)
                                    _events.emit(BillingEvent.PurchaseSuccess)
                                }
                                Purchase.PurchaseState.PENDING ->
                                    _events.emit(BillingEvent.PurchasePending)
                                else -> Unit
                            }
                        }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> Unit
                // Play says they already own it: just sync the flag.
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refresh()
                else -> _events.emit(BillingEvent.PurchaseFailed(result.debugMessage))
            }
        }
    }

    // ------------------------------------------------------ grant + ack

    private suspend fun grant(purchase: Purchase) {
        store.setPro(true)
        // Unacknowledged purchases are refunded by Play after 3 days.
        // If this fails, the next refresh() sees isAcknowledged == false and retries.
        if (!purchase.isAcknowledged) {
            val ack = client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            )
            if (ack.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Acknowledge failed: ${ack.debugMessage}")
            }
        }
    }

    companion object {
        const val PRODUCT_ID = "pro"
        private const val TAG = "BillingRepository"
    }
}