package ru.urfu.commands;

import ru.urfu.repository.MoodRepository;
import ru.urfu.model.MoodEntry;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Команда для записи оценки настроения пользователя
 */
public class CheckinCommand implements BotCommand {
    /**
     * Репозиторий для хранения записей настроения
     */
    private final MoodRepository moodRepository;
    private final Clock clock;

    /**
     * Инициализация команды /checkin
     * @param moodRepository репозиторий для хранения записей настроения
     * @param clock источник текущего времени
     */
    public CheckinCommand(MoodRepository moodRepository, Clock clock) {
        this.moodRepository = moodRepository;
        this.clock = clock;
    }

    @Override
    public String getCommandKey() {
        return "/checkin";

    }

    @Override
    public String getDescription() {
        return "Ежедневная отметка настроения";
    }

    @Override
    public String execute(String userId, String[] args) {
        if (args == null || args.length != 1) {
            return ("Использование команды /checkin: \n" +
                    "/checkin <n> - n число от 1 до 10");
        }
        int score;
        try {
            score = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            return ("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.");
        }
        if (score < 1 || score > 10) {
            return ("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.");
        }

        LocalDateTime createdAt = LocalDateTime.now(clock);
        MoodEntry entry = new MoodEntry(createdAt, score);
        moodRepository.save(userId, entry);
        return ("✅ Настроение " + score+ " успешно сохранено!");
    }
}
