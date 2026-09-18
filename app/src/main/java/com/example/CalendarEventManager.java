package com.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Java business logic manager for filtering, sorting, and initializing calendar events.
 */
public final class CalendarEventManager {

    private CalendarEventManager() {}

    /**
     * Sort events by date ascending, then all-day first, then by start time, then title.
     */
    public static List<CalendarEvent> sortEvents(List<CalendarEvent> events) {
        if (events == null) return Collections.emptyList();
        List<CalendarEvent> sorted = new ArrayList<>(events);
        sorted.sort(new Comparator<CalendarEvent>() {
            @Override
            public int compare(CalendarEvent e1, CalendarEvent e2) {
                int dateComp = e1.getDate().compareTo(e2.getDate());
                if (dateComp != 0) return dateComp;

                if (e1.isAllDay() && !e2.isAllDay()) return -1;
                if (!e1.isAllDay() && e2.isAllDay()) return 1;

                int timeComp = e1.getStartTime().compareTo(e2.getStartTime());
                if (timeComp != 0) return timeComp;

                return e1.getTitle().compareToIgnoreCase(e2.getTitle());
            }
        });
        return sorted;
    }

    /**
     * Filter events by a text query and/or category.
     */
    public static List<CalendarEvent> filterEvents(
            List<CalendarEvent> events,
            String query,
            EventCategory category
    ) {
        if (events == null) return Collections.emptyList();

        List<CalendarEvent> result = new ArrayList<>();
        String lowerQuery = query != null ? query.trim().toLowerCase() : "";

        for (CalendarEvent event : events) {
            boolean matchesCategory = (category == null || event.getCategory() == category);
            if (!matchesCategory) continue;

            if (lowerQuery.isEmpty()) {
                result.add(event);
            } else {
                boolean matchesTitle = event.getTitle().toLowerCase().contains(lowerQuery);
                boolean matchesDesc = event.getDescription().toLowerCase().contains(lowerQuery);
                boolean matchesLoc = event.getLocation().toLowerCase().contains(lowerQuery);
                if (matchesTitle || matchesDesc || matchesLoc) {
                    result.add(event);
                }
            }
        }
        return sortEvents(result);
    }

    /**
     * Get default initial sample events around the current date to showcase the calendar.
     */
    public static List<CalendarEvent> getSampleEvents(LocalDate baseDate) {
        List<CalendarEvent> list = new ArrayList<>();
        String today = CalendarDateUtils.toIsoDate(baseDate);
        String tomorrow = CalendarDateUtils.toIsoDate(baseDate.plusDays(1));
        String dayAfterTomorrow = CalendarDateUtils.toIsoDate(baseDate.plusDays(2));
        String nextWeek = CalendarDateUtils.toIsoDate(baseDate.plusDays(5));
        String yesterday = CalendarDateUtils.toIsoDate(baseDate.minusDays(1));

        list.add(new CalendarEvent(
                0,
                "Team Standup & Planning",
                "Daily project sync with the mobile engineering team",
                today,
                "09:30",
                "10:00",
                false,
                EventCategory.MEETING,
                "Conference Room A / Google Meet"
        ));

        list.add(new CalendarEvent(
                0,
                "Doctor Appointment",
                "Annual health checkup at St. Jude Clinic",
                today,
                "14:00",
                "15:00",
                false,
                EventCategory.PERSONAL,
                "Health Center 4th Floor"
        ));

        list.add(new CalendarEvent(
                0,
                "Gym & Cardio Workout",
                "Leg day and 30 minutes cardio session",
                today,
                "18:00",
                "19:15",
                false,
                EventCategory.PERSONAL,
                "Downtown Fitness Club"
        ));

        list.add(new CalendarEvent(
                0,
                "Sarah's Birthday Party",
                "Celebrate Sarah's birthday with friends and family",
                tomorrow,
                "19:00",
                "22:00",
                false,
                EventCategory.CELEBRATION,
                "Bistro Bella Vista"
        ));

        list.add(new CalendarEvent(
                0,
                "Quarterly Product Review",
                "Presentation of Q3 milestones and roadmap goals",
                dayAfterTomorrow,
                "11:00",
                "12:30",
                false,
                EventCategory.WORK,
                "Main Auditorium"
        ));

        list.add(new CalendarEvent(
                0,
                "Pay Electricity & Utility Bills",
                "Monthly reminder to settle electricity and internet invoices",
                baseDate.plusDays(3).toString(),
                "09:00",
                "09:15",
                false,
                EventCategory.REMINDER,
                "Online Banking"
        ));

        list.add(new CalendarEvent(
                0,
                "Weekend Family Trip",
                "Relaxing weekend getaway to the lake cabin",
                nextWeek,
                "",
                "",
                true,
                EventCategory.PERSONAL,
                "Pine Lake Resort"
        ));

        return list;
    }
}
