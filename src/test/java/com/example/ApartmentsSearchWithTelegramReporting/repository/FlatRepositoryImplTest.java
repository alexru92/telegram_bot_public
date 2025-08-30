package com.example.ApartmentsSearchWithTelegramReporting.repository;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FlatRepositoryImplTest {

    private FlatRepositoryImpl flatRepo;
    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("flats_test", ".txt").toFile();
        flatRepo = new FlatRepositoryImpl();

        ReflectionTestUtils.setField(flatRepo, "FLATS_FILE", tempFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) tempFile.delete();
    }

    private Flat createFlat(String address, int price) {
        Flat flat = new Flat();
        flat.setAddress(address);
        flat.setPrice(String.valueOf(price));
        flat.setRooms("2");
        flat.setSize("50");
        flat.setLink("link");
        return flat;
    }

    @Test
    void readFlats_InitiallyEmpty_ReturnsEmptyMap() {
        Map<Long, Flat> flats = flatRepo.readFlats();
        assertNotNull(flats);
        assertTrue(flats.isEmpty());
    }

    @Test
    void saveFlat_ThenReadFlats_ReturnsSavedFlat() {
        Flat flat = createFlat("Address 1", 1000);
        flatRepo.saveFlat(1L, flat);

        Map<Long, Flat> flats = flatRepo.readFlats();
        assertEquals(1, flats.size());
        assertEquals(flat.getAddress(), flats.get(1L).getAddress());
    }

    @Test
    void emptyFlats_ClearsFileAndMap() {
        Flat flat1 = createFlat("Address 1", 1000);
        flatRepo.saveFlat(1L, flat1);

        flatRepo.emptyFlats();
        Map<Long, Flat> flats = flatRepo.readFlats();
        assertTrue(flats.isEmpty());
    }

    @Test
    void saveMultipleFlats_ReadFlats_ReturnsAll() {
        Flat flat1 = createFlat("Address 1", 1000);
        Flat flat2 = createFlat("Address 2", 2000);

        flatRepo.saveFlat(1L, flat1);
        flatRepo.saveFlat(2L, flat2);

        Map<Long, Flat> flats = flatRepo.readFlats();
        assertEquals(2, flats.size());
        assertEquals(flat1.getAddress(), flats.get(1L).getAddress());
        assertEquals(flat2.getAddress(), flats.get(2L).getAddress());
    }
}
