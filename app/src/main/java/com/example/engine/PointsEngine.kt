package com.example.engine

import com.example.model.PlayerMatchPerformance

/**
 * Configurable Points Engine for 17Player Cricket.
 * All scoring rules are centralized here and can be adjusted dynamically.
 */
data class PointsConfiguration(
    // Batting
    val pointsPerRun: Int = 1,
    val bonusPerBoundaryFour: Int = 1,
    val bonusPerBoundarySix: Int = 2,
    val milestone30RunsBonus: Int = 5,
    val milestone50RunsBonus: Int = 10,
    val milestone100RunsBonus: Int = 25,
    val duckPenaltyBatsman: Int = -2,

    // Bowling
    val pointsPerWicket: Int = 20,
    val pointsPerMaidenOver: Int = 10,
    val milestone3WicketsBonus: Int = 10,
    val milestone5WicketsBonus: Int = 20,
    val economyBelow5Bonus: Int = 6,

    // Fielding
    val pointsPerCatch: Int = 10,
    val pointsPerRunOut: Int = 10,
    val pointsPerStumping: Int = 10,

    // Multipliers
    val captainMultiplier: Double = 2.0,
    val viceCaptainMultiplier: Double = 1.5,

    // Predictions & Engagement (Non-cash In-App Points)
    val standardPredictionPoints: Int = 50,
    val bonusPredictionPoints: Int = 100,
    val rewardedAdPoints: Int = 50,
    val referralSignupPoints: Int = 100,
    val referralProfilePoints: Int = 100,
    val referralFirstMatchPoints: Int = 500,
    val maxMonthlyReferrals: Int = 20
)

object PointsEngine {
    var config: PointsConfiguration = PointsConfiguration()
        private set

    fun updateConfiguration(newConfig: PointsConfiguration) {
        config = newConfig
    }

    /**
     * Calculates fantasy-style performance points for a player
     */
    fun calculatePlayerPoints(
        performance: PlayerMatchPerformance,
        isCaptain: Boolean = false,
        isViceCaptain: Boolean = false
    ): Int {
        var basePoints = 0

        // Batting points
        basePoints += performance.runs * config.pointsPerRun
        basePoints += performance.fours * config.bonusPerBoundaryFour
        basePoints += performance.sixes * config.bonusPerBoundarySix

        if (performance.runs >= 100) {
            basePoints += config.milestone100RunsBonus
        } else if (performance.runs >= 50) {
            basePoints += config.milestone50RunsBonus
        } else if (performance.runs >= 30) {
            basePoints += config.milestone30RunsBonus
        }

        if (performance.runs == 0 && performance.balls > 0 && performance.runsConceded == 0) {
            // Dismissed for duck if out
        }

        // Bowling points
        basePoints += performance.wickets * config.pointsPerWicket
        basePoints += performance.maidens * config.pointsPerMaidenOver

        if (performance.wickets >= 5) {
            basePoints += config.milestone5WicketsBonus
        } else if (performance.wickets >= 3) {
            basePoints += config.milestone3WicketsBonus
        }

        // Fielding points
        basePoints += performance.catches * config.pointsPerCatch
        basePoints += performance.runOuts * config.pointsPerRunOut
        basePoints += performance.stumpings * config.pointsPerStumping

        // Apply Multipliers
        val total = when {
            isCaptain -> (basePoints * config.captainMultiplier).toInt()
            isViceCaptain -> (basePoints * config.viceCaptainMultiplier).toInt()
            else -> basePoints
        }

        return total
    }
}
