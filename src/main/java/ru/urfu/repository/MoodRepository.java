package ru.urfu.repository;

import ru.urfu.model.MoodEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс для работы с записями пользователей
 */
public interface MoodRepository {
    //[C]reate: сохранение новой записи

    /**
     * Сохраняет новую запись (отметку) пользователя
     *
     * @param userId идентификатор пользователя
     * @param entry  запись (отметка)
     */
    void save(UUID userId, MoodEntry entry);
    //[R]ead: получение последних N записей от новых к старым

    /**
     * Возвращает последние записи пользователя (от новых к старым)
     *
     * @param userId идентификатор пользователя
     * @param limit  максимальное число возвращаемых записей
     * @return список последних записей
     */
    List<MoodEntry> findLast(UUID userId, int limit);
    //[R]ead: получение записей за указанный период

    Optional<MoodEntry> findLatest(UUID userId);

    /**
     * Возвращает записи пользователя за указанный период
     *
     * @param userId идентификатор пользователя
     * @param from начало периода включительно
     * @param to конец периода исключительно
     * @return записи пользователя за указанный период
     */
    List<MoodEntry> findByPeriod(UUID userId, LocalDateTime from, LocalDateTime to);
}