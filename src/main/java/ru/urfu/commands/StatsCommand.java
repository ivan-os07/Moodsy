package ru.urfu.commands;

import ru.urfu.application.BotResponse;
import ru.urfu.service.fsm.SessionCoordinator;

import java.util.Objects;
import java.util.UUID;

/**
 * Выполняет команду /stats и формирует статистику за календарный месяц или неделю
 */
public class StatsCommand implements BotCommand {
    /**
     * Сообщение о корректном использовании команды, при некорректном вызове
     */
    private static final String ARGS_ERROR = "❌ Команда /stats вызывается без параметров";

    /**
     * Координатор диалоговых сессий.
     */
    private final SessionCoordinator sessionCoordinator;

    /**
     * Создаёт команду /stats.
     *
     * @param sessionCoordinator координатор диалоговых сессий
     * @throws NullPointerException если {@code sessionCoordinator} равен {@code null}
     */
    public StatsCommand(SessionCoordinator sessionCoordinator) {
        this.sessionCoordinator = Objects.requireNonNull(
                sessionCoordinator, "sessionCoordinator не может быть null");
    }

    @Override
    public String getCommandKey() {
        return "/stats";
    }

    @Override
    public String getDescription() {
        return "Получить статистику своих отметок";
    }

    @Override
    public BotResponse execute(UUID userId, String[] args) {
        if (args != null && args.length > 0) {
            return new BotResponse(ARGS_ERROR);
        }
        return //TODO сделать как у меня только свое
    }
}