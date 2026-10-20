package ru.urfu.commands;

/**
 * Реализация команды /help
 */
public class HelpCommand implements BotCommand {

    /**
     * Диспетчер
     */
    private final CommandDispatcher dispatcher;

    /**
     * Конструктор для инициализации команды помощи.
     *
     * @param dispatcher диспетчер команд, используемый для получения списка
     *                   всех доступных в приложении команд и их описаний
     */
    public HelpCommand(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getCommandKey() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Получить помощь и список команд";
    }

    @Override
    public String execute(String userId, String[] args) {
        StringBuilder builder = new StringBuilder("Привет! Я бот Moodsy\n");

        for (BotCommand command : this.dispatcher.getCommands()) {
            builder.append(command.getCommandKey());
            builder.append(" - ");
            builder.append(command.getDescription());
            builder.append("\n");
        }

        return builder.toString();

    }
}
