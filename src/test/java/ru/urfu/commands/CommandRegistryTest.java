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
    void find_registeredCommand_returnsItTest() {
        Assertions.assertSame(fakeCommand, registry.find("/test").orElseThrow());
    }

    /**
     * Для незарегистрированного ключа возвращается пустой Optional.
     */
    @Test
    void find_unknownKey_returnsEmptyTest() {
        Assertions.assertTrue(registry.find("/unknown").isEmpty());
    }

    /**
     * Тк при инициализации добавлена одна команда, размер коллекции должен быть 1.
     */
    @Test
    void getCommands_returnsAllRegisteredCommandsTest() {
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
    void register_addNewCommandTest() {
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
    void register_sameKey_replacesCommandTest() {
        FakeCommand newCommand = new FakeCommand("/test", "new test", "i am new test");

        registry.register(newCommand);

        Assertions.assertEquals(1, registry.getCommands().size());
        // Сравниваем по ссылке осознанно: хранилище должно вернуть
        // именно те объекты, которые мы зарегистрировали.
        Assertions.assertSame(newCommand, registry.find("/test").orElseThrow());
    }

    /**
     * Проверяет, что команды возвращаются в порядке регистрации:
     * /help выводит список именно в этом порядке, а не по алфавиту.
     * Ключи /b и /a выбраны так, чтобы порядок регистрации не совпадал с алфавитным.
     */
    @Test
    void getCommands_keepsRegistrationOrderTest() {
        FakeCommand second = new FakeCommand("/b", "b", "b");
        registry.register(second);

        List<String> keys = registry
                .getCommands()
                .stream()
                .map(BotCommand::getCommandKey)
                .toList();

        // Ключи, а не объекты: FakeCommand не переопределяет equals(),
        // поэтому assertEquals на списке команд сравнивал бы ссылки.
        Assertions.assertEquals(List.of("/test", "/b", "/a"), keys);
        Assertions.assertEquals(List.of("/test", "/b", "/a"), keys);
    }

    /**
     * Возвращаемую коллекцию нельзя изменить снаружи
     * попытка очистки бросает исключение и не портит данные хранилища.
     */
    @Test
    void getCommands_isUnmodifiableTest() {
        Collection<BotCommand> commands = registry.getCommands();

        Assertions.assertThrows(UnsupportedOperationException.class, commands::clear);
        Assertions.assertEquals(1, registry.getCommands().size());
    }
}
