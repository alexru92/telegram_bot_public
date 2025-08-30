package com.example.ApartmentsSearchWithTelegramReporting.repository;

import com.example.ApartmentsSearchWithTelegramReporting.model.UserWithFilter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.ApartmentsSearchWithTelegramReporting.utils.FileUtils.*;

@Log4j2
@Repository
public class UserWithFilterRepositoryImpl implements UserWithFilterRepository {

    private static String CHAT_IDS_FILE = "chatIds.txt";


    @Override
    public Set<String> readChatIds() {
        return readUsersWithFilters().keySet();
    }

    @Override
    public Map<String, UserWithFilter> readUsersWithFilters() {
        Map<String, UserWithFilter> usersWithFilters = new HashMap<>();
        Set<String> lines = readFromFile(CHAT_IDS_FILE, reader -> reader.lines().collect(Collectors.toSet()));
        if (lines != null && !lines.isEmpty()) {
            for (String line : lines) {
                String[] s = line.split(";");
                if (s.length == 8 || s.length == 9) {
                    UserWithFilter userWithFilter = new UserWithFilter(s[1], s[2], s[3]);

                    userWithFilter.setMinPrice(Integer.parseInt(s[4]));
                    userWithFilter.setMaxPrice(Integer.parseInt(s[5]));
                    userWithFilter.setMinRooms(Integer.parseInt(s[6]));
                    userWithFilter.setMaxRooms(Integer.parseInt(s[7]));
                    if (s.length == 9) userWithFilter.setExceptionsStrings(List.of(s[8].split(",")));
                    else userWithFilter.setExceptionsStrings(new ArrayList<>());

                    usersWithFilters.put(s[0], userWithFilter);
                } else log.error("Wrong row format. Cannot read: {}", line);
            }
        }
        return usersWithFilters;
    }

    @Override
    public void saveUserWithFilters(String id, UserWithFilter userWithFilter) {
        Map<String, String> data = readFromFile(CHAT_IDS_FILE, reader ->
                reader.lines()
                        .filter(line -> !line.isBlank())
                        .map(line -> line.split(";", 2))
                        .filter(arr -> arr.length == 2)
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]))
        );
        if (data == null) data = new HashMap<>();

        String serialized = String.join(";",
                userWithFilter.getFirstName(),
                userWithFilter.getLastName(),
                userWithFilter.getUsername(),
                String.valueOf(userWithFilter.getMinPrice()),
                String.valueOf(userWithFilter.getMaxPrice()),
                String.valueOf(userWithFilter.getMinRooms()),
                String.valueOf(userWithFilter.getMaxRooms()),
                String.join(",", userWithFilter.getExceptionsStrings())
        );

        data.put(id, serialized);

        String content = data.entrySet().stream()
                .map(e -> e.getKey() + ";" + e.getValue())
                .collect(Collectors.joining("\n"));

        writeToFile(CHAT_IDS_FILE, content, false);
    }
}
