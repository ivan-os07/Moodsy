package ru.urfu.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.fsm.stats.StatsFsmService;


import java.time.*;
import java.util.UUID;

/**
 * Тестируем формирование статистики по команде /stats
 * на фиксированном времени
 */
public class StatsCommandTest {
    /**
     * Команда получения статистики
     */
    private StatsCommand command;
    /**
     * Сервис конечного автомата
     */
    private StatsFsmService statsFsmService;
    /**
     * Внутренний идентификатор пользователя
     */
    private UUID userId;
    /**
     * Фиксированные часы, источник текущего времени
     */
    Clock clock = Clock.fixed(
            Instant.parse("2026-11-02T12:00:00Z"),
            ZoneId.of("Europe/Moscow")
    );

    /**
     * Инициализация команды и зависимостей перед каждым тестом
     */

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        statsFsmService = new StatsFsmService(
                new InMemoryMoodRepository(),
                new MoodAnalyticsService(), clock
        );
        command = new StatsCommand(statsFsmService);
    }

    /**
     * Тестируем получение ключа команды /stats
     */
    @Test
    void testGetCommandKey() {
        Assertions.assertEquals("/stats", command.getCommandKey());
    }

    /**
     * Тестируем получение описания команды /stats
     */
    @Test
    void testGetDescription() {
        Assertions.assertEquals(
                "Получить статистику своих отметок",
                command.getDescription()
        );
    }

    /**
     * Тестируем запуск сессии при выхове команды
     */
    @Test
    void testExecuteStartsSession(){
        String result = command.execute(userId, new String[0]);
        Assertions.assertEquals(
                "Выберите период статистики:",
                result
        );
        Assertions.assertTrue(statsFsmService.hasActiveSession(userId));
    }

    /**
     * Тестируем запуск сессии при передаче null вместо аргумента
     */
    @Test
    void testExecuteWithNullArguments(){
        String result = command.execute(userId, null);
        Assertions.assertEquals(
                "Выберите период статистики:",
                result
        );
        Assertions.assertTrue(statsFsmService.hasActiveSession(userId));

    }

    /**
     * Тестируем отклонение команды при передаче одного аргумента
     * старый вариант команды - не работает
     */
    @Test
    void testExecuteRejectArguments(){
        String expected = "❌ Команда /stats вызывается без параметров";
        Assertions.assertEquals(
                expected,
                command.execute(userId, new String[]{"week"})

        );
        Assertions.assertEquals(
                expected,
                command.execute(userId, new String[]{"month"})

        );
        Assertions.assertFalse(statsFsmService.hasActiveSession(userId));

    }

    /**
     * Тестируем формирование ответа
     * с клавиатурой при вызове команды без аргументов
     */
    @Test
    void testExecuteWithResponseStartsSession(){
        BotResponse response = command.executeWithResponse(userId, new String[0]);

        Assertions.assertEquals(userId, response.userId());
        Assertions.assertEquals(
                "Выберите период статистики:",
                response.text()
        );
        Assertions.assertTrue(response.keyboard().isPresent());
        Assertions.assertTrue(statsFsmService.hasActiveSession(userId));
    }

    /**
     * Тестируем формирование ответа с ошибкой
     * и отсутствие запуска сессии
     */
    @Test
    void testExecuteWithResponseRejectArguments(){
        BotResponse response = command.executeWithResponse(userId, new String[]{"month"});

        Assertions.assertEquals(userId, response.userId());
        Assertions.assertEquals(
                "❌ Команда /stats вызывается без параметров",
                response.text()
        );
        Assertions.assertTrue(response.keyboard().isEmpty());
        Assertions.assertFalse(statsFsmService.hasActiveSession(userId));
    }

    /**
     * Тестируем отклонение при передаче нескольких аргументов
     * и соответственно отсутствие сессии
     */
    @Test
    void testExecuteWithResponseRejectMultiplyArguments(){
        BotResponse response = command.executeWithResponse(userId, new String[]{"week", "month"});
        Assertions.assertEquals(
                "❌ Команда /stats вызывается без параметров",
                response.text()
        );
        Assertions.assertFalse(statsFsmService.hasActiveSession(userId));
    }

    /**
     * Тестируем повторный вызов команды для пользователя,
     * что сессия не прерывается
     */
    @Test
    void testRepeatedExecute(){
        command.executeWithResponse(userId, new String[0]);
        command.executeWithResponse(userId, new String[0]);

        Assertions.assertTrue(statsFsmService.hasActiveSession(userId));
    }

}