package ru.urfu.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.fsm.CheckinFsmService;
import ru.urfu.service.fsm.InMemoryCheckinSessionStore;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;

/**
 * Тестируем работу диспетчера команд.
 */
class CommandDispatcherTest {

    /**
     * Диспетчер
     */
    private CommandDispatcher dispatcher;

    /**
     * Сервис диалога чекина, на который диспетчер передаёт ввод.
     */
    private CheckinFsmService checkinFsmService;

    /**
     * Фейковая команда
     */
    private FakeCommand fakeCommand;

    /**
     * Инициализация окружения перед каждым тестом.
     * Создаем новый диспетчер и регистрируем одну тестовую команду (/test),
     * используя заглушку FakeCommand
     */
    @BeforeEach
    void setup() {
        checkinFsmService = new CheckinFsmService(
                new InMemoryCheckinSessionStore(),
                new InMemoryMoodRepository(),
                Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC));
        dispatcher = new CommandDispatcher(checkinFsmService);
        fakeCommand = new FakeCommand("/test", "test description", "answer test");
        dispatcher.register(fakeCommand);
    }

    /**
     * Тестирование обработки известной(зарегистрированной) команды.
     * Убеждаемся в валидности ответа и за счет FakeCommand
     * проверяем количество вызовов и id пользователя
     */
    @Test
    void handle_knownCommand() {
        BotResponse response = dispatcher.handle(UUID.fromString("00000000-0000-0000-0000-000000000123"), "/test");

        Assertions.assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000123"), response.userId());
        Assertions.assertEquals("answer test", response.text());
        Assertions.assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000123"), fakeCommand.getLastUserId());
        Assertions.assertEquals(1, fakeCommand.getCallCount());
    }

    /**
     * Тестирование обработки неизвестной(незарегистрированной) команды.
     */
    @Test
    void handle_unknownCommand() {
        BotResponse response = dispatcher.handle(UUID.fromString("00000000-0000-0000-0000-000000000123"), "/unknown");

        Assertions.assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000123"), response.userId());
        Assertions.assertEquals("Неизвестная команда. Введите /help для справки.", response.text());
        Assertions.assertNull(fakeCommand.getLastUserId());
        Assertions.assertEquals(0, fakeCommand.getCallCount());

    }

    /**
     * Тестирование обработки ошибки при регистрации пустой команды
     */
    @Test
    void handle_blankCommand() {
        BotResponse response = dispatcher.handle(UUID.fromString("00000000-0000-0000-0000-000000000123"), "");

        Assertions.assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000123"), response.userId());
        Assertions.assertEquals("Сообщение не может быть пустым!", response.text());
        Assertions.assertNull(fakeCommand.getLastUserId());
        Assertions.assertEquals(0, fakeCommand.getCallCount());

    }

    /**
     * Тестирование обработки команды с несколькими аргументами
     */
    @Test
    void handle_commandWithArguments() {
        dispatcher.handle(UUID.fromString("00000000-0000-0000-0000-000000000123"), "/test arg1 arg2 arg3");
        Assertions.assertArrayEquals(new String[]{"arg1", "arg2", "arg3"}, fakeCommand.getLastArgs());
    }

    /**
     * Тестирование обработки возвращаемого количества команд.
     * Тк мы при инициализации добавили одну команду, то размер коллекции должен быть 1
     */
    @Test
    void getCommands_returnsAllRegisteredCommands() {
        Collection<BotCommand> commands = dispatcher.getCommands();

        Assertions.assertEquals(1, commands.size());
        Assertions.assertTrue(commands.contains(fakeCommand));
    }

    /**
     * Тестирование добавления новой команды.
     * Убеждаемся в корректности обработки диспетчера.
     */
    @Test
    void getCommands_addNewCommand() {
        FakeCommand secondCommand = new FakeCommand("/test2", "test2 description", "answer test2");
        dispatcher.register(secondCommand);

        Collection<BotCommand> commands = dispatcher.getCommands();

        Assertions.assertEquals(2, commands.size());
        Assertions.assertTrue(commands.contains(fakeCommand));
        Assertions.assertTrue(commands.contains(secondCommand));
    }

    /**
     * Тестирование регистрации новой команды.
     * Убеждаемся в корректности обработки диспетчера этой новой команды
     */
    @Test
    void register_addNewCommand() {
        FakeCommand newCommand = new FakeCommand("/ping", "check conn", "pong");

        dispatcher.register(newCommand);

        BotResponse response = dispatcher.handle(UUID.fromString("00000000-0000-0000-0000-000000000123"), "/ping");

        Assertions.assertEquals("pong", response.text());
        Assertions.assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000123"), response.userId());
    }

    /**
     * Тестирование поведения диспетчера при перерегистрации команды.
     * Добавляется команда с ключом который есть в коллекции, но с новым описанием,
     * проверяем валидность после перерегистрации
     */
    @Test
    void register_sameKeyRegister() {
        FakeCommand newCommand = new FakeCommand("/test", "new test", "i am new test");

        dispatcher.register(newCommand);

        BotResponse response = dispatcher.handle(UUID.fromString("00000000-0000-0000-0000-000000000123"), "/test");

        Assertions.assertEquals("i am new test", response.text());
    }

    /**
     * Проверяет, что при открытой сессии обычный текст передаётся в FSM чекина.
     */
    @Test
    void handle_textWithActiveSession_routesToCheckinFsm() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000123");
        dispatcher.register(new CheckinCommand(checkinFsmService));

        dispatcher.handle(userId, "/checkin");
        BotResponse response = dispatcher.handle(userId, "7");

        Assertions.assertEquals("Добавьте заметку или отправьте /skip", response.text());
        Assertions.assertTrue(checkinFsmService.hasActiveSession(userId));
    }

    /**
     * Проверяет, что без открытой сессии обычный текст считается неизвестной командой.
     */
    @Test
    void handle_plainTextWithoutSession_returnsUnknownCommand() {
        BotResponse response = dispatcher.handle(
                UUID.fromString("00000000-0000-0000-0000-000000000123"), "7");

        Assertions.assertEquals("Неизвестная команда. Введите /help для справки.", response.text());
    }


}