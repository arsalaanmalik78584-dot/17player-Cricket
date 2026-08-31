package com.example.model

enum class MatchStatus(val label: String) {
    LIVE("LIVE"),
    UPCOMING("UPCOMING"),
    COMPLETED("COMPLETED"),
    DELAYED("DELAYED")
}

enum class MatchFormat {
    T20, ODI, TEST
}

data class BatsmanScore(
    val name: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: Double,
    val isStriker: Boolean = false,
    val isOut: Boolean = false,
    val dismissal: String = ""
)

data class BowlerFigure(
    val name: String,
    val overs: Double,
    val maidens: Int,
    val runs: Int,
    val wickets: Int,
    val economy: Double,
    val isCurrentBowler: Boolean = false
)

data class BallEvent(
    val ballNumber: String, // e.g. "14.2"
    val result: String,     // "0", "1", "4", "6", "W", "Wd", "Nb"
    val commentary: String,
    val batsman: String,
    val bowler: String,
    val isBoundary: Boolean = false,
    val isWicket: Boolean = false
)

data class FallOfWicket(
    val score: Int,
    val wicketNumber: Int,
    val over: Double,
    val playerOut: String
)

data class CricketMatch(
    val id: String,
    val tournament: String,
    val matchFormat: MatchFormat,
    val team1Name: String,
    val team1Short: String,
    val team1LogoEmoji: String = "🏏",
    val team1Score: String = "",
    val team1Overs: String = "",
    val team2Name: String,
    val team2Short: String,
    val team2LogoEmoji: String = "🦁",
    val team2Score: String = "",
    val team2Overs: String = "",
    val status: MatchStatus,
    val statusSummary: String,
    val venue: String,
    val startTime: String,
    val countdownText: String = "",
    val currentInnings: Int = 1,
    val currentBatsmen: List<BatsmanScore> = emptyList(),
    val currentBowlers: List<BowlerFigure> = emptyList(),
    val recentBalls: List<BallEvent> = emptyList(),
    val partnershipRuns: Int = 0,
    val partnershipBalls: Int = 0,
    val fallOfWickets: List<FallOfWicket> = emptyList(),
    val target: Int? = null,
    val requiredRunRate: Double? = null,
    val currentRunRate: Double = 0.0,
    val broadcasterUrl: String = "https://www.hotstar.com/sports/cricket",
    val broadcasterName: String = "Official Broadcaster",
    val isDemo: Boolean = true
)
