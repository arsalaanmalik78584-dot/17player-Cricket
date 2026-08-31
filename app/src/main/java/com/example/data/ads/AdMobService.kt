package com.example.data.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdMobConfiguration(
    val appId: String = "ca-app-pub-3940256099942544~3347511713", // Standard Google AdMob Test App ID
    val bannerAdUnitId: String = "ca-app-pub-3940256099942544/6300978111", // Test Banner
    val interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712", // Test Interstitial
    val rewardedAdUnitId: String = "ca-app-pub-3940256099942544/5224354917", // Test Rewarded
    val nativeAdUnitId: String = "ca-app-pub-3940256099942544/2247696110", // Test Native
    val isEnabled: Boolean = true
)

object AdMobService {
    val config = AdMobConfiguration()

    private val _isShowingInterstitial = MutableStateFlow(false)
    val isShowingInterstitial = _isShowingInterstitial.asStateFlow()

    private val _isShowingRewarded = MutableStateFlow(false)
    val isShowingRewarded = _isShowingRewarded.asStateFlow()

    var isUserPremium: Boolean = false

    /**
     * Safely triggers an interstitial ad respecting strict safety constraints:
     * NEVER to Premium users, NEVER during live ball-by-ball, NEVER during active squad selection.
     */
    fun showInterstitial(isLiveActive: Boolean = false, isSelectingSquad: Boolean = false, onDismissed: () -> Unit = {}) {
        if (isUserPremium || isLiveActive || isSelectingSquad) {
            // Protected UX / Premium Ad-Free: Do not show ad
            onDismissed()
            return
        }
        _isShowingInterstitial.value = true
    }

    fun dismissInterstitial() {
        _isShowingInterstitial.value = false
    }

    fun showRewardedAd() {
        if (isUserPremium) {
            // Premium users get instant reward perk without having to watch an ad
            _isShowingRewarded.value = false
            return
        }
        _isShowingRewarded.value = true
    }

    fun dismissRewardedAd() {
        _isShowingRewarded.value = false
    }
}
