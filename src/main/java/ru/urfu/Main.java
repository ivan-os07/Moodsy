package ru.urfu;

import ru.urfu.config.TelegramConfig;
import ru.urfu.service.UserService;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.fsm.checkin.CheckinFsmService;
import ru.urfu.service.fsm.checkin.CheckinSessionStore;
import ru.urfu.service.fsm.checkin.InMemoryCheckinSessionStore;
import ru.urfu.service.fsm.stats.StatsFsmService;
import ru.urfu.transport.telegram.TelegramBot;

import java.time.Clock;
import java.time.ZoneOffset;


/**
 * Точка входа в приложение.
 */
public class Main {
    /**
     * Точка входа в приложение.
     * Запускает инициализацию и конфигурацию компонентов Telegram-бота.
     *
     * @param args аргументы командной строки, передаваемые при запуске приложения
     */
    public static void main(String[] args) {
        // Загрузка конфигурационных данных
        TelegramConfig tgConfig;
        try {
            tgConfig = new TelegramConfig();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return;
        }

        // время мск UTC+3
        BotComposition composition = new BotComposition(Clock.system(ZoneOffset.ofHours(3)));


        // Инициализация и запуск транспортного слоя (Telegram-бота)
        // Использование try-with-resources гарантирует корректное закрытие ресурсов бота
        try (TelegramBot bot = new TelegramBot(
                tgConfig.getBotToken(),
                composition.getDispatcher(),
                composition.getUserService()
        )) {
            bot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}