package com.manekelsa.app.data.dummy

import com.manekelsa.app.data.local.BangaloreAreas
import com.manekelsa.app.domain.model.*
import kotlin.random.Random

object FakeWorkerData {

    private val skillsPool = listOf(
        "Plumber", "Electrician", "Carpenter", "Cleaner", "Painter",
        "Driver", "Cook", "Mason", "Gardener", "AC Technician"
    )

    private val names = listOf("Ravi", "Suresh", "Mahesh", "Ganesh", "Vikram", "Rajesh", "Kiran", "Prakash", "Arjun", "Deepak")

    fun get100Workers(): List<Worker> {
        val workers = mutableListOf<Worker>()
        for (i in 1..100) {
            val baseName = names.random()
            val area = BangaloreAreas.getRandomArea()
            workers.add(
                Worker(
                    id = "WK${1000 + i}",
                    name = "$baseName $i",
                    email = "${baseName.lowercase()}$i@example.com",
                    role = if (i % 10 == 0) UserRole.ADMIN else if (i % 2 == 0) UserRole.WORKER else UserRole.CLIENT,
                    profileImageUrl = "https://randomuser.me/api/portraits/men/${Random.nextInt(1, 100)}.jpg",
                    phone = "+91 9${Random.nextInt(10000000, 100000000)}",
                    skills = listOf(skillsPool.random(), skillsPool.random()).distinct(),
                    experience = Random.nextInt(2, 21),
                    hourlyRate = Random.nextInt(80, 251).toDouble(),
                    dailyRate = Random.nextInt(600, 1801).toDouble(),
                    baseSalary = Random.nextInt(25000, 45001).toDouble(),
                    absentDays = Random.nextInt(0, 4),
                    walletBalance = Random.nextInt(500, 5001).toDouble(),
                    kycStatus = KycStatus.entries.random(),
                    rating = Random.nextInt(35, 50) / 10f,
                    totalRatings = Random.nextInt(10, 501),
                    isAvailable = Random.nextBoolean(),
                    latitude = 12.9716, // Default for non-map usage
                    longitude = 77.5946,
                    city = "Bengaluru",
                    assignedArea = area,
                    bio = "Experienced professional serving in $area.",
                    completedJobs = Random.nextInt(5, 401),
                    identityVerified = Random.nextBoolean()
                )
            )
        }
        return workers
    }

    fun getDummyJobs(): List<JobRequest> {
        return listOf(
            JobRequest("j1", "WK1001", "Kitchen Pipe Repair", "Fixing leakage", "Koramangala", 800.0, JobStatus.PENDING),
            JobRequest("j2", "WK1002", "Full House Wiring", "New electrical setup", "Whitefield", 1200.0, JobStatus.ACCEPTED),
            JobRequest("j3", "WK1003", "Deep Cleaning", "Post-construction cleaning", "Indiranagar", 1500.0, JobStatus.COMPLETED),
            JobRequest("j4", "WK1004", "Sofa Polishing", "Polish and repair", "HSR Layout", 2000.0, JobStatus.REJECTED),
            JobRequest("j5", "WK1005", "Garden Maintenance", "Mowing and pruning", "JP Nagar", 700.0, JobStatus.CANCELLED)
        )
    }
}
