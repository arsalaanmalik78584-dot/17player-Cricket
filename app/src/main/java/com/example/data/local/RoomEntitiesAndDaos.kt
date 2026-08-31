package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_squads")
data class UserSquadEntity(
    @PrimaryKey val matchId: String,
    val playerIds: List<String>,
    val captainId: String,
    val viceCaptainId: String,
    val totalPoints: Int = 0,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val email: String,
    val avatarEmoji: String,
    val totalPoints: Int,
    val currentRank: Int,
    val bestRank: Int,
    val matchesPlayed: Int,
    val totalPredictions: Int,
    val correctPredictions: Int,
    val referralCode: String,
    val referralCount: Int,
    val isProfileCompleted: Boolean,
    val dailyRewardedAdsWatched: Int
)

@Entity(tableName = "user_predictions")
data class UserPredictionEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val question: String,
    val selectedOptionId: String,
    val pointsReward: Int,
    val status: String,
    val isUserCorrect: Boolean?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "friend_leagues")
data class FriendLeagueEntity(
    @PrimaryKey val leagueId: String,
    val name: String,
    val inviteCode: String,
    val creatorName: String,
    val matchTitle: String,
    val memberCount: Int,
    val userRank: Int,
    val userPoints: Int
)

@Entity(tableName = "user_badges")
data class UserBadgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val rewardPoints: Int,
    val unlockedDate: String?
)

@Dao
interface SquadDao {
    @Query("SELECT * FROM user_squads WHERE matchId = :matchId")
    fun getSquadForMatch(matchId: String): Flow<UserSquadEntity?>

    @Query("SELECT * FROM user_squads")
    fun getAllSquads(): Flow<List<UserSquadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSquad(squad: UserSquadEntity)

    @Query("DELETE FROM user_squads WHERE matchId = :matchId")
    suspend fun deleteSquad(matchId: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET totalPoints = totalPoints + :points WHERE userId = :userId")
    suspend fun addPoints(userId: String, points: Int)
}

@Dao
interface PredictionDao {
    @Query("SELECT * FROM user_predictions WHERE matchId = :matchId ORDER BY timestamp DESC")
    fun getPredictionsForMatch(matchId: String): Flow<List<UserPredictionEntity>>

    @Query("SELECT * FROM user_predictions ORDER BY timestamp DESC")
    fun getAllUserPredictions(): Flow<List<UserPredictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: UserPredictionEntity)
}

@Dao
interface FriendLeagueDao {
    @Query("SELECT * FROM friend_leagues")
    fun getAllLeagues(): Flow<List<FriendLeagueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeague(league: FriendLeagueEntity)

    @Query("DELETE FROM friend_leagues WHERE leagueId = :leagueId")
    suspend fun deleteLeague(leagueId: String)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM user_badges")
    fun getAllBadges(): Flow<List<UserBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<UserBadgeEntity>)

    @Query("UPDATE user_badges SET isUnlocked = 1, unlockedDate = :date WHERE id = :badgeId")
    suspend fun unlockBadge(badgeId: String, date: String)
}
