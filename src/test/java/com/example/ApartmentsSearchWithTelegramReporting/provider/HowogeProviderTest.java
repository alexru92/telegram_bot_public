package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HowogeProviderTest {

    @Mock
    private RestService restService;

    private HowogeProvider howogeProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        howogeProvider = new HowogeProvider(restService);
    }

    @Test
    void fetchFlats_ReturnsFlatsFromJson() throws Exception {
        String json = """
        {
          "immoobjects": [
            {
              "uid": "123",
              "title": "Test Address",
              "link": "/flat/123",
              "rent": "1000",
              "area": "50",
              "rooms": "2"
            }
          ]
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json).path("immoobjects");

        when(restService.fetchJson(anyString(), anyString())).thenReturn(root);

        Map<Long, Flat> flats = howogeProvider.fetchFlats();

        assertEquals(1, flats.size());
        Flat flat = flats.values().iterator().next();
        assertEquals(2.0, flat.getRooms());
        assertEquals(50.0, flat.getSize());
        assertEquals(1000.0, flat.getPrice());
        assertEquals("Test Address", flat.getAddress());
        assertEquals("https://www.howoge.de/flat/123", flat.getLink());
    }

    @Test
    void fetchFlats_NullJson_ReturnsEmptyMap() throws Exception {
        when(restService.fetchJson(anyString(), anyString())).thenReturn(null);

        Map<Long, Flat> flats = howogeProvider.fetchFlats();
        assertNotNull(flats);
        assertTrue(flats.isEmpty());
    }

    @Test
    void buildFlat_MapsElementToFlat() {
        Element element = new Element("div");
        element.attr("rooms", "2");
        element.attr("area", "50");
        element.attr("price", "1000");
        element.attr("title", "Test Address");
        element.attr("link", "/flat/123");

        Flat flat = howogeProvider.buildFlat(element);
        assertEquals(2.0, flat.getRooms());
        assertEquals(50.0, flat.getSize());
        assertEquals(1000.0, flat.getPrice());
        assertEquals("Test Address", flat.getAddress());
        assertEquals("https://www.howoge.de/flat/123", flat.getLink());
    }

    @Test
    void parseId_ValidLong_ReturnsId() {
        Element element = new Element("div");
        element.attr("data-id", "123");

        Long id = howogeProvider.parseId(element);
        assertEquals(123L, id);
    }

    @Test
    void parseId_InvalidLong_ReturnsPositiveHash() {
        Element element = new Element("div");
        element.attr("data-id", "abc");
        element.attr("title", "Test");
        element.attr("link", "/flat/123");

        Long id = howogeProvider.parseId(element);
        assertTrue(id > 0);
    }

    private JsonNode mockTextNode(String value) {
        JsonNode node = mock(JsonNode.class);
        when(node.asText()).thenReturn(value);
        return node;
    }
}