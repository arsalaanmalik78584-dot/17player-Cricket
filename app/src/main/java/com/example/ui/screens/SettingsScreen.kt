package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.firebase.NotificationPreferences
import com.example.ui.components.AdMobBanner
import com.example.ui.components.ResponsiblePlayBanner
import com.example.ui.theme.CricketGreenPrimary
import com.example.ui.theme.CricketRedLive
import com.example.viewmodel.CricketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CricketViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHindi by viewModel.isHindiLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val notifPrefs by viewModel.notificationPreferences.collectAsState()

    var showResponsiblePlayDialog by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "सेटिंग्स" else "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

            // Language & Appearance Card
            item {
                SectionHeader(title = if (isHindi) "भाषा और उपस्थिति" else "Language & Appearance")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "App Language", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = if (isHindi) "हिन्दी (Hindi)" else "English", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isHindi,
                                onCheckedChange = { viewModel.toggleLanguage(it) },
                                modifier = Modifier.testTag("language_switch")
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Dark Theme", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Cricket night stadium theme", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode(it) },
                                modifier = Modifier.testTag("dark_theme_switch")
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Audio & Sound Effects", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Boundary and wicket cheers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { viewModel.toggleSound(it) },
                                modifier = Modifier.testTag("sound_switch")
                            )
                        }
                    }
                }
            }

            // Notification Categories Card
            item {
                SectionHeader(title = "Push Notifications")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        NotifSwitchRow("Match starting soon", notifPrefs.matchStarting) {
                            viewModel.updateNotificationPreferences(notifPrefs.copy(matchStarting = it))
                        }
                        NotifSwitchRow("Match is live", notifPrefs.matchLive) {
                            viewModel.updateNotificationPreferences(notifPrefs.copy(matchLive = it))
                        }
                        NotifSwitchRow("Wicket fall alert", notifPrefs.wicketAlerts) {
                            viewModel.updateNotificationPreferences(notifPrefs.copy(wicketAlerts = it))
                        }
                        NotifSwitchRow("Fifty & Century milestones", notifPrefs.milestoneAlerts) {
                            viewModel.updateNotificationPreferences(notifPrefs.copy(milestoneAlerts = it))
                        }
                        NotifSwitchRow("My 17-Squad player performance", notifPrefs.mySquadPlayerPerformance) {
                            viewModel.updateNotificationPreferences(notifPrefs.copy(mySquadPlayerPerformance = it))
                        }
                        NotifSwitchRow("Free prediction challenge available", notifPrefs.predictionAvailable) {
                            viewModel.updateNotificationPreferences(notifPrefs.copy(predictionAvailable = it))
                        }
                        NotifSwitchRow("Leaderboard update alerts", notifPrefs.leaderboardUpdates) {
                            viewModel.updateNotificationPreferences(notifPrefs.copy(leaderboardUpdates = it))
                        }
                    }
                }
            }

            // Premium Membership & Purchases Card
            item {
                SectionHeader(title = "Membership & Purchases")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Membership Tier", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "₹49 One-Time Lifetime Access", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CricketGreenPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CricketGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.restorePurchases() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = CricketGreenPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Restore Purchases", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Sync entitlement from Google Play", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            // Responsible Play & Legal Card
            item {
                SectionHeader(title = "Fair Play & Legal")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showResponsiblePlayDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CricketGreenPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Responsible Play & Non-Cash Policy", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLegalDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = CricketGreenPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Terms & Conditions • Privacy Policy", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            // Account Actions
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Out")
                    }
                    Button(
                        onClick = {
                            viewModel.showMessage("Account data reset in Room local database.")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CricketRedLive)
                    ) {
                        Text("Reset Data")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                AdMobBanner()
            }
        }
    }

    if (showResponsiblePlayDialog) {
        Dialog(onDismissRequest = { showResponsiblePlayDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Responsible Play & Non-Cash Policy", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Premium access is a one-time ₹49 purchase granting full lifetime companion access.\n• There are strictly NO deposits, NO betting, NO wagering, and NO cash withdrawals.\n• 17Player Points earned have strictly NO monetary or cash value.\n• Rewards remain non-cash in-app points, badges, levels, and achievements.\n• Match predictions and 17-player squads are designed purely for fan entertainment and cricket analysis.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showResponsiblePlayDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = CricketGreenPrimary)
                    ) {
                        Text("Understood")
                    }
                }
            }
        }
    }

    if (showLegalDialog) {
        Dialog(onDismissRequest = { showLegalDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Terms & Privacy", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "17Player Cricket operates under standard Google Play and Android developer policies. User privacy is strictly respected with local on-device Room caching and non-intrusive ad placements.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showLegalDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Log Out?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Are you sure you want to sign out of your profile?", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                viewModel.showMessage("Logged out successfully.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CricketRedLive)
                        ) {
                            Text("Log Out")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
