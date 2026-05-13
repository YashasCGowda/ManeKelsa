package com.manekelsa.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [WorkerEntity::class, JobEntity::class, LeaveRequestEntity::class],
    version = 60,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ManeKelsaDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun jobDao(): JobDao
    abstract fun leaveDao(): LeaveDao
}
