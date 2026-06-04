package com.example.sportrecord.data

import com.example.sportrecord.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SportRepository(private val db: AppDatabase) {
    private val recordDao = db.recordDao()
    private val trackDao = db.trackDao()
    private val stateDao = db.appStateDao()
    private val userDao = db.userDao()

    // ── 记录 ──
    fun getRecordsByUser(uid: String): Flow<List<SportRecord>> = recordDao.getByUser(uid)
    fun getAllRecords(): Flow<List<SportRecord>> = recordDao.getAll()
    suspend fun getRecordsByView(mode: String, currentUserId: String): List<SportRecord> {
        return when (mode) {
            "all" -> recordDao.getAllSync()
            "self" -> recordDao.getAllSync().filter { it.userId == currentUserId }
            else -> recordDao.getAllSync().filter { it.userId == mode }
        }
    }
    suspend fun addRecord(r: SportRecord) = recordDao.insert(r)
    suspend fun deleteRecord(id: String) = recordDao.deleteById(id)
    suspend fun deleteAllRecords() = recordDao.deleteAll()

    // ── 轨迹 ──
    suspend fun getTrack(recordId: String) = trackDao.getByRecordId(recordId)
    suspend fun saveTrack(t: Track) = trackDao.insert(t)
    suspend fun getTracksByUser(uid: String, limit: Int = 50) = trackDao.getListByUser(uid, limit)
    suspend fun getAllTracks(limit: Int = 50) = trackDao.getAll(limit)
    suspend fun deleteTrack(recordId: String) = trackDao.deleteByRecordId(recordId)

    // ── 用户 ──
    suspend fun getUsers(): List<User> = userDao.getAll()
    suspend fun getUser(uid: String): User? = userDao.getById(uid)
    suspend fun createUser(user: User) {
        userDao.insert(user)
        // 第一个用户自动为管理员
        if (userDao.getAll().size == 1 && user.isAdmin) {
            userDao.insert(user.copy(isAdmin = true))
        }
    }
    suspend fun switchUser(uid: String) {
        val st = getState() ?: AppState()
        stateDao.save(st.copy(currentUserId = uid, adminViewMode = "self"))
    }

    // ── 状态 ──
    suspend fun getState(): AppState? = stateDao.get()
    fun getStateFlow(): Flow<AppState?> = stateDao.getFlow()
    suspend fun saveState(s: AppState) = stateDao.save(s)
    suspend fun setWeight(w: Int) {
        val st = getState() ?: AppState()
        stateDao.save(st.copy(weight = w))
    }
    suspend fun setAdminMode(mode: String) {
        val st = getState() ?: AppState()
        stateDao.save(st.copy(adminViewMode = mode))
    }

    // ── 工具 ──
    fun genId() = "${System.currentTimeMillis().toString(36)}-${(0..5).map { ('a'..'z').random() }.joinToString("")}"
}
