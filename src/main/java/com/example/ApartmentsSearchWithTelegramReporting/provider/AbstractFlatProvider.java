package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Log4j2
public abstract class AbstractFlatProvider {

    protected final RestService restService;

    protected AbstractFlatProvider(RestService restService, String s) {
        this.restService = restService;
        log.info("Service for monitoring: {} is created.", s);
    }

    protected AbstractFlatProvider(RestService restService) {
        this.restService = restService;
    }

    public Map<Long, Flat> fetchFlats() {
        Map<Long, Flat> flats = new HashMap<>();
        Elements elements = loadFlats();
        for (Element flat : elements) {
            Long id = parseId(flat);
            flats.put(id, buildFlat(flat));
        }
        return flats;
    }

    protected abstract Elements loadFlats();

    protected abstract Flat buildFlat(Element flat);

    protected abstract Long parseId(Element flat);

    protected Long safeHash(String input) {
        long hash = input.hashCode();
        return hash < 0 ? -hash : hash;
    }
}

