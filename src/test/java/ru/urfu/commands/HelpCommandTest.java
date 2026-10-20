package ru.urfu.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

/**
 * Тестирование команды /help
 */
class HelpCommandTest {

    /**
     * Диспетчер
     */
    private CommandDispatcher dispatcher;

    /**
     * Команда /help
     */
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
     * Тестируем валидность возврата команды
     */
    @Test
    void getCommandKey() {
        String result = command.getCommandKey();
        Assertions.assertEquals("/help", result);
    }

    /**
     * Тестируем валидность возврата описания команды
     */
    @Test
    void getDescription_returnsNonEmpty() {
        String result = command.getDescription();
        Assertions.assertEquals("Получить помощь и список команд", result);
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

        Assertions.assertTrue(result.contains("/cmd1 - cmd1 description"));
        Assertions.assertTrue(result.contains("/cmd2 - cmd2 description"));
    }
}