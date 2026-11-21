package ru.urfu.model;


import java.util.Objects;

/**
 * Идентификатор пользователя на конкретной платформе
 *
 * @param platform       тип платформы
 * @param platformUserId идентификатор пользователя на конкретной платформе
 */
public record PlatformIdentity(PlatformType platform, String platformUserId) {
    public PlatformIdentity {
        Objects.requireNonNull(platform, "platform не может быть null");
        Objects.requireNonNull(platformUserId, "platformUserId не может быть null");
        if (platformUserId.isBlank()) {
            throw new IllegalArgumentException("Идентификатор платформы не может быть пустым");
        }
    }
}
