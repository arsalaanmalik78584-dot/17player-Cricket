package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.*
import com.example.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

sealed interface BillingUiState {
    object Idle : BillingUiState
    object Initializing : BillingUiState
    data class Ready(val price: String = "₹49") : BillingUiState
    object Purchasing : BillingUiState
    object Pending : BillingUiState
    object Restoring : BillingUiState
    data class Success(val message: String) : BillingUiState
    data class Error(val message: String, val canRetry: Boolean = true) : BillingUiState
}

class BillingRepository(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        private const val TAG = "BillingRepository"
        const val PRODUCT_ID_PREMIUM = "premium_49"
        private const val PREFS_NAME = "17player_billing_prefs"
        private const val KEY_IS_PREMIUM_CACHED = "key_is_premium_cached"
        private const val KEY_PURCHASE_TOKEN = "key_purchase_token"

        @Volatile
        private var INSTANCE: BillingRepository? = null

        fun getInstance(context: Context): BillingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isPremiumUser = MutableStateFlow(sharedPreferences.getBoolean(KEY_IS_PREMIUM_CACHED, false))
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    private val _billingState = MutableStateFlow<BillingUiState>(BillingUiState.Idle)
    val billingState: StateFlow<BillingUiState> = _billingState.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _priceText = MutableStateFlow("₹49")
    val priceText: StateFlow<String> = _priceText.asStateFlow()

    private var billingClient: BillingClient? = null
    private var isConnecting = false
    private var connectionRetryCount = 0

    init {
        initBillingClient()
    }

    private fun initBillingClient() {
        try {
            val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(pendingPurchasesParams)
                .build()

            startBillingConnection()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing BillingClient", e)
            _billingState.value = BillingUiState.Error("Billing initialization failed: ${e.localizedMessage}")
        }
    }

    fun startBillingConnection() {
        val client = billingClient ?: return
        if (client.isReady || isConnecting) return

        isConnecting = true
        _billingState.value = BillingUiState.Initializing

        try {
            client.startConnection(this)
        } catch (e: Exception) {
            isConnecting = false
            Log.e(TAG, "Exception starting billing connection", e)
            _billingState.value = BillingUiState.Error("Unable to connect to Google Play Billing.")
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        isConnecting = false
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Google Play Billing connected successfully.")
            connectionRetryCount = 0
            // Query products & check existing purchases
            queryProductDetails()
            queryExistingPurchases()
        } else {
            Log.w(TAG, "Billing setup failed with responseCode: ${billingResult.responseCode}, debugMessage: ${billingResult.debugMessage}")
            val errorMsg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
            _billingState.value = BillingUiState.Error(errorMsg)
        }
    }

    override fun onBillingServiceDisconnected() {
        isConnecting = false
        Log.w(TAG, "Billing service disconnected.")
        if (connectionRetryCount < 3) {
            connectionRetryCount++
            coroutineScope.launch {
                delay(1000L * connectionRetryCount)
                startBillingConnection()
            }
        } else {
            _billingState.value = BillingUiState.Error("Google Play Billing connection lost. Tap to retry.")
        }
    }

    /**
     * Queries the ₹49 one-time product from Google Play.
     */
    fun queryProductDetails() {
        val client = billingClient
        if (client == null || !client.isReady) {
            startBillingConnection()
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PREMIUM)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumProduct = productDetailsList.firstOrNull { it.productId == PRODUCT_ID_PREMIUM }
                if (premiumProduct != null) {
                    _productDetails.value = premiumProduct
                    val formattedPrice = premiumProduct.oneTimePurchaseOfferDetails?.formattedPrice ?: "₹49"
                    _priceText.value = formattedPrice
                    _billingState.value = BillingUiState.Ready(formattedPrice)
                    Log.d(TAG, "Loaded premium product: $formattedPrice")
                } else {
                    Log.w(TAG, "Product '$PRODUCT_ID_PREMIUM' not found in store catalog, using default config")
                    _priceText.value = "₹49"
                    _billingState.value = BillingUiState.Ready("₹49")
                }
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
                _billingState.value = BillingUiState.Ready("₹49")
            }
        }
    }

    /**
     * Launches the Google Play billing flow for the ₹49 Premium purchase.
     * In DEBUG builds, if Google Play Billing is unavailable (e.g. AI Studio browser preview),
     * it simulates entitlement for preview testing without claiming real payment.
     * In RELEASE builds, strictly requires verified Google Play Billing connection and catalog.
     */
    fun launchPurchaseFlow(activity: Activity) {
        if (BuildConfig.DEBUG && (billingClient == null || !billingClient!!.isReady || _productDetails.value == null)) {
            Log.i(TAG, "Debug test mode: Simulating premium preview entitlement without real payment.")
            debugSimulatePurchaseSuccess()
            return
        }

        val client = billingClient
        if (client == null || !client.isReady) {
            _billingState.value = BillingUiState.Error("Google Play Billing is not ready. Reconnecting...")
            startBillingConnection()
            return
        }

        val product = _productDetails.value
        if (product == null) {
            // Re-query and retry
            _billingState.value = BillingUiState.Initializing
            queryProductDetails()
            _billingState.value = BillingUiState.Error("Product details are loading. Please tap 'GET PREMIUM — ₹49' again.")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _billingState.value = BillingUiState.Purchasing
        val billingResult = client.launchBillingFlow(activity, billingFlowParams)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Launch billing flow failed: ${billingResult.debugMessage}")
            val msg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
            _billingState.value = BillingUiState.Error(msg)
        }
    }

    /**
     * PurchasesUpdatedListener callback
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null && purchases.isNotEmpty()) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else {
                    _billingState.value = BillingUiState.Ready(_priceText.value)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "User canceled the purchase.")
                _billingState.value = BillingUiState.Ready(_priceText.value)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.i(TAG, "Item is already owned. Restoring entitlement...")
                queryExistingPurchases()
            }
            else -> {
                val errorMsg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
                Log.e(TAG, "Purchase failed: $errorMsg")
                _billingState.value = BillingUiState.Error(errorMsg)
            }
        }
    }

    /**
     * Processes and acknowledges a verified purchase.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.products.contains(PRODUCT_ID_PREMIUM)) {
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    } else {
                        // Already acknowledged and purchased
                        grantPremiumEntitlement(purchase.purchaseToken)
                        _billingState.value = BillingUiState.Success("Premium unlocked! Welcome to 17Player Cricket.")
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    Log.i(TAG, "Purchase is pending confirmation from Google Play.")
                    _billingState.value = BillingUiState.Pending
                }
                else -> {
                    _billingState.value = BillingUiState.Error("Purchase state unrecognized: ${purchase.purchaseState}")
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val client = billingClient ?: return
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        client.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully.")
                grantPremiumEntitlement(purchase.purchaseToken)
                _billingState.value = BillingUiState.Success("₹49 Premium Membership activated successfully!")
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                _billingState.value = BillingUiState.Error("Acknowledgement failed: ${billingResult.debugMessage}. Tap Restore Purchase to sync.")
            }
        }
    }

    /**
     * Restore existing purchases (queried from Google Play).
     */
    fun restorePurchases(onComplete: ((Boolean, String) -> Unit)? = null) {
        val client = billingClient
        if (client == null || !client.isReady) {
            _billingState.value = BillingUiState.Restoring
            startBillingConnection()
            onComplete?.invoke(false, "Connecting to Google Play Billing... please try again in a moment.")
            return
        }

        _billingState.value = BillingUiState.Restoring

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumPurchase = purchasesList.firstOrNull {
                    it.products.contains(PRODUCT_ID_PREMIUM) && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (premiumPurchase != null) {
                    if (!premiumPurchase.isAcknowledged) {
                        acknowledgePurchase(premiumPurchase)
                    } else {
                        grantPremiumEntitlement(premiumPurchase.purchaseToken)
                        _billingState.value = BillingUiState.Success("Premium restored successfully!")
                    }
                    onComplete?.invoke(true, "Premium restored successfully!")
                } else {
                    // Check if pending
                    val pendingPurchase = purchasesList.firstOrNull {
                        it.products.contains(PRODUCT_ID_PREMIUM) && it.purchaseState == Purchase.PurchaseState.PENDING
                    }
                    if (pendingPurchase != null) {
                        _billingState.value = BillingUiState.Pending
                        onComplete?.invoke(false, "Purchase is still pending confirmation by Google Play.")
                    } else {
                        // Revoke entitlement if Play store confirms no active purchase
                        revokePremiumEntitlement()
                        _billingState.value = BillingUiState.Ready(_priceText.value)
                        onComplete?.invoke(false, "No Premium purchase found.")
                    }
                }
            } else {
                val msg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
                _billingState.value = BillingUiState.Error(msg)
                onComplete?.invoke(false, msg)
            }
        }
    }

    /**
     * Silent verification of purchases on launch or activity resume.
     */
    fun queryExistingPurchases() {
        val client = billingClient
        if (client == null || !client.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumPurchase = purchasesList.firstOrNull {
                    it.products.contains(PRODUCT_ID_PREMIUM) && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (premiumPurchase != null) {
                    if (!premiumPurchase.isAcknowledged) {
                        acknowledgePurchase(premiumPurchase)
                    } else {
                        grantPremiumEntitlement(premiumPurchase.purchaseToken)
                    }
                } else {
                    // In production, if Google Play confirms no purchase, update local state
                    val hasCached = sharedPreferences.getBoolean(KEY_IS_PREMIUM_CACHED, false)
                    if (hasCached && !BuildConfig.DEBUG) {
                        // Play store confirmed not owned
                        revokePremiumEntitlement()
                    }
                }
            }
        }
    }

    private fun grantPremiumEntitlement(token: String) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_PREMIUM_CACHED, true)
            .putString(KEY_PURCHASE_TOKEN, token)
            .apply()
        _isPremiumUser.value = true
    }

    private fun revokePremiumEntitlement() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_PREMIUM_CACHED, false)
            .remove(KEY_PURCHASE_TOKEN)
            .apply()
        _isPremiumUser.value = false
    }

    /**
     * Development/Debug-only simulator for local verification during unit testing/emulator sessions.
     * Guarded strictly by BuildConfig.DEBUG.
     */
    fun debugSimulatePurchaseSuccess() {
        if (BuildConfig.DEBUG) {
            grantPremiumEntitlement("debug_simulated_token_${System.currentTimeMillis()}")
            _billingState.value = BillingUiState.Success("Debug: Premium activated for testing.")
        }
    }

    fun debugResetPremium() {
        if (BuildConfig.DEBUG) {
            revokePremiumEntitlement()
            _billingState.value = BillingUiState.Ready(_priceText.value)
        }
    }

    fun resetBillingState() {
        _billingState.value = BillingUiState.Ready(_priceText.value)
    }

    private fun mapBillingResponseToMessage(responseCode: Int, debugMessage: String?): String {
        return when (responseCode) {
            BillingClient.BillingResponseCode.USER_CANCELED -> "Purchase was cancelled."
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Google Play Billing is unavailable on this device."
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Google Play Billing service is currently unavailable. Please try again."
            BillingClient.BillingResponseCode.NETWORK_ERROR -> "Network error. Please check your internet connection and retry."
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "Premium ₹49 product is currently unavailable in the store."
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "You already own Premium ₹49 access. Restoring entitlement..."
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "No Premium purchase found."
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Google Play configuration issue (${debugMessage ?: "Unknown"})."
            else -> "Google Play error ($responseCode): ${debugMessage ?: "Unable to complete transaction"}"
        }
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}
