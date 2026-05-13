package com.manekelsa.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manekelsa.app.data.dummy.FakeWorkerData
import com.manekelsa.app.data.local.*
import com.manekelsa.app.domain.model.*
import com.manekelsa.app.domain.repository.WorkerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val workerDao: WorkerDao,
    private val jobDao: JobDao,
    private val leaveDao: LeaveDao
) : WorkerRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            // Seed Room if empty
            val localWorkers = workerDao.getAllWorkers().first()
            if (localWorkers.isEmpty()) {
                workerDao.insertWorkers(FakeWorkerData.get100Workers().map { it.toEntity() })
            }

            val localJobs = jobDao.getAllJobs().first()
            if (localJobs.isEmpty()) {
                jobDao.insertJobs(FakeWorkerData.getDummyJobs().map { it.toEntity() })
            }
            syncFromFirebase()
        }
    }

    private fun syncFromFirebase() {
        firestore.collection("workers").addSnapshotListener { snap, _ ->
            val workers = snap?.documents?.mapNotNull { doc ->
                try {
                    Worker(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        role = UserRole.valueOf(doc.getString("role") ?: "CLIENT"),
                        phone = doc.getString("phone") ?: "",
                        skills = (doc.get("skills") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        experience = doc.getLong("experience")?.toInt() ?: 0,
                        dailyRate = doc.getDouble("dailyRate") ?: 0.0,
                        hourlyRate = doc.getDouble("hourlyRate") ?: 0.0,
                        baseSalary = doc.getDouble("baseSalary") ?: 30000.0,
                        absentDays = doc.getLong("absentDays")?.toInt() ?: 0,
                        deductions = doc.getDouble("deductions") ?: 0.0,
                        walletBalance = doc.getDouble("walletBalance") ?: 0.0,
                        kycStatus = KycStatus.valueOf(doc.getString("kycStatus") ?: "NOT_STARTED"),
                        rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                        totalRatings = doc.getLong("totalRatings")?.toInt() ?: 0,
                        isAvailable = doc.getBoolean("isAvailable") ?: true,
                        city = doc.getString("city") ?: "Bengaluru",
                        assignedArea = doc.getString("assignedArea") ?: "",
                        preferredAreas = (doc.get("preferredAreas") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        isBlocked = doc.getBoolean("isBlocked") ?: false
                    )
                } catch (e: Exception) { null }
            }
            workers?.let { repositoryScope.launch { workerDao.insertWorkers(it.map { it.toEntity() }) } }
        }

        firestore.collection("job_requests").addSnapshotListener { snap, _ ->
            val jobs = snap?.documents?.mapNotNull { doc ->
                try {
                    JobRequest(
                        id = doc.id,
                        workerId = doc.getString("workerId") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        location = doc.getString("location") ?: "",
                        payPerDay = doc.getDouble("payPerDay") ?: 0.0,
                        status = JobStatus.valueOf(doc.getString("status") ?: "PENDING")
                    )
                } catch (e: Exception) { null }
            }
            jobs?.let { repositoryScope.launch { jobDao.insertJobs(it.map { it.toEntity() }) } }
        }
    }

    override fun getAllWorkers(): Flow<List<Worker>> = workerDao.getAllWorkers().map { it.map { e -> e.toDomain() } }
    override fun searchWorkers(query: String): Flow<List<Worker>> = getAllWorkers().map { l -> l.filter { !it.isBlocked && it.name.contains(query, true) } }
    override suspend fun getWorkerById(id: String): Worker? = workerDao.getWorkerById(id)?.toDomain()
    override suspend fun toggleAvailability(workerId: String, isAvailable: Boolean) = workerDao.updateAvailability(workerId, isAvailable)
    override fun getJobs(): Flow<List<JobRequest>> = jobDao.getAllJobs().map { it.map { e -> e.toDomain() } }

    override suspend fun sendJobRequest(request: JobRequest) {
        val newJob = request.copy(id = if(request.id.isBlank()) "j${System.currentTimeMillis()}" else request.id)
        jobDao.insertJob(newJob.toEntity())
        try { firestore.collection("job_requests").document(newJob.id).set(newJob).await() } catch (e: Exception) {}
    }

    override suspend fun updateJobStatus(jobId: String, status: JobStatus) = jobDao.updateJobStatus(jobId, status)

    override suspend fun requestLeave(request: LeaveRequest): Result<Unit> {
        val id = if(request.id.isBlank()) "leave_${System.currentTimeMillis()}" else request.id
        leaveDao.insertLeaveRequest(request.copy(id = id).toEntity())
        return Result.success(Unit)
    }

    override fun getLeaveRequests(): Flow<List<LeaveRequest>> =
        leaveDao.getAllLeaveRequests().map { it.map { e -> e.toDomain() } }

    override suspend fun updateLeaveStatus(leaveId: String, status: LeaveStatus): Result<Unit> {
        leaveDao.updateLeaveStatus(leaveId, status)
        return Result.success(Unit)
    }

    override suspend fun updateAttendance(workerId: String, absentDays: Int, deductions: Double): Result<Unit> {
        workerDao.updateAttendance(workerId, absentDays, deductions)
        return Result.success(Unit)
    }

    override suspend fun updateWorkerArea(workerId: String, area: String): Result<Unit> {
        workerDao.updateArea(workerId, area)
        return Result.success(Unit)
    }

    override suspend fun updateKycStatus(workerId: String, status: KycStatus): Result<Unit> {
        workerDao.updateKyc(workerId, status, status == KycStatus.VERIFIED)
        return Result.success(Unit)
    }

    override suspend fun blockWorkerWithPenalty(workerId: String, deduction: Double): Result<Unit> {
        val w = workerDao.getWorkerById(workerId) ?: return Result.failure(Exception("Worker not found"))
        val totalDeduction = w.deductions + deduction
        workerDao.updateBlockedStatus(workerId, true)
        workerDao.updateAttendance(workerId, w.absentDays, totalDeduction)
        return Result.success(Unit)
    }

    override suspend fun rejectLeaveWithPenalty(leaveId: String, workerId: String, penalty: Double): Result<Unit> {
        val w = workerDao.getWorkerById(workerId) ?: return Result.failure(Exception("Worker not found"))
        leaveDao.updateLeaveStatus(leaveId, LeaveStatus.REJECTED)
        workerDao.updateAttendance(workerId, w.absentDays, w.deductions + penalty)
        return Result.success(Unit)
    }

    override fun getBangaloreAreas(): List<String> = BangaloreAreas.allAreas
    override suspend fun signIn(email: String, password: String) = Result.success("demo_uid")
    override suspend fun signUp(email: String, password: String, name: String, role: UserRole) = Result.success("demo_uid")
    override suspend fun signOut() {}
    override fun isLoggedIn(): Boolean = true
    override fun getCurrentUserEmail(): String = "admin@manekelsa.com"
    override suspend fun getCurrentUserRole(): UserRole = UserRole.ADMIN

    override fun getRecommendedWorkers(jobType: String): Flow<List<Worker>> =
        getAllWorkers().map { workers ->
            workers
                .filter { w -> (jobType == "All" || w.skills.any { it.contains(jobType, ignoreCase = true) }) && !w.isBlocked }
                .sortedByDescending { w ->
                    var score = (w.rating * 12).toDouble()
                    score += (w.completedJobs / 4.0)
                    if (w.isAvailable) score += 25.0
                    if (w.identityVerified) score += 20.0
                    score
                }
                .take(3)
        }
}
