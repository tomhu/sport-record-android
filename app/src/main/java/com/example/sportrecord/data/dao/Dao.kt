package com.example.sportrecord.data.dao

import androidx.room.*
import com.example.sportrecord.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<SportRecord>>

    @Query("SELECT * FROM records WHERE userId = :uid ORDER BY createdAt DESC")
    fun getByUser(uid: String): Flow<List<SportRecord>>

    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    suspend fun getAllSync(): List<SportRecord>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: String): SportRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SportRecord)

    @Delete
    suspend fun delete(record: SportRecord)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM records")
    suspend fun deleteAll()
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks WHERE recordId = :recordId")
    suspend fun getByRecordId(recordId: String): Track?

    @Query("SELECT * FROM tracks WHERE userId = :uid ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getListByUser(uid: String, limit: Int = 50): List<Track>

    @Query("SELECT * FROM tracks ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getAll(limit: Int = 50): List<Track>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: Track)

    @Query("DELETE FROM tracks WHERE recordId = :recordId")
    suspend fun deleteByRecordId(recordId: String)
}

@Dao
interface AppStateDao {
    @Query("SELECT * FROM app_state WHERE `key` = 'state'")
    suspend fun get(): AppState?

    @Query("SELECT * FROM app_state WHERE `key` = 'state'")
    fun getFlow(): Flow<AppState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: AppState)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE userId = :uid")
    suspend fun getById(uid: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("DELETE FROM users WHERE userId = :uid")
    suspend fun deleteById(uid: String)
}
