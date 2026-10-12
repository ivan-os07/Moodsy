package ru.urfu;

import ru.urfu.config.TelegramConfig;
import ru.urfu.service.CommandDispatcher;
import ru.urfu.service.HelpCommand;
import ru.urfu.service.StartCommand;
import ru.urfu.transport.telegram.TelegramBot;


public class Main {
    static void main() {
        TelegramConfig tgConfig;
        try {
            tgConfig = new TelegramConfig();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return;
        }

        String botToken = tgConfig.getBotToken();


        CommandDispatcher dispatcher = new CommandDispatcher();

        // регистрируем команды в диспетчере
        dispatcher.register(new StartCommand());
        dispatcher.register(new HelpCommand(dispatcher));


        try (TelegramBot bot = new TelegramBot(botToken, dispatcher)
             // в будущем можно подключить этот же диспетчер к другим платформам
        ) {
            bot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}