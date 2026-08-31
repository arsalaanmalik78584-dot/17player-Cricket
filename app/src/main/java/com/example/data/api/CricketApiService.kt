package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Authorized Cricket API Service abstraction.
 * Cleanly separates production licensed cricket data APIs from offline demo simulation.
 */
interface CricketApiService {

    @GET("matches/live")
    suspend fun getLiveMatches(@Query("apiKey") apiKey: String? = null): List<MatchDto>

    @GET("matches/upcoming")
    suspend fun getUpcomingMatches(@Query("apiKey") apiKey: String? = null): List<MatchDto>

    @GET("matches/{id}")
    suspend fun getMatchDetails(
        @Path("id") matchId: String,
        @Query("apiKey") apiKey: String? = null
    ): MatchDetailsDto

    @GET("matches/{id}/ball-by-ball")
    suspend fun getBallByBall(
        @Path("id") matchId: String,
        @Query("apiKey") apiKey: String? = null
    ): List<BallEventDto>

    @GET("matches/{id}/players")
    suspend fun getPlayers(
        @Path("id") matchId: String,
        @Query("apiKey") apiKey: String? = null
    ): List<PlayerDto>

    @GET("players/{id}/stats")
    suspend fun getPlayerStats(
        @Path("id") playerId: String,
        @Query("apiKey") apiKey: String? = null
    ): PlayerStatsDto
}

data class MatchDto(
    val id: String,
    val tournament: String,
    val team1: String,
    val team2: String,
    val score1: String,
    val score2: String,
    val status: String
)

data class MatchDetailsDto(
    val id: String,
    val tournament: String,
    val venue: String,
    val liveScore: String
)

data class BallEventDto(
    val over: String,
    val result: String,
    val commentary: String
)

data class PlayerDto(
    val id: String,
    val name: String,
    val team: String,
    val role: String
)

data class PlayerStatsDto(
    val playerId: String,
    val matches: Int,
    val runs: Int,
    val wickets: Int
)
