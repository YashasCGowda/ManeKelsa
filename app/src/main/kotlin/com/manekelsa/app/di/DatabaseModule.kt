package com.manekelsa.app.di

import android.content.Context
import androidx.room.Room
import com.manekelsa.app.data.local.JobDao
import com.manekelsa.app.data.local.LeaveDao
import com.manekelsa.app.data.local.ManeKelsaDatabase
import com.manekelsa.app.data.local.WorkerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ManeKelsaDatabase {
        return Room.databaseBuilder(
            context,
            ManeKelsaDatabase::class.java,
            "manekelsa_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideWorkerDao(db: ManeKelsaDatabase): WorkerDao = db.workerDao()

    @Provides
    fun provideJobDao(db: ManeKelsaDatabase): JobDao = db.jobDao()

    @Provides
    fun provideLeaveDao(db: ManeKelsaDatabase): LeaveDao = db.leaveDao()
}
