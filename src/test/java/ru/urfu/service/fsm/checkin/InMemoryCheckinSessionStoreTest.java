package ru.urfu.service.fsm.checkin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import ru.urfu.model.Sphere;

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
    void testSaveAndFindById() {
        CheckinSession session = new CheckinSession(firstUserId, CheckinStep.SCORE, 0, Sphere.WORK, "");

        store.save(session);

        Assertions.assertSame(
                session,
                store.findById(firstUserId).orElseThrow()
        );
    }

    /**
     * Проверяет, что для пользователя без открытой сессии возвращается пустой Optional.
     */
    @Test
    void testFindByIdUnknownUser() {
        Assertions.assertTrue(store.findById(firstUserId).isEmpty());
    }

    /**
     * Проверяет, что повторное сохранение заменяет предыдущую сессию пользователя.
     */
    @Test
    void testSaveReplacesExistingSession() {
        CheckinSession first = new CheckinSession(firstUserId, CheckinStep.SCORE, 0, Sphere.WORK, "");
        CheckinSession second = new CheckinSession(firstUserId, CheckinStep.SCORE, 0, Sphere.WORK, "");

        store.save(first);
        store.save(second);

        Assertions.assertSame(
                second,
                store.findById(firstUserId).orElseThrow()
        );
    }

    /**
     * Проверяет, что удаление возвращает удалённую сессию и убирает её из хранилища.
     */
    @Test
    void testRemoveExistingSession() {
        CheckinSession session = new CheckinSession(firstUserId, CheckinStep.SCORE, 0, Sphere.WORK, "");
        store.save(session);

        CheckinSession removed = store.remove(firstUserId).orElseThrow();

        Assertions.assertSame(session, removed);
        Assertions.assertTrue(store.findById(firstUserId).isEmpty());

        // Сессия отсутствует
        Assertions.assertFalse(store.hasSession(firstUserId));
    }

    /**
     * Проверяет, что удаление несуществующей сессии ничего не возвращает.
     */
    @Test
    void testRemoveUnknownUser() {
        Assertions.assertTrue(store.remove(firstUserId).isEmpty());
    }

    /**
     * Проверяет, что сессии разных пользователей хранятся раздельно:
     * удаление одного диалога не затрагивает другой.
     */
    @Test
    void testSessionsOfDifferentUsersAreIsolated() {
        CheckinSession firstSession = new CheckinSession(firstUserId, CheckinStep.SCORE, 0, Sphere.WORK, "");
        CheckinSession secondSession = new CheckinSession(secondUserId, CheckinStep.SCORE, 0, Sphere.WORK, "");
        store.save(firstSession);
        store.save(secondSession);

        Assertions.assertSame(
                firstSession,
                store.findById(firstUserId).orElseThrow()
        );

        Assertions.assertSame(
                secondSession,
                store.findById(secondUserId).orElseThrow()
        );

        store.remove(firstUserId);

        Assertions.assertTrue(store.findById(firstUserId).isEmpty());
        Assertions.assertSame(
                secondSession,
                store.findById(secondUserId).orElseThrow()
        );
    }


    /**
     * Проверяет, что сохранение {@code null}-сессии вызывает {@link NullPointerException}.
     */
    @Test
    void testSaveRejectsNullSession() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> store.save(null)
        );

        Assertions.assertEquals(
                "session не может быть null",
                exception.getMessage()
        );
    }

    /**
     * Проверяет, что поиск по {@code null}-идентификатору вызывает {@link NullPointerException}.
     */
    @Test
    void testFindByIdRejectsNullUserId() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> store.findById(null)
        );

        Assertions.assertEquals(
                "userId не может быть null",
                exception.getMessage()
        );
    }

    /**
     * Проверяет, что удаление по {@code null}-идентификатору вызывает {@link NullPointerException}.
     */
    @Test
    void testRemoveRejectsNullUserId() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> store.remove(null)
        );

        Assertions.assertEquals(
                "userId не может быть null",
                exception.getMessage()
        );
    }

    /**
     * Проверяет, что проверка наличия сессии по {@code null}-идентификатору вызывает {@link NullPointerException}.
     */
    @Test
    void testHasSessionRejectsNullUserId() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> store.hasSession(null)
        );

        Assertions.assertEquals(
                "userId не может быть null",
                exception.getMessage()
        );
    }


    /**
     * Проверяет, что {@code hasSession} возвращает {@code true} для пользователя
     * с открытой сессией и {@code false} для пользователя без неё.
     */
    @Test
    void testHasSession() {
        CheckinSession firstSession = new CheckinSession(firstUserId, CheckinStep.SCORE, 0, Sphere.WORK, "");

        store.save(firstSession);

        Assertions.assertTrue(store.hasSession(firstUserId));
        Assertions.assertFalse(store.hasSession(secondUserId));

    }

}