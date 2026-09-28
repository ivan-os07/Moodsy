package ru.urfu;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import java.util.List;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

/**
 * Класс, реализующий Telegram-бота.
 */
public class MyBot implements LongPollingUpdateConsumer {

    /**
     * Клиент для выполнения запросов к Telegram API.
     */
    private final TelegramClient telegramClient;

    /**
     * Инициализирует бота с заданным токеном.
     *
     * @param botToken токен для доступа к Telegram API
     */
    public MyBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    /**
     * Создает строку-ответ по заданному сообщению.
     *
     * @param text строка по которой формируем ответ
     * @return текст ответа бота, умолчанию просто возвращает text
     */
    public String buildResponse(String text) {
        return switch (text) {
            case "/start" -> "Добро пожаловать!";
            case "/help" -> """
                    Привет! Я бот Moodsy
                    /start - начать работу
                    /help - показать справку
                    """;
            default -> "Вы ввели " + text;
        };
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String chatId = String.valueOf(update.getMessage().getChatId());
                String text = update.getMessage().getText();

                SendMessage sendMessage;

                sendMessage = new SendMessage(chatId, buildResponse(text));

                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}