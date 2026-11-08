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
    void handle_knownCommand() {
        BotResponse response = dispatcher.handle("tg:123", "/test");

        Assertions.assertEquals("tg:123", response.userId());
        Assertions.assertEquals("answer test", response.text());
        Assertions.assertEquals("tg:123", fakeCommand.getLastUserId());
        Assertions.assertEquals(1, fakeCommand.getCallCount());
    }

    /**
     * Тестирование обработки неизвестной(незарегистрированной) команды.
     */
    @Test
    void handle_unknownCommand() {
        BotResponse response = dispatcher.handle("tg:123", "/unknown");

        Assertions.assertEquals("tg:123", response.userId());
        Assertions.assertEquals("Неизвестная команда. Введите /help для справки.", response.text());
        Assertions.assertNull(fakeCommand.getLastUserId());
        Assertions.assertEquals(0, fakeCommand.getCallCount());

    }

    /**
     * Тестирование обработки ошибки при регистрации пустой команды
     */
    @Test
    void handle_blankCommand() {
        BotResponse response = dispatcher.handle("tg:123", "");

        Assertions.assertEquals("tg:123", response.userId());
        Assertions.assertEquals("Сообщение не может быть пустым!", response.text());
        Assertions.assertNull(fakeCommand.getLastUserId());
        Assertions.assertEquals(0, fakeCommand.getCallCount());

    }

    /**
     * Тестирование обработки команды с несколькими аргументами
     */
    @Test
    void handle_commandWithArguments() {
        dispatcher.handle("tg:123", "/test arg1 arg2 arg3");
        Assertions.assertArrayEquals(new String[]{"arg1", "arg2", "arg3"}, fakeCommand.getLastArgs());
    }
}