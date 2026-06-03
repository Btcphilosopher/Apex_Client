package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ApexViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    // Reactive streams from Repository
    val games: StateFlow<List<Game>> = repository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadJobs: StateFlow<List<DownloadJob>> = repository.downloadJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friends: StateFlow<List<FriendActivity>> = repository.friendActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<ClientAlert>> = repository.clientAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI State / Window Manager ---
    var activeTab by mutableStateOf("STORE") // "STORE", "LIBRARY", "SOCIAL", "SETTINGS"
    var selectedDetailsGame by mutableStateOf<Game?>(null) // Layered details modal/window
    
    // Desktop multi-panel window toggles
    var isDownloadManagerOpen by mutableStateOf(false)
    var isSocialDashboardOpen by mutableStateOf(true) // Start side pane open on tablet grids
    var isAlertsOverlayOpen by mutableStateOf(false)
    
    // Customized Settings
    var gamerTag by mutableStateOf("ApexGamer#404")
    var neonAccentHex by mutableStateOf("#00FFCC") // Toxic electric mint, customizer option
    var soundEnabled by mutableStateOf(true)
    var desktopGridColumns by mutableStateOf(2)

    // --- Active Game Runner state ---
    var activeRunningGame by mutableStateOf<Game?>(null)
    var runTimeInMinutes by mutableLongStateOf(0L)
    val sessionUnlockedAchievements = mutableStateListOf<String>()
    
    // Immersive custom running game action inputs:
    var virtualScore by mutableStateOf(0)
    var virtualStatusLog by mutableStateOf("Kernel running safely...")
    private var gameLoopJob: Job? = null

    init {
        // Observe local settings to load user choices
        viewModelScope.launch {
            repository.dao.getSetting("gamer_tag").collect { setting ->
                if (setting != null) {
                    gamerTag = setting.value
                }
            }
        }
        viewModelScope.launch {
            repository.dao.getSetting("neon_accent").collect { setting ->
                if (setting != null) {
                    neonAccentHex = setting.value
                }
            }
        }
    }

    // Dynamic store / library sorted recommendations
    val recommendedStoreCatalog: StateFlow<List<Game>> = games
        .map { list -> repository.generateRecommendations(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interactive Operations ---

    fun selectDetailsGameById(id: String) {
        viewModelScope.launch {
            val game = repository.dao.getGameByIdSync(id)
            selectedDetailsGame = game
        }
    }

    fun installGame(id: String) {
        viewModelScope.launch {
            repository.installGame(id)
            // If details is open, update details window focus
            val updated = repository.dao.getGameByIdSync(id)
            if (selectedDetailsGame?.id == id) {
                selectedDetailsGame = updated
            }
        }
    }

    fun resumeDownloadJob(id: String) {
        viewModelScope.launch {
            repository.resumeDownload(id)
        }
    }

    fun pauseDownloadJob(id: String) {
        viewModelScope.launch {
            repository.pauseDownload(id)
        }
    }

    fun cancelDownloadJob(id: String) {
        viewModelScope.launch {
            repository.cancelDownload(id)
        }
    }

    fun updateGame(id: String) {
        viewModelScope.launch {
            repository.applyGameUpdate(id)
            val updated = repository.dao.getGameByIdSync(id)
            if (selectedDetailsGame?.id == id) {
                selectedDetailsGame = updated
            }
        }
    }

    fun changeGamerTag(newTag: String) {
        val trimmed = newTag.trim()
        if (trimmed.isNotEmpty()) {
            gamerTag = trimmed
            viewModelScope.launch {
                repository.dao.insertSetting(UserSetting("gamer_tag", trimmed))
                repository.dao.insertAlert(
                    ClientAlert(
                        title = "Identity Redefined",
                        message = "Your Apex Grid tag is now recorded as: $trimmed"
                    )
                )
            }
        }
    }

    fun changeNeonAccent(newHex: String) {
        neonAccentHex = newHex
        viewModelScope.launch {
            repository.dao.insertSetting(UserSetting("neon_accent", newHex))
        }
    }

    fun markAllAlertsRead() {
        viewModelScope.launch {
            repository.dao.clearAllAlerts()
        }
    }

    // --- Embedded Runner Pipeline ---

    fun launchGameSession(game: Game) {
        activeRunningGame = game
        runTimeInMinutes = 0L
        virtualScore = 0
        sessionUnlockedAchievements.clear()
        virtualStatusLog = "Direct launch buffer online. Standby..."
        
        repositoryScopeLaunch {
            repository.launchGame(game.id)
        }

        // Start local active simulator game clock loop
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var cycles = 0
            while (activeRunningGame != null) {
                delay(1000L) // virtual game session advances
                cycles++
                
                // Every 10 seconds, increment "playtime minutes" to reflect real gaming OS statistics
                if (cycles % 10 == 0) {
                    runTimeInMinutes += 1
                    virtualStatusLog = "Process synced. Session advanced +1m."
                }

                // Randomly prompt virtual achievements based on user virtual score milestones
                checkVirtualMilestones(cycles)
            }
        }
    }

    private fun checkVirtualMilestones(cycles: Int) {
        val game = activeRunningGame ?: return
        when (game.id) {
            "cyberfall" -> {
                if (virtualScore >= 100 && !sessionUnlockedAchievements.contains("Root Override")) {
                    sessionUnlockedAchievements.add("Root Override")
                    virtualStatusLog = "DECRYPT SUCCESS: Root privilege achieved!"
                }
                if (virtualScore >= 250 && !sessionUnlockedAchievements.contains("Ghost in the Shell")) {
                    sessionUnlockedAchievements.add("Ghost in the Shell")
                    virtualStatusLog = "INTEGRATION UNLOCKED: Neural Link initialized!"
                }
            }
            "dreadspace" -> {
                if (virtualScore >= 80 && !sessionUnlockedAchievements.contains("Vacuum Leak Survival")) {
                    sessionUnlockedAchievements.add("Vacuum Leak Survival")
                    virtualStatusLog = "PRESSURE SYNC: Patched breach with milliseconds to spare!"
                }
            }
            "velocity" -> {
                if (virtualScore >= 150 && !sessionUnlockedAchievements.contains("Synth Glide")) {
                    sessionUnlockedAchievements.add("Synth Glide")
                    virtualStatusLog = "ACCELERATING: Dynamic glide multiplier x5!"
                }
            }
            "astro" -> {
                if (virtualScore >= 120 && !sessionUnlockedAchievements.contains("Miner Guild Leader")) {
                    sessionUnlockedAchievements.add("Miner Guild Leader")
                    virtualStatusLog = "RESOURCES FLOWING: Galactic mining colony thriving!"
                }
            }
            "zenith" -> {
                if (virtualScore >= 200 && !sessionUnlockedAchievements.contains("Buffer Overflow")) {
                    sessionUnlockedAchievements.add("Buffer Overflow")
                    virtualStatusLog = "COMPILATION STACK OVERFLOW: Disassembling card variables!"
                }
            }
        }
    }

    fun virtualGameAction(scoreDelta: Int, actionLog: String) {
        virtualScore += scoreDelta
        virtualStatusLog = actionLog
    }

    fun terminateGameSession() {
        val game = activeRunningGame ?: return
        val currentAchievementsUnbound = sessionUnlockedAchievements.toList()
        
        viewModelScope.launch {
            // Commit final playtime metrics and newly unlocked achievement badges back to Room db.
            repository.finishGameSession(
                gameId = game.id,
                additionalMinutes = runTimeInMinutes.coerceAtLeast(1L), // at least count 1 minute for session
                unlockedAchievements = currentAchievementsUnbound
            )
            // Clear running game state
            activeRunningGame = null
            gameLoopJob?.cancel()
            gameLoopJob = null
            
            // Sync details if open
            selectedDetailsGame = repository.dao.getGameByIdSync(game.id)
        }
    }

    private fun repositoryScopeLaunch(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}
