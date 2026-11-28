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
     *
     * @param command команда для регистрации
     * @throws NullPointerException если command равен null
     */
    public void register(BotCommand command) {
        commands.put(command.getCommandKey(), command);
    }

    /**
     * Ищет команду по ключу, например "/help".
     * @param key ключ команды
     * @return Optional с найденной командой или пустой Optional
     */
    public Optional<BotCommand> find(String key) {
        return Optional.ofNullable(commands.get(key));
    }

    /**
     * Возвращает все зарегистрированные команды.
     * @return неизменяемая коллекция зарегистрированных команд
     */
    public Collection<BotCommand> getCommands() {

        return Collections.unmodifiableCollection(commands.values());
    }
}