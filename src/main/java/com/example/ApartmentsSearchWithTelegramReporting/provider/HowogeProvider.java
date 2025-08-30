package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "provider.howoge.enabled", havingValue = "true", matchIfMissing = true)
@Log4j2
public class HowogeProvider extends AbstractFlatProvider {

    private static final String URL = "https://www.howoge.de/?type=999&tx_howrealestate_json_list[action]=immoList";

    public HowogeProvider(RestService restService) {
        super(restService, "https://www.howoge.de/");
    }

    @Override
    public Elements loadFlats() {
        Elements flats = new Elements();
        try {
            JsonNode immoObjects = restService.fetchJson(URL, "immoobjects");
            if (immoObjects == null) return flats;

            for (JsonNode flatNode : immoObjects) {
                Element flat = new Element("div");
                flat.attr("data-id", flatNode.path("uid").asText());
                flat.attr("title", flatNode.path("title").asText());
                flat.attr("link", flatNode.path("link").asText());
                flat.attr("price", flatNode.path("rent").asText());
                flat.attr("area", flatNode.path("area").asText());
                flat.attr("rooms", flatNode.path("rooms").asText());
                flats.add(flat);
            }
        } catch (Exception e) {
            log.error("Error fetching HOWOGE flats", e);
        }
        return flats;
    }

    @Override
    public Long parseId(Element flat) {
        try {
            return Long.valueOf(flat.attr("data-id"));
        } catch (NumberFormatException e) {
            long hash = (flat.attr("title") + flat.attr("link")).hashCode();
            return hash < 0 ? -hash : hash;
        }
    }

    @Override
    public Flat buildFlat(Element flat) {
        Flat result = new Flat();

        result.setRooms(flat.attr("rooms"));
        result.setSize(flat.attr("area"));
        result.setPrice(flat.attr("price"));
        result.setAddress(flat.attr("title"));
        result.setLink("https://www.howoge.de" + flat.attr("link"));

        return result;
    }
}
