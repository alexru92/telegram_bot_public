package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.example.ApartmentsSearchWithTelegramReporting.model.FilterType;
import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.model.UserWithFilter;
import com.example.ApartmentsSearchWithTelegramReporting.repository.UserWithFilterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.User;
import java.util.*;

import static com.example.ApartmentsSearchWithTelegramReporting.model.BotCommand.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterServiceSendMessageTest {

    @Mock
    UserWithFilterRepository userRepo;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    FilterService filterService;


    private Flat createFlat(int price, int rooms, String address) {
        Flat flat = new Flat();
        flat.setPrice(String.valueOf(price));
        flat.setRooms(String.valueOf(rooms));
        flat.setSize("50");
        flat.setAddress(address);
        flat.setLink("link");
        return flat;
    }

    private UserWithFilter createUserWithFilter(int minPrice, int maxPrice, int minRooms, int maxRooms, String... exceptions) {
        UserWithFilter user = new UserWithFilter("Alice", "A", "alice");
        user.setMinPrice(minPrice);
        user.setMaxPrice(maxPrice);
        user.setMinRooms(minRooms);
        user.setMaxRooms(maxRooms);
        user.setExceptionsStrings(new ArrayList<>(Arrays.asList(exceptions)));
        return user;
    }

    @Test
    void testSendMessageToAll_NoMatches() {
        UserWithFilter user = createUserWithFilter(2000, 3000, 1, 3);

        Map<String, UserWithFilter> users = Map.of("2", user);

        when(userRepo.readChatIds()).thenReturn(users.keySet());
        when(userRepo.readUsersWithFilters()).thenReturn(users);

        Flat flat = createFlat(1000, 2, "Dummy Address 1");

        filterService.sendMessageToAll(flat);

        verify(notificationService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void registerUser_NewUser_Saved() {
        when(userRepo.readChatIds()).thenReturn(Set.of());

        User tgUser = new User(321L, "Alice", false);
        tgUser.setLastName("A");
        tgUser.setUserName("alice");

        filterService.registerUser("123", tgUser);

        verify(userRepo).saveUserWithFilters(eq("123"), any(UserWithFilter.class));
    }

    @Test
    void registerUser_AlreadyExists_NotSaved() {
        when(userRepo.readChatIds()).thenReturn(Set.of("123"));

        User tgUser = new User(321L, "Alice", false);
        tgUser.setFirstName("Alice");

        filterService.registerUser("123", tgUser);

        verify(userRepo, never()).saveUserWithFilters(any(), any());
    }

    @Test
    void updatePriceFilter_ValidCommand_SavedAndNotified() {
        User tgUser = new User(321L, "Alice", false);
        tgUser.setUserName("alice");

        when(userRepo.readUsersWithFilters()).thenReturn(new HashMap<>());

        filterService.updatePriceFilter("123", SET_PRICE_FILTER.getPattern() + " 500 1500", tgUser);

        verify(userRepo).saveUserWithFilters(eq("123"), any(UserWithFilter.class));
        verify(notificationService).sendValidFilterMessage("123", FilterType.PRICE, 500, 1500);
    }

    @Test
    void updatePriceFilter_InvalidArgCount_NotifiedInvalid() {
        User tgUser = new User(321L, "Alice", false);
        filterService.updatePriceFilter("123", SET_PRICE_FILTER.getPattern() + " 500", tgUser);

        verify(notificationService).sendInvalidFilterMessage("123", FilterType.PRICE);
    }

    @Test
    void updatePriceFilter_InvalidNumbers_NotifiedInvalid() {
        User tgUser = new User(321L, "Alice", false);
        filterService.updatePriceFilter("123", SET_PRICE_FILTER.getPattern() + " a b", tgUser);

        verify(notificationService).sendInvalidFilterMessage("123", FilterType.PRICE);
    }

    @Test
    void updateRoomFilter_ValidCommand_SavedAndNotified() {
        User tgUser = new User(321L, "Alice", false);
        when(userRepo.readUsersWithFilters()).thenReturn(new HashMap<>());

        filterService.updateRoomFilter("123", SET_ROOM_FILTER.getPattern() + " 1 3", tgUser);

        verify(userRepo).saveUserWithFilters(eq("123"), any(UserWithFilter.class));
        verify(notificationService).sendValidFilterMessage("123", FilterType.ROOM, 1, 3);
    }

    @Test
    void updateExceptionsFilter_ValidCommand_Saved() {
        User tgUser = new User(321L, "Alice", false);
        when(userRepo.readUsersWithFilters()).thenReturn(new HashMap<>());

        filterService.updateExceptionsFilter("123", EXCEPT.getPattern() + " badword", tgUser);

        verify(userRepo).saveUserWithFilters(eq("123"), any(UserWithFilter.class));
    }

    @Test
    void updateExceptionsFilter_InvalidFormat_NotifiedInvalid() {
        User tgUser = new User(321L, "Alice", false);
        filterService.updateExceptionsFilter("123", EXCEPT.getPattern() + "", tgUser);

        verify(notificationService).sendInvalidFilterMessage("123", FilterType.EXCEPT);
    }

    @Test
    void updateExceptionsFilter_ExceptionThrown_NotifiedInvalid() {
        User tgUser = new User(321L, "Alice", false);
        when(userRepo.readUsersWithFilters()).thenThrow(new RuntimeException("DB error"));

        filterService.updateExceptionsFilter("123", "/except word", tgUser);

        verify(notificationService).sendInvalidFilterMessage("123", FilterType.EXCEPT);
    }

    @Test
    void sendMessageToAll_MatchesFilter_MessageSent() throws InterruptedException {
        UserWithFilter user = createUserWithFilter(500, 1500, 1, 3);
        Map<String, UserWithFilter> users = Map.of("1", user);
        when(userRepo.readChatIds()).thenReturn(users.keySet());
        when(userRepo.readUsersWithFilters()).thenReturn(users);

        Flat flat = createFlat(1000, 2, "Dummy Address 1");

        filterService.sendMessageToAll(flat);

        Thread.sleep(50);
        verify(notificationService).sendMessage(eq("1"), contains("Dummy Address 1"));
    }

    @Test
    void sendMessageToAll_PriceTooHigh_NotSent() throws InterruptedException {
        UserWithFilter user = createUserWithFilter(0, 500, 1, 3);
        Map<String, UserWithFilter> users = Map.of("1", user);
        when(userRepo.readChatIds()).thenReturn(users.keySet());
        when(userRepo.readUsersWithFilters()).thenReturn(users);

        Flat flat = createFlat(1000, 2, "Dummy Address 1");

        filterService.sendMessageToAll(flat);

        Thread.sleep(50);
        verify(notificationService, never()).sendMessage(any(), any());
    }

    @Test
    void sendMessageToAll_ExceptionString_NotSent() throws InterruptedException {
        UserWithFilter user = createUserWithFilter(0, 2000, 1, 3, "Dummy Address 1");
        Map<String, UserWithFilter> users = Map.of("1", user);
        when(userRepo.readChatIds()).thenReturn(users.keySet());
        when(userRepo.readUsersWithFilters()).thenReturn(users);

        Flat flat = createFlat(1000, 2, "Dummy Address 1");

        filterService.sendMessageToAll(flat);

        Thread.sleep(50);
        verify(notificationService, never()).sendMessage(any(), any());
    }
}