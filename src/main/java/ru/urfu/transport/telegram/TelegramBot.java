package ru.urfu.transport.telegram;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import java.util.List;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import ru.urfu.commands.BotResponse;
import ru.urfu.commands.CommandDispatcher;

/**
 * Класс, реализующий Telegram-бота.
 */
public class TelegramBot implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;

    /**
     * Диспетчер для маршрутизации команд.
     */
    private final CommandDispatcher dispatcher;

    /**
     * Токен для доступа к Telegram API.
     */
    private final String botToken;

    /**
     * Инициализирует бота с заданным токеном.
     *
     * @param botToken   токен для доступа к Telegram API
     * @param dispatcher диспетчер для обработки команд
     */
    public TelegramBot(String botToken, CommandDispatcher dispatcher) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.botToken = botToken;
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

    /**
     * Запускает бота и начинает принимать обновления от Telegram.
     *
     * @throws Exception если произошла ошибка при запуске или регистрации бота
     */
    public void start() throws Exception {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(this.botToken, this);
            System.out.println("Бот успешно запущен и слушает обновления...");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}