package ru.urfu.service.fsm;

import ru.urfu.application.InteractionType;

import java.util.Objects;

public record CheckinResult (String text, InteractionType interactionType){
    public CheckinResult {
        Objects.requireNonNull(interactionType, "interactionType не должен быть null");
        Objects.requireNonNull(text, "text не должен быть null");
    }
}


