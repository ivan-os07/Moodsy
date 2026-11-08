package ru.urfu.commands;

import java.util.Arrays;
import java.util.Optional;

/**
 * Маршрутизатор. Получает сырой текст от пользователя, находит нужную команду и вызывает её
 */
public class CommandDispatcher {

    /**
     * Хранилище зарегистрированных команд, в котором ищем команду по ключу.
     */
    private final CommandRegistry registry;

    /**
     * Конструктор диспетчера.
     *
     * @param registry хранилище команд
     */
    public CommandDispatcher(CommandRegistry registry) {
        this.registry = registry;
    }

    /**
     * Обработка входящего сообщения.
     *
     * @param userId id пользователя.
     * @param text   текст сообщения
     * @return ответ бота
     */
    public BotResponse handle(String userId, String text) {
        if (text == null || text.isBlank()) {
            return new BotResponse(userId, "Сообщение не может быть пустым!");
        }

        // Разрезаем строку на части по регулярному выражению
        String[] parts = text.trim().split("\\s+");
        String commandKey = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        // Ищем команду в хранилище
        Optional<BotCommand> command = this.registry.find(commandKey);

        // Если не нашли - возвращаем сообщение об ошибке
        if (command.isEmpty()) {
            return new BotResponse(userId, "Неизвестная команда. Введите /help для справки.");
        }

        // Если нашли - выполняем и возвращаем результат
        String responseText = command.get().execute(userId, args);
        return new BotResponse(userId, responseText);
    }
}
