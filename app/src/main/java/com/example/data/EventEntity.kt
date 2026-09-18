package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.CalendarEvent
import com.example.EventCategory

@Entity(tableName = "calendar_events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val date: String, // yyyy-MM-dd
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val isAllDay: Boolean,
    val category: String, // Enum name
    val location: String
) {
    fun toCalendarEvent(): CalendarEvent {
        return CalendarEvent(
            id,
            title,
            description,
            date,
            startTime,
            endTime,
            isAllDay,
            EventCategory.fromString(category),
            location
        )
    }

    companion object {
        fun fromCalendarEvent(event: CalendarEvent): EventEntity {
            return EventEntity(
                id = event.id,
                title = event.title,
                description = event.description,
                date = event.date,
                startTime = event.startTime,
                endTime = event.endTime,
                isAllDay = event.isAllDay,
                category = event.category.name,
                location = event.location
            )
        }
    }
}
