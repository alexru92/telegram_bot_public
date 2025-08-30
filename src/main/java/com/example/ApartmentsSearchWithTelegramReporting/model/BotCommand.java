package com.example.ApartmentsSearchWithTelegramReporting.model;

import java.util.Optional;

public enum BotCommand {
    START("/start"),
    SET_PRICE_FILTER("/set_price_filter"),
    SET_ROOM_FILTER("/set_room_filter"),
    EXCEPT("/except");

    private final String pattern;

    BotCommand(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean matches(String input) {
        return input != null && input.trim().startsWith(pattern);
    }

    public static Optional<BotCommand> fromInput(String input) {
        for (BotCommand cmd : values()) {
            if (cmd.matches(input)) return Optional.of(cmd);
        }
        return Optional.empty();
    }
}

