package ru.urfu;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

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