package com.example.engine

enum class ReferralStage(val title: String, val pointsReward: Int, val description: String) {
    SIGNUP("Account Creation", 100, "New user installs and creates account with code"),
    PROFILE_COMPLETION("Profile Completion", 100, "User completes player avatar & cricket preferences"),
    FIRST_MATCH("First Match Squad", 500, "User creates their first 17-player match squad")
}

data class ReferralRecord(
    val id: String,
    val referredUserId: String,
    val referredUserName: String,
    val referralCode: String,
    val currentStage: ReferralStage,
    val totalPointsGranted: Int,
    val isActivated: Boolean,
    val timestamp: Long,
    val fraudRiskScore: Double = 0.0
)

object ReferralEngine {

    fun generateUniqueCode(userId: String): String {
        val hash = (userId.hashCode() and 0xFFFF).toString(16).uppercase().padStart(4, '0')
        return "17PC$hash"
    }

    fun validateReferralCode(code: String, currentUserId: String, ownCode: String): Pair<Boolean, String> {
        val trimmed = code.trim().uppercase()
        if (trimmed.isEmpty()) {
            return Pair(false, "Referral code cannot be empty")
        }
        if (trimmed == ownCode.trim().uppercase()) {
            return Pair(false, "Self-referral is not permitted")
        }
        if (!trimmed.startsWith("17PC") || trimmed.length < 6) {
            return Pair(false, "Invalid referral code format. Must start with 17PC")
        }
        return Pair(true, "Referral code applied successfully")
    }

    fun evaluateActivationPoints(
        stage: ReferralStage,
        monthlyReferralCount: Int
    ): Pair<Int, String> {
        if (monthlyReferralCount >= PointsEngine.config.maxMonthlyReferrals) {
            return Pair(0, "Monthly referral limit of ${PointsEngine.config.maxMonthlyReferrals} reached. Points cannot be credited this month.")
        }
        val points = when (stage) {
            ReferralStage.SIGNUP -> PointsEngine.config.referralSignupPoints
            ReferralStage.PROFILE_COMPLETION -> PointsEngine.config.referralProfilePoints
            ReferralStage.FIRST_MATCH -> PointsEngine.config.referralFirstMatchPoints
        }
        return Pair(points, "${points} In-App Points awarded for ${stage.title}!")
    }
}
