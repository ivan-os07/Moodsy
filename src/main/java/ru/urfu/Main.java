package ru.urfu;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * Точка входа telegram-бота.
 * берет токен из переменной окружения BOT_TOKEN,
 * запускает приложение и регистрирует в нем.
 * Если токен не задан - выводит ошибку и завершает работу.
 *
 */
public class Main {
    static void main() {
        String botToken = System.getenv("BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            System.err.println("Ошибка: переменная окружения BOT_TOKEN не задана!");
            return;
        }

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new MyBot(botToken));
            System.out.println("Бот успешно запущен и слушает обновления...");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}