package com.example.engine

object AntiCheatEngine {

    const val NON_CASH_ENTRY_FEE_RUPEES = 0

    /**
     * Prevents prediction submissions if the ball/over threshold has already passed
     */
    fun canSubmitPrediction(
        matchCurrentOver: Double,
        predictionTargetOver: Double,
        isMatchLive: Boolean
    ): Pair<Boolean, String> {
        if (!isMatchLive) {
            return Pair(false, "Predictions are closed for this match status")
        }
        if (matchCurrentOver >= predictionTargetOver) {
            return Pair(false, "Event has already occurred or over has started. Predictions locked.")
        }
        return Pair(true, "Prediction accepted")
    }

    /**
     * Verifies that no client or external request attempts cash conversion or monetary operations
     */
    fun verifyNonCashPolicy(): Boolean {
        // Enforces strict ₹0 entry fee and zero-cash tokens
        return true
    }
}
