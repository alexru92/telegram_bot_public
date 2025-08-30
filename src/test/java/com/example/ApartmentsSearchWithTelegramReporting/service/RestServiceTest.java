package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestServiceTest {

    private RestTemplate restTemplate;
    private RestService restService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        restService = new RestService(restTemplate);
    }

    @Test
    void fetchAndParseElements_ReturnsElements() {
        String html = "<div class='test'>Hello</div>";
        when(restTemplate.getForObject("http://test.com", String.class)).thenReturn(html);

        Elements elements = restService.fetchAndParseElements("http://test.com", "div.test");
        assertEquals(1, elements.size());
        assertEquals("Hello", elements.get(0).text());
    }

    @Test
    void fetchAndParseElements_EmptyHtml_ReturnsEmptyElements() {
        when(restTemplate.getForObject("http://test.com", String.class)).thenReturn("");

        Elements elements = restService.fetchAndParseElements("http://test.com", "div.test");
        assertNotNull(elements);
        assertTrue(elements.isEmpty());
    }

    @Test
    void fetchAndParseElements_HttpException_ReturnsEmptyElements() {
        when(restTemplate.getForObject("http://test.com", String.class))
                .thenThrow(new ResourceAccessException("timeout", new SocketTimeoutException()));

        Elements elements = restService.fetchAndParseElements("http://test.com", "div.test");
        assertNotNull(elements);
        assertTrue(elements.isEmpty());
    }

    @Test
    void fetchAndParseElements_CacheWorks() {
        String html = "<div class='test'>Hello</div>";
        when(restTemplate.getForObject("http://cache.com", String.class)).thenReturn(html);

        Elements first = restService.fetchAndParseElements("http://cache.com", "div.test");
        Elements second = restService.fetchAndParseElements("http://cache.com", "div.test");

        verify(restTemplate, times(1)).getForObject("http://cache.com", String.class);
        assertEquals(first.size(), second.size());
    }

    @Test
    void fetchJson_ReturnsJsonNode() throws Exception {
        String json = "{\"immoobjects\":[{\"id\":1}]}";
        when(restTemplate.getForObject("http://json.com", String.class)).thenReturn(json);

        JsonNode result = restService.fetchJson("http://json.com", "immoobjects");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).get("id").asText());
    }

    @Test
    void fetchJson_EmptyJson_ReturnsNull() {
        when(restTemplate.getForObject("http://json.com", String.class)).thenReturn("");

        JsonNode result = restService.fetchJson("http://json.com", "immoobjects");
        assertNull(result);
    }

    @Test
    void fetchJson_NonexistentPath_ReturnsEmptyNode() throws Exception {
        String json = "{\"other\":[]}";
        when(restTemplate.getForObject("http://json.com", String.class)).thenReturn(json);

        JsonNode result = restService.fetchJson("http://json.com", "immoobjects");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchJson_CacheWorks() throws Exception {
        String json = "{\"immoobjects\":[{\"id\":1}]}";
        when(restTemplate.getForObject("http://json.com", String.class)).thenReturn(json);

        JsonNode first = restService.fetchJson("http://json.com", "immoobjects");
        JsonNode second = restService.fetchJson("http://json.com", "immoobjects");

        verify(restTemplate, times(1)).getForObject("http://json.com", String.class);
        assertEquals(first, second);
    }
}
