package ru.urfu.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import java.util.UUID;

/**
 * Тестирование команды /test
 */
class StartCommandTest {

    /**
     * Команда /start
     */
    private StartCommand command;

    /**
     * При инициализации создаем новую команду старт перед всеми тестами
     */
    @BeforeEach
    void setUp() {
        command = new StartCommand();
    }

    /**
     * Тестируем валидность возврата команды
     */
    @Test
    void getCommandKey_returnsCorrectKey() {
        String result = command.getCommandKey();
        Assertions.assertEquals("/start", result);
    }

    /**
     * Тестируем валидность возврата описания команды
     */
    @Test
    void getDescription_returnsNonEmpty() {
        String result = command.getDescription();
        Assertions.assertEquals("Начать работу", result);
    }

    /**
     * Тестируем, что команда возвращает корректное приветственное сообщение.
     */
    @Test
    void execute_returnsWelcomeMessage() {
        String result = command.execute(UUID.fromString("00000000-0000-0000-0000-000000000123"), new String[]{});
        Assertions.assertEquals("Добро пожаловать!", result);
    }
}