package ru.urfu.repository;

import ru.urfu.model.MoodEntry;
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
    //[R]ead: получение самой последней записи
    /**
     * Возвращает последнюю запись пользователя
     *
     * @param userId идентификатор пользователя
     * @return последняя запись, иначе пустой Optional
     */
    Optional<MoodEntry> findLatest(UUID userId);
    //[U]pdate: обновление последней записи пользователя
    /**
     * Обновляет последнюю запись пользователя
     *
     * @param userId       идентификатор пользователя
     * @param updatedEntry новая версия записи
     * @return true, если обновлена, иначе false
     */
    boolean updateLast(UUID userId, MoodEntry updatedEntry);
    //[D]elete: удаление последней записи пользователя
    /**
     * Удаляет последнюю запись пользователя
     * @param userId идентификатор пользователя
     * @return true, если запись удалена, иначе false
     */
    boolean deleteLast(UUID userId);
}
