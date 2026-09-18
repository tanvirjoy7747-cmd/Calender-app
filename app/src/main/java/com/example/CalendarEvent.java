package com.example;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Java model representing a Calendar Event.
 */
public class CalendarEvent implements Serializable {
    private long id;
    private String title;
    private String description;
    private String date; // Format: yyyy-MM-dd
    private String startTime; // Format: HH:mm
    private String endTime; // Format: HH:mm
    private boolean isAllDay;
    private EventCategory category;
    private String location;

    public CalendarEvent() {
        this.category = EventCategory.OTHER;
        this.isAllDay = false;
    }

    public CalendarEvent(long id, String title, String description, String date,
                         String startTime, String endTime, boolean isAllDay,
                         EventCategory category, String location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAllDay = isAllDay;
        this.category = category != null ? category : EventCategory.OTHER;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date != null ? date : "";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime != null ? startTime : "";
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime != null ? endTime : "";
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public EventCategory getCategory() {
        return category != null ? category : EventCategory.OTHER;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Formats the time range as a user-friendly string (e.g., "9:00 AM - 10:30 AM" or "All Day")
     */
    public String getFormattedTimeRange() {
        if (isAllDay) {
            return "All Day";
        }
        if (startTime == null || startTime.trim().isEmpty()) {
            return "All Day";
        }

        try {
            LocalTime sTime = LocalTime.parse(startTime);
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("h:mm a");
            String startFormatted = sTime.format(displayFormatter);

            if (endTime != null && !endTime.trim().isEmpty()) {
                LocalTime eTime = LocalTime.parse(endTime);
                String endFormatted = eTime.format(displayFormatter);
                return startFormatted + " - " + endFormatted;
            }
            return startFormatted;
        } catch (DateTimeParseException e) {
            return startTime + (endTime != null && !endTime.isEmpty() ? " - " + endTime : "");
        }
    }

    public LocalDate getLocalDate() {
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarEvent that = (CalendarEvent) o;
        return id == that.id &&
                isAllDay == that.isAllDay &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(date, that.date) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                category == that.category &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, date, startTime, endTime, isAllDay, category, location);
    }
}
