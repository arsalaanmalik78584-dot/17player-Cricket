package com.example.model

data class SquadRequirements(
    val minWicketKeepers: Int = 2,
    val maxWicketKeepers: Int = 3,
    val minBatsmen: Int = 5,
    val maxBatsmen: Int = 7,
    val minAllRounders: Int = 2,
    val maxAllRounders: Int = 4,
    val minBowlers: Int = 4,
    val maxBowlers: Int = 6,
    val totalRequired: Int = 17
)

data class SquadValidationResult(
    val isValid: Boolean,
    val message: String,
    val currentTotal: Int,
    val wkCount: Int,
    val batCount: Int,
    val arCount: Int,
    val bowlCount: Int,
    val captainSet: Boolean,
    val viceCaptainSet: Boolean
)

data class UserSquad(
    val matchId: String,
    val playerIds: List<String> = emptyList(),
    val captainId: String? = null,
    val viceCaptainId: String? = null,
    val totalPoints: Int = 0,
    val rank: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
