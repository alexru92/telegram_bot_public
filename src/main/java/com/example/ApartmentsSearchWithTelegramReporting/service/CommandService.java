package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.example.ApartmentsSearchWithTelegramReporting.model.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
public class CommandService {

    private final FilterService filterService;
    private final NotificationService notificationService;

    public CommandService(FilterService filterService, NotificationService notificationService) {
        this.filterService = filterService;
        this.notificationService = notificationService;
    }

    public void handleCommand(String chatId, String text, User user) {
        BotCommand.fromInput(text).ifPresentOrElse(cmd -> {
            switch (cmd) {
                case START -> {
                    filterService.registerUser(chatId, user);
                    notificationService.sendWelcomeMessage(chatId);
                }
                case SET_PRICE_FILTER -> filterService.updatePriceFilter(chatId, text, user);
                case SET_ROOM_FILTER -> filterService.updateRoomFilter(chatId, text, user);
                case EXCEPT -> filterService.updateExceptionsFilter(chatId, text, user);
            }
        }, () -> notificationService.sendMessage(chatId, "Unknown command"));
    }

    public String getBotTogen() {
        return notificationService.getBotToken();
    }
}
