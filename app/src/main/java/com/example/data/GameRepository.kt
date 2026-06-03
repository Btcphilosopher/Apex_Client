package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

class GameRepository(private val context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "apex_client_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val dao: ApexDao get() = database.dao

    // Expose flows from DAO
    val allGames: Flow<List<Game>> = dao.getAllGames()
    val downloadJobs: Flow<List<DownloadJob>> = dao.getDownloadJobs()
    val friendActivities: Flow<List<FriendActivity>> = dao.getFriendActivities()
    val clientAlerts: Flow<List<ClientAlert>> = dao.getClientAlerts()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        repositoryScope.launch {
            // Seed base games catalog if DB is empty
            dao.getAllGames().first().let { currentList ->
                if (currentList.isEmpty()) {
                    seedDatabase()
                }
            }
            // Seed base friends if empty
            dao.getFriendActivities().first().let { currentFriends ->
                if (currentFriends.isEmpty()) {
                    seedFriends()
                }
            }
            // Start background tasks simulation
            startDownloadEngine()
            startOnlineSimulation()
        }
    }

    private suspend fun seedDatabase() {
        val defaultGames = listOf(
            Game(
                id = "cyberfall",
                title = "Cyberfall: Neo-Tokyo",
                developer = "Apex Lab Labs",
                genre = "Action RPG",
                installSize = 51200 * 1024 * 1024L, // 50 GB
                isInstalled = false,
                playtimeMinutes = 0L,
                achievements = listOf("Tokyo Drifter", "Root Override", "Ghost in the Shell"),
                version = "1.4.2",
                rating = 4.8f,
                price = 59.99,
                tags = listOf("Cyberpunk", "RPG", "Sci-Fi", "Hacking"),
                executableRef = "cyberfall",
                updateAvailable = false
            ),
            Game(
                id = "dreadspace",
                title = "Dread Space: Void",
                developer = "Sector-7 Biosystems",
                genre = "Atmospheric Horror",
                installSize = 25600 * 1024 * 1024L, // 25 GB
                isInstalled = false,
                playtimeMinutes = 0L,
                achievements = listOf("Into the Abyss", "Vacuum Leak Survival", "Quiet Sector"),
                version = "2.0.1",
                rating = 4.6f,
                price = 39.99,
                tags = listOf("Horror", "Space", "Sci-Fi", "Survival"),
                executableRef = "dreadspace",
                updateAvailable = false
            ),
            Game(
                id = "velocity",
                title = "Velocity: HyperDrive",
                developer = "Vector Synth Studios",
                genre = "Futuristic Racing",
                installSize = 12288 * 1024 * 1024L, // 12 GB
                isInstalled = false,
                playtimeMinutes = 0L,
                achievements = listOf("Light Speed", "Synth Glide", "Perfect Launch"),
                version = "3.1.0",
                rating = 4.9f,
                price = 19.99,
                tags = listOf("Racing", "Synthwave", "High-Speed", "Arcade"),
                executableRef = "velocity",
                updateAvailable = true // triggers update available flag
            ),
            Game(
                id = "primal",
                title = "Primal Arena: Fusion",
                developer = "Titan Cyber-Combat",
                genre = "Fighting",
                installSize = 18432 * 1024 * 1024L, // 18 GB
                isInstalled = true, // start installed as pre-loaded
                playtimeMinutes = 120L, // user already played this!
                achievements = listOf("K.O. Machine", "Overdrive Fighter"),
                version = "1.0.8",
                rating = 4.2f,
                price = 29.99,
                tags = listOf("Action", "Fighting", "Mecha", "PvP"),
                executableRef = "primal",
                updateAvailable = false
            ),
            Game(
                id = "astro",
                title = "Astro Builder: Galaxy",
                developer = "Pixel Orbit Simulations",
                genre = "Strategy",
                installSize = 6144 * 1024 * 1024L, // 6 GB
                isInstalled = false,
                playtimeMinutes = 0L,
                achievements = listOf("Atmosphere Scrubber", "Miner Guild Leader", "Dyson Node"),
                version = "0.9.4",
                rating = 4.5f,
                price = 14.99,
                tags = listOf("Build", "Strategy", "Simulation", "Sci-Fi"),
                executableRef = "astro",
                updateAvailable = false
            ),
            Game(
                id = "zenith",
                title = "Zenith: Rogue Signal",
                developer = "Binary Oracle Inc.",
                genre = "Roguelike Deckbuilder",
                installSize = 3072 * 1024 * 1024L, // 3 GB
                isInstalled = true, // another pre-installed game
                playtimeMinutes = 340L, // more play time to trigger recommendation bias
                achievements = listOf("Decompiled Code", "Perfect Run", "Buffer Overflow"),
                version = "1.0.0",
                rating = 4.7f,
                price = 9.99,
                tags = listOf("Card", "Hack", "Roguelike", "Cyberpunk"),
                executableRef = "zenith",
                updateAvailable = false
            ),
            Game(
                id = "deadlock",
                title = "Deadlock Protocol",
                developer = "Cortex Systems",
                genre = "Tactical Shooter",
                installSize = 35840 * 1024 * 1024L, // 35 GB
                isInstalled = false,
                playtimeMinutes = 0L,
                achievements = listOf("Silent Recon", "Sector Sweeper", "Apex Shot"),
                version = "1.1.2",
                rating = 4.4f,
                price = 49.99,
                tags = listOf("Action", "Tactical", "Shooter", "Sci-Fi"),
                executableRef = "deadlock",
                updateAvailable = false
            )
        )
        dao.insertGames(defaultGames)
        dao.insertAlert(
            ClientAlert(
                title = "System Online",
                message = "Apex Gaming Client OS Boot Successful. Databases synchronized."
            )
        )
    }

    private suspend fun seedFriends() {
        val friendsList = listOf(
            FriendActivity("Viper_X", "online", null, "#00FFCC"),
            FriendActivity("CyberSlayer", "in-game", "Cyberfall: Neo-Tokyo", "#FF3366"),
            FriendActivity("RogueBinary", "online", null, "#FFFF33"),
            FriendActivity("ZenithMaster", "offline", null, "#999999"),
            FriendActivity("NeonGhost", "in-game", "Velocity: HyperDrive", "#33CCFF")
        )
        dao.insertFriendActivities(friendsList)
    }

    // --- Dynamic Recommendations ---
    fun generateRecommendations(games: List<Game>): List<Game> {
        if (games.isEmpty()) return emptyList()

        // 1. Determine genre playtime affinity:
        val genreMinutes = mutableMapOf<String, Long>()
        var totalPlaytime = 0L
        for (g in games) {
            if (g.playtimeMinutes > 0) {
                genreMinutes[g.genre] = (genreMinutes[g.genre] ?: 0L) + g.playtimeMinutes
                totalPlaytime += g.playtimeMinutes
            }
        }

        // Sort by weighted scores
        return games.map { g ->
            var score = 0.0
            
            // Base score from rating (4.0 ~ 5.0 scaled up)
            score += g.rating * 10.0

            // Playtime category weight bonus
            val minutesInGenre = genreMinutes[g.genre] ?: 0L
            if (minutesInGenre > 0) {
                score += (minutesInGenre.toDouble() / (totalPlaytime.coerceAtLeast(1L))) * 50.0
            }

            // Tag overlap matches (e.g. if cyberpunk is a major tag)
            val hasCyberpunk = g.tags.any { it.equals("cyberpunk", ignoreCase = true) }
            val hasSciFi = g.tags.any { it.equals("sci-fi", ignoreCase = true) }
            val cyberPlaytime = games.filter { it.tags.any { t -> t.equals("cyberpunk", ignoreCase = true) } }.sumOf { it.playtimeMinutes }
            
            if (hasCyberpunk && cyberPlaytime > 0) {
                score += (cyberPlaytime.toDouble() / totalPlaytime.coerceAtLeast(1L)) * 30.0
            }
            if (hasSciFi) {
                score += 5.0
            }

            // Installation status check, promote uninstalled games for Store, and installed for library shortcuts
            if (g.isInstalled) {
                score -= 10.0 // reduce shop recommendation score slightly since they already own it
            }

            // Price/Dev matches
            if (g.price < 20.0) score += 3.0

            g to score
        }
        .sortedByDescending { it.second }
        .map { it.first }
    }

    // --- Action Methods ---

    suspend fun installGame(gameId: String) {
        val game = dao.getGameByIdSync(gameId) ?: return
        // Create a simulated download job
        val totalBytes = game.installSize
        val job = DownloadJob(
            gameId = gameId,
            title = game.title,
            status = "PENDING",
            progress = 0.0f,
            currentBytes = 0L,
            totalBytes = totalBytes,
            etaSeconds = 120
        )
        dao.insertDownloadJob(job)
        dao.insertAlert(
            ClientAlert(
                title = "${game.title} Queued",
                message = "Simulating background download pipeline configuration..."
            )
        )
    }

    suspend fun pauseDownload(gameId: String) {
        val job = dao.getDownloadJobSync(gameId) ?: return
        dao.insertDownloadJob(job.copy(status = "PAUSED"))
    }

    suspend fun resumeDownload(gameId: String) {
        val job = dao.getDownloadJobSync(gameId) ?: return
        dao.insertDownloadJob(job.copy(status = "DOWNLOADING"))
    }

    suspend fun cancelDownload(gameId: String) {
        dao.deleteDownloadJob(gameId)
    }

    suspend fun launchGame(gameId: String) {
        dao.insertAlert(
            ClientAlert(
                title = "Launcher Executing",
                message = "Executing sandbox runner for Game UUID $gameId..."
            )
        )
    }

    suspend fun finishGameSession(gameId: String, additionalMinutes: Long, unlockedAchievements: List<String>) {
        val game = dao.getGameByIdSync(gameId) ?: return
        val currentAchievements = game.achievements.toMutableList()
        val newAlerts = mutableListOf<ClientAlert>()

        unlockedAchievements.forEach { ach ->
            if (!currentAchievements.contains(ach)) {
                currentAchievements.add(ach)
                newAlerts.add(
                    ClientAlert(
                        title = "Achievement Unlocked 🏆",
                        message = "Unlocked '$ach' in ${game.title}!"
                    )
                )
            }
        }

        val updatedGame = game.copy(
            playtimeMinutes = game.playtimeMinutes + additionalMinutes,
            achievements = currentAchievements
        )
        dao.updateGame(updatedGame)

        newAlerts.forEach { alert ->
            dao.insertAlert(alert)
        }
    }

    suspend fun applyGameUpdate(gameId: String) {
        val game = dao.getGameByIdSync(gameId) ?: return
        if (game.updateAvailable) {
            dao.updateGame(game.copy(updateAvailable = false, version = "Latest"))
            dao.insertAlert(
                ClientAlert(
                    title = "Update Installed",
                    message = "${game.title} updated to latest executable binary container."
                )
            )
        }
    }

    // --- Simulated Background Pipelines ---

    private fun startDownloadEngine() {
        repositoryScope.launch {
            while (isActive) {
                delay(1200L) // tick every ~1.2s to simulate real download advancement
                val activeJobs = dao.getDownloadJobs().first()
                for (job in activeJobs) {
                    if (job.status == "PENDING") {
                        // Move to downloading
                        val updated = job.copy(status = "DOWNLOADING")
                        dao.updateDownloadJob(updated)
                    } else if (job.status == "DOWNLOADING") {
                        val currentProgress = job.progress
                        if (currentProgress >= 1.0f) {
                            // Installation complete!
                            dao.deleteDownloadJob(job.gameId)
                            val game = dao.getGameByIdSync(job.gameId)
                            if (game != null) {
                                dao.updateGame(game.copy(isInstalled = true, updateAvailable = false))
                                dao.insertAlert(
                                    ClientAlert(
                                        title = "Installation Ready ✅",
                                        message = "${game.title} has been successfully compiled and embedded in direct run buffer."
                                    )
                                )
                            }
                        } else {
                            val nextProgress = (currentProgress + 0.15f).coerceAtMost(1.0f)
                            val nextBytes = (nextProgress * job.totalBytes).toLong()
                            val speedMbps = Random.nextInt(45, 120) // simulated speed
                            val remainingBytes = job.totalBytes - nextBytes
                            val eta = if (speedMbps > 0) (remainingBytes / (speedMbps * 125000L)).toInt().coerceAtLeast(1) else 10
                            
                            val updated = job.copy(
                                progress = nextProgress,
                                currentBytes = nextBytes,
                                etaSeconds = eta
                            )
                            dao.updateDownloadJob(updated)
                        }
                    }
                }
            }
        }
    }

    private fun startOnlineSimulation() {
        repositoryScope.launch {
            while (isActive) {
                // Every 25 seconds, simulate an event (friend changes status or random broadcast alert)
                delay(25000L)
                val currentFriends = dao.getFriendActivities().first()
                if (currentFriends.isNotEmpty()) {
                    val randomFriend = currentFriends.random()
                    val nextStatus = when (randomFriend.status) {
                        "online" -> "in-game"
                        "in-game" -> "offline"
                        else -> "online"
                    }
                    val gameJoined = if (nextStatus == "in-game") {
                        listOf("Cyberfall: Neo-Tokyo", "Velocity: HyperDrive", "Dread Space: Void", "Zenith: Rogue Signal").random()
                    } else null

                    val updatedFriend = randomFriend.copy(status = nextStatus, currentGame = gameJoined, timestamp = System.currentTimeMillis())
                    dao.updateFriendActivity(updatedFriend)

                    // Inject friend alert
                    val msg = when (nextStatus) {
                        "in-game" -> "${randomFriend.friendName} is now playing $gameJoined"
                        "online" -> "${randomFriend.friendName} is now online"
                        else -> "${randomFriend.friendName} disconnected"
                    }
                    
                    dao.insertAlert(
                        ClientAlert(
                            title = "Social System Broadcast",
                            message = msg
                        )
                    )
                }

                // Randomly trigger an update notification for some uninstalled or installed games
                if (Random.nextFloat() < 0.25f) {
                    val all = dao.getAllGames().first()
                    if (all.isNotEmpty()) {
                        val unupdated = all.filter { !it.updateAvailable }
                        if (unupdated.isNotEmpty()) {
                            val lucky = unupdated.random()
                            dao.updateGame(lucky.copy(updateAvailable = true))
                            dao.insertAlert(
                                ClientAlert(
                                    title = "System patch found 📡",
                                    message = "A system update patch is ready for ${lucky.title} (v${lucky.version})"
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
