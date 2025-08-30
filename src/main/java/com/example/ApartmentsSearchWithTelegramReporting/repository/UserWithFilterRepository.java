package com.example.ApartmentsSearchWithTelegramReporting.repository;

import com.example.ApartmentsSearchWithTelegramReporting.model.UserWithFilter;
import java.util.Set;
import java.util.Map;

public interface UserWithFilterRepository {

    Set<String> readChatIds();

    Map<String, UserWithFilter> readUsersWithFilters();

    void saveUserWithFilters(String chatId, UserWithFilter userWithFilter);
}
