package ru.urfu.commands;

/**
 * Контракт для всех команд. Каждая команда будет его реализовывать.
 * Диспетчер будет работать с этим интерфейсом и для него не важно какая именно команда пришла
 */
public interface BotCommand {
    /**
     * Диспетчер по этому ключу находит нужную команду
     *
     * @return String, например ключ команды вида "/start"
     */
    String getCommandKey();

    /**
     * Возвращение описания для команды /help
     *
     * @return String, например "начать работу"
     */
    String getDescription();

    /**
     * Выполняет логику команды
     *
     * @param userId идентификатор пользователя, вызвавшего команду
     * @param args   массив аргументов команды
     * @return String - текст ответа
     */
    String execute(String userId, String[] args);
}
