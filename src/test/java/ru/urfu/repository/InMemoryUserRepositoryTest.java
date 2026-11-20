package ru.urfu.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.model.PlatformIdentity;
import ru.urfu.model.PlatformType;
import ru.urfu.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Тестирование хранилища пользователей в оперативной памяти
 */
class InMemoryUserRepositoryTest {
    /**
     * Репозиторий для тестов
     */
    private InMemoryUserRepository repository;
    /**
     * Тестовый аккаунт telegram
     */
    private final PlatformIdentity telegramIdentity = new PlatformIdentity(PlatformType.TELEGRAM, "telegram-123");
    /**
     * Тестовый аккаунт discord
     */
    private final PlatformIdentity discordIdentity = new PlatformIdentity(PlatformType.DISCORD, "discord-456");
    /**
     * Дополнительный тестовый аккаунт telegram
     */
    private final PlatformIdentity telegramSecondIdentity = new PlatformIdentity(PlatformType.TELEGRAM, "telegram-456");


    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    /**
     * Тестирует поиск по UUID, по аккаунту платформу
     * в случае отсутствия зарегистрированного аккаунта
     * и возвращает пустой результат
     *
     */
    @Test
    void TestFindByIdNoUser() {
        Assertions.assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
        Assertions.assertTrue(repository.findByPlatformIdentity(telegramIdentity).isEmpty());
    }

    /**
     * Тестирует метод getOrCreate - создание пользователя/ей и сохранение его в репозитории
     */
    @Test
    void TestGetOrCreate() {
        User user = repository.getOrCreate(telegramIdentity);

        Assertions.assertNotNull(user);
        Assertions.assertNotNull(user.getId());
        Assertions.assertEquals(List.of(telegramIdentity), user.getPlatforms());
        Assertions.assertEquals(Optional.of(user), repository.findById(user.getId()));
        Assertions.assertEquals(Optional.of(user), repository.findByPlatformIdentity(telegramIdentity));


        User secondUser = repository.getOrCreate(telegramIdentity);
        Assertions.assertEquals(user.getId(), secondUser.getId());
        Assertions.assertEquals(secondUser.getPlatforms(), user.getPlatforms());


        User discordUser = repository.getOrCreate(discordIdentity);
        Assertions.assertNotEquals(user.getId(), discordUser.getId());
    }

    /**
     * Тестируем метод addPlatform
     * привязка дополнительного аккаунта к существующему пользователю
     */
    @Test
    void TestAddPlatformIdentity() {
        User user = repository.getOrCreate(telegramIdentity);
        User updUser = repository.addPlatformIdentity(user.getId(), discordIdentity);

        Assertions.assertEquals(user.getId(), updUser.getId());

        Assertions.assertEquals(List.of(telegramIdentity, discordIdentity), updUser.getPlatforms());
        Assertions.assertEquals(Optional.of(updUser), repository.findByPlatformIdentity(discordIdentity));
    }

    /**
     * Тестируем метод addPlatform
     * повторная привязка не создает дубликат
     * привязка второго аккаунта той же платформы запрещена
     */
    @Test
    void TestAddPlatformIdentitySamePlatform() {
        User user = repository.getOrCreate(telegramIdentity);
        User updUser = repository.addPlatformIdentity(user.getId(), telegramIdentity);

        Assertions.assertEquals(List.of(telegramIdentity), updUser.getPlatforms());

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.addPlatformIdentity(user.getId(), telegramSecondIdentity)
        );
        Assertions.assertEquals(
                "У пользователя уже привязан аккаунт на платформе TELEGRAM",
                exception.getMessage()
        );
        Assertions.assertTrue(repository.findByPlatformIdentity(telegramSecondIdentity).isEmpty());
        Assertions.assertEquals(List.of(telegramIdentity), user.getPlatforms());


    }

    /**
     * Тестируем метод addPlatform
     * запрет привязки аккаунта,
     * привязанного к другому пользователю
     */
    @Test
    void TestAddPlatformIdentityAlreadyLinked() {

        User firstUser = repository.getOrCreate(telegramIdentity);
        User secondUser = repository.getOrCreate(discordIdentity);

        IllegalStateException firstException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> repository.addPlatformIdentity(secondUser.getId(), telegramIdentity)
        );
        Assertions.assertEquals(
                "Аккаунт платформы уже привязан к другому пользователю",
                firstException.getMessage()
        );
        IllegalStateException secondException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> repository.addPlatformIdentity(firstUser.getId(), discordIdentity)
        );
        Assertions.assertEquals(
                "Аккаунт платформы уже привязан к другому пользователю",
                secondException.getMessage()
        );
        Assertions.assertEquals(
                Optional.of(firstUser),
                repository.findByPlatformIdentity(telegramIdentity));

        Assertions.assertEquals(
                Optional.of(secondUser),
                repository.findByPlatformIdentity(discordIdentity));

        Assertions.assertEquals(
                List.of(discordIdentity),
                secondUser.getPlatforms());
        Assertions.assertEquals(
                List.of(telegramIdentity),
                firstUser.getPlatforms());
    }

    /**
     * Тестируем метод addPlatform
     * запрет привязки аккаунта к несуществующему пользователю
     */
    @Test
    void TestAddPlatformIdentityUnknownUser() {
        UUID unknownUserId = UUID.randomUUID();
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.addPlatformIdentity(
                        unknownUserId, telegramIdentity)
        );
        Assertions.assertEquals("Пользователь не найден: " + unknownUserId,
                exception.getMessage());
    }

    /**
     * Тестируем обработку null в аргументах методов репозитория
     */
    @Test
    void TestMethodsRejectNull() {
        NullPointerException findByIdException = Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.findById(null)
        );
        Assertions.assertEquals("id не может быть null", findByIdException.getMessage());
        NullPointerException findByPlatformException = Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.findByPlatformIdentity(null)
        );
        Assertions.assertEquals("identity не может быть null", findByPlatformException.getMessage());
        NullPointerException getOrCreateException = Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.getOrCreate(null)
        );
        Assertions.assertEquals("identity не может быть null", getOrCreateException.getMessage());
        NullPointerException addNullUserIdException = Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.addPlatformIdentity(null, discordIdentity)
        );
        Assertions.assertEquals("userId не может быть null", addNullUserIdException.getMessage());
        NullPointerException addNullIdentityException = Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.addPlatformIdentity(UUID.randomUUID(), null)
        );
        Assertions.assertEquals("identity не может быть null", addNullIdentityException.getMessage());

    }
}
