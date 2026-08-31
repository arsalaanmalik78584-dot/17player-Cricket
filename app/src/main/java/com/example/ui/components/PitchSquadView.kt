package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.model.PlayerRole
import com.example.ui.theme.*

@Composable
fun PlayerItemRow(
    player: Player,
    isSelected: Boolean,
    isCaptain: Boolean,
    isViceCaptain: Boolean,
    onToggleSelect: () -> Unit,
    onSelectCaptain: () -> Unit,
    onSelectViceCaptain: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlayerClick() }
            .testTag("player_row_${player.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF132338) else Color(0xFF0F172A)
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) CricketGreenLight else Color(0x14FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x1AFFFFFF),
                border = BorderStroke(0.5.dp, Color(0x26FFFFFF)),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = player.avatarEmoji, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name, Team, Role
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x1AFFFFFF),
                        border = BorderStroke(0.5.dp, Color(0x26FFFFFF))
                    ) {
                        Text(
                            text = "${player.team} • ${player.role.shortLabel}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CricketGreenLight,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Pts: ${player.currentPerformance.fantasyPoints} | Credits: ${player.credits}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Captain / Vice-Captain Selector (if selected)
            if (isSelected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Captain Pill (2x)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isCaptain) CricketGold else Color(0xFF1E293B))
                            .border(1.dp, if (isCaptain) CricketGoldLight else Color(0x26FFFFFF), CircleShape)
                            .clickable { onSelectCaptain() }
                            .testTag("captain_button_${player.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2× C",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCaptain) Color.Black else Color(0xFF94A3B8)
                        )
                    }

                    // Vice Captain Pill (1.5x)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isViceCaptain) CricketGoldDark else Color(0xFF1E293B))
                            .border(1.dp, if (isViceCaptain) CricketGold else Color(0x26FFFFFF), CircleShape)
                            .clickable { onSelectViceCaptain() }
                            .testTag("vc_button_${player.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1.5 VC",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isViceCaptain) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Add/Remove Button
            FilledIconButton(
                onClick = onToggleSelect,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_player_${player.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isSelected) CricketRedLive else CricketGreenPrimary
                )
            ) {
                Text(
                    text = if (isSelected) "−" else "+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SquadSlotCounter(
    totalSelected: Int,
    wkCount: Int,
    batCount: Int,
    arCount: Int,
    bowlCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("squad_slot_counter"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0x14FFFFFF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "17-Player Squad Balance",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (totalSelected == 17) CricketGreenLight.copy(alpha = 0.2f) else CricketGold.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, if (totalSelected == 17) CricketGreenLight else CricketGold)
                ) {
                    Text(
                        text = "$totalSelected / 17 Players",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (totalSelected == 17) CricketGreenLight else CricketGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoleCounterPill("WK (2)", wkCount, 2)
                RoleCounterPill("BAT (6)", batCount, 5)
                RoleCounterPill("AR (3)", arCount, 2)
                RoleCounterPill("BOWL (5)", bowlCount, 4)
            }
        }
    }
}

@Composable
private fun RoleCounterPill(
    label: String,
    current: Int,
    minRequired: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$current",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = if (current >= minRequired) CricketGreenLight else CricketGold
        )
    }
}

