package ru.urfu.commands;

import ru.urfu.application.BotResponse;

import java.util.UUID;

/**
 * Реализация команды /help
 */
public class HelpCommand implements BotCommand {

    /**
     * Хранилище команд, из которого берём список для вывода.
     */
    private final CommandRegistry registry;

    /**
     * Конструктор.
     *
     * @param registry хранилище, содержащее все доступные команды
     */
    public HelpCommand(CommandRegistry registry) {
        this.registry = registry;
    }


    @Override
    public String getCommandKey() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Получить помощь и список команд";
    }

    @Override
    public BotResponse execute(UUID userId, String[] args) {
        StringBuilder builder = new StringBuilder("Привет! Я бот Moodsy\n");

        for (BotCommand command : registry.getCommands()) {
            builder.append(command.getCommandKey());
            builder.append(" - ");
            builder.append(command.getDescription());
            builder.append("\n");
        }

        return new BotResponse(builder.toString());

    }
}
