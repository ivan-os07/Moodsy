package ru.urfu.commands;

import ru.urfu.application.BotResponse;
import ru.urfu.service.fsm.SessionCoordinator;

import java.util.*;

/**
 * Маршрутизатор. <br>
 * Получает сырой текст от пользователя, находит нужную команду и вызывает её
 */
public class CommandDispatcher {

    /**
     * Хранилище зарегистрированных команд, в котором ищем команду по ключу.
     */
    private final CommandRegistry registry;

    /**
     * Координатор диалоговых сессий.
     */
    private final SessionCoordinator sessionCoordinator;

    /**
     * Создаёт диспетчер сообщений.
     *
     * @param registry           реестр команд
     * @param sessionCoordinator координатор диалоговых сессий
     * @throws NullPointerException если любой из аргументов равен {@code null}
     */
    public CommandDispatcher(CommandRegistry registry, SessionCoordinator sessionCoordinator) {
        this.registry = Objects.requireNonNull(
                registry, "registry не может быть null"
        );
        this.sessionCoordinator = Objects.requireNonNull(
                sessionCoordinator, "sessionCoordinator не может быть null"
        );
    }

    /**
     * Обрабатывает входящее сообщение пользователя.
     *
     * @param userId внутренний идентификатор пользователя
     * @param text   текст сообщения или значение нажатой кнопки
     * @return ответ бота
     * @throws NullPointerException если {@code userId} равен {@code null}
     */
    public BotResponse handle(UUID userId, String text) {
        Objects.requireNonNull(userId, "userId не может быть null");

        if (text == null || text.isBlank()) {
            return new BotResponse("Сообщение не может быть пустым!");
        }

        // Сначала проверяем активную диалоговую сессию (до парсинга команды)
        if (sessionCoordinator.hasActiveSession(userId)) {
            return sessionCoordinator.handle(userId, text);
        }

        // Разрезаем строку на части по регулярному выражению
        String[] parts = text.trim().split("\\s+");
        String commandKey = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        Optional<BotCommand> command = registry.find(commandKey);
        if (command.isEmpty()) {
            return new BotResponse("Неизвестная команда. Введите /help для справки.");
        }

        return command.get().execute(userId, args);
    }
}
