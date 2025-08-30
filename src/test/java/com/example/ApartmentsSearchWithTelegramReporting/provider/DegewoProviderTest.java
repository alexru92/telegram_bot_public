package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DegewoProviderTest {

    private RestService restService;
    private DegewoProvider degewoProvider;

    @BeforeEach
    void setUp() {
        restService = mock(RestService.class);
        degewoProvider = new DegewoProvider(restService);
    }

    @Test
    void fetchFlats_ReturnsFlatsFromHtml() {
        Elements pagination = new Elements();
        Element pageLink = new Element("a").text("1 2 3");
        pagination.add(pageLink);
        when(restService.fetchAndParseElements("https://immosuche.degewo.de/de/search", "li.pager__page > a"))
                .thenReturn(pagination);

        Elements flatsPage = new Elements();
        Element flatEl = new Element("article");
        flatEl.appendElement("ul").addClass("article__properties")
                .appendElement("li").addClass("article__properties-item").appendElement("span").text("2");
        flatEl.select("ul.article__properties li.article__properties-item > span").get(0).text("2");
        flatEl.appendElement("div").addClass("article__price-tag").appendElement("span").addClass("price").text("1000");
        flatEl.appendElement("span").addClass("article__meta").text("Address | City");
        flatEl.appendElement("a").attr("href", "/flat/123");
        flatEl.appendElement("div").addClass("merken").attr("data-openimmo-bookmark-item-uid", "W123");
        flatsPage.add(flatEl);

        for (int i = 1; i <= 3; i++) {
            when(restService.fetchAndParseElements(
                    "https://immosuche.degewo.de/de/search?size=10&page=" + i,
                    "div.search__results.article-list > article"))
                    .thenReturn(flatsPage);
        }

        Map<Long, Flat> flats = degewoProvider.fetchFlats();

        assertEquals(1, flats.size());
        Flat flat = flats.values().iterator().next();
        assertEquals(2.0, flat.getRooms());
        assertEquals(0.0, flat.getSize());
        assertEquals(1000.0, flat.getPrice());
        assertEquals("Address, City", flat.getAddress());
        assertEquals("https://degewo.de/flat/123", flat.getLink());
        assertEquals(123L, flats.keySet().iterator().next());
    }

    @Test
    void fetchFlats_EmptyPagination_ReturnsEmpty() {
        when(restService.fetchAndParseElements(anyString(), anyString()))
                .thenReturn(new Elements());

        Map<Long, Flat> flats = degewoProvider.fetchFlats();

        assertTrue(flats.isEmpty());
    }

    @Test
    void parseId_FallbackHash() {
        Element flatEl = new Element("article");
        flatEl.text("Sample Flat");
        flatEl.appendElement("a").attr("href", "/flat/999");
        Long id = degewoProvider.parseId(flatEl);
        assertNotNull(id);
    }
}
