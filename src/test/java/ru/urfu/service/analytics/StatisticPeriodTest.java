package ru.urfu.service.analytics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тестируем на границы календарные периоды статистики
 */
public class StatisticPeriodTest {
    /**
     * Тестируем, что начало недели в понедельник для даты в середине недели
     */
    @Test
    void TestCalculatesWeek() {
        LocalDateTime current = LocalDateTime.of(2026, 11, 18, 17, 45);
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 16, 0, 0),
                StatisticsPeriod.WEEK.getStart(current));
    }

    /**
     * Тестируем, что границы верные при вызове в понедельник и воскресенье
     */
    @Test
    void TestCalculatesMondaySundayTest() {
        LocalDateTime sunday = LocalDateTime.of(2026, 11, 22, 23, 59);
        LocalDateTime monday = LocalDateTime.of(2026, 11, 23, 0, 0);
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 16, 0, 0),
                StatisticsPeriod.WEEK.getStart(sunday));
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 23, 0, 0),
                StatisticsPeriod.WEEK.getStart(monday));
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 23, 0, 0),
                StatisticsPeriod.WEEK.getEndExclusive(sunday));

    }

    /**
     * Тестируем, что дня в конце месяца возвращается верное начало месяца
     */
    @Test
    void testMonthStarts() {
        LocalDateTime current = LocalDateTime.of(2026, 11, 30, 23, 59);
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0)
                , StatisticsPeriod.MONTH.getStart(current));
    }

    /**
     * Тестируем верхнюю границу месяца, включая переход между годами
     */
    @Test
    void testMonthEnds() {
        LocalDateTime current = LocalDateTime.of(2026, 12, 31, 23, 59);
        Assertions.assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0),
                StatisticsPeriod.MONTH.getEndExclusive(current));
    }

    /**
     * Тестируем на исключения при передаче null вместо аргумента для
     * команд поиска начала и конца периода
     */
    @Test
    void testRejectNull() {
        NullPointerException weekExeption =
                Assertions.assertThrows(NullPointerException.class,
                        () -> StatisticsPeriod.WEEK.getStart(null));
        NullPointerException monthExeption =
                Assertions.assertThrows(NullPointerException.class,
                        () -> StatisticsPeriod.MONTH.getEndExclusive(null));
        Assertions.assertEquals("currentDateTime не может быть null",
                weekExeption.getMessage());
        Assertions.assertEquals("currentDateTime не может быть null",
                monthExeption.getMessage());

    }
}

