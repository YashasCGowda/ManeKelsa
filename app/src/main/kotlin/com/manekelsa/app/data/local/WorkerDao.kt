package com.manekelsa.app.data.local

import androidx.room.*
import com.manekelsa.app.domain.model.KycStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers")
    fun getAllWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: String): WorkerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<WorkerEntity>)

    @Query("UPDATE workers SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateAvailability(id: String, isAvailable: Boolean)

    @Query("UPDATE workers SET assignedArea = :area WHERE id = :id")
    suspend fun updateArea(id: String, area: String)

    @Query("UPDATE workers SET absentDays = :absentDays, deductions = :deductions WHERE id = :id")
    suspend fun updateAttendance(id: String, absentDays: Int, deductions: Double)

    @Query("UPDATE workers SET kycStatus = :status, identityVerified = :verified WHERE id = :id")
    suspend fun updateKyc(id: String, status: KycStatus, verified: Boolean)

    @Query("UPDATE workers SET isBlocked = :isBlocked, isAvailable = 0 WHERE id = :id")
    suspend fun updateBlockedStatus(id: String, isBlocked: Boolean)
}
