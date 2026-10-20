package ru.urfu.service;

/**
 * Тестовая заглушка команды: запоминает, как её вызывали:
 * кто и сколько вызывал.
 */
public class FakeCommand implements BotCommand {

    // Настройки команды
    private final String key;
    private final String description;
    private final String response;

    // Состояние для верификации в тестах
    private String lastUserId;
    private String[] lastArgs;
    private int callCount;

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
     * @return id последнего юзера
     */
    public String getLastUserId() {
        return lastUserId;
    }

    /**
     * Аргументы переданные команде
     *
     * @return аргументы
     */
    public String[] getLastArgs() {
        return lastArgs;
    }

    /**
     * Получение кол-ва вызовов
     *
     * @return количество вызовов команд
     */
    public int getCallCount() {
        return callCount;
    }
}
