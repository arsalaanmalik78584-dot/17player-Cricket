package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CricketCyan
import com.example.ui.theme.CricketCyanLight
import com.example.ui.theme.CricketGreenLight
import com.example.ui.theme.CricketGreenPrimary
import kotlinx.coroutines.delay

/**
 * Launch Splash Screen for 17Player Cricket.
 *
 * Displays the 17Player Cricket branding, the new cricket icon logo with prominent "17",
 * app title, and "17 Players. One Squad. Play Cricket." tagline over an obsidian & midnight
 * canvas with emerald and cyber-cyan accents.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    // Animation States
    val transitionScale = remember { Animatable(0.85f) }
    val transitionAlpha = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0.1f) }

    // Infinite ambient pulse for neon backlights
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Trigger entrance animation & transition
    LaunchedEffect(Unit) {
        // Animate entrance scale and alpha
        transitionAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        transitionScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        // Animate progress bar smoothly
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1600, easing = LinearEasing)
        )
        delay(200) // Brief moment at 100%
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0F172A),
                        Color(0xFF070B14)
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Neon Glow Orbs in Background
        Box(
            modifier = Modifier
                .size(320.dp)
                .blur(80.dp)
                .alpha(glowAlpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CricketGreenPrimary.copy(alpha = 0.45f),
                            CricketCyan.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main App Logo Container with Glassmorphism & Shadow
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .scale(transitionScale.value)
                    .alpha(transitionAlpha.value)
                    .testTag("splash_logo"),
                shape = RoundedCornerShape(32.dp),
                color = Color(0x330F172A),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CricketCyanLight.copy(alpha = 0.8f),
                            CricketGreenLight.copy(alpha = 0.4f),
                            Color(0x22FFFFFF)
                        )
                    )
                ),
                shadowElevation = 16.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_17player_logo),
                        contentDescription = "17Player Cricket Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with Emerald & Cyber-Cyan Accents
            Text(
                text = "17Player Cricket",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(transitionAlpha.value)
                    .testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline: "17 Players. One Squad. Play Cricket."
            Surface(
                modifier = Modifier
                    .alpha(transitionAlpha.value)
                    .testTag("splash_tagline"),
                shape = RoundedCornerShape(20.dp),
                color = Color(0x1A06B6D4),
                border = BorderStroke(1.dp, Color(0x3306B6D4))
            ) {
                Text(
                    text = "17 Players. One Squad. Play Cricket.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    color = CricketCyanLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Subtle Progress Bar and Loading status
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .alpha(transitionAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Custom Slim Glowing Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Color(0x33334155))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = progressAnim.value)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(CricketGreenPrimary, CricketCyan)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Initializing Match Engine...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.4.sp
                )
            }
        }

        // Bottom Brand Footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(transitionAlpha.value)
        ) {
            Text(
                text = "v1.0 • Official 17-Player Companion",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF475569),
                letterSpacing = 0.5.sp
            )
        }
    }
}
