package ru.urfu;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import ru.urfu.config.TelegramConfig;
import ru.urfu.service.BotCommand;
import ru.urfu.service.CommandDispatcher;
import ru.urfu.service.HelpCommand;
import ru.urfu.service.StartCommand;
import ru.urfu.transport.telegram.TelegramBot;

import java.util.List;

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

        List<BotCommand> commands = List.of(new StartCommand(), new HelpCommand());
        CommandDispatcher dispatcher = new CommandDispatcher(commands);

        TelegramBot bot = new TelegramBot(botToken, dispatcher);
        // в будущем можно подключить этот же диспетчер к другим платформам

        try {
            bot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}