package com.example.model

data class FriendsLeague(
    val leagueId: String,
    val name: String,
    val inviteCode: String,
    val creatorName: String,
    val matchTitle: String,
    val memberCount: Int,
    val userRank: Int,
    val userPoints: Int,
    val isUserCreator: Boolean = false
)

data class CricketUpdate(
    val id: String,
    val title: String,
    val summary: String,
    val tag: String,
    val timeAgo: String,
    val category: String = "Tournament News"
)
