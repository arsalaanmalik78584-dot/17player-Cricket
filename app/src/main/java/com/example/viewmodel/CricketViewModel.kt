package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ads.AdMobService
import com.example.data.billing.BillingRepository
import com.example.data.billing.BillingUiState
import com.example.data.firebase.FirebaseNotificationService
import com.example.data.firebase.NotificationPreferences
import com.example.data.local.CricketDatabase
import com.example.data.repository.CricketRepository
import com.example.engine.SquadValidationEngine
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SquadBuilderUiState(
    val matchId: String = "",
    val selectedPlayerIds: Set<String> = emptySet(),
    val captainId: String? = null,
    val viceCaptainId: String? = null,
    val roleFilter: PlayerRole? = null,
    val validationMessage: String = "",
    val isSquadValid: Boolean = false,
    val showSuccessDialog: Boolean = false
)

data class UiMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class CricketViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CricketDatabase.getDatabase(application)
    val repository = CricketRepository(database)
    val billingRepository = BillingRepository.getInstance(application)

    val isPremiumUser = billingRepository.isPremiumUser
    val billingState = billingRepository.billingState
    val premiumPriceText = billingRepository.priceText

    val liveMatches = repository.liveMatches.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val userProfile = repository.userProfile.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile()
    )

    val badges = repository.badges.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val friendsLeagues = repository.friendsLeagues.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val predictions = repository.predictions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val currentSquads = repository.currentSquads.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap()
    )

    val notificationPreferences = FirebaseNotificationService.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationPreferences()
    )

    // Current Active Match selection
    private val _selectedMatchId = MutableStateFlow("match_ind_aus_01")
    val selectedMatchId = _selectedMatchId.asStateFlow()

    val selectedMatch: StateFlow<CricketMatch?> = combine(liveMatches, _selectedMatchId) { matches, id ->
        matches.find { it.id == id } ?: matches.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Selected Player for Details screen
    private val _selectedPlayerId = MutableStateFlow<String?>("p4")
    val selectedPlayerId = _selectedPlayerId.asStateFlow()

    val selectedPlayer: StateFlow<Player?> = combine(_selectedPlayerId, _selectedMatchId) { playerId, matchId ->
        if (playerId == null) null
        else repository.getPlayersForMatch(matchId).find { it.id == playerId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Squad Builder State
    private val _squadBuilderState = MutableStateFlow(SquadBuilderUiState())
    val squadBuilderState = _squadBuilderState.asStateFlow()

    // Leaderboard Filter State
    private val _leaderboardFilter = MutableStateFlow(LeaderboardFilter.DAILY)
    val leaderboardFilter = _leaderboardFilter.asStateFlow()

    // UI Snackbar / Messages
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage = _uiMessage.asStateFlow()

    // Settings
    private val _isHindiLanguage = MutableStateFlow(false)
    val isHindiLanguage = _isHindiLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled = _soundEnabled.asStateFlow()

    init {
        // Initialize default squad selection if available
        initSquadBuilder("match_ind_aus_01")

        // Sync Premium state with AdMob service to ensure ad-free experience for premium users
        viewModelScope.launch {
            isPremiumUser.collect { premium ->
                AdMobService.isUserPremium = premium
            }
        }
    }

    fun selectMatch(matchId: String) {
        _selectedMatchId.value = matchId
        initSquadBuilder(matchId)
    }

    fun selectPlayer(playerId: String) {
        _selectedPlayerId.value = playerId
    }

    fun setLeaderboardFilter(filter: LeaderboardFilter) {
        _leaderboardFilter.value = filter
    }

    fun toggleLanguage(isHindi: Boolean) {
        _isHindiLanguage.value = isHindi
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun toggleSound(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun updateNotificationPreferences(prefs: NotificationPreferences) {
        FirebaseNotificationService.updatePreferences(prefs)
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(text: String, isError: Boolean = false) {
        _uiMessage.value = UiMessage(message = text, isError = isError)
    }

    // Squad Builder Methods
    fun initSquadBuilder(matchId: String) {
        val existingSquad = repository.getSquadForMatch(matchId)
        val allPlayers = repository.getPlayersForMatch(matchId)

        if (existingSquad != null && existingSquad.playerIds.isNotEmpty()) {
            val selectedPlayers = allPlayers.filter { it.id in existingSquad.playerIds }
            val validation = SquadValidationEngine.validateSquad(
                selectedPlayers,
                existingSquad.captainId,
                existingSquad.viceCaptainId
            )
            _squadBuilderState.value = SquadBuilderUiState(
                matchId = matchId,
                selectedPlayerIds = existingSquad.playerIds.toSet(),
                captainId = existingSquad.captainId,
                viceCaptainId = existingSquad.viceCaptainId,
                validationMessage = validation.message,
                isSquadValid = validation.isValid
            )
        } else {
            // Pick a suggested starter 17 players from the match pool for convenient start
            val suggested17 = allPlayers.take(17).map { it.id }.toSet()
            val captain = "p4" // Kohli
            val viceCaptain = "p18" // Bumrah
            val selected = allPlayers.filter { it.id in suggested17 }
            val validation = SquadValidationEngine.validateSquad(selected, captain, viceCaptain)

            _squadBuilderState.value = SquadBuilderUiState(
                matchId = matchId,
                selectedPlayerIds = suggested17,
                captainId = captain,
                viceCaptainId = viceCaptain,
                validationMessage = validation.message,
                isSquadValid = validation.isValid
            )
        }
    }

    fun setRoleFilter(role: PlayerRole?) {
        _squadBuilderState.update { it.copy(roleFilter = role) }
    }

    fun togglePlayerSelection(player: Player) {
        val current = _squadBuilderState.value
        val newSelected = current.selectedPlayerIds.toMutableSet()

        if (newSelected.contains(player.id)) {
            newSelected.remove(player.id)
            val newCap = if (current.captainId == player.id) null else current.captainId
            val newVc = if (current.viceCaptainId == player.id) null else current.viceCaptainId
            updateSquadValidation(current.matchId, newSelected, newCap, newVc)
        } else {
            if (newSelected.size >= 17) {
                showMessage("17 players already selected! Remove or replace a player first.", isError = true)
                return
            }
            newSelected.add(player.id)
            updateSquadValidation(current.matchId, newSelected, current.captainId, current.viceCaptainId)
        }
    }

    fun setCaptain(playerId: String) {
        val current = _squadBuilderState.value
        val newVc = if (current.viceCaptainId == playerId) null else current.viceCaptainId
        updateSquadValidation(current.matchId, current.selectedPlayerIds, playerId, newVc)
    }

    fun setViceCaptain(playerId: String) {
        val current = _squadBuilderState.value
        val newCap = if (current.captainId == playerId) null else current.captainId
        updateSquadValidation(current.matchId, current.selectedPlayerIds, newCap, playerId)
    }

    private fun updateSquadValidation(
        matchId: String,
        selectedIds: Set<String>,
        captainId: String?,
        viceCaptainId: String?
    ) {
        val allPlayers = repository.getPlayersForMatch(matchId)
        val selected = allPlayers.filter { it.id in selectedIds }
        val validation = SquadValidationEngine.validateSquad(selected, captainId, viceCaptainId)

        _squadBuilderState.value = _squadBuilderState.value.copy(
            selectedPlayerIds = selectedIds,
            captainId = captainId,
            viceCaptainId = viceCaptainId,
            validationMessage = validation.message,
            isSquadValid = validation.isValid
        )
    }

    fun saveSquad() {
        val state = _squadBuilderState.value
        if (!state.isSquadValid || state.captainId == null || state.viceCaptainId == null) {
            showMessage(state.validationMessage, isError = true)
            return
        }

        viewModelScope.launch {
            val result = repository.saveSquad(
                matchId = state.matchId,
                selectedPlayerIds = state.selectedPlayerIds.toList(),
                captainId = state.captainId,
                viceCaptainId = state.viceCaptainId
            )
            if (result.isValid) {
                _squadBuilderState.update { it.copy(showSuccessDialog = true) }
                showMessage("17-Player Squad saved successfully! Captain (2×) & Vice-Captain (1.5×) locked in.")
                // Trigger interstitial after major completion (not during live or building)
                AdMobService.showInterstitial(isLiveActive = false, isSelectingSquad = false)
            } else {
                showMessage(result.message, isError = true)
            }
        }
    }

    fun dismissSquadSuccessDialog() {
        _squadBuilderState.update { it.copy(showSuccessDialog = false) }
    }

    // Prediction actions
    fun submitPrediction(predictionId: String, optionId: String) {
        viewModelScope.launch {
            val success = repository.submitPrediction(predictionId, optionId)
            if (success) {
                showMessage("Prediction locked in! +50 Points will be credited on settlement.")
            } else {
                showMessage("Could not submit prediction. Event may be locked.", isError = true)
            }
        }
    }

    // Rewarded Ad action
    fun watchRewardedAd() {
        viewModelScope.launch {
            val result = repository.claimRewardedAdPoints()
            showMessage(result.second, isError = !result.first)
            AdMobService.dismissRewardedAd()
        }
    }

    // Referral action
    fun applyReferral(code: String) {
        viewModelScope.launch {
            val result = repository.applyReferralCode(code)
            showMessage(result.second, isError = !result.first)
        }
    }

    // Friends League actions
    fun createLeague(name: String, matchTitle: String) {
        viewModelScope.launch {
            val league = repository.createFriendsLeague(name, matchTitle)
            showMessage("Created private league '${league.name}'! Invite code: ${league.inviteCode}")
        }
    }

    fun joinLeague(code: String) {
        viewModelScope.launch {
            val result = repository.joinFriendsLeague(code)
            showMessage(result.second, isError = !result.first)
        }
    }

    // Google Play Billing Actions
    fun purchasePremium(activity: Activity) {
        billingRepository.launchPurchaseFlow(activity)
    }

    fun restorePurchases() {
        billingRepository.restorePurchases { success, message ->
            showMessage(message, isError = !success)
        }
    }

    fun retryBillingConnection() {
        billingRepository.startBillingConnection()
        billingRepository.queryProductDetails()
    }

    fun resetBillingState() {
        billingRepository.resetBillingState()
    }

    fun debugSimulatePurchase() {
        billingRepository.debugSimulatePurchaseSuccess()
        showMessage("Debug: Premium unlocked for testing.")
    }

    fun debugResetPremium() {
        billingRepository.debugResetPremium()
        showMessage("Debug: Premium entitlement reset.")
    }
}
