package com.manekelsa.app.data.local

import com.manekelsa.app.domain.model.*

fun Worker.toEntity() = WorkerEntity(
    id = id,
    name = name,
    email = email,
    role = role,
    profileImageUrl = profileImageUrl,
    phone = phone,
    skills = skills,
    experience = experience,
    hourlyRate = hourlyRate,
    dailyRate = dailyRate,
    baseSalary = baseSalary,
    absentDays = absentDays,
    deductions = deductions,
    walletBalance = walletBalance,
    kycStatus = kycStatus,
    rating = rating,
    totalRatings = totalRatings,
    isAvailable = isAvailable,
    latitude = latitude,
    longitude = longitude,
    city = city,
    assignedArea = assignedArea,
    preferredAreas = preferredAreas,
    bio = bio,
    completedJobs = completedJobs,
    identityVerified = identityVerified,
    isBlocked = isBlocked
)

fun WorkerEntity.toDomain() = Worker(
    id = id,
    name = name,
    email = email,
    role = role,
    profileImageUrl = profileImageUrl,
    phone = phone,
    skills = skills,
    experience = experience,
    hourlyRate = hourlyRate,
    dailyRate = dailyRate,
    baseSalary = baseSalary,
    absentDays = absentDays,
    deductions = deductions,
    walletBalance = walletBalance,
    kycStatus = kycStatus,
    rating = rating,
    totalRatings = totalRatings,
    isAvailable = isAvailable,
    latitude = latitude,
    longitude = longitude,
    city = city,
    assignedArea = assignedArea,
    preferredAreas = preferredAreas,
    bio = bio,
    completedJobs = completedJobs,
    identityVerified = identityVerified,
    isBlocked = isBlocked
)

fun JobRequest.toEntity() = JobEntity(
    id = id,
    workerId = workerId,
    title = title,
    description = description,
    location = location,
    payPerDay = payPerDay,
    status = status
)

fun JobEntity.toDomain() = JobRequest(
    id = id,
    workerId = workerId,
    title = title,
    description = description,
    location = location,
    payPerDay = payPerDay,
    status = status
)

fun LeaveRequest.toEntity() = LeaveRequestEntity(
    id = id,
    workerId = workerId,
    workerName = workerName,
    workerEmail = workerEmail,
    reason = reason,
    startDate = startDate,
    endDate = endDate,
    status = status
)

fun LeaveRequestEntity.toDomain() = LeaveRequest(
    id = id,
    workerId = workerId,
    workerName = workerName,
    workerEmail = workerEmail,
    reason = reason,
    startDate = startDate,
    endDate = endDate,
    status = status
)
