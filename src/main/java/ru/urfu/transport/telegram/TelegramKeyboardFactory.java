package ru.urfu.transport.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.urfu.application.InteractionType;
import ru.urfu.model.Sphere;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Создает inline-клавиатуры Telegram в соответствии с текущим шагом в /checkin
 */
public class TelegramKeyboardFactory {
    /**
     * Создает клавиатуру под тип
     *
     * @param interactionType тип взаимодействия
     * @return клавиатура или пустой Optional
     */
    public Optional<InlineKeyboardMarkup> createKeyboard(
            InteractionType interactionType
    ) {
        Objects.requireNonNull(interactionType);
        return switch (interactionType) {
            case NONE -> Optional.empty();
            case SCORE_SELECTION -> Optional.of(createScoreKeyboard());
            case SPHERE_SELECTION -> Optional.of(createSphereKeyboard());
            case NOTE_INPUT -> Optional.of(createNoteKeyboard());
            case PREVIEW_CONFIRMATION -> Optional.of(createPreviewKeyboard());
        };
    }

    /**
     * Создает клавиатуру без рядов кнопок для удаления ранее созданной клавиатуры
     *
     * @return пустая inline-клавиатура
     */
    public InlineKeyboardMarkup createEmptyKeyboard() {
        return new InlineKeyboardMarkup(List.of());
    }

    /**
     * Создает inline-клавиатуру для выбора отметки настроения от 1 до 10
     *
     * @return inline-клавиатура для выбора отметки настроения от 1 до 10 и навигации
     */
    public InlineKeyboardMarkup createScoreKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        button("1", "1"),
                        button("2", "2"),
                        button("3", "3"),
                        button("4", "4"),
                        button("5", "5")
                ))
                .keyboardRow(new InlineKeyboardRow(
                        button("6", "6"),
                        button("7", "7"),
                        button("8", "8"),
                        button("9", "9"),
                        button("10", "10")
                ))
                .keyboardRow(new InlineKeyboardRow(
                        button("Отмена", "/exit")
                ))
                .build();
    }

    /**
     * Создает inline-клавиатуру для выбора сферы
     *
     * @return inline-клавиатура для выбора сферы и навигации
     */
    private InlineKeyboardMarkup createSphereKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        button(Sphere.WORK.getDisplayName(), Sphere.WORK.getDisplayName()),
                        button(Sphere.SOCIAL.getDisplayName(), Sphere.SOCIAL.getDisplayName())
                ))
                .keyboardRow(new InlineKeyboardRow(
                        button(Sphere.HEALTH.getDisplayName(), Sphere.HEALTH.getDisplayName()),
                        button(Sphere.REST.getDisplayName(), Sphere.REST.getDisplayName())
                ))
                .keyboardRow(new InlineKeyboardRow(
                        button("Назад", "/back"),
                        button("Отмена", "/exit")
                ))
                .build();

    }

    /**
     * Создает inline-клавиатуру для пропуска заметки
     *
     * @return inline-клавиатура для пропуска заметки и навигации
     */
    private InlineKeyboardMarkup createNoteKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        button("Пропустить", "/skip")
                ))
                .keyboardRow(new InlineKeyboardRow(
                        button("Назад", "/back"),
                        button("Отмена", "/exit")
                ))
                .build();
    }

    /**
     * Создает inline-клавиатуру для подтверждения и отмены сохранения отметки
     *
     * @return inline-клавиатура для подтверждения и отмены сохранения отметки, а так же навигации
     */
    private InlineKeyboardMarkup createPreviewKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        button("✅ Сохранить", "/approve")
                ))
                .keyboardRow(new InlineKeyboardRow(
                        button("Назад", "/back"),
                        button("❌ Отменить", "/reject")
                ))
                .build();
    }

    /**
     * Создает inline-кнопку с отображаемым текстом и callback значением
     *
     * @param text         отображаемый текст
     * @param callbackData данные отправляемые Telegram при нажатии
     * @return настроенная inline-кнопка
     */
    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

}
