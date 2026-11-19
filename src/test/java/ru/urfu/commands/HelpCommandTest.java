package ru.urfu.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Тестирование команды /help
 */
class HelpCommandTest {

    /**
     * Хранилище команд, из которого /help берет список
     */
    private CommandRegistry registry;

    /**
     * Команда /help
     */
    private HelpCommand command;

    /**
     * Инициализация хранилища и команды /help перед каждым тестом
     */
    @BeforeEach
    void startup() {
        registry = new CommandRegistry();
        command = new HelpCommand(registry);
    }

    /**
     * Тестируем валидность возврата ключа команды
     */
    @Test
    void getCommandKeyTest() {
        Assertions.assertEquals("/help", command.getCommandKey());
    }

    /**
     * Тестируем валидность возврата описания команды
     */
    @Test
    void getValidDescriptionTest() {
        Assertions.assertEquals("Получить помощь и список команд", command.getDescription());
    }

    /**
     * Тестируем вывод команд при регистрации 2 новых команд:
     * приветствие, затем команды в порядке регистрации.
     */
    @Test
    void getListsAllRegisteredCommandsTest() {
        registry.register(new FakeCommand("/cmd1", "cmd1 description", "ans1"));
        registry.register(new FakeCommand("/cmd2", "cmd2 description", "ans2"));

        String result = command.execute("tg123", new String[]{});

        String expected = """
                Привет! Я бот Moodsy
                /cmd1 - cmd1 description
                /cmd2 - cmd2 description
                """;
        Assertions.assertEquals(expected, result);
    }

    /**
     * Если сама команда /help зарегистрирована в хранилище, она тоже попадает в список.
     */
    @Test
    void getIncludesItselfWhenRegisteredTest() {
        registry.register(command);

        String result = command.execute("tg123", new String[]{});

        String expected = """
                Привет! Я бот Moodsy
                /help - Получить помощь и список команд
                """;
        Assertions.assertEquals(expected, result);
    }

    /**
     * Если других команд нет, выводится только приветствие.
     */
    @Test
    void getEmptyRegistry_returnsOnlyGreetingTest() {
        String result = command.execute("tg123", new String[]{});

        Assertions.assertEquals("Привет! Я бот Moodsy\n", result);
    }
}