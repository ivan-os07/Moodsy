package ru.urfu.commands;

import ru.urfu.application.BotResponse;

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
    public BotResponse execute(UUID userId, String[] args) {
        return new BotResponse("Добро пожаловать!");
    }


}
