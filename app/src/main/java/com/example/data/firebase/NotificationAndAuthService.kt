package com.example.data.firebase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationPreferences(
    val matchStarting: Boolean = true,
    val matchLive: Boolean = true,
    val wicketAlerts: Boolean = true,
    val milestoneAlerts: Boolean = true,
    val mySquadPlayerPerformance: Boolean = true,
    val predictionAvailable: Boolean = true,
    val leaderboardUpdates: Boolean = true,
    val dailyChallenge: Boolean = true
)

object FirebaseNotificationService {
    private val _preferences = MutableStateFlow(NotificationPreferences())
    val preferences = _preferences.asStateFlow()

    fun updatePreferences(newPrefs: NotificationPreferences) {
        _preferences.value = newPrefs
    }
}

/**
 * Admin System Data Structure
 * Ready for backend sync with Firestore rules and admin dashboard
 */
data class AdminAnnouncement(
    val id: String,
    val title: String,
    val message: String,
    val publishedAt: Long,
    val priority: String = "NORMAL"
)

data class AdminReferralReview(
    val referralId: String,
    val referrerId: String,
    val refereeId: String,
    val riskScore: Double,
    val status: String // PENDING, APPROVED, FLAGGED
)

object AdminSystemRegistry {
    // Protected admin data models
    val announcements = listOf(
        AdminAnnouncement("an_1", "Welcome to 17Player Cricket", "Enjoy the ₹0 Free-to-Play companion app with 17-player squads!", System.currentTimeMillis()),
        AdminAnnouncement("an_2", "Responsible Play Notice", "17Player Points have strictly no cash value. Play purely for the love of cricket!", System.currentTimeMillis())
    )
}
