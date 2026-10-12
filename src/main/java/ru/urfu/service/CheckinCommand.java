package ru.urfu.service;

import ru.urfu.repository.MoodRepository;

import java.time.Clock;
import java.time.LocalDateTime;

public class CheckinCommand implements BotCommand {
    private final MoodRepository moodRepository;
    private final Clock clock;

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
            return ("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.");
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
