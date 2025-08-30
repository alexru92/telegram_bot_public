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
@ConditionalOnProperty(name = "provider.buwog.enabled", havingValue = "true", matchIfMissing = true)
@Log4j2
public class BuwogProvider extends AbstractFlatProvider{

    private static final String URL =
            "https://www.buwog-immobilientreuhand.de/property-search?property_search[result_count_only]=false&" +
                    "property_search[marketing_type]=rent&property_search[property_type]=flat&" +
                    "property_search[zip_code]=10627,10585,10587,10315,10245,10247,13585,12619,10115,14167";

    protected BuwogProvider(RestService restService) {
        super(restService, "https://www.buwog-immobilientreuhand.de/");
    }

    @Override
    protected Elements loadFlats() {
        Elements flats = new Elements();
        try {
            JsonNode immoObjects = restService.fetchJson(URL, "real_estate_result");
            if (immoObjects == null) return flats;

            for (JsonNode flatNode : immoObjects) {
                Element flat = new Element("div");
                flat.attr("data-id", flatNode.path("real_estate_id").asText());
                flat.attr("title", flatNode.path("title").asText());
                flat.attr("rooms", flatNode.path("total_rooms").asText());
                flat.attr("price", flatNode.path("rent_price").asText());
                flat.attr("sqare", flatNode.path("total_size").asText());

                String citySlug = flatNode.path("city").asText().toLowerCase().replace(" ", "-");
                String realEstateId = flatNode.path("real_estate_id").asText();
                String url = String.format("https://www.buwog-immobilientreuhand.de/immobilien/%s-wohnung-%s", citySlug, realEstateId);
                flat.attr("details_url", url);

                String address = flatNode.path("street").asText() + " " +
                        flatNode.path("house_number").asText() + ", " +
                        flatNode.path("zip_code").asText() + " " +
                        flatNode.path("city").asText();
                flat.attr("address", address);

                flats.add(flat);
            }
        } catch (Exception e) {
            log.error("Error fetching Buwog flats", e);
        }
        return flats;
    }

    @Override
    protected Flat buildFlat(Element flat) {
        Flat result = new Flat();

        result.setRooms(flat.attr("rooms"));
        result.setSize(flat.attr("sqare"));
        result.setPrice(flat.attr("price"));
        result.setAddress(flat.attr("address"));
        result.setLink(flat.attr("details_url"));

        return result;
    }

    @Override
    protected Long parseId(Element flat) {
        String idStr = flat.attr("data-id");
        if (!idStr.isEmpty()) {
            String numeric = idStr.replaceAll("\\D", "");
            try {
                return Long.parseLong(numeric);
            } catch (NumberFormatException e) {
                long hash = idStr.hashCode();
                return hash < 0 ? -hash : hash;
            }
        } else {
            long hash = (flat.attr("title") + flat.attr("address")).hashCode();
            return hash < 0 ? -hash : hash;
        }
    }
}
