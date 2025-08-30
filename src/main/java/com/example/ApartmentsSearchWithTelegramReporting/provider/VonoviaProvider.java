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
@ConditionalOnProperty(name = "provider.vonovia.enabled", havingValue = "true", matchIfMissing = true)
@Log4j2
public class VonoviaProvider extends AbstractFlatProvider {

    private static final String URL =
            "https://www.wohnraumkarte.de/api/getImmoList?limit=100&geoLocation=1&rentType=miete&city=Berlin&immoType=wohnung";

    public VonoviaProvider(RestService restService) {
        super(restService, "https://www.vonovia.de/");
    }

    @Override
    protected Elements loadFlats() {
        Elements flats = new Elements();
        try {
            JsonNode immoObjects = restService.fetchJson(URL, "results");
            if (immoObjects == null) return flats;

            for (JsonNode flatNode : immoObjects) {
                Element flat = new Element("div");
                flat.attr("data-id", flatNode.path("wrk_id").asText());
                flat.attr("data-slug", flatNode.path("slug").asText());
                flat.attr("rooms", flatNode.path("anzahl_zimmer").asText());
                flat.attr("price", flatNode.path("preis").asText());
                flat.attr("sqare", flatNode.path("groesse").asText());
                flat.attr("address", flatNode.path("strasse").asText() + ", " +
                        flatNode.path("plz").asText() + " " + flatNode.path("ort").asText());
                flats.add(flat);
            }
        } catch (Exception e) {
            log.error("Error fetching Vonovia flats", e);
        }
        return flats;
    }

    @Override
    protected Flat buildFlat(Element flat) {
        String link = flat.attr("data-slug");
        String id = flat.attr("data-id");
        String url = "https://www.vonovia.de/zuhause-finden/immobilien/" + link + "-" + id;

        Flat result = new Flat();

        result.setRooms(flat.attr("rooms"));
        result.setSize(flat.attr("sqare"));
        result.setPrice(flat.attr("price"));
        result.setAddress(flat.attr("address"));
        result.setLink(url);

        return result;
    }

    @Override
    protected Long parseId(Element flat) {
        String idStr = flat.attr("data-id");
        return idStr.isEmpty() ? safeHash(flat.attr("data-slug") + flat.attr("address")) : Long.valueOf(idStr);
    }
}

