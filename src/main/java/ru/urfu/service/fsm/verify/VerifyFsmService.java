package ru.urfu.service.fsm.verify;

import ru.urfu.service.fsm.FsmResult;
import ru.urfu.service.fsm.FsmService;

import java.util.UUID;

/**
 * Управляет диалогом команды /verify
 * предлагает ввести код.
 */
public class VerifyFsmService implements FsmService {

    /**
     * Сообщение с предложением ввести код
     */
    private static final String CODE_WAIT = "Для привязки новой платформы введите код";

    /**
     * Сообщение об ошибке - невалидный ключ
     */
    private static final String CODE_ERROR = "❌ Ошибка неверный код";


    @Override
    public boolean hasActiveSession(UUID userId) {
        return false;
    }

    @Override
    public FsmResult handleWithResponse(UUID userId, String text) {
        return null;
    }
}
