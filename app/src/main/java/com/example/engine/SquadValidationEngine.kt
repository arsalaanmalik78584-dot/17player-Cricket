package com.example.engine

import com.example.model.Player
import com.example.model.PlayerRole
import com.example.model.SquadRequirements
import com.example.model.SquadValidationResult

object SquadValidationEngine {

    val defaultRequirements = SquadRequirements()

    fun validateSquad(
        selectedPlayers: List<Player>,
        captainId: String?,
        viceCaptainId: String?,
        requirements: SquadRequirements = defaultRequirements
    ): SquadValidationResult {
        val total = selectedPlayers.size
        val wkCount = selectedPlayers.count { it.role == PlayerRole.WICKET_KEEPER }
        val batCount = selectedPlayers.count { it.role == PlayerRole.BATSMAN }
        val arCount = selectedPlayers.count { it.role == PlayerRole.ALL_ROUNDER }
        val bowlCount = selectedPlayers.count { it.role == PlayerRole.BOWLER }

        val captainSet = captainId != null && selectedPlayers.any { it.id == captainId }
        val viceCaptainSet = viceCaptainId != null && selectedPlayers.any { it.id == viceCaptainId }
        val captainDistinct = captainId != viceCaptainId

        val errors = mutableListOf<String>()

        if (total < requirements.totalRequired) {
            errors.add("Need ${requirements.totalRequired - total} more player(s) to complete 17-player squad")
        } else if (total > requirements.totalRequired) {
            errors.add("Squad has exceeded 17 players limit")
        }

        if (wkCount < requirements.minWicketKeepers) {
            errors.add("Select at least ${requirements.minWicketKeepers} Wicketkeepers (Current: $wkCount)")
        } else if (wkCount > requirements.maxWicketKeepers) {
            errors.add("Maximum ${requirements.maxWicketKeepers} Wicketkeepers allowed")
        }

        if (batCount < requirements.minBatsmen) {
            errors.add("Select at least ${requirements.minBatsmen} Batsmen (Current: $batCount)")
        } else if (batCount > requirements.maxBatsmen) {
            errors.add("Maximum ${requirements.maxBatsmen} Batsmen allowed")
        }

        if (arCount < requirements.minAllRounders) {
            errors.add("Select at least ${requirements.minAllRounders} All-rounders (Current: $arCount)")
        } else if (arCount > requirements.maxAllRounders) {
            errors.add("Maximum ${requirements.maxAllRounders} All-rounders allowed")
        }

        if (bowlCount < requirements.minBowlers) {
            errors.add("Select at least ${requirements.minBowlers} Bowlers (Current: $bowlCount)")
        } else if (bowlCount > requirements.maxBowlers) {
            errors.add("Maximum ${requirements.maxBowlers} Bowlers allowed")
        }

        if (!captainSet) {
            errors.add("Please select a Captain (2× points multiplier)")
        }

        if (!viceCaptainSet) {
            errors.add("Please select a Vice-Captain (1.5× points multiplier)")
        }

        if (captainId != null && viceCaptainId != null && !captainDistinct) {
            errors.add("Captain and Vice-Captain must be two different players")
        }

        val isValid = errors.isEmpty()
        val message = if (isValid) {
            "Valid 17-Player Squad ready for match submission!"
        } else {
            errors.first()
        }

        return SquadValidationResult(
            isValid = isValid,
            message = message,
            currentTotal = total,
            wkCount = wkCount,
            batCount = batCount,
            arCount = arCount,
            bowlCount = bowlCount,
            captainSet = captainSet,
            viceCaptainSet = viceCaptainSet
        )
    }
}
