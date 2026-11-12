package com.enigmaticdevs.wallhaven.ui.screens.features.pro

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.billing.BillingEvent
import com.enigmaticdevs.wallhaven.data.billing.BillingRepository
import com.enigmaticdevs.wallhaven.data.billing.RefreshResult
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Use the same annotations as your other ViewModels (SettingsViewModel uses a bare @ViewModelKey).
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class ProViewModel(
    private val billing: BillingRepository,
    flags: GlobalFlagsRepository,
) : ViewModel() {

    val isPro: StateFlow<Boolean> = flags.proStatus

    /** Localized price for the upgrade dialog, null until Play has answered. */
    val price: StateFlow<String?> = billing.priceText
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        // A successful purchase needs no message: isPro flips and the dialog
        // switches to the thank-you view by itself.
        viewModelScope.launch {
            billing.events.collect { event ->
                when (event) {
                    BillingEvent.PurchaseSuccess -> Unit
                    BillingEvent.PurchasePending ->
                        _messages.send("Purchase pending. Pro unlocks once the payment completes.")
                    is BillingEvent.PurchaseFailed ->
                        _messages.send("Purchase didn't go through. Please try again.")
                }
            }
        }
    }

    fun buy(activity: Activity) = billing.launchPurchase(activity)

    fun restore() {
        if (_isRestoring.value) return
        viewModelScope.launch {
            _isRestoring.value = true
            val message = when (billing.refresh()) {
                RefreshResult.Owned -> "Pro restored. Thank you!"
                RefreshResult.NotOwned -> "No Pro purchase found for this Google account."
                RefreshResult.Pending -> "Your purchase is still pending."
                RefreshResult.Error -> "Couldn't reach Google Play. Check your connection and try again."
            }
            _isRestoring.value = false
            _messages.send(message)
        }
    }
}