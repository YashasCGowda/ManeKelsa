package com.manekelsa.app.data.local

import androidx.room.*
import com.manekelsa.app.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val profileImageUrl: String,
    val phone: String,
    val skills: List<String>,
    val experience: Int,
    val hourlyRate: Double,
    val dailyRate: Double,
    val baseSalary: Double,
    val absentDays: Int,
    val deductions: Double,
    val walletBalance: Double,
    val kycStatus: KycStatus,
    val rating: Float,
    val totalRatings: Int,
    val isAvailable: Boolean,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val assignedArea: String,
    val preferredAreas: List<String>,
    val bio: String,
    val completedJobs: Int,
    val identityVerified: Boolean,
    val isBlocked: Boolean
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val workerId: String,
    val title: String,
    val description: String,
    val location: String,
    val payPerDay: Double,
    val status: JobStatus
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey val id: String,
    val workerId: String,
    val workerName: String,
    val workerEmail: String,
    val reason: String,
    val startDate: String,
    val endDate: String,
    val status: LeaveStatus
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name
    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromKycStatus(value: KycStatus): String = value.name
    @TypeConverter
    fun toKycStatus(value: String): KycStatus = KycStatus.valueOf(value)

    @TypeConverter
    fun fromJobStatus(value: JobStatus): String = value.name
    @TypeConverter
    fun toJobStatus(value: String): JobStatus = JobStatus.valueOf(value)

    @TypeConverter
    fun fromLeaveStatus(value: LeaveStatus): String = value.name
    @TypeConverter
    fun toLeaveStatus(value: String): LeaveStatus = LeaveStatus.valueOf(value)
}
