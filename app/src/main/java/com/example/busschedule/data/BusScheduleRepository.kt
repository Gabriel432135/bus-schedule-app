package com.example.busschedule.data

import kotlinx.coroutines.flow.Flow

interface BusScheduleRepository {
    fun getAllBusSchedulesStream(): Flow<List<BusSchedule>>
    fun getBusScheduleStream(id: Int): Flow<BusSchedule?>
    fun getScheduleFor(stopName: String): Flow<List<BusSchedule>>
    suspend fun insertBusSchedule(busSchedule: BusSchedule)
    suspend fun deleteBusSchedule(busSchedule: BusSchedule)
    suspend fun updateBusSchedule(busSchedule: BusSchedule)
}