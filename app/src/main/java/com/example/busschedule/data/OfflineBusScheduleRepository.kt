package com.example.busschedule.data

import kotlinx.coroutines.flow.Flow

class OfflineBusScheduleRepository(private val scheduleDao: ScheduleDao) : BusScheduleRepository {
    override fun getAllBusSchedulesStream(): Flow<List<BusSchedule>> = scheduleDao.getAllSchedules()

    override fun getBusScheduleStream(id: Int): Flow<BusSchedule?> = scheduleDao.getSchedule(id)

    override fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> = scheduleDao.getScheduleFor(stopName)

    override suspend fun insertBusSchedule(busSchedule: BusSchedule) = scheduleDao.insert(busSchedule)

    override suspend fun deleteBusSchedule(busSchedule: BusSchedule) = scheduleDao.delete(busSchedule)

    override suspend fun updateBusSchedule(busSchedule: BusSchedule) = scheduleDao.update(busSchedule)


}