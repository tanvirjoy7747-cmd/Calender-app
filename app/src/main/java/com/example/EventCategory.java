package com.example;

public enum EventCategory {
    WORK("Work", 0xFF1976D2),         // Blue
    PERSONAL("Personal", 0xFF388E3C), // Green
    MEETING("Meeting", 0xFFF57C00),   // Orange
    REMINDER("Reminder", 0xFF7B1FA2), // Purple
    CELEBRATION("Celebration", 0xFFD32F2F), // Red / Coral
    OTHER("Other", 0xFF00796B);       // Teal

    private final String displayName;
    private final long colorValue;

    EventCategory(String displayName, long colorValue) {
        this.displayName = displayName;
        this.colorValue = colorValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getColorValue() {
        return colorValue;
    }

    public static EventCategory fromString(String name) {
        if (name == null) return OTHER;
        try {
            return EventCategory.valueOf(name);
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
