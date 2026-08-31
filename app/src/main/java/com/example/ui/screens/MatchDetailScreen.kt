package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.DemoCricketData
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.CricketViewModel

enum class MatchDetailTab(val title: String) {
    LIVE("LIVE"),
    SQUAD("SQUAD"),
    POINTS("POINTS"),
    PREDICT("PREDICT"),
    LEADERBOARD("RANKS"),
    STATS("STATS")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    viewModel: CricketViewModel,
    matchId: String,
    onBack: () -> Unit,
    onNavigateToSquadBuilder: (String) -> Unit,
    onNavigateToPlayerDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(matchId) {
        viewModel.selectMatch(matchId)
    }

    val match by viewModel.selectedMatch.collectAsState()
    val squadsMap by viewModel.currentSquads.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val userSquad = squadsMap[matchId]

    var selectedTab by remember { mutableStateOf(MatchDetailTab.LIVE) }

    val activeMatch = match ?: DemoCricketData.demoMatches.first()
    val matchPredictions = predictions.filter { it.matchId == matchId }
    val allPlayers = viewModel.repository.getPlayersForMatch(matchId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${activeMatch.team1Short} vs ${activeMatch.team2Short}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeMatch.venue,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("match_detail_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (activeMatch.isDemo) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "DEMO DATA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Score Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Team 1
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = activeMatch.team1LogoEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = activeMatch.team1Name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeMatch.team1Score} (${activeMatch.team1Overs} ov)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CricketGreenPrimary
                                )
                            }
                        }

                        // Team 2
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = activeMatch.team2Name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (activeMatch.team2Score.isNotEmpty()) "${activeMatch.team2Score} (${activeMatch.team2Overs} ov)" else "Yet to bat",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = activeMatch.team2LogoEmoji, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CricketRedLive.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeMatch.statusSummary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CricketRedLive,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }

            // Official Broadcaster CTA
            WatchLiveBroadcasterButton(
                broadcasterUrl = activeMatch.broadcasterUrl,
                broadcasterName = activeMatch.broadcasterName,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CricketGreenPrimary
            ) {
                MatchDetailTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name}")
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                MatchDetailTab.LIVE -> LiveTabContent(activeMatch)
                MatchDetailTab.SQUAD -> SquadTabContent(
                    matchId = matchId,
                    userSquad = userSquad,
                    allPlayers = allPlayers,
                    onEditSquad = { onNavigateToSquadBuilder(matchId) },
                    onPlayerClick = onNavigateToPlayerDetail
                )
                MatchDetailTab.POINTS -> PointsTabContent(userSquad, allPlayers)
                MatchDetailTab.PREDICT -> PredictTabContent(
                    predictions = matchPredictions,
                    onSelectOption = { predId, optId -> viewModel.submitPrediction(predId, optId) }
                )
                MatchDetailTab.LEADERBOARD -> LeaderboardTabContent()
                MatchDetailTab.STATS -> StatsTabContent(allPlayers, onPlayerClick = onNavigateToPlayerDetail)
            }
        }
    }
}

@Composable
private fun LiveTabContent(match: CricketMatch) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current Batsmen Table
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Current Batsmen",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    match.currentBatsmen.forEach { bat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (bat.isStriker) "${bat.name} *" else bat.name,
                                fontSize = 13.sp,
                                fontWeight = if (bat.isStriker) FontWeight.Bold else FontWeight.Normal,
                                color = if (bat.isStriker) CricketGreenPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${bat.runs} (${bat.balls})  4s: ${bat.fours}  6s: ${bat.sixes}  SR: ${bat.strikeRate}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Current Bowlers Table
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Current Bowler",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    match.currentBowlers.forEach { bowl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${bowl.name} *",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CricketGreenPrimary
                            )
                            Text(
                                text = "${bowl.overs}-${bowl.maidens}-${bowl.runs}-${bowl.wickets} (Econ: ${bowl.economy})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Partnership & Fall of Wickets
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Partnership: ${match.partnershipRuns} runs (${match.partnershipBalls} balls)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "CRR: ${match.currentRunRate}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CricketGreenPrimary
                        )
                    }
                    if (match.fallOfWickets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fall of Wickets: " + match.fallOfWickets.joinToString(", ") { "${it.score}/${it.wicketNumber} (${it.playerOut}, ${it.over})" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Ball by ball commentary
        item {
            BallByBallRow(ballEvents = match.recentBalls)
        }

        items(match.recentBalls) { ball ->
            BallCommentaryCard(ball = ball)
        }
    }
}

@Composable
private fun SquadTabContent(
    matchId: String,
    userSquad: UserSquad?,
    allPlayers: List<Player>,
    onEditSquad: () -> Unit,
    onPlayerClick: (String) -> Unit
) {
    if (userSquad == null || userSquad.playerIds.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🏏", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No 17-Player Squad Created",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Create your 17-player squad (2 WK, 6 BAT, 3 AR, 5 BOWL, 1 FLEX) with 2× Captain and 1.5× Vice-Captain to earn points!",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onEditSquad,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CricketGreenPrimary)
                ) {
                    Text("Build 17-Player Squad")
                }
            }
        }
    } else {
        val selectedPlayers = allPlayers.filter { it.id in userSquad.playerIds }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My 17-Player Squad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Captain (2×) & Vice-Captain (1.5×) Applied",
                            fontSize = 11.sp,
                            color = CricketGoldDark
                        )
                    }
                    Button(
                        onClick = onEditSquad,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Edit Squad", fontSize = 12.sp)
                    }
                }
            }

            items(selectedPlayers) { player ->
                val isCap = player.id == userSquad.captainId
                val isVc = player.id == userSquad.viceCaptainId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayerClick(player.id) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = player.avatarEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = player.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isCap) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = CricketGold) {
                                            Text(
                                                text = "2× CAP",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    if (isVc) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = CricketGoldDark) {
                                            Text(
                                                text = "1.5× VC",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${player.team} • ${player.role.label}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${player.currentPerformance.fantasyPoints} Pts",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CricketGreenPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PointsTabContent(userSquad: UserSquad?, allPlayers: List<Player>) {
    val selectedPlayers = if (userSquad != null) allPlayers.filter { it.id in userSquad.playerIds } else allPlayers.take(17)
    val captainId = userSquad?.captainId ?: "p4"
    val viceCaptainId = userSquad?.viceCaptainId ?: "p18"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CricketGreenPrimary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Fantasy Points Breakdown",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configurable scoring rules: Runs (+1), Fours (+1 bonus), Sixes (+2 bonus), Wickets (+20), Maidens (+10), Catches (+10), Captain (2×), Vice-Captain (1.5×)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(selectedPlayers) { player ->
            val isCap = player.id == captainId
            val isVc = player.id == viceCaptainId
            val perf = player.currentPerformance
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${player.name} ${if (isCap) "(2× C)" else if (isVc) "(1.5× VC)" else ""}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Runs: ${perf.runs} (${perf.fours}x4, ${perf.sixes}x6) | Wkts: ${perf.wickets} | Catches: ${perf.catches}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${perf.fantasyPoints} Pts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CricketGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictTabContent(
    predictions: List<PredictionQuestion>,
    onSelectOption: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CricketGold.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔮", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "100% Free Live Match Predictions",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Make free predictions before the event occurs to earn in-app points!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(predictions) { pred ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (pred.status == PredictionStatus.OPEN) CricketGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = pred.status.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pred.status == PredictionStatus.OPEN) CricketOnGreenContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "+${pred.pointsReward} Points",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CricketGoldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = pred.question,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    pred.options.forEach { opt ->
                        val isSelected = pred.userSelectedOptionId == opt.id
                        OutlinedButton(
                            onClick = { onSelectOption(pred.id, opt.id) },
                            enabled = pred.status == PredictionStatus.OPEN,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = CricketGreenContainer.copy(alpha = 0.5f)) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = opt.text,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Text(
                                        text = "✓ Selected",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CricketGreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardTabContent() {
    val entries = DemoCricketData.demoGlobalLeaderboard
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(entries) { entry ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (entry.isCurrentUser) CricketGreenContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${entry.rank}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (entry.rank <= 3) CricketGoldDark else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = entry.avatarEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = entry.username,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = entry.badge,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${entry.points} Pts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CricketGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsTabContent(
    allPlayers: List<Player>,
    onPlayerClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(allPlayers) { player ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayerClick(player.id) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = player.avatarEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = player.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${player.team} • ${player.role.label}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Runs: ${player.totalRuns} | Wkts: ${player.totalWickets}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SR: ${player.strikeRate} | Econ: ${player.economy}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
