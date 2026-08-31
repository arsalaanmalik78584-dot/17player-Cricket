package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CricketMatch
import com.example.model.MatchStatus
import com.example.ui.theme.*

@Composable
fun LiveMatchCard(
    match: CricketMatch,
    onOpenMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLive = match.status == MatchStatus.LIVE

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenMatch() }
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLive) Color(0xFF0F172A) else Color(0xFF131C31)
        ),
        border = BorderStroke(
            1.dp,
            if (isLive) Color(0x3310B981) else Color(0x14FFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLive) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isLive) {
                        Brush.verticalGradient(
                            listOf(
                                Color(0x55312E81), // Indigo 900 tint
                                Color(0xFF0F172A),
                                Color(0xFF0B1120)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF162036),
                                Color(0xFF0F172A)
                            )
                        )
                    }
                )
                .padding(20.dp)
        ) {
            Column {
                // Header: Tournament + Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = match.tournament.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (isLive) CricketGreenLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (match.isDemo) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x1FFFFFFF),
                                border = BorderStroke(0.5.dp, Color(0x33FFFFFF))
                            ) {
                                Text(
                                    text = "DEMO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (isLive) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CricketRedLive
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x1AFFFFFF),
                            border = BorderStroke(0.5.dp, Color(0x26FFFFFF))
                        ) {
                            Text(
                                text = match.status.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Teams & Score Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Team 1
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(68.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0x14FFFFFF),
                            border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = match.team1LogoEmoji, fontSize = 26.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = match.team1Name.take(3).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }

                    // Center Scores & Overs
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        if (match.team1Score.isNotEmpty() || match.team2Score.isNotEmpty()) {
                            val activeScore = if (match.team2Score.isNotEmpty()) "${match.team1Score} & ${match.team2Score}" else match.team1Score
                            Text(
                                text = match.team1Score.ifEmpty { "vs" },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                color = Color.White
                            )
                            if (match.team1Overs.isNotEmpty()) {
                                Text(
                                    text = "${match.team1Overs} Overs",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CricketGreenLight
                                )
                            }
                        } else {
                            Text(
                                text = "VS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0x66FFFFFF)
                            )
                            Text(
                                text = "Starting Soon",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Team 2
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(68.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0x14FFFFFF),
                            border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = match.team2LogoEmoji, fontSize = 26.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = match.team2Name.take(3).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Divider(color = Color(0x14FFFFFF))

                Spacer(modifier = Modifier.height(14.dp))

                // Footer: Striker status + Action Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = if (isLive) "ON STRIKE" else "MATCH STATUS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = match.statusSummary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    Surface(
                        onClick = onOpenMatch,
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x1A10B981),
                        border = BorderStroke(1.dp, Color(0x3310B981)),
                        modifier = Modifier.testTag("open_match_button_${match.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isLive) Icons.Default.PlayArrow else Icons.Default.SportsCricket,
                                contentDescription = null,
                                tint = CricketGreenLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLive) "WATCH LIVE" else "VIEW MATCH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = CricketGreenLight
                            )
                        }
                    }
                }
            }
        }
    }
}

