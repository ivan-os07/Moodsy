package ru.urfu.config;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.util.function.Function;

/**
 * Настройка Телеграма
 * Берет токен из переменной окружения BOT_TOKEN,
 * если токен не задан - выводит ошибку и завершает работу
 */
public class TelegramConfig {

    /**
     * ENV_BOT_TOKEN - константное название поля в env
     * botToken - токен телеграм бота
     */
    private static final String ENV_BOT_TOKEN = "BOT_TOKEN";
    private final String botToken;

    /**
     * Обращается ко второму конструктору передавая метод ОС System::getenv
     * Вызывается для обращения к переменным окружения ОС
     */
    public TelegramConfig() {
        this(System::getenv);
    }

    /**
     * Сами передаем фейковый токен, например для тестов.
     * Как пример: TelegramConfig(key -> "12345:ABC-DEF");
     */
    public TelegramConfig(Function<String, String> envProvider) {
        String token = envProvider.apply(ENV_BOT_TOKEN);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Ошибка: переменная окружения " + ENV_BOT_TOKEN + " не задана!");
        }
        this.botToken = token;
    }


    public String getBotToken() {
        return botToken;
    }

}
