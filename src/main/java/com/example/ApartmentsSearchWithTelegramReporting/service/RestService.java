package com.example.ApartmentsSearchWithTelegramReporting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import java.net.SocketTimeoutException;
import java.time.Duration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
@Scope("singleton")
@Log4j2
public class RestService {

    private final RestTemplate restTemplate;
    private final Cache<String, Document> documentCache = Caffeine.newBuilder()
            .maximumSize(300)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    private final Cache<String, JsonNode> jsonCache = Caffeine.newBuilder()
            .maximumSize(300)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public RestService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        this.restTemplate = new RestTemplate(factory);
    }

    protected RestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Elements fetchAndParseElements(String url, String cssQuery) {
        Document document = documentCache.get(url, this::loadHtmlDocument);
        if (document == null) {
            log.error("Failed to load document for URL {}", url);
            return new Elements();
        }

        Elements elements = document.select(cssQuery);
        if (elements.isEmpty()) {
            log.warn("No elements found for URL {}", url);
        } else {
            log.info("Found {} elements for URL {}", elements.size(), url);
        }
        return elements;
    }

    private Document loadHtmlDocument(String url) {
        try {
            log.info("Fetching data for {}", url);
            String html = restTemplate.getForObject(url, String.class);
            if (html == null || html.isEmpty()) {
                log.error("Content is empty for {}", url);
                return null;
            }
            return Jsoup.parse(html);
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Timeout while fetching {}", url);
            } else {
                log.error("Resource access error while fetching {}", url);
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP Status Code: {} when fetching {}", e.getStatusCode(), url);
        } catch (RestClientException e) {
            log.error("Error during REST call to {}: {}", url, e.getMessage());
        }
        return null;
    }

    public JsonNode fetchJson(String url, String path) {
        try {
            JsonNode node = jsonCache.get(url, u -> {
                try {
                    log.info("Fetching JSON data for {}", url);
                    String json = restTemplate.getForObject(u, String.class);
                    if (json == null || json.isEmpty()) {
                        log.error("Empty JSON response for {}", u);
                        return null;
                    }
                    return new ObjectMapper().readTree(json);
                } catch (Exception e) {
                    log.error("Error fetching JSON from {}: {}", u, e.getMessage());
                    return null;
                }
            });

            if (node == null) return null;

            JsonNode result = node.path(path);
            if (result.isEmpty()) {
                log.warn("No elements found for URL {}", url);
            } else {
                log.info("Found {} elements for URL {}", result.size(), url);
            }
            return result;
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Timeout while fetching {}", url);
            } else {
                log.error("Resource access error while fetching {}", url);
            }
        } catch (Exception e) {
            log.error("Error fetching JSON from {}: {}", url, e.getMessage());
        }
        return null;
    }

    public void clearCache() {
        documentCache.invalidateAll();
        jsonCache.invalidateAll();
        log.info("Cache cleared");
    }
}
