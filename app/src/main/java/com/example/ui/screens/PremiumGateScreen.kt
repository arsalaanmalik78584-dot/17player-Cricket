package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.billing.BillingUiState
import com.example.ui.theme.CricketGold
import com.example.ui.theme.CricketGoldLight
import com.example.ui.theme.CricketGreenLight
import com.example.ui.theme.CricketGreenPrimary
import com.example.ui.theme.CricketRedLive
import com.example.viewmodel.CricketViewModel

@Composable
fun PremiumGateScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val billingState by viewModel.billingState.collectAsState()
    val priceText by viewModel.premiumPriceText.collectAsState()
    val scrollState = rememberScrollState()

    val isPurchasing = billingState is BillingUiState.Purchasing
    val isRestoring = billingState is BillingUiState.Restoring
    val isPending = billingState is BillingUiState.Pending

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0D1527),
                        Color(0xFF0F172A)
                    )
                )
            )
            .testTag("premium_gate_screen")
    ) {
        // Decorative background stadium glow
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x2E10B981),
                            Color(0x1406B6D4),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Demo / Test Mode indicator for development builds
            if (BuildConfig.DEBUG) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x26F59E0B),
                    border = BorderStroke(1.dp, Color(0x66F59E0B)),
                    modifier = Modifier.padding(bottom = 8.dp).testTag("demo_test_mode_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.size(7.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DEMO / TEST MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }

            // 17Player Brand Badge
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0x1A10B981),
                border = BorderStroke(1.dp, Color(0x4D10B981))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CricketGreenLight,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "17PLAYER CRICKET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = CricketGreenLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Tagline
            Text(
                text = "17 Players. One Squad.\nPlay Cricket.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                modifier = Modifier.testTag("premium_gate_title")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error / Pending Banners
            AnimatedVisibility(
                visible = billingState is BillingUiState.Error,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val errorMsg = (billingState as? BillingUiState.Error)?.message ?: "Error connecting to Google Play"
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1515)),
                    border = BorderStroke(1.dp, CricketRedLive.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("billing_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = CricketRedLive,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = errorMsg,
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { viewModel.retryBillingConnection() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Retry", fontSize = 12.sp, color = CricketGoldLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isPending,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261C0D)),
                    border = BorderStroke(1.dp, CricketGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("billing_pending_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = CricketGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Purchase Pending",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CricketGold
                            )
                            Text(
                                text = "Payment is processing with Google Play. Access will unlock automatically once confirmed.",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }

            // Glassmorphic Premium Membership Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0x26FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("premium_pricing_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Membership Tier Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CricketGold.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, CricketGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = CricketGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PREMIUM MEMBERSHIP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = CricketGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Price Display: ₹49
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = priceText,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ONE-TIME",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CricketGreenLight,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Text(
                        text = "Unlock the complete 17Player Cricket experience.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color(0x1AFFFFFF))
                    Spacer(modifier = Modifier.height(18.dp))

                    // Benefits List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumBenefitRow(
                            icon = Icons.Default.SportsCricket,
                            text = "Live matches and live scores"
                        )
                        PremiumBenefitRow(
                            icon = Icons.Default.Group,
                            text = "17-player squad builder"
                        )
                        PremiumBenefitRow(
                            icon = Icons.Default.Analytics,
                            text = "Player statistics"
                        )
                        PremiumBenefitRow(
                            icon = Icons.Default.AutoAwesome,
                            text = "Predictions"
                        )
                        PremiumBenefitRow(
                            icon = Icons.Default.Leaderboard,
                            text = "Leaderboards"
                        )
                        PremiumBenefitRow(
                            icon = Icons.Default.EmojiEvents,
                            text = "Rewards and achievements"
                        )
                        PremiumBenefitRow(
                            icon = Icons.Default.Shield,
                            text = "Friends leagues"
                        )
                        PremiumBenefitRow(
                            icon = Icons.Default.TrendingUp,
                            text = "Advanced cricket analytics"
                        )
                        PremiumBenefitRow(
                            icon = Icons.Default.Block,
                            text = "Ad-free experience"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Action: GET PREMIUM — ₹49
            Button(
                onClick = {
                    if (activity != null) {
                        viewModel.purchasePremium(activity)
                    } else {
                        viewModel.showMessage("Cannot launch billing: Activity unavailable", isError = true)
                    }
                },
                enabled = !isPurchasing && !isRestoring,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CricketGreenPrimary,
                    disabledContainerColor = CricketGreenPrimary.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("get_premium_button")
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Connecting to Google Play...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GET PREMIUM — $priceText",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Premium Test Mode — No real payment",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFBBF24),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("debug_test_mode_label")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Action: RESTORE PURCHASE
            OutlinedButton(
                onClick = { viewModel.restorePurchases() },
                enabled = !isPurchasing && !isRestoring,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFCBD5E1)
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restore_purchase_button")
            ) {
                if (isRestoring) {
                    CircularProgressIndicator(
                        color = CricketGreenLight,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Checking Google Play...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESTORE PURCHASE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Google Play Security & Legal Notices
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Secure payment through Google Play",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Premium access is a one-time $priceText purchase. 17Player Points have no cash value.",
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Non-cash in-app points. Strictly no deposits, withdrawals, betting, wagering, or cash prizes.",
                fontSize = 9.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )

            // DEBUG / TESTING CONTROLS (strictly BuildConfig.DEBUG only)
            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    modifier = Modifier.fillMaxWidth().testTag("debug_test_panel")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🛠️ DEBUG TESTING PANEL (DEV ONLY)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CricketGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { viewModel.debugSimulatePurchase() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("debug_simulate_purchase_button")
                            ) {
                                Text("Simulate Unlock", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.debugResetPremium() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("debug_reset_premium_button")
                            ) {
                                Text("Reset Gate", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PremiumBenefitRow(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0x2610B981),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CricketGreenLight,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE2E8F0)
        )
    }
}
