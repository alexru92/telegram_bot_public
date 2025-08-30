package com.example.ApartmentsSearchWithTelegramReporting.bot;

import com.example.ApartmentsSearchWithTelegramReporting.service.CommandService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@Log4j2
public class MyTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final CommandService commandService;

    public MyTelegramBot(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public String getBotToken() {
        return commandService.getBotTogen();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Message message = update.getMessage();
        commandService.handleCommand(String.valueOf(message.getChatId()), message.getText(), message.getFrom());
    }
}