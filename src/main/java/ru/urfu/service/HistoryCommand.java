package ru.urfu.service;

import ru.urfu.repository.MoodRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Команда для просмотра записей пользователя
 */
public class HistoryCommand implements BotCommand {
    /**
     * Репозиторий для получения записей настроения
     */
    private final MoodRepository moodRepository;
    /**
     * Формат даты, времени в истории записей
     */
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Инициализация команды просмотра истории
     * @param moodRepository репозиторий с записями настроения
     */
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
        int limit = 10;
        if (args != null && args.length > 0) {
            if (args.length != 1) {
                return ("Формат использования команды /history: \n " +
                        "/history <n> - где n кол-во записей\n"+
                        "/history - последние 10 записей");
            }
            try {
                limit = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                return ("❌ Ошибка: переданный формат числа не соответствует ожидаемому");
            }
            if (limit <= 0) {
                return ("❌ Ошибка: переданный формат числа не соответствует ожидаемому");
            }
        }
        List<MoodEntry> entries = moodRepository.findLast(userId, limit);
        if (entries.isEmpty()) {
            return ("История пуста. Попробуйте использовать команду /checkin");
        }
        StringBuilder result = new StringBuilder(" \uD83D\uDCC5 Ваша история записей:");
        for (MoodEntry entry : entries) {
            if (!result.isEmpty()) {
                result.append("\n");

            }
            result.append(entry.createdAt().format(formatter)).append(" - ").append(entry.score()).append("/10");
        }

        return result.toString();
    }
}
