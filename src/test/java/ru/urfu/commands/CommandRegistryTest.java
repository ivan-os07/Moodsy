package ru.urfu.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Тестируем хранилище команд.
 */
class CommandRegistryTest {

    /**
     * Хранилище команд
     */
    private CommandRegistry registry;

    /**
     * Фейковая команда, зарегистрированная перед каждым тестом
     */
    private FakeCommand fakeCommand;

    /**
     * Создаем пустое хранилище и регистрируем в нем одну команду (/test).
     */
    @BeforeEach
    void setup() {
        registry = new CommandRegistry();
        fakeCommand = new FakeCommand("/test", "test description", "answer test");
        registry.register(fakeCommand);
    }

    /**
     * Зарегистрированную команду можно найти по ключу.
     */
    @Test
    void find_registeredCommand_returnsIt() {
        Assertions.assertSame(fakeCommand, registry.find("/test").orElseThrow());
    }

    /**
     * Для незарегистрированного ключа возвращается пустой Optional.
     */
    @Test
    void find_unknownKey_returnsEmpty() {
        Assertions.assertTrue(registry.find("/unknown").isEmpty());
    }

    /**
     * Тк при инициализации добавлена одна команда, размер коллекции должен быть 1.
     */
    @Test
    void getCommands_returnsAllRegisteredCommands() {
        List<BotCommand> commands = new ArrayList<>(registry.getCommands());

        Assertions.assertEquals(1, commands.size());
        // Сравниваем по ссылке осознанно: хранилище должно вернуть
        // именно тот объект, который мы зарегистрировали.
        Assertions.assertSame(fakeCommand, commands.getFirst());
    }

    /**
     * После регистрации новой команды в коллекции есть обе команды.
     */
    @Test
    void register_addNewCommand() {
        FakeCommand secondCommand = new FakeCommand("/test2", "test2 description", "answer test2");

        registry.register(secondCommand);

        Assertions.assertEquals(2, registry.getCommands().size());

        // Сравниваем по ссылке осознанно: хранилище должно вернуть
        // именно те объекты, которые мы зарегистрировали.
        Assertions.assertSame(secondCommand, registry.find("/test2").orElseThrow());
    }

    /**
     * Повторная регистрация с тем же ключом заменяет команду, а не добавляет вторую.
     */
    @Test
    void register_sameKey_replacesCommand() {
        FakeCommand newCommand = new FakeCommand("/test", "new test", "i am new test");

        registry.register(newCommand);

        Assertions.assertEquals(1, registry.getCommands().size());
        // Сравниваем по ссылке осознанно: хранилище должно вернуть
        // именно те объекты, которые мы зарегистрировали.
        Assertions.assertSame(newCommand, registry.find("/test").orElseThrow());
    }

    /**
     * Команды возвращаются в порядке регистрации (для вывода /help).
     */
    @Test
    void getCommands_keepsRegistrationOrder() {
        FakeCommand second = new FakeCommand("/b", "b", "b");
        FakeCommand third = new FakeCommand("/a", "a", "a");
        registry.register(second);
        registry.register(third);

        List<BotCommand> commands = new ArrayList<>(registry.getCommands());

        Assertions.assertEquals(List.of(fakeCommand, second, third), commands);
    }

    /**
     * Возвращаемую коллекцию нельзя изменить снаружи.
     */
    @Test
    void getCommands_isUnmodifiable() {
        Collection<BotCommand> commands = registry.getCommands();

        Assertions.assertThrows(UnsupportedOperationException.class, commands::clear);
    }
}