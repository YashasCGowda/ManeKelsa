package com.manekelsa.app.domain.model

import com.manekelsa.app.data.local.BangaloreAreas
import kotlin.random.Random

enum class KycStatus { PENDING, VERIFIED, REJECTED, NOT_STARTED }
enum class UserRole { CLIENT, WORKER, ADMIN }
enum class LeaveStatus { PENDING, APPROVED, REJECTED }

data class Worker(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CLIENT,
    val profileImageUrl: String = "",
    val phone: String = "",
    val skills: List<String> = emptyList(),
    val experience: Int = 0,
    val hourlyRate: Double = 0.0,
    val dailyRate: Double = 0.0,
    val baseSalary: Double = 30000.0,
    val absentDays: Int = 0,
    val deductions: Double = 0.0,
    val walletBalance: Double = 0.0,
    val kycStatus: KycStatus = KycStatus.NOT_STARTED,
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val isAvailable: Boolean = true,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "Bengaluru",
    val assignedArea: String = "",
    val preferredAreas: List<String> = emptyList(),
    val bio: String = "",
    val completedJobs: Int = 0,
    val identityVerified: Boolean = false,
    val isBlocked: Boolean = false
) {
    fun toDisplayRating(): String = if (totalRatings == 0) "New" else "%.1f".format(rating)
    fun primarySkill(): String = skills.firstOrNull() ?: "General Labour"

    fun calculateFinalSalary(): Double {
        val perDayDeduction = baseSalary / 30.0
        return baseSalary - (absentDays * perDayDeduction) - deductions
    }
}

data class LeaveRequest(
    val id: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val workerEmail: String = "",
    val reason: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: LeaveStatus = LeaveStatus.PENDING
)

data class JobRequest(
    val id: String = "",
    val workerId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val payPerDay: Double = 0.0,
    val status: JobStatus = JobStatus.PENDING
)

enum class JobStatus { PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED }

object DummyData {
    val workers: List<Worker> = generate100Workers()

    private fun generate100Workers(): List<Worker> {
        val workers = mutableListOf<Worker>()
        val skillsPool = listOf("Plumber", "Electrician", "Carpenter", "Cleaner", "Painter", "Driver", "Cook", "Mason", "Gardener", "AC Technician")
        val names = listOf("Ravi", "Suresh", "Mahesh", "Ganesh", "Vikram", "Rajesh", "Kiran", "Prakash", "Arjun", "Deepak", "Sanjay", "Mohan")

        for (i in 1..100) {
            val baseName = names.random()
            val area = BangaloreAreas.getRandomArea()

            val worker = Worker(
                id = "WK${1000 + i}",
                name = "$baseName $i",
                email = "${baseName.lowercase()}${i}@example.com",
                role = if (i % 10 == 0) UserRole.ADMIN else if (i % 2 == 0) UserRole.WORKER else UserRole.CLIENT,
                profileImageUrl = "https://randomuser.me/api/portraits/men/${(1..99).random()}.jpg",
                phone = "+91 9${(10000000..99999999).random()}",
                skills = listOf(skillsPool.random(), skillsPool.random()).distinct(),
                experience = (2..20).random(),
                hourlyRate = (80..250).random().toDouble(),
                dailyRate = (600..1800).random().toDouble(),
                baseSalary = (25000..45000).random().toDouble(),
                absentDays = (0..3).random(),
                deductions = 0.0,
                walletBalance = (1000..5000).random().toDouble(),
                kycStatus = KycStatus.entries.random(),
                rating = (35..49).random() / 10f,
                totalRatings = (15..450).random(),
                isAvailable = Random.nextBoolean(),
                latitude = 12.85 + Random.nextDouble(-0.20, 0.20),
                longitude = 77.55 + Random.nextDouble(-0.20, 0.20),
                city = "Bengaluru",
                assignedArea = area,
                preferredAreas = listOf(area, BangaloreAreas.getRandomArea(), BangaloreAreas.getRandomArea()).distinct().take(3),
                bio = "Experienced professional available in $area area.",
                completedJobs = (10..380).random(),
                identityVerified = Random.nextBoolean()
            )
            workers.add(worker)
        }
        return workers
    }
}
