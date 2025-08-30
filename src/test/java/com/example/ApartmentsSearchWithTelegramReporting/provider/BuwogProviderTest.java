package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BuwogProviderTest {

    private RestService restService;
    private BuwogProvider buwogProvider;

    @BeforeEach
    void setUp() {
        restService = mock(RestService.class);
        buwogProvider = new BuwogProvider(restService);
    }

    @Test
    void fetchFlats_ReturnsFlatsFromJson() throws Exception {
        JsonNode flatNode = mock(JsonNode.class);

        when(flatNode.path("real_estate_id")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("real_estate_id").asText()).thenReturn("1");

        when(flatNode.path("title")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("title").asText()).thenReturn("Title 1");

        when(flatNode.path("total_rooms")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("total_rooms").asText()).thenReturn("2");

        when(flatNode.path("rent_price")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("rent_price").asText()).thenReturn("1000");

        when(flatNode.path("total_size")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("total_size").asText()).thenReturn("50");

        when(flatNode.path("city")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("city").asText()).thenReturn("City");

        when(flatNode.path("street")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("street").asText()).thenReturn("Street");

        when(flatNode.path("house_number")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("house_number").asText()).thenReturn("10");

        when(flatNode.path("zip_code")).thenReturn(mock(JsonNode.class));
        when(flatNode.path("zip_code").asText()).thenReturn("12345");

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.iterator()).thenReturn(List.of(flatNode).iterator());

        when(restService.fetchJson(anyString(), eq("real_estate_result"))).thenReturn(jsonNode);

        Map<Long, Flat> flats = buwogProvider.fetchFlats();

        assertEquals(1, flats.size());
        Flat flat = flats.values().iterator().next();
        assertEquals(1000.0, flat.getPrice());
        assertEquals(2.0, flat.getRooms());
        assertEquals(50.0, flat.getSize());
        assertEquals("Street 10, 12345 City", flat.getAddress());
        assertTrue(flat.getLink().contains("https://www.buwog-immobilientreuhand.de/immobilien/city-wohnung-1"));
    }



    @Test
    void fetchFlats_EmptyElements_ReturnsEmptyMap() {
        when(restService.fetchAndParseElements(anyString(), anyString()))
                .thenReturn(new Elements());

        Map<Long, Flat> flats = buwogProvider.fetchFlats();

        assertEquals(0, flats.size());
    }
}
