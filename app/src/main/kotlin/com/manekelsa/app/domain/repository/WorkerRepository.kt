package com.manekelsa.app.domain.repository

import com.manekelsa.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface WorkerRepository {
    fun getAllWorkers(): Flow<List<Worker>>
    fun searchWorkers(query: String): Flow<List<Worker>>
    suspend fun getWorkerById(id: String): Worker?
    suspend fun toggleAvailability(workerId: String, isAvailable: Boolean)
    fun getJobs(): Flow<List<JobRequest>>
    suspend fun sendJobRequest(request: JobRequest)
    suspend fun updateJobStatus(jobId: String, status: JobStatus)
    
    // Admin & Management features
    suspend fun requestLeave(request: LeaveRequest): Result<Unit>
    fun getLeaveRequests(): Flow<List<LeaveRequest>>
    suspend fun updateLeaveStatus(leaveId: String, status: LeaveStatus): Result<Unit>
    
    suspend fun updateAttendance(workerId: String, absentDays: Int, deductions: Double): Result<Unit>
    suspend fun updateWorkerArea(workerId: String, area: String): Result<Unit>
    suspend fun updateKycStatus(workerId: String, status: KycStatus): Result<Unit>
    
    // Block & Penalty feature
    suspend fun blockWorkerWithPenalty(workerId: String, deduction: Double): Result<Unit>
    suspend fun rejectLeaveWithPenalty(leaveId: String, workerId: String, penalty: Double): Result<Unit>

    fun getBangaloreAreas(): List<String>

    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signUp(email: String, password: String, name: String, role: UserRole): Result<String>
    suspend fun signOut()
    fun isLoggedIn(): Boolean
    fun getCurrentUserEmail(): String
    suspend fun getCurrentUserRole(): UserRole

    // AI Matching & Ranking
    fun getRecommendedWorkers(jobType: String): Flow<List<Worker>>
}
