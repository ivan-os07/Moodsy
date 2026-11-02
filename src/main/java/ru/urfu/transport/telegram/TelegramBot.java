package ru.urfu.transport.telegram;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageAutoDeleteTimerChanged;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import java.util.List;
import java.util.UUID;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import ru.urfu.commands.BotResponse;
import ru.urfu.commands.CommandDispatcher;
import ru.urfu.model.PlatformIdentity;
import ru.urfu.model.PlatformType;
import ru.urfu.service.UserService;

/**
 * Класс, реализующий Telegram-бота.
 */
public class TelegramBot implements LongPollingUpdateConsumer {

    /**
     * Клиент телеграмма
     */
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
     * Сервис поиска, создания и связывания пользователей с аккаунтами платформ
     */
    private final UserService userService;

    /**
     * Инициализирует бота с заданным токеном.
     *
     * @param botToken   токен для доступа к Telegram API
     * @param dispatcher диспетчер для обработки команд
     */
    public TelegramBot(String botToken, CommandDispatcher dispatcher, UserService userService) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.botToken = botToken;
        this.dispatcher = dispatcher;
        this.userService = userService;
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (!update.hasMessage() || !update.getMessage().hasText()) {
                continue;
            }
            Message message = update.getMessage();
            if (message.getFrom() == null){
                continue;
            }


            String chatId = String.valueOf(update.getMessage().getChatId());
            String platformUserId = String.valueOf(message.getFrom().getId());
            PlatformIdentity identity = new PlatformIdentity(PlatformType.TELEGRAM, platformUserId);
            UUID userId = userService.getOrCreateUserId(identity);

            BotResponse response = this.dispatcher.handle(userId, message.getText());

            SendMessage sendMessage = new SendMessage(chatId, response.text());

            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
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