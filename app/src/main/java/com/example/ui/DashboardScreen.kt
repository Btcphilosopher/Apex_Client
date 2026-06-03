package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.ripple.rememberRipple
import kotlinx.coroutines.delay
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ClientAlert
import com.example.data.DownloadJob
import com.example.data.FriendActivity
import com.example.data.Game
import com.example.ui.theme.*
import com.example.viewmodel.ApexViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ApexLauncherDashboard(
    viewModel: ApexViewModel,
    modifier: Modifier = Modifier
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val downloads by viewModel.downloadJobs.collectAsStateWithLifecycle()
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val recommendedStoreCatalog by viewModel.recommendedStoreCatalog.collectAsStateWithLifecycle()

    val primaryAccent = Color(android.graphics.Color.parseColor(viewModel.neonAccentHex))

    // Clock state
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            currentTime = sdf.format(Date())
            delay(1000L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(ApexDeepDark, ApexObsidian),
                    center = Offset(400f, 300f),
                    radius = 1800f
                )
            )
            .drawBehind {
                // Cyber Grid pattern simulation
                val strokeWidth = 1f
                val gridSpacing = 80f
                for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                    drawLine(
                        color = primaryAccent.copy(alpha = 0.04f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = strokeWidth
                    )
                }
                for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                    drawLine(
                        color = primaryAccent.copy(alpha = 0.04f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = strokeWidth
                    )
                }
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. LEFT SIDEBAR NAVIGATION
            SidebarNavigation(
                activeTab = viewModel.activeTab,
                onTabSelected = { viewModel.activeTab = it },
                accentColor = primaryAccent,
                hasActiveDownloads = downloads.any { it.status == "DOWNLOADING" },
                alertCount = alerts.filter { !it.isRead }.size
            )

            // MAIN CONTENT ROW/COLUMN
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                // 2. TOP SYSTEM COMMAND BAR
                TopCommandBar(
                    gamerTag = viewModel.gamerTag,
                    timeString = currentTime,
                    ping = "14ms SPEEDWAY",
                    accentColor = primaryAccent,
                    downloadCount = downloads.size,
                    alertCount = alerts.size,
                    onToggleDownloads = { viewModel.isDownloadManagerOpen = !viewModel.isDownloadManagerOpen },
                    onToggleAlerts = { viewModel.isAlertsOverlayOpen = !viewModel.isAlertsOverlayOpen }
                )

                // MAIN CONTENT PANEL VIEW SELECTOR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AnimatedContent(
                        targetState = viewModel.activeTab,
                        transitionSpec = {
                            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) with
                                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                        },
                        label = "MainTabsTransitions"
                    ) { tab ->
                        when (tab) {
                            "STORE" -> StoreView(
                                games = games,
                                recommended = recommendedStoreCatalog,
                                accentColor = primaryAccent,
                                onGameClicked = { game -> viewModel.selectedDetailsGame = game }
                            )
                            "LIBRARY" -> LibraryView(
                                games = games,
                                accentColor = primaryAccent,
                                onGameClicked = { game -> viewModel.selectedDetailsGame = game },
                                onQuickLaunch = { game -> viewModel.launchGameSession(game) }
                            )
                            "SOCIAL" -> SocialDashboardView(
                                friends = friends,
                                alerts = alerts,
                                accentColor = primaryAccent,
                                gamerTag = viewModel.gamerTag
                            )
                            "SETTINGS" -> SettingsView(
                                gamerTag = viewModel.gamerTag,
                                activeAccent = viewModel.neonAccentHex,
                                soundEnabled = viewModel.soundEnabled,
                                columns = viewModel.desktopGridColumns,
                                alerts = alerts,
                                onTagChanged = { viewModel.changeGamerTag(it) },
                                onAccentChanged = { viewModel.changeNeonAccent(it) },
                                onSoundToggled = { viewModel.soundEnabled = it },
                                onColumnsChanged = { viewModel.desktopGridColumns = it },
                                onClearLogs = { viewModel.markAllAlertsRead() }
                            )
                        }
                    }
                }

                // 3. BOTTOM ACTIVITY/TASK BAR
                BottomActivityBar(
                    installedGames = games.filter { it.isInstalled },
                    activeRunning = viewModel.activeRunningGame,
                    accentColor = primaryAccent,
                    onLaunch = { game -> viewModel.launchGameSession(game) }
                )
            }
        }

        // 4. FLOATING WINDOWS / OVERLAYS

        // Game Details Layer Modal Panel
        viewModel.selectedDetailsGame?.let { game ->
            val downloadActive = downloads.find { it.gameId == game.id }
            GameDetailsOverlay(
                game = game,
                downloadJob = downloadActive,
                accentColor = primaryAccent,
                onClose = { viewModel.selectedDetailsGame = null },
                onInstall = { viewModel.installGame(game.id) },
                onLaunch = {
                    viewModel.selectedDetailsGame = null
                    viewModel.launchGameSession(game)
                },
                onUpdate = { viewModel.updateGame(game.id) }
            )
        }

        // Draggable Download Manager Window
        if (viewModel.isDownloadManagerOpen) {
            DraggableDownloadWindow(
                jobs = downloads,
                accentColor = primaryAccent,
                onClose = { viewModel.isDownloadManagerOpen = false },
                onPause = { viewModel.pauseDownloadJob(it) },
                onResume = { viewModel.resumeDownloadJob(it) },
                onCancel = { viewModel.cancelDownloadJob(it) }
            )
        }

        // Draggable System Alerts Overlay
        if (viewModel.isAlertsOverlayOpen) {
            DraggableAlertsWindow(
                alerts = alerts,
                accentColor = primaryAccent,
                onClose = { viewModel.isAlertsOverlayOpen = false },
                onClearAll = { viewModel.markAllAlertsRead() }
            )
        }
    }
}

// ==========================================
// COMPONENT 1: SIDEBAR NAVIGATION
// ==========================================
@Composable
fun SidebarNavigation(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    accentColor: Color,
    hasActiveDownloads: Boolean,
    alertCount: Int
) {
    val scrollAnim by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(ApexDeepDark)
            .drawBehind {
                drawLine(
                    color = ApexEdgeBorder,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Apex Logo Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent)))
                    .border(1.5.dp, accentColor, CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "APEX CORE",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs
            SidebarNavItem(
                icon = Icons.Default.Storefront,
                label = "STORE",
                isActive = activeTab == "STORE",
                accentColor = accentColor,
                onClick = { onTabSelected("STORE") }
            )

            SidebarNavItem(
                icon = Icons.Default.VideogameAsset,
                label = "LIBRARY",
                isActive = activeTab == "LIBRARY",
                accentColor = accentColor,
                onClick = { onTabSelected("LIBRARY") }
            )

            SidebarNavItem(
                icon = Icons.Default.PeopleOutline,
                label = "SOCIAL",
                isActive = activeTab == "SOCIAL",
                accentColor = accentColor,
                onClick = { onTabSelected("SOCIAL") },
                badgeCount = if (alertCount > 0) alertCount else 0
            )

            SidebarNavItem(
                icon = Icons.Default.Settings,
                label = "SETTINGS",
                isActive = activeTab == "SETTINGS",
                accentColor = accentColor,
                onClick = { onTabSelected("SETTINGS") }
            )
        }

        // Live telemetry active pulsing node
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (hasActiveDownloads) 14.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (hasActiveDownloads) ApexHotPink else accentColor)
                    .border(
                        width = if (hasActiveDownloads) scrollAnim.dp else 1.dp,
                        color = if (hasActiveDownloads) ApexHotPink.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun SidebarNavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) accentColor.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isActive) accentColor.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .testTag("nav_tab_${label.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) accentColor else ApexTextGray,
                    modifier = Modifier.size(24.dp)
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(ApexHotPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = label,
                color = if (isActive) Color.White else ApexTextGray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ==========================================
// COMPONENT 2: TOP SYSTEM COMMAND BAR
// ==========================================
@Composable
fun TopCommandBar(
    gamerTag: String,
    timeString: String,
    ping: String,
    accentColor: Color,
    downloadCount: Int,
    alertCount: Int,
    onToggleDownloads: () -> Unit,
    onToggleAlerts: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(ApexDeepDark)
            .drawBehind {
                drawLine(
                    color = ApexEdgeBorder,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Pilot gamerTag Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Pilot",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = gamerTag,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RANK: PILOT V-9",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Center Clock
        Box(
            modifier = Modifier
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .background(ApexObsidian.copy(alpha = 0.8f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = "CORESYNC // $timeString",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Telemetry system buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Ping Node
            Box(
                modifier = Modifier
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = ping,
                    color = accentColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Toggle Downloads Overlay button
            IconButton(
                onClick = onToggleDownloads,
                modifier = Modifier.testTag("toggle_downloads_button")
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = if (downloadCount > 0) Icons.Default.Download else Icons.Outlined.FileDownload,
                        contentDescription = "Downloads Manager",
                        tint = if (downloadCount > 0) ApexCyberGold else Color.White
                    )
                    if (downloadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ApexCyberGold)
                        )
                    }
                }
            }

            // Toggle Alerts Overlay button
            IconButton(
                onClick = onToggleAlerts,
                modifier = Modifier.testTag("toggle_alerts_button")
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = if (alertCount > 0) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                        contentDescription = "System Alerts Logs",
                        tint = if (alertCount > 0) accentColor else Color.White
                    )
                    if (alertCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ApexHotPink)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 3: STORE VIEW
// ==========================================
@Composable
fun StoreView(
    games: List<Game>,
    recommended: List<Game>,
    accentColor: Color,
    onGameClicked: (Game) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("ALL") }

    val filteredGames = games.filter { game ->
        val matchesSearch = game.title.contains(searchQuery, ignoreCase = true) ||
                game.tags.any { it.contains(searchQuery, ignoreCase = true) }
        val matchesGenre = selectedGenre == "ALL" || game.genre.equals(selectedGenre, ignoreCase = true)
        matchesSearch && matchesGenre
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Genre Filter Toolbar
        item {
            StoreHeaderToolbar(
                searchQuery = searchQuery,
                onSearchChanged = { searchQuery = it },
                selectedGenre = selectedGenre,
                onGenreChanged = { selectedGenre = it },
                accentColor = accentColor
            )
        }

        // Spotlight Featured Carousel Banner (Only show when SEARCH is empty and ALL genre is focused)
        if (searchQuery.isEmpty() && selectedGenre == "ALL" && recommended.isNotEmpty()) {
            item {
                FeaturedSpotlightBanner(
                    game = recommended.first(),
                    accentColor = accentColor,
                    onClick = { onGameClicked(recommended.first()) }
                )
            }
        }

        // Dynamic local Recommendations section
        if (searchQuery.isEmpty() && selectedGenre == "ALL" && recommended.size >= 2) {
            item {
                Text(
                    text = "DYNAMICS // RECOMMENDED FOR PILOT",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recommended_catalog_row")
                ) {
                    items(recommended.drop(1)) { game ->
                        RecommendedMiniCard(
                            game = game,
                            accentColor = accentColor,
                            onClick = { onGameClicked(game) }
                        )
                    }
                }
            }
        }

        // Full Shop Catalog List
        item {
            Text(
                text = "INDEX // CATALOG SECTOR",
                color = ApexCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        if (filteredGames.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Database Desynced",
                    desc = "No matching executables found in this quadrant of the storefront."
                )
            }
        } else {
            items(filteredGames.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pair.forEach { game ->
                        Box(modifier = Modifier.weight(1f)) {
                            CatalogStoreCard(
                                game = game,
                                accentColor = accentColor,
                                onClick = { onGameClicked(game) }
                            )
                        }
                    }
                    if (pair.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun StoreHeaderToolbar(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selectedGenre: String,
    onGenreChanged: (String) -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1F161925), RoundedCornerShape(8.dp))
            .border(1.dp, ApexEdgeBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text("SEARCH VECTOR CHANNELS...", color = ApexTextGray, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = accentColor) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0x4400FFCC),
                    focusedContainerColor = Color(0xBB07080C),
                    unfocusedContainerColor = Color(0xBB07080C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("store_search_input")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Genres Row scrollable Tab Row
        val genres = listOf("ALL", "ACTION RPG", "FUTURISTIC RACING", "ATMOSPHERIC HORROR", "STRATEGY", "FIGHTING")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(genres) { genre ->
                val isSelected = selectedGenre == genre
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) accentColor else Color(0x338A93A6),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onGenreChanged(genre) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("genre_tab_$genre"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = genre,
                        color = if (isSelected) Color.White else ApexTextGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedSpotlightBanner(
    game: Game,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .border(1.dp, accentColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("featured_banner_${game.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ApexDeepDark.copy(alpha = 0.5f), ApexObsidian),
                        startY = 0f,
                        endY = 500f
                    )
                )
                .drawBehind {
                    // Futuristic glowing ambient vector drawing on banner
                    drawCircle(
                        color = accentColor.copy(alpha = 0.15f),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.45f
                    )
                }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.65f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(ApexHotPink, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "HOT // RECOMMENDED DECK",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = game.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "DEVS // ${game.developer.uppercase()}",
                        color = ApexTextGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (game.price == 0.0) "FREE TO RUN" else "$${game.price}",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "GENRE: ${game.genre.uppercase()}",
                        color = ApexCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right side graphic simulation
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    imageVector = Icons.Default.Cyclone,
                    contentDescription = "Spotlight",
                    tint = accentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun RecommendedMiniCard(
    game: Game,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(110.dp)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("reco_minicard_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = ApexCardDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(ApexCyan.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "AFFINITY",
                        color = ApexCyan,
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "${game.genre} // ${game.developer}",
                color = ApexTextGray,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (game.price == 0.0) "FREE" else "$${game.price}",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = ApexCyberGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = game.rating.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CatalogStoreCard(
    game: Game,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .border(1.dp, ApexEdgeBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("catalog_card_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = ApexCardDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon visual placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F111A))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (game.id) {
                        "cyberfall" -> Icons.Default.Fingerprint
                        "dreadspace" -> Icons.Default.Dangerous
                        "velocity" -> Icons.Default.Speed
                        "primal" -> Icons.Default.FlashOn
                        "astro" -> Icons.Default.Public
                        "zenith" -> Icons.Default.Memory
                        else -> Icons.Default.Terminal
                    },
                    contentDescription = "",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text columns
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = game.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "[${game.genre.uppercase()}]",
                        color = ApexTextGray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    game.tags.take(2).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0x1F8A93A6), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = tag.uppercase(), color = ApexTextGray, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Quick actions/Price on right edge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = ApexCyberGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(game.rating.toString(), color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(start = 2.dp))
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (game.isInstalled) accentColor.copy(alpha = 0.1f) else Color(0x2200D2FF),
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (game.isInstalled) accentColor.copy(alpha = 0.5f) else ApexCyan.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (game.isInstalled) "INSTALLED" else if (game.price == 0.0) "FREE" else "$${game.price}",
                        color = if (game.isInstalled) accentColor else ApexCyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 4: LIBRARY VIEW
// ==========================================
@Composable
fun LibraryView(
    games: List<Game>,
    accentColor: Color,
    onGameClicked: (Game) -> Unit,
    onQuickLaunch: (Game) -> Unit
) {
    val installed = games.filter { it.isInstalled }
    val uninstalledOwned = games.filter { !it.isInstalled }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick statistics headers of our local OS buffer
        item {
            LibraryStatsPanel(
                totalCount = installed.size,
                totalPlaytime = installed.sumOf { it.playtimeMinutes },
                accentColor = accentColor
            )
        }

        // Section: Installed buffer
        item {
            Text(
                text = "ONLINE // INSTALLED VIRTUAL BUFFERS",
                color = accentColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        if (installed.isEmpty()) {
            item {
                EmptyStateView(
                    title = "System Workspace Empty",
                    desc = "Go to the Shop storefront, purchase resources, and simulate compile and installation pipelines."
                )
            }
        } else {
            items(installed) { game ->
                InstalledLibraryRowItem(
                    game = game,
                    accentColor = accentColor,
                    onClick = { onGameClicked(game) },
                    onLaunch = { onQuickLaunch(game) }
                )
            }
        }

        // Section: Non-Installed purchased reserves
        if (uninstalledOwned.isNotEmpty()) {
            item {
                Text(
                    text = "RESERVE // DIRECT DECK STORES",
                    color = ApexTextGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
            }

            items(uninstalledOwned) { game ->
                UninstalledLibraryRowItem(
                    game = game,
                    accentColor = accentColor,
                    onClick = { onGameClicked(game) }
                )
            }
        }
    }
}

@Composable
fun LibraryStatsPanel(
    totalCount: Int,
    totalPlaytime: Long,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ApexEdgeBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = ApexDeepDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("APEX LOCAL OS ARCHIVE", color = ApexTextGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DASHBOARD SECTOR SYSTEM",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("EXECUTABLES", color = ApexTextGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text("$totalCount ACTIVE", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Divider line
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0x338A93A6)))

                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL TIME LOCKED", color = ApexTextGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    val hours = totalPlaytime / 60
                    val min = totalPlaytime % 60
                    Text(
                        text = if (hours > 0) "${hours}h ${min}m" else "${min}m",
                        color = ApexCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun InstalledLibraryRowItem(
    game: Game,
    accentColor: Color,
    onClick: () -> Unit,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("library_installed_row_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = ApexCardDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status icon circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.1f))
                        .border(1.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        tint = accentColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = game.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LOCKED: ${game.playtimeMinutes} MINS",
                            color = ApexTextGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ApexTextGray))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACHS: ${game.achievements.size}",
                            color = ApexCyberGold,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Launcher trigger button
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("launch_game_button_${game.id}")
            ) {
                Text(
                    text = "LAUNCH OS",
                    color = ApexObsidian,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun UninstalledLibraryRowItem(
    game: Game,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x138A93A6), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("library_uninstalled_row_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = ApexDeepDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = "Cloud Save Ready",
                    tint = ApexTextGray,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = game.title,
                        color = ApexTextGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "READY FOR SETUP // SIZE: ${(game.installSize / (1024 * 1024L))} MB",
                        color = ApexTextGray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.SettingsApplications,
                    contentDescription = "Actions menu",
                    tint = ApexCyan
                )
            }
        }
    }
}

// ==========================================
// COMPONENT 5: SOCIAL DASHBOARD VIEW
// ==========================================
@Composable
fun SocialDashboardView(
    friends: List<FriendActivity>,
    alerts: List<ClientAlert>,
    accentColor: Color,
    gamerTag: String
) {
    var chatMessage by remember { mutableStateOf("") }
    val mockChatHistory = remember {
        mutableStateListOf(
            "System" to "Social comm link synchronized.",
            "Viper_X" to "Hey $gamerTag! Any team slot open for Cyberfall?",
            "NeonGhost" to "That last patch in Velocity Race was intense!"
        )
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Live Grid Profile chat panel
        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, ApexEdgeBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = ApexCardDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "GRID // INTEGRATED CHAT COMMS",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Chat message stack
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mockChatHistory) { (sender, msg) ->
                            Column {
                                Text(
                                    text = sender.uppercase(),
                                    color = if (sender == gamerTag) accentColor else if (sender == "System") ApexCyan else ApexCyberGold,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Message send inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessage,
                            onValueChange = { chatMessage = it },
                            placeholder = { Text("BROADCAST MASS SIGNAL...", color = DividerDefaults.color, fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.White),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color(0x3300FFCC)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (chatMessage.trim().isNotEmpty()) {
                                    mockChatHistory.add(gamerTag to chatMessage.trim())
                                    val promptMsg = chatMessage.lowercase()
                                    chatMessage = ""
                                    // Simulated dynamic replies to make feed responsive!
                                    if (promptMsg.contains("play") || promptMsg.contains("team") || promptMsg.contains("cyber")) {
                                        mockChatHistory.add("Viper_X" to "Roger that. Just launching my sandbox engine overlay.")
                                    } else {
                                        mockChatHistory.add("NeonGhost" to "Signal synced. Command accepted.")
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(accentColor, RoundedCornerShape(4.dp))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = ApexObsidian)
                        }
                    }
                }
            }
        }

        // Right Column: Online Friends Presence list
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color(0x1F00FFCC), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = ApexDeepDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "ONLINE SATELLITES FEED",
                        color = ApexCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(friends) { friend ->
                            FriendRowItem(friend = friend, accentColor = accentColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRowItem(
    friend: FriendActivity,
    accentColor: Color
) {
    val presenceColor = when (friend.status) {
        "online" -> ApexCyan
        "in-game" -> accentColor
        else -> ApexTextGray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF07080C), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0x1F8A93A6), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color Avatar node
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(friend.avatarColorHex)).copy(alpha = 0.2f))
                .border(1.dp, Color(android.graphics.Color.parseColor(friend.avatarColorHex)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = friend.friendName.take(1).uppercase(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.friendName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (friend.status == "in-game") "PLAYING: ${friend.currentGame?.uppercase()}" else friend.status.uppercase(),
                color = presenceColor,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Pulse dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(presenceColor)
        )
    }
}

// ==========================================
// COMPONENT 6: SETTINGS VIEW
// ==========================================
@Composable
fun SettingsView(
    gamerTag: String,
    activeAccent: String,
    soundEnabled: Boolean,
    columns: Int,
    alerts: List<ClientAlert>,
    onTagChanged: (String) -> Unit,
    onAccentChanged: (String) -> Unit,
    onSoundToggled: (Boolean) -> Unit,
    onColumnsChanged: (Int) -> Unit,
    onClearLogs: () -> Unit
) {
    var tagInput by remember { mutableStateOf(gamerTag) }
    val accentPalettes = listOf("#00FFCC", "#00D2FF", "#FFAA00", "#FF0055", "#E03CFF")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x3300FFCC), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = ApexCardDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PILOT COGNIZANCE RECORDS",
                        color = Color(0xFF00FFCC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Column {
                        Text("IDENTIFICATION CALLSIGN", color = ApexTextGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = tagInput,
                                onValueChange = { tagInput = it },
                                textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FFCC),
                                    unfocusedBorderColor = Color(0x2200FFCC)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("gamer_tag_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onTagChanged(tagInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(44.dp).testTag("save_gamer_tag_button")
                            ) {
                                Text("DECRYPT", color = ApexObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1F00D2FF), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = ApexCardDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "OS BRANDING ACCENT MODULES",
                        color = ApexCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        accentPalettes.forEach { hex ->
                            val isSelected = activeAccent.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { onAccentChanged(hex) }
                                    .testTag("preset_accent_$hex")
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x19FFAA00), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = ApexCardDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "HARDWARE SIM MECHANICS",
                        color = ApexCyberGold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SYNTHETIC HUD AUDIO", color = Color.White, fontSize = 12.sp)
                            Text("Trigger dynamic alerts audio playback events", color = ApexTextGray, fontSize = 9.sp)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { onSoundToggled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = ApexCyberGold)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x338A93A6))
                    )

                    Button(
                        onClick = onClearLogs,
                        colors = ButtonDefaults.buttonColors(containerColor = ApexHotPink),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().testTag("clear_logs_button")
                    ) {
                        Text(
                            text = "SWEEP CLOUD TELEMETRY REGISTRY",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 7: BOTTOM ACTIVITY BAR
// ==========================================
@Composable
fun BottomActivityBar(
    installedGames: List<Game>,
    activeRunning: Game?,
    accentColor: Color,
    onLaunch: (Game) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ApexDeepDark)
            .drawBehind {
                drawLine(
                    color = ApexEdgeBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "OS PANEL EXEC //",
                color = ApexTextGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            if (installedGames.isEmpty()) {
                Text(
                    text = "NO COMPORTS BUFFERED",
                    color = ApexTextGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(installedGames) { game ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .background(Color(0xFF07080C))
                                .clickable { onLaunch(game) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("shortcut_${game.id}")
                        ) {
                            Text(
                                text = game.title.uppercase().take(12) + "...",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Active runner banner status anchor
        if (activeRunning != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ApexHotPink)
                )
                Text(
                    text = "CORE ALIVE: ${activeRunning.title.uppercase()}",
                    color = ApexHotPink,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = "SYSTEM STATUS: SECURED // PING: DIRECT",
                color = accentColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ==========================================
// COMPONENT 8: FLOATING DRAGGABLE WINDOWS (COMPOSE GESTURES)
// ==========================================
@Composable
fun DraggableDownloadWindow(
    jobs: List<DownloadJob>,
    accentColor: Color,
    onClose: () -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Gestures state offsets
    var offset by remember { mutableStateOf(IntOffset(40, 160)) }

    Card(
        modifier = modifier
            .offset { offset }
            .width(340.dp)
            .height(290.dp)
            .border(1.5.dp, ApexCyberGold.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .testTag("download_manager_window"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFB0F111A)),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag handle header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(ApexCardDark)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offset = IntOffset(
                                x = (offset.x + dragAmount.x).roundToInt(),
                                y = (offset.y + dragAmount.y).roundToInt()
                            )
                        }
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = "", tint = ApexCyberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DOWNLOAD_MANAGER // COMPILES",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp).testTag("close_downloads_window")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ApexTextGray, modifier = Modifier.size(16.dp))
                }
            }

            // Connection metrics summary banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF07080C))
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = "STATUS // SIMULATION PIPELINE OPERATIVE",
                    color = ApexCyberGold,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Active list
            if (jobs.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DownloadDone, contentDescription = "", tint = ApexTextGray, modifier = Modifier.size(32.dp))
                        Text(
                            text = "NO ACTIVE COMPILES",
                            color = ApexTextGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(jobs) { job ->
                        DownloadJobItemRow(
                            job = job,
                            accentColor = accentColor,
                            onPause = { onPause(job.gameId) },
                            onResume = { onResume(job.gameId) },
                            onCancel = { onCancel(job.gameId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadJobItemRow(
    job: DownloadJob,
    accentColor: Color,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    val totalMb = job.totalBytes / (1024 * 1024L)
    val curMb = job.currentBytes / (1024 * 1024L)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF07080C), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0x1FFAA000), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = job.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // ETA Seconds text
            Text(
                text = "ETA: ${job.etaSeconds}S",
                color = ApexCyberGold,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Stats speeds row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$curMb MB / $totalMb MB",
                color = ApexTextGray,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = job.status,
                color = if (job.status == "DOWNLOADING") accentColor else ApexTextGray,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress = job.progress,
            color = if (job.status == "PAUSED") ApexTextGray else ApexCyberGold,
            trackColor = Color(0xFF161925),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )

        // Command shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (job.status == "DOWNLOADING") {
                IconButton(onClick = onPause, modifier = Modifier.size(28.dp).testTag("pause_download_${job.gameId}")) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            } else {
                IconButton(onClick = onResume, modifier = Modifier.size(28.dp).testTag("resume_download_${job.gameId}")) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = accentColor, modifier = Modifier.size(16.dp))
                }
            }

            IconButton(onClick = onCancel, modifier = Modifier.size(28.dp).testTag("cancel_download_${job.gameId}")) {
                Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = ApexHotPink, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DraggableAlertsWindow(
    alerts: List<ClientAlert>,
    accentColor: Color,
    onClose: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offset by remember { mutableStateOf(IntOffset(120, 100)) }

    Card(
        modifier = modifier
            .offset { offset }
            .width(320.dp)
            .height(290.dp)
            .border(1.5.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .testTag("alerts_window"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA0F111A)),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(ApexCardDark)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offset = IntOffset(
                                x = (offset.x + dragAmount.x).roundToInt(),
                                y = (offset.y + dragAmount.y).roundToInt()
                            )
                        }
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = "", tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "APEX_OS_TELEMETRY // LOGS",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp).testTag("close_alerts_window")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ApexTextGray, modifier = Modifier.size(16.dp))
                }
            }

            // Actions bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF07080C))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${alerts.size} EVENTS SIGNALED",
                    color = accentColor,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "CLEAR LOGS",
                    color = ApexHotPink,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onClearAll() }
                )
            }

            // Alerts list
            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SYSTEM REGISTRY SECURED. NO LOGS.",
                        color = ApexTextGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alerts) { alert ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF07080C), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0x3300FFCC), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = alert.title.uppercase(),
                                    color = accentColor,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                Text(
                                    text = sdf.format(Date(alert.timestamp)),
                                    color = ApexTextGray,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = alert.message,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 9: GAME DETAILS LAYER MODAL
// ==========================================
@Composable
fun GameDetailsOverlay(
    game: Game,
    downloadJob: DownloadJob?,
    accentColor: Color,
    onClose: () -> Unit,
    onInstall: () -> Unit,
    onLaunch: () -> Unit,
    onUpdate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD907080C))
            .clickable(enabled = true, onClick = onClose), // click scrim outside closes
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.82f)
                .clickable(enabled = false, onClick = { }) // stop propagation Inside
                .border(2.dp, accentColor, RoundedCornerShape(16.dp))
                .testTag("game_details_card"),
            colors = CardDefaults.cardColors(containerColor = ApexDeepDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ApexCardDark)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeveloperMode, contentDescription = "", tint = accentColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOCAL // CONSOLE EXECUTABLE INFO",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp).testTag("close_details_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Body grid splitting content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Game Title & Stats summary banner
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.title,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "CREATOR: ${game.developer.uppercase()} // RELEASE STATUS: STABLE",
                                    color = ApexTextGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Dynamic Action buttons section (Launch / Install / Update / download status)
                            Column(horizontalAlignment = Alignment.End) {
                                if (game.isInstalled) {
                                    if (game.updateAvailable) {
                                        Button(
                                            onClick = onUpdate,
                                            colors = ButtonDefaults.buttonColors(containerColor = ApexCyberGold),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.testTag("update_game_action")
                                        ) {
                                            Text("INSTALL PATCH LOG", color = ApexObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    } else {
                                        Button(
                                            onClick = onLaunch,
                                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.testTag("launch_game_action_${game.id}")
                                        ) {
                                            Text("EXEC CONTAINER", color = ApexObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                } else {
                                    if (downloadJob != null) {
                                        Box(
                                            modifier = Modifier
                                                .background(ApexCyberGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .border(1.dp, ApexCyberGold, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "COMPILING // ${(downloadJob.progress * 100).toInt()}%",
                                                color = ApexCyberGold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = onInstall,
                                            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.testTag("install_game_action_${game.id}")
                                        ) {
                                            Text(
                                                text = if (game.price == 0.0) "COMPILE FREE" else "ACQUIRE // $${game.price}",
                                                color = ApexObsidian,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Metadata attributes list (Rating, Pricing, File Size, Version)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0x338A93A6), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = ApexCardDark)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AttributeView(label = "rating", value = "${game.rating} ★", color = ApexCyberGold)
                                AttributeView(label = "install size", value = "${game.installSize / (1024 * 1024L)} MB", color = Color.White)
                                AttributeView(label = "version", value = game.version, color = ApexCyan)
                                AttributeView(label = "genre", value = game.genre.uppercase(), color = accentColor)
                            }
                        }
                    }

                    // Tag micro-chips
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            game.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tag.uppercase(),
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Achievements record unlocked / total list
                    item {
                        Text(
                            text = "GRID // SYSTEM TROPHIES RECORD",
                            color = ApexCyberGold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        if (game.achievements.isEmpty()) {
                            Text("No achievements tracked in execution binary archive.", color = ApexTextGray, fontSize = 11.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                game.achievements.forEach { ach ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF07080C), RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0x1FFAA000), RoundedCornerShape(6.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = "",
                                                tint = ApexCyberGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = ach,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Status badge unlocked
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0x22FFAA00), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "COMPILED",
                                                color = ApexCyberGold,
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace
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
    }
}

@Composable
fun AttributeView(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label.uppercase(), color = ApexTextGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Text(
            text = value,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp),
            fontFamily = FontFamily.Monospace
        )
    }
}

// ==========================================
// AUXILIARY COMPOSABLE HELPER VIEWS
// ==========================================
@Composable
fun EmptyStateView(title: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Dns, contentDescription = "", tint = ApexTextGray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                color = ApexTextGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
    }
}
