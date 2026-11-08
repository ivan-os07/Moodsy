package ru.urfu.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Тестируем работу диспетчера команд.
 */
class CommandDispatcherTest {

    /**
     * Диспетчер
     */
    private CommandDispatcher dispatcher;

    /**
     * Фейковая команда
     */
    private FakeCommand fakeCommand;

    /**
     * Идентификатор пользователя
     */
    private static final String USER_ID = "tg:123";

    /**
     * Инициализация окружения перед каждым тестом.
     * Создаем хранилище с одной тестовой командой (/test), используя заглушку FakeCommand,
     * и диспетчер, работающий с этим хранилищем.
     */
    @BeforeEach
    void setup() {
        CommandRegistry registry = new CommandRegistry();
        fakeCommand = new FakeCommand("/test", "test description", "answer test");
        registry.register(fakeCommand);
        dispatcher = new CommandDispatcher(registry);
    }

    /**
     * Тестирование обработки известной(зарегистрированной) команды.
     * Убеждаемся в валидности ответа и за счет FakeCommand
     * проверяем количество вызовов и id пользователя
     */
    @Test
    void handle_knownCommandTest() {
        BotResponse response = dispatcher.handle(USER_ID, "/test");

        Assertions.assertEquals(USER_ID, response.userId());
        Assertions.assertEquals("answer test", response.text());
        Assertions.assertEquals(USER_ID, fakeCommand.getLastUSER_ID());
        Assertions.assertEquals(1, fakeCommand.getCallCount());
    }

    /**
     * Тестирование обработки неизвестной(незарегистрированной) команды.
     */
    @Test
    void handle_unknownCommandTest() {
        BotResponse response = dispatcher.handle(USER_ID, "/unknown");

        Assertions.assertEquals(USER_ID, response.userId());
        Assertions.assertEquals("Неизвестная команда. Введите /help для справки.", response.text());
        Assertions.assertNull(fakeCommand.getLastUSER_ID());
        Assertions.assertEquals(0, fakeCommand.getCallCount());

    }

    /**
     * Тестирование обработки ошибки при регистрации пустой команды
     */
    @Test
    void handle_blankCommandTest() {
        BotResponse response = dispatcher.handle(USER_ID, "");

        Assertions.assertEquals(USER_ID, response.userId());
        Assertions.assertEquals("Сообщение не может быть пустым!", response.text());
        Assertions.assertNull(fakeCommand.getLastUSER_ID());
        Assertions.assertEquals(0, fakeCommand.getCallCount());

    }

    /**
     * Тестирование обработки команды с несколькими аргументами
     */
    @Test
    void handle_commandWithArgumentsTest() {
        dispatcher.handle(USER_ID, "/test arg1 arg2 arg3");
        Assertions.assertArrayEquals(new String[]{"arg1", "arg2", "arg3"}, fakeCommand.getLastArgs());
    }
}