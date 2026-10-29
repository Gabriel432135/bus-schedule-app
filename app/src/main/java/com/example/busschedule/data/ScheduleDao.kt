package com.example.busschedule.data

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: BusSchedule)

    @Update
    suspend fun update(schedule: BusSchedule)

    @Delete
    suspend fun delete(schedule: BusSchedule)

    @Query("SELECT * from schedule WHERE id = :id")
    fun getSchedule(id: Int): Flow<BusSchedule>

    @Query("SELECT * from schedule ORDER BY arrivalTimeInMillis ASC")
    fun getAllSchedules(): Flow<List<BusSchedule>>
}