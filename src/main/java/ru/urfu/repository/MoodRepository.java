package ru.urfu.repository;

import ru.urfu.service.MoodEntry;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с записями пользователей
 */
public interface MoodRepository {
    //[C]reate: сохранение новой записи
    void save(String userId, MoodEntry entry);
    //[R]ead: получение последних N записей от новых к старым
    List<MoodEntry> findLast(String userId, int limit);
    //[R]ead: получение самой последней записи
    Optional<MoodEntry> findLatest(String userId);
    //[U]pdate: обновление последней записи пользователя
    boolean updateLast(String userId, MoodEntry updatedEntry);
    //[D]elete: удаление последней записи пользователя
    boolean deleteLast(String userId);
}
