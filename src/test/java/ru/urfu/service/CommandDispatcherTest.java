package ru.urfu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CommandDispatcherTest {

    private CommandDispatcher dispatcher;
    private FakeCommand fakeCommand;

    @BeforeEach
    void setup() {
        dispatcher = new CommandDispatcher();
        fakeCommand = new FakeCommand("/test", "test description", "answer test");
        dispatcher.register(fakeCommand);
    }

    @Test
    void handle_knownCommand() {
        BotResponse response = dispatcher.handle("tg:123", "/test");

        assertEquals("tg:123", response.userId());
        assertEquals("answer test", response.text());
        assertEquals("tg:123", fakeCommand.getLastUserId());
        assertEquals(1, fakeCommand.getCallCount());
    }

    @Test
    void handle_unknownCommand() {
        BotResponse response = dispatcher.handle("tg:123", "/unknown");

        assertEquals("tg:123", response.userId());
        assertEquals("Неизвестная команда. Введите /help для справки.", response.text());
        assertNull(fakeCommand.getLastUserId());
        assertEquals(0, fakeCommand.getCallCount());

    }

    @Test
    void handle_blankCommand() {
        BotResponse response = dispatcher.handle("tg:123", "");

        assertEquals("tg:123", response.userId());
        assertEquals("Сообщение не может быть пустым!", response.text());
        assertNull(fakeCommand.getLastUserId());
        assertEquals(0, fakeCommand.getCallCount());

    }

    @Test
    void handle_commandWithArguments() {
        dispatcher.handle("tg:123", "/test arg1 arg2 arg3");
        assertArrayEquals(new String[]{"arg1", "arg2", "arg3"}, fakeCommand.getLastArgs());
    }

    @Test
    void getCommands_returnsAllRegisteredCommands() {
        Collection<BotCommand> commands = dispatcher.getCommands();

        assertEquals(1, commands.size());
        assertTrue(commands.contains(fakeCommand));
    }

    @Test
    void getCommands_addNewCommand() {
        FakeCommand secondCommand = new FakeCommand("/test2", "test2 description", "answer test2");
        dispatcher.register(secondCommand);

        Collection<BotCommand> commands = dispatcher.getCommands();

        assertEquals(2, commands.size());
        assertTrue(commands.contains(fakeCommand));
        assertTrue(commands.contains(secondCommand));
    }

    @Test
    void register_addNewCommand() {
        FakeCommand newCommand = new FakeCommand("/ping", "check conn", "pong");

        dispatcher.register(newCommand);

        BotResponse response = dispatcher.handle("tg:123", "/ping");

        assertEquals("pong", response.text());
        assertEquals("tg:123", response.userId());
    }

    @Test
    void register_sameKeyRegister() {
        FakeCommand newCommand = new FakeCommand("/test", "new test", "i am new test");

        dispatcher.register(newCommand);

        BotResponse response = dispatcher.handle("tg:123", "/test");

        assertEquals("i am new test", response.text());
    }
}