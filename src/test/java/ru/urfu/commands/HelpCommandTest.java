package ru.urfu.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.fsm.checkin.CheckinFsmService;
import ru.urfu.service.fsm.checkin.InMemoryCheckinSessionStore;

import org.junit.jupiter.api.Assertions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Тестирование команды /help
 */
class HelpCommandTest {

    /**
     * Идентификатор пользователя для вызовов команды в тестах.
     */
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000123");

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
    void testGetCommandKey() {
        Assertions.assertEquals("/help", command.getCommandKey());
    }

    /**
     * Тестируем валидность возврата описания команды
     */
    @Test
    void testGetValidDescription() {
        Assertions.assertEquals("Получить помощь и список команд", command.getDescription());
    }

    /**
     * Тестируем вывод команд при регистрации 2 новых команд:
     * приветствие, затем команды в порядке регистрации.
     */
    @Test
    void testGetListsAllRegisteredCommands() {
        registry.register(new FakeCommand("/cmd1", "cmd1 description", "ans1"));
        registry.register(new FakeCommand("/cmd2", "cmd2 description", "ans2"));

        String result = command.execute(USER_ID, new String[]{});

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
    void testGetIncludesItselfWhenRegistered() {
        registry.register(command);

        String result = command.execute(USER_ID, new String[]{});

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
    void testGetEmptyRegistry_returnsOnlyGreeting() {
        String result = command.execute(USER_ID, new String[]{});

        Assertions.assertEquals("Привет! Я бот Moodsy\n", result);
    }
}