package ru.urfu.service.fsm;

import java.util.UUID;

public interface FsmService {
    /**
     * Проверяет наличие открытой сессии у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return true, если у пользователя есть открытый диалог
     */
    boolean hasActiveSession(UUID userId);
    /**
     * Обрабатывает пользовательский ввод и определяет тип клавиатуры для следующего шага
     *
     * @param userId идентификатор пользователя
     * @param text   текст ввода
     * @return результат обработки вместе с необходимым типом клавиатуры
     */
    FsmResult handleWithResponse(UUID userId, String text);
}
