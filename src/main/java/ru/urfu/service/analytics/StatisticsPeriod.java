package ru.urfu.service.analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Периоды, за которые доступна статистика
 */
public enum StatisticsPeriod {
    WEEK("week", "неделю"),
    MONTH("month", "месяц");

    private final String argument;
    private final String displayName;
    StatisticsPeriod(String argument, String displayName) {
        this.argument = argument;
        this.displayName = displayName;
    }

    public String getArgument() {
        return argument;
    }
    public String getDisplayName() {
        return displayName;
    }

    public LocalDateTime getStart(LocalDateTime currentDateTime){
        Objects.requireNonNull(currentDateTime, "currentDateTime не может быть null");
        LocalDate currentDate = currentDateTime.toLocalDate();
        if (this == WEEK){
            int daysFromMonday = currentDate.getDayOfWeek().getValue() - 1;
            return currentDate.minusDays(daysFromMonday).atStartOfDay();
        }
        return currentDate.withDayOfMonth(1).atStartOfDay();

    }
    public LocalDateTime getEndExclusive(LocalDateTime currentDateTime){
        LocalDateTime start =  getStart(currentDateTime);
        if (this == WEEK){
            return start.plusWeeks(1);
        }
        return start.plusMonths(1);
    }

}

