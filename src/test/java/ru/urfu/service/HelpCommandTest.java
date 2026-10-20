package ru.urfu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование команды /help
 */
class HelpCommandTest {

    private CommandDispatcher dispatcher;
    private HelpCommand command;

    /**
     * Инициализация диспетчера и команды /help перед всеми тестами
     */
    @BeforeEach
    void startup() {
        dispatcher = new CommandDispatcher();
        command = new HelpCommand(dispatcher);
    }

    /**
     * Проверяем валидность возврата команды
     */
    @Test
    void getCommandKey() {
        String result = command.getCommandKey();
        assertEquals("/help", result);
    }

    /**
     * Проверяем валидность возврата описания команды
     */
    @Test
    void getDescription_returnsNonEmpty() {
        String result = command.getDescription();
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    /**
     * Тестируем вывод команд при регистрации 2 новых команд
     */
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