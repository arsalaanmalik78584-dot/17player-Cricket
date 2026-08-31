package com.example.model

enum class LeaderboardFilter(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    FRIENDS("Friends")
}

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val username: String,
    val avatarEmoji: String = "🏏",
    val points: Int,
    val badge: String = "Cricket Fan",
    val isCurrentUser: Boolean = false,
    val city: String = "Mumbai",
    val change: Int = 0 // +1, -2, 0
)
