package ru.urfu;

/** Вынесенная логика работы бота, не зависящая от платформы
 *
 * Может использоваться и тестироваться независимо от API
 */
public class MyBotLogic {
    /**
     * Создает строку-ответ по заданному сообщению.
     *
     * @param text строка по которой формируем ответ
     * @return текст ответа бота, умолчанию просто возвращает text
     */

    public String buildResponse(String text) {
        return switch (text) {
            case "/start" -> "Добро пожаловать!";
            case "/help" -> """
                    Привет! Я бот Moodsy
                    /start - начать работу
                    /help - показать справку
                    """;
            default -> "Вы ввели " + text;
        };
    }
}
