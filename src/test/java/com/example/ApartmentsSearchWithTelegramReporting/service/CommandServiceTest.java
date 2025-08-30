package com.example.ApartmentsSearchWithTelegramReporting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.telegram.telegrambots.meta.api.objects.User;
import com.example.ApartmentsSearchWithTelegramReporting.model.BotCommand;

import static org.mockito.Mockito.*;

class CommandServiceTest {

    @Mock
    private FilterService filterService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommandService commandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleCommand_StartCommand_RegistersUserAndSendsWelcome() {
        String chatId = "123";
        User tgUser = new User(1L, "Alice", false);

        commandService.handleCommand(chatId, BotCommand.START.getPattern(), tgUser);

        verify(filterService).registerUser(chatId, tgUser);
        verify(notificationService).sendWelcomeMessage(chatId);
        verifyNoMoreInteractions(filterService, notificationService);
    }

    @Test
    void handleCommand_PriceFilter_DelegatesToFilterService() {
        String chatId = "123";
        User tgUser = new User(1L, "Alice", false);
        String command = BotCommand.SET_PRICE_FILTER.getPattern() + " 100 1000";

        commandService.handleCommand(chatId, command, tgUser);

        verify(filterService).updatePriceFilter(chatId, command, tgUser);
        verifyNoMoreInteractions(filterService, notificationService);
    }

    @Test
    void handleCommand_RoomFilter_DelegatesToFilterService() {
        String chatId = "123";
        User tgUser = new User(1L, "Alice", false);
        String command = BotCommand.SET_ROOM_FILTER.getPattern() + " 1 3";

        commandService.handleCommand(chatId, command, tgUser);

        verify(filterService).updateRoomFilter(chatId, command, tgUser);
        verifyNoMoreInteractions(filterService, notificationService);
    }

    @Test
    void handleCommand_ExceptFilter_SendsNotSupportedMessage() {
        String chatId = "123";
        User tgUser = new User(1L, "Alice", false);
        String command = BotCommand.EXCEPT.getPattern() + " Street";

        commandService.handleCommand(chatId, command, tgUser);

        verify(filterService).updateExceptionsFilter(chatId, command, tgUser);
        verifyNoMoreInteractions(filterService, notificationService);
    }

    @Test
    void getBotToken_ReturnsCorrectToken() {
        when(notificationService.getBotToken()).thenReturn("dummy-token");
        String token = commandService.getBotTogen();
        assert token.equals("dummy-token");
    }
}
