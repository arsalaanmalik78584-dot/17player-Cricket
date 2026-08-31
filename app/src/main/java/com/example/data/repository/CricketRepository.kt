package com.example.data.repository

import com.example.data.api.CricketApiService
import com.example.data.api.DemoCricketData
import com.example.data.local.*
import com.example.engine.PointsEngine
import com.example.engine.ReferralEngine
import com.example.engine.ReferralStage
import com.example.engine.SquadValidationEngine
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class CricketRepository(
    private val database: CricketDatabase,
    private val apiService: CricketApiService? = null
) {
    private val _liveMatches = MutableStateFlow(DemoCricketData.demoMatches)
    val liveMatches: Flow<List<CricketMatch>> = _liveMatches.asStateFlow()

    private val _predictions = MutableStateFlow(DemoCricketData.demoPredictions)
    val predictions: Flow<List<PredictionQuestion>> = _predictions.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            userId = "usr_cricket_01",
            username = "CricketChamp17",
            totalPoints = 1850,
            currentRank = 42,
            bestRank = 12,
            matchesPlayed = 14,
            totalPredictions = 28,
            correctPredictions = 19,
            referralCode = "17PC4582",
            referralCount = 4
        )
    )
    val userProfile: Flow<UserProfile> = _userProfile.asStateFlow()

    private val _badges = MutableStateFlow(DemoCricketData.demoBadges)
    val badges: Flow<List<AchievementBadge>> = _badges.asStateFlow()

    private val _friendsLeagues = MutableStateFlow(DemoCricketData.demoLeagues)
    val friendsLeagues: Flow<List<FriendsLeague>> = _friendsLeagues.asStateFlow()

    private val _currentSquad = MutableStateFlow<Map<String, UserSquad>>(
        mapOf(
            "match_ind_aus_01" to UserSquad(
                matchId = "match_ind_aus_01",
                playerIds = DemoCricketData.matchPlayersIndAus.take(17).map { it.id },
                captainId = "p4", // Virat Kohli (2x)
                viceCaptainId = "p18", // Jasprit Bumrah (1.5x)
                totalPoints = 842,
                rank = 42
            )
        )
    )
    val currentSquads: Flow<Map<String, UserSquad>> = _currentSquad.asStateFlow()

    fun getMatchById(matchId: String): CricketMatch? {
        return _liveMatches.value.find { it.id == matchId }
    }

    fun getPlayersForMatch(matchId: String): List<Player> {
        return DemoCricketData.matchPlayersIndAus
    }

    fun getPredictionsForMatch(matchId: String): List<PredictionQuestion> {
        return _predictions.value.filter { it.matchId == matchId }
    }

    fun getSquadForMatch(matchId: String): UserSquad? {
        return _currentSquad.value[matchId]
    }

    suspend fun saveSquad(
        matchId: String,
        selectedPlayerIds: List<String>,
        captainId: String,
        viceCaptainId: String
    ): SquadValidationResult {
        val allPlayers = getPlayersForMatch(matchId)
        val selectedPlayers = allPlayers.filter { it.id in selectedPlayerIds }
        val validation = SquadValidationEngine.validateSquad(selectedPlayers, captainId, viceCaptainId)

        if (validation.isValid) {
            val userSquad = UserSquad(
                matchId = matchId,
                playerIds = selectedPlayerIds,
                captainId = captainId,
                viceCaptainId = viceCaptainId,
                totalPoints = calculateSquadLivePoints(matchId, selectedPlayerIds, captainId, viceCaptainId),
                rank = 42
            )
            _currentSquad.value = _currentSquad.value + (matchId to userSquad)

            // Save to Room DB
            database.squadDao().insertSquad(
                UserSquadEntity(
                    matchId = matchId,
                    playerIds = selectedPlayerIds,
                    captainId = captainId,
                    viceCaptainId = viceCaptainId,
                    totalPoints = userSquad.totalPoints
                )
            )

            // Increment matches played if first time
            val current = _userProfile.value
            _userProfile.value = current.copy(matchesPlayed = current.matchesPlayed + 1)
        }

        return validation
    }

    fun calculateSquadLivePoints(
        matchId: String,
        playerIds: List<String>,
        captainId: String,
        viceCaptainId: String
    ): Int {
        val allPlayers = getPlayersForMatch(matchId)
        var total = 0
        for (player in allPlayers) {
            if (player.id in playerIds) {
                val isCaptain = player.id == captainId
                val isViceCaptain = player.id == viceCaptainId
                total += PointsEngine.calculatePlayerPoints(player.currentPerformance, isCaptain, isViceCaptain)
            }
        }
        return total
    }

    suspend fun submitPrediction(predictionId: String, optionId: String): Boolean {
        val list = _predictions.value.toMutableList()
        val index = list.indexOfFirst { it.id == predictionId }
        if (index != -1) {
            val pred = list[index]
            if (pred.status == PredictionStatus.OPEN) {
                list[index] = pred.copy(userSelectedOptionId = optionId)
                _predictions.value = list

                // Record in Room
                database.predictionDao().insertPrediction(
                    UserPredictionEntity(
                        id = predictionId,
                        matchId = pred.matchId,
                        question = pred.question,
                        selectedOptionId = optionId,
                        pointsReward = pred.pointsReward,
                        status = "SUBMITTED",
                        isUserCorrect = null
                    )
                )
                return true
            }
        }
        return false
    }

    suspend fun claimRewardedAdPoints(): Pair<Boolean, String> {
        val profile = _userProfile.value
        if (profile.dailyRewardedAdsWatched >= profile.maxDailyRewardedAds) {
            return Pair(false, "Daily limit of ${profile.maxDailyRewardedAds} rewarded ads reached. Come back tomorrow!")
        }
        val bonus = PointsEngine.config.rewardedAdPoints
        _userProfile.value = profile.copy(
            totalPoints = profile.totalPoints + bonus,
            dailyRewardedAdsWatched = profile.dailyRewardedAdsWatched + 1
        )
        database.userDao().addPoints(profile.userId, bonus)
        return Pair(true, "+$bonus In-App Points credited successfully! (No cash value)")
    }

    suspend fun applyReferralCode(code: String): Pair<Boolean, String> {
        val profile = _userProfile.value
        val validation = ReferralEngine.validateReferralCode(code, profile.userId, profile.referralCode)
        if (!validation.first) {
            return validation
        }

        val activation = ReferralEngine.evaluateActivationPoints(ReferralStage.SIGNUP, profile.referralCount)
        if (activation.first > 0) {
            _userProfile.value = profile.copy(
                totalPoints = profile.totalPoints + activation.first,
                referralCount = profile.referralCount + 1
            )
            return Pair(true, "Referral Code Applied! +${activation.first} Points credited to your account.")
        }
        return Pair(false, activation.second)
    }

    suspend fun createFriendsLeague(leagueName: String, matchTitle: String): FriendsLeague {
        val code = "17P" + (1000..9999).random().toString()
        val newLeague = FriendsLeague(
            leagueId = "lg_" + System.currentTimeMillis(),
            name = leagueName,
            inviteCode = code,
            creatorName = _userProfile.value.username,
            matchTitle = matchTitle,
            memberCount = 1,
            userRank = 1,
            userPoints = _userProfile.value.totalPoints,
            isUserCreator = true
        )
        _friendsLeagues.value = _friendsLeagues.value + newLeague
        database.friendLeagueDao().insertLeague(
            FriendLeagueEntity(
                leagueId = newLeague.leagueId,
                name = newLeague.name,
                inviteCode = newLeague.inviteCode,
                creatorName = newLeague.creatorName,
                matchTitle = newLeague.matchTitle,
                memberCount = newLeague.memberCount,
                userRank = newLeague.userRank,
                userPoints = newLeague.userPoints
            )
        )
        return newLeague
    }

    suspend fun joinFriendsLeague(code: String): Pair<Boolean, String> {
        val trimmed = code.trim().uppercase()
        if (trimmed.length < 4) {
            return Pair(false, "Invalid League Code")
        }
        val existing = _friendsLeagues.value.find { it.inviteCode.equals(trimmed, ignoreCase = true) }
        if (existing != null) {
            return Pair(true, "You are already a member of '${existing.name}'!")
        }
        val joined = FriendsLeague(
            leagueId = "lg_joined_${System.currentTimeMillis()}",
            name = "Premier Friends XI ($trimmed)",
            inviteCode = trimmed,
            creatorName = "CricketBuddy",
            matchTitle = "India vs Australia",
            memberCount = 5,
            userRank = 2,
            userPoints = _userProfile.value.totalPoints,
            isUserCreator = false
        )
        _friendsLeagues.value = _friendsLeagues.value + joined
        return Pair(true, "Successfully joined league '${joined.name}'!")
    }
}
