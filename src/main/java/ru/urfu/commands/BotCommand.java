package ru.urfu.commands;

import ru.urfu.application.BotResponse;

import java.util.UUID;

/**
 * Контракт для всех команд. Каждая команда его реализует.
 * Диспетчер работает с этим интерфейсом и не зависит от конкретной команды.
 */
public interface BotCommand {

    /**
     * Диспетчер по этому ключу находит нужную команду.
     *
     * @return ключ команды вида "/start"
     */
    String getCommandKey();

    /**
     * Возвращает описание для команды /help.
     *
     * @return краткое описание команды
     */
    String getDescription();

    /**
     * Выполняет логику команды и возвращает ответ для пользователя.
     *
     * @param userId идентификатор пользователя, вызвавшего команду
     * @param args   массив аргументов команды
     * @return ответ бота (текст и опциональная клавиатура)
     */
    BotResponse execute(UUID userId, String[] args);
}