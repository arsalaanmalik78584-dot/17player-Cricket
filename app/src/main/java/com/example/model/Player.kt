package com.example.model

enum class PlayerRole(val label: String, val shortLabel: String) {
    WICKET_KEEPER("Wicketkeeper", "WK"),
    BATSMAN("Batsman", "BAT"),
    ALL_ROUNDER("All-Rounder", "AR"),
    BOWLER("Bowler", "BOWL")
}

data class PlayerMatchPerformance(
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val wickets: Int = 0,
    val oversBowled: Double = 0.0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val catches: Int = 0,
    val runOuts: Int = 0,
    val stumpings: Int = 0,
    val fantasyPoints: Int = 0
)

data class Player(
    val id: String,
    val name: String,
    val team: String,
    val role: PlayerRole,
    val avatarEmoji: String = "👤",
    val credits: Double = 9.0,
    val recentForm: List<Int> = listOf(45, 82, 12, 60, 95), // points in last 5 matches
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val totalCatches: Int = 0,
    val strikeRate: Double = 135.4,
    val economy: Double = 7.8,
    val matchesPlayed: Int = 24,
    val currentPerformance: PlayerMatchPerformance = PlayerMatchPerformance(),
    val isPlaying11: Boolean = true
)
