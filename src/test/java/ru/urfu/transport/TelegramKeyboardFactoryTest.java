package ru.urfu.transport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.urfu.application.InteractionType;
import ru.urfu.model.Sphere;
import ru.urfu.transport.telegram.TelegramKeyboardFactory;

import java.util.List;
import java.util.Optional;

/**
 * Тестируем, состав и значение callback значений клавиатур Telegram для каждого шага FSM
 */
public class TelegramKeyboardFactoryTest {
    /**
     * Билдер клавиатур
     */
    private final TelegramKeyboardFactory factory = new TelegramKeyboardFactory();

    /**
     * Вытаскиваем отображаемые подписи кнопок одного ряда
     *
     * @param row ряд inline-кнопок
     * @return подписи кнопок в исходном порядке
     */
    private List<String> texts(InlineKeyboardRow row) {
        return row.stream().map(InlineKeyboardButton::getText).toList();
    }

    /**
     * Тестируем, что в обычном взаимодействии inline-клавиатура не создается
     */
    @Test
    void testNoCreateTelegramKeyboardForNoneInteraction() {
        Assertions.assertEquals(Optional.empty(),
                factory.createKeyboard(InteractionType.NONE));
    }

    /**
     * Тестируем, что есть 10 кнопок с оценками и одна на выход
     */
    @Test
    void testCreateScoreKeyboard() {
        InlineKeyboardMarkup keyboard = factory.createScoreKeyboard();
        Assertions.assertEquals(3, keyboard.getKeyboard().size());
        Assertions.assertEquals(List.of("1", "2", "3", "4", "5"), texts(keyboard.getKeyboard().get(0)));
        Assertions.assertEquals(List.of("6", "7", "8", "9", "10"), texts(keyboard.getKeyboard().get(1)));
        Assertions.assertEquals(List.of("Отмена"), texts(keyboard.getKeyboard().get(2)));
        Assertions.assertEquals("/exit", keyboard.getKeyboard().get(2).getFirst().getCallbackData());
    }

    /**
     * Тестируем, наличие всех кнопок у клавиатуры выбора сферы
     *
     */
    @Test
    void testCreateSphereKeyboard() {
        InlineKeyboardMarkup keyboard = factory
                .createKeyboard(InteractionType.SPHERE_SELECTION)
                .orElseThrow();
        Assertions.assertEquals(3, keyboard.getKeyboard().size());
        Assertions.assertEquals(
                List.of(Sphere.WORK.getDisplayName(), Sphere.SOCIAL.getDisplayName()),
                texts(keyboard.getKeyboard().get(0))
        );
        Assertions.assertEquals(
                List.of(Sphere.HEALTH.getDisplayName(), Sphere.REST.getDisplayName()),
                texts(keyboard.getKeyboard().get(1))
        );
        Assertions.assertEquals(List.of("Назад", "Отмена"), texts(keyboard.getKeyboard().get(2)));
        Assertions.assertEquals("/back", keyboard.getKeyboard().get(2).get(0).getCallbackData());
        Assertions.assertEquals("/exit", keyboard.getKeyboard().get(2).get(1).getCallbackData());
    }

    /**
     * Тестируем, клавиатуру на шаге заметки
     */
    @Test
    void testCreateKeyboardNote() {
        InlineKeyboardMarkup keyboard = factory
                .createKeyboard(InteractionType.NOTE_INPUT)
                .orElseThrow();
        Assertions.assertEquals(2, keyboard.getKeyboard().size());
        Assertions.assertEquals("/skip", keyboard.getKeyboard().get(0).getFirst().getCallbackData());
        Assertions.assertEquals(List.of("Назад", "Отмена"), texts(keyboard.getKeyboard().get(1)));
        Assertions.assertEquals("/back", keyboard.getKeyboard().get(1).get(0).getCallbackData());
        Assertions.assertEquals("/exit", keyboard.getKeyboard().get(1).get(1).getCallbackData());
    }

    /**
     * Тестируем клавиатуру на шаге предпросмотра
     */
    @Test
    void testCreateKeyboardPreview() {
        InlineKeyboardMarkup keyboard = factory
                .createKeyboard(InteractionType.PREVIEW_CONFIRMATION)
                .orElseThrow();
        Assertions.assertEquals(2, keyboard.getKeyboard().size());
        Assertions.assertEquals("/approve", keyboard.getKeyboard().get(0).get(0).getCallbackData());
        Assertions.assertEquals("/back", keyboard.getKeyboard().get(1).get(0).getCallbackData());
        Assertions.assertEquals("/reject", keyboard.getKeyboard().get(1).get(1).getCallbackData());
    }

    /**
     * Тестируем на исключения
     */
    @Test
    void testRejectNullInteractionType() {

        NullPointerException expected = Assertions.assertThrows(NullPointerException.class,
                () -> factory.createKeyboard(null));
        Assertions.assertEquals("interactionType не может быть null",
                expected.getMessage());
    }

    /**
     * Тестируем, что пустая клавиатура не содержит рядов кнопок
     */
    void testCreatesEmptyKeyboard() {
        InlineKeyboardMarkup keyboard = factory.createEmptyKeyboard();
        Assertions.assertTrue(keyboard.getKeyboard().isEmpty());
    }
}
