package com.example.ApartmentsSearchWithTelegramReporting.repository;

import com.example.ApartmentsSearchWithTelegramReporting.model.UserWithFilter;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserWithFilterRepositoryImplTest {

    private File tempFile;
    private UserWithFilterRepositoryImpl repo;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("chatIds_test", ".txt").toFile();
        tempFile.deleteOnExit();

        repo = new UserWithFilterRepositoryImpl();
        ReflectionTestUtils.setField(repo, "CHAT_IDS_FILE", tempFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void saveUserWithFilters_NewUser_FileContainsUser() {
        UserWithFilter user = new UserWithFilter("Alice", "A", "alice");
        user.setMinPrice(500);
        user.setMaxPrice(1500);
        user.setMinRooms(1);
        user.setMaxRooms(3);

        repo.saveUserWithFilters("123", user);

        Map<String, UserWithFilter> users = repo.readUsersWithFilters();
        assertEquals(1, users.size());
        assertTrue(users.containsKey("123"));
        assertEquals(500, users.get("123").getMinPrice());
    }

    @Test
    void saveUserWithFilters_UpdateExistingUser_ChangesApplied() {
        UserWithFilter user = new UserWithFilter("Alice", "A", "alice");
        user.setMinPrice(500);
        user.setMaxPrice(1500);
        repo.saveUserWithFilters("123", user);

        user.setMaxPrice(2000);
        repo.saveUserWithFilters("123", user);

        Map<String, UserWithFilter> users = repo.readUsersWithFilters();
        assertEquals(1, users.size());
        assertEquals(2000, users.get("123").getMaxPrice());
    }

    @Test
    void readChatIds_ReturnsCorrectIds() {
        UserWithFilter user = new UserWithFilter("Alice", "A", "alice");
        repo.saveUserWithFilters("123", user);
        repo.saveUserWithFilters("456", new UserWithFilter("Bob", "B", "bob"));

        Set<String> chatIds = repo.readChatIds();
        assertEquals(2, chatIds.size());
        assertTrue(chatIds.contains("123"));
        assertTrue(chatIds.contains("456"));
    }

    @Test
    void readUsersWithFilters_InvalidLine_Ignored() throws Exception {
        Files.writeString(tempFile.toPath(), "bad;line;here");

        Map<String, UserWithFilter> users = repo.readUsersWithFilters();
        assertTrue(users.isEmpty());
    }
}
