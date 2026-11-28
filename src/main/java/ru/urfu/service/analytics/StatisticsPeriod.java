package ru.urfu.service.analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Периоды, за которые доступна статистика
 */
public enum StatisticsPeriod {
    /**
     * Календарная неделя, с понедельника по воскресенье
     */
    WEEK("week", "неделю"),
    /**
     * Календарный месяц, с первого по последний день
     */
    MONTH("month", "месяц");
    /**
     * Аргумент команды, который отвечает за выбор периода
     */
    private final String argument;
    /**
     * Название периода в пользовательском сообщении
     */
    private final String displayName;

    /**
     * Создает период статистики
     *
     * @param argument    аргумент команды
     * @param displayName отображаемое название
     */
    StatisticsPeriod(String argument, String displayName) {
        this.argument = argument;
        this.displayName = displayName;
    }

    /**
     * Геттер на аргумент команды
     *
     * @return строковый аргумент периода
     */
    public String getArgument() {
        return argument;
    }

    /**
     * Геттер на отображаемое название
     *
     * @return отображаемое название периода
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Вычисляет начало периода
     *
     * @param currentDateTime текущие дата и время
     * @return начало недели или месяца в полночь
     */
    public LocalDateTime getStart(LocalDateTime currentDateTime) {
        Objects.requireNonNull(currentDateTime, "currentDateTime не может быть null");
        LocalDate currentDate = currentDateTime.toLocalDate();
        if (this == WEEK) {
            int daysFromMonday = currentDate.getDayOfWeek().getValue() - 1;
            return currentDate.minusDays(daysFromMonday).atStartOfDay();
        }
        return currentDate.withDayOfMonth(1).atStartOfDay();

    }

    /**
     * Вычисляет верхнюю границу календарного периода не включительно
     *
     * @param currentDateTime текущая дата и время
     * @return начало следующей недели или месяца
     */
    public LocalDateTime getEndExclusive(LocalDateTime currentDateTime) {
        Objects.requireNonNull(currentDateTime, "currentDateTime не может быть null");
        LocalDateTime start = getStart(currentDateTime);
        if (this == WEEK) {
            return start.plusWeeks(1);
        }
        return start.plusMonths(1);
    }

}

