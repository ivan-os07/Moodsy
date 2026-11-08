package ru.urfu.commands;

/**
 * Тестовая заглушка команды: запоминает, как её вызывали:
 * кто и сколько вызывал.
 */
public class FakeCommand implements BotCommand {

    /**
     * Ключ команды, по которому она распознается.
     */
    private final String key;

    /**
     * Краткое описание того, что делает команда.
     */
    private final String description;

    /**
     * Текст ответа, который отправляет команда пользователю.
     */
    private final String response;


    /**
     * ID пользователя из последнего обработанного вызова (для верификации в тестах).
     */
    private String lastUserId;

    /**
     * Аргументы последнего вызова команды (для верификации в тестах).
     */
    private String[] lastArgs;

    /**
     * Общее количество вызовов метода (для верификации в тестах).
     */
    private int callCount;


    /**
     * Создает тестовую команду с заданными параметрами конфигурации.
     * Инициализирует счетчик вызовов нулевым значением.
     *
     * @param key         ключ команды, по которому она распознается
     * @param description краткое описание назначения команды
     * @param response    текст ответа, который команда отправляет пользователю
     */
    public FakeCommand(String key, String description, String response) {
        this.key = key;
        this.description = description;
        this.response = response;
        this.callCount = 0;
    }

    @Override
    public String getCommandKey() {
        return key;
    }


    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String execute(String userId, String[] args) {
        lastUserId = userId;
        lastArgs = args;
        callCount++;
        return response;
    }

    /**
     * Получаем id последнего юзера
     *
     * @return lastUserId
     */
    public String getLastUserId() {
        return lastUserId;
    }

    /**
     * Аргументы переданные команде
     *
     * @return lastArgs
     */
    public String[] getLastArgs() {
        return lastArgs;
    }

    /**
     * Получение кол-ва вызовов
     *
     * @return callCount
     */
    public int getCallCount() {
        return callCount;
    }
}
