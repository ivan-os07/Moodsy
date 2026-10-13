package ru.urfu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StartCommandTest {

    private StartCommand command;

    @BeforeEach
    void setUp() {
        command = new StartCommand();
    }

    @Test
    void getCommandKey_returnsCorrectKey() {
        String result = command.getCommandKey();
        assertEquals("/start", result);
    }

    @Test
    void getDescription_returnsNonEmpty() {
        String result = command.getDescription();
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void execute_returnsWelcomeMessage() {
        String result = command.execute("tg:123", new String[]{});
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

}