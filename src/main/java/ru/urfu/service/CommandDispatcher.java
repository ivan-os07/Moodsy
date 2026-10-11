package ru.urfu.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Маршрутизатор. Получает сырой текст от пользователя, находит нужную команду и вызывает её
 */
public class CommandDispatcher {
    /**
     * Словарь зарегистрированных команд: ключ команды -> объект
     * команды.
     */
    private final Map<String, BotCommand> commands;

    /**
     * Конструктор диспетчера.
     *
     * @param commandList список всех доступных команд
     */
    public CommandDispatcher(List<BotCommand> commandList) {
        this.commands = new HashMap<>();
        // Проходим по списку и складываем каждую команду в мапу:
        for (BotCommand command : commandList) {
            this.commands.put(command.getCommandKey(), command);
        }
    }

    /**
     * Обработка входящего сообщения.
     *
     * @param userId id пользователя.
     * @param text текст сообщения
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

        // Ищем команду в мапе
        BotCommand command = this.commands.get(commandKey);

        // Если не нашли - возвращаем сообщение об ошибке
        if (command == null) {
            return new BotResponse(userId, "Неизвестная команда. Введите /help для справки.");
        }

        // Если нашли - выполняем и возвращаем результат
        String responseText = command.execute(userId, args);
        return new BotResponse(userId, responseText);

    }
}
