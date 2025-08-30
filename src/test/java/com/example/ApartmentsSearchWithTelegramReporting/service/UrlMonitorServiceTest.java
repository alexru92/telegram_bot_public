package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.provider.AbstractFlatProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.mockito.Mockito.*;

class UrlMonitorServiceTest {

    @Mock
    private FlatServiceImpl flatService;

    @Mock
    private RestService restService;

    @Mock
    private FilterService filterService;

    @Mock
    private AbstractFlatProvider provider1;

    @Mock
    private AbstractFlatProvider provider2;

    @InjectMocks
    private UrlMonitorService urlMonitorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        urlMonitorService = new UrlMonitorService(
                flatService,
                List.of(provider1, provider2),
                restService,
                filterService
        );
    }

    @Test
    void monitorUrl_NewFlats_SendMessage() {
        Map<Long, Flat> knownFlats = Map.of(1L, new Flat());
        Map<Long, Flat> newFlatsProvider1 = Map.of(2L, new Flat());
        Map<Long, Flat> newFlatsProvider2 = Map.of(3L, new Flat());

        when(flatService.getAllFlats()).thenReturn(knownFlats);
        when(provider1.fetchFlats()).thenReturn(newFlatsProvider1);
        when(provider2.fetchFlats()).thenReturn(newFlatsProvider2);

        urlMonitorService.monitorUrl();

        verify(filterService).sendMessageToAll(newFlatsProvider1.get(2L));
        verify(filterService).sendMessageToAll(newFlatsProvider2.get(3L));

        verify(flatService).replaceFlats(argThat(map ->
                map.keySet().containsAll(Set.of(2L, 3L)) &&
                        map.values().stream().allMatch(Objects::nonNull)
        ));

    }

    @Test
    void monitorUrl_NoNewFlats_NothingSent() {
        Map<Long, Flat> knownFlats = Map.of(1L, new Flat());
        Map<Long, Flat> newFlatsProvider1 = Map.of(1L, new Flat());

        when(flatService.getAllFlats()).thenReturn(knownFlats);
        when(provider1.fetchFlats()).thenReturn(newFlatsProvider1);
        when(provider2.fetchFlats()).thenReturn(Collections.emptyMap());

        urlMonitorService.monitorUrl();

        verify(flatService, never()).replaceFlats(anyMap());
        verify(filterService, never()).sendMessageToAll(any());
    }

    @Test
    void monitorUrl_CallsRestServiceClearCache() {
        when(flatService.getAllFlats()).thenReturn(Collections.emptyMap());
        when(provider1.fetchFlats()).thenReturn(Collections.emptyMap());
        when(provider2.fetchFlats()).thenReturn(Collections.emptyMap());

        urlMonitorService.monitorUrl();

        verify(restService).clearCache();
    }
}
