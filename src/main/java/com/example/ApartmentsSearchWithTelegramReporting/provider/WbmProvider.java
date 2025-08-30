package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "provider.wbm.enabled", havingValue = "true", matchIfMissing = true)
@Log4j2
public class WbmProvider extends AbstractFlatProvider{

    private static final String URL = "https://www.wbm.de/wohnungen-berlin/angebote/";

    public WbmProvider(RestService restService) {
        super(restService, "https://www.wbm.de/");
    }

    @Override
    protected Elements loadFlats() {
        try {
            return restService.fetchAndParseElements(URL, "article > div.textWrap");
        } catch (Exception e) {
            log.error("Error fetching WBM flats", e);
            return new Elements();
        }
    }

    @Override
    protected Flat buildFlat(Element flat) {
        Flat result = new Flat();

        result.setRooms(flat.select("ul.main-property-list li:nth-child(3) .main-property-value").text());
        result.setSize(flat.select("ul.main-property-list li:nth-child(2) .main-property-value").text());
        result.setPrice(flat.select("ul.main-property-list li:nth-child(1) .main-property-value").text());
        result.setAddress(flat.select("div.address").text());
        result.setLink("https://www.wbm.de" + flat.select("div.btn-holder > a").attr("href"));

        return result;
    }

    @Override
    protected Long parseId(Element flat) {
        return safeHash(flat.select("div.btn-holder > a").attr("href"));
    }
}
