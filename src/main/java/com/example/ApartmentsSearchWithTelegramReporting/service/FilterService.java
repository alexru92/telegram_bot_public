package com.example.ApartmentsSearchWithTelegramReporting.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import com.example.ApartmentsSearchWithTelegramReporting.model.FilterType;
import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.model.UserWithFilter;
import com.example.ApartmentsSearchWithTelegramReporting.repository.UserWithFilterRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.example.ApartmentsSearchWithTelegramReporting.model.FilterType.EXCEPT;

@Service
@Log4j2
public class FilterService {

    private final UserWithFilterRepository userWithFilterRepository;
    private final NotificationService notificationService;

    public FilterService(UserWithFilterRepository userWithFilterRepository, NotificationService notificationService) {
        this.userWithFilterRepository = userWithFilterRepository;
        this.notificationService = notificationService;
    }

    public void registerUser(String chatId, User user) {
        if (!userWithFilterRepository.readChatIds().contains(chatId)) {
            UserWithFilter userWithFilter = createUserWithFilter(user);
            userWithFilterRepository.saveUserWithFilters(chatId, createUserWithFilter(user));
            log.info("User {} {}, {}, has subscribed with chat_id: {}",
                    userWithFilter.getFirstName(),
                    userWithFilter.getLastName(),
                    userWithFilter.getUsername(),
                    chatId
            );
        }
    }

    public void updatePriceFilter(String chatId, String text, User user) {
        updateRangeFilter(chatId, text, user, FilterType.PRICE,
                UserWithFilter::setMinPrice,
                UserWithFilter::setMaxPrice);
    }

    public void updateRoomFilter(String chatId, String text, User user) {
        updateRangeFilter(chatId, text, user, FilterType.ROOM,
                UserWithFilter::setMinRooms,
                UserWithFilter::setMaxRooms);
    }

    public void updateExceptionsFilter(String chatId, String text, User user) {
        String[] command = text.split(" ");
        if (command.length != 2) {
            notificationService.sendInvalidFilterMessage(chatId, EXCEPT);
            return;
        }

        try {
            String newException = command[1];

            Map<String, UserWithFilter> usersWithFilters = userWithFilterRepository.readUsersWithFilters();
            UserWithFilter userWithFilter = usersWithFilters
                    .getOrDefault(chatId, createUserWithFilter(user));

            List<String> exceptionsStrings = userWithFilter.getExceptionsStrings();
            exceptionsStrings.add(newException);
            userWithFilter.setExceptionsStrings(exceptionsStrings);

            userWithFilterRepository.saveUserWithFilters(chatId, userWithFilter);
        } catch (Exception e) {
            notificationService.sendInvalidFilterMessage(chatId, EXCEPT);
        }
    }

    private void updateRangeFilter(
            String chatId,
            String text,
            User user,
            FilterType filterName,
            BiConsumer<UserWithFilter, Integer> minSetter,
            BiConsumer<UserWithFilter, Integer> maxSetter
    ) {
        String[] command = text.split(" ");
        if (command.length != 3) {
            notificationService.sendInvalidFilterMessage(chatId, filterName);
            return;
        }

        try {
            int min = Integer.parseInt(command[1]);
            int max = Integer.parseInt(command[2]);

            Map<String, UserWithFilter> usersWithFilters = userWithFilterRepository.readUsersWithFilters();
            UserWithFilter userWithFilter = usersWithFilters
                    .getOrDefault(chatId, createUserWithFilter(user));

            minSetter.accept(userWithFilter, min);
            maxSetter.accept(userWithFilter, max);

            userWithFilterRepository.saveUserWithFilters(chatId, userWithFilter);

            notificationService.sendValidFilterMessage(chatId, filterName, min, max);

            log.info("User {} {}, {}, set {} filter: from {} to {}",
                    userWithFilter.getFirstName(),
                    userWithFilter.getLastName(),
                    userWithFilter.getUsername(),
                    filterName,
                    min,
                    max
            );

        } catch (NumberFormatException e) {
            notificationService.sendInvalidFilterMessage(chatId, filterName);
        }
    }

    public void sendMessageToAll(Flat flat) {
        userWithFilterRepository.readChatIds().forEach(chatId -> {
            UserWithFilter userWithFilter = userWithFilterRepository.readUsersWithFilters().get(chatId);
            if (userWithFilter != null && matchesFilter(userWithFilter, flat)) {
                CompletableFuture.runAsync(() -> notificationService.sendMessage(chatId, flat.getMessage()));
            }
        });
    }

    private UserWithFilter createUserWithFilter(User user){
        String userName = Optional.ofNullable(user.getUserName()).orElse("");
        String firstName = Optional.ofNullable(user.getFirstName()).orElse("");
        String lastName = Optional.ofNullable(user.getLastName()).orElse("");

        return new UserWithFilter(firstName, lastName, userName);
    }

    private boolean matchesFilter(UserWithFilter userWithFilter, Flat flat) {
        int intPrice = flat.getIntPrice();
        int intRooms = flat.getIntRooms();
        String exceptionString = flat.getShortMessage();

        if (intPrice > userWithFilter.getMaxPrice()) {
            log.info("Flat excluded: price {} is above user {}'s max ({}) | {}",
                    intPrice,
                    userWithFilter.getUsername(),
                    userWithFilter.getMaxPrice(),
                    flat.getShortMessage()
            );
            return false;
        }
        if (intPrice < userWithFilter.getMinPrice()) {
            log.info("Flat excluded: price {} is below user {}'s min ({}) | {}",
                    intPrice,
                    userWithFilter.getUsername(),
                    userWithFilter.getMinPrice(),
                    flat.getShortMessage()
            );
            return false;
        }
        if (intRooms > userWithFilter.getMaxRooms()) {
            log.info("Flat excluded: rooms {} exceed user {}'s max ({}) | {}",
                    intRooms,
                    userWithFilter.getUsername(),
                    userWithFilter.getMaxRooms(),
                    flat.getShortMessage()
            );
            return false;
        }
        if (intRooms < userWithFilter.getMinRooms()) {
            log.info("Flat excluded: rooms {} are below user {}'s min ({}) | {}",
                    intRooms,
                    userWithFilter.getUsername(),
                    userWithFilter.getMinRooms(),
                    flat.getShortMessage()
            );
            return false;
        }
        Optional<String> filterMatch = userWithFilter.getExceptionsStrings().stream().filter(exceptionString::contains).findFirst();
        if (filterMatch.isPresent()) {
            log.info("Flat excluded: row {} is in exceptions for user {} | {}",
                    filterMatch.get(),
                    userWithFilter.getUsername(),
                    flat.getShortMessage()
            );
            return false;
        }

        return true;
    }
}
