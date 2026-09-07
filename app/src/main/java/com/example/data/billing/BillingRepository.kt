package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.*
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

        /**
         * The canonical Google Play In-App Product ID for the ₹49 one-time purchase.
         * Must match the Product ID configured and activated in Google Play Console.
         */
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
    private val connectionWaiters = mutableListOf<() -> Unit>()

    init {
        Log.d(TAG, "Initializing BillingRepository. Initial cached premium status: ${_isPremiumUser.value}")
        initBillingClient()
    }

    private fun initBillingClient() {
        try {
            Log.d(TAG, "Creating BillingClient with PendingPurchases support for one-time INAPP products...")
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

    /**
     * Connects to the Google Play Billing Service.
     */
    fun startBillingConnection(onConnected: (() -> Unit)? = null) {
        val client = billingClient ?: run {
            Log.e(TAG, "startBillingConnection called but billingClient is null.")
            return
        }

        if (client.isReady) {
            Log.d(TAG, "BillingClient is already ready and connected.")
            onConnected?.invoke()
            return
        }

        if (onConnected != null) {
            synchronized(connectionWaiters) {
                connectionWaiters.add(onConnected)
            }
        }

        if (isConnecting) {
            Log.d(TAG, "BillingClient connection already in progress. Queued callback.")
            return
        }

        isConnecting = true
        _billingState.value = BillingUiState.Initializing
        Log.d(TAG, "Starting Google Play BillingClient connection...")

        try {
            client.startConnection(this)
        } catch (e: Exception) {
            isConnecting = false
            Log.e(TAG, "Exception starting billing connection", e)
            _billingState.value = BillingUiState.Error("Unable to connect to Google Play Billing: ${e.localizedMessage}")
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        isConnecting = false
        Log.d(TAG, "onBillingSetupFinished: ResponseCode=${billingResult.responseCode} (${billingResponseCodeToString(billingResult.responseCode)}), DebugMessage=${billingResult.debugMessage}")

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i(TAG, "Google Play Billing connected successfully. Ready to query products and purchases.")
            connectionRetryCount = 0

            // Query product details and existing purchases
            queryProductDetails()
            queryExistingPurchases()

            // Run any queued connection callbacks
            val callbacks = synchronized(connectionWaiters) {
                val list = ArrayList(connectionWaiters)
                connectionWaiters.clear()
                list
            }
            callbacks.forEach { it.invoke() }
        } else {
            val errorMsg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
            Log.e(TAG, "Billing setup failed: [Code: ${billingResult.responseCode}] $errorMsg")
            _billingState.value = BillingUiState.Error(errorMsg)

            synchronized(connectionWaiters) {
                connectionWaiters.clear()
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        isConnecting = false
        Log.w(TAG, "Billing service disconnected. Reconnect attempt ${connectionRetryCount + 1}/3...")
        if (connectionRetryCount < 3) {
            connectionRetryCount++
            coroutineScope.launch {
                delay(1500L * connectionRetryCount)
                startBillingConnection()
            }
        } else {
            Log.e(TAG, "Exceeded maximum connection retry attempts to Google Play Billing.")
            _billingState.value = BillingUiState.Error("Google Play Billing connection lost. Please check Google Play Services and tap retry.")
        }
    }

    /**
     * Queries the ₹49 one-time INAPP product from Google Play Console.
     */
    fun queryProductDetails(onProductReady: ((ProductDetails?) -> Unit)? = null) {
        val client = billingClient
        if (client == null || !client.isReady) {
            Log.d(TAG, "queryProductDetails: BillingClient not ready. Initiating connection before query...")
            startBillingConnection {
                queryProductDetails(onProductReady)
            }
            return
        }

        Log.d(TAG, "Querying ProductDetails for Product ID: '$PRODUCT_ID_PREMIUM' (ProductType: INAPP)...")

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
            Log.d(TAG, "queryProductDetailsAsync finished: ResponseCode=${billingResult.responseCode} (${billingResponseCodeToString(billingResult.responseCode)}), DebugMessage=${billingResult.debugMessage}, Found=${productDetailsList.size} products")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumProduct = productDetailsList.firstOrNull { it.productId == PRODUCT_ID_PREMIUM }
                if (premiumProduct != null) {
                    _productDetails.value = premiumProduct
                    val formattedPrice = premiumProduct.oneTimePurchaseOfferDetails?.formattedPrice ?: "₹49"
                    _priceText.value = formattedPrice
                    _billingState.value = BillingUiState.Ready(formattedPrice)
                    Log.i(TAG, "Product '$PRODUCT_ID_PREMIUM' found and ready. Formatted Price: $formattedPrice")
                    onProductReady?.invoke(premiumProduct)
                } else {
                    _productDetails.value = null
                    val msg = "Product '$PRODUCT_ID_PREMIUM' was not found in Google Play Console. Verify that in-app product '$PRODUCT_ID_PREMIUM' is Active in Play Console for package '${context.packageName}'."
                    Log.e(TAG, msg)
                    _billingState.value = BillingUiState.Error("In-app product '$PRODUCT_ID_PREMIUM' is not available in Google Play Console yet.")
                    onProductReady?.invoke(null)
                }
            } else {
                _productDetails.value = null
                val errorMsg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
                Log.e(TAG, "Failed to query ProductDetails for '$PRODUCT_ID_PREMIUM': [Code: ${billingResult.responseCode}] $errorMsg")
                _billingState.value = BillingUiState.Error(errorMsg)
                onProductReady?.invoke(null)
            }
        }
    }

    /**
     * Launches the real Google Play billing flow for the ₹49 Premium one-time purchase.
     * Guaranteed to launch the native Google Play purchase bottom sheet on real devices.
     */
    fun launchPurchaseFlow(activity: Activity) {
        Log.i(TAG, "launchPurchaseFlow: User tapped GET PREMIUM button for product '$PRODUCT_ID_PREMIUM'")

        val client = billingClient
        if (client == null || !client.isReady) {
            Log.w(TAG, "launchPurchaseFlow: BillingClient is not connected yet. Connecting now and will launch upon connection...")
            _billingState.value = BillingUiState.Purchasing
            startBillingConnection {
                launchPurchaseFlow(activity)
            }
            return
        }

        val cachedProduct = _productDetails.value
        if (cachedProduct != null) {
            executeBillingFlow(activity, client, cachedProduct)
        } else {
            Log.i(TAG, "launchPurchaseFlow: Product details not cached yet. Querying Google Play store now...")
            _billingState.value = BillingUiState.Purchasing
            queryProductDetails { product ->
                if (product != null) {
                    executeBillingFlow(activity, client, product)
                } else {
                    Log.e(TAG, "launchPurchaseFlow aborted: Product details for '$PRODUCT_ID_PREMIUM' could not be retrieved from Google Play.")
                }
            }
        }
    }

    private fun executeBillingFlow(activity: Activity, client: BillingClient, product: ProductDetails) {
        try {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            _billingState.value = BillingUiState.Purchasing
            Log.i(TAG, "Invoking BillingClient.launchBillingFlow for Product ID '${product.productId}'...")

            activity.runOnUiThread {
                val billingResult = client.launchBillingFlow(activity, billingFlowParams)
                Log.i(TAG, "launchBillingFlow response: ResponseCode=${billingResult.responseCode} (${billingResponseCodeToString(billingResult.responseCode)}), DebugMessage=${billingResult.debugMessage}")

                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        Log.i(TAG, "Google Play purchase bottom sheet launched successfully.")
                        // Remain in Purchasing state until onPurchasesUpdated is called
                    }
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        Log.i(TAG, "launchBillingFlow: Item '$PRODUCT_ID_PREMIUM' is already owned. Restoring entitlement...")
                        restorePurchases()
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        Log.i(TAG, "launchBillingFlow: User canceled before launch completed.")
                        _billingState.value = BillingUiState.Ready(_priceText.value)
                    }
                    else -> {
                        val errorMsg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
                        Log.e(TAG, "launchBillingFlow failed: [Code: ${billingResult.responseCode}] $errorMsg")
                        _billingState.value = BillingUiState.Error(errorMsg)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during launchBillingFlow", e)
            _billingState.value = BillingUiState.Error("Failed to open Google Play purchase screen: ${e.localizedMessage}")
        }
    }

    /**
     * PurchasesUpdatedListener callback invoked by Google Play when a purchase flow completes.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        Log.i(TAG, "onPurchasesUpdated callback received: ResponseCode=${billingResult.responseCode} (${billingResponseCodeToString(billingResult.responseCode)}), DebugMessage=${billingResult.debugMessage}, PurchasesCount=${purchases?.size ?: 0}")

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else {
                    Log.w(TAG, "onPurchasesUpdated: ResponseCode is OK but purchases list is empty.")
                    _billingState.value = BillingUiState.Ready(_priceText.value)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "onPurchasesUpdated: User canceled the Google Play purchase.")
                _billingState.value = BillingUiState.Ready(_priceText.value)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.i(TAG, "onPurchasesUpdated: Item is already owned. Restoring entitlement...")
                restorePurchases()
            }
            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                Log.e(TAG, "onPurchasesUpdated: Network error during transaction.")
                _billingState.value = BillingUiState.Error("Network error. Please check your internet connection and try again.")
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Log.e(TAG, "onPurchasesUpdated: Google Play Billing service unavailable.")
                _billingState.value = BillingUiState.Error("Google Play service is temporarily unavailable. Please try again in a few moments.")
            }
            else -> {
                val errorMsg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
                Log.e(TAG, "onPurchasesUpdated: Purchase failed: [Code: ${billingResult.responseCode}] $errorMsg")
                _billingState.value = BillingUiState.Error(errorMsg)
            }
        }
    }

    /**
     * Processes and acknowledges a verified purchase from Google Play.
     */
    private fun handlePurchase(purchase: Purchase) {
        Log.d(TAG, "handlePurchase: Products=${purchase.products}, PurchaseState=${purchase.purchaseState}, OrderId=${purchase.orderId}, IsAcknowledged=${purchase.isAcknowledged}")

        if (purchase.products.contains(PRODUCT_ID_PREMIUM)) {
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    if (!purchase.isAcknowledged) {
                        Log.i(TAG, "Purchase '${purchase.orderId}' confirmed but not yet acknowledged. Acknowledging with Google Play...")
                        acknowledgePurchase(purchase)
                    } else {
                        Log.i(TAG, "Purchase '${purchase.orderId}' is already acknowledged. Granting Premium entitlement.")
                        grantPremiumEntitlement(purchase.purchaseToken)
                        _billingState.value = BillingUiState.Success("Premium unlocked! Welcome to 17Player Cricket.")
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    Log.i(TAG, "Purchase '${purchase.orderId}' is PENDING confirmation from Google Play (e.g. slow bank authorization or cash payment).")
                    _billingState.value = BillingUiState.Pending
                }
                else -> {
                    Log.w(TAG, "handlePurchase: Unrecognized purchase state: ${purchase.purchaseState}")
                    _billingState.value = BillingUiState.Error("Purchase state unrecognized: ${purchase.purchaseState}")
                }
            }
        } else {
            Log.w(TAG, "handlePurchase: Purchase does not contain expected product '$PRODUCT_ID_PREMIUM'. Contained: ${purchase.products}")
        }
    }

    /**
     * Acknowledges the purchase with Google Play to finalize the one-time entitlement.
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val client = billingClient ?: run {
            Log.e(TAG, "acknowledgePurchase: billingClient is null.")
            return
        }

        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        Log.d(TAG, "Sending acknowledgePurchase request to Google Play...")
        client.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            Log.i(TAG, "acknowledgePurchase callback: ResponseCode=${billingResult.responseCode} (${billingResponseCodeToString(billingResult.responseCode)}), DebugMessage=${billingResult.debugMessage}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "Purchase successfully acknowledged with Google Play. Granting permanent entitlement.")
                grantPremiumEntitlement(purchase.purchaseToken)
                _billingState.value = BillingUiState.Success("₹49 Premium Membership activated successfully!")
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: [Code: ${billingResult.responseCode}] ${billingResult.debugMessage}")
                _billingState.value = BillingUiState.Error("Acknowledgement failed: ${billingResult.debugMessage}. Tap Restore Purchase to sync.")
            }
        }
    }

    /**
     * Restore existing purchases (queried directly from Google Play).
     */
    fun restorePurchases(onComplete: ((Boolean, String) -> Unit)? = null) {
        Log.i(TAG, "restorePurchases: Querying Google Play for active INAPP purchases...")

        val client = billingClient
        if (client == null || !client.isReady) {
            Log.d(TAG, "restorePurchases: BillingClient not ready. Connecting now...")
            _billingState.value = BillingUiState.Restoring
            startBillingConnection {
                restorePurchases(onComplete)
            }
            return
        }

        _billingState.value = BillingUiState.Restoring

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchasesList ->
            Log.i(TAG, "restorePurchases finished: ResponseCode=${billingResult.responseCode} (${billingResponseCodeToString(billingResult.responseCode)}), PurchasesCount=${purchasesList.size}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumPurchase = purchasesList.firstOrNull {
                    it.products.contains(PRODUCT_ID_PREMIUM) && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (premiumPurchase != null) {
                    Log.i(TAG, "restorePurchases: Found active purchase '${premiumPurchase.orderId}'.")
                    if (!premiumPurchase.isAcknowledged) {
                        acknowledgePurchase(premiumPurchase)
                    } else {
                        grantPremiumEntitlement(premiumPurchase.purchaseToken)
                        _billingState.value = BillingUiState.Success("Premium restored successfully!")
                    }
                    onComplete?.invoke(true, "Premium restored successfully!")
                } else {
                    val pendingPurchase = purchasesList.firstOrNull {
                        it.products.contains(PRODUCT_ID_PREMIUM) && it.purchaseState == Purchase.PurchaseState.PENDING
                    }
                    if (pendingPurchase != null) {
                        Log.i(TAG, "restorePurchases: Found pending purchase '${pendingPurchase.orderId}'.")
                        _billingState.value = BillingUiState.Pending
                        onComplete?.invoke(false, "Purchase is still pending confirmation by Google Play.")
                    } else {
                        Log.i(TAG, "restorePurchases: No active or pending Premium purchase found in Google Play.")
                        revokePremiumEntitlement()
                        _billingState.value = BillingUiState.Ready(_priceText.value)
                        onComplete?.invoke(false, "No active Premium purchase found on this Google Play account.")
                    }
                }
            } else {
                val msg = mapBillingResponseToMessage(billingResult.responseCode, billingResult.debugMessage)
                Log.e(TAG, "restorePurchases: Failed: [Code: ${billingResult.responseCode}] $msg")
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

        Log.d(TAG, "queryExistingPurchases: Checking active purchases silently...")

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchasesList ->
            Log.d(TAG, "queryExistingPurchases finished: ResponseCode=${billingResult.responseCode}, PurchasesCount=${purchasesList.size}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumPurchase = purchasesList.firstOrNull {
                    it.products.contains(PRODUCT_ID_PREMIUM) && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (premiumPurchase != null) {
                    Log.i(TAG, "queryExistingPurchases: Active purchase verified on Google Play.")
                    if (!premiumPurchase.isAcknowledged) {
                        acknowledgePurchase(premiumPurchase)
                    } else {
                        grantPremiumEntitlement(premiumPurchase.purchaseToken)
                    }
                } else {
                    val wasCached = sharedPreferences.getBoolean(KEY_IS_PREMIUM_CACHED, false)
                    if (wasCached) {
                        Log.i(TAG, "queryExistingPurchases: No active purchase found on Google Play. Syncing status.")
                        revokePremiumEntitlement()
                    }
                }
            }
        }
    }

    private fun grantPremiumEntitlement(token: String) {
        Log.i(TAG, "grantPremiumEntitlement: Unlocking Premium in SharedPreferences with purchase token.")
        sharedPreferences.edit()
            .putBoolean(KEY_IS_PREMIUM_CACHED, true)
            .putString(KEY_PURCHASE_TOKEN, token)
            .apply()
        _isPremiumUser.value = true
    }

    private fun revokePremiumEntitlement() {
        Log.i(TAG, "revokePremiumEntitlement: Revoking cached Premium status from SharedPreferences.")
        sharedPreferences.edit()
            .putBoolean(KEY_IS_PREMIUM_CACHED, false)
            .remove(KEY_PURCHASE_TOKEN)
            .apply()
        _isPremiumUser.value = false
    }

    fun resetBillingState() {
        _billingState.value = BillingUiState.Ready(_priceText.value)
    }

    private fun mapBillingResponseToMessage(responseCode: Int, debugMessage: String?): String {
        return when (responseCode) {
            BillingClient.BillingResponseCode.USER_CANCELED -> "Purchase was cancelled."
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Google Play Billing is unavailable on this device. Please check that Google Play Store is installed and updated."
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Google Play Billing service is currently unavailable. Please check your internet connection and try again."
            BillingClient.BillingResponseCode.NETWORK_ERROR -> "Network error. Please check your internet connection and try again."
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "Product '$PRODUCT_ID_PREMIUM' is currently unavailable in Google Play. Please check that the product is active in Google Play Console."
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "You already own Premium ₹49 access. Restoring entitlement..."
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "No active Premium purchase found."
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Google Play configuration error (${debugMessage ?: "Verify app setup in Play Console"})."
            else -> "Google Play error [Code $responseCode]: ${debugMessage ?: "Unable to complete transaction"}"
        }
    }

    private fun billingResponseCodeToString(responseCode: Int): String {
        return when (responseCode) {
            BillingClient.BillingResponseCode.OK -> "OK (0)"
            BillingClient.BillingResponseCode.USER_CANCELED -> "USER_CANCELED (1)"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE (2)"
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE (3)"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE (4)"
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "DEVELOPER_ERROR (5)"
            BillingClient.BillingResponseCode.ERROR -> "ERROR (6)"
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED (7)"
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED (8)"
            BillingClient.BillingResponseCode.NETWORK_ERROR -> "NETWORK_ERROR (12)"
            else -> "UNKNOWN ($responseCode)"
        }
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}

