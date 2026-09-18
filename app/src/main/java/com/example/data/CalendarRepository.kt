package com.example.data

import com.example.CalendarDateUtils
import com.example.CalendarEvent
import com.example.CalendarEventManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class CalendarRepository(private val eventDao: EventDao) {

    val allEvents: Flow<List<CalendarEvent>> = eventDao.getAllEvents().map { entities ->
        entities.map { it.toCalendarEvent() }
    }

    suspend fun insertEvent(event: CalendarEvent): Long {
        return eventDao.insertEvent(EventEntity.fromCalendarEvent(event))
    }

    suspend fun updateEvent(event: CalendarEvent) {
        eventDao.updateEvent(EventEntity.fromCalendarEvent(event))
    }

    suspend fun deleteEvent(event: CalendarEvent) {
        eventDao.deleteById(event.id)
    }

    suspend fun deleteEventById(id: Long) {
        eventDao.deleteById(id)
    }

    suspend fun seedInitialDataIfEmpty(baseDate: LocalDate) {
        val count = eventDao.getCount()
        if (count == 0) {
            val sampleEvents = CalendarEventManager.getSampleEvents(baseDate)
            val entities = sampleEvents.map { EventEntity.fromCalendarEvent(it) }
            eventDao.insertAll(entities)
        }
    }
}
