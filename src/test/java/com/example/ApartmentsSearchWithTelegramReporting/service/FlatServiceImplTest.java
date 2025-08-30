package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.repository.FlatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlatServiceImplTest {

    @Mock
    private FlatRepository flatRepository;

    @InjectMocks
    private FlatServiceImpl flatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    void getAllFlats_ReturnsAllFlats() {
        Map<Long, Flat> flats = new HashMap<>();
        flats.put(1L, createFlat("Address 1", 1000));
        flats.put(2L, createFlat("Address 2", 2000));

        when(flatRepository.readFlats()).thenReturn(flats);

        FlatServiceImpl service = new FlatServiceImpl(flatRepository);
        Map<Long, Flat> result = service.getAllFlats();

        assertEquals(2, result.size());
        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
    }

    @Test
    void getLastFlats_ReturnsLimitedMessagesInReverseOrder() {
        Map<Long, Flat> flats = new LinkedHashMap<>();
        flats.put(1L, createFlat("Address 1", 1000));
        flats.put(2L, createFlat("Address 2", 2000));
        flats.put(3L, createFlat("Address 3", 3000));

        when(flatRepository.readFlats()).thenReturn(flats);

        FlatServiceImpl service = new FlatServiceImpl(flatRepository);
        List<String> last2 = service.getLastFlats(2);

        assertEquals(2, last2.size());
        assertTrue(last2.get(0).contains("Address 3"));
        assertTrue(last2.get(1).contains("Address 2"));
    }

    @Test
    void replaceFlats_UpdatesRepository() {
        Map<Long, Flat> newFlats = new HashMap<>();
        Flat flat1 = createFlat("Address 1", 1000);
        Flat flat2 = createFlat("Address 2", 2000);
        newFlats.put(1L, flat1);
        newFlats.put(2L, flat2);

        flatService.replaceFlats(newFlats);

        verify(flatRepository).emptyFlats();
        verify(flatRepository).saveFlat(1L, flat1);
        verify(flatRepository).saveFlat(2L, flat2);

        Map<Long, Flat> currentFlats = flatService.getAllFlats();
        assertEquals(2, currentFlats.size());
        assertEquals(flat1, currentFlats.get(1L));
        assertEquals(flat2, currentFlats.get(2L));
    }
}
