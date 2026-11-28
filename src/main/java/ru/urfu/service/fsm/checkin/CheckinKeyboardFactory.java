package ru.urfu.service.fsm.checkin;

import ru.urfu.application.Keyboard;
import ru.urfu.application.KeyboardButton;
import ru.urfu.application.KeyboardRow;
import ru.urfu.model.Sphere;

import java.util.List;
import java.util.Objects;

/**
 * Создаёт универсальные клавиатуры для каждого шага диалога чекина.
 */
public class CheckinKeyboardFactory {

    private static final String CMD_EXIT = "/exit";
    private static final String CMD_BACK = "/back";
    private static final String CMD_SKIP = "/skip";
    private static final String CMD_APPROVE = "/approve";

    /**
     * Формирует клавиатуру, соответствующую текущему шагу чекина.
     *
     * @param step шаг диалога чекина
     * @return универсальная клавиатура с доступными действиями
     * @throws NullPointerException если {@code step} равен {@code null}
     */
    public Keyboard create(CheckinStep step) {
        Objects.requireNonNull(step, "step не может быть null");

        return switch (step) {
            case SCORE -> createScoreKeyboard();
            case SPHERE -> createSphereKeyboard();
            case NOTE -> createNoteKeyboard();
            case PREVIEW -> createPreviewKeyboard();
        };
    }

    private Keyboard createScoreKeyboard() {
        return new Keyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton("1", "1"),
                        new KeyboardButton("2", "2"),
                        new KeyboardButton("3", "3"),
                        new KeyboardButton("4", "4"),
                        new KeyboardButton("5", "5")
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton("6", "6"),
                        new KeyboardButton("7", "7"),
                        new KeyboardButton("8", "8"),
                        new KeyboardButton("9", "9"),
                        new KeyboardButton("10", "10")
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton("Отмена", CMD_EXIT)
                ))
        ));
    }

    private Keyboard createSphereKeyboard() {
        return new Keyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Sphere.WORK.getDisplayName(), Sphere.WORK.getDisplayName()),
                        new KeyboardButton(Sphere.SOCIAL.getDisplayName(), Sphere.SOCIAL.getDisplayName())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Sphere.HEALTH.getDisplayName(), Sphere.HEALTH.getDisplayName()),
                        new KeyboardButton(Sphere.REST.getDisplayName(), Sphere.REST.getDisplayName())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton("Назад", CMD_BACK),
                        new KeyboardButton("Отмена", CMD_EXIT)
                ))
        ));
    }

    private Keyboard createNoteKeyboard() {
        return new Keyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton("Пропустить", CMD_SKIP)
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton("Назад", CMD_BACK),
                        new KeyboardButton("Отмена", CMD_EXIT)
                ))
        ));
    }

    private Keyboard createPreviewKeyboard() {
        return new Keyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton("✅ Сохранить", CMD_APPROVE)
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton("Назад", CMD_BACK),
                        new KeyboardButton("Отмена", CMD_EXIT)
                ))
        ));
    }
}