package ru.urfu;

import ru.urfu.config.TelegramConfig;
import ru.urfu.commands.CommandDispatcher;
import ru.urfu.commands.HelpCommand;
import ru.urfu.commands.StartCommand;
import ru.urfu.transport.telegram.TelegramBot;

import java.time.Clock;


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

        MoodRepository moodRepository = new InMemoryMoodRepository();
        CommandDispatcher dispatcher = new CommandDispatcher();

        // регистрируем команды в диспетчере
        dispatcher.register(new StartCommand());
        dispatcher.register(new HelpCommand(dispatcher));
        dispatcher.register(new CheckinCommand(moodRepository, Clock.systemDefaultZone()));
        dispatcher.register(new HistoryCommand(moodRepository));


        try (TelegramBot bot = new TelegramBot(botToken, dispatcher)
             // в будущем можно подключить этот же диспетчер к другим платформам
        ) {
            bot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}