package ru.urfu.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.application.BotResponse;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.fsm.checkin.CheckinFsmService;
import ru.urfu.service.fsm.checkin.InMemoryCheckinSessionStore;
import ru.urfu.service.fsm.stats.StatsFsmService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Тестируем работу диспетчера команд.
 */
class CommandDispatcherTest {

    /**
     * Идентификатор пользователя, от имени которого вызываются команды.
     */
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000123");

    /**
     * Диспетчер
     */
    private CommandDispatcher dispatcher;

    /**
     * Хранилище команд, из которого диспетчер берёт команду по ключу.
     */
    private CommandRegistry registry;

    /**
     * Сервис диалога чекина, на который диспетчер передаёт ввод.
     */
    private CheckinFsmService checkinFsmService;
    /**
     * Сервис диалога stats, на который диспетчер передает ввод.
     */
    private StatsFsmService statsFsmService;
    /**
     * Фейковая команда
     */
    private FakeCommand fakeCommand;

    /**
     * Инициализация окружения перед каждым тестом.
     * Создаем хранилище с одной тестовой командой (/test), используя заглушку FakeCommand,
     * и диспетчер, работающий с этим хранилищем.
     */
    @BeforeEach
    void setup() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC);
        InMemoryMoodRepository repository = new InMemoryMoodRepository();
        checkinFsmService = new CheckinFsmService(new InMemoryCheckinSessionStore(),
                repository,
                clock);
        statsFsmService = new StatsFsmService(repository, new MoodAnalyticsService(), clock);
        registry = new CommandRegistry();
        fakeCommand = new FakeCommand("/test", "test description", "answer test");
        registry.register(fakeCommand);
        dispatcher = new CommandDispatcher(registry, checkinFsmService, statsFsmService);
    }

    /**
     * Тестирование обработки известной(зарегистрированной) команды.
     * Убеждаемся в валидности ответа и за счет FakeCommand
     * проверяем количество вызовов и id пользователя
     */
    @Test
    void testHandle_knownCommand() {
        BotResponse response = dispatcher.handle(USER_ID, "/test");

        Assertions.assertEquals(USER_ID, response.userId());
        Assertions.assertEquals("answer test", response.text());
        Assertions.assertEquals(USER_ID, fakeCommand.getLastUserId());
        Assertions.assertEquals(1, fakeCommand.getCallCount());
    }

    /**
     * Тестирование обработки неизвестной(незарегистрированной) команды.
     */
    @Test
    void testHandle_unknownCommand() {
        BotResponse response = dispatcher.handle(USER_ID, "/unknown");

        Assertions.assertEquals(USER_ID, response.userId());
        Assertions.assertEquals("Неизвестная команда. Введите /help для справки.", response.text());
        Assertions.assertNull(fakeCommand.getLastUserId());
        Assertions.assertEquals(0, fakeCommand.getCallCount());

    }

    /**
     * Тестирование обработки ошибки при регистрации пустой команды
     */
    @Test
    void testHandle_blankCommand() {
        BotResponse response = dispatcher.handle(USER_ID, "");

        Assertions.assertEquals(USER_ID, response.userId());
        Assertions.assertEquals("Сообщение не может быть пустым!", response.text());
        Assertions.assertNull(fakeCommand.getLastUserId());
        Assertions.assertEquals(0, fakeCommand.getCallCount());

    }

    /**
     * Тестирование обработки команды с несколькими аргументами
     */
    @Test
    void testHandle_commandWithArguments() {
        dispatcher.handle(USER_ID, "/test arg1 arg2 arg3");
        Assertions.assertArrayEquals(new String[]{"arg1", "arg2", "arg3"}, fakeCommand.getLastArgs());
    }

    /**
     * Проверяет, что при открытой сессии обычный текст передаётся в FSM чекина.
     */
    @Test
    void testHandle_textWithActiveSession_routesToCheckinFsm() {
        registry.register(new CheckinCommand(checkinFsmService));

        dispatcher.handle(USER_ID, "/checkin");
        BotResponse response = dispatcher.handle(USER_ID, "7");

        Assertions.assertEquals("Выберете сферу из предложенных:\nДела, Общение, Здоровье, Отдых\n", response.text());
        Assertions.assertTrue(checkinFsmService.hasActiveSession(USER_ID));
    }

    /**
     * Проверяет, что без открытой сессии обычный текст считается неизвестной командой.
     */
    @Test
    void testHandle_plainTextWithoutSession_returnsUnknownCommand() {
        BotResponse response = dispatcher.handle(USER_ID, "7");

        Assertions.assertEquals("Неизвестная команда. Введите /help для справки.", response.text());
    }

    /**
     * Тестируем, что при открытой сессии (для статистики)
     * обычный текст передается в FSM stats
     */
    @Test
    void testHandle_textWithActiveStatsSession_routesToStatsFsm() {
        registry.register(new StatsCommand(statsFsmService));
        dispatcher.handle(USER_ID, "/stats");
        BotResponse response = dispatcher.handle(USER_ID, "week");
        Assertions.assertEquals("Записей нет, используйте /checkin", response.text()
        );
        Assertions.assertTrue(response.keyboard().isEmpty());
        Assertions.assertFalse(statsFsmService.hasActiveSession(USER_ID));
    }

    /**
     * Тестируем независимость сессий статистики разных пользователей
     */
    @Test
    void testHandle_statsSessionsAreIndependent() {
        registry.register(new StatsCommand(statsFsmService));
        UUID anotherUserId = UUID.randomUUID();

        dispatcher.handle(USER_ID, "/stats");
        dispatcher.handle(anotherUserId, "/stats");

        dispatcher.handle(USER_ID, "week");

        Assertions.assertFalse(statsFsmService.hasActiveSession(USER_ID));
        Assertions.assertTrue(statsFsmService.hasActiveSession(anotherUserId));
    }
}