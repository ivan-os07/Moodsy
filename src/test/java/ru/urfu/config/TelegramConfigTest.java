package ru.urfu.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelegramConfigTest {

    @Test
    void getBotToken_withValidToken() {
        TelegramConfig config = new TelegramConfig(_ -> "valid_secret_token");

        assertEquals("valid_secret_token", config.getBotToken());
    }

    @Test
    void getBotToken_withInvalidToken() {
        assertThrows(IllegalStateException.class, () -> new TelegramConfig(_ -> null));
    }

    @Test
    void getBotToken_withBlankToken() {
        assertThrows(IllegalStateException.class, () -> new TelegramConfig(_ -> "   "));
    }

}