package ru.urfu.service;

/**
 * Реализация команды /help
 */
public class HelpCommand implements BotCommand {

    @Override
    public String getCommandKey() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Получить помощь и список команд";
    }

    @Override
    public String execute(String userId, String[] args) {
        return """
                Привет! Я бот Moodsy
                /start - начать работу
                /help - показать справку
                """;
    }
}
