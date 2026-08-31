package com.example.model

enum class PredictionStatus {
    OPEN,
    LOCKED,
    SETTLED
}

data class PredictionOption(
    val id: String,
    val text: String,
    val votePercentage: Int = 50
)

data class PredictionQuestion(
    val id: String,
    val matchId: String,
    val question: String,
    val options: List<PredictionOption>,
    val pointsReward: Int = 50,
    val status: PredictionStatus = PredictionStatus.OPEN,
    val closingOverText: String = "Before Over 10.0",
    val correctOptionId: String? = null,
    val userSelectedOptionId: String? = null,
    val isUserCorrect: Boolean? = null,
    val awardedPoints: Int = 0
)
