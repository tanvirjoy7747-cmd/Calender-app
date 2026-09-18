package com.example;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Java utility class for calendar date calculations, month grid generation, and formatting.
 */
public final class CalendarDateUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault());
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault());

    private CalendarDateUtils() {}

    /**
     * Day of week headers starting from Sunday
     */
    public static List<String> getDayOfWeekHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("SUN");
        headers.add("MON");
        headers.add("TUE");
        headers.add("WED");
        headers.add("THU");
        headers.add("FRI");
        headers.add("SAT");
        return headers;
    }

    /**
     * Generates a 35 or 42 day grid for the given YearMonth, padding with previous
     * and next month's days to fit standard calendar weeks (Sunday to Saturday).
     */
    public static List<CalendarDay> generateMonthDays(
            YearMonth yearMonth,
            LocalDate selectedDate,
            Map<String, List<CalendarEvent>> eventsByDate
    ) {
        List<CalendarDay> days = new ArrayList<>();
        LocalDate today = LocalDate.now();

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Sunday = 7 in DayOfWeek enum, convert to 0-based where Sunday = 0
        int firstDayOfWeekVal = firstOfMonth.getDayOfWeek().getValue() % 7;

        // Previous month padding
        YearMonth prevMonth = yearMonth.minusMonths(1);
        int prevMonthDays = prevMonth.lengthOfMonth();
        for (int i = firstDayOfWeekVal - 1; i >= 0; i--) {
            LocalDate date = prevMonth.atDay(prevMonthDays - i);
            CalendarDay day = createDay(date, false, today, selectedDate, eventsByDate);
            days.add(day);
        }

        // Current month days
        for (int dayNum = 1; dayNum <= daysInMonth; dayNum++) {
            LocalDate date = yearMonth.atDay(dayNum);
            CalendarDay day = createDay(date, true, today, selectedDate, eventsByDate);
            days.add(day);
        }

        // Next month padding to fill complete weeks (multiples of 7)
        int remainingDays = (7 - (days.size() % 7)) % 7;
        // If grid has only 35 days and needs 6 rows to look uniform or remaining padding:
        if (days.size() + remainingDays < 35) {
            remainingDays += 7;
        }

        YearMonth nextMonth = yearMonth.plusMonths(1);
        for (int dayNum = 1; dayNum <= remainingDays; dayNum++) {
            LocalDate date = nextMonth.atDay(dayNum);
            CalendarDay day = createDay(date, false, today, selectedDate, eventsByDate);
            days.add(day);
        }

        return days;
    }

    private static CalendarDay createDay(
            LocalDate date,
            boolean isCurrentMonth,
            LocalDate today,
            LocalDate selectedDate,
            Map<String, List<CalendarEvent>> eventsByDate
    ) {
        boolean isToday = date.equals(today);
        boolean isSelected = selectedDate != null && date.equals(selectedDate);
        CalendarDay day = new CalendarDay(date, isCurrentMonth, isToday, isSelected);

        if (eventsByDate != null) {
            String isoDate = toIsoDate(date);
            List<CalendarEvent> dateEvents = eventsByDate.get(isoDate);
            if (dateEvents != null) {
                for (CalendarEvent event : dateEvents) {
                    day.addEvent(event);
                }
            }
        }
        return day;
    }

    public static Map<String, List<CalendarEvent>> groupEventsByDate(List<CalendarEvent> events) {
        Map<String, List<CalendarEvent>> map = new HashMap<>();
        if (events == null) return map;

        for (CalendarEvent event : events) {
            String date = event.getDate();
            if (date != null && !date.isEmpty()) {
                map.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
            }
        }
        return map;
    }

    public static String toIsoDate(LocalDate date) {
        return date.format(ISO_FORMATTER);
    }

    public static LocalDate parseIsoDate(String isoString) {
        try {
            return LocalDate.parse(isoString, ISO_FORMATTER);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    public static String formatMonthYear(YearMonth yearMonth) {
        return yearMonth.format(MONTH_YEAR_FORMATTER);
    }

    public static String formatFullDate(LocalDate date) {
        return date.format(FULL_DATE_FORMATTER);
    }

    public static String formatHeaderDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) {
            return "Today • " + date.format(SHORT_DATE_FORMATTER);
        } else if (date.equals(today.plusDays(1))) {
            return "Tomorrow • " + date.format(SHORT_DATE_FORMATTER);
        } else if (date.equals(today.minusDays(1))) {
            return "Yesterday • " + date.format(SHORT_DATE_FORMATTER);
        }
        return date.format(SHORT_DATE_FORMATTER);
    }
}
