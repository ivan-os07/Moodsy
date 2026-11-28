package ru.urfu.commands;

import ru.urfu.application.BotResponse;
import ru.urfu.service.fsm.DialogType;
import ru.urfu.service.fsm.SessionCoordinator;

import java.util.Objects;
import java.util.UUID;

/**
 * Команда начала пошаговой отметки настроения.
 * Сама запись не сохраняет: запускает диалог в сервисе чекина.
 */
public class CheckinCommand implements BotCommand {

    /**
     * Сообщение о недопустимом вызове команды с параметрами.
     */
    private static final String ARGS_ERROR =
            "❌ Команда /checkin вызывается без параметров.";

    /**
     * Координатор диалоговых сессий.
     */
    private final SessionCoordinator sessionCoordinator;

    /**
     * Создаёт команду /checkin.
     *
     * @param sessionCoordinator координатор диалоговых сессий
     * @throws NullPointerException если {@code sessionCoordinator} равен {@code null}
     */
    public CheckinCommand(SessionCoordinator sessionCoordinator) {
        this.sessionCoordinator = Objects.requireNonNull(
                sessionCoordinator, "sessionCoordinator не может быть null");
    }

    @Override
    public String getCommandKey() {
        return "/checkin";
    }

    @Override
    public String getDescription() {
        return "Пошаговая отметка настроения";
    }

    @Override
    public BotResponse execute(UUID userId, String[] args) {
        if (args != null && args.length > 0) {
            return new BotResponse(ARGS_ERROR);
        }
        return sessionCoordinator.start(userId, DialogType.CHECKIN);
    }
}
