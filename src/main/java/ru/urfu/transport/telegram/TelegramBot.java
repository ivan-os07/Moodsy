package ru.urfu.transport.telegram;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import java.util.List;
import java.util.Optional;
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
     * Сборка inline-клавиатур
     */
    private final TelegramKeyboardFactory keyboardFactory;

    /**
     * Инициализирует бота с заданным токеном.
     *
     * @param botToken    токен для доступа к Telegram API
     * @param dispatcher  диспетчер для обработки команд
     * @param userService сервис для создания и поиска внутренних пользователей
     */
    public TelegramBot(String botToken, CommandDispatcher dispatcher, UserService userService) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.botToken = botToken;
        this.dispatcher = dispatcher;
        this.userService = userService;
        this.keyboardFactory = new TelegramKeyboardFactory();
    }

    @Override
    public void consume(List<Update> updates) {

        for (Update update : updates) {
            try {
                if (update.hasCallbackQuery()) {
                    handleCallBack(update);
                } else if (update.hasMessage()
                        && update.getMessage().hasText()) {
                    handleMessage(update);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();

            }
        }
    }

    /**
     * Обработка обычного сообщения
     *
     * @param update обновление Telegram с текстовым сообщением
     * @throws TelegramApiException если Telegram API отклоняет отправку
     */

    private void handleMessage(Update update) throws TelegramApiException {
        Message message = update.getMessage();
        if (message.getFrom() == null) {
            return;
        }


        String chatId = String.valueOf(message.getChatId());

        String platformUserId = String.valueOf(message.getFrom().getId());
        PlatformIdentity identity = new PlatformIdentity(PlatformType.TELEGRAM, platformUserId);
        UUID userId = userService.getOrCreateUserId(identity);

        BotResponse response = dispatcher.handle(userId, message.getText());
        SendMessage sendMessage = new SendMessage(chatId, response.text());

        response.keyboard().map(keyboardFactory::createKeyboard)
                .ifPresent(sendMessage::setReplyMarkup);


        telegramClient.execute(sendMessage);
    }

    /**
     * Обрабатывает Callback запрос, передает его в FSM и обновляет исходное сообщение
     *
     * @param update обновление Telegram с callback запросом
     * @throws TelegramApiException если Tg API отклоняет отправку или изменение сообщения
     */
    private void handleCallBack(Update update) throws TelegramApiException {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.getId());
        telegramClient.execute(answer);
        if (callbackQuery.getMessage() == null || callbackQuery.getFrom() == null) {
            return;
        }
        String chatId = String.valueOf(callbackQuery.getMessage().getChatId());
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String platformUserId = String.valueOf(callbackQuery.getFrom().getId());
        PlatformIdentity identity = new PlatformIdentity(PlatformType.TELEGRAM, platformUserId);
        UUID userId = userService.getOrCreateUserId(identity);
        String callbackData = callbackQuery.getData();
        BotResponse response = dispatcher.handle(userId, callbackData);

        Optional<InlineKeyboardMarkup> keyboard =
                response.keyboard().map(keyboardFactory::createKeyboard);

        EditMessageText editMessage = new EditMessageText(
                chatId,
                messageId,
                null,
                response.text(),
                null,
                null,
                keyboard.orElseGet(keyboardFactory::createEmptyKeyboard),
                null,
                null,
                null,
                null
        );

        telegramClient.execute(editMessage);

    }

    /**
     * Запускает бота и начинает принимать обновления от Telegram.
     *
     * @throws Exception если произошла ошибка при запуске или регистрации бота
     */
    public void start() throws Exception {
        try (TelegramBotsLongPollingApplication botsApplication =
                     new TelegramBotsLongPollingApplication()) {

            botsApplication.registerBot(this.botToken, this);
            System.out.println("Бот успешно запущен и слушает обновления...");

            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}