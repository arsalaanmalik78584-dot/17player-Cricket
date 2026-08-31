package com.example

import com.example.engine.*
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPointsEngine_NormalCalculation() {
        val performance = PlayerMatchPerformance(
            runs = 50,
            balls = 30,
            fours = 4,
            sixes = 2,
            wickets = 2,
            maidens = 1,
            catches = 1
        )
        // Expected: 50 runs + 4 fours bonus + 4 sixes bonus + 8 half-century bonus + 40 (2 wkts) + 10 maiden + 10 catch = 126
        val normalPoints = PointsEngine.calculatePlayerPoints(performance, isCaptain = false, isViceCaptain = false)
        assertTrue(normalPoints > 100)

        // Captain gets 2x
        val captainPoints = PointsEngine.calculatePlayerPoints(performance, isCaptain = true, isViceCaptain = false)
        assertEquals(normalPoints * 2, captainPoints)

        // Vice Captain gets 1.5x
        val viceCaptainPoints = PointsEngine.calculatePlayerPoints(performance, isCaptain = false, isViceCaptain = true)
        assertEquals((normalPoints * 1.5).toInt(), viceCaptainPoints)
    }

    @Test
    fun testSquadValidation_Valid17Squad() {
        val players = mutableListOf<Player>()
        // 2 WK
        repeat(2) { idx -> players.add(Player("wk_$idx", "WK $idx", "IND", PlayerRole.WICKET_KEEPER, "🧤", 9.0, emptyList())) }
        // 6 BAT
        repeat(6) { idx -> players.add(Player("bat_$idx", "BAT $idx", "IND", PlayerRole.BATSMAN, "🏏", 9.0, emptyList())) }
        // 3 AR
        repeat(3) { idx -> players.add(Player("ar_$idx", "AR $idx", "IND", PlayerRole.ALL_ROUNDER, "⚡", 9.0, emptyList())) }
        // 5 BOWL
        repeat(5) { idx -> players.add(Player("bowl_$idx", "BOWL $idx", "IND", PlayerRole.BOWLER, "🎯", 9.0, emptyList())) }
        // 1 FLEX (e.g. extra batsman)
        players.add(Player("flex_1", "FLEX 1", "IND", PlayerRole.BATSMAN, "🏏", 9.0, emptyList()))

        assertEquals(17, players.size)

        val validation = SquadValidationEngine.validateSquad(players, captainId = "bat_0", viceCaptainId = "bowl_0")
        assertTrue(validation.message, validation.isValid)
    }

    @Test
    fun testSquadValidation_IncompleteSquad() {
        val players = listOf(
            Player("p1", "Player 1", "IND", PlayerRole.BATSMAN, "🏏", 9.0, emptyList())
        )
        val validation = SquadValidationEngine.validateSquad(players, captainId = null, viceCaptainId = null)
        assertFalse(validation.isValid)
    }

    @Test
    fun testReferralEngine_Validation() {
        // Self referral should fail
        val (validSelf, _) = ReferralEngine.validateReferralCode("17PC1234", "user_1234", "17PC1234")
        assertFalse(validSelf)

        // Valid other code
        val (validOther, _) = ReferralEngine.validateReferralCode("17PC9999", "user_1234", "17PC1234")
        assertTrue(validOther)
    }

    @Test
    fun testAntiCheat_NonCashStrictCompliance() {
        assertTrue(AntiCheatEngine.verifyNonCashPolicy())
        assertEquals(0, AntiCheatEngine.NON_CASH_ENTRY_FEE_RUPEES)
    }

    @Test
    fun testBillingConfiguration_ProductIdAndInAppType() {
        // Product ID must be exactly "premium_49"
        assertEquals("premium_49", com.example.data.billing.BillingRepository.PRODUCT_ID_PREMIUM)
    }

    @Test
    fun testAdMobService_SuppressionForPremiumUsers() {
        // When user is premium, interstitial ads should never display
        com.example.data.ads.AdMobService.isUserPremium = true
        var dismissedCalled = false
        com.example.data.ads.AdMobService.showInterstitial(isLiveActive = false, isSelectingSquad = false) {
            dismissedCalled = true
        }
        assertTrue(dismissedCalled)
        assertFalse(com.example.data.ads.AdMobService.isShowingInterstitial.value)

        // Reset for non-premium
        com.example.data.ads.AdMobService.isUserPremium = false
    }

    @Test
    fun testBillingUiStates_Integrity() {
        val readyState = com.example.data.billing.BillingUiState.Ready("₹49")
        assertEquals("₹49", readyState.price)

        val errorState = com.example.data.billing.BillingUiState.Error("Network failure", canRetry = true)
        assertEquals("Network failure", errorState.message)
        assertTrue(errorState.canRetry)

        val successState = com.example.data.billing.BillingUiState.Success("Unlocked")
        assertEquals("Unlocked", successState.message)
    }

    @Test
    fun testNonCashIntegrity_NoDepositNoBetting() {
        // Assert zero monetary/cash wallet or wagering exists
        assertEquals(0, AntiCheatEngine.NON_CASH_ENTRY_FEE_RUPEES)
        assertTrue(AntiCheatEngine.verifyNonCashPolicy())
    }
}

