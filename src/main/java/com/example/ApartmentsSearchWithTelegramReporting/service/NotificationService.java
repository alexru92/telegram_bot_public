package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.example.ApartmentsSearchWithTelegramReporting.model.FilterType;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.util.ArrayList;
import java.util.List;

import static com.example.ApartmentsSearchWithTelegramReporting.model.BotCommand.*;

@Service
@Log4j2
public class NotificationService {

    @Getter
    private String botToken;
    private final TelegramClient telegramClient;
    private final FlatServiceImpl flatServiceImpl;

    public NotificationService(@Value("${bot.token}") String botToken,
                               FlatServiceImpl flatServiceImpl) {
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.flatServiceImpl = flatServiceImpl;
    }

    public void sendWelcomeMessage(String chatId) {
        List<String> messagesToSend = new ArrayList<>();
        messagesToSend.add("You were subscribed to updates.\n" +
                "To change price filter use:\n" +
                SET_PRICE_FILTER.getPattern() + " 100 1000\n" +
                "To change room filter use:\n" +
                SET_ROOM_FILTER.getPattern() + " 1 3\n" +
                "To exclude specific streets from results use:\n" +
                EXCEPT.getPattern() + "\n\n" +
                "There are 5 last ads:");

        messagesToSend.addAll(flatServiceImpl.getLastFlats(5));
        messagesToSend.forEach(text -> sendMessage(chatId, text));
    }

    public void sendInvalidFilterMessage(String chatId, FilterType filterType) {
        if (filterType == FilterType.PRICE)
            sendMessage(chatId, "Invalid filter format. Please use the pattern: /set_price_filter 0 100000");
        else if (filterType == FilterType.ROOM)
            sendMessage(chatId, "Invalid filter format. Please use the pattern: /set_room_filter 0 100000");
        else if (filterType == FilterType.EXCEPT)
            sendMessage(chatId, "Invalid filter format. Please use the pattern: /except Street");
        else
            sendMessage(chatId, "Invalid filter format.");
    }

    public void sendValidFilterMessage(String chatId, FilterType filterType, int min, int max) {
        if (filterType == FilterType.PRICE)
            sendMessage(chatId, "Price filter set: " + min + " to " + max);
        else if (filterType == FilterType.ROOM)
            sendMessage(chatId, "Room filter set: " + min + " to " + max);
        else
            sendMessage(chatId, "Filter set: " + min + " to " + max);
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error during sending message with text: " + text + "\nto chat: " + chatId + "\nError message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
