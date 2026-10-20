package ru.urfu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование команды /test
 */
class StartCommandTest {

    private StartCommand command;

    /**
     * При инициализации создаем новую команду старт перед всеми тестами
     */
    @BeforeEach
    void setUp() {
        command = new StartCommand();
    }

    /**
     * Проверяем валидность возврата команды
     */
    @Test
    void getCommandKey_returnsCorrectKey() {
        String result = command.getCommandKey();
        assertEquals("/start", result);
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
     * Проверяет, что команда возвращает корректное приветственное сообщение.
     */
    @Test
    void execute_returnsWelcomeMessage() {
        String result = command.execute("tg:123", new String[]{});

        assertNotNull(result);
        assertFalse(result.isBlank());
    }

}