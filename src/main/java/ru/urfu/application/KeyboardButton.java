package ru.urfu.application;

import java.util.Objects;

/**
 * Независимое от транспортного слоя описание кнопки
 * @param label отображаемая подпись кнопки
 * @param action текст действия, который транспорт передает платформе
 */
public record KeyboardButton(String label, String action) {
    /**
     * Проверяет корректность подписи и действия кнопки
     *
     * @param label отображаемая подпись кнопки
     * @param action действие кнопки
     * @throws NullPointerException если label или action равны null
     * @throws IllegalArgumentException если подпись или действие пустые
     */
    public KeyboardButton{
        Objects.requireNonNull(label, "label не равно null");
        Objects.requireNonNull(action, "action не равно null");
        if (label.isEmpty() || action.isEmpty() ) {
            throw new IllegalArgumentException(
                    "Подпись и действие кнопки не должны быть пустыми"
            );
        }
    }

}
