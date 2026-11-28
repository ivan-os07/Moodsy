package ru.urfu.service.fsm.stats;

import ru.urfu.service.fsm.UserSession;

import java.util.UUID;

//TODO написать java doc и параметры
public record StatsSession(
        UUID userId
) implements UserSession {
}
