package ru.urfu.service;

/**
 * Тестовая заглушка команды: запоминает, как её вызывали.
 */
public class FakeCommand implements BotCommand {

    private final String key;
    private final String description;
    private final String response;

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

    public String getLastUserId() {
        return lastUserId;
    }

    public String[] getLastArgs() {
        return lastArgs;
    }

    public int getCallCount() {
        return callCount;
    }
}
