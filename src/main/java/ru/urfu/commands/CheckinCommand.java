package ru.urfu.commands;

import ru.urfu.service.fsm.CheckinFsmService;

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
     * Сервис диалога чекина.
     */
    private final CheckinFsmService checkinFsmService;

    /**
     * Создаёт команду /checkin.
     *
     * @param checkinFsmService сервис диалога чекина
     */
    public CheckinCommand(CheckinFsmService checkinFsmService) {
        this.checkinFsmService = Objects.requireNonNull(
                checkinFsmService, "checkinFsmService не может быть null");
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
    public String execute(UUID userId, String[] args) {
        if (args != null && args.length > 0) {
            return ARGS_ERROR;
        }
        return checkinFsmService.start(userId);
    }
}
