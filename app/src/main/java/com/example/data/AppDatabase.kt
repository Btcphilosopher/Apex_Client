package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

class StringListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",,,").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        if (list == null) return ""
        return list.joinToString(",,,")
    }
}

@Dao
interface ApexDao {
    // --- GAMES ---
    @Query("SELECT * FROM games")
    fun getAllGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getGameById(id: String): Flow<Game?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameByIdSync(id: String): Game?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<Game>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game)

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    // --- SETTINGS ---
    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<UserSetting?>

    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    suspend fun getSettingSync(key: String): UserSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: UserSetting)

    // --- DOWNLOADS ---
    @Query("SELECT * FROM download_jobs")
    fun getDownloadJobs(): Flow<List<DownloadJob>>

    @Query("SELECT * FROM download_jobs WHERE gameId = :gameId")
    fun getDownloadJob(gameId: String): Flow<DownloadJob?>

    @Query("SELECT * FROM download_jobs WHERE gameId = :gameId")
    suspend fun getDownloadJobSync(gameId: String): DownloadJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadJob(job: DownloadJob)

    @Update
    suspend fun updateDownloadJob(job: DownloadJob)

    @Query("DELETE FROM download_jobs WHERE gameId = :gameId")
    suspend fun deleteDownloadJob(gameId: String)

    @Query("DELETE FROM download_jobs")
    suspend fun clearDownloadJobs()

    // --- FRIENDS ---
    @Query("SELECT * FROM friend_activities ORDER BY timestamp DESC")
    fun getFriendActivities(): Flow<List<FriendActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendActivities(activities: List<FriendActivity>)

    @Update
    suspend fun updateFriendActivity(activity: FriendActivity)

    // --- ALERTS ---
    @Query("SELECT * FROM client_alerts ORDER BY timestamp DESC")
    fun getClientAlerts(): Flow<List<ClientAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: ClientAlert)

    @Query("UPDATE client_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAlertAsRead(id: Long)

    @Query("DELETE FROM client_alerts")
    suspend fun clearAllAlerts()
}

@Database(
    entities = [
        Game::class,
        UserSetting::class,
        DownloadJob::class,
        FriendActivity::class,
        ClientAlert::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: ApexDao
}
