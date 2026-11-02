package ru.urfu.service.fsm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Тестирование конечного автомата checkin
 */
class CheckinFsmServiceTest {
    /**
     * Проверяет, что запуск чекина просит ввести оценку
     */
    @Test
    void start_asksForScore() {
//        CheckinSessionStore store = new InMemoryCheckinSessionStore();
        CheckinFsmService service = new CheckinFsmService();

        Assertions.assertEquals("Введите оценку от 1 до 10",
                service.start(UUID.randomUUID()));
    }
}
}