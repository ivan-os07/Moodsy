package ru.urfu.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.urfu.model.MoodEntry;
import ru.urfu.model.Sphere;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.analytics.MoodAnalyticsService;


import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Проверяет формирование статистики по команде /stats
 * на фиксированном времени
 */
public class StatsCommandTest {
    private StatsCommand command;
    private InMemoryMoodRepository repository;
    private UUID userId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMoodRepository();
        userId = UUID.randomUUID();
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneId.of("Europe/Moscow"));
        command = new StatsCommand(repository, new MoodAnalyticsService(), clock);
    }

    /**
     * Тестируем обработку отсутствующих, лишних и неизвестных аргуметнов
     */
    @Test
    void TestRejectIvalidArguments() {
        String expected = """
                ❌ Неверные аргументы.
                Использование: /stats <период>
                Доступные периоды: week, month.
                """;
        Assertions.assertEquals(expected, command.execute(userId, null));
        Assertions.assertEquals(expected, command.execute(userId, new String[0]));
        Assertions.assertEquals(expected, command.execute(userId, new String[]{"week", "month"}));
        Assertions.assertEquals(expected, command.execute(userId, new String[]{"year"}));

    }

    /**
     * Тестируем сообщение для пользователя, у которого нет отметок за выбранный период
     */
    @Test
    void TestMissingEntriesForPeriod() {
        Assertions.assertEquals("Записей нет, используйте /checkin", command.execute(userId, new String[]{"week"}));
    }

    /**
     * Тестируем, какие записи попадают в статистику за текущую неделю
     */
    @Test
    void TestCalculatesStatisticWeek() {
        repository.save(userId, new MoodEntry(
                LocalDateTime.of(2026, 11, 3, 0, 0),
                4,
                Sphere.WORK,
                ""
        ));

        repository.save(userId, new MoodEntry(
                LocalDateTime.of(2026, 11, 3, 12, 0),
                8,
                Sphere.REST,
                ""
        ));

        repository.save(userId, new MoodEntry(
                LocalDateTime.of(2026, 11, 4, 0, 0),
                10,
                Sphere.HEALTH,
                ""
        ));
        String result = command.execute(userId, new String[]{"WEEK"});
        String expected = """
                📊 Статистика за неделю (02.11.2026 - 08.11.2026)
                Средняя оценка; 7,33/10
                Лучший день: 04.11.2026 00:00 - 10/10
                Худший день: 03.11.2026 00:00 - 4/10
                Самая частая сфера: Здоровье (1 отметка)""";
        Assertions.assertEquals(expected, result);

    }

    /**
     * Тестируем, какие записи попадают в статистику за текущий месяц
     */
    @Test
    void TestCalculatesStatisticMonth() {
        repository.save(userId, new MoodEntry(
                LocalDateTime.of(2026, 11, 1, 0, 30),
                7,
                Sphere.SOCIAL,
                ""
        ));

        repository.save(userId, new MoodEntry(
                LocalDateTime.of(2026, 10, 31, 23, 59),
                3,
                Sphere.WORK,
                ""
        ));

        repository.save(userId, new MoodEntry(
                LocalDateTime.of(2026, 12, 1, 0, 0),
                9,
                Sphere.HEALTH,
                ""
        ));
        String result = command.execute(userId, new String[]{"MONTH"});
        String expected = """
                📊 Статистика за месяц (01.11.2026 - 30.11.2026)
                Средняя оценка; 7,00/10
                Лучший день: 01.11.2026 00:30 - 7/10
                Худший день: 01.11.2026 00:30 - 7/10
                Самая частая сфера: Общение (1 отметка)""";
        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "1, отметка",
            "2, отметки",
            "4, отметки",
            "5, отметок",
            "11, отметок",
            "21, отметка",
            "22, отметки",

    })
    void TestRussianFormsSphereCounts(int count, String expected) {
        for (int i = 0; i < count; i++) {
            repository.save(userId, new MoodEntry(
                    LocalDateTime.of(2026, 11, 2, 0, i),
                    5,
                    Sphere.WORK,
                    ""
            ));
        }
        String result = command.execute(userId, new String[]{"week"});

        String expectedLastLine = "Самая частая сфера: Дела ("
                + count + " " + expected + ")";

        String actualLastLine = result.substring(result.lastIndexOf("\n") + 1);

        Assertions.assertEquals(expectedLastLine, actualLastLine);

    }
}
