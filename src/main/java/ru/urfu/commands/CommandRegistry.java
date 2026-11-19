package ru.urfu.commands;

import java.util.*;

/**
 * Хранилище всех доступных команд бота.
 */
public class CommandRegistry {

    /**
     * Команды.
     */
    private final Map<String, BotCommand> commands = new LinkedHashMap<>();

    /**
     * Регистрирует команду по её ключу.
     */
    public void register(BotCommand command) {
        commands.put(command.getCommandKey(), command);
    }

    /**
     * Ищет команду по ключу, например "/help".
     */
    public Optional<BotCommand> find(String key) {
        return Optional.ofNullable(commands.get(key));
    }

    /**
     * Возвращает все зарегистрированные команды.
     */
    public Collection<BotCommand> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }
}