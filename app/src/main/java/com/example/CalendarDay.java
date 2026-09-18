package com.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Java model representing a single cell in the calendar grid.
 */
public class CalendarDay {
    private final LocalDate date;
    private final int dayOfMonth;
    private final boolean isCurrentMonth;
    private final boolean isToday;
    private boolean isSelected;
    private final List<CalendarEvent> events;

    public CalendarDay(LocalDate date, boolean isCurrentMonth, boolean isToday, boolean isSelected) {
        this.date = date;
        this.dayOfMonth = date.getDayOfMonth();
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
        this.isSelected = isSelected;
        this.events = new ArrayList<>();
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public boolean isToday() {
        return isToday;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public List<CalendarEvent> getEvents() {
        return events;
    }

    public void addEvent(CalendarEvent event) {
        if (event != null) {
            events.add(event);
        }
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    public int getEventCount() {
        return events.size();
    }

    /**
     * Get unique event category colors for drawing dots on the day cell
     */
    public List<Long> getUniqueCategoryColors() {
        List<Long> colors = new ArrayList<>();
        Set<EventCategory> seenCategories = new HashSet<>();
        for (CalendarEvent event : events) {
            if (event.getCategory() != null && seenCategories.add(event.getCategory())) {
                colors.add(event.getCategory().getColorValue());
                if (colors.size() >= 3) break; // Limit to 3 dots
            }
        }
        return colors;
    }
}
