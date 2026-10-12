package ru.urfu.service;

import ru.urfu.repository.MoodRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryCommand implements BotCommand {
    private final int defaultLimit = 10;
    private final MoodRepository moodRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public HistoryCommand(MoodRepository moodRepository) {
        this.moodRepository = moodRepository;

    }

    @Override
    public String getCommandKey() {
        return "/history";
    }

    @Override
    public String getDescription() {
        return ("Показать историю отметок");
    }

    @Override
    public String execute(String userId, String[] args) {
        int limit = defaultLimit;
        if (args != null && args.length > 0) {
            if (args.length != 1) {
                return ("Сообщение об ошибке №2");
            }
            try {
                limit = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                return ("Сообщение об ошибке №2");
            }
            if (limit <= 0) {
                return ("Сообщение об ошибке №2");
            }
        }
        List<MoodEntry> entries = moodRepository.findLast(userId, limit);
        if (entries.isEmpty()) {
            return ("История пуста. Попробуйте использовать команду /checkin");
        }
        StringBuilder result = new StringBuilder();
        for (MoodEntry entry : entries) {
            if (!result.isEmpty()) {
                result.append("\n");

            }
            result.append(entry.createdAt().format(formatter)).append(" - ").append(entry.score()).append("/10");
        }

        return result.toString();
    }
}
