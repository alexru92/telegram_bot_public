package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "provider.inberwohn.enabled", havingValue = "true", matchIfMissing = true)
@Log4j2
public class InBerlinWohnenProvider extends AbstractFlatProvider {

    private static final String URL = "https://inberlinwohnen.de/";

    public InBerlinWohnenProvider(RestService restService) {
        super(restService, URL);
    }

    @Override
    protected Elements loadFlats() {
        try {
            return restService.fetchAndParseElements(URL + "wohnungsfinder/", "li.tb-merkflat");
        } catch (Exception e) {
            log.error("Error fetching InBerlinWohnen flats", e);
            return new Elements();
        }
    }

    @Override
    protected Flat buildFlat(Element flat) {
        Flat result = new Flat();

        result.setRooms("1");
        result.setSize("50");
        result.setPrice("1000");
        result.setAddress("Dummy Address");
        result.setLink(URL + flat.select("a.org-but").attr("href"));

        String priceAndAddress = "Price and Address: " +
                flat.select("h3 > span > span._tb_left").text();
        return result;
    }

    @Override
    protected Long parseId(Element flat) {
        return Long.valueOf(flat.id().replace("flat_", ""));
    }
}

