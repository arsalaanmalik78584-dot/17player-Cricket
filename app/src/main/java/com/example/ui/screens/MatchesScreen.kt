package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CricketMatch
import com.example.model.MatchStatus
import com.example.ui.components.AdMobBanner
import com.example.ui.components.LiveMatchCard
import com.example.ui.components.ResponsiblePlayBanner
import com.example.ui.theme.CricketGreenPrimary
import com.example.viewmodel.CricketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    viewModel: CricketViewModel,
    onNavigateToMatch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.liveMatches.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredMatches = when (selectedFilter) {
        "LIVE" -> matches.filter { it.status == MatchStatus.LIVE }
        "UPCOMING" -> matches.filter { it.status == MatchStatus.UPCOMING }
        "COMPLETED" -> matches.filter { it.status == MatchStatus.COMPLETED }
        else -> matches
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Match Center",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            ResponsiblePlayBanner()

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "All Matches", "LIVE" to "Live Now", "UPCOMING" to "Upcoming", "COMPLETED" to "Results").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("filter_chip_$key")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredMatches) { match ->
                    LiveMatchCard(
                        match = match,
                        onOpenMatch = { onNavigateToMatch(match.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    AdMobBanner()
                }
            }
        }
    }
}
