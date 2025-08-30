package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VonoviaProviderTest {

    @Mock
    private RestService restService;

    private VonoviaProvider vonoviaProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vonoviaProvider = new VonoviaProvider(restService);
    }

    @Test
    void fetchFlats_ReturnsFlatsFromJson() {
        JsonNode flatNode = mock(JsonNode.class);
        when(flatNode.path("wrk_id")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("wrk_id").asText()).thenReturn("1");

        when(flatNode.path("slug")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("slug").asText()).thenReturn("test-slug");

        when(flatNode.path("anzahl_zimmer")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("anzahl_zimmer").asText()).thenReturn("2");

        when(flatNode.path("preis")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("preis").asText()).thenReturn("1000");

        when(flatNode.path("groesse")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("groesse").asText()).thenReturn("50");

        when(flatNode.path("strasse")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("strasse").asText()).thenReturn("Musterstraße");

        when(flatNode.path("plz")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("plz").asText()).thenReturn("12345");

        when(flatNode.path("ort")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("ort").asText()).thenReturn("Berlin");

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.iterator()).thenReturn(List.of(flatNode).iterator());

        when(restService.fetchJson(anyString(), eq("results"))).thenReturn(jsonNode);

        Map<Long, Flat> flats = vonoviaProvider.fetchFlats();

        assertEquals(1, flats.size());
        Flat flat = flats.values().iterator().next();
        assertEquals(1000.0, flat.getPrice());
        assertEquals(2.0, flat.getRooms());
        assertEquals(50.0, flat.getSize());
        assertEquals("Musterstraße, 12345 Berlin", flat.getAddress());
        assertTrue(flat.getLink().contains("https://www.vonovia.de/zuhause-finden/immobilien/test-slug-1"));
    }

    @Test
    void fetchFlats_EmptyJson_ReturnsEmpty() {
        when(restService.fetchJson(anyString(), eq("results"))).thenReturn(null);

        Map<Long, Flat> flats = vonoviaProvider.fetchFlats();
        assertNotNull(flats);
        assertTrue(flats.isEmpty());
    }
}
