package com.example.model

data class UserProfile(
    val userId: String = "usr_cricket_01",
    val username: String = "CricketChamp17",
    val email: String = "cricket.fan@example.com",
    val avatarEmoji: String = "🏏",
    val totalPoints: Int = 1850,
    val currentRank: Int = 42,
    val bestRank: Int = 12,
    val matchesPlayed: Int = 14,
    val totalPredictions: Int = 28,
    val correctPredictions: Int = 19,
    val referralCode: String = "17PC4582",
    val referralCount: Int = 4,
    val isProfileCompleted: Boolean = true,
    val rewardPointsToday: Int = 50,
    val dailyRewardedAdsWatched: Int = 1,
    val maxDailyRewardedAds: Int = 5
)
