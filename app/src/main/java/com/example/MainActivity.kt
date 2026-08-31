package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.ads.AdMobService
import com.example.ui.screens.*
import com.example.ui.theme.CricketGold
import com.example.ui.theme.CricketGreenLight
import com.example.ui.theme.CricketGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CricketViewModel

enum class MainBottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("home", "Home", Icons.Default.SportsCricket, "nav_home"),
    MATCHES("matches", "Matches", Icons.Default.CalendarMonth, "nav_matches"),
    PREDICTIONS("predictions", "Predict", Icons.Default.AutoAwesome, "nav_predictions"),
    LEADERBOARD("leaderboard", "Ranks", Icons.Default.Leaderboard, "nav_leaderboard"),
    REWARDS("rewards", "Rewards", Icons.Default.EmojiEvents, "nav_rewards"),
    PROFILE("profile", "Profile", Icons.Default.Person, "nav_profile")
}

class MainActivity : ComponentActivity() {

    private val viewModel: CricketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val isPremiumUser by viewModel.isPremiumUser.collectAsState()
            var isSplashFinished by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                if (!isSplashFinished) {
                    SplashScreen(
                        onSplashFinished = {
                            isSplashFinished = true
                        }
                    )
                } else if (isPremiumUser) {
                    MainAppContainer(viewModel = viewModel)
                } else {
                    val snackbarHostState = remember { SnackbarHostState() }
                    val uiMessage by viewModel.uiMessage.collectAsState()

                    LaunchedEffect(uiMessage) {
                        uiMessage?.let {
                            snackbarHostState.showSnackbar(
                                message = it.message,
                                duration = SnackbarDuration.Short
                            )
                            viewModel.clearUiMessage()
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        PremiumGateScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-verify Google Play purchases whenever the Activity resumes
        viewModel.billingRepository.queryExistingPurchases()
    }
}

@Composable
fun MainAppContainer(viewModel: CricketViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val uiMessage by viewModel.uiMessage.collectAsState()
    val isShowingInterstitial by AdMobService.isShowingInterstitial.collectAsState()

    // Handle Snackbar messages
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(
                message = it.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUiMessage()
        }
    }

    val bottomBarRoutes = listOf("home", "matches", "predictions", "leaderboard", "rewards", "profile")
    val shouldShowBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF070B14),
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        BorderStroke(1.dp, Color(0x14FFFFFF))
                    )
                ) {
                    MainBottomNavDestination.values().forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (selected) Color(0x2610B981) else Color.Transparent
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                        tint = if (selected) CricketGreenLight else Color(0xFF64748B)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) CricketGreenLight else Color(0xFF64748B)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainBottomNavDestination.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToMatch = { matchId -> navController.navigate("match_detail/$matchId") },
                    onNavigateToPredictions = { navController.navigate("predictions") },
                    onNavigateToSquadBuilder = { matchId -> navController.navigate("squad_builder/$matchId") },
                    onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                    onNavigateToRewards = { navController.navigate("rewards") }
                )
            }

            composable("matches") {
                MatchesScreen(
                    viewModel = viewModel,
                    onNavigateToMatch = { matchId -> navController.navigate("match_detail/$matchId") }
                )
            }

            composable(
                route = "match_detail/{matchId}",
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) { backStack ->
                val matchId = backStack.arguments?.getString("matchId") ?: "match_ind_aus_01"
                MatchDetailScreen(
                    viewModel = viewModel,
                    matchId = matchId,
                    onBack = { navController.popBackStack() },
                    onNavigateToSquadBuilder = { mId -> navController.navigate("squad_builder/$mId") },
                    onNavigateToPlayerDetail = { pId -> navController.navigate("player_detail/$pId") }
                )
            }

            composable(
                route = "squad_builder/{matchId}",
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) { backStack ->
                val matchId = backStack.arguments?.getString("matchId") ?: "match_ind_aus_01"
                SquadBuilderScreen(
                    viewModel = viewModel,
                    matchId = matchId,
                    onBack = { navController.popBackStack() },
                    onNavigateToPlayerDetail = { pId -> navController.navigate("player_detail/$pId") }
                )
            }

            composable("predictions") {
                PredictionsScreen(viewModel = viewModel)
            }

            composable("leaderboard") {
                LeaderboardScreen(
                    viewModel = viewModel,
                    onNavigateToFriendsLeague = { navController.navigate("friends_league") }
                )
            }

            composable("rewards") {
                RewardsScreen(
                    viewModel = viewModel,
                    onNavigateToReferral = { navController.navigate("referral") }
                )
            }

            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToReferral = { navController.navigate("referral") },
                    onNavigateToFriendsLeague = { navController.navigate("friends_league") },
                    onNavigateToRewards = { navController.navigate("rewards") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("referral") {
                ReferralScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("friends_league") {
                FriendsLeagueScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "player_detail/{playerId}",
                arguments = listOf(navArgument("playerId") { type = NavType.StringType })
            ) { backStack ->
                val playerId = backStack.arguments?.getString("playerId") ?: "p4"
                PlayerDetailScreen(
                    viewModel = viewModel,
                    playerId = playerId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Safe Interstitial Ad Presentation Dialog
    if (isShowingInterstitial) {
        Dialog(onDismissRequest = { AdMobService.dismissInterstitial() }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("interstitial_ad_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CricketGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "SPONSORED ADVERTISEMENT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CricketGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { AdMobService.dismissInterstitial() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Ad")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Official Cricket Merchandise & Kit Gear",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Explore exclusive cricket gear, pro batting gloves, and authentic match jerseys with fast express delivery.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { AdMobService.dismissInterstitial() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CricketGreenPrimary)
                    ) {
                        Text("Continue to 17Player Cricket")
                    }
                }
            }
        }
    }
}

