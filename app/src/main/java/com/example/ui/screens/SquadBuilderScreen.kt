package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PlayerRole
import com.example.ui.components.PlayerItemRow
import com.example.ui.components.SquadSlotCounter
import com.example.ui.theme.CricketGold
import com.example.ui.theme.CricketGreenPrimary
import com.example.ui.theme.CricketRedLive
import com.example.viewmodel.CricketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadBuilderScreen(
    viewModel: CricketViewModel,
    matchId: String,
    onBack: () -> Unit,
    onNavigateToPlayerDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.squadBuilderState.collectAsState()
    val allPlayers = viewModel.repository.getPlayersForMatch(matchId)

    val selectedPlayers = allPlayers.filter { it.id in state.selectedPlayerIds }
    val filteredPlayers = if (state.roleFilter == null) {
        allPlayers
    } else {
        allPlayers.filter { it.role == state.roleFilter }
    }

    val wkCount = selectedPlayers.count { it.role == PlayerRole.WICKET_KEEPER }
    val batCount = selectedPlayers.count { it.role == PlayerRole.BATSMAN }
    val arCount = selectedPlayers.count { it.role == PlayerRole.ALL_ROUNDER }
    val bowlCount = selectedPlayers.count { it.role == PlayerRole.BOWLER }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "17-Player Squad Builder",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pick 17: 2 WK, 6 BAT, 3 AR, 5 BOWL, 1 FLEX",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("squad_builder_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveSquad() },
                        enabled = state.isSquadValid,
                        modifier = Modifier.testTag("save_squad_top_button")
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (state.isSquadValid) CricketGreenPrimary else MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Selected: ${state.selectedPlayerIds.size} / 17",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val capName = allPlayers.find { it.id == state.captainId }?.name ?: "None"
                            val vcName = allPlayers.find { it.id == state.viceCaptainId }?.name ?: "None"
                            Text(
                                text = "Cap (2×): $capName | VC (1.5×): $vcName",
                                fontSize = 11.sp,
                                color = CricketGold
                            )
                        }

                        Button(
                            onClick = { viewModel.saveSquad() },
                            enabled = state.isSquadValid,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CricketGreenPrimary),
                            modifier = Modifier.testTag("save_squad_bottom_button")
                        ) {
                            Text("Save 17 Squad", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Squad Slot Counter
            SquadSlotCounter(
                totalSelected = state.selectedPlayerIds.size,
                wkCount = wkCount,
                batCount = batCount,
                arCount = arCount,
                bowlCount = bowlCount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Validation Warning / Message
            if (state.validationMessage.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (state.isSquadValid) CricketGreenPrimary.copy(alpha = 0.12f) else CricketRedLive.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.isSquadValid) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (state.isSquadValid) CricketGreenPrimary else CricketRedLive,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.validationMessage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (state.isSquadValid) CricketGreenPrimary else CricketRedLive
                        )
                    }
                }
            }

            // Role Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = state.roleFilter == null,
                    onClick = { viewModel.setRoleFilter(null) },
                    label = { Text("ALL (${allPlayers.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
                PlayerRole.values().forEach { role ->
                    val count = allPlayers.count { it.role == role }
                    FilterChip(
                        selected = state.roleFilter == role,
                        onClick = { viewModel.setRoleFilter(role) },
                        label = { Text("${role.shortLabel} ($count)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("role_filter_${role.name}")
                    )
                }
            }

            // Player List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredPlayers) { player ->
                    val isSelected = state.selectedPlayerIds.contains(player.id)
                    val isCaptain = state.captainId == player.id
                    val isViceCaptain = state.viceCaptainId == player.id

                    PlayerItemRow(
                        player = player,
                        isSelected = isSelected,
                        isCaptain = isCaptain,
                        isViceCaptain = isViceCaptain,
                        onToggleSelect = { viewModel.togglePlayerSelection(player) },
                        onSelectCaptain = { viewModel.setCaptain(player.id) },
                        onSelectViceCaptain = { viewModel.setViceCaptain(player.id) },
                        onPlayerClick = { onNavigateToPlayerDetail(player.id) }
                    )
                }
            }
        }
    }

    // Success Dialog
    if (state.showSuccessDialog) {
        Dialog(onDismissRequest = { viewModel.dismissSquadSuccessDialog() }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CricketGreenPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "17-Player Squad Locked!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your squad is ready for match points calculation. Captain earns 2× points and Vice-Captain earns 1.5× points during live updates!",
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.dismissSquadSuccessDialog()
                            onBack()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CricketGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Match")
                    }
                }
            }
        }
    }
}
