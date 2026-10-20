package ru.urfu;

import ru.urfu.config.TelegramConfig;
import ru.urfu.transport.telegram.TelegramBot;
import ru.urfu.commands.*;
import ru.urfu.repository.*;

import java.time.Clock;


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

        String botToken = tgConfig.getBotToken();

        // Инициализация инфраструктурных компонентов и слоев данных
        MoodRepository moodRepository = new InMemoryMoodRepository();
        CommandDispatcher dispatcher = new CommandDispatcher();

        // Регистрируем команды в диспетчере
        dispatcher.register(new StartCommand());
        dispatcher.register(new HelpCommand(dispatcher));
        dispatcher.register(new CheckinCommand(moodRepository, Clock.systemDefaultZone()));
        dispatcher.register(new HistoryCommand(moodRepository));


        // Инициализация и запуск транспортного слоя (Telegram-бота)
        // Использование try-with-resources гарантирует корректное закрытие ресурсов бота

        try (TelegramBot bot = new TelegramBot(botToken, dispatcher)
             // в будущем можно подключить этот же диспетчер к другим платформам
        ) {
            bot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}