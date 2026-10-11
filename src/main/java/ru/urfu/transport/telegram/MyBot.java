package ru.urfu.transport.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import java.util.List;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import ru.urfu.service.BotResponse;
import ru.urfu.service.CommandDispatcher;

/**
 * Класс, реализующий Telegram-бота.
 */
public class MyBot implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;
    private final CommandDispatcher dispatcher;

    /**
     * Инициализирует бота с заданным токеном.
     *
     * @param botToken токен для доступа к Telegram API
     * @param dispatcher диспетчер для обработки команд
     */
    public MyBot(String botToken, CommandDispatcher dispatcher) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.dispatcher = dispatcher;
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String chatId = String.valueOf(update.getMessage().getChatId());
                String text = update.getMessage().getText();

                // формируем userId с префиксом платформы (пример "tg:1234")
                String userId = "tg:" + chatId;

                BotResponse response = this.dispatcher.handle(userId, text);

                SendMessage sendMessage = new SendMessage(chatId, response.text());

                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}