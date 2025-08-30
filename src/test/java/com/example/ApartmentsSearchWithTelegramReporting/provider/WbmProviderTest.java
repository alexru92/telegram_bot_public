package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WbmProviderTest {

    @Mock
    private RestService restService;

    private WbmProvider wbmProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        wbmProvider = new WbmProvider(restService);
    }

    @Test
    void fetchFlats_ReturnsFlatsFromHtml() {
        Element flatElement = mock(Element.class);

        when(flatElement.select("ul.main-property-list li:nth-child(3) .main-property-value"))
                .thenReturn(new Elements(new Element("span").text("2")));
        when(flatElement.select("ul.main-property-list li:nth-child(2) .main-property-value"))
                .thenReturn(new Elements(new Element("span").text("50")));
        when(flatElement.select("ul.main-property-list li:nth-child(1) .main-property-value"))
                .thenReturn(new Elements(new Element("span").text("1000")));
        when(flatElement.select("div.address")).thenReturn(new Elements(new Element("div").text("Musterstraße 1, 12345 Berlin")));
        when(flatElement.select("div.btn-holder > a")).thenReturn(new Elements(new Element("a").attr("href", "/angebot/1")));


        Elements elements = new Elements(flatElement);

        when(restService.fetchAndParseElements(anyString(), anyString())).thenReturn(elements);

        Map<Long, Flat> flats = wbmProvider.fetchFlats();

        assertEquals(1, flats.size());
        Flat flat = flats.values().iterator().next();
        assertEquals(2.0, flat.getRooms());
        assertEquals(50.0, flat.getSize());
        assertEquals(1000.0, flat.getPrice());
        assertEquals("Musterstraße 1, 12345 Berlin", flat.getAddress());
        assertEquals("https://www.wbm.de/angebot/1", flat.getLink());
    }

    @Test
    void fetchFlats_EmptyElements_ReturnsEmptyMap() {
        when(restService.fetchAndParseElements(anyString(), anyString())).thenReturn(new Elements());

        Map<Long, Flat> flats = wbmProvider.fetchFlats();
        assertNotNull(flats);
        assertTrue(flats.isEmpty());
    }

    @Test
    void parseId_ReturnsSafeHash() {
        Element element = mock(Element.class);
        when(element.select("div.btn-holder > a")).thenReturn(new Elements(new Element("a").attr("href", "/angebot/1")));

        Long id = wbmProvider.parseId(element);
        assertNotNull(id);
    }

    @Test
    void buildFlat_MapsElementToFlat() {
        Element element = mock(Element.class);

        when(element.select("ul.main-property-list li:nth-child(3) .main-property-value"))
                .thenReturn(new Elements(new Element("span").text("2")));
        when(element.select("ul.main-property-list li:nth-child(2) .main-property-value"))
                .thenReturn(new Elements(new Element("span").text("50")));
        when(element.select("ul.main-property-list li:nth-child(1) .main-property-value"))
                .thenReturn(new Elements(new Element("span").text("1000")));
        when(element.select("div.address")).thenReturn(new Elements(new Element("div").text("Musterstraße 1, 12345 Berlin")));
        when(element.select("div.btn-holder > a")).thenReturn(new Elements(new Element("a").attr("href", "/angebot/1")));

        Flat flat = wbmProvider.buildFlat(element);

        assertEquals(2.0, flat.getRooms());
        assertEquals(50.0, flat.getSize());
        assertEquals(1000.0, flat.getPrice());
        assertEquals("Musterstraße 1, 12345 Berlin", flat.getAddress());
        assertEquals("https://www.wbm.de/angebot/1", flat.getLink());
    }
}
