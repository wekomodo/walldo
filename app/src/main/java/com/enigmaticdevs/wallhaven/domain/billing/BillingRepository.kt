package com.enigmaticdevs.wallhaven.domain.billing

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.enigmaticdevs.wallhaven.data.billing.LocalBillingDb
import com.enigmaticdevs.wallhaven.data.billing.Sku
import com.enigmaticdevs.wallhaven.data.billing.Sku.INAPP_PRODUCTS
import com.enigmaticdevs.wallhaven.data.billing.Sku.SUBSCRIPTIONS
import com.enigmaticdevs.wallhaven.data.billing.Sku.WALLDO_PRO
import com.enigmaticdevs.wallhaven.data.billing.models.Donation
import com.enigmaticdevs.wallhaven.data.billing.models.Entitlement
import com.enigmaticdevs.wallhaven.data.billing.models.LEVEL_COFFEE
import com.enigmaticdevs.wallhaven.data.billing.models.LEVEL_FANCY_MEAL
import com.enigmaticdevs.wallhaven.data.billing.models.LEVEL_PIZZA
import com.enigmaticdevs.wallhaven.data.billing.models.LEVEL_SMOOTHIE
import com.enigmaticdevs.wallhaven.data.billing.models.WalldoPro
import com.enigmaticdevs.wallhaven.utils.livedata.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Long.min

private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L // 15 minutes
private const val LOG_TAG = "BillingRepository"
class BillingRepository(private val application: Application) :
        PurchasesUpdatedListener, BillingClientStateListener {

    // How long before the data source tries to reconnect to Google Play
    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS

    private lateinit var playStoreBillingClient: BillingClient

    private lateinit var localCacheBillingClient: LocalBillingDb

    val productsWithProductDetails = MutableLiveData<Map<String, ProductDetails>>()
    val subscriptionsWithProductDetails = MutableLiveData<Map<String,ProductDetails>>()

    private val _purchaseCompleteLiveData = MutableLiveData<Event<Purchase>>()
    val purchaseCompleteLiveData: LiveData<Event<Purchase>> = _purchaseCompleteLiveData

    private val _billingMessageLiveData = MutableLiveData<Event<String>>()
    val billingMessageLiveData: LiveData<Event<String>> = _billingMessageLiveData

    private val _billingErrorLiveData = MutableLiveData<Event<BillingResult>>()
    val billingErrorLiveData: LiveData<Event<BillingResult>> = _billingErrorLiveData

    val donationLiveData: LiveData<Donation?> by lazy {
       if(!::localCacheBillingClient.isInitialized){
           localCacheBillingClient = LocalBillingDb.getInstance(application)
       }
        localCacheBillingClient.entitlementsDao().getDonation()
    }

    val proLiveData: LiveData<WalldoPro?> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.entitlementsDao().getWalldoPro()
    }


    init {
        startDataSourceConnections()
    }

    fun startDataSourceConnections() {
        Log.d(LOG_TAG, "startDataSourceConnections")
        instantiateAndConnectToPlayBillingService()
        localCacheBillingClient = LocalBillingDb.getInstance(application)
    }

    //billing repo is singleton
    fun endDataSourceConnections() {
        playStoreBillingClient.endConnection()
        // normally you don't worry about closing a DB connection unless you have more than
        // one DB open. so no need to call 'localCacheBillingClient.close()'
        Log.d(LOG_TAG, "endDataSourceConnections")
    }

    private fun instantiateAndConnectToPlayBillingService() {
        playStoreBillingClient = BillingClient.newBuilder(application.applicationContext)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        if (!playStoreBillingClient.isReady) {
            playStoreBillingClient.startConnection(this)
        }
    }
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                queryProductDetailsAsync()
                querySubscriptionsDetailsAsync()
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                Log.d(LOG_TAG, billingResult.debugMessage)
            else ->  Log.d(LOG_TAG, billingResult.debugMessage)
        }
    }

    private fun querySubscriptionsDetailsAsync() {
            Log.d(LOG_TAG, "querySkuDetailsAsync")
            val productList = SUBSCRIPTIONS.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()
            playStoreBillingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        if (productDetailsList.isNotEmpty()) {
                            subscriptionsWithProductDetails.postValue(
                                productDetailsList.associateBy { it.productId }
                            )
                            Log.d(LOG_TAG, "subscriptionsWithProductDetails: $productDetailsList")
                        } else {
                            subscriptionsWithProductDetails.postValue(emptyMap())
                            error("querySubscriptionsDetailsAsync response was empty")
                        }
                    }
                    else -> error(billingResult.debugMessage)
                }
            }
    }

    fun querySubscriptionPurchasesAsync(restore: Boolean = false) {
        Log.d(LOG_TAG,"querySubscriptionPurchasesAsync")
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        playStoreBillingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(LOG_TAG,"Subscriptions Active: $purchasesList")
                    if (restore && purchasesList.isEmpty()) {
                        _billingMessageLiveData.postValue(Event("No subscription active"))
                    }
                    processPurchases(purchasesList.toSet())
                }
                else -> error(billingResult.debugMessage)
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(LOG_TAG, "onBillingServiceDisconnected")
        retryBillingServiceConnectionWithExponentialBackoff()
    }
    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(
            { playStoreBillingClient.startConnection(this@BillingRepository) },
            reconnectMilliseconds
        )
        reconnectMilliseconds = min(
            reconnectMilliseconds * 2,
            RECONNECT_TIMER_MAX_TIME_MILLISECONDS
        )
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        Log.d(LOG_TAG,"onPurchasesUpdated: ${billingResult.responseCode} ${billingResult.debugMessage}")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                purchases?.apply { processPurchases(this.toSet()) }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> queryPurchasesAsync()
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> querySubscriptionsDetailsAsync()
        }
    }


    fun queryPurchasesAsync(restore: Boolean = false) {
        Log.d(LOG_TAG,"queryPurchasesAsync")
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        playStoreBillingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(LOG_TAG,"InApp purchases: $purchasesList")
                    if (restore && purchasesList.isEmpty()) {
                        _billingMessageLiveData.postValue(Event("No purchases found"))
                    }
                    processPurchases(purchasesList.toSet())
                }
                else -> error(billingResult.debugMessage)
            }
        }
    }
    private fun queryProductDetailsAsync() {
        Log.d(LOG_TAG, "querySkuDetailsAsync")
        val productList = INAPP_PRODUCTS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        playStoreBillingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (productDetailsList.isNotEmpty()) {
                        productsWithProductDetails.postValue(
                            productDetailsList.associateBy { it.productId }
                        )
                        Log.d(LOG_TAG, "productDetailsList: $productDetailsList")
                    } else {
                        productsWithProductDetails.postValue(emptyMap())
                        error("queryProductDetailsAsync response was empty")
                    }
                }
                else -> error(billingResult.debugMessage)
            }
        }
    }
    private fun isSignatureValid(purchase: Purchase) = BillingSecurity.verifyPurchase(
        BillingSecurity.BASE_64_ENCODED_PUBLIC_KEY, purchase.originalJson, purchase.signature
    )

    private fun processPurchases(purchases: Set<Purchase>) =
            CoroutineScope(Job() + Dispatchers.IO).launch {
                Log.d(LOG_TAG, "processPurchases called")
                val validPurchases = HashSet<Purchase>(purchases.size)
                Log.d(LOG_TAG, "processPurchases newBatch content $purchases")
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (isSignatureValid(purchase)) {
                            validPurchases.add(purchase)
                        }
                        else {
                            _billingMessageLiveData.postValue(Event("Unable to validate purchase"))
                        }
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        Log.d(LOG_TAG, "Received a pending purchase of SKU: ${purchase.products}")
                        // handle pending purchases, e.g. confirm with users about the pending
                        // purchases, prompt them to complete it, etc.
                        _billingMessageLiveData.postValue(Event("Purchase is pending"))
                    }
                }
                val (consumables, nonConsumables) = validPurchases.partition { purchase ->
                    purchase.products.any { Sku.CONSUMABLE_PRODUCTS.contains(it) }
                }
                Log.d(LOG_TAG, "processPurchases consumables content $consumables")
                Log.d(LOG_TAG, "processPurchases non-consumables content $nonConsumables")
                handleConsumablePurchasesAsync(consumables)
                acknowledgeNonConsumablePurchasesAsync(nonConsumables)
            }

    private fun handleConsumablePurchasesAsync(consumables: List<Purchase>) {
        Log.d(LOG_TAG, "handleConsumablePurchasesAsync called")
        consumables.forEach {
            Log.d(LOG_TAG, "handleConsumablePurchasesAsync foreach it is $it")
            val params = ConsumeParams.newBuilder().setPurchaseToken(it.purchaseToken).build()
            playStoreBillingClient.consumeAsync(params) { billingResult, _ ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> disburseConsumableEntitlement(it)
                    else -> {
                        val message = "${billingResult.responseCode}: ${billingResult.debugMessage}"
                        Log.w(LOG_TAG,message)
                        _billingMessageLiveData.postValue(Event(message))
                        _billingErrorLiveData.postValue(Event(billingResult))
                    }
                }
            }
        }
    }
    private fun acknowledgeNonConsumablePurchasesAsync(nonConsumables: List<Purchase>) {
        Log.d(LOG_TAG,"acknowledgeNonConsumablePurchasesAsync")
        nonConsumables.forEach { purchase ->
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                playStoreBillingClient.acknowledgePurchase(params) { billingResult ->
                    val message = "Response code ${billingResult.responseCode}: ${billingResult.debugMessage}"
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            Log.d(LOG_TAG,message)
                            disburseNonConsumableEntitlement(purchase)
                        }
                        else -> {
                            Log.w(LOG_TAG,message)
                            _billingMessageLiveData.postValue(Event(message))
                            _billingErrorLiveData.postValue(Event(billingResult))
                        }
                    }
                }
            } else {
                disburseNonConsumableEntitlement(purchase)
            }
        }
    }

    /**
     * This is the final step, where purchases/receipts are converted to premium contents.
     * In this sample, once the entitlement is disbursed the receipt is thrown out.
     */

    private fun disburseNonConsumableEntitlement(purchase: Purchase) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            Log.d(LOG_TAG,"disburseNonConsumableEntitlement: ${purchase.products}")
            for (product in purchase.products) {
                when (product) {
                    WALLDO_PRO -> insert(WalldoPro(true))
                }
            }
        }
    private fun disburseConsumableEntitlement(purchase: Purchase) =
            CoroutineScope(Job() + Dispatchers.IO).launch {
                for (product in purchase.products) {
                    if (Sku.CONSUMABLE_PRODUCTS.contains(product)) {
                        when (product) {
                            Sku.COFFEE -> updateDonations(Donation(LEVEL_COFFEE))
                            Sku.SMOOTHIE -> updateDonations(Donation(LEVEL_SMOOTHIE))
                            Sku.PIZZA -> updateDonations(Donation(LEVEL_PIZZA))
                            Sku.FANCY_MEAL -> updateDonations(Donation(LEVEL_FANCY_MEAL))
                        }
                    }
                }
                _purchaseCompleteLiveData.postValue(Event(purchase))
            }

    @WorkerThread
    suspend fun updateDonations(donation: Donation) = withContext(Dispatchers.IO) {
        var update = donation
        donationLiveData.value?.apply {
            synchronized(this) {
                if (this != donation) { //new purchase
                    update = Donation(level + donation.level)
                }
                Log.d(LOG_TAG,"New purchase level is ${donation.level}; existing level is ${level}; " +
                        "so the final result is ${update.level}")
                localCacheBillingClient.entitlementsDao().update(update)
            }
        }
        if (donationLiveData.value == null) {
            localCacheBillingClient.entitlementsDao().insert(update)
            Log.d(LOG_TAG,"We just added from null donation with level: ${donation.level}")
        }
    }

    @WorkerThread
    private suspend fun insert(entitlement: Entitlement) = withContext(Dispatchers.IO) {
        localCacheBillingClient.entitlementsDao().insert(entitlement)
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    offerToken?.let {
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(it)
                            .build()
                    }
                )
            )
            .build()
        playStoreBillingClient.launchBillingFlow(activity, params)
    }
}

