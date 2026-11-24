package ru.urfu.service.fsm.stats;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.urfu.application.Keyboard;
import ru.urfu.model.MoodEntry;
import ru.urfu.model.Sphere;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.fsm.FsmResult;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Тестируем fsm команды stats
 */
class StatsFsmServiceTest {

    /**
     * Сервис конечного автомата статистики
     */
    private StatsFsmService service;

    /**
     * Репозиторий отметок
     */
    private InMemoryMoodRepository repository;

    /**
     * Внутренний идентификатор пользователя
     */
    private UUID userId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMoodRepository();
        userId = UUID.randomUUID();

        Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.ofHours(3));

        service = new StatsFsmService(repository, new MoodAnalyticsService(), clock);
    }

    /**
     * Тестируем запуск диалога и наличие клавиатуры выбора периода
     */
    @Test
    void testStartWithResponse() {
        FsmResult result = service.startWithResponse(userId);

        Assertions.assertEquals("Выберите период статистики:", result.text());
        Assertions.assertTrue(service.hasActiveSession(userId));

        Keyboard keyboard = result.keyboard().orElseThrow();

        Assertions.assertEquals(2, keyboard.rows().size());

        Assertions.assertEquals(2, keyboard.rows().getFirst().buttons().size());
        Assertions.assertEquals("Неделя", keyboard.rows().getFirst().buttons().getFirst().label());
        Assertions.assertEquals("week", keyboard.rows().getFirst().buttons().getFirst().action());

        Assertions.assertEquals("Месяц", keyboard.rows().get(0).buttons().get(1).label());
        Assertions.assertEquals("month", keyboard.rows().get(0).buttons().get(1).action());

        Assertions.assertEquals("Отмена", keyboard.rows().get(1).buttons().getFirst().label());
        Assertions.assertEquals("/exit", keyboard.rows().get(1).buttons().getFirst().action());
    }

    /**
     * Тестируем, что неизвестный период не завершает диалог и возвращает клавиатуру
     */
    @Test
    void testInvalidPeriodDoesNotFinishSession() {
        service.startWithResponse(userId);
        FsmResult result = service.handleWithResponse(userId, "year");

        Assertions.assertEquals(
                "❌ Ошибка: необходимо выбрать один из предложенных периодов",
                result.text()
        );
        Assertions.assertTrue(result.keyboard().isPresent());
        Assertions.assertTrue(service.hasActiveSession(userId));
    }

    /**
     * Тестируем отмену диалога командой /exit и завершение пользовательской сессии
     */
    @Test
    void testExitCancelsSession() {
        service.startWithResponse(userId);

        FsmResult result = service.handleWithResponse(userId, "/exit");

        Assertions.assertEquals("Выбор периода статистики отменён.", result.text());
        Assertions.assertTrue(result.keyboard().isEmpty());
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Тестируем, что команда отмены распознается без учета регистра и внешних пробелов
     */
    @Test
    void testExitWithDifferentCaseAndWhitespaceCancelsSession() {
        service.start(userId);

        FsmResult result = service.handleWithResponse(userId, "  /EXIT  ");

        Assertions.assertEquals("Выбор периода статистики отменён.", result.text());
        Assertions.assertFalse(service.hasActiveSession(userId));
        Assertions.assertTrue(result.keyboard().isEmpty());
    }

    /**
     * Тестируем расчёт недельной статистики только по записям выбранной недели
     */
    @Test
    void testSelectWeekReturnsStatisticsForCurrentWeek() {
        repository.save(userId, entry("2026-11-02T10:00", 5, Sphere.WORK));
        repository.save(userId, entry("2026-11-03T10:00", 9, Sphere.WORK));
        repository.save(userId, entry("2026-11-08T23:59", 7, Sphere.SOCIAL));
        repository.save(userId, entry("2026-11-09T00:00", 1, Sphere.REST));

        service.start(userId);
        FsmResult result = service.handleWithResponse(userId, "week");

        Assertions.assertEquals("""
                📊 Статистика за неделю (02.11.2026 - 08.11.2026)
                Средняя оценка: 7,00/10
                Лучший день: 03.11.2026 10:00 - 9/10
                Худший день: 02.11.2026 10:00 - 5/10
                Самая частая сфера: Дела (2 отметки)""", result.text()
        );
        Assertions.assertTrue(result.keyboard().isEmpty());
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Тестируем расчёт месячной статистики с календарными границами месяца.
     */
    @Test
    void testSelectMonthReturnsStatisticsForCurrentMonth() {
        repository.save(userId, entry("2026-10-31T23:59", 1, Sphere.REST));
        repository.save(userId, entry("2026-11-01T10:00", 4, Sphere.WORK));
        repository.save(userId, entry("2026-11-30T10:00", 10, Sphere.WORK));
        repository.save(userId, entry("2026-12-01T00:00", 1, Sphere.SOCIAL));

        service.start(userId);
        FsmResult result = service.handleWithResponse(userId, "month");

        Assertions.assertEquals("""
                        📊 Статистика за месяц (01.11.2026 - 30.11.2026)
                        Средняя оценка: 7,00/10
                        Лучший день: 30.11.2026 10:00 - 10/10
                        Худший день: 01.11.2026 10:00 - 4/10
                        Самая частая сфера: Дела (2 отметки)""",
                result.text());
        Assertions.assertTrue(result.keyboard().isEmpty());
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Тестируем правильное склонение слова "отметка" для разных количеств записей
     */
    @ParameterizedTest
    @CsvSource({
            "1, отметка",
            "2, отметки",
            "4, отметки",
            "5, отметок",
            "11, отметок",
            "21, отметка"
    })
    void testStatisticsUsesCorrectMarkWord(int count, String expectedWord) {
        for (int i = 0; i < count; i++) {
            repository.save(userId,
                    entry(LocalDateTime.of(2026, 11, 2, 10, 0)
                                    .plusMinutes(i).toString(),
                            5, Sphere.WORK));
        }

        service.start(userId);
        FsmResult result = service.handleWithResponse(userId, "week");

        Assertions.assertTrue(
                result.text().endsWith("(" + count + " " + expectedWord + ")")
        );
    }

    /**
     * Тестируем сообщение при отсутствии отметок за выбранный период
     */
    @Test
    void testSelectPeriodWithoutEntriesReturnsEmptyMessage() {
        service.start(userId);

        FsmResult result = service.handleWithResponse(userId, "week");

        Assertions.assertEquals("Записей нет, используйте /checkin", result.text());
        Assertions.assertTrue(result.keyboard().isEmpty());
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Тестируем, что пробелы и регистр не мешают распознаванию периода
     */
    @Test
    void testSelectPeriodTrimsInputAndIgnoresCase() {
        repository.save(userId, entry("2026-11-02T10:00", 6, Sphere.HEALTH));
        service.start(userId);

        FsmResult result = service.handleWithResponse(userId, "  WEEK  ");

        Assertions.assertEquals("""
                📊 Статистика за неделю (02.11.2026 - 08.11.2026)
                Средняя оценка: 6,00/10
                Лучший день: 02.11.2026 10:00 - 6/10
                Худший день: 02.11.2026 10:00 - 6/10
                Самая частая сфера: Здоровье (1 отметка)""", result.text());
        Assertions.assertTrue(result.keyboard().isEmpty());
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Тестируем, что null вместо пользовательского ввода обрабатывается как неизвестный период
     */
    @Test
    void testNullInputDoesNotFinishSession() {
        service.start(userId);

        FsmResult result = service.handleWithResponse(userId, null);

        Assertions.assertEquals(
                "❌ Ошибка: необходимо выбрать один из предложенных периодов",
                result.text()
        );
        Assertions.assertTrue(result.keyboard().isPresent());
        Assertions.assertTrue(service.hasActiveSession(userId));
    }

    /**
     * Тестируем, что ввод без активной сессии не запускает обработку периода
     */
    @Test
    void testHandleWithoutActiveSessionReturnsInputUnchanged() {
        FsmResult result = service.handleWithResponse(userId, "week");

        Assertions.assertEquals("week", result.text());
        Assertions.assertTrue(result.keyboard().isEmpty());
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Тестируем независимость сессий выбора периода разных пользователей
     */
    @Test
    void testSessionsAreIndependentForDifferentUsers() {
        UUID anotherUserId = UUID.randomUUID();
        service.start(userId);

        FsmResult result = service.handleWithResponse(anotherUserId, "week");

        Assertions.assertEquals("week", result.text());
        Assertions.assertTrue(service.hasActiveSession(userId));
        Assertions.assertFalse(service.hasActiveSession(anotherUserId));
    }

    /**
     * Тестируем отклонение null в качестве идентификатора пользователя.
     */
    @Test
    void testStartRejectsNullUserId() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> service.start(null)
        );

        Assertions.assertEquals("userId не может быть null", exception.getMessage());
    }

    /**
     * Тестируем отклонение null в качестве идентификатора при обработке ввода.
     */
    @Test
    void testHandleRejectsNullUserId() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> service.handleWithResponse(null, "week")
        );

        Assertions.assertEquals("userId не может быть null", exception.getMessage());
    }

    /**
     * Создаёт отметку для тестовых сценариев статистики.
     */
    private MoodEntry entry(String dateTime, int score, Sphere sphere) {
        return new MoodEntry(LocalDateTime.parse(dateTime), score, sphere, "");
    }
}