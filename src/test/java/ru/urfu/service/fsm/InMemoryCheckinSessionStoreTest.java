package ru.urfu.service.fsm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import java.util.UUID;

/**
 * Тестирование хранилища сессий чекина.
 */
class InMemoryCheckinSessionStoreTest {

    /**
     * Хранилище сессий, создаваемое перед каждым тестом.
     */
    private InMemoryCheckinSessionStore store;

    /**
     * Идентификатор первого пользователя.
     */
    private final UUID firstUserId = UUID.randomUUID();

    /**
     * Идентификатор второго пользователя.
     */
    private final UUID secondUserId = UUID.randomUUID();


    /**
     * Создаёт пустое хранилище перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        store = new InMemoryCheckinSessionStore();
    }

    /**
     * Проверяет, что сохранённая сессия возвращается тому же пользователю.
     */
    @Test
    void saveAndFindById() {
        CheckinSession session = new CheckinSession(firstUserId);

        store.save(session);

        Assertions.assertSame(session, store.findById(firstUserId).orElseThrow());
    }

    /**
     * Проверяет, что для пользователя без открытой сессии возвращается пустой Optional.
     */
    @Test
    void findByIdUnknownUser() {
        Assertions.assertTrue(store.findById(firstUserId).isEmpty());
    }

    /**
     * Проверяет, что повторное сохранение заменяет предыдущую сессию пользователя.
     */
    @Test
    void saveReplacesExistingSession() {
        CheckinSession first = new CheckinSession(firstUserId);
        CheckinSession second = new CheckinSession(firstUserId);

        store.save(first);
        store.save(second);

        Assertions.assertSame(second, store.findById(firstUserId).orElseThrow());
    }

    /**
     * Проверяет, что удаление возвращает удалённую сессию и убирает её из хранилища.
     */
    @Test
    void removeExistingSession() {
        CheckinSession session = new CheckinSession(firstUserId);
        store.save(session);

        CheckinSession removed = store.remove(firstUserId).orElseThrow();

        Assertions.assertSame(session, removed);
        Assertions.assertTrue(store.findById(firstUserId).isEmpty());
    }

    /**
     * Проверяет, что удаление несуществующей сессии ничего не возвращает.
     */
    @Test
    void removeUnknownUser() {
        Assertions.assertTrue(store.remove(firstUserId).isEmpty());
    }

    /**
     * Проверяет, что сессии разных пользователей хранятся раздельно:
     * удаление одного диалога не затрагивает другой.
     */
    @Test
    void sessionsOfDifferentUsersAreIsolated() {
        CheckinSession firstSession = new CheckinSession(firstUserId);
        CheckinSession secondSession = new CheckinSession(secondUserId);
        store.save(firstSession);
        store.save(secondSession);

        Assertions.assertSame(firstSession, store.findById(firstUserId).orElseThrow());
        Assertions.assertSame(secondSession, store.findById(secondUserId).orElseThrow());

        store.remove(firstUserId);

        Assertions.assertTrue(store.findById(firstUserId).isEmpty());
        Assertions.assertSame(secondSession, store.findById(secondUserId).orElseThrow());
    }


    /**
     * Проверяет, что null в аргументах отклоняется с NullPointerException.
     */
    @Test
    void nullArgumentsAreRejected() {
        Assertions.assertThrows(NullPointerException.class, () -> store.save(null));
        Assertions.assertThrows(NullPointerException.class, () -> store.findById(null));
        Assertions.assertThrows(NullPointerException.class, () -> store.remove(null));
        Assertions.assertThrows(NullPointerException.class, () -> store.hasSession(null));

        Assertions.assertTrue(store.findById(firstUserId).isEmpty());
    }

    /**
     * Проверяет наличие сессии у пользователя,
     * у которого сессия возвращается true,
     * аналогично для пользователя без сессии закрыт - false
     */
    @Test
    void hasSession() {
        CheckinSession firstSession = new CheckinSession(firstUserId);
        store.save(firstSession);

        Assertions.assertTrue(store.hasSession(firstUserId));
        Assertions.assertFalse(store.hasSession(secondUserId));

    }


}