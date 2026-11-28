package ru.urfu.transport.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.urfu.application.Keyboard;
import ru.urfu.application.KeyboardButton;
import ru.urfu.application.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Создает inline-клавиатуры Telegram в соответствии с текущим шагом в /checkin
 */
public class TelegramKeyboardFactory {
    /**
     * Создает Telegram клавиатуру по универсальной
     * @param keyboard универсальная клавиатура
     * @return Telegram inline клавиатура
     */
    public InlineKeyboardMarkup createKeyboard(Keyboard keyboard) {
        Objects.requireNonNull(keyboard, "keyboard не может быть null");

        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (KeyboardRow row : keyboard.rows()){
            List<InlineKeyboardButton> telegramButtons = new ArrayList<>();
            for (KeyboardButton button : row.buttons()){
                telegramButtons.add(InlineKeyboardButton.builder()
                        .text(button.label())
                        .callbackData(button.action())
                        .build());
            }
            rows.add(new InlineKeyboardRow(telegramButtons.toArray(new InlineKeyboardButton[0])));
        }
        return new InlineKeyboardMarkup(rows);
    }

    /**
     * Создает пустую inline клавиатуру Telegram
     * используется для удаления клавиатуры из сообщения
     * @return пустая inline клавиатура
     */
    public InlineKeyboardMarkup createEmptyKeyboard() {
        return new InlineKeyboardMarkup(List.of());
    }

}
