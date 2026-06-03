package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Game
import com.example.ui.theme.*
import com.example.viewmodel.ApexViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameRunnerScreen(
    viewModel: ApexViewModel,
    modifier: Modifier = Modifier
) {
    val game = viewModel.activeRunningGame ?: return
    val primaryColor = Color(android.graphics.Color.parseColor(viewModel.neonAccentHex))

    // Animation pulsing glow in background
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCirc),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ApexObsidian)
            .drawBehind {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.08f * glowPulse),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.7f
                )
            }
            .padding(16.dp)
            .testTag("game_runner_immersive_container")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Session HUD Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .background(ApexDeepDark)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ApexHotPink)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "LIVE // PROCESS COMPILING",
                            color = ApexTextGray,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = game.title.uppercase(),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Playtime Tracker details
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SESSION SCORE", color = ApexTextGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = viewModel.virtualScore.toString(),
                            color = primaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ACTIVE RUNTIME", color = ApexTextGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "${viewModel.runTimeInMinutes}m elapsed",
                            color = ApexCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Embedded Sandbox Game Canvas / Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, primaryColor, RoundedCornerShape(12.dp))
                    .background(Color(0xFF040508))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Status Info
                    Box(
                        modifier = Modifier
                            .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .border(1.dp, primaryColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SANDBOX KERNEL: ACTIVE // COMPORT BUFFERS ONLINE",
                            color = primaryColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Interactive Custom Mini-Game View Canvas
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (game.id) {
                            "cyberfall" -> CyberfallGameSim(viewModel = viewModel, accentColor = primaryColor)
                            "dreadspace" -> DreadspaceGameSim(viewModel = viewModel)
                            "velocity" -> VelocityGameSim(viewModel = viewModel, accentColor = primaryColor)
                            "astro" -> AstroGameSim(viewModel = viewModel, accentColor = primaryColor)
                            "zenith" -> ZenithGameSim(viewModel = viewModel, accentColor = primaryColor)
                            else -> GenericGameSim(viewModel = viewModel, gameTitle = game.title, accentColor = primaryColor)
                        }
                    }

                    // Console live logs ticker
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .border(1.dp, Color(0x1F8A93A6), RoundedCornerShape(4.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF07080C))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Code, contentDescription = "Terminal", tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CORE-LOG // ${viewModel.virtualStatusLog.uppercase()}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Session Bottom Control Tray (Unlocks achievements list + Close trigger)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live unlocked achievements tracker
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .border(1.dp, Color(0x33FFAA00), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = ApexDeepDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "", tint = ApexCyberGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SESSION UNLOCKS",
                                color = ApexTextGray,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (viewModel.sessionUnlockedAchievements.isEmpty()) {
                                Text(
                                    text = "TAP CONTROLS TO UNLOCK OVERLAYS",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = viewModel.sessionUnlockedAchievements.joinToString(", "),
                                    color = ApexCyberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.testTag("session_unlocks_marquee")
                                )
                            }
                        }
                    }
                }

                // Massive glow exit save button
                Button(
                    onClick = { viewModel.terminateGameSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = ApexHotPink),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("terminate_session_button")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TERMINATE & COMPILE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// INDIVIDUAL MINI-GAME EXEC SIMULATORS
// ==========================================

@Composable
fun CyberfallGameSim(viewModel: ApexViewModel, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Terminal, contentDescription = "", tint = accentColor, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NEO-TOKYO MAINFRAME ENCRYPTED",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rapid tap the direct bypass links to inject root exploits into the firewall stack",
                color = ApexTextGray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 15,
                        actionLog = "Buffer payload sent. Threat index decreased."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, accentColor, RoundedCornerShape(4.dp)).testTag("action_override")
            ) {
                Text(text = "OVERWRITE BUFFER", color = accentColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 25,
                        actionLog = "Hacking kernel signature spoofed. Root access unlocked."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ApexCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, ApexCyan, RoundedCornerShape(4.dp)).testTag("action_inject")
            ) {
                Text(text = "INJECT SPLIT ROOT", color = ApexCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun DreadspaceGameSim(viewModel: ApexViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Warning, contentDescription = "", tint = ApexHotPink, modifier = Modifier.size(54.dp))
        Text(
            text = "VACUUM VENT VALVE BREACH G-7",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ship internal atmospheric integrity: ${100 - (viewModel.virtualScore / 3).coerceIn(0, 99)}%",
            color = ApexHotPink,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 10,
                        actionLog = "Emergency magnetic bulkheads sealed. Vent speed stalled."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ApexHotPink.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, ApexHotPink, RoundedCornerShape(4.dp)).testTag("action_seal")
            ) {
                Text(text = "SEAL BULKHEADS", color = ApexHotPink, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 20,
                        actionLog = "Fuel cells recycled! Power diverted back to environmental life grid."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ApexCyberGold.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, ApexCyberGold, RoundedCornerShape(4.dp)).testTag("action_power")
            ) {
                Text(text = "DIVERT AUX POWER", color = ApexCyberGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun VelocityGameSim(viewModel: ApexViewModel, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Speed, contentDescription = "", tint = ApexCyan, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "HYPER SPEED ACTIVE: ${1500 + viewModel.virtualScore} KM/H",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 15,
                        actionLog = "Drifting grid corner with perfect synth timing! Overdrive multiplier active!"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ApexCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, ApexCyan, RoundedCornerShape(4.dp)).testTag("action_drift")
            ) {
                Text(text = "DRIFT CORNER", color = ApexCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 35,
                        actionLog = "Nitro combustors fully ignited. Space-time warping."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, accentColor, RoundedCornerShape(4.dp)).testTag("action_nitro")
            ) {
                Text(text = "NITRO COMBUSTION", color = accentColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun AstroGameSim(viewModel: ApexViewModel, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Public, contentDescription = "", tint = ApexCyberGold, modifier = Modifier.size(54.dp))
        Text(
            text = "DYSON SOLAR COLLIMATOR NODE",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Planet core resources harvested: ${viewModel.virtualScore * 10} Tons",
            color = ApexCyberGold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 12,
                        actionLog = "Heavy mineral drills dispatched of stellar core asteroid fields."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ApexCyberGold.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, ApexCyberGold, RoundedCornerShape(4.dp)).testTag("action_drill")
            ) {
                Text(text = "EXTRACT RESOURCES", color = ApexCyberGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 18,
                        actionLog = "collimator solar array compiled! Planetary shielding enhanced."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, accentColor, RoundedCornerShape(4.dp)).testTag("action_shield")
            ) {
                Text(text = "DEPLOY SOLAR ARRAY", color = accentColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun ZenithGameSim(viewModel: ApexViewModel, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.DeveloperBoard, contentDescription = "", tint = accentColor, modifier = Modifier.size(54.dp))
        Text(
            text = "[BINARY ROGUE] MEMORY GRID",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Total exploits loaded in card deck: ${viewModel.virtualScore / 10} Exploits",
            color = ApexCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 15,
                        actionLog = "Drew exploit card: Overwrite Registry call. Executed instantly."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, accentColor, RoundedCornerShape(4.dp)).testTag("action_draw")
            ) {
                Text(text = "DRAW EXPLOIT CARDS", color = accentColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 30,
                        actionLog = "Binary stack overflow triggered. Hacking card engine overridden!"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ApexHotPink.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, ApexHotPink, RoundedCornerShape(4.dp)).testTag("action_overflow")
            ) {
                Text(text = "TRIGGER STACK OVERFLOW", color = ApexHotPink, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun GenericGameSim(viewModel: ApexViewModel, gameTitle: String, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.VideogameAsset, contentDescription = "", tint = accentColor, modifier = Modifier.size(64.dp))
        Text(
            text = "RUNNING EXEC CONTAINER: $gameTitle",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    viewModel.virtualGameAction(
                        scoreDelta = 10,
                        actionLog = "Standard executable bypass node engaged."
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, accentColor, RoundedCornerShape(4.dp)).testTag("action_generic_tap")
            ) {
                Text(text = "BYPASS BUFFER", color = accentColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
