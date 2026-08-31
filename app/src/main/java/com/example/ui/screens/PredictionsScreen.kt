package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PredictionOption
import com.example.model.PredictionQuestion
import com.example.model.PredictionStatus
import com.example.ui.components.AdMobBanner
import com.example.ui.components.ResponsiblePlayBanner
import com.example.ui.theme.CricketGold
import com.example.ui.theme.CricketGoldDark
import com.example.ui.theme.CricketGreenContainer
import com.example.ui.theme.CricketGreenPrimary
import com.example.ui.theme.CricketOnGreenContainer
import com.example.viewmodel.CricketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionsScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val predictions by viewModel.predictions.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Free Predictions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CricketGold.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "🎯", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile.correctPredictions}/${profile.totalPredictions} Won",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ResponsiblePlayBanner()
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CricketGreenPrimary.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CricketGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How Free Match Predictions Work",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• All prediction challenges have ₹0 Entry Fee.\n• Win between +50 to +100 In-App Points per correct pick.\n• In-App Points boost your leaderboard ranking & unlock achievement badges.\n• Strictly no money or cash prizes.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(predictions) { pred ->
                PredictionCardItem(
                    prediction = pred,
                    onSelect = { optId -> viewModel.submitPrediction(pred.id, optId) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                AdMobBanner()
            }
        }
    }
}

@Composable
private fun PredictionCardItem(
    prediction: PredictionQuestion,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prediction_card_${prediction.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (prediction.status == PredictionStatus.OPEN) CricketGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (prediction.status == PredictionStatus.OPEN) "OPEN • ${prediction.closingOverText}" else prediction.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (prediction.status == PredictionStatus.OPEN) CricketOnGreenContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CricketGold.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+${prediction.pointsReward} Pts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CricketGoldDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = prediction.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            prediction.options.forEach { opt ->
                val isSelected = prediction.userSelectedOptionId == opt.id
                val isCorrect = prediction.correctOptionId == opt.id
                val isSettled = prediction.status == PredictionStatus.SETTLED

                OutlinedButton(
                    onClick = { onSelect(opt.id) },
                    enabled = prediction.status == PredictionStatus.OPEN,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("pred_opt_${opt.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (isSelected) {
                        ButtonDefaults.outlinedButtonColors(containerColor = CricketGreenContainer.copy(alpha = 0.6f))
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = opt.text,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isSettled && isCorrect) {
                            Text(
                                text = "✓ Correct Outcome",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CricketGreenPrimary
                            )
                        } else if (isSelected) {
                            Text(
                                text = "Your Pick",
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
