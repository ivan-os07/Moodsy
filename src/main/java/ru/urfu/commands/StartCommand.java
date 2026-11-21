package ru.urfu.commands;

import java.util.UUID;

/**
 * Реализация команды /start
 */
public class StartCommand implements BotCommand {

    @Override
    public String getCommandKey() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Начать работу";
    }

    @Override
    public String execute(UUID userId, String[] args) {
        return "Добро пожаловать!";
    }


}
