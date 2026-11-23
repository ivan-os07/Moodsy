package ru.urfu.service.fsm.stats;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.application.Keyboard;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.fsm.FsmResult;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

class StatsFsmServiceTest {
    private StatsFsmService service;
    private InMemoryMoodRepository repository;
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
        Assertions.assertTrue(result.keyboard().isPresent());
        Assertions.assertTrue(service.hasActiveSession(userId));

        Keyboard keyboard = result.keyboard().orElseThrow();

        Assertions.assertEquals(2, keyboard.rows().size());

        // Первая строка: выбор периода
        Assertions.assertEquals(2, keyboard.rows().get(0).buttons().size());

        Assertions.assertEquals("Неделя", keyboard.rows().get(0).buttons().get(0).label());
        Assertions.assertEquals("week", keyboard.rows().get(0).buttons().get(0).action());

        Assertions.assertEquals("Месяц", keyboard.rows().get(0).buttons().get(1).label());
        Assertions.assertEquals("month", keyboard.rows().get(0).buttons().get(1).action());

        // Вторая строка: отмена
        Assertions.assertEquals(1, keyboard.rows().get(1).buttons().size());

        Assertions.assertEquals("Отмена", keyboard.rows().get(1).buttons().get(0).label());
        Assertions.assertEquals("/exit", keyboard.rows().get(1).buttons().get(0).action());
    }
    /**
     * Проверяем, что ввод неправильного периода не завершает диалог
     */
    @Test
    void testInvalidPeriodDoNotFinishSession() {
        service.startWithResponse(userId);
        FsmResult result = service.handleWithResponse(userId, "year");
        Assertions.assertEquals("❌ Ошибка: необходимо выбрать один из предложенных периодов", result.text());

        Assertions.assertTrue(result.keyboard().isPresent());
        Assertions.assertTrue(service.hasActiveSession(userId));
    }
}
