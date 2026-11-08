package ru.urfu.repository;

import ru.urfu.model.MoodEntry;
import java.util.List;
import java.util.Optional;

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
    void save(String userId, MoodEntry entry);
    //[R]ead: получение последних N записей от новых к старым
    /**
     * Возвращает последние записи пользователя (от новых к старым)
     *
     * @param userId идентификатор пользователя
     * @param limit  максимальное число возвращаемых записей
     * @return список последних записей
     */
    List<MoodEntry> findLast(String userId, int limit);
}
