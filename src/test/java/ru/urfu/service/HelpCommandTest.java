package ru.urfu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelpCommandTest {

    private CommandDispatcher dispatcher;
    private HelpCommand command;

    @BeforeEach
    void startup() {
        dispatcher = new CommandDispatcher();
        command = new HelpCommand(dispatcher);
    }

    @Test
    void getCommandKey() {
        String result = command.getCommandKey();
        assertEquals("/help", result);
    }

    @Test
    void getDescription_returnsNonEmpty() {
        String result = command.getDescription();
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void execute() {
        dispatcher.register(new FakeCommand("/cmd1", "cmd1 description", "ans1"));
        dispatcher.register(new FakeCommand("/cmd2", "cmd2 description", "ans2"));

        HelpCommand helpCommand = new HelpCommand(dispatcher);
        dispatcher.register(helpCommand);

        String result = helpCommand.execute("tg123", new String[]{});

        assertTrue(result.contains("/cmd1 - cmd1 description"));
        assertTrue(result.contains("/cmd2 - cmd2 description"));
    }
}